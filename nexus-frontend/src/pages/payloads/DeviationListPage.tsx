import { useState, useEffect } from 'react';
import { useDeviations } from '@/hooks/usePayloads';
import { DataTable, type Column } from '@/components/DataTable';
import { Pagination } from '@/components/Pagination';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { DeviationDetailPanel } from './DeviationDetailPanel';
import type { Deviation } from '@/types/deviation';
import type { Severity } from '@/types/expectation';

const PAGE_SIZE = 10;

const SEVERITY_OPTIONS: { value: '' | Severity; label: string }[] = [
  { value: '', label: 'All Severities' },
  { value: 'WARNING', label: 'Warning' },
  { value: 'CRITICAL', label: 'Critical' },
];

const columns: Column<Deviation>[] = [
  {
    key: 'severity',
    header: 'Severity',
    render: (d) => <StatusBadge status={d.severity} />,
  },
  {
    key: 'description',
    header: 'Description',
    render: (d) => (
      <span className="max-w-xs truncate block">{d.description}</span>
    ),
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
  {
    key: 'createdAt',
    header: 'Date',
    render: (d) => new Date(d.createdAt).toLocaleDateString(),
  },
];

export function DeviationListPage() {
  const [page, setPage] = useState(0);
  const [severity, setSeverity] = useState<'' | Severity>('');
  const [contractId, setContractId] = useState('');
  const [debouncedContractId, setDebouncedContractId] = useState('');
  const [selectedDeviation, setSelectedDeviation] = useState<Deviation | null>(null);

  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedContractId(contractId.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(t);
  }, [contractId]);

  const { data, isLoading, error, refetch } = useDeviations({
    page,
    size: PAGE_SIZE,
    ...(severity ? { severity } : {}),
    ...(debouncedContractId ? { contractId: debouncedContractId } : {}),
  });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message="Failed to load deviations" onRetry={() => refetch()} />;

  const deviations = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Deviations</h1>

      <div className="mb-4 flex flex-wrap gap-3">
        <div>
          <label htmlFor="severity-filter" className="sr-only">
            Filter by severity
          </label>
          <select
            id="severity-filter"
            value={severity}
            onChange={(e) => {
              setSeverity(e.target.value as '' | Severity);
              setPage(0);
            }}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            {SEVERITY_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="contract-filter" className="sr-only">
            Filter by contract ID
          </label>
          <input
            id="contract-filter"
            type="text"
            placeholder="Contract ID…"
            value={contractId}
            onChange={(e) => setContractId(e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          />
        </div>
      </div>

      <DataTable
        columns={columns}
        data={deviations}
        keyExtractor={(d) => d.id}
        onRowClick={(d) => setSelectedDeviation(d)}
        emptyMessage="No deviations found."
      />

      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />

      {selectedDeviation && (
        <DeviationDetailPanel
          deviation={selectedDeviation}
          onClose={() => setSelectedDeviation(null)}
        />
      )}
    </div>
  );
}
