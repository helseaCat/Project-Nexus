import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
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

    // Lock scroll on the AppShell main container and body
    const mainEl = document.querySelector('main');
    const originalMainOverflow = mainEl?.style.overflow ?? '';
    const originalBodyOverflow = document.body.style.overflow;
    if (mainEl) mainEl.style.overflow = 'hidden';
    document.body.style.overflow = 'hidden';

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
        if (focusable.length === 0) {
          e.preventDefault();
          return;
        }
        const first = focusable[0]!;
        const last = focusable[focusable.length - 1]!;
        const active = document.activeElement;

        // Handle Tab when focus is on the container itself (tabIndex=-1)
        const isOnContainer = active === panelRef.current;

        if (e.shiftKey && (active === first || isOnContainer)) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && (active === last || isOnContainer)) {
          e.preventDefault();
          first.focus();
        }
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      if (mainEl) mainEl.style.overflow = originalMainOverflow;
      document.body.style.overflow = originalBodyOverflow;
      previousFocusRef.current?.focus();
    };
  }, [onClose]);

  const { expectationId } = deviation;

  function handleNavigate(path: string) {
    onClose();
    navigate(path);
  }

  return createPortal(
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
        aria-labelledby="deviation-panel-title"
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

        <h2 id="deviation-panel-title" className="text-lg font-semibold text-gray-900 mb-4">
          Deviation Details
        </h2>

        <div className="mb-4">
          <StatusBadge status={deviation.severity} />
        </div>

        <dl className="space-y-3">
          <div>
            <dt className="text-sm font-medium text-gray-500">Description</dt>
            <dd className="mt-1 text-sm text-gray-900 break-words">{deviation.description}</dd>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <dt className="text-sm font-medium text-gray-500">Detected Value</dt>
              <dd className="mt-1 text-sm text-gray-900 font-mono break-all overflow-x-auto">
                {deviation.detectedValue}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Expected Value</dt>
              <dd className="mt-1 text-sm text-gray-900 font-mono break-all overflow-x-auto">
                {deviation.expectedValue}
              </dd>
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
            onClick={() => handleNavigate(buildRoute(ROUTES.PAYLOAD_DETAIL, { id: deviation.payloadId }))}
          >
            View Payload
          </button>
          <button
            type="button"
            className="btn-secondary text-sm"
            onClick={() => handleNavigate(buildRoute(ROUTES.CONTRACT_DETAIL, { id: deviation.dataContractId }))}
          >
            View Contract
          </button>
          {expectationId && (
            <button
              type="button"
              className="btn-secondary text-sm"
              onClick={() => handleNavigate(buildRoute(ROUTES.EXPECTATION_DETAIL, { id: expectationId }))}
            >
              View Expectation
            </button>
          )}
        </div>
      </div>
    </div>,
    document.body
  );
}
