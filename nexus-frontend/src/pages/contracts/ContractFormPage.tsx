import { useEffect, useRef, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useContract, useCreateContract, useUpdateContract } from '@/hooks/useContracts';
import { FormField } from '@/components/FormField';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { ApiError } from '@/types/api';
import type { CreateContractRequest } from '@/types/contract';
import axios from 'axios';

function serializeJson(value: unknown): string {
  if (value === null || value === undefined) return '';
  return JSON.stringify(value, null, 2);
}

export function ContractFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const { data: existing, isLoading, error: loadError } = useContract(id ?? '', { enabled: isEdit });
  const createMutation = useCreateContract();
  const updateMutation = useUpdateContract();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [businessGoals, setBusinessGoals] = useState('');
  const [sharingRules, setSharingRules] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');
  const lastHydratedIdRef = useRef<string | null>(null);

  // Hydrate form when editing and data arrives, or reset for create mode
  useEffect(() => {
    if (isEdit && existing) {
      if (lastHydratedIdRef.current === existing.id) return;
      setName(existing.name);
      setDescription(existing.description);
      setBusinessGoals(serializeJson(existing.businessGoals));
      setSharingRules(serializeJson(existing.sharingRules));
      lastHydratedIdRef.current = existing.id;
    } else if (!isEdit) {
      lastHydratedIdRef.current = null;
      setName('');
      setDescription('');
      setBusinessGoals('');
      setSharingRules('');
    }
    setFieldErrors({});
    setFormError('');
  }, [isEdit, existing]);

  if (isEdit && isLoading) return <LoadingSpinner />;
  if (isEdit && loadError) {
    return <ErrorMessage message="Failed to load contract" onRetry={() => {}} />;
  }

  const isPending = createMutation.isPending || updateMutation.isPending;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFieldErrors({});
    setFormError('');

    // Validate JSON fields before submitting
    let parsedGoals: unknown;
    let parsedRules: unknown;

    if (businessGoals.trim()) {
      try {
        parsedGoals = JSON.parse(businessGoals);
      } catch {
        setFieldErrors((prev) => ({ ...prev, businessGoals: 'Invalid JSON' }));
        return;
      }
    }

    if (sharingRules.trim()) {
      try {
        parsedRules = JSON.parse(sharingRules);
      } catch {
        setFieldErrors((prev) => ({ ...prev, sharingRules: 'Invalid JSON' }));
        return;
      }
    }

    const payload: CreateContractRequest = {
      name: name.trim(),
      description: description.trim(),
      businessGoals: parsedGoals,
      sharingRules: parsedRules,
    };

    try {
      if (isEdit) {
        await updateMutation.mutateAsync({ id: id!, data: payload });
        navigate(buildRoute(ROUTES.CONTRACT_DETAIL, { id: id! }));
      } else {
        const created = await createMutation.mutateAsync(payload);
        navigate(buildRoute(ROUTES.CONTRACT_DETAIL, { id: created.id }));
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
        {isEdit ? 'Edit Contract' : 'Create Contract'}
      </h1>

      {formError && (
        <p className="mb-4 text-sm text-red-600" role="alert">{formError}</p>
      )}

      <form onSubmit={handleSubmit} className="max-w-2xl space-y-1">
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
          <label htmlFor="businessGoals" className="label">Business Goals (JSON)</label>
          <textarea
            id="businessGoals"
            className={`input min-h-[80px] font-mono text-sm ${fieldErrors.businessGoals ? 'input-error' : ''}`}
            value={businessGoals}
            onChange={(e) => setBusinessGoals(e.target.value)}
            aria-invalid={!!fieldErrors.businessGoals}
            aria-describedby={fieldErrors.businessGoals ? 'businessGoals-error' : undefined}
            placeholder='e.g. ["Improve data quality"]'
          />
          {fieldErrors.businessGoals && (
            <p id="businessGoals-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.businessGoals}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label htmlFor="sharingRules" className="label">Sharing Rules (JSON)</label>
          <textarea
            id="sharingRules"
            className={`input min-h-[80px] font-mono text-sm ${fieldErrors.sharingRules ? 'input-error' : ''}`}
            value={sharingRules}
            onChange={(e) => setSharingRules(e.target.value)}
            aria-invalid={!!fieldErrors.sharingRules}
            aria-describedby={fieldErrors.sharingRules ? 'sharingRules-error' : undefined}
            placeholder='e.g. {"visibility": "INTERNAL"}'
          />
          {fieldErrors.sharingRules && (
            <p id="sharingRules-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.sharingRules}
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
            onClick={() => navigate(ROUTES.CONTRACTS)}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
