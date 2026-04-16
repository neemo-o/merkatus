import { prisma } from '../config/database';
import { ClienteLicenciado } from '@prisma/client';
import { Prisma } from '@prisma/client';

export interface ClienteFilters {
  search?: string;
  ativo?: boolean;
  cidade?: string;
  estado?: string;
}

export class ClienteRepository {
  private static instance: ClienteRepository;

  static getInstance(): ClienteRepository {
    if (!ClienteRepository.instance) {
      ClienteRepository.instance = new ClienteRepository();
    }
    return ClienteRepository.instance;
  }

  async findById(
    id: number,
    options?: {
      includeLicencas?: boolean;
      includeCount?: boolean;
    }
  ): Promise<(ClienteLicenciado & {
    licencas?: any[];
    _count?: { licencas: number };
  }) | null> {
    const include: Prisma.ClienteLicenciadoInclude = {};

    if (options?.includeLicencas) {
      include.licencas = {
        select: {
          id_licenca: true,
          chave_ativacao: true,
          status: true,
          data_validade: true,
        },
      };
    }

    if (options?.includeCount) {
      include._count = { select: { licencas: true } };
    }

    return prisma.clienteLicenciado.findUnique({
      where: { id_cliente: id },
      include: Object.keys(include).length > 0 ? include : undefined,
    });
  }

  async findByCNPJHash(cnpjHash: string): Promise<ClienteLicenciado | null> {
    return prisma.clienteLicenciado.findUnique({
      where: { cnpj_hash: cnpjHash },
    });
  }

  async findAll(
    filters: ClienteFilters,
    skip: number,
    take: number,
    orderBy: { [key: string]: 'asc' | 'desc' }
  ): Promise<(ClienteLicenciado & { _count?: { licencas: number } })[]> {
    const where: Prisma.ClienteLicenciadoWhereInput = {};

    if (filters.ativo !== undefined) {
      where.ativo = filters.ativo;
    }

    if (filters.cidade) {
      where.cidade = { contains: filters.cidade, mode: 'insensitive' };
    }

    if (filters.estado) {
      where.estado = filters.estado.toUpperCase();
    }

    if (filters.search) {
      where.OR = [
        { razao_social: { contains: filters.search, mode: 'insensitive' } },
        { nome_fantasia: { contains: filters.search, mode: 'insensitive' } },
        { responsavel: { contains: filters.search, mode: 'insensitive' } },
      ];
    }

    return prisma.clienteLicenciado.findMany({
      where,
      skip,
      take,
      orderBy,
      include: {
        _count: { select: { licencas: true } },
      },
    });
  }

  async count(filters: ClienteFilters): Promise<number> {
    const where: Prisma.ClienteLicenciadoWhereInput = {};

    if (filters.ativo !== undefined) {
      where.ativo = filters.ativo;
    }

    if (filters.cidade) {
      where.cidade = { contains: filters.cidade, mode: 'insensitive' };
    }

    if (filters.estado) {
      where.estado = filters.estado.toUpperCase();
    }

    if (filters.search) {
      where.OR = [
        { razao_social: { contains: filters.search, mode: 'insensitive' } },
        { nome_fantasia: { contains: filters.search, mode: 'insensitive' } },
        { responsavel: { contains: filters.search, mode: 'insensitive' } },
      ];
    }

    return prisma.clienteLicenciado.count({ where });
  }

  async create(
    data: Prisma.ClienteLicenciadoCreateInput
  ): Promise<ClienteLicenciado> {
    return prisma.clienteLicenciado.create({ data });
  }

  async update(
    id: number,
    data: Prisma.ClienteLicenciadoUpdateInput
  ): Promise<ClienteLicenciado> {
    return prisma.clienteLicenciado.update({
      where: { id_cliente: id },
      data,
    });
  }

  async softDelete(id: number): Promise<ClienteLicenciado> {
    return prisma.clienteLicenciado.update({
      where: { id_cliente: id },
      data: { ativo: false },
    });
  }

  async delete(id: number): Promise<ClienteLicenciado> {
    return prisma.clienteLicenciado.delete({
      where: { id_cliente: id },
    });
  }

  async existsByCNPJHash(cnpjHash: string): Promise<boolean> {
    const count = await prisma.clienteLicenciado.count({
      where: { cnpj_hash: cnpjHash },
    });
    return count > 0;
  }

  async hasLicencasAtivas(id: number): Promise<boolean> {
    const count = await prisma.licenca.count({
      where: {
        id_cliente: id,
        status: 'ATIVA',
      },
    });
    return count > 0;
  }
}
