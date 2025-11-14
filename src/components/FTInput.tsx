import { useState, forwardRef, InputHTMLAttributes } from 'react';
import svgPaths from '../imports/svg-kcuto5puic';

interface FTInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'prefix'> {
  label?: string;
  prefix?: string;
  suffix?: string;
  showClear?: boolean;
  onClear?: () => void;
  error?: boolean;
  helperText?: string;
}

function CrossIcon({ onClick }: { onClick?: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="relative shrink-0 size-[16px] cursor-pointer border-0 bg-transparent p-0 hover:opacity-70 transition-opacity"
      aria-label="Clear input"
    >
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 16">
        <g>
          <g>
            <path d={svgPaths.p5d34300} fill="var(--tertiary)" />
            <path clipRule="evenodd" d={svgPaths.p22fafb00} fill="var(--tertiary)" fillRule="evenodd" />
          </g>
        </g>
      </svg>
    </button>
  );
}

export const FTInput = forwardRef<HTMLInputElement, FTInputProps>(
  ({ 
    label, 
    prefix, 
    suffix, 
    showClear = false, 
    onClear,
    error = false,
    helperText,
    className,
    value,
    onChange,
    ...props 
  }, ref) => {
    const [internalValue, setInternalValue] = useState(value || '');
    const displayValue = value !== undefined ? value : internalValue;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      if (value === undefined) {
        setInternalValue(e.target.value);
      }
      onChange?.(e);
    };

    const handleClear = () => {
      if (value === undefined) {
        setInternalValue('');
      }
      onClear?.();
      // Create a synthetic event for onChange
      const syntheticEvent = {
        target: { value: '' }
      } as React.ChangeEvent<HTMLInputElement>;
      onChange?.(syntheticEvent);
    };

    return (
      <div className="flex flex-col" style={{ gap: 'var(--space-2)' }}>
        {/* Label */}
        {label && (
          <label 
            className="flex items-center"
            style={{ 
              gap: 'var(--space-1)',
              fontSize: 'var(--font-size-sm)',
              fontWeight: 'var(--font-weight-medium)',
              color: 'var(--secondary)',
              lineHeight: 'var(--line-height-normal)'
            }}
          >
            {label}
          </label>
        )}

        {/* Input Body */}
        <div 
          className="relative"
          style={{
            height: 'var(--component-height-lg)',
            minHeight: 'var(--component-height-lg)',
            backgroundColor: 'var(--surface)',
            borderRadius: 'var(--radius-md)'
          }}
        >
          {/* Border */}
          <div 
            aria-hidden="true"
            className="absolute inset-0 pointer-events-none"
            style={{
              border: `1px solid ${error ? 'var(--critical)' : 'var(--border-primary)'}`,
              borderRadius: 'var(--radius-md)'
            }}
          />

          {/* Content Container */}
          <div className="flex items-center size-full" style={{ minHeight: 'inherit' }}>
            <div 
              className="flex items-center justify-between w-full"
              style={{
                height: 'var(--component-height-lg)',
                padding: '0 var(--space-3)',
                minHeight: 'inherit'
              }}
            >
              {/* Content Area */}
              <div 
                className="flex items-center flex-1"
                style={{
                  gap: 'var(--space-1)',
                  height: 'var(--component-height-lg)',
                  padding: 'var(--space-5) 0'
                }}
              >
                {/* Prefix */}
                {prefix && (
                  <span 
                    style={{
                      fontSize: 'var(--font-size-md)',
                      fontWeight: 'var(--font-weight-regular)',
                      color: 'var(--placeholder)',
                      lineHeight: 'var(--line-height-normal)',
                      whiteSpace: 'pre'
                    }}
                  >
                    {prefix}
                  </span>
                )}

                {/* Input Field */}
                <input
                  ref={ref}
                  value={displayValue}
                  onChange={handleChange}
                  className="flex-1 bg-transparent border-0 outline-none min-w-0"
                  style={{
                    fontSize: 'var(--font-size-md)',
                    fontWeight: 'var(--font-weight-regular)',
                    color: 'var(--input)',
                    lineHeight: 'var(--line-height-normal)',
                    fontFamily: 'var(--font-family-primary)'
                  }}
                  {...props}
                />

                {/* Suffix */}
                {suffix && (
                  <span 
                    style={{
                      fontSize: 'var(--font-size-md)',
                      fontWeight: 'var(--font-weight-regular)',
                      color: 'var(--placeholder)',
                      lineHeight: 'var(--line-height-normal)',
                      whiteSpace: 'pre'
                    }}
                  >
                    {suffix}
                  </span>
                )}

                {/* Clear Button */}
                {showClear && displayValue && (
                  <CrossIcon onClick={handleClear} />
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Helper Text */}
        {helperText && (
          <p 
            style={{
              fontSize: 'var(--font-size-xs)',
              color: error ? 'var(--critical)' : 'var(--helper)',
              lineHeight: 'var(--line-height-normal)'
            }}
          >
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

FTInput.displayName = 'FTInput';
