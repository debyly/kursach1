import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import javafx.application.Application;

public class Main extends Application {

    /*private static String mainfxml = "res/sample.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource(mainfxml));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        *//* test of repo cloning *//*
        //Another one shit
        //second one
        //I DON UBawfaw

    }*/


    public static void main(String[] args) {
        launch(args);


    }

    @Override
    public void start(Stage primaryStage) throws Exception {
            primaryStage.show();
            LVS lvs = new LVS();
            lvs.working_20000();
    }
}
