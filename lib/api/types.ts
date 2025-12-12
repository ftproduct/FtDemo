/**
 * API client types
 */

import type {
  AssetsResponse,
  AssetResponse,
  JourneysResponse,
  OrdersResponse,
  ShipmentsResponse,
  GetAssetsParams,
  GetJourneysParams,
  GetOrdersParams,
  GetShipmentsParams,
} from '@/types/api';

export interface ApiClient {
  assets: {
    getAll: (params?: GetAssetsParams) => Promise<AssetsResponse>;
    getById: (id: string) => Promise<AssetResponse>;
  };
  journeys: {
    getAll: (params?: GetJourneysParams) => Promise<JourneysResponse>;
  };
  orders: {
    getAll: (params?: GetOrdersParams) => Promise<OrdersResponse>;
  };
  shipments: {
    getAll: (params?: GetShipmentsParams) => Promise<ShipmentsResponse>;
  };
}

export interface ApiClientConfig {
  baseUrl?: string;
  timeout?: number;
}
