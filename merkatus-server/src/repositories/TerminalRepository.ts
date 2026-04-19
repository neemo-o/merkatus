import { prisma } from "../config/database";
import {
  TerminalAutorizado,
  TerminalStatus,
  TerminalTipo,
  Prisma,
} from "@prisma/client";

export interface TerminalFilters {
  id_licenca?: number;
  tipo?: TerminalTipo;
  status?: TerminalStatus;
}

export class TerminalRepository {
  private static instance: TerminalRepository;

  static getInstance(): TerminalRepository {
    if (!TerminalRepository.instance) {
      TerminalRepository.instance = new TerminalRepository();
    }
    return TerminalRepository.instance;
  }

  async findById(
    id: number,
    options?: {
      includeLicenca?: boolean;
    },
  ): Promise<(TerminalAutorizado & { licenca?: any }) | null> {
    const include: Prisma.TerminalAutorizadoInclude = {};

    if (options?.includeLicenca) {
      include.licenca = {
        include: {
          cliente: {
            select: {
              razao_social: true,
            },
          },
        },
      };
    }

    return prisma.terminalAutorizado.findUnique({
      where: { id_terminal: id },
      include: Object.keys(include).length > 0 ? include : undefined,
    });
  }

  async findByLicencaAndMaquina(
    idLicenca: number,
    identificadorMaquina: string,
  ): Promise<TerminalAutorizado | null> {
    return prisma.terminalAutorizado.findUnique({
      where: {
        id_licenca_identificador_maquina: {
          id_licenca: idLicenca,
          identificador_maquina: identificadorMaquina,
        },
      },
    });
  }

  async findAll(
    filters: TerminalFilters,
    skip: number,
    take: number,
    orderBy: { [key: string]: "asc" | "desc" },
  ): Promise<
    (TerminalAutorizado & {
      licenca: {
        id_licenca: number;
        chave_ativacao: string;
      };
    })[]
  > {
    const where: Prisma.TerminalAutorizadoWhereInput = {};

    if (filters.id_licenca) {
      where.id_licenca = filters.id_licenca;
    }

    if (filters.tipo) {
      where.tipo = filters.tipo;
    }

    if (filters.status) {
      where.status = filters.status;
    }

    return prisma.terminalAutorizado.findMany({
      where,
      skip,
      take,
      orderBy,
      include: {
        licenca: {
          select: {
            id_licenca: true,
            chave_ativacao: true,
          },
        },
      },
    });
  }

  async count(filters: TerminalFilters): Promise<number> {
    const where: Prisma.TerminalAutorizadoWhereInput = {};

    if (filters.id_licenca) {
      where.id_licenca = filters.id_licenca;
    }

    if (filters.tipo) {
      where.tipo = filters.tipo;
    }

    if (filters.status) {
      where.status = filters.status;
    }

    return prisma.terminalAutorizado.count({ where });
  }

  async create(
    data: Prisma.TerminalAutorizadoCreateInput,
  ): Promise<TerminalAutorizado> {
    return prisma.terminalAutorizado.create({ data });
  }

  async update(
    id: number,
    data: Prisma.TerminalAutorizadoUpdateInput,
  ): Promise<TerminalAutorizado> {
    return prisma.terminalAutorizado.update({
      where: { id_terminal: id },
      data,
    });
  }

  async updateStatus(
    id: number,
    status: TerminalStatus,
  ): Promise<TerminalAutorizado> {
    return prisma.terminalAutorizado.update({
      where: { id_terminal: id },
      data: { status },
    });
  }

  async updateHeartbeat(id: number): Promise<TerminalAutorizado> {
    return prisma.terminalAutorizado.update({
      where: { id_terminal: id },
      data: { ultimo_heartbeat: new Date() },
    });
  }

  async delete(id: number): Promise<TerminalAutorizado> {
    return prisma.terminalAutorizado.delete({
      where: { id_terminal: id },
    });
  }

  async deleteByLicencaId(idLicenca: number): Promise<Prisma.BatchPayload> {
    return prisma.terminalAutorizado.deleteMany({
      where: { id_licenca: idLicenca },
    });
  }

  async countByLicencaAndTipo(
    idLicenca: number,
    tipo: TerminalTipo,
  ): Promise<number> {
    return prisma.terminalAutorizado.count({
      where: {
        id_licenca: idLicenca,
        tipo,
        status: "ATIVO",
      },
    });
  }

  async findInativosPorTempo(minutos: number): Promise<TerminalAutorizado[]> {
    const dataLimite = new Date();
    dataLimite.setMinutes(dataLimite.getMinutes() - minutos);

    return prisma.terminalAutorizado.findMany({
      where: {
        status: "ATIVO",
        OR: [
          { ultimo_heartbeat: { lt: dataLimite } },
          { ultimo_heartbeat: null },
        ],
      },
    });
  }
}
