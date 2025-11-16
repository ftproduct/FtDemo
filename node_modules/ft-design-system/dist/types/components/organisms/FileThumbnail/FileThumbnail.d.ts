import React from 'react';
export interface FileThumbnailProps extends React.HTMLAttributes<HTMLDivElement> {
    fileName: string;
    onDownload?: () => void;
    variant?: 'uploaded' | 'downloading';
}
export declare const FileThumbnail: React.ForwardRefExoticComponent<FileThumbnailProps & React.RefAttributes<HTMLDivElement>>;
//# sourceMappingURL=FileThumbnail.d.ts.map