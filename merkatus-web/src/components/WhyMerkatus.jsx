const points = [
  { title: 'Nativo para desktop', desc: 'Sem depender do navegador. Sem lentidão. Performance real de um aplicativo nativo feito para operar o dia inteiro sem travar.' },
  { title: 'Feito para mercados', desc: 'Não é genérico. Cada fluxo foi pensado para a realidade de um supermercado: estoque perecível, tabela de preços, margens e fornecedores recorrentes.' },
]

export default function WhyMerkatus() {

  return (
    <section id="why" className="section-full border-t border-theme-border">
      <div className="section-content flex flex-col items-center justify-center">
        <div className="grid md:grid-cols-2 gap-12 lg:gap-16 items-center w-full">
          <div className="animate-fade-in-up">
            <span className="font-mono text-xs text-theme-accent tracking-wider">POR QUE MERKATUS?</span>
            <h2 className="mt-4 text-3xl md:text-4xl font-heading font-bold tracking-tight text-theme-text">
              Construído para quem opera de verdade
            </h2>
        
            <div className="mt-8 space-y-5">
              {points.map((p) => (
                <div key={p.title}>
                  <h3 className="font-semibold text-theme-text">{p.title}</h3>
                  <p className="mt-1 text-sm text-theme-muted leading-relaxed">{p.desc}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="animate-fade-in-up" data-delay="150">
            <div className="bg-theme-surface rounded-2xl border border-theme-border p-8">
              <div className="space-y-5">
                {[
                  'Zero dependência de browser — App nativo, sem Chrome consumindo RAM',
                  'Funciona offline com sync — Operação continua mesmo sem internet',
                  'Instalação em 2 minutos — Sem configuração complexa ou servidor extra',
                  'Suporte técnico dedicado — Time que entende de operação de mercado',
                ].map((text, i) => {
                  const [title, desc] = text.split(' — ')
                  return (
                    <div key={i} className="flex items-start gap-3">
                      <div
                        className="w-6 h-6 rounded flex items-center justify-center flex-shrink-0 mt-0.5"
                        style={{ background: 'color-mix(in srgb, var(--accent) 10%, transparent)' }}
                      >
                        <svg className="w-3.5 h-3.5 text-theme-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                        </svg>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-theme-text">{title}</p>
                        <p className="text-xs text-theme-muted mt-0.5">{desc}</p>
                      </div>
                    </div>
                  )
                })}
              </div>

              <div className="mt-7 pt-6 border-t border-theme-border flex items-center justify-between">
                <span className="text-xs font-mono text-theme-muted">Uptime médio</span>
                <span className="text-sm font-bold text-theme-accent font-mono">99.97%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
