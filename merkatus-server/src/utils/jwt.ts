import jwt from 'jsonwebtoken';
import { env } from '../config/env';

export interface JWTPayload {
  sub: number;      // id_usuario
  email: string;
  perfil: string;
}

export interface JWTDecoded extends JWTPayload {
  iat: number;
  exp: number;
}

/**
 * Gera token JWT de acesso
 */
export function generateAccessToken(payload: JWTPayload): string {
  return jwt.sign(payload, env.JWT_SECRET, {
    expiresIn: env.JWT_EXPIRES_IN as jwt.SignOptions['expiresIn'],
  });
}

/**
 * Gera token de refresh
 */
export function generateRefreshToken(payload: JWTPayload): string {
  return jwt.sign({ sub: payload.sub }, env.JWT_REFRESH_SECRET, {
    expiresIn: env.JWT_REFRESH_EXPIRES_IN as jwt.SignOptions['expiresIn'],
  });
}

/**
 * Verifica e decodifica token JWT
 */
export function verifyAccessToken(token: string): JWTDecoded {
  return jwt.verify(token, env.JWT_SECRET) as unknown as JWTDecoded;
}

/**
 * Verifica token de refresh
 */
export function verifyRefreshToken(token: string): { sub: number; iat: number; exp: number } {
  return jwt.verify(token, env.JWT_REFRESH_SECRET) as unknown as { sub: number; iat: number; exp: number };
}

/**
 * Extrai token do header Authorization
 */
export function extractTokenFromHeader(authHeader: string | undefined): string | null {
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return null;
  }
  return authHeader.substring(7);
}
