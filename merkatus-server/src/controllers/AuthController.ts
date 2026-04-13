import { Request, Response, NextFunction } from 'express';
import { UsuarioRepository } from '../repositories/UsuarioRepository';
import {
  generateAccessToken,
  generateRefreshToken,
  verifyRefreshToken,
} from '../utils/jwt';
import { compareHash } from '../utils/hash';
import { AuthenticationError, ValidationError } from '../errors/AppError';

export class AuthController {
  private usuarioRepository: UsuarioRepository;

  constructor() {
    this.usuarioRepository = UsuarioRepository.getInstance();
  }

  /**
   * Login de usuário
   * POST /auth/login
   */
  login = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const { email, senha } = req.body;

      // Busca usuário pelo email
      const usuario = await this.usuarioRepository.findByEmail(email);

      if (!usuario) {
        throw new AuthenticationError('Credenciais inválidas');
      }

      // Verifica se usuário está ativo
      if (!usuario.ativo) {
        throw new AuthenticationError('Usuário inativo');
      }

      // Verifica senha
      const senhaValida = await compareHash(senha, usuario.senha_hash);

      if (!senhaValida) {
        throw new AuthenticationError('Credenciais inválidas');
      }

      // Atualiza último login
      await this.usuarioRepository.updateUltimoLogin(usuario.id_usuario);

      // Gera tokens
      const payload = {
        sub: usuario.id_usuario,
        email: usuario.email,
        perfil: usuario.perfil,
      };

      const accessToken = generateAccessToken(payload);
      const refreshToken = generateRefreshToken(payload);

      res.json({
        success: true,
        data: {
          user: {
            id: usuario.id_usuario,
            nome: usuario.nome,
            email: usuario.email,
            perfil: usuario.perfil,
            senha_trocada: usuario.senha_trocada,
          },
          tokens: {
            access_token: accessToken,
            refresh_token: refreshToken,
            token_type: 'Bearer',
            expires_in: 86400, // 24 horas em segundos
          },
        },
        meta: {
          timestamp: new Date().toISOString(),
        },
      });
    } catch (error) {
      next(error);
    }
  };

  /**
   * Refresh token
   * POST /auth/refresh
   */
  refresh = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const { refresh_token } = req.body;

      if (!refresh_token) {
        throw new ValidationError('Refresh token é obrigatório');
      }

      // Verifica refresh token
      const decoded = verifyRefreshToken(refresh_token);

      // Busca usuário
      const usuario = await this.usuarioRepository.findById(decoded.sub);

      if (!usuario || !usuario.ativo) {
        throw new AuthenticationError('Token inválido');
      }

      // Gera novos tokens
      const payload = {
        sub: usuario.id_usuario,
        email: usuario.email,
        perfil: usuario.perfil,
      };

      const newAccessToken = generateAccessToken(payload);
      const newRefreshToken = generateRefreshToken(payload);

      res.json({
        success: true,
        data: {
          tokens: {
            access_token: newAccessToken,
            refresh_token: newRefreshToken,
            token_type: 'Bearer',
            expires_in: 86400,
          },
        },
        meta: {
          timestamp: new Date().toISOString(),
        },
      });
    } catch (error) {
      next(error);
    }
  };

  /**
   * Logout (revoga refresh token - stateless, apenas remove do client)
   * POST /auth/logout
   */
  logout = async (
    _req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      // Em uma implementação stateless com JWT, o logout é feito no cliente
      // removendo os tokens do storage. Aqui podemos adicionar lógica
      // de blacklist se necessário no futuro.

      res.json({
        success: true,
        data: {
          message: 'Logout realizado com sucesso',
        },
        meta: {
          timestamp: new Date().toISOString(),
        },
      });
    } catch (error) {
      next(error);
    }
  };

  /**
   * Obtém perfil do usuário logado
   * GET /auth/me
   */
  me = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const userId = req.user?.sub;

      if (!userId) {
        throw new AuthenticationError('Não autenticado');
      }

      const usuario = await this.usuarioRepository.findById(userId);

      if (!usuario || !usuario.ativo) {
        throw new AuthenticationError('Usuário não encontrado');
      }

      res.json({
        success: true,
        data: {
          user: {
            id: usuario.id_usuario,
            nome: usuario.nome,
            email: usuario.email,
            perfil: usuario.perfil,
            senha_trocada: usuario.senha_trocada,
            ultimo_login: usuario.ultimo_login,
          },
        },
        meta: {
          timestamp: new Date().toISOString(),
        },
      });
    } catch (error) {
      next(error);
    }
  };
}
