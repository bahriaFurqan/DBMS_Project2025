package task.dbms_project;

public class Enrollment {
    private int enrollmentId;
    private String studentId;
    private String courseName;
    private String enrollmentDate;
    private String status;

    public Enrollment(int enrollmentId, String studentId, String courseName, String enrollmentDate, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseName = courseName;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public String getStatus() {
        return status;
    }
}