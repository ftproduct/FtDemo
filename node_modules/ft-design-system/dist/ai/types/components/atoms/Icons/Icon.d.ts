import React from 'react';
import { iconMap } from './iconMap';
export type IconName = keyof typeof iconMap;
export interface IconProps {
    name: IconName;
    size?: number | string;
    color?: string;
    className?: string;
    style?: React.CSSProperties;
}
export declare const Icon: React.FC<IconProps>;
export default Icon;
//# sourceMappingURL=Icon.d.ts.map