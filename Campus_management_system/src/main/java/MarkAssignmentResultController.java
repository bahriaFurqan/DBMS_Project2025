package task.dbms_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MarkAssignmentResultController {

    @FXML private Button Addbtn, Removebtn, editbtn, searchbtn, backbtn;
    @FXML private ComboBox<Assignment> examid_Combo;
    @FXML private ComboBox<Student> teachername_Combo;
    @FXML private TextField markob_field;
    @FXML private TableView<AssignmentResult> attendanceTable;
    @FXML private TableColumn<AssignmentResult, Integer> ResultidColumn, AssignmentidColumn;
    @FXML private TableColumn<AssignmentResult, String> Coursenamecolumn, StudentNameColumn, gradecolumn;
    @FXML private TableColumn<AssignmentResult, Double> MarksObtainedcolumn, totalmarkscolumn;
    @FXML private TableColumn<AssignmentResult, Timestamp> gradeatcolumn;

    private ObservableList<Assignment> assignmentList = FXCollections.observableArrayList();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private ObservableList<AssignmentResult> resultList = FXCollections.observableArrayList();
    private Map<Integer, String> courseNameMap = new HashMap<>();
    private Map<Integer, Double> assignmentTotalMarks = new HashMap<>();

    @FXML
    public void initialize() {
        // Set up TableView columns
        ResultidColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("resultId"));
        AssignmentidColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignmentId"));
        Coursenamecolumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("courseName"));
        StudentNameColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("studentName"));
        MarksObtainedcolumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("marksObtained"));
        totalmarkscolumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalMarks"));
        gradecolumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("grade"));
        gradeatcolumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("gradedAt"));

        attendanceTable.setItems(resultList);

        // Set up ComboBox converters
        examid_Combo.setConverter(new StringConverter<Assignment>() {
            @Override public String toString(Assignment a) { return a == null ? "" : String.valueOf(a.assignmentId); }
            @Override public Assignment fromString(String s) { return null; }
        });
        teachername_Combo.setConverter(new StringConverter<Student>() {
            @Override public String toString(Student s) { return s == null ? "" : s.studentName; }
            @Override public Student fromString(String s) { return null; }
        });

        loadAssignmentsForTeacher();
        examid_Combo.setItems(assignmentList);

        examid_Combo.setOnAction(e -> onAssignmentSelected());
        loadResults();
    }

    private void loadAssignmentsForTeacher() {
        assignmentList.clear();
        courseNameMap.clear();
        assignmentTotalMarks.clear();
        String teacherId = Session.getUserId();
        if (teacherId == null) return;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.assignment_id, a.course_id, a.total_marks, c.course_name " +
                             "FROM assignments a JOIN courses c ON a.course_id = c.course_id WHERE a.teacher_id = ?")) {
            stmt.setInt(1, Integer.parseInt(teacherId));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int aid = rs.getInt("assignment_id");
                int cid = rs.getInt("course_id");
                double tmarks = rs.getDouble("total_marks");
                String cname = rs.getString("course_name");
                assignmentList.add(new Assignment(aid, cid, tmarks));
                courseNameMap.put(cid, cname);
                assignmentTotalMarks.put(aid, tmarks);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void onAssignmentSelected() {
        Assignment selected = examid_Combo.getValue();
        if (selected == null) return;
        int courseId = selected.courseId;
        loadStudentsForCourse(courseId);
    }

    private void loadStudentsForCourse(int courseId) {
        studentList.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT s.student_id, s.full_name FROM enrollments e JOIN students s ON e.student_id = s.student_id WHERE e.course_id = ?")) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                studentList.add(new Student(rs.getInt("student_id"), rs.getString("full_name")));
            }
            teachername_Combo.setItems(studentList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadResults() {
        resultList.clear();
        String teacherId = Session.getUserId();
        if (teacherId == null) return;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.*, a.course_id, c.course_name, s.full_name, a.total_marks " +
                             "FROM assignment_results r " +
                             "JOIN assignments a ON r.assignment_id = a.assignment_id " +
                             "JOIN courses c ON a.course_id = c.course_id " +
                             "JOIN students s ON r.student_id = s.student_id " +
                             "WHERE a.teacher_id = ?")) {
            stmt.setInt(1, Integer.parseInt(teacherId));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultList.add(new AssignmentResult(
                        rs.getInt("result_id"),
                        rs.getInt("assignment_id"),
                        rs.getString("course_name"),
                        rs.getString("full_name"),
                        rs.getDouble("marks_obtained"),
                        rs.getDouble("total_marks"),
                        rs.getString("grade"),
                        rs.getTimestamp("graded_at")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void addHandler(ActionEvent event) {
        Assignment assignment = examid_Combo.getValue();
        Student student = teachername_Combo.getValue();
        String marksStr = markob_field.getText();
        if (assignment == null || student == null || marksStr.isEmpty()) {
            showAlert("Select assignment, student, and enter marks.");
            return;
        }
        double marks;
        try {
            marks = Double.parseDouble(marksStr);
        } catch (NumberFormatException e) {
            showAlert("Enter valid marks.");
            return;
        }
        double totalMarks = assignmentTotalMarks.getOrDefault(assignment.assignmentId, 0.0);
        String grade = marks >= totalMarks * 0.5 ? "PASS" : "FAIL";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO assignment_results (assignment_id, student_id, marks_obtained, grade) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, assignment.assignmentId);
            stmt.setInt(2, student.studentId);
            stmt.setDouble(3, marks);
            stmt.setString(4, grade);
            stmt.executeUpdate();
            showAlert("Result added.");
            loadResults();
        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert("Result already exists for this student and assignment.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error adding result.");
        }
    }

    @FXML
    void RemoveHandler(ActionEvent event) {
        AssignmentResult selected = attendanceTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a result to remove."); return; }
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM assignment_results WHERE result_id = ?")) {
            stmt.setInt(1, selected.resultId);
            stmt.executeUpdate();
            showAlert("Result removed.");
            loadResults();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error removing result.");
        }
    }

    @FXML
    void editHandler(ActionEvent event) {
        AssignmentResult selected = attendanceTable.getSelectionModel().getSelectedItem();
        String marksStr = markob_field.getText();
        if (selected == null || marksStr.isEmpty()) {
            showAlert("Select a result and enter new marks.");
            return;
        }
        double marks;
        try {
            marks = Double.parseDouble(marksStr);
        } catch (NumberFormatException e) {
            showAlert("Enter valid marks.");
            return;
        }
        double totalMarks = selected.totalMarks;
        String grade = marks >= totalMarks * 0.5 ? "PASS" : "FAIL";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE assignment_results SET marks_obtained = ?, grade = ?, graded_at = CURRENT_TIMESTAMP WHERE result_id = ?")) {
            stmt.setDouble(1, marks);
            stmt.setString(2, grade);
            stmt.setInt(3, selected.resultId);
            stmt.executeUpdate();
            showAlert("Result updated.");
            loadResults();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating result.");
        }
    }

    @FXML
    void searchHandler(ActionEvent event) {
        String keyword = markob_field.getText();
        resultList.clear();
        String teacherId = Session.getUserId();
        if (teacherId == null) return;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.*, a.course_id, c.course_name, s.full_name, a.total_marks " +
                             "FROM assignment_results r " +
                             "JOIN assignments a ON r.assignment_id = a.assignment_id " +
                             "JOIN courses c ON a.course_id = c.course_id " +
                             "JOIN students s ON r.student_id = s.student_id " +
                             "WHERE a.teacher_id = ? AND (s.full_name LIKE ? OR c.course_name LIKE ?)")) {
            stmt.setInt(1, Integer.parseInt(teacherId));
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultList.add(new AssignmentResult(
                        rs.getInt("result_id"),
                        rs.getInt("assignment_id"),
                        rs.getString("course_name"),
                        rs.getString("full_name"),
                        rs.getDouble("marks_obtained"),
                        rs.getDouble("total_marks"),
                        rs.getString("grade"),
                        rs.getTimestamp("graded_at")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Teacher_panel.fxml");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    // Helper classes
    public static class Assignment {
        int assignmentId, courseId;
        double totalMarks;
        public Assignment(int assignmentId, int courseId, double totalMarks) {
            this.assignmentId = assignmentId; this.courseId = courseId; this.totalMarks = totalMarks;
        }
        @Override public String toString() { return String.valueOf(assignmentId); }
    }
    public static class Student {
        int studentId; String studentName;
        public Student(int studentId, String studentName) {
            this.studentId = studentId; this.studentName = studentName;
        }
        @Override public String toString() { return studentName; }
    }
    public static class AssignmentResult {
        int resultId, assignmentId;
        String courseName, studentName, grade;
        double marksObtained, totalMarks;
        Timestamp gradedAt;
        public AssignmentResult(int resultId, int assignmentId, String courseName, String studentName,
                                double marksObtained, double totalMarks, String grade, Timestamp gradedAt) {
            this.resultId = resultId; this.assignmentId = assignmentId; this.courseName = courseName;
            this.studentName = studentName; this.marksObtained = marksObtained; this.totalMarks = totalMarks;
            this.grade = grade; this.gradedAt = gradedAt;
        }
        public int getResultId() { return resultId; }
        public int getAssignmentId() { return assignmentId; }
        public String getCourseName() { return courseName; }
        public String getStudentName() { return studentName; }
        public double getMarksObtained() { return marksObtained; }
        public double getTotalMarks() { return totalMarks; }
        public String getGrade() { return grade; }
        public Timestamp getGradedAt() { return gradedAt; }
    }
}