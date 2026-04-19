import { AuditRepository, AuditFilters } from "../repositories/AuditRepository";
import {
  parsePaginationParams,
  calculateSkip,
  createPaginatedResponse,
  PaginatedResponse,
} from "../utils/paginacao";
import { env } from "../config/env";

export interface AuditLogListDTO {
  id_log: number;
  acao: string;
  tabela?: string | null;
  id_registro?: number | null;
  dados_antes?: any;
  dados_depois?: any;
  ip_address?: string | null;
  data_acao: string;
  usuario: {
    id_usuario: number;
    nome: string;
    email: string;
  };
}

export interface AuditLogCreateDTO {
  id_usuario_equipe: number;
  acao: string;
  tabela?: string;
  id_registro?: number;
  dados_antes?: any;
  dados_depois?: any;
  ip_address?: string;
}

export class AuditService {
  private auditRepository: AuditRepository;

  constructor() {
    this.auditRepository = AuditRepository.getInstance();
  }

  async listarLogs(query: {
    [key: string]: string | undefined;
  }): Promise<PaginatedResponse<AuditLogListDTO>> {
    const { page, limit, sort, order } = parsePaginationParams(query);

    const filters: AuditFilters = {
      search: query.search,
      acao: query.acao,
      dateFrom: query.date_from ? new Date(query.date_from) : undefined,
      dateTo: query.date_to
        ? (() => {
            const date = new Date(query.date_to);
            date.setHours(23, 59, 59, 999);
            return date;
          })()
        : undefined,
    };

    const skip = calculateSkip(page, limit);
    const orderBy = { [sort]: order } as { [key: string]: "asc" | "desc" };

    const [logs, total] = await Promise.all([
      this.auditRepository.findAll(filters, skip, limit, orderBy),
      this.auditRepository.count(filters),
    ]);

    const data = logs.map((log) => ({
      id_log: Number(log.id_log),
      acao: log.acao,
      tabela: log.tabela,
      id_registro: log.id_registro,
      dados_antes: log.dados_antes,
      dados_depois: log.dados_depois,
      ip_address: log.ip_address || undefined,
      data_acao: log.data_acao.toISOString(),
      usuario: {
        id_usuario: log.usuario.id_usuario,
        nome: log.usuario.nome,
        email: log.usuario.email,
      },
    }));

    return createPaginatedResponse(data, total, page, limit);
  }

  async criarLog(data: AuditLogCreateDTO): Promise<void> {
    if (!env.ENABLE_AUDIT_LOG) {
      return;
    }

    await this.auditRepository.create({
      id_usuario_equipe: data.id_usuario_equipe,
      acao: data.acao,
      tabela: data.tabela,
      id_registro: data.id_registro,
      dados_antes: data.dados_antes || null,
      dados_depois: data.dados_depois || null,
      ip_address: data.ip_address || null,
    });
  }
}
