import { z } from "zod";

export const listAuditSchema = z.object({
  page: z.string().default("1"),
  limit: z.string().default("20"),
  sort: z.enum(["data_acao", "acao", "tabela"]).default("data_acao"),
  order: z.enum(["asc", "desc"]).default("desc"),
  search: z.string().optional(),
  acao: z.string().optional(),
  date_from: z.string().optional(),
  date_to: z.string().optional(),
});

export type ListAuditQuery = z.infer<typeof listAuditSchema>;
