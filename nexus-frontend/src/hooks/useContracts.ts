import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { contractsApi } from '@/api/contracts';
import type { CreateContractRequest, UpdateContractRequest } from '@/types/contract';
import type { PaginationParams } from '@/types/api';

export function useContracts(params: PaginationParams) {
  return useQuery({
    queryKey: ['contracts', params],
    queryFn: () => contractsApi.list(params),
    staleTime: 30_000,
  });
}

export function usePublishedContracts(params: PaginationParams) {
  return useQuery({
    queryKey: ['contracts', 'published', params],
    queryFn: () => contractsApi.listPublished(params),
    staleTime: 30_000,
  });
}

export function useContract(id: string) {
  return useQuery({
    queryKey: ['contracts', id],
    queryFn: () => contractsApi.getById(id),
    staleTime: 30_000,
  });
}

export function useCreateContract() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateContractRequest) => contractsApi.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['contracts'] }),
  });
}

export function useUpdateContract() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateContractRequest }) =>
      contractsApi.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['contracts'] }),
  });
}

export function usePublishContract() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => contractsApi.publish(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['contracts'] }),
  });
}
