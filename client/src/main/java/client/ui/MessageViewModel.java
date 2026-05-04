package client.ui;

/**
 * UI model for rendering messages in the chat ListView.
 */
public class MessageViewModel {
    private final String username;
    private final String content;
    private final boolean system;
    private final boolean image;
    private final String imagePath;

    public MessageViewModel(String username, String content, boolean system, boolean image, String imagePath) {
        this.username = username;
        this.content = content;
        this.system = system;
        this.image = image;
        this.imagePath = imagePath;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isImage() {
        return image;
    }

    public String getImagePath() {
        return imagePath;
    }

    public static MessageViewModel system(String content) {
        return new MessageViewModel("SYSTEM", content, true, false, null);
    }

    public static MessageViewModel text(String username, String content) {
        return new MessageViewModel(username, content, false, false, null);
    }

    public static MessageViewModel image(String username, String content, String imagePath) {
        return new MessageViewModel(username, content, false, true, imagePath);
    }
}

