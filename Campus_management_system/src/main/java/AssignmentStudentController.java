package task.dbms_project;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AssignmentStudentController {

    @FXML
    private TableColumn<AssignmentEntry, Integer> AssignmentidColumn;

    @FXML
    private Button Checkresultbtn;

    @FXML
    private ComboBox<String> Courseid_Combo;

    @FXML
    private TableColumn<AssignmentEntry, String> Coursenamecolumn;

    @FXML
    private TableColumn<AssignmentEntry, String> Createdatcolumn1;

    @FXML
    private TableColumn<AssignmentEntry, String> DescriptionColumn;

    @FXML
    private TableColumn<AssignmentEntry, String> Duedatecolumn;

    @FXML
    private TableColumn<AssignmentEntry, String> Titlecolumn;

    @FXML
    private TableColumn<AssignmentEntry, Double> TotalMarksscolumn;

    @FXML
    private TableView<AssignmentEntry> attendanceTable;

    @FXML
    private Button backbtn;

    @FXML
    private Button searchbtn;

    @FXML
    private TableColumn<AssignmentEntry, String> teacherNameColumn;

    private ObservableList<AssignmentEntry> assignmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind TableView columns to AssignmentEntry class properties
        AssignmentidColumn.setCellValueFactory(cellData -> cellData.getValue().assignmentIdProperty().asObject());
        Coursenamecolumn.setCellValueFactory(cellData -> cellData.getValue().courseNameProperty());
        teacherNameColumn.setCellValueFactory(cellData -> cellData.getValue().teacherNameProperty());
        Titlecolumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        DescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        Duedatecolumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        TotalMarksscolumn.setCellValueFactory(cellData -> cellData.getValue().totalMarksProperty().asObject());
        Createdatcolumn1.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());

        attendanceTable.setItems(assignmentList);

        // Populate ComboBox with enrolled courses
        populateCourseComboBox();
    }

    private void populateCourseComboBox() {
        String studentId = Session.getUserId();
        if (studentId == null) return;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DISTINCT c.course_name " +
                             "FROM courses c " +
                             "JOIN enrollments e ON c.course_id = e.course_id " +
                             "WHERE e.student_id = ?")) {
            stmt.setInt(1, Integer.parseInt(studentId));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Courseid_Combo.getItems().add(rs.getString("course_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void searchHandler(ActionEvent event) {
        String selectedCourse = Courseid_Combo.getValue();
        if (selectedCourse == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course.");
            alert.show();
            return;
        }

        assignmentList.clear();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.assignment_id, c.course_name, t.full_name AS teacher_name, a.title, " +
                             "a.description, a.due_date, a.total_marks, a.created_at " +
                             "FROM assignments a " +
                             "JOIN courses c ON a.course_id = c.course_id " +
                             "JOIN teachers t ON a.teacher_id = t.teacher_id " +
                             "JOIN enrollments e ON c.course_id = e.course_id " +
                             "WHERE e.student_id = ? AND c.course_name = ?")) {
            stmt.setInt(1, Integer.parseInt(Session.getUserId()));
            stmt.setString(2, selectedCourse);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                assignmentList.add(new AssignmentEntry(
                        rs.getInt("assignment_id"),
                        rs.getString("course_name"),
                        rs.getString("teacher_name"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("due_date"),
                        rs.getDouble("total_marks"),
                        rs.getString("created_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void markresulthandler(ActionEvent event) {
        AssignmentEntry selectedAssignment = attendanceTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an assignment to check results.");
            alert.show();
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT ar.marks_obtained, ar.grade " +
                             "FROM assignment_results ar " +
                             "WHERE ar.student_id = ? AND ar.assignment_id = ?")) {
            stmt.setInt(1, Integer.parseInt(Session.getUserId()));
            stmt.setInt(2, selectedAssignment.assignmentIdProperty().get());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String resultMessage = "Assignment: " + selectedAssignment.titleProperty().get() +
                        "\nMarks: " + rs.getDouble("marks_obtained") +
                        "\nGrade: " + rs.getString("grade");

                Alert alert = new Alert(Alert.AlertType.INFORMATION, resultMessage);
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No result found for the selected assignment.");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Student_panel.fxml");
    }

    public static class AssignmentEntry {
        private final SimpleIntegerProperty assignmentId;
        private final SimpleStringProperty courseName;
        private final SimpleStringProperty teacherName;
        private final SimpleStringProperty title;
        private final SimpleStringProperty description;
        private final SimpleStringProperty dueDate;
        private final SimpleDoubleProperty totalMarks;
        private final SimpleStringProperty createdAt;

        public AssignmentEntry(int assignmentId, String courseName, String teacherName, String title,
                               String description, String dueDate, double totalMarks, String createdAt) {
            this.assignmentId = new SimpleIntegerProperty(assignmentId);
            this.courseName = new SimpleStringProperty(courseName);
            this.teacherName = new SimpleStringProperty(teacherName);
            this.title = new SimpleStringProperty(title);
            this.description = new SimpleStringProperty(description);
            this.dueDate = new SimpleStringProperty(dueDate);
            this.totalMarks = new SimpleDoubleProperty(totalMarks);
            this.createdAt = new SimpleStringProperty(createdAt);
        }

        public SimpleIntegerProperty assignmentIdProperty() {
            return assignmentId;
        }

        public SimpleStringProperty courseNameProperty() {
            return courseName;
        }

        public SimpleStringProperty teacherNameProperty() {
            return teacherName;
        }

        public SimpleStringProperty titleProperty() {
            return title;
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public SimpleStringProperty dueDateProperty() {
            return dueDate;
        }

        public SimpleDoubleProperty totalMarksProperty() {
            return totalMarks;
        }

        public SimpleStringProperty createdAtProperty() {
            return createdAt;
        }
    }
}