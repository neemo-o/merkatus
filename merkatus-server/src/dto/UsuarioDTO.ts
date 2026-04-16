import { UsuarioPerfil } from '@prisma/client';

// DTOs para UsuarioEquipe

export interface UsuarioResponseDTO {
  id_usuario: number;
  nome: string;
  email: string;
  perfil: UsuarioPerfil;
  ativo: boolean;
  senha_trocada: boolean;
  ultimo_login?: string;
  data_cadastro: string;
}

export interface UsuarioCreateDTO {
  nome: string;
  email: string;
  senha: string;
  perfil?: UsuarioPerfil;
  ativo?: boolean;
}

export interface UsuarioUpdateDTO {
  nome?: string;
  email?: string;
  perfil?: UsuarioPerfil;
  ativo?: boolean;
}

export interface UsuarioListDTO {
  id_usuario: number;
  nome: string;
  email: string;
  perfil: UsuarioPerfil;
  ativo: boolean;
  ultimo_login?: string;
}

export interface LoginResponseDTO {
  usuario: {
    id_usuario: number;
    nome: string;
    email: string;
    perfil: UsuarioPerfil;
  };
  tokens: {
    access_token: string;
    refresh_token: string;
    expires_in: string;
  };
}

export interface ChangePasswordDTO {
  senha_atual: string;
  nova_senha: string;
}

// Mappers
export function mapUsuarioToResponse(usuario: any): UsuarioResponseDTO {
  return {
    id_usuario: usuario.id_usuario,
    nome: usuario.nome,
    email: usuario.email,
    perfil: usuario.perfil,
    ativo: usuario.ativo,
    senha_trocada: usuario.senha_trocada,
    ultimo_login: usuario.ultimo_login?.toISOString(),
    data_cadastro: usuario.data_cadastro.toISOString(),
  };
}

export function mapUsuarioToListDTO(usuario: any): UsuarioListDTO {
  return {
    id_usuario: usuario.id_usuario,
    nome: usuario.nome,
    email: usuario.email,
    perfil: usuario.perfil,
    ativo: usuario.ativo,
    ultimo_login: usuario.ultimo_login?.toISOString(),
  };
}

export function mapUsuarioToLoginResponse(
  usuario: any,
  accessToken: string,
  refreshToken: string,
  expiresIn: string
): LoginResponseDTO {
  return {
    usuario: {
      id_usuario: usuario.id_usuario,
      nome: usuario.nome,
      email: usuario.email,
      perfil: usuario.perfil,
    },
    tokens: {
      access_token: accessToken,
      refresh_token: refreshToken,
      expires_in: expiresIn,
    },
  };
}
