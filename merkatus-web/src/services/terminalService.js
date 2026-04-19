/**
 * Serviço de gerenciamento de terminais
 * Gerencia operações CRUD para terminais autorizados
 * NOTA: Backend atualmente não possui endpoints admin para terminais.
 * Este serviço assume que os endpoints existem conforme especificado.
 */

import { api } from "./api";

const TERMINAL_ENDPOINTS = {
  LIST: "/terminais", // Assumido
  CREATE: "/terminais", // Assumido
  GET: (id) => `/terminais/${id}`, // Assumido
  UPDATE: (id) => `/terminais/${id}`, // Assumido
  DELETE: (id) => `/terminais/${id}`, // Assumido
};

/**
 * Lista terminais com paginação e filtros
 * @param {object} params - Parâmetros de busca
 * @param {string} params.search - Busca por identificador ou nome
 * @param {string} params.tipo - Filtro por tipo (PDV/GERENCIADOR)
 * @param {string} params.status - Filtro por status (ATIVO/INATIVO/BLOQUEADO)
 * @param {number} params.id_licenca - Filtro por licença
 * @param {number} params.page - Página atual (1-based)
 * @param {number} params.limit - Itens por página
 * @returns {Promise<{data: Array, meta: {pagination: object}}>}
 */
export async function listarTerminais(params = {}) {
  try {
    const queryParams = new URLSearchParams();

    if (params.search) queryParams.append("search", params.search);
    if (params.tipo) queryParams.append("tipo", params.tipo);
    if (params.status) queryParams.append("status", params.status);
    if (params.id_licenca)
      queryParams.append("id_licenca", params.id_licenca.toString());
    if (params.page) queryParams.append("page", params.page.toString());
    if (params.limit) queryParams.append("limit", params.limit.toString());

    const query = queryParams.toString();
    const endpoint = query
      ? `${TERMINAL_ENDPOINTS.LIST}?${query}`
      : TERMINAL_ENDPOINTS.LIST;

    const response = await api.get(endpoint);

    if (response.success) {
      return {
        data: response.data || [],
        meta: response.meta || {},
      };
    }

    throw new Error("Resposta inválida do servidor");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Busca terminal por ID
 * @param {number} id - ID do terminal
 * @returns {Promise<object>} Dados do terminal
 */
export async function buscarTerminal(id) {
  try {
    const response = await api.get(TERMINAL_ENDPOINTS.GET(id));

    if (response.success) {
      return response.data;
    }

    throw new Error("Terminal não encontrado");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Cria novo terminal
 * @param {object} terminalData - Dados do terminal
 * @param {number} terminalData.id_licenca - ID da licença (obrigatório)
 * @param {string} terminalData.tipo - Tipo (PDV/GERENCIADOR)
 * @param {string} terminalData.nome - Nome do terminal (1-60 chars)
 * @param {string} terminalData.identificador_maquina - Identificador único (auto-gerado se vazio)
 * @returns {Promise<object>} Terminal criado
 */
export async function criarTerminal(terminalData) {
  try {
    const response = await api.post(TERMINAL_ENDPOINTS.CREATE, terminalData);

    if (response.success) {
      return response.data;
    }

    throw new Error("Erro ao criar terminal");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Atualiza terminal existente
 * @param {number} id - ID do terminal
 * @param {object} terminalData - Dados atualizados (nome, status editáveis)
 * @returns {Promise<object>} Terminal atualizado
 */
export async function atualizarTerminal(id, terminalData) {
  try {
    const response = await api.put(TERMINAL_ENDPOINTS.UPDATE(id), terminalData);

    if (response.success) {
      return response.data;
    }

    throw new Error("Erro ao atualizar terminal");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Conta total de terminais
 * @returns {Promise<number>} Total de terminais
 */
export async function contarTerminais() {
  try {
    const result = await listarTerminais({ page: 1, limit: 1 });
    return result.meta?.pagination?.total || 0;
  } catch (error) {
    console.error("Erro ao contar terminais:", error);
    return 0;
  }
}
