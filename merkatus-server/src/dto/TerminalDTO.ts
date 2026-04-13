import { TerminalTipo, TerminalStatus } from '@prisma/client';

// DTOs para TerminalAutorizado

export interface TerminalResponseDTO {
  id_terminal: number;
  id_licenca: number;
  licenca?: {
    chave_ativacao: string;
    cliente: {
      razao_social: string;
    };
  };
  tipo: TerminalTipo;
  nome: string;
  identificador_maquina: string;
  status: TerminalStatus;
  data_ativacao?: string;
  ultimo_heartbeat?: string;
  data_cadastro: string;
  data_atualizacao: string;
}

export interface TerminalCreateDTO {
  id_licenca: number;
  tipo: TerminalTipo;
  nome: string;
  identificador_maquina: string;
}

export interface TerminalUpdateDTO {
  nome?: string;
  status?: TerminalStatus;
}

export interface TerminalListDTO {
  id_terminal: number;
  nome: string;
  tipo: TerminalTipo;
  status: TerminalStatus;
  identificador_maquina: string;
  licenca: {
    id_licenca: number;
    chave_ativacao: string;
  };
  ultimo_heartbeat?: string;
}

export interface HeartbeatResponseDTO {
  sucesso: boolean;
  timestamp: string;
  mensagem: string;
}

// Mappers
export function mapTerminalToResponse(terminal: any): TerminalResponseDTO {
  const response: TerminalResponseDTO = {
    id_terminal: terminal.id_terminal,
    id_licenca: terminal.id_licenca,
    tipo: terminal.tipo,
    nome: terminal.nome,
    identificador_maquina: terminal.identificador_maquina,
    status: terminal.status,
    data_ativacao: terminal.data_ativacao?.toISOString(),
    ultimo_heartbeat: terminal.ultimo_heartbeat?.toISOString(),
    data_cadastro: terminal.data_cadastro.toISOString(),
    data_atualizacao: terminal.data_atualizacao.toISOString(),
  };

  if (terminal.licenca) {
    response.licenca = {
      chave_ativacao: terminal.licenca.chave_ativacao,
      cliente: {
        razao_social: terminal.licenca.cliente.razao_social,
      },
    };
  }

  return response;
}

export function mapTerminalToListDTO(terminal: any): TerminalListDTO {
  return {
    id_terminal: terminal.id_terminal,
    nome: terminal.nome,
    tipo: terminal.tipo,
    status: terminal.status,
    identificador_maquina: terminal.identificador_maquina,
    licenca: {
      id_licenca: terminal.licenca?.id_licenca || terminal.id_licenca,
      chave_ativacao: terminal.licenca?.chave_ativacao || '',
    },
    ultimo_heartbeat: terminal.ultimo_heartbeat?.toISOString(),
  };
}
