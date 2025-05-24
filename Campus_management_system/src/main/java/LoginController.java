package task.dbms_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private Label label;

    @FXML
    private Button login_button;

    @FXML
    private AnchorPane login_form;

    @FXML
    private PasswordField login_password;

    @FXML
    private Hyperlink login_registerhere_link;

    @FXML
    private ComboBox<String> login_user;

    @FXML
    private TextField login_username;

    // Initialize the ComboBox with user roles
    @FXML
    public void initialize() {
        login_user.getItems().addAll("admin", "teacher", "student");
    }

    @FXML
    void Combobox_handler(ActionEvent event) {
        // Optional: Handle ComboBox selection changes if needed
    }

    @FXML
    void login_handler(ActionEvent event) {
        String username = login_username.getText();
        String password = login_password.getText();
        String role = login_user.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            label.setText("Please fill all fields!");
            return;
        }

        String query = null;
        String idColumn = null;

        // Determine the query and ID column based on the selected role
        if ("admin".equals(role)) {
            query = "SELECT admin_id, username, full_name FROM admins WHERE username = ? AND password = ?";
            idColumn = "admin_id";
        } else if ("teacher".equals(role)) {
            query = "SELECT teacher_id, username, full_name FROM teachers WHERE username = ? AND password = ?";
            idColumn = "teacher_id";
        } else if ("student".equals(role)) {
            query = "SELECT student_id, username, full_name FROM students WHERE username = ? AND password = ?";
            idColumn = "student_id";
        } else {
            label.setText("Invalid role selected!");
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Dynamically retrieve the ID column
                Session.setUserId(resultSet.getString(idColumn));
                Session.setUsername(resultSet.getString("username"));
                Session.setRole(role);

                label.setText("Login successful! Welcome, " + resultSet.getString("full_name"));

                // Navigate to the appropriate dashboard
                if ("admin".equals(role)) {
                    Move_page.navigateToPage(event, "Admin_panel.fxml");
                } else if ("teacher".equals(role)) {
                    Move_page.navigateToPage(event, "Teacher_panel.fxml");
                } else if ("student".equals(role)) {
                    Move_page.navigateToPage(event, "Student_panel.fxml");
                }
            } else {
                label.setText("Invalid credentials!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Database error! Please check the logs.");
        }
    }

    @FXML
    void register_page_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "REGISTER_page.fxml");
    }
}