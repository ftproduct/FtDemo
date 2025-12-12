interface MissingComponentProps {
  name: string;
  description?: string;
}

export function MissingComponent({ name, description }: MissingComponentProps) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '200px',
        padding: 'var(--space-6)',
        border: '2px dashed var(--border-primary)',
        borderRadius: 'var(--radius-md)',
        backgroundColor: 'var(--bg-primary)',
        gap: 'var(--space-2)',
      }}
    >
      <div
        style={{
          fontSize: 'var(--font-size-xxl)',
          color: 'var(--tertiary)',
        }}
      >
        📦
      </div>
      <div
        style={{
          fontSize: 'var(--font-size-lg)',
          fontWeight: 'var(--font-weight-semibold)',
          color: 'var(--secondary)',
          textAlign: 'center',
        }}
      >
        Component Missing
      </div>
      <div
        style={{
          fontSize: 'var(--font-size-md)',
          fontWeight: 'var(--font-weight-medium)',
          color: 'var(--primary)',
          textAlign: 'center',
        }}
      >
        {name}
      </div>
      {description && (
        <div
          style={{
            fontSize: 'var(--font-size-sm)',
            color: 'var(--tertiary)',
            textAlign: 'center',
            maxWidth: '300px',
          }}
        >
          {description}
        </div>
      )}
      <div
        style={{
          fontSize: 'var(--font-size-xs)',
          color: 'var(--tertiary)',
          marginTop: 'var(--space-2)',
          padding: 'var(--space-2) var(--space-3)',
          backgroundColor: 'var(--surface-alt)',
          borderRadius: 'var(--radius-sm)',
        }}
      >
        Not available in ft-design-system
      </div>
    </div>
  );
}

