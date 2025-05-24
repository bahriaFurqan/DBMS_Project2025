package task.dbms_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;

public class AssignmentAdminController {

    @FXML private Button Addbtn, Removebtn, editbtn, markresultbtn, searchbtn, backbtn;
    @FXML private ComboBox<Course> Courseid_Combo;
    @FXML private TextField Description_field, Marks_field, title_field;
    @FXML private DatePicker Examdate;
    @FXML private TableView<Assignment> attendanceTable;
    @FXML private TableColumn<Assignment, Integer> AssignmentidColumn;
    @FXML private TableColumn<Assignment, String> Coursenamecolumn, teacherNameColumn, Titlecolumn, DescriptionColumn;
    @FXML private TableColumn<Assignment, LocalDate> Duedatecolumn;
    @FXML private TableColumn<Assignment, Double> TotalMarksscolumn;
    @FXML private TableColumn<Assignment, Timestamp> Createdatcolumn1;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private ObservableList<Assignment> assignmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        AssignmentidColumn.setCellValueFactory(new PropertyValueFactory<>("assignmentId"));
        Coursenamecolumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        teacherNameColumn.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        Titlecolumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        DescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        Duedatecolumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        TotalMarksscolumn.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
        Createdatcolumn1.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadCoursesForTeacher();
        loadAssignments();
        Courseid_Combo.setConverter(new StringConverter<Course>() {
            @Override public String toString(Course course) { return course == null ? "" : course.getCourseName(); }
            @Override public Course fromString(String s) { return null; }
        });
        attendanceTable.setItems(assignmentList);
    }

    private void loadCoursesForTeacher() {
        String teacherId = Session.getUserId();
        if (teacherId == null) return;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT course_id, course_name FROM courses WHERE teacher_id = ?")) {
            stmt.setInt(1, Integer.parseInt(teacherId));
            ResultSet rs = stmt.executeQuery();
            courseList.clear();
            while (rs.next()) {
                courseList.add(new Course(rs.getInt("course_id"), rs.getString("course_name")));
            }
            Courseid_Combo.setItems(courseList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAssignments() {
        assignmentList.clear();
        String teacherId = Session.getUserId();
        if (teacherId == null) return;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.*, c.course_name, t.full_name as teacher_name FROM assignments a " +
                             "JOIN courses c ON a.course_id = c.course_id " +
                             "JOIN teachers t ON a.teacher_id = t.teacher_id " +
                             "WHERE a.teacher_id = ?")) {
            stmt.setInt(1, Integer.parseInt(teacherId));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                assignmentList.add(new Assignment(
                        rs.getInt("assignment_id"),
                        rs.getString("course_name"),
                        rs.getString("teacher_name"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getDouble("total_marks"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void addHandler(ActionEvent event) {
        Course selectedCourse = Courseid_Combo.getValue();
        String title = title_field.getText();
        String description = Description_field.getText();
        LocalDate dueDate = Examdate.getValue();
        String marksStr = Marks_field.getText();
        String teacherId = Session.getUserId();

        if (selectedCourse == null || title.isEmpty() || dueDate == null || marksStr.isEmpty() || teacherId == null) {
            showAlert("Please fill all required fields.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO assignments (course_id, teacher_id, title, description, due_date, total_marks) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, selectedCourse.getCourseId());
            stmt.setInt(2, Integer.parseInt(teacherId));
            stmt.setString(3, title);
            stmt.setString(4, description);
            stmt.setDate(5, Date.valueOf(dueDate));
            stmt.setBigDecimal(6, new java.math.BigDecimal(marksStr));
            stmt.executeUpdate();
            showAlert("Assignment added successfully.");
            loadAssignments();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error adding assignment.");
        }
    }

    @FXML
    void RemoveHandler(ActionEvent event) {
        Assignment selected = attendanceTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an assignment to remove."); return; }
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM assignments WHERE assignment_id = ?")) {
            stmt.setInt(1, selected.getAssignmentId());
            stmt.executeUpdate();
            showAlert("Assignment removed.");
            loadAssignments();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error removing assignment.");
        }
    }

    @FXML
    void editHandler(ActionEvent event) {
        Assignment selected = attendanceTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an assignment to edit."); return; }
        // Example: update only title and description
        String newTitle = title_field.getText();
        String newDesc = Description_field.getText();
        if (newTitle.isEmpty() || newDesc.isEmpty()) { showAlert("Title and Description required."); return; }
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE assignments SET title = ?, description = ? WHERE assignment_id = ?")) {
            stmt.setString(1, newTitle);
            stmt.setString(2, newDesc);
            stmt.setInt(3, selected.getAssignmentId());
            stmt.executeUpdate();
            showAlert("Assignment updated.");
            loadAssignments();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating assignment.");
        }
    }

    @FXML
    void searchHandler(ActionEvent event) {
        String keyword = title_field.getText();
        assignmentList.clear();
        String teacherId = Session.getUserId();
        if (teacherId == null) return;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.*, c.course_name, t.full_name as teacher_name FROM assignments a " +
                             "JOIN courses c ON a.course_id = c.course_id " +
                             "JOIN teachers t ON a.teacher_id = t.teacher_id " +
                             "WHERE a.teacher_id = ? AND (a.title LIKE ? OR c.course_name LIKE ?)")) {
            stmt.setInt(1, Integer.parseInt(teacherId));
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                assignmentList.add(new Assignment(
                        rs.getInt("assignment_id"),
                        rs.getString("course_name"),
                        rs.getString("teacher_name"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getDouble("total_marks"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void markresulthandler(ActionEvent event) {
        // Navigate to the Mark Result page
        Move_page.navigateToPage(event, "Mark_assignment_result.fxml");
    }

    @FXML
    void back_handler(ActionEvent event) {
        // Navigate back to the previous page
        Move_page.navigateToPage(event, "Teacher_panel.fxml");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public static class Course {
        private int courseId;
        private String courseName;
        public Course(int id, String name) { this.courseId = id; this.courseName = name; }
        public int getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }
        @Override public String toString() { return courseName; }
    }

    public static class Assignment {
        private int assignmentId;
        private String courseName, teacherName, title, description;
        private LocalDate dueDate;
        private double totalMarks;
        private Timestamp createdAt;
        public Assignment(int assignmentId, String courseName, String teacherName, String title, String description,
                          LocalDate dueDate, double totalMarks, Timestamp createdAt) {
            this.assignmentId = assignmentId;
            this.courseName = courseName;
            this.teacherName = teacherName;
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.totalMarks = totalMarks;
            this.createdAt = createdAt;
        }
        public int getAssignmentId() { return assignmentId; }
        public String getCourseName() { return courseName; }
        public String getTeacherName() { return teacherName; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDate getDueDate() { return dueDate; }
        public double getTotalMarks() { return totalMarks; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
}