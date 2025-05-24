package task.dbms_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;

public class TimetableAdminController {

    @FXML
    private Button Searchbtn;

    @FXML
    private Button Add_btn;

    @FXML
    private Button Removebtn;

    @FXML
    private Button Editbtn;

    @FXML
    private Button backbtn;

    @FXML
    private ComboBox<String> COurse_Combo;

    @FXML
    private ComboBox<String> Day_Combo;

    @FXML
    private ComboBox<String> Endtime_Combo;

    @FXML
    private ComboBox<String> Location_Combo;

    @FXML
    private ComboBox<String> Starttime_Combo;

    @FXML
    private ComboBox<String> Teacher_Combo;

    @FXML
    private TableView<TimetableEntry> tableViewdoctors;

    @FXML
    private TableColumn<TimetableEntry, Integer> id_timetable;

    @FXML
    private TableColumn<TimetableEntry, String> Coursename_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Teachername_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Day_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Starttime_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Endtime_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Location_Column;

    private ObservableList<String> teacherList = FXCollections.observableArrayList();
    private ObservableList<String> courseList = FXCollections.observableArrayList();
    private ObservableList<String> dayList = FXCollections.observableArrayList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private ObservableList<String> timeList = FXCollections.observableArrayList();
    private ObservableList<String> locationList = FXCollections.observableArrayList("Room 101", "Room 202", "Room 303", "Lab 1", "Lab 2");
    private ObservableList<TimetableEntry> timetableList = FXCollections.observableArrayList();

    private int selectedTeacherId;
    private int selectedCourseId;

    @FXML
    public void initialize() {
        populateTeacherCombo();
        populateCourseCombo();
        populateDayCombo();
        populateTimeCombo();
        populateLocationCombo();
        loadTimetableData();

        id_timetable.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        Coursename_Column.setCellValueFactory(cellData -> cellData.getValue().courseNameProperty());
        Teachername_Column.setCellValueFactory(cellData -> cellData.getValue().teacherNameProperty());
        Day_Column.setCellValueFactory(cellData -> cellData.getValue().dayProperty());
        Starttime_Column.setCellValueFactory(cellData -> cellData.getValue().startTimeProperty());
        Endtime_Column.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());
        Location_Column.setCellValueFactory(cellData -> cellData.getValue().locationProperty());

        tableViewdoctors.setItems(timetableList);

        Teacher_Combo.setOnAction(event -> {
            String selectedTeacher = Teacher_Combo.getValue();
            selectedTeacherId = getTeacherIdByName(selectedTeacher);
        });

        COurse_Combo.setOnAction(event -> {
            String selectedCourse = COurse_Combo.getValue();
            selectedCourseId = getCourseIdByName(selectedCourse);
        });
    }

    private void populateTeacherCombo() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT teacher_id, full_name FROM teachers")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                teacherList.add(rs.getString("full_name"));
            }
            Teacher_Combo.setItems(teacherList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateCourseCombo() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT course_id, course_name FROM courses")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courseList.add(rs.getString("course_name"));
            }
            COurse_Combo.setItems(courseList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateDayCombo() {
        Day_Combo.setItems(dayList);
    }

    private void populateTimeCombo() {
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(18, 0);
        while (startTime.isBefore(endTime)) {
            timeList.add(startTime.toString());
            startTime = startTime.plusHours(1);
        }
        Starttime_Combo.setItems(timeList);
        Endtime_Combo.setItems(timeList);
    }

    private void populateLocationCombo() {
        Location_Combo.setItems(locationList);
    }

    private int getTeacherIdByName(String teacherName) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT teacher_id FROM teachers WHERE full_name = ?")) {
            stmt.setString(1, teacherName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("teacher_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getCourseIdByName(String courseName) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT course_id FROM courses WHERE course_name = ?")) {
            stmt.setString(1, courseName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("course_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void loadTimetableData() {
        timetableList.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT t.timetable_id, c.course_name, te.full_name AS teacher_name, t.day_of_week, t.start_time, t.end_time, t.location " +
                             "FROM timetable t " +
                             "JOIN courses c ON t.course_id = c.course_id " +
                             "JOIN teachers te ON t.teacher_id = te.teacher_id")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                timetableList.add(new TimetableEntry(
                        rs.getInt("timetable_id"),
                        rs.getString("course_name"),
                        rs.getString("teacher_name"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("location")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void Add_handler(ActionEvent event) {
        String day = Day_Combo.getValue();
        String startTime = Starttime_Combo.getValue();
        String endTime = Endtime_Combo.getValue();
        String location = Location_Combo.getValue();

        if (selectedTeacherId == -1 || selectedCourseId == -1 || day == null || startTime == null || endTime == null || location == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!");
            alert.show();
            return;
        }

        // Check that the selected teacher is the one assigned to the course
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement teacherCheckStmt = conn.prepareStatement(
                     "SELECT teacher_id FROM courses WHERE course_id = ?")) {
            teacherCheckStmt.setInt(1, selectedCourseId);
            ResultSet rs = teacherCheckStmt.executeQuery();

            if (rs.next()) {
                int courseTeacherId = rs.getInt("teacher_id");
                if (courseTeacherId != selectedTeacherId) {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "The selected teacher is not assigned to this course. Please select the correct teacher.");
                    alert.show();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Check for time slot clashes
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement clashCheckStmt = conn.prepareStatement(
                     "SELECT * FROM timetable WHERE day_of_week = ? AND " +
                             "((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?) OR (start_time = ? OR end_time = ?)) AND " +
                             "(teacher_id = ? OR course_id = ? OR location = ?)")) {

            clashCheckStmt.setString(1, day);
            clashCheckStmt.setString(2, endTime);
            clashCheckStmt.setString(3, startTime);
            clashCheckStmt.setString(4, endTime);
            clashCheckStmt.setString(5, startTime);
            clashCheckStmt.setString(6, startTime);
            clashCheckStmt.setString(7, endTime);
            clashCheckStmt.setInt(8, selectedTeacherId);
            clashCheckStmt.setInt(9, selectedCourseId);
            clashCheckStmt.setString(10, location);

            ResultSet rs = clashCheckStmt.executeQuery();
            if (rs.next()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Clash detected! Please check:\n" +
                        "1. Teacher is already scheduled at this time\n" +
                        "2. Course is already scheduled at this time\n" +
                        "3. Location is already booked at this time");
                alert.show();
                return;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO timetable (course_id, teacher_id, day_of_week, start_time, end_time, location) VALUES (?, ?, ?, ?, ?, ?)")) {
                insertStmt.setInt(1, selectedCourseId);
                insertStmt.setInt(2, selectedTeacherId);
                insertStmt.setString(3, day);
                insertStmt.setString(4, startTime);
                insertStmt.setString(5, endTime);
                insertStmt.setString(6, location);
                insertStmt.executeUpdate();

                loadTimetableData();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Timetable entry added successfully!");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error adding timetable entry: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    void Remove_handler(ActionEvent event) {
        TimetableEntry selectedEntry = tableViewdoctors.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a timetable entry to remove.");
            alert.show();
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM timetable WHERE timetable_id = ?")) {
            stmt.setInt(1, selectedEntry.getId());
            stmt.executeUpdate();
            timetableList.remove(selectedEntry);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Timetable entry removed successfully!");
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Edit_handler(ActionEvent event) {
        TimetableEntry selectedEntry = tableViewdoctors.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a timetable entry to edit.");
            alert.show();
            return;
        }

        String day = Day_Combo.getValue();
        String startTime = Starttime_Combo.getValue();
        String endTime = Endtime_Combo.getValue();
        String location = Location_Combo.getValue();

        if (day == null || startTime == null || endTime == null || location == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields!");
            alert.show();
            return;
        }

        // Check for clashes when editing
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement clashCheckStmt = conn.prepareStatement(
                     "SELECT * FROM timetable WHERE timetable_id != ? AND day_of_week = ? AND " +
                             "((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?) OR (start_time = ? OR end_time = ?)) AND " +
                             "(teacher_id = ? OR location = ?)")) {

            clashCheckStmt.setInt(1, selectedEntry.getId());
            clashCheckStmt.setString(2, day);
            clashCheckStmt.setString(3, endTime);
            clashCheckStmt.setString(4, startTime);
            clashCheckStmt.setString(5, endTime);
            clashCheckStmt.setString(6, startTime);
            clashCheckStmt.setString(7, startTime);
            clashCheckStmt.setString(8, endTime);
            clashCheckStmt.setInt(9, selectedTeacherId);
            clashCheckStmt.setString(10, location);

            ResultSet rs = clashCheckStmt.executeQuery();
            if (rs.next()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Clash detected! Please check scheduling conflicts.");
                alert.show();
                return;
            }

            // Update the timetable entry
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE timetable SET day_of_week = ?, start_time = ?, end_time = ?, location = ? WHERE timetable_id = ?")) {
                stmt.setString(1, day);
                stmt.setString(2, startTime);
                stmt.setString(3, endTime);
                stmt.setString(4, location);
                stmt.setInt(5, selectedEntry.getId());
                stmt.executeUpdate();

                loadTimetableData();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Timetable entry updated successfully!");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error editing timetable entry: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Admin_panel.fxml");
    }
    @FXML
    void Search_handler(ActionEvent event) {
        String selectedTeacher = Teacher_Combo.getValue();
        String selectedLocation = Location_Combo.getValue();
        String selectedStartTime = Starttime_Combo.getValue();
        String selectedEndTime = Endtime_Combo.getValue();
        String selectedDay = Day_Combo.getValue();

        // Base query
        StringBuilder query = new StringBuilder(
                "SELECT t.timetable_id, c.course_name, te.full_name AS teacher_name, t.day_of_week, t.start_time, t.end_time, t.location " +
                        "FROM timetable t " +
                        "JOIN courses c ON t.course_id = c.course_id " +
                        "JOIN teachers te ON t.teacher_id = te.teacher_id WHERE 1=1"
        );

        // Add filters dynamically
        if (selectedTeacher != null) {
            query.append(" AND te.full_name = ?");
        }
        if (selectedLocation != null) {
            query.append(" AND t.location = ?");
        }
        if (selectedStartTime != null) {
            query.append(" AND t.start_time = ?");
        }
        if (selectedEndTime != null) {
            query.append(" AND t.end_time = ?");
        }
        if (selectedDay != null) {
            query.append(" AND t.day_of_week = ?");
        }

        timetableList.clear();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            // Set parameters dynamically
            int paramIndex = 1;
            if (selectedTeacher != null) {
                stmt.setString(paramIndex++, selectedTeacher);
            }
            if (selectedLocation != null) {
                stmt.setString(paramIndex++, selectedLocation);
            }
            if (selectedStartTime != null) {
                stmt.setString(paramIndex++, selectedStartTime);
            }
            if (selectedEndTime != null) {
                stmt.setString(paramIndex++, selectedEndTime);
            }
            if (selectedDay != null) {
                stmt.setString(paramIndex++, selectedDay);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                timetableList.add(new TimetableEntry(
                        rs.getInt("timetable_id"),
                        rs.getString("course_name"),
                        rs.getString("teacher_name"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("location")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}