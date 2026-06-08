package main.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;

public class DialogUtil {

    /**
     * Exibe um modal de confirmação customizado (estilo da sua aplicação).
     * @return true se confirmado, false se cancelado
     */
    public static boolean confirmar(Stage owner, String titulo, String mensagem) {
        try {
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle(titulo);
            dialog.setResizable(false);

            // Topo colorido
            HBox topBar = new HBox();
            topBar.setMinHeight(8);
            topBar.setMaxWidth(Double.MAX_VALUE);
            topBar.setStyle("-fx-background-color: #194e8f;");

            // Texto da mensagem
            Label lblMsg = new Label(mensagem);
            lblMsg.setWrapText(true);
            lblMsg.setTextAlignment(TextAlignment.CENTER);
            lblMsg.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI'; -fx-text-fill: #333;");
            lblMsg.setMaxWidth(340);

            // Botões
            Button btnSim = new Button("Confirmar");
            btnSim.setStyle("-fx-background-color: #194e8f; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
            
            Button btnNao = new Button("Cancelar");
            btnNao.setStyle("-fx-background-color: transparent; -fx-border-color: #194e8f; -fx-text-fill: #194e8f; -fx-cursor: hand; -fx-padding: 8 20; -fx-border-width: 1;");

            HBox boxBotoes = new HBox(10, btnNao, btnSim);
            boxBotoes.setAlignment(Pos.CENTER);
            boxBotoes.setPadding(new Insets(12, 0, 0, 0));

            // Layout principal
            VBox root = new VBox(16, topBar, lblMsg, boxBotoes);
            root.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 0 0 16 0;");
            root.setPrefWidth(380);
            VBox.setMargin(lblMsg, new Insets(16, 16, 0, 16));
            VBox.setMargin(boxBotoes, new Insets(16, 16, 0, 16));

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.sizeToScene();

            // Resultado
            final boolean[] confirmado = {false};
            btnSim.setOnAction(e -> { confirmado[0] = true; dialog.close(); });
            btnNao.setOnAction(e -> dialog.close());

            dialog.showAndWait();
            return confirmado[0];

        } catch (Exception e) {
            // Fallback para o Alert padrão do JavaFX em caso de erro
            javafx.scene.control.Alert fallback = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            fallback.setTitle(titulo);
            fallback.setHeaderText(null);
            fallback.setContentText(mensagem);
            fallback.initOwner(owner);
            return fallback.showAndWait().filter(r -> r == javafx.scene.control.ButtonType.OK).isPresent();
        }
    }
}