/**
 * Serviço de autenticação
 * Gerencia login, logout, refresh token e verificação de sessão
 */

import { api, setTokens, clearTokens } from './api';

const AUTH_ENDPOINTS = {
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  REFRESH: '/auth/refresh',
  ME: '/auth/me',
};

/**
 * Realiza login com email e senha
 * @param {string} email
 * @param {string} senha
 * @returns {Promise<{user: object, tokens: object}>}
 */
export async function login(email, senha) {
  try {
    const response = await api.post(AUTH_ENDPOINTS.LOGIN, { email, senha });

    if (response.success && response.data?.tokens) {
      // Armazena tokens
      setTokens(
        response.data.tokens.access_token,
        response.data.tokens.refresh_token
      );

      // Armazena dados do usuário
      localStorage.setItem('user', JSON.stringify(response.data.user));

      return {
        user: response.data.user,
        tokens: response.data.tokens,
      };
    }

    throw new Error('Resposta inválida do servidor');
  } catch (error) {
    // Repassa o erro com a mensagem apropriada
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Realiza logout
 */
export async function logout() {
  try {
    // Notifica backend (opcional, já que JWT é stateless)
    await api.post(AUTH_ENDPOINTS.LOGOUT);
  } catch (error) {
    // Ignora erro no logout
  } finally {
    // Sempre limpa tokens localmente
    clearTokens();
  }
}

/**
 * Renova o access token usando refresh token
 * @returns {Promise<{access_token: string, refresh_token: string}>}
 */
export async function refreshToken() {
  const refreshToken = localStorage.getItem('refresh_token');

  if (!refreshToken) {
    throw new Error('Nenhum refresh token disponível');
  }

  const response = await api.post(AUTH_ENDPOINTS.REFRESH, { refresh_token: refreshToken });

  if (response.success && response.data?.tokens) {
    setTokens(
      response.data.tokens.access_token,
      response.data.tokens.refresh_token
    );

    return response.data.tokens;
  }

  throw new Error('Falha ao renovar token');
}

/**
 * Obtém dados do usuário logado
 * @returns {Promise<object>}
 */
export async function getCurrentUser() {
  const response = await api.get(AUTH_ENDPOINTS.ME);

  if (response.success && response.data?.user) {
    localStorage.setItem('user', JSON.stringify(response.data.user));
    return response.data.user;
  }

  throw new Error('Não foi possível obter dados do usuário');
}

/**
 * Verifica se há um usuário autenticado
 * @returns {boolean}
 */
export function isAuthenticated() {
  const token = localStorage.getItem('access_token');
  return !!token;
}

/**
 * Obtém dados do usuário do localStorage
 * @returns {object|null}
 */
export function getStoredUser() {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

/**
 * Verifica se o token está expirado (decodificação básica do JWT)
 * @param {string} token
 * @returns {boolean}
 */
export function isTokenExpired(token) {
  if (!token) return true;

  try {
    // Decodifica payload do JWT (base64)
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000; // Converte para milissegundos
    return Date.now() >= exp;
  } catch {
    return true;
  }
}

/**
 * Verifica e renova token se necessário
 * @returns {Promise<boolean>}
 */
export async function checkAndRefreshToken() {
  const token = localStorage.getItem('access_token');

  if (!token || isTokenExpired(token)) {
    try {
      await refreshToken();
      return true;
    } catch {
      clearTokens();
      return false;
    }
  }

  return true;
}

export default {
  login,
  logout,
  refreshToken,
  getCurrentUser,
  isAuthenticated,
  getStoredUser,
  isTokenExpired,
  checkAndRefreshToken,
};
