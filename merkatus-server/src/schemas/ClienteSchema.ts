import { z } from 'zod';
import { validarCNPJ, normalizarCNPJ } from '../utils/hash';

// Schema base para endereço
const enderecoSchema = z.object({
  logradouro: z.string().max(255).optional(),
  numero: z.string().max(10).optional(),
  complemento: z.string().max(255).optional(),
  bairro: z.string().max(100).optional(),
  cidade: z.string().max(100).optional(),
  estado: z.string().length(2).optional(),
  cep: z.string().max(10).optional(),
});

// Schema para criação de cliente
export const createClienteSchema = z.object({
  cnpj: z.string()
    .min(14, 'CNPJ deve ter pelo menos 14 caracteres')
    .max(18, 'CNPJ deve ter no máximo 18 caracteres')
    .refine((val) => validarCNPJ(val), {
      message: 'CNPJ inválido',
    })
    .transform((val) => normalizarCNPJ(val)),

  razao_social: z.string()
    .min(2, 'Razão social é obrigatória')
    .max(255, 'Razão social deve ter no máximo 255 caracteres'),

  nome_fantasia: z.string().max(255).optional(),
  inscricao_estadual: z.string().max(50).optional(),
  telefone: z.string().max(20).optional(),
  email: z.string().email('Email inválido').max(255).optional().or(z.literal('')),
  responsavel: z.string().max(100).optional(),
  ...enderecoSchema.shape,
  observacoes: z.string().optional(),
  ativo: z.boolean().default(true),
});

// Schema para atualização de cliente
export const updateClienteSchema = z.object({
  razao_social: z.string().min(2).max(255).optional(),
  nome_fantasia: z.string().max(255).optional(),
  inscricao_estadual: z.string().max(50).optional(),
  telefone: z.string().max(20).optional(),
  email: z.string().email().max(255).optional().or(z.literal('')),
  responsavel: z.string().max(100).optional(),
  ...enderecoSchema.shape,
  observacoes: z.string().optional(),
  ativo: z.boolean().optional(),
});

// Schema para busca/filtro
export const listClienteSchema = z.object({
  page: z.string().default('1'),
  limit: z.string().default('20'),
  sort: z.enum(['razao_social', 'nome_fantasia', 'cidade', 'data_cadastro']).default('razao_social'),
  order: z.enum(['asc', 'desc']).default('asc'),
  search: z.string().optional(),
  ativo: z.enum(['true', 'false']).optional(),
  cidade: z.string().optional(),
  estado: z.string().optional(),
});

// Schema para parâmetro de ID
export const clienteIdParamSchema = z.object({
  id: z.string().regex(/^\d+$/, 'ID deve ser um número'),
});

export type CreateClienteInput = z.infer<typeof createClienteSchema>;
export type UpdateClienteInput = z.infer<typeof updateClienteSchema>;
export type ListClienteQuery = z.infer<typeof listClienteSchema>;
