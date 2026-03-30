package main.util;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FXMLLoaderFactory {

    private final ApplicationContext context;

    public FXMLLoaderFactory(ApplicationContext context) {
        this.context = context;
    }

    public FXMLLoader create(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(context::getBean);
        return loader;
    }
}