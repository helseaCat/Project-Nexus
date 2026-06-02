import { useAuth } from '@/auth/useAuth';

export function Header() {
  const { user, logout } = useAuth();

  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-6">
      <h1 className="text-lg font-semibold text-gray-900 md:hidden">Nexus</h1>
      <div className="flex items-center gap-4 ml-auto">
        {user && (
          <span className="text-sm text-gray-600">
            {user.tenantId.slice(0, 8)}…
          </span>
        )}
        <button type="button" onClick={logout} className="btn-secondary text-sm">
          Logout
        </button>
      </div>
    </header>
  );
}
