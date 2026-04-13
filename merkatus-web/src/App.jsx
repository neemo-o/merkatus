import { Routes, Route, useLocation, Navigate } from "react-router-dom";
import { useEffect } from "react";
import { ThemeProvider } from "./context/ThemeContext";
import { AuthProvider, useAuth } from "./context/AuthContext";
import useScrollAnimation from "./hooks/useScrollAnimation";
import Topbar from "./components/Topbar";
import Hero from "./components/Hero";
import WhyMerkatus from "./components/WhyMerkatus";
import Features from "./components/Features";
import Integrations from "./components/Integrations";
import Download from "./components/Download";
import Footer from "./components/Footer";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";

function ScrollToTop() {
  const location = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location]);
  return null;
}

/**
 * Componente para redirecionar usuário autenticado
 * Se já estiver logado, redireciona para o dashboard
 */
function PublicOnlyRoute({ children }) {
  const { isAuthenticated, initialized, loading } = useAuth();

  if (!initialized || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-[var(--accent)]"></div>
      </div>
    );
  }

  return isAuthenticated ? <Navigate to="/dashboard" replace /> : children;
}

/**
 * Componente para proteger rotas privadas
 * Apenas usuários autenticados podem acessar
 */
function ProtectedRoute({ children }) {
  const { isAuthenticated, initialized, loading } = useAuth();

  if (!initialized || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-[var(--accent)]"></div>
      </div>
    );
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

function Landing() {
  useScrollAnimation(".animate-fade-in-up");
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
  );
}

/**
 * Componente de rotas da aplicação
 * Separado para ter acesso ao AuthContext
 */
function AppRoutes() {
  return (
    <>
      <ScrollToTop />
      <Routes>
        {/* Landing page - pública */}
        <Route path="/" element={<Landing />} />

        {/* Login - apenas para não autenticados */}
        <Route
          path="/login"
          element={
            <PublicOnlyRoute>
              <LoginPage />
            </PublicOnlyRoute>
          }
        />

        {/* Dashboard - rota protegida */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <div
          className="bg-[var(--bg)] min-h-screen transition-colors duration-300"
          style={{ color: "var(--text)" }}
        >
          <AppRoutes />
        </div>
      </AuthProvider>
    </ThemeProvider>
  );
}
