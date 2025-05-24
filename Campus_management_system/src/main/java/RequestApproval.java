package task.dbms_project;

public class RequestApproval {
    private int id;
    private String username;
    private String email;
    private String fullName;
    private String dob;
    private String contact;
    private String role;
    private String password;

    public RequestApproval(int id, String username, String email, String fullName, String dob, String contact, String role, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.dob = dob;
        this.contact = contact;
        this.role = role;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDob() {
        return dob;
    }

    public String getContact() {
        return contact;
    }

    public String getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}