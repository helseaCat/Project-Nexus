import { useNavigate, useParams } from 'react-router-dom';
import { useContract, usePublishContract } from '@/hooks/useContracts';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';

export function ContractDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: contract, isLoading, error, refetch } = useContract(id!);
  const publishMutation = usePublishContract();

  if (isLoading) return <LoadingSpinner />;
  if (error || !contract) {
    return <ErrorMessage message="Failed to load contract" onRetry={() => refetch()} />;
  }

  const isDraft = contract.status === 'DRAFT';

  async function handlePublish() {
    await publishMutation.mutateAsync(contract!.id);
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{contract.name}</h1>
          <div className="mt-1 flex items-center gap-3">
            <StatusBadge status={contract.status} />
            <span className="text-sm text-gray-500">v{contract.version}</span>
          </div>
        </div>

        {isDraft && (
          <div className="flex gap-2">
            <button
              type="button"
              className="btn-secondary"
              onClick={() => navigate(buildRoute(ROUTES.CONTRACT_EDIT, { id: contract.id }))}
            >
              Edit
            </button>
            <button
              type="button"
              className="btn-primary"
              onClick={handlePublish}
              disabled={publishMutation.isPending}
            >
              {publishMutation.isPending ? 'Publishing…' : 'Publish'}
            </button>
          </div>
        )}
      </div>

      {publishMutation.isError && (
        <p className="mb-4 text-sm text-red-600">Failed to publish contract. Please try again.</p>
      )}

      <section className="space-y-6">
        <div className="rounded-md border border-gray-200 bg-white p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Details</h2>
          <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <dt className="text-sm font-medium text-gray-500">Description</dt>
              <dd className="mt-1 text-sm text-gray-900">{contract.description || '—'}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Created</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {new Date(contract.createdAt).toLocaleDateString()}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Last Updated</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {new Date(contract.updatedAt).toLocaleDateString()}
              </dd>
            </div>
            {contract.publishedAt && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Published</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {new Date(contract.publishedAt).toLocaleDateString()}
                </dd>
              </div>
            )}
          </dl>
        </div>

        {contract.testVariables.length > 0 && (
          <div className="rounded-md border border-gray-200 bg-white p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Test Variables</h2>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Name</th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Type</th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Unit</th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Min</th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Max</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {contract.testVariables.map((tv) => (
                    <tr key={tv.id}>
                      <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">{tv.name}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">{tv.dataType}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">{tv.unit || '—'}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">{tv.minValue ?? '—'}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">{tv.maxValue ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
