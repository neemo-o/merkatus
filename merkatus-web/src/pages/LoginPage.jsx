import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTheme } from '../context/ThemeContext'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const [user, setUser] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { theme } = useTheme()
  const { login } = useAuth()
  const navigate = useNavigate()
  const logoSrc = theme === 'dark' ? '/logo_white.png' : '/logo.png'

  useEffect(() => {
    document.body.style.backgroundColor = 'var(--bg)'
    return () => { document.body.style.backgroundColor = '' }
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      console.log('Tentando login...', { user })
      const result = await login(user, password)
      console.log('Resultado do login:', result)

      if (result.success) {
        // Login bem-sucedido - redireciona para dashboard
        console.log('Login OK, redirecionando para dashboard...')
        window.location.href = '/dashboard'
      } else {
        // Erro de autenticação
        setError(result.error || 'Credenciais inválidas')
      }
    } catch (err) {
      console.error('Erro no login:', err)
      setError(err.message || 'Erro ao conectar com o servidor. Tente novamente.')
    } finally {
      setLoading(false)
    }
  }

  const inputCls = "w-full px-4 py-3 rounded-xl text-sm bg-theme-surface text-theme-text border border-theme-border transition-all focus:outline-none focus:ring-2 focus:border-theme-accent"
  const placeholderCls = "placeholder-theme-muted"

  return (
    <div className="min-h-screen flex items-center justify-center px-6 page-transition">
      <div className="w-full max-w-sm">
        <div className="text-center mb-10">
          <div className="flex justify-center mb-6">
            <div
              className="w-16 h-16 rounded-2xl flex items-center justify-center"
              style={{
                background: 'color-mix(in srgb, var(--accent) 10%, transparent)',
                border: '1px solid color-mix(in srgb, var(--accent) 20%, transparent)',
              }}
            >
              <img
                src={logoSrc}
                alt="Merkatus"
                className="w-10 h-10 object-contain"
                onError={(e) => {
                  e.target.style.display = 'none'
                  e.target.parentNode.querySelector('.fallback-logo').style.display = 'flex'
                }}
              />
              <div className="fallback-logo w-7 h-7 text-theme-accent font-heading font-extrabold text-lg hidden" style={{ display: 'none' }}>M</div>
            </div>
          </div>
          <h1 className="text-2xl font-heading font-bold text-theme-text">Merkatus</h1>
          <p className="mt-2 text-sm text-theme-muted font-mono">Acesso ao sistema</p>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="user" className="block text-xs font-mono text-theme-muted mb-2">
              Email
            </label>
            <input
              id="user"
              type="email"
              value={user}
              onChange={(e) => setUser(e.target.value)}
              placeholder="exemplo@email.com"
              className={`${inputCls} ${placeholderCls}`}
              autoComplete="username"
              required
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-xs font-mono text-theme-muted mb-2">
              Senha
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className={`${inputCls} ${placeholderCls}`}
              autoComplete="current-password"
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full btn-primary"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24" aria-hidden="true">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Entrando...
              </span>
            ) : (
              'Entrar'
            )}
          </button>
        </form>

        <div className="mt-8 text-center">
          <Link
            to="/"
            className="text-sm font-mono text-theme-muted hover:text-theme-accent transition-colors"
          >
            &larr; Voltar para o início
          </Link>
        </div>
      </div>
    </div>
  )
}
