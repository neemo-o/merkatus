import bcrypt from 'bcrypt';
import { env } from '../config/env';

const SALT_ROUNDS = env.BCRYPT_ROUNDS;

/**
 * Gera hash bcrypt para senhas ou dados sensíveis
 */
export async function hashData(data: string): Promise<string> {
  return bcrypt.hash(data, SALT_ROUNDS);
}

/**
 * Compara dado com hash bcrypt
 */
export async function compareHash(data: string, hash: string): Promise<boolean> {
  return bcrypt.compare(data, hash);
}

/**
 * Normaliza CNPJ (remove caracteres não numéricos)
 */
export function normalizarCNPJ(cnpj: string): string {
  return cnpj.replace(/\D/g, '');
}

/**
 * Formata CNPJ para exibição (XX.XXX.XXX/XXXX-XX)
 */
export function formatarCNPJ(cnpj: string): string {
  const numeros = normalizarCNPJ(cnpj);
  return numeros.replace(
    /(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/,
    '$1.$2.$3/$4-$5'
  );
}

/**
 * Valida se um CNPJ é válido (algoritmo de validação)
 */
export function validarCNPJ(cnpj: string): boolean {
  const numeros = normalizarCNPJ(cnpj);

  if (numeros.length !== 14) return false;

  // Elimina CNPJs inválidos conhecidos (todos iguais)
  if (/^(\d)\1{13}$/.test(numeros)) return false;

  // Validação do primeiro dígito verificador
  let tamanho = numeros.length - 2;
  let numerosBase = numeros.substring(0, tamanho);
  let digitos = numeros.substring(tamanho);
  let soma = 0;
  let pos = tamanho - 7;

  for (let i = tamanho; i >= 1; i--) {
    soma += parseInt(numerosBase.charAt(tamanho - i)) * pos--;
    if (pos < 2) pos = 9;
  }

  let resultado = soma % 11 < 2 ? 0 : 11 - (soma % 11);
  if (resultado !== parseInt(digitos.charAt(0))) return false;

  // Validação do segundo dígito verificador
  tamanho = tamanho + 1;
  numerosBase = numeros.substring(0, tamanho);
  soma = 0;
  pos = tamanho - 7;

  for (let i = tamanho; i >= 1; i--) {
    soma += parseInt(numerosBase.charAt(tamanho - i)) * pos--;
    if (pos < 2) pos = 9;
  }

  resultado = soma % 11 < 2 ? 0 : 11 - (soma % 11);
  if (resultado !== parseInt(digitos.charAt(1))) return false;

  return true;
}
