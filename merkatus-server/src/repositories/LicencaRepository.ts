import { prisma } from '../config/database';
import { Licenca, LicencaStatus, Prisma } from '@prisma/client';

export interface LicencaFilters {
  id_cliente?: number;
  status?: LicencaStatus;
  expirando_em_dias?: number;
}

export class LicencaRepository {
  private static instance: LicencaRepository;

  static getInstance(): LicencaRepository {
    if (!LicencaRepository.instance) {
      LicencaRepository.instance = new LicencaRepository();
    }
    return LicencaRepository.instance;
  }

  async findById(
    id: number,
    options?: {
      includeCliente?: boolean;
      includeTerminais?: boolean;
      includeCount?: boolean;
    }
  ): Promise<(Licenca & {
    cliente?: any;
    terminais?: any[];
    _count?: { terminais: number };
  }) | null> {
    const include: Prisma.LicencaInclude = {};

    if (options?.includeCliente) {
      include.cliente = {
        select: {
          id_cliente: true,
          razao_social: true,
          cnpj: true,
        },
      };
    }

    if (options?.includeTerminais) {
      include.terminais = {
        select: {
          id_terminal: true,
          nome: true,
          tipo: true,
          status: true,
        },
      };
    }

    if (options?.includeCount) {
      include._count = { select: { terminais: true } };
    }

    return prisma.licenca.findUnique({
      where: { id_licenca: id },
      include: Object.keys(include).length > 0 ? include : undefined,
    });
  }

  async findByChaveAtivacao(
    chave: string,
    options?: {
      includeCliente?: boolean;
      includeTerminais?: boolean;
    }
  ): Promise<(Licenca & {
    cliente?: any;
    terminais?: any[];
  }) | null> {
    const include: Prisma.LicencaInclude = {};

    if (options?.includeCliente) {
      include.cliente = {
        select: {
          id_cliente: true,
          razao_social: true,
          cnpj: true,
          ativo: true,
        },
      };
    }

    if (options?.includeTerminais) {
      include.terminais = true;
    }

    return prisma.licenca.findUnique({
      where: { chave_ativacao: chave },
      include: Object.keys(include).length > 0 ? include : undefined,
    });
  }

  async findAll(
    filters: LicencaFilters,
    skip: number,
    take: number,
    orderBy: { [key: string]: 'asc' | 'desc' }
  ): Promise<(Licenca & {
    cliente: {
      id_cliente: number;
      razao_social: string;
      cnpj: string;
    };
  })[]> {
    const where: Prisma.LicencaWhereInput = {};

    if (filters.id_cliente) {
      where.id_cliente = filters.id_cliente;
    }

    if (filters.status) {
      where.status = filters.status;
    }

    if (filters.expirando_em_dias !== undefined) {
      const dataLimite = new Date();
      dataLimite.setDate(dataLimite.getDate() + filters.expirando_em_dias);
      where.data_validade = { lte: dataLimite };
      where.status = 'ATIVA';
    }

    return prisma.licenca.findMany({
      where,
      skip,
      take,
      orderBy,
      include: {
        cliente: {
          select: {
            id_cliente: true,
            razao_social: true,
            cnpj: true,
          },
        },
      },
    });
  }

  async count(filters: LicencaFilters): Promise<number> {
    const where: Prisma.LicencaWhereInput = {};

    if (filters.id_cliente) {
      where.id_cliente = filters.id_cliente;
    }

    if (filters.status) {
      where.status = filters.status;
    }

    if (filters.expirando_em_dias !== undefined) {
      const dataLimite = new Date();
      dataLimite.setDate(dataLimite.getDate() + filters.expirando_em_dias);
      where.data_validade = { lte: dataLimite };
      where.status = 'ATIVA';
    }

    return prisma.licenca.count({ where });
  }

  async create(data: Prisma.LicencaCreateInput): Promise<Licenca> {
    return prisma.licenca.create({ data });
  }

  async update(
    id: number,
    data: Prisma.LicencaUpdateInput
  ): Promise<Licenca> {
    return prisma.licenca.update({
      where: { id_licenca: id },
      data,
    });
  }

  async updateStatus(id: number, status: LicencaStatus): Promise<Licenca> {
    return prisma.licenca.update({
      where: { id_licenca: id },
      data: { status },
    });
  }

  async updateDataValidade(
    id: number,
    novaData: Date
  ): Promise<Licenca> {
    return prisma.licenca.update({
      where: { id_licenca: id },
      data: { data_validade: novaData },
    });
  }

  async delete(id: number): Promise<Licenca> {
    return prisma.licenca.delete({
      where: { id_licenca: id },
    });
  }

  async existsByChave(chave: string): Promise<boolean> {
    const count = await prisma.licenca.count({
      where: { chave_ativacao: chave },
    });
    return count > 0;
  }

  async countTerminaisAtivos(id: number): Promise<number> {
    return prisma.terminalAutorizado.count({
      where: {
        id_licenca: id,
        status: 'ATIVO',
      },
    });
  }

  async findExpirando(
    dias: number
  ): Promise<(Licenca & { cliente: { razao_social: string; email: string | null } })[]> {
    const dataLimite = new Date();
    dataLimite.setDate(dataLimite.getDate() + dias);

    return prisma.licenca.findMany({
      where: {
        status: 'ATIVA',
        data_validade: { lte: dataLimite },
      },
      include: {
        cliente: {
          select: {
            razao_social: true,
            email: true,
          },
        },
      },
      orderBy: { data_validade: 'asc' },
    });
  }
}
