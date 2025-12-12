import * as React from "react";
import { Slot } from "@radix-ui/react-slot@1.1.2";
import { cva, type VariantProps } from "class-variance-authority@0.7.1";

import { cn } from "./utils";

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap outline-none transition-all disabled:pointer-events-none disabled:opacity-40 [&_svg]:pointer-events-none [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default: "bg-[var(--button-primary-bg)] text-[var(--button-primary-text)] border-[var(--button-primary-border)] hover:bg-[var(--button-primary-hover-bg)]",
        primary: "bg-[var(--button-primary-bg)] text-[var(--button-primary-text)] border-[var(--button-primary-border)] hover:bg-[var(--button-primary-hover-bg)]",
        secondary: "bg-[var(--button-secondary-bg)] text-[var(--button-secondary-text)] border-[var(--button-secondary-border)] hover:bg-[var(--button-secondary-hover-bg)] hover:border-[var(--button-secondary-hover-border)]",
        destructive: "bg-[var(--button-destructive-bg)] text-[var(--button-destructive-text)] border-[var(--button-destructive-border)] hover:bg-[var(--button-destructive-hover-bg)]",
        outline: "bg-[var(--button-secondary-bg)] text-[var(--button-secondary-text)] border-[var(--button-secondary-border)] hover:bg-[var(--button-secondary-hover-bg)] hover:border-[var(--button-secondary-hover-border)]",
        ghost: "bg-[var(--button-text-bg)] text-[var(--button-text-text)] border-transparent hover:bg-[var(--button-text-hover-bg)]",
        link: "bg-[var(--button-link-bg)] text-[var(--button-link-text)] border-transparent hover:text-[var(--button-link-hover-text)]",
      },
      size: {
        sm: "h-[36px] px-[12px] py-[8px] text-[14px] gap-[8px]",
        md: "h-[40px] px-[16px] py-[12px] text-[14px] gap-[12px]",
        lg: "h-[48px] px-[24px] py-[12px] text-[20px] gap-[8px]",
        xl: "h-[64px] px-[24px] py-[20px] text-[18px] gap-[16px]",
        icon: "h-[40px] w-[40px] p-0",
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

  // Icon sizing based on button size - matches Figma design
  const iconSize =
    size === "sm" ? "16px" : size === "md" ? "20px" : size === "lg" ? "24px" : size === "xl" ? "24px" : "20px";

  return (
    <Comp
      data-slot="button"
      className={cn(
        buttonVariants({ variant, size }),
        "border rounded-[var(--component-border-radius)] font-[var(--font-family-primary)] font-[var(--component-font-weight)] leading-[1.4] cursor-pointer select-none",
        "focus-visible:outline focus-visible:outline-2 focus-visible:outline-[var(--focus)] focus-visible:outline-offset-2",
        className
      )}
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
