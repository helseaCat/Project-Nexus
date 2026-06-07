import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/auth/AuthProvider';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { AppShell } from '@/layout/AppShell';
import { DashboardPage } from '@/pages/DashboardPage';
import { ContractListPage } from '@/pages/contracts/ContractListPage';
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
                <Route path={ROUTES.CONTRACT_DETAIL} element={<Placeholder name="Contract Detail" />} />
                <Route path={ROUTES.CONTRACT_CREATE} element={<Placeholder name="Create Contract" />} />
                <Route path={ROUTES.EXPECTATIONS} element={<Placeholder name="Expectations" />} />
                <Route path={ROUTES.EXPECTATION_DETAIL} element={<Placeholder name="Expectation Detail" />} />
                <Route path={ROUTES.EXPECTATION_CREATE} element={<Placeholder name="Create Expectation" />} />
                <Route path={ROUTES.TASKS} element={<Placeholder name="Tasks" />} />
                <Route path={ROUTES.TASK_DETAIL} element={<Placeholder name="Task Detail" />} />
                <Route path={ROUTES.TASK_CREATE} element={<Placeholder name="Create Task" />} />
                <Route path={ROUTES.PAYLOADS} element={<Placeholder name="Payloads" />} />
                <Route path={ROUTES.PAYLOAD_DETAIL} element={<Placeholder name="Payload Detail" />} />
                <Route path={ROUTES.PAYLOAD_SUBMIT} element={<Placeholder name="Submit Payload" />} />
                <Route path={ROUTES.DEVIATIONS} element={<Placeholder name="Deviations" />} />
                <Route path={ROUTES.CONTRACT_EDIT} element={<Placeholder name="Edit Contract" />} />
                <Route path={ROUTES.EXPECTATION_EDIT} element={<Placeholder name="Edit Expectation" />} />
                <Route path="*" element={<Placeholder name="Page Not Found" />} />
              </Route>
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
