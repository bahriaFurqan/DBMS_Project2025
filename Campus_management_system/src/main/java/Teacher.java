package task.dbms_project;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Teacher {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty fullName;
    private final StringProperty contact;
    private final StringProperty email;
    private final StringProperty dob;
    private final StringProperty password;

    public Teacher(Integer id, String username, String fullName, String contact, String email, String dob, String password) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.fullName = new SimpleStringProperty(fullName);
        this.contact = new SimpleStringProperty(contact);
        this.email = new SimpleStringProperty(email);
        this.dob = new SimpleStringProperty(dob);
        this.password = new SimpleStringProperty(password);
    }

    // Additional constructor for simplified use
    public Teacher(int id, String fullName) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty("");
        this.fullName = new SimpleStringProperty(fullName);
        this.contact = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.dob = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
    }

    // ID
    public int getId() {
        return id.get();
    }
    public IntegerProperty idProperty() {
        return id;
    }
    public void setId(int id) {
        this.id.set(id);
    }

    // Username
    public String getUsername() {
        return username.get();
    }
    public StringProperty usernameProperty() {
        return username;
    }
    public void setUsername(String username) {
        this.username.set(username);
    }

    // Full Name
    public String getFullName() {
        return fullName.get();
    }
    public StringProperty fullNameProperty() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    // Contact
    public String getContact() {
        return contact.get();
    }
    public StringProperty contactProperty() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact.set(contact);
    }

    // Email
    public String getEmail() {
        return email.get();
    }
    public StringProperty emailProperty() {
        return email;
    }
    public void setEmail(String email) {
        this.email.set(email);
    }

    // Date of Birth
    public String getDob() {
        return dob.get();
    }
    public StringProperty dobProperty() {
        return dob;
    }
    public void setDob(String dob) {
        this.dob.set(dob);
    }

    // Password
    public String getPassword() {
        return password.get();
    }
    public StringProperty passwordProperty() {
        return password;
    }
    public void setPassword(String password) {
        this.password.set(password);
    }

    @Override
    public String toString() {
        return fullName.get();
    }
}