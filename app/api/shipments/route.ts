/**
 * GET /api/shipments - Get all shipments with pagination
 */

import { NextRequest, NextResponse } from 'next/server';
import shipmentsData from '@/mock-db/shipments.json';
import type { ShipmentsResponse, GetShipmentsParams } from '@/types/api';
import type { Shipment } from '@/types/entities';

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
    const orderId = searchParams.get('order_id');

    let filteredShipments: Shipment[] = [...shipmentsData] as Shipment[];

    // Apply filters
    if (status) {
      filteredShipments = filteredShipments.filter((shipment) => shipment.status === status);
    }
    if (orderId) {
      filteredShipments = filteredShipments.filter((shipment) => shipment.order_id === orderId);
    }

    // Pagination
    const total = filteredShipments.length;
    const totalPages = Math.ceil(total / pageSize);
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedShipments = filteredShipments.slice(startIndex, endIndex);

    const response: ShipmentsResponse = {
      data: paginatedShipments,
      pagination: {
        page,
        page_size: pageSize,
        total,
        total_pages: totalPages,
      },
    };

    return NextResponse.json(response);
  } catch (error) {
    console.error('Error fetching shipments:', error);
    return NextResponse.json(
      { message: 'Failed to fetch shipments', success: false },
      { status: 500 }
    );
  }
}
