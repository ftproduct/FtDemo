import { useCallback, useEffect, useMemo, useState } from 'react';
import { NavigationPopover } from 'ft-design-system';
import ComponentGallery from './components/pages/ComponentGallery';
import MyJourneys from './components/pages/MyJourneys';
import PlaceholderPage from './components/pages/PlaceholderPage';
import {
  APP_NAVIGATION_SECTIONS,
  NAV_ROUTES,
  routeToNavEntry,
  routeToSectionId,
  sectionIdToRoute,
  sectionLabelRouteMap,
} from './navigation/sections';

const COMPONENT_ROUTE = '/components';
const DEFAULT_ROUTE = '/my-journeys';
const KNOWN_ROUTES = new Set([COMPONENT_ROUTE, ...NAV_ROUTES]);

const normalizeRoute = (rawPath: string | null | undefined) => {
  if (!rawPath) return DEFAULT_ROUTE;
  const lowercase = rawPath.toLowerCase();
  const base = lowercase.split('?')[0]?.split('#')[0] ?? '';
  const trimmed = base.replace(/\/+$/, '') || '/';

  if (trimmed === '/' || trimmed === '') {
    return DEFAULT_ROUTE;
  }

  if (KNOWN_ROUTES.has(trimmed)) {
    return trimmed;
  }

  if (trimmed.includes('components')) {
    return COMPONENT_ROUTE;
  }

  if (trimmed.includes('myjourneys') || trimmed.includes('my-journeys')) {
    return DEFAULT_ROUTE;
  }

  return DEFAULT_ROUTE;
};

const getInitialRoute = () => (typeof window === 'undefined' ? DEFAULT_ROUTE : normalizeRoute(window.location.pathname));

export default function App() {
  const [currentRoute, setCurrentRoute] = useState<string>(getInitialRoute);
  const [theme, setTheme] = useState<'light' | 'dark' | 'night'>('light');
  const [isNavigationOpen, setIsNavigationOpen] = useState(false);

  useEffect(() => {
    document.documentElement.className = theme;
  }, [theme]);

  useEffect(() => {
    const handlePopState = () => {
      setCurrentRoute(normalizeRoute(window.location.pathname));
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const navigateToRoute = useCallback((nextRoute: string) => {
    const normalized = normalizeRoute(nextRoute);
    setCurrentRoute((prev) => {
      if (prev === normalized) {
        return prev;
      }
      window.history.pushState({}, '', normalized);
      return normalized;
    });
  }, []);

  const openNavigation = useCallback(() => setIsNavigationOpen(true), []);
  const closeNavigation = useCallback(() => setIsNavigationOpen(false), []);

  const activeSectionId = routeToSectionId.get(currentRoute) ?? APP_NAVIGATION_SECTIONS[0]?.id ?? '';

  const activeSection = useMemo(
    () => APP_NAVIGATION_SECTIONS.find((section) => section.id === activeSectionId),
    [activeSectionId],
  );

  const activeNavEntry = routeToNavEntry.get(currentRoute);

  const pageContent = useMemo(() => {
    if (currentRoute === COMPONENT_ROUTE) {
      return <ComponentGallery />;
    }

    if (currentRoute === DEFAULT_ROUTE) {
      return <MyJourneys onOpenNavigation={openNavigation} />;
    }

    const placeholderTitle = activeNavEntry?.label ?? activeSection?.label ?? 'Coming Soon';
    const placeholderDescription =
      activeNavEntry?.description ?? activeSection?.hero?.description ?? 'This module is yet to be developed.';
    const placeholderIllustration = activeSection?.hero?.illustrationVariant ?? 'workspace';

    return (
      <PlaceholderPage
        title={placeholderTitle}
        description={placeholderDescription}
        illustrationVariant={placeholderIllustration}
        onOpenNavigation={openNavigation}
      />
    );
  }, [activeNavEntry, activeSection, currentRoute, openNavigation]);

  const handleSectionChange = useCallback(
    (sectionId: string) => {
      const route = sectionIdToRoute.get(sectionId);
      if (!route) return;
      navigateToRoute(route);
      closeNavigation();
    },
    [closeNavigation, navigateToRoute],
  );

  const handlePopoverClick = useCallback(
    (event: React.MouseEvent<HTMLDivElement>) => {
      event.stopPropagation();
      const button = (event.target as HTMLElement)?.closest('button');
      if (!button) return;

      const labelElement = button.querySelector('p');
      const rawLabel = (labelElement?.textContent ?? button.textContent ?? '').trim();
      if (!rawLabel) return;

      const normalizedLabel = rawLabel.split('\n')[0]?.trim().toLowerCase();
      if (!normalizedLabel) return;

      const key = `${activeSectionId}:${normalizedLabel}`;
      const matchedRoute = sectionLabelRouteMap.get(key);
      if (!matchedRoute) return;

      navigateToRoute(matchedRoute);
      closeNavigation();
    },
    [activeSectionId, closeNavigation, navigateToRoute],
  );

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-secondary)' }}>
      {pageContent}

      {isNavigationOpen && (
        <div
          role="presentation"
          onClick={closeNavigation}
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'var(--overlay-scrim)',
            zIndex: 1000,
            display: 'flex',
            justifyContent: 'flex-start',
            alignItems: 'flex-start',
            padding: 'var(--space-6)',
          }}
        >
          <div
            onClick={handlePopoverClick}
            style={{
              width: 'min(1200px, 100%)',
              marginLeft: 0,
            }}
          >
            <NavigationPopover
              open={isNavigationOpen}
              onClose={closeNavigation}
              sections={APP_NAVIGATION_SECTIONS}
              initialSectionId={activeSectionId}
              onSectionChange={handleSectionChange}
            />
          </div>
        </div>
      )}
    </div>
  );
}
