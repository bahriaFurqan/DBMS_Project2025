package task.dbms_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TimetableStudentController {

    @FXML
    private ComboBox<String> COurse_Combo;

    @FXML
    private TableColumn<TimetableEntry, String> Coursename_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Day_Column;

    @FXML
    private ComboBox<String> Day_Combo;

    @FXML
    private TableColumn<TimetableEntry, String> Endtime_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Location_Column;

    @FXML
    private ComboBox<String> Location_Combo;

    @FXML
    private Button Searchbtn;

    @FXML
    private TableColumn<TimetableEntry, String> Starttime_Column;

    @FXML
    private TableColumn<TimetableEntry, String> Teachername_Column;

    @FXML
    private Button backbtn;

    @FXML
    private TableColumn<TimetableEntry, Integer> id_timetable;

    @FXML
    private TableView<TimetableEntry> tableViewdoctors;

    private ObservableList<TimetableEntry> timetableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind TableView columns to TimetableEntry class properties
        id_timetable.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timetableId"));
        Coursename_Column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("courseName"));
        Teachername_Column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("teacherName"));
        Day_Column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dayOfWeek"));
        Starttime_Column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startTime"));
        Endtime_Column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endTime"));
        Location_Column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("location"));

        tableViewdoctors.setItems(timetableList);

        // Populate ComboBoxes
        populateDayComboBox();
        populateLocationComboBox();
        populateCourseComboBox();

        // Load timetable data for the student
        loadTimetableForStudent();
    }

    private void loadTimetableForStudent() {
        timetableList.clear();
        String studentId = Session.getUserId();
        if (studentId == null) return;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT t.timetable_id, c.course_name, te.full_name AS teacher_name, t.day_of_week, " +
                             "t.start_time, t.end_time, t.location " +
                             "FROM timetable t " +
                             "JOIN courses c ON t.course_id = c.course_id " +
                             "JOIN teachers te ON t.teacher_id = te.teacher_id " +
                             "JOIN enrollments e ON c.course_id = e.course_id " +
                             "WHERE e.student_id = ?")) {
            stmt.setInt(1, Integer.parseInt(studentId));
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

    private void populateDayComboBox() {
        Day_Combo.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    }

    private void populateLocationComboBox() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT location FROM timetable")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Location_Combo.getItems().add(rs.getString("location"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateCourseComboBox() {
        String studentId = Session.getUserId();
        if (studentId == null) return;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DISTINCT c.course_name " +
                             "FROM enrollments e " +
                             "JOIN courses c ON e.course_id = c.course_id " +
                             "WHERE e.student_id = ?")) {
            stmt.setInt(1, Integer.parseInt(studentId));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                COurse_Combo.getItems().add(rs.getString("course_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Search_handler(ActionEvent event) {
        String selectedDay = Day_Combo.getValue();
        String selectedLocation = Location_Combo.getValue();
        String selectedCourse = COurse_Combo.getValue();

        StringBuilder query = new StringBuilder(
                "SELECT t.timetable_id, c.course_name, te.full_name AS teacher_name, t.day_of_week, " +
                        "t.start_time, t.end_time, t.location " +
                        "FROM timetable t " +
                        "JOIN courses c ON t.course_id = c.course_id " +
                        "JOIN teachers te ON t.teacher_id = te.teacher_id " +
                        "JOIN enrollments e ON c.course_id = e.course_id " +
                        "WHERE e.student_id = ?"
        );

        if (selectedDay != null) {
            query.append(" AND t.day_of_week = ?");
        }
        if (selectedLocation != null) {
            query.append(" AND t.location = ?");
        }
        if (selectedCourse != null) {
            query.append(" AND c.course_name = ?");
        }

        timetableList.clear();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setInt(paramIndex++, Integer.parseInt(Session.getUserId()));

            if (selectedDay != null) {
                stmt.setString(paramIndex++, selectedDay);
            }
            if (selectedLocation != null) {
                stmt.setString(paramIndex++, selectedLocation);
            }
            if (selectedCourse != null) {
                stmt.setString(paramIndex++, selectedCourse);
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

    @FXML
    void back_handler(ActionEvent event) {
        Move_page.navigateToPage(event, "Student_panel.fxml");
    }

    public static class TimetableEntry {
        private int timetableId;
        private String courseName;
        private String teacherName;
        private String dayOfWeek;
        private String startTime;
        private String endTime;
        private String location;

        public TimetableEntry(int timetableId, String courseName, String teacherName, String dayOfWeek,
                              String startTime, String endTime, String location) {
            this.timetableId = timetableId;
            this.courseName = courseName;
            this.teacherName = teacherName;
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
        }

        public int getTimetableId() {
            return timetableId;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getLocation() {
            return location;
        }
    }
}