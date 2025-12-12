# Missing Data Points for My Journeys Page

Based on the Figma design and current mock data, here are the data points that need to be added to the PostgreSQL database:

## Required Data Points

The original scope focused on UI display fields. Production payloads provide significantly richer journey metadata. We now need to persist those attributes so the UI (and future reports) can query them natively.

### 1. **Feed Unique ID** (Missing)
- **Field Name**: `feed_unique_id`
- **Type**: `TEXT`
- **Description**: Unique identifier displayed in the "Feed Unique ID" column (e.g., "324673-948B478-84...")
- **Table**: `journeys`
- **Required**: Yes

### 2. **Alert Information** (Missing)
- **Field Name**: `alert_type`
- **Type**: `TEXT` or `ENUM`
- **Description**: Type of alert (e.g., "Long Stoppage", "Route Deviation", "Transit Delay")
- **Table**: `journeys`
- **Possible Values**: 
  - `long_stoppage`
  - `route_deviation`
  - `transit_delay`
  - `e_waybill_expiring`
  - `e_waybill_expired`
  - `eta_approaching`
- **Required**: No (nullable)

- **Field Name**: `alert_time`
- **Type**: `TIMESTAMPTZ`
- **Description**: When the alert was triggered (e.g., "1 hour ago")
- **Table**: `journeys`
- **Required**: No (nullable)

### 3. **SLA Status** (Partially Missing)
- **Field Name**: `sla_status`
- **Type**: `TEXT` or `ENUM`
- **Description**: SLA status display (e.g., "On time", "Delayed by 13 hr")
- **Table**: `journeys`
- **Possible Values**:
  - `on_time`
  - `delayed`
- **Required**: Yes

- **Field Name**: `delay_hours`
- **Type**: `NUMERIC(5,2)`
- **Description**: Number of hours delayed (for delayed status)
- **Table**: `journeys`
- **Required**: No (nullable, only when delayed)

### 4. **Current Location Display** (Missing)
- **Field Name**: `current_location_display`
- **Type**: `TEXT`
- **Description**: Human-readable current location (e.g., "Ambala, Haryana", "at 12:30 pm, 12 Aug")
- **Table**: `journeys`
- **Note**: This can be derived from `last_known_lat`, `last_known_lng`, and `journey_events`, but a display field is useful
- **Required**: Yes

### 5. **Status Display** (Needs Mapping)
- **Field Name**: `status_display`
- **Type**: `TEXT`
- **Description**: Human-readable status (e.g., "On Road", "At Drop", "At Pickup")
- **Table**: `journeys`
- **Mapping from `current_status` enum**:
  - `pending_start` → "Planned"
  - `en_route` → "On Road"
  - `arrived` → "At Drop"
  - `unloaded` → "At Unloading"
  - `completed` → "Delivered"
- **Note**: Can be computed, but a display field is useful
- **Required**: Yes

### 6. **Trip Type Display** (Needs Mapping)
- **Field Name**: `trip_type_display`
- **Type**: `TEXT`
- **Description**: Display name for tracking mode (e.g., "SIM", "GPS", "Fastag")
- **Table**: `journeys`
- **Mapping from `tracking_mode_code`**:
  - `driver_sim` → "SIM"
  - `ft_driver_app` → "GPS"
  - `vehicle_gps` → "GPS"
  - `fastag` → "Fastag"
- **Note**: Can be computed from `tracking_mode_code`, but a display field is useful
- **Required**: Yes

### 7. **Trip ID** (Missing)
- **Field Name**: `trip_id`
- **Type**: `TEXT`
- **Description**: Trip identifier displayed in Trip Info column (e.g., "84973-47593")
- **Table**: `journeys`
- **Required**: Yes

### 8. **ETA Display** (Missing)
- **Field Name**: `eta_display`
- **Type**: `TEXT`
- **Description**: Formatted ETA string (e.g., "ETA: 12:30 pm, 12 Aug", "at 12:30 pm, 12 Aug")
- **Table**: `journeys`
- **Note**: Can be computed from `eta` timestamp, but a display field is useful
- **Required**: Yes

### 9. **Origin/Destination Display** (Needs Enhancement)
- **Current**: `locations` table has address fields
- **Missing**: Formatted display strings for "From" and "To" columns
- **Solution**: Create computed columns or views:
  - `origin_display`: "City, State" format (e.g., "Amritsar, Punjab")
  - `destination_display`: "City, State" format (e.g., "Mumbai, M...")
  - `origin_company_display`: Company name from `companies` table
  - `destination_company_display`: Company name from `companies` table

### 10. **Vehicle Number Display** (Exists but needs formatting)
- **Current**: `vehicles.vehicle_number` exists
- **Missing**: Proper formatting/display
- **Table**: `vehicles`
- **Required**: Format is already correct

### 11. **Transporter Company Display** (Needs Join)
- **Current**: `vehicles.transporter_id` references `companies`
- **Missing**: Easy access to transporter company name
- **Solution**: Use JOIN or create a view

### 12. **Journey Tab Status** (Needs Mapping)
- **Field Name**: `tab_status`
- **Type**: `TEXT` or `ENUM`
- **Description**: Status for tab filtering (e.g., "Planned", "En Route to Loading", "At Loading", "In Transit", "At Unloading", "In Return", "Delivered")
- **Table**: `journeys`
- **Mapping**:
  - `pending_start` → "Planned"
  - `en_route` (before loading) → "En Route to Loading"
  - `en_route` (at origin) → "At Loading"
  - `en_route` (in transit) → "In Transit"
  - `arrived` → "At Unloading"
  - `en_route` (return trip) → "In Return"
  - `completed` → "Delivered"
- **Required**: Yes

## Database Schema Changes Required

### ALTER TABLE journeys
```sql
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
```

## Views Recommended

### journeys_display_view
Create a view that joins all necessary tables and formats display fields:

```sql
CREATE OR REPLACE VIEW journeys_display_view AS
SELECT 
  j.journey_id,
  j.feed_unique_id,
  j.trip_id,
  j.status_display,
  j.current_location_display,
  j.sla_status,
  j.delay_hours,
  j.alert_type,
  j.alert_time,
  j.eta_display,
  j.tab_status,
  -- Origin info
  origin_loc.city || ', ' || origin_loc.state AS origin_display,
  origin_comp.name AS origin_company_display,
  -- Destination info
  dest_loc.city || ', ' || dest_loc.state AS destination_display,
  dest_comp.name AS destination_company_display,
  -- Vehicle info
  v.vehicle_number,
  transporter_comp.name AS transporter_name,
  -- Trip type
  j.trip_type_display,
  -- Dates
  j.start_time,
  j.eta,
  j.actual_arrival
FROM journeys j
JOIN freight_orders fo ON j.fo_id = fo.fo_id
JOIN delivery_orders do ON fo.do_id = do.do_id
JOIN locations origin_loc ON do.origin_location_id = origin_loc.location_id
JOIN locations dest_loc ON do.destination_location_id = dest_loc.location_id
JOIN companies origin_comp ON origin_loc.company_id = origin_comp.company_id
JOIN companies dest_comp ON dest_loc.company_id = dest_comp.company_id
JOIN vehicles v ON j.vehicle_id = v.vehicle_id
JOIN companies transporter_comp ON v.transporter_id = transporter_comp.company_id;
```

## Summary

**Total Missing Fields**: 10 new fields need to be added to `journeys` table
**Views Needed**: 1 view for easy querying
**Computed Fields**: Several fields can be computed from existing data but display fields are recommended for performance

---

## Production Payload Mapping (Journey / Stop / Load / LCU / Alerts)

The following sections map real production payloads to database fields we must add. Field names follow the payload, while table/column suggestions follow FT naming conventions.

### Journey-Level Fields

| Payload Field | Suggested Column | Type | Notes |
| --- | --- | --- | --- |
| `journey_fteid` | `journeys.feed_unique_id` | `TEXT` | Already covered; acts as external identifier |
| `journey_status` | `journeys.current_status` | `journey_status_enum` | Already exists, but ensure value set from payload |
| `journey_direction` | `journeys.direction` | `TEXT CHECK (direction IN ('outbound','return'))` | Needed to differentiate tabs |
| `movement_subtype`, `movement_channel` | `journeys.movement_subtype`, `journeys.movement_channel` | `TEXT` | Enables reporting/filtering |
| `expected_transit_time` + `expected_transit_time_unit` | `journeys.expected_transit_time_minutes` | `INTEGER` | Normalize to minutes |
| `erp_transit_distance` + `erp_transit_distance_unit` | `journeys.planned_distance_km` | `NUMERIC(10,3)` | Normalize to KM |
| `total_journey_distance`, `distance_covered`, `avg_distance_covered` | `journeys.actual_distance_km`, `journeys.distance_covered_km`, `journeys.avg_distance_km` | `NUMERIC(12,3)` | Supports KPI cards |
| `time_to_reach_destination`, `delay_in_minutes` | `journeys.time_to_destination_min`, `journeys.delay_minutes` | `INTEGER` | Delay mirrors SLA fields |
| `created_on`, `ETA`, `STA`, `last_ping` | `journeys.created_at`, `journeys.eta`, `journeys.scheduled_arrival`, `journeys.last_ping_at` | `TIMESTAMPTZ` | Already partially present |
| `last_known_location` | `journeys.current_location_display` | `TEXT` | Already planned |
| `tracking_data.available_source` | `journeys.trip_type_display` | `TEXT` | Already planned; map SIM/FST/FST etc |
| `tracking_data.tracking_strength` | `journeys.tracking_signal_strength` | `NUMERIC(5,2)` | Optional but useful |
| `is_round_trip` | `journeys.is_round_trip` | `BOOLEAN` | Tab logic |
| `transit_sla_in_hrs` | `journeys.transit_sla_hours` | `NUMERIC(6,2)` | Compare with actual |
| `freight_value`, `freight_value_currency`, `freight_rate`, `freight_rate_unit`, `negotiated_freight_value`, `contracted_freight_value` | dedicated numeric + enum columns | | Provide finance reporting |
| `sales_rep_id`, `indent_fteid`, `planning_id` | `journeys.sales_rep_id`, `journeys.indent_external_id`, `journeys.planning_external_id` | `TEXT` | External references |
| `trip_type` | `journeys.trip_type_code` | `TEXT` | Raw value before display mapping |

### Stop-Level Metrics

Production payload exposes stop metrics (gate in/out, STA/STD, dwell times). Recommended approach:

- Create `journey_stops` table keyed by `journey_id` + `stop_id` (payload `stop_id` / `alert_stage_id`).
- Columns:
  - `gate_in_at`, `gate_out_at` (`TIMESTAMPTZ`)
  - `sta`, `std` (`TIMESTAMPTZ`)
  - `scheduled_travel_time_min`, `estimated_travel_time_min` (`INTEGER`)
  - `halt_time_min`, `processing_time_min`, `standard_travel_km`, `avg_speed_kmph`
  - `journey_stop_type` (`origin`, `destination`, `waypoint`)
  - `journey_stop_action` (array or join table for pickup/drop/milestone)

This aligns with payload fields under `journey_summary.forward_leg`, `return_leg`, and `stop`.

### Load / Consignment Unit Fields

Payload sections `load` and `lcu` include:

- `billing_type`, `order_type`, `yard_entry_number`
- `tare_weight`, `net_weight`, `gross_weight`, `weight_unit`
- `shipment_numbers` (array)
- `vehicle_type_ref_code`, `vehicle_assigned_at`
- LCU level negotiated / contracted freight

Recommended storage:

1. Extend `delivery_orders` or create `journey_loads` table keyed by `journey_id`.
2. Add JSONB column for `shipment_numbers` or a join table `journey_shipments`.
3. Numeric columns for weights (store in metric tons) with unit indicator.

### Alert Feed

Production payload returns an `alerts` array per journey. Instead of storing a single alert on `journeys`, create a normalized table:

```sql
CREATE TABLE journey_alerts (
  alert_id UUID PRIMARY KEY,
  journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
  alert_name TEXT CHECK (alert_name IN ('long_stoppage','route_deviation','transit_delay','sta_breach','continuous')),
  alert_display_text TEXT,
  alert_status TEXT CHECK (alert_status IN ('IN_PROGRESS','CLOSED')),
  alert_stage_type TEXT CHECK (alert_stage_type IN ('leg','stop','pre_transit')),
  alert_stage_id TEXT,
  alert_start_time TIMESTAMPTZ,
  alert_close_time TIMESTAMPTZ,
  alert_duration_minutes INTEGER,
  alert_start_location TEXT,
  alert_current_location TEXT,
  alert_visualization_type TEXT,
  is_pre_transit BOOLEAN DEFAULT FALSE,
  metadata JSONB
);
CREATE INDEX idx_journey_alerts_journey ON journey_alerts(journey_id, alert_status);
```

`journeys` can still keep a denormalized `current_alert_type` + `current_alert_time` referencing the latest active alert for quick UI badges.

### Driver / Vehicle / Transporter Blocks

- `vehicle_info.vehicle_number` already joins to `vehicles`.
- Add optional columns for `vehicle_external_id`, `vehicle_assigned_at`.
- `driver_info` is an array; create `journey_drivers` table with `journey_id`, `driver_name`, `driver_phone`, `license_number`, `lcu_ids`.
- `transporter_info` fields map to `companies` table; add fallback columns (`transporter_name`, `transporter_phone`) when no master exists.

### Derived Display Fields

Continue storing formatted strings (`status_display`, `eta_display`, `current_location_display`, `alert_display_text`) so React can simply render them. Populate them when ingesting payloads.

---

## Next Steps

1. **Schema Design** – finalize column names/types listed above plus new tables (`journey_alerts`, `journey_stops`, `journey_drivers`, optional `journey_loads`).
2. **Migration Script** – add columns, create new tables, indexes, constraints.
3. **Ingestion Updates** – ETL/service mapping from payload fields to DB columns (documented in API section).

---

## API / ETL Ingestion Expectations

1. **Source of Truth**
   - Journey payload (`/journeys/:id`) provides core fields.
   - Alert payload (`/journeys/:id/alerts`) supplies alert history.
   - Stop/load/LCU sections live under `journey_summary`, `load`, `lcu` blocks.

2. **Upsert Flow**
   1. Upsert `journeys` row using `journey_fteid` → `journeys.feed_unique_id`.
   2. Insert/update associated rows in `journey_loads`, `journey_lcus`, `journey_drivers`, `journey_stops`, `journey_shipments`.
   3. Upsert alerts into `journey_alerts`. After inserting, set `journeys.current_alert_id` to the latest `IN_PROGRESS` alert (if any) and denormalize `current_alert_display`.

3. **Field Normalization**
   - Convert duration/distance units to canonical columns (minutes, KM, metric tons).
   - Map `tracking_data.available_source` → `trip_type_display`.
   - Derive `tab_status` + `status_display` from `journey_status` + `journey_direction`.
   - Generate formatted strings (`eta_display`, `current_location_display`, `alert_time_display`) before persisting.

4. **Error Handling**
   - Payload arrays may contain null entries; sanitize before insert.
   - Alerts can repeat `alert_id`; use UPSERT with `ON CONFLICT (alert_id) DO UPDATE`.
   - Shipments may be large; deduplicate before inserting into `journey_shipments`.

5. **Backfill Strategy**
   - Run migration.
   - Replay last N days of journey + alert payloads to populate new tables.
   - Verify UI queries via `journeys_display_view` and new tables.
