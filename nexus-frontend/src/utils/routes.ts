/**
 * Centralized route path constants for the application.
 * Use these instead of hardcoded strings in navigation and routing.
 */
export const ROUTES = {
  LOGIN: '/login',
  DASHBOARD: '/',

  CONTRACTS: '/contracts',
  CONTRACT_DETAIL: '/contracts/:id',
  CONTRACT_CREATE: '/contracts/new',
  CONTRACT_EDIT: '/contracts/:id/edit',

  EXPECTATIONS: '/expectations',
  EXPECTATION_DETAIL: '/expectations/:id',
  EXPECTATION_CREATE: '/expectations/new',
  EXPECTATION_EDIT: '/expectations/:id/edit',

  TASKS: '/tasks',
  TASK_DETAIL: '/tasks/:id',
  TASK_CREATE: '/tasks/new',

  PAYLOADS: '/payloads',
  PAYLOAD_DETAIL: '/payloads/:id',
  PAYLOAD_SUBMIT: '/payloads/submit',

  DEVIATIONS: '/deviations',
} as const;

/**
 * Helper to build parameterized routes.
 */
export function buildRoute(route: string, params: Record<string, string>): string {
  let result = route;
  for (const [key, value] of Object.entries(params)) {
    result = result.replace(`:${key}`, value);
  }
  return result;
}
