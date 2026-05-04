import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class QuickClient {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 5555;
        try (Socket s = new Socket(host, port)) {
            OutputStream out = s.getOutputStream();
            out.write("TEXT|QuickTester|Hello from QuickClient\n".getBytes());
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line = in.readLine();
            System.out.println("Server replied: " + line);
        }
    }
}
