import { useNavigate } from 'react-router-dom';
import { StatusBadge } from '@/components/StatusBadge';
import { ROUTES, buildRoute } from '@/utils/routes';
import type { Deviation } from '@/types/deviation';

interface DeviationDetailPanelProps {
  deviation: Deviation;
  onClose: () => void;
}

export function DeviationDetailPanel({ deviation, onClose }: DeviationDetailPanelProps) {
  const navigate = useNavigate();

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="Deviation details"
    >
      <div
        className="relative w-full max-w-lg rounded-lg bg-white p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          className="absolute right-4 top-4 text-gray-400 hover:text-gray-600"
          onClick={onClose}
          aria-label="Close panel"
        >
          ✕
        </button>

        <h2 className="text-lg font-semibold text-gray-900 mb-4">Deviation Details</h2>

        <div className="mb-4">
          <StatusBadge status={deviation.severity} />
        </div>

        <dl className="space-y-3">
          <div>
            <dt className="text-sm font-medium text-gray-500">Description</dt>
            <dd className="mt-1 text-sm text-gray-900">{deviation.description}</dd>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <dt className="text-sm font-medium text-gray-500">Detected Value</dt>
              <dd className="mt-1 text-sm text-gray-900 font-mono">{deviation.detectedValue}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Expected Value</dt>
              <dd className="mt-1 text-sm text-gray-900 font-mono">{deviation.expectedValue}</dd>
            </div>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Detected At</dt>
            <dd className="mt-1 text-sm text-gray-900">
              {new Date(deviation.createdAt).toLocaleString()}
            </dd>
          </div>
        </dl>

        <h3 className="text-sm font-semibold text-gray-900 mt-6 mb-3">Linked Entities</h3>

        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            className="btn-secondary text-sm"
            onClick={() => navigate(buildRoute(ROUTES.PAYLOAD_DETAIL, { id: deviation.payloadId }))}
          >
            View Payload
          </button>
          <button
            type="button"
            className="btn-secondary text-sm"
            onClick={() => navigate(buildRoute(ROUTES.CONTRACT_DETAIL, { id: deviation.dataContractId }))}
          >
            View Contract
          </button>
          {deviation.expectationId && (
            <button
              type="button"
              className="btn-secondary text-sm"
              onClick={() => navigate(buildRoute(ROUTES.EXPECTATION_DETAIL, { id: deviation.expectationId! }))}
            >
              View Expectation
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
