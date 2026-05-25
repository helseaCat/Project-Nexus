/** Spring Data Page response structure. */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page (0-indexed)
}

/** Pagination parameters for list queries. */
export interface PaginationParams {
  page: number;
  size: number;
  sort?: string;
}

/** Standard API error response from the backend. */
export interface ApiError {
  status: number;
  message: string;
  errors?: Record<string, string>; // field -> error message
}
