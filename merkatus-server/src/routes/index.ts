import { Router } from 'express';
import clienteRoutes from './cliente.routes';
import licencaRoutes from './licenca.routes';

const router = Router();

router.use('/clientes', clienteRoutes);
router.use('/licencas', licencaRoutes);

export default router;
