import java.io.*;
import java.net.*;

public class BPMClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("Received BPM list: " + line);
            // Optionally parse JSON into array
        }

        in.close();
        socket.close();
    }
}