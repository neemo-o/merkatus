import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  listarClientes,
  removerCliente,
  criarCliente,
  atualizarCliente,
} from "../services/clienteService";
import Button from "../components/ui/Button";
import Table from "../components/ui/Table";
import SearchBar from "../components/ui/SearchBar";
import Select from "../components/ui/Select";
import Modal from "../components/ui/Modal";
import Pagination from "../components/ui/Pagination";
import LoadingSpinner from "../components/ui/LoadingSpinner";
import DashboardLayout from "../components/DashboardLayout";
import ClienteModal from "../components/modals/ClienteModal";
import IconButton from "../components/ui/IconButton";
import { useToast } from "../context/ToastContext";

export default function ClienteListPage() {
  const { showSuccess, showError } = useToast();
  const [clientes, setClientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [estado, setEstado] = useState("");
  const [ativo, setAtivo] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [deleteModal, setDeleteModal] = useState({
    open: false,
    cliente: null,
  });
  const [deleting, setDeleting] = useState(false);
  const [clienteModal, setClienteModal] = useState({
    open: false,
    cliente: null,
  });
  const [savingCliente, setSavingCliente] = useState(false);

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

  const statusOptions = [
    { value: "", label: "Todos" },
    { value: "true", label: "Ativo" },
    { value: "false", label: "Inativo" },
  ];

  const loadClientes = async () => {
    try {
      setLoading(true);
      const params = {
        search: search || undefined,
        estado: estado || undefined,
        ativo: ativo === "" ? undefined : ativo === "true",
        page: currentPage,
        limit: 10,
      };

      const result = await listarClientes(params);
      setClientes(result.data);
      setTotalPages(result.meta?.pagination?.totalPages || 1);
    } catch (error) {
      console.error("Erro ao carregar clientes:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao carregar clientes";
      showError(mensagem);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadClientes();
  }, [search, estado, ativo, currentPage]);

  const handleDelete = async () => {
    if (!deleteModal.cliente) return;

    try {
      setDeleting(true);
      await removerCliente(deleteModal.cliente.id_cliente);
      setDeleteModal({ open: false, cliente: null });
      loadClientes(); // Recarregar lista
      showSuccess("Cliente removido com sucesso!");
    } catch (error) {
      console.error("Erro ao remover cliente:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao remover cliente. Tente novamente.";
      showError(mensagem);
    } finally {
      setDeleting(false);
    }
  };

  const columns = [
    { key: "razao_social", label: "Razão Social" },
    { key: "cnpj", label: "CNPJ" },
    { key: "cidade", label: "Cidade" },
    { key: "estado", label: "Estado" },
    {
      key: "ativo",
      label: "Status",
      render: (value) => (
        <span
          className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${
            value ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
          }`}
        >
          {value ? "Ativo" : "Inativo"}
        </span>
      ),
    },
    {
      key: "actions",
      label: "Ações",
      render: (_, cliente) => (
        <div className="flex gap-2">
          <IconButton
            action="view"
            tooltip="Ver detalhes"
            to={`/dashboard/clientes/${cliente.id_cliente}`}
            variant="ghost"
          />
          <IconButton
            action="edit"
            tooltip="Editar cliente"
            onClick={() => setClienteModal({ open: true, cliente })}
            variant="ghost"
          />
          <IconButton
            action="delete"
            tooltip="Remover cliente"
            onClick={() => setDeleteModal({ open: true, cliente })}
            variant="danger"
          />
        </div>
      ),
    },
  ];

  const handleSaveCliente = async (formData) => {
    try {
      setSavingCliente(true);
      if (clienteModal.cliente?.id_cliente) {
        await atualizarCliente(clienteModal.cliente.id_cliente, formData);
        showSuccess("Cliente atualizado com sucesso!");
      } else {
        await criarCliente(formData);
        showSuccess("Cliente criado com sucesso!");
      }
      await loadClientes();
      setClienteModal({ open: false, cliente: null });
    } catch (error) {
      console.error("Erro ao salvar cliente:", error);
      const mensagem =
        error?.data?.error?.message ||
        error?.message ||
        "Erro ao salvar cliente. Verifique os dados e tente novamente.";
      showError(mensagem);
    } finally {
      setSavingCliente(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-bold text-[var(--text)]">Clientes</h1>
          <Button
            onClick={() => setClienteModal({ open: true, cliente: null })}
          >
            + Novo Cliente
          </Button>
        </div>

        {/* Filtros */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <SearchBar
            value={search}
            onChange={setSearch}
            placeholder="Buscar por razão social, CNPJ ou cidade..."
            className="md:col-span-2"
          />
          <Select
            value={estado}
            onChange={(e) => setEstado(e.target.value)}
            options={[{ value: "", label: "Todos os estados" }, ...estados]}
          />
          <Select
            value={ativo}
            onChange={(e) => setAtivo(e.target.value)}
            options={statusOptions}
          />
        </div>

        {/* Tabela */}
        <Table
          columns={columns}
          data={clientes}
          loading={loading}
          emptyMessage="Nenhum cliente encontrado"
        />

        {/* Paginação */}
        {!loading && clientes.length > 0 && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        )}

        {/* Modal de confirmação de exclusão */}
        <Modal
          isOpen={deleteModal.open}
          onClose={() => setDeleteModal({ open: false, cliente: null })}
          title="Confirmar exclusão"
        >
          <p className="text-[var(--text)]">
            Tem certeza que deseja remover o cliente{" "}
            <strong>{deleteModal.cliente?.razao_social}</strong>? Esta ação não
            pode ser desfeita.
          </p>
          <div className="flex justify-end gap-3 mt-6">
            <Button
              variant="secondary"
              onClick={() => setDeleteModal({ open: false, cliente: null })}
            >
              Cancelar
            </Button>
            <Button variant="danger" onClick={handleDelete} loading={deleting}>
              Remover
            </Button>
          </div>
        </Modal>

        {/* Cliente Modal */}
        <ClienteModal
          isOpen={clienteModal.open}
          cliente={clienteModal.cliente}
          onClose={() => setClienteModal({ open: false, cliente: null })}
          onSave={handleSaveCliente}
          loading={savingCliente}
        />
      </div>
    </DashboardLayout>
  );
}
