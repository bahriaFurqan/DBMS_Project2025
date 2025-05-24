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

public class teacher_add_adminController {

    @FXML
    private Button Add_btn;

    @FXML
    private TableColumn<Teacher, String> ContactNo_Column;

    @FXML
    private TableColumn<Teacher, String> DOB_Column;

    @FXML
    private DatePicker DOB_field;

    @FXML
    private Button Editbtn;

    @FXML
    private TableColumn<Teacher, String> Email_Column;

    @FXML
    private TableColumn<Teacher, String> Password_Column;

    @FXML
    private Button Removebtn;

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
    private TableColumn<Teacher, String> Username_Column;

    @FXML
    private Button backbtn;

    @FXML
    private TableColumn<Teacher, Integer> id_Column;

    @FXML
    private TableColumn<Teacher, String> name_Column;

    @FXML
    private TableView<Teacher> tableViewdoctors;

    private ObservableList<Teacher> teacherList = FXCollections.observableArrayList();

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

        backbtn.setOnAction(this::back_handler);

        tableViewdoctors.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        tableViewdoctors_SelectedChanged();
                    }
                });

        // Load data into TableView
        loadTeachers();
    }

    private void loadTeachers() {
        teacherList.clear();
        String query = "SELECT * FROM teachers";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                teacherList.add(new Teacher(
                        resultSet.getInt("teacher_id"),
                        resultSet.getString("username"),
                        resultSet.getString("full_name"),
                        resultSet.getString("contact"),
                        resultSet.getString("email"),
                        resultSet.getString("dob"),
                        resultSet.getString("password")
                ));
            }
            tableViewdoctors.setItems(teacherList);
        } catch (Exception e) {
            e.printStackTrace();
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

        if (username.isEmpty() || fullName.isEmpty() || contact.isEmpty() || email.isEmpty() || password.isEmpty() || dob.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
            return;
        }

        String query = "INSERT INTO teachers (username, full_name, contact, email, dob, password) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, fullName);
            preparedStatement.setString(3, contact);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, dob);
            preparedStatement.setString(6, password);

            preparedStatement.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher added successfully!");
            loadTeachers();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add teacher!");
        }
    }

    @FXML
    void Edit_handler(ActionEvent event) {
        Teacher selectedTeacher = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedTeacher == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a teacher to edit");
            return;
        }

        String username = Student_username.getText();
        String fullName = Student_fullname_filed.getText();
        String contact = Student_Contact.getText();
        String email = Student_Email.getText();
        String password = Student_password.getText();
        String dob = DOB_field.getValue().toString();

        if (username.isEmpty() || fullName.isEmpty() || contact.isEmpty() || email.isEmpty() || password.isEmpty() || dob.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
            return;
        }

        String query = "UPDATE teachers SET username = ?, full_name = ?, contact = ?, email = ?, dob = ?, password = ? WHERE teacher_id = ?";

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, fullName);
            preparedStatement.setString(3, contact);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, dob);
            preparedStatement.setString(6, password);
            preparedStatement.setInt(7, selectedTeacher.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher updated successfully!");
                loadTeachers();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update teacher!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update teacher: " + e.getMessage());
        }
    }

    @FXML
    void Remove_handler(ActionEvent event) {
        Teacher selectedTeacher = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedTeacher == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a teacher to remove");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Teacher Record");
        confirmDialog.setContentText("Are you sure you want to delete teacher: " + selectedTeacher.getFullName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "DELETE FROM teachers WHERE teacher_id = ?";

                try (Connection connection = DatabaseConnector.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                    preparedStatement.setInt(1, selectedTeacher.getId());

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher deleted successfully!");
                        loadTeachers();
                        clearFields();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete teacher!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete teacher: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Admin_panel.fxml");
    }

    private void tableViewdoctors_SelectedChanged() {
        Teacher selectedTeacher = tableViewdoctors.getSelectionModel().getSelectedItem();
        if (selectedTeacher != null) {
            Student_username.setText(selectedTeacher.getUsername());
            Student_fullname_filed.setText(selectedTeacher.getFullName());
            Student_Contact.setText(selectedTeacher.getContact());
            Student_Email.setText(selectedTeacher.getEmail());
            Student_password.setText(selectedTeacher.getPassword());
            if (selectedTeacher.getDob() != null && !selectedTeacher.getDob().isEmpty()) {
                try {
                    DOB_field.setValue(java.time.LocalDate.parse(selectedTeacher.getDob()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void clearFields() {
        Student_username.clear();
        Student_fullname_filed.clear();
        Student_Contact.clear();
        Student_Email.clear();
        Student_password.clear();
        DOB_field.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}