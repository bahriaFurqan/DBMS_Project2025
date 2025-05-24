package task.dbms_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Move_page {
    public static void navigateToPage(ActionEvent event, String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(Move_page.class.getResource(fxmlFileName));
            Parent pageParent = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene pageScene = new Scene(pageParent);
            stage.setScene(pageScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
