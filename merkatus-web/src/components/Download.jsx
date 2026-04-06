import { useState } from 'react'

const VERSION = '0.1'

function formatCNPJ(value) {
  const digits = value.replace(/\D/g, '').slice(0, 14)
  if (!digits) return ''
  let formatted = digits
  if (digits.length > 2) formatted = digits.slice(0, 2) + '.' + digits.slice(2)
  if (digits.length > 5) formatted = formatted.replace(/\.(\d{3})(\d)/, '.$1.$2')
  if (digits.length > 8) formatted = formatted.replace(/\.(\d{3})\.(\d{3})/, '.$1.$2/')
  if (digits.length > 12) formatted = formatted.replace(/\/(\d{4})(\d)/, '/$1-$2')
  return formatted
}

export default function Download() {
  const [cnpj, setCNPJ] = useState('')
  const [status, setStatus] = useState('idle')
  const [error, setError] = useState('')

  const handleCNPJChange = (e) => {
    const formatted = formatCNPJ(e.target.value)
    setCNPJ(formatted)
    setError('')
    setStatus('idle')
  }

  const validate = () => {
    if (!cnpj.trim()) {
      setError('Insira o CNPJ da licença.')
      return
    }
    if (cnpj.replace(/\D/g, '').length < 14) {
      setError('CNPJ incompleto.')
      return
    }
    setError('')
    setStatus('validating')
    const timer = setTimeout(() => {
      setStatus('ready')
    }, 1200)
    return () => clearTimeout(timer)
  }

  return (
    <section id="download" className="section-full scroll-snap-start border-t border-theme-border">
      <div className="section-content text-center">
        <div className="max-w-xl mx-auto">
          <span className="font-mono text-xs text-theme-accent tracking-wider">DOWNLOAD</span>
          
          <h2 className="mt-4 text-3xl md:text-4xl font-heading font-bold tracking-tight text-theme-text">
            Baixe o Merkatus
          </h2>
          <p className="mt-3 text-theme-muted max-w-md mx-auto">
            Insira o CNPJ registrado para baixar a versão mais recente.
          </p>

          <div className="mt-8 bg-theme-surface rounded-2xl border border-theme-border p-8">
            <div className="space-y-4">
              <div className="text-left">
                <label htmlFor="license" className="block text-sm font-mono text-theme-muted mb-2">
                  <span className="text-theme-accent">$</span> Licença CNPJ
                </label>
                <input
                  id="license"
                  type="text"
                  value={cnpj}
                  onChange={handleCNPJChange}
                  placeholder="00.000.000/0000-00"
                  className="w-full px-4 py-3 rounded-xl text-sm font-mono bg-theme-bg text-theme-text placeholder-theme-muted border transition-all focus:outline-none focus:ring-2 focus:border-theme-accent"
                  style={{
                    borderColor: error ? 'rgba(239,68,68,0.5)' : status === 'ready' ? 'rgba(34,197,94,0.5)' : 'var(--border)',
                    background: 'color-mix(in srgb, var(--border) 30%, var(--bg))',
                  }}
                />
                {error && (
                  <p className="mt-2 text-xs" style={{ color: '#ef4444' }}>{error}</p>
                )}
                {status === 'ready' && (
                  <p className="mt-2 text-xs flex items-center gap-1" style={{ color: '#22c55e' }}>
                    <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    Licença válida
                  </p>
                )}
              </div>

              <button
                onClick={validate}
                disabled={status === 'validating'}
                className="w-full btn-primary"
              >
                {status === 'validating' ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24" aria-hidden="true">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                    </svg>
                    Verificando...
                  </span>
                ) : status === 'ready' ? (
                  <span className="flex items-center justify-center gap-2">
                    Baixar Merkatus
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                    </svg>
                  </span>
                ) : (
                  'Validar e baixar'
                )}
              </button>
            </div>

            <p className="mt-4 text-xs font-mono text-theme-muted">
              v{VERSION} · Windows · x64
            </p>
          </div>
        </div>
      </div>
    </section>
  )
}
