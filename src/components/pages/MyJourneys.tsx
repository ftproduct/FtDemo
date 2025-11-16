import React, { useState, useEffect } from 'react';
import { 
  Tabs, 
  Badge, 
  Button, 
  Input, 
  Table, 
  Checkbox, 
  Dropdown,
  QuickFilters,
  DatePicker,
  type TableColumn
} from 'ft-design-system/ai';
import { type Journey } from '../../api/journeys';
import '../../styles/globals.css';
import AppHeader from '../AppHeader';
import {
  Home,
  LayoutList,
  Map,
  RefreshCw,
  Share2,
  Download,
  Upload,
  Settings,
  Filter,
  ChevronLeft,
  ChevronRight,
  Calendar,
  Search,
  MoreHorizontal,
  ArrowRight,
  Clock,
  MapPin,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';

interface MyJourneysProps {
  onOpenNavigation: () => void;
}

export default function MyJourneys({ onOpenNavigation }: MyJourneysProps) {
  const [journeys, setJourneys] = useState<Journey[]>([]);
  const [filteredJourneys, setFilteredJourneys] = useState<Journey[]>([]);
  const [selectedTab, setSelectedTab] = useState(3); // In Transit is default
  const [totalCount, setTotalCount] = useState(56);
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedJourneyIds, setSelectedJourneyIds] = useState<number[]>([]);
  const [page, setPage] = useState(1);
  const [activeFilters, setActiveFilters] = useState<Set<string>>(new Set());

  // Mock data
  const mockJourneys: Journey[] = [
    {
      journey_id: 1,
      feed_unique_id: '324673-948B478-84...',
      origin_display: 'Amritsar, Punjab',
      origin_company_display: 'MDC Labs ltd',
      destination_display: 'Mumbai, M...',
      destination_company_display: 'Maa kaali Distribut...',
      vehicle_number: 'PB09 HH 6439',
      transporter_name: 'Yonex Transporter',
      trip_type_display: 'SIM',
      trip_id: '84973-47593',
      status_display: 'On Road',
      current_location_display: 'Ambala, Haryana',
      sla_status: 'on_time',
      sla_status_display: 'On time',
      eta_display: 'ETA: 12:30 pm, 12 Aug',
      alert_type: null,
      alert_time_display: null,
      tab_status: 'in_transit',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    },
    {
      journey_id: 2,
      feed_unique_id: '324673-948B478-84...',
      origin_display: 'Amritsar, Punjab',
      origin_company_display: 'MDC Labs LTD',
      destination_display: 'Secunderabad, Tel...',
      destination_company_display: 'Sai Traders',
      vehicle_number: 'KA12 AS 3422',
      transporter_name: 'Laal Kamal Trans...',
      trip_type_display: 'GPS',
      trip_id: '84973-47593',
      status_display: 'At Pickup',
      current_location_display: 'Ambala, Haryana',
      sla_status: 'delayed',
      sla_status_display: 'Delayed by 13 hr',
      eta_display: 'ETA: 12:30 pm, 12 Aug',
      alert_type: 'long_stoppage',
      alert_time_display: '1 hour ago',
      tab_status: 'in_transit',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    },
    {
      journey_id: 3,
      feed_unique_id: '324673-948B478-84...',
      origin_display: 'Amritsar, Punjab',
      origin_company_display: 'MDC Labs LTD',
      destination_display: 'Secunderabad, Tel...',
      destination_company_display: 'Sai Traders',
      vehicle_number: 'KA12 AS 3423',
      transporter_name: 'Laal Kamal Trans...',
      trip_type_display: 'Fastag',
      trip_id: '84973-47593',
      status_display: 'At Drop',
      current_location_display: 'Ambala, Haryana',
      sla_status: 'delayed',
      sla_status_display: 'Delayed by 13 hr',
      eta_display: 'ETA: 12:30 pm, 12 Aug',
      alert_type: 'route_deviation',
      alert_time_display: '1 hour ago',
      tab_status: 'in_transit',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    },
    {
      journey_id: 4,
      feed_unique_id: '324673-948B478-84...',
      origin_display: 'Amritsar, Punjab',
      origin_company_display: 'MDC Labs LTD',
      destination_display: 'Siddipet, Telangana',
      destination_company_display: 'Jai Sri Ram',
      vehicle_number: 'KA12 AS 3424',
      transporter_name: 'Laal Kamal Trans...',
      trip_type_display: 'GPS',
      trip_id: '84973-47593',
      status_display: 'On Road',
      current_location_display: 'at 12:30 pm, 12 Aug',
      sla_status: 'on_time',
      sla_status_display: 'On time',
      eta_display: 'ETA: 12:30 pm, 12 Aug',
      alert_type: null,
      alert_time_display: null,
      tab_status: 'in_transit',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    },
    {
      journey_id: 5,
      feed_unique_id: '324673-948B478-84...',
      origin_display: 'Amritsar, Punjab',
      origin_company_display: 'MDC Labs LTD',
      destination_display: 'Secunderabad, Tel...',
      destination_company_display: 'Sai Traders',
      vehicle_number: 'KA12 AS 3423',
      transporter_name: 'Laal Kamal Trans...',
      trip_type_display: 'Fastag',
      trip_id: '84973-47593',
      status_display: 'On Road',
      current_location_display: 'Ambala, Haryana',
      sla_status: 'delayed',
      sla_status_display: 'Delayed by 13 hr',
      eta_display: 'ETA: 12:30 pm, 12 Aug',
      alert_type: 'transit_delay',
      alert_time_display: '1 hour ago',
      tab_status: 'in_transit',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    },
    {
      journey_id: 6,
      feed_unique_id: '324673-948B478-84...',
      origin_display: 'Amritsar, Punjab',
      origin_company_display: 'MDC Labs LTD',
      destination_display: 'Secunderabad, Tel...',
      destination_company_display: 'Sai Traders',
      vehicle_number: 'KA12 AS 3421',
      transporter_name: 'Laal Kamal Trans...',
      trip_type_display: 'SIM',
      trip_id: '84973-47593',
      status_display: 'At Drop',
      current_location_display: 'Ambala, Haryana',
      sla_status: 'on_time',
      sla_status_display: 'On time',
      eta_display: 'ETA: 12:30 pm, 12 Aug',
      alert_type: null,
      alert_time_display: null,
      tab_status: 'in_transit',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    }
  ];

  // Calculate counts from actual data
  const calculateTabCounts = React.useMemo(() => {
    const allJourneys = journeys.length > 0 ? journeys : mockJourneys;
    return {
      planned: allJourneys.filter(j => j.tab_status === 'planned').length,
      en_route_to_loading: allJourneys.filter(j => j.tab_status === 'en_route_to_loading').length,
      at_loading: allJourneys.filter(j => j.tab_status === 'at_loading').length,
      in_transit: allJourneys.filter(j => j.tab_status === 'in_transit').length,
      at_unloading: allJourneys.filter(j => j.tab_status === 'at_unloading').length,
      in_return: allJourneys.filter(j => j.tab_status === 'in_return').length,
      delivered: allJourneys.filter(j => j.tab_status === 'delivered').length
    };
  }, [journeys]);

  const calculateFilterCounts = React.useMemo(() => {
    const allJourneys = journeys.length > 0 ? journeys : mockJourneys;
    const delayed = allJourneys.filter(j => j.sla_status === 'delayed');
    const longStoppage = allJourneys.filter(j => j.alert_type === 'long_stoppage').length;
    const routeDeviation = allJourneys.filter(j => j.alert_type === 'route_deviation').length;
    
    return {
      stoppage: longStoppage,
      deviation: routeDeviation,
      delayed: delayed.length,
      '0-6hrs': delayed.length, // For demo, using delayed count
      '6-12hrs': 0, // Would need actual delay hours in real data
      '12plus': 0, // Would need actual delay hours in real data
      expiring: 0, // Would need e-way bill data
      expired: 0, // Would need e-way bill data
      '6hrs': 0, // Would need ETA data
      '12hrs': 0, // Would need ETA data
      '24plus': 0 // Would need ETA data
    };
  }, [journeys]);

  // Tabs configuration with icons - using dynamic counts
  const tabs = [
    { label: 'Planned', badge: true, badgeCount: calculateTabCounts.planned, icon: true },
    { label: 'En Route to Loading', badge: true, badgeCount: calculateTabCounts.en_route_to_loading, icon: true },
    { label: 'At Loading', badge: true, badgeCount: calculateTabCounts.at_loading, icon: true },
    { label: 'In Transit', badge: true, badgeCount: calculateTabCounts.in_transit, icon: true },
    { label: 'At Unloading', badge: true, badgeCount: calculateTabCounts.at_unloading, icon: true },
    { label: 'In Return', badge: true, badgeCount: calculateTabCounts.in_return, icon: true },
    { label: 'Delivered', badge: true, badgeCount: calculateTabCounts.delivered, icon: true }
  ];

  useEffect(() => {
    // Map journeys to include 'id' property required by Table component
    const journeysWithId = mockJourneys.map(journey => ({
      ...journey,
      id: journey.journey_id
    }));
    setJourneys(journeysWithId);
    setSelectedJourneyIds([]);
  }, [selectedTab]);

  // Filter journeys based on active filters
  useEffect(() => {
    let filtered = [...journeys];

    if (activeFilters.size > 0) {
      filtered = journeys.filter(journey => {
        // Check each active filter
        for (const filterKey of activeFilters) {
          const [filterId, optionId] = filterKey.split(':');
          
          // Single option filters
          if (filterId === 'stoppage' && journey.alert_type === 'long_stoppage') {
            return true;
          }
          if (filterId === 'deviation' && journey.alert_type === 'route_deviation') {
            return true;
          }
          
          // Multi-option filters
          if (filterId === 'delayed') {
            if (journey.sla_status === 'delayed') {
              // Check specific delay ranges if option is selected
              if (optionId === '0-6hrs' || optionId === '6-12hrs' || optionId === '12plus') {
                // For demo, if delayed, show it (in real app, check actual delay hours)
                return true;
              }
              // If no specific option, show all delayed
              if (!optionId) return true;
            }
          }
          
          if (filterId === 'eway') {
            // E Way bill filters - for demo, show all if selected
            return true;
          }
          
          if (filterId === 'eta') {
            // ETA filters - for demo, show all if selected
            return true;
          }
        }
        return false;
      });
    }

    setFilteredJourneys(filtered);
  }, [journeys, activeFilters]);

  // Quick Filters - Single and Multi-option filters - using dynamic counts
  const quickFilters = [
    // Single option filters
    { id: 'stoppage', label: 'Long Stoppage', count: calculateFilterCounts.stoppage, type: 'alert' as const },
    { id: 'deviation', label: 'Route Deviation', count: calculateFilterCounts.deviation, type: 'alert' as const },
    // Multi-option filter: Delayed
    {
      id: 'delayed',
      label: 'Delayed',
      count: calculateFilterCounts.delayed,
      type: 'normal' as const,
      options: [
        { id: '0-6hrs', label: '0-6 hrs', count: calculateFilterCounts['0-6hrs'], type: 'warning' as const },
        { id: '6-12hrs', label: '6-12 hrs', count: calculateFilterCounts['6-12hrs'], type: 'warning' as const },
        { id: '12plus', label: '12+ hrs', count: calculateFilterCounts['12plus'], type: 'alert' as const }
      ]
    },
    // Multi-option filter: E Way bill
    {
      id: 'eway',
      label: 'E Way bill',
      type: 'normal' as const,
      options: [
        { id: 'expiring', label: 'Expiring in 3 hrs', count: calculateFilterCounts.expiring, type: 'warning' as const },
        { id: 'expired', label: 'Expired', count: calculateFilterCounts.expired, type: 'alert' as const }
      ]
    },
    // Multi-option filter: ETA
    {
      id: 'eta',
      label: 'ETA',
      type: 'normal' as const,
      options: [
        { id: '6hrs', label: '6 hrs', count: calculateFilterCounts['6hrs'], type: 'success' as const },
        { id: '12hrs', label: '12 hrs', count: calculateFilterCounts['12hrs'], type: 'success' as const },
        { id: '24plus', label: '24+ hrs', count: calculateFilterCounts['24plus'], type: 'alert' as const }
      ]
    }
  ];

  // Table columns
  const columns: TableColumn<Journey>[] = [
    {
      key: 'select',
      title: '',
      width: '48px',
      render: (_: any, record: Journey) => (
        <Checkbox
          checked={selectedJourneyIds.includes(record.journey_id)}
          onChange={(event) => {
            setSelectedJourneyIds((prev) => {
              if (event.target.checked) {
                return Array.from(new Set([...prev, record.journey_id]));
              }
              return prev.filter((id) => id !== record.journey_id);
            });
          }}
        />
      )
    },
    {
      key: 'feed_unique_id',
      title: 'Feed Unique ID',
      width: '180px',
      render: (_: any, record: Journey) => (
        <>
          <div>{record.feed_unique_id}</div>
          <Button variant="link">View ID's</Button>
        </>
      )
    },
    {
      key: 'from',
      title: 'From',
      width: '200px',
      render: (_: any, record: Journey) => (
        <>
          <div>
            <span>{record.origin_display}</span>
            <Badge variant="normal">+1P</Badge>
          </div>
          <div>{record.origin_company_display}</div>
        </>
      )
    },
    {
      key: 'to',
      title: 'To',
      width: '200px',
      render: (_: any, record: Journey) => (
        <>
          <div>
            <span>{record.destination_display}</span>
            <Badge variant="normal">+3D</Badge>
          </div>
          <div>{record.destination_company_display}</div>
        </>
      )
    },
    {
      key: 'vehicle',
      title: 'Vehicle Info',
      width: '180px',
      render: (_: any, record: Journey) => (
        <>
          <div>{record.vehicle_number}</div>
          <div>{record.transporter_name}</div>
        </>
      )
    },
    {
      key: 'trip',
      title: 'Trip Info',
      width: '180px',
      render: (_: any, record: Journey) => {
        const getTripIcon = (type: string) => {
          if (type === 'SIM') {
            return <div style={{ width: '16px', height: '16px', borderRadius: '50%', background: '#52c41a', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <div style={{ width: '8px', height: '8px', background: 'white', borderRadius: '50%' }} />
            </div>;
          }
          if (type === 'GPS') {
            return <MapPin style={{ width: '16px', height: '16px', color: '#1890ff' }} />;
          }
          if (type === 'Fastag') {
            return <div style={{ width: '16px', height: '16px', borderRadius: '2px', background: '#722ed1', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'white', fontWeight: 600 }}>F</div>;
          }
          return null;
        };
        
        return (
          <>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              {getTripIcon(record.trip_type_display)}
              <span>{record.trip_type_display}</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <CheckCircle2 style={{ width: '14px', height: '14px', color: '#52c41a' }} />
              <span>{record.trip_id}</span>
            </div>
          </>
        );
      }
    },
    {
      key: 'status',
      title: 'Status',
      width: '180px',
      render: (_: any, record: Journey) => (
        <>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <MapPin style={{ width: '14px', height: '14px', color: 'var(--secondary)' }} />
            <span>{record.status_display}</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <MapPin style={{ width: '14px', height: '14px', color: 'var(--secondary)' }} />
            <span>{record.current_location_display}</span>
          </div>
        </>
      )
    },
    {
      key: 'sla',
      title: 'SLA',
      width: '160px',
      render: (_: any, record: Journey) => (
        <>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            {record.sla_status === 'on_time' ? (
              <CheckCircle2 style={{ width: '16px', height: '16px', color: '#52c41a' }} />
            ) : (
              <Clock style={{ width: '16px', height: '16px', color: '#ff4d4f' }} />
            )}
            <span style={{ color: record.sla_status === 'on_time' ? '#52c41a' : '#ff4d4f' }}>
              {record.sla_status_display}
            </span>
          </div>
          <div>{record.eta_display}</div>
        </>
      )
    },
    {
      key: 'alerts',
      title: 'Alerts',
      width: '160px',
      render: (_: any, record: Journey) => {
        if (!record.alert_type) return null;
        
        const alertLabels: Record<string, string> = {
          long_stoppage: 'Long Stoppage',
          route_deviation: 'Route Deviation',
          transit_delay: 'Transit Delay'
        };
        
        return (
          <>
            <Badge variant="danger">
              {alertLabels[record.alert_type] || record.alert_type}
            </Badge>
            <div>{record.alert_time_display || '1 hour ago'}</div>
          </>
        );
      }
    },
    {
      key: 'actions',
      title: 'Actions',
      width: '100px',
      render: () => (
        <>
          <Button variant="text" icon="more" />
          <Button variant="text" icon="arrow-right" />
        </>
      )
    }
  ];

  return (
    <div style={{ backgroundColor: 'var(--bg-secondary)', minHeight: '100vh' }}>
      <AppHeader onOpenNavigation={onOpenNavigation} />
      
      <div style={{ backgroundColor: 'var(--bg-primary)', padding: 'var(--space-5)' }}>
        {/* Title Bar */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-5)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Home style={{ width: '28px', height: '28px', color: 'var(--primary)' }} />
            <h1 style={{ margin: 0, fontSize: 'var(--font-size-xl)', fontWeight: 600, color: 'var(--primary)' }}>My Journeys</h1>
          </div>
          
          {/* Filter Bar */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Dropdown
              options={[
                { value: 'mdc-labs', label: 'MDC Labs, Amritsar' },
                { value: 'all', label: 'All Companies' }
              ]}
              placeholder="Select company"
              defaultValue="mdc-labs"
            />
            
            <DatePicker
              placeholder="12 Aug, 2024 → 12 Sep 2024"
            />
            
            <Dropdown
              options={[
                { value: 'outbound', label: 'Outbound - Source' },
                { value: 'inbound', label: 'Inbound' }
              ]}
              placeholder="Direction"
              defaultValue="outbound"
            />
            
            <Input
              placeholder="Search My Journeys"
              leadingIcon="search"
              value={searchTerm}
              onChange={(event: React.ChangeEvent<HTMLInputElement>) => setSearchTerm(event.target.value)}
            />
            
            <Button variant="primary" icon="calendar">Add Journey</Button>
          </div>
            </div>

        {/* Tabs + View Toggle */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-4)' }}>
          <Tabs tabs={tabs} activeTab={selectedTab} onChange={(index: number) => setSelectedTab(index)} />
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', backgroundColor: 'var(--bg-secondary)', borderRadius: 'var(--radius-md)', padding: '4px' }}>
            <Button
              variant={viewMode === 'list' ? 'secondary' : 'text'}
              style={{ width: '32px', height: '32px', padding: 0 }}
              onClick={() => setViewMode('list')}
            >
              <LayoutList style={{ width: '16px', height: '16px' }} />
            </Button>
            <Button 
              variant={viewMode === 'map' ? 'secondary' : 'text'}
              style={{ width: '32px', height: '32px', padding: 0 }}
              onClick={() => setViewMode('map')}
            >
              <Map style={{ width: '16px', height: '16px' }} />
            </Button>
        </div>
      </div>

        {/* Quick Filters - Single and Multi-option */}
        <div className="quick-filter-scroll">
          <QuickFilters 
            className="quick-filter-row"
            filters={quickFilters.map(filter => ({
              ...filter,
              selected: activeFilters.has(filter.id),
              selectedOption: Array.from(activeFilters)
                .find(f => f.startsWith(`${filter.id}:`))?.split(':')[1]
            }))}
            onFilterClick={(filterId, optionId) => {
              const filterKey = optionId ? `${filterId}:${optionId}` : filterId;
              setActiveFilters(prev => {
                const next = new Set(prev);
                if (next.has(filterKey)) {
                  next.delete(filterKey);
                } else {
                  next.add(filterKey);
                }
                return next;
              });
            }}
            onFilterRemove={(filterId, optionId) => {
              const filterKey = optionId ? `${filterId}:${optionId}` : filterId;
              setActiveFilters(prev => {
                const next = new Set(prev);
                next.delete(filterKey);
                return next;
              });
            }}
          />
        </div>

        {/* Actions Row */}
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center', 
          marginTop: 'var(--space-4)', 
          marginBottom: 'var(--space-4)',
          padding: 'var(--space-3) var(--space-4)',
          backgroundColor: 'var(--bg-primary)',
          borderRadius: 'var(--radius-md)'
        }}>
          <div style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-sm)' }}>
            {selectedJourneyIds.length > 0 ? `${selectedJourneyIds.length} journeys selected · ` : ''}
            {activeFilters.size > 0 ? filteredJourneys.length : journeys.length} journeys available
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <Button variant="text" style={{ width: '40px', height: '40px', padding: 0 }}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
              </svg>
            </Button>
            <Button variant="text" style={{ width: '40px', height: '40px', padding: 0 }}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M9 11l3 3L22 4" />
                <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
              </svg>
            </Button>
            <Button variant="text" style={{ width: '40px', height: '40px', padding: 0 }}>
              <Filter style={{ width: '20px', height: '20px' }} />
            </Button>
            <Button variant="text" style={{ width: '40px', height: '40px', padding: 0 }}>
              <LayoutList style={{ width: '20px', height: '20px' }} />
            </Button>
            <Button variant="text" style={{ width: '40px', height: '40px', padding: 0 }}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="3" width="7" height="7" />
                <rect x="14" y="3" width="7" height="7" />
                <rect x="14" y="14" width="7" height="7" />
                <rect x="3" y="14" width="7" height="7" />
              </svg>
            </Button>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 'var(--space-2)',
            backgroundColor: 'var(--bg-primary)',
            border: '1px solid var(--border-primary)',
            borderRadius: 'var(--radius-md)',
              padding: 'var(--space-1) var(--space-2)'
            }}>
              <Button
                variant="text"
                style={{ width: '24px', height: '24px', padding: 0 }}
                onClick={() => setPage((prev) => Math.max(1, prev - 1))}
              >
                <ChevronLeft style={{ width: '16px', height: '16px' }} />
              </Button>
              <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)', minWidth: '20px', textAlign: 'center' }}>
                {page}
              </span>
              <Button 
                variant="text"
                style={{ width: '24px', height: '24px', padding: 0 }}
                onClick={() => setPage((prev) => prev + 1)}
              >
                <ChevronRight style={{ width: '16px', height: '16px' }} />
              </Button>
            </div>
          </div>
        </div>

                {/* Table */}
                <Table columns={columns} data={activeFilters.size > 0 ? filteredJourneys : journeys} />
      </div>
    </div>
  );
}
