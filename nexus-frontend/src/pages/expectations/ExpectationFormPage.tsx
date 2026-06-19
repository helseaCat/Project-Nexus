import { useEffect, useRef, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useExpectation, useCreateExpectation, useUpdateExpectation } from '@/hooks/useExpectations';
import { usePublishedContracts } from '@/hooks/useContracts';
import { FormField } from '@/components/FormField';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { ApiError } from '@/types/api';
import type { CreateExpectationRequest, Severity } from '@/types/expectation';
import axios from 'axios';

export function ExpectationFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const {
    data: existing,
    isLoading,
    error: loadError,
    refetch: refetchExpectation,
  } = useExpectation(id ?? '', { enabled: isEdit });
  const contracts = usePublishedContracts({ page: 0, size: 100 });
  const createMutation = useCreateExpectation();
  const updateMutation = useUpdateExpectation();

  const [dataContractId, setDataContractId] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [severity, setSeverity] = useState<Severity>('WARNING');
  const [ruleExpression, setRuleExpression] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');
  const lastHydratedIdRef = useRef<string | null>(null);

  useEffect(() => {
    if (isEdit && existing) {
      if (lastHydratedIdRef.current === existing.id) return;
      setDataContractId(existing.dataContractId);
      setName(existing.name);
      setDescription(existing.description);
      setSeverity(existing.severity);
      setRuleExpression(existing.ruleExpression);
      lastHydratedIdRef.current = existing.id;
    } else if (!isEdit) {
      lastHydratedIdRef.current = null;
      setDataContractId('');
      setName('');
      setDescription('');
      setSeverity('WARNING');
      setRuleExpression('');
    }
    setFieldErrors({});
    setFormError('');
  }, [isEdit, existing]);

  if (isEdit && isLoading) return <LoadingSpinner />;
  if (isEdit && loadError) {
    return <ErrorMessage message="Failed to load expectation" onRetry={() => refetchExpectation()} />;
  }
  if (!isEdit && contracts.isLoading) return <LoadingSpinner />;
  if (!isEdit && contracts.error) {
    return <ErrorMessage message="Failed to load published contracts" onRetry={() => contracts.refetch()} />;
  }

  const isPending = createMutation.isPending || updateMutation.isPending;
  const publishedContracts = contracts.data?.content ?? [];

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFieldErrors({});
    setFormError('');

    const payload: CreateExpectationRequest = {
      dataContractId,
      name: name.trim(),
      description: description.trim(),
      severity,
      ruleExpression: ruleExpression.trim(),
    };

    try {
      if (isEdit) {
        const { dataContractId: _, ...updateData } = payload;
        await updateMutation.mutateAsync({ id: id!, data: updateData });
        navigate(buildRoute(ROUTES.EXPECTATION_DETAIL, { id: id! }));
      } else {
        const created = await createMutation.mutateAsync(payload);
        navigate(buildRoute(ROUTES.EXPECTATION_DETAIL, { id: created.id }));
      }
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
      <h1 className="text-2xl font-bold text-gray-900 mb-6">
        {isEdit ? 'Edit Expectation' : 'Create Expectation'}
      </h1>

      {formError && (
        <p className="mb-4 text-sm text-red-600" role="alert">{formError}</p>
      )}

      <form onSubmit={handleSubmit} className="max-w-2xl space-y-1">
        {!isEdit && (
          <div className="mb-4">
            <label htmlFor="dataContractId" className="label">Linked Contract</label>
            <select
              id="dataContractId"
              className={`input ${fieldErrors.dataContractId ? 'input-error' : ''}`}
              value={dataContractId}
              onChange={(e) => setDataContractId(e.target.value)}
              aria-invalid={!!fieldErrors.dataContractId}
              required
            >
              <option value="">Select a published contract…</option>
              {publishedContracts.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            {fieldErrors.dataContractId && (
              <p className="mt-1 text-sm text-red-600" role="alert">{fieldErrors.dataContractId}</p>
            )}
          </div>
        )}

        <FormField
          label="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          error={fieldErrors.name}
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
            required
          />
          {fieldErrors.description && (
            <p id="description-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.description}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label htmlFor="severity" className="label">Severity</label>
          <select
            id="severity"
            className="input"
            value={severity}
            onChange={(e) => setSeverity(e.target.value as Severity)}
          >
            <option value="WARNING">Warning</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>

        <div className="mb-4">
          <label htmlFor="ruleExpression" className="label">Rule Expression</label>
          <textarea
            id="ruleExpression"
            className={`input min-h-[100px] font-mono text-sm ${fieldErrors.ruleExpression ? 'input-error' : ''}`}
            value={ruleExpression}
            onChange={(e) => setRuleExpression(e.target.value)}
            aria-invalid={!!fieldErrors.ruleExpression}
            aria-describedby={fieldErrors.ruleExpression ? 'ruleExpression-error' : undefined}
            placeholder='e.g. value >= 0 && value <= 100'
            required
          />
          {fieldErrors.ruleExpression && (
            <p id="ruleExpression-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.ruleExpression}
            </p>
          )}
        </div>

        <div className="flex gap-3 pt-4">
          <button type="submit" className="btn-primary" disabled={isPending}>
            {isPending ? 'Saving…' : isEdit ? 'Update' : 'Create'}
          </button>
          <button
            type="button"
            className="btn-secondary"
            onClick={() => navigate(ROUTES.EXPECTATIONS)}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
