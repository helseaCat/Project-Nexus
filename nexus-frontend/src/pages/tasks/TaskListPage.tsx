import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTasks } from '@/hooks/useTasks';
import { DataTable, type Column } from '@/components/DataTable';
import { Pagination } from '@/components/Pagination';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { Task, TaskStatus } from '@/types/task';

const PAGE_SIZE = 10;

const STATUS_OPTIONS: { value: TaskStatus | ''; label: string }[] = [
  { value: '', label: 'All Statuses' },
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'IN_REVIEW', label: 'In Review' },
  { value: 'DONE', label: 'Done' },
];

const columns: Column<Task>[] = [
  { key: 'title', header: 'Title', render: (t) => t.title },
  { key: 'status', header: 'Status', render: (t) => <StatusBadge status={t.status} /> },
  {
    key: 'assignee',
    header: 'Assignee',
    render: (t) => t.assigneeId ?? '—',
  },
  {
    key: 'dueDate',
    header: 'Due Date',
    render: (t) => {
      if (!t.dueDate) return '—';
      const date = new Date(t.dueDate);
      return Number.isNaN(date.getTime()) ? '—' : date.toLocaleDateString();
    },
  },
  {
    key: 'linkedToType',
    header: 'Linked To',
    render: (t) => t.linkedToType ?? '—',
  },
];

export function TaskListPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<TaskStatus | ''>('');

  const { data, isLoading, error, refetch } = useTasks({
    page,
    size: PAGE_SIZE,
    ...(statusFilter ? { status: statusFilter } : {}),
  });

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message="Failed to load tasks" onRetry={() => refetch()} />;

  const tasks = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Tasks</h1>
        <button
          type="button"
          className="btn-primary"
          onClick={() => navigate(ROUTES.TASK_CREATE)}
        >
          Create Task
        </button>
      </div>

      <div className="mb-4">
        <label htmlFor="status-filter" className="sr-only">
          Filter by status
        </label>
        <select
          id="status-filter"
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value as TaskStatus | '');
            setPage(0);
          }}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      <DataTable
        columns={columns}
        data={tasks}
        keyExtractor={(t) => t.id}
        onRowClick={(t) => navigate(buildRoute(ROUTES.TASK_DETAIL, { id: t.id }))}
        emptyMessage="No tasks found. Create your first task to get started."
      />

      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
