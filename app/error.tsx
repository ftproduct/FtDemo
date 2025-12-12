/**
 * Global error boundary
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

'use client';

import { useEffect } from 'react';
import { Button } from 'ft-design-system';

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error('Global error:', error);
  }, [error]);

  return (
    <div className="p-8 flex flex-col items-center justify-center min-h-screen gap-4">
      <h2 className="text-xl-rem font-semibold text-critical">
        Something went wrong!
      </h2>
      <p className="text-sm-rem text-neutral-500 text-center">
        {error.message || 'An unexpected error occurred'}
      </p>
      {error.digest && (
        <p className="text-xs-rem text-neutral-500">
          Error ID: {error.digest}
        </p>
      )}
      <Button variant="primary" size="md" onClick={reset}>
        Try again
      </Button>
    </div>
  );
}
