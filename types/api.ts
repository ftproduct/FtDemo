/**
 * API request and response types
 */

import { Journey, Asset, Order, Shipment, User } from './entities';

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    page_size: number;
    total: number;
    total_pages: number;
  };
}

export interface ApiError {
  message: string;
  code?: string;
  details?: Record<string, unknown>;
}

// Request types
export interface GetAssetsParams {
  page?: number;
  page_size?: number;
  status?: string;
  type?: string;
}

export interface GetJourneysParams {
  page?: number;
  page_size?: number;
  status?: string;
  tab_status?: string;
}

export interface GetOrdersParams {
  page?: number;
  page_size?: number;
  status?: string;
  customer_id?: string;
}

export interface GetShipmentsParams {
  page?: number;
  page_size?: number;
  status?: string;
  order_id?: string;
}

// Response types
export type AssetsResponse = PaginatedResponse<Asset>;
export type AssetResponse = ApiResponse<Asset>;
export type JourneysResponse = PaginatedResponse<Journey>;
export type OrdersResponse = PaginatedResponse<Order>;
export type ShipmentsResponse = PaginatedResponse<Shipment>;
export type UsersResponse = ApiResponse<User[]>;
