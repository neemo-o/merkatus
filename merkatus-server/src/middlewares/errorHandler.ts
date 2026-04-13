import { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';
import { AppError } from '../errors/AppError';
import { env } from '../config/env';
import { logger } from '../config/logger';

interface ErrorResponse {
  success: false;
  error: {
    code: string;
    message: string;
    details?: unknown;
  };
  meta: {
    timestamp: string;
    requestId: string;
    path: string;
    stack?: string;
  };
}

export function errorHandler(
  err: Error,
  req: Request,
  res: Response,
  _next: NextFunction
): void {
  const requestId = req.headers['x-request-id'] as string || 'unknown';
  const timestamp = new Date().toISOString();

  let statusCode = 500;
  let code = 'INTERNAL_ERROR';
  let message = 'Erro interno do servidor';
  let details: unknown = undefined;

  // Zod validation errors
  if (err instanceof ZodError) {
    statusCode = 400;
    code = 'VALIDATION_ERROR';
    message = 'Erro de validação dos dados';
    details = err.errors.map((e) => ({
      path: e.path.join('.'),
      message: e.message,
    }));
  }

  // App errors customizados
  else if (err instanceof AppError) {
    statusCode = err.statusCode;
    code = err.code;
    message = err.message;
    details = err.details;
  }

  // Prisma errors
  else if (err.name?.includes('Prisma')) {
    if (err.name === 'PrismaClientKnownRequestError') {
      const prismaError = err as unknown as { code: string; meta?: unknown };

      // Unique constraint violation
      if (prismaError.code === 'P2002') {
        statusCode = 409;
        code = 'DUPLICATE_ENTRY';
        message = 'Registro duplicado';
        details = prismaError.meta;
      }
      // Foreign key constraint
      else if (prismaError.code === 'P2003') {
        statusCode = 400;
        code = 'FOREIGN_KEY_VIOLATION';
        message = 'Violação de chave estrangeira';
      }
      // Record not found
      else if (prismaError.code === 'P2025') {
        statusCode = 404;
        code = 'NOT_FOUND';
        message = 'Registro não encontrado';
      }
    }
  }

  // Log do erro
  if (statusCode >= 500) {
    logger.error('Erro interno:', {
      error: err.message,
      stack: err.stack,
      path: req.path,
      method: req.method,
      requestId,
    });
  } else {
    logger.warn('Erro de requisição:', {
      statusCode,
      code,
      message,
      path: req.path,
      method: req.method,
      requestId,
    });
  }

  // Monta resposta
  const response: ErrorResponse = {
    success: false,
    error: {
      code,
      message: env.NODE_ENV === 'production' && statusCode === 500
        ? 'Erro interno do servidor'
        : message,
      details,
    },
    meta: {
      timestamp,
      requestId,
      path: req.path,
      ...(env.NODE_ENV === 'development' && { stack: err.stack }),
    },
  };

  res.status(statusCode).json(response);
}
