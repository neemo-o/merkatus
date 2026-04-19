import { Request, Response, NextFunction } from "express";
import { LicencaService } from "../services/LicencaService";
import { AuditService } from "../services/AuditService";
import { ValidationError } from "../errors/AppError";

function extractRequestIp(req: any): string | undefined {
  return (
    req.headers?.['x-forwarded-for']?.split(',')?.[0]?.trim() ||
    req.ip ||
    req.connection?.remoteAddress ||
    undefined
  );
}

export class LicencaController {
  private licencaService: LicencaService;
  private auditService: AuditService;

  constructor() {
    this.licencaService = new LicencaService();
    this.auditService = new AuditService();
  }

  criarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const dto = req.body;
      const licenca = await this.licencaService.criarLicenca(dto);

      await this.auditService.criarLog({
        id_usuario_equipe: req.user!.sub,
        acao: "Criar Licença",
        tabela: "licencas",
        id_registro: licenca.id_licenca,
        dados_depois: licenca,
        ip_address: extractRequestIp(req),
      });

      res.status(201).json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
        },
      });
    } catch (error) {
      next(error);
    }
  };

  atualizarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError("ID inválido");
      }

      const dto = req.body;
      const licencaAntes = await this.licencaService.obterLicenca(id);
      const licenca = await this.licencaService.atualizarLicenca(id, dto);

      await this.auditService.criarLog({
        id_usuario_equipe: req.user!.sub,
        acao: "Atualizar Licença",
        tabela: "licencas",
        id_registro: licenca.id_licenca,
        dados_antes: licencaAntes,
        dados_depois: licenca,
        ip_address: extractRequestIp(req),
      });

      res.json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
        },
      });
    } catch (error) {
      next(error);
    }
  };

  obterLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError("ID inválido");
      }

      const licenca = await this.licencaService.obterLicenca(id);

      res.json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
        },
      });
    } catch (error) {
      next(error);
    }
  };

  listarLicencas = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const query = req.query as { [key: string]: string | undefined };
      const result = await this.licencaService.listarLicencas(query);

      res.json({
        success: true,
        data: result.data,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
          pagination: result.meta,
        },
      });
    } catch (error) {
      next(error);
    }
  };

  renovarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError("ID inválido");
      }

      const dto = req.body;
      const licenca = await this.licencaService.renovarLicenca(id, dto);

      res.json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
        },
      });
    } catch (error) {
      next(error);
    }
  };

  deletarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError("ID inválido");
      }

      const licencaAntes = await this.licencaService.obterLicenca(id);
      await this.licencaService.deletarLicenca(id);

      await this.auditService.criarLog({
        id_usuario_equipe: req.user!.sub,
        acao: "Remover Licença",
        tabela: "licencas",
        id_registro: id,
        dados_antes: licencaAntes,
        ip_address: extractRequestIp(req),
      });

      res.status(204).send();
    } catch (error) {
      next(error);
    }
  };

  // Endpoints públicos (usados pelo ERP cliente)
  verificarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const { chave_ativacao, identificador_maquina } = req.body;

      const resultado = await this.licencaService.verificarLicenca(
        chave_ativacao,
        identificador_maquina,
      );

      res.json({
        success: true,
        data: resultado,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
        },
      });
    } catch (error) {
      next(error);
    }
  };

  ativarTerminal = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const { chave_ativacao, identificador_maquina, tipo, nome } = req.body;

      const resultado = await this.licencaService.ativarTerminal(
        chave_ativacao,
        identificador_maquina,
        tipo,
        nome,
      );

      res.json({
        success: resultado.sucesso,
        data: resultado,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
        },
      });
    } catch (error) {
      next(error);
    }
  };

  heartbeat = async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const { chave_ativacao, identificador_maquina } = req.body;

      const resultado = await this.licencaService.heartbeat(
        chave_ativacao,
        identificador_maquina,
      );

      res.json({
        success: resultado.sucesso,
        data: resultado,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: (req.headers["x-request-id"] as string) || "unknown",
        },
      });
    } catch (error) {
      next(error);
    }
  };
}
