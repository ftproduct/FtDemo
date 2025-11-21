import { DEFAULT_NAVIGATION_SECTIONS } from 'ft-design-system/ai';

export type NavigationSectionShape = (typeof DEFAULT_NAVIGATION_SECTIONS)[number];

type BaseSubCategoryItem = NavigationSectionShape['subCategories'] extends Array<infer Category>
  ? Category extends { items: infer Items }
  ? Items extends Array<infer Item>
  ? Item
  : never
  : never
  : never;

type AppNavigationSubCategoryItem = BaseSubCategoryItem & { route: string };
type AppNavigationSubCategory = { title?: string; items: AppNavigationSubCategoryItem[] };

export type AppNavigationSection = Omit<NavigationSectionShape, 'subCategories'> & {
  route: string;
  subCategories?: AppNavigationSubCategory[];
};

type NavigationRouteEntry = {
  label: string;
  route: string;
  sectionId: string;
  description?: string;
};

export const APP_NAVIGATION_SECTIONS: AppNavigationSection[] = [
  {
    id: 'summary',
    label: 'Summary Page',
    icon: 'home',
    route: '/summary',
    hero: {
      illustrationVariant: 'workspace',
      title: 'Summary Page',
      description: 'View performance, operations, and order-level insights at a glance.',
    },
    metrics: [
      {
        variant: 'highlight',
        title: 'Performance',
        description: 'Track high-level KPIs to measure overall logistics performance.',
      },
      {
        variant: 'highlight',
        title: 'Operations',
        description: 'Monitor day-to-day execution to ensure every handoff stays on track.',
      },
      {
        variant: 'highlight',
        title: 'Orders',
        description: 'See every order stage with ownership, next steps, and alerts in one view.',
      },
    ],
  },
  {
    id: 'planning',
    label: 'Planning',
    icon: 'planning',
    route: '/planning',
    hero: {
      illustrationVariant: 'workspace',
      title: 'Planning',
      description: 'Plan and optimize routes, capacity, and demand allocation.',
    },
    metrics: [
      { label: 'Unplanned Orders', value: '200' },
      { label: 'Planned Orders', value: '200' },
      { label: 'Dispatched Orders', value: '200' },
      { label: 'Delivered Orders', value: '200' },
    ],
  },
  {
    id: 'full-truck-load',
    label: 'Full Truck Load',
    icon: 'truck',
    route: '/full-truck-load',
    showChevron: true,
    subCategories: [
      {
        title: 'Indent',
        items: [
          { label: 'My Indents', description: 'Create and manage indent requests.', route: '/full-truck-load/my-indents' },
          { label: 'Assigned Vehicles', description: 'Track allocations for every indent.', route: '/full-truck-load/assigned-vehicles' },
        ],
      },
      {
        title: 'Tracking',
        items: [
          { label: 'My Journeys', description: 'Live tracking for every active journey.', status: 'active', route: '/my-journeys' },
          { label: 'History', description: 'Audit completed journeys and milestones.', route: '/full-truck-load/history' },
          { label: 'Live View', description: 'Visual map of every moving truck.', route: '/full-truck-load/live-view' },
          { label: 'Yard Management', description: 'Unlock to orchestrate yard traffic.', disabled: true, route: '/full-truck-load/yard-management' },
          { label: 'Dedicated Vehicles', description: 'Visibility into dedicated fleets.', disabled: true, route: '/full-truck-load/dedicated-vehicles' },
        ],
      },
      {
        title: 'Freight Invoicing',
        items: [
          { label: 'Freight Bill', description: 'Digitised freight bill creation.', route: '/full-truck-load/freight-bill' },
          { label: 'Reconciliation', description: 'Match trips, LR, and invoices automatically.', route: '/full-truck-load/reconciliation' },
          { label: 'Dispute Management', description: 'Resolve billing differences faster.', route: '/full-truck-load/dispute-management' },
          { label: 'Contracts', description: 'Reference contracted rates and terms.', route: '/full-truck-load/contracts' },
        ],
      },
    ],
  },
  {
    id: 'part-truck-load',
    label: 'Part Truck Load',
    icon: 'warehouse',
    route: '/part-truck-load',
    showChevron: true,
    subCategories: [
      {
        title: 'Operations',
        items: [
          { label: 'Orders', description: 'Manage PTL orders and promises.', route: '/part-truck-load/orders' },
          { label: 'Shipments', description: 'Track every shared load.', status: 'active', route: '/part-truck-load/shipments' },
          { label: 'Contract Bill', description: 'Contract specific billing view.', route: '/part-truck-load/contract-bill' },
          { label: 'Reconciliation', description: 'Settle charges across shippers.', route: '/part-truck-load/reconciliation' },
        ],
      },
    ],
  },
  {
    id: 'control-tower',
    label: 'Control Tower',
    icon: 'control-tower',
    route: '/control-tower',
    hero: {
      illustrationVariant: 'workspace',
      title: 'Control Tower',
      description: 'Monitor and resolve exceptions across shipments in real-time.',
    },
    metrics: [
      { variant: 'alert', label: 'Critical', value: '200', badgeVariant: 'danger', description: 'Shipments requiring immediate action.' },
      { variant: 'alert', label: 'High', value: '200', badgeVariant: 'warning', description: 'High priority events to review.' },
      { variant: 'alert', label: 'Medium', value: '200', badgeVariant: 'neutral', description: 'Exceptions being worked on.' },
      { variant: 'alert', label: 'Expired', value: '200', badgeVariant: 'neutral', description: 'Tasks overdue for closure.' },
    ],
  },
  {
    id: 'dashboard',
    label: 'Dashboard',
    icon: 'dashboard',
    route: '/dashboard',
    showChevron: true,
    subCategories: [
      {
        title: 'Overview',
        items: [
          { label: 'Network KPIs', description: 'Compare SLA, cost, and throughput at a glance.', route: '/dashboard/network-kpis' },
          { label: 'Alert Feed', description: 'Escalations streaming in real-time.', route: '/dashboard/alert-feed' },
          { label: 'Customization', description: 'Drag-and-drop widgets for personal views.', route: '/dashboard/customization' },
        ],
      },
      {
        title: 'Automation',
        items: [
          { label: 'Alert Builder', description: 'Configure rules that watch every lane.', route: '/dashboard/alert-builder' },
          { label: 'Scheduled Emails', description: 'Share dashboards via recurring digests.', route: '/dashboard/scheduled-emails' },
        ],
      },
    ],
  },
  {
    id: 'reports',
    label: 'Reports',
    icon: 'reports',
    route: '/reports',
    hero: {
      illustrationVariant: 'workspace',
      title: 'Reports',
      description: "Track, analyze, and share shipment insights using Freight Tiger's templates or your own.",
    },
    metrics: [
      {
        variant: 'highlight',
        title: 'FT Reports',
        description: 'Pre-built reports to monitor exceptions, performance, and trends.',
      },
      {
        variant: 'highlight',
        title: 'My Reports',
        description: 'Personalised reports tailored to KPIs, partners, or corridors.',
      },
    ],
  },
  {
    id: 'onboarding',
    label: 'Onboarding',
    icon: 'data-stack',
    route: '/onboarding',
    showChevron: true,
    subCategories: [
      {
        title: 'Organization Set Up',
        items: [
          { label: 'My account', route: '/onboarding/my-account' },
          { label: 'Branches', route: '/onboarding/branches' },
          { label: 'Groups', route: '/onboarding/groups' },
          { label: 'Departments', route: '/onboarding/departments' },
          { label: 'Related Partners', route: '/onboarding/related-partners' },
          { label: 'Users', route: '/onboarding/users' },
          { label: 'Notifications', route: '/onboarding/notifications' },
          { label: 'Vehicle Master', route: '/onboarding/vehicle-master' },
          { label: 'Driver Master', route: '/onboarding/driver-master' },
          { label: 'History', route: '/onboarding/history' },
          { label: 'Past Uploads', route: '/onboarding/past-uploads' },
        ],
      },
      {
        title: 'Configurations',
        items: [
          { label: 'Template Management', route: '/onboarding/template-management' },
          { label: 'Transporter Onboarding', route: '/onboarding/transporter-onboarding' },
          { label: 'Live View', route: '/onboarding/live-view' },
          { label: 'Yard Management', route: '/onboarding/yard-management' },
          { label: 'ePOD', route: '/onboarding/epod' },
          { label: 'Dedicated Vehicles', route: '/onboarding/dedicated-vehicles' },
        ],
      },
      {
        title: 'Master Data',
        items: [{ label: 'Location Master', route: '/onboarding/location-master' }],
      },
    ],
  },
];

const flattenNavigationEntries = (): NavigationRouteEntry[] => {
  const entries: NavigationRouteEntry[] = [];

  APP_NAVIGATION_SECTIONS.forEach((section) => {
    entries.push({
      label: section.label,
      route: section.route,
      sectionId: section.id,
      description: section.hero?.description,
    });

    section.subCategories?.forEach((category) => {
      category.items.forEach((item) => {
        entries.push({
          label: item.label,
          route: item.route,
          sectionId: section.id,
          description: item.description,
        });
      });
    });
  });

  return entries;
};

const NAVIGATION_ENTRIES = flattenNavigationEntries();

export const NAV_ROUTES = NAVIGATION_ENTRIES.map((entry) => entry.route);

export const routeToNavEntry = new Map<string, NavigationRouteEntry>(
  NAVIGATION_ENTRIES.map((entry) => [entry.route, entry]),
);

export const routeToSectionId = new Map<string, string>(
  NAVIGATION_ENTRIES.map((entry) => [entry.route, entry.sectionId]),
);

export const sectionLabelRouteMap = new Map<string, string>(
  NAVIGATION_ENTRIES.map((entry) => [`${entry.sectionId}:${entry.label.toLowerCase()}`, entry.route]),
);

export const sectionIdToRoute = new Map<string, string>(
  APP_NAVIGATION_SECTIONS.map((section) => [section.id, section.route]),
);

export const REDIRECT_ROUTES = new Map<string, string>();

APP_NAVIGATION_SECTIONS.forEach((section) => {
  if (section.subCategories?.length) {
    let targetRoute: string | undefined;

    // Find active item
    for (const category of section.subCategories) {
      const activeItem = category.items.find((item) => (item as any).status === 'active');
      if (activeItem) {
        targetRoute = activeItem.route;
        break;
      }
    }

    // Fallback to first item
    if (!targetRoute && section.subCategories[0]?.items?.length) {
      targetRoute = section.subCategories[0].items[0].route;
    }

    if (targetRoute) {
      REDIRECT_ROUTES.set(section.route, targetRoute);
    }
  }
});
