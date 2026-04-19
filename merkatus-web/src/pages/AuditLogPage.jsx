import { useEffect, useState } from "react";
import DashboardLayout from "../components/DashboardLayout";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";
import SearchBar from "../components/ui/SearchBar";
import Table from "../components/ui/Table";
import Pagination from "../components/ui/Pagination";
import LoadingSpinner from "../components/ui/LoadingSpinner";
import { listarLogs } from "../services/auditService";
import { useToast } from "../context/ToastContext";

export default function AuditLogPage() {
  const { showError } = useToast();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [acao, setAcao] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    loadLogs();
  }, [search, acao, dateFrom, dateTo, currentPage]);

  const loadLogs = async () => {
    try {
      setLoading(true);
      const result = await listarLogs({
        search: search || undefined,
        acao: acao ? normalizeAuditAction(acao) : undefined,
        date_from: dateFrom || undefined,
        date_to: dateTo || undefined,
        page: currentPage,
        limit: 12,
        sort: "data_acao",
        order: "desc",
      });
      setLogs(result.data);
      setTotalPages(result.meta?.pagination?.totalPages || 1);
    } catch (error) {
      console.error("Erro ao carregar logs de auditoria:", error);
      showError(
        error?.data?.error?.message ||
          error?.message ||
          "Erro ao carregar logs de auditoria",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleResetFilters = () => {
    setSearch("");
    setAcao("");
    setDateFrom("");
    setDateTo("");
    setCurrentPage(1);
  };

  const normalizeAuditAction = (acao) => {
    if (!acao) return "-";

    const raw = String(acao).toUpperCase().trim();
    if (
      raw === "CREATE" ||
      raw.startsWith("CRE") ||
      raw.includes("CRIAR") ||
      raw.includes("CREATE") ||
      raw.includes("CADASTR") ||
      raw.includes("CRIA")
    ) {
      return "CREATE";
    }
    if (
      raw === "UPDATE" ||
      raw.startsWith("UPD") ||
      raw.includes("ATUAL") ||
      raw.includes("ALTERA") ||
      raw.includes("MODIF") ||
      raw.includes("EDIT") ||
      raw.includes("ALTER")
    ) {
      return "UPDATE";
    }
    if (
      raw === "DELETE" ||
      raw.startsWith("DEL") ||
      raw.includes("REMOV") ||
      raw.includes("EXCL") ||
      raw.includes("REMOVE") ||
      raw.includes("DELETE")
    ) {
      return "DELETE";
    }

    return "UPDATE";
  };

  const formatFieldValue = (value) => {
    if (value === null || value === undefined) return "-";
    if (typeof value === "boolean") return value ? "Sim" : "Não";
    if (typeof value === "object") {
      if (Array.isArray(value)) {
        return `[${value.map((item) => formatFieldValue(item)).join(", ")}]`;
      }
      const entries = Object.entries(value);
      if (entries.length === 0) return "-";
      return entries
        .map(([key, val]) => `${key}: ${formatFieldValue(val)}`)
        .join(", ");
    }
    return String(value);
  };

  const diffAuditValues = (oldValue, newValue, path = []) => {
    if (oldValue === newValue) return [];

    const isObject = (value) =>
      value !== null && typeof value === "object" && !Array.isArray(value);

    if (isObject(oldValue) && isObject(newValue)) {
      const keys = new Set([
        ...Object.keys(oldValue),
        ...Object.keys(newValue),
      ]);
      const changes = [];

      keys.forEach((key) => {
        changes.push(
          ...diffAuditValues(oldValue?.[key], newValue?.[key], [...path, key]),
        );
      });

      return changes;
    }

    if (isObject(oldValue) && (newValue === null || newValue === undefined)) {
      return Object.entries(oldValue).flatMap(([key, value]) =>
        diffAuditValues(value, undefined, [...path, key]),
      );
    }

    if ((oldValue === null || oldValue === undefined) && isObject(newValue)) {
      return Object.entries(newValue).flatMap(([key, value]) =>
        diffAuditValues(undefined, value, [...path, key]),
      );
    }

    return [
      {
        path,
        oldValue,
        newValue,
      },
    ];
  };

  const shouldShowAuditField = (path, tabela) => {
    const fullKey = path.join(".");
    const hiddenKeys = [
      "data_atualizacao",
      "created_at",
      "updated_at",
      "data_criacao",
      "ip",
      "id",
    ];

    if (hiddenKeys.some((hidden) => fullKey.endsWith(hidden))) {
      return false;
    }

    if (tabela === "licencas") {
      const visibleKeys = [
        "qtd_pdv_incluso",
        "qtd_pdv_adicional",
        "qtd_gerenciador_incluso",
        "qtd_gerenciador_adicional",
        "status",
        "meses_validade",
        "id_cliente",
        "capacidade.qtd_pdv_total",
        "capacidade.qtd_pdv_adicional",
        "capacidade.qtd_gerenciador_total",
        "capacidade.qtd_gerenciador_adicional",
      ];
      return visibleKeys.some((key) => fullKey === key);
    }

    if (tabela === "clientes_licenciados") {
      const visibleKeys = [
        "razao_social",
        "cnpj",
        "email",
        "telefone",
        "ativo",
      ];
      return visibleKeys.some((key) => fullKey === key);
    }

    return true;
  };

  const isEmptyAuditValue = (value) =>
    value === null || value === undefined || value === "";

  const formatAuditData = (data, table) => {
    if (!data) return "-";

    if (table === "licencas") {
      const changes = [];
      if (data.qtd_pdv_incluso !== undefined)
        changes.push(`PDV Incluído: ${formatFieldValue(data.qtd_pdv_incluso)}`);
      if (data.qtd_pdv_adicional !== undefined)
        changes.push(
          `PDV Adicional: ${formatFieldValue(data.qtd_pdv_adicional)}`,
        );
      if (data.qtd_gerenciador_incluso !== undefined)
        changes.push(
          `Gerenciador Incluído: ${formatFieldValue(data.qtd_gerenciador_incluso)}`,
        );
      if (data.qtd_gerenciador_adicional !== undefined)
        changes.push(
          `Gerenciador Adicional: ${formatFieldValue(data.qtd_gerenciador_adicional)}`,
        );
      if (data.status !== undefined)
        changes.push(`Status: ${formatFieldValue(data.status)}`);
      if (data.meses_validade !== undefined)
        changes.push(
          `Validade: ${formatFieldValue(data.meses_validade)} meses`,
        );
      if (data.id_cliente !== undefined)
        changes.push(`Cliente ID: ${formatFieldValue(data.id_cliente)}`);

      return changes.length > 0 ? changes.join(", ") : "Dados da licença";
    }

    if (table === "clientes_licenciados") {
      const changes = [];
      if (data.razao_social !== undefined)
        changes.push(`Razão Social: ${formatFieldValue(data.razao_social)}`);
      if (data.cnpj !== undefined)
        changes.push(`CNPJ: ${formatFieldValue(data.cnpj)}`);
      if (data.email !== undefined)
        changes.push(`Email: ${formatFieldValue(data.email)}`);
      if (data.telefone !== undefined)
        changes.push(`Telefone: ${formatFieldValue(data.telefone)}`);
      if (data.ativo !== undefined)
        changes.push(`Ativo: ${formatFieldValue(data.ativo)}`);

      return changes.length > 0 ? changes.join(", ") : "Dados do cliente";
    }

    const entries = Object.entries(data);
    if (entries.length === 0) return "-";

    const formatted = entries
      .slice(0, 3)
      .map(([key, value]) => `${key}: ${formatFieldValue(value)}`)
      .join(", ");

    return entries.length > 3 ? `${formatted}...` : formatted;
  };

  const formatAuditChange = (antes, depois, acao, tabela) => {
    const normalized = normalizeAuditAction(acao);

    if (normalized === "CREATE") {
      return (
        <div className="space-y-1 text-green-600 font-medium text-sm">
          <div>✓ Criado</div>
          <div>{formatAuditData(depois, tabela)}</div>
        </div>
      );
    }
    if (normalized === "DELETE") {
      return (
        <div className="space-y-1 text-red-600 font-medium text-sm">
          <div>✗ Removido</div>
          <div>{formatAuditData(antes, tabela)}</div>
        </div>
      );
    }
    if (normalized === "UPDATE") {
      if (!antes || !depois) {
        return (
          <div className="text-blue-600 font-medium text-sm">
            {formatAuditData(depois || antes, tabela)}
          </div>
        );
      }

      const changes = diffAuditValues(antes, depois);

      const visibleChanges = changes.filter(
        ({ path, oldValue, newValue }) =>
          shouldShowAuditField(path, tabela) &&
          !isEmptyAuditValue(oldValue) &&
          !isEmptyAuditValue(newValue),
      );

      const lines = visibleChanges.map(({ path, oldValue, newValue }) => {
        const fieldName = path.join(".");
        const oldText = formatFieldValue(oldValue);
        const newText = formatFieldValue(newValue);

        if (tabela === "licencas") {
          if (fieldName === "qtd_pdv_incluso")
            return `PDV incluído: ${oldText} → ${newText}`;
          if (fieldName === "qtd_pdv_adicional")
            return `PDV adicional: ${oldText} → ${newText}`;
          if (fieldName === "qtd_gerenciador_incluso")
            return `Gerenciador incluído: ${oldText} → ${newText}`;
          if (fieldName === "qtd_gerenciador_adicional")
            return `Gerenciador adicional: ${oldText} → ${newText}`;
          if (fieldName === "status") return `Status: ${oldText} → ${newText}`;
          if (fieldName === "meses_validade")
            return `Validade: ${oldText} → ${newText} meses`;
          if (fieldName === "capacidade.qtd_pdv_total")
            return `PDV total: ${oldText} → ${newText}`;
          if (fieldName === "capacidade.qtd_pdv_adicional")
            return `PDV adicional: ${oldText} → ${newText}`;
          if (fieldName === "capacidade.qtd_gerenciador_total")
            return `Gerenciador total: ${oldText} → ${newText}`;
          if (fieldName === "capacidade.qtd_gerenciador_adicional")
            return `Gerenciador adicional: ${oldText} → ${newText}`;
          if (fieldName === "total_terminais")
            return `Total de terminais: ${oldText} → ${newText}`;
          return `${fieldName}: ${oldText} → ${newText}`;
        }

        if (tabela === "clientes_licenciados" && fieldName === "ativo") {
          return `Ativo: ${oldText} → ${newText}`;
        }

        return `${fieldName}: ${oldText} → ${newText}`;
      });

      return lines.length > 0 ? (
        <div className="space-y-1 text-blue-600 text-sm">
          <div className="font-medium">↻ Alterado</div>
          {lines.map((change, index) => (
            <div key={index} className="whitespace-pre-wrap">
              {change}
            </div>
          ))}
        </div>
      ) : (
        "Nenhuma alteração detectada"
      );
    }

    return formatAuditData(depois || antes, tabela);
  };

  const columns = [
    {
      key: "data_acao",
      label: "Data / Hora",
      render: (value) =>
        value ? new Date(value).toLocaleString("pt-BR") : "-",
    },
    {
      key: "usuario",
      label: "Autor",
      render: (usuario) => usuario?.nome || "-",
    },
    {
      key: "acao",
      label: "Ação",
      render: (value) => {
        const normalized = normalizeAuditAction(value);
        const actionStyles = {
          CREATE:
            "bg-green-100 text-green-800 px-2 py-1 rounded-full text-xs font-medium",
          UPDATE:
            "bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs font-medium",
          DELETE:
            "bg-red-100 text-red-800 px-2 py-1 rounded-full text-xs font-medium",
        };
        return (
          <span
            className={
              actionStyles[normalized] ||
              "bg-gray-100 text-gray-800 px-2 py-1 rounded-full text-xs font-medium"
            }
          >
            {normalized}
          </span>
        );
      },
    },
    {
      key: "tabela",
      label: "Tabela",
    },
    {
      key: "id_registro",
      label: "Registro",
      render: (value) => (value !== null && value !== undefined ? value : "-"),
    },
    {
      key: "alteracoes",
      label: "Alterações",
      className: "min-w-[300px]",
      render: (value, row) =>
        formatAuditChange(
          row.dados_antes,
          row.dados_depois,
          row.acao,
          row.tabela,
        ),
    },
  ];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-[var(--text)]">Auditoria</h1>
            <p className="text-sm text-[var(--muted)]">
              Visualize ações de usuários com busca unificada, filtro por ação e
              período.
            </p>
          </div>
          <div className="flex gap-2 flex-wrap">
            <Button variant="secondary" onClick={handleResetFilters}>
              Limpar filtros
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-4 items-end">
          <div className="space-y-2 lg:col-span-2">
            <label className="block text-xs font-mono text-[var(--muted)]">
              Buscar
            </label>
            <SearchBar
              value={search}
              onChange={(value) => {
                setSearch(value);
                setCurrentPage(1);
              }}
              placeholder="Buscar por autor, tabela ou registro"
            />
          </div>
          <Input
            label="Ação"
            value={acao}
            onChange={(e) => {
              setAcao(e.target.value);
              setCurrentPage(1);
            }}
            placeholder="CREATE, UPDATE ou DELETE"
          />
          <Input
            label="Data início"
            type="date"
            value={dateFrom}
            onChange={(e) => {
              setDateFrom(e.target.value);
              setCurrentPage(1);
            }}
          />
          <Input
            label="Data fim"
            type="date"
            value={dateTo}
            onChange={(e) => {
              setDateTo(e.target.value);
              setCurrentPage(1);
            }}
          />
        </div>

        <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-4">
          {loading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : (
            <>
              <Table
                columns={columns}
                data={logs}
                emptyMessage="Nenhum log encontrado"
              />
              {logs.length > 0 && (
                <div className="mt-4">
                  <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                  />
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
}
