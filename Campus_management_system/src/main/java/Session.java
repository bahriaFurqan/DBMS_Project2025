package task.dbms_project;

public class Session {
    private static String userId;
    private static String username;
    private static String role;

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        Session.userId = userId;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Session.username = username;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        Session.role = role;
    }

    public static void clearSession() {
        userId = null;
        username = null;
        role = null;
    }
}