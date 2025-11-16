import React from 'react';
export interface RadioOption {
    value: string;
    label: string;
    disabled?: boolean;
}
export interface RadioGroupProps {
    name: string;
    value?: string;
    defaultValue?: string;
    options?: RadioOption[];
    onChange?: (value: string) => void;
    className?: string;
    size?: 'sm' | 'md';
    orientation?: 'horizontal' | 'vertical';
    children?: React.ReactNode;
}
export declare const RadioGroup: React.FC<RadioGroupProps>;
export declare const RadioGroupItem: ({ children, ...props }: {
    children?: React.ReactNode;
    [key: string]: any;
}) => import("react/jsx-runtime").JSX.Element;
//# sourceMappingURL=RadioGroup.d.ts.map