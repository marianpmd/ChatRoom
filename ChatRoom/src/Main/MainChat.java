package Main;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MainChat {
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button xButton;
    @FXML
    private Button minusButton;
    @FXML
    private Button squareButton;
    @FXML
    private Button offButton;
    @FXML
    private TextArea textArea;
    @FXML
    private VBox vBox;
    @FXML
    private HBox top;
    @FXML
    private ScrollPane scrollPane;

    private CurrentUser currentUser = new CurrentUser();
    private static Socket socket;
    private MessagingManager messagingManager;
    private BufferedReader in;
    private ArrayList<String>messageArray=new ArrayList<>();

    private double xOffset = 0;
    private double yOffset = 0;


    public void initialize() throws IOException {
        this.vBox.setMinHeight(this.scrollPane.getMinHeight());
        this.vBox.setMinWidth(this.scrollPane.getMinWidth());
        this.vBox.setAlignment(Pos.CENTER_LEFT);
        this.textArea.setMinWidth(this.borderPane.getMinWidth() - 40);
        this.textArea.setMinHeight(40.0);
        socket = Controller.getSocket();
        this.in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Scanner scanner = new Scanner(in);
        try {
            requestPublicUserData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        printInChat("Welcome to ChatRoom, for a list of commands type /commands. ");
        MessagingManager obs = new MessagingManager(socket);
        Listener listener = new Listener();
        obs.register(listener);
        obs.connect();

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                System.out.println("Is Fx App Thread : " + Platform.isFxApplicationThread());
                while (true) {
                    Thread.sleep(1);
                    if (listener.getValue() == null){
                        //
                    }else{
                        System.out.println("The message is : " + listener.getValue());
                        messageArray.add(listener.getValue());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                printInChat(listener.getValue());
                                listener.setValue(null);
                            }
                        });
                    }
                }
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        top.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        top.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                top.getScene().getWindow().setX(event.getScreenX() - xOffset);
                top.getScene().getWindow().setY(event.getScreenY() - yOffset);
            }
        });


    }
    public void printInChat(String message){

        if (message!=null){
            String toPrint = message.replaceAll("\n", "");
            vBox.heightProperty().addListener(observable -> scrollPane.setVvalue(1D));

            Label label = new Label();
            label.setAlignment(Pos.CENTER_LEFT);
            label.setTextAlignment(TextAlignment.LEFT);
            label.setFont(Font.font(18));
            label.setLayoutY(19.0);
            label.getStylesheets().add(getClass().getResource("chatBubble.css").toString());
            label.setText(toPrint);
            vBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            vBox.getChildren().add(label);
        }
    }
    public void requestPublicUserData() throws IOException {
        currentUser.setId(in.read());
        currentUser.setName(in.readLine());
        System.out.println(currentUser.toString());

    }


    public void onOffButtonClick(ActionEvent event) throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.write("/exit");
        printWriter.flush();
        socket.close();
        Stage stage = (Stage) borderPane.getScene().getWindow();
        stage.close();

    }

    public void onSquareButtonClick(ActionEvent event) {
        Stage stage = (Stage) borderPane.getScene().getWindow();
        if (stage.isMaximized()) {
            this.textArea.setMinWidth(borderPane.getMinWidth() - 40);
            stage.setMaximized(false);
        } else {
            this.textArea.setMinWidth(stage.getMinWidth());
            stage.setMaximized(true);
        }
    }

    public void onMinusButtonClick(ActionEvent event) {
        Stage stage = (Stage) borderPane.getScene().getWindow();
        stage.setIconified(true);
    }

    public void onSendButtonPress(ActionEvent event) throws IOException {
        String message = textArea.getText();

        if (message.isBlank() || message.equals("\n")) {
            printInChat(null);
            textArea.setText(null);
        }else if (message.startsWith("/")){
            performAction(message);
            textArea.setText(null);
        }else {
            StringBuilder messageAndName = new StringBuilder();
            messageAndName.append(currentUser.getName());
            messageAndName.append(" : ").append(message);
            printInChat(messageAndName.toString());

            Service<String> service = new Service<String>() {
                @Override
                protected Task<String> createTask() {
                    return new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            PrintWriter out = new PrintWriter(socket.getOutputStream());
                            out.write(messageAndName.toString() + "\n");
                            out.flush();
                            return null;
                        }
                    };
                }
            };
            service.start();
        }
        textArea.setText(null);
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        String message = textArea.getText();
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            if (message.isBlank()){
                printInChat(null);
                textArea.setText(null);
            }else if (message.startsWith("/")) {
                performAction(message.replaceAll("\n",""));
                textArea.setText(null);
            }else {
                StringBuilder messageAndName = new StringBuilder();
                messageAndName.append(currentUser.getName());
                messageAndName.append(" : ").append(message.replaceAll("\n", ""));
                printInChat(messageAndName.toString());


                Service<String> service = new Service<String>() {
                    @Override
                    protected Task<String> createTask() {
                        return new Task<String>() {
                            @Override
                            protected String call() throws Exception {
                                PrintWriter out = new PrintWriter(socket.getOutputStream());
                                out.write(messageAndName + "\n");
                                out.flush();
                                return null;
                            }
                        };
                    }
                };
                service.start();
                textArea.setText(null);

            }

        }
    }

    private void performAction(String message) throws IOException {
        switch (message) {
            case "/commands":
                printInChat("The commands are : " + commandList());
                break;
            case "/exit":
                /*Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                PrintWriter out = new PrintWriter(socket.getOutputStream());
                                out.write("/exit" + "\n");
                                out.flush();
                                return null;
                            }
                        };
                    }
                };
                service.start();*/
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.write("/exit" + "\n");
                out.flush();
                socket.close();
                Platform.exit();

                break;
            case "/time":
                printInChat("Time is : " + new Date());
                break;
        }
    }

    private String commandList() {
        return "/commands;/exit;/time";
    }


}
