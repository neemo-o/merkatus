package main.util;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TableViewUtils {

    // Configurações padrão
    private static final int MAX_LINHAS_PARA_MEDIR = 100;
    private static final double PADDING_PADRAO = 20;
    private static final double LARGURA_MINIMA = 50;
    private static final double LARGURA_MAXIMA = 400;
    
    // ✅ Fonte padrão para medição (System 12pt)
    private static final Font FONTE_MEDICAO = Font.font("System", 12);

    /**
     * Ajusta automaticamente TODAS as colunas da tabela.
     */
    public static <T> void autoFitColumns(TableView<T> table) {
        javafx.application.Platform.runLater(() -> {
            for (TableColumn<T, ?> col : table.getColumns()) {
                autoFitColumn(table, col);
            }
        });
    }

    /**
     * Ajusta automaticamente apenas as colunas especificadas.
     * Útil quando algumas colunas devem ficar flexíveis (ex: "Descrição").
     */
    @SafeVarargs
    public static <T> void autoFitColumns(TableView<T> table, TableColumn<T, ?>... colunas) {
        javafx.application.Platform.runLater(() -> {
            for (TableColumn<T, ?> col : colunas) {
                autoFitColumn(table, col);
            }
        });
    }

    /**
     * Ajusta a largura de uma única coluna baseada no conteúdo.
     */
    public static <T> void autoFitColumn(TableView<T> table, TableColumn<T, ?> col) {
        double maxWidth = medirLarguraHeader(col);

        int totalItens = table.getItems().size();
        int limite = Math.min(totalItens, MAX_LINHAS_PARA_MEDIR);

        for (int i = 0; i < limite; i++) {
            T item = table.getItems().get(i);
            String texto = obterTextoCelula(col, item);

            double larguraTexto = medirTexto(texto);
            maxWidth = Math.max(maxWidth, larguraTexto);
        }

        // Margem extra se a tabela tiver muitas linhas
        if (totalItens > MAX_LINHAS_PARA_MEDIR) {
            maxWidth *= 1.05;
        }

        maxWidth = Math.max(LARGURA_MINIMA, Math.min(maxWidth, LARGURA_MAXIMA));

        col.setMinWidth(maxWidth);
        col.setMaxWidth(maxWidth);
        col.setPrefWidth(maxWidth);
    }

    /**
     * Ajusta com largura máxima personalizada.
     */
    public static <T> void autoFitColumn(TableView<T> table, TableColumn<T, ?> col, double larguraMaxima) {
        double maxWidth = medirLarguraHeader(col);

        int limite = Math.min(table.getItems().size(), MAX_LINHAS_PARA_MEDIR);
        for (int i = 0; i < limite; i++) {
            T item = table.getItems().get(i);
            String texto = obterTextoCelula(col, item);
            maxWidth = Math.max(maxWidth, medirTexto(texto));
        }

        maxWidth = Math.max(LARGURA_MINIMA, Math.min(maxWidth, larguraMaxima));

        col.setMinWidth(maxWidth);
        col.setMaxWidth(maxWidth);
        col.setPrefWidth(maxWidth);
    }

    // ==============================
    // Métodos auxiliares privados
    // ==============================

    private static <T> double medirLarguraHeader(TableColumn<T, ?> col) {
        String textoHeader = col.getText() != null ? col.getText() : "";
        return medirTexto(textoHeader) + PADDING_PADRAO;
    }

    private static <T> String obterTextoCelula(TableColumn<T, ?> col, T item) {
        try {
            ObservableValue<?> cellValue = col.getCellObservableValue(item);
            Object valor = cellValue != null ? cellValue.getValue() : null;
            return valor != null ? valor.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static double medirTexto(String texto) {
        if (texto == null || texto.isEmpty()) return PADDING_PADRAO;

        Text textNode = new Text(texto);
        textNode.setFont(FONTE_MEDICAO); // Usa fonte fixa
        return textNode.getLayoutBounds().getWidth() + PADDING_PADRAO;
    }
}