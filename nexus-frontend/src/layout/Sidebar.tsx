import { NavLink } from 'react-router-dom';
import { ROUTES } from '@/utils/routes';

const NAV_ITEMS = [
  { to: ROUTES.DASHBOARD, label: 'Dashboard' },
  { to: ROUTES.CONTRACTS, label: 'Contracts' },
  { to: ROUTES.EXPECTATIONS, label: 'Expectations' },
  { to: ROUTES.TASKS, label: 'Tasks' },
  { to: ROUTES.PAYLOADS, label: 'Payloads' },
  { to: ROUTES.DEVIATIONS, label: 'Deviations' },
];

export function Sidebar() {
  return (
    <nav className="hidden md:flex md:w-64 md:flex-col md:border-r md:border-gray-200 md:bg-white" aria-label="Main navigation">
      <div className="flex h-16 items-center px-6">
        <span className="text-lg font-semibold text-nexus-700">Nexus</span>
      </div>
      <ul className="flex-1 space-y-1 px-3">
        {NAV_ITEMS.map(({ to, label }) => (
          <li key={to}>
            <NavLink
              to={to}
              end={to === ROUTES.DASHBOARD}
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-nexus-50 text-nexus-700'
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                }`
              }
            >
              {label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
