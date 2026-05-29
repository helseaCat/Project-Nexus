import apiClient from './client';
import type { Task, CreateTaskRequest, TaskStatus } from '@/types/task';
import type { PageResponse, PaginationParams } from '@/types/api';

interface TaskListParams extends PaginationParams {
  status?: TaskStatus;
}

export const tasksApi = {
  list: (params: TaskListParams) =>
    apiClient.get<PageResponse<Task>>('/tasks', { params })
      .then((r) => r.data),

  getById: (id: string) =>
    apiClient.get<Task>(`/tasks/${id}`)
      .then((r) => r.data),

  create: (data: CreateTaskRequest) =>
    apiClient.post<Task>('/tasks', data)
      .then((r) => r.data),

  transitionStatus: (id: string, status: TaskStatus) =>
    apiClient.patch<Task>(`/tasks/${id}/status`, { status })
      .then((r) => r.data),
};
