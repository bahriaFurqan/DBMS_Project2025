module task.campus_management_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens task.campus_management_system to javafx.fxml;
    exports task.campus_management_system;
}