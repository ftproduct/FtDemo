/**
 * GET /api/assets/[id] - Get asset by ID
 */

import { NextRequest, NextResponse } from 'next/server';
import assetsData from '@/mock-db/assets.json';
import type { AssetResponse } from '@/types/api';
import type { Asset } from '@/types/entities';

// Simulate latency
function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    // Simulate network latency (100-300ms)
    await delay(Math.random() * 200 + 100);

    const asset = (assetsData as Asset[]).find((a) => a.id === params.id || a.asset_id === params.id);

    if (!asset) {
      return NextResponse.json(
        { message: 'Asset not found', success: false },
        { status: 404 }
      );
    }

    const response: AssetResponse = {
      data: asset,
      success: true,
    };

    return NextResponse.json(response);
  } catch (error) {
    console.error('Error fetching asset:', error);
    return NextResponse.json(
      { message: 'Failed to fetch asset', success: false },
      { status: 500 }
    );
  }
}
