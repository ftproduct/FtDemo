import * as React from "react";
import { cn } from "./utils";
import svgPaths from "../../imports/svg-j9dd9v5ruc";

interface InputProps extends Omit<React.ComponentProps<"input">, 'prefix'> {
  prefix?: React.ReactNode;
  suffix?: React.ReactNode;
  clearable?: boolean;
  onClear?: () => void;
}

function CrossIcon({ onClick }: { onClick?: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="relative shrink-0 size-[16px] cursor-pointer hover:opacity-70 transition-opacity"
      style={{
        background: 'transparent',
        border: 'none',
        padding: 0,
      }}
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

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, prefix, suffix, clearable, onClear, value, ...props }, ref) => {
    const [internalValue, setInternalValue] = React.useState(value || '');
    const hasValue = value !== undefined ? value : internalValue;
    
    const handleClear = () => {
      if (onClear) {
        onClear();
      }
      setInternalValue('');
      // Trigger onChange if provided
      if (props.onChange) {
        const syntheticEvent = {
          target: { value: '' },
        } as React.ChangeEvent<HTMLInputElement>;
        props.onChange(syntheticEvent);
      }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      setInternalValue(e.target.value);
      if (props.onChange) {
        props.onChange(e);
      }
    };

    // If prefix, suffix, or clearable is provided, render the enhanced input
    if (prefix || suffix || clearable) {
      return (
        <div
          className={cn("relative w-full", className)}
          style={{
            height: 'var(--component-height-lg)',
            minHeight: 'var(--component-height-lg)',
          }}
        >
          <div
            className="size-full relative rounded-[8px]"
            style={{
              backgroundColor: 'var(--surface)',
              borderRadius: 'var(--component-border-radius)',
            }}
          >
            <div
              aria-hidden="true"
              className="absolute inset-0 pointer-events-none"
              style={{
                border: '1px solid var(--border-primary)',
                borderRadius: 'var(--component-border-radius)',
              }}
            />
            <div className="flex flex-row items-center size-full">
              <div
                className="flex items-center w-full"
                style={{
                  height: 'var(--component-height-lg)',
                  padding: '0 var(--space-3)',
                  gap: 'var(--space-1)',
                }}
              >
                {prefix && (
                  <span
                    className="shrink-0"
                    style={{
                      color: 'var(--placeholder)',
                      fontFamily: 'var(--font-family-primary)',
                      fontSize: 'var(--component-font-size-lg)',
                      fontWeight: 'var(--font-weight-regular)',
                      lineHeight: '1.4',
                    }}
                  >
                    {prefix}
                  </span>
                )}
                <input
                  type={type}
                  ref={ref}
                  value={value}
                  onChange={handleChange}
                  data-slot="input"
                  className="flex-1 min-w-0 bg-transparent outline-none border-none"
                  style={{
                    color: 'var(--input)',
                    fontFamily: 'var(--font-family-primary)',
                    fontSize: 'var(--component-font-size-lg)',
                    fontWeight: 'var(--font-weight-regular)',
                    lineHeight: '1.4',
                    padding: 0,
                  }}
                  {...props}
                />
                {suffix && (
                  <span
                    className="shrink-0"
                    style={{
                      color: 'var(--placeholder)',
                      fontFamily: 'var(--font-family-primary)',
                      fontSize: 'var(--component-font-size-lg)',
                      fontWeight: 'var(--font-weight-regular)',
                      lineHeight: '1.4',
                    }}
                  >
                    {suffix}
                  </span>
                )}
                {clearable && hasValue && (
                  <CrossIcon onClick={handleClear} />
                )}
              </div>
            </div>
          </div>
        </div>
      );
    }

    // Default simple input without prefix/suffix
    return (
      <input
        type={type}
        ref={ref}
        value={value}
        onChange={handleChange}
        data-slot="input"
        className={cn("flex w-full min-w-0 outline-none", className)}
        style={{
          height: 'var(--component-height-md)',
          padding: 'var(--component-padding-md)',
          border: '1px solid var(--border-primary)',
          borderRadius: 'var(--component-border-radius)',
          backgroundColor: 'var(--surface)',
          color: 'var(--input)',
          fontFamily: 'var(--font-family-primary)',
          fontSize: 'var(--component-font-size-md)',
          fontWeight: 'var(--font-weight-regular)',
          lineHeight: '1.4',
          transition: 'var(--component-transition)',
        }}
        onFocus={(e) => {
          e.currentTarget.style.borderColor = 'var(--focus)';
          e.currentTarget.style.boxShadow = '0 0 0 2px var(--focus-ring)';
        }}
        onBlur={(e) => {
          e.currentTarget.style.borderColor = 'var(--border-primary)';
          e.currentTarget.style.boxShadow = 'none';
        }}
        {...props}
      />
    );
  }
);

Input.displayName = "Input";

export { Input };
