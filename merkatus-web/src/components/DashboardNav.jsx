import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import { LayoutDashboard, Users, KeyRound, ClipboardList } from "lucide-react";

export default function DashboardNav() {
  const { user, logout } = useAuth();
  const { theme, toggle } = useTheme();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const navItems = [
    {
      label: "Dashboard",
      path: "/dashboard",
      icon: LayoutDashboard,
      Component: LayoutDashboard,
    },
    {
      label: "Clientes",
      path: "/dashboard/clientes",
      icon: Users,
      Component: Users,
    },
    {
      label: "Licenças",
      path: "/dashboard/licencas",
      icon: KeyRound,
      Component: KeyRound,
    },
    {
      label: "Auditoria",
      path: "/dashboard/auditoria",
      icon: ClipboardList,
      Component: ClipboardList,
    },
  ];

  const isActive = (path) => location.pathname === path;

  const handleLogout = async () => {
    await logout();
  };

  return (
    <>
      {/* Mobile menu button */}
      <div className="md:hidden fixed bottom-6 right-6 z-40 flex gap-2">
        <button
          onClick={toggle}
          className="w-12 h-12 rounded-full flex items-center justify-center bg-[var(--accent)] text-white hover:shadow-lg transition-shadow"
          aria-label="Alternar tema"
        >
          {theme === "dark" ? "☀️" : "🌙"}
        </button>
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="w-12 h-12 rounded-full flex items-center justify-center bg-[var(--surface)] border border-[var(--border)] hover:border-[var(--accent)] transition-colors"
          aria-label="Menu"
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            {sidebarOpen ? (
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            ) : (
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 6h16M4 12h16M4 18h16"
              />
            )}
          </svg>
        </button>
      </div>

      {/* Sidebar */}
      <aside
        className={`fixed left-0 top-0 z-30 w-64 h-screen bg-[var(--surface)] border-r border-[var(--border)] transform transition-transform duration-300 md:translate-x-0 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {/* Header */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-[var(--border)]">
          <div className="flex items-center gap-3">
            <img
              src={theme === "dark" ? "/logo_white.png" : "/logo.png"}
              alt="Merkatus"
              className="w-8 h-8 object-contain"
              onError={(e) => {
                e.target.style.display = "none";
                e.target.nextElementSibling.style.display = "flex";
              }}
            />
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-white hidden"
              style={{ background: "var(--accent)" }}
            >
              M
            </div>
            <span className="font-heading font-bold text-lg">Merkatus</span>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="md:hidden p-1 text-[var(--muted)] hover:text-[var(--text)]"
          >
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-2">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              onClick={() => setSidebarOpen(false)}
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                isActive(item.path)
                  ? "bg-[var(--accent)]/10 text-[var(--accent)] border border-[var(--accent)]/20"
                  : "text-[var(--muted)] hover:text-[var(--text)] hover:bg-[var(--bg)]"
              }`}
            >
              <item.Component className="w-5 h-5 flex-shrink-0" />
              <span>{item.label}</span>
            </Link>
          ))}
        </nav>

        {/* User section */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-[var(--border)] bg-[var(--bg)]">
          <div className="space-y-3">
            {/* User info */}
            <div className="px-2">
              <p className="text-xs font-mono text-[var(--muted)]">USUÁRIO</p>
              <p className="text-sm font-medium text-[var(--text)] truncate">
                {user?.nome}
              </p>
              <p className="text-xs text-[var(--muted)] truncate">
                {user?.email}
              </p>
            </div>

            {/* Logout button */}
            <button
              onClick={handleLogout}
              className="w-full px-4 py-2 text-sm rounded-lg border border-[var(--border)] hover:border-red-500 hover:text-red-500 text-[var(--muted)] transition-colors"
            >
              Sair
            </button>
          </div>
        </div>
      </aside>

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="md:hidden fixed inset-0 bg-black/50 z-20"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Desktop top bar with theme toggle */}
      <div className="hidden md:flex fixed top-0 left-64 right-0 h-16 bg-[var(--surface)] border-b border-[var(--border)] items-center justify-end px-6 gap-4 z-20">
        <button
          onClick={toggle}
          className="p-2 text-[var(--muted)] hover:text-[var(--text)] transition-colors"
          aria-label="Alternar tema"
        >
          {theme === "dark" ? (
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
              />
            </svg>
          ) : (
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
              />
            </svg>
          )}
        </button>

        <div className="w-px h-6 bg-[var(--border)]" />

        {/* User menu */}
        <div className="flex items-center gap-2">
          <div className="text-right">
            <p className="text-sm font-medium text-[var(--text)]">
              {user?.nome}
            </p>
            <p className="text-xs text-[var(--muted)]">{user?.perfil}</p>
          </div>
          <button
            onClick={handleLogout}
            className="p-2 text-[var(--muted)] hover:text-red-500 transition-colors"
            title="Sair"
          >
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
          </button>
        </div>
      </div>
    </>
  );
}
