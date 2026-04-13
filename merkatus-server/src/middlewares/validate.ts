import { Request, Response, NextFunction } from 'express';
import { ZodSchema, ZodError } from 'zod';
import { ValidationError } from '../errors/AppError';

/**
 * Valida body da requisição contra schema Zod
 */
export function validateBody<T>(schema: ZodSchema<T>) {
  return async (req: Request, _res: Response, next: NextFunction): Promise<void> => {
    try {
      const validated = await schema.parseAsync(req.body);
      // Substitui body pelo resultado validado (type-safe)
      req.body = validated;
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        next(error);
      } else {
        next(new ValidationError('Erro na validação dos dados'));
      }
    }
  };
}

/**
 * Valida query params da requisição
 */
export function validateQuery<T>(schema: ZodSchema<T>) {
  return async (req: Request, _res: Response, next: NextFunction): Promise<void> => {
    try {
      const validated = await schema.parseAsync(req.query);
      req.query = validated as unknown as typeof req.query;
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        next(error);
      } else {
        next(new ValidationError('Erro na validação dos parâmetros'));
      }
    }
  };
}

/**
 * Valida URL params da requisição
 */
export function validateParams<T>(schema: ZodSchema<T>) {
  return async (req: Request, _res: Response, next: NextFunction): Promise<void> => {
    try {
      const validated = await schema.parseAsync(req.params);
      req.params = validated as typeof req.params;
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        next(error);
      } else {
        next(new ValidationError('Erro na validação dos parâmetros da URL'));
      }
    }
  };
}
