/**
 * Serviço de gerenciamento de clientes
 * Gerencia operações CRUD para clientes licenciados
 */

import { api } from "./api";

const CLIENTE_ENDPOINTS = {
  LIST: "/clientes",
  CREATE: "/clientes",
  GET: (id) => `/clientes/${id}`,
  UPDATE: (id) => `/clientes/${id}`,
  DELETE: (id) => `/clientes/${id}`,
};

/**
 * Lista clientes com paginação e filtros
 * @param {object} params - Parâmetros de busca
 * @param {string} params.search - Busca por razão social, CNPJ ou cidade
 * @param {string} params.estado - Filtro por estado
 * @param {boolean} params.ativo - Filtro por status ativo
 * @param {number} params.page - Página atual (1-based)
 * @param {number} params.limit - Itens por página
 * @returns {Promise<{data: Array, meta: {pagination: object}}>}
 */
export async function listarClientes(params = {}) {
  try {
    const queryParams = new URLSearchParams();

    if (params.search) queryParams.append("search", params.search);
    if (params.estado) queryParams.append("estado", params.estado);
    if (params.ativo !== undefined)
      queryParams.append("ativo", params.ativo.toString());
    if (params.page) queryParams.append("page", params.page.toString());
    if (params.limit) queryParams.append("limit", params.limit.toString());

    const query = queryParams.toString();
    const endpoint = query
      ? `${CLIENTE_ENDPOINTS.LIST}?${query}`
      : CLIENTE_ENDPOINTS.LIST;

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
 * Busca cliente por ID
 * @param {number} id - ID do cliente
 * @returns {Promise<object>} Dados do cliente
 */
export async function buscarCliente(id) {
  try {
    const response = await api.get(CLIENTE_ENDPOINTS.GET(id));

    if (response.success) {
      return response.data;
    }

    throw new Error("Cliente não encontrado");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Cria novo cliente
 * @param {object} clienteData - Dados do cliente
 * @param {string} clienteData.cnpj - CNPJ (14-18 caracteres)
 * @param {string} clienteData.razao_social - Razão social (obrigatório)
 * @param {string} clienteData.email - Email
 * @param {string} clienteData.telefone - Telefone
 * @param {string} clienteData.endereco - Endereço
 * @param {string} clienteData.cidade - Cidade
 * @param {string} clienteData.estado - Estado (UF)
 * @param {string} clienteData.cep - CEP
 * @returns {Promise<object>} Cliente criado
 */
export async function criarCliente(clienteData) {
  try {
    const response = await api.post(CLIENTE_ENDPOINTS.CREATE, clienteData);

    if (response.success) {
      return response.data;
    }

    throw new Error("Erro ao criar cliente");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Atualiza cliente existente
 * @param {number} id - ID do cliente
 * @param {object} clienteData - Dados atualizados
 * @returns {Promise<object>} Cliente atualizado
 */
export async function atualizarCliente(id, clienteData) {
  try {
    const response = await api.put(CLIENTE_ENDPOINTS.UPDATE(id), clienteData);

    if (response.success) {
      return response.data;
    }

    throw new Error("Erro ao atualizar cliente");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Remove cliente (soft delete)
 * @param {number} id - ID do cliente
 * @returns {Promise<void>}
 */
export async function removerCliente(id) {
  try {
    const response = await api.delete(CLIENTE_ENDPOINTS.DELETE(id));

    if (!response.success) {
      throw new Error("Erro ao remover cliente");
    }
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Conta total de clientes
 * @returns {Promise<number>} Total de clientes
 */
export async function contarClientes() {
  try {
    const result = await listarClientes({ page: 1, limit: 1 });
    return result.meta?.pagination?.total || 0;
  } catch (error) {
    console.error("Erro ao contar clientes:", error);
    return 0;
  }
}
