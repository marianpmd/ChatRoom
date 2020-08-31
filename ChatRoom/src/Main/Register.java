package Main;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Register {
    @FXML
    private BorderPane pane;
    @FXML
    private Button backButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField nickName;
    @FXML
    private TextField userName;
    @FXML
    private TextField password;
    @FXML
    private Label alert;
    @FXML
    private ProgressIndicator progressIndicator;

    private byte registerRequestProtocol = 1;
    private static Socket socket ;
    private static InetAddress address;
    private String DNS = "marianchatroom.go.ro";



    public void initialize() throws UnknownHostException {
        socket = Controller.getSocket();
        addTextLimiter(nickName,15);
        addTextLimiter(userName,15);
        addTextLimiter(password,15);
        formatTextField(nickName);
        formatTextField(userName);
        formatTextField(password);
        progressIndicator.setVisible(false);

    }

    private static void resetSocket() throws IOException {
        socket = new Socket(address,3999);
    }

    public void onBackButtonPress() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("mainLogin.fxml"));
        Stage previousStage = (Stage) pane.getScene().getWindow();
        Scene scene = new Scene(root,400,450);
        previousStage.setScene(scene);
    }

    public void onRegisterButtonPress() throws IOException {

        String nickName = this.nickName.getText();
        String userName = this.userName.getText();
        String password = this.password.getText();

        if (nickName.isEmpty() || userName.isEmpty() || password.isEmpty()){
            alert.setText("Please input all your information first !");
            alert.setVisible(true);
            return;
        }

        if (password.length() < 6){
            alert.setText("Password must be 6 characters or longer !");
            alert.setVisible(true);
            return;
        }

        progressIndicator.setVisible(true);
        this.nickName.setDisable(true);
        this.userName.setDisable(true);
        this.password.setDisable(true);
        this.registerButton.setDisable(true);
        this.backButton.setDisable(true);


        Service<Boolean> service = new Service<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        //Thread.sleep(1000);
                        return register(nickName, userName, password);
                    }
                };
            }
        };
        service.start();
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                if (service.getValue().equals(true)){
                    Controller.setWasRegistered(true);
                    Parent root = null;
                    try {
                        root = FXMLLoader.load(getClass().getResource("mainLogin.fxml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Stage previousStage = (Stage) pane.getScene().getWindow();
                    Scene scene = new Scene(root,400,450);
                    previousStage.setScene(scene);

                }else {
                    try {
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
        this.nickName.setDisable(false);
        this.userName.setDisable(false);
        this.password.setDisable(false);
        this.backButton.setDisable(false);
        this.registerButton.setDisable(false);
        this.alert.setText("User may already exist.");
        this.alert.setVisible(true);
    }

    public static void addTextLimiter(final TextField tf, final int maxLength) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (tf.getText().length() > maxLength) {
                    String s = tf.getText().substring(0, maxLength);
                    tf.setText(s);
                }
            }
        });
    }

    public void formatTextField(TextField textField){
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

    private boolean register(String nickName, String userName,String password) throws IOException {

        try {
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.write(registerRequestProtocol +"\n");
            out.flush();
            out.write(nickName + "\n");
            out.flush();
            out.write(userName + "\n");
            out.flush();
            out.write(password + "\n");
            out.flush();

            int returnedVal =  in.read();
          //  System.out.println("I gotthe value " + returnedVal);
            return returnedVal == 1;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
