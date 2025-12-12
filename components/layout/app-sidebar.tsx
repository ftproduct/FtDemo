/**
 * AppSidebar - Application sidebar component
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

'use client';

import { useAppStore } from '@/lib/store';
import { cn } from '@/lib/utils/cn';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export function AppSidebar() {
  const sidebarOpen = useAppStore((state) => state.sidebarOpen);
  const pathname = usePathname();

  if (!sidebarOpen) {
    return null;
  }

  const navItems = [
    { name: 'Dashboard', href: '/dashboard' },
    { name: 'Components', href: '/components' },
    { name: 'My Journeys', href: '/my-journeys' },
    { name: 'Orders', href: '/orders' },
    { name: 'Shipments', href: '/shipments' },
    { name: 'Assets', href: '/assets' },
  ];

  return (
    <aside
      className={cn(
        'w-[250px] h-screen border-r border-neutral-200 bg-white p-4',
        'fixed left-0 top-0 z-50'
      )}
    >
      <div className="mb-4 mt-12">
        <h2 className="text-md-rem font-semibold text-primary-700">
          Navigation
        </h2>
      </div>
      <nav className="flex flex-col gap-2">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'p-3 rounded-lg text-sm-rem no-underline transition-colors',
                isActive
                  ? 'text-primary-700 bg-neutral-100 font-semibold'
                  : 'text-neutral-600 bg-transparent font-normal hover:bg-neutral-100'
              )}
            >
              {item.name}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
