# Mock TMS Entities & Attributes

## Parties & Hierarchies

### Consignor
- Description: Shipper initiating PO/SO/DO and freight orders.
- Key attributes: `consignor_id`, `name`, `industry`, `erp_code`, `primary_contact_id`.
- Hierarchy: Company → Business Unit → Warehouse/Plant.
- Touchpoints: Generates PO/SO/DO, configures planning constraints, validates ePOD, raises invoices, handles disputes.

### Transporter
- Description: Logistics partner providing vehicles/drivers.
- Key attributes: `transporter_id`, `name`, `scac_code`, `pan_number`, `gst_number`, `primary_contact_id`.
- Hierarchy: Company HQ → Regional Branch → Fleet/Driver pool.
- Touchpoints: Bids on indents, allocates vehicles, tracks journeys, submits ePOD, invoices consignor.

### Consignee
- Description: Receiver of goods; may mirror consignor hierarchy.
- Key attributes: `consignee_id`, `name`, `industry`, `customer_code`, `primary_contact_id`.
- Hierarchy: Company → Region → Delivery location.
- Touchpoints: Issues PO, acknowledges DO receipt, accepts ePOD, confirms disputes.

## Users & Roles

| Party        | Role                | Typical Scope                                       | Attributes |
|--------------|---------------------|----------------------------------------------------|------------|
| Consignor    | CXO / Director      | Multi-BU KPIs, approvals                            | `user_id`, `role`, `company_id`, `email`, `phone`, `password_hash`, `mfa_enabled`, `status` |
| Consignor    | Logistics Head      | Planning configs, FO approvals                      | same as above + `default_warehouse_id` |
| Consignor    | Warehouse Manager   | DO execution, dock scheduling                       | `shift`, `warehouse_id` |
| Consignor    | Logistics Manager   | Daily indenting, journey tracking                   | |
| Transporter  | Transporter Head    | Pricing, fleet allocation, dispute resolution       | |
| Transporter  | Transporter Manager | Bidding, driver assignment                          | |
| Transporter  | Driver              | Journey execution, ePOD capture                     | `driver_license`, `device_id`, `consent_timestamp` |
| Consignee    | Regional Head       | Receiving SLAs                                      | |
| Consignee    | Warehouse Manager   | Delivery confirmation, ePOD validation              | |

Common user attributes: `name`, `email`, `phone`, `role`, `branch_id`, `company_id`, `status`, `password_hash`, `created_at`, `last_login_at`. Authentication artifacts: `password_hash`, `password_salt`, optional `otp_secret`.

## Locations & Geofences

- `location_id`, `party_type`, `party_id`, `name`, `address_line1/2`, `city`, `state`, `country`, `pincode`, `latitude`, `longitude`, `geofence_radius_m` (500/1000/3000 default), `location_type` (plant, warehouse, delivery point).
- Relationships: Consignor warehouses reference `consignor_id`; Consignee delivery points reference `consignee_id`.
- Touchpoints: Planning (origin/destination), journey geofence alerts, ePOD validation.

## Vehicles & Assets

- `vehicle_id`, `transporter_id`, `vehicle_number`, `vehicle_type` (enum: FTL, PTL, Trailer, Mini), `capacity_weight_tonnes`, `capacity_volume_cubic_m`, `tracking_mode_default`.
- Tracking modes: `driver_sim`, `ft_driver_app`, `vehicle_gps`, `fastag`.
- Linked driver: `primary_driver_id`, optional `secondary_driver_id`.
- Compliance attributes: `rc_expiry`, `insurance_expiry`, `fitness_certificate_expiry`.

## Orders & Execution Flow

1. **Purchase Order (PO)**: `po_id`, `consignee_id`, `consignor_id`, `po_number`, `po_date`, `status`, `total_value`, `currency`.
2. **Sales Order (SO)**: `so_id`, references `po_id`, `so_number`, `status`, `priority`, `planned_ship_date`.
3. **Delivery Order (DO)**: `do_id`, references `so_id`, `do_number`, `status`, `shipment_type` (FTL/PTL), `origin_location_id`, `destination_location_id`, `weight_kg`, `volume_cu_m`, `material_mix_profile`.
4. **DO Material Line**: `do_line_id`, `do_id`, `material_code`, `material_desc`, `hsn_code`, `packaging_type`, `weight_kg`, `quantity`, `unit_of_measure`.
5. **Invoice**: `invoice_id`, `do_id`, `invoice_number`, `invoice_type` (single/multi), `value`, `tax_value`, `status`.

## Planning & Freight

- **Freight Order (FO)**: `fo_id`, `do_id`, `transport_mode`, `ftl_flag`, `weight_utilization_pct`, `volume_utilization_pct`, `status`, `planned_departure`.
- **Indent**: `indent_id`, `fo_id`, `indent_status`, `bid_deadline`, `awarded_transporter_id`, `awarded_vehicle_id`, `awarded_rate`.
- **Bid**: `bid_id`, `indent_id`, `transporter_id`, `bid_rate`, `vehicle_type_offer`, `remarks`, `status`, `submitted_at`.

## Journey & Tracking

- **Journey**: `journey_id`, `fo_id`, `vehicle_id`, `driver_id`, `tracking_mode`, `consent_document_url`, `start_time`, `eta`, `current_status`, `last_known_location`, `delay_reason`.
- **Tracking Event**: `event_id`, `journey_id`, `event_type` (geofence_entry/exit, gps_ping, fastag_ping, manual_update), `event_time`, `lat`, `lng`, `source`.
- **Consent Log**: `consent_id`, `driver_id`, `journey_id`, `method` (SIM/App), `consent_timestamp`, `document_url`.

## Proof & Settlement

- **ePOD**: `epod_id`, `journey_id`, `submitted_by_user_id`, `submitted_at`, `pod_type` (photo/digital_signature/paper_scan), `status`, `validated_by_user_id`, `validated_at`.
- **Freight Invoice**: `freight_invoice_id`, `transporter_id`, `fo_id`, `invoice_number`, `invoice_amount`, `gst_amount`, `status`, `payment_due_date`, `payment_status`.
- **Reconciliation**: `recon_id`, `freight_invoice_id`, `variance_amount`, `variance_reason`, `resolution_status`.
- **Dispute**: `dispute_id`, `freight_invoice_id`, `raised_by_party`, `issue_type` (short_delivery, rate_dispute, damage), `description`, `credit_note_id`, `debit_note_id`, `status`.
- **Credit/Debit Note**: `note_id`, `note_type`, `reference_invoice_id`, `amount`, `issued_by`, `issued_to`, `status`.

## Reference & Configuration Tables

- `roles` lookup with party-specific flags.
- `tracking_modes` with capabilities and consent requirements.
- `material_categories`, `industry_types`, `vehicle_types`, `shipment_types`.
- `planning_constraints`: `constraint_id`, `consignor_id`, `name`, `value`, `unit`, `applies_to_shipment_type`.

## Touchpoint Matrix (sample)

| Stage | Consignor | Transporter | Consignee |
|-------|-----------|-------------|-----------|
| PO Creation | Issues PO | — | Approves PO terms |
| SO/DO | Breaks PO, configures shipment | Provides lead times | Receives DO |
| Planning | Sets FTL/PTL, constraints | Shares fleet availability | Confirms slots |
| Indenting | Creates indent | Bids, allocates vehicle | Aware of ETA |
| Journey | Tracks vehicle, alerts | Tracks driver, updates | Preps receiving |
| ePOD | Validates | Captures, uploads | Confirms receipt |
| Freight Invoicing | Creates proforma, reconciles | Submits invoice | Raises disputes if delivery issues |

This matrix drives validation when reviewing demo screens: every UI must surface required data for the relevant party/stage, matching attributes above.

