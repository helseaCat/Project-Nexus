import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAllPayloads } from '@/hooks/usePayloads';
import { DataTable, type Column } from '@/components/DataTable';
import { Pagination } from '@/components/Pagination';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { Payload } from '@/types/payload';

const PAGE_SIZE = 10;

const columns: Column<Payload>[] = [
  {
    key: 'dataContractId',
    header: 'Contract ID',
    render: (p) => (
      <span className="font-mono text-xs">{p.dataContractId.slice(0, 8)}…</span>
    ),
  },
  { key: 'status', header: 'Status', render: (p) => <StatusBadge status={p.status} /> },
  {
    key: 'createdAt',
    header: 'Submitted',
    render: (p) => new Date(p.createdAt).toLocaleDateString(),
  },
];

export function PayloadListPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);

  const { data, isLoading, error, refetch } = useAllPayloads({ page, size: PAGE_SIZE });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message="Failed to load payloads" onRetry={() => refetch()} />;

  const payloads = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Payloads</h1>
        <button
          type="button"
          className="btn-primary"
          onClick={() => navigate(ROUTES.PAYLOAD_SUBMIT)}
        >
          Submit Payload
        </button>
      </div>

      <DataTable
        columns={columns}
        data={payloads}
        keyExtractor={(p) => p.id}
        onRowClick={(p) => navigate(buildRoute(ROUTES.PAYLOAD_DETAIL, { id: p.id }))}
        emptyMessage="No payloads found. Submit your first payload to get started."
      />

      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
