package main.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Modal.ModalManager;
import main.Modal.ModalType;
import main.database.DatabaseManager;
import main.util.FXMLLoaderFactory;

@Component
public class MainScreenController {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private FXMLLoaderFactory loaderFactory;

    @FXML
    private Button minimizeButton;
    @FXML
    private Button closeButton;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnProdutos;
    @FXML
    private Button btnClientes;
    @FXML
    private Button btnVendas;
    @FXML
    private Button btnEstoque;
    @FXML
    private Button btnFornecedores;
    @FXML
    private Button btnRelatorios;
    @FXML
    private Button btnConfiguracoes;
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox dashboardContent;
    @FXML
    private Label usuarioLabel;
    @FXML
    private Label ipLabel;
    @FXML
    private Label dataHoraLabel;
    @FXML
    private Text vendasHojeText;
    @FXML
    private Text produtosText;
    @FXML
    private Text clientesText;

    private Button currentActiveButton;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (contentArea.getScene() != null && contentArea.getScene().getWindow() != null) {
                configurarStage((Stage) contentArea.getScene().getWindow());
            } else {
                contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null && newScene.getWindow() != null) {
                        configurarStage((Stage) newScene.getWindow());
                    }
                });
            }
        });

        ipLabel.setText("127.0.0.1");

        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    dataHoraLabel.setText(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                }),
                new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        carregarDashboard();
        adicionarEfeitosMenu();
    }

    private void carregarDashboard() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM clientes)                     AS total_clientes,
                    (SELECT COUNT(*) FROM produto)                      AS total_produtos,
                    COALESCE((SELECT SUM(valor_total) FROM venda), 0)   AS total_vendas
                """;

        try (Connection conn = databaseManager.getOficialConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                vendasHojeText.setText(String.format("R$ %.2f", rs.getDouble("total_vendas")));
                produtosText.setText(String.valueOf(rs.getInt("total_produtos")));
                clientesText.setText(String.valueOf(rs.getInt("total_clientes")));
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar dashboard: " + e.getMessage());
        }
    }

    private void configurarStage(Stage stage) {
        try {
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            stage.setX(screen.getMinX());
            stage.setY(screen.getMinY());
            stage.setWidth(screen.getWidth());
            stage.setHeight(screen.getHeight());
            stage.setResizable(false);
        } catch (Exception e) {
            System.err.println("Erro ao configurar stage: " + e.getMessage());
        }
    }

    private void adicionarEfeitosMenu() {
        Button[] botoes = { btnHome, btnProdutos, btnClientes, btnVendas,
                btnEstoque, btnFornecedores, btnRelatorios, btnConfiguracoes };

        for (Button btn : botoes) {
            if (btn == null)
                continue;
            btn.setOnMouseEntered(e -> {
                if (btn != currentActiveButton)
                    btn.setStyle(btn.getStyle() + "-fx-background-color: #e8f4f8;");
            });
            btn.setOnMouseExited(e -> {
                if (btn != currentActiveButton)
                    btn.setStyle(btn.getStyle().replace("-fx-background-color: #e8f4f8;",
                            "-fx-background-color: transparent;"));
            });
        }
    }

    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(currentActiveButton.getStyle()
                    .replace("-fx-border-color: #4fa8d8;", "-fx-border-color: transparent;")
                    .replace("-fx-background-color: #e8f4f8;", "-fx-background-color: transparent;"));
        }
        currentActiveButton = button;
        button.setStyle(button.getStyle() + "-fx-border-color: #4fa8d8; -fx-background-color: #e8f4f8;");
    }

    private void carregarTela(String fxmlPath) {
        try {
            FXMLLoader loader = loaderFactory.create(fxmlPath);
            Parent tela = loader.load();
            contentArea.getChildren().setAll(tela);
        } catch (Exception e) {
            System.err.println("Erro ao carregar tela: " + fxmlPath);
            Text erro = new Text("Erro ao carregar módulo: " + e.getMessage());
            erro.setStyle("-fx-fill: #d32f2f; -fx-font-size: 14;");
            contentArea.getChildren().setAll(erro);
        }
    }

    @FXML
    private void handleHome() {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(currentActiveButton.getStyle()
                    .replace("-fx-border-color: #4fa8d8;", "-fx-border-color: transparent;")
                    .replace("-fx-background-color: #e8f4f8;", "-fx-background-color: transparent;"));
            currentActiveButton = null;
        }
        contentArea.getChildren().setAll(dashboardContent);
        carregarDashboard();
    }

    @FXML
    private void handleProdutos() {
        setActiveButton(btnProdutos);
        ModalManager.open(ModalType.PRODUTO, (Stage) contentArea.getScene().getWindow());
    }

    @FXML
    private void handleClientes() {
        setActiveButton(btnClientes);
        carregarTela("/main/view/Clientes.fxml");
    }

    @FXML
    private void handleVendas() {
        setActiveButton(btnVendas);
        carregarTela("/main/view/Vendas.fxml");
    }

    @FXML
    private void handleEstoque() {
        setActiveButton(btnEstoque);
        carregarTela("/main/view/Estoque.fxml");
    }

    @FXML
    private void handleFornecedores() {
        setActiveButton(btnFornecedores);
        carregarTela("/main/view/Fornecedores.fxml");
    }

    @FXML
    private void handleRelatorios() {
        setActiveButton(btnRelatorios);
        carregarTela("/main/view/Relatorios.fxml");
    }

    @FXML
    private void handleConfiguracoes() {
        setActiveButton(btnConfiguracoes);
        carregarTela("/main/view/Configuracoes.fxml");
    }

    @FXML
    private void handleMinimize() {
        if (minimizeButton.getScene() != null && minimizeButton.getScene().getWindow() != null)
            ((Stage) minimizeButton.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void handleClose() {
        Platform.exit();
    }

    public void setUsuario(String nomeUsuario) {
        usuarioLabel.setText(nomeUsuario);
    }
}