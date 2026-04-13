import { z } from 'zod';

// Schema para criação de usuário
export const createUsuarioSchema = z.object({
  nome: z.string()
    .min(2, 'Nome deve ter pelo menos 2 caracteres')
    .max(100, 'Nome deve ter no máximo 100 caracteres'),

  email: z.string()
    .email('Email inválido')
    .max(120, 'Email deve ter no máximo 120 caracteres'),

  senha: z.string()
    .min(8, 'Senha deve ter pelo menos 8 caracteres')
    .max(100, 'Senha deve ter no máximo 100 caracteres')
    .regex(/[A-Z]/, 'Senha deve conter pelo menos uma letra maiúscula')
    .regex(/[a-z]/, 'Senha deve conter pelo menos uma letra minúscula')
    .regex(/[0-9]/, 'Senha deve conter pelo menos um número')
    .regex(/[^A-Za-z0-9]/, 'Senha deve conter pelo menos um caractere especial'),

  perfil: z.enum(['ADMIN', 'SUPORTE', 'COMERCIAL']).default('SUPORTE'),

  ativo: z.boolean().default(true),
});

// Schema para atualização de usuário
export const updateUsuarioSchema = z.object({
  nome: z.string().min(2).max(100).optional(),
  email: z.string().email().max(120).optional(),
  perfil: z.enum(['ADMIN', 'SUPORTE', 'COMERCIAL']).optional(),
  ativo: z.boolean().optional(),
});

// Schema para alteração de senha
export const changePasswordSchema = z.object({
  senha_atual: z.string().min(1, 'Senha atual é obrigatória'),
  nova_senha: z.string()
    .min(8, 'Nova senha deve ter pelo menos 8 caracteres')
    .regex(/[A-Z]/, 'Nova senha deve conter pelo menos uma letra maiúscula')
    .regex(/[a-z]/, 'Nova senha deve conter pelo menos uma letra minúscula')
    .regex(/[0-9]/, 'Nova senha deve conter pelo menos um número')
    .regex(/[^A-Za-z0-9]/, 'Nova senha deve conter pelo menos um caractere especial'),
});

// Schema para login
export const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  senha: z.string().min(1, 'Senha é obrigatória'),
});

// Schema para refresh token
export const refreshTokenSchema = z.object({
  refresh_token: z.string().min(1, 'Refresh token é obrigatório'),
});

// Schema para busca/filtro
export const listUsuarioSchema = z.object({
  page: z.string().default('1'),
  limit: z.string().default('20'),
  sort: z.enum(['nome', 'email', 'perfil', 'data_cadastro']).default('nome'),
  order: z.enum(['asc', 'desc']).default('asc'),
  perfil: z.enum(['ADMIN', 'SUPORTE', 'COMERCIAL']).optional(),
  ativo: z.enum(['true', 'false']).optional(),
});

// Schema para parâmetro de ID
export const usuarioIdParamSchema = z.object({
  id: z.string().regex(/^\d+$/, 'ID deve ser um número'),
});

export type CreateUsuarioInput = z.infer<typeof createUsuarioSchema>;
export type UpdateUsuarioInput = z.infer<typeof updateUsuarioSchema>;
export type ChangePasswordInput = z.infer<typeof changePasswordSchema>;
export type LoginInput = z.infer<typeof loginSchema>;
export type RefreshTokenInput = z.infer<typeof refreshTokenSchema>;
export type ListUsuarioQuery = z.infer<typeof listUsuarioSchema>;
