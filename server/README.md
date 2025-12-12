# FT Demo Backend Server

Express.js API server for connecting the My Journeys page to PostgreSQL database.

## Setup

1. **Install dependencies:**
```bash
cd server
npm install
```

2. **Configure database connection:**
Create a `.env` file in the `server` directory:
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=freight_tiger
DB_USER=postgres
DB_PASSWORD=postgres
DB_SCHEMA=mock_tms
PORT=3001
```

3. **Run database migrations:**
```bash
psql -U postgres -d freight_tiger -f ../data/migrations/add_journeys_display_fields.sql
```

4. **Start the server:**
```bash
npm start
# or for development with auto-reload:
npm run dev
```

The server will run on `http://localhost:3001`

## API Endpoints

### GET /api/health
Health check endpoint

### GET /api/journeys
Get journeys with optional filters:
- `tab_status` - Filter by tab status (planned, in_transit, etc.)
- `alert_type` - Filter by alert type
- `origin_location` - Filter by origin location
- `date_from` - Filter by start date (ISO format)
- `date_to` - Filter by end date (ISO format)
- `journey_type` - Filter by trip type (SIM, GPS, Fastag)
- `search` - Search across multiple fields
- `limit` - Results per page (default: 50)
- `offset` - Pagination offset (default: 0)

### GET /api/journeys/:id
Get a single journey by ID

### GET /api/journeys/counts/by-status
Get journey counts grouped by tab status

### GET /api/journeys/counts/alerts
Get alert counts grouped by alert type

## Database Requirements

Make sure you have:
1. PostgreSQL installed and running
2. Database created: `freight_tiger`
3. Schema created: `mock_tms`
4. Migration script run: `add_journeys_display_fields.sql`

