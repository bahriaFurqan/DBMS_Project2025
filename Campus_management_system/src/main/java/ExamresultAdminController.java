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

public class ExamresultAdminController {

    @FXML
    private Button Addbtn, Removebtn, editbtn, searchbtn, backbtn;

    @FXML
    private ComboBox<String> Coursename_Combo, teachername_Combo, examid_Combo, examtype_Combo;

    @FXML
    private TextField Totalmarks_field, markob_field;

    @FXML
    private TableView<Result> attendanceTable;

    @FXML
    private TableColumn<Result, Integer> ResultidColumn, ExamidColumn;

    @FXML
    private TableColumn<Result, String> Coursenamecolumn, StudentNameColumn, Examtypecolumn;

    @FXML
    private TableColumn<Result, Integer> MarksObtainedcolumn, totalmarkscolumn;

    @FXML
    private TableColumn<Result, String> gradecolumn1;

    private ObservableList<String> courseList = FXCollections.observableArrayList();
    private ObservableList<String> studentList = FXCollections.observableArrayList();
    private ObservableList<String> examIdList = FXCollections.observableArrayList();
    private ObservableList<Result> resultList = FXCollections.observableArrayList();
    @FXML
    private void populateExamTypeComboBox() {
        ObservableList<String> examTypeList = FXCollections.observableArrayList("Midterm", "Final", "Quiz", "Assignment");
        examtype_Combo.setItems(examTypeList);
    }
    @FXML
    public void initialize() {
        // Bind TableView columns to Result class properties
        ResultidColumn.setCellValueFactory(new PropertyValueFactory<>("resultId"));
        ExamidColumn.setCellValueFactory(new PropertyValueFactory<>("examId"));
        Coursenamecolumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        StudentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        Examtypecolumn.setCellValueFactory(new PropertyValueFactory<>("examType"));
        MarksObtainedcolumn.setCellValueFactory(new PropertyValueFactory<>("marksObtained"));
        totalmarkscolumn.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
        gradecolumn1.setCellValueFactory(new PropertyValueFactory<>("grade"));

        // Populate ComboBoxes and load data
        populateCourseComboBox();
        populateStudentComboBox();
        populateExamIdComboBox();
        populateExamTypeComboBox();
        loadResults();
    }

    private void populateCourseComboBox() {
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT course_name FROM courses")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                courseList.add(resultSet.getString("course_name"));
            }
            Coursename_Combo.setItems(courseList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateStudentComboBox() {
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT full_name FROM students")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                studentList.add(resultSet.getString("full_name"));
            }
            teachername_Combo.setItems(studentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateExamIdComboBox() {
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT exam_id FROM exams")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                examIdList.add(String.valueOf(resultSet.getInt("exam_id")));
            }
            examid_Combo.setItems(examIdList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadResults() {
        resultList.clear();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT r.result_id, r.exam_id, c.course_name, s.full_name AS student_name, " +
                             "r.exam_type, r.marks_obtained, r.total_marks, r.grade " +
                             "FROM exam_results r " +
                             "JOIN courses c ON r.course_id = c.course_id " +
                             "JOIN students s ON r.student_id = s.student_id")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resultList.add(new Result(
                        resultSet.getInt("result_id"),
                        resultSet.getInt("exam_id"),
                        resultSet.getString("course_name"),
                        resultSet.getString("student_name"),
                        resultSet.getString("exam_type"),
                        resultSet.getInt("marks_obtained"),
                        resultSet.getInt("total_marks"),
                        resultSet.getString("grade")
                ));
            }
            attendanceTable.setItems(resultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void addHandler(ActionEvent event) {
        String courseName = Coursename_Combo.getValue();
        String studentName = teachername_Combo.getValue();
        String examId = examid_Combo.getValue();
        String examType = examtype_Combo.getValue();
        String marksObtained = markob_field.getText();
        String totalMarks = Totalmarks_field.getText();

        if (courseName == null || studentName == null || examId == null || examType == null || marksObtained.isEmpty() || totalMarks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection()) {
            // Retrieve course_id from course_name
            int courseId;
            try (PreparedStatement getCourseId = connection.prepareStatement("SELECT course_id FROM courses WHERE course_name = ?")) {
                getCourseId.setString(1, courseName);
                ResultSet resultSet = getCourseId.executeQuery();
                if (resultSet.next()) {
                    courseId = resultSet.getInt("course_id");
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Course not found!", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            // Retrieve student_id from student_name
            int studentId;
            try (PreparedStatement getStudentId = connection.prepareStatement("SELECT student_id FROM students WHERE full_name = ?")) {
                getStudentId.setString(1, studentName);
                ResultSet resultSet = getStudentId.executeQuery();
                if (resultSet.next()) {
                    studentId = resultSet.getInt("student_id");
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Student not found!", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            // Check if the student is enrolled in the course
            try (PreparedStatement checkEnrollment = connection.prepareStatement(
                    "SELECT * FROM enrollments WHERE student_id = ? AND course_id = ?")) {
                checkEnrollment.setInt(1, studentId);
                checkEnrollment.setInt(2, courseId);
                ResultSet resultSet = checkEnrollment.executeQuery();
                if (!resultSet.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Student is not enrolled in the selected course!", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            // Check if the exam is held for the selected course
            try (PreparedStatement checkExam = connection.prepareStatement(
                    "SELECT * FROM exams WHERE exam_id = ? AND course_id = ?")) {
                checkExam.setInt(1, Integer.parseInt(examId));
                checkExam.setInt(2, courseId);
                ResultSet resultSet = checkExam.executeQuery();
                if (!resultSet.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "The selected exam is not held for the selected course!", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            // Insert into exam_results table
            try (PreparedStatement insertResult = connection.prepareStatement(
                    "INSERT INTO exam_results (student_id, course_id, exam_id, exam_type, marks_obtained, total_marks, grade) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                insertResult.setInt(1, studentId);
                insertResult.setInt(2, courseId);
                insertResult.setInt(3, Integer.parseInt(examId));
                insertResult.setString(4, examType);
                insertResult.setDouble(5, Double.parseDouble(marksObtained));
                insertResult.setDouble(6, Double.parseDouble(totalMarks));
                insertResult.setString(7, calculateGrade(Double.parseDouble(marksObtained), Double.parseDouble(totalMarks)));

                int rowsInserted = insertResult.executeUpdate();
                if (rowsInserted > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Result added successfully!", ButtonType.OK);
                    alert.showAndWait();
                    loadResults();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error adding result. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private String calculateGrade(double marksObtained, double totalMarks) {
        double percentage = (marksObtained / totalMarks) * 100;
        if (percentage >= 90) return "A";
        else if (percentage >= 80) return "B";
        else if (percentage >= 70) return "C";
        else if (percentage >= 60) return "D";
        else return "F";
    }

    private String calculateGrade(int marksObtained, int totalMarks) {
        double percentage = (double) marksObtained / totalMarks * 100;
        if (percentage >= 90) return "A";
        else if (percentage >= 80) return "B";
        else if (percentage >= 70) return "C";
        else if (percentage >= 60) return "D";
        else return "F";
    }

    @FXML
    void RemoveHandler(ActionEvent event) {
        Result selectedResult = attendanceTable.getSelectionModel().getSelectedItem();
        if (selectedResult == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a result to remove!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM results WHERE result_id = ?")) {
            statement.setInt(1, selectedResult.getResultId());
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Result removed successfully!", ButtonType.OK);
                alert.showAndWait();
                loadResults();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error removing result. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void searchHandler(ActionEvent event) {
        String courseName = Coursename_Combo.getValue();
        String studentName = teachername_Combo.getValue();
        String examType = examtype_Combo.getValue();

        StringBuilder query = new StringBuilder("SELECT * FROM results WHERE 1=1");
        if (courseName != null) {
            query.append(" AND course_name = '").append(courseName).append("'");
        }
        if (studentName != null) {
            query.append(" AND student_name = '").append(studentName).append("'");
        }
        if (examType != null) {
            query.append(" AND exam_type = '").append(examType).append("'");
        }

        resultList.clear();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(query.toString())) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resultList.add(new Result(
                        resultSet.getInt("result_id"),
                        resultSet.getInt("exam_id"),
                        resultSet.getString("course_name"),
                        resultSet.getString("student_name"),
                        resultSet.getString("exam_type"),
                        resultSet.getInt("marks_obtained"),
                        resultSet.getInt("total_marks"),
                        resultSet.getString("grade")
                ));
            }
            attendanceTable.setItems(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error searching results. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void editHandler(ActionEvent event) {
        Result selectedResult = attendanceTable.getSelectionModel().getSelectedItem();
        if (selectedResult == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a result to edit!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String courseName = Coursename_Combo.getValue();
        String studentName = teachername_Combo.getValue();
        String examId = examid_Combo.getValue();
        String marksObtained = markob_field.getText();
        String totalMarks = Totalmarks_field.getText();

        if (courseName == null || studentName == null || examId == null || marksObtained.isEmpty() || totalMarks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE results SET exam_id = ?, course_name = ?, student_name = ?, marks_obtained = ?, total_marks = ?, grade = ? WHERE result_id = ?")) {
            statement.setInt(1, Integer.parseInt(examId));
            statement.setString(2, courseName);
            statement.setString(3, studentName);
            statement.setInt(4, Integer.parseInt(marksObtained));
            statement.setInt(5, Integer.parseInt(totalMarks));
            statement.setString(6, calculateGrade(Integer.parseInt(marksObtained), Integer.parseInt(totalMarks)));
            statement.setInt(7, selectedResult.getResultId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Result updated successfully!", ButtonType.OK);
                alert.showAndWait();
                loadResults();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error updating result. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Admin_panel.fxml");
    }
}