/**
 * Base API client
 */

import type { ApiClientConfig, ApiClient } from './types';
import { apiEndpoints } from './endpoints';

const DEFAULT_CONFIG: Required<ApiClientConfig> = {
  baseUrl: typeof window !== 'undefined' ? window.location.origin : 'http://localhost:3000',
  timeout: 30000,
};

class ApiClientImpl implements ApiClient {
  private config: Required<ApiClientConfig>;

  constructor(config: ApiClientConfig = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config };
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.config.baseUrl}${endpoint}`;
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.config.timeout);

    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal,
        headers: {
          'Content-Type': 'application/json',
          ...options.headers,
        },
      });

      clearTimeout(timeoutId);

      if (!response.ok) {
        const error = await response.json().catch(() => ({
          message: `HTTP ${response.status}: ${response.statusText}`,
        }));
        throw new Error(error.message || 'Request failed');
      }

      return await response.json();
    } catch (error) {
      clearTimeout(timeoutId);
      if (error instanceof Error) {
        if (error.name === 'AbortError') {
          throw new Error('Request timeout');
        }
        throw error;
      }
      throw new Error('Unknown error occurred');
    }
  }

  assets = {
    getAll: async (params?: Parameters<ApiClient['assets']['getAll']>[0]) => {
      const queryParams = new URLSearchParams();
      if (params) {
        Object.entries(params).forEach(([key, value]) => {
          if (value !== undefined) {
            queryParams.append(key, String(value));
          }
        });
      }
      const queryString = queryParams.toString();
      return this.request<ReturnType<ApiClient['assets']['getAll']>>(
        `/api/assets${queryString ? `?${queryString}` : ''}`
      );
    },
    getById: async (id: string) => {
      return this.request<ReturnType<ApiClient['assets']['getById']>>(
        `/api/assets/${id}`
      );
    },
  };

  journeys = {
    getAll: async (params?: Parameters<ApiClient['journeys']['getAll']>[0]) => {
      const queryParams = new URLSearchParams();
      if (params) {
        Object.entries(params).forEach(([key, value]) => {
          if (value !== undefined) {
            queryParams.append(key, String(value));
          }
        });
      }
      const queryString = queryParams.toString();
      return this.request<ReturnType<ApiClient['journeys']['getAll']>>(
        `/api/journeys${queryString ? `?${queryString}` : ''}`
      );
    },
  };

  orders = {
    getAll: async (params?: Parameters<ApiClient['orders']['getAll']>[0]) => {
      const queryParams = new URLSearchParams();
      if (params) {
        Object.entries(params).forEach(([key, value]) => {
          if (value !== undefined) {
            queryParams.append(key, String(value));
          }
        });
      }
      const queryString = queryParams.toString();
      return this.request<ReturnType<ApiClient['orders']['getAll']>>(
        `/api/orders${queryString ? `?${queryString}` : ''}`
      );
    },
  };

  shipments = {
    getAll: async (params?: Parameters<ApiClient['shipments']['getAll']>[0]) => {
      const queryParams = new URLSearchParams();
      if (params) {
        Object.entries(params).forEach(([key, value]) => {
          if (value !== undefined) {
            queryParams.append(key, String(value));
          }
        });
      }
      const queryString = queryParams.toString();
      return this.request<ReturnType<ApiClient['shipments']['getAll']>>(
        `/api/shipments${queryString ? `?${queryString}` : ''}`
      );
    },
  };
}

export const apiClient = new ApiClientImpl();
