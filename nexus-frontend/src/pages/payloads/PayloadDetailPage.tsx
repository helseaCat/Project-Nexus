import { useParams, useNavigate } from 'react-router-dom';
import { usePayload, usePayloadDeviations } from '@/hooks/usePayloads';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { DataTable, type Column } from '@/components/DataTable';
import { Pagination } from '@/components/Pagination';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { Deviation } from '@/types/deviation';
import { useState } from 'react';

const DEVIATIONS_PAGE_SIZE = 10;

const deviationColumns: Column<Deviation>[] = [
  {
    key: 'severity',
    header: 'Severity',
    render: (d) => <StatusBadge status={d.severity} />,
  },
  {
    key: 'description',
    header: 'Description',
    render: (d) => <span className="max-w-xs truncate block">{d.description}</span>,
  },
  {
    key: 'detectedValue',
    header: 'Detected',
    render: (d) => <span className="font-mono text-xs">{d.detectedValue}</span>,
  },
  {
    key: 'expectedValue',
    header: 'Expected',
    render: (d) => <span className="font-mono text-xs">{d.expectedValue}</span>,
  },
];

export function PayloadDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: payload, isLoading, error, refetch } = usePayload(id!);
  const [devPage, setDevPage] = useState(0);
  const deviations = usePayloadDeviations(id!, { page: devPage, size: DEVIATIONS_PAGE_SIZE });

  if (isLoading) return <LoadingSpinner />;
  if (error || !payload) {
    return <ErrorMessage message="Failed to load payload" onRetry={() => refetch()} />;
  }

  const deviationList = deviations.data?.content ?? [];
  const deviationTotalPages = deviations.data?.totalPages ?? 0;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Payload Details</h1>
          <div className="mt-1">
            <StatusBadge status={payload.status} />
          </div>
        </div>
      </div>

      <section className="space-y-6">
        <div className="rounded-md border border-gray-200 bg-white p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Metadata</h2>
          <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <dt className="text-sm font-medium text-gray-500">Payload ID</dt>
              <dd className="mt-1 text-sm text-gray-900 font-mono">{payload.id}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Contract</dt>
              <dd className="mt-1 text-sm text-gray-900">
                <button
                  type="button"
                  className="text-blue-600 hover:underline font-mono text-xs"
                  onClick={() => navigate(buildRoute(ROUTES.CONTRACT_DETAIL, { id: payload.dataContractId }))}
                >
                  {payload.dataContractId}
                </button>
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Submitted</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {new Date(payload.createdAt).toLocaleString()}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Last Updated</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {new Date(payload.updatedAt).toLocaleString()}
              </dd>
            </div>
            {payload.s3Key && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Storage Key</dt>
                <dd className="mt-1 text-sm text-gray-900 font-mono text-xs">{payload.s3Key}</dd>
              </div>
            )}
          </dl>
        </div>

        <div className="rounded-md border border-gray-200 bg-white p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Deviations
            {deviations.data && (
              <span className="ml-2 text-sm font-normal text-gray-500">
                ({deviations.data.totalElements})
              </span>
            )}
          </h2>

          {deviations.isLoading ? (
            <LoadingSpinner />
          ) : deviations.error ? (
            <ErrorMessage message="Failed to load deviations" onRetry={() => deviations.refetch()} />
          ) : (
            <>
              <DataTable
                columns={deviationColumns}
                data={deviationList}
                keyExtractor={(d) => d.id}
                emptyMessage="No deviations detected for this payload."
              />
              <Pagination currentPage={devPage} totalPages={deviationTotalPages} onPageChange={setDevPage} />
            </>
          )}
        </div>
      </section>
    </div>
  );
}
