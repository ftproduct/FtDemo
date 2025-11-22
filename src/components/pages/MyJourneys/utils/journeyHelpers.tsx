import { Icon } from 'ft-design-system';

/**
 * Returns the appropriate icon component based on trip type
 */
export const getTripIcon = (type: string) => {
    if (type === 'SIM') {
        return <Icon name="sim" style={{ width: '16px', height: '16px', color: 'var(--positive)' }} />;
    }
    if (type === 'GPS') {
        return <Icon name="gps" style={{ width: '20px', height: '20px', color: 'var(--neutral)' }} />;
    }
    if (type === 'Fastag') {
        return (
            <div style={{
                width: '16px',
                height: '16px',
                borderRadius: '2px',
                background: '#722ed1',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '10px',
                color: 'white',
                fontWeight: 600
            }}>
                F
            </div>
        );
    }
    return null;
};

/**
 * Returns alert display labels
 */
export const getAlertLabel = (alertType: string): string => {
    const alertLabels: Record<string, string> = {
        long_stoppage: 'Long Stoppage',
        route_deviation: 'Route Deviation',
        transit_delay: 'Transit Delay'
    };
    return alertLabels[alertType] || alertType;
};
