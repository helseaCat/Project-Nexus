import type { InputHTMLAttributes } from 'react';

interface FormFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export function FormField({ label, error, id, className = '', ...props }: FormFieldProps) {
  const fieldId = id ?? label.toLowerCase().replace(/\s+/g, '-');
  return (
    <div className="mb-4">
      <label htmlFor={fieldId} className="label">{label}</label>
      <input
        id={fieldId}
        className={`input ${error ? 'input-error' : ''} ${className}`}
        aria-invalid={!!error}
        aria-describedby={error ? `${fieldId}-error` : undefined}
        {...props}
      />
      {error && (
        <p id={`${fieldId}-error`} className="mt-1 text-sm text-red-600" role="alert">
          {error}
        </p>
      )}
    </div>
  );
}
