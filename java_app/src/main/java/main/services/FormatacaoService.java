package main.services;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatacaoService {

    // Instância única otimizada para a thread da UI (JavaFX Application Thread)
    private static final NumberFormat FORMATADOR_MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    /**
     * Retorna um CellFactory genérico para formatar colunas de BigDecimal como moeda.
     * Compatível com qualquer TableView<T> do seu projeto.
     */
    public static <T> Callback<TableColumn<T, BigDecimal>, TableCell<T, BigDecimal>> cellFactoryMoeda() {
        return column -> new TableCell<T, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                // Se a célula estiver vazia ou o valor for nulo, não exibe nada
                setText((empty || item == null) ? null : FORMATADOR_MOEDA.format(item));
            }
        };
    }
}
