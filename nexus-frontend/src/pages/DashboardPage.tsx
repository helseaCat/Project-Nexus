import { useNavigate } from 'react-router-dom';
import { useContracts } from '@/hooks/useContracts';
import { useTasks } from '@/hooks/useTasks';
import { MetricCard } from '@/components/MetricCard';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES } from '@/utils/routes';

export function DashboardPage() {
  const navigate = useNavigate();
  const contracts = useContracts({ page: 0, size: 100 });
  const tasks = useTasks({ page: 0, size: 5, sort: 'createdAt,desc' });

  if (contracts.isLoading || tasks.isLoading) return <LoadingSpinner />;
  if (contracts.error) return <ErrorMessage message="Failed to load contracts" onRetry={() => contracts.refetch()} />;

  const contractData = contracts.data;
  const draftCount = contractData?.content.filter(c => c.status === 'DRAFT').length ?? 0;
  const publishedCount = contractData?.content.filter(c => c.status === 'PUBLISHED').length ?? 0;

  const recentTasks = tasks.data?.content ?? [];

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>

      <section aria-label="Summary metrics">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8">
          <MetricCard
            title="Draft Contracts"
            value={draftCount}
            onClick={() => navigate(ROUTES.CONTRACTS)}
          />
          <MetricCard
            title="Published Contracts"
            value={publishedCount}
            onClick={() => navigate(ROUTES.CONTRACTS)}
          />
          <MetricCard
            title="Total Contracts"
            value={contractData?.totalElements ?? 0}
            onClick={() => navigate(ROUTES.CONTRACTS)}
          />
          <MetricCard
            title="Tasks"
            value={tasks.data?.totalElements ?? 0}
            onClick={() => navigate(ROUTES.TASKS)}
          />
        </div>
      </section>

      <section aria-label="Recent tasks">
        <h2 className="text-lg font-semibold text-gray-900 mb-3">Recent Tasks</h2>
        {recentTasks.length === 0 ? (
          <p className="text-sm text-gray-500">No tasks yet.</p>
        ) : (
          <ul className="divide-y divide-gray-200 rounded-md border border-gray-200 bg-white">
            {recentTasks.map(task => (
              <li key={task.id} className="flex items-center justify-between px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-gray-900">{task.title}</p>
                  {task.linkedToType && (
                    <p className="text-xs text-gray-500">{task.linkedToType}</p>
                  )}
                </div>
                <StatusBadge status={task.status} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
