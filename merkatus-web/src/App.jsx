import { Routes, Route, useLocation } from 'react-router-dom'
import { useEffect } from 'react'
import { ThemeProvider } from './context/ThemeContext'
import useScrollAnimation from './hooks/useScrollAnimation'
import Topbar from './components/Topbar'
import Hero from './components/Hero'
import WhyMerkatus from './components/WhyMerkatus'
import Features from './components/Features'
import Integrations from './components/Integrations'
import Download from './components/Download'
import Footer from './components/Footer'
import LoginPage from './pages/LoginPage'

function ScrollToTop() {
  const location = useLocation()
  useEffect(() => { window.scrollTo(0, 0) }, [location])
  return null
}

function Landing() {
  useScrollAnimation('.animate-fade-in-up')
  return (
    <>
      <Topbar />
      <Hero />
      <WhyMerkatus />
      <Features />
      <Integrations />
      <Download />
      <Footer />
    </>
  )
}

export default function App() {
  return (
    <ThemeProvider>
      <div className="bg-[var(--bg)] min-h-screen transition-colors duration-300" style={{ color: 'var(--text)' }}>
        <ScrollToTop />
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </div>
    </ThemeProvider>
  )
}
