package Main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class MessagingManager {
    private String value;
    private Socket socket;
    private Scanner input;
    private Listener listener;

    public MessagingManager(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new Scanner(new InputStreamReader(socket.getInputStream()));
    }

    public void connect(){
        Thread thread = new Thread(){
            @Override
            public void run() {
                while (true) {
                    if (input.hasNextLine())
                    updateMessageListener(input.nextLine().replaceAll("\n", ""));
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

    }

    private void updateMessageListener(String value) {
        this.listener.update(value);
    }

    public void register(Listener listener){
        this.listener = listener;
    }

}
