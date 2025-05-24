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

public class StudentAddAdminController {

    @FXML
    private Button Add_btn;

    @FXML
    private TableColumn<Student, String> ContactNo_Column;

    @FXML
    private TableColumn<Student, String> DOB_Column;

    @FXML
    private DatePicker DOB_field;

    @FXML
    private ComboBox<String> Department_Combobox;

    @FXML
    private Button Editbtn;

    @FXML
    private TableColumn<Student, String> Email_Column;

    @FXML
    private TableColumn<Student, String> Password_Column;

    @FXML
    private TableColumn<Student, Integer> Semester_column;

    @FXML
    private ComboBox<Integer> Sem_Combobox;

    @FXML
    private TextField Student_Contact;

    @FXML
    private TextField Student_Email;

    @FXML
    private TextField Student_fullname_filed;

    @FXML
    private TextField Student_password;

    @FXML
    private TextField Student_username;

    @FXML
    private TableColumn<Student, String> Username_Column;

    @FXML
    private Button backbtn;

    @FXML
    private TableColumn<Student, Integer> id_Column;

    @FXML
    private TableColumn<Student, String> name_Column;

    @FXML
    private TableView<Student> tableViewdoctors;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize TableView columns
        id_Column.setCellValueFactory(new PropertyValueFactory<>("id"));
        Username_Column.setCellValueFactory(new PropertyValueFactory<>("username"));
        name_Column.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        ContactNo_Column.setCellValueFactory(new PropertyValueFactory<>("contact"));
        Email_Column.setCellValueFactory(new PropertyValueFactory<>("email"));
        DOB_Column.setCellValueFactory(new PropertyValueFactory<>("dob"));
        Password_Column.setCellValueFactory(new PropertyValueFactory<>("password"));
        Semester_column.setCellValueFactory(new PropertyValueFactory<>("semester"));


        backbtn.setOnAction(this::back_handler);

        tableViewdoctors.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        tableViewdoctors_SelectedChanged();
                    }
                });

        // Load data into TableView and ComboBoxes
        loadStudents();
        loadDepartments();
        loadSemesters();
    }

    private void loadStudents() {
        studentList.clear();
        String query = "SELECT * FROM students";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                studentList.add(new Student(
                        resultSet.getInt("student_id"),
                        resultSet.getString("username"),
                        resultSet.getString("full_name"),
                        resultSet.getString("contact"),
                        resultSet.getString("email"),
                        resultSet.getString("dob"),
                        resultSet.getString("password"),
                        resultSet.getInt("semester")
                ));
            }
            tableViewdoctors.setItems(studentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDepartments() {
        String query = "SELECT department_name FROM departments";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Department_Combobox.getItems().add(resultSet.getString("department_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSemesters() {
        for (int i = 1; i <= 8; i++) {
            Sem_Combobox.getItems().add(i);
        }
    }

    @FXML
    void Add_handler(ActionEvent event) {
        String username = Student_username.getText();
        String fullName = Student_fullname_filed.getText();
        String contact = Student_Contact.getText();
        String email = Student_Email.getText();
        String password = Student_password.getText();
        String dob = DOB_field.getValue().toString();
        String department = Department_Combobox.getValue();
        Integer semester = Sem_Combobox.getValue();

        if (username.isEmpty() || fullName.isEmpty() || contact.isEmpty() || email.isEmpty() || password.isEmpty() || dob.isEmpty() || department == null || semester == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
            return;
        }

        String query = "INSERT INTO students (username, full_name, contact, email, dob, password, department_id, semester) " +
                "VALUES (?, ?, ?, ?, ?, ?, (SELECT department_id FROM departments WHERE department_name = ?), ?)";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, fullName);
            preparedStatement.setString(3, contact);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, dob);
            preparedStatement.setString(6, password);
            preparedStatement.setString(7, department);
            preparedStatement.setInt(8, semester);

            preparedStatement.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student added successfully!");
            loadStudents();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add student!");
        }
    }

    @FXML
    void Edit_handler(ActionEvent event) {
        Student selectedStudent = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit");
            return;
        }

        String username = Student_username.getText();
        String fullName = Student_fullname_filed.getText();
        String contact = Student_Contact.getText();
        String email = Student_Email.getText();
        String password = Student_password.getText();
        String department = Department_Combobox.getValue();
        Integer semester = Sem_Combobox.getValue();

        if (username.isEmpty() || fullName.isEmpty() || contact.isEmpty() || email.isEmpty()
                || password.isEmpty() || department == null || semester == null || DOB_field.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
            return;
        }

        String dob = DOB_field.getValue().toString();

        String query = "UPDATE students SET username = ?, full_name = ?, contact = ?, email = ?, "
                + "dob = ?, password = ?, department_id = (SELECT department_id FROM departments "
                + "WHERE department_name = ?), semester = ? WHERE student_id = ?";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, fullName);
            preparedStatement.setString(3, contact);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, dob);
            preparedStatement.setString(6, password);
            preparedStatement.setString(7, department);
            preparedStatement.setInt(8, semester);
            preparedStatement.setInt(9, selectedStudent.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student updated successfully!");
                loadStudents(); // Refresh the table
                clearFields(); // Optional: clear the input fields
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update student!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update student: " + e.getMessage());
        }
    }

    @FXML
    void Remove_handler(ActionEvent event) {
        Student selectedStudent = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to remove");
            return;
        }

        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Student Record");
        confirmDialog.setContentText("Are you sure you want to delete student: " + selectedStudent.getFullName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "DELETE FROM students WHERE student_id = ?";

                try (Connection connection = DatabaseConnector.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                    preparedStatement.setInt(1, selectedStudent.getId());

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully!");
                        loadStudents(); // Refresh the table
                        clearFields(); // Optional: clear the input fields
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student: " + e.getMessage());
                }
            }
        });
    }

    // Helper method to populate form fields when a student is selected
    @FXML
    void tableViewdoctors_SelectedChanged() {
        Student selectedStudent = tableViewdoctors.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            Student_username.setText(selectedStudent.getUsername());
            Student_fullname_filed.setText(selectedStudent.getFullName());
            Student_Contact.setText(selectedStudent.getContact());
            Student_Email.setText(selectedStudent.getEmail());
            Student_password.setText(selectedStudent.getPassword());
            // For DatePicker, parse the date string to LocalDate
            if (selectedStudent.getDob() != null && !selectedStudent.getDob().isEmpty()) {
                try {
                    DOB_field.setValue(java.time.LocalDate.parse(selectedStudent.getDob()));
                } catch (Exception e) {
                    // Handle date parsing error
                    e.printStackTrace();
                }
            }
            Department_Combobox.setValue(selectedStudent.getDepartmentName());
            Sem_Combobox.setValue(selectedStudent.getSemester());
        }
    }

    // Helper method to clear form fields after operations
    private void clearFields() {
        Student_username.clear();
        Student_fullname_filed.clear();
        Student_Contact.clear();
        Student_Email.clear();
        Student_password.clear();
        DOB_field.setValue(null);
        Department_Combobox.setValue(null);
        Sem_Combobox.setValue(null);
    }

    // Add this to initialize method to set up table row selection


    @FXML
    void Department_Comboboxhandler(ActionEvent event) {
        // Optional: Handle department selection
    }

    @FXML
    void Sem_Comboboxhandler(ActionEvent event) {
        // Optional: Handle semester selection
    }

    @FXML
    void back_handler(ActionEvent event) {
        // Navigate back to the previous page
        Move_page.navigateToPage(event, "Admin_panel.fxml");

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}