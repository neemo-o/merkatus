const integrations = [
  { name: 'Nota Fiscal\nNFe', icon: 'doc', color: '#60a5fa' },
  { name: 'Boleto\nBancário', icon: 'bank', color: '#4ade80' },
  { name: 'Impressora\nFiscal', icon: 'print', color: '#fbbf24' },
  { name: 'Leitor de\nCódigo', icon: 'link', color: null },
  { name: 'Balança\nDigital', icon: 'link', color: null },
  { name: 'Catálogo de\nProdutos', icon: 'link', color: null },
  { name: 'Gateway\nPagamento', icon: 'link', color: null },
  { name: 'Backup\nNuvem', icon: 'link', color: null },
]

function integrationIcon(icon) {
  switch (icon) {
    case 'doc':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      )
    case 'bank':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
      )
    case 'print':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
        </svg>
      )
    default:
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
        </svg>
      )
  }
}

export default function Integrations() {
  return (
    <section id="integrations" className="section-full border-t border-theme-border">
      <div className="section-content flex flex-col items-center justify-center">
        <div className="text-center max-w-xl mx-auto mb-10">
          <span className="font-mono text-xs text-theme-accent tracking-wider">INTEGRAÇÕES</span>
          <h2 className="mt-4 text-3xl md:text-4xl font-heading font-bold tracking-tight text-theme-text">
            Conecta com tudo que importa
          </h2>
          <p className="mt-3 text-theme-muted">
            Integrações nativas que você já usa no mercado. Zero configuração extra.
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3 w-full">
          {integrations.map((item) => (
            <div
              key={item.name}
              className="card-glow bg-theme-surface rounded-xl p-5 border border-theme-border flex flex-col items-center justify-center text-center gap-3 min-h-[120px]"
            >
              <div
                className="w-10 h-10 rounded-lg flex items-center justify-center"
                style={{
                  background: item.color
                    ? `color-mix(in srgb, ${item.color} 10%, transparent)`
                    : 'color-mix(in srgb, var(--accent) 5%, transparent)',
                  color: item.color || 'var(--accent)',
                  opacity: item.color ? 1 : 0.5,
                }}
              >
                {integrationIcon(item.icon)}
              </div>
              <span className="text-xs text-theme-muted font-medium whitespace-pre-line leading-relaxed">{item.name}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
