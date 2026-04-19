import { api } from "./api";

const AUDIT_ENDPOINT = "/logs";

export async function listarLogs(params = {}) {
  try {
    const queryParams = new URLSearchParams();

    if (params.search) queryParams.append("search", params.search);
    if (params.acao) queryParams.append("acao", params.acao);
    if (params.date_from) queryParams.append("date_from", params.date_from);
    if (params.date_to) queryParams.append("date_to", params.date_to);
    if (params.page) queryParams.append("page", params.page.toString());
    if (params.limit) queryParams.append("limit", params.limit.toString());
    if (params.sort) queryParams.append("sort", params.sort);
    if (params.order) queryParams.append("order", params.order);

    const endpoint = queryParams.toString()
      ? `${AUDIT_ENDPOINT}?${queryParams.toString()}`
      : AUDIT_ENDPOINT;

    const response = await api.get(endpoint);

    if (response.success) {
      return {
        data: response.data || [],
        meta: response.meta || {},
      };
    }

    throw new Error("Resposta inválida do servidor");
  } catch (error) {
    if (error.data?.error?.message) {
      throw new Error(error.data.error.message);
    }
    throw error;
  }
}

export default {
  listarLogs,
};
