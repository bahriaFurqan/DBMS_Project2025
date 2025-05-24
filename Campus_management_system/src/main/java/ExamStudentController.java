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

public class ExamStudentController {

    @FXML
    private ComboBox<String> Courseid_Combo;

    @FXML
    private TableColumn<ExamEntry, Integer> ExamidColumn;

    @FXML
    private TableColumn<ExamEntry, String> Coursenamecolumn;

    @FXML
    private TableColumn<ExamEntry, String> teacherNameColumn;

    @FXML
    private TableColumn<ExamEntry, String> Examnamecolumn;

    @FXML
    private TableColumn<ExamEntry, String> ExamdatesColumn;

    @FXML
    private TableColumn<ExamEntry, String> examstartcolumn;

    @FXML
    private TableColumn<ExamEntry, Integer> Durationcolumn;

    @FXML
    private TableColumn<ExamEntry, String> locationcolumn1;

    @FXML
    private TableView<ExamEntry> attendanceTable;

    @FXML
    private Button backbtn;

    @FXML
    private Button markresultbtn;

    @FXML
    private Button searchbtn;

    private ObservableList<ExamEntry> examList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind TableView columns to ExamEntry class properties
        ExamidColumn.setCellValueFactory(cellData -> cellData.getValue().examIdProperty().asObject());
        Coursenamecolumn.setCellValueFactory(cellData -> cellData.getValue().courseNameProperty());
        teacherNameColumn.setCellValueFactory(cellData -> cellData.getValue().teacherNameProperty());
        Examnamecolumn.setCellValueFactory(cellData -> cellData.getValue().examTypeProperty());
        ExamdatesColumn.setCellValueFactory(cellData -> cellData.getValue().examDateProperty());
        examstartcolumn.setCellValueFactory(cellData -> cellData.getValue().startTimeProperty());
        Durationcolumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty().asObject());
        locationcolumn1.setCellValueFactory(cellData -> cellData.getValue().locationProperty());

        attendanceTable.setItems(examList);

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

        examList.clear();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT e.exam_id, c.course_name, t.full_name AS teacher_name, e.exam_type, " +
                             "e.exam_date, e.start_time, e.duration, e.location " +
                             "FROM exams e " +
                             "JOIN courses c ON e.course_id = c.course_id " +
                             "JOIN teachers t ON e.teacher_id = t.teacher_id " +
                             "JOIN enrollments en ON c.course_id = en.course_id " +
                             "WHERE en.student_id = ? AND c.course_name = ?")) {
            stmt.setInt(1, Integer.parseInt(Session.getUserId()));
            stmt.setString(2, selectedCourse);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                examList.add(new ExamEntry(
                        rs.getInt("exam_id"),
                        rs.getString("course_name"),
                        rs.getString("teacher_name"),
                        rs.getString("exam_type"),
                        rs.getString("exam_date"),
                        rs.getString("start_time"),
                        rs.getInt("duration"),
                        rs.getString("location")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void markresulthandler(ActionEvent event) {
        ExamEntry selectedExam = attendanceTable.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an exam to check results.");
            alert.show();
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT er.marks_obtained, er.grade " +
                             "FROM exam_results er " +
                             "WHERE er.student_id = ? AND er.exam_id = ?")) {
            stmt.setInt(1, Integer.parseInt(Session.getUserId()));
            stmt.setInt(2, selectedExam.examIdProperty().get());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String resultMessage = "Exam: " + selectedExam.examTypeProperty().get() +
                        "\nMarks: " + rs.getDouble("marks_obtained") +
                        "\nGrade: " + rs.getString("grade");

                Alert alert = new Alert(Alert.AlertType.INFORMATION, resultMessage);
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No result found for the selected exam.");
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

    public static class ExamEntry {
        private final SimpleIntegerProperty examId;
        private final SimpleStringProperty courseName;
        private final SimpleStringProperty teacherName;
        private final SimpleStringProperty examType;
        private final SimpleStringProperty examDate;
        private final SimpleStringProperty startTime;
        private final SimpleIntegerProperty duration;
        private final SimpleStringProperty location;

        public ExamEntry(int examId, String courseName, String teacherName, String examType,
                         String examDate, String startTime, int duration, String location) {
            this.examId = new SimpleIntegerProperty(examId);
            this.courseName = new SimpleStringProperty(courseName);
            this.teacherName = new SimpleStringProperty(teacherName);
            this.examType = new SimpleStringProperty(examType);
            this.examDate = new SimpleStringProperty(examDate);
            this.startTime = new SimpleStringProperty(startTime);
            this.duration = new SimpleIntegerProperty(duration);
            this.location = new SimpleStringProperty(location);
        }

        public SimpleIntegerProperty examIdProperty() {
            return examId;
        }

        public SimpleStringProperty courseNameProperty() {
            return courseName;
        }

        public SimpleStringProperty teacherNameProperty() {
            return teacherName;
        }

        public SimpleStringProperty examTypeProperty() {
            return examType;
        }

        public SimpleStringProperty examDateProperty() {
            return examDate;
        }

        public SimpleStringProperty startTimeProperty() {
            return startTime;
        }

        public SimpleIntegerProperty durationProperty() {
            return duration;
        }

        public SimpleStringProperty locationProperty() {
            return location;
        }
    }
}