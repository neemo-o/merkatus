import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { criarCliente } from "../services/clienteService";
import DashboardLayout from "../components/DashboardLayout";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";
import Select from "../components/ui/Select";

export default function ClienteCreatePage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    cnpj: "",
    razao_social: "",
    email: "",
    telefone: "",
    endereco: "",
    cidade: "",
    estado: "",
    cep: "",
  });
  const [errors, setErrors] = useState({});

  const estados = [
    { value: "AC", label: "Acre" },
    { value: "AL", label: "Alagoas" },
    { value: "AP", label: "Amapá" },
    { value: "AM", label: "Amazonas" },
    { value: "BA", label: "Bahia" },
    { value: "CE", label: "Ceará" },
    { value: "DF", label: "Distrito Federal" },
    { value: "ES", label: "Espírito Santo" },
    { value: "GO", label: "Goiás" },
    { value: "MA", label: "Maranhão" },
    { value: "MT", label: "Mato Grosso" },
    { value: "MS", label: "Mato Grosso do Sul" },
    { value: "MG", label: "Minas Gerais" },
    { value: "PA", label: "Pará" },
    { value: "PB", label: "Paraíba" },
    { value: "PR", label: "Paraná" },
    { value: "PE", label: "Pernambuco" },
    { value: "PI", label: "Piauí" },
    { value: "RJ", label: "Rio de Janeiro" },
    { value: "RN", label: "Rio Grande do Norte" },
    { value: "RS", label: "Rio Grande do Sul" },
    { value: "RO", label: "Rondônia" },
    { value: "RR", label: "Roraima" },
    { value: "SC", label: "Santa Catarina" },
    { value: "SP", label: "São Paulo" },
    { value: "SE", label: "Sergipe" },
    { value: "TO", label: "Tocantins" },
  ];

  const formatCNPJ = (value) => {
    const cleaned = value.replace(/\D/g, "");
    const match = cleaned.match(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/);
    if (match) {
      return `${match[1]}.${match[2]}.${match[3]}/${match[4]}-${match[5]}`;
    }
    return cleaned;
  };

  const formatCEP = (value) => {
    const cleaned = value.replace(/\D/g, "");
    const match = cleaned.match(/^(\d{5})(\d{3})$/);
    if (match) {
      return `${match[1]}-${match[2]}`;
    }
    return cleaned;
  };

  const formatTelefone = (value) => {
    const cleaned = value.replace(/\D/g, "");
    const match = cleaned.match(/^(\d{2})(\d{4,5})(\d{4})$/);
    if (match) {
      return `(${match[1]}) ${match[2]}-${match[3]}`;
    }
    return cleaned;
  };

  const handleInputChange = (field, value) => {
    let formattedValue = value;

    if (field === "cnpj") {
      formattedValue = formatCNPJ(value);
    } else if (field === "cep") {
      formattedValue = formatCEP(value);
    } else if (field === "telefone") {
      formattedValue = formatTelefone(value);
    }

    setFormData((prev) => ({ ...prev, [field]: formattedValue }));

    // Limpar erro do campo
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const validateCNPJ = (cnpj) => {
    const cleaned = cnpj.replace(/\D/g, "");
    if (cleaned.length !== 14) return false;

    // Verificar se todos os dígitos são iguais
    if (/^(\d)\1+$/.test(cleaned)) return false;

    // Calcular primeiro dígito verificador
    let sum = 0;
    let weight = 5;
    for (let i = 0; i < 12; i++) {
      sum += parseInt(cleaned[i]) * weight;
      weight = weight === 2 ? 9 : weight - 1;
    }
    let digit = 11 - (sum % 11);
    if (digit >= 10) digit = 0;
    if (digit !== parseInt(cleaned[12])) return false;

    // Calcular segundo dígito verificador
    sum = 0;
    weight = 6;
    for (let i = 0; i < 13; i++) {
      sum += parseInt(cleaned[i]) * weight;
      weight = weight === 2 ? 9 : weight - 1;
    }
    digit = 11 - (sum % 11);
    if (digit >= 10) digit = 0;
    return digit === parseInt(cleaned[13]);
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.cnpj.trim()) {
      newErrors.cnpj = "CNPJ é obrigatório";
    } else {
      const cleaned = formData.cnpj.replace(/\D/g, "");
      if (cleaned.length !== 14) {
        newErrors.cnpj = "CNPJ deve ter 14 dígitos";
      } else if (!validateCNPJ(formData.cnpj)) {
        newErrors.cnpj = "CNPJ inválido";
      }
    }

    if (!formData.razao_social.trim()) {
      newErrors.razao_social = "Razão social é obrigatória";
    } else if (formData.razao_social.length < 2) {
      newErrors.razao_social = "Razão social deve ter pelo menos 2 caracteres";
    }

    if (formData.email && !/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "Email inválido";
    }

    if (!formData.cidade.trim()) {
      newErrors.cidade = "Cidade é obrigatória";
    }

    if (!formData.estado) {
      newErrors.estado = "Estado é obrigatório";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      setLoading(true);
      const clienteData = {
        ...formData,
        cnpj: formData.cnpj.replace(/\D/g, ""), // Remove formatação para envio
        cep: formData.cep.replace(/\D/g, ""), // Remove formatação para envio
      };

      await criarCliente(clienteData);
      navigate("/dashboard/clientes");
      // TODO: Add success toast
    } catch (error) {
      console.error("Erro ao criar cliente:", error);
      // TODO: Add error toast
    } finally {
      setLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-2xl mx-auto space-y-6">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate("/dashboard/clientes")}
            className="text-[var(--muted)] hover:text-[var(--text)]"
          >
            ← Voltar
          </button>
          <h1 className="text-2xl font-bold text-[var(--text)]">
            Novo Cliente
          </h1>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Input
              label="CNPJ"
              type="text"
              value={formData.cnpj}
              onChange={(e) => handleInputChange("cnpj", e.target.value)}
              placeholder="00.000.000/0000-00"
              error={errors.cnpj}
              required
              maxLength={18}
            />

            <Input
              label="Razão Social"
              type="text"
              value={formData.razao_social}
              onChange={(e) =>
                handleInputChange("razao_social", e.target.value)
              }
              placeholder="Nome da empresa"
              error={errors.razao_social}
              required
              maxLength={255}
            />

            <Input
              label="Email"
              type="email"
              value={formData.email}
              onChange={(e) => handleInputChange("email", e.target.value)}
              placeholder="contato@empresa.com"
              error={errors.email}
            />

            <Input
              label="Telefone"
              type="text"
              value={formData.telefone}
              onChange={(e) => handleInputChange("telefone", e.target.value)}
              placeholder="(00) 00000-0000"
              maxLength={15}
            />

            <Input
              label="Endereço"
              type="text"
              value={formData.endereco}
              onChange={(e) => handleInputChange("endereco", e.target.value)}
              placeholder="Rua, número, bairro"
              className="md:col-span-2"
            />

            <Input
              label="Cidade"
              type="text"
              value={formData.cidade}
              onChange={(e) => handleInputChange("cidade", e.target.value)}
              placeholder="Nome da cidade"
            />

            <Select
              label="Estado"
              value={formData.estado}
              onChange={(e) => handleInputChange("estado", e.target.value)}
              options={estados}
              placeholder="Selecione o estado"
            />

            <Input
              label="CEP"
              type="text"
              value={formData.cep}
              onChange={(e) => handleInputChange("cep", e.target.value)}
              placeholder="00000-000"
              maxLength={9}
            />
          </div>

          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate("/dashboard/clientes")}
            >
              Cancelar
            </Button>
            <Button type="submit" loading={loading}>
              Criar Cliente
            </Button>
          </div>
        </form>
      </div>
    </DashboardLayout>
  );
}
