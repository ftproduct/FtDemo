/**
 * GET /api/journeys - Get all journeys with pagination
 */

import { NextRequest, NextResponse } from 'next/server';
import journeysData from '@/mock-db/journeys.json';
import type { JourneysResponse, GetJourneysParams } from '@/types/api';
import type { Journey } from '@/types/entities';

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
    const tabStatus = searchParams.get('tab_status');

    let filteredJourneys: Journey[] = [...journeysData] as Journey[];

    // Apply filters
    if (status) {
      filteredJourneys = filteredJourneys.filter((journey) => journey.status === status);
    }
    if (tabStatus) {
      filteredJourneys = filteredJourneys.filter((journey) => journey.tab_status === tabStatus);
    }

    // Pagination
    const total = filteredJourneys.length;
    const totalPages = Math.ceil(total / pageSize);
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedJourneys = filteredJourneys.slice(startIndex, endIndex);

    const response: JourneysResponse = {
      data: paginatedJourneys,
      pagination: {
        page,
        page_size: pageSize,
        total,
        total_pages: totalPages,
      },
    };

    return NextResponse.json(response);
  } catch (error) {
    console.error('Error fetching journeys:', error);
    return NextResponse.json(
      { message: 'Failed to fetch journeys', success: false },
      { status: 500 }
    );
  }
}
