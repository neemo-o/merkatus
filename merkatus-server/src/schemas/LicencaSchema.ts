import { z } from "zod";

function mergeCapacidades(body: unknown) {
  if (
    body &&
    typeof body === "object" &&
    "capacidades" in body &&
    body !== null
  ) {
    const anyBody = body as Record<string, any>;
    return {
      ...anyBody,
      ...anyBody.capacidades,
    };
  }
  return body;
}

// Schema para criação de licença
export const createLicencaSchema = z.preprocess(
  mergeCapacidades,
  z.object({
    id_cliente: z.number().int().positive("ID do cliente é obrigatório"),

    // Capacidade
    qtd_pdv_incluso: z.number().int().min(0).default(1),
    qtd_gerenciador_incluso: z.number().int().min(0).default(1),
    qtd_pdv_adicional: z.number().int().min(0).default(0),
    qtd_gerenciador_adicional: z.number().int().min(0).default(0),

    // Validade (meses)
    meses_validade: z.number().int().min(1).default(12),

    // Dias de alerta antes da expiração
    dias_alerta: z.number().int().min(1).default(30),
  }),
);

// Schema para atualização de licença
export const updateLicencaSchema = z.preprocess(
  mergeCapacidades,
  z.object({
    qtd_pdv_adicional: z.number().int().min(0).optional(),
    qtd_gerenciador_adicional: z.number().int().min(0).optional(),
    data_validade: z.string().datetime().optional(),
    status: z.enum(["ATIVA", "EXPIRADA", "SUSPENSA", "CANCELADA"]).optional(),
    dias_alerta: z.number().int().min(1).optional(),
  }),
);

// Schema para renovação de licença
export const renovarLicencaSchema = z.object({
  meses_adicionais: z.number().int().min(1).default(12),
  nova_qtd_pdv: z.number().int().min(0).optional(),
  nova_qtd_gerenciador: z.number().int().min(0).optional(),
});

// Schema para busca/filtro
export const listLicencaSchema = z.object({
  page: z.string().default("1"),
  limit: z.string().default("20"),
  sort: z
    .enum(["data_cadastro", "data_validade", "status"])
    .default("data_cadastro"),
  order: z.enum(["asc", "desc"]).default("desc"),
  id_cliente: z.string().optional(),
  status: z.enum(["ATIVA", "EXPIRADA", "SUSPENSA", "CANCELADA"]).optional(),
  expirando_em_dias: z.string().optional(), // Filtro para alertas
});

// Schema para ativação de licença (público)
export const ativarLicencaSchema = z.object({
  chave_ativacao: z.string().min(10).max(100),
  identificador_maquina: z.string().min(1).max(255),
  tipo: z.enum(["PDV", "GERENCIADOR"]),
  nome: z.string().min(1).max(60),
});

// Schema para verificação de licença (público)
export const verificarLicencaSchema = z.object({
  chave_ativacao: z.string().min(10).max(100),
  identificador_maquina: z.string().min(1).max(255),
});

// Schema para heartbeat (público)
export const heartbeatSchema = z.object({
  chave_ativacao: z.string().min(10).max(100),
  identificador_maquina: z.string().min(1).max(255),
});

// Schema para parâmetro de ID
export const licencaIdParamSchema = z.object({
  id: z.string().regex(/^\d+$/, "ID deve ser um número"),
});

export type CreateLicencaInput = z.infer<typeof createLicencaSchema>;
export type UpdateLicencaInput = z.infer<typeof updateLicencaSchema>;
export type RenovarLicencaInput = z.infer<typeof renovarLicencaSchema>;
export type ListLicencaQuery = z.infer<typeof listLicencaSchema>;
export type AtivarLicencaInput = z.infer<typeof ativarLicencaSchema>;
export type VerificarLicencaInput = z.infer<typeof verificarLicencaSchema>;
export type HeartbeatInput = z.infer<typeof heartbeatSchema>;
