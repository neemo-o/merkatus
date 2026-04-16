import { z } from 'zod';

// Schema para criação de terminal
export const createTerminalSchema = z.object({
  id_licenca: z.number().int().positive('ID da licença é obrigatório'),
  tipo: z.enum(['PDV', 'GERENCIADOR']),
  nome: z.string().min(1).max(60),
  identificador_maquina: z.string().min(1).max(255),
});

// Schema para atualização de terminal
export const updateTerminalSchema = z.object({
  nome: z.string().min(1).max(60).optional(),
  status: z.enum(['ATIVO', 'INATIVO', 'BLOQUEADO']).optional(),
});

// Schema para busca/filtro
export const listTerminalSchema = z.object({
  page: z.string().default('1'),
  limit: z.string().default('20'),
  sort: z.enum(['nome', 'tipo', 'status', 'data_cadastro']).default('nome'),
  order: z.enum(['asc', 'desc']).default('asc'),
  id_licenca: z.string().optional(),
  tipo: z.enum(['PDV', 'GERENCIADOR']).optional(),
  status: z.enum(['ATIVO', 'INATIVO', 'BLOQUEADO']).optional(),
});

// Schema para heartbeat
export const terminalHeartbeatSchema = z.object({
  identificador_maquina: z.string().min(1).max(255),
});

// Schema para parâmetro de ID
export const terminalIdParamSchema = z.object({
  id: z.string().regex(/^\d+$/, 'ID deve ser um número'),
});

export type CreateTerminalInput = z.infer<typeof createTerminalSchema>;
export type UpdateTerminalInput = z.infer<typeof updateTerminalSchema>;
export type ListTerminalQuery = z.infer<typeof listTerminalSchema>;
