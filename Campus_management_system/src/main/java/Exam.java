package task.dbms_project;

public class Exam {
    private int examId;
    private String courseName;
    private String teacherName;
    private String examType;
    private String examDate;
    private String startTime;
    private String duration;
    private String location;

    // Constructor
    public Exam(int examId, String courseName, String teacherName, String examType, String examDate, String startTime, String duration, String location) {
        this.examId = examId;
        this.courseName = courseName;
        this.teacherName = teacherName;
        this.examType = examType;
        this.examDate = examDate;
        this.startTime = startTime;
        this.duration = duration;
        this.location = location;
    }

    // Getters and Setters
    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}