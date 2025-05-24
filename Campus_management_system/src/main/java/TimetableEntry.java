package task.dbms_project;

import javafx.beans.property.*;

public class TimetableEntry {
    private final IntegerProperty id;
    private final StringProperty courseName;
    private final StringProperty teacherName;
    private final StringProperty day;
    private final StringProperty startTime;
    private final StringProperty endTime;
    private final StringProperty location;

    public TimetableEntry(int id, String courseName, String teacherName, String day, String startTime, String endTime, String location) {
        this.id = new SimpleIntegerProperty(id);
        this.courseName = new SimpleStringProperty(courseName);
        this.teacherName = new SimpleStringProperty(teacherName);
        this.day = new SimpleStringProperty(day);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);
        this.location = new SimpleStringProperty(location);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getCourseName() {
        return courseName.get();
    }

    public void setCourseName(String courseName) {
        this.courseName.set(courseName);
    }

    public StringProperty courseNameProperty() {
        return courseName;
    }

    public String getTeacherName() {
        return teacherName.get();
    }

    public void setTeacherName(String teacherName) {
        this.teacherName.set(teacherName);
    }

    public StringProperty teacherNameProperty() {
        return teacherName;
    }

    public String getDay() {
        return day.get();
    }

    public void setDay(String day) {
        this.day.set(day);
    }

    public StringProperty dayProperty() {
        return day;
    }

    public String getStartTime() {
        return startTime.get();
    }

    public void setStartTime(String startTime) {
        this.startTime.set(startTime);
    }

    public StringProperty startTimeProperty() {
        return startTime;
    }

    public String getEndTime() {
        return endTime.get();
    }

    public void setEndTime(String endTime) {
        this.endTime.set(endTime);
    }

    public StringProperty endTimeProperty() {
        return endTime;
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public StringProperty locationProperty() {
        return location;
    }
}