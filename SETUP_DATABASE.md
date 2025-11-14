# Database Setup Guide for My Journeys Page

This guide will help you connect the My Journeys page to your PostgreSQL database.

## Prerequisites

1. PostgreSQL installed and running
2. Node.js and npm installed
3. Database created (e.g., `freight_tiger`)

## Step 1: Database Setup

### 1.1 Create Database (if not exists)
```bash
createdb freight_tiger
# or using psql:
psql -U postgres
CREATE DATABASE freight_tiger;
```

### 1.2 Run Schema Script
```bash
psql -U postgres -d freight_tiger -f data/mock_tms_schema.sql
```

### 1.3 Run Migration Script
```bash
psql -U postgres -d freight_tiger -f data/migrations/add_journeys_display_fields.sql
```

### 1.4 (Optional) Seed Sample Data
```bash
psql -U postgres -d freight_tiger -f data/mock_tms_seed.sql
```

## Step 2: Backend Server Setup

### 2.1 Install Backend Dependencies
```bash
cd server
npm install
```

### 2.2 Configure Environment Variables
Create `server/.env` file:
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=freight_tiger
DB_USER=postgres
DB_PASSWORD=your_password
DB_SCHEMA=mock_tms
PORT=3001
```

### 2.3 Start Backend Server
```bash
npm start
# or for development:
npm run dev
```

The server will run on `http://localhost:3001`

## Step 3: Frontend Configuration

### 3.1 Set API URL (Optional)
Create `.env` file in project root:
```env
VITE_API_URL=http://localhost:3001/api
```

If not set, it defaults to `http://localhost:3001/api`

### 3.2 Start Frontend
```bash
npm run dev
```

## Step 4: Populate Missing Data Points

After running the migration, you need to populate the new fields in your `journeys` table. Here's a sample SQL to update existing journeys:

```sql
-- Update feed_unique_id (generate from journey_id if not exists)
UPDATE journeys 
SET feed_unique_id = 'JRN-' || journey_id::TEXT || '-' || SUBSTRING(MD5(RANDOM()::TEXT) FROM 1 FOR 8)
WHERE feed_unique_id IS NULL;

-- Update trip_id (generate if not exists)
UPDATE journeys 
SET trip_id = 'TRIP-' || journey_id::TEXT
WHERE trip_id IS NULL;

-- Update status_display based on current_status
UPDATE journeys 
SET status_display = CASE current_status
  WHEN 'pending_start' THEN 'Planned'
  WHEN 'en_route' THEN 'On Road'
  WHEN 'arrived' THEN 'At Drop'
  WHEN 'unloaded' THEN 'At Unloading'
  WHEN 'completed' THEN 'Delivered'
  ELSE current_status::TEXT
END
WHERE status_display IS NULL;

-- Update trip_type_display based on tracking_mode_code
UPDATE journeys 
SET trip_type_display = CASE tracking_mode_code
  WHEN 'driver_sim' THEN 'SIM'
  WHEN 'ft_driver_app' THEN 'GPS'
  WHEN 'vehicle_gps' THEN 'GPS'
  WHEN 'fastag' THEN 'Fastag'
  ELSE 'Unknown'
END
WHERE trip_type_display IS NULL;

-- Update tab_status based on current_status
UPDATE journeys 
SET tab_status = CASE current_status
  WHEN 'pending_start' THEN 'planned'
  WHEN 'en_route' THEN 'in_transit'
  WHEN 'arrived' THEN 'at_unloading'
  WHEN 'unloaded' THEN 'delivered'
  WHEN 'completed' THEN 'delivered'
  ELSE 'planned'
END
WHERE tab_status IS NULL;

-- Update current_location_display (you'll need to populate this based on your location data)
UPDATE journeys 
SET current_location_display = COALESCE(
  (SELECT city || ', ' || state 
   FROM locations 
   WHERE location_id IN (
     SELECT origin_location_id FROM delivery_orders WHERE do_id IN (
       SELECT do_id FROM freight_orders WHERE fo_id = journeys.fo_id
     )
   )
   LIMIT 1),
  'Unknown Location'
)
WHERE current_location_display IS NULL;

-- Update eta_display (format from eta timestamp)
UPDATE journeys 
SET eta_display = CASE 
  WHEN eta IS NOT NULL THEN 'ETA: ' || TO_CHAR(eta, 'HH12:MI am, DD Mon')
  ELSE 'TBD'
END
WHERE eta_display IS NULL;

-- Update sla_status and delay_hours (example logic)
UPDATE journeys 
SET sla_status = CASE 
  WHEN eta IS NOT NULL AND NOW() > eta THEN 'delayed'
  ELSE 'on_time'
END,
delay_hours = CASE 
  WHEN eta IS NOT NULL AND NOW() > eta THEN EXTRACT(EPOCH FROM (NOW() - eta)) / 3600
  ELSE NULL
END
WHERE sla_status IS NULL;
```

## Missing Data Points Summary

The following fields were added to the `journeys` table:

1. **feed_unique_id** - Unique identifier for display
2. **alert_type** - Type of alert (long_stoppage, route_deviation, etc.)
3. **alert_time** - When alert was triggered
4. **sla_status** - On time or delayed
5. **delay_hours** - Hours delayed (if delayed)
6. **current_location_display** - Human-readable location
7. **status_display** - Human-readable status
8. **trip_type_display** - Display name for tracking mode
9. **trip_id** - Trip identifier
10. **eta_display** - Formatted ETA string
11. **tab_status** - Status for tab filtering

## Testing

1. **Test Database Connection:**
```bash
curl http://localhost:3001/api/health
```

2. **Test Journeys Endpoint:**
```bash
curl http://localhost:3001/api/journeys
```

3. **Test with Filters:**
```bash
curl "http://localhost:3001/api/journeys?tab_status=in_transit&limit=10"
```

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running: `pg_isready`
- Check credentials in `server/.env`
- Test connection: `psql -U postgres -d freight_tiger`

### API Not Responding
- Check server logs for errors
- Verify port 3001 is not in use
- Check CORS settings if accessing from different origin

### No Data Showing
- Verify migration script ran successfully
- Check if journeys table has data
- Verify view `journeys_display_view` exists and has data
- Check browser console for API errors

## Next Steps

1. Populate your database with actual journey data
2. Customize alert logic based on your business rules
3. Add filtering and search functionality
4. Implement pagination if needed
5. Add real-time updates if required

