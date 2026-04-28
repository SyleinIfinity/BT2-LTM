package client;

import client.network.ChatClient;
import client.ui.ChatUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ClientMain extends Application {
    private ChatClient client;
    private ChatUI chatUI;

    @Override
    public void start(Stage stage) {
        // Ask username and build UI.
        String username = askUsername();
        client = new ChatClient("127.0.0.1", 5555, username);
        chatUI = new ChatUI(client);

        client.setMessageListener(chatUI::appendLine);

        Scene scene = new Scene(chatUI.createContent(), 640, 420);
        stage.setTitle("TCP Chat Client");
        stage.setScene(scene);
        stage.show();

        connectInBackground();
    }

    @Override
    public void stop() {
        if (client != null) {
            client.disconnect();
        }
    }

    private void connectInBackground() {
        // Connect without blocking the JavaFX thread.
        new Thread(() -> {
            try {
                client.connect();
                chatUI.appendSystem("Connected to server.");
            } catch (IOException ex) {
                chatUI.appendSystem("Connection failed: " + ex.getMessage());
            }
        }).start();
    }

    private String askUsername() {
        TextInputDialog dialog = new TextInputDialog("User");
        dialog.setTitle("Username");
        dialog.setHeaderText("Enter your username");
        Optional<String> result = dialog.showAndWait();
        return result.map(String::trim).filter(name -> !name.isEmpty()).orElse("User");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
