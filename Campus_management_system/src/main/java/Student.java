package task.dbms_project;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Student {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty fullName;
    private final StringProperty contact;
    private final StringProperty email;
    private final StringProperty dob;
    private final StringProperty password;
    private final IntegerProperty semester;
    private final StringProperty departmentName;

    public Student(int id, String username, String fullName, String contact,
                   String email, String dob, String password, int semester) {
        this(id, username, fullName, contact, email, dob, password, semester, null);
    }

    public Student(int id, String username, String fullName, String contact,
                   String email, String dob, String password, int semester, String departmentName) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.fullName = new SimpleStringProperty(fullName);
        this.contact = new SimpleStringProperty(contact);
        this.email = new SimpleStringProperty(email);
        this.dob = new SimpleStringProperty(dob);
        this.password = new SimpleStringProperty(password);
        this.semester = new SimpleIntegerProperty(semester);
        this.departmentName = new SimpleStringProperty(departmentName);
    }

    // Getters for TableView
    public int getId() {
        return id.get();
    }

    public String getUsername() {
        return username.get();
    }

    public String getFullName() {
        return fullName.get();
    }

    public String getContact() {
        return contact.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getDob() {
        return dob.get();
    }

    public String getPassword() {
        return password.get();
    }

    public int getSemester() {
        return semester.get();
    }

    public String getDepartmentName() {
        return departmentName.get();
    }

    // Property getters for TableView bindings
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public StringProperty contactProperty() {
        return contact;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty dobProperty() {
        return dob;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public IntegerProperty semesterProperty() {
        return semester;
    }

    public StringProperty departmentNameProperty() {
        return departmentName;
    }

    // Setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public void setContact(String contact) {
        this.contact.set(contact);
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public void setDob(String dob) {
        this.dob.set(dob);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public void setSemester(int semester) {
        this.semester.set(semester);
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName.set(departmentName);
    }
}