import * as React from "react";
import { Slot } from "@radix-ui/react-slot@1.1.2";
import { cva, type VariantProps } from "class-variance-authority@0.7.1";

import { cn } from "./utils";

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap outline-none transition-all disabled:pointer-events-none disabled:opacity-40 [&_svg]:pointer-events-none [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default: "",
        primary: "",
        secondary: "",
        destructive: "",
        outline: "",
        ghost: "",
        link: "",
      },
      size: {
        sm: "",
        md: "",
        lg: "",
        xl: "",
        icon: "",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "md",
    },
  },
);

function Button({
  className,
  variant = "default",
  size = "md",
  asChild = false,
  ...props
}: React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean;
  }) {
  const Comp = asChild ? Slot : "button";

  // Determine variant styles
  const variantStyles: React.CSSProperties = {
    backgroundColor:
      variant === "primary" || variant === "default"
        ? "var(--button-primary-bg)"
        : variant === "secondary"
        ? "var(--button-secondary-bg)"
        : variant === "destructive"
        ? "var(--button-destructive-bg)"
        : variant === "outline"
        ? "var(--button-secondary-bg)"
        : variant === "ghost"
        ? "var(--button-text-bg)"
        : variant === "link"
        ? "var(--button-link-bg)"
        : undefined,
    color:
      variant === "primary" || variant === "default"
        ? "var(--button-primary-text)"
        : variant === "secondary"
        ? "var(--button-secondary-text)"
        : variant === "destructive"
        ? "var(--button-destructive-text)"
        : variant === "outline"
        ? "var(--button-secondary-text)"
        : variant === "ghost"
        ? "var(--button-text-text)"
        : variant === "link"
        ? "var(--button-link-text)"
        : undefined,
    border:
      variant === "outline"
        ? "1px solid var(--button-secondary-border)"
        : variant === "primary" || variant === "default"
        ? "1px solid var(--button-primary-border)"
        : variant === "secondary"
        ? "1px solid var(--button-secondary-border)"
        : variant === "destructive"
        ? "1px solid var(--button-destructive-border)"
        : "1px solid transparent",
    borderRadius: "var(--component-border-radius)",
    fontFamily: "var(--font-family-primary)",
    fontWeight: "var(--component-font-weight)",
    lineHeight: "1",
    transition: "var(--component-transition)",
    cursor: props.disabled ? "not-allowed" : "pointer",
    userSelect: "none",
  };

  // Determine size styles
  const sizeStyles: React.CSSProperties = {
    height:
      size === "sm"
        ? "var(--component-height-sm)"
        : size === "md"
        ? "var(--component-height-md)"
        : size === "lg"
        ? "var(--component-height-lg)"
        : size === "xl"
        ? "var(--component-height-xl)"
        : size === "icon"
        ? "var(--component-height-md)"
        : undefined,
    padding:
      size === "sm"
        ? "var(--component-padding-sm)"
        : size === "md"
        ? "var(--component-padding-md)"
        : size === "lg"
        ? "var(--component-padding-lg)"
        : size === "xl"
        ? "var(--component-padding-xl)"
        : size === "icon"
        ? "0"
        : undefined,
    fontSize:
      size === "sm"
        ? "var(--component-font-size-sm)"
        : size === "md"
        ? "var(--component-font-size-md)"
        : size === "lg"
        ? "var(--component-font-size-lg)"
        : size === "xl"
        ? "var(--component-font-size-xl)"
        : undefined,
    gap:
      size === "sm"
        ? "var(--component-gap-sm)"
        : size === "md"
        ? "var(--component-gap-md)"
        : size === "lg"
        ? "var(--component-gap-lg)"
        : size === "xl"
        ? "var(--component-gap-lg)"
        : undefined,
    width: size === "icon" ? "var(--component-height-md)" : undefined,
  };

  // Icon sizing based on button size
  const iconSize =
    size === "sm" ? "16px" : size === "md" ? "20px" : size === "lg" ? "24px" : size === "xl" ? "24px" : "20px";

  return (
    <Comp
      data-slot="button"
      className={cn(buttonVariants({ variant, size }), className)}
      style={{
        ...variantStyles,
        ...sizeStyles,
        ...(className?.includes("size-") ? {} : {}),
      }}
      onMouseEnter={(e) => {
        if (!props.disabled) {
          const target = e.currentTarget as HTMLElement;
          if (variant === "primary" || variant === "default") {
            target.style.backgroundColor = "var(--button-primary-hover-bg)";
          } else if (variant === "secondary") {
            target.style.backgroundColor = "var(--button-secondary-hover-bg)";
            target.style.borderColor = "var(--button-secondary-hover-border)";
          } else if (variant === "destructive") {
            target.style.backgroundColor = "var(--button-destructive-hover-bg)";
          } else if (variant === "outline") {
            target.style.backgroundColor = "var(--button-secondary-hover-bg)";
            target.style.borderColor = "var(--button-secondary-hover-border)";
          } else if (variant === "ghost") {
            target.style.backgroundColor = "var(--button-text-hover-bg)";
          } else if (variant === "link") {
            target.style.color = "var(--button-link-hover-text)";
          }
        }
      }}
      onMouseLeave={(e) => {
        if (!props.disabled) {
          const target = e.currentTarget as HTMLElement;
          if (variant === "primary" || variant === "default") {
            target.style.backgroundColor = "var(--button-primary-bg)";
          } else if (variant === "secondary") {
            target.style.backgroundColor = "var(--button-secondary-bg)";
            target.style.borderColor = "var(--button-secondary-border)";
          } else if (variant === "destructive") {
            target.style.backgroundColor = "var(--button-destructive-bg)";
          } else if (variant === "outline") {
            target.style.backgroundColor = "var(--button-secondary-bg)";
            target.style.borderColor = "var(--button-secondary-border)";
          } else if (variant === "ghost") {
            target.style.backgroundColor = "var(--button-text-bg)";
          } else if (variant === "link") {
            target.style.color = "var(--button-link-text)";
          }
        }
      }}
      onFocus={(e) => {
        if (!props.disabled) {
          const target = e.currentTarget as HTMLElement;
          target.style.outline = "2px solid var(--focus)";
          target.style.outlineOffset = "2px";
        }
      }}
      onBlur={(e) => {
        const target = e.currentTarget as HTMLElement;
        target.style.outline = "none";
      }}
      {...props}
    >
      {/* Apply icon sizing to children SVGs */}
      {React.Children.map(props.children, (child) => {
        if (React.isValidElement(child) && child.type === "svg") {
          return React.cloneElement(child as React.ReactElement<any>, {
            style: {
              width: iconSize,
              height: iconSize,
              ...(child.props.style || {}),
            },
          });
        }
        return child;
      })}
    </Comp>
  );
}

export { Button, buttonVariants };
