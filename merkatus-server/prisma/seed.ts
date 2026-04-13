import { PrismaClient } from '@prisma/client';
import { hashData } from '../src/utils/hash';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Iniciando seed do banco de dados...');

  // Cria usuário admin
  const adminExists = await prisma.usuarioEquipe.findUnique({
    where: { email: 'admin@merkatus.com.br' },
  });

  if (!adminExists) {
    const senhaHash = await hashData('admin123');

    await prisma.usuarioEquipe.create({
      data: {
        nome: 'Administrador',
        email: 'admin@merkatus.com.br',
        senha_hash: senhaHash,
        perfil: 'ADMIN',
        ativo: true,
        senha_trocada: false,
      },
    });

    console.log('✅ Usuário admin criado: admin@merkatus.com.br / admin123');
  } else {
    console.log('ℹ️ Usuário admin já existe');
  }

  console.log('✅ Seed concluído!');
}

main()
  .catch((e) => {
    console.error('❌ Erro no seed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
