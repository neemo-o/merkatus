const features = [
  {
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
      </svg>
    ),
    title: 'Controle de estoque',
    desc: 'Entradas, saídas, perdas e níveis mínimos em tempo real com alertas automáticos.'
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" />
      </svg>
    ),
    title: 'Gestão de vendas',
    desc: 'PDV rápido, suporte a múltiplas formas de pagamento e integração com leitor de código.'
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
    ),
    title: 'Relatórios em tempo real',
    desc: 'Dashboards visuais com métricas de faturamento, margens e performance.'
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13 10V3L4 14h7v7l9-11h-7z" />
      </svg>
    ),
    title: 'Alertas automáticos',
    desc: 'Notificações de estoque baixo, produtos vencendo e metas ultrapassadas.'
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
    ),
    title: 'Fornecedores',
    desc: 'Cadastro, cotações automáticas e histórico de compras por fornecedor.'
  },
  {
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
      </svg>
    ),
    title: 'Controle de usuários',
    desc: 'Permissões por perfil, log de ações e controle de acesso por turno.'
  },
]

export default function Features() {
  return (
    <section id="features" className="section-full border-t border-theme-border">
      <div className="section-content flex flex-col items-center justify-center">
        <div className="text-center max-w-xl mx-auto mb-10">
          <span className="font-mono text-xs text-theme-accent tracking-wider">FUNCIONALIDADES</span>
          <h2 className="mt-4 text-3xl md:text-4xl font-heading font-bold tracking-tight text-theme-text">
            Tudo que seu mercado precisa
          </h2>
          <p className="mt-3 text-theme-muted">
            Ferramentas integradas para cada área da sua operação.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4 w-full">
          {features.map((f) => (
            <div
              key={f.title}
              className="card-glow bg-theme-surface rounded-xl p-6 border border-theme-border group"
            >
              <div
                className="w-10 h-10 rounded-lg flex items-center justify-center text-theme-muted transition-colors"
                style={{ background: 'color-mix(in srgb, var(--accent) 5%, transparent)' }}
              >
                {f.icon}
              </div>
              <h3 className="mt-4 font-semibold text-theme-text">{f.title}</h3>
              <p className="mt-1.5 text-sm text-theme-muted leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
