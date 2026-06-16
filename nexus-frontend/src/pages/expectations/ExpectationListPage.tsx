import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useExpectations } from '@/hooks/useExpectations';
import { DataTable, type Column } from '@/components/DataTable';
import { Pagination } from '@/components/Pagination';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { AlignmentExpectation } from '@/types/expectation';

const PAGE_SIZE = 10;

const columns: Column<AlignmentExpectation>[] = [
  { key: 'name', header: 'Name', render: (e) => e.name },
  { key: 'severity', header: 'Severity', render: (e) => <StatusBadge status={e.severity} /> },
  {
    key: 'active',
    header: 'Status',
    render: (e) => (
      <span className={`text-sm font-medium ${e.active ? 'text-green-700' : 'text-gray-500'}`}>
        {e.active ? 'Active' : 'Inactive'}
      </span>
    ),
  },
  { key: 'contractId', header: 'Contract ID', render: (e) => e.dataContractId.slice(0, 8) + '…' },
];

export function ExpectationListPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const { data, isLoading, error, refetch } = useExpectations({ page, size: PAGE_SIZE });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message="Failed to load expectations" onRetry={() => refetch()} />;

  const expectations = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Expectations</h1>
        <button
          type="button"
          className="btn-primary"
          onClick={() => navigate(ROUTES.EXPECTATION_CREATE)}
        >
          Create Expectation
        </button>
      </div>

      <DataTable
        columns={columns}
        data={expectations}
        keyExtractor={(e) => e.id}
        onRowClick={(e) => navigate(buildRoute(ROUTES.EXPECTATION_DETAIL, { id: e.id }))}
        emptyMessage="No expectations yet. Create your first expectation to get started."
      />

      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
