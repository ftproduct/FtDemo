'use client';

/**
 * My Journeys page
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

import { useState } from 'react';
import {
  Button,
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
  Table,
  Badge,
  Icon,
  AppHeader,
  Input,
  InputField,
  Dropdown,
  DropdownTrigger,
  DropdownContent,
  DropdownMenu,
  DropdownMenuItem,
  DropdownMenuList,
  DatePicker,
  SegmentedTabs,
  SegmentedTabItem,
  Spacer
} from 'ft-design-system';

// Mock data with required 'id' field
const journeyData = [
  {
    id: 1,
    from: 'Amritsar, Punjab',
    fromSub: 'MDC Labs ltd',
    fromBadge: '+ 1 P',
    to: 'Mumbai, Maharashtra',
    toSub: 'Maa kaali Distributors',
    toBadge: '+ 1 D',
    vehicle: 'PB09 HH 6439',
    transporter: 'Vonex Transporter',
    tripType: 'SIM',
    trackingId: '84973-47693',
    status: 'On Road',
    location: 'Ambala, Haryana',
    sla: 'On time',
    slaColor: 'success',
    eta: '12:30 pm, 12 Aug',
    alerts: ['Long Stoppage'],
    alertTime: '1 hour ago'
  },
  {
    id: 2,
    from: 'Amritsar, Punjab',
    fromSub: 'MDC Labs ltd',
    fromBadge: '+ 1 P',
    to: 'Mumbai, Maharashtra',
    toSub: 'Maa kaali Distributors',
    toBadge: '+ 1 D',
    vehicle: 'PB09 HH 6439',
    transporter: 'Vonex Transporter',
    tripType: 'Text',
    trackingId: '84973-47693',
    status: 'At Drop',
    location: 'Ambala, Haryana',
    sla: 'On time',
    slaColor: 'success',
    eta: '12:30 pm, 12 Aug',
    alerts: [],
    alertTime: '1 hour ago'
  },
  {
    id: 3,
    from: 'Amritsar, Punjab',
    fromSub: 'MDC Labs ltd',
    fromBadge: '+ 1 P',
    to: 'Amritsar, Punjab',
    toSub: 'Sai Traders',
    toBadge: '+ 1 D',
    vehicle: 'PB09 HH 6439',
    transporter: 'Vonex Transporter',
    tripType: 'SIM',
    trackingId: '84973-47693',
    status: 'At Pickup',
    location: 'Ambala, Haryana',
    sla: 'Delayed by 12 hrs 30 min',
    slaColor: 'error',
    eta: '12:30 pm, 12 Aug',
    alerts: ['Long Stoppage'],
    alertTime: '1 hour ago'
  }
];

// Table columns with 'title' not 'header'
const tableColumns = [
  { 
    key: 'checkbox', 
    title: '', 
    width: 60,
    render: () => <input type="checkbox" className="w-4 h-4 cursor-pointer" />
  },
  { 
    key: 'from', 
    title: 'From',
    render: (_: unknown, row: typeof journeyData[0]) => (
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-1">
          <Badge variant="neutral" size="sm">{row.fromBadge}</Badge>
          <span className="font-normal text-sm-rem text-primary-700">{row.from}</span>
        </div>
        <span className="text-sm-rem text-neutral-500">{row.fromSub}</span>
      </div>
    )
  },
  { 
    key: 'to', 
    title: 'To',
    render: (_: unknown, row: typeof journeyData[0]) => (
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-1">
          <Badge variant="neutral" size="sm">{row.toBadge}</Badge>
          <span className="font-normal text-sm-rem text-primary">{row.to}</span>
        </div>
        <span className="text-sm-rem text-neutral-500">{row.toSub}</span>
      </div>
    )
  },
  { 
    key: 'vehicle', 
    title: 'Vehicle Info',
    render: (_: unknown, row: typeof journeyData[0]) => (
      <div className="flex flex-col gap-1">
        <span className="font-normal text-sm-rem text-primary-700">{row.vehicle}</span>
        <span className="text-sm-rem text-neutral-500">{row.transporter}</span>
      </div>
    )
  },
  { 
    key: 'tripInfo', 
    title: 'Trip Info',
    render: (_: unknown, row: typeof journeyData[0]) => (
      <div className="flex items-start gap-2">
        <div className="flex flex-col gap-1">
          <Icon name={row.tripType === 'SIM' ? 'sim' : 'phone'} size={20} />
          <div className="flex items-center gap-1">
            <span className="font-normal text-sm-rem text-primary-700">{row.tripType}</span>
          </div>
          <div className="flex items-center gap-1">
            <Icon name="phone" size={14} />
            <span className="text-sm-rem text-neutral-500">{row.trackingId}</span>
          </div>
        </div>
      </div>
    )
  },
  { 
    key: 'status', 
    title: 'Status',
    render: (_: unknown, row: typeof journeyData[0]) => (
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-1">
          <Icon name="location" size={16} />
          <span className="font-normal text-sm-rem text-primary-700">{row.status}</span>
        </div>
        <div className="flex items-center gap-1">
          <Icon name="location" size={16} />
          <span className="text-sm-rem text-neutral-500">{row.location}</span>
        </div>
      </div>
    )
  },
  { 
    key: 'sla', 
    title: 'SLA',
    render: (_: unknown, row: typeof journeyData[0]) => (
      <div className="flex flex-col gap-1">
        <span className={row.slaColor === 'success' ? 'text-positive font-normal text-sm-rem' : 'text-critical font-normal text-sm-rem'}>
          {row.sla}
        </span>
        <span className="text-sm-rem text-neutral-500">ETA: {row.eta}</span>
      </div>
    )
  },
  { 
    key: 'alerts', 
    title: 'Alerts',
    render: (_: unknown, row: typeof journeyData[0]) => (
      row.alerts.length > 0 ? (
        <div className="flex flex-col gap-1">
          <Badge variant="danger" size="sm">{row.alerts[0]}</Badge>
          <span className="text-xs-rem text-neutral-500">{row.alertTime}</span>
        </div>
      ) : (
        <span className="text-neutral-500">-</span>
      )
    )
  },
  { 
    key: 'actions', 
    title: 'Actions',
    render: () => (
      <div className="flex gap-2">
        <Button variant="text" icon="more" iconPosition="only" size="sm" />
        <Button variant="text" icon="chevron-right" iconPosition="only" size="sm" />
      </div>
    )
  },
];

export default function MyJourneysPage() {
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');
  const [startDate, setStartDate] = useState('2024-08-12');
  const [endDate, setEndDate] = useState('2024-09-12');

  return (
    <div className="bg-white min-h-screen flex flex-col">
      <AppHeader 
        size="md"
        device="desktop"
        user={{
          name: 'MDC Labs',
          email: 'labs@mdc.com'
        }}
      />
      
      {/* Title Bar */}
      <div className="p-5 border-b border-neutral-200 flex items-center justify-between">
        {/* Title */}
        <div className="flex items-center gap-3">
          <Icon name="my-trip" size={28} />
          <h1 className="text-xl-rem font-semibold text-primary-700 m-0">
            My Journeys
          </h1>
        </div>

        {/* Filter Bar */}
        <div className="flex gap-3 items-center">
          <Dropdown>
            <DropdownTrigger className="w-60">
              <div className="flex items-center justify-between w-full">
                <span>MDC Labs, Amritsar</span>
                <Icon name="chevron-down" size={16} />
              </div>
            </DropdownTrigger>
            <DropdownContent>
              <DropdownMenu>
                <DropdownMenuList>
                  <DropdownMenuItem>MDC Labs, Amritsar</DropdownMenuItem>
                  <DropdownMenuItem>Other Location</DropdownMenuItem>
                </DropdownMenuList>
              </DropdownMenu>
            </DropdownContent>
          </Dropdown>

          <DatePicker
            range
            startValue={startDate}
            endValue={endDate}
            onStartChange={setStartDate}
            onEndChange={setEndDate}
            placeholder="12 Aug, 2024 → 12 Sep 2024"
            size="md"
          />

          <Dropdown>
            <DropdownTrigger className="w-full">
              <div className="flex items-center justify-between w-full">
                <span>Outbound - Source</span>
                <Icon name="chevron-down" size={16} />
              </div>
            </DropdownTrigger>
            <DropdownContent>
              <DropdownMenu>
                <DropdownMenuList>
                  <DropdownMenuItem>Outbound - Source</DropdownMenuItem>
                  <DropdownMenuItem>Inbound - Destination</DropdownMenuItem>
                </DropdownMenuList>
              </DropdownMenu>
            </DropdownContent>
          </Dropdown>

          <Input className="w-full">
            <InputField 
              type="text" 
              placeholder="Search My Journeys"
              leadingIcon="search"
              size="md"
            />
          </Input>

          <Button variant="primary" icon="add" iconPosition="left" size="md" className="h-10">
            Add Journey
          </Button>
        </div>
      </div>

      {/* Main Content */}
      <div className="px-5 flex-1 bg-white">
        {/* Tabs */}
        <div className="flex flex-col relative">
          <Tabs defaultValue="in-transit" type="primary" overflowBehavior="auto">
          <div className="flex items-center justify-between w-fit pt-0 pb-0 gap-4 h-fit">
            <TabsList className="border-none gap-0 p-0 overflow-hidden w-fit">
              <TabsTrigger value="planned">
                <div className="flex items-center gap-2">
                  <Icon name="clock" size={16} />
                  <span>Planned</span>
                  <Badge variant="neutral" size="sm">56</Badge>
                </div>
              </TabsTrigger>
              <TabsTrigger value="en-route">
                <div className="flex items-center gap-2">
                  <Icon name="truck" size={16} />
                  <span>En Route to Loading</span>
                  <Badge variant="neutral" size="sm">56</Badge>
                </div>
              </TabsTrigger>
              <TabsTrigger value="at-loading">
                <div className="flex items-center gap-2">
                  <Icon name="warehouse" size={16} />
                  <span>At Loading</span>
                  <Badge variant="neutral" size="sm">56</Badge>
                </div>
              </TabsTrigger>
              <TabsTrigger value="in-transit">
                <div className="flex items-center gap-2">
                  <Icon name="truck" size={16} />
                  <span>In Transit</span>
                  <Badge variant="neutral" size="sm">56</Badge>
                </div>
              </TabsTrigger>
              <TabsTrigger value="at-unloading">
                <div className="flex items-center gap-2">
                  <Icon name="warehouse" size={16} />
                  <span>At Unloading</span>
                  <Badge variant="neutral" size="sm">56</Badge>
                </div>
              </TabsTrigger>
              <TabsTrigger value="in-return">
                <div className="flex items-center gap-2">
                  <Icon name="arrow-left" size={16} />
                  <span>In Return</span>
                  <Badge variant="neutral" size="sm">56</Badge>
                </div>
              </TabsTrigger>
              <TabsTrigger value="delivered">
                <div className="flex items-center gap-2">
                  <Icon name="check" size={16} />
                  <span>Delivered</span>
                  <Badge variant="neutral" size="sm">56</Badge>
                </div>
              </TabsTrigger>
            </TabsList>

            {/* View Toggle */}
            <SegmentedTabs 
              variant="icon-only" 
              value={viewMode} 
              onChange={(value) => setViewMode(value as 'list' | 'map')}
              className="my-0"
            >
              <SegmentedTabItem value="list" icon={<Icon name="menu" size={16} />} />
              <SegmentedTabItem value="map" icon={<Icon name="map" size={16} />} />
            </SegmentedTabs>
          </div>

          <Spacer height="var(--spacing-x4)" />

          {/* Quick Filters */}
          <div className="flex gap-2 items-center overflow-x-auto">
            <div className="flex items-center gap-2 px-2 py-1 border border-neutral-200 rounded-lg bg-white h-9 whitespace-nowrap">
              <Badge variant="danger" size="sm">19</Badge>
              <span className="text-sm-rem font-semibold text-primary-700">Long Stoppage</span>
            </div>

            <div className="flex items-center gap-2 px-2 py-1 border border-neutral-200 rounded-lg bg-white h-9 whitespace-nowrap">
              <Badge variant="danger" size="sm">19</Badge>
              <span className="text-sm-rem font-semibold text-primary-700">Route Deviation</span>
            </div>
            
            {/* Delayed Filter Group */}
            <div className="flex items-center overflow-hidden bg-white h-9 whitespace-nowrap border border-neutral-200 rounded-lg w-fit">
              <div className="bg-neutral-100 px-3 font-semibold text-sm-rem text-primary-700 h-full flex items-center">
                Delayed
              </div>
              <div className="flex gap-2 px-3 items-center h-full">
                <div className="flex items-center gap-1">
                  <Badge variant="danger" size="sm">19</Badge>
                  <span className="text-sm-rem font-semibold text-primary-700">0-6 hrs</span>
                </div>
                <div className="flex items-center gap-1">
                  <Badge variant="danger" size="sm">19</Badge>
                  <span className="text-sm-rem font-semibold text-primary-700">6-12 hrs</span>
                </div>
                <div className="flex items-center gap-1">
                  <Badge variant="danger" size="sm">19</Badge>
                  <span className="text-sm-rem font-semibold text-primary-700">12+ hrs</span>
                </div>
              </div>
            </div>

            {/* E Way Bill Filter Group */}
            <div className="flex items-center overflow-hidden bg-white h-9 whitespace-nowrap border border-neutral-200 rounded-lg">
              <div className="bg-neutral-100 px-3 font-semibold text-sm-rem text-primary-700 h-full flex items-center">
                E Way bill
              </div>
              <div className="flex gap-2 px-3 items-center h-full">
                <div className="flex items-center gap-1">
                  <Badge variant="warning" size="sm">19</Badge>
                  <span className="text-sm-rem font-semibold text-primary-700">Expiring in 3 hrs</span>
                </div>
                <div className="flex items-center gap-1">
                  <Badge variant="danger" size="sm">19</Badge>
                  <span className="text-sm-rem font-semibold text-primary-700">Expired</span>
                </div>
              </div>
            </div>

            {/* Reaching Destination Filter Group */}
            <div className="flex items-center overflow-hidden bg-white h-9 whitespace-nowrap border border-neutral-200 rounded-lg">
              <div className="bg-neutral-100 px-3 font-semibold text-sm-rem text-primary-700 h-full flex items-center">
                Reaching Destination
              </div>
              <div className="flex gap-2 px-3 items-center h-full">
                <div className="flex items-center gap-1">
                  <Badge variant="neutral" size="sm">19</Badge>
                  <span className="text-sm-rem font-semibold text-primary-700">0-6 hrs</span>
                </div>
              </div>
            </div>
          </div>

          {/* Journey Count and Actions */}
          <div className="flex justify-between items-center py-4">
            <p className="text-md-rem font-semibold text-primary-700 m-0">
              56 Journeys available
            </p>

            <div className="flex gap-4 items-center">
              <Button variant="text" icon="star" iconPosition="only" size="sm" />
              <Button variant="text" icon="add" iconPosition="only" size="sm" />
              <Button variant="text" icon="download" iconPosition="only" size="sm" />
              <Button variant="text" icon="filter" iconPosition="only" size="sm" />
              <Button variant="text" icon="settings" iconPosition="only" size="sm" />
              
              <div className="flex items-center gap-1 border border-neutral-200 rounded-lg px-3 h-10">
                <Button variant="text" icon="chevron-left" iconPosition="only" size="sm" />
                <span className="text-md-rem text-neutral-500 px-2">1</span>
                <Button variant="text" icon="chevron-right" iconPosition="only" size="sm" />
              </div>
            </div>
          </div>

          {/* Table */}
          <TabsContent value="in-transit" id="tabpanel-in-transit" className="mt-0">
            <div className="border border-neutral-200 rounded-lg overflow-hidden bg-white">
              <Table columns={tableColumns} data={journeyData} />
            </div>
          </TabsContent>
        </Tabs>
        </div>
      </div>
    </div>
  );
}
