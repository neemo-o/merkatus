import { Router } from "express";
import clienteRoutes from "./cliente.routes";
import licencaRoutes from "./licenca.routes";
import logsRoutes from "./logs.routes";

const router = Router();

router.use("/clientes", clienteRoutes);
router.use("/licencas", licencaRoutes);
router.use("/logs", logsRoutes);

export default router;
