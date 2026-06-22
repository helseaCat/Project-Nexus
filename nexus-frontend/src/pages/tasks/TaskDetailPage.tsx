import { useParams } from 'react-router-dom';
import { useTask, useTransitionTaskStatus } from '@/hooks/useTasks';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { getValidTransitions, STATUS_LABELS } from '@/utils/transitions';
import type { TaskStatus } from '@/types/task';

export function TaskDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: task, isLoading, error, refetch } = useTask(id!);
  const transitionMutation = useTransitionTaskStatus();

  if (isLoading) return <LoadingSpinner />;
  if (error || !task) {
    return <ErrorMessage message="Failed to load task" onRetry={() => refetch()} />;
  }

  const validTransitions = getValidTransitions(task.status);

  async function handleTransition(newStatus: TaskStatus) {
    try {
      await transitionMutation.mutateAsync({ id: task!.id, status: newStatus });
    } catch {
      // Error state is exposed via transitionMutation.isError
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{task.title}</h1>
          <div className="mt-1 flex items-center gap-3">
            <StatusBadge status={task.status} />
            {task.aiGenerated && (
              <span className="rounded bg-purple-100 px-2 py-0.5 text-xs font-medium text-purple-700">
                AI Generated
              </span>
            )}
          </div>
        </div>

        {validTransitions.length > 0 && (
          <div className="flex gap-2">
            {validTransitions.map((status) => (
              <button
                key={status}
                type="button"
                className="btn-primary"
                onClick={() => handleTransition(status)}
                disabled={transitionMutation.isPending}
              >
                {transitionMutation.isPending ? 'Updating…' : `Move to ${STATUS_LABELS[status]}`}
              </button>
            ))}
          </div>
        )}
      </div>

      {transitionMutation.isError && (
        <p className="mb-4 text-sm text-red-600">
          Failed to update task status. Please try again.
        </p>
      )}

      <section className="space-y-6">
        <div className="rounded-md border border-gray-200 bg-white p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Details</h2>
          <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <dt className="text-sm font-medium text-gray-500">Description</dt>
              <dd className="mt-1 text-sm text-gray-900">{task.description || '—'}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Assignee</dt>
              <dd className="mt-1 text-sm text-gray-900">{task.assigneeId ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Due Date</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '—'}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Created</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {new Date(task.createdAt).toLocaleDateString()}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Last Updated</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {new Date(task.updatedAt).toLocaleDateString()}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Created By</dt>
              <dd className="mt-1 text-sm text-gray-900">{task.createdBy}</dd>
            </div>
          </dl>
        </div>

        {task.linkedToType && (
          <div className="rounded-md border border-gray-200 bg-white p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Linked Entity</h2>
            <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <dt className="text-sm font-medium text-gray-500">Type</dt>
                <dd className="mt-1 text-sm text-gray-900">{task.linkedToType}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">ID</dt>
                <dd className="mt-1 text-sm text-gray-900 font-mono text-xs">
                  {task.linkedToId}
                </dd>
              </div>
            </dl>
          </div>
        )}
      </section>
    </div>
  );
}
