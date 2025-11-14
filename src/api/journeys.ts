// API client for journeys endpoints

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3001/api';

export interface Journey {
  journey_id: number;
  feed_unique_id: string;
  trip_id: string;
  status_display: string;
  current_location_display: string;
  sla_status: 'on_time' | 'delayed';
  delay_hours?: number;
  sla_status_display: string;
  alert_type?: string;
  alert_time?: string;
  alert_time_display?: string;
  eta_display: string;
  tab_status: string;
  origin_display: string;
  origin_company_display: string;
  destination_display: string;
  destination_company_display: string;
  vehicle_number: string;
  transporter_name: string;
  trip_type_display: string;
  start_time?: string;
  eta?: string;
  actual_arrival?: string;
}

export interface JourneysResponse {
  journeys: Journey[];
  pagination: {
    total: number;
    limit: number;
    offset: number;
    hasMore: boolean;
  };
}

export interface JourneyFilters {
  tab_status?: string;
  alert_type?: string;
  origin_location?: string;
  date_from?: string;
  date_to?: string;
  journey_type?: string;
  search?: string;
  limit?: number;
  offset?: number;
}

export async function fetchJourneys(filters: JourneyFilters = {}): Promise<JourneysResponse> {
  const params = new URLSearchParams();
  
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.append(key, String(value));
    }
  });

  const response = await fetch(`${API_BASE_URL}/journeys?${params.toString()}`);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch journeys: ${response.statusText}`);
  }

  return response.json();
}

export async function fetchJourneyById(id: number): Promise<Journey> {
  const response = await fetch(`${API_BASE_URL}/journeys/${id}`);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch journey: ${response.statusText}`);
  }

  return response.json();
}

export async function fetchJourneyCountsByStatus(): Promise<Array<{ tab_status: string; count: number }>> {
  const response = await fetch(`${API_BASE_URL}/journeys/counts/by-status`);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch journey counts: ${response.statusText}`);
  }

  return response.json();
}

export async function fetchAlertCounts(): Promise<Array<{ alert_type: string; count: number }>> {
  const response = await fetch(`${API_BASE_URL}/journeys/counts/alerts`);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch alert counts: ${response.statusText}`);
  }

  return response.json();
}

