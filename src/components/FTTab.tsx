import { ReactNode } from 'react';

interface FTTabProps {
  label: string;
  badge?: string | number;
  icon?: ReactNode;
  active?: boolean;
  onClick?: () => void;
}

function Badge({ count }: { count: string | number }) {
  return (
    <div 
      className="box-border content-stretch flex h-[24px] items-center justify-center relative rounded-[4px] shrink-0"
      style={{
        backgroundColor: 'var(--bg-primary)',
        gap: 'var(--space-2)',
        padding: '0 var(--space-1)',
      }}
      data-name="Badge"
    >
      <div 
        aria-hidden="true" 
        className="absolute inset-0 pointer-events-none rounded-[4px]" 
        style={{ border: '1px solid var(--border-primary)' }}
      />
      <p 
        className="relative shrink-0 text-nowrap whitespace-pre"
        style={{
          fontFamily: 'var(--font-family-primary)',
          fontWeight: 'var(--font-weight-semibold)',
          fontSize: 'var(--font-size-sm)',
          lineHeight: '1.4',
          color: 'var(--foreground)',
        }}
      >
        {count}
      </p>
    </div>
  );
}

export function FTTab({ label, badge, icon, active = false, onClick }: FTTabProps) {
  return (
    <button
      className="box-border content-stretch flex flex-col items-start relative shrink-0"
      style={{
        padding: '12px 32px',
        gap: '10px',
        backgroundColor: 'transparent',
        cursor: 'pointer',
        border: 'none',
      }}
      onClick={onClick}
      data-name="Tab Item"
    >
      <div 
        aria-hidden="true" 
        className="absolute inset-0 pointer-events-none" 
        style={{ borderBottom: active ? '4px solid var(--primary)' : '1px solid var(--border-primary)' }}
      />
      <div 
        className="content-stretch flex items-center relative shrink-0 w-full"
        style={{ gap: 'var(--space-2)' }}
        data-name="Container"
      >
        {icon}
        <p 
          className="relative shrink-0 text-nowrap whitespace-pre"
          style={{
            fontFamily: 'var(--font-family-primary)',
            fontWeight: active ? 'var(--font-weight-semibold)' : 'var(--font-weight-regular)',
            fontSize: 'var(--font-size-base)',
            lineHeight: '1.4',
            color: 'var(--foreground)',
          }}
        >
          {label}
        </p>
        {badge !== undefined && <Badge count={badge} />}
      </div>
    </button>
  );
}

interface FTTabsProps {
  children: ReactNode;
  showEndBorder?: boolean;
}

export function FTTabs({ children, showEndBorder = true }: FTTabsProps) {
  return (
    <div 
      className="content-stretch flex items-start relative w-full"
      style={{ backgroundColor: 'var(--bg-primary)' }}
      data-name="Tabs"
    >
      {children}
      {showEndBorder && (
        <div className="basis-0 grow min-h-px min-w-px relative self-stretch shrink-0">
          <div 
            aria-hidden="true" 
            className="absolute inset-0 pointer-events-none" 
            style={{ borderBottom: '1px solid var(--border-primary)' }}
          />
        </div>
      )}
    </div>
  );
}
