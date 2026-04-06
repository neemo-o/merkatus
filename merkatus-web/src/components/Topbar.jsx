import { useEffect, useState, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTheme } from '../context/ThemeContext'

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

const navLinks = [
  { label: 'Início', section: 'hero' },
  { label: 'Por que Merkatus?', section: 'why' },
  { label: 'Funcionalidades', section: 'features' },
  { label: 'Integrações', section: 'integrations' },
  { label: 'Download', section: 'download' },
]

export default function Topbar() {
  const [scrolled, setScrolled] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)
  const navigate = useNavigate()
  const { theme, toggle } = useTheme()
  const menuRef = useRef(null)

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 40)
    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  useEffect(() => {
    if (!menuOpen) return
    const handleClick = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [menuOpen])

  const handleNav = (section) => () => {
    navigate('/')
    setMenuOpen(false)
    requestAnimationFrame(() => {
      setTimeout(() => scrollTo(section), 50)
    })
  }

  const handleLogo = (e) => {
    e.preventDefault()
    navigate('/')
    setMenuOpen(false)
    requestAnimationFrame(() => {
      setTimeout(() => scrollTo('hero'), 50)
    })
  }

  const logoSrc = theme === 'dark' ? '/logo_white.png' : '/logo.png'

  return (
    <header
      id="topbar"
      className={`fixed top-0 left-0 right-0 z-50 border-b border-transparent transition-all duration-300 ${
        scrolled ? 'topbar-scrolled' : 'bg-[var(--bg)]'
      }`}
    >
      <div className="max-w-6xl mx-auto px-4 flex items-center justify-between h-16">
        <button onClick={handleLogo} className="flex items-center group bg-transparent border-0 p-0 m-0 cursor-pointer">
          <img
            src={logoSrc}
            alt="Merkatus"
            className="w-14 h-auto object-contain group-hover:opacity-80 transition-opacity"
            onError={(e) => {
              e.target.style.display = 'none'
              e.target.nextElementSibling.style.display = 'flex'
            }}
          />
          <div className="w-10 h-10 rounded-lg items-center justify-center hidden" style={{ background: 'var(--accent)' }}>
            <span className="text-bg font-bold text-base">M</span>
          </div>
        </button>

        <nav className="hidden md:flex items-center gap-8">
          {navLinks.map((link) => (
            <button
              key={link.section}
              onClick={handleNav(link.section)}
              className="text-sm transition-colors bg-transparent border-0 cursor-pointer"
              style={{ color: 'var(--muted)' }}
              onMouseEnter={(e) => (e.target.style.color = 'var(--accent)')}
              onMouseLeave={(e) => (e.target.style.color = 'var(--muted)')}
            >
              {link.label}
            </button>
          ))}
        </nav>

        <div className="hidden md:flex items-center gap-3">
          <button
            onClick={toggle}
            className="p-2 bg-transparent border-0 cursor-pointer transition-colors"
            style={{ color: 'var(--muted)' }}
            onMouseEnter={(e) => (e.currentTarget.style.color = 'var(--accent)')}
            onMouseLeave={(e) => (e.currentTarget.style.color = 'var(--muted)')}
            aria-label="Alternar tema"
          >
            {theme === 'dark' ? (
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            ) : (
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
              </svg>
            )}
          </button>
          <Link
            to="/login"
            className="text-sm px-4 py-2 transition-colors"
            style={{ color: 'var(--muted)' }}
            onMouseEnter={(e) => (e.currentTarget.style.color = 'var(--accent)')}
            onMouseLeave={(e) => (e.currentTarget.style.color = 'var(--muted)')}
          >
            Login
          </Link>
        </div>

        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="md:hidden p-2 transition-colors"
          style={{ color: 'var(--muted)' }}
          onMouseEnter={(e) => (e.currentTarget.style.color = 'var(--text)')}
          onMouseLeave={(e) => (e.currentTarget.style.color = 'var(--muted)')}
          aria-label="Menu"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            {menuOpen
              ? <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              : <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            }
          </svg>
        </button>
      </div>

      {menuOpen && (
        <div ref={menuRef} className="md:hidden bg-[var(--surface)] border-t border-[var(--border)] px-6 py-5 space-y-4 menu-slide">
          {navLinks.map((link) => (
            <button
              key={link.section}
              onClick={handleNav(link.section)}
              className="block text-left text-sm bg-transparent border-0 cursor-pointer w-full p-0 transition-colors"
              style={{ color: 'var(--muted)' }}
              onMouseEnter={(e) => (e.currentTarget.style.color = 'var(--accent)')}
              onMouseLeave={(e) => (e.currentTarget.style.color = 'var(--muted)')}
            >
              {link.label}
            </button>
          ))}
          <div className="flex items-center justify-between pt-3 border-t border-[var(--border)]">
            <Link to="/login" onClick={() => setMenuOpen(false)} className="text-sm font-medium text-theme-accent">
              Login
            </Link>
            <button
              onClick={toggle}
              className="text-sm transition-colors bg-transparent border-0 cursor-pointer"
              style={{ color: 'var(--muted)' }}
              onMouseEnter={(e) => (e.target.style.color = 'var(--accent)')}
              onMouseLeave={(e) => (e.target.style.color = 'var(--muted)')}
            >
              {theme === 'dark' ? '☀ Light' : 'Dark'}
            </button>
          </div>
        </div>
      )}
    </header>
  )
}
