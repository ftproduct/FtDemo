import '../../styles/globals.css';
import { Button, Tabs } from 'ft-design-system/ai';

export default function MyJourneys() {
  const journeyTabs = [
    { label: 'All Journeys', icon: 'grid' },
    { label: 'Planning', icon: 'calendar', badge: '3' },
    { label: 'In Transit', icon: 'truck', badge: '5' },
    { label: 'Completed', icon: 'check-circle', badge: '12' },
    { label: 'Invoiced', icon: 'file-text', badge: '8' },
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
        />

        {/* Empty State */}
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
      </div>
    </div>
  );
}
