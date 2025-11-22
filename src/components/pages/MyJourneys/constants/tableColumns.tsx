import React from 'react';
import type { TableColumn } from 'ft-design-system/ai';
import { Checkbox, Badge, Button } from 'ft-design-system/ai';
import { TableCell, TableCellText, TableCellItem, Icon } from 'ft-design-system';
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
                <TableCell lineVariant="single">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        <Checkbox
                            checked={selectAll}
                            onChange={(event: React.ChangeEvent<HTMLInputElement>) => onToggleAll(event.target.checked)}
                        />
                        <Icon name="star" style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
                    </div>
                </TableCell>
            ) as any,
            width: 48 as any,
            render: (_: any, record: Journey) => (
                <TableCell lineVariant="single">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        <Checkbox
                            checked={selectedJourneyIds.includes(record.journey_id)}
                            onChange={(event: React.ChangeEvent<HTMLInputElement>) =>
                                onToggleRow(record.journey_id, event.target.checked)
                            }
                        />
                        <Icon name="star" style={{ width: '16px', height: '16px', color: 'var(--secondary)' }} />
                    </div>
                </TableCell>
            )
        },
        {
            key: 'feed_unique_id',
            title: 'Feed Unique ID',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <TableCell lineVariant="double">
                    <TableCellText type="primary">{record.feed_unique_id}</TableCellText>
                    <Button variant="link" style={{ padding: 0, height: 'auto', fontSize: 'var(--font-size-sm)' }}>
                        View ID's
                    </Button>
                </TableCell>
            )
        },
        {
            key: 'from',
            title: 'From',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <TableCell lineVariant="double">
                    <TableCellItem
                        text={record.origin_display}
                        textType="primary"
                        badge={<Badge variant="normal">+1P</Badge>}
                    />
                    <TableCellText type="secondary">{record.origin_company_display}</TableCellText>
                </TableCell>
            )
        },
        {
            key: 'to',
            title: 'To',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <TableCell lineVariant="double">
                    <TableCellItem
                        text={record.destination_display}
                        textType="primary"
                        badge={<Badge variant="normal">+3D</Badge>}
                    />
                    <TableCellText type="secondary">{record.destination_company_display}</TableCellText>
                </TableCell>
            )
        },
        {
            key: 'vehicle',
            title: 'Vehicle Info',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <TableCell lineVariant="double">
                    <TableCellText type="primary">{record.vehicle_number}</TableCellText>
                    <TableCellItem
                        text={record.transporter_name}
                        textType="secondary"
                        suffixIcon="chevron-right"
                    />
                </TableCell>
            )
        },
        {
            key: 'trip',
            title: 'Trip Info',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <TableCell lineVariant="double">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        {getTripIcon(record.trip_type_display)}
                        <TableCellText type="primary">{record.trip_type_display}</TableCellText>
                    </div>
                    <TableCellItem
                        text={record.trip_id}
                        textType="primary"
                        prefixIcon="check-fill"
                    />
                </TableCell>
            )
        },
        {
            key: 'status',
            title: 'Status',
            width: 200 as any,
            render: (_: any, record: Journey) => (
                <TableCell lineVariant="double">
                    <TableCellItem
                        text={record.status_display}
                        textType="primary"
                        prefixIcon="location"
                    />
                    <TableCellItem
                        text={record.current_location_display}
                        textType="secondary"
                        prefixIcon="location"
                    />
                </TableCell>
            )
        },
        {
            key: 'sla',
            title: 'SLA',
            width: 200 as any,
            render: (_: any, record: Journey) => {
                const isOnTime = record.sla_status === 'on_time';
                return (
                    <TableCell lineVariant="double">
                        <TableCellItem
                            text={record.sla_status_display}
                            textType="primary"
                            prefixIcon={isOnTime ? "check-fill" : "clock"}
                        />
                        <TableCellText type="secondary">{record.eta_display}</TableCellText>
                    </TableCell>
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
                    <TableCell lineVariant="single">
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                            <Badge variant="danger">{getAlertLabel(record.alert_type)}</Badge>
                            <TableCellText type="secondary">{record.alert_time_display || '1 hour ago'}</TableCellText>
                        </div>
                    </TableCell>
                );
            }
        },
        {
            key: 'actions',
            title: 'Actions',
            width: 100 as any,
            render: () => (
                <TableCell lineVariant="single">
                    <div style={{ display: 'flex', gap: 'var(--space-2)', justifyContent: 'flex-end' }}>
                        <Button variant="text" style={{ width: '32px', height: '32px', padding: 0 }}>
                            <Icon name="more" style={{ width: '16px', height: '16px' }} />
                        </Button>
                        <Button variant="text" style={{ width: '32px', height: '32px', padding: 0 }}>
                            <Icon name="chevron-right" style={{ width: '16px', height: '16px' }} />
                        </Button>
                    </div>
                </TableCell>
            )
        }
    ];
