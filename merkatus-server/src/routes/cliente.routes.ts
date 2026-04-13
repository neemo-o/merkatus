import { Router } from 'express';
import { ClienteController } from '../controllers/ClienteController';
import { authMiddleware, requirePerfil } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
import {
  createClienteSchema,
  updateClienteSchema,
  listClienteSchema,
  clienteIdParamSchema,
} from '../schemas/ClienteSchema';

const router = Router();
const controller = new ClienteController();

// Middleware de autenticação para todas as rotas
router.use(authMiddleware);

// Listar clientes (paginado)
router.get(
  '/',
  validateQuery(listClienteSchema),
  controller.listarClientes
);

// Obter cliente por ID
router.get(
  '/:id',
  validateParams(clienteIdParamSchema),
  controller.obterCliente
);

// Criar cliente (ADMIN e COMERCIAL)
router.post(
  '/',
  requirePerfil('ADMIN', 'COMERCIAL'),
  validateBody(createClienteSchema),
  controller.criarCliente
);

// Atualizar cliente (ADMIN e COMERCIAL)
router.put(
  '/:id',
  requirePerfil('ADMIN', 'COMERCIAL'),
  validateParams(clienteIdParamSchema),
  validateBody(updateClienteSchema),
  controller.atualizarCliente
);

// Desativar cliente (ADMIN apenas)
router.delete(
  '/:id',
  requirePerfil('ADMIN'),
  validateParams(clienteIdParamSchema),
  controller.desativarCliente
);

export default router;
