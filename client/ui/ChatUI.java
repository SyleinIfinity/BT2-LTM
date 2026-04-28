package client.ui;

import client.model.ChatMessage;
import client.network.ChatClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

public class ChatUI {
    private final ChatClient client;
    private TextArea chatArea;
    private TextField inputField;

    public ChatUI(ChatClient client) {
        this.client = client;
    }

    public Parent createContent() {
        // Basic chat layout: message area + input + buttons.
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        inputField = new TextField();
        inputField.setPromptText("Type your message...");

        Button sendButton = new Button("Send");
        Button imageButton = new Button("Send Image");

        sendButton.setOnAction(event -> sendText());
        inputField.setOnAction(event -> sendText());
        imageButton.setOnAction(event -> sendImage());

        HBox bottomBar = new HBox(8, inputField, sendButton, imageButton);
        bottomBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(chatArea);
        root.setBottom(bottomBar);
        BorderPane.setMargin(chatArea, new Insets(8));
        return root;
    }

    public void appendLine(String line) {
        // Called from network thread; update UI safely.
        Platform.runLater(() -> {
            ChatMessage message = ChatMessage.fromProtocol(line);
            if (message != null) {
                chatArea.appendText(message.getUsername() + ": " + message.getMessage() + "\n");
            } else {
                chatArea.appendText(line + "\n");
            }
        });
    }

    public void appendSystem(String line) {
        Platform.runLater(() -> chatArea.appendText("[System] " + line + "\n"));
    }

    private void sendText() {
        // Send text message using the protocol.
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        inputField.clear();
        try {
            client.sendText(text);
        } catch (IOException ex) {
            appendSystem("Send failed: " + ex.getMessage());
        }
    }

    private void sendImage() {
        // Open file chooser and send image bytes.
        Window window = chatArea.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.jpeg", "*.png")
        );
        File file = chooser.showOpenDialog(window);
        if (file == null) {
            return;
        }
        try {
            client.sendImage(file);
            appendSystem("Uploading image: " + file.getName());
        } catch (IOException ex) {
            appendSystem("Image upload failed: " + ex.getMessage());
        }
    }
}
