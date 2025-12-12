/**
 * Global loading UI
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

import { LoadingSkeleton } from '@/components/ui/loading-skeleton';

export default function Loading() {
  return (
    <div className="p-8 flex flex-col gap-4">
      <LoadingSkeleton className="h-10 w-72" />
      <LoadingSkeleton className="h-5 w-48" />
      <div className="mt-4">
        <LoadingSkeleton className="h-96 w-full" />
      </div>
    </div>
  );
}
