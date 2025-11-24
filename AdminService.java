package service;

import database.JsonDatabaseManager;
import model.Course;
import model.Course.ApprovalStatus;
import java.util.ArrayList;
import java.util.List;

public class AdminService {
    private JsonDatabaseManager dbManager;
    
    public AdminService() {
        this.dbManager = JsonDatabaseManager.getInstance();
    }
    public List<Course> getPendingCourses() {
        List<Course> allCourses = dbManager.getAllCourses();
        List<Course> pendingCourses = new ArrayList<>();
        
        for (Course course : allCourses) {
            if (course.getApprovalStatus() == ApprovalStatus.PENDING) {
                pendingCourses.add(course);
            }
        }
        
        return pendingCourses;
    }
    
   
    public List<Course> getApprovedCourses() {
        List<Course> allCourses = dbManager.getAllCourses();
        List<Course> approvedCourses = new ArrayList<>();
        
        for (Course course : allCourses) {
            if (course.getApprovalStatus() == ApprovalStatus.APPROVED) {
                approvedCourses.add(course);
            }
        }
        
        return approvedCourses;
    }
    
 
    public List<Course> getRejectedCourses() {
        List<Course> allCourses = dbManager.getAllCourses();
        List<Course> rejectedCourses = new ArrayList<>();
        
        for (Course course : allCourses) {
            if (course.getApprovalStatus() == ApprovalStatus.REJECTED) {
                rejectedCourses.add(course);
            }
        }
        
        return rejectedCourses;
    }
    
   
    public List<Course> getAllCourses() {
        return dbManager.getAllCourses();
    }
    public boolean approveCourse(String courseId, String adminUsername) {
        try {
            Course course = dbManager.getCourseById(Integer.parseInt(courseId));
            
            if (course == null) {
                System.err.println("Course not found: " + courseId);
                return false;
            }
            
       
            course.approve(adminUsername);
            
            // Save changes to database
            dbManager.updateCourse(course);
            
            System.out.println("Course '" + course.getCourseTitle() + "' approved successfully!");
            return true;
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid course ID format: " + courseId);
            return false;
        } catch (Exception e) {
            System.err.println("Error approving course: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean rejectCourse(String courseId, String reason, String adminUsername) {
        try {
            Course course = dbManager.getCourseById(Integer.parseInt(courseId));
            
            if (course == null) {
                System.err.println("Course not found: " + courseId);
                return false;
            }
            
            // Validate rejection reason
            if (reason == null || reason.trim().isEmpty()) {
                System.err.println("Rejection reason cannot be empty");
                return false;
            }
            
            
            course.reject(adminUsername, reason);
            
            // Save changes to database
            dbManager.updateCourse(course);
            
            System.out.println("Course '" + course.getCourseTitle() + "' rejected.");
            return true;
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid course ID format: " + courseId);
            return false;
        } catch (Exception e) {
            System.err.println("Error rejecting course: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public Course getCourseById(int courseId) {
        return dbManager.getCourseById(courseId);
    }
    public AdminStatistics getStatistics() {
        List<Course> allCourses = dbManager.getAllCourses();
        
        int totalCourses = allCourses.size();
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        
        for (Course course : allCourses) {
            switch (course.getApprovalStatus()) {
                case PENDING:
                    pendingCount++;
                    break;
                case APPROVED:
                    approvedCount++;
                    break;
                case REJECTED:
                    rejectedCount++;
                    break;
            }
        }
        
        return new AdminStatistics(totalCourses, pendingCount, approvedCount, rejectedCount);
    }
    
    
    public static class AdminStatistics {
        public final int totalCourses;
        public final int pendingCourses;
        public final int approvedCourses;
        public final int rejectedCourses;
        
        public AdminStatistics(int total, int pending, int approved, int rejected) {
            this.totalCourses = total;
            this.pendingCourses = pending;
            this.approvedCourses = approved;
            this.rejectedCourses = rejected;
        }
        
        @Override
        public String toString() {
            return "AdminStatistics{" +
                    "totalCourses=" + totalCourses +
                    ", pendingCourses=" + pendingCourses +
                    ", approvedCourses=" + approvedCourses +
                    ", rejectedCourses=" + rejectedCourses +
                    '}';
        }
    }
}