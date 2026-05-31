interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <nav className="flex items-center justify-between border-t border-gray-200 pt-4" aria-label="Pagination">
      <p className="text-sm text-gray-700">
        Page {currentPage + 1} of {totalPages}
      </p>
      <div className="flex gap-2">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="btn-secondary text-sm"
          aria-label="Previous page"
        >
          Previous
        </button>
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="btn-secondary text-sm"
          aria-label="Next page"
        >
          Next
        </button>
      </div>
    </nav>
  );
}
