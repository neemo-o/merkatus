import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { buscarLicenca, atualizarLicenca } from "../services/licencaService";
import { listarClientes } from "../services/clienteService";
import DashboardLayout from "../components/DashboardLayout";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";
import Select from "../components/ui/Select";
import LoadingSpinner from "../components/ui/LoadingSpinner";
import { useToast } from "../context/ToastContext";

export default function LicencaEditPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { showSuccess, showError } = useToast();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [clientes, setClientes] = useState([]);
  const [clientesLoading, setClientesLoading] = useState(true);
  const [formData, setFormData] = useState({
    id_cliente: "",
    meses_validade: 12,
    capacidades: {
      qtd_pdv_incluso: 1,
      qtd_pdv_adicional: 0,
      qtd_gerenciador_incluso: 1,
      qtd_gerenciador_adicional: 0,
    },
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    loadClientes();
    loadLicenca();
  }, [id]);

  const loadClientes = async () => {
    try {
      setClientesLoading(true);
      const result = await listarClientes({ ativo: true, limit: 1000 });
      setClientes(result.data);
    } catch (error) {
      console.error("Erro ao carregar clientes:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao carregar clientes";
      showError(mensagem);
    } finally {
      setClientesLoading(false);
    }
  };

  const loadLicenca = async () => {
    try {
      setInitialLoading(true);
      const licenca = await buscarLicenca(id);

      setFormData({
        id_cliente: licenca.id_cliente?.toString() || "",
        meses_validade: 12,
        capacidades: licenca.capacidade || {
          qtd_pdv_incluso: 1,
          qtd_pdv_adicional: 0,
          qtd_gerenciador_incluso: 1,
          qtd_gerenciador_adicional: 0,
        },
      });
    } catch (error) {
      console.error("Erro ao carregar licença:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao carregar licença";
      showError(mensagem);
      navigate("/dashboard/licencas");
    } finally {
      setInitialLoading(false);
    }
  };

  const handleInputChange = (field, value) => {
    if (field.startsWith("capacidades.")) {
      const capacidadeField = field.split(".")[1];
      setFormData((prev) => ({
        ...prev,
        capacidades: {
          ...prev.capacidades,
          [capacidadeField]: parseInt(value) || 0,
        },
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [field]: value,
      }));
    }

    // Limpar erro do campo
    if (errors[field]) {
      setErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.id_cliente) {
      newErrors.id_cliente = "Cliente é obrigatório";
    }

    if (!formData.meses_validade || formData.meses_validade < 1) {
      newErrors.meses_validade = "Meses de validade deve ser pelo menos 1";
    }

    const capacidades = formData.capacidades;
    if (capacidades.qtd_pdv_incluso < 0) {
      newErrors["capacidades.qtd_pdv_incluso"] =
        "Quantidade deve ser maior ou igual a 0";
    }
    if (capacidades.qtd_pdv_adicional < 0) {
      newErrors["capacidades.qtd_pdv_adicional"] =
        "Quantidade deve ser maior ou igual a 0";
    }
    if (capacidades.qtd_gerenciador_incluso < 0) {
      newErrors["capacidades.qtd_gerenciador_incluso"] =
        "Quantidade deve ser maior ou igual a 0";
    }
    if (capacidades.qtd_gerenciador_adicional < 0) {
      newErrors["capacidades.qtd_gerenciador_adicional"] =
        "Quantidade deve ser maior ou igual a 0";
    }

    // Validar que pelo menos 1 PDV incluído
    if (capacidades.qtd_pdv_incluso < 1) {
      newErrors["capacidades.qtd_pdv_incluso"] =
        "Deve haver pelo menos 1 PDV incluído";
    }

    // Validar que pelo menos 1 Gerenciador incluído
    if (capacidades.qtd_gerenciador_incluso < 1) {
      newErrors["capacidades.qtd_gerenciador_incluso"] =
        "Deve haver pelo menos 1 Gerenciador incluído";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      setLoading(true);
      await atualizarLicenca(id, formData);
      showSuccess("Licença atualizada com sucesso!");
      navigate("/dashboard/licencas");
    } catch (error) {
      console.error("Erro ao atualizar licença:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao atualizar licença. Tente novamente.";
      showError(mensagem);
    } finally {
      setLoading(false);
    }
  };

  const clienteOptions = clientes.map((cliente) => ({
    value: cliente.id_cliente.toString(),
    label: `${cliente.razao_social} - ${cliente.cidade || "Cidade não informada"}`,
  }));

  const totalPDV =
    formData.capacidades.qtd_pdv_incluso +
    formData.capacidades.qtd_pdv_adicional;
  const totalGerenciador =
    formData.capacidades.qtd_gerenciador_incluso +
    formData.capacidades.qtd_gerenciador_adicional;

  if (initialLoading) {
    return (
      <DashboardLayout>
        <div className="flex justify-center items-center min-h-64">
          <LoadingSpinner size="lg" />
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="max-w-2xl mx-auto space-y-6">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate("/dashboard/licencas")}
            className="text-[var(--muted)] hover:text-[var(--text)]"
          >
            ← Voltar
          </button>
          <h1 className="text-2xl font-bold text-[var(--text)]">
            Editar Licença
          </h1>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-4">
            <Select
              label="Cliente"
              value={formData.id_cliente}
              onChange={(e) => handleInputChange("id_cliente", e.target.value)}
              options={clienteOptions}
              placeholder={
                clientesLoading
                  ? "Carregando clientes..."
                  : "Selecione um cliente"
              }
              error={errors.id_cliente}
              required
              disabled={clientesLoading}
            />

            <Input
              label="Meses de Validade"
              type="number"
              value={formData.meses_validade}
              onChange={(e) =>
                handleInputChange("meses_validade", e.target.value)
              }
              placeholder="12"
              error={errors.meses_validade}
              required
              min="1"
            />
          </div>

          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-[var(--text)]">
              Capacidades - PDV
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="PDV Incluído"
                type="number"
                value={formData.capacidades.qtd_pdv_incluso}
                onChange={(e) =>
                  handleInputChange(
                    "capacidades.qtd_pdv_incluso",
                    e.target.value,
                  )
                }
                placeholder="1"
                error={errors["capacidades.qtd_pdv_incluso"]}
                min="0"
              />

              <Input
                label="PDV Adicional"
                type="number"
                value={formData.capacidades.qtd_pdv_adicional}
                onChange={(e) =>
                  handleInputChange(
                    "capacidades.qtd_pdv_adicional",
                    e.target.value,
                  )
                }
                placeholder="0"
                error={errors["capacidades.qtd_pdv_adicional"]}
                min="0"
              />
            </div>

            <div className="p-4 bg-[var(--surface)] border border-[var(--border)] rounded-lg">
              <p className="text-sm text-[var(--muted)]">
                Total PDV:{" "}
                <span className="font-semibold text-[var(--text)]">
                  {totalPDV}
                </span>
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-[var(--text)]">
              Capacidades - Gerenciador
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Gerenciador Incluído"
                type="number"
                value={formData.capacidades.qtd_gerenciador_incluso}
                onChange={(e) =>
                  handleInputChange(
                    "capacidades.qtd_gerenciador_incluso",
                    e.target.value,
                  )
                }
                placeholder="1"
                error={errors["capacidades.qtd_gerenciador_incluso"]}
                min="0"
              />

              <Input
                label="Gerenciador Adicional"
                type="number"
                value={formData.capacidades.qtd_gerenciador_adicional}
                onChange={(e) =>
                  handleInputChange(
                    "capacidades.qtd_gerenciador_adicional",
                    e.target.value,
                  )
                }
                placeholder="0"
                error={errors["capacidades.qtd_gerenciador_adicional"]}
                min="0"
              />
            </div>

            <div className="p-4 bg-[var(--surface)] border border-[var(--border)] rounded-lg">
              <p className="text-sm text-[var(--muted)]">
                Total Gerenciador:{" "}
                <span className="font-semibold text-[var(--text)]">
                  {totalGerenciador}
                </span>
              </p>
            </div>
          </div>

          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate("/dashboard/licencas")}
            >
              Cancelar
            </Button>
            <Button type="submit" loading={loading}>
              Salvar Alterações
            </Button>
          </div>
        </form>
      </div>
    </DashboardLayout>
  );
}
