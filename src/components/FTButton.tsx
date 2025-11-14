import { ButtonHTMLAttributes, ReactNode } from 'react';
import svgPathsCheck from "../imports/svg-wg310dabi8";
import svgPathsPlus from "../imports/svg-k1ql5rg7bl";

interface FTButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'destructive' | 'text' | 'link';
  children: ReactNode;
  icon?: ReactNode;
  disabled?: boolean;
}

function CheckCircleIcon({ color }: { color?: string }) {
  return (
    <div className="relative shrink-0 size-[24px]" data-name="Leading icon">
      <svg className="block" style={{ width: '24px', height: '24px' }} fill="none" preserveAspectRatio="none" viewBox="0 0 24 24">
        <g id="Leading icon">
          <path d={svgPathsCheck.pf726c00} fill={color || "currentColor"} id="icon" />
        </g>
      </svg>
    </div>
  );
}

function PlusIcon({ color }: { color?: string }) {
  return (
    <div className="relative shrink-0 size-[24px]" data-name="Leading icon">
      <svg className="block" style={{ width: '24px', height: '24px' }} fill="none" preserveAspectRatio="none" viewBox="0 0 24 24">
        <g id="Leading icon">
          <path d={svgPathsPlus.pf726c00} fill={color || "currentColor"} id="icon" />
        </g>
      </svg>
    </div>
  );
}

export function FTButton({ 
  variant = 'primary', 
  children, 
  icon,
  disabled = false,
  className = '',
  style,
  ...props 
}: FTButtonProps) {
  
  const getVariantStyles = () => {
    switch (variant) {
      case 'primary':
        return {
          backgroundColor: 'var(--button-primary-bg)',
          color: 'var(--button-primary-text)',
          borderColor: 'var(--button-primary-border)',
          border: '1px solid',
        };
      case 'secondary':
        return {
          backgroundColor: 'var(--button-secondary-bg)',
          color: 'var(--button-secondary-text)',
          borderColor: 'var(--button-secondary-border)',
          border: '1px solid',
        };
      case 'destructive':
        return {
          backgroundColor: 'var(--button-destructive-bg)',
          color: 'var(--button-destructive-text)',
          borderColor: 'var(--button-destructive-border)',
          border: '1px solid',
        };
      case 'text':
        return {
          backgroundColor: 'transparent',
          color: 'var(--button-text-text)',
          border: 'none',
        };
      case 'link':
        return {
          backgroundColor: 'transparent',
          color: 'var(--button-link-text)',
          border: 'none',
        };
      default:
        return {
          backgroundColor: 'var(--button-primary-bg)',
          color: 'var(--button-primary-text)',
          borderColor: 'var(--button-primary-border)',
          border: '1px solid',
        };
    }
  };

  const variantStyles = getVariantStyles();
  
  // For text and link variants, use different padding (no full wrapper needed)
  const isMinimal = variant === 'text' || variant === 'link';

  return (
    <button
      className={`relative ${className}`}
      disabled={disabled}
      style={{
        ...variantStyles,
        borderRadius: 'var(--component-border-radius)',
        opacity: disabled ? 0.4 : 1,
        cursor: disabled ? 'not-allowed' : 'pointer',
        transition: 'var(--component-transition)',
        ...style,
      }}
      {...props}
    >
      {isMinimal ? (
        <div 
          className="content-stretch flex items-center relative"
          style={{
            gap: 'var(--space-2)',
          }}
        >
          {icon}
          <div 
            className="flex flex-col justify-end leading-[0] relative shrink-0 text-nowrap"
            style={{
              fontFamily: 'var(--font-family-primary)',
              fontWeight: 'var(--font-weight-medium)',
              fontSize: 'var(--font-size-lg)',
            }}
          >
            <p style={{ lineHeight: '1.4' }} className="whitespace-pre">{children}</p>
          </div>
        </div>
      ) : (
        <div className="flex flex-row items-center justify-center size-full">
          <div 
            className="box-border content-stretch flex items-center justify-center relative size-full"
            style={{
              gap: 'var(--space-2)',
              padding: '12px 24px',
            }}
          >
            {icon}
            <div 
              className="flex flex-col justify-end leading-[0] relative shrink-0 text-nowrap"
              style={{
                fontFamily: 'var(--font-family-primary)',
                fontWeight: 'var(--font-weight-medium)',
                fontSize: 'var(--font-size-lg)',
              }}
            >
              <p style={{ lineHeight: '1.4' }} className="whitespace-pre">{children}</p>
            </div>
          </div>
        </div>
      )}
    </button>
  );
}

// Export convenience components with different icons
export function FTButtonWithIcon(props: Omit<FTButtonProps, 'icon'>) {
  return <FTButton {...props} icon={<CheckCircleIcon />} />;
}

export function FTButtonWithPlus(props: Omit<FTButtonProps, 'icon'>) {
  return <FTButton {...props} icon={<PlusIcon />} />;
}
