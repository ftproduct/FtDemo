/**
 * MissingComponent - Placeholder for components not available in FT Design System
 * Using FT Design System Tailwind classes (no forbidden patterns)
 */

interface MissingComponentProps {
  name: string;
  description?: string;
}

export function MissingComponent({ name, description }: MissingComponentProps) {
  return (
    <div className="p-4 border-2 border-dashed border-neutral-300 rounded-lg bg-neutral-50 text-center text-neutral-500">
      <div className="text-lg-rem font-semibold mb-2 text-primary-700">
        Component Not Available: {name}
      </div>
      {description && (
        <div className="text-sm-rem text-neutral-500">
          {description}
        </div>
      )}
      <div className="text-xs-rem mt-2 text-neutral-500">
        This component is shown in the design but not available in ft-design-system yet.
      </div>
    </div>
  );
}
