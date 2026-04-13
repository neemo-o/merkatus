import { ClienteRepository, ClienteFilters } from '../repositories/ClienteRepository';
import {
  ClienteResponseDTO,
  ClienteCreateDTO,
  ClienteUpdateDTO,
  ClienteListDTO,
  mapClienteToResponse,
  mapClienteToListDTO,
} from '../dto/ClienteDTO';
import {
  parsePaginationParams,
  calculateSkip,
  createPaginatedResponse,
  PaginatedResponse,
} from '../utils/paginacao';
import { encrypt } from '../utils/crypto';
import { hashData, formatarCNPJ } from '../utils/hash';
import {
  NotFoundError,
  ConflictError,
  ValidationError,
} from '../errors/AppError';

export class ClienteService {
  private clienteRepository: ClienteRepository;

  constructor() {
    this.clienteRepository = ClienteRepository.getInstance();
  }

  async criarCliente(dto: ClienteCreateDTO): Promise<ClienteResponseDTO> {
    // Verifica se CNPJ já existe
    const cnpjHash = await hashData(dto.cnpj);
    const exists = await this.clienteRepository.existsByCNPJHash(cnpjHash);

    if (exists) {
      throw new ConflictError('CNPJ já cadastrado');
    }

    // Criptografa CNPJ para armazenamento
    const cnpjCriptografado = encrypt(dto.cnpj);

    // Prepara dados para criação
    const createData = {
      cnpj: cnpjCriptografado,
      cnpj_hash: cnpjHash,
      razao_social: dto.razao_social,
      nome_fantasia: dto.nome_fantasia,
      inscricao_estadual: dto.inscricao_estadual,
      telefone: dto.telefone,
      email: dto.email,
      responsavel: dto.responsavel,
      logradouro: dto.logradouro,
      numero: dto.numero,
      complemento: dto.complemento,
      bairro: dto.bairro,
      cidade: dto.cidade,
      estado: dto.estado,
      cep: dto.cep,
      observacoes: dto.observacoes,
      ativo: dto.ativo ?? true,
    };

    const cliente = await this.clienteRepository.create(createData);

    // Mapeia para resposta (descriptografa CNPJ para exibição)
    const response = mapClienteToResponse({
      ...cliente,
      cnpj: formatarCNPJ(dto.cnpj), // Usa o CNPJ original formatado
    });

    return response;
  }

  async atualizarCliente(
    id: number,
    dto: ClienteUpdateDTO
  ): Promise<ClienteResponseDTO> {
    // Verifica se cliente existe
    const exists = await this.clienteRepository.findById(id);
    if (!exists) {
      throw new NotFoundError('Cliente');
    }

    // Prepara dados para atualização
    const updateData: any = {};

    if (dto.razao_social !== undefined) updateData.razao_social = dto.razao_social;
    if (dto.nome_fantasia !== undefined) updateData.nome_fantasia = dto.nome_fantasia;
    if (dto.inscricao_estadual !== undefined) updateData.inscricao_estadual = dto.inscricao_estadual;
    if (dto.telefone !== undefined) updateData.telefone = dto.telefone;
    if (dto.email !== undefined) updateData.email = dto.email || null;
    if (dto.responsavel !== undefined) updateData.responsavel = dto.responsavel;
    if (dto.logradouro !== undefined) updateData.logradouro = dto.logradouro;
    if (dto.numero !== undefined) updateData.numero = dto.numero;
    if (dto.complemento !== undefined) updateData.complemento = dto.complemento;
    if (dto.bairro !== undefined) updateData.bairro = dto.bairro;
    if (dto.cidade !== undefined) updateData.cidade = dto.cidade;
    if (dto.estado !== undefined) updateData.estado = dto.estado;
    if (dto.cep !== undefined) updateData.cep = dto.cep;
    if (dto.observacoes !== undefined) updateData.observacoes = dto.observacoes;
    if (dto.ativo !== undefined) updateData.ativo = dto.ativo;

    const cliente = await this.clienteRepository.update(id, updateData);

    // Recupera o CNPJ formatado para resposta
    return mapClienteToResponse({
      ...cliente,
      cnpj: formatarCNPJ(cliente.cnpj), // Note: em produção, descriptografar
    });
  }

  async obterCliente(id: number): Promise<ClienteResponseDTO> {
    const cliente = await this.clienteRepository.findById(id, {
      includeCount: true,
    });

    if (!cliente) {
      throw new NotFoundError('Cliente');
    }

    return mapClienteToResponse(cliente, true);
  }

  async listarClientes(
    query: { [key: string]: string | undefined }
  ): Promise<PaginatedResponse<ClienteListDTO>> {
    const { page, limit, sort, order } = parsePaginationParams(query);

    // Prepara filtros
    const filters: ClienteFilters = {
      search: query.search,
      ativo: query.ativo ? query.ativo === 'true' : undefined,
      cidade: query.cidade,
      estado: query.estado,
    };

    const skip = calculateSkip(page, limit);
    const orderBy: { [key: string]: 'asc' | 'desc' } = {};
    orderBy[sort] = order;

    const [clientes, total] = await Promise.all([
      this.clienteRepository.findAll(filters, skip, limit, orderBy),
      this.clienteRepository.count(filters),
    ]);

    const data = clientes.map(mapClienteToListDTO);

    return createPaginatedResponse(data, total, page, limit);
  }

  async desativarCliente(id: number): Promise<void> {
    // Verifica se cliente existe
    const cliente = await this.clienteRepository.findById(id);
    if (!cliente) {
      throw new NotFoundError('Cliente');
    }

    // Verifica se possui licenças ativas
    const temLicencasAtivas = await this.clienteRepository.hasLicencasAtivas(id);
    if (temLicencasAtivas) {
      throw new ValidationError(
        'Não é possível desativar cliente com licenças ativas. Cancele ou desative as licenças primeiro.'
      );
    }

    await this.clienteRepository.softDelete(id);
  }

  async buscarPorCNPJ(cnpj: string): Promise<ClienteResponseDTO | null> {
    const cnpjHash = await hashData(cnpj);
    const cliente = await this.clienteRepository.findByCNPJHash(cnpjHash);

    if (!cliente) {
      return null;
    }

    return mapClienteToResponse(cliente);
  }
}
