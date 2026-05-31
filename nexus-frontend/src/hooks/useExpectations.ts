import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { expectationsApi } from '@/api/expectations';
import type { CreateExpectationRequest, UpdateExpectationRequest } from '@/types/expectation';
import type { PaginationParams } from '@/types/api';

export function useExpectations(params: PaginationParams) {
  return useQuery({
    queryKey: ['expectations', params],
    queryFn: () => expectationsApi.list(params),
    staleTime: 30_000,
  });
}

export function useExpectation(id: string) {
  return useQuery({
    queryKey: ['expectations', id],
    queryFn: () => expectationsApi.getById(id),
    staleTime: 30_000,
  });
}

export function useCreateExpectation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateExpectationRequest) => expectationsApi.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expectations'] }),
  });
}

export function useUpdateExpectation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateExpectationRequest }) =>
      expectationsApi.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expectations'] }),
  });
}

export function useActivateExpectation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => expectationsApi.activate(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expectations'] }),
  });
}

export function useDeactivateExpectation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => expectationsApi.deactivate(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expectations'] }),
  });
}
