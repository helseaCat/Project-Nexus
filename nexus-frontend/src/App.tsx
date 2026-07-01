import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/auth/AuthProvider';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { AppShell } from '@/layout/AppShell';
import { DashboardPage } from '@/pages/DashboardPage';
import { ContractListPage } from '@/pages/contracts/ContractListPage';
import { ContractDetailPage } from '@/pages/contracts/ContractDetailPage';
import { ContractFormPage } from '@/pages/contracts/ContractFormPage';
import { ExpectationListPage } from '@/pages/expectations/ExpectationListPage';
import { ExpectationDetailPage } from '@/pages/expectations/ExpectationDetailPage';
import { ExpectationFormPage } from '@/pages/expectations/ExpectationFormPage';
import { TaskListPage } from '@/pages/tasks/TaskListPage';
import { TaskDetailPage } from '@/pages/tasks/TaskDetailPage';
import { TaskFormPage } from '@/pages/tasks/TaskFormPage';
import { PayloadListPage } from '@/pages/payloads/PayloadListPage';
import { ROUTES } from '@/utils/routes';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 2, refetchOnWindowFocus: false },
  },
});

function Placeholder({ name }: { name: string }) {
  return <h2 className="text-xl font-semibold text-gray-700">{name}</h2>;
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path={ROUTES.LOGIN} element={<Placeholder name="Login" />} />
            <Route element={<ProtectedRoute />}>
              <Route element={<AppShell />}>
                <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
                <Route path={ROUTES.CONTRACTS} element={<ContractListPage />} />
                <Route path={ROUTES.CONTRACT_DETAIL} element={<ContractDetailPage />} />
                <Route path={ROUTES.CONTRACT_CREATE} element={<ContractFormPage />} />
                <Route path={ROUTES.EXPECTATIONS} element={<ExpectationListPage />} />
                <Route path={ROUTES.EXPECTATION_DETAIL} element={<ExpectationDetailPage />} />
                <Route path={ROUTES.EXPECTATION_CREATE} element={<ExpectationFormPage />} />
                <Route path={ROUTES.TASKS} element={<TaskListPage />} />
                <Route path={ROUTES.TASK_DETAIL} element={<TaskDetailPage />} />
                <Route path={ROUTES.TASK_CREATE} element={<TaskFormPage />} />
                <Route path={ROUTES.PAYLOADS} element={<PayloadListPage />} />
                <Route path={ROUTES.PAYLOAD_DETAIL} element={<Placeholder name="Payload Detail" />} />
                <Route path={ROUTES.PAYLOAD_SUBMIT} element={<Placeholder name="Submit Payload" />} />
                <Route path={ROUTES.DEVIATIONS} element={<Placeholder name="Deviations" />} />
                <Route path={ROUTES.CONTRACT_EDIT} element={<ContractFormPage />} />
                <Route path={ROUTES.EXPECTATION_EDIT} element={<ExpectationFormPage />} />
                <Route path="*" element={<Placeholder name="Page Not Found" />} />
              </Route>
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
