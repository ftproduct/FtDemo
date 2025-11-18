import React, { useState, useEffect, useRef, useCallback } from 'react';
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
  Spacer,
  Divider,
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
  AlertCircle,
  Star
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
  const [selectAll, setSelectAll] = useState(false);
  const tabsContainerRef = useRef<HTMLDivElement>(null);
  const tabsListRef = useRef<HTMLDivElement>(null);
  const tabRefs = useRef<(HTMLElement | null)[]>([]);
  const [visibleTabs, setVisibleTabs] = useState<typeof tabs>([]);
  const [hiddenTabs, setHiddenTabs] = useState<typeof tabs>([]);
  const [showOverflowDropdown, setShowOverflowDropdown] = useState(false);

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
  const tabs = React.useMemo(() => [
    { label: 'Planned', badge: true, badgeCount: calculateTabCounts.planned, icon: true },
    { label: 'En Route to Loading', badge: true, badgeCount: calculateTabCounts.en_route_to_loading, icon: true },
    { label: 'At Loading', badge: true, badgeCount: calculateTabCounts.at_loading, icon: true },
    { label: 'In Transit', badge: true, badgeCount: calculateTabCounts.in_transit, icon: true },
    { label: 'At Unloading', badge: true, badgeCount: calculateTabCounts.at_unloading, icon: true },
    { label: 'In Return', badge: true, badgeCount: calculateTabCounts.in_return, icon: true },
    { label: 'Delivered', badge: true, badgeCount: calculateTabCounts.delivered, icon: true }
  ], [calculateTabCounts]);

  // Calculate which tabs fit and which overflow - measure from hidden container
  const calculateTabs = useCallback(() => {
    if (!tabsContainerRef.current || tabs.length === 0) return;

    // Find the hidden tabs container for measurement
    const hiddenContainer = tabsContainerRef.current.querySelector('[data-slot="tabs-list"]') as HTMLElement;
    if (!hiddenContainer) {
      // If hidden container not found, show all tabs initially
      setVisibleTabs(tabs);
      setHiddenTabs([]);
      setShowOverflowDropdown(false);
      return;
    }

    const containerWidth = tabsContainerRef.current.offsetWidth || 0;
    const dropdownButtonWidth = 120; // Width reserved for dropdown button
    const availableWidth = containerWidth - dropdownButtonWidth;
    
    const tabElements = hiddenContainer.querySelectorAll('[data-slot="tabs-trigger"]');
    
    if (tabElements.length === 0) {
      setVisibleTabs(tabs);
      setHiddenTabs([]);
      setShowOverflowDropdown(false);
      return;
    }
    
    let usedWidth = 0;
    const visible: typeof tabs = [];
    const hidden: typeof tabs = [];
    
    tabElements.forEach((tabElement, index) => {
      if (index >= tabs.length) return;
      
      const tabWidth = (tabElement as HTMLElement).offsetWidth || 0;
      
      if (usedWidth + tabWidth <= availableWidth) {
        visible.push(tabs[index]);
        usedWidth += tabWidth;
      } else {
        hidden.push(tabs[index]);
      }
    });
    
    // If all tabs fit, ensure we show them all
    if (hidden.length === 0 && visible.length < tabs.length) {
      const remaining = tabs.slice(visible.length);
      visible.push(...remaining);
    }
    
    setVisibleTabs(visible);
    setHiddenTabs(hidden);
    setShowOverflowDropdown(hidden.length > 0);
  }, [tabs]);

  // Initialize visible tabs when tabs are ready
  useEffect(() => {
    if (tabs.length > 0 && visibleTabs.length === 0) {
      setVisibleTabs(tabs);
    }
  }, [tabs]);

  useEffect(() => {
    // Delay to ensure DOM has rendered before measuring
    const timeoutId = setTimeout(() => {
      calculateTabs();
    }, 100);
    
    const handleResize = () => {
      setTimeout(() => {
        calculateTabs();
      }, 50);
    };
    
    window.addEventListener('resize', handleResize);
    
    const resizeObserver = new ResizeObserver(() => {
      setTimeout(() => {
        calculateTabs();
      }, 50);
    });
    
    if (tabsContainerRef.current) {
      resizeObserver.observe(tabsContainerRef.current);
    }
    
    if (tabsListRef.current) {
      resizeObserver.observe(tabsListRef.current);
    }
    
    return () => {
      clearTimeout(timeoutId);
      window.removeEventListener('resize', handleResize);
      resizeObserver.disconnect();
    };
  }, [calculateTabs, tabs, selectedTab]);

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
      title: (
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
          <Checkbox
            checked={selectAll}
            onChange={(event) => {
              setSelectAll(event.target.checked);
              if (event.target.checked) {
                const allIds = (activeFilters.size > 0 ? filteredJourneys : journeys).map(j => j.journey_id);
                setSelectedJourneyIds(allIds);
              } else {
                setSelectedJourneyIds([]);
              }
            }}
          />
          <Star style={{ width: '16px', height: '16px', color: 'var(--secondary)', flexShrink: 0 }} />
        </div>
      ) as any,
      width: '48px',
      render: (_: any, record: Journey) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
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
          <Star style={{ width: '16px', height: '16px', color: 'var(--secondary)', flexShrink: 0 }} />
        </div>
      )
    },
    {
      key: 'feed_unique_id',
      title: 'Feed Unique ID',
      width: '200px',
      render: (_: any, record: Journey) => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
          <div style={{ 
            overflow: 'hidden', 
            textOverflow: 'ellipsis', 
            display: '-webkit-box', 
            WebkitLineClamp: 2, 
            WebkitBoxOrient: 'vertical',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--primary)'
          }}>
            {record.feed_unique_id}
          </div>
          <Button variant="link" style={{ padding: 0, height: 'auto', fontSize: 'var(--font-size-sm)' }}>View ID's</Button>
        </div>
      )
    },
    {
      key: 'from',
      title: 'From',
      width: '200px',
      render: (_: any, record: Journey) => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
          <div style={{ 
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-2)',
            flexWrap: 'wrap',
            lineHeight: 'var(--line-height-normal)'
          }}>
            <span style={{ 
              overflow: 'hidden', 
              textOverflow: 'ellipsis', 
              whiteSpace: 'nowrap',
              fontSize: 'var(--font-size-sm)',
              color: 'var(--primary)',
              flex: 1,
              minWidth: 0
            }}>
              {record.origin_display}
            </span>
            <Badge variant="normal">+1P</Badge>
          </div>
          <div style={{ 
            overflow: 'hidden', 
            textOverflow: 'ellipsis', 
            display: '-webkit-box', 
            WebkitLineClamp: 2, 
            WebkitBoxOrient: 'vertical',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)'
          }}>
            {record.origin_company_display}
          </div>
        </div>
      )
    },
    {
      key: 'to',
      title: 'To',
      width: '200px',
      render: (_: any, record: Journey) => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
          <div style={{ 
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-2)',
            flexWrap: 'wrap',
            lineHeight: 'var(--line-height-normal)'
          }}>
            <span style={{ 
              overflow: 'hidden', 
              textOverflow: 'ellipsis', 
              whiteSpace: 'nowrap',
              fontSize: 'var(--font-size-sm)',
              color: 'var(--primary)',
              flex: 1,
              minWidth: 0
            }}>
              {record.destination_display}
            </span>
            <Badge variant="normal">+3D</Badge>
          </div>
          <div style={{ 
            overflow: 'hidden', 
            textOverflow: 'ellipsis', 
            display: '-webkit-box', 
            WebkitLineClamp: 2, 
            WebkitBoxOrient: 'vertical',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)'
          }}>
            {record.destination_company_display}
          </div>
        </div>
      )
    },
    {
      key: 'vehicle',
      title: 'Vehicle Info',
      width: '200px',
      render: (_: any, record: Journey) => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
          <div style={{ 
            overflow: 'hidden', 
            textOverflow: 'ellipsis', 
            display: '-webkit-box', 
            WebkitLineClamp: 2, 
            WebkitBoxOrient: 'vertical',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--primary)'
          }}>
            {record.vehicle_number}
          </div>
          <div style={{ 
            overflow: 'hidden', 
            textOverflow: 'ellipsis', 
            display: '-webkit-box', 
            WebkitLineClamp: 2, 
            WebkitBoxOrient: 'vertical',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)'
          }}>
            {record.transporter_name}
          </div>
        </div>
      )
    },
    {
      key: 'trip',
      title: 'Trip Info',
      width: '200px',
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
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 'var(--space-2)',
              lineHeight: 'var(--line-height-normal)',
              overflow: 'hidden'
            }}>
              <div style={{ flexShrink: 0 }}>{getTripIcon(record.trip_type_display)}</div>
              <span style={{ 
                overflow: 'hidden', 
                textOverflow: 'ellipsis', 
                whiteSpace: 'nowrap',
                fontSize: 'var(--font-size-sm)',
                color: 'var(--primary)'
              }}>
                {record.trip_type_display}
              </span>
            </div>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 'var(--space-2)',
              lineHeight: 'var(--line-height-normal)',
              overflow: 'hidden'
            }}>
              <CheckCircle2 style={{ width: '14px', height: '14px', color: 'var(--positive)', flexShrink: 0 }} />
              <span style={{ 
                overflow: 'hidden', 
                textOverflow: 'ellipsis', 
                whiteSpace: 'nowrap',
                fontSize: 'var(--font-size-sm)',
                color: 'var(--primary)'
              }}>
                {record.trip_id}
              </span>
            </div>
          </div>
        );
      }
    },
    {
      key: 'status',
      title: 'Status',
      width: '200px',
      render: (_: any, record: Journey) => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 'var(--space-2)',
            lineHeight: 'var(--line-height-normal)',
            overflow: 'hidden'
          }}>
            <MapPin style={{ width: '14px', height: '14px', color: 'var(--secondary)', flexShrink: 0 }} />
            <span style={{ 
              overflow: 'hidden', 
              textOverflow: 'ellipsis', 
              whiteSpace: 'nowrap',
              fontSize: 'var(--font-size-sm)',
              color: 'var(--primary)'
            }}>
              {record.status_display}
            </span>
          </div>
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 'var(--space-2)',
            lineHeight: 'var(--line-height-normal)',
            overflow: 'hidden'
          }}>
            <MapPin style={{ width: '14px', height: '14px', color: 'var(--secondary)', flexShrink: 0 }} />
            <span style={{ 
              overflow: 'hidden', 
              textOverflow: 'ellipsis', 
              whiteSpace: 'nowrap',
              fontSize: 'var(--font-size-sm)',
              color: 'var(--secondary)'
            }}>
              {record.current_location_display}
            </span>
          </div>
        </div>
      )
    },
    {
      key: 'sla',
      title: 'SLA',
      width: '200px',
      render: (_: any, record: Journey) => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 'var(--space-2)',
            lineHeight: 'var(--line-height-normal)',
            overflow: 'hidden'
          }}>
            {record.sla_status === 'on_time' ? (
              <CheckCircle2 style={{ width: '16px', height: '16px', color: 'var(--positive)', flexShrink: 0 }} />
            ) : (
              <Clock style={{ width: '16px', height: '16px', color: 'var(--critical)', flexShrink: 0 }} />
            )}
            <span style={{ 
              color: record.sla_status === 'on_time' ? 'var(--positive)' : 'var(--critical)',
              overflow: 'hidden', 
              textOverflow: 'ellipsis', 
              whiteSpace: 'nowrap',
              fontSize: 'var(--font-size-sm)',
              fontWeight: 'var(--font-weight-medium)'
            }}>
              {record.sla_status_display}
            </span>
          </div>
          <div style={{ 
            overflow: 'hidden', 
            textOverflow: 'ellipsis', 
            display: '-webkit-box', 
            WebkitLineClamp: 2, 
            WebkitBoxOrient: 'vertical',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)'
          }}>
            {record.eta_display}
          </div>
        </div>
      )
    },
    {
      key: 'alerts',
      title: 'Alerts',
      width: '200px',
      render: (_: any, record: Journey) => {
        if (!record.alert_type) return null;
        
        const alertLabels: Record<string, string> = {
          long_stoppage: 'Long Stoppage',
          route_deviation: 'Route Deviation',
          transit_delay: 'Transit Delay'
        };
        
        return (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingLeft: 'var(--space-4)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', flexWrap: 'wrap' }}>
              <Badge variant="danger" style={{ 
                height: '24px', 
                borderRadius: '6px', 
                padding: '4px 8px',
                fontSize: 'var(--font-size-xs)',
                fontWeight: 'var(--font-weight-medium)'
              }}>
                {alertLabels[record.alert_type] || record.alert_type}
              </Badge>
              <span style={{ 
                fontSize: 'var(--font-size-sm)',
                color: 'var(--secondary)',
                whiteSpace: 'nowrap'
              }}>
                {record.alert_time_display || '1 hour ago'}
              </span>
            </div>
          </div>
        );
      }
    },
    {
      key: 'actions',
      title: 'Actions',
      width: '100px',
      render: () => (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 'var(--space-2)', paddingRight: 'var(--space-4)' }}>
          <div style={{ 
            width: '32px', 
            height: '32px', 
            borderRadius: '50%', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            cursor: 'pointer',
            transition: 'background-color var(--transition-fast)'
          }}
          onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = 'var(--surface-hover)'; }}
          onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; }}
          >
            <MoreHorizontal style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
          </div>
        </div>
      )
    }
  ];

  return (
    <div style={{ backgroundColor: 'var(--bg-secondary)', minHeight: '100vh' }}>
      <AppHeader onOpenNavigation={onOpenNavigation} />
      
      <div style={{ backgroundColor: 'var(--bg-primary)', paddingTop: 'var(--space-8)', paddingLeft: 'var(--space-8)', paddingRight: 'var(--space-8)', paddingBottom: 'var(--space-8)' }}>
        {/* Title Bar + Filter Bar */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-6)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Home style={{ width: '28px', height: '28px', color: 'var(--primary)' }} />
            <h1 style={{ margin: 0, fontSize: 'var(--font-size-xl)', fontWeight: 'var(--font-weight-semibold)', color: 'var(--primary)', fontFamily: 'var(--font-family-primary)' }}>My Journeys</h1>
          </div>

          {/* Filter Bar */}
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 'var(--space-6)',
            height: 'var(--component-height-md)'
          }}>
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
              style={{ width: '280px', flexShrink: 0 }}
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
              style={{ width: '300px', flexShrink: 0 }}
            />
            
            <Button variant="primary" icon="calendar">Add Journey</Button>
          </div>
        </div>

        {/* Tabs + View Toggle */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-6)' }}>
          <div 
            ref={tabsContainerRef}
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 'var(--space-2)',
              flex: 1,
              minWidth: 0,
              position: 'relative',
              whiteSpace: 'nowrap',
              borderBottom: '1px solid var(--border-primary)',
              padding: '0 var(--space-2)',
              overflow: 'visible'
            }}
          >
            {/* Hidden tabs for measurement - render all tabs invisibly */}
            <div 
              style={{ 
                position: 'absolute',
                visibility: 'hidden',
                opacity: 0,
                pointerEvents: 'none',
                width: '100%',
                whiteSpace: 'nowrap',
                zIndex: -1
              }}
            >
              <Tabs tabs={tabs} activeTab={selectedTab} onChange={() => {}} />
            </div>
            
            <div 
              ref={tabsListRef}
              style={{ 
                display: 'flex', 
                flex: 1,
                minWidth: 0,
                overflow: 'hidden',
                whiteSpace: 'nowrap',
                marginRight: showOverflowDropdown && hiddenTabs.length > 0 ? 'var(--space-2)' : 0
              }}
            >
              <style>{`
                [data-slot="tabs-list"] {
                  white-space: nowrap !important;
                  overflow-x: visible !important;
                  flex-wrap: nowrap !important;
                  display: flex !important;
                }
                [data-slot="tabs-trigger"] {
                  white-space: nowrap !important;
                  flex-shrink: 0 !important;
                  overflow: visible !important;
                  min-width: fit-content !important;
                }
                [data-slot="tabs-trigger"] * {
                  white-space: nowrap !important;
                }
                [data-slot="tabs-trigger"] span {
                  white-space: nowrap !important;
                  overflow: visible !important;
                  text-overflow: clip !important;
                  word-break: keep-all !important;
                  display: inline-block !important;
                }
              `}</style>
              <Tabs 
                tabs={visibleTabs.length > 0 ? visibleTabs : tabs} 
                activeTab={(() => {
                  const tabsToRender = visibleTabs.length > 0 ? visibleTabs : tabs;
                  const idx = tabsToRender.findIndex((t) => {
                    const originalIndex = tabs.findIndex(origTab => 
                      origTab.label === t.label && 
                      origTab.badge === t.badge && 
                      origTab.badgeCount === t.badgeCount
                    );
                    return originalIndex === selectedTab;
                  });
                  return idx >= 0 ? idx : (hiddenTabs.some((tab) => {
                    const originalIndex = tabs.findIndex(t => 
                      t.label === tab.label && 
                      t.badge === tab.badge && 
                      t.badgeCount === tab.badgeCount
                    );
                    return originalIndex === selectedTab;
                  }) ? -1 : 0);
                })()} 
                onChange={(index: number) => {
                  const tabsToRender = visibleTabs.length > 0 ? visibleTabs : tabs;
                  if (index >= 0 && index < tabsToRender.length) {
                    const tab = tabsToRender[index];
                    const originalIndex = tabs.findIndex(t => 
                      t.label === tab.label && 
                      t.badge === tab.badge && 
                      t.badgeCount === tab.badgeCount
                    );
                    if (originalIndex >= 0) {
                      setSelectedTab(originalIndex);
                    }
                  }
                }} 
              />
            </div>
            {showOverflowDropdown && hiddenTabs.length > 0 && (
              <div style={{ flexShrink: 0, zIndex: 10 }}>
                <Dropdown
                  options={hiddenTabs.map((tab) => {
                    const originalIndex = tabs.findIndex(t => 
                      t.label === tab.label && 
                      t.badge === tab.badge && 
                      t.badgeCount === tab.badgeCount
                    );
                    return {
                      value: String(originalIndex >= 0 ? originalIndex : 0),
                      label: `${tab.label}${tab.badge ? ` ${tab.badgeCount || 0}` : ''}`
                    };
                  })}
                  placeholder="More"
                  value={(() => {
                    const isHidden = hiddenTabs.some((tab) => {
                      const originalIndex = tabs.findIndex(t => 
                        t.label === tab.label && 
                        t.badge === tab.badge && 
                        t.badgeCount === tab.badgeCount
                      );
                      return originalIndex === selectedTab;
                    });
                    return isHidden ? String(selectedTab) : undefined;
                  })()}
                  onChange={(value) => {
                    if (value) {
                      setSelectedTab(parseInt(value));
                    }
                  }}
                />
              </div>
            )}
          </div>
          
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
        <div className="quick-filter-scroll" style={{ marginBottom: 'var(--space-6)' }}>
          <QuickFilters 
            className="quick-filter-row"
            filters={quickFilters.map(filter => ({
              ...filter,
              selected: activeFilters.has(filter.id),
              selectedOption: (Array.from(activeFilters) as string[])
                .find((f: string) => f.startsWith(`${filter.id}:`))?.split(':')[1]
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
          marginBottom: 'var(--space-6)',
          padding: 'var(--space-3) var(--space-4)',
          backgroundColor: 'var(--bg-primary)',
          borderRadius: 'var(--radius-md)'
        }}>
          <div style={{ 
            color: 'var(--primary)', 
            fontSize: 'var(--font-size-md)', 
            fontWeight: 'var(--font-weight-semibold)',
            fontFamily: 'var(--font-family-primary)'
          }}>
            {selectedJourneyIds.length > 0 ? `${selectedJourneyIds.length} journeys selected · ` : ''}
            {activeFilters.size > 0 ? filteredJourneys.length : journeys.length} journeys available
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: 0, minWidth: '32px' }}>
              <Star style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: 0, minWidth: '32px' }}>
              <Download style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: 0, minWidth: '32px' }}>
              <Filter style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: 0, minWidth: '32px' }}>
              <LayoutList style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: 0, minWidth: '32px' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ color: 'var(--secondary)' }}>
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
              padding: 'var(--space-1) var(--space-2)',
              height: '32px'
            }}>
              <Button
                variant="text"
                style={{ width: '24px', height: '24px', padding: 0 }}
                onClick={() => setPage((prev) => Math.max(1, prev - 1))}
              >
                <ChevronLeft style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
              </Button>
              <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)', minWidth: '20px', textAlign: 'center' }}>
                {page}
              </span>
              <Button 
                variant="text"
                style={{ width: '24px', height: '24px', padding: 0 }}
                onClick={() => setPage((prev) => prev + 1)}
              >
                <ChevronRight style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
              </Button>
            </div>
          </div>
        </div>

         {/* Table */}
         <div style={{ overflowX: 'auto', width: '100%', position: 'relative' }}>
           <style>{`
             table[data-slot="table"] th:first-child,
             table[data-slot="table"] td:first-child {
               position: sticky;
               left: 0;
               z-index: 10;
               background-color: var(--bg-primary);
               width: 48px !important;
               min-width: 48px !important;
               max-width: 48px !important;
             }
             table[data-slot="table"] thead th:first-child {
               background-color: var(--bg-secondary);
               z-index: 11;
             }
             table[data-slot="table"] th:last-child,
             table[data-slot="table"] td:last-child {
               position: sticky;
               right: 0;
               z-index: 10;
               background-color: var(--bg-primary);
               width: 100px !important;
               min-width: 100px !important;
               max-width: 100px !important;
             }
             table[data-slot="table"] thead th:last-child {
               background-color: var(--bg-secondary);
               z-index: 11;
             }
             table[data-slot="table"] thead th:not(:first-child):not(:last-child),
             table[data-slot="table"] tbody td:not(:first-child):not(:last-child) {
               width: 200px !important;
               min-width: 200px !important;
               max-width: 200px !important;
             }
             table[data-slot="table"] thead th {
               height: 72px;
               padding: var(--space-4);
               background-color: var(--bg-secondary);
               text-align: left !important;
               vertical-align: middle !important;
             }
             table[data-slot="table"] thead th:not(:first-child):not(:last-child) {
               padding-left: var(--space-4) !important;
             }
             table[data-slot="table"] thead th:not(:first-child):not(:last-child) span,
             table[data-slot="table"] thead th:not(:first-child):not(:last-child) > * {
               padding-left: var(--space-4) !important;
               margin-left: 0 !important;
               display: block !important;
             }
             table[data-slot="table"] tbody td:not(:first-child):not(:last-child) {
               padding-left: var(--space-4) !important;
               padding-right: var(--space-4) !important;
               padding-top: var(--space-4) !important;
               padding-bottom: var(--space-4) !important;
             }
             table[data-slot="table"] tbody td:not(:first-child):not(:last-child) > div {
               padding-left: var(--space-4) !important;
               margin-left: 0 !important;
             }
             table[data-slot="table"] tbody tr {
               transition: background-color var(--transition-fast);
             }
             table[data-slot="table"] tbody tr:hover {
               background-color: var(--surface-hover);
             }
           `}</style>
           <Table columns={columns} data={activeFilters.size > 0 ? filteredJourneys : journeys} />
         </div>
      </div>
    </div>
  );
}
