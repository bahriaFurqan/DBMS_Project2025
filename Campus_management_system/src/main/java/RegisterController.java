package task.dbms_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML
    private TextField Contact_textfield;

    @FXML
    private DatePicker DOB_filed;

    @FXML
    private TextField Full_name_textfield;

    @FXML
    private ComboBox<String> Select_user;

    @FXML
    private Label label_fill;

    @FXML
    private Button register_button;

    @FXML
    private TextField register_email;

    @FXML
    private Hyperlink register_loginhere_link;

    @FXML
    private PasswordField register_password;

    @FXML
    private TextField register_username;

    @FXML
    public void initialize() {
        // Initialize the ComboBox with user roles
        Select_user.getItems().addAll("admin", "teacher", "student");
    }

    @FXML
    void Combobox_handler(ActionEvent event) {
        // Optional: Handle ComboBox selection changes if needed
    }

    @FXML
    void Loginpage_handler(ActionEvent event) {
        // Navigate to the login page
        Move_page.navigateToPage(event, "LOGIN_page.fxml");
    }

    @FXML
    void Register_userhandler(ActionEvent event) {
        String username = register_username.getText();
        String password = register_password.getText();
        String email = register_email.getText();
        String fullName = Full_name_textfield.getText();
        String contact = Contact_textfield.getText();
        String role = Select_user.getValue();
        java.time.LocalDate dob = DOB_filed.getValue();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty() || contact.isEmpty() || role == null || dob == null) {
            label_fill.setText("Please fill all fields!");
            return;
        }

        // Insert data into the request_approval table, including the password
        String query = "INSERT INTO request_approval (username, password, email, full_name, dob, contact, role) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, fullName);
            preparedStatement.setDate(5, java.sql.Date.valueOf(dob));
            preparedStatement.setString(6, contact);
            preparedStatement.setString(7, role);

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                label_fill.setText("Registration successful! Awaiting approval.");
            } else {
                label_fill.setText("Registration failed! Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            label_fill.setText("Database error!");
        }
    }
}