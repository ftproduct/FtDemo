// Header Component
function Header() {
  return (
    <div 
      className="flex items-center justify-between w-full"
      style={{
        padding: 'var(--space-4) var(--space-6)',
        backgroundColor: 'var(--bg-primary)',
        borderBottom: '1px solid var(--border-secondary)',
      }}
    >
      {/* Logo */}
      <div className="flex items-center" style={{ gap: 'var(--space-3)' }}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <rect x="4" y="6" width="16" height="3" rx="1" fill="var(--accent)" />
          <rect x="4" y="10" width="16" height="8" rx="2" fill="var(--accent)" />
          <rect x="7" y="13" width="3" height="3" fill="var(--bg-primary)" />
          <rect x="14" y="13" width="3" height="3" fill="var(--bg-primary)" />
        </svg>
        <span 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-lg)',
            fontWeight: 'var(--font-weight-semibold)',
            color: 'var(--primary)',
          }}
        >
          FREIGHT TIGER
        </span>
      </div>

      {/* Right Actions */}
      <div className="flex items-center" style={{ gap: 'var(--space-4)' }}>
        <span 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-lg)',
            fontWeight: 'var(--font-weight-regular)',
            color: 'var(--neutral)',
          }}
        >
          MDC Labs
        </span>
        <button 
          style={{
            width: '32px',
            height: '32px',
            borderRadius: '50%',
            backgroundColor: 'var(--bg-secondary)',
            border: 'none',
            cursor: 'pointer',
          }}
        />
      </div>
    </div>
  );
}

// Page Header with Title
function PageHeader() {
  return (
    <div 
      style={{
        padding: 'var(--space-4) var(--space-6)',
        backgroundColor: 'var(--bg-primary)',
      }}
    >
      <h1 
        style={{
          fontFamily: 'var(--font-family-primary)',
          fontSize: 'var(--font-size-xl)',
          fontWeight: 'var(--font-weight-semibold)',
          color: 'var(--primary)',
          margin: 0,
        }}
      >
        My Journeys
      </h1>
    </div>
  );
}

// Tab Component
function TabItem({ label, count, active }: { label: string; count: number; active?: boolean }) {
  return (
    <button
      className="flex items-center relative"
      style={{
        padding: '12px 16px',
        gap: 'var(--space-2)',
        backgroundColor: 'transparent',
        border: 'none',
        borderBottom: active ? '4px solid var(--primary)' : '1px solid var(--border-secondary)',
        cursor: 'pointer',
      }}
    >
      <svg width="16" height="16" viewBox="0 0 16 16" style={{ opacity: 0.6 }}>
        <circle cx="8" cy="8" r="6" fill="none" stroke="currentColor" strokeWidth="2" />
      </svg>
      <span 
        style={{
          fontFamily: 'var(--font-family-primary)',
          fontSize: 'var(--font-size-base)',
          fontWeight: active ? 'var(--font-weight-semibold)' : 'var(--font-weight-regular)',
          color: 'var(--foreground)',
        }}
      >
        {label}
      </span>
      <span 
        style={{
          fontFamily: 'var(--font-family-primary)',
          fontSize: 'var(--font-size-sm)',
          fontWeight: 'var(--font-weight-semibold)',
          color: 'var(--foreground)',
        }}
      >
        {count}
      </span>
    </button>
  );
}

// Tabs Container
function TabsBar() {
  return (
    <div 
      className="flex items-center"
      style={{
        backgroundColor: 'var(--bg-primary)',
        borderBottom: '1px solid var(--border-secondary)',
        gap: 0,
      }}
    >
      <TabItem label="Planned" count={56} />
      <TabItem label="En Route to Loading" count={56} />
      <TabItem label="At Loading" count={56} />
      <TabItem label="In Transit" count={56} active />
      <TabItem label="At Unloading" count={56} />
      <TabItem label="In Return" count={56} />
      <TabItem label="Delivered" count={56} />
    </div>
  );
}

// Filter Pills
function FilterPills() {
  const filters = [
    { label: 'Long Stoppage', count: 19, color: 'var(--critical)' },
    { label: 'Route Deviation', count: 19, color: 'var(--critical)' },
    { label: 'Delayed', count: 51, color: 'var(--critical)' },
    { label: '0-6 hrs', count: 28, color: 'var(--secondary)' },
    { label: '6-12 hrs', count: 18, color: 'var(--secondary)' },
    { label: '12+ hrs', count: 9, color: 'var(--secondary)' },
    { label: 'E Way bill', count: null, color: 'var(--neutral)' },
    { label: 'Expiring in 3 hrs', count: 28, color: 'var(--warning)' },
    { label: 'Expired', count: 18, color: 'var(--critical)' },
    { label: 'ETA', count: null, color: 'var(--secondary)' },
    { label: '6 hrs', count: 28, color: 'var(--secondary)' },
    { label: '12 hrs', count: 18, color: 'var(--secondary)' },
    { label: '24+ hrs', count: 5, color: 'var(--secondary)' },
  ];

  return (
    <div 
      className="flex items-center flex-wrap"
      style={{
        padding: 'var(--space-3) var(--space-6)',
        gap: 'var(--space-2)',
        backgroundColor: 'var(--bg-primary)',
        borderBottom: '1px solid var(--border-secondary)',
      }}
    >
      {filters.map((filter, index) => (
        <div
          key={index}
          className="flex items-center"
          style={{
            padding: '4px 8px',
            gap: 'var(--space-1)',
            borderRadius: 'var(--radius-sm)',
            border: `1px solid ${filter.color}`,
            backgroundColor: 'var(--bg-primary)',
          }}
        >
          <span 
            style={{
              fontFamily: 'var(--font-family-primary)',
              fontSize: 'var(--font-size-sm)',
              color: filter.color,
            }}
          >
            {filter.label}
          </span>
          {filter.count !== null && (
            <span 
              style={{
                fontFamily: 'var(--font-family-primary)',
                fontSize: 'var(--font-size-sm)',
                fontWeight: 'var(--font-weight-semibold)',
                color: filter.color,
              }}
            >
              {filter.count}
            </span>
          )}
        </div>
      ))}
    </div>
  );
}

// Journey Count
function JourneyCount() {
  return (
    <div 
      style={{
        padding: 'var(--space-3) var(--space-6)',
        backgroundColor: 'var(--bg-secondary)',
      }}
    >
      <span 
        style={{
          fontFamily: 'var(--font-family-primary)',
          fontSize: 'var(--font-size-sm)',
          color: 'var(--secondary)',
        }}
      >
        56 Journeys available
      </span>
    </div>
  );
}

// Table Header
function TableHeader() {
  const columns = [
    'Feed Unique ID',
    'From',
    'To',
    'Vehicle Info',
    'Trip Info',
    'Status',
    'SLA',
    'Alerts',
    'Actions',
  ];

  return (
    <div 
      className="grid"
      style={{
        gridTemplateColumns: '40px 180px 180px 180px 140px 140px 140px 140px 100px 60px',
        gap: 'var(--space-2)',
        padding: 'var(--space-3) var(--space-4)',
        backgroundColor: 'var(--secondary)',
        borderBottom: '1px solid var(--border-secondary)',
      }}
    >
      <div />
      {columns.map((col) => (
        <div 
          key={col}
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            fontWeight: 'var(--font-weight-semibold)',
            color: 'var(--bg-primary)',
          }}
        >
          {col}
        </div>
      ))}
    </div>
  );
}

// Table Row
function TableRow({ journey }: { journey: any }) {
  return (
    <div 
      className="grid items-center"
      style={{
        gridTemplateColumns: '40px 180px 180px 180px 140px 140px 140px 140px 100px 60px',
        gap: 'var(--space-2)',
        padding: 'var(--space-4)',
        backgroundColor: 'var(--bg-primary)',
        borderBottom: '1px solid var(--border-secondary)',
      }}
    >
      {/* Checkbox */}
      <input type="checkbox" />

      {/* Feed Unique ID */}
      <div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            fontWeight: 'var(--font-weight-regular)',
            color: 'var(--foreground)',
          }}
        >
          {journey.id}
        </div>
        <a 
          href="#"
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            fontWeight: 'var(--font-weight-medium)',
            color: 'var(--neutral)',
            textDecoration: 'none',
          }}
        >
          View ID's
        </a>
      </div>

      {/* From */}
      <div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--foreground)',
          }}
        >
          {journey.from}
        </div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)',
          }}
        >
          {journey.fromCompany}
        </div>
      </div>

      {/* To */}
      <div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--foreground)',
          }}
        >
          {journey.to}
        </div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)',
          }}
        >
          {journey.toCompany}
        </div>
      </div>

      {/* Vehicle Info */}
      <div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--foreground)',
          }}
        >
          {journey.vehicle}
        </div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)',
          }}
        >
          {journey.transporter}
        </div>
      </div>

      {/* Trip Info */}
      <div className="flex items-center" style={{ gap: 'var(--space-1)' }}>
        <span 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--foreground)',
          }}
        >
          {journey.tripType}
        </span>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--positive)',
          }}
        >
          {journey.tripId}
        </div>
      </div>

      {/* Status */}
      <div>
        <div 
          className="flex items-center"
          style={{
            gap: 'var(--space-1)',
            marginBottom: 'var(--space-1)',
          }}
        >
          <svg width="12" height="12" viewBox="0 0 12 12">
            <circle cx="6" cy="6" r="5" fill="var(--positive)" />
          </svg>
          <span 
            style={{
              fontFamily: 'var(--font-family-primary)',
              fontSize: 'var(--font-size-sm)',
              color: 'var(--foreground)',
            }}
          >
            {journey.status}
          </span>
        </div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)',
          }}
        >
          {journey.location}
        </div>
      </div>

      {/* SLA */}
      <div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            fontWeight: 'var(--font-weight-medium)',
            color: journey.slaStatus === 'On time' ? 'var(--positive)' : 'var(--critical)',
          }}
        >
          {journey.slaStatus}
        </div>
        <div 
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--secondary)',
          }}
        >
          {journey.eta}
        </div>
      </div>

      {/* Alerts */}
      <div>
        {journey.alert && (
          <span 
            style={{
              display: 'inline-block',
              padding: '2px 6px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--critical-light)',
              fontFamily: 'var(--font-family-primary)',
              fontSize: 'var(--font-size-sm)',
              fontWeight: 'var(--font-weight-medium)',
              color: 'var(--critical)',
            }}
          >
            {journey.alert}
          </span>
        )}
        {journey.alertTime && (
          <div 
            style={{
              fontFamily: 'var(--font-family-primary)',
              fontSize: 'var(--font-size-sm)',
              color: 'var(--secondary)',
              marginTop: 'var(--space-1)',
            }}
          >
            {journey.alertTime}
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="flex items-center" style={{ gap: 'var(--space-2)' }}>
        <button 
          style={{
            width: '32px',
            height: '32px',
            border: 'none',
            backgroundColor: 'transparent',
            cursor: 'pointer',
          }}
        >
          •••
        </button>
        <button 
          style={{
            width: '32px',
            height: '32px',
            border: 'none',
            backgroundColor: 'transparent',
            cursor: 'pointer',
          }}
        >
          →
        </button>
      </div>
    </div>
  );
}

// Mock Data
const mockJourneys = [
  {
    id: '324673-948B478-84...',
    from: 'Amritsar, Punjab',
    fromCompany: 'MDC Labs ltd',
    to: 'Mumbai, M...',
    toCompany: 'Maa kali Distribut...',
    vehicle: 'PB09 HN 6439',
    transporter: 'Yonex Transporter 〉',
    tripType: 'SIM',
    tripId: '84973-47593',
    status: 'On Road',
    location: 'Ambala, Haryana',
    slaStatus: 'On time',
    eta: 'ETA: 12:30 pm, 12 Aug',
    alert: null,
    alertTime: null,
  },
  {
    id: '324673-948B478-84...',
    from: 'Amritsar, Punjab',
    fromCompany: 'MDC Labs LTD',
    to: 'Secunderab...',
    toCompany: 'Jai Sri Ram',
    vehicle: 'KA12 AS 3421',
    transporter: 'Laal Kamal Trans... 〉',
    tripType: 'SIM',
    tripId: '84973-47593',
    status: 'At Drop',
    location: 'Ambala, Haryana',
    slaStatus: 'On time',
    eta: 'ETA: 12:30 pm, 12 Aug',
    alert: null,
    alertTime: null,
  },
  {
    id: '324673-948B478-84...',
    from: 'Amritsar, Punjab',
    fromCompany: 'MDC Labs LTD',
    to: 'Secunderabad, Tel...',
    toCompany: 'Sai Traders',
    vehicle: 'KA12 AS 3422',
    transporter: 'Laal Kamal Trans... 〉',
    tripType: 'GPS',
    tripId: '84973-47593',
    status: 'At Pickup',
    location: 'Ambala, Haryana',
    slaStatus: 'Delayed by 13 hr',
    eta: 'ETA: 12:30 pm, 12 Aug',
    alert: 'Long Stoppage',
    alertTime: '1 hour ago',
  },
  {
    id: '324673-948B478-84...',
    from: 'Amritsar, Punjab',
    fromCompany: 'MDC Labs LTD',
    to: 'Secunderabad, Tel...',
    toCompany: 'Sai Traders',
    vehicle: 'KA12 AS 3423',
    transporter: 'Laal Kamal Trans... 〉',
    tripType: 'Fastag',
    tripId: '84973-47593',
    status: 'At Drop',
    location: 'Ambala, Haryana',
    slaStatus: 'Delayed by 13 hr',
    eta: 'ETA: 12:30 pm, 12 Aug',
    alert: 'Route Deviation',
    alertTime: '1 hour ago',
  },
  {
    id: '324673-948B478-84...',
    from: 'Amritsar, Punjab',
    fromCompany: 'MDC Labs LTD',
    to: 'Siddipet, Telangana',
    toCompany: 'Jai Sri Ram',
    vehicle: 'KA12 AS 3424',
    transporter: 'Laal Kamal Trans... 〉',
    tripType: 'GPS',
    tripId: '84973-47593',
    status: 'On Road',
    location: 'at 12:30 pm, 12 Aug',
    slaStatus: 'On time',
    eta: 'at 12:30 pm, 12 Aug',
    alert: null,
    alertTime: null,
  },
  {
    id: '324673-948B478-84...',
    from: 'Amritsar, Punjab',
    fromCompany: 'MDC Labs LTD',
    to: 'Secunderabad, Tel...',
    toCompany: 'Sai Traders',
    vehicle: 'KA12 AS 3423',
    transporter: 'Laal Kamal Trans... 〉',
    tripType: 'Fastag',
    tripId: '84973-47593',
    status: 'On Road',
    location: 'Ambala, Haryana',
    slaStatus: 'Delayed by 13 hr',
    eta: 'ETA: 12:30 pm, 12 Aug',
    alert: 'Transit Delay',
    alertTime: '1 hour ago',
  },
];

// Main Component
export default function InTransit() {
  return (
    <div 
      style={{
        width: '100%',
        minHeight: '100vh',
        backgroundColor: 'var(--bg-secondary)',
      }}
    >
      <Header />
      <PageHeader />
      <TabsBar />
      <FilterPills />
      <JourneyCount />
      <TableHeader />
      {mockJourneys.map((journey, index) => (
        <TableRow key={index} journey={journey} />
      ))}
    </div>
  );
}
