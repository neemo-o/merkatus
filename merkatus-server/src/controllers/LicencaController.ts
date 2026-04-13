import { Request, Response, NextFunction } from 'express';
import { LicencaService } from '../services/LicencaService';
import { ValidationError } from '../errors/AppError';

export class LicencaController {
  private licencaService: LicencaService;

  constructor() {
    this.licencaService = new LicencaService();
  }

  criarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const dto = req.body;
      const licenca = await this.licencaService.criarLicenca(dto);

      res.status(201).json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  atualizarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError('ID inválido');
      }

      const dto = req.body;
      const licenca = await this.licencaService.atualizarLicenca(id, dto);

      res.json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  obterLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError('ID inválido');
      }

      const licenca = await this.licencaService.obterLicenca(id);

      res.json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  listarLicencas = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const query = req.query as { [key: string]: string | undefined };
      const result = await this.licencaService.listarLicencas(query);

      res.json({
        success: true,
        data: result.data,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
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
    next: NextFunction
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError('ID inválido');
      }

      const dto = req.body;
      const licenca = await this.licencaService.renovarLicenca(id, dto);

      res.json({
        success: true,
        data: licenca,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  // Endpoints públicos (usados pelo ERP cliente)
  verificarLicenca = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const { chave_ativacao, identificador_maquina } = req.body;

      const resultado = await this.licencaService.verificarLicenca(
        chave_ativacao,
        identificador_maquina
      );

      res.json({
        success: true,
        data: resultado,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  ativarTerminal = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const { chave_ativacao, identificador_maquina, tipo, nome } = req.body;

      const resultado = await this.licencaService.ativarTerminal(
        chave_ativacao,
        identificador_maquina,
        tipo,
        nome
      );

      res.json({
        success: resultado.sucesso,
        data: resultado,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  heartbeat = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const { chave_ativacao, identificador_maquina } = req.body;

      const resultado = await this.licencaService.heartbeat(
        chave_ativacao,
        identificador_maquina
      );

      res.json({
        success: resultado.sucesso,
        data: resultado,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };
}
