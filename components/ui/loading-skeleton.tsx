/**
 * LoadingSkeleton - Loading state component
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

import { cn } from '@/lib/utils/cn';

interface LoadingSkeletonProps {
  className?: string;
}

export function LoadingSkeleton({ className }: LoadingSkeletonProps) {
  return (
    <div
      className={cn(
        'bg-neutral-100 rounded-lg animate-pulse',
        className
      )}
    />
  );
}

export function LoadingSkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="flex flex-col gap-2">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4">
          <LoadingSkeleton className="h-5 w-1/4" />
          <LoadingSkeleton className="h-5 w-1/5" />
          <LoadingSkeleton className="h-5 w-1/5" />
          <LoadingSkeleton className="h-5 w-[15%]" />
        </div>
      ))}
    </div>
  );
}
