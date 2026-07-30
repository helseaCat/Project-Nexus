import { useEffect, useRef } from 'react';
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
  const panelRef = useRef<HTMLDivElement>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    previousFocusRef.current = document.activeElement as HTMLElement | null;

    // Focus the panel on open
    panelRef.current?.focus();

    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') {
        onClose();
        return;
      }

      // Trap focus within the panel
      if (e.key === 'Tab' && panelRef.current) {
        const focusable = panelRef.current.querySelectorAll<HTMLElement>(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        if (focusable.length === 0) return;
        const first = focusable[0]!;
        const last = focusable[focusable.length - 1]!;

        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      previousFocusRef.current?.focus();
    };
  }, [onClose]);

  const { expectationId } = deviation;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onClick={onClose}
    >
      <div
        ref={panelRef}
        className="relative w-full max-w-lg max-h-[90vh] overflow-y-auto rounded-lg bg-white p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Deviation details"
        tabIndex={-1}
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
          {expectationId && (
            <button
              type="button"
              className="btn-secondary text-sm"
              onClick={() => navigate(buildRoute(ROUTES.EXPECTATION_DETAIL, { id: expectationId }))}
            >
              View Expectation
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
