import type { TaskStatus } from '../types/task';

/**
 * Defines the valid status transitions for tasks.
 * State machine: TODO → IN_PROGRESS → IN_REVIEW → DONE
 */
const TRANSITION_MAP: Readonly<Record<TaskStatus, readonly TaskStatus[]>> = {
  TODO: ['IN_PROGRESS'],
  IN_PROGRESS: ['IN_REVIEW'],
  IN_REVIEW: ['DONE'],
  DONE: [],
};

/**
 * Returns the list of valid next statuses for a given current status.
 * Returns an empty array if no transitions are available (terminal state).
 */
export function getValidTransitions(currentStatus: TaskStatus): TaskStatus[] {
  return [...(TRANSITION_MAP[currentStatus] ?? [])];
}

/**
 * Checks whether transitioning from one status to another is valid.
 */
export function isValidTransition(
  from: TaskStatus,
  to: TaskStatus
): boolean {
  return TRANSITION_MAP[from]?.includes(to) ?? false;
}

/**
 * Human-readable labels for task statuses.
 */
export const STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  IN_REVIEW: 'In Review',
  DONE: 'Done',
};
