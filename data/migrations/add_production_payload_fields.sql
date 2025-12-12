-- Migration: Extend journeys schema with production payload fields
-- Run after mock_tms base schema + add_journeys_display_fields.sql

SET search_path TO mock_tms;

-- ===== ENUMS =====
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'journey_direction_enum') THEN
    CREATE TYPE journey_direction_enum AS ENUM ('outbound', 'return');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'freight_rate_unit_enum') THEN
    CREATE TYPE freight_rate_unit_enum AS ENUM ('per_trip', 'per_km', 'per_ton', 'per_unit', 'other');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'journey_stop_type_enum') THEN
    CREATE TYPE journey_stop_type_enum AS ENUM ('origin', 'destination', 'waypoint');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'journey_alert_stage_enum') THEN
    CREATE TYPE journey_alert_stage_enum AS ENUM ('leg', 'stop', 'pre_transit');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'journey_alert_status_enum') THEN
    CREATE TYPE journey_alert_status_enum AS ENUM ('IN_PROGRESS', 'CLOSED');
  END IF;
END$$;

-- ===== JOURNEYS TABLE EXTENSIONS =====
ALTER TABLE journeys
  ADD COLUMN IF NOT EXISTS direction journey_direction_enum,
  ADD COLUMN IF NOT EXISTS movement_subtype TEXT,
  ADD COLUMN IF NOT EXISTS movement_channel TEXT,
  ADD COLUMN IF NOT EXISTS expected_transit_time_minutes INTEGER,
  ADD COLUMN IF NOT EXISTS planned_distance_km NUMERIC(12,3),
  ADD COLUMN IF NOT EXISTS actual_distance_km NUMERIC(12,3),
  ADD COLUMN IF NOT EXISTS distance_covered_km NUMERIC(12,3),
  ADD COLUMN IF NOT EXISTS avg_distance_km NUMERIC(12,3),
  ADD COLUMN IF NOT EXISTS time_to_destination_min INTEGER,
  ADD COLUMN IF NOT EXISTS delay_minutes INTEGER,
  ADD COLUMN IF NOT EXISTS last_ping_at TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS tracking_signal_strength NUMERIC(6,3),
  ADD COLUMN IF NOT EXISTS transit_sla_hours NUMERIC(8,3),
  ADD COLUMN IF NOT EXISTS is_round_trip BOOLEAN,
  ADD COLUMN IF NOT EXISTS freight_value NUMERIC(14,2),
  ADD COLUMN IF NOT EXISTS freight_value_currency TEXT,
  ADD COLUMN IF NOT EXISTS freight_rate NUMERIC(14,2),
  ADD COLUMN IF NOT EXISTS freight_rate_unit freight_rate_unit_enum,
  ADD COLUMN IF NOT EXISTS negotiated_freight_value NUMERIC(14,2),
  ADD COLUMN IF NOT EXISTS contracted_freight_value NUMERIC(14,2),
  ADD COLUMN IF NOT EXISTS sales_rep_id TEXT,
  ADD COLUMN IF NOT EXISTS indent_external_id TEXT,
  ADD COLUMN IF NOT EXISTS planning_external_id TEXT,
  ADD COLUMN IF NOT EXISTS trip_type_code TEXT,
  ADD COLUMN IF NOT EXISTS vehicle_type_ref_code TEXT,
  ADD COLUMN IF NOT EXISTS vehicle_assigned_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_journeys_direction ON journeys(direction);
CREATE INDEX IF NOT EXISTS idx_journeys_trip_type_code ON journeys(trip_type_code);

-- ===== JOURNEY LOADS =====
CREATE TABLE IF NOT EXISTS journey_loads (
  journey_load_id BIGSERIAL PRIMARY KEY,
  journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
  billing_type TEXT CHECK (billing_type IN ('to pay','to bill','unknown')) DEFAULT 'unknown',
  order_type TEXT,
  yard_entry_number TEXT,
  tare_weight_tons NUMERIC(12,3),
  net_weight_tons NUMERIC(12,3),
  gross_weight_tons NUMERIC(12,3),
  weight_unit TEXT DEFAULT 'TONS',
  erp_transit_distance_km NUMERIC(12,3),
  expected_transit_time_minutes INTEGER,
  freight_value NUMERIC(14,2),
  freight_value_currency TEXT,
  shipment_numbers TEXT[],
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_journey_loads_journey ON journey_loads(journey_id);

-- ===== JOURNEY LCU (Last Mile / Consignment Unit) =====
CREATE TABLE IF NOT EXISTS journey_lcus (
  journey_lcu_id BIGSERIAL PRIMARY KEY,
  journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
  vehicle_type_ref_code TEXT,
  vehicle_assigned_at TIMESTAMPTZ,
  negotiated_freight_value NUMERIC(14,2),
  contracted_freight_value NUMERIC(14,2),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_journey_lcus_journey ON journey_lcus(journey_id);

-- ===== JOURNEY DRIVERS =====
CREATE TABLE IF NOT EXISTS journey_drivers (
  journey_driver_id BIGSERIAL PRIMARY KEY,
  journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
  driver_name TEXT,
  driver_phone TEXT,
  license_number TEXT,
  lcu_ids INTEGER[],
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_journey_drivers_journey ON journey_drivers(journey_id);

-- ===== JOURNEY STOPS =====
CREATE TABLE IF NOT EXISTS journey_stops (
  journey_stop_id BIGSERIAL PRIMARY KEY,
  journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
  external_stop_id TEXT,
  leg_id TEXT,
  stop_type journey_stop_type_enum,
  stop_actions TEXT[],
  branch_name TEXT,
  company_name TEXT,
  display_name TEXT,
  branch_address TEXT,
  latitude NUMERIC(10,6),
  longitude NUMERIC(10,6),
  gate_in_at TIMESTAMPTZ,
  gate_out_at TIMESTAMPTZ,
  sta TIMESTAMPTZ,
  std TIMESTAMPTZ,
  scheduled_travel_time_min INTEGER,
  estimated_travel_time_min INTEGER,
  halt_time_min INTEGER,
  processing_time_min INTEGER,
  standard_travel_km NUMERIC(12,3),
  avg_speed_kmph NUMERIC(8,3),
  distance_travelled_km NUMERIC(12,3),
  total_time_travelled_hours NUMERIC(10,3),
  leg_state TEXT,
  is_active_stop BOOLEAN,
  is_achieved BOOLEAN,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_journey_stops_journey ON journey_stops(journey_id);

-- ===== JOURNEY ALERTS =====
CREATE TABLE IF NOT EXISTS journey_alerts (
  alert_id UUID PRIMARY KEY,
  journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
  alert_name TEXT CHECK (alert_name IN ('long_stoppage','route_deviation','transit_delay','sta_breach','continuous')),
  alert_display_text TEXT,
  alert_status journey_alert_status_enum NOT NULL,
  alert_stage_type journey_alert_stage_enum,
  alert_stage_id TEXT,
  alert_start_time TIMESTAMPTZ,
  alert_close_time TIMESTAMPTZ,
  alert_duration_minutes INTEGER,
  alert_start_location TEXT,
  alert_current_location TEXT,
  alert_visualization_type TEXT,
  is_pre_transit BOOLEAN DEFAULT FALSE,
  metadata JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_journey_alerts_journey ON journey_alerts(journey_id);
CREATE INDEX IF NOT EXISTS idx_journey_alerts_active ON journey_alerts(journey_id, alert_status);

-- denormalized latest alert columns on journeys for quick lookup
ALTER TABLE journeys
  ADD COLUMN IF NOT EXISTS current_alert_id UUID REFERENCES journey_alerts(alert_id),
  ADD COLUMN IF NOT EXISTS current_alert_display TEXT;

-- ===== JOURNEY SHIPMENTS (optional breakout) =====
CREATE TABLE IF NOT EXISTS journey_shipments (
  journey_shipment_id BIGSERIAL PRIMARY KEY,
  journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
  shipment_number TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_journey_shipments_unique ON journey_shipments(journey_id, shipment_number);

-- ===== COMMENTS =====
COMMENT ON TABLE journey_alerts IS 'Normalized alert feed data linked to journeys';
COMMENT ON TABLE journey_stops IS 'Detailed stop/leg level metrics from production payload';
COMMENT ON TABLE journey_loads IS 'Load level commercial and weight information';
COMMENT ON TABLE journey_lcus IS 'Last-mile / consignment unit level metadata';
COMMENT ON TABLE journey_shipments IS 'Unique shipment identifiers associated with a journey';

-- ===== BACKFILL GUIDANCE =====
-- Populate new columns using ETL/service layer. Examples:
-- UPDATE journeys SET direction = 'outbound' WHERE direction IS NULL AND journey_direction = 'outbound';
-- INSERT INTO journey_alerts (...) SELECT ... FROM raw_alert_feed ...;

