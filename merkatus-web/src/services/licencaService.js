/**
 * Serviço para verificação de licenças
 * Endpoints públicos para uso no ERP cliente
 */

import { api } from './api';

const LICENCA_ENDPOINTS = {
  VERIFICAR: '/licencas/public/verificar',
  ATIVAR: '/licencas/public/ativar',
  HEARTBEAT: '/licencas/public/heartbeat',
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
  return btoa(raw).replace(/[^a-zA-Z0-9]/g, '').substring(0, 50);
}

/**
 * Verifica se uma licença é válida para o CNPJ informado
 * @param {string} cnpj - CNPJ do cliente (pode estar formatado)
 * @returns {Promise<object>}
 */
export async function verificarLicenca(cnpj) {
  const cnpjLimpo = cnpj.replace(/\D/g, '');
  const identificador = gerarIdentificadorMaquina();

  const response = await api.post(LICENCA_ENDPOINTS.VERIFICAR, {
    chave_ativacao: cnpjLimpo,
    identificador_maquina: identificador,
  });

  if (response.success) {
    return {
      valido: response.data?.valido ?? false,
      podeOperar: response.data?.pode_operar ?? false,
      mensagem: response.data?.mensagem || '',
      licenca: response.data?.licenca || null,
      terminal: response.data?.terminal || null,
    };
  }

  throw new Error(response.error?.message || 'Erro ao verificar licença');
}

/**
 * Ativa um terminal (PDV ou Gerenciador)
 * @param {string} cnpj - CNPJ do cliente
 * @param {string} tipo - 'PDV' ou 'GERENCIADOR'
 * @param {string} nome - Nome do terminal
 * @returns {Promise<object>}
 */
export async function ativarTerminal(cnpj, tipo, nome) {
  const cnpjLimpo = cnpj.replace(/\D/g, '');
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
      mensagem: response.data?.mensagem || '',
      licenca: response.data?.licenca || null,
      terminal: response.data?.terminal || null,
    };
  }

  throw new Error(response.error?.message || 'Erro ao ativar terminal');
}

/**
 * Envia heartbeat para manter terminal ativo
 * @param {string} cnpj - CNPJ do cliente
 * @returns {Promise<object>}
 */
export async function enviarHeartbeat(cnpj) {
  const cnpjLimpo = cnpj.replace(/\D/g, '');
  const identificador = gerarIdentificadorMaquina();

  const response = await api.post(LICENCA_ENDPOINTS.HEARTBEAT, {
    chave_ativacao: cnpjLimpo,
    identificador_maquina: identificador,
  });

  if (response.success) {
    return {
      sucesso: response.data?.sucesso ?? false,
      mensagem: response.data?.mensagem || '',
    };
  }

  throw new Error(response.error?.message || 'Erro ao enviar heartbeat');
}

export default {
  verificarLicenca,
  ativarTerminal,
  enviarHeartbeat,
};
