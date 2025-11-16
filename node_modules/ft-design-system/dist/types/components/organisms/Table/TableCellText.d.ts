import React from 'react';
export type CellTextType = 'primary' | 'secondary';
export interface TableCellTextProps {
    type?: CellTextType;
    children: React.ReactNode;
    className?: string;
}
export declare const TableCellText: React.FC<TableCellTextProps>;
//# sourceMappingURL=TableCellText.d.ts.map