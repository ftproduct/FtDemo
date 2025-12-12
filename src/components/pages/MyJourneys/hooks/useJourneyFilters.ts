import { useMemo } from 'react';
import type { Journey } from '../../../../api/journeys';

/**
 * Custom hook to filter journeys based on active filters
 */
export const useJourneyFilters = (
    journeys: Journey[],
    activeFilters: Set<string>
) => {
    return useMemo(() => {
        if (activeFilters.size === 0) {
            return journeys;
        }

        return journeys.filter((journey: Journey) => {
            // Check each active filter
            for (const filterKey of activeFilters) {
                const [filterId, optionId] = filterKey.split(':');

                // Single option filters
                if (filterId === 'stoppage' && journey.alert_type === 'long_stoppage') {
                    return true;
                }
                if (filterId === 'deviation' && journey.alert_type === 'route_deviation') {
                    return true;
                }

                // Multi-option filters
                if (filterId === 'delayed') {
                    if (journey.sla_status === 'delayed') {
                        // Check specific delay ranges if option is selected
                        if (optionId === '0-6hrs' || optionId === '6-12hrs' || optionId === '12plus') {
                            // For demo, if delayed, show it (in real app, check actual delay hours)
                            return true;
                        }
                        // If no specific option, show all delayed
                        if (!optionId) return true;
                    }
                }

                if (filterId === 'eway') {
                    // E Way bill filters - for demo, show all if selected
                    return true;
                }

                if (filterId === 'eta') {
                    // ETA filters - for demo, show all if selected
                    return true;
                }
            }
            return false;
        });
    }, [journeys, activeFilters]);
};
