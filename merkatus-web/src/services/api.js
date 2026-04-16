/**
 * Cliente HTTP centralizado para comunicação com o backend
 * Utiliza fetch API nativo do JavaScript
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000/api';

/**
 * Extrai o token JWT do localStorage
 */
function getAccessToken() {
  return localStorage.getItem('access_token');
}

/**
 * Armazena tokens no localStorage
 */
export function setTokens(accessToken, refreshToken) {
  localStorage.setItem('access_token', accessToken);
  if (refreshToken) {
    localStorage.setItem('refresh_token', refreshToken);
  }
}

/**
 * Remove tokens do localStorage
 */
export function clearTokens() {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  localStorage.removeItem('user');
}

/**
 * Faz requisição HTTP com tratamento de erro e injeção de token
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;

  // Configura headers padrão
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Injeta token JWT se existir
  const token = getAccessToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  // Adiciona body se for objeto
  if (options.body && typeof options.body === 'object') {
    config.body = JSON.stringify(options.body);
  }

  try {
    const response = await fetch(url, config);
    const data = await response.json();

    // Trata erros HTTP
    if (!response.ok) {
      const error = new Error(data?.error?.message || 'Erro na requisição');
      error.response = response;
      error.status = response.status;
      error.data = data;

      // Se for 401 (não autorizado), dispara evento para logout
      if (response.status === 401) {
        window.dispatchEvent(new CustomEvent('auth:unauthorized'));
      }

      throw error;
    }

    return data;
  } catch (error) {
    // Erro de rede ou fetch falhou
    if (!error.status) {
      error.message = 'Erro de conexão. Verifique se o servidor está online.';
    }
    throw error;
  }
}

/**
 * Métodos HTTP convenientes
 */
export const api = {
  /**
   * GET request
   */
  get: (endpoint, options = {}) => {
    return request(endpoint, { ...options, method: 'GET' });
  },

  /**
   * POST request
   */
  post: (endpoint, body, options = {}) => {
    return request(endpoint, { ...options, method: 'POST', body });
  },

  /**
   * PUT request
   */
  put: (endpoint, body, options = {}) => {
    return request(endpoint, { ...options, method: 'PUT', body });
  },

  /**
   * DELETE request
   */
  delete: (endpoint, options = {}) => {
    return request(endpoint, { ...options, method: 'DELETE' });
  },
};

/**
 * Hook para obter base URL da API (útil para URLs de download)
 */
export function getApiBaseUrl() {
  return API_BASE_URL;
}

export default api;
