-- Migration: Add display fields for My Journeys page
-- Run this script to add missing data points to the journeys table

SET search_path TO mock_tms;

-- Add missing columns to journeys table
ALTER TABLE journeys
  ADD COLUMN IF NOT EXISTS feed_unique_id TEXT,
  ADD COLUMN IF NOT EXISTS alert_type TEXT CHECK (alert_type IN ('long_stoppage', 'route_deviation', 'transit_delay', 'e_waybill_expiring', 'e_waybill_expired', 'eta_approaching')),
  ADD COLUMN IF NOT EXISTS alert_time TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS sla_status TEXT CHECK (sla_status IN ('on_time', 'delayed')) DEFAULT 'on_time',
  ADD COLUMN IF NOT EXISTS delay_hours NUMERIC(5,2),
  ADD COLUMN IF NOT EXISTS current_location_display TEXT,
  ADD COLUMN IF NOT EXISTS status_display TEXT,
  ADD COLUMN IF NOT EXISTS trip_type_display TEXT,
  ADD COLUMN IF NOT EXISTS trip_id TEXT,
  ADD COLUMN IF NOT EXISTS eta_display TEXT,
  ADD COLUMN IF NOT EXISTS tab_status TEXT CHECK (tab_status IN ('planned', 'en_route_to_loading', 'at_loading', 'in_transit', 'at_unloading', 'in_return', 'delivered'));

-- Create index on feed_unique_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_journeys_feed_unique_id ON journeys(feed_unique_id);

-- Create index on tab_status for filtering
CREATE INDEX IF NOT EXISTS idx_journeys_tab_status ON journeys(tab_status);

-- Create index on alert_type for alert filtering
CREATE INDEX IF NOT EXISTS idx_journeys_alert_type ON journeys(alert_type);

-- Create view for easy querying with all display fields
CREATE OR REPLACE VIEW journeys_display_view AS
SELECT 
  j.journey_id,
  j.feed_unique_id,
  j.trip_id,
  j.status_display,
  j.current_location_display,
  j.sla_status,
  j.delay_hours,
  CASE 
    WHEN j.sla_status = 'delayed' AND j.delay_hours IS NOT NULL 
    THEN 'Delayed by ' || ROUND(j.delay_hours) || ' hr'
    ELSE 'On time'
  END AS sla_status_display,
  j.alert_type,
  j.alert_time,
  CASE 
    WHEN j.alert_time IS NOT NULL 
    THEN EXTRACT(EPOCH FROM (NOW() - j.alert_time)) / 3600 || ' hour ago'
    ELSE NULL
  END AS alert_time_display,
  j.eta_display,
  j.tab_status,
  -- Origin info
  COALESCE(origin_loc.city || ', ' || origin_loc.state, '') AS origin_display,
  COALESCE(origin_comp.name, '') AS origin_company_display,
  -- Destination info
  COALESCE(dest_loc.city || ', ' || dest_loc.state, '') AS destination_display,
  COALESCE(dest_comp.name, '') AS destination_company_display,
  -- Vehicle info
  v.vehicle_number,
  COALESCE(transporter_comp.name, '') AS transporter_name,
  -- Trip type
  j.trip_type_display,
  -- Dates
  j.start_time,
  j.eta,
  j.actual_arrival,
  -- Additional useful fields
  j.current_status,
  j.tracking_mode_code,
  fo.fo_id,
  do_orders.do_id
FROM journeys j
JOIN freight_orders fo ON j.fo_id = fo.fo_id
JOIN delivery_orders do_orders ON fo.do_id = do_orders.do_id
LEFT JOIN locations origin_loc ON do_orders.origin_location_id = origin_loc.location_id
LEFT JOIN locations dest_loc ON do_orders.destination_location_id = dest_loc.location_id
LEFT JOIN companies origin_comp ON origin_loc.company_id = origin_comp.company_id
LEFT JOIN companies dest_comp ON dest_loc.company_id = dest_comp.company_id
LEFT JOIN vehicles v ON j.vehicle_id = v.vehicle_id
LEFT JOIN companies transporter_comp ON v.transporter_id = transporter_comp.company_id;

-- Grant permissions (adjust as needed)
-- GRANT SELECT ON journeys_display_view TO your_app_user;

