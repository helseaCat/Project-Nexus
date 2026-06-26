import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCreateTask } from '@/hooks/useTasks';
import { FormField } from '@/components/FormField';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { ApiError } from '@/types/api';
import type { LinkedEntityType } from '@/types/task';
import axios from 'axios';

const LINKED_ENTITY_OPTIONS: { value: '' | LinkedEntityType; label: string }[] = [
  { value: '', label: 'None' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'PAYLOAD', label: 'Payload' },
  { value: 'DEVIATION', label: 'Deviation' },
];

export function TaskFormPage() {
  const navigate = useNavigate();
  const createMutation = useCreateTask();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [assigneeId, setAssigneeId] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [linkedToType, setLinkedToType] = useState<'' | LinkedEntityType>('');
  const [linkedToId, setLinkedToId] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFieldErrors({});
    setFormError('');

    const base = {
      title: title.trim(),
      ...(description.trim() && { description: description.trim() }),
      ...(assigneeId.trim() && { assigneeId: assigneeId.trim() }),
      ...(dueDate && { dueDate }),
    };

    const payload: Parameters<typeof createMutation.mutateAsync>[0] =
      linkedToType && linkedToId.trim()
        ? { ...base, linkedToType, linkedToId: linkedToId.trim() }
        : base;

    try {
      const created = await createMutation.mutateAsync(payload);
      navigate(buildRoute(ROUTES.TASK_DETAIL, { id: created.id }));
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.data) {
        const apiError = err.response.data as ApiError;
        if (apiError.errors) {
          setFieldErrors(apiError.errors);
        } else {
          setFormError(apiError.message || 'An error occurred. Please try again.');
        }
      } else {
        setFormError('An unexpected error occurred. Please try again.');
      }
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Create Task</h1>

      {formError && (
        <p className="mb-4 text-sm text-red-600" role="alert">{formError}</p>
      )}

      <form onSubmit={handleSubmit} className="max-w-2xl space-y-1">
        <FormField
          label="Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          error={fieldErrors.title}
          required
        />

        <div className="mb-4">
          <label htmlFor="description" className="label">Description</label>
          <textarea
            id="description"
            className={`input min-h-[80px] ${fieldErrors.description ? 'input-error' : ''}`}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            aria-invalid={!!fieldErrors.description}
            aria-describedby={fieldErrors.description ? 'description-error' : undefined}
          />
          {fieldErrors.description && (
            <p id="description-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.description}
            </p>
          )}
        </div>

        <FormField
          label="Assignee"
          value={assigneeId}
          onChange={(e) => setAssigneeId(e.target.value)}
          error={fieldErrors.assigneeId}
          placeholder="User ID"
        />

        <div className="mb-4">
          <label htmlFor="dueDate" className="label">Due Date</label>
          <input
            id="dueDate"
            type="date"
            className={`input ${fieldErrors.dueDate ? 'input-error' : ''}`}
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            aria-invalid={!!fieldErrors.dueDate}
            aria-describedby={fieldErrors.dueDate ? 'dueDate-error' : undefined}
          />
          {fieldErrors.dueDate && (
            <p id="dueDate-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.dueDate}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label htmlFor="linkedToType" className="label">Linked Entity Type</label>
          <select
            id="linkedToType"
            className="input"
            value={linkedToType}
            onChange={(e) => setLinkedToType(e.target.value as '' | LinkedEntityType)}
          >
            {LINKED_ENTITY_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>

        {linkedToType && (
          <FormField
            label="Linked Entity ID"
            value={linkedToId}
            onChange={(e) => setLinkedToId(e.target.value)}
            error={fieldErrors.linkedToId}
            required
          />
        )}

        <div className="flex gap-3 pt-4">
          <button type="submit" className="btn-primary" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Creating…' : 'Create'}
          </button>
          <button
            type="button"
            className="btn-secondary"
            onClick={() => navigate(ROUTES.TASKS)}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
