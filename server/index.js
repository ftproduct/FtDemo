const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(express.json());

// PostgreSQL connection pool
const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 5432,
  database: process.env.DB_NAME || 'freight_tiger',
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD || 'postgres',
  schema: process.env.DB_SCHEMA || 'mock_tms',
});

// Test database connection
pool.on('connect', () => {
  console.log('Connected to PostgreSQL database');
});

pool.on('error', (err) => {
  console.error('Unexpected error on idle client', err);
  process.exit(-1);
});

// Health check endpoint
app.get('/api/health', async (req, res) => {
  try {
    const result = await pool.query('SELECT NOW()');
    res.json({ 
      status: 'ok', 
      database: 'connected',
      timestamp: result.rows[0].now 
    });
  } catch (error) {
    res.status(500).json({ 
      status: 'error', 
      message: error.message 
    });
  }
});

// Get all journeys with filters
app.get('/api/journeys', async (req, res) => {
  try {
    const {
      tab_status,
      alert_type,
      origin_location,
      date_from,
      date_to,
      journey_type,
      search,
      limit = 50,
      offset = 0
    } = req.query;

    let query = `
      SELECT 
        journey_id,
        feed_unique_id,
        trip_id,
        status_display,
        current_location_display,
        sla_status,
        delay_hours,
        CASE 
          WHEN sla_status = 'delayed' AND delay_hours IS NOT NULL 
          THEN 'Delayed by ' || ROUND(delay_hours) || ' hr'
          ELSE 'On time'
        END AS sla_status_display,
        alert_type,
        alert_time,
        CASE 
          WHEN alert_time IS NOT NULL 
          THEN ROUND(EXTRACT(EPOCH FROM (NOW() - alert_time)) / 3600) || ' hour ago'
          ELSE NULL
        END AS alert_time_display,
        eta_display,
        tab_status,
        origin_display,
        origin_company_display,
        destination_display,
        destination_company_display,
        vehicle_number,
        transporter_name,
        trip_type_display,
        start_time,
        eta,
        actual_arrival
      FROM journeys_display_view
      WHERE 1=1
    `;

    const params = [];
    let paramCount = 1;

    if (tab_status) {
      query += ` AND tab_status = $${paramCount}`;
      params.push(tab_status);
      paramCount++;
    }

    if (alert_type) {
      query += ` AND alert_type = $${paramCount}`;
      params.push(alert_type);
      paramCount++;
    }

    if (origin_location) {
      query += ` AND origin_display ILIKE $${paramCount}`;
      params.push(`%${origin_location}%`);
      paramCount++;
    }

    if (date_from) {
      query += ` AND start_time >= $${paramCount}`;
      params.push(date_from);
      paramCount++;
    }

    if (date_to) {
      query += ` AND start_time <= $${paramCount}`;
      params.push(date_to);
      paramCount++;
    }

    if (journey_type) {
      query += ` AND trip_type_display = $${paramCount}`;
      params.push(journey_type);
      paramCount++;
    }

    if (search) {
      query += ` AND (
        feed_unique_id ILIKE $${paramCount} OR
        trip_id ILIKE $${paramCount} OR
        origin_display ILIKE $${paramCount} OR
        destination_display ILIKE $${paramCount} OR
        vehicle_number ILIKE $${paramCount} OR
        transporter_name ILIKE $${paramCount}
      )`;
      params.push(`%${search}%`);
      paramCount++;
    }

    query += ` ORDER BY start_time DESC LIMIT $${paramCount} OFFSET $${paramCount + 1}`;
    params.push(parseInt(limit), parseInt(offset));
    paramCount += 2;

    const result = await pool.query(query, params);

    // Get total count for pagination
    let countQuery = `
      SELECT COUNT(*) as total
      FROM journeys_display_view
      WHERE 1=1
    `;
    const countParams = [];
    let countParamCount = 1;

    if (tab_status) {
      countQuery += ` AND tab_status = $${countParamCount}`;
      countParams.push(tab_status);
      countParamCount++;
    }

    if (alert_type) {
      countQuery += ` AND alert_type = $${countParamCount}`;
      countParams.push(alert_type);
      countParamCount++;
    }

    if (origin_location) {
      countQuery += ` AND origin_display ILIKE $${countParamCount}`;
      countParams.push(`%${origin_location}%`);
      countParamCount++;
    }

    if (date_from) {
      countQuery += ` AND start_time >= $${countParamCount}`;
      countParams.push(date_from);
      countParamCount++;
    }

    if (date_to) {
      countQuery += ` AND start_time <= $${countParamCount}`;
      countParams.push(date_to);
      countParamCount++;
    }

    if (journey_type) {
      countQuery += ` AND trip_type_display = $${countParamCount}`;
      countParams.push(journey_type);
      countParamCount++;
    }

    if (search) {
      countQuery += ` AND (
        feed_unique_id ILIKE $${countParamCount} OR
        trip_id ILIKE $${countParamCount} OR
        origin_display ILIKE $${countParamCount} OR
        destination_display ILIKE $${countParamCount} OR
        vehicle_number ILIKE $${countParamCount} OR
        transporter_name ILIKE $${countParamCount}
      )`;
      countParams.push(`%${search}%`);
      countParamCount++;
    }

    const countResult = await pool.query(countQuery, countParams);
    const total = parseInt(countResult.rows[0].total);

    res.json({
      journeys: result.rows,
      pagination: {
        total,
        limit: parseInt(limit),
        offset: parseInt(offset),
        hasMore: offset + limit < total
      }
    });
  } catch (error) {
    console.error('Error fetching journeys:', error);
    res.status(500).json({ 
      error: 'Failed to fetch journeys',
      message: error.message 
    });
  }
});

// Get journey by ID
app.get('/api/journeys/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const result = await pool.query(
      'SELECT * FROM journeys_display_view WHERE journey_id = $1',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Journey not found' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching journey:', error);
    res.status(500).json({ 
      error: 'Failed to fetch journey',
      message: error.message 
    });
  }
});

// Get journey counts by tab status
app.get('/api/journeys/counts/by-status', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        tab_status,
        COUNT(*) as count
      FROM journeys_display_view
      GROUP BY tab_status
      ORDER BY 
        CASE tab_status
          WHEN 'planned' THEN 1
          WHEN 'en_route_to_loading' THEN 2
          WHEN 'at_loading' THEN 3
          WHEN 'in_transit' THEN 4
          WHEN 'at_unloading' THEN 5
          WHEN 'in_return' THEN 6
          WHEN 'delivered' THEN 7
          ELSE 8
        END
    `);

    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching journey counts:', error);
    res.status(500).json({ 
      error: 'Failed to fetch journey counts',
      message: error.message 
    });
  }
});

// Get alert counts
app.get('/api/journeys/counts/alerts', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        alert_type,
        COUNT(*) as count
      FROM journeys_display_view
      WHERE alert_type IS NOT NULL
      GROUP BY alert_type
    `);

    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching alert counts:', error);
    res.status(500).json({ 
      error: 'Failed to fetch alert counts',
      message: error.message 
    });
  }
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
  console.log(`API endpoints available at http://localhost:${PORT}/api`);
});

