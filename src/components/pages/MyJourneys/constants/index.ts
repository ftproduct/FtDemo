import { Icon } from 'ft-design-system';
import type { IconName } from 'ft-design-system';

export interface TabConfig {
    label: string;
    status: string;
    icon: IconName;
}

/**
 * Tab configuration for journey statuses
 */
export const TAB_CONFIG: TabConfig[] = [
    { label: 'Planned', status: 'planned', icon: 'calendar' },
    { label: 'En Route to Loading', status: 'en_route_to_loading', icon: 'truck' },
    { label: 'At Loading', status: 'at_loading', icon: 'bundle' },
    { label: 'In Transit', status: 'in_transit', icon: 'location' },
    { label: 'At Unloading', status: 'at_unloading', icon: 'bundle' },
    { label: 'In Return', status: 'in_return', icon: 'refresh' },
    { label: 'Delivered', status: 'delivered', icon: 'check-fill' }
];

/**
 * Quick filter type definitions
 */
export const FILTER_TYPES = {
    ALERT: 'alert' as const,
    NORMAL: 'normal' as const,
    WARNING: 'warning' as const,
    SUCCESS: 'success' as const
};

/**
 * Page header dropdown options
 */
export const COMPANY_OPTIONS = [
    { value: 'mdc', label: 'MDC Labs, Amritsar' },
    { value: 'abc', label: 'ABC Corp, Mumbai' }
];

export const DIRECTION_OPTIONS = [
    { value: 'outbound', label: 'Outbound - Source' },
    { value: 'inbound', label: 'Inbound' }
];

export const DATE_RANGE_OPTIONS = [
    { value: 'today', label: 'Today' },
    { value: 'yesterday', label: 'Yesterday' },
    { value: 'last7days', label: 'Last 7 Days' },
    { value: 'last30days', label: 'Last 30 Days' },
    { value: 'custom', label: 'Custom Range' }
];
