package client.ui;

import client.network.ChatClient;
import common.protocol.ProtocolParser;
import common.protocol.messages.ErrMessage;
import common.protocol.messages.OkMessage;
import common.protocol.messages.ProtocolMessage;
import common.protocol.messages.TextMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * JavaFX controller for the chat UI.
 * <p>
 * Rules:
 * - Never update JavaFX UI from the network thread.
 * - Use Platform.runLater when handling messages from ChatClient listener.
 * - UI depends on ChatClient via injection (setter).
 */
public class ChatController {
    @FXML
    private ListView<MessageViewModel> messageList;

    @FXML
    private TextField inputField;

    @FXML
    private Button sendButton;

    @FXML
    private Button imageButton;

    private final ObservableList<MessageViewModel> messages = FXCollections.observableArrayList();
    private final ProtocolParser protocolParser = new ProtocolParser();

    private ChatClient chatClient;
    private String currentUsername = "User";
    private String pendingLocalImagePath;
    private String pendingLocalImageName;

    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;

        this.chatClient.setMessageListener(line -> Platform.runLater(() -> onServerLine(line)));
    }

    public void setCurrentUsername(String currentUsername) {
        if (currentUsername != null && !currentUsername.trim().isEmpty()) {
            this.currentUsername = currentUsername.trim();
        }
    }

    public void connectAsync() {
        if (chatClient == null) {
            addMessage(MessageViewModel.system("ChatClient not injected."));
            return;
        }

        Thread connectThread = new Thread(() -> {
            try {
                chatClient.connect();
                Platform.runLater(() -> addMessage(MessageViewModel.system("Connected.")));
            } catch (IOException ex) {
                Platform.runLater(() -> addMessage(MessageViewModel.system("Connection failed: " + ex.getMessage())));
            }
        });
        connectThread.setDaemon(true);
        connectThread.start();
    }

    @FXML
    private void initialize() {
        messageList.setItems(messages);
        messageList.setCellFactory(list -> new MessageCell(() -> this.currentUsername));

        sendButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> inputField.getText() == null || inputField.getText().trim().isEmpty(),
                inputField.textProperty()
        ));

        inputField.setOnAction(event -> onSend());
    }

    @FXML
    private void onSend() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        inputField.clear();

        if (chatClient == null) {
            addMessage(MessageViewModel.system("ChatClient not injected."));
            return;
        }

        Thread sendThread = new Thread(() -> {
            try {
                chatClient.sendText(text);
            } catch (IOException ex) {
                Platform.runLater(() -> addMessage(MessageViewModel.system("Send failed: " + ex.getMessage())));
            }
        });
        sendThread.setDaemon(true);
        sendThread.start();
    }

    @FXML
    private void onImage() {
        if (chatClient == null) {
            addMessage(MessageViewModel.system("ChatClient not injected."));
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.jpeg", "*.png")
        );
        File file = chooser.showOpenDialog(messageList.getScene().getWindow());
        if (file == null) {
            return;
        }

        pendingLocalImagePath = file.getAbsolutePath();
        pendingLocalImageName = file.getName();
        addMessage(MessageViewModel.system("Uploading image: " + file.getName()));

        Thread sendThread = new Thread(() -> {
            try {
                chatClient.sendImage(file);
            } catch (IOException ex) {
                Platform.runLater(() -> addMessage(MessageViewModel.system("Image upload failed: " + ex.getMessage())));
            }
        });
        sendThread.setDaemon(true);
        sendThread.start();
    }

    private void onServerLine(String line) {
        ProtocolMessage message = protocolParser.parseLine(line);

        if (message instanceof TextMessage textMessage) {
            String username = textMessage.username();
            String content = textMessage.message();
            boolean isSystem = "SERVER".equalsIgnoreCase(username) || "SYSTEM".equalsIgnoreCase(username);

            if (isSystem) {
                addMessage(MessageViewModel.system(content));
            } else {
                addMessage(MessageViewModel.text(username, content));
            }
            return;
        }

        if (message instanceof OkMessage okMessage) {
            if ("IMAGE".equalsIgnoreCase(okMessage.context())) {
                addMessage(MessageViewModel.system("Image accepted: " + okMessage.detail()));
                if (pendingLocalImagePath != null) {
                    addMessage(MessageViewModel.image(currentUsername, "sent image: " + pendingLocalImageName, pendingLocalImagePath));
                    pendingLocalImagePath = null;
                    pendingLocalImageName = null;
                }
            } else {
                addMessage(MessageViewModel.system("OK: " + okMessage.context() + " - " + okMessage.detail()));
            }
            return;
        }

        if (message instanceof ErrMessage errMessage) {
            addMessage(MessageViewModel.system("ERR: " + errMessage.context() + " - " + errMessage.code() + " - " + errMessage.message()));
            if ("IMAGE".equalsIgnoreCase(errMessage.context())) {
                pendingLocalImagePath = null;
                pendingLocalImageName = null;
            }
            return;
        }

        addMessage(MessageViewModel.system(line));
    }

    private void addMessage(MessageViewModel viewModel) {
        messages.add(viewModel);
        messageList.scrollTo(messages.size() - 1);
    }

    private static final class MessageCell extends ListCell<MessageViewModel> {
        private final UsernameSupplier usernameSupplier;

        private MessageCell(UsernameSupplier usernameSupplier) {
            this.usernameSupplier = usernameSupplier;
        }

        @Override
        protected void updateItem(MessageViewModel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().removeAll("system-cell", "text-cell", "image-cell");
                return;
            }

            getStyleClass().removeAll("system-cell", "text-cell", "image-cell");

            if (item.isSystem()) {
                getStyleClass().add("system-cell");
                Text text = new Text("[SYSTEM] " + item.getContent());
                text.getStyleClass().add("system-text");
                setGraphic(text);
                return;
            }

            if (item.isImage()) {
                getStyleClass().add("image-cell");
                VBox box = new VBox(6);

                Text header = new Text(item.getUsername() + " " + item.getContent());
                header.getStyleClass().add("message-header");
                box.getChildren().add(header);

                if (item.getImagePath() != null) {
                    try (FileInputStream in = new FileInputStream(item.getImagePath())) {
                        Image image = new Image(in, 280, 0, true, true);
                        ImageView imageView = new ImageView(image);
                        imageView.getStyleClass().add("message-image");
                        box.getChildren().add(imageView);
                    } catch (IOException ignored) {
                    }
                }

                HBox container = new HBox();
                Region bubble = new VBox(box);
                bubble.getStyleClass().add("bubble");
                applyDirection(container, bubble, item.getUsername());
                container.getChildren().add(bubble);
                setGraphic(container);
                return;
            }

            getStyleClass().add("text-cell");
            Text text = new Text(item.getUsername() + ": " + item.getContent());
            text.getStyleClass().add("message-text");

            HBox container = new HBox();
            Region bubble = new VBox(text);
            bubble.getStyleClass().add("bubble");
            applyDirection(container, bubble, item.getUsername());
            container.getChildren().add(bubble);
            setGraphic(container);
        }

        private void applyDirection(HBox container, Region bubble, String username) {
            String current = usernameSupplier.get();
            boolean self = current != null && username != null && current.equalsIgnoreCase(username);
            bubble.getStyleClass().removeAll("bubble-self", "bubble-other");
            bubble.getStyleClass().add(self ? "bubble-self" : "bubble-other");

            container.getStyleClass().removeAll("row-self", "row-other");
            container.getStyleClass().add(self ? "row-self" : "row-other");
        }
    }

    @FunctionalInterface
    private interface UsernameSupplier {
        String get();
    }
}
