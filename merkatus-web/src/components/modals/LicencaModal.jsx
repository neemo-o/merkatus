import { useState, useEffect } from "react";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import Input from "../ui/Input";
import Select from "../ui/Select";

export default function LicencaModal({
  isOpen,
  onClose,
  onSubmit,
  clientes = [],
  licenca = null,
  loading = false,
}) {
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
    if (licenca) {
      setFormData({
        id_cliente: licenca.id_cliente?.toString() || "",
        meses_validade: licenca.meses_validade || 12,
        capacidades: {
          qtd_pdv_incluso: licenca.capacidade?.qtd_pdv_incluso || 1,
          qtd_pdv_adicional: licenca.capacidade?.qtd_pdv_adicional || 0,
          qtd_gerenciador_incluso:
            licenca.capacidade?.qtd_gerenciador_incluso || 1,
          qtd_gerenciador_adicional:
            licenca.capacidade?.qtd_gerenciador_adicional || 0,
        },
      });
    } else {
      setFormData({
        id_cliente: "",
        meses_validade: 12,
        capacidades: {
          qtd_pdv_incluso: 1,
          qtd_pdv_adicional: 0,
          qtd_gerenciador_incluso: 1,
          qtd_gerenciador_adicional: 0,
        },
      });
    }
    setErrors({});
  }, [licenca, isOpen]);

  const handleInputChange = (field, value) => {
    if (field.startsWith("capacidades.")) {
      const capacidadeField = field.replace("capacidades.", "");
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
    if (errors[field]) {
      setErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    // Só valida cliente obrigatório se for criação (não edição)
    if (!licenca && !formData.id_cliente)
      newErrors.id_cliente = "Cliente é obrigatório";

    // Validação de meses só necessária na criação
    if (!licenca && (!formData.meses_validade || formData.meses_validade <= 0))
      newErrors.meses_validade = "Meses deve ser maior que 0";

    const capacidades = formData.capacidades;

    // Na criação, valida campos inclusos
    if (!licenca) {
      if (capacidades.qtd_pdv_incluso < 1) {
        newErrors["capacidades.qtd_pdv_incluso"] =
          "Deve haver pelo menos 1 PDV incluído";
      }
      if (capacidades.qtd_gerenciador_incluso < 1) {
        newErrors["capacidades.qtd_gerenciador_incluso"] =
          "Deve haver pelo menos 1 Gerenciador incluído";
      }
    }

    // Valida campos adicionais (criação e edição)
    if (capacidades.qtd_pdv_adicional < 0) {
      newErrors["capacidades.qtd_pdv_adicional"] =
        "Quantidade deve ser maior ou igual a 0";
    }
    if (capacidades.qtd_gerenciador_adicional < 0) {
      newErrors["capacidades.qtd_gerenciador_adicional"] =
        "Quantidade deve ser maior ou igual a 0";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
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

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={licenca ? "Editar Licença" : "Nova Licença"}
      size="lg"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <Select
          label="Cliente"
          value={formData.id_cliente}
          onChange={(e) => handleInputChange("id_cliente", e.target.value)}
          options={clienteOptions}
          placeholder="Selecione um cliente"
          error={errors.id_cliente}
          disabled={!!licenca}
          required={!licenca}
        />

        <Input
          label="Meses de Validade"
          type="number"
          value={formData.meses_validade}
          onChange={(e) => handleInputChange("meses_validade", e.target.value)}
          placeholder="12"
          error={errors.meses_validade}
          required
          min="1"
        />

        <div className="space-y-4">
          <p className="text-sm font-semibold text-[var(--text)]">
            Capacidades - PDV
          </p>
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="PDV Inclusos"
              type="number"
              value={formData.capacidades.qtd_pdv_incluso}
              onChange={(e) =>
                handleInputChange("capacidades.qtd_pdv_incluso", e.target.value)
              }
              placeholder="1"
              min="0"
              disabled={!!licenca}
            />
            <Input
              label="PDV Adicionais"
              type="number"
              value={formData.capacidades.qtd_pdv_adicional}
              onChange={(e) =>
                handleInputChange(
                  "capacidades.qtd_pdv_adicional",
                  e.target.value,
                )
              }
              placeholder="0"
              min="0"
            />
          </div>

          <div className="p-3 bg-[var(--surface)] border border-[var(--border)] rounded-lg">
            <p className="text-sm text-[var(--muted)]">
              Total PDV:{" "}
              <span className="font-semibold text-[var(--text)]">
                {totalPDV}
              </span>
            </p>
            {licenca && (
              <p className="text-xs text-[var(--muted)] mt-2">
                ℹ️ Apenas PDV Adicionais podem ser alterados
              </p>
            )}
          </div>
        </div>

        <div className="space-y-4">
          <p className="text-sm font-semibold text-[var(--text)]">
            Capacidades - Gerenciador
          </p>
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Gerenciador Inclusos"
              type="number"
              value={formData.capacidades.qtd_gerenciador_incluso}
              onChange={(e) =>
                handleInputChange(
                  "capacidades.qtd_gerenciador_incluso",
                  e.target.value,
                )
              }
              placeholder="1"
              min="0"
              disabled={!!licenca}
            />
            <Input
              label="Gerenciador Adicionais"
              type="number"
              value={formData.capacidades.qtd_gerenciador_adicional}
              onChange={(e) =>
                handleInputChange(
                  "capacidades.qtd_gerenciador_adicional",
                  e.target.value,
                )
              }
              placeholder="0"
              min="0"
            />
          </div>

          <div className="p-3 bg-[var(--surface)] border border-[var(--border)] rounded-lg">
            <p className="text-sm text-[var(--muted)]">
              Total Gerenciador:{" "}
              <span className="font-semibold text-[var(--text)]">
                {totalGerenciador}
              </span>
            </p>
            {licenca && (
              <p className="text-xs text-[var(--muted)] mt-2">
                ℹ️ Apenas Gerenciador Adicionais podem ser alterados
              </p>
            )}
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-4">
          <Button
            type="button"
            variant="secondary"
            onClick={onClose}
            disabled={loading}
          >
            Cancelar
          </Button>
          <Button type="submit" loading={loading}>
            {licenca ? "Atualizar" : "Criar"} Licença
          </Button>
        </div>
      </form>
    </Modal>
  );
}
