import { useMemo } from 'react';
import type { Journey } from '../../../../api/journeys';

/**
 * Custom hook to calculate journey counts by tab status
 */
export const useTabCounts = (journeys: Journey[]) => {
    return useMemo(() => ({
        planned: journeys.filter(j => j.tab_status === 'planned').length,
        en_route_to_loading: journeys.filter(j => j.tab_status === 'en_route_to_loading').length,
        at_loading: journeys.filter(j => j.tab_status === 'at_loading').length,
        in_transit: journeys.filter(j => j.tab_status === 'in_transit').length,
        at_unloading: journeys.filter(j => j.tab_status === 'at_unloading').length,
        in_return: journeys.filter(j => j.tab_status === 'in_return').length,
        delivered: journeys.filter(j => j.tab_status === 'delivered').length
    }), [journeys]);
};

/**
 * Custom hook to calculate filter counts
 */
export const useFilterCounts = (journeys: Journey[]) => {
    return useMemo(() => {
        const delayed = journeys.filter(j => j.sla_status === 'delayed');

        return {
            stoppage: journeys.filter(j => j.alert_type === 'long_stoppage').length,
            deviation: journeys.filter(j => j.alert_type === 'route_deviation').length,
            delayed: delayed.length,
            '0-6hrs': delayed.length, // Would need actual delay hours in real data
            '6-12hrs': 0,
            '12plus': 0,
            expiring: 0, // Would need e-way bill data
            expired: 0,
            '6hrs': 0, // Would need ETA data
            '12hrs': 0,
            '24plus': 0
        };
    }, [journeys]);
};
