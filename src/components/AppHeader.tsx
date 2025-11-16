import svgPaths from "../imports/svg-0p0qopiq1k";
import imgCompanyLogos from "figma:asset/eb5617c1fb8c4f18cf1db67cafb34a6e0fe3d26c.png";
import imgUserImage from "figma:asset/0c12b272f4ee3a49b56adf99c21f0cc746e805cb.png";

function Menu() {
  return (
    <div className="relative shrink-0 size-[24px]" data-name="Menu">
      <svg className="block" style={{ width: '24px', height: '24px' }} fill="none" preserveAspectRatio="none" viewBox="0 0 24 24">
        <g id="Menu">
          <g id="Vector">
            <path d={svgPaths.p1cfa1bc0} stroke="var(--primary)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
            <path d={svgPaths.p2cfdb900} stroke="var(--primary)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
            <path d={svgPaths.p17f25d40} stroke="var(--primary)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
            <path d={svgPaths.p15fb5e00} stroke="var(--primary)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
          </g>
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
        gap: 'var(--space-3)',
        padding: 'var(--space-4)',
        borderRadius: 'var(--radius-full)',
        border: '1px solid var(--border-primary)',
        cursor: 'pointer'
      }}
    >
      <Menu />
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

function CompanyLogos() {
  return (
    <div className="h-full relative shrink-0 w-[155px]" data-name="Company logos">
      <img alt="Company Logo" className="absolute inset-0 max-w-none object-50%-50% object-contain pointer-events-none size-full" src={imgCompanyLogos} />
    </div>
  );
}

function UserAvatar() {
  return (
    <div 
      className="overflow-hidden relative shrink-0 size-[30px]" 
      data-name="User Avatar"
      style={{
        backgroundColor: 'var(--bg-primary)',
        borderRadius: 'var(--radius-full)',
        border: '1px solid var(--border-primary)'
      }}
    >
      <img 
        alt="User Avatar" 
        src={imgUserImage} 
        style={{
          width: '100%',
          height: '100%',
          objectFit: 'cover',
          borderRadius: 'var(--radius-full)'
        }}
      />
    </div>
  );
}

function Avatar() {
  return (
    <div 
      className="content-stretch flex items-center justify-center relative shrink-0 size-[30px]" 
      data-name="Avatar"
      style={{ gap: 'var(--space-3)' }}
    >
      <UserAvatar />
    </div>
  );
}

function UserProfile() {
  return (
    <div 
      className="box-border content-stretch flex items-center h-[46px] relative shrink-0" 
      data-name="User Profile"
      style={{
        backgroundColor: 'var(--bg-primary)',
        gap: 'var(--space-4)',
        padding: 'var(--space-2)',
        borderRadius: 'var(--radius-md)'
      }}
    >
      <CompanyLogos />
      <Avatar />
    </div>
  );
}

function NotificationIcons() {
  return (
    <div 
      className="content-stretch flex items-center relative shrink-0" 
      data-name="Notification Icons"
      style={{ gap: 'var(--space-4)' }}
    >
      <NotificationContainer />
      <UserProfile />
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
      style={{ backgroundColor: 'var(--bg-secondary)' }}
    >
      <div 
        aria-hidden="true" 
        className="absolute inset-0 pointer-events-none border-solid border-[0px_0px_1px]"
        style={{ borderColor: 'var(--border-primary)' }}
      />
      <div className="flex flex-row items-center size-full">
        <div 
          className="box-border content-stretch flex items-center justify-between relative size-full"
          style={{
            paddingLeft: 'var(--space-5)',
            paddingRight: 'var(--space-5)',
            paddingTop: 'var(--space-3)',
            paddingBottom: 'var(--space-3)'
          }}
        >
          <Logo onOpenNavigation={onOpenNavigation} />
          <NotificationIcons />
        </div>
      </div>
    </div>
  );
}
