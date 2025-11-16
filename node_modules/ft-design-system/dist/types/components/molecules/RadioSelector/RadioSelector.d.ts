import React from 'react';
export interface RadioSelectorProps {
    name: string;
    value?: string;
    defaultValue?: string;
    options: Array<{
        value: string;
        header: string;
        description?: string;
        icon?: React.ReactNode;
        disabled?: boolean;
        hideRadio?: boolean;
    }>;
    onChange?: (value: string) => void;
    className?: string;
}
export declare const RadioSelector: React.FC<RadioSelectorProps>;
//# sourceMappingURL=RadioSelector.d.ts.map