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
import java.util.HashMap;
import java.util.Map;

public class AttendanceStudentController {

    @FXML
    private TableColumn<AttendanceEntry, String> AttendacedateColumn;

    @FXML
    private TableColumn<AttendanceEntry, Integer> AttendanceColumn;

    @FXML
    private ComboBox<String> Course_Combo; // Display course names

    @FXML
    private TableColumn<AttendanceEntry, Integer> EnrollmentidColumn;

    @FXML
    private TableColumn<AttendanceEntry, String> RemarksColumn;

    @FXML
    private TableColumn<AttendanceEntry, Integer> TimetableidColumn;

    @FXML
    private TableView<AttendanceEntry> attendanceTable;

    @FXML
    private Button backbtn;

    @FXML
    private Button searchbtn;

    @FXML
    private TableColumn<AttendanceEntry, String> statusColumn;

    @FXML
    private TableColumn<AttendanceEntry, String> studentNameColumn;

    private ObservableList<AttendanceEntry> attendanceList = FXCollections.observableArrayList();
    private Map<String, Integer> courseMap = new HashMap<>(); // Map course names to IDs

    @FXML
    public void initialize() {
        // Bind TableView columns to AttendanceEntry class properties
        AttendanceColumn.setCellValueFactory(new PropertyValueFactory<>("attendanceId"));
        EnrollmentidColumn.setCellValueFactory(new PropertyValueFactory<>("enrollmentId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        TimetableidColumn.setCellValueFactory(new PropertyValueFactory<>("timetableId"));
        AttendacedateColumn.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        RemarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        // Populate the Course ComboBox
        populateCourseComboBox();
    }

    private void populateCourseComboBox() {
        Integer studentId;
        try {
            studentId = Integer.parseInt(Session.getUserId()); // Fetch student ID from session
        } catch (NumberFormatException e) {
            showErrorAlert("Error", "Invalid student ID in session.");
            return;
        }

        if (studentId == null) {
            showErrorAlert("Error", "Student ID not found in session.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DISTINCT c.course_id, c.course_name " +
                             "FROM enrollments e " +
                             "JOIN courses c ON e.course_id = c.course_id " +
                             "WHERE e.student_id = ?")) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                int courseId = rs.getInt("course_id");
                courseMap.put(courseName, courseId); // Map course name to course ID
                Course_Combo.getItems().add(courseName); // Add course name to ComboBox
            }
        } catch (Exception e) {
            showErrorAlert("Error Loading Courses", e.getMessage());
        }
    }

    @FXML
    void searchHandler(ActionEvent event) {
        String selectedCourseName = Course_Combo.getValue();
        if (selectedCourseName == null) {
            showErrorAlert("Input Error", "Please select a course.");
            return;
        }

        Integer selectedCourseId = courseMap.get(selectedCourseName); // Get course ID from map
        if (selectedCourseId == null) {
            showErrorAlert("Error", "Invalid course selected.");
            return;
        }

        attendanceList.clear();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.attendance_id, a.enrollment_id, e.student_id, s.full_name AS student_name, " +
                             "a.timetable_id, a.attendance_date, a.status, a.remarks " +
                             "FROM attendance a " +
                             "JOIN enrollments e ON a.enrollment_id = e.enrollment_id " +
                             "JOIN students s ON e.student_id = s.student_id " +
                             "WHERE e.course_id = ? AND e.student_id = ?")) {
            stmt.setInt(1, selectedCourseId);
            stmt.setInt(2, Integer.parseInt(Session.getUserId())); // Fetch student ID from session
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(new AttendanceEntry(
                        rs.getInt("attendance_id"),
                        rs.getInt("enrollment_id"),
                        rs.getInt("timetable_id"),
                        rs.getString("student_name"),
                        rs.getString("attendance_date"),
                        rs.getString("status"),
                        rs.getString("remarks")
                ));
            }
            attendanceTable.setItems(attendanceList);
        } catch (Exception e) {
            showErrorAlert("Error Loading Attendance", e.getMessage());
        }
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Student_panel.fxml");
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}