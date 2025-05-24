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

public class RequestApprovalController {

    @FXML
    private Button Add_btn;

    @FXML
    private TableColumn<RequestApproval, String> Contact_Column;

    @FXML
    private TableColumn<RequestApproval, String> Dob_Column;

    @FXML
    private TableColumn<RequestApproval, String> Email_Column;

    @FXML
    private TableColumn<RequestApproval, String> Fullname_Column;

    @FXML
    private TableColumn<RequestApproval, String> Password_Column1;

    @FXML
    private Button Removebtn;

    @FXML
    private TableColumn<RequestApproval, String> Username_Column;

    @FXML
    private Button backbtn;

    @FXML
    private TableView<RequestApproval> tableViewdoctors;

    private ObservableList<RequestApproval> requestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize table columns
        Username_Column.setCellValueFactory(new PropertyValueFactory<>("username"));
        Fullname_Column.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        Email_Column.setCellValueFactory(new PropertyValueFactory<>("email"));
        Dob_Column.setCellValueFactory(new PropertyValueFactory<>("dob"));
        Contact_Column.setCellValueFactory(new PropertyValueFactory<>("contact"));
        Password_Column1.setCellValueFactory(new PropertyValueFactory<>("password"));

        // Populate the table
        populateTable();
    }

    private void populateTable() {
        requestList.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM request_approval")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requestList.add(new RequestApproval(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getDate("dob").toString(),
                        rs.getString("contact"),
                        rs.getString("role"),
                        rs.getString("password")
                ));
            }
            tableViewdoctors.setItems(requestList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Add_handler(ActionEvent event) {
        RequestApproval selectedRequest = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a request to approve.");
            alert.show();
            return;
        }

        String role = selectedRequest.getRole();
        String insertQuery = null;

        // Determine the target table based on the role
        switch (role) {
            case "teacher":
                insertQuery = "INSERT INTO teachers (username, password, email, full_name, contact, dob) VALUES (?, ?, ?, ?, ?, ?)";
                break;
            case "student":
                insertQuery = "INSERT INTO students (username, password, email, full_name, contact, dob) VALUES (?, ?, ?, ?, ?, ?)";
                break;
            case "admin":
                insertQuery = "INSERT INTO admins (username, password, email, full_name, contact, dob) VALUES (?, ?, ?, ?, ?, ?)";
                break;
            default:
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid role!");
                alert.show();
                return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM request_approval WHERE id = ?")) {

            // Insert into the respective role table
            insertStmt.setString(1, selectedRequest.getUsername());
            insertStmt.setString(2, selectedRequest.getPassword());
            insertStmt.setString(3, selectedRequest.getEmail());
            insertStmt.setString(4, selectedRequest.getFullName());
            insertStmt.setString(5, selectedRequest.getContact());
            insertStmt.setString(6, selectedRequest.getDob());
            insertStmt.executeUpdate();

            // Delete the request from the request_approval table
            deleteStmt.setInt(1, selectedRequest.getId());
            deleteStmt.executeUpdate();

            // Refresh the table
            populateTable();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Request approved and added to " + role + "s table.");
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Remove_handler(ActionEvent event) {
        RequestApproval selectedRequest = tableViewdoctors.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a request to remove.");
            alert.show();
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM request_approval WHERE id = ?")) {

            // Delete the request from the request_approval table
            deleteStmt.setInt(1, selectedRequest.getId());
            deleteStmt.executeUpdate();

            // Refresh the table
            populateTable();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Request removed successfully.");
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Admin_panel.fxml");
    }
}