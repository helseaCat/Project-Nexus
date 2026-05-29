import apiClient from './client';
import type { DataContract, CreateContractRequest, UpdateContractRequest } from '@/types/contract';
import type { PageResponse, PaginationParams } from '@/types/api';

export const contractsApi = {
  list: (params: PaginationParams) =>
    apiClient.get<PageResponse<DataContract>>('/contracts', { params })
      .then((r) => r.data),

  getById: (id: string) =>
    apiClient.get<DataContract>(`/contracts/${id}`)
      .then((r) => r.data),

  create: (data: CreateContractRequest) =>
    apiClient.post<DataContract>('/contracts', data)
      .then((r) => r.data),

  update: (id: string, data: UpdateContractRequest) =>
    apiClient.put<DataContract>(`/contracts/${id}`, data)
      .then((r) => r.data),

  publish: (id: string) =>
    apiClient.post<DataContract>(`/contracts/${id}/publish`)
      .then((r) => r.data),

  listPublished: (params: PaginationParams) =>
    apiClient.get<PageResponse<DataContract>>('/contracts/published', { params })
      .then((r) => r.data),
};
