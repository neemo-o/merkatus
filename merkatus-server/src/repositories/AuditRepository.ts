import { prisma } from "../config/database";
import { LogAcaoEquipe, Prisma } from "@prisma/client";

export interface AuditFilters {
  search?: string;
  acao?: string;
  dateFrom?: Date;
  dateTo?: Date;
}

const buildAuditWhere = (
  filters: AuditFilters,
): Prisma.LogAcaoEquipeWhereInput => {
  const where: Prisma.LogAcaoEquipeWhereInput = {};

  if (filters.search) {
    where.OR = [
      {
        usuario: {
          nome: {
            contains: filters.search,
            mode: "insensitive" as const,
          },
        },
      },
      {
        tabela: {
          contains: filters.search,
          mode: "insensitive" as const,
        },
      },
      ...(isNaN(Number(filters.search))
        ? []
        : [
            {
              id_registro: Number(filters.search),
            },
          ]),
    ];
  }

  if (filters.acao) {
    const action = filters.acao.toUpperCase().trim();

    if (action === "CREATE") {
      where.acao = {
        contains: "Criar",
        mode: "insensitive" as const,
      };
    } else if (action === "UPDATE") {
      where.acao = {
        contains: "Atualiz",
        mode: "insensitive" as const,
      };
    } else if (action === "DELETE") {
      where.acao = {
        contains: "Remov",
        mode: "insensitive" as const,
      };
    }
  }

  if (filters.dateFrom || filters.dateTo) {
    where.data_acao = {};
    if (filters.dateFrom) {
      where.data_acao.gte = filters.dateFrom;
    }
    if (filters.dateTo) {
      where.data_acao.lte = filters.dateTo;
    }
  }

  return where;
};

export class AuditRepository {
  private static instance: AuditRepository;

  static getInstance(): AuditRepository {
    if (!AuditRepository.instance) {
      AuditRepository.instance = new AuditRepository();
    }
    return AuditRepository.instance;
  }

  async findAll(
    filters: AuditFilters,
    skip: number,
    take: number,
    orderBy: { [key: string]: "asc" | "desc" },
  ): Promise<
    (LogAcaoEquipe & {
      usuario: { id_usuario: number; nome: string; email: string };
    })[]
  > {
    const where = buildAuditWhere(filters);

    return prisma.logAcaoEquipe.findMany({
      where,
      skip,
      take,
      orderBy: orderBy as Prisma.LogAcaoEquipeOrderByWithRelationInput,
      include: {
        usuario: {
          select: {
            id_usuario: true,
            nome: true,
            email: true,
          },
        },
      },
    });
  }

  async count(filters: AuditFilters): Promise<number> {
    const where = buildAuditWhere(filters);

    return prisma.logAcaoEquipe.count({ where });
  }

  async create(
    data: Prisma.LogAcaoEquipeUncheckedCreateInput,
  ): Promise<LogAcaoEquipe> {
    return prisma.logAcaoEquipe.create({ data });
  }
}
