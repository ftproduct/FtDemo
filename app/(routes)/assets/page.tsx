/**
 * Assets page (placeholder)
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

import { AppHeader } from '@/components/layout/app-header';
import { AppSidebar } from '@/components/layout/app-sidebar';

export default function AssetsPage() {
  return (
    <div className="flex flex-col min-h-screen">
      <AppHeader />
      <div className="flex flex-1">
        <AppSidebar />
        <main className="flex-1 p-5 ml-[250px]">
          <h1 className="text-xl-rem font-semibold mb-4 text-primary-700">
            Assets
          </h1>
          <p className="text-neutral-500">
            Assets page placeholder - ready for pixel-perfect implementation
          </p>
        </main>
      </div>
    </div>
  );
}
