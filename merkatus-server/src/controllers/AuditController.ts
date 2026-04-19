import { Request, Response, NextFunction } from "express";
import { AuditService } from "../services/AuditService";

export class AuditController {
  private auditService: AuditService;

  constructor() {
    this.auditService = new AuditService();
  }

  listarLogs = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const result = await this.auditService.listarLogs(
        req.query as { [key: string]: string | undefined },
      );

      res.json({
        success: true,
        data: result.data,
        meta: {
          timestamp: new Date().toISOString(),
          pagination: result.meta,
        },
      });
    } catch (error) {
      next(error);
    }
  };
}
