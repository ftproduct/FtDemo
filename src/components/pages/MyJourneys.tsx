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
  Card,
  Divider,
  SegmentedTabs,
  type TableColumn
} from 'ft-design-system/ai';
import { type Journey } from '../../api/journeys';
import '../../styles/globals.css';
import { mockJourneys } from '../../data/mockJourneys';
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
  Star,
  Signal,
  SignalHigh,
  SignalMedium,
  SignalLow,
  SignalZero,
  Truck,
  Package
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
  const [isMobile, setIsMobile] = useState(false);



  // Calculate counts from actual data
  const calculateTabCounts = React.useMemo(() => {
    const allJourneys = journeys.length > 0 ? journeys : mockJourneys;
    return {
      planned: allJourneys.filter((j: Journey) => j.tab_status === 'planned').length,
      en_route_to_loading: allJourneys.filter((j: Journey) => j.tab_status === 'en_route_to_loading').length,
      at_loading: allJourneys.filter((j: Journey) => j.tab_status === 'at_loading').length,
      in_transit: allJourneys.filter((j: Journey) => j.tab_status === 'in_transit').length,
      at_unloading: allJourneys.filter((j: Journey) => j.tab_status === 'at_unloading').length,
      in_return: allJourneys.filter((j: Journey) => j.tab_status === 'in_return').length,
      delivered: allJourneys.filter((j: Journey) => j.tab_status === 'delivered').length
    };
  }, [journeys]);

  const calculateFilterCounts = React.useMemo(() => {
    const allJourneys = journeys.length > 0 ? journeys : mockJourneys;
    const delayed = allJourneys.filter((j: Journey) => j.sla_status === 'delayed');
    const longStoppage = allJourneys.filter((j: Journey) => j.alert_type === 'long_stoppage').length;
    const routeDeviation = allJourneys.filter((j: Journey) => j.alert_type === 'route_deviation').length;

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
    { label: 'Planned', badge: true, badgeCount: calculateTabCounts.planned, icon: <Calendar style={{ width: '16px', height: '16px' }} /> },
    { label: 'En Route to Loading', badge: true, badgeCount: calculateTabCounts.en_route_to_loading, icon: <Truck style={{ width: '16px', height: '16px' }} /> },
    { label: 'At Loading', badge: true, badgeCount: calculateTabCounts.at_loading, icon: <Package style={{ width: '16px', height: '16px' }} /> },
    { label: 'In Transit', badge: true, badgeCount: calculateTabCounts.in_transit, icon: <MapPin style={{ width: '16px', height: '16px' }} /> },
    { label: 'At Unloading', badge: true, badgeCount: calculateTabCounts.at_unloading, icon: <Package style={{ width: '16px', height: '16px' }} /> },
    { label: 'In Return', badge: true, badgeCount: calculateTabCounts.in_return, icon: <RefreshCw style={{ width: '16px', height: '16px' }} /> },
    { label: 'Delivered', badge: true, badgeCount: calculateTabCounts.delivered, icon: <CheckCircle2 style={{ width: '16px', height: '16px' }} /> }
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

    const tabElements = hiddenContainer.querySelectorAll('[data-slot="tabs-trigger"]');

    if (tabElements.length === 0) {
      setVisibleTabs(tabs);
      setHiddenTabs([]);
      setShowOverflowDropdown(false);
      return;
    }

    // First pass: Check if ALL tabs fit within the full container width
    let totalTabsWidth = 0;
    tabElements.forEach((el) => {
      totalTabsWidth += (el as HTMLElement).offsetWidth || 0;
    });

    // Add gap spacing (tabs.length - 1 * 8px gap)
    totalTabsWidth += (tabs.length - 1) * 8;

    if (totalTabsWidth <= containerWidth) {
      setVisibleTabs(tabs);
      setHiddenTabs([]);
      setShowOverflowDropdown(false);
      return;
    }

    // Second pass: If they don't fit, calculate with reserved space for dropdown
    const availableWidth = containerWidth - dropdownButtonWidth;
    let usedWidth = 0;
    const visible: typeof tabs = [];
    const hidden: typeof tabs = [];

    tabElements.forEach((tabElement, index) => {
      if (index >= tabs.length) return;

      const tabWidth = (tabElement as HTMLElement).offsetWidth || 0;
      // Add gap if not the first item
      const gap = visible.length > 0 ? 8 : 0;

      if (usedWidth + tabWidth + gap <= availableWidth) {
        visible.push(tabs[index]);
        usedWidth += tabWidth + gap;
      } else {
        hidden.push(tabs[index]);
      }
    });

    setVisibleTabs(visible);
    setHiddenTabs(hidden);
    setShowOverflowDropdown(hidden.length > 0);
  }, [tabs]);

  // Initialize visible tabs when tabs are ready - always show all tabs initially
  useEffect(() => {
    if (tabs.length > 0 && visibleTabs.length === 0) {
      setVisibleTabs(tabs);
      setHiddenTabs([]);
      setShowOverflowDropdown(false);
    }
  }, [tabs, visibleTabs.length]);

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

  // Track screen width for responsive view
  useEffect(() => {
    const checkScreenWidth = () => {
      setIsMobile(window.innerWidth <= 800);
    };

    checkScreenWidth();
    window.addEventListener('resize', checkScreenWidth);
    return () => window.removeEventListener('resize', checkScreenWidth);
  }, []);


  // Filter journeys based on active filters
  useEffect(() => {
    let filtered = [...journeys];

    if (activeFilters.size > 0) {
      filtered = journeys.filter((journey: Journey) => {
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
      type: 'alert' as const,
      options: [
        { id: '0-6hrs', label: '0-6 hrs', count: calculateFilterCounts['0-6hrs'], type: 'alert' as const },
        { id: '6-12hrs', label: '6-12 hrs', count: calculateFilterCounts['6-12hrs'], type: 'alert' as const },
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
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 'var(--space-2)',
          width: '100%',
          padding: '0 12px'
        }}>
          <div style={{ width: '16px', height: '16px', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Checkbox
              checked={selectAll}
              onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                setSelectAll(event.target.checked);
                if (event.target.checked) {
                  const allIds = (activeFilters.size > 0 ? filteredJourneys : journeys).map((j: Journey) => j.journey_id);
                  setSelectedJourneyIds(allIds);
                } else {
                  setSelectedJourneyIds([]);
                }
              }}
            />
          </div>
          <Star style={{ width: '16px', height: '16px', color: 'var(--secondary)', flexShrink: 0 }} />
        </div>
      ) as any,
      width: 48 as any,
      render: (_: any, record: Journey) => (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 'var(--space-2)',
          width: '100%',
          padding: '0 12px'
        }}>
          <div style={{ width: '16px', height: '16px', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Checkbox
              checked={selectedJourneyIds.includes(record.journey_id)}
              onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                setSelectedJourneyIds((prev: number[]) => {
                  if (event.target.checked) {
                    return Array.from(new Set([...prev, record.journey_id]));
                  }
                  return prev.filter((id: number) => id !== record.journey_id);
                });
              }}
            />
          </div>
          <Star style={{ width: '16px', height: '16px', color: 'var(--secondary)', flexShrink: 0 }} />
        </div>
      )
    },
    {
      key: 'feed_unique_id',
      title: 'Feed Unique ID',
      width: 200 as any,
      render: (_: any, record: Journey) => (
        <div style={{
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-2)',
          padding: 0
        }}>
          <div style={{
            minWidth: 0,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--primary)'
          }}>
            {record.feed_unique_id}
          </div>
          <Button variant="link" style={{
            padding: 0,
            height: 'auto',
            minWidth: 0,
            fontSize: 'var(--font-size-sm)',
            color: '#1890FF',
            fontWeight: '500'
          }}>View ID's</Button>
        </div>
      )
    },
    {
      key: 'from',
      title: 'From',
      width: 200 as any,
      render: (_: any, record: Journey) => (
        <div style={{
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-2)',
          padding: 0
        }}>
          <div style={{
            minWidth: 0,
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
            <Badge variant="normal" style={{ alignSelf: 'center', flexShrink: 0, borderRadius: '12px', padding: '2px 8px', fontSize: '11px', fontWeight: 500, backgroundColor: '#F3F4F6', color: '#4B5563' }}>+1P</Badge>
          </div>
          <div style={{
            minWidth: 0,
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
      width: 200 as any,
      render: (_: any, record: Journey) => (
        <div style={{
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-2)',
          padding: 0
        }}>
          <div style={{
            minWidth: 0,
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
            <Badge variant="normal" style={{ alignSelf: 'center', flexShrink: 0, borderRadius: '12px', padding: '2px 8px', fontSize: '11px', fontWeight: 500, backgroundColor: '#F3F4F6', color: '#4B5563' }}>+3D</Badge>
          </div>
          <div style={{
            minWidth: 0,
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
      width: 200 as any,
      render: (_: any, record: Journey) => (
        <div style={{
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-2)',
          padding: 0
        }}>
          <div style={{
            minWidth: 0,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
            lineHeight: 'var(--line-height-normal)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--primary)'
          }}>
            {record.vehicle_number}
          </div>
          <div style={{
            minWidth: 0,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
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
      width: 200 as any,
      render: (_: any, record: Journey) => {
        const getTripIcon = (type: string) => {
          if (type === 'SIM') {
            return <SignalHigh style={{ width: '16px', height: '16px', color: 'var(--positive)' }} />;
          }
          if (type === 'GPS') {
            return <MapPin style={{ width: '20px', height: '20px', color: 'var(--neutral)' }} />;
          }
          if (type === 'Fastag') {
            return <div style={{ width: '16px', height: '16px', borderRadius: '2px', background: '#722ed1', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'white', fontWeight: 600 }}>F</div>;
          }
          return null;
        };

        return (
          <div style={{
            minWidth: 0,
            display: 'flex',
            flexDirection: 'column',
            gap: 'var(--space-2)',
            padding: 0
          }}>
            <div style={{
              minWidth: 0,
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--space-2)',
              lineHeight: 'var(--line-height-normal)',
              overflow: 'hidden'
            }}>
              <div style={{ flexShrink: 0 }}>{getTripIcon(record.trip_type_display)}</div>
              <span style={{
                minWidth: 0,
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
              minWidth: 0,
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--space-2)',
              lineHeight: 'var(--line-height-normal)',
              overflow: 'hidden'
            }}>
              <CheckCircle2 style={{ width: '14px', height: '14px', color: 'var(--positive)', flexShrink: 0 }} />
              <span style={{
                minWidth: 0,
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
      width: 200 as any,
      render: (_: any, record: Journey) => (
        <div style={{
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-2)',
          padding: 0
        }}>
          <div style={{
            minWidth: 0,
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-2)',
            lineHeight: 'var(--line-height-normal)',
            overflow: 'hidden'
          }}>
            <MapPin style={{ width: '20px', height: '20px', color: 'var(--secondary)', flexShrink: 0 }} />
            <span style={{
              minWidth: 0,
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
            minWidth: 0,
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-2)',
            lineHeight: 'var(--line-height-normal)',
            overflow: 'hidden'
          }}>
            <MapPin style={{ width: '20px', height: '20px', color: 'var(--secondary)', flexShrink: 0 }} />
            <span style={{
              minWidth: 0,
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
      width: 200 as any,
      render: (_: any, record: Journey) => (
        <div style={{
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-2)',
          padding: 0
        }}>
          <div style={{
            minWidth: 0,
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-2)',
            lineHeight: 'var(--line-height-normal)',
            overflow: 'hidden'
          }}>
            {record.sla_status === 'on_time' ? (
              <CheckCircle2 style={{ width: '20px', height: '20px', color: '#00C853', flexShrink: 0 }} />
            ) : (
              <Clock style={{ width: '20px', height: '20px', color: '#D32F2F', flexShrink: 0 }} />
            )}
            <span style={{
              minWidth: 0,
              color: record.sla_status === 'on_time' ? '#00C853' : '#D32F2F',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              fontSize: 'var(--font-size-sm)',
              fontWeight: 700
            }}>
              {record.sla_status_display}
            </span>
          </div>
          <div style={{
            minWidth: 0,
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
      width: 200 as any,
      render: (_: any, record: Journey) => {
        if (!record.alert_type) return null;

        const alertLabels: Record<string, string> = {
          long_stoppage: 'Long Stoppage',
          route_deviation: 'Route Deviation',
          transit_delay: 'Transit Delay'
        };

        return (
          <div style={{
            minWidth: 0,
            display: 'flex',
            flexDirection: 'column',
            gap: 'var(--space-2)',
            padding: 0
          }}>
            <div style={{
              minWidth: 0,
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--space-2)',
              flexWrap: 'wrap'
            }}>
              <Badge variant="danger" style={{
                height: '24px',
                borderRadius: '6px',
                padding: '4px 8px',
                fontSize: 'var(--font-size-xs)',
                fontWeight: 'var(--font-weight-medium)',
                flexShrink: 0
              }}>
                {alertLabels[record.alert_type] || record.alert_type}
              </Badge>
              <span style={{
                minWidth: 0,
                fontSize: '12px',
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
      width: 100 as any,
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
            onMouseEnter={(e: React.MouseEvent<HTMLDivElement>) => { e.currentTarget.style.backgroundColor = 'var(--surface-hover)'; }}
            onMouseLeave={(e: React.MouseEvent<HTMLDivElement>) => { e.currentTarget.style.backgroundColor = 'transparent'; }}
          >
            <MoreHorizontal style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
          </div>
        </div>
      )
    }
  ];

  // Helper function to get trip icon
  const getTripIcon = (type: string) => {
    if (type === 'SIM') {
      return <SignalHigh style={{ width: '16px', height: '16px', color: 'var(--positive)' }} />;
    }
    if (type === 'GPS') {
      return <MapPin style={{ width: '20px', height: '20px', color: 'var(--neutral)' }} />;
    }
    if (type === 'Fastag') {
      return <div style={{ width: '16px', height: '16px', borderRadius: '2px', background: '#722ed1', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'white', fontWeight: 600 }}>F</div>;
    }
    return null;
  };

  // Helper function to render journey card
  const renderJourneyCard = (journey: Journey) => {
    const alertLabels: Record<string, string> = {
      long_stoppage: 'Long Stoppage',
      route_deviation: 'Route Deviation',
      transit_delay: 'Transit Delay'
    };

    return (
      <Card
        key={journey.journey_id}
        style={{
          padding: 'var(--space-4)',
          marginBottom: 'var(--space-3)',
          border: '1px solid var(--border-primary)',
          borderRadius: 'var(--radius-lg)',
          backgroundColor: 'var(--bg-primary)'
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          {/* Header Row */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', flex: 1 }}>
              <Checkbox
                checked={selectedJourneyIds.includes(journey.journey_id)}
                onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                  setSelectedJourneyIds((prev: number[]) => {
                    if (event.target.checked) {
                      return Array.from(new Set([...prev, journey.journey_id]));
                    }
                    return prev.filter((id: number) => id !== journey.journey_id);
                  });
                }}
              />
              <Star style={{ width: '16px', height: '16px', color: 'var(--secondary)', flexShrink: 0 }} />
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 'var(--font-weight-semibold)', color: 'var(--primary)', marginBottom: 'var(--space-1)' }}>
                  {journey.feed_unique_id}
                </div>
                <Button variant="link" style={{ padding: 0, height: 'auto', fontSize: 'var(--font-size-sm)', color: 'var(--neutral)', fontWeight: '500' }}>View ID's</Button>
              </div>
            </div>
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
              onMouseEnter={(e: React.MouseEvent<HTMLDivElement>) => { e.currentTarget.style.backgroundColor = 'var(--surface-hover)'; }}
              onMouseLeave={(e: React.MouseEvent<HTMLDivElement>) => { e.currentTarget.style.backgroundColor = 'transparent'; }}
            >
              <MoreHorizontal style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </div>
          </div>

          {/* From/To Section */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
            <div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 'var(--space-1)' }}>From</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)', fontWeight: 'var(--font-weight-medium)' }}>
                  {journey.origin_display}
                </span>
                <Badge variant="normal">+1P</Badge>
              </div>
              <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)' }}>
                {journey.origin_company_display}
              </div>
            </div>
            <div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 'var(--space-1)' }}>To</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)', fontWeight: 'var(--font-weight-medium)' }}>
                  {journey.destination_display}
                </span>
                <Badge variant="normal">+3D</Badge>
              </div>
              <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)' }}>
                {journey.destination_company_display}
              </div>
            </div>
          </div>

          {/* Vehicle & Trip Info */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
            <div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 'var(--space-1)' }}>Vehicle Info</div>
              <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)', fontWeight: 'var(--font-weight-medium)', marginBottom: 'var(--space-1)' }}>
                {journey.vehicle_number}
              </div>
              <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)' }}>
                {journey.transporter_name}
              </div>
            </div>
            <div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 'var(--space-1)' }}>Trip Info</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                <div style={{ flexShrink: 0 }}>{getTripIcon(journey.trip_type_display)}</div>
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)' }}>
                  {journey.trip_type_display}
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <CheckCircle2 style={{ width: '14px', height: '14px', color: 'var(--positive)', flexShrink: 0 }} />
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)' }}>
                  {journey.trip_id}
                </span>
              </div>
            </div>
          </div>

          {/* Status & SLA */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
            <div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 'var(--space-1)' }}>Status</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                <MapPin style={{ width: '16px', height: '16px', color: 'var(--secondary)', flexShrink: 0 }} />
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)' }}>
                  {journey.status_display}
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <MapPin style={{ width: '16px', height: '16px', color: 'var(--secondary)', flexShrink: 0 }} />
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)' }}>
                  {journey.current_location_display}
                </span>
              </div>
            </div>
            <div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 'var(--space-1)' }}>SLA</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                {journey.sla_status === 'on_time' ? (
                  <CheckCircle2 style={{ width: '16px', height: '16px', color: '#00C853', flexShrink: 0 }} />
                ) : (
                  <Clock style={{ width: '16px', height: '16px', color: '#D32F2F', flexShrink: 0 }} />
                )}
                <span style={{
                  color: journey.sla_status === 'on_time' ? '#00C853' : '#D32F2F',
                  fontSize: 'var(--font-size-sm)',
                  fontWeight: 700
                }}>
                  {journey.sla_status_display}
                </span>
              </div>
              <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)' }}>
                {journey.eta_display}
              </div>
            </div>
          </div>

          {/* Alerts */}
          {journey.alert_type && (
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', flexWrap: 'wrap' }}>
                <Badge variant="danger" style={{
                  height: '24px',
                  borderRadius: '6px',
                  padding: '4px 8px',
                  fontSize: 'var(--font-size-xs)',
                  fontWeight: 'var(--font-weight-medium)'
                }}>
                  {alertLabels[journey.alert_type] || journey.alert_type}
                </Badge>
                <span style={{
                  fontSize: '12px',
                  color: 'var(--secondary)',
                  whiteSpace: 'nowrap'
                }}>
                  {journey.alert_time_display || '1 hour ago'}
                </span>
              </div>
            </div>
          )}
        </div>
      </Card>
    );
  };

  return (
    <div style={{ backgroundColor: 'var(--bg-secondary)', minHeight: '100vh' }}>
      <AppHeader onOpenNavigation={onOpenNavigation} />
      <div className="my-journeys-container" style={{ backgroundColor: 'var(--bg-primary)', paddingLeft: '20px', paddingRight: '20px' }}>
        <style>{`
        .my-journeys-container {
          border: none !important;
          border-width: 0 !important;
          border-style: none !important;
          border-top: none !important;
          border-bottom: none !important;
          border-left: none !important;
          border-right: none !important;
          outline: none !important;
          box-shadow: none !important;
        }
        /* Override DatePicker minimum width to hug content but ensure text is visible */
        .my-journeys-container [data-slot="date-picker"],
        .my-journeys-container [class*="min-w-"] {
          min-width: auto !important;
          width: fit-content !important;
        }
        /* Ensure DatePicker input text is visible */
        .my-journeys-container input[readonly][type="text"],
        .my-journeys-container input[type="text"][readonly] {
          min-width: fit-content !important;
          width: auto !important;
          overflow: visible !important;
          text-overflow: clip !important;
          white-space: nowrap !important;
        }
        /* Ensure DatePicker container shows content */
        .my-journeys-container div[class*="relative"][class*="flex"][class*="items-center"] {
          min-width: fit-content !important;
          width: fit-content !important;
        }
      `}</style>
        {/* Title Bar + Filter Bar */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '20px', paddingBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Home style={{ width: '28px', height: '28px', color: 'var(--primary)' }} />
            <h1 style={{ margin: 0, fontSize: 'var(--font-size-xl)', fontWeight: 700, color: '#1F2937', fontFamily: 'var(--font-family-primary)' }}>My Journeys</h1>
          </div>

          {/* Filter Bar */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-4)',
            height: 'var(--component-height-md)'
          }}>
            <Dropdown
              options={[
                { value: 'mdc-labs', label: 'MDC Labs, Amritsar' },
                { value: 'all', label: 'All Companies' }
              ]}
              placeholder="Select company"
              defaultValue="mdc-labs"
              style={{ height: 'var(--component-height-md)' }}
            />

            <DatePicker
              placeholder="12 Aug, 2024 → 12 Sep 2024"
              style={{ width: 'fit-content', minWidth: 'fit-content', flexShrink: 0, height: 'var(--component-height-md)', border: 'none', boxShadow: 'none' }}
            />

            <Dropdown
              options={[
                { value: 'outbound', label: 'Outbound - Source' },
                { value: 'inbound', label: 'Inbound' }
              ]}
              placeholder="Direction"
              defaultValue="outbound"
              style={{ height: 'var(--component-height-md)' }}
            />

            <Input
              placeholder="Search My Journeys"
              leadingIcon="search"
              value={searchTerm}
              onChange={(event: React.ChangeEvent<HTMLInputElement>) => setSearchTerm(event.target.value)}
              style={{ width: '300px', flexShrink: 0, height: 'var(--component-height-md)' }}
            />

            <Button variant="primary" icon="calendar" style={{ height: 'var(--component-height-md)', backgroundColor: '#1F2937', color: 'white', borderRadius: '6px' }}>Add Journey</Button>
          </div>
        </div>

        {/* Tabs + View Toggle */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--page-gap)', width: '100%', gap: 'var(--space-4)' }}>
          <div
            ref={tabsContainerRef}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--space-2)',
              flex: '1 1 0%',
              minWidth: 0,
              maxWidth: '100%',
              position: 'relative',
              whiteSpace: 'nowrap',
              padding: '0 var(--space-2)',
              overflow: 'hidden'
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
              <Tabs tabs={tabs} activeTab={selectedTab} onChange={() => { }} />
            </div>

            <div
              ref={tabsListRef}
              style={{
                display: 'flex',
                flex: '1 1 0%',
                minWidth: 0,
                maxWidth: '100%',
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
                [data-slot="tabs-trigger"][data-state="active"] {
                  box-shadow: inset 0 -2px 0 0 #1F2937 !important;
                  color: #1F2937 !important;
                  font-weight: 600 !important;
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
                  onChange={(value: string) => {
                    if (value) {
                      setSelectedTab(parseInt(value));
                    }
                  }}
                />
              </div>
            )}
          </div>
          <SegmentedTabs
            iconOnly={true}
            items={[
              { value: 'list', icon: 'list-view', label: '' },
              { value: 'map', icon: 'map', label: '' }
            ]}
            value={viewMode}
            onValueChange={(value) => setViewMode(value as 'list' | 'map')}
          />
        </div>

        {/* Quick Filters - Single and Multi-option */}
        <div className="quick-filter-scroll">
          <QuickFilters
            className="quick-filter-row"
            filters={quickFilters.map(filter => ({
              ...filter,
              selected: activeFilters.has(filter.id),
              selectedOption: (Array.from(activeFilters) as string[])
                .find((f: string) => f.startsWith(`${filter.id}:`))?.split(':')[1]
            }))}
            onFilterClick={(filterId: string, optionId?: string) => {
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
            onFilterRemove={(filterId: string, optionId?: string) => {
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
          marginBottom: 'var(--page-gap)',
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
            <Button variant="text" style={{ width: '32px', height: '32px', padding: '8px', minWidth: '32px' }}>
              <Star style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: '8px', minWidth: '32px' }}>
              <Download style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: '8px', minWidth: '32px' }}>
              <Filter style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: '8px', minWidth: '32px' }}>
              <LayoutList style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
            </Button>
            <Button variant="text" style={{ width: '32px', height: '32px', padding: '8px', minWidth: '32px' }}>
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

        {/* Table or Card View */}
        {!isMobile ? (
          <div
            className="journeys-table-wrapper"
            style={{
              overflowX: 'auto',
              width: '100%',
              position: 'relative',
              borderRadius: 'var(--radius-lg)',
              boxShadow: 'var(--shadow-sm)',
              border: '1px solid var(--border-primary)',
              backgroundColor: 'var(--bg-primary)'
            }}
          >
            <style>{`
              /* Remove Tailwind padding on table headers */
              .journeys-table-wrapper table[data-slot="table"] th {
                text-align: left !important;
                padding-top: 16px !important;
                padding-bottom: 16px !important;
                padding-left: 0 !important;
                padding-right: 0 !important;
                height: auto !important;
                box-sizing: border-box !important;
                background-color: #F9FAFB !important;
                font-weight: 600 !important;
                color: #4B5563 !important;
              }
              
              /* First column header and cells - ensure checkbox is visible */
              .journeys-table-wrapper table[data-slot="table"] th:first-child,
              .journeys-table-wrapper table[data-slot="table"] td:first-child {
                padding: 0 !important;
                width: 48px !important;
                min-width: 48px !important;
                max-width: 48px !important;
              }
              
              /* Ensure checkbox wrapper has proper width */
              .journeys-table-wrapper table[data-slot="table"] th:first-child > div,
              .journeys-table-wrapper table[data-slot="table"] td:first-child > div {
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                gap: var(--space-2) !important;
                width: 100% !important;
              }
              
              /* Ensure checkbox has proper width */
              .journeys-table-wrapper table[data-slot="table"] th:first-child [data-slot="checkbox"],
              .journeys-table-wrapper table[data-slot="table"] td:first-child [data-slot="checkbox"] {
                width: 16px !important;
                min-width: 16px !important;
                height: 16px !important;
                min-height: 16px !important;
                flex-shrink: 0 !important;
              }
              
              /* Ensure checkbox wrapper div has width */
              .journeys-table-wrapper table[data-slot="table"] th:first-child > div > div:first-child,
              .journeys-table-wrapper table[data-slot="table"] td:first-child > div > div:first-child {
                width: 16px !important;
                min-width: 16px !important;
                height: 16px !important;
                min-height: 16px !important;
                flex-shrink: 0 !important;
                display: flex !important;
              }

              /* Remove padding on table cells */
              .journeys-table-wrapper table[data-slot="table"] td {
                padding: 16px !important;
                box-sizing: border-box !important;
              }
              
              /* Truncate text in table cells - prevent wrapping to second line */
              .journeys-table-wrapper table[data-slot="table"] td {
                overflow: hidden !important;
              }
              
              /* Force truncation on first child div in cells (main text content) */
              .journeys-table-wrapper table[data-slot="table"] td > div[style*="flexDirection"][style*="column"] > div:first-child {
                white-space: nowrap !important;
                overflow: hidden !important;
                text-overflow: ellipsis !important;
               display: block !important;
                max-width: 100% !important;
                -webkit-line-clamp: 1 !important;
                -webkit-box-orient: vertical !important;
                display: -webkit-box !important;
              }
              
              /* Override any webkit-box display that allows multiple lines */
              .journeys-table-wrapper table[data-slot="table"] td > div > div[style*="-webkit-box"] {
                -webkit-line-clamp: 1 !important;
                white-space: nowrap !important;
                display: block !important;
             }
           `}</style>
            <Table
              columns={columns}
              data={activeFilters.size > 0 ? filteredJourneys : journeys}
              className="journeys-table"
            />
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
            {(activeFilters.size > 0 ? filteredJourneys : journeys).map(journey => renderJourneyCard(journey))}
          </div>
        )}
      </div>
    </div>
  );
}
