import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  login as loginService,
  logout as logoutService,
  getStoredUser,
  isAuthenticated as checkIsAuthenticated,
  checkAndRefreshToken,
} from '../services/authService';

/**
 * Contexto de Autenticação
 * Gerencia estado de autenticação do usuário em toda a aplicação
 */

const AuthContext = createContext(null);

/**
 * Provider de Autenticação
 * Deve envolver a aplicação para fornecer contexto de auth
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [initialized, setInitialized] = useState(false);

  /**
   * Inicializa estado de autenticação ao carregar
   */
  useEffect(() => {
    const initAuth = async () => {
      try {
        if (checkIsAuthenticated()) {
          // Tenta renovar token se necessário
          const isValid = await checkAndRefreshToken();

          if (isValid) {
            const storedUser = getStoredUser();
            setUser(storedUser);
            setIsAuthenticated(true);
          } else {
            // Token inválido, limpa estado
            setUser(null);
            setIsAuthenticated(false);
          }
        }
      } catch (error) {
        console.error('Erro ao inicializar autenticação:', error);
        setUser(null);
        setIsAuthenticated(false);
      } finally {
        setLoading(false);
        setInitialized(true);
      }
    };

    initAuth();
  }, []);

  /**
   * Escuta evento de não autorizado para fazer logout
   */
  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
      setIsAuthenticated(false);
      // Redireciona para login se necessário
      window.location.href = '/login';
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);

    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, []);

  /**
   * Realiza login
   */
  const login = useCallback(async (email, senha) => {
    setLoading(true);

    try {
      const result = await loginService(email, senha);

      setUser(result.user);
      setIsAuthenticated(true);

      return { success: true, user: result.user };
    } catch (error) {
      setUser(null);
      setIsAuthenticated(false);

      return {
        success: false,
        error: error.message || 'Credenciais inválidas',
      };
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Realiza logout
   */
  const logout = useCallback(async () => {
    setLoading(true);

    try {
      await logoutService();
    } finally {
      setUser(null);
      setIsAuthenticated(false);
      setLoading(false);
    }
  }, []);

  /**
   * Atualiza dados do usuário no estado
   */
  const updateUser = useCallback((userData) => {
    setUser((prev) => ({
      ...prev,
      ...userData,
    }));
    // Atualiza também no localStorage
    localStorage.setItem('user', JSON.stringify({ ...user, ...userData }));
  }, [user]);

  const value = {
    user,
    isAuthenticated,
    loading,
    initialized,
    login,
    logout,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Hook para usar o contexto de autenticação
 * @returns {object} { user, isAuthenticated, loading, login, logout, updateUser }
 */
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }

  return context;
}

export default AuthContext;
