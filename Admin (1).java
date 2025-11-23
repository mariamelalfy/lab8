package model;

import service.AdminService;
import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private List<String> managedCourses; // List of course IDs managed by this admin
    
    public Admin(String userId, String username, String email, String passwordHash) {
        super(userId, username, email, passwordHash, "Admin");
        this.managedCourses = new ArrayList<>();
    }
    
   
    public List<String> getManagedCourses() {
        return managedCourses;
    }
    
    public void setManagedCourses(List<String> managedCourses) {
        this.managedCourses = managedCourses;
    }
    
  
    public void addManagedCourse(String courseId) {
        if (!this.managedCourses.contains(courseId)) {
            this.managedCourses.add(courseId);
        }
    }
    
    
    public void removeManagedCourse(String courseId) {
        this.managedCourses.remove(courseId);
    }
    
    
    public boolean approveCourse(String courseId) {
        try {
            AdminService adminService = new AdminService();
            boolean success = adminService.approveCourse(courseId, this.getUsername());
            
            if (success) {
                addManagedCourse(courseId);
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Error approving course: " + e.getMessage());
            return false;
        }
    }
    
    
    public boolean rejectCourse(String courseId, String reason) {
        try {
            AdminService adminService = new AdminService();
            boolean success = adminService.rejectCourse(courseId, reason, this.getUsername());
            
            if (success) {
                addManagedCourse(courseId);
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Error rejecting course: " + e.getMessage());
            return false;
        }
    }
    
    
    public List<Course> getPendingCourses() {
        try {
            AdminService adminService = new AdminService();
            return adminService.getPendingCourses();
        } catch (Exception e) {
            System.err.println("Error getting pending courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all approved courses
     * @return List of courses with APPROVED status
     */
    public List<Course> getApprovedCourses() {
        try {
            AdminService adminService = new AdminService();
            return adminService.getApprovedCourses();
        } catch (Exception e) {
            System.err.println("Error getting approved courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
    public List<Course> getRejectedCourses() {
        try {
            AdminService adminService = new AdminService();
            return adminService.getRejectedCourses();
        } catch (Exception e) {
            System.err.println("Error getting rejected courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
    public List<Course> getAllCourses() {
        try {
            AdminService adminService = new AdminService();
            return adminService.getAllCourses();
        } catch (Exception e) {
            System.err.println("Error getting all courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
    public AdminService.AdminStatistics getStatistics() {
        try {
            AdminService adminService = new AdminService();
            return adminService.getStatistics();
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return new AdminService.AdminStatistics(0, 0, 0, 0);
        }
    }
    
    
    @Override
    public void displayDashboard() {
        
        System.out.println("Admin Dashboard for: " + username);
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "userId='" + getUserId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", managedCourses=" + managedCourses.size() +
                '}';
    }
}