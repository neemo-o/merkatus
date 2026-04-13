import { Request, Response, NextFunction } from 'express';
import { ClienteService } from '../services/ClienteService';
import { ClienteCreateDTO, ClienteUpdateDTO } from '../dto/ClienteDTO';
import { ValidationError } from '../errors/AppError';

export class ClienteController {
  private clienteService: ClienteService;

  constructor() {
    this.clienteService = new ClienteService();
  }

  criarCliente = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const dto: ClienteCreateDTO = req.body;
      const cliente = await this.clienteService.criarCliente(dto);

      res.status(201).json({
        success: true,
        data: cliente,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  atualizarCliente = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError('ID inválido');
      }

      const dto: ClienteUpdateDTO = req.body;
      const cliente = await this.clienteService.atualizarCliente(id, dto);

      res.json({
        success: true,
        data: cliente,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  obterCliente = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError('ID inválido');
      }

      const cliente = await this.clienteService.obterCliente(id);

      res.json({
        success: true,
        data: cliente,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.headers['x-request-id'] as string || 'unknown',
        },
      });
    } catch (error) {
      next(error);
    }
  };

  listarClientes = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const query = req.query as { [key: string]: string | undefined };
      const result = await this.clienteService.listarClientes(query);

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

  desativarCliente = async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new ValidationError('ID inválido');
      }

      await this.clienteService.desativarCliente(id);

      res.json({
        success: true,
        data: { message: 'Cliente desativado com sucesso' },
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
