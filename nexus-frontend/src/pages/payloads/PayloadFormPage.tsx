import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSubmitPayload } from '@/hooks/usePayloads';
import { usePublishedContracts } from '@/hooks/useContracts';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { ApiError } from '@/types/api';
import axios from 'axios';

export function PayloadFormPage() {
  const navigate = useNavigate();
  const contracts = usePublishedContracts({ page: 0, size: 100 });
  const submitMutation = useSubmitPayload();

  const [dataContractId, setDataContractId] = useState('');
  const [rawPayload, setRawPayload] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');

  if (contracts.isLoading) return <LoadingSpinner />;
  if (contracts.error) {
    return <ErrorMessage message="Failed to load published contracts" onRetry={() => contracts.refetch()} />;
  }

  const publishedContracts = contracts.data?.content ?? [];

  if (publishedContracts.length === 0) {
    return (
      <div>
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Submit Payload</h1>
        <ErrorMessage message="No published contracts available. Please publish a contract first." />
      </div>
    );
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFieldErrors({});
    setFormError('');

    // Validate all fields at once
    const errors: Record<string, string> = {};

    let parsed: unknown;
    try {
      parsed = JSON.parse(rawPayload);
    } catch {
      errors.rawPayload = 'Invalid JSON format.';
    }

    if (!dataContractId) {
      errors.dataContractId = 'Please select a contract.';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    try {
      const created = await submitMutation.mutateAsync({
        dataContractId,
        rawPayload: parsed,
      });
      navigate(buildRoute(ROUTES.PAYLOAD_DETAIL, { id: created.id }));
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.data) {
        const apiError = err.response.data as ApiError;
        if (apiError.details) {
          const mapped: Record<string, string> = {};
          for (const entry of apiError.details) {
            mapped[entry.field] = entry.message;
          }
          setFieldErrors(mapped);
        } else if (apiError.errors) {
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
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Submit Payload</h1>

      {formError && (
        <p className="mb-4 text-sm text-red-600" role="alert">{formError}</p>
      )}

      <form onSubmit={handleSubmit} className="max-w-2xl space-y-1">
        <div className="mb-4">
          <label htmlFor="dataContractId" className="label">Linked Contract</label>
          <select
            id="dataContractId"
            className={`input ${fieldErrors.dataContractId ? 'input-error' : ''}`}
            value={dataContractId}
            onChange={(e) => setDataContractId(e.target.value)}
            aria-invalid={!!fieldErrors.dataContractId}
            aria-describedby={fieldErrors.dataContractId ? 'dataContractId-error' : undefined}
            required
          >
            <option value="">Select a published contract…</option>
            {publishedContracts.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          {fieldErrors.dataContractId && (
            <p id="dataContractId-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.dataContractId}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label htmlFor="rawPayload" className="label">Payload (JSON)</label>
          <textarea
            id="rawPayload"
            className={`input min-h-[200px] font-mono text-sm ${fieldErrors.rawPayload ? 'input-error' : ''}`}
            value={rawPayload}
            onChange={(e) => setRawPayload(e.target.value)}
            aria-invalid={!!fieldErrors.rawPayload}
            aria-describedby={fieldErrors.rawPayload ? 'rawPayload-error' : undefined}
            placeholder='{ "key": "value" }'
            required
          />
          {fieldErrors.rawPayload && (
            <p id="rawPayload-error" className="mt-1 text-sm text-red-600" role="alert">
              {fieldErrors.rawPayload}
            </p>
          )}
        </div>

        <div className="flex gap-3 pt-4">
          <button type="submit" className="btn-primary" disabled={submitMutation.isPending}>
            {submitMutation.isPending ? 'Submitting…' : 'Submit'}
          </button>
          <button
            type="button"
            className="btn-secondary"
            onClick={() => navigate(ROUTES.PAYLOADS)}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
