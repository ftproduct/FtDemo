-- Seed data for mock TMS schema
SET search_path TO mock_tms;

TRUNCATE TABLE
    credit_debit_notes,
    disputes,
    reconciliations,
    freight_invoices,
    epods,
    journey_events,
    driver_consents,
    journeys,
    indent_bids,
    indents,
    freight_orders,
    do_invoices,
    delivery_order_lines,
    delivery_orders,
    sales_orders,
    purchase_orders,
    driver_vehicle_assignments,
    driver_profiles,
    vehicles,
    materials,
    locations,
    shipment_constraints,
    users,
    branches,
    companies,
    vehicle_types,
    tracking_modes,
    roles,
    industry_types
    RESTART IDENTITY CASCADE;

-- Lookups
INSERT INTO industry_types (name) VALUES
    ('Cement'),
    ('FMCG'),
    ('Automotive');

INSERT INTO roles (role_code, party_type, display_name, description) VALUES
    ('consignor_cxo', 'consignor', 'Consignor CXO', 'Exec overview access'),
    ('consignor_logistics_head', 'consignor', 'Consignor Logistics Head', 'Controls planning & FO approvals'),
    ('warehouse_manager', 'consignor', 'Warehouse Manager', 'Handles DO execution'),
    ('logistics_manager', 'consignor', 'Logistics Manager', 'Daily operations & tracking'),
    ('transporter_head', 'transporter', 'Transporter Head', 'Owns pricing & SLA'),
    ('transporter_manager', 'transporter', 'Transporter Manager', 'Bids and allocates vehicles'),
    ('driver', 'transporter', 'Driver', 'Executes journey & ePOD'),
    ('consignee_head', 'consignee', 'Consignee Regional Head', 'Confirms receiving'),
    ('consignee_warehouse', 'consignee', 'Consignee Warehouse Manager', 'Validates delivery');

INSERT INTO tracking_modes (tracking_mode_code, display_name, requires_driver_consent, description) VALUES
    ('driver_sim', 'Driver SIM Tracking', TRUE, 'Location via driver SIM'),
    ('ft_driver_app', 'FT Driver App', TRUE, 'In-app GPS tracking'),
    ('vehicle_gps', 'Vehicle GPS', FALSE, 'OEM device feed'),
    ('fastag', 'Fastag Tolls', FALSE, 'Checkpoint-based tracking');

INSERT INTO vehicle_types (name, capacity_weight_tonnes, capacity_volume_cu_m, default_shipment_type) VALUES
    ('32FT Trailer', 32, 70, 'FTL'),
    ('20FT Container', 20, 45, 'FTL'),
    ('14FT Truck', 9, 25, 'PTL');

-- Companies & branches
INSERT INTO companies (party_type, name, code, industry_type_id, erp_code, gst_number, pan_number)
VALUES
    ('consignor', 'Alpha Cements Ltd', 'ACL', 1, 'ACL-ERP', '27ABCDE1234F1Z5', 'ABCDE1234F'),
    ('transporter', 'SwiftLog Transport', 'SLT', 3, 'SLT-ERP', '29AAICS7654G1Z2', 'AAICS7654G'),
    ('consignee', 'RetailHub Distribution', 'RHD', 2, 'RHD-ERP', '07AABCR7890H1Z7', 'AABCR7890H');

INSERT INTO branches (company_id, name, code, city, state) VALUES
    (1, 'Alpha HQ', 'ACL-HQ', 'Mumbai', 'Maharashtra'),
    (1, 'Alpha Plant - Pune', 'ACL-PN', 'Pune', 'Maharashtra'),
    (2, 'SwiftLog West', 'SLT-W', 'Mumbai', 'Maharashtra'),
    (2, 'SwiftLog Central', 'SLT-C', 'Nagpur', 'Maharashtra'),
    (3, 'RetailHub NCR', 'RHD-NCR', 'Gurgaon', 'Haryana');

-- Users
INSERT INTO users (company_id, branch_id, role_code, name, email, phone, password_hash)
VALUES
    (1, 1, 'consignor_cxo', 'Niharika Menon', 'niharika.menon@alpha.com', '+911122334455', 'hash-cxo'),
    (1, 2, 'consignor_logistics_head', 'Kunal Iyer', 'kunal.iyer@alpha.com', '+919820000111', 'hash-log-head'),
    (1, 2, 'warehouse_manager', 'Ravi Kulkarni', 'ravi.kulkarni@alpha.com', '+919767555333', 'hash-warehouse'),
    (1, 2, 'logistics_manager', 'Saanvi Rao', 'saanvi.rao@alpha.com', '+919619888777', 'hash-log-mgr'),
    (2, 3, 'transporter_head', 'Prakash Shetty', 'prakash.shetty@swiftlog.com', '+918451223344', 'hash-trans-head'),
    (2, 4, 'transporter_manager', 'Ibrahim Khan', 'ibrahim.khan@swiftlog.com', '+918527889900', 'hash-trans-mgr'),
    (2, 4, 'driver', 'Deepak Sharma', 'deepak.sharma@swiftlog.com', '+918888112233', 'hash-driver'),
    (3, 5, 'consignee_head', 'Ritu Malhotra', 'ritu.malhotra@retailhub.com', '+911244556677', 'hash-consignee-head'),
    (3, 5, 'consignee_warehouse', 'Vivek Sharma', 'vivek.sharma@retailhub.com', '+919999556688', 'hash-consignee-wh');

-- Primary contacts
UPDATE companies SET primary_contact_user_id = 1 WHERE company_id = 1;
UPDATE companies SET primary_contact_user_id = 5 WHERE company_id = 2;
UPDATE companies SET primary_contact_user_id = 8 WHERE company_id = 3;

-- Locations
INSERT INTO locations (company_id, branch_id, name, location_type, address_line1, city, state, pincode, latitude, longitude, geofence_radius_m)
VALUES
    (1, 2, 'Alpha Cement Plant Pune', 'plant', 'MIDC Phase 2', 'Pune', 'Maharashtra', '411057', 18.620000, 73.780000, 1000),
    (1, 2, 'Alpha Regional Warehouse Bhiwandi', 'warehouse', 'Dapode Road', 'Bhiwandi', 'Maharashtra', '421302', 19.281200, 73.048900, 500),
    (3, 5, 'RetailHub NCR DC', 'delivery_point', 'Sector 72', 'Gurgaon', 'Haryana', '122001', 28.459500, 77.026600, 1000);

-- Materials
INSERT INTO materials (consignor_id, material_code, description, hsn_code, category, unit_of_measure)
VALUES
    (1, 'AC-BULK', 'OPC 53 Bulk Cement', '25232910', 'Cement', 'MT'),
    (1, 'AC-BAG', 'OPC 53 Bag Cement', '25232910', 'Cement', 'Bag');

-- Vehicles & drivers
INSERT INTO vehicles (transporter_id, vehicle_type_id, vehicle_number, capacity_weight_tonnes, capacity_volume_cu_m, tracking_mode_code, rc_expiry, insurance_expiry, fitness_certificate_expiry)
VALUES
    (2, 1, 'MH12AB1234', 32, 70, 'vehicle_gps', '2026-03-31', '2025-12-31', '2026-06-30'),
    (2, 3, 'MH31CD5678', 9, 25, 'driver_sim', '2025-08-31', '2025-05-31', '2025-09-30');

INSERT INTO driver_profiles (user_id, license_number, license_expiry, device_id)
VALUES
    (7, 'MH1420150001234', '2026-11-30', 'FT-DRIVER-001');

INSERT INTO driver_vehicle_assignments (driver_id, vehicle_id, is_primary)
VALUES
    (1, 1, TRUE);

-- Constraints
INSERT INTO shipment_constraints (company_id, name, value, unit, applies_to_shipment_type)
VALUES
    (1, 'Max PTL weight per DO', '9000', 'kg', 'PTL'),
    (1, 'FTL target weight utilization', '0.9', 'ratio', 'FTL'),
    (1, 'FTL target volume utilization', '0.85', 'ratio', 'FTL');

-- Orders flow
INSERT INTO purchase_orders (po_number, consignor_id, consignee_id, po_date, status, total_value)
VALUES
    ('PO-2025-00045', 1, 3, '2025-11-01', 'confirmed', 12500000);

INSERT INTO sales_orders (po_id, so_number, status, planned_ship_date, priority)
VALUES
    (1, 'SO-2025-01011', 'released', '2025-11-05', 'high');

INSERT INTO delivery_orders (so_id, do_number, status, shipment_type, origin_location_id, destination_location_id, weight_kg, volume_cu_m, industry_profile, requires_epod)
VALUES
    (1, 'DO-2025-7001', 'assigned', 'FTL', 1, 3, 28500, 60, 'Cement-Bulk', TRUE),
    (1, 'DO-2025-7002', 'open', 'PTL', 2, 3, 6500, 12, 'Cement-Bag', TRUE);

INSERT INTO delivery_order_lines (do_id, material_id, material_code, material_description, quantity, unit_of_measure, weight_kg, volume_cu_m, invoice_number)
VALUES
    (1, 1, 'AC-BULK', 'OPC 53 Bulk Cement', 570, 'MT', 28500, 60, 'INV-FTL-001'),
    (2, 2, 'AC-BAG', 'OPC 53 Bag Cement', 1300, 'Bag', 6500, 12, 'INV-PTL-001');

INSERT INTO do_invoices (do_id, invoice_number, invoice_type, value, tax_value, status, issued_at)
VALUES
    (1, 'INV-FTL-001', 'single', 7200000, 1296000, 'open', '2025-11-05 09:00+05:30'),
    (2, 'INV-PTL-001', 'single', 1100000, 198000, 'open', '2025-11-06 09:30+05:30');

-- Freight planning & indents
INSERT INTO freight_orders (do_id, transport_mode, ftl_flag, weight_utilization_pct, volume_utilization_pct, status, planned_departure, remarks)
VALUES
    (1, 'FTL', TRUE, 0.89, 0.86, 'indented', '2025-11-05 12:00+05:30', 'Direct Pune to NCR'),
    (2, 'PTL', FALSE, 0.72, 0.48, 'planned', '2025-11-06 14:00+05:30', 'Club with FMCG loads');

INSERT INTO indents (fo_id, indent_status, bid_deadline, awarded_transporter_id, awarded_vehicle_id, awarded_rate)
VALUES
    (1, 'awarded', '2025-11-04 18:00+05:30', 2, 1, 165000),
    (2, 'bidding', '2025-11-05 18:00+05:30', NULL, NULL, NULL);

INSERT INTO indent_bids (indent_id, transporter_id, vehicle_type_id, bid_rate, remarks, status)
VALUES
    (1, 2, 1, 165000, 'Trailer with GPS ready', 'accepted'),
    (2, 2, 3, 42000, '14FT truck availability on 6 Nov', 'submitted');

-- Journey & tracking
INSERT INTO journeys (fo_id, vehicle_id, driver_id, tracking_mode_code, consent_document_url, start_time, eta, current_status, last_known_lat, last_known_lng)
VALUES
    (1, 1, 1, 'vehicle_gps', 'https://cdn.ftdemo.com/consents/consent-journey-1.pdf', '2025-11-05 13:00+05:30', '2025-11-07 08:00+05:30', 'en_route', 24.572100, 75.123400);

-- Align the journey with production-style payload attributes
UPDATE journeys
SET
    feed_unique_id = 'JRN-c53ba305-1757-4237-9743-cbca0492458a',
    start_time = '2025-03-26 12:24:05+05:30',
    eta = '2025-03-27 17:29:09+05:30',
    current_status = 'en_route',
    direction = 'outbound',
    movement_subtype = 'primary',
    movement_channel = 'road',
    expected_transit_time_minutes = 1740,
    planned_distance_km = 398.827,
    actual_distance_km = 398.827,
    distance_covered_km = 387.919683365,
    avg_distance_km = 387.919683365,
    time_to_destination_min = 0,
    delay_minutes = 1085,
    last_ping_at = '2025-03-27 03:56:39+05:30',
    tracking_signal_strength = 16.2393,
    transit_sla_hours = NULL,
    is_round_trip = FALSE,
    freight_value = 950000,
    freight_value_currency = 'INR',
    freight_rate = 165000,
    freight_rate_unit = 'per_trip',
    negotiated_freight_value = 165000,
    contracted_freight_value = 165000,
    sales_rep_id = 'SR-0098',
    indent_external_id = 'IND-FT-2025-0001',
    planning_external_id = 'PLAN-FT-777',
    trip_id = 'TRIP-2025-0001',
    trip_type_code = 'SIM',
    trip_type_display = 'SIM',
    vehicle_type_ref_code = 'TRAILER-32FT',
    vehicle_assigned_at = '2025-03-26 10:00+05:30',
    current_location_display = 'GRW3+F2 Bahamnola, Haryana, India',
    sla_status = 'delayed',
    delay_hours = 18.08,
    eta_display = 'ETA: 27 Mar, 5:29 pm',
    status_display = 'On Road',
    tab_status = 'in_transit',
    alert_type = 'long_stoppage',
    alert_time = '2025-11-05 23:39:29+00'
WHERE journey_id = 1;

INSERT INTO journey_loads (journey_id, billing_type, order_type, yard_entry_number, tare_weight_tons, net_weight_tons, gross_weight_tons, erp_transit_distance_km, expected_transit_time_minutes, freight_value, freight_value_currency, shipment_numbers)
VALUES
    (1, 'to bill', 'primary', 'YARD-ENTRY-7788', 12.5, 25.0, 37.5, 398.827, 1740, 950000, 'INR', ARRAY['SHIP-FT-0001','SHIP-FT-0002']);

INSERT INTO journey_shipments (journey_id, shipment_number)
VALUES
    (1, 'SHIP-FT-0001'),
    (1, 'SHIP-FT-0002');

INSERT INTO journey_lcus (journey_id, vehicle_type_ref_code, vehicle_assigned_at, negotiated_freight_value, contracted_freight_value)
VALUES
    (1, 'TRAILER-32FT', '2025-03-26 10:00+05:30', 165000, 165000);

INSERT INTO journey_drivers (journey_id, driver_name, driver_phone, license_number, lcu_ids)
VALUES
    (1, 'RAJA', '+910000000099', 'DL-RAJA-2025', ARRAY[2910]);

-- Stops derived from journey_summary.forward_leg
INSERT INTO journey_stops (
    journey_id, external_stop_id, leg_id, stop_type, stop_actions, branch_name, company_name,
    display_name, branch_address, latitude, longitude, distance_travelled_km,
    total_time_travelled_hours, leg_state, is_active_stop, is_achieved
) VALUES
    (1, '14104', '5370', 'origin', ARRAY['pickup'], 'Journey Cnr', 'Journey Cnr',
     'Journey Cnr-Branch Two', 'New Delhi, Delhi, India', 28.6139298, 77.2088282,
     0, 0, 'IN_TRANSIT_SURE', FALSE, FALSE),
    (1, '14105', '5370', 'waypoint', ARRAY[]::TEXT[], NULL, NULL,
     'Ambala Cantt Waypoint', 'Ambala Cantt, Haryana, India', 30.3610314, 76.8485468,
     387.919683365, 3.057157222, 'IN_TRANSIT_SURE', FALSE, FALSE),
    (1, '14106', '5370', 'destination', ARRAY['drop'], NULL, NULL,
     'Punjab Drop Yard', 'Punjab, India', 31.1471305, 75.3412179,
     398.827, 3.057157222, 'PENDING', FALSE, FALSE);

-- Alert feed extracted from production payload sample
INSERT INTO journey_alerts (
    alert_id, journey_id, alert_name, alert_display_text, alert_status, alert_stage_type,
    alert_stage_id, alert_start_time, alert_close_time, alert_duration_minutes,
    alert_start_location, alert_current_location, alert_visualization_type, is_pre_transit, metadata
) VALUES
    ('b615cb41-6086-402b-b8ae-81df9a90ea42', 1, 'route_deviation', 'Deviated 45.0 km from route', 'CLOSED',
     'leg', '63690', '2025-10-30 05:59:59+00', '2025-10-30 06:40:08+00', 41,
     'W96H+6JP, Patna - Aurangabad Rd, Atrauli, Babhandiha, Bihar 824124, India',
     'NS-4A, NS-4A, Tola, Manjurahi, Bihar 824102, India', 'path', FALSE, '{"source":"simulator"}'::jsonb),
    ('00508ae0-8dc0-46b8-9f86-e6b5db5fc8ce', 1, 'long_stoppage', 'Stoppage for 3h 0m', 'CLOSED',
     'leg', '63690', '2025-10-30 18:19:08+00', '2025-10-31 13:20:07+00', 1141,
     'F44H+W7 Khutha, Madhya Pradesh, India',
     '5XHP+JJP, Bodhala, Maharashtra 441501, India', 'point', FALSE, '{}'::jsonb),
    ('bd9cd805-c01b-40fb-8d39-b0fa70ddc2b2', 1, 'long_stoppage', 'Stoppage for 3h 9m', 'IN_PROGRESS',
     'leg', '63691', '2025-11-06 07:50:00+00', NULL, 190,
     '9PM4+4CQ, Luhari, Haryana 124108, India',
     '9PM4+4CQ, Luhari, Haryana 124108, India', 'point', FALSE, '{}'::jsonb);

UPDATE journeys
SET current_alert_id = 'bd9cd805-c01b-40fb-8d39-b0fa70ddc2b2'::uuid,
    current_alert_display = 'Stoppage for 3h 9m'
WHERE journey_id = 1;

INSERT INTO driver_consents (journey_id, driver_id, method, consent_timestamp, document_url)
VALUES
    (1, 1, 'driver_sim', '2025-11-05 11:45+05:30', 'https://cdn.ftdemo.com/consents/consent-journey-1.pdf');

INSERT INTO journey_events (journey_id, event_type, event_time, source, latitude, longitude, metadata)
VALUES
    (1, 'gps_ping', '2025-11-05 16:00+05:30', 'vehicle_gps', 19.980000, 75.320000, '{"speed_kmph": 58}'),
    (1, 'fastag_ping', '2025-11-06 04:30+05:30', 'fastag', 23.020500, 72.579700, '{"toll_plaza": "Ahmedabad Sardar Patel"}'),
    (1, 'geofence_entry', '2025-11-07 07:55+05:30', 'vehicle_gps', 28.459400, 77.026800, '{"location":"RetailHub NCR DC"}');

-- Proof & invoicing
INSERT INTO epods (journey_id, submitted_by_user_id, submitted_at, pod_type, status, validated_by_user_id, validated_at, document_url)
VALUES
    (1, 9, '2025-11-07 09:30+05:30', 'photo', 'validated', 4, '2025-11-07 10:00+05:30', 'https://cdn.ftdemo.com/epod/epod-journey-1.jpg');

INSERT INTO freight_invoices (transporter_id, fo_id, invoice_number, invoice_amount, gst_amount, status, payment_status, payment_due_date, submitted_at)
VALUES
    (2, 1, 'FINV-2025-0901', 165000, 29700, 'under_review', 'pending', '2025-11-21', '2025-11-08 12:00+05:30');

INSERT INTO reconciliations (freight_invoice_id, variance_amount, variance_reason, resolution_status)
VALUES
    (1, -2500, 'Short delivery adjustment', 'pending');

INSERT INTO credit_debit_notes (note_type, reference_invoice_id, amount, issued_by, issued_to, status, issued_at)
VALUES
    ('credit', 1, 2500, 1, 2, 'issued', '2025-11-10 15:00+05:30');

INSERT INTO disputes (freight_invoice_id, raised_by_party, issue_type, description, status, credit_note_id, created_at)
VALUES
    (1, 'consignor', 'short_delivery', '2 MT short at unloading, adjust freight.', 'under_review', 1, '2025-11-08 15:30+05:30');

-- Sequence alignment
SELECT
    setval('mock_tms.companies_company_id_seq', COALESCE((SELECT MAX(company_id) FROM companies), 1), TRUE),
    setval('mock_tms.branches_branch_id_seq', COALESCE((SELECT MAX(branch_id) FROM branches), 1), TRUE),
    setval('mock_tms.users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1), TRUE),
    setval('mock_tms.locations_location_id_seq', COALESCE((SELECT MAX(location_id) FROM locations), 1), TRUE),
    setval('mock_tms.materials_material_id_seq', COALESCE((SELECT MAX(material_id) FROM materials), 1), TRUE),
    setval('mock_tms.vehicles_vehicle_id_seq', COALESCE((SELECT MAX(vehicle_id) FROM vehicles), 1), TRUE),
    setval('mock_tms.driver_profiles_driver_id_seq', COALESCE((SELECT MAX(driver_id) FROM driver_profiles), 1), TRUE),
    setval('mock_tms.purchase_orders_po_id_seq', COALESCE((SELECT MAX(po_id) FROM purchase_orders), 1), TRUE),
    setval('mock_tms.sales_orders_so_id_seq', COALESCE((SELECT MAX(so_id) FROM sales_orders), 1), TRUE),
    setval('mock_tms.delivery_orders_do_id_seq', COALESCE((SELECT MAX(do_id) FROM delivery_orders), 1), TRUE),
    setval('mock_tms.delivery_order_lines_do_line_id_seq', COALESCE((SELECT MAX(do_line_id) FROM delivery_order_lines), 1), TRUE),
    setval('mock_tms.do_invoices_invoice_id_seq', COALESCE((SELECT MAX(invoice_id) FROM do_invoices), 1), TRUE),
    setval('mock_tms.freight_orders_fo_id_seq', COALESCE((SELECT MAX(fo_id) FROM freight_orders), 1), TRUE),
    setval('mock_tms.indents_indent_id_seq', COALESCE((SELECT MAX(indent_id) FROM indents), 1), TRUE),
    setval('mock_tms.indent_bids_bid_id_seq', COALESCE((SELECT MAX(bid_id) FROM indent_bids), 1), TRUE),
    setval('mock_tms.journeys_journey_id_seq', COALESCE((SELECT MAX(journey_id) FROM journeys), 1), TRUE),
    setval('mock_tms.journey_events_event_id_seq', COALESCE((SELECT MAX(event_id) FROM journey_events), 1), TRUE),
    setval('mock_tms.epods_epod_id_seq', COALESCE((SELECT MAX(epod_id) FROM epods), 1), TRUE),
    setval('mock_tms.freight_invoices_freight_invoice_id_seq', COALESCE((SELECT MAX(freight_invoice_id) FROM freight_invoices), 1), TRUE),
    setval('mock_tms.reconciliations_recon_id_seq', COALESCE((SELECT MAX(recon_id) FROM reconciliations), 1), TRUE),
    setval('mock_tms.credit_debit_notes_note_id_seq', COALESCE((SELECT MAX(note_id) FROM credit_debit_notes), 1), TRUE),
    setval('mock_tms.disputes_dispute_id_seq', COALESCE((SELECT MAX(dispute_id) FROM disputes), 1), TRUE),
    setval('mock_tms.journey_loads_journey_load_id_seq', COALESCE((SELECT MAX(journey_load_id) FROM journey_loads), 1), TRUE),
    setval('mock_tms.journey_lcus_journey_lcu_id_seq', COALESCE((SELECT MAX(journey_lcu_id) FROM journey_lcus), 1), TRUE),
    setval('mock_tms.journey_drivers_journey_driver_id_seq', COALESCE((SELECT MAX(journey_driver_id) FROM journey_drivers), 1), TRUE),
    setval('mock_tms.journey_stops_journey_stop_id_seq', COALESCE((SELECT MAX(journey_stop_id) FROM journey_stops), 1), TRUE),
    setval('mock_tms.journey_shipments_journey_shipment_id_seq', COALESCE((SELECT MAX(journey_shipment_id) FROM journey_shipments), 1), TRUE);

