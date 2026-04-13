// DTOs para ClienteLicenciado

export interface EnderecoDTO {
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
}

export interface ClienteResponseDTO {
  id_cliente: number;
  cnpj: string;  // Formatado para exibição
  razao_social: string;
  nome_fantasia?: string;
  inscricao_estadual?: string;
  telefone?: string;
  email?: string;
  responsavel?: string;
  endereco: EnderecoDTO;
  observacoes?: string;
  ativo: boolean;
  data_cadastro: string;
  data_atualizacao: string;
  // Relacionamentos
  total_licencas?: number;
  licencas_ativas?: number;
}

export interface ClienteCreateDTO {
  cnpj: string;
  razao_social: string;
  nome_fantasia?: string;
  inscricao_estadual?: string;
  telefone?: string;
  email?: string;
  responsavel?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
  observacoes?: string;
  ativo?: boolean;
}

export interface ClienteUpdateDTO {
  razao_social?: string;
  nome_fantasia?: string;
  inscricao_estadual?: string;
  telefone?: string;
  email?: string;
  responsavel?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
  observacoes?: string;
  ativo?: boolean;
}

export interface ClienteListDTO {
  id_cliente: number;
  cnpj: string;
  razao_social: string;
  nome_fantasia?: string;
  cidade?: string;
  estado?: string;
  telefone?: string;
  email?: string;
  ativo: boolean;
}

// Mappers
export function mapClienteToResponse(
  cliente: any,
  incluirContadores = false
): ClienteResponseDTO {
  const response: ClienteResponseDTO = {
    id_cliente: cliente.id_cliente,
    cnpj: cliente.cnpj, // Assumindo que já foi formatado
    razao_social: cliente.razao_social,
    nome_fantasia: cliente.nome_fantasia || undefined,
    inscricao_estadual: cliente.inscricao_estadual || undefined,
    telefone: cliente.telefone || undefined,
    email: cliente.email || undefined,
    responsavel: cliente.responsavel || undefined,
    endereco: {
      logradouro: cliente.logradouro || undefined,
      numero: cliente.numero || undefined,
      complemento: cliente.complemento || undefined,
      bairro: cliente.bairro || undefined,
      cidade: cliente.cidade || undefined,
      estado: cliente.estado || undefined,
      cep: cliente.cep || undefined,
    },
    observacoes: cliente.observacoes || undefined,
    ativo: cliente.ativo,
    data_cadastro: cliente.data_cadastro.toISOString(),
    data_atualizacao: cliente.data_atualizacao.toISOString(),
  };

  if (incluirContadores && cliente._count) {
    response.total_licencas = cliente._count.licencas;
  }

  if (incluirContadores && cliente.licencas) {
    response.licencas_ativas = cliente.licencas.filter(
      (l: any) => l.status === 'ATIVA'
    ).length;
  }

  return response;
}

export function mapClienteToListDTO(cliente: any): ClienteListDTO {
  return {
    id_cliente: cliente.id_cliente,
    cnpj: cliente.cnpj,
    razao_social: cliente.razao_social,
    nome_fantasia: cliente.nome_fantasia || undefined,
    cidade: cliente.cidade || undefined,
    estado: cliente.estado || undefined,
    telefone: cliente.telefone || undefined,
    email: cliente.email || undefined,
    ativo: cliente.ativo,
  };
}
