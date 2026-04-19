/**
 * Serviço para verificação de licenças
 * Endpoints públicos para uso no ERP cliente
 */

import { api } from "./api";

const LICENCA_ENDPOINTS = {
  VERIFICAR: "/licencas/public/verificar",
  ATIVAR: "/licencas/public/ativar",
  HEARTBEAT: "/licencas/public/heartbeat",
  LIST: "/licencas",
  CREATE: "/licencas",
  GET: (id) => `/licencas/${id}`,
  UPDATE: (id) => `/licencas/${id}`,
  DELETE: (id) => `/licencas/${id}`,
  RENOVAR: (id) => `/licencas/${id}/renovar`,
};

/**
 * Gera identificador único da máquina
 * Combina informações do navegador para criar um fingerprint
 * @returns {string}
 */
function gerarIdentificadorMaquina() {
  const navegador = navigator.userAgent;
  const plataforma = navigator.platform;
  const idioma = navigator.language;
  const resolucao = `${screen.width}x${screen.height}`;

  // Cria uma string única baseada nas características
  const raw = `${navegador}|${plataforma}|${idioma}|${resolucao}`;

  // Converte para base64 simples
  return btoa(raw)
    .replace(/[^a-zA-Z0-9]/g, "")
    .substring(0, 50);
}

/**
 * Verifica se uma licença é válida para o CNPJ informado
 * @param {string} cnpj - CNPJ do cliente (pode estar formatado)
 * @returns {Promise<object>}
 */
export async function verificarLicenca(cnpj) {
  const cnpjLimpo = cnpj.replace(/\D/g, "");
  const identificador = gerarIdentificadorMaquina();

  const response = await api.post(LICENCA_ENDPOINTS.VERIFICAR, {
    chave_ativacao: cnpjLimpo,
    identificador_maquina: identificador,
  });

  if (response.success) {
    return {
      valido: response.data?.valido ?? false,
      podeOperar: response.data?.pode_operar ?? false,
      mensagem: response.data?.mensagem || "",
      licenca: response.data?.licenca || null,
      terminal: response.data?.terminal || null,
    };
  }

  throw new Error(response.error?.message || "Erro ao verificar licença");
}

/**
 * Ativa um terminal (PDV ou Gerenciador)
 * @param {string} cnpj - CNPJ do cliente
 * @param {string} tipo - 'PDV' ou 'GERENCIADOR'
 * @param {string} nome - Nome do terminal
 * @returns {Promise<object>}
 */
export async function ativarTerminal(cnpj, tipo, nome) {
  const cnpjLimpo = cnpj.replace(/\D/g, "");
  const identificador = gerarIdentificadorMaquina();

  const response = await api.post(LICENCA_ENDPOINTS.ATIVAR, {
    chave_ativacao: cnpjLimpo,
    identificador_maquina: identificador,
    tipo: tipo.toUpperCase(),
    nome: nome,
  });

  if (response.success) {
    return {
      sucesso: response.data?.sucesso ?? false,
      mensagem: response.data?.mensagem || "",
      licenca: response.data?.licenca || null,
      terminal: response.data?.terminal || null,
    };
  }

  throw new Error(response.error?.message || "Erro ao ativar terminal");
}

/**
 * Envia heartbeat para manter terminal ativo
 * @param {string} cnpj - CNPJ do cliente
 * @returns {Promise<object>}
 */
export async function enviarHeartbeat(cnpj) {
  const cnpjLimpo = cnpj.replace(/\D/g, "");
  const identificador = gerarIdentificadorMaquina();

  const response = await api.post(LICENCA_ENDPOINTS.HEARTBEAT, {
    chave_ativacao: cnpjLimpo,
    identificador_maquina: identificador,
  });

  if (response.success) {
    return {
      sucesso: response.data?.sucesso ?? false,
      mensagem: response.data?.mensagem || "",
    };
  }

  throw new Error(response.error?.message || "Erro ao enviar heartbeat");
}

/**
 * Lista licenças com paginação e filtros (Admin)
 * @param {object} params - Parâmetros de busca
 * @param {string} params.search - Busca por chave de ativação
 * @param {string} params.status - Filtro por status (ATIVA/EXPIRADA/SUSPENSA/CANCELADA)
 * @param {number} params.id_cliente - Filtro por cliente
 * @param {number} params.expiring_days - Licenças que expiram em X dias
 * @param {number} params.page - Página atual (1-based)
 * @param {number} params.limit - Itens por página
 * @returns {Promise<{data: Array, meta: {pagination: object}}>}
 */
export async function listarLicencas(params = {}) {
  try {
    const queryParams = new URLSearchParams();

    if (params.search) queryParams.append("search", params.search);
    if (params.status) queryParams.append("status", params.status);
    if (params.id_cliente)
      queryParams.append("id_cliente", params.id_cliente.toString());
    if (params.expiring_days)
      queryParams.append("expiring_days", params.expiring_days.toString());
    if (params.page) queryParams.append("page", params.page.toString());
    if (params.limit) queryParams.append("limit", params.limit.toString());

    const query = queryParams.toString();
    const endpoint = query
      ? `${LICENCA_ENDPOINTS.LIST}?${query}`
      : LICENCA_ENDPOINTS.LIST;

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
 * Busca licença por ID (Admin)
 * @param {number} id - ID da licença
 * @returns {Promise<object>} Dados da licença
 */
export async function buscarLicenca(id) {
  try {
    const response = await api.get(LICENCA_ENDPOINTS.GET(id));

    if (response.success) {
      return response.data;
    }

    throw new Error("Licença não encontrada");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Cria nova licença (Admin)
 * @param {object} licencaData - Dados da licença
 * @param {number} licencaData.id_cliente - ID do cliente (obrigatório)
 * @param {number} licencaData.meses_validade - Meses de validade (≥1, padrão 12)
 * @param {object} licencaData.capacidades - Capacidades PDV/GERENCIADOR
 * @param {number} licencaData.capacidades.qtd_pdv_incluso - PDV incluído
 * @param {number} licencaData.capacidades.qtd_pdv_adicional - PDV adicional
 * @param {number} licencaData.capacidades.qtd_gerenciador_incluso - GERENCIADOR incluído
 * @param {number} licencaData.capacidades.qtd_gerenciador_adicional - GERENCIADOR adicional
 * @returns {Promise<object>} Licença criada
 */
export async function criarLicenca(licencaData) {
  try {
    const response = await api.post(LICENCA_ENDPOINTS.CREATE, licencaData);

    if (response.success) {
      return response.data;
    }

    throw new Error("Erro ao criar licença");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Atualiza licença existente (Admin)
 * @param {number} id - ID da licença
 * @param {object} licencaData - Dados atualizados
 * @returns {Promise<object>} Licença atualizada
 */
export async function atualizarLicenca(id, licencaData) {
  try {
    const response = await api.put(LICENCA_ENDPOINTS.UPDATE(id), licencaData);

    if (response.success) {
      return response.data;
    }

    throw new Error("Erro ao atualizar licença");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Renova licença (estende validade) (Admin)
 * @param {number} id - ID da licença
 * @param {object} renovacaoData - Dados da renovação
 * @param {number} renovacaoData.meses_adicionais - Meses a adicionar
 * @returns {Promise<object>} Licença renovada
 */
export async function renovarLicenca(id, renovacaoData = {}) {
  try {
    const response = await api.post(
      LICENCA_ENDPOINTS.RENOVAR(id),
      renovacaoData,
    );

    if (response.success) {
      return response.data;
    }

    throw new Error("Erro ao renovar licença");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Remove licença (Admin)
 * @param {number} id - ID da licença
 * @returns {Promise<void>}
 */
export async function removerLicenca(id) {
  try {
    const response = await api.delete(LICENCA_ENDPOINTS.DELETE(id));

    if (!response.success) {
      throw new Error("Erro ao remover licença");
    }
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

/**
 * Conta total de licenças
 * @returns {Promise<number>} Total de licenças
 */
export async function contarLicencas() {
  try {
    const result = await listarLicencas({ page: 1, limit: 1 });
    return result.meta?.pagination?.total || 0;
  } catch (error) {
    console.error("Erro ao contar licenças:", error);
    return 0;
  }
}

/**
 * Lista licenças recentes (últimas 5)
 * @returns {Promise<Array>} Licenças recentes
 */
export async function listarLicencasRecentes() {
  try {
    const result = await listarLicencas({ page: 1, limit: 5 });
    return result.data || [];
  } catch (error) {
    console.error("Erro ao listar licenças recentes:", error);
    return [];
  }
}

export default {
  verificarLicenca,
  ativarTerminal,
  enviarHeartbeat,
  listarLicencas,
  buscarLicenca,
  criarLicenca,
  atualizarLicenca,
  renovarLicenca,
  removerLicenca,
  contarLicencas,
  listarLicencasRecentes,
};
