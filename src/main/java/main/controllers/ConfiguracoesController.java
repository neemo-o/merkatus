package main.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import main.database.DatabaseConnection;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.prefs.Preferences;

public class ConfiguracoesController {

    @FXML private TextField txtNomeEmpresa;
    @FXML private TextField txtCnpj;
    @FXML private TextField txtEndereco;
    @FXML private TextField txtHorarioAbertura;
    @FXML private TextField txtHorarioFechamento;
    @FXML private CheckBox chkFuncionaFds;
    @FXML private CheckBox chkBloquearSistemaForaHorario;
    @FXML private ComboBox<String> cbNivelPermissao;
    @FXML private Button btnFazerBackup;
    @FXML private Button btnRestaurarBackup;
    @FXML private Button btnSalvarConfiguracoes;
    @FXML private Button btnCancelar;
    @FXML private Button btnTestarBloqueio;
    
    private int idUsuarioLogado = 1; // TODO: Ajustar para pegar o ID do usuário logado

    // Preferências locais para salvar configurações
    private Preferences prefs = Preferences.userNodeForPackage(ConfiguracoesController.class);
    
    // Chaves para preferências
    private static final String PREF_HORARIO_ABERTURA = "horario_abertura";
    private static final String PREF_HORARIO_FECHAMENTO = "horario_fechamento";
    private static final String PREF_FUNCIONA_FDS = "funciona_fds";
    private static final String PREF_BLOQUEAR_FORA_HORARIO = "bloquear_fora_horario";

    // Variáveis para armazenar valores originais (para o botão Cancelar)
    private String originalHorarioAbertura;
    private String originalHorarioFechamento;
    private boolean originalFuncionaFds;
    private boolean originalBloquearForaHorario;
    private String originalNivelPermissao;

    @FXML
    void initialize() {
        System.out.println("=== Inicializando ConfiguracoesController ===");
        
        // Configurar níveis de permissão do banco
        cbNivelPermissao.setItems(FXCollections.observableArrayList(
            "admin",
            "user"
        ));
        
        // ComboBox editável
        cbNivelPermissao.setDisable(false);
        
        // Carregar configurações
        carregarConfiguracoes();
        
        // Aplicar máscaras
        aplicarMascaras();
        
        txtNomeEmpresa.setEditable(false);
        txtNomeEmpresa.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-padding: 6;");
        
        System.out.println("=== ConfiguracoesController inicializado ===");
    }

    /**
     * Carrega configurações do banco de dados e preferências locais
     */
    private void carregarConfiguracoes() {
        try {
            // Carregar dados da empresa do banco erp_licencas
            Connection conn = DatabaseConnection.getConnectionLicenses();
            
            String sql = "SELECT * FROM licencas WHERE status = 'PAGO' LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                txtNomeEmpresa.setText(rs.getString("razao_social"));
                txtCnpj.setText(rs.getString("cnpj"));
                
                StringBuilder endereco = new StringBuilder();
                adicionarCampoEndereco(endereco, rs.getString("rua"), "");
                adicionarCampoEndereco(endereco, rs.getString("numero"), ", ");
                adicionarCampoEndereco(endereco, rs.getString("bairro"), " - ");
                adicionarCampoEndereco(endereco, rs.getString("cidade"), ", ");
                adicionarCampoEndereco(endereco, rs.getString("estado"), "/");
                adicionarCampoEndereco(endereco, rs.getString("cep"), " - CEP: ");
                
                txtEndereco.setText(endereco.toString());
                
                System.out.println("✓ Configurações carregadas: " + rs.getString("razao_social"));
            } else {
                txtNomeEmpresa.setText("Nenhuma empresa cadastrada");
                txtCnpj.setText("");
                txtEndereco.setText("");
                System.out.println("⚠ Nenhuma empresa encontrada no banco");
            }
            
            rs.close();
            stmt.close();
            
            // Carregar nível de permissão do usuário logado do banco erp_oficial
            Connection connOficial = DatabaseConnection.getConnectionMercado();
            String sqlUser = "SELECT tipo_usuario FROM licencas WHERE id_usuario = ?";
            PreparedStatement stmtUser = connOficial.prepareStatement(sqlUser);
            stmtUser.setInt(1, idUsuarioLogado);
            ResultSet rsUser = stmtUser.executeQuery();
            
            if (rsUser.next()) {
                cbNivelPermissao.setValue(rsUser.getString("tipo_usuario"));
                originalNivelPermissao = rsUser.getString("tipo_usuario"); // Salvar valor original
            } else {
                cbNivelPermissao.setValue("user");
                originalNivelPermissao = "user"; // Salvar valor original
            }
            
            rsUser.close();
            stmtUser.close();
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao carregar configurações do banco: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Carregar configurações locais (Preferences)
        carregarPreferenciasLocais();
    }

    /**
     * Carrega configurações salvas localmente
     */
    private void carregarPreferenciasLocais() {
        // Valores padrão
        String horaAbertura = prefs.get(PREF_HORARIO_ABERTURA, "08:00");
        String horaFechamento = prefs.get(PREF_HORARIO_FECHAMENTO, "18:00");
        boolean funcionaFds = prefs.getBoolean(PREF_FUNCIONA_FDS, false);
        boolean bloquearForaHorario = prefs.getBoolean(PREF_BLOQUEAR_FORA_HORARIO, false);
        
        txtHorarioAbertura.setText(horaAbertura);
        txtHorarioFechamento.setText(horaFechamento);
        chkFuncionaFds.setSelected(funcionaFds);
        chkBloquearSistemaForaHorario.setSelected(bloquearForaHorario);
        
        // Salvar valores originais
        originalHorarioAbertura = horaAbertura;
        originalHorarioFechamento = horaFechamento;
        originalFuncionaFds = funcionaFds;
        originalBloquearForaHorario = bloquearForaHorario;
        
        System.out.println("✓ Preferências locais carregadas");
    }

    /**
     * Adiciona campo ao endereço se não for vazio
     */
    private void adicionarCampoEndereco(StringBuilder endereco, String valor, String separador) {
        if (valor != null && !valor.trim().isEmpty()) {
            if (endereco.length() > 0 && !separador.isEmpty()) {
                endereco.append(separador);
            }
            endereco.append(valor);
        }
    }

    /**
     * Aplica máscaras de formatação
     */
    private void aplicarMascaras() {
        // Máscara HH:MM para horários
        txtHorarioAbertura.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d{0,2}:?\\d{0,2}")) {
                txtHorarioAbertura.setText(oldVal);
            }
        });
        
        txtHorarioFechamento.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d{0,2}:?\\d{0,2}")) {
                txtHorarioFechamento.setText(oldVal);
            }
        });
    }

    /**
     * Verifica se o sistema está dentro do horário de funcionamento
     */
    public static boolean verificarHorarioFuncionamento() {
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracoesController.class);
        
        boolean bloquearForaHorario = prefs.getBoolean(PREF_BLOQUEAR_FORA_HORARIO, false);
        
        // Se não está configurado para bloquear, sempre permite acesso
        if (!bloquearForaHorario) {
            return true;
        }
        
        LocalDateTime agora = LocalDateTime.now();
        int diaSemana = agora.getDayOfWeek().getValue(); // 1=Segunda, 7=Domingo
        
        // Verificar se funciona aos finais de semana
        boolean funcionaFds = prefs.getBoolean(PREF_FUNCIONA_FDS, false);
        if (!funcionaFds && (diaSemana == 6 || diaSemana == 7)) {
            return false;
        }
        
        // Verificar horário
        String horaAbertura = prefs.get(PREF_HORARIO_ABERTURA, "08:00");
        String horaFechamento = prefs.get(PREF_HORARIO_FECHAMENTO, "18:00");
        
        try {
            LocalTime abertura = LocalTime.parse(horaAbertura);
            LocalTime fechamento = LocalTime.parse(horaFechamento);
            LocalTime horaAtual = agora.toLocalTime();
            
            // Horário atual deve estar ENTRE abertura e fechamento (inclusive)
            // Permite se: horaAtual >= abertura E horaAtual <= fechamento
            return (horaAtual.equals(abertura) || horaAtual.isAfter(abertura)) && 
                   (horaAtual.equals(fechamento) || horaAtual.isBefore(fechamento));
        } catch (Exception e) {
            System.err.println("Erro ao validar horário: " + e.getMessage());
            return true; // Em caso de erro, permite acesso
        }
    }

    /**
     * Testa o bloqueio de horário
     */
    @FXML
    void handleTestarBloqueio() {
        if (!chkBloquearSistemaForaHorario.isSelected()) {
            mostrarAlerta("Informação", 
                "O bloqueio de horário está DESATIVADO.\n\n" +
                "Ative a opção 'Bloquear acesso fora do horário' e salve as configurações para testar.", 
                Alert.AlertType.INFORMATION);
            return;
        }
        
        LocalDateTime agora = LocalDateTime.now();
        String diaSemanaNome = agora.getDayOfWeek().getDisplayName(
            java.time.format.TextStyle.FULL, 
            java.util.Locale.forLanguageTag("pt-BR")
        );
        
        String horaAtual = agora.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        boolean permiteAcesso = verificarHorarioFuncionamento();
        
        String mensagem = String.format(
            "📅 Dia: %s\n" +
            "🕐 Hora Atual: %s\n\n" +
            "⏰ Horário Configurado:\n" +
            "   • Abertura: %s\n" +
            "   • Fechamento: %s\n" +
            "   • Funciona FDS: %s\n\n" +
            "🔒 Status: %s",
            diaSemanaNome,
            horaAtual,
            txtHorarioAbertura.getText(),
            txtHorarioFechamento.getText(),
            chkFuncionaFds.isSelected() ? "Sim" : "Não",
            permiteAcesso ? "✓ ACESSO PERMITIDO" : "✗ ACESSO BLOQUEADO"
        );
        
        mostrarAlerta("Teste de Bloqueio de Horário", mensagem, 
            permiteAcesso ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
    }

    /**
     * Faz backup do banco de dados
     */
    @FXML
    void handleFazerBackup() {
        System.out.println("=== Iniciando Backup ===");
        
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Selecione onde salvar o backup");
            File selectedDirectory = directoryChooser.showDialog(btnFazerBackup.getScene().getWindow());
            
            if (selectedDirectory == null) {
                System.out.println("✗ Backup cancelado pelo usuário");
                return;
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "backup_erp_" + timestamp + ".sql";
            File backupFile = new File(selectedDirectory, backupFileName);
            
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Backup");
            confirmacao.setHeaderText("Fazer backup do banco de dados?");
            confirmacao.setContentText("O backup será salvo em:\n" + backupFile.getAbsolutePath());
            
            Optional<ButtonType> resultado = confirmacao.showAndWait();
            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                System.out.println("✗ Backup cancelado");
                return;
            }
            
            System.out.println("→ Iniciando exportação para: " + backupFileName);
            fazerBackupSQL(backupFile);
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao fazer backup: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao fazer backup:\n" + e.getMessage(), 
                Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Faz backup exportando dados em SQL
     */
    private void fazerBackupSQL(File backupFile) {
        try (Connection conn = DatabaseConnection.getConnectionMercado();
             PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {
            
            writer.println("-- ========================================");
            writer.println("-- Backup do Sistema ERP");
            writer.println("-- Data: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println("-- ========================================");
            writer.println();
            
            // Exportar tabelas principais
            exportarTabela(conn, writer, "fornecedor");
            exportarTabela(conn, writer, "produto");
            exportarTabela(conn, writer, "clientes");
            exportarTabela(conn, writer, "enderecos");
            exportarTabela(conn, writer, "venda");
            exportarTabela(conn, writer, "compra");
            
            writer.println("-- ========================================");
            writer.println("-- Backup finalizado com sucesso!");
            writer.println("-- ========================================");
            
            System.out.println("✓ Backup realizado com sucesso!");
            
            mostrarAlerta("Sucesso", 
                "Backup realizado com sucesso!\n\n" +
                "Arquivo: " + backupFile.getName() + "\n" +
                "Local: " + backupFile.getParent(), 
                Alert.AlertType.INFORMATION);
                
        } catch (Exception e) {
            System.err.println("✗ Erro ao exportar dados: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao exportar dados:\n" + e.getMessage(), 
                Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Exporta dados de uma tabela
     */
    private void exportarTabela(Connection conn, PrintWriter writer, String tableName) throws Exception {
        System.out.println("  → Exportando tabela: " + tableName);
        
        String sql = "SELECT * FROM " + tableName;
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        writer.println("-- ========================================");
        writer.println("-- Tabela: " + tableName);
        writer.println("-- ========================================");
        
        int columnCount = rs.getMetaData().getColumnCount();
        int count = 0;
        
        while (rs.next()) {
            StringBuilder insert = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
            
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                
                if (value == null) {
                    insert.append("NULL");
                } else if (value instanceof String) {
                    insert.append("'").append(value.toString().replace("'", "''")).append("'");
                } else if (value instanceof java.sql.Timestamp || value instanceof java.sql.Date) {
                    insert.append("'").append(value.toString()).append("'");
                } else {
                    insert.append(value);
                }
                
                if (i < columnCount) {
                    insert.append(", ");
                }
            }
            
            insert.append(");");
            writer.println(insert.toString());
            count++;
        }
        
        writer.println("-- Total de registros: " + count);
        writer.println();
        
        System.out.println("    ✓ " + count + " registros exportados");
        
        rs.close();
        stmt.close();
    }

    /**
     * Restaura backup
     */
    @FXML
    void handleRestaurarBackup() {
        System.out.println("=== Iniciando Restauração de Backup ===");
        
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecione o arquivo de backup");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos SQL", "*.sql"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
            );
            
            File selectedFile = fileChooser.showOpenDialog(btnRestaurarBackup.getScene().getWindow());
            
            if (selectedFile == null) {
                System.out.println("✗ Restauração cancelada");
                return;
            }
            
            Alert confirmacao = new Alert(Alert.AlertType.WARNING);
            confirmacao.setTitle("⚠️ ATENÇÃO - OPERAÇÃO IRREVERSÍVEL");
            confirmacao.setHeaderText("Restaurar backup SUBSTITUIRÁ todos os dados atuais!");
            confirmacao.setContentText(
                "Esta ação NÃO pode ser desfeita!\n\n" +
                "Todos os dados atuais do sistema serão perdidos.\n\n" +
                "Arquivo: " + selectedFile.getName() + "\n\n" +
                "Tem certeza que deseja continuar?"
            );
            
            ButtonType btnContinuar = new ButtonType("Sim, Restaurar", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancelar = new ButtonType("Não, Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmacao.getButtonTypes().setAll(btnContinuar, btnCancelar);
            
            Optional<ButtonType> resultado = confirmacao.showAndWait();
            if (resultado.isEmpty() || resultado.get() != btnContinuar) {
                System.out.println("✗ Restauração cancelada pelo usuário");
                return;
            }
            
            System.out.println("→ Restaurando de: " + selectedFile.getName());
            
            mostrarAlerta("Informação", 
                "Para restaurar o backup:\n\n" +
                "1. Abra o pgAdmin 4\n" +
                "2. Botão direito no banco 'erp_oficial'\n" +
                "3. Selecione 'Restore...'\n" +
                "4. Escolha o arquivo: " + selectedFile.getName() + "\n" +
                "5. Clique em 'Restore'\n\n" +
                "Ou execute no terminal:\n" +
                "psql -U postgres -d erp_oficial -f \"" + selectedFile.getAbsolutePath() + "\"", 
                Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao restaurar: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao restaurar backup:\n" + e.getMessage(), 
                Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Salva configurações
     */
    @FXML
    void handleSalvarConfiguracoes() {
        System.out.println("=== Salvando Configurações ===");
        
        try {
            // Validar horários
            if (!validarHorario(txtHorarioAbertura.getText())) {
                mostrarAlerta("Erro", "Horário de abertura inválido!\nUse o formato HH:MM (ex: 08:00)", 
                    Alert.AlertType.ERROR);
                return;
            }
            
            if (!validarHorario(txtHorarioFechamento.getText())) {
                mostrarAlerta("Erro", "Horário de fechamento inválido!\nUse o formato HH:MM (ex: 18:00)", 
                    Alert.AlertType.ERROR);
                return;
            }
            
            if (txtCnpj.getText().trim().isEmpty()) {
                mostrarAlerta("Aviso", "Não há empresa cadastrada para atualizar.", 
                    Alert.AlertType.WARNING);
                return;
            }
            
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Salvar Configurações");
            confirmacao.setHeaderText("Deseja salvar as alterações?");
            confirmacao.setContentText(
                "✓ Configurações que serão salvas:\n\n" +
                "• Nível de permissão: no banco erp_oficial\n" +
                "• Horários de funcionamento: localmente\n" +
                "• Funciona FDS: localmente\n" +
                "• Bloquear fora do horário: localmente\n\n" +
                "Nome da Empresa, CNPJ e Endereço: somente leitura"
            );
            
            Optional<ButtonType> resultado = confirmacao.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                
                // REMOVIDO: Não atualiza mais o nome da empresa no banco erp_licencas
                
                // Salvar nível de permissão no banco erp_oficial
                Connection connOficial = DatabaseConnection.getConnectionMercado();
                String sqlPermissao = "UPDATE licencas SET tipo_usuario = ? WHERE id_usuario = ?";
                PreparedStatement stmtPermissao = connOficial.prepareStatement(sqlPermissao);
                stmtPermissao.setString(1, cbNivelPermissao.getValue());
                stmtPermissao.setInt(2, idUsuarioLogado);
                
                int rowsPermissao = stmtPermissao.executeUpdate();
                stmtPermissao.close();
                
                // Salvar configurações locais
                prefs.put(PREF_HORARIO_ABERTURA, txtHorarioAbertura.getText());
                prefs.put(PREF_HORARIO_FECHAMENTO, txtHorarioFechamento.getText());
                prefs.putBoolean(PREF_FUNCIONA_FDS, chkFuncionaFds.isSelected());
                prefs.putBoolean(PREF_BLOQUEAR_FORA_HORARIO, chkBloquearSistemaForaHorario.isSelected());
                
                //Atualizar valores originais após salvar
                originalHorarioAbertura = txtHorarioAbertura.getText();
                originalHorarioFechamento = txtHorarioFechamento.getText();
                originalFuncionaFds = chkFuncionaFds.isSelected();
                originalBloquearForaHorario = chkBloquearSistemaForaHorario.isSelected();
                originalNivelPermissao = cbNivelPermissao.getValue();
                
                System.out.println("✓ Configurações salvas com sucesso!");
                
                String mensagem = "Configurações salvas com sucesso!\n\n";
                if (rowsPermissao > 0) {
                    mensagem += "✓ Nível de permissão atualizado no banco\n";
                }
                mensagem += "✓ Horários e preferências salvos localmente";
                
                mostrarAlerta("Sucesso", mensagem, Alert.AlertType.INFORMATION);
            }
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao salvar: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao salvar configurações:\n" + e.getMessage(), 
                Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Valida formato de horário HH:MM
     */
    private boolean validarHorario(String horario) {
        if (horario == null || horario.trim().isEmpty()) {
            return false;
        }
        
        String[] partes = horario.split(":");
        if (partes.length != 2) {
            return false;
        }
        
        try {
            int hora = Integer.parseInt(partes[0]);
            int minuto = Integer.parseInt(partes[1]);
            return hora >= 0 && hora <= 23 && minuto >= 0 && minuto <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Cancela alterações e restaura valores originais
     */
    @FXML
    void handleCancelar() {
        System.out.println("=== Cancelando Alterações ===");
        
        try {
            // Verificar se houve alterações
            boolean houveAlteracoes = false;
            
            if (!txtHorarioAbertura.getText().equals(originalHorarioAbertura) ||
                !txtHorarioFechamento.getText().equals(originalHorarioFechamento) ||
                chkFuncionaFds.isSelected() != originalFuncionaFds ||
                chkBloquearSistemaForaHorario.isSelected() != originalBloquearForaHorario ||
                !cbNivelPermissao.getValue().equals(originalNivelPermissao)) {
                houveAlteracoes = true;
            }
            
            if (houveAlteracoes) {
                Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacao.setTitle("Cancelar Alterações");
                confirmacao.setHeaderText("Descartar alterações?");
                confirmacao.setContentText("Todas as alterações não salvas serão perdidas.\n\nDeseja continuar?");
                
                Optional<ButtonType> resultado = confirmacao.showAndWait();
                if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                    System.out.println("✗ Cancelamento abortado pelo usuário");
                    return;
                }
            }
            
            // Restaurar valores originais
            txtHorarioAbertura.setText(originalHorarioAbertura);
            txtHorarioFechamento.setText(originalHorarioFechamento);
            chkFuncionaFds.setSelected(originalFuncionaFds);
            chkBloquearSistemaForaHorario.setSelected(originalBloquearForaHorario);
            cbNivelPermissao.setValue(originalNivelPermissao);
            
            System.out.println("✓ Valores restaurados para os originais");
            
            if (houveAlteracoes) {
                mostrarAlerta("Cancelado", 
                    "Alterações descartadas!\n\nTodos os campos foram restaurados para os valores originais.", 
                    Alert.AlertType.INFORMATION);
            }
            
        } catch (Exception e) {
            System.err.println("✗ Erro ao cancelar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mostra alerta
     */
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
