package service;

import database.JsonDatabaseManager;
import model.Instructor;
import model.Student;
import model.User;
import utils.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final JsonDatabaseManager db;

    public UserService(JsonDatabaseManager db) {
        this.db = db;
    }

    // SIGNUP
    public boolean signup(String username, String email, String password, String role) {
        if (username == null || username.isEmpty() ||
            email == null || email.isEmpty() ||
            password == null || password.isEmpty()) {
            return false;
        }

        // Simple email validation
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }

        if (password.length() < 6) {
            return false;
        }

        List<User> users = db.loadUsers();

        // Check for duplicate email
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }

        String hashedPassword = PasswordHasher.hashPassword(password);

        User newUser;
        String userId = String.valueOf(db.generateUserId(users));

        if (role.equalsIgnoreCase("Student")) {
            newUser = new Student(userId, username, email, hashedPassword);
        } else if (role.equalsIgnoreCase("Instructor")) {
            newUser = new Instructor(userId, username, email, hashedPassword);
        } else {
            return false; // invalid role
        }

        users.add(newUser);
        db.saveUsers(users);

        return true;
    }

    // LOGIN
    public User login(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }

        List<User> users = db.loadUsers();
        String hashedPassword = PasswordHasher.hashPassword(password);

        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email) &&
                u.getPasswordHash().equals(hashedPassword)) {
                return u; // Returns either Student or Instructor
            }
        }

        return null; // Login failed
    }
}
