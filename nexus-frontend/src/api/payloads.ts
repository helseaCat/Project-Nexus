import apiClient from './client';
import type { Payload, SubmitPayloadRequest } from '@/types/payload';
import type { Deviation } from '@/types/deviation';
import type { PageResponse, PaginationParams } from '@/types/api';

export const payloadsApi = {
  list: (contractId: string, params: PaginationParams) =>
    apiClient.get<PageResponse<Payload>>(`/payloads/by-contract/${contractId}`, { params })
      .then((r) => r.data),

  getById: (id: string) =>
    apiClient.get<Payload>(`/payloads/${id}`)
      .then((r) => r.data),

  submit: (data: SubmitPayloadRequest) =>
    apiClient.post<Payload>('/payloads', data)
      .then((r) => r.data),

  getDeviations: (payloadId: string, params: PaginationParams) =>
    apiClient.get<PageResponse<Deviation>>(`/payloads/${payloadId}/deviations`, { params })
      .then((r) => r.data),

  listDeviationsByContract: (contractId: string, params: PaginationParams) =>
    apiClient.get<PageResponse<Deviation>>(`/payloads/deviations/by-contract/${contractId}`, { params })
      .then((r) => r.data),
};
