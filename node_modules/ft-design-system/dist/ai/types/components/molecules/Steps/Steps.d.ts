import React from 'react';
export interface StepsItemProps {
    label?: string;
    state: 'selected' | 'unselected';
    device: 'desktop' | 'mobile';
    className?: string;
}
export declare const StepsItem: React.ForwardRefExoticComponent<StepsItemProps & React.RefAttributes<HTMLDivElement>>;
export interface Step {
    label: string;
    completed?: boolean;
}
export interface StepsProps {
    steps: Step[];
    currentStep?: number;
    device?: 'desktop' | 'mobile';
    className?: string;
}
export declare const Steps: React.ForwardRefExoticComponent<StepsProps & React.RefAttributes<HTMLDivElement>>;
export default Steps;
//# sourceMappingURL=Steps.d.ts.map