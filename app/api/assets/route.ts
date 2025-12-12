/**
 * GET /api/assets - Get all assets with pagination
 */

import { NextRequest, NextResponse } from 'next/server';
import assetsData from '@/mock-db/assets.json';
import type { AssetsResponse, GetAssetsParams } from '@/types/api';
import type { Asset } from '@/types/entities';

// Simulate latency
function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function GET(request: NextRequest) {
  try {
    // Simulate network latency (100-500ms)
    await delay(Math.random() * 400 + 100);

    const searchParams = request.nextUrl.searchParams;
    const page = parseInt(searchParams.get('page') || '1', 10);
    const pageSize = parseInt(searchParams.get('page_size') || '10', 10);
    const status = searchParams.get('status');
    const type = searchParams.get('type');

    let filteredAssets: Asset[] = [...assetsData] as Asset[];

    // Apply filters
    if (status) {
      filteredAssets = filteredAssets.filter((asset) => asset.status === status);
    }
    if (type) {
      filteredAssets = filteredAssets.filter((asset) => asset.type === type);
    }

    // Pagination
    const total = filteredAssets.length;
    const totalPages = Math.ceil(total / pageSize);
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedAssets = filteredAssets.slice(startIndex, endIndex);

    const response: AssetsResponse = {
      data: paginatedAssets,
      pagination: {
        page,
        page_size: pageSize,
        total,
        total_pages: totalPages,
      },
    };

    return NextResponse.json(response);
  } catch (error) {
    console.error('Error fetching assets:', error);
    return NextResponse.json(
      { message: 'Failed to fetch assets', success: false },
      { status: 500 }
    );
  }
}
