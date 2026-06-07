import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useContracts } from '@/hooks/useContracts';
import { DataTable, type Column } from '@/components/DataTable';
import { Pagination } from '@/components/Pagination';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { DataContract } from '@/types/contract';

const PAGE_SIZE = 10;

const columns: Column<DataContract>[] = [
  { key: 'name', header: 'Name', render: (c) => c.name },
  { key: 'status', header: 'Status', render: (c) => <StatusBadge status={c.status} /> },
  { key: 'version', header: 'Version', render: (c) => `v${c.version}` },
  {
    key: 'createdAt',
    header: 'Created',
    render: (c) => new Date(c.createdAt).toLocaleDateString(),
  },
];

export function ContractListPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const { data, isLoading, error, refetch } = useContracts({ page, size: PAGE_SIZE });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message="Failed to load contracts" onRetry={() => refetch()} />;

  const contracts = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Contracts</h1>
        <button
          type="button"
          className="btn-primary"
          onClick={() => navigate(ROUTES.CONTRACT_CREATE)}
        >
          Create Contract
        </button>
      </div>

      <DataTable
        columns={columns}
        data={contracts}
        keyExtractor={(c) => c.id}
        onRowClick={(c) => navigate(buildRoute(ROUTES.CONTRACT_DETAIL, { id: c.id }))}
        emptyMessage="No contracts yet. Create your first contract to get started."
      />

      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
