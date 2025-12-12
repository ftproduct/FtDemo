/**
 * AppHeader - Application header using FT Design System
 */

'use client';

import { AppHeader as FTAppHeader, Button } from 'ft-design-system';
import { useAppStore } from '@/lib/store';

export function AppHeader() {
  const sidebarOpen = useAppStore((state) => state.sidebarOpen);
  const setSidebarOpen = useAppStore((state) => state.setSidebarOpen);

  // Mock user data for the header
  const user = {
    name: 'Demo User',
    avatar: undefined,
    role: 'Administrator',
    location: 'San Francisco, CA',
  };

  const handleNotificationClick = (type: 'rocket' | 'bell' | 'menu') => {
    console.log(`Notification clicked: ${type}`);
    if (type === 'menu') {
      setSidebarOpen(!sidebarOpen);
    }
  };

  const handleUserClick = () => {
    console.log('User profile clicked');
  };

  const handleUserMenuItemClick = (item: string) => {
    console.log(`Menu item clicked: ${item}`);
  };

  return (
    <FTAppHeader
      size="xl"
      device="Desktop"
      user={user}
      onNotificationClick={handleNotificationClick}
      onUserClick={handleUserClick}
      onUserMenuItemClick={handleUserMenuItemClick}
      leftAddon={() => (
        <Button
          variant="text"
          icon="menu"
          iconPosition="only"
          onClick={() => setSidebarOpen(!sidebarOpen)}
        />
      )}
    />
  );
}
