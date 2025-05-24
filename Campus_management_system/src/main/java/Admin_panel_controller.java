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

public class Admin_panel_controller {
    @FXML
    private Button EnrollStudent_button1;


    @FXML
    private Button Attendance_button;

    @FXML
    private Button Courses_button;

    @FXML
    private Button Exam_button;

    @FXML
    private Button Registration_req_button;

    @FXML
    private Button Student_button;

    @FXML
    private Button Teacher_button;

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
    private Button Timetable_button;

    @FXML
    public void initialize() {
        // Initialize the controller
        View_profile_anchor.setVisible(false);
        welcome_profile_anchor.setVisible(true);
        Student_button.setOnAction(this::Student_handler);
        Teacher_button.setOnAction(this::Teacher_handler);
        View_profile.setOnAction(this::View_Profile_handler);
        logout.setOnAction(this::logouthandler);

        Attendance_button.setOnAction(this::Attendance_buttonhandler);
        Courses_button.setOnAction(this::Courses_button_handler);
        Exam_button.setOnAction(this::Exam_buttonhandler);
        EnrollStudent_button1.setOnAction(this::EnrollStudent_button_handler);
        Registration_req_button.setOnAction(this::Registration_req_button_handler);
        Timetable_button.setOnAction(this::Timetable_button_handler);
    }
    @FXML
    void Timetable_button_handler(ActionEvent event) {
        // Handle timetable button click
        Move_page.navigateToPage(event, "Timetable_admin.fxml");
    }
    @FXML

    void EnrollStudent_button_handler(ActionEvent event) {
        // Handle enroll student button click
        Move_page.navigateToPage(event, "Enroll_Student_admin.fxml");
    }

    @FXML
    void Attendance_buttonhandler(ActionEvent event) {
        // Handle attendance button click
        Move_page.navigateToPage(event, "Attendace_admin.fxml");
    }

    @FXML
    void Courses_button_handler(ActionEvent event) {

        // Handle courses button click
        Move_page.navigateToPage(event, "Courses_add_admin.fxml");
    }

    @FXML
    void Exam_buttonhandler(ActionEvent event) {

        // Handle exam button click
        Move_page.navigateToPage(event, "Exam_admin.fxml");
    }

    @FXML
    void Registration_req_button_handler(ActionEvent event) {

        // Handle registration request button click
        Move_page.navigateToPage(event, "Request_approval.fxml");
    }

    @FXML
    void Student_handler(ActionEvent event) {
        // Handle student button click
        Move_page.navigateToPage(event, "Student_add_admin.fxml");

    }

    @FXML
    void Teacher_handler(ActionEvent event) {
        // Handle teacher button click
        Move_page.navigateToPage(event, "teacher_add_admin.fxml");
    }

    @FXML
    void View_Profile_handler(ActionEvent event) {
        String userId = Session.getUserId();

        if (userId == null) {
            // Handle case where session data is missing
            View_profile_anchor.setVisible(false);
            label_name.setText("Session data missing!");
            return;
        }

        String query = "SELECT admin_id, full_name, email, contact, username FROM admins WHERE admin_id = ?";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Populate profile information with data from the database
                VIew_profile_Adminid.setText(resultSet.getString("admin_id"));
                VIew_profile_Name.setText(resultSet.getString("full_name"));
                VIew_profile_email.setText(resultSet.getString("email"));
                VIew_profile_contact.setText(resultSet.getString("contact"));
                VIew_profile_username.setText(resultSet.getString("username"));

                // Make the profile anchor pane visible
                View_profile_anchor.setVisible(true);
            } else {
                // Handle case where no admin is found
                View_profile_anchor.setVisible(false);
                label_name.setText("No admin found with the given ID!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle database error
            View_profile_anchor.setVisible(false);
            label_name.setText("Database error! Please check the logs.");
        }
    }
    @FXML
    void logouthandler(ActionEvent event) {
        // Clear the session and navigate to the login page
        Session.clearSession();
        Move_page.navigateToPage(event, "LOGIN_page.fxml");
    }
}