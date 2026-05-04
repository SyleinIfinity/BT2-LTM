package client.app;

import client.network.ChatClient;
import client.ui.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

/**
 * JavaFX entry point. Loads FXML + CSS and injects the network layer (ChatClient)
 * into the UI controller.
 */
public class MainApp extends Application {
    private ChatClient chatClient;

    @Override
    public void start(Stage stage) throws IOException {
        String username = askUsername();
        chatClient = new ChatClient("127.0.0.1", 5555, username);

        URL fxml = MainApp.class.getResource("/ui/main.fxml");
        if (fxml == null) {
            throw new IOException("Missing FXML resource: /ui/main.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load(), 720, 520);
        scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("/ui/style.css")).toExternalForm());

        ChatController controller = loader.getController();
        controller.setChatClient(chatClient);
        controller.setCurrentUsername(username);
        controller.connectAsync();

        stage.setTitle("TCP Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (chatClient != null) {
            chatClient.disconnect();
        }
    }

    private String askUsername() {
        TextInputDialog dialog = new TextInputDialog("User");
        dialog.setTitle("Username");
        dialog.setHeaderText("Enter your username");
        Optional<String> result = dialog.showAndWait();
        String name = result.map(String::trim).orElse("");
        return name.isEmpty() ? "User" : name;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
