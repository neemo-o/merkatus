import { useState, useEffect, useRef } from 'react'

export default function Hero() {
  const [typedText, setTypedText] = useState('')
  const [phase, setPhase] = useState(0)
  const phrasesRef = useRef(['estoque', 'vendas', 'financeiro', 'fornecedores'])

  useEffect(() => {
    const word = phrasesRef.current[phase]
    let index = 0
    let deleting = false
    const baseDelay = 120

    const interval = setInterval(() => {
      if (!deleting) {
        setTypedText(word.slice(0, index + 1))
        index++
        if (index === word.length) {
          setTimeout(() => { deleting = true }, 1500)
        }
      } else {
        setTypedText(word.slice(0, index - 1))
        index--
        if (index === 0) {
          deleting = false
          setPhase((p) => (p + 1) % phrasesRef.current.length)
        }
      }
    }, baseDelay)

    return () => clearInterval(interval)
  }, [phase])

  return (
    <section id="hero" className="section-full scroll-snap-start">
      <div className="section-content text-center">
        <div className="max-w-3xl mx-auto">
          

          <h1 className="animate-fade-in-up text-4xl md:text-6xl lg:text-7xl font-heading font-bold tracking-tight leading-[1.08] text-theme-text" data-delay="100">
            Gerencie todo o seu mercado de forma&nbsp;simples
          </h1>

          <p className="animate-fade-in-up mt-5 md:mt-6 text-lg md:text-xl text-theme-muted max-w-lg mx-auto leading-relaxed" data-delay="200">
            Controle <span className="text-theme-accent font-mono inline min-w-[100px] text-left">{typedText}<span className="animate-pulse">|</span></span> com a eficiência que seu negócio merece.
          </p>

          <div className="animate-fade-in-up mt-8 flex justify-center" data-delay="300">
            <button onClick={() => { document.getElementById('download')?.scrollIntoView({ behavior: 'smooth' }) }} className="group btn-primary inline-flex items-center justify-center">
              Baixar agora
              <svg className="inline w-4 h-4 ml-1 group-hover:translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            </button>
          </div>
        </div>

        <div className="animate-fade-in-up mt-12 md:mt-16 max-w-4xl mx-auto" data-delay="400">
          <div className="rounded-2xl overflow-hidden shadow-2xl shadow-black/40" style={{ border: '1px solid var(--border)' }}>
            <img src="/apresentation.jpg" alt="Sistema Merkatus" className="w-full h-auto" width="1280" height="720" loading="eager" />
          </div>
        </div>
      </div>
    </section>
  )
}
