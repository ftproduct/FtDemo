-- Mock TMS PostgreSQL schema
SET client_encoding = 'UTF8';
CREATE SCHEMA IF NOT EXISTS mock_tms;
SET search_path TO mock_tms;

-- Enums
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'party_type_enum') THEN
        CREATE TYPE party_type_enum AS ENUM ('consignor', 'transporter', 'consignee');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'shipment_type_enum') THEN
        CREATE TYPE shipment_type_enum AS ENUM ('FTL', 'PTL');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'po_status_enum') THEN
        CREATE TYPE po_status_enum AS ENUM ('draft', 'confirmed', 'closed', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'so_status_enum') THEN
        CREATE TYPE so_status_enum AS ENUM ('planned', 'released', 'fulfilled', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'do_status_enum') THEN
        CREATE TYPE do_status_enum AS ENUM ('open', 'assigned', 'in_transit', 'delivered', 'closed', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'freight_order_status_enum') THEN
        CREATE TYPE freight_order_status_enum AS ENUM ('draft', 'planned', 'indented', 'awarded', 'journey_started', 'completed', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'indent_status_enum') THEN
        CREATE TYPE indent_status_enum AS ENUM ('open', 'bidding', 'awarded', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'bid_status_enum') THEN
        CREATE TYPE bid_status_enum AS ENUM ('submitted', 'withdrawn', 'accepted', 'rejected');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'journey_status_enum') THEN
        CREATE TYPE journey_status_enum AS ENUM ('pending_start', 'en_route', 'delayed', 'arrived', 'unloaded', 'completed', 'cancelled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tracking_event_type_enum') THEN
        CREATE TYPE tracking_event_type_enum AS ENUM ('geofence_entry', 'geofence_exit', 'gps_ping', 'fastag_ping', 'manual_update');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'epod_status_enum') THEN
        CREATE TYPE epod_status_enum AS ENUM ('pending', 'submitted', 'validated', 'rejected');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'freight_invoice_status_enum') THEN
        CREATE TYPE freight_invoice_status_enum AS ENUM ('draft', 'submitted', 'under_review', 'approved', 'paid', 'rejected');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status_enum') THEN
        CREATE TYPE payment_status_enum AS ENUM ('pending', 'partial', 'paid', 'on_hold');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'dispute_status_enum') THEN
        CREATE TYPE dispute_status_enum AS ENUM ('open', 'under_review', 'resolved', 'closed');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'note_type_enum') THEN
        CREATE TYPE note_type_enum AS ENUM ('credit', 'debit');
    END IF;
END$$;

-- Lookup tables
CREATE TABLE IF NOT EXISTS industry_types (
    industry_type_id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS roles (
    role_code TEXT PRIMARY KEY,
    party_type party_type_enum NOT NULL,
    display_name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS tracking_modes (
    tracking_mode_code TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    requires_driver_consent BOOLEAN DEFAULT FALSE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS vehicle_types (
    vehicle_type_id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    capacity_weight_tonnes NUMERIC(10,2),
    capacity_volume_cu_m NUMERIC(10,2),
    default_shipment_type shipment_type_enum
);

-- Core master tables
CREATE TABLE IF NOT EXISTS companies (
    company_id BIGSERIAL PRIMARY KEY,
    party_type party_type_enum NOT NULL,
    name TEXT NOT NULL,
    code TEXT UNIQUE,
    industry_type_id INTEGER REFERENCES industry_types(industry_type_id),
    erp_code TEXT,
    gst_number TEXT,
    pan_number TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS shipment_constraints (
    constraint_id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(company_id),
    name TEXT NOT NULL,
    value TEXT NOT NULL,
    unit TEXT,
    applies_to_shipment_type shipment_type_enum
);

CREATE TABLE IF NOT EXISTS branches (
    branch_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(company_id),
    name TEXT NOT NULL,
    code TEXT,
    city TEXT,
    state TEXT,
    country TEXT DEFAULT 'India',
    timezone TEXT DEFAULT 'Asia/Kolkata',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(company_id),
    branch_id BIGINT REFERENCES branches(branch_id),
    role_code TEXT NOT NULL REFERENCES roles(role_code),
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    status TEXT DEFAULT 'active',
    password_hash TEXT NOT NULL,
    password_salt TEXT,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS primary_contact_user_id BIGINT REFERENCES users(user_id);

CREATE TABLE IF NOT EXISTS locations (
    location_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(company_id),
    branch_id BIGINT REFERENCES branches(branch_id),
    name TEXT NOT NULL,
    location_type TEXT NOT NULL CHECK (location_type IN ('plant','warehouse','delivery_point')),
    address_line1 TEXT,
    address_line2 TEXT,
    city TEXT,
    state TEXT,
    country TEXT DEFAULT 'India',
    pincode TEXT,
    latitude NUMERIC(10,6),
    longitude NUMERIC(10,6),
    geofence_radius_m INTEGER DEFAULT 500,
    CONSTRAINT geofence_radius_check CHECK (geofence_radius_m IN (500,1000,3000))
);

CREATE TABLE IF NOT EXISTS materials (
    material_id BIGSERIAL PRIMARY KEY,
    consignor_id BIGINT NOT NULL REFERENCES companies(company_id),
    material_code TEXT NOT NULL,
    description TEXT NOT NULL,
    hsn_code TEXT,
    category TEXT,
    unit_of_measure TEXT NOT NULL DEFAULT 'MT',
    UNIQUE(consignor_id, material_code)
);

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id BIGSERIAL PRIMARY KEY,
    transporter_id BIGINT NOT NULL REFERENCES companies(company_id),
    vehicle_type_id INTEGER NOT NULL REFERENCES vehicle_types(vehicle_type_id),
    vehicle_number TEXT NOT NULL UNIQUE,
    vin TEXT,
    capacity_weight_tonnes NUMERIC(10,2),
    capacity_volume_cu_m NUMERIC(10,2),
    tracking_mode_code TEXT REFERENCES tracking_modes(tracking_mode_code),
    rc_expiry DATE,
    insurance_expiry DATE,
    fitness_certificate_expiry DATE,
    status TEXT DEFAULT 'active'
);

CREATE TABLE IF NOT EXISTS driver_profiles (
    driver_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id),
    license_number TEXT NOT NULL,
    license_expiry DATE,
    device_id TEXT,
    consent_required BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS driver_vehicle_assignments (
    assignment_id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL REFERENCES driver_profiles(driver_id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(vehicle_id),
    is_primary BOOLEAN DEFAULT FALSE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ
);

-- Orders hierarchy
CREATE TABLE IF NOT EXISTS purchase_orders (
    po_id BIGSERIAL PRIMARY KEY,
    po_number TEXT NOT NULL UNIQUE,
    consignor_id BIGINT NOT NULL REFERENCES companies(company_id),
    consignee_id BIGINT NOT NULL REFERENCES companies(company_id),
    po_date DATE NOT NULL,
    status po_status_enum NOT NULL DEFAULT 'draft',
    total_value NUMERIC(14,2),
    currency TEXT DEFAULT 'INR',
    remarks TEXT
);

CREATE TABLE IF NOT EXISTS sales_orders (
    so_id BIGSERIAL PRIMARY KEY,
    po_id BIGINT NOT NULL REFERENCES purchase_orders(po_id),
    so_number TEXT NOT NULL,
    status so_status_enum NOT NULL DEFAULT 'planned',
    planned_ship_date DATE,
    priority TEXT CHECK (priority IN ('low','medium','high')) DEFAULT 'medium',
    UNIQUE (po_id, so_number)
);

CREATE TABLE IF NOT EXISTS delivery_orders (
    do_id BIGSERIAL PRIMARY KEY,
    so_id BIGINT NOT NULL REFERENCES sales_orders(so_id),
    do_number TEXT NOT NULL,
    status do_status_enum NOT NULL DEFAULT 'open',
    shipment_type shipment_type_enum NOT NULL,
    origin_location_id BIGINT NOT NULL REFERENCES locations(location_id),
    destination_location_id BIGINT NOT NULL REFERENCES locations(location_id),
    weight_kg NUMERIC(12,3),
    volume_cu_m NUMERIC(12,3),
    industry_profile TEXT,
    requires_epod BOOLEAN DEFAULT TRUE,
    UNIQUE (so_id, do_number)
);

CREATE TABLE IF NOT EXISTS delivery_order_lines (
    do_line_id BIGSERIAL PRIMARY KEY,
    do_id BIGINT NOT NULL REFERENCES delivery_orders(do_id) ON DELETE CASCADE,
    material_id BIGINT REFERENCES materials(material_id),
    material_code TEXT,
    material_description TEXT,
    quantity NUMERIC(14,3),
    unit_of_measure TEXT DEFAULT 'MT',
    weight_kg NUMERIC(12,3),
    volume_cu_m NUMERIC(12,3),
    invoice_number TEXT
);

CREATE TABLE IF NOT EXISTS do_invoices (
    invoice_id BIGSERIAL PRIMARY KEY,
    do_id BIGINT NOT NULL REFERENCES delivery_orders(do_id),
    invoice_number TEXT NOT NULL,
    invoice_type TEXT CHECK (invoice_type IN ('single','multiple')),
    value NUMERIC(14,2) NOT NULL,
    tax_value NUMERIC(14,2),
    status TEXT DEFAULT 'open',
    issued_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (do_id, invoice_number)
);

-- Planning & freight execution
CREATE TABLE IF NOT EXISTS freight_orders (
    fo_id BIGSERIAL PRIMARY KEY,
    do_id BIGINT NOT NULL REFERENCES delivery_orders(do_id),
    transport_mode shipment_type_enum NOT NULL,
    ftl_flag BOOLEAN NOT NULL DEFAULT TRUE,
    weight_utilization_pct NUMERIC(5,2),
    volume_utilization_pct NUMERIC(5,2),
    status freight_order_status_enum NOT NULL DEFAULT 'draft',
    planned_departure TIMESTAMPTZ,
    remarks TEXT
);

CREATE TABLE IF NOT EXISTS indents (
    indent_id BIGSERIAL PRIMARY KEY,
    fo_id BIGINT NOT NULL REFERENCES freight_orders(fo_id),
    indent_status indent_status_enum NOT NULL DEFAULT 'open',
    bid_deadline TIMESTAMPTZ,
    awarded_transporter_id BIGINT REFERENCES companies(company_id),
    awarded_vehicle_id BIGINT REFERENCES vehicles(vehicle_id),
    awarded_rate NUMERIC(14,2)
);

CREATE TABLE IF NOT EXISTS indent_bids (
    bid_id BIGSERIAL PRIMARY KEY,
    indent_id BIGINT NOT NULL REFERENCES indents(indent_id) ON DELETE CASCADE,
    transporter_id BIGINT NOT NULL REFERENCES companies(company_id),
    vehicle_type_id INTEGER REFERENCES vehicle_types(vehicle_type_id),
    bid_rate NUMERIC(14,2) NOT NULL,
    remarks TEXT,
    status bid_status_enum NOT NULL DEFAULT 'submitted',
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (indent_id, transporter_id)
);

-- Journey & tracking
CREATE TABLE IF NOT EXISTS journeys (
    journey_id BIGSERIAL PRIMARY KEY,
    fo_id BIGINT NOT NULL REFERENCES freight_orders(fo_id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(vehicle_id),
    driver_id BIGINT REFERENCES driver_profiles(driver_id),
    tracking_mode_code TEXT REFERENCES tracking_modes(tracking_mode_code),
    consent_document_url TEXT,
    start_time TIMESTAMPTZ,
    eta TIMESTAMPTZ,
    actual_arrival TIMESTAMPTZ,
    current_status journey_status_enum NOT NULL DEFAULT 'pending_start',
    last_known_lat NUMERIC(10,6),
    last_known_lng NUMERIC(10,6),
    delay_reason TEXT
);

CREATE TABLE IF NOT EXISTS driver_consents (
    consent_id BIGSERIAL PRIMARY KEY,
    journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
    driver_id BIGINT NOT NULL REFERENCES driver_profiles(driver_id),
    method TEXT CHECK (method IN ('driver_sim','ft_driver_app')),
    consent_timestamp TIMESTAMPTZ NOT NULL,
    document_url TEXT
);

CREATE TABLE IF NOT EXISTS journey_events (
    event_id BIGSERIAL PRIMARY KEY,
    journey_id BIGINT NOT NULL REFERENCES journeys(journey_id) ON DELETE CASCADE,
    event_type tracking_event_type_enum NOT NULL,
    event_time TIMESTAMPTZ NOT NULL,
    source TEXT CHECK (source IN ('driver_sim','ft_driver_app','vehicle_gps','fastag')),
    latitude NUMERIC(10,6),
    longitude NUMERIC(10,6),
    metadata JSONB
);

-- Proof & settlement
CREATE TABLE IF NOT EXISTS epods (
    epod_id BIGSERIAL PRIMARY KEY,
    journey_id BIGINT NOT NULL REFERENCES journeys(journey_id),
    submitted_by_user_id BIGINT REFERENCES users(user_id),
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    pod_type TEXT CHECK (pod_type IN ('photo','signature','paper_scan')),
    status epod_status_enum NOT NULL DEFAULT 'pending',
    validated_by_user_id BIGINT REFERENCES users(user_id),
    validated_at TIMESTAMPTZ,
    document_url TEXT
);

CREATE TABLE IF NOT EXISTS freight_invoices (
    freight_invoice_id BIGSERIAL PRIMARY KEY,
    transporter_id BIGINT NOT NULL REFERENCES companies(company_id),
    fo_id BIGINT NOT NULL REFERENCES freight_orders(fo_id),
    invoice_number TEXT NOT NULL,
    invoice_amount NUMERIC(14,2) NOT NULL,
    gst_amount NUMERIC(14,2),
    status freight_invoice_status_enum NOT NULL DEFAULT 'draft',
    payment_status payment_status_enum NOT NULL DEFAULT 'pending',
    payment_due_date DATE,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (transporter_id, invoice_number)
);

CREATE TABLE IF NOT EXISTS reconciliations (
    recon_id BIGSERIAL PRIMARY KEY,
    freight_invoice_id BIGINT NOT NULL REFERENCES freight_invoices(freight_invoice_id),
    variance_amount NUMERIC(14,2),
    variance_reason TEXT,
    resolution_status TEXT CHECK (resolution_status IN ('pending','accepted','rejected')) DEFAULT 'pending',
    resolved_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS disputes (
    dispute_id BIGSERIAL PRIMARY KEY,
    freight_invoice_id BIGINT NOT NULL REFERENCES freight_invoices(freight_invoice_id),
    raised_by_party party_type_enum NOT NULL,
    issue_type TEXT CHECK (issue_type IN ('short_delivery','rate_dispute','damage','other')),
    description TEXT,
    status dispute_status_enum NOT NULL DEFAULT 'open',
    credit_note_id BIGINT,
    debit_note_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS credit_debit_notes (
    note_id BIGSERIAL PRIMARY KEY,
    note_type note_type_enum NOT NULL,
    reference_invoice_id BIGINT NOT NULL REFERENCES freight_invoices(freight_invoice_id),
    amount NUMERIC(14,2) NOT NULL,
    issued_by BIGINT REFERENCES companies(company_id),
    issued_to BIGINT REFERENCES companies(company_id),
    status TEXT CHECK (status IN ('draft','issued','settled','void')) DEFAULT 'draft',
    issued_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE disputes
    ADD CONSTRAINT fk_dispute_credit_note FOREIGN KEY (credit_note_id) REFERENCES credit_debit_notes(note_id),
    ADD CONSTRAINT fk_dispute_debit_note FOREIGN KEY (debit_note_id) REFERENCES credit_debit_notes(note_id);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_company ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_locations_company ON locations(company_id);
CREATE INDEX IF NOT EXISTS idx_delivery_orders_status ON delivery_orders(status);
CREATE INDEX IF NOT EXISTS idx_freight_orders_do ON freight_orders(do_id);
CREATE INDEX IF NOT EXISTS idx_journeys_vehicle ON journeys(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_freight_invoices_status ON freight_invoices(status, payment_status);

