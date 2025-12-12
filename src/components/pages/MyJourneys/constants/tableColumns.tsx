import React from 'react';
import type { TableColumn } from 'ft-design-system/ai';
import { Checkbox, Badge, Button } from 'ft-design-system/ai';
import { TableCellText, TableCellItem, Icon } from 'ft-design-system';
import type { Journey } from '../../../../api/journeys';
import { getTripIcon, getAlertLabel } from '../utils/journeyHelpers';

export const createTableColumns = (
    selectAll: boolean,
    selectedJourneyIds: number[],
    onToggleAll: (checked: boolean) => void,
    onToggleRow: (journeyId: number, checked: boolean) => void
): TableColumn<Journey>[] => [
        {
            key: 'select',
            title: (
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', justifyContent: 'center' }}>
                    <Checkbox
                        checked={selectAll}
                        onChange={(event: React.ChangeEvent<HTMLInputElement>) => onToggleAll(event.target.checked)}
                    />
                    <Icon name="star" style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
                </div>
            ) as any,
            width: 48 as any,
            render: (_: any, record: Journey) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', justifyContent: 'center' }}>
                    <Checkbox
                        checked={selectedJourneyIds.includes(record.journey_id)}
                        onChange={(event: React.ChangeEvent<HTMLInputElement>) =>
                            onToggleRow(record.journey_id, event.target.checked)
                        }
                    />
                    <Icon name="star" style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
                </div>
            )
        },
        {
            key: 'feed_unique_id',
            title: 'Feed Unique ID',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellText type="primary">{record.feed_unique_id}</TableCellText>
                    </div>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <Button variant="link" style={{ padding: 0, height: 'auto', fontSize: 'var(--font-size-sm)' }}>
                            View ID's
                        </Button>
                    </div>
                </div>
            )
        },
        {
            key: 'from',
            title: 'From',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellItem
                            text={record.origin_display}
                            textType="primary"
                            badge={<Badge variant="normal">+1P</Badge>}
                        />
                    </div>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellText type="secondary">{record.origin_company_display}</TableCellText>
                    </div>
                </div>
            )
        },
        {
            key: 'to',
            title: 'To',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellItem
                            text={record.destination_display}
                            textType="primary"
                            badge={<Badge variant="normal">+3D</Badge>}
                        />
                    </div>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellText type="secondary">{record.destination_company_display}</TableCellText>
                    </div>
                </div>
            )
        },
        {
            key: 'vehicle',
            title: 'Vehicle Info',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellText type="primary">{record.vehicle_number}</TableCellText>
                    </div>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellItem
                            text={record.transporter_name}
                            textType="secondary"
                            suffixIcon="chevron-right"
                        />
                    </div>
                </div>
            )
        },
        {
            key: 'trip',
            title: 'Trip Info',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', overflow: 'hidden', minWidth: 0, maxWidth: '100%' }}>
                        {getTripIcon(record.trip_type_display)}
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                            <TableCellText type="primary">{record.trip_type_display}</TableCellText>
                        </div>
                    </div>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellItem
                            text={record.trip_id}
                            textType="primary"
                            prefixIcon="check-fill"
                        />
                    </div>
                </div>
            )
        },
        {
            key: 'status',
            title: 'Status',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellItem
                            text={record.status_display}
                            textType="primary"
                            prefixIcon="location"
                        />
                    </div>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                        <TableCellItem
                            text={record.current_location_display}
                            textType="secondary"
                            prefixIcon="location"
                        />
                    </div>
                </div>
            )
        },
        {
            key: 'sla',
            title: 'SLA',
            width: 200 as any,
            render: (_: any, record: Journey) => {
                const isOnTime = record.sla_status === 'on_time';
                return (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                            <TableCellItem
                                text={record.sla_status_display}
                                textType="primary"
                                prefixIcon={isOnTime ? "check-fill" : "clock"}
                            />
                        </div>
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                            <TableCellText type="secondary">{record.eta_display}</TableCellText>
                        </div>
                    </div>
                );
            }
        },
        {
            key: 'alerts',
            title: 'Alerts',
            width: 200 as any,
            render: (_: any, record: Journey) => {
                if (!record.alert_type) return null;
                return (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', overflow: 'hidden', minWidth: 0, width: '100%' }}>
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                            <Badge variant="danger">{getAlertLabel(record.alert_type)}</Badge>
                        </div>
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '100%', minWidth: 0 }}>
                            <TableCellText type="secondary">{record.alert_time_display || '1 hour ago'}</TableCellText>
                        </div>
                    </div>
                );
            }
        },
        {
            key: 'actions',
            title: 'Actions',
            width: 100 as any,
            render: () => (
                <div style={{ display: 'flex', gap: 'var(--space-2)', justifyContent: 'flex-end' }}>
                    <Button variant="secondary" style={{ width: '32px', height: '32px', padding: 0, borderRadius: '50%', border: '1px solid var(--border-primary)' }}>
                        <Icon name="more" style={{ width: '16px', height: '16px' }} />
                    </Button>
                    <Button variant="secondary" style={{ width: '32px', height: '32px', padding: 0, borderRadius: '50%', border: '1px solid var(--border-primary)' }}>
                        <Icon name="chevron-right" style={{ width: '16px', height: '16px' }} />
                    </Button>
                </div>
            )
        }
    ];
