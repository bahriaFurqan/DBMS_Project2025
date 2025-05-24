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

public class ExamAdminController {

    @FXML
    private Button Addbtn, Removebtn, editbtn, searchbtn, backbtn;

    @FXML
    private ComboBox<String> Courseid_Combo, Teacherid_Combo, examtype_Combo, Duration_Combo, Location_Combo, examstart_Combo;

    @FXML
    private DatePicker Examdate;

    @FXML
    private TableView<Exam> attendanceTable;

    @FXML
    private TableColumn<Exam, Integer> ExamidColumn;

    @FXML
    private TableColumn<Exam, String> Coursenamecolumn, teacherNameColumn, Examnamecolumn, ExamdatesColumn, examstartcolumn, Durationcolumn, locationcolumn1;

    private ObservableList<Exam> examList = FXCollections.observableArrayList();
    private ObservableList<String> courseList = FXCollections.observableArrayList();
    private ObservableList<String> teacherList = FXCollections.observableArrayList();
    private ObservableList<String> examTypeList = FXCollections.observableArrayList("Midterm", "Final", "Quiz", "Assignment");
    private ObservableList<String> durationList = FXCollections.observableArrayList("30", "60", "90", "120");
    private ObservableList<String> locationList = FXCollections.observableArrayList("Room 101", "Room 102", "Auditorium");
    private ObservableList<String> startTimeList = FXCollections.observableArrayList("08:00", "10:00", "12:00", "14:00");

    @FXML
    public void initialize() {
        populateCourseComboBox();
        populateTeacherComboBox();
        examtype_Combo.setItems(examTypeList);
        Duration_Combo.setItems(durationList);
        Location_Combo.setItems(locationList);
        examstart_Combo.setItems(startTimeList);

        // Initialize TableView columns
        ExamidColumn.setCellValueFactory(new PropertyValueFactory<>("examId"));
        Coursenamecolumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        teacherNameColumn.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        Examnamecolumn.setCellValueFactory(new PropertyValueFactory<>("examType"));
        ExamdatesColumn.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        examstartcolumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        Durationcolumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        locationcolumn1.setCellValueFactory(new PropertyValueFactory<>("location"));

        loadExams();
    }

    private void populateCourseComboBox() {
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT course_id, course_name FROM courses")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String course = resultSet.getInt("course_id") + " - " + resultSet.getString("course_name");
                courseList.add(course);
            }
            Courseid_Combo.setItems(courseList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateTeacherComboBox() {
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT teacher_id, full_name FROM teachers")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String teacher = resultSet.getInt("teacher_id") + " - " + resultSet.getString("full_name");
                teacherList.add(teacher);
            }
            Teacherid_Combo.setItems(teacherList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExams() {
        examList.clear();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM exams")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                examList.add(new Exam(
                        resultSet.getInt("exam_id"),
                        resultSet.getString("course_id"),
                        resultSet.getString("teacher_id"),
                        resultSet.getString("exam_type"),
                        resultSet.getString("exam_date"),
                        resultSet.getString("start_time"),
                        resultSet.getString("duration"),
                        resultSet.getString("location")
                ));
            }
            attendanceTable.setItems(examList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void addHandler(ActionEvent event) {
        String selectedCourse = Courseid_Combo.getValue();
        String selectedTeacher = Teacherid_Combo.getValue();
        String examType = examtype_Combo.getValue();
        String duration = Duration_Combo.getValue();
        String location = Location_Combo.getValue();
        String startTime = examstart_Combo.getValue();
        String examDate = (Examdate.getValue() != null) ? Examdate.getValue().toString() : null;

        if (selectedCourse == null || selectedTeacher == null || examType == null || duration == null || location == null || startTime == null || examDate == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        int courseId = Integer.parseInt(selectedCourse.split(" - ")[0]);
        int teacherId = Integer.parseInt(selectedTeacher.split(" - ")[0]);

        // Input for total marks and obtained marks
        TextInputDialog totalMarksDialog = new TextInputDialog();
        totalMarksDialog.setTitle("Input Total Marks");
        totalMarksDialog.setHeaderText("Enter Total Marks:");
        totalMarksDialog.setContentText("Total Marks:");
        String totalMarksInput = totalMarksDialog.showAndWait().orElse(null);

        TextInputDialog obtainedMarksDialog = new TextInputDialog();
        obtainedMarksDialog.setTitle("Input Obtained Marks");
        obtainedMarksDialog.setHeaderText("Enter Obtained Marks:");
        obtainedMarksDialog.setContentText("Obtained Marks:");
        String obtainedMarksInput = obtainedMarksDialog.showAndWait().orElse(null);

        if (totalMarksInput == null || obtainedMarksInput == null || totalMarksInput.isEmpty() || obtainedMarksInput.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please provide both total marks and obtained marks!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        int totalMarks = Integer.parseInt(totalMarksInput);
        int obtainedMarks = Integer.parseInt(obtainedMarksInput);

        // Calculate grade
        String grade = (obtainedMarks >= totalMarks * 0.5) ? "Pass" : "Fail";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO exams (course_id, teacher_id, exam_type, exam_date, start_time, duration, location, total_marks, obtained_marks, grade) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setInt(1, courseId);
            statement.setInt(2, teacherId);
            statement.setString(3, examType);
            statement.setString(4, examDate);
            statement.setString(5, startTime);
            statement.setInt(6, Integer.parseInt(duration));
            statement.setString(7, location);
            statement.setInt(8, totalMarks);
            statement.setInt(9, obtainedMarks);
            statement.setString(10, grade);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Exam added successfully with grade: " + grade, ButtonType.OK);
                alert.showAndWait();
                loadExams();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error adding exam. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void RemoveHandler(ActionEvent event) {
        Exam selectedExam = attendanceTable.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an exam to remove!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM exams WHERE exam_id = ?")) {
            statement.setInt(1, selectedExam.getExamId());
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Exam removed successfully!", ButtonType.OK);
                alert.showAndWait();
                loadExams();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error removing exam. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void editHandler(ActionEvent event) {
        Exam selectedExam = attendanceTable.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an exam to edit!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String selectedCourse = Courseid_Combo.getValue();
        String selectedTeacher = Teacherid_Combo.getValue();
        String examType = examtype_Combo.getValue();
        String duration = Duration_Combo.getValue();
        String location = Location_Combo.getValue();
        String startTime = examstart_Combo.getValue();
        String examDate = (Examdate.getValue() != null) ? Examdate.getValue().toString() : null;

        if (selectedCourse == null || selectedTeacher == null || examType == null || duration == null || location == null || startTime == null || examDate == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        int courseId = Integer.parseInt(selectedCourse.split(" - ")[0]);
        int teacherId = Integer.parseInt(selectedTeacher.split(" - ")[0]);

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE exams SET course_id = ?, teacher_id = ?, exam_type = ?, exam_date = ?, start_time = ?, duration = ?, location = ? WHERE exam_id = ?")) {
            statement.setInt(1, courseId);
            statement.setInt(2, teacherId);
            statement.setString(3, examType);
            statement.setString(4, examDate);
            statement.setString(5, startTime);
            statement.setInt(6, Integer.parseInt(duration));
            statement.setString(7, location);
            statement.setInt(8, selectedExam.getExamId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Exam updated successfully!", ButtonType.OK);
                alert.showAndWait();
                loadExams();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error updating exam. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void searchHandler(ActionEvent event) {
        String selectedCourse = Courseid_Combo.getValue();
        String selectedTeacher = Teacherid_Combo.getValue();
        String examType = examtype_Combo.getValue();

        StringBuilder query = new StringBuilder("SELECT * FROM exams WHERE 1=1");
        if (selectedCourse != null) {
            int courseId = Integer.parseInt(selectedCourse.split(" - ")[0]);
            query.append(" AND course_id = ").append(courseId);
        }
        if (selectedTeacher != null) {
            int teacherId = Integer.parseInt(selectedTeacher.split(" - ")[0]);
            query.append(" AND teacher_id = ").append(teacherId);
        }
        if (examType != null) {
            query.append(" AND exam_type = '").append(examType).append("'");
        }

        examList.clear();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(query.toString())) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                examList.add(new Exam(
                        resultSet.getInt("exam_id"),
                        resultSet.getString("course_id"),
                        resultSet.getString("teacher_id"),
                        resultSet.getString("exam_type"),
                        resultSet.getString("exam_date"),
                        resultSet.getString("start_time"),
                        resultSet.getString("duration"),
                        resultSet.getString("location")
                ));
            }
            attendanceTable.setItems(examList);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error searching exams. Please try again.", ButtonType.OK);
            alert.showAndWait();
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
    @FXML
    void markresulthandler(ActionEvent event) {
        Move_page.navigateToPage(event, "Examresult_admin.fxml");
    }

}