interface MetricCardProps {
  title: string;
  value: number | string;
  onClick?: () => void;
}

export function MetricCard({ title, value, onClick }: MetricCardProps) {
  const Component = onClick ? 'button' : 'div';
  return (
    <Component
      onClick={onClick}
      className={`card text-left ${onClick ? 'cursor-pointer hover:border-nexus-300 hover:shadow-md transition-shadow' : ''}`}
    >
      <p className="text-sm font-medium text-gray-500">{title}</p>
      <p className="mt-1 text-2xl font-semibold text-gray-900">{value}</p>
    </Component>
  );
}
