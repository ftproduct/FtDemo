import React from 'react';
export interface FileTypeIconProps extends React.HTMLAttributes<HTMLDivElement> {
    fileType: string;
    variant?: 'default' | 'error';
    size?: 'sm' | 'md' | 'lg';
}
export declare const FileTypeIcon: React.ForwardRefExoticComponent<FileTypeIconProps & React.RefAttributes<HTMLDivElement>>;
//# sourceMappingURL=FileTypeIcon.d.ts.map