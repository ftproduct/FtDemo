/**
 * API hook utilities
 */

import { useQuery, UseQueryOptions } from '@tanstack/react-query';
import { apiEndpoints } from '@/lib/api/endpoints';
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

/**
 * Query keys factory
 */
export const queryKeys = {
  assets: {
    all: ['assets'] as const,
    lists: () => [...queryKeys.assets.all, 'list'] as const,
    list: (params?: GetAssetsParams) => [...queryKeys.assets.lists(), params] as const,
    details: () => [...queryKeys.assets.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.assets.details(), id] as const,
  },
  journeys: {
    all: ['journeys'] as const,
    lists: () => [...queryKeys.journeys.all, 'list'] as const,
    list: (params?: GetJourneysParams) => [...queryKeys.journeys.lists(), params] as const,
  },
  orders: {
    all: ['orders'] as const,
    lists: () => [...queryKeys.orders.all, 'list'] as const,
    list: (params?: GetOrdersParams) => [...queryKeys.orders.lists(), params] as const,
  },
  shipments: {
    all: ['shipments'] as const,
    lists: () => [...queryKeys.shipments.all, 'list'] as const,
    list: (params?: GetShipmentsParams) => [...queryKeys.shipments.lists(), params] as const,
  },
};

/**
 * Hook to fetch assets
 */
export function useAssets(
  params?: GetAssetsParams,
  options?: Omit<UseQueryOptions<AssetsResponse>, 'queryKey' | 'queryFn'>
) {
  return useQuery({
    queryKey: queryKeys.assets.list(params),
    queryFn: () => apiEndpoints.assets.getAll(params),
    ...options,
  });
}

/**
 * Hook to fetch asset by ID
 */
export function useAsset(
  id: string,
  options?: Omit<UseQueryOptions<AssetResponse>, 'queryKey' | 'queryFn'>
) {
  return useQuery({
    queryKey: queryKeys.assets.detail(id),
    queryFn: () => apiEndpoints.assets.getById(id),
    enabled: !!id,
    ...options,
  });
}

/**
 * Hook to fetch journeys
 */
export function useJourneys(
  params?: GetJourneysParams,
  options?: Omit<UseQueryOptions<JourneysResponse>, 'queryKey' | 'queryFn'>
) {
  return useQuery({
    queryKey: queryKeys.journeys.list(params),
    queryFn: () => apiEndpoints.journeys.getAll(params),
    ...options,
  });
}

/**
 * Hook to fetch orders
 */
export function useOrders(
  params?: GetOrdersParams,
  options?: Omit<UseQueryOptions<OrdersResponse>, 'queryKey' | 'queryFn'>
) {
  return useQuery({
    queryKey: queryKeys.orders.list(params),
    queryFn: () => apiEndpoints.orders.getAll(params),
    ...options,
  });
}

/**
 * Hook to fetch shipments
 */
export function useShipments(
  params?: GetShipmentsParams,
  options?: Omit<UseQueryOptions<ShipmentsResponse>, 'queryKey' | 'queryFn'>
) {
  return useQuery({
    queryKey: queryKeys.shipments.list(params),
    queryFn: () => apiEndpoints.shipments.getAll(params),
    ...options,
  });
}
