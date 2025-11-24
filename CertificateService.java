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
        boolean lessonsComplete = tracker.areAllLessonsCompleted(studentId, courseId);
        boolean quizzesPassed = tracker.areAllQuizzesPassed(studentId, courseId);
        return lessonsComplete && quizzesPassed;
    }
    
    // Make certificate for student
    public Certificate generateCertificate(String studentId, String courseId) {
        // Check if eligible
        if (!isEligibleForCertificate(studentId, courseId)) {
            return null;
        }
        
        // Check if already exists
        Certificate existing = getCertificateForCourse(studentId, courseId);
        if (existing != null) {
            return existing;
        }
        
        // Get student and course
        Student student = db.findStudent(studentId);
        Course course = db.findCourse(courseId);
        
        if (student == null || course == null) {
            return null;
        }
        
        // Get instructor
        List<User> users = db.loadUsers();
        Instructor instructor = null;
        for (User u : users) {
            if (u instanceof Instructor) {
                if (Integer.parseInt(u.getUserId()) == course.getInstructorID()) {
                    instructor = (Instructor) u;
                    break;
                }
            }
        }
        
        String instructorName = "Unknown";
        if (instructor != null) {
            instructorName = instructor.getUsername();
        }
        
        // Calculate average
        double avg = tracker.calculateAverageScore(studentId, courseId);
        
        // Make certificate
        List<Certificate> allCerts = db.loadCertificates();
        int maxId = 0;
        for (Certificate c : allCerts) {
            if (c.getCertificateID() > maxId) {
                maxId = c.getCertificateID();
            }
        }
        int newId = maxId + 1;
        
        Certificate cert = new Certificate(
            newId,
            Integer.parseInt(studentId),
            Integer.parseInt(courseId),
            student.getUsername(),
            course.getCourseTitle(),
            instructorName,
            avg
        );
        
        // Save certificate
        db.saveCertificate(cert);
        
        // Add to student
        List<User> allUsers = db.loadUsers();
        for (User u : allUsers) {
            if (u.getUserId().equals(studentId)) {
                if (u instanceof Student) {
                    ((Student) u).addCertificate(String.valueOf(newId));
                }
                break;
            }
        }
        db.saveUsers(allUsers);
        
        return cert;
    }
    
    // Get all certificates for student
    public List<Certificate> getStudentCertificates(String studentId) {
        List<Certificate> all = db.loadCertificates();
        List<Certificate> mine = new ArrayList<>();
        
        int id = Integer.parseInt(studentId);
        for (Certificate c : all) {
            if (c.getStudentID() == id) {
                mine.add(c);
            }
        }
        
        return mine;
    }
    
    // Get certificate for course
    public Certificate getCertificateForCourse(String studentId, String courseId) {
        List<Certificate> certs = getStudentCertificates(studentId);
        int cid = Integer.parseInt(courseId);
        
        for (Certificate c : certs) {
            if (c.getCourseID() == cid) {
                return c;
            }
        }
        return null;
    }
}