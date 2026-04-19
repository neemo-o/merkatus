import { Router } from "express";
import { AuditController } from "../controllers/AuditController";
import { authMiddleware } from "../middlewares/auth";
import { validateQuery } from "../middlewares/validate";
import { listAuditSchema } from "../schemas/AuditSchema";

const router = Router();
const controller = new AuditController();

router.use(authMiddleware);

router.get("/", validateQuery(listAuditSchema), controller.listarLogs);

export default router;
