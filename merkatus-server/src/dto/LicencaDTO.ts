import { LicencaStatus, TerminalTipo } from '@prisma/client';

// DTOs para Licenca

export interface LicencaCapacidadeDTO {
  qtd_pdv_incluso: number;
  qtd_gerenciador_incluso: number;
  qtd_pdv_adicional: number;
  qtd_gerenciador_adicional: number;
  qtd_pdv_total: number;
  qtd_gerenciador_total: number;
}

export interface LicencaResponseDTO {
  id_licenca: number;
  id_cliente: number;
  cliente?: {
    razao_social: string;
    cnpj: string;
  };
  chave_ativacao: string;
  capacidade: LicencaCapacidadeDTO;
  data_ativacao?: string;
  data_validade: string;
  status: LicencaStatus;
  dias_alerta?: number;
  dias_para_expirar?: number;
  data_cadastro: string;
  data_atualizacao: string;
  // Relacionamentos
  total_terminais?: number;
  terminais_ativos?: number;
}

export interface LicencaCreateDTO {
  id_cliente: number;
  qtd_pdv_incluso?: number;
  qtd_gerenciador_incluso?: number;
  qtd_pdv_adicional?: number;
  qtd_gerenciador_adicional?: number;
  meses_validade?: number;
  dias_alerta?: number;
}

export interface LicencaUpdateDTO {
  qtd_pdv_adicional?: number;
  qtd_gerenciador_adicional?: number;
  data_validade?: Date;
  status?: LicencaStatus;
  dias_alerta?: number;
}

export interface LicencaRenovacaoDTO {
  meses_adicionais: number;
  nova_qtd_pdv?: number;
  nova_qtd_gerenciador?: number;
}

export interface LicencaListDTO {
  id_licenca: number;
  chave_ativacao: string;
  cliente: {
    id_cliente: number;
    razao_social: string;
    cnpj: string;
  };
  data_validade: string;
  status: LicencaStatus;
  capacidade: {
    qtd_pdv_total: number;
    qtd_gerenciador_total: number;
  };
}

export interface VerificacaoLicencaResponseDTO {
  valido: boolean;
  licenca?: {
    chave_ativacao: string;
    status: LicencaStatus;
    data_validade: string;
  };
  terminal?: {
    id_terminal: number;
    nome: string;
    tipo: TerminalTipo;
    status: string;
  };
  pode_operar: boolean;
  mensagem: string;
}

export interface AtivacaoLicencaResponseDTO {
  sucesso: boolean;
  licenca?: {
    chave_ativacao: string;
    status: LicencaStatus;
    data_validade: string;
  };
  terminal?: {
    id_terminal: number;
    nome: string;
    tipo: TerminalTipo;
    status: string;
  };
  mensagem: string;
}

// Mappers
export function mapLicencaToResponse(licenca: any): LicencaResponseDTO {
  const response: LicencaResponseDTO = {
    id_licenca: licenca.id_licenca,
    id_cliente: licenca.id_cliente,
    chave_ativacao: licenca.chave_ativacao,
    capacidade: {
      qtd_pdv_incluso: licenca.qtd_pdv_incluso,
      qtd_gerenciador_incluso: licenca.qtd_gerenciador_incluso,
      qtd_pdv_adicional: licenca.qtd_pdv_adicional,
      qtd_gerenciador_adicional: licenca.qtd_gerenciador_adicional,
      qtd_pdv_total: licenca.qtd_pdv_total,
      qtd_gerenciador_total: licenca.qtd_gerenciador_total,
    },
    data_ativacao: licenca.data_ativacao?.toISOString(),
    data_validade: licenca.data_validade.toISOString(),
    status: licenca.status,
    dias_alerta: licenca.dias_alerta || undefined,
    data_cadastro: licenca.data_cadastro.toISOString(),
    data_atualizacao: licenca.data_atualizacao.toISOString(),
  };

  if (licenca.cliente) {
    response.cliente = {
      razao_social: licenca.cliente.razao_social,
      cnpj: licenca.cliente.cnpj,
    };
  }

  // Calcula dias para expirar
  const hoje = new Date();
  const validade = new Date(licenca.data_validade);
  const diffTime = validade.getTime() - hoje.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  response.dias_para_expirar = diffDays;

  if (licenca._count) {
    response.total_terminais = licenca._count.terminais;
  }

  if (licenca.terminais) {
    response.terminais_ativos = licenca.terminais.filter(
      (t: any) => t.status === 'ATIVO'
    ).length;
  }

  return response;
}

export function mapLicencaToListDTO(licenca: any): LicencaListDTO {
  return {
    id_licenca: licenca.id_licenca,
    chave_ativacao: licenca.chave_ativacao,
    cliente: {
      id_cliente: licenca.cliente.id_cliente,
      razao_social: licenca.cliente.razao_social,
      cnpj: licenca.cliente.cnpj,
    },
    data_validade: licenca.data_validade.toISOString(),
    status: licenca.status,
    capacidade: {
      qtd_pdv_total: licenca.qtd_pdv_total,
      qtd_gerenciador_total: licenca.qtd_gerenciador_total,
    },
  };
}
