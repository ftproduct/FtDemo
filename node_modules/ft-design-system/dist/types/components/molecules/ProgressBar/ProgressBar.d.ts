import React from 'react';
export interface ProgressBarProps extends React.HTMLAttributes<HTMLDivElement> {
    value?: number;
    variant?: 'primary' | 'success' | 'warning' | 'danger';
    size?: 'sm' | 'md' | 'lg';
    showPercentage?: boolean;
    animated?: boolean;
}
export declare const ProgressBar: React.ForwardRefExoticComponent<ProgressBarProps & React.RefAttributes<HTMLDivElement>>;
//# sourceMappingURL=ProgressBar.d.ts.map