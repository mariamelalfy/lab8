package service;

import database.JsonDatabaseManager;
import model.Course;
import model.Lesson;
import model.Student;

import java.util.List;

public class CourseCompletionTracker {
    
    private JsonDatabaseManager db;
    
   
    public CourseCompletionTracker(JsonDatabaseManager db) {
        this.db = db;
    }
    
    // Calculate completion percentage for a course
    public double calculateCompletionPercentage(String studentId, String courseId) {
        Student student = db.findStudent(studentId);
        Course course = db.findCourse(courseId);
        
        if (student == null || course == null) {
            return 0.0;
        }
        
        List<Lesson> lessons = course.getLessons();
        if (lessons.isEmpty()) {
            return 0.0;
        }
        
        List<String> completedLessons = student.getCompletedLessons()
            .getOrDefault(courseId, List.of());
        
        return (completedLessons.size() * 100.0) / lessons.size();
    }
    
    // Check if all lessons are completed
    public boolean areAllLessonsCompleted(String studentId, String courseId) {
        Student student = db.findStudent(studentId);
        Course course = db.findCourse(courseId);
        
        if (student == null || course == null) {
            return false;
        }
        
        List<Lesson> lessons = course.getLessons();
        if (lessons.isEmpty()) {
            return false;
        }
        
        List<String> completedLessons = student.getCompletedLessons()
            .getOrDefault(courseId, List.of());
        
        // Check if all lesson IDs are in completed list
        for (Lesson lesson : lessons) {
            if (!completedLessons.contains(String.valueOf(lesson.getLessonID()))) {
                return false;
            }
        }
        
        return true;
    }
    
    // Check if all quizzes are passed
    public boolean areAllQuizzesPassed(String studentId, String courseId) {
        Student student = db.findStudent(studentId);
        Course course = db.findCourse(courseId);
        
        if (student == null || course == null) {
            return false;
        }
        
        List<Lesson> lessons = course.getLessons();
        if (lessons.isEmpty()) {
            return false;
        }
        
        // Check each lesson for quiz pass status
        for (Lesson lesson : lessons) {
            String lessonId = String.valueOf(lesson.getLessonID());
       
            if (!student.hasPassedQuiz(lessonId)) {
                return false;
            }
        }
        
        return true;
    }
    
    // Calculate average score across all quizzes in course
    public double calculateAverageScore(String studentId, String courseId) {
        Student student = db.findStudent(studentId);
        Course course = db.findCourse(courseId);
        
        if (student == null || course == null) {
            return 0.0;
        }
        
        List<Lesson> lessons = course.getLessons();
        if (lessons.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        int quizCount = 0;
        
        for (Lesson lesson : lessons) {
            String lessonId = String.valueOf(lesson.getLessonID());
            double score = student.getQuizScore(lessonId);
            
            if (score > 0) { // Only count quizzes that were attempted
                totalScore += score;
                quizCount++;
            }
        }
        
        return quizCount > 0 ? totalScore / quizCount : 0.0;
    }
    
    // Check if student is eligible for certificate
    public boolean isEligibleForCertificate(String studentId, String courseId) {
        return areAllLessonsCompleted(studentId, courseId) && 
               areAllQuizzesPassed(studentId, courseId);
    }
}