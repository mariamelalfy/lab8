package model;

import database.JsonDatabaseManager;
import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {
    private List<String> createdCourses; // list of courseIds
    private JsonDatabaseManager dbManager; // ADD THIS
    
    public Instructor(String userId, String username, String email, String passwordHash) {
        super(userId, username, email, passwordHash, "Instructor");
        this.createdCourses = new ArrayList<>();
        this.dbManager = JsonDatabaseManager.getInstance(); // ADD THIS
    }
    
    @Override
    public void displayDashboard() {
        System.out.println("Instructor Dashboard for: " + username);
    }
    
    // REPLACE THIS METHOD
    public Course createCourse(String title, String description) {
        // Get instructor ID as int
        int instructorId = Integer.parseInt(this.getUserId());
        
        // Load existing courses to generate new ID
        List<Course> existingCourses = dbManager.getAllCourses();
        int newCourseId = dbManager.generateCourseId(existingCourses);
        
        // sets status to PENDING automatically when creating new course 
        Course newCourse = new Course(newCourseId, title, description, instructorId);
        
        
        createdCourses.add(String.valueOf(newCourseId));
        
        // Save course to database
        existingCourses.add(newCourse);
        dbManager.saveCourses(existingCourses);
        
        // Update instructor in database
        List<User> users = dbManager.loadUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(this.getUserId())) {
                users.set(i, this);
                break;
            }
        }
        dbManager.saveUsers(users);
        
        System.out.println("Course created with PENDING status. Waiting for admin approval.");
        return newCourse;
    }
    
    // KEEP THIS - for loading existing courseIds from JSON
    public void addCreatedCourse(String courseId) {
        if (!createdCourses.contains(courseId)) {
            createdCourses.add(courseId);
        }
    }
    
    public void removeCourse(String courseId) {
        createdCourses.remove(courseId);
    }
    
    public List<String> getCreatedCourses() { 
        return createdCourses; 
    }
    
    public void setCreatedCourses(List<String> createdCourses) {
        this.createdCourses = createdCourses != null ? 
            createdCourses : new ArrayList<>();
    }
    
   
    public List<Course> getMyCourses() {
        List<Course> allCourses = dbManager.getAllCourses();
        List<Course> myCourses = new ArrayList<>();
        
        int myInstructorId = Integer.parseInt(this.getUserId());
        
        for (Course course : allCourses) {
            if (course.getInstructorID() == myInstructorId) {
                myCourses.add(course);
            }
        }
        
        return myCourses;
    }
    
    // Get only approved courses
    public List<Course> getApprovedCourses() {
        List<Course> myCourses = getMyCourses();
        List<Course> approved = new ArrayList<>();
        
        for (Course course : myCourses) {
            if (course.isApproved()) {
                approved.add(course);
            }
        }
        
        return approved;
    }
    
    public void uploadLesson(int courseID, Lesson lesson) {
        // ADD: Check if course is approved before allowing lesson upload
        Course course = dbManager.getCourseById(courseID);
        
        if (course == null) {
            System.out.println("Course not found!");
            return;
        }
        
        if (!course.isApproved()) {
            System.out.println("Cannot upload lesson: Course is not approved yet!");
            return;
        }
        
        System.out.println("Uploading lesson to course: " + courseID);
        // Add lesson upload logic here
    }
    
    public void createQuiz(int courseID, Quiz quiz) {
        // ADD: Check if course is approved before allowing quiz creation
        Course course = dbManager.getCourseById(courseID);
        
        if (course == null) {
            System.out.println("Course not found!");
            return;
        }
        
        if (!course.isApproved()) {
            System.out.println("Cannot create quiz: Course is not approved yet!");
            return;
        }
        
        System.out.println("Creating quiz for course: " + courseID);
        
    }
}