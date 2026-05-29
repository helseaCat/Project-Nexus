import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { payloadsApi } from '@/api/payloads';
import type { SubmitPayloadRequest } from '@/types/payload';
import type { PaginationParams } from '@/types/api';

export function usePayloads(contractId: string, params: PaginationParams) {
  return useQuery({
    queryKey: ['payloads', contractId, params],
    queryFn: () => payloadsApi.list(contractId, params),
    staleTime: 15_000,
  });
}

export function usePayload(id: string) {
  return useQuery({
    queryKey: ['payloads', id],
    queryFn: () => payloadsApi.getById(id),
    staleTime: 15_000,
  });
}

export function useSubmitPayload() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: SubmitPayloadRequest) => payloadsApi.submit(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['payloads'] }),
  });
}

export function usePayloadDeviations(payloadId: string, params: PaginationParams) {
  return useQuery({
    queryKey: ['deviations', 'payload', payloadId, params],
    queryFn: () => payloadsApi.getDeviations(payloadId, params),
    staleTime: 60_000,
  });
}

export function useDeviationsByContract(contractId: string, params: PaginationParams) {
  return useQuery({
    queryKey: ['deviations', 'contract', contractId, params],
    queryFn: () => payloadsApi.listDeviationsByContract(contractId, params),
    staleTime: 60_000,
  });
}
