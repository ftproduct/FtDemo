/**
 * GET /api/orders - Get all orders with pagination
 */

import { NextRequest, NextResponse } from 'next/server';
import ordersData from '@/mock-db/orders.json';
import type { OrdersResponse, GetOrdersParams } from '@/types/api';
import type { Order } from '@/types/entities';

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
    const customerId = searchParams.get('customer_id');

    let filteredOrders: Order[] = [...ordersData] as Order[];

    // Apply filters
    if (status) {
      filteredOrders = filteredOrders.filter((order) => order.status === status);
    }
    if (customerId) {
      filteredOrders = filteredOrders.filter((order) => order.customer_id === customerId);
    }

    // Pagination
    const total = filteredOrders.length;
    const totalPages = Math.ceil(total / pageSize);
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedOrders = filteredOrders.slice(startIndex, endIndex);

    const response: OrdersResponse = {
      data: paginatedOrders,
      pagination: {
        page,
        page_size: pageSize,
        total,
        total_pages: totalPages,
      },
    };

    return NextResponse.json(response);
  } catch (error) {
    console.error('Error fetching orders:', error);
    return NextResponse.json(
      { message: 'Failed to fetch orders', success: false },
      { status: 500 }
    );
  }
}
