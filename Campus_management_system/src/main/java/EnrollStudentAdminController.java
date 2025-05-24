package task.dbms_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EnrollStudentAdminController {

    @FXML
    private Button Add_btn;

    @FXML
    private TableColumn<Enrollment, String> ContactNo_Column;

    @FXML
    private ComboBox<String> Course_combo;

    @FXML
    private Button Editbtn;

    @FXML
    private TableColumn<Enrollment, String> Email_Column;

    @FXML
    private Button Removebtn;

    @FXML
    private ComboBox<String> Status_Combo;

    @FXML
    private TextField Student_id_field;

    @FXML
    private TableColumn<Enrollment, String> Username_Column;

    @FXML
    private Button backbtn;

    @FXML
    private TableColumn<Enrollment, Integer> id_Column;

    @FXML
    private TableColumn<Enrollment, String> name_Column;

    @FXML
    private TableView<Enrollment> tableViewdoctors;

    private ObservableList<Enrollment> enrollmentList = FXCollections.observableArrayList();
    private ObservableList<String> courseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize table columns
        id_Column.setCellValueFactory(new PropertyValueFactory<>("enrollmentId"));
        Username_Column.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        name_Column.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        ContactNo_Column.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));
        Email_Column.setCellValueFactory(new PropertyValueFactory<>("status"));
        backbtn.setOnAction(this::back_handler);

        // Populate course combo, status combo, and table
        populateCourseCombo();
        populateStatusCombo();
        populateTable();
    }

    private void populateStatusCombo() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList("ENROLLED", "NOT ENROLLED");
        Status_Combo.setItems(statusOptions);
    }

    private void populateCourseCombo() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT course_name FROM courses")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courseList.add(rs.getString("course_name"));
            }
            Course_combo.setItems(courseList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateTable() {
        enrollmentList.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT e.enrollment_id, e.student_id, c.course_name, e.enrollment_date, e.status " +
                             "FROM enrollments e " +
                             "JOIN courses c ON e.course_id = c.course_id")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                enrollmentList.add(new Enrollment(
                        rs.getInt("enrollment_id"),
                        rs.getString("student_id"),
                        rs.getString("course_name"),
                        rs.getString("enrollment_date"),
                        rs.getString("status")
                ));
            }
            tableViewdoctors.setItems(enrollmentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Add_handler(ActionEvent event) {
        String studentId = Student_id_field.getText();
        String courseName = Course_combo.getValue();
        String status = Status_Combo.getValue();

        if (studentId == null || courseName == null || status == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!");
            alert.show();
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement courseStmt = conn.prepareStatement("SELECT course_id FROM courses WHERE course_name = ?");
             PreparedStatement insertStmt = conn.prepareStatement(
                     "INSERT INTO enrollments (student_id, course_id, status) VALUES (?, ?, ?)")) {

            // Get course_id from course_name
            courseStmt.setString(1, courseName);
            ResultSet rs = courseStmt.executeQuery();
            if (rs.next()) {
                int courseId = rs.getInt("course_id");

                // Insert into enrollments
                insertStmt.setString(1, studentId);
                insertStmt.setInt(2, courseId);
                insertStmt.setString(3, status);
                insertStmt.executeUpdate();

                // Refresh table
                populateTable();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Enrollment added successfully!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course not found!");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Edit_handler(ActionEvent event) {
        Enrollment selectedEnrollment = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedEnrollment == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an enrollment to edit.");
            alert.show();
            return;
        }

        String studentId = Student_id_field.getText();
        String courseName = Course_combo.getValue();
        String status = Status_Combo.getValue();

        if (studentId == null || courseName == null || status == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!");
            alert.show();
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement courseStmt = conn.prepareStatement("SELECT course_id FROM courses WHERE course_name = ?");
             PreparedStatement updateStmt = conn.prepareStatement(
                     "UPDATE enrollments SET student_id = ?, course_id = ?, status = ? WHERE enrollment_id = ?")) {

            // Get course_id from course_name
            courseStmt.setString(1, courseName);
            ResultSet rs = courseStmt.executeQuery();
            if (rs.next()) {
                int courseId = rs.getInt("course_id");

                // Update the enrollment
                updateStmt.setString(1, studentId);
                updateStmt.setInt(2, courseId);
                updateStmt.setString(3, status);
                updateStmt.setInt(4, selectedEnrollment.getEnrollmentId());
                updateStmt.executeUpdate();

                // Refresh table
                populateTable();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Enrollment updated successfully!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course not found!");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Remove_handler(ActionEvent event) {
        Enrollment selectedEnrollment = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedEnrollment == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an enrollment to remove.");
            alert.show();
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Enrollment");
        confirmDialog.setContentText("Are you sure you want to delete this enrollment?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnector.getConnection();
                     PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM enrollments WHERE enrollment_id = ?")) {

                    // Delete the enrollment
                    deleteStmt.setInt(1, selectedEnrollment.getEnrollmentId());
                    deleteStmt.executeUpdate();

                    // Refresh table
                    populateTable();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Enrollment removed successfully!");
                    alert.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    void back_handler(ActionEvent event) {
        String role = Session.getRole();
        if ("teacher".equalsIgnoreCase(role)) {
            Move_page.navigateToPage(event, "Teacher_panel.fxml");
        } else {
            Move_page.navigateToPage(event, "Admin_panel.fxml");
        }
    }
}