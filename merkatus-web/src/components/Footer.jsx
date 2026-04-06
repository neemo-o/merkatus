import { useTheme } from '../context/ThemeContext'

export default function Footer() {
  const { theme } = useTheme()
  const logoSrc = theme === 'dark' ? '/logo_white.png' : '/logo.png'

  return (
    <footer className="border-t border-[var(--border)] py-10 px-6">
      <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <img
            src={logoSrc}
            alt="Merkatus"
            className="w-6 h-auto object-contain"
            onError={(e) => {
              e.target.style.display = 'none'
              e.target.nextElementSibling.style.display = 'flex'
            }}
          />
          <div className="w-6 h-6 rounded-md items-center justify-center hidden" style={{ background: 'var(--accent)' }}>
            <span className="text-bg font-bold text-[10px]">M</span>
          </div>
          <span className="text-sm font-heading font-semibold text-[var(--muted)]">Merkatus</span>
        </div>
        <p className="text-xs font-mono text-[var(--border)]">
          &copy; {new Date().getFullYear()} Merkatus. Todos os direitos reservados.
        </p>
      </div>
    </footer>
  )
}
