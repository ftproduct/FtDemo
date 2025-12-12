/**
 * API endpoint functions
 * Re-exported from client for convenience
 */

import { apiClient } from './client';

export const apiEndpoints = {
  assets: apiClient.assets,
  journeys: apiClient.journeys,
  orders: apiClient.orders,
  shipments: apiClient.shipments,
};
