import React from 'react';
export interface SegmentedTabItem {
    label: string;
    value: string;
    icon?: React.ReactNode;
}
export interface SegmentedTabsProps {
    items: SegmentedTabItem[];
    value?: string;
    defaultValue?: string;
    onChange?: (value: string) => void;
    className?: string;
}
export declare const SegmentedTabs: React.FC<SegmentedTabsProps>;
//# sourceMappingURL=SegmentedTabs.d.ts.map