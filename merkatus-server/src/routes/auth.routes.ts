import { Router } from 'express';
import { AuthController } from '../controllers/AuthController';
import { validateBody } from '../middlewares/validate';
import { loginSchema, refreshTokenSchema } from '../schemas/UsuarioSchema';
import { authMiddleware } from '../middlewares/auth';

const router = Router();
const authController = new AuthController();

// POST /auth/login - Login de usuário
router.post('/login', validateBody(loginSchema), authController.login);

// POST /auth/refresh - Renova access token
router.post('/refresh', validateBody(refreshTokenSchema), authController.refresh);

// POST /auth/logout - Logout (revoga token)
router.post('/logout', authController.logout);

// GET /auth/me - Perfil do usuário logado (protegido)
router.get('/me', authMiddleware, authController.me);

export default router;
