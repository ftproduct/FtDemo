import React from 'react';
import { UserProfile, UserProfileDropdown } from 'ft-design-system/ai';
import svgPaths from "../imports/svg-0p0qopiq1k";

function Menu() {
  return (
    <div className="relative shrink-0" data-name="Menu" style={{ width: '28px', height: '28px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <svg className="block" style={{ width: '24px', height: '24px' }} fill="none" preserveAspectRatio="none" viewBox="0 0 24 24">
        <g id="Menu">
          <path d="M4 6H20M4 12H20M4 18H20" stroke="var(--primary)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
        </g>
      </svg>
    </div>
  );
}

function LucideLayoutGrid({ onOpenNavigation }: { onOpenNavigation?: () => void }) {
  return (
    <button
      type="button"
      onClick={onOpenNavigation}
      aria-label="Open navigation"
      className="box-border content-stretch flex items-center justify-center overflow-clip relative shrink-0 size-[54px]"
      data-name="lucide/layout-grid"
      style={{
        backgroundColor: 'var(--bg-primary)',
        borderRadius: 'var(--radius-full)',
        border: '1px solid var(--border-primary)',
        cursor: 'pointer',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 0
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', height: '100%' }}>
        <Menu />
      </div>
    </button>
  );
}

function CompanyName() {
  return (
    <div className="h-[28px] relative shrink-0 w-[190.242px]" data-name="Company Name">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 191 28">
        <g id="Company Name">
          <g id="Vector">
            <path d={svgPaths.p216b7900} fill="#FFBE07" />
            <path d={svgPaths.p199cd600} fill="#211F1F" />
            <path d={svgPaths.p31367e00} fill="#211F1F" />
            <path d={svgPaths.p5a1dc00} fill="#FFBE07" />
            <path d={svgPaths.pc21d9c0} fill="#FFBE07" />
          </g>
          <g id="Vector_2">
            <path d={svgPaths.p1f434d40} fill="black" />
            <path d={svgPaths.p38ec5440} fill="black" />
            <path d={svgPaths.p28565200} fill="black" />
            <path d={svgPaths.p66791f0} fill="black" />
            <path d={svgPaths.p11690780} fill="black" />
            <path d={svgPaths.p29318c00} fill="black" />
            <path d={svgPaths.p2d24a500} fill="black" />
            <path d={svgPaths.p3d094300} fill="black" />
            <path d={svgPaths.p2b460900} fill="black" />
            <path d={svgPaths.p72cc700} fill="black" />
            <path d={svgPaths.p12a41700} fill="black" />
            <path d={svgPaths.p3e0baae0} fill="black" />
          </g>
        </g>
      </svg>
    </div>
  );
}

function Logo({ onOpenNavigation }: { onOpenNavigation?: () => void }) {
  return (
    <div
      className="content-stretch flex items-center relative shrink-0"
      data-name="Logo"
      style={{ gap: 'var(--space-5)' }}
    >
      <LucideLayoutGrid onOpenNavigation={onOpenNavigation} />
      <CompanyName />
    </div>
  );
}

function Rocket() {
  return (
    <div className="relative shrink-0 size-[24px]" data-name="Rocket">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 24 24">
        <g id="Rocket">
          <path d={svgPaths.p1e2a7900} id="icon" stroke="var(--primary)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
        </g>
      </svg>
    </div>
  );
}

function Bell() {
  return (
    <div className="relative shrink-0 size-[24px]" data-name="Bell">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 24 24">
        <g id="Bell">
          <path d={svgPaths.p19389200} id="icon" stroke="var(--primary)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
        </g>
      </svg>
    </div>
  );
}

function NotificationContainer() {
  return (
    <div
      className="content-stretch flex items-center relative shrink-0"
      data-name="Notification Container"
      style={{ gap: 'var(--space-9)' }}
    >
      <Rocket />
      <Bell />
    </div>
  );
}


function NotificationIcons() {
  const [isUserProfileOpen, setIsUserProfileOpen] = React.useState(false);
  const profileRef = React.useRef<HTMLDivElement | null>(null);

  React.useEffect(() => {
    if (!isUserProfileOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
        setIsUserProfileOpen(false);
      }
    };

    const handleEsc = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsUserProfileOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEsc);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEsc);
    };
  }, [isUserProfileOpen]);

  return (
    <div
      ref={profileRef}
      className="content-stretch flex items-center relative shrink-0"
      data-name="Notification Icons"
      style={{ gap: 'var(--space-4)', overflow: 'visible', zIndex: 1200 }}
    >
      <NotificationContainer />
      <div style={{ position: 'relative' }}>
        <UserProfile
          userName="John Doe"
          userRole="Administrator"
          userLocation="Mumbai, India"
          company={{
            name: 'mdc',
            displayName: 'MDC Labs'
          }}
          onClick={() => setIsUserProfileOpen((prev) => !prev)}
        />
        <UserProfileDropdown
          userName="John Doe"
          userRole="Administrator"
          userLocation="Mumbai, India"
          userBadge="Admin"
          isOpen={isUserProfileOpen}
          onMenuItemClick={(item: string) => {
            console.log('Menu item clicked:', item);
            if (item === 'logout') {
              // Handle logout
            }
            setIsUserProfileOpen(false);
          }}
        />
      </div>
    </div>
  );
}

interface AppHeaderProps {
  onOpenNavigation?: () => void;
}

export default function AppHeader({ onOpenNavigation }: AppHeaderProps) {
  return (
    <div
      className="relative size-full"
      data-name="App header"
      style={{ backgroundColor: 'var(--bg-secondary)', overflow: 'visible', zIndex: 100 }}
    >
      <div
        aria-hidden="true"
        className="absolute inset-0 pointer-events-none border-solid border-[0px_0px_1px]"
        style={{ borderColor: 'var(--border-primary)' }}
      />
      <div className="flex flex-row items-center size-full" style={{ overflow: 'visible' }}>
        <div
          className="box-border content-stretch flex items-center justify-between relative size-full"
          style={{
            paddingLeft: 'var(--space-5)',
            paddingRight: 'var(--space-5)',
            paddingTop: 'var(--space-3)',
            paddingBottom: 'var(--space-3)',
            overflow: 'visible'
          }}
        >
          <Logo onOpenNavigation={onOpenNavigation} />
          <NotificationIcons />
        </div>
      </div>
    </div>
  );
}
