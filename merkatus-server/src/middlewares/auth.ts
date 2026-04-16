import { Request, Response, NextFunction } from 'express';
import { verifyAccessToken, extractTokenFromHeader, JWTDecoded } from '../utils/jwt';
import { AuthenticationError, AuthorizationError } from '../errors/AppError';
import { prisma } from '../config/database';

// Extende Request do Express para incluir usuário
declare global {
  namespace Express {
    interface Request {
      user?: JWTDecoded;
    }
  }
}

/**
 * Middleware de autenticação JWT
 */
export async function authMiddleware(
  req: Request,
  _res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const token = extractTokenFromHeader(req.headers.authorization);

    if (!token) {
      throw new AuthenticationError('Token não fornecido');
    }

    const decoded = verifyAccessToken(token);

    // Verifica se usuário ainda existe e está ativo
    const usuario = await prisma.usuarioEquipe.findUnique({
      where: { id_usuario: decoded.sub },
      select: { ativo: true },
    });

    if (!usuario) {
      throw new AuthenticationError('Usuário não encontrado');
    }

    if (!usuario.ativo) {
      throw new AuthenticationError('Usuário inativo');
    }

    req.user = decoded;
    next();
  } catch (error) {
    next(error);
  }
}

/**
 * Middleware de autorização por perfil
 */
export function requirePerfil(...perfis: string[]) {
  return (req: Request, _res: Response, next: NextFunction): void => {
    if (!req.user) {
      next(new AuthenticationError());
      return;
    }

    if (!perfis.includes(req.user.perfil)) {
      next(new AuthorizationError(`Requer perfil: ${perfis.join(' ou ')}`));
      return;
    }

    next();
  };
}

/**
 * Middleware opcional - adiciona usuário se token existir
 * Útil para endpoints que funcionam com ou sem autenticação
 */
export async function optionalAuthMiddleware(
  req: Request,
  _res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const token = extractTokenFromHeader(req.headers.authorization);

    if (token) {
      const decoded = verifyAccessToken(token);
      req.user = decoded;
    }

    next();
  } catch {
    // Ignora erro e continua sem usuário
    next();
  }
}
