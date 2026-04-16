import { Router } from 'express';
import { LicencaController } from '../controllers/LicencaController';
import { authMiddleware, requirePerfil } from '../middlewares/auth';
import { validateBody, validateParams, validateQuery } from '../middlewares/validate';
import {
  createLicencaSchema,
  updateLicencaSchema,
  renovarLicencaSchema,
  listLicencaSchema,
  licencaIdParamSchema,
  verificarLicencaSchema,
  ativarLicencaSchema,
  heartbeatSchema,
} from '../schemas/LicencaSchema';

const router = Router();
const controller = new LicencaController();

// Rotas públicas (usadas pelo ERP cliente)
router.post(
  '/public/verificar',
  validateBody(verificarLicencaSchema),
  controller.verificarLicenca
);

router.post(
  '/public/ativar',
  validateBody(ativarLicencaSchema),
  controller.ativarTerminal
);

router.post(
  '/public/heartbeat',
  validateBody(heartbeatSchema),
  controller.heartbeat
);

// Rotas protegidas (painel administrativo)
router.use(authMiddleware);

// Listar licenças
router.get(
  '/',
  validateQuery(listLicencaSchema),
  controller.listarLicencas
);

// Obter licença por ID
router.get(
  '/:id',
  validateParams(licencaIdParamSchema),
  controller.obterLicenca
);

// Criar licença (ADMIN e COMERCIAL)
router.post(
  '/',
  requirePerfil('ADMIN', 'COMERCIAL'),
  validateBody(createLicencaSchema),
  controller.criarLicenca
);

// Atualizar licença (ADMIN e COMERCIAL)
router.put(
  '/:id',
  requirePerfil('ADMIN', 'COMERCIAL'),
  validateParams(licencaIdParamSchema),
  validateBody(updateLicencaSchema),
  controller.atualizarLicenca
);

// Renovar licença (ADMIN e COMERCIAL)
router.post(
  '/:id/renovar',
  requirePerfil('ADMIN', 'COMERCIAL'),
  validateParams(licencaIdParamSchema),
  validateBody(renovarLicencaSchema),
  controller.renovarLicenca
);

export default router;
