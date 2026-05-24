export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';
export type LinkedEntityType = 'CONTRACT' | 'PAYLOAD' | 'DEVIATION';

export interface Task {
  id: string;
  tenantId: string;
  title: string;
  description: string;
  status: TaskStatus;
  assigneeId: string | null;
  dueDate: string | null;
  linkedToType: LinkedEntityType | null;
  linkedToId: string | null;
  aiGenerated: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  assigneeId?: string;
  dueDate?: string;
  linkedToType?: LinkedEntityType;
  linkedToId?: string;
}
