package main.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import main.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class RelatoriosController {

    // Filtros
    @FXML private ComboBox<String> cbPeriodo;
    @FXML private DatePicker dpDataInicial;
    @FXML private DatePicker dpDataFinal;
    @FXML private Button btnFiltrar;
    @FXML private Button btnAtualizar;
    @FXML private Label lblPeriodoAtual;
    @FXML private Label lblUltimaAtualizacao;

    // Cards de Resumo
    @FXML private Text txtTotalVendas;
    @FXML private Text txtTicketMedio;
    @FXML private Text txtTotalProdutos;
    @FXML private Text txtTotalClientes;
    @FXML private Text txtTotalFornecedores;
    @FXML private Text txtProdutosBaixoEstoque;

    // Gráficos
    @FXML private BarChart<String, Number> chartFormasPagamento;
    @FXML private CategoryAxis xAxisPagamento;
    @FXML private NumberAxis yAxisPagamento;

    @FXML private BarChart<String, Number> chartVendasMes;
    @FXML private CategoryAxis xAxisVendas;
    @FXML private NumberAxis yAxisVendas;

    // Variáveis de controle
    private LocalDate dataInicial;
    private LocalDate dataFinal;

    @FXML
    public void initialize() {
        try {
            setupPeriodoComboBox();
            setupDatePickers();
            carregarDados();
        } catch (Exception e) {
            System.err.println("Erro ao inicializar controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupPeriodoComboBox() {
        ObservableList<String> periodos = FXCollections.observableArrayList(
            "Este Mês",
            "Últimos 7 Dias",
            "Últimos 30 Dias",
            "Últimos 3 Meses",
            "Este Ano",
            "Personalizado"
        );
        cbPeriodo.setItems(periodos);
        cbPeriodo.setValue("Este Mês");
        
        cbPeriodo.setOnAction(event -> {
            String periodo = cbPeriodo.getValue();
            if (!"Personalizado".equals(periodo)) {
                atualizarDatasPorPeriodo(periodo);
                carregarDados();
            }
        });
    }

    private void setupDatePickers() {
        atualizarDatasPorPeriodo("Este Mês");
        dpDataInicial.setValue(dataInicial);
        dpDataFinal.setValue(dataFinal);
        
        // Desabilitar date pickers se não for período personalizado
        dpDataInicial.setDisable(true);
        dpDataFinal.setDisable(true);
        
        cbPeriodo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isPersonalizado = "Personalizado".equals(newVal);
            dpDataInicial.setDisable(!isPersonalizado);
            dpDataFinal.setDisable(!isPersonalizado);
        });
    }

    private void atualizarDatasPorPeriodo(String periodo) {
        LocalDate hoje = LocalDate.now();
        
        switch (periodo) {
            case "Este Mês":
                dataInicial = hoje.withDayOfMonth(1);
                dataFinal = hoje;
                break;
            case "Últimos 7 Dias":
                dataInicial = hoje.minusDays(7);
                dataFinal = hoje;
                break;
            case "Últimos 30 Dias":
                dataInicial = hoje.minusDays(30);
                dataFinal = hoje;
                break;
            case "Últimos 3 Meses":
                dataInicial = hoje.minusMonths(3);
                dataFinal = hoje;
                break;
            case "Este Ano":
                dataInicial = hoje.withDayOfYear(1);
                dataFinal = hoje;
                break;
            default:
                // Personalizado - não altera as datas
                break;
        }
        
        if (dpDataInicial != null && dpDataFinal != null) {
            dpDataInicial.setValue(dataInicial);
            dpDataFinal.setValue(dataFinal);
        }
    }

    @FXML
    private void handleFiltrar() {
        if ("Personalizado".equals(cbPeriodo.getValue())) {
            dataInicial = dpDataInicial.getValue();
            dataFinal = dpDataFinal.getValue();
            
            if (dataInicial == null || dataFinal == null) {
                System.out.println("Por favor, selecione as datas inicial e final.");
                return;
            }
            
            if (dataInicial.isAfter(dataFinal)) {
                System.out.println("A data inicial não pode ser posterior à data final.");
                return;
            }
        }
        
        carregarDados();
    }

    @FXML
    private void handleAtualizar() {
        carregarDados();
    }

    private void carregarDados() {
        try {
            atualizarLabelPeriodo();
            carregarResumo();
            carregarGraficoFormasPagamento();
            carregarGraficoVendasMes();
            atualizarHoraAtualizacao();
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarLabelPeriodo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String periodoTexto = String.format("Período: %s a %s", 
            dataInicial.format(formatter), 
            dataFinal.format(formatter));
        lblPeriodoAtual.setText(periodoTexto);
    }

    private void atualizarHoraAtualizacao() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblUltimaAtualizacao.setText("Última atualização: " + LocalDateTime.now().format(formatter));
    }

    private void carregarResumo() {
        try (Connection conn = DatabaseConnection.getConnectionMercado()) {
            
            // Total de Vendas e Ticket Médio
            String sqlVendas = "SELECT COUNT(*) as total_vendas, " +
                             "COALESCE(SUM(valor_total), 0) as total, " +
                             "COALESCE(AVG(valor_total), 0) as ticket_medio " +
                             "FROM venda " +
                             "WHERE data_venda::date BETWEEN ? AND ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendas)) {
                stmt.setDate(1, Date.valueOf(dataInicial));
                stmt.setDate(2, Date.valueOf(dataFinal));
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    double ticketMedio = rs.getDouble("ticket_medio");
                    
                    txtTotalVendas.setText(formatarMoeda(total));
                    txtTicketMedio.setText(formatarMoeda(ticketMedio));
                }
            }
            
            // Total de Produtos
            String sqlProdutos = "SELECT COUNT(*) as total FROM produto";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlProdutos);
                if (rs.next()) {
                    txtTotalProdutos.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // Total de Clientes
            String sqlClientes = "SELECT COUNT(*) as total FROM clientes";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlClientes);
                if (rs.next()) {
                    txtTotalClientes.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // Total de Fornecedores
            String sqlFornecedores = "SELECT COUNT(*) as total FROM fornecedor";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlFornecedores);
                if (rs.next()) {
                    txtTotalFornecedores.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // Produtos com Baixo Estoque (estoque < 10)
            String sqlBaixoEstoque = "SELECT COUNT(*) as total FROM produto WHERE estoque_atual < 10";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlBaixoEstoque);
                if (rs.next()) {
                    txtProdutosBaixoEstoque.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao carregar resumo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarGraficoFormasPagamento() {
        try (Connection conn = DatabaseConnection.getConnectionMercado()) {
            chartFormasPagamento.getData().clear();
            
            String sql = "SELECT forma_pagamento, COUNT(*) as quantidade " +
                        "FROM venda " +
                        "WHERE data_venda::date BETWEEN ? AND ? " +
                        "GROUP BY forma_pagamento " +
                        "ORDER BY forma_pagamento";
            
            // Inicializar todas as formas de pagamento com 0
            Map<String, Integer> dados = new LinkedHashMap<>();
            dados.put("PIX", 0);
            dados.put("CARTAO", 0);
            dados.put("DINHEIRO", 0);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(dataInicial));
                stmt.setDate(2, Date.valueOf(dataFinal));
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String forma = rs.getString("forma_pagamento");
                    int quantidade = rs.getInt("quantidade");
                    if (forma != null) {
                        dados.put(forma.toUpperCase(), quantidade);
                    }
                }
            }
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Vendas");
            
            // Mapear os nomes para exibição mais amigável
            Map<String, String> nomesExibicao = new LinkedHashMap<>();
            nomesExibicao.put("PIX", "Pix");
            nomesExibicao.put("CARTAO", "Cartão");
            nomesExibicao.put("DINHEIRO", "Dinheiro");
            
            dados.forEach((forma, quantidade) -> {
                String nomeExibicao = nomesExibicao.getOrDefault(forma, forma);
                series.getData().add(new XYChart.Data<>(nomeExibicao, quantidade));
            });
            
            chartFormasPagamento.getData().add(series);
            
            // Configurações do gráfico
            yAxisPagamento.setLabel("Quantidade");
            xAxisPagamento.setLabel("Forma de Pagamento");
            
        } catch (SQLException e) {
            System.err.println("Erro ao carregar gráfico de formas de pagamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarGraficoVendasMes() {
        try (Connection conn = DatabaseConnection.getConnectionMercado()) {
            chartVendasMes.getData().clear();
            
            String sql = "SELECT TO_CHAR(data_venda, 'YYYY-MM') as mes, " +
                        "SUM(valor_total) as total " +
                        "FROM venda " +
                        "WHERE data_venda::date BETWEEN ? AND ? " +
                        "GROUP BY TO_CHAR(data_venda, 'YYYY-MM') " +
                        "ORDER BY mes";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(dataInicial));
                stmt.setDate(2, Date.valueOf(dataFinal));
                ResultSet rs = stmt.executeQuery();
                
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Vendas");
                
                while (rs.next()) {
                    String mes = rs.getString("mes");
                    double total = rs.getDouble("total");
                    
                    // Formatar o mês para exibição (ex: 2024-01 -> Jan/24)
                    String mesFormatado = formatarMes(mes);
                    series.getData().add(new XYChart.Data<>(mesFormatado, total));
                }
                
                chartVendasMes.getData().add(series);
                
                // Configurações do gráfico
                yAxisVendas.setLabel("Valor (R$)");
                xAxisVendas.setLabel("Mês");
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao carregar gráfico de vendas por mês: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatarMoeda(double valor) {
        return String.format("R$ %,.2f", valor);
    }

    private String formatarMes(String mes) {
        if (mes == null || mes.length() < 7) return mes;
        
        String[] partes = mes.split("-");
        if (partes.length == 2) {
            String ano = partes[0].substring(2); // Pega os 2 últimos dígitos do ano
            String mesNum = partes[1];
            
            String[] meses = {"", "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", 
                            "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
            
            try {
                int mesIndex = Integer.parseInt(mesNum);
                if (mesIndex >= 1 && mesIndex <= 12) {
                    return meses[mesIndex] + "/" + ano;
                }
            } catch (NumberFormatException e) {
                return mes;
            }
        }
        return mes;
    }
}