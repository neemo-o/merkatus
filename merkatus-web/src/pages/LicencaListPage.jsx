import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  listarLicencas,
  removerLicenca,
  renovarLicenca,
  criarLicenca,
  atualizarLicenca,
  buscarLicenca,
} from "../services/licencaService";
import { listarClientes } from "../services/clienteService";
import DashboardLayout from "../components/DashboardLayout";
import Button from "../components/ui/Button";
import Table from "../components/ui/Table";
import SearchBar from "../components/ui/SearchBar";
import Select from "../components/ui/Select";
import Modal from "../components/ui/Modal";
import Pagination from "../components/ui/Pagination";
import LoadingSpinner from "../components/ui/LoadingSpinner";
import LicencaModal from "../components/modals/LicencaModal";
import LicencaToggleModal from "../components/modals/LicencaToggleModal";
import IconButton from "../components/ui/IconButton";
import { useToast } from "../context/ToastContext";

export default function LicencaListPage() {
  const { showSuccess, showError } = useToast();
  const [licencas, setLicencas] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [clientesLoading, setClientesLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [expiringDays, setExpiringDays] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [deleteModal, setDeleteModal] = useState({
    open: false,
    licenca: null,
  });
  const [renovarModal, setRenovarModal] = useState({
    open: false,
    licenca: null,
  });
  const [licencaModal, setLicencaModal] = useState({
    open: false,
    licenca: null,
  });
  const [modalLoading, setModalLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [renovando, setRenovando] = useState(false);
  const [toggleModal, setToggleModal] = useState({
    open: false,
    licenca: null,
  });
  const [toggling, setToggling] = useState(false);
  const [savingLicenca, setSavingLicenca] = useState(false);

  const openLicencaModal = async (licenca) => {
    if (!licenca) {
      setLicencaModal({ open: true, licenca: null });
      return;
    }

    try {
      setModalLoading(true);
      const fullLicenca = await buscarLicenca(licenca.id_licenca);
      setLicencaModal({ open: true, licenca: fullLicenca });
    } catch (error) {
      console.error("Erro ao carregar licença para edição:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao carregar licença para edição";
      showError(mensagem);
    } finally {
      setModalLoading(false);
    }
  };

  const handleSaveLicenca = async (formData) => {
    try {
      setSavingLicenca(true);

      // Transforma a estrutura de capacidades para o formato esperado pela API
      if (licencaModal.licenca?.id_licenca) {
        // Atualização: só envia os campos adicionais
        const updateData = {
          qtd_pdv_adicional:
            parseInt(formData.capacidades?.qtd_pdv_adicional) || 0,
          qtd_gerenciador_adicional:
            parseInt(formData.capacidades?.qtd_gerenciador_adicional) || 0,
        };
        console.log("Dados de atualização enviados:", updateData);
        await atualizarLicenca(licencaModal.licenca.id_licenca, updateData);
        showSuccess("Licença atualizada com sucesso!");
      } else {
        // Criação: envia todos os campos de capacidade
        const createData = {
          id_cliente: parseInt(formData.id_cliente),
          meses_validade: parseInt(formData.meses_validade),
          qtd_pdv_incluso: parseInt(formData.capacidades?.qtd_pdv_incluso) || 1,
          qtd_pdv_adicional:
            parseInt(formData.capacidades?.qtd_pdv_adicional) || 0,
          qtd_gerenciador_incluso:
            parseInt(formData.capacidades?.qtd_gerenciador_incluso) || 1,
          qtd_gerenciador_adicional:
            parseInt(formData.capacidades?.qtd_gerenciador_adicional) || 0,
        };
        console.log("Dados de criação enviados:", createData);
        await criarLicenca(createData);
        showSuccess("Licença criada com sucesso!");
      }
      loadLicencas();
      setLicencaModal({ open: false, licenca: null });
    } catch (error) {
      console.error("Erro ao salvar licença:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao salvar licença. Verifique os dados e tente novamente.";
      showError(mensagem);
    } finally {
      setSavingLicenca(false);
    }
  };

  const statusOptions = [
    { value: "", label: "Todos" },
    { value: "ATIVA", label: "Ativa" },
    { value: "EXPIRADA", label: "Expirada" },
    { value: "SUSPENSA", label: "Suspensa" },
    { value: "CANCELADA", label: "Cancelada" },
  ];

  const expiringOptions = [
    { value: "", label: "Todas" },
    { value: "30", label: "Expiram em 30 dias" },
    { value: "7", label: "Expiram em 7 dias" },
    { value: "1", label: "Expiram hoje" },
  ];

  const loadClientes = async () => {
    try {
      setClientesLoading(true);
      const result = await listarClientes({ ativo: true, limit: 1000 });
      setClientes(result.data);
    } catch (error) {
      console.error("Erro ao carregar clientes:", error);
    } finally {
      setClientesLoading(false);
    }
  };

  const loadLicencas = async () => {
    try {
      setLoading(true);
      const params = {
        search: search || undefined,
        status: status || undefined,
        expiring_days: expiringDays || undefined,
        page: currentPage,
        limit: 10,
      };

      const result = await listarLicencas(params);
      setLicencas(result.data);
      setTotalPages(result.meta?.pagination?.totalPages || 1);
    } catch (error) {
      console.error("Erro ao carregar licenças:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao carregar licenças";
      showError(mensagem);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadClientes();
  }, []);

  useEffect(() => {
    loadLicencas();
  }, [search, status, expiringDays, currentPage]);

  const handleDelete = async () => {
    if (!deleteModal.licenca) return;

    try {
      setDeleting(true);
      await removerLicenca(deleteModal.licenca.id_licenca);
      setDeleteModal({ open: false, licenca: null });
      loadLicencas();
      showSuccess("Licença removida com sucesso!");
    } catch (error) {
      console.error("Erro ao remover licença:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao remover licença. Tente novamente.";
      showError(mensagem);
    } finally {
      setDeleting(false);
    }
  };

  const handleRenovar = async () => {
    if (!renovarModal.licenca) return;

    try {
      setRenovando(true);
      await renovarLicenca(renovarModal.licenca.id_licenca, {
        meses_adicionais: 12,
      });
      setRenovarModal({ open: false, licenca: null });
      loadLicencas();
      showSuccess("Licença renovada com sucesso!");
    } catch (error) {
      console.error("Erro ao renovar licença:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao renovar licença. Tente novamente.";
      showError(mensagem);
    } finally {
      setRenovando(false);
    }
  };

  const handleToggleStatus = async (id_licenca, novoStatus) => {
    try {
      setToggling(true);
      await atualizarLicenca(id_licenca, { status: novoStatus });
      loadLicencas();
      showSuccess(
        `Licença ${novoStatus === "ATIVA" ? "ativada" : "suspendida"} com sucesso!`,
      );
    } catch (error) {
      console.error("Erro ao alternar status da licença:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao alterar status da licença. Tente novamente.";
      showError(mensagem);
    } finally {
      setToggling(false);
    }
  };

  const columns = [
    { key: "chave_ativacao", label: "Chave de Ativação" },
    {
      key: "cliente",
      label: "Cliente",
      render: (cliente) => cliente?.razao_social || "N/A",
    },
    {
      key: "status",
      label: "Status",
      render: (value) => {
        const statusColors = {
          ATIVA: "bg-green-100 text-green-800",
          EXPIRADA: "bg-red-100 text-red-800",
          SUSPENSA: "bg-yellow-100 text-yellow-800",
          CANCELADA: "bg-gray-100 text-gray-800",
        };
        return (
          <span
            className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${statusColors[value] || "bg-gray-100 text-gray-800"}`}
          >
            {value}
          </span>
        );
      },
    },
    {
      key: "data_validade",
      label: "Validade",
      render: (value) => new Date(value).toLocaleDateString("pt-BR"),
    },
    {
      key: "actions",
      label: "Ações",
      render: (_, licenca) => (
        <div className="flex gap-2">
          <IconButton
            action="view"
            tooltip="Ver detalhes"
            to={`/dashboard/licencas/${licenca.id_licenca}`}
            variant="ghost"
          />
          <IconButton
            action="edit"
            tooltip="Editar licença"
            onClick={() => openLicencaModal(licenca)}
            variant="ghost"
          />
          <IconButton
            action="delete"
            tooltip="Remover licença"
            onClick={() => setDeleteModal({ open: true, licenca })}
            variant="danger"
          />
          <IconButton
            action="toggle"
            tooltip="Toggle status"
            onClick={() => setToggleModal({ open: true, licenca })}
            variant="secondary"
          />
          {licenca.status === "EXPIRADA" && (
            <IconButton
              action="renew"
              tooltip="Renovar licença"
              onClick={() => setRenovarModal({ open: true, licenca })}
              variant="secondary"
            />
          )}
        </div>
      ),
    },
  ];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-[var(--text)]">Licenças</h1>
          <Button
            onClick={() => setLicencaModal({ open: true, licenca: null })}
          >
            Nova Licença
          </Button>
        </div>

        {/* Filtros */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <SearchBar
            value={search}
            onChange={setSearch}
            placeholder="Buscar por chave de ativação..."
          />
          <Select
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            options={statusOptions}
          />
          <Select
            value={expiringDays}
            onChange={(e) => setExpiringDays(e.target.value)}
            options={expiringOptions}
          />
        </div>

        {/* Tabela */}
        <Table
          columns={columns}
          data={licencas}
          loading={loading}
          emptyMessage="Nenhuma licença encontrada"
        />

        {/* Paginação */}
        {!loading && licencas.length > 0 && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        )}

        {/* Modal de confirmação de exclusão */}
        <Modal
          isOpen={deleteModal.open}
          onClose={() => setDeleteModal({ open: false, licenca: null })}
          title="Confirmar exclusão"
        >
          <p className="text-[var(--text)]">
            Tem certeza que deseja remover a licença{" "}
            <strong>{deleteModal.licenca?.chave_ativacao}</strong>? Esta ação
            não pode ser desfeita.
          </p>
          <div className="flex justify-end gap-3 mt-6">
            <Button
              variant="secondary"
              onClick={() => setDeleteModal({ open: false, licenca: null })}
            >
              Cancelar
            </Button>
            <Button variant="danger" onClick={handleDelete} loading={deleting}>
              Remover
            </Button>
          </div>
        </Modal>

        <LicencaModal
          isOpen={licencaModal.open}
          licenca={licencaModal.licenca}
          onClose={() => setLicencaModal({ open: false, licenca: null })}
          onSubmit={handleSaveLicenca}
          clientes={clientes}
          loading={savingLicenca || modalLoading}
        />
        <LicencaToggleModal
          isOpen={toggleModal.open}
          licenca={toggleModal.licenca}
          onClose={() => setToggleModal({ open: false, licenca: null })}
          onToggle={handleToggleStatus}
          loading={toggling}
        />
        {/* Modal de renovação */}
        <Modal
          isOpen={renovarModal.open}
          onClose={() => setRenovarModal({ open: false, licenca: null })}
          title="Renovar Licença"
        >
          <p className="text-[var(--text)]">
            Deseja renovar a licença{" "}
            <strong>{renovarModal.licenca?.chave_ativacao}</strong> por mais 12
            meses?
          </p>
          <div className="flex justify-end gap-3 mt-6">
            <Button
              variant="secondary"
              onClick={() => setRenovarModal({ open: false, licenca: null })}
            >
              Cancelar
            </Button>
            <Button onClick={handleRenovar} loading={renovando}>
              Renovar
            </Button>
          </div>
        </Modal>
      </div>
    </DashboardLayout>
  );
}
