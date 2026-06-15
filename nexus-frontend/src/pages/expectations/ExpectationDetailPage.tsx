import { useParams } from 'react-router-dom';
import { useExpectation, useActivateExpectation, useDeactivateExpectation } from '@/hooks/useExpectations';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';

export function ExpectationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: expectation, isLoading, error, refetch } = useExpectation(id!);
  const activateMutation = useActivateExpectation();
  const deactivateMutation = useDeactivateExpectation();

  if (isLoading) return <LoadingSpinner />;
  if (error || !expectation) {
    return <ErrorMessage message="Failed to load expectation" onRetry={() => refetch()} />;
  }

  const togglePending = activateMutation.isPending || deactivateMutation.isPending;

  async function handleToggle() {
    try {
      if (expectation!.active) {
        await deactivateMutation.mutateAsync(expectation!.id);
      } else {
        await activateMutation.mutateAsync(expectation!.id);
      }
    } catch {
      // Error state exposed via mutation.isError
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{expectation.name}</h1>
          <div className="mt-1 flex items-center gap-3">
            <StatusBadge status={expectation.severity} />
            <span className={`text-sm font-medium ${expectation.active ? 'text-green-700' : 'text-gray-500'}`}>
              {expectation.active ? 'Active' : 'Inactive'}
            </span>
          </div>
        </div>

        <button
          type="button"
          className={expectation.active ? 'btn-secondary' : 'btn-primary'}
          onClick={handleToggle}
          disabled={togglePending}
        >
          {togglePending
            ? 'Updating…'
            : expectation.active
              ? 'Deactivate'
              : 'Activate'}
        </button>
      </div>

      {(activateMutation.isError || deactivateMutation.isError) && (
        <p className="mb-4 text-sm text-red-600">Failed to update activation state. Please try again.</p>
      )}

      <div className="rounded-md border border-gray-200 bg-white p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Details</h2>
        <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-sm font-medium text-gray-500">Description</dt>
            <dd className="mt-1 text-sm text-gray-900">{expectation.description || '—'}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Contract ID</dt>
            <dd className="mt-1 text-sm text-gray-900 font-mono">{expectation.dataContractId}</dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-sm font-medium text-gray-500">Rule Expression</dt>
            <dd className="mt-1 text-sm text-gray-900 font-mono bg-gray-50 p-3 rounded">
              {expectation.ruleExpression}
            </dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Created</dt>
            <dd className="mt-1 text-sm text-gray-900">
              {new Date(expectation.createdAt).toLocaleDateString()}
            </dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Last Updated</dt>
            <dd className="mt-1 text-sm text-gray-900">
              {new Date(expectation.updatedAt).toLocaleDateString()}
            </dd>
          </div>
        </dl>
      </div>
    </div>
  );
}
