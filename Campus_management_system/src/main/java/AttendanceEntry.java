package task.dbms_project;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class AttendanceEntry {
    private final SimpleIntegerProperty attendanceId;
    private final SimpleIntegerProperty enrollmentId;
    private final SimpleIntegerProperty timetableId;
    private final SimpleStringProperty studentName;
    private final SimpleStringProperty attendanceDate;
    private final SimpleStringProperty status;
    private final SimpleStringProperty remarks;

    public AttendanceEntry(int attendanceId, int enrollmentId, int timetableId, String studentName,
                           String attendanceDate, String status, String remarks) {
        this.attendanceId = new SimpleIntegerProperty(attendanceId);
        this.enrollmentId = new SimpleIntegerProperty(enrollmentId);
        this.timetableId = new SimpleIntegerProperty(timetableId);
        this.studentName = new SimpleStringProperty(studentName);
        this.attendanceDate = new SimpleStringProperty(attendanceDate);
        this.status = new SimpleStringProperty(status);
        this.remarks = new SimpleStringProperty(remarks);
    }

    public int getAttendanceId() {
        return attendanceId.get();
    }

    public SimpleIntegerProperty attendanceIdProperty() {
        return attendanceId;
    }

    public int getEnrollmentId() {
        return enrollmentId.get();
    }

    public SimpleIntegerProperty enrollmentIdProperty() {
        return enrollmentId;
    }

    public int getTimetableId() {
        return timetableId.get();
    }

    public SimpleIntegerProperty timetableIdProperty() {
        return timetableId;
    }

    public String getStudentName() {
        return studentName.get();
    }

    public SimpleStringProperty studentNameProperty() {
        return studentName;
    }

    public String getAttendanceDate() {
        return attendanceDate.get();
    }

    public SimpleStringProperty attendanceDateProperty() {
        return attendanceDate;
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public String getRemarks() {
        return remarks.get();
    }

    public SimpleStringProperty remarksProperty() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks.set(remarks);
    }
}