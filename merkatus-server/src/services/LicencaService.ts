import { LicencaRepository, LicencaFilters } from '../repositories/LicencaRepository';
import { TerminalRepository } from '../repositories/TerminalRepository';
import {
  LicencaResponseDTO,
  LicencaCreateDTO,
  LicencaUpdateDTO,
  LicencaRenovacaoDTO,
  LicencaListDTO,
  VerificacaoLicencaResponseDTO,
  AtivacaoLicencaResponseDTO,
  mapLicencaToResponse,
  mapLicencaToListDTO,
} from '../dto/LicencaDTO';
import {
  parsePaginationParams,
  calculateSkip,
  createPaginatedResponse,
  PaginatedResponse,
} from '../utils/paginacao';
import { gerarChaveAtivacao } from '../utils/crypto';
import { NotFoundError } from '../errors/AppError';
import { LicencaStatus } from '@prisma/client';

export class LicencaService {
  private licencaRepository: LicencaRepository;
  private terminalRepository: TerminalRepository;

  constructor() {
    this.licencaRepository = LicencaRepository.getInstance();
    this.terminalRepository = TerminalRepository.getInstance();
  }

  async criarLicenca(dto: LicencaCreateDTO): Promise<LicencaResponseDTO> {
    // Calcula totais
    const qtd_pdv_total = (dto.qtd_pdv_incluso || 1) + (dto.qtd_pdv_adicional || 0);
    const qtd_gerenciador_total = (dto.qtd_gerenciador_incluso || 1) + (dto.qtd_gerenciador_adicional || 0);

    // Calcula data de validade
    const data_validade = new Date();
    data_validade.setMonth(data_validade.getMonth() + (dto.meses_validade || 12));

    // Gera chave de ativação única
    let chave_ativacao = gerarChaveAtivacao();
    while (await this.licencaRepository.existsByChave(chave_ativacao)) {
      chave_ativacao = gerarChaveAtivacao();
    }

    const licenca = await this.licencaRepository.create({
      cliente: { connect: { id_cliente: dto.id_cliente } },
      chave_ativacao,
      qtd_pdv_incluso: dto.qtd_pdv_incluso,
      qtd_gerenciador_incluso: dto.qtd_gerenciador_incluso,
      qtd_pdv_adicional: dto.qtd_pdv_adicional,
      qtd_gerenciador_adicional: dto.qtd_gerenciador_adicional,
      qtd_pdv_total,
      qtd_gerenciador_total,
      data_validade,
      status: 'ATIVA',
      dias_alerta: dto.dias_alerta,
    });

    return mapLicencaToResponse(licenca);
  }

  async atualizarLicenca(
    id: number,
    dto: LicencaUpdateDTO
  ): Promise<LicencaResponseDTO> {
    const licencaExistente = await this.licencaRepository.findById(id);
    if (!licencaExistente) {
      throw new NotFoundError('Licença');
    }

    const updateData: any = {};

    if (dto.qtd_pdv_adicional !== undefined) {
      updateData.qtd_pdv_adicional = dto.qtd_pdv_adicional;
      updateData.qtd_pdv_total =
        licencaExistente.qtd_pdv_incluso + dto.qtd_pdv_adicional;
    }

    if (dto.qtd_gerenciador_adicional !== undefined) {
      updateData.qtd_gerenciador_adicional = dto.qtd_gerenciador_adicional;
      updateData.qtd_gerenciador_total =
        licencaExistente.qtd_gerenciador_incluso + dto.qtd_gerenciador_adicional;
    }

    if (dto.data_validade) {
      updateData.data_validade = new Date(dto.data_validade);
    }

    if (dto.status) {
      updateData.status = dto.status;
    }

    if (dto.dias_alerta !== undefined) {
      updateData.dias_alerta = dto.dias_alerta;
    }

    const licenca = await this.licencaRepository.update(id, updateData);
    return mapLicencaToResponse(licenca);
  }

  async obterLicenca(id: number): Promise<LicencaResponseDTO> {
    const licenca = await this.licencaRepository.findById(id, {
      includeCliente: true,
      includeCount: true,
    });

    if (!licenca) {
      throw new NotFoundError('Licença');
    }

    return mapLicencaToResponse(licenca);
  }

  async listarLicencas(
    query: { [key: string]: string | undefined }
  ): Promise<PaginatedResponse<LicencaListDTO>> {
    const { page, limit, sort, order } = parsePaginationParams(query);

    const filters: LicencaFilters = {
      id_cliente: query.id_cliente ? parseInt(query.id_cliente, 10) : undefined,
      status: query.status as LicencaStatus | undefined,
      expirando_em_dias: query.expirando_em_dias
        ? parseInt(query.expirando_em_dias, 10)
        : undefined,
    };

    const skip = calculateSkip(page, limit);
    const orderBy: { [key: string]: 'asc' | 'desc' } = {};
    orderBy[sort] = order;

    const [licencas, total] = await Promise.all([
      this.licencaRepository.findAll(filters, skip, limit, orderBy),
      this.licencaRepository.count(filters),
    ]);

    const data = licencas.map(mapLicencaToListDTO);

    return createPaginatedResponse(data, total, page, limit);
  }

  async renovarLicenca(
    id: number,
    dto: LicencaRenovacaoDTO
  ): Promise<LicencaResponseDTO> {
    const licenca = await this.licencaRepository.findById(id);
    if (!licenca) {
      throw new NotFoundError('Licença');
    }

    // Calcula nova data de validade
    const dataAtual = new Date();
    const dataValidadeAtual = new Date(licenca.data_validade);
    const baseDate = dataValidadeAtual > dataAtual ? dataValidadeAtual : dataAtual;

    const novaDataValidade = new Date(baseDate);
    novaDataValidade.setMonth(
      novaDataValidade.getMonth() + dto.meses_adicionais
    );

    const updateData: any = {
      data_validade: novaDataValidade,
      status: 'ATIVA',
    };

    // Atualiza capacidade se fornecido
    if (dto.nova_qtd_pdv !== undefined) {
      updateData.qtd_pdv_total = dto.nova_qtd_pdv;
    }

    if (dto.nova_qtd_gerenciador !== undefined) {
      updateData.qtd_gerenciador_total = dto.nova_qtd_gerenciador;
    }

    const licencaAtualizada = await this.licencaRepository.update(id, updateData);
    return mapLicencaToResponse(licencaAtualizada);
  }

  async verificarLicenca(
    chaveAtivacao: string,
    identificadorMaquina: string
  ): Promise<VerificacaoLicencaResponseDTO> {
    const licenca = await this.licencaRepository.findByChaveAtivacao(
      chaveAtivacao,
      { includeCliente: true, includeTerminais: true }
    );

    if (!licenca) {
      return {
        valido: false,
        pode_operar: false,
        mensagem: 'Licença não encontrada',
      };
    }

    // Verifica se cliente está ativo
    if (!licenca.cliente?.ativo) {
      return {
        valido: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        pode_operar: false,
        mensagem: 'Cliente inativo',
      };
    }

    // Verifica status da licença
    if (licenca.status === 'CANCELADA') {
      return {
        valido: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        pode_operar: false,
        mensagem: 'Licença cancelada',
      };
    }

    if (licenca.status === 'SUSPENSA') {
      return {
        valido: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        pode_operar: false,
        mensagem: 'Licença suspensa',
      };
    }

    // Verifica validade
    const hoje = new Date();
    const dataValidade = new Date(licenca.data_validade);

    if (dataValidade < hoje) {
      // Atualiza status para EXPIRADA
      if (licenca.status !== 'EXPIRADA') {
        await this.licencaRepository.updateStatus(licenca.id_licenca, 'EXPIRADA');
      }

      return {
        valido: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: 'EXPIRADA',
          data_validade: licenca.data_validade.toISOString(),
        },
        pode_operar: false,
        mensagem: 'Licença expirada',
      };
    }

    // Verifica se terminal está autorizado
    const terminal = licenca.terminais?.find(
      (t) => t.identificador_maquina === identificadorMaquina
    );

    if (!terminal) {
      return {
        valido: true,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        pode_operar: false,
        mensagem: 'Terminal não autorizado',
      };
    }

    if (terminal.status !== 'ATIVO') {
      return {
        valido: true,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        terminal: {
          id_terminal: terminal.id_terminal,
          nome: terminal.nome,
          tipo: terminal.tipo,
          status: terminal.status,
        },
        pode_operar: false,
        mensagem: `Terminal ${terminal.status.toLowerCase()}`,
      };
    }

    return {
      valido: true,
      licenca: {
        chave_ativacao: licenca.chave_ativacao,
        status: licenca.status,
        data_validade: licenca.data_validade.toISOString(),
      },
      terminal: {
        id_terminal: terminal.id_terminal,
        nome: terminal.nome,
        tipo: terminal.tipo,
        status: terminal.status,
      },
      pode_operar: true,
      mensagem: 'Licença válida',
    };
  }

  async ativarTerminal(
    chaveAtivacao: string,
    identificadorMaquina: string,
    tipo: 'PDV' | 'GERENCIADOR',
    nome: string
  ): Promise<AtivacaoLicencaResponseDTO> {
    const licenca = await this.licencaRepository.findByChaveAtivacao(
      chaveAtivacao,
      { includeCliente: true, includeTerminais: true }
    );

    if (!licenca) {
      return {
        sucesso: false,
        mensagem: 'Licença não encontrada',
      };
    }

    // Verifica se cliente está ativo
    if (!licenca.cliente?.ativo) {
      return {
        sucesso: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        mensagem: 'Cliente inativo',
      };
    }

    // Verifica se licença está ativa
    if (licenca.status !== 'ATIVA') {
      return {
        sucesso: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        mensagem: `Licença ${licenca.status.toLowerCase()}`,
      };
    }

    // Verifica validade
    const hoje = new Date();
    const dataValidade = new Date(licenca.data_validade);

    if (dataValidade < hoje) {
      await this.licencaRepository.updateStatus(licenca.id_licenca, 'EXPIRADA');
      return {
        sucesso: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: 'EXPIRADA',
          data_validade: licenca.data_validade.toISOString(),
        },
        mensagem: 'Licença expirada',
      };
    }

    // Verifica se terminal já existe
    const terminalExistente = await this.terminalRepository.findByLicencaAndMaquina(
      licenca.id_licenca,
      identificadorMaquina
    );

    if (terminalExistente) {
      // Atualiza status e data de ativação
      await this.terminalRepository.update(terminalExistente.id_terminal, {
        status: 'ATIVO',
        data_ativacao: new Date(),
        ultimo_heartbeat: new Date(),
      });

      return {
        sucesso: true,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        terminal: {
          id_terminal: terminalExistente.id_terminal,
          nome: terminalExistente.nome,
          tipo: terminalExistente.tipo,
          status: 'ATIVO',
        },
        mensagem: 'Terminal reativado com sucesso',
      };
    }

    // Verifica limite de terminais
    const terminaisAtivosTipo = await this.terminalRepository.countByLicencaAndTipo(
      licenca.id_licenca,
      tipo
    );

    const limite =
      tipo === 'PDV'
        ? licenca.qtd_pdv_total
        : licenca.qtd_gerenciador_total;

    if (terminaisAtivosTipo >= limite) {
      return {
        sucesso: false,
        licenca: {
          chave_ativacao: licenca.chave_ativacao,
          status: licenca.status,
          data_validade: licenca.data_validade.toISOString(),
        },
        mensagem: `Limite de ${tipo.toLowerCase()}s atingido (${limite})`,
      };
    }

    // Cria novo terminal
    const novoTerminal = await this.terminalRepository.create({
      licenca: { connect: { id_licenca: licenca.id_licenca } },
      tipo,
      nome,
      identificador_maquina: identificadorMaquina,
      status: 'ATIVO',
      data_ativacao: new Date(),
      ultimo_heartbeat: new Date(),
    });

    // Atualiza data de ativação da licença se for o primeiro terminal
    if (!licenca.data_ativacao) {
      await this.licencaRepository.update(licenca.id_licenca, {
        data_ativacao: new Date(),
      });
    }

    return {
      sucesso: true,
      licenca: {
        chave_ativacao: licenca.chave_ativacao,
        status: licenca.status,
        data_validade: licenca.data_validade.toISOString(),
      },
      terminal: {
        id_terminal: novoTerminal.id_terminal,
        nome: novoTerminal.nome,
        tipo: novoTerminal.tipo,
        status: novoTerminal.status,
      },
      mensagem: 'Terminal ativado com sucesso',
    };
  }

  async heartbeat(
    chaveAtivacao: string,
    identificadorMaquina: string
  ): Promise<{ sucesso: boolean; mensagem: string }> {
    const licenca = await this.licencaRepository.findByChaveAtivacao(chaveAtivacao);

    if (!licenca) {
      return { sucesso: false, mensagem: 'Licença não encontrada' };
    }

    if (licenca.status !== 'ATIVA') {
      return { sucesso: false, mensagem: `Licença ${licenca.status.toLowerCase()}` };
    }

    const terminal = await this.terminalRepository.findByLicencaAndMaquina(
      licenca.id_licenca,
      identificadorMaquina
    );

    if (!terminal) {
      return { sucesso: false, mensagem: 'Terminal não registrado' };
    }

    if (terminal.status !== 'ATIVO') {
      return { sucesso: false, mensagem: `Terminal ${terminal.status.toLowerCase()}` };
    }

    await this.terminalRepository.updateHeartbeat(terminal.id_terminal);

    return { sucesso: true, mensagem: 'Heartbeat registrado' };
  }

  async obterLicencasExpirando(dias: number) {
    return this.licencaRepository.findExpirando(dias);
  }
}
