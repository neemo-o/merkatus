import CryptoJS from 'crypto-js';
import { env } from '../config/env';

const CIPHER_KEY = env.CIPHER_KEY;

/**
 * Criptografa um texto usando AES-256
 * Usado para armazenar CNPJ de forma segura
 */
export function encrypt(text: string): string {
  return CryptoJS.AES.encrypt(text, CIPHER_KEY).toString();
}

/**
 * Descriptografa um texto cifrado
 */
export function decrypt(ciphertext: string): string {
  const bytes = CryptoJS.AES.decrypt(ciphertext, CIPHER_KEY);
  return bytes.toString(CryptoJS.enc.Utf8);
}

/**
 * Gera uma chave de ativação aleatória
 * Formato: XXXXX-XXXXX-XXXXX-XXXXX
 */
export function gerarChaveAtivacao(): string {
  const segmentos = [];
  const caracteres = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';

  for (let i = 0; i < 4; i++) {
    let segmento = '';
    for (let j = 0; j < 5; j++) {
      segmento += caracteres.charAt(Math.floor(Math.random() * caracteres.length));
    }
    segmentos.push(segmento);
  }

  return segmentos.join('-');
}

/**
 * Gera um token seguro para recuperação de senha
 */
export function gerarTokenReset(): string {
  const caracteres = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let token = '';
  for (let i = 0; i < 64; i++) {
    token += caracteres.charAt(Math.floor(Math.random() * caracteres.length));
  }
  return token;
}
