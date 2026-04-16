import { prisma } from '../config/database';
import { UsuarioEquipe, UsuarioPerfil, Prisma } from '@prisma/client';

export interface UsuarioFilters {
  perfil?: UsuarioPerfil;
  ativo?: boolean;
}

export class UsuarioRepository {
  private static instance: UsuarioRepository;

  static getInstance(): UsuarioRepository {
    if (!UsuarioRepository.instance) {
      UsuarioRepository.instance = new UsuarioRepository();
    }
    return UsuarioRepository.instance;
  }

  async findById(id: number): Promise<UsuarioEquipe | null> {
    return prisma.usuarioEquipe.findUnique({
      where: { id_usuario: id },
    });
  }

  async findByEmail(email: string): Promise<UsuarioEquipe | null> {
    return prisma.usuarioEquipe.findUnique({
      where: { email: email.toLowerCase() },
    });
  }

  async findByTokenReset(token: string): Promise<UsuarioEquipe | null> {
    return prisma.usuarioEquipe.findFirst({
      where: { token_reset: token },
    });
  }

  async findAll(
    filters: UsuarioFilters,
    skip: number,
    take: number,
    orderBy: { [key: string]: 'asc' | 'desc' }
  ): Promise<UsuarioEquipe[]> {
    const where: Prisma.UsuarioEquipeWhereInput = {};

    if (filters.perfil) {
      where.perfil = filters.perfil;
    }

    if (filters.ativo !== undefined) {
      where.ativo = filters.ativo;
    }

    return prisma.usuarioEquipe.findMany({
      where,
      skip,
      take,
      orderBy,
    });
  }

  async count(filters: UsuarioFilters): Promise<number> {
    const where: Prisma.UsuarioEquipeWhereInput = {};

    if (filters.perfil) {
      where.perfil = filters.perfil;
    }

    if (filters.ativo !== undefined) {
      where.ativo = filters.ativo;
    }

    return prisma.usuarioEquipe.count({ where });
  }

  async create(
    data: Prisma.UsuarioEquipeCreateInput
  ): Promise<UsuarioEquipe> {
    return prisma.usuarioEquipe.create({ data });
  }

  async update(
    id: number,
    data: Prisma.UsuarioEquipeUpdateInput
  ): Promise<UsuarioEquipe> {
    return prisma.usuarioEquipe.update({
      where: { id_usuario: id },
      data,
    });
  }

  async updateSenha(
    id: number,
    senhaHash: string
  ): Promise<UsuarioEquipe> {
    return prisma.usuarioEquipe.update({
      where: { id_usuario: id },
      data: {
        senha_hash: senhaHash,
        senha_trocada: true,
      },
    });
  }

  async updateUltimoLogin(id: number): Promise<UsuarioEquipe> {
    return prisma.usuarioEquipe.update({
      where: { id_usuario: id },
      data: { ultimo_login: new Date() },
    });
  }

  async setTokenReset(
    id: number,
    token: string
  ): Promise<UsuarioEquipe> {
    return prisma.usuarioEquipe.update({
      where: { id_usuario: id },
      data: { token_reset: token },
    });
  }

  async clearTokenReset(id: number): Promise<UsuarioEquipe> {
    return prisma.usuarioEquipe.update({
      where: { id_usuario: id },
      data: { token_reset: null },
    });
  }

  async delete(id: number): Promise<UsuarioEquipe> {
    return prisma.usuarioEquipe.delete({
      where: { id_usuario: id },
    });
  }

  async existsByEmail(email: string): Promise<boolean> {
    const count = await prisma.usuarioEquipe.count({
      where: { email: email.toLowerCase() },
    });
    return count > 0;
  }

  async isAdmin(id: number): Promise<boolean> {
    const usuario = await prisma.usuarioEquipe.findUnique({
      where: { id_usuario: id },
      select: { perfil: true },
    });
    return usuario?.perfil === 'ADMIN';
  }
}
