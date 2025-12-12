/**
 * Domain entity types for FT Demo Application
 */

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'admin' | 'user' | 'viewer';
  company_id: string;
  created_at: string;
  updated_at: string;
}

export interface Journey {
  id: string;
  journey_id: number;
  feed_unique_id: string;
  origin_display: string;
  origin_company_display: string;
  destination_display: string;
  destination_company_display: string;
  vehicle_number: string;
  transporter_name: string;
  status: string;
  status_display: string;
  current_location_display: string;
  tab_status: 'in_transit' | 'completed' | 'cancelled' | 'pending';
  sla_status: 'on_time' | 'delayed' | 'at_risk';
  sla_status_display: string;
  eta_display: string;
  alert_type?: 'long_stoppage' | 'route_deviation' | 'eway_expiring' | 'eway_expired' | null;
  alert_time_display?: string;
  created_at: string;
  updated_at: string;
}

export interface Asset {
  id: string;
  asset_id: string;
  name: string;
  type: 'vehicle' | 'container' | 'equipment';
  status: 'active' | 'inactive' | 'maintenance';
  location: string;
  company_id: string;
  metadata?: Record<string, unknown>;
  created_at: string;
  updated_at: string;
}

export interface Order {
  id: string;
  order_id: string;
  order_number: string;
  customer_id: string;
  customer_name: string;
  status: 'pending' | 'confirmed' | 'in_transit' | 'delivered' | 'cancelled';
  total_amount: number;
  currency: string;
  origin: string;
  destination: string;
  journey_ids: string[];
  created_at: string;
  updated_at: string;
}

export interface Shipment {
  id: string;
  shipment_id: string;
  tracking_number: string;
  order_id: string;
  journey_id: string;
  status: 'pending' | 'picked_up' | 'in_transit' | 'out_for_delivery' | 'delivered' | 'exception';
  origin: string;
  destination: string;
  estimated_delivery: string;
  actual_delivery?: string;
  carrier: string;
  weight?: number;
  dimensions?: {
    length: number;
    width: number;
    height: number;
  };
  created_at: string;
  updated_at: string;
}
