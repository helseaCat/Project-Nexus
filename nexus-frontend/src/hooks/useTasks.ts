import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tasksApi } from '@/api/tasks';
import type { CreateTaskRequest, TaskStatus } from '@/types/task';
import type { PaginationParams } from '@/types/api';

interface TaskListParams extends PaginationParams {
  status?: TaskStatus;
}

export function useTasks(params: TaskListParams) {
  return useQuery({
    queryKey: ['tasks', params],
    queryFn: () => tasksApi.list(params),
    staleTime: 15_000,
  });
}

export function useTask(id: string) {
  return useQuery({
    queryKey: ['tasks', id],
    queryFn: () => tasksApi.getById(id),
    staleTime: 15_000,
  });
}

export function useCreateTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTaskRequest) => tasksApi.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}

export function useTransitionTaskStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: TaskStatus }) =>
      tasksApi.transitionStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}
