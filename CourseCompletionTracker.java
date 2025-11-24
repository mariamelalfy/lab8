package service;

import database.JsonDatabaseManager;
import model.Course;
import model.Lesson;
import model.Student;
import model.Quiz;

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
    
    // Check if all required quizzes are passed
    public boolean areAllQuizzesPassed(String studentId, String courseId) {
        Course course = db.findCourse(courseId);
        
        if (course == null) {
            return false;
        }
        
        List<Lesson> lessons = course.getLessons();
        if (lessons.isEmpty()) {
            return false;
        }
        // Check each lesson that has a quiz
        for (Lesson lesson : lessons) {
            Quiz quiz = lesson.getQuiz();
            if (quiz != null) {
                int quizId = quiz.getQuizId();
                boolean passed = db.hasPassedQuiz(studentId, lesson.getLessonID(), quizId);
                if (!passed) {
                    return false;
                }   
            } 
        }
        
        return true;
    }
    
    // Calculate average score across all quizzes in course
    public double calculateAverageScore(String studentId, String courseId) {
        Course course = db.findCourse(courseId);
        
        if (course == null) {
            return 0.0;
        }
        
        List<Lesson> lessons = course.getLessons();
        if (lessons.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        int quizCount = 0;
        
        for (Lesson lesson : lessons) {
            if (lesson.getQuiz() != null) {
                Quiz quiz = lesson.getQuiz();
                
                // Get best score from database attempts
                List<model.QuizAttempt> attempts = db.getStudentQuizAttempts(
                    studentId, 
                    quiz.getQuizId()
                );
                
                double bestScore = 0.0;
                for (model.QuizAttempt attempt : attempts) {
                    if (attempt.getScore() > bestScore) {
                        bestScore = attempt.getScore();
                    }
                }
                
                if (bestScore > 0) {
                    totalScore += bestScore;
                    quizCount++;
                }
            }
        }
        
        if (quizCount == 0) {
            return 100.0;
        }
        
        return totalScore / quizCount;
    }
    
    // Check if student is eligible for certificate
    public boolean isEligibleForCertificate(String studentId, String courseId) {
        boolean allLessons = areAllLessonsCompleted(studentId, courseId);
        boolean allQuizzes = areAllQuizzesPassed(studentId, courseId);
        return allLessons && allQuizzes;
    }
}