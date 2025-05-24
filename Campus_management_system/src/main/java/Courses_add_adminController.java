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

public class Courses_add_adminController {

    @FXML
    private Button Add_btn;

    @FXML
    private TableColumn<Course, String> ContactNo_Column;

    @FXML
    private TextField Course_Code;

    @FXML
    private TextField Course_Description;

    @FXML
    private TextField Course_field;

    @FXML
    private Button Editbtn;

    @FXML
    private TableColumn<Course, String> Email_Column;

    @FXML
    private Button Removebtn;

    @FXML
    private ComboBox<Teacher> Teacher_Combo;

    @FXML
    private TableColumn<Course, String> Username_Column;

    @FXML
    private Button backbtn;

    @FXML
    private TableColumn<Course, Integer> id_Column;

    @FXML
    private TableColumn<Course, String> name_Column;

    @FXML
    private TableView<Course> tableViewdoctors;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private ObservableList<Teacher> teacherList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize TableView columns
        id_Column.setCellValueFactory(new PropertyValueFactory<>("id"));
        name_Column.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        Username_Column.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        ContactNo_Column.setCellValueFactory(new PropertyValueFactory<>("description"));
        Email_Column.setCellValueFactory(new PropertyValueFactory<>("teacherName"));

        backbtn.setOnAction(this::back_handler);

        // Load data into TableView and ComboBox
        loadCourses();
        loadTeachers();
    }

    private void loadCourses() {
        courseList.clear();
        String query = "SELECT c.course_id, c.course_name, c.course_code, c.description, t.full_name AS teacher_name " +
                "FROM courses c " +
                "JOIN teachers t ON c.teacher_id = t.teacher_id";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                courseList.add(new Course(
                        resultSet.getInt("course_id"),
                        resultSet.getString("course_name"),
                        resultSet.getString("course_code"),
                        resultSet.getString("description"),
                        resultSet.getString("teacher_name")
                ));
            }
            tableViewdoctors.setItems(courseList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTeachers() {
        teacherList.clear();
        String query = "SELECT teacher_id, full_name FROM teachers";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                teacherList.add(new Teacher(
                        resultSet.getInt("teacher_id"),
                        resultSet.getString("full_name")
                ));
            }
            Teacher_Combo.setItems(teacherList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Add_handler(ActionEvent event) {
        String courseName = Course_field.getText();
        String courseCode = Course_Code.getText();
        String description = Course_Description.getText();
        Teacher selectedTeacher = Teacher_Combo.getSelectionModel().getSelectedItem();

        if (courseName.isEmpty() || courseCode.isEmpty() || description.isEmpty() || selectedTeacher == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
            return;
        }

        String query = "INSERT INTO courses (course_name, course_code, description, teacher_id) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, courseName);
            preparedStatement.setString(2, courseCode);
            preparedStatement.setString(3, description);
            preparedStatement.setInt(4, selectedTeacher.getId());

            preparedStatement.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully!");
            loadCourses();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add course!");
        }
    }

    @FXML
    void Edit_handler(ActionEvent event) {
        Course selectedCourse = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a course to edit");
            return;
        }

        String courseName = Course_field.getText();
        String courseCode = Course_Code.getText();
        String description = Course_Description.getText();
        Teacher selectedTeacher = Teacher_Combo.getSelectionModel().getSelectedItem();

        if (courseName.isEmpty() || courseCode.isEmpty() || description.isEmpty() || selectedTeacher == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
            return;
        }

        String query = "UPDATE courses SET course_name = ?, course_code = ?, description = ?, teacher_id = ? WHERE course_id = ?";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, courseName);
            preparedStatement.setString(2, courseCode);
            preparedStatement.setString(3, description);
            preparedStatement.setInt(4, selectedTeacher.getId());
            preparedStatement.setInt(5, selectedCourse.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully!");
                loadCourses();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update course!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update course: " + e.getMessage());
        }
    }

    @FXML
    void Remove_handler(ActionEvent event) {
        Course selectedCourse = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a course to remove");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Course Record");
        confirmDialog.setContentText("Are you sure you want to delete course: " + selectedCourse.getCourseName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "DELETE FROM courses WHERE course_id = ?";

                try (Connection connection = DatabaseConnector.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                    preparedStatement.setInt(1, selectedCourse.getId());

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully!");
                        loadCourses();
                        clearFields();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete course!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete course: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Admin_panel.fxml");
    }

    private void clearFields() {
        Course_field.clear();
        Course_Code.clear();
        Course_Description.clear();
        Teacher_Combo.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}