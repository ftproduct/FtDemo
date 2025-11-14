import { useState, useEffect } from 'react';
import '../../styles/globals.css';
import { Button, Tabs } from 'ft-design-system/ai';
import { fetchJourneys, fetchJourneyCountsByStatus, type Journey, type JourneyFilters } from '../../api/journeys';

export default function MyJourneys() {
  const [journeys, setJourneys] = useState<Journey[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<string>('all');
  const [journeyCounts, setJourneyCounts] = useState<Record<string, number>>({});
  const [totalCount, setTotalCount] = useState(0);

  // Map tab labels to database tab_status values
  const tabStatusMap: Record<string, string | undefined> = {
    'all': undefined,
    'planning': 'planned',
    'in_transit': 'in_transit',
    'completed': 'delivered',
    'invoiced': 'delivered', // Assuming invoiced journeys are delivered
  };

  // Fetch journey counts by status
  useEffect(() => {
    const loadCounts = async () => {
      try {
        const counts = await fetchJourneyCountsByStatus();
        const countsMap: Record<string, number> = {};
        let total = 0;
        
        counts.forEach(({ tab_status, count }) => {
          countsMap[tab_status] = parseInt(String(count));
          total += parseInt(String(count));
        });
        
        setJourneyCounts(countsMap);
        setTotalCount(total);
      } catch (err) {
        console.error('Failed to load journey counts:', err);
      }
    };
    
    loadCounts();
  }, []);

  // Fetch journeys when tab changes
  useEffect(() => {
    const loadJourneys = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const filters: JourneyFilters = {
          tab_status: tabStatusMap[selectedTab],
          limit: 50,
          offset: 0,
        };
        
        const response = await fetchJourneys(filters);
        setJourneys(response.journeys);
        setTotalCount(response.pagination.total);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load journeys');
        console.error('Error loading journeys:', err);
      } finally {
        setLoading(false);
      }
    };
    
    loadJourneys();
  }, [selectedTab]);

  const journeyTabs = [
    { 
      label: 'All Journeys', 
      icon: 'grid', 
      badge: totalCount > 0 ? String(totalCount) : undefined 
    },
    { 
      label: 'Planning', 
      icon: 'calendar', 
      badge: journeyCounts['planned'] ? String(journeyCounts['planned']) : undefined 
    },
    { 
      label: 'In Transit', 
      icon: 'truck', 
      badge: journeyCounts['in_transit'] ? String(journeyCounts['in_transit']) : undefined 
    },
    { 
      label: 'Completed', 
      icon: 'check-circle', 
      badge: journeyCounts['delivered'] ? String(journeyCounts['delivered']) : undefined 
    },
    { 
      label: 'Invoiced', 
      icon: 'file-text', 
      badge: journeyCounts['delivered'] ? String(journeyCounts['delivered']) : undefined 
    },
  ];

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-secondary)' }}>
      {/* Header */}
      <div 
        className="border-b"
        style={{ 
          borderColor: 'var(--border-primary)',
          backgroundColor: 'var(--bg-primary)'
        }}
      >
        <div style={{ 
          maxWidth: '1440px',
          margin: '0 auto',
          padding: 'var(--space-6) var(--space-5)'
        }}>
          {/* Breadcrumb */}
          <div style={{ marginBottom: 'var(--space-4)' }}>
            <nav style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
              <a 
                href="#" 
                style={{ 
                  color: 'var(--secondary)', 
                  fontSize: 'var(--font-size-sm)',
                  textDecoration: 'none'
                }}
              >
                Home
              </a>
              <span style={{ color: 'var(--tertiary)' }}>/</span>
              <span style={{ 
                color: 'var(--primary)', 
                fontSize: 'var(--font-size-sm)',
                fontWeight: 'var(--font-weight-medium)'
              }}>
                My Journeys
              </span>
            </nav>
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              <h1 style={{ 
                fontSize: 'var(--font-size-xxl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--primary)'
              }}>
                My Journeys
              </h1>
              <p style={{ 
                color: 'var(--secondary)',
                fontSize: 'var(--font-size-md)'
              }}>
                Track and manage your freight journeys from planning to invoicing
              </p>
            </div>
            <Button 
              variant="primary"
              icon="plus"
              className="bg-[var(--button-primary-bg)] text-[var(--button-primary-text)] rounded-lg px-5 py-3"
              style={{
                backgroundColor: 'var(--button-primary-bg)',
                color: 'var(--button-primary-text)',
                borderRadius: 'var(--radius-md)'
              }}
            >
              Create Journey
            </Button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div style={{ 
        maxWidth: '1440px',
        margin: '0 auto',
        padding: 'var(--space-8) var(--space-5)'
      }}>
        <Tabs 
          tabs={journeyTabs}
          className="w-full"
          onTabChange={(index) => {
            const tabKeys = ['all', 'planning', 'in_transit', 'completed', 'invoiced'];
            setSelectedTab(tabKeys[index] || 'all');
          }}
        />

        {/* Loading State */}
        {loading && (
          <div 
            style={{
              marginTop: 'var(--space-6)',
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-16)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center'
            }}
          >
            <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-md)' }}>
              Loading journeys...
            </p>
          </div>
        )}

        {/* Error State */}
        {error && !loading && (
          <div 
            style={{
              marginTop: 'var(--space-6)',
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--critical)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-6)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center'
            }}
          >
            <p style={{ color: 'var(--critical)', fontSize: 'var(--font-size-md)', marginBottom: 'var(--space-4)' }}>
              {error}
            </p>
            <Button 
              variant="primary"
              onClick={() => {
                setSelectedTab('all');
                setError(null);
              }}
            >
              Retry
            </Button>
          </div>
        )}

        {/* Journeys List */}
        {!loading && !error && journeys.length > 0 && (
          <div style={{ marginTop: 'var(--space-6)' }}>
            <div style={{ 
              marginBottom: 'var(--space-4)',
              padding: 'var(--space-3)',
              backgroundColor: 'var(--bg-primary)',
              borderRadius: 'var(--radius-md)'
            }}>
              <p style={{ 
                color: 'var(--secondary)', 
                fontSize: 'var(--font-size-sm)' 
              }}>
                {totalCount} {totalCount === 1 ? 'Journey' : 'Journeys'} available
              </p>
            </div>
            
            <div style={{
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              overflow: 'hidden'
            }}>
              {journeys.map((journey) => (
                <div 
                  key={journey.journey_id}
                  style={{
                    padding: 'var(--space-4)',
                    borderBottom: '1px solid var(--border-secondary)',
                    display: 'grid',
                    gridTemplateColumns: '1fr 2fr 2fr 2fr 1.5fr 1.5fr 1.5fr 1.5fr 1fr',
                    gap: 'var(--space-4)',
                    alignItems: 'center'
                  }}
                >
                  <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)' }}>
                    {journey.feed_unique_id || journey.journey_id}
                  </div>
                  <div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)', fontWeight: 'var(--font-weight-medium)' }}>
                      {journey.origin_display}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {journey.origin_company_display}
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)', fontWeight: 'var(--font-weight-medium)' }}>
                      {journey.destination_display}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {journey.destination_company_display}
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)' }}>
                      {journey.vehicle_number}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {journey.transporter_name}
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)' }}>
                      {journey.trip_type_display}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--positive)' }}>
                      {journey.trip_id}
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--primary)' }}>
                      {journey.status_display}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {journey.current_location_display}
                    </div>
                  </div>
                  <div>
                    <div style={{ 
                      fontSize: 'var(--font-size-sm)', 
                      color: journey.sla_status === 'on_time' ? 'var(--positive)' : 'var(--critical)',
                      fontWeight: 'var(--font-weight-medium)'
                    }}>
                      {journey.sla_status_display}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                      {journey.eta_display}
                    </div>
                  </div>
                  <div>
                    {journey.alert_type && (
                      <div style={{
                        display: 'inline-block',
                        padding: '2px 6px',
                        borderRadius: 'var(--radius-sm)',
                        backgroundColor: 'var(--critical-light)',
                        fontSize: 'var(--font-size-xs)',
                        color: 'var(--critical)',
                        fontWeight: 'var(--font-weight-medium)',
                        marginBottom: 'var(--space-1)'
                      }}>
                        {journey.alert_type.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                      </div>
                    )}
                    {journey.alert_time_display && (
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>
                        {journey.alert_time_display}
                      </div>
                    )}
                  </div>
                  <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                    <button style={{ 
                      padding: 'var(--space-2)', 
                      border: 'none', 
                      background: 'transparent', 
                      cursor: 'pointer' 
                    }}>
                      •••
                    </button>
                    <button style={{ 
                      padding: 'var(--space-2)', 
                      border: 'none', 
                      background: 'transparent', 
                      cursor: 'pointer' 
                    }}>
                      →
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && journeys.length === 0 && (
          <div 
            style={{
              marginTop: 'var(--space-6)',
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-16)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center'
            }}
          >
            <div style={{ 
              width: '64px', 
              height: '64px', 
              marginBottom: 'var(--space-4)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <svg 
                width="64" 
                height="64" 
                viewBox="0 0 24 24" 
                fill="none" 
                stroke="var(--tertiary)" 
                strokeWidth="1.5"
              >
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                <polyline points="3.29 7 12 12 20.71 7" />
                <line x1="12" y1="22" x2="12" y2="12" />
              </svg>
            </div>
            <h3 style={{ 
              fontSize: 'var(--font-size-lg)',
              fontWeight: 'var(--font-weight-semibold)',
              color: 'var(--primary)',
              marginBottom: 'var(--space-2)'
            }}>
              No journeys yet
            </h3>
            <p style={{ 
              color: 'var(--secondary)',
              fontSize: 'var(--font-size-md)',
              maxWidth: '400px',
              marginBottom: 'var(--space-6)'
            }}>
              Get started by creating your first freight journey. Track shipments from planning through invoicing.
            </p>
            <Button 
              variant="primary"
              icon="plus"
              className="bg-[var(--button-primary-bg)] text-[var(--button-primary-text)] rounded-lg px-5 py-3"
              style={{
                backgroundColor: 'var(--button-primary-bg)',
                color: 'var(--button-primary-text)',
                borderRadius: 'var(--radius-md)'
              }}
            >
              Create Your First Journey
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
