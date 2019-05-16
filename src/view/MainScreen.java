package view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import model.LVS;

import java.io.IOException;
import java.util.ArrayList;


public class MainScreen {

    @FXML
    TextArea console;
    @FXML
    ToggleButton turnButton;
    @FXML
    Button execButton;
    @FXML
    Button stopButton;
    @FXML
    Button profileButton;
    @FXML
    Text statePrompt;
    @FXML
    Pane lvsPane;
    @FXML
    Line lineA;
    @FXML
    Line lineB;

    private ArrayList<VisualDevice> visualDevices;
    private LVS lvs;
    private WindowManager manager;
    private LVS.LineStateProperty lineStateProperty = new LVS.LineStateProperty(LVS.LineState.A_WORKING);

    void setManager(WindowManager manager) {
        this.manager = manager;
    }

    private void addToConsole(String string){

        console.setText(console.getText() + string + "\n");

        console.selectPositionCaret(console.getLength());
        console.deselect();
    }

    private void cleanConsole(String prompt){

        console.clear();
        console.setPromptText(prompt);
    }

    @FXML
    void initialize(){

        visualDevices = new ArrayList<>();

        execButton.setDisable(true);
        console.setEditable(false);

        turnButton.selectedProperty().addListener((observable, oldValue, newValue) -> {

            execButton.setDisable(!newValue);
            lineA.setStroke(Paint.valueOf( newValue ? "#5bd983" : "#b1b1b1"));
            lineB.setStroke(Paint.valueOf("#b1b1b1"));
        });

        lvs = new LVS(true, 250, 18,20000,5000,2000,2000);
        lineStateProperty.bind(lvs.getLineStateProperty());

        lineStateProperty.addListener((o, old, value) -> Platform.runLater(()-> {
            if (value == LVS.LineState.A_WORKING) {

                addToConsole("*Линия А активна*");

                lineA.setStroke(Paint.valueOf("#5bd983"));
                lineB.setStroke(Paint.valueOf("#b1b1b1"));
            }
            if (value == LVS.LineState.B_WORKING) {

                addToConsole("*Запущена линия B*");

                lineA.setStroke(Paint.valueOf("#b1b1b1"));
                lineB.setStroke(Paint.valueOf("#5bd983"));
            }
            if (value == LVS.LineState.A_GENERATION) {

                addToConsole("*Обнаружена генерация на линии А*");

                lineA.setStroke(Paint.valueOf("#1F9DFF"));
                lineB.setStroke(Paint.valueOf("#b1b1b1"));
            }
        }));

        try {
            for (int i = 0; i < lvs.getClientsAmount(); i++){
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        i % 2 == 0 ? "DeviceUpper.fxml" : "DeviceLower.fxml"));

                Node elm = loader.load();

                visualDevices.add(loader.getController());
                visualDevices.get(i).setTerminalDevice(i, lvs.getClients().get(i));
                visualDevices.get(i).setConsole(console);
                visualDevices.get(i).powerSwitch();
                lvsPane.getChildren().add(elm);
                elm.setLayoutX(i*34 + 10);
                elm.setLayoutY(i % 2 == 0 ? 10 : 107);
            }

        } catch (IOException e){

            lvs = null;
            lvsPane.getChildren().clear();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Ошибка инициализации ЛВС");
            alert.setTitle("Error: внутренняя ошибка");
            alert.setContentText("Сообщение ошибки:\n" + e.getCause() + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void turnHandle(){

        turnButton.setText(turnButton.isSelected() ? "ВЫКЛЮЧИТЬ СЕТЬ" : "ВКЛЮЧИТЬ СЕТЬ");

        statePrompt.setText("ЛВС "
                + (turnButton.isSelected() ? "включена" : "отключена"));

        for (VisualDevice visualDevice : visualDevices)
            visualDevice.powerSwitch();

        cleanConsole(turnButton.isSelected() ? "*СИСТЕМА ЛВС ВКЛЮЧЕНА*\n> " : "*ОТКЛЮЧЕНО*");
    }

    @FXML
    void execHandle(){

        cleanConsole("*Запуск контроллера сети*\n");

        turnUI();
        Runnable r = () -> {
            try {

                lvs.start(new ArrayList<Double>(){{
                    for (int i = 0; i < 5; i++) add(.0);

                }});
                Platform.runLater(this::turnUI);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        (new Thread(r)).start();
    }

    @FXML
    void stopHandle(){

    }

    private void turnUI() {

        turnButton.setDisable(!turnButton.isDisabled());
        execButton.setDisable(!execButton.isDisabled());
        profileButton.setDisable(!profileButton.isDisabled());
        for (VisualDevice v : visualDevices)
            v.disableButton(!v.tdStateButton.isDisabled());
    }

    @FXML
    void profileHandle() { manager.testWindow(); }

}
