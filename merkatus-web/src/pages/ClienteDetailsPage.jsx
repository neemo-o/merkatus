import { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { buscarCliente } from "../services/clienteService";
import DashboardLayout from "../components/DashboardLayout";
import Button from "../components/ui/Button";
import Card from "../components/ui/Card";
import LoadingSpinner from "../components/ui/LoadingSpinner";

export default function ClienteDetailsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [cliente, setCliente] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCliente();
  }, [id]);

  const loadCliente = async () => {
    try {
      setLoading(true);
      const data = await buscarCliente(id);
      setCliente(data);
    } catch (error) {
      console.error("Erro ao carregar cliente:", error);
      navigate("/dashboard/clientes");
      // TODO: Add error toast
    } finally {
      setLoading(false);
    }
  };

  const formatCNPJ = (cnpj) => {
    if (!cnpj) return "";
    const cleaned = cnpj.toString().replace(/\D/g, "");
    const match = cleaned.match(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/);
    if (match) {
      return `${match[1]}.${match[2]}.${match[3]}/${match[4]}-${match[5]}`;
    }
    return cleaned;
  };

  const formatCEP = (cep) => {
    if (!cep) return "";
    const cleaned = cep.toString().replace(/\D/g, "");
    const match = cleaned.match(/^(\d{5})(\d{3})$/);
    if (match) {
      return `${match[1]}-${match[2]}`;
    }
    return cleaned;
  };

  const formatTelefone = (telefone) => {
    if (!telefone) return "";
    const cleaned = telefone.toString().replace(/\D/g, "");
    const match = cleaned.match(/^(\d{2})(\d{4,5})(\d{4})$/);
    if (match) {
      return `(${match[1]}) ${match[2]}-${match[3]}`;
    }
    return cleaned;
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

  if (!cliente) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <p className="text-[var(--muted)]">Cliente não encontrado</p>
          <Button
            onClick={() => navigate("/dashboard/clientes")}
            className="mt-4"
          >
            Voltar à lista
          </Button>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate("/dashboard/clientes")}
              className="text-[var(--muted)] hover:text-[var(--text)]"
            >
              ← Voltar
            </button>
            <h1 className="text-2xl font-bold text-[var(--text)]">
              {cliente.razao_social}
            </h1>
          </div>
          <div className="flex gap-3">
            <Link to={`/dashboard/clientes/${id}/edit`}>
              <Button variant="secondary">Editar</Button>
            </Link>
            <Link to="/dashboard/licencas/create">
              <Button>Criar Licença</Button>
            </Link>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Informações Básicas */}
          <Card title="Informações Básicas">
            <div className="space-y-4">
              <div>
                <label className="text-xs font-mono text-[var(--muted)]">
                  CNPJ
                </label>
                <p className="text-[var(--text)] font-medium">
                  {formatCNPJ(cliente.cnpj)}
                </p>
              </div>

              <div>
                <label className="text-xs font-mono text-[var(--muted)]">
                  Razão Social
                </label>
                <p className="text-[var(--text)]">{cliente.razao_social}</p>
              </div>

              <div>
                <label className="text-xs font-mono text-[var(--muted)]">
                  Status
                </label>
                <span
                  className={`inline-flex px-2 py-1 text-xs font-medium rounded-full mt-1 ${
                    cliente.ativo
                      ? "bg-green-100 text-green-800"
                      : "bg-red-100 text-red-800"
                  }`}
                >
                  {cliente.ativo ? "Ativo" : "Inativo"}
                </span>
              </div>
            </div>
          </Card>

          {/* Contato */}
          <Card title="Contato">
            <div className="space-y-4">
              {cliente.email && (
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Email
                  </label>
                  <p className="text-[var(--text)]">{cliente.email}</p>
                </div>
              )}

              {cliente.telefone && (
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Telefone
                  </label>
                  <p className="text-[var(--text)]">
                    {formatTelefone(cliente.telefone)}
                  </p>
                </div>
              )}

              {!cliente.email && !cliente.telefone && (
                <p className="text-[var(--muted)] text-sm">
                  Nenhuma informação de contato cadastrada
                </p>
              )}
            </div>
          </Card>

          {/* Endereço */}
          <Card title="Endereço" className="lg:col-span-2">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {cliente.endereco && (
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Endereço
                  </label>
                  <p className="text-[var(--text)]">{cliente.endereco}</p>
                </div>
              )}

              {cliente.cidade && (
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Cidade
                  </label>
                  <p className="text-[var(--text)]">{cliente.cidade}</p>
                </div>
              )}

              {cliente.estado && (
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    Estado
                  </label>
                  <p className="text-[var(--text)]">{cliente.estado}</p>
                </div>
              )}

              {cliente.cep && (
                <div>
                  <label className="text-xs font-mono text-[var(--muted)]">
                    CEP
                  </label>
                  <p className="text-[var(--text)]">{formatCEP(cliente.cep)}</p>
                </div>
              )}

              {!cliente.endereco &&
                !cliente.cidade &&
                !cliente.estado &&
                !cliente.cep && (
                  <div className="md:col-span-2">
                    <p className="text-[var(--muted)] text-sm">
                      Nenhum endereço cadastrado
                    </p>
                  </div>
                )}
            </div>
          </Card>

          {/* Estatísticas */}
          <Card title="Estatísticas" className="lg:col-span-2">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-[var(--accent)]">
                  {cliente._count?.licencas || 0}
                </div>
                <div className="text-sm text-[var(--muted)]">Licenças</div>
              </div>

              <div className="text-center">
                <div className="text-2xl font-bold text-[var(--accent)]">
                  {cliente._count?.terminais || 0}
                </div>
                <div className="text-sm text-[var(--muted)]">Terminais</div>
              </div>

              <div className="text-center">
                <div className="text-2xl font-bold text-[var(--accent)]">
                  {cliente.ativo ? "Ativo" : "Inativo"}
                </div>
                <div className="text-sm text-[var(--muted)]">Status</div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}
