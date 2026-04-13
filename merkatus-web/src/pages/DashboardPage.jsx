import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function DashboardPage() {
  const { user, isAuthenticated, logout, loading, initialized } = useAuth()
  const navigate = useNavigate()

  // Redireciona para login se não estiver autenticado
  useEffect(() => {
    if (initialized && !isAuthenticated && !loading) {
      navigate('/login')
    }
  }, [initialized, isAuthenticated, loading, navigate])

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  // Mostra loading enquanto inicializa
  if (!initialized || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--bg)]">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-[var(--accent)]"></div>
      </div>
    )
  }

  // Não renderiza nada se não estiver autenticado (vai redirecionar)
  if (!isAuthenticated) {
    return null
  }

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      {/* Header */}
      <header className="border-b border-[var(--border)]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-white"
                   style={{ background: 'var(--accent)' }}>
                M
              </div>
              <span className="font-heading font-bold text-xl">Merkatus</span>
            </div>

            <div className="flex items-center gap-4">
              <span className="text-sm text-[var(--muted)]">
                {user?.nome}
              </span>
              <button
                onClick={handleLogout}
                className="px-4 py-2 text-sm rounded-lg border border-[var(--border)] hover:border-[var(--accent)] transition-colors"
              >
                Sair
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-heading font-bold mb-6">
          Dashboard
        </h1>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Card - Perfil */}
          <div className="p-6 rounded-2xl border border-[var(--border)] bg-[var(--surface)]">
            <h2 className="text-sm font-mono text-[var(--muted)] mb-4">PERFIL</h2>
            <div className="space-y-2">
              <p className="text-lg font-medium">{user?.nome}</p>
              <p className="text-sm text-[var(--muted)]">{user?.email}</p>
              <span className="inline-block mt-2 px-2 py-1 text-xs rounded bg-[var(--accent)]/10 text-[var(--accent)]">
                {user?.perfil}
              </span>
            </div>
          </div>

          {/* Card - Status */}
          <div className="p-6 rounded-2xl border border-[var(--border)] bg-[var(--surface)]">
            <h2 className="text-sm font-mono text-[var(--muted)] mb-4">STATUS</h2>
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-green-500"></div>
                <span className="text-sm">Sessão ativa</span>
              </div>
              <p className="text-xs text-[var(--muted)]">
                Último login: {user?.ultimo_login ? new Date(user.ultimo_login).toLocaleString('pt-BR') : 'N/A'}
              </p>
            </div>
          </div>

          {/* Card - Ações */}
          <div className="p-6 rounded-2xl border border-[var(--border)] bg-[var(--surface)]">
            <h2 className="text-sm font-mono text-[var(--muted)] mb-4">AÇÕES RÁPIDAS</h2>
            <div className="space-y-3">
              <button className="w-full px-4 py-2 text-sm rounded-lg border border-[var(--border)] hover:border-[var(--accent)] transition-colors text-left">
                Gerenciar Licenças
              </button>
              <button className="w-full px-4 py-2 text-sm rounded-lg border border-[var(--border)] hover:border-[var(--accent)] transition-colors text-left">
                Ver Clientes
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
