package Main;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Controller {
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button xButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label label;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button sendButton;
    @FXML
    private TextArea textArea;

    private static String DNS = "mariancr.go.ro";
    private byte loginRequestProtocol = 2;
    private static Socket socket;
    private static boolean wasRegistered = false;
    private static InetAddress address;
    private static boolean wasSocketInitialized=false;


    public void initialize() throws InterruptedException, UnknownHostException {
      if (!wasSocketInitialized) {
          initSocket();
      }
        formatTextField(username);
        formatTextField(password);
        label.setVisible(false);
        progressIndicator.setVisible(false);
        if (wasRegistered){
            this.label.setText("    You have been registered , please log in.");
            this.label.setVisible(true);
        }
    }

    private void initSocket() {
        try {
            address = InetAddress.getByName(DNS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            socket = new Socket("localhost", 3333);
            wasSocketInitialized = true;
        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR,"Failed to connect to the server, please restart the application !",ButtonType.OK);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Connection Error");
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void setWasRegistered(boolean wasRegistered) {
        Controller.wasRegistered = wasRegistered;
    }

    public static void resetSocket() throws IOException {
        socket = new Socket("localhost",3333);
    }

    static Socket getSocket(){
        return socket;
    }

    public void onRegisterClick() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("register.fxml"));
        Stage previousStage = (Stage) borderPane.getScene().getWindow();
        Scene scene = new Scene(root, 400, 449);
        previousStage.setScene(scene);


    }

    public void onClick() {
        Stage stage = (Stage) xButton.getScene().getWindow();
        stage.close();
    }

    public void onLoginButtonPress() throws IOException, InterruptedException {
        String password = this.password.getText();
        String username = this.username.getText();


        if (this.password.getText().isEmpty() || this.username.getText().isEmpty()) {
            this.label.setVisible(true);
            return;
        }


        registerButton.setDisable(true);
        loginButton.setDisable(true);
        this.username.setDisable(true);
        this.password.setDisable(true);
        progressIndicator.setVisible(true);


        Service<Boolean> service = new Service<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        System.out.println("Check Database Service on login : on");
                        return checkDatabase(username, password);
                    }
                };
            }
        };
        service.start();
        resetSocket();
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                if (service.getValue().equals(true)) {
                    /*System.out.println("Exista");*/
                    Stage closingStage = (Stage) borderPane.getScene().getWindow();
                    closingStage.close();

                    Parent root = null;
                    try {
                        root = FXMLLoader.load(getClass().getResource("mainChat.fxml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Stage stage = new Stage();
                    Scene scene = new Scene(root, 1080.0, 720.0);
                    stage.setScene(scene);
                    stage.setResizable(true);
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.show();


                } else {
                    /*System.out.println("Nu exista");*/
                    try {
                        System.out.println("Resetare Socket on Login");
                        resetSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    enableButtonsAndFields();
                }

            }
        });
    }

    private void enableButtonsAndFields() {
        progressIndicator.setVisible(false);
        registerButton.setDisable(false);
        loginButton.setDisable(false);
        this.username.setDisable(false);
        this.password.setDisable(false);
        this.label.setVisible(true);
        this.label.setText("Unknown username/password please register or try again.");
        this.label.setFont(Font.font(14));
    }

    private boolean checkDatabase(String username, String password) {
        try {

             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.write(loginRequestProtocol + "\n");
            out.flush();
            out.write(username + "\n");
            out.flush();
            out.write(password + "\n");
            out.flush();

            // in.mark(1);
            int returnedValue =  in.read();
            // in.reset();

            System.out.println("Login retvalt : "+returnedValue);

            return returnedValue == 1;

        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;

    }

    public void formatTextField(TextField textField) {
        TextFormatter<?> formatter = new TextFormatter<>((TextFormatter.Change change) -> {
            String text = change.getText();

            // if text was added, fix the text to fit the requirements
            if (!text.isEmpty()) {
                String newText = text.replace(" ", "").toLowerCase();

                int carretPos = change.getCaretPosition() - text.length() + newText.length();
                change.setText(newText);

                // fix carret position based on difference in originally added text and fixed text
                change.selectRange(carretPos, carretPos);
            }
            return change;
        });

        textField.setTextFormatter(formatter);
    }
}
