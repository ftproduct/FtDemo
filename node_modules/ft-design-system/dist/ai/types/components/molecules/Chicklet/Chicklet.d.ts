import React from 'react';
export type ChickletVariant = 'rounded' | 'rectangular';
export type ChickletState = 'default' | 'hover';
export interface ChickletProps {
    /** The text content of the chicklet */
    label: string;
    /** Whether the chicklet has rounded corners (pill) or rectangular corners */
    variant?: ChickletVariant;
    /** Whether to show the close (cross) icon */
    showClose?: boolean;
    /** Callback when the close icon is clicked */
    onClose?: () => void;
    /** Callback when the chicklet is clicked */
    onClick?: () => void;
    /** Whether the chicklet is disabled */
    disabled?: boolean;
    /** Additional CSS classes */
    className?: string;
}
export declare const Chicklet: React.ForwardRefExoticComponent<ChickletProps & React.RefAttributes<HTMLDivElement>>;
export default Chicklet;
//# sourceMappingURL=Chicklet.d.ts.map