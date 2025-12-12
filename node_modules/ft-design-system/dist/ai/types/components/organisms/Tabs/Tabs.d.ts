import React from 'react';
export type TabType = 'primary' | 'secondary' | 'tertiary';
export type TabState = 'unselected' | 'selected' | 'hover';
export interface TabItemProps {
    label: string;
    badge?: boolean;
    badgeCount?: string | number;
    notification?: boolean;
    icon?: boolean;
    active?: boolean;
    type?: TabType;
    onSelect?: () => void;
    className?: string;
}
export declare const TabItem: React.ForwardRefExoticComponent<TabItemProps & React.RefAttributes<HTMLDivElement>>;
export interface Tab {
    label: string;
    badge?: boolean;
    badgeCount?: string | number;
    notification?: boolean;
    icon?: boolean;
}
export interface TabsProps {
    tabs: Tab[];
    activeTab?: number;
    onTabChange?: (index: number) => void;
    type?: TabType;
    showLine?: boolean;
    className?: string;
}
export declare const Tabs: React.ForwardRefExoticComponent<TabsProps & React.RefAttributes<HTMLDivElement>>;
export default Tabs;
//# sourceMappingURL=Tabs.d.ts.map