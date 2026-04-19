import Modal from "../ui/Modal";
import Button from "../ui/Button";

export default function LicencaToggleModal({
  isOpen,
  onClose,
  onToggle,
  licenca = null,
  loading = false,
}) {
  const isAtiva = licenca?.status === "ATIVA";
  const novoStatus = isAtiva ? "SUSPENSA" : "ATIVA";

  const handleConfirm = () => {
    onToggle(licenca.id_licenca, novoStatus);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`${isAtiva ? "Suspender" : "Ativar"} licença`}
      size="sm"
    >
      <div className="space-y-4 py-4">
        <p className="text-[var(--text)]">
          Deseja {isAtiva ? "suspender" : "ativar"} a licença{" "}
          <strong>{licenca?.chave_ativacao}</strong> do cliente{" "}
          <strong>{licenca?.cliente?.razao_social}</strong>?
        </p>
        <p className="text-sm text-[var(--muted)]">
          Status atual: <strong>{licenca?.status}</strong>
        </p>
        <div className="flex justify-end gap-3 pt-4">
          <Button variant="secondary" onClick={onClose} disabled={loading}>
            Cancelar
          </Button>
          <Button
            onClick={handleConfirm}
            loading={loading}
            variant={isAtiva ? "danger" : "primary"}
          >
            {isAtiva ? "Suspender" : "Ativar"}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
