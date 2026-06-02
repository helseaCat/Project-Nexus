interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorMessage({ message, onRetry }: ErrorMessageProps) {
  return (
    <div className="rounded-md border border-red-200 bg-red-50 p-4" role="alert">
      <p className="text-sm text-red-700">{message}</p>
      {onRetry && (
        <button onClick={onRetry} className="btn-secondary mt-3 text-sm">
          Try again
        </button>
      )}
    </div>
  );
}
