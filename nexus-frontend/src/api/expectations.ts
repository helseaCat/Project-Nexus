import apiClient from './client';
import type { AlignmentExpectation, CreateExpectationRequest, UpdateExpectationRequest } from '@/types/expectation';
import type { PageResponse, PaginationParams } from '@/types/api';

export const expectationsApi = {
  list: (params: PaginationParams) =>
    apiClient.get<PageResponse<AlignmentExpectation>>('/expectations', { params })
      .then((r) => r.data),

  listByContract: (contractId: string, params: PaginationParams) =>
    apiClient.get<PageResponse<AlignmentExpectation>>(`/expectations/by-contract/${contractId}`, { params })
      .then((r) => r.data),

  getById: (id: string) =>
    apiClient.get<AlignmentExpectation>(`/expectations/${id}`)
      .then((r) => r.data),

  create: (data: CreateExpectationRequest) =>
    apiClient.post<AlignmentExpectation>('/expectations', data)
      .then((r) => r.data),

  update: (id: string, data: UpdateExpectationRequest) =>
    apiClient.put<AlignmentExpectation>(`/expectations/${id}`, data)
      .then((r) => r.data),

  activate: (id: string) =>
    apiClient.post<AlignmentExpectation>(`/expectations/${id}/activate`)
      .then((r) => r.data),

  deactivate: (id: string) =>
    apiClient.post<AlignmentExpectation>(`/expectations/${id}/deactivate`)
      .then((r) => r.data),
};
