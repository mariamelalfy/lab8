package service;

import database.JsonDatabaseManager;
import model.*;

import java.util.ArrayList;
import java.util.List;

public class CertificateService {
    
    private JsonDatabaseManager db;
    private CourseCompletionTracker tracker;
    
    public CertificateService(JsonDatabaseManager db) {
        this.db = db;
        this.tracker = new CourseCompletionTracker(db);
    }
    
    // Check if student can get certificate
    public boolean isEligibleForCertificate(String studentId, String courseId) {
        return tracker.isEligibleForCertificate(studentId, courseId);
    }
    
    // Generate certificate for a student
    public Certificate generateCertificate(String studentId, String courseId) {
   
        if (!isEligibleForCertificate(studentId, courseId)) {
            System.err.println("Student not eligible for certificate");
            return null;
        }
        
        // Check if certificate already exists
        Certificate existing = getCertificateForCourse(studentId, courseId);
        if (existing != null) {
            System.out.println("Certificate already exists");
            return existing;
        }
        
        // Get student and course info
        Student student = db.findStudent(studentId);
        Course course = db.findCourse(courseId);
        
        if (student == null || course == null) {
            System.err.println("Student or course not found");
            return null;
        }
        
        // Get instructor info
        Instructor instructor = findInstructorById(course.getInstructorID());
        String instructorName = instructor != null ? 
            instructor.getUsername() : "Unknown Instructor";
        
        // Calculate average score
        double avgScore = tracker.calculateAverageScore(studentId, courseId);
        
        // Generate certificate
        List<Certificate> allCerts = db.loadCertificates();
        int newCertId = db.generateCertificateId(allCerts);
        
        Certificate cert = new Certificate(
            newCertId,
            Integer.parseInt(studentId),
            Integer.parseInt(courseId),
            student.getUsername(),
            course.getCourseTitle(),
            instructorName,
            avgScore
        );
        
        // Save certificate
        db.saveCertificate(cert);
        
        // Add to student's certificate list and save
    
        List<User> users = db.loadUsers();
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                ((Student) u).addCertificate(String.valueOf(newCertId));
                break;
            }
        }
        db.saveUsers(users);
        
        return cert;
    }
    
    // Get all certificates for a student
    public List<Certificate> getStudentCertificates(String studentId) {
        List<Certificate> allCerts = db.loadCertificates();
        List<Certificate> studentCerts = new ArrayList<>();
        
        int studentIdInt = Integer.parseInt(studentId);
        for (Certificate cert : allCerts) {
            if (cert.getStudentID() == studentIdInt) {
                studentCerts.add(cert);
            }
        }
        
        return studentCerts;
    }
    
    // Get certificate for specific course
    public Certificate getCertificateForCourse(String studentId, String courseId) {
        List<Certificate> certs = getStudentCertificates(studentId);
        int courseIdInt = Integer.parseInt(courseId);
        
        for (Certificate cert : certs) {
            if (cert.getCourseID() == courseIdInt) {
                return cert;
            }
        }
        return null;
    }
    
    // Get completion progress for display
    public String getProgressText(String studentId, String courseId) {
        double percentage = tracker.calculateCompletionPercentage(studentId, courseId);
        return String.format("%.1f%% Complete", percentage);
    }
    
    // Helper method to find instructor
    private Instructor findInstructorById(int instructorId) {
        List<User> users = db.loadUsers();
        for (User user : users) {
            if (user instanceof Instructor && 
                Integer.parseInt(user.getUserId()) == instructorId) {
                return (Instructor) user;
            }
        }
        return null;
    }
}