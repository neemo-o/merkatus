// Classe base para erros da aplicação
export class AppError extends Error {
  public readonly statusCode: number;
  public readonly code: string;
  public readonly details?: unknown;

  constructor(
    message: string,
    statusCode: number = 500,
    code: string = 'INTERNAL_ERROR',
    details?: unknown
  ) {
    super(message);
    this.statusCode = statusCode;
    this.code = code;
    this.details = details;

    Error.captureStackTrace(this, this.constructor);
  }
}

// Erro de validação
export class ValidationError extends AppError {
  constructor(message: string = 'Dados inválidos', details?: unknown) {
    super(message, 400, 'VALIDATION_ERROR', details);
  }
}

// Erro de autenticação
export class AuthenticationError extends AppError {
  constructor(message: string = 'Não autenticado') {
    super(message, 401, 'AUTHENTICATION_ERROR');
  }
}

// Erro de autorização
export class AuthorizationError extends AppError {
  constructor(message: string = 'Não autorizado') {
    super(message, 403, 'AUTHORIZATION_ERROR');
  }
}

// Recurso não encontrado
export class NotFoundError extends AppError {
  constructor(resource: string = 'Recurso') {
    super(`${resource} não encontrado`, 404, 'NOT_FOUND');
  }
}

// Conflito (ex: CNPJ duplicado)
export class ConflictError extends AppError {
  constructor(message: string = 'Conflito de dados') {
    super(message, 409, 'CONFLICT_ERROR');
  }
}

// Rate limit exceeded
export class TooManyRequestsError extends AppError {
  constructor(message: string = 'Muitas requisições') {
    super(message, 429, 'TOO_MANY_REQUESTS');
  }
}

// Erro de licença (casos específicos do domínio)
export class LicencaError extends AppError {
  constructor(message: string, code: string) {
    super(message, 400, code);
  }
}

export const ErrorCodes = {
  // Autenticação
  INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  TOKEN_EXPIRED: 'TOKEN_EXPIRED',
  TOKEN_INVALID: 'TOKEN_INVALID',

  // Licença
  LICENCA_EXPIRADA: 'LICENCA_EXPIRADA',
  LICENCA_SUSPENSA: 'LICENCA_SUSPENSA',
  LICENCA_CANCELADA: 'LICENCA_CANCELADA',
  LIMITE_TERMINAIS_EXCEDIDO: 'LIMITE_TERMINAIS_EXCEDIDO',
  TERMINAL_NAO_AUTORIZADO: 'TERMINAL_NAO_AUTORIZADO',

  // Validação
  CNPJ_INVALIDO: 'CNPJ_INVALIDO',
  CNPJ_DUPLICADO: 'CNPJ_DUPLICADO',
  EMAIL_INVALIDO: 'EMAIL_INVALIDO',
  CAMPO_OBRIGATORIO: 'CAMPO_OBRIGATORIO',

  // Negócio
  CLIENTE_INATIVO: 'CLIENTE_INATIVO',
  CHAVE_ATIVACAO_INVALIDA: 'CHAVE_ATIVACAO_INVALIDA',
} as const;
