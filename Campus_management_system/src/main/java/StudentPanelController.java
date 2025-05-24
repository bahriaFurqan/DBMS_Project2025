package task.dbms_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentPanelController {

    @FXML
    private Button Assignment_button;

    @FXML
    private Button Attendance_button;

    @FXML
    private Button Exam_button;

    @FXML
    private Button Timetable_button;

    @FXML
    private Text VIew_profile_Adminid;

    @FXML
    private Text VIew_profile_Name;

    @FXML
    private Text VIew_profile_contact;

    @FXML
    private Text VIew_profile_contact1;

    @FXML
    private Text VIew_profile_email;

    @FXML
    private Text VIew_profile_username;

    @FXML
    private Button View_profile;

    @FXML
    private AnchorPane View_profile_anchor;

    @FXML
    private Label adminid_label;

    @FXML
    private Label label_name;

    @FXML
    private Label label_name2;

    @FXML
    private Button logout;

    @FXML
    private AnchorPane welcome_profile_anchor;

    @FXML
    public void initialize() {
        // Initialize the controller
        View_profile_anchor.setVisible(false);
        welcome_profile_anchor.setVisible(true);

        // Set student ID and username from session
        String userId = Session.getUserId();
        String username = Session.getUsername();

        if (userId != null) {
            adminid_label.setText(userId);
        }

        if (username != null) {
            label_name.setText(username);
            label_name2.setText(username);
        }

        // Load student profile data if available
        loadStudentProfile();
    }

    private void loadStudentProfile() {
        String userId = Session.getUserId();
        if (userId == null) {
            return;
        }

        String query = "SELECT student_id, full_name, email, contact, username FROM students WHERE student_id = ?";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Set the values for display
                VIew_profile_Adminid.setText(resultSet.getString("student_id"));
                VIew_profile_Name.setText(resultSet.getString("full_name"));
                VIew_profile_email.setText(resultSet.getString("email"));
                VIew_profile_contact.setText(resultSet.getString("contact"));
                VIew_profile_username.setText(resultSet.getString("username"));

                // Also update the labels in the sidebar
                label_name.setText(resultSet.getString("full_name"));
                label_name2.setText(resultSet.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Assignment_button_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Assignment_student.fxml");
    }

    @FXML
    void Attendance_buttonhandler(ActionEvent event) {
        Move_page.navigateToPage(event, "Attendance_student.fxml");
    }

    @FXML
    void Exam_buttonhandler(ActionEvent event) {
        Move_page.navigateToPage(event, "Exam_student.fxml");
    }

    @FXML
    void Timetable_button_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Timetable_Student.fxml");
    }

    @FXML
    void View_Profile_handler(ActionEvent event) {
        // Show the profile anchor pane and hide welcome pane
        View_profile_anchor.setVisible(true);
        welcome_profile_anchor.setVisible(false);

        // Refresh student profile data
        loadStudentProfile();
    }

    @FXML
    void logouthandler(ActionEvent event) {
        // Clear the session and navigate to the login page
        Session.clearSession();
        Move_page.navigateToPage(event, "LOGIN_page.fxml");
    }
}