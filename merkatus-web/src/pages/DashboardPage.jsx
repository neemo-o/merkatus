import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { contarClientes } from "../services/clienteService";
import {
  contarLicencas,
  listarLicencasRecentes,
} from "../services/licencaService";
// import { contarTerminais } from '../services/terminalService'

import Button from "../components/ui/Button";
import Card from "../components/ui/Card";
import LoadingSpinner from "../components/ui/LoadingSpinner";
import DashboardLayout from "../components/DashboardLayout";
export default function DashboardPage() {
  const { user, isAuthenticated, logout, loading, initialized } = useAuth();
  const navigate = useNavigate();
  const [dashboardData, setDashboardData] = useState({
    clientes: 0,
    licencas: 0,
    terminais: 0,
    recentes: [],
  });
  const [dashboardLoading, setDashboardLoading] = useState(true);

  // Redireciona para login se não estiver autenticado
  useEffect(() => {
    if (initialized && !isAuthenticated && !loading) {
      navigate("/login");
    }
  }, [initialized, isAuthenticated, loading, navigate]);

  // Carrega dados do dashboard
  useEffect(() => {
    if (isAuthenticated) {
      loadDashboardData();
    }
  }, [isAuthenticated]);

  const loadDashboardData = async () => {
    try {
      setDashboardLoading(true);
      const [clientesCount, licencasCount, terminaisCount, recentes] =
        await Promise.all([
          contarClientes(),
          contarLicencas(),
          0,
          listarLicencasRecentes(),
        ]);

      setDashboardData({
        clientes: clientesCount,
        licencas: licencasCount,
        terminais: terminaisCount,
        recentes,
      });
    } catch (error) {
      console.error("Erro ao carregar dados do dashboard:", error);
    } finally {
      setDashboardLoading(false);
    }
  };

  // Mostra loading enquanto inicializa
  if (!initialized || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--bg)]">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-[var(--accent)]"></div>
      </div>
    );
  }

  // Não renderiza nada se não estiver autenticado (vai redirecionar)
  if (!isAuthenticated) {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-heading font-bold">Dashboard</h1>
        <div className="text-lg text-[var(--muted)]">
          Visão geral do sistema
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <Card>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-mono text-[var(--muted)]">CLIENTES</p>
              {dashboardLoading ? (
                <LoadingSpinner className="mt-2" />
              ) : (
                <p className="text-3xl font-bold text-[var(--text)]">
                  {dashboardData.clientes}
                </p>
              )}
            </div>
            <div className="w-12 h-12 rounded-lg flex items-center justify-center bg-blue-500/10">
              <svg
                className="w-6 h-6 text-blue-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
                />
              </svg>
            </div>
          </div>
          <Link
            to="/dashboard/clientes"
            className="text-sm text-[var(--accent)] hover:text-[var(--accent-hover)] mt-4 inline-block"
          >
            Ver todos →
          </Link>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-mono text-[var(--muted)]">LICENÇAS</p>
              {dashboardLoading ? (
                <LoadingSpinner className="mt-2" />
              ) : (
                <p className="text-3xl font-bold text-[var(--text)]">
                  {dashboardData.licencas}
                </p>
              )}
            </div>
            <div className="w-12 h-12 rounded-lg flex items-center justify-center bg-green-500/10">
              <svg
                className="w-6 h-6 text-green-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          </div>
          <Link
            to="/dashboard/licencas"
            className="text-sm text-[var(--accent)] hover:text-[var(--accent-hover)] mt-4 inline-block"
          >
            Ver todas →
          </Link>
        </Card>
      </div>

      {/* Recent Activity */}
      <Card
        title="Atividade Recente"
        headerAction={
          <Link to="/dashboard/licencas">
            <Button variant="secondary" size="sm">
              Ver Todas
            </Button>
          </Link>
        }
      >
        {dashboardLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div
                key={i}
                className="h-16 bg-[var(--surface)] border border-[var(--border)] rounded-lg animate-pulse"
              ></div>
            ))}
          </div>
        ) : dashboardData.recentes.length > 0 ? (
          <div className="space-y-4">
            {dashboardData.recentes.map((licenca) => (
              <div
                key={licenca.id_licenca}
                className="flex items-center justify-between p-4 border border-[var(--border)] rounded-lg"
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg flex items-center justify-center bg-[var(--accent)]/10">
                    <svg
                      className="w-5 h-5 text-[var(--accent)]"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p className="font-medium text-[var(--text)]">
                      Licença {licenca.chave_ativacao}
                    </p>
                    <p className="text-sm text-[var(--muted)]">
                      {licenca.cliente?.razao_social ||
                        "Cliente não encontrado"}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <span
                    className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${
                      licenca.status === "ATIVA"
                        ? "bg-green-100 text-green-800"
                        : licenca.status === "EXPIRADA"
                          ? "bg-red-100 text-red-800"
                          : "bg-yellow-100 text-yellow-800"
                    }`}
                  >
                    {licenca.status}
                  </span>
                  <p className="text-xs text-[var(--muted)] mt-1">
                    {new Date(licenca.data_validade).toLocaleDateString(
                      "pt-BR",
                    )}
                  </p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8">
            <p className="text-[var(--muted)]">Nenhuma atividade recente</p>
          </div>
        )}
      </Card>
    </DashboardLayout>
  );
}
