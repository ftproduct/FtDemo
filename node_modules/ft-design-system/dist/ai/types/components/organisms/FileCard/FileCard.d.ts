import React from 'react';
export interface FileStats {
    total?: number;
    success?: number;
    invalid?: number;
}
export interface FileCardProps extends React.HTMLAttributes<HTMLDivElement> {
    fileName: string;
    fileType: string;
    fileDate?: string;
    status: 'uploading' | 'validating' | 'processed' | 'partially-processed' | 'failed' | 'template-mismatch' | 'upload-failed' | 'unsupported' | 'empty' | 'too-large';
    progress?: number;
    stats?: FileStats;
    errorMessage?: string;
    onDownload?: () => void;
    onPreview?: () => void;
    onDelete?: () => void;
    onRefresh?: () => void;
    onClose?: () => void;
    variant?: 'compact' | 'expanded' | 'with-progress' | 'with-stats';
    downloadDisabled?: boolean;
}
export declare const FileCard: React.ForwardRefExoticComponent<FileCardProps & React.RefAttributes<HTMLDivElement>>;
//# sourceMappingURL=FileCard.d.ts.map