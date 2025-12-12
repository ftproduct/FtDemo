/**
 * 404 Not Found page
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

'use client';

import { Button } from 'ft-design-system';
import Link from 'next/link';

export default function NotFound() {
  return (
    <div className="p-8 flex flex-col items-center justify-center min-h-screen gap-4">
      <h1 className="text-xxl-rem font-semibold text-primary-700">
        404
      </h1>
      <h2 className="text-lg-rem font-semibold text-primary-700">
        Page Not Found
      </h2>
      <p className="text-sm-rem text-neutral-500 text-center">
        The page you are looking for does not exist.
      </p>
      <Link href="/dashboard">
        <Button variant="primary" size="md">Go to Dashboard</Button>
      </Link>
    </div>
  );
}
