import AppHeader from '../AppHeader';

interface PlaceholderPageProps {
  title: string;
  description?: string;
  onOpenNavigation: () => void;
}

export default function PlaceholderPage({ title, description, onOpenNavigation }: PlaceholderPageProps) {
  return (
    <div style={{ backgroundColor: 'var(--bg-secondary)', minHeight: '100vh' }}>
      <AppHeader onOpenNavigation={onOpenNavigation} />
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 'var(--space-16)',
        }}
      >
        <div
          style={{
            color: 'var(--primary)',
            backgroundColor: 'var(--bg-primary)',
            border: '1px dashed var(--border-primary)',
            borderRadius: 'var(--radius-md)',
            padding: 'var(--space-8) var(--space-12)',
            textAlign: 'center',
            fontWeight: 'var(--font-weight-semibold)',
          }}
        >
          Yet to be built
        </div>
      </div>
    </div>
  );
}
