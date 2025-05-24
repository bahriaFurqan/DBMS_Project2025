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

public class AttendaceAdminController {

    @FXML
    private ComboBox<Integer> Enrollment_id_Combo;

    @FXML
    private TableColumn<AttendanceEntry, String> AttendacedateColumn;

    @FXML
    private TableColumn<AttendanceEntry, Integer> AttendanceColumn;

    @FXML
    private TableColumn<AttendanceEntry, Integer> EnrollmentidColumn;

    @FXML
    private TableColumn<AttendanceEntry, String> RemarksColumn;

    @FXML
    private TextField Remarks_field;

    @FXML
    private ComboBox<String> Starttime_Combo;

    @FXML
    private TableColumn<AttendanceEntry, Integer> TimetableidColumn;

    @FXML
    private TableView<AttendanceEntry> attendanceTable;

    @FXML
    private Button backbtn;

    @FXML
    private Button editbtn;

    @FXML
    private Button markbutn;

    @FXML
    private Button searchbtn;

    @FXML
    private TableColumn<AttendanceEntry, String> statusColumn;

    @FXML
    private TableColumn<AttendanceEntry, String> studentNameColumn;

    @FXML
    private ComboBox<Integer> timetableid_Combo;

    private ObservableList<AttendanceEntry> attendanceList = FXCollections.observableArrayList();

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

        // Populate ComboBoxes
        populateTimetableComboBox();
        populateStatusComboBox();

        // Add listener to timetableid_Combo to populate Enrollment_id_Combo
        timetableid_Combo.setOnAction(event -> populateEnrollmentComboBox());

        // Load attendance data into the TableView
        loadAttendance();
    }

    private void loadAttendance() {
        attendanceList.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.attendance_id, a.enrollment_id, e.student_id, s.full_name AS student_name, " +
                             "a.timetable_id, a.attendance_date, a.status, a.remarks " +
                             "FROM attendance a " +
                             "JOIN enrollments e ON a.enrollment_id = e.enrollment_id " +
                             "JOIN students s ON e.student_id = s.student_id")) {
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

    private void populateTimetableComboBox() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT timetable_id FROM timetable")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                timetableid_Combo.getItems().add(rs.getInt("timetable_id"));
            }
        } catch (Exception e) {
            showErrorAlert("Error Loading Timetable IDs", e.getMessage());
        }
    }

    private void populateStatusComboBox() {
        Starttime_Combo.getItems().addAll("Present", "Absent", "Late", "Excused");
    }

    private void populateEnrollmentComboBox() {
        Integer selectedTimetableId = timetableid_Combo.getValue();
        if (selectedTimetableId == null) {
            return;
        }

        Enrollment_id_Combo.getItems().clear();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT e.enrollment_id " +
                             "FROM enrollments e " +
                             "JOIN courses c ON e.course_id = c.course_id " +
                             "JOIN timetable t ON c.course_id = t.course_id " +
                             "WHERE t.timetable_id = ?")) {
            stmt.setInt(1, selectedTimetableId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Enrollment_id_Combo.getItems().add(rs.getInt("enrollment_id"));
            }
        } catch (Exception e) {
            showErrorAlert("Error Loading Enrollments", e.getMessage());
        }
    }

    @FXML
    void searchHandler(ActionEvent event) {
        Integer enrollmentId = Enrollment_id_Combo.getValue();

        if (enrollmentId == null) {
            showErrorAlert("Input Error", "Please select an Enrollment ID.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT e.student_id, s.full_name " +
                             "FROM enrollments e " +
                             "JOIN students s ON e.student_id = s.student_id " +
                             "WHERE e.enrollment_id = ?")) {
            stmt.setInt(1, enrollmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int studentId = rs.getInt("student_id");
                String studentName = rs.getString("full_name");

                attendanceList.clear();
                attendanceList.add(new AttendanceEntry(
                        0,
                        enrollmentId,
                        0,
                        studentName,
                        "",
                        "Absent",
                        ""
                ));
                attendanceTable.refresh();
            } else {
                showErrorAlert("Not Found", "No student found for the selected Enrollment ID.");
            }
        } catch (Exception e) {
            showErrorAlert("Error Searching Student", e.getMessage());
        }
    }

    @FXML
    void markAttendanceHandler(ActionEvent event) {
        Integer enrollmentId = Enrollment_id_Combo.getValue();
        String remarks = Remarks_field.getText();
        Integer timetableId = timetableid_Combo.getValue();
        String status = Starttime_Combo.getValue();

        if (enrollmentId == null || timetableId == null || status == null) {
            showErrorAlert("Input Error", "Please provide all required fields.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO attendance (enrollment_id, timetable_id, attendance_date, status, remarks) " +
                             "VALUES (?, ?, CURDATE(), ?, ?) " +
                             "ON DUPLICATE KEY UPDATE status = ?, remarks = ?")) {
            stmt.setInt(1, enrollmentId);
            stmt.setInt(2, timetableId);
            stmt.setString(3, status);
            stmt.setString(4, remarks);
            stmt.setString(5, status);
            stmt.setString(6, remarks);
            stmt.executeUpdate();

            showInfoAlert("Success", "Attendance marked successfully!");
        } catch (Exception e) {
            showErrorAlert("Error Marking Attendance", e.getMessage());
        }
    }

    @FXML
    void editHandler(ActionEvent event) {
        AttendanceEntry selectedEntry = attendanceTable.getSelectionModel().getSelectedItem();

        if (selectedEntry == null) {
            showErrorAlert("Selection Error", "Please select a record to edit.");
            return;
        }

        String newRemarks = Remarks_field.getText();
        if (newRemarks.isEmpty()) {
            showErrorAlert("Input Error", "Please provide remarks.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE attendance SET remarks = ? WHERE enrollment_id = ? AND timetable_id = ? AND attendance_date = CURDATE()")) {
            stmt.setString(1, newRemarks);
            stmt.setInt(2, selectedEntry.getEnrollmentId());
            stmt.setInt(3, timetableid_Combo.getValue());
            stmt.executeUpdate();

            selectedEntry.setRemarks(newRemarks);
            attendanceTable.refresh();

            showInfoAlert("Success", "Remarks updated successfully!");
        } catch (Exception e) {
            showErrorAlert("Error Editing Remarks", e.getMessage());
        }
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

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}