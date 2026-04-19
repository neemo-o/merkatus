import { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { buscarLicenca } from "../services/licencaService";
import { buscarCliente } from "../services/clienteService";
import DashboardLayout from "../components/DashboardLayout";
import Button from "../components/ui/Button";
import Card from "../components/ui/Card";
import LoadingSpinner from "../components/ui/LoadingSpinner";

export default function LicencaDetailsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [licenca, setLicenca] = useState(null);
  const [cliente, setCliente] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadLicenca();
  }, [id]);

  const loadLicenca = async () => {
    try {
      setLoading(true);
      const licencaData = await buscarLicenca(id);
      setLicenca(licencaData);

      // Carregar dados do cliente
      if (licencaData.id_cliente) {
        const clienteData = await buscarCliente(licencaData.id_cliente);
        setCliente(clienteData);
      }
    } catch (error) {
      console.error("Erro ao carregar licença:", error);
      navigate("/dashboard/licencas");
      // TODO: Add error toast
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleDateString("pt-BR");
  };

  const getStatusColor = (status) => {
    const colors = {
      ATIVA: "bg-green-100 text-green-800",
      EXPIRADA: "bg-red-100 text-red-800",
      SUSPENSA: "bg-yellow-100 text-yellow-800",
      CANCELADA: "bg-gray-100 text-gray-800",
    };
    return colors[status] || "bg-gray-100 text-gray-800";
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex justify-center items-center min-h-64">
          <LoadingSpinner size="lg" />
        </div>
      </DashboardLayout>
    );
  }

  if (!licenca) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <p className="text-[var(--muted)]">Licença não encontrada</p>
          <Button
            onClick={() => navigate("/dashboard/licencas")}
            className="mt-4"
          >
            Voltar à lista
          </Button>
        </div>
      </DashboardLayout>
    );
  }

  const totalPDV =
    (licenca.capacidade?.qtd_pdv_incluso || 0) +
    (licenca.capacidade?.qtd_pdv_adicional || 0);
  const totalGerenciador =
    (licenca.capacidade?.qtd_gerenciador_incluso || 0) +
    (licenca.capacidade?.qtd_gerenciador_adicional || 0);

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate("/dashboard/licencas")}
              className="text-[var(--muted)] hover:text-[var(--text)]"
            >
              ← Voltar
            </button>
            <h1 className="text-2xl font-bold text-[var(--text)]">
              Licença {licenca.chave_ativacao}
            </h1>
          </div>
          <div className="flex gap-3">
            <Link to={`/dashboard/licencas/${id}/edit`}>
              <Button variant="secondary">Editar</Button>
            </Link>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Informações Básicas */}
          <Card title="Informações Básicas">
            <div className="space-y-4">
              <div>
                <label className="text-xs font-mono text-[var(--muted)]">
                  Chave de Ativação
                </label>
                <p className="text-[var(--text)] font-mono text-sm">
                  {licenca.chave_ativacao}
                </p>
              </div>

              <div>
                <label className="text-xs font-mono text-[var(--muted)]">
                  Status
                </label>
                <span
                  className={`inline-flex px-2 py-1 text-xs font-medium rounded-full mt-1 ${getStatusColor(licenca.status)}`}
                >
                  {licenca.status}
                </span>
              </div>

              <div>
                <label className="text-xs font-mono text-[var(--muted)]">
                  Data de Criação
                </label>
                <p className="text-[var(--text)]">
                  {formatDate(licenca.data_cadastro)}
                </p>
              </div>

              <div>
                <label className="text-xs font-mono text-[var(--muted)]">
                  Data de Validade
                </label>
                <p className="text-[var(--text)]">
                  {formatDate(licenca.data_validade)}
                </p>
              </div>
            </div>
          </Card>

          {/* Cliente */}
          <Card title="Cliente">
            <div className="space-y-4">
              {cliente ? (
                <>
                  <div>
                    <label className="text-xs font-mono text-[var(--muted)]">
                      Razão Social
                    </label>
                    <p className="text-[var(--text)]">{cliente.razao_social}</p>
                  </div>

                  <div>
                    <label className="text-xs font-mono text-[var(--muted)]">
                      CNPJ
                    </label>
                    <p className="text-[var(--text)] font-mono text-sm">
                      {cliente.cnpj?.replace(
                        /(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/,
                        "$1.$2.$3/$4-$5",
                      )}
                    </p>
                  </div>

                  <div>
                    <label className="text-xs font-mono text-[var(--muted)]">
                      Cidade/Estado
                    </label>
                    <p className="text-[var(--text)]">
                      {cliente.cidade}, {cliente.estado}
                    </p>
                  </div>
                </>
              ) : (
                <p className="text-[var(--muted)] text-sm">
                  Carregando dados do cliente...
                </p>
              )}
            </div>
          </Card>

          {/* Capacidades - PDV */}
          <Card title="Capacidades - PDV">
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Incluído
                  </label>
                  <p className="text-2xl font-bold text-[var(--accent)]">
                    {licenca.capacidade?.qtd_pdv_incluso || 0}
                  </p>
                </div>
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Adicional
                  </label>
                  <p className="text-2xl font-bold text-[var(--accent)]">
                    {licenca.capacidade?.qtd_pdv_adicional || 0}
                  </p>
                </div>
              </div>

              <div className="pt-4 border-t border-[var(--border)]">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-[var(--muted)]">Total PDV</span>
                  <span className="text-xl font-bold text-[var(--text)]">
                    {totalPDV}
                  </span>
                </div>
              </div>
            </div>
          </Card>

          {/* Capacidades - Gerenciador */}
          <Card title="Capacidades - Gerenciador">
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Incluído
                  </label>
                  <p className="text-2xl font-bold text-[var(--accent)]">
                    {licenca.capacidade?.qtd_gerenciador_incluso || 0}
                  </p>
                </div>
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Adicional
                  </label>
                  <p className="text-2xl font-bold text-[var(--accent)]">
                    {licenca.capacidade?.qtd_gerenciador_adicional || 0}
                  </p>
                </div>
              </div>

              <div className="pt-4 border-t border-[var(--border)]">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-[var(--muted)]">
                    Total Gerenciador
                  </span>
                  <span className="text-xl font-bold text-[var(--text)]">
                    {totalGerenciador}
                  </span>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}
