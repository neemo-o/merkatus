import { useState, useEffect } from "react";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import Input from "../ui/Input";
import Select from "../ui/Select";

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

export default function ClienteModal({
  isOpen,
  onClose,
  onSubmit,
  cliente = null,
  loading = false,
}) {
  const [formData, setFormData] = useState({
    razao_social: "",
    cnpj: "",
    email: "",
    telefone: "",
    endereco: "",
    numero: "",
    complemento: "",
    cidade: "",
    estado: "",
    cep: "",
    ativo: true,
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (cliente) {
      setFormData(cliente);
    } else {
      setFormData({
        razao_social: "",
        cnpj: "",
        email: "",
        telefone: "",
        endereco: "",
        numero: "",
        complemento: "",
        cidade: "",
        estado: "",
        cep: "",
        ativo: true,
      });
    }
    setErrors({});
  }, [cliente, isOpen]);

  const handleInputChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
    if (errors[field]) {
      setErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
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
    if (!formData.razao_social)
      newErrors.razao_social = "Razão social é obrigatória";
    if (!formData.cnpj) {
      newErrors.cnpj = "CNPJ é obrigatório";
    } else {
      const cleaned = formData.cnpj.replace(/\D/g, "");
      if (cleaned.length !== 14) {
        newErrors.cnpj = "CNPJ deve ter 14 dígitos";
      } else if (!validateCNPJ(formData.cnpj)) {
        newErrors.cnpj = "CNPJ inválido";
      }
    }
    if (!formData.cidade) newErrors.cidade = "Cidade é obrigatória";
    if (!formData.estado) newErrors.estado = "Estado é obrigatório";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={cliente ? "Editar Cliente" : "Novo Cliente"}
      size="lg"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Razão Social"
            value={formData.razao_social}
            onChange={(e) => handleInputChange("razao_social", e.target.value)}
            placeholder="Nome da empresa"
            error={errors.razao_social}
            required
          />
          <Input
            label="CNPJ"
            value={formData.cnpj}
            onChange={(e) => handleInputChange("cnpj", e.target.value)}
            placeholder="00.000.000/0000-00"
            error={errors.cnpj}
            required
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Email"
            type="email"
            value={formData.email}
            onChange={(e) => handleInputChange("email", e.target.value)}
            placeholder="email@empresa.com"
          />
          <Input
            label="Telefone"
            value={formData.telefone}
            onChange={(e) => handleInputChange("telefone", e.target.value)}
            placeholder="(11) 9999-9999"
          />
        </div>

        <Input
          label="Endereço"
          value={formData.endereco}
          onChange={(e) => handleInputChange("endereco", e.target.value)}
          placeholder="Rua, avenida, etc"
        />

        <div className="grid grid-cols-3 gap-4">
          <Input
            label="Número"
            value={formData.numero}
            onChange={(e) => handleInputChange("numero", e.target.value)}
            placeholder="123"
          />
          <Input
            label="Complemento"
            value={formData.complemento}
            onChange={(e) => handleInputChange("complemento", e.target.value)}
            placeholder="Apto, sala, etc"
          />
          <Input
            label="CEP"
            value={formData.cep}
            onChange={(e) => handleInputChange("cep", e.target.value)}
            placeholder="00000-000"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Cidade"
            value={formData.cidade}
            onChange={(e) => handleInputChange("cidade", e.target.value)}
            placeholder="São Paulo"
            error={errors.cidade}
            required
          />
          <Select
            label="Estado"
            value={formData.estado}
            onChange={(e) => handleInputChange("estado", e.target.value)}
            options={[{ value: "", label: "Selecione um estado" }, ...estados]}
            error={errors.estado}
            required
          />
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
            {cliente ? "Atualizar" : "Criar"} Cliente
          </Button>
        </div>
      </form>
    </Modal>
  );
}
