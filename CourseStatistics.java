package model;

import java.util.HashMap;
import java.util.Map;

public class CourseStatistics {
    private int courseId;
    private String courseTitle;
    private int totalStudentsEnrolled;
    private int studentsCompleted;        // Students who finished 100%
    private double averageProgress;       // Average completion % across all students
    private double averageQuizScore;      // Average quiz score across all students
    private double completionRate;        // (studentsCompleted / totalStudents) * 100
    
    // Lesson-specific statistics: lessonId -> LessonStats
    private Map<Integer, LessonStats> lessonStatistics;
    
    public CourseStatistics(int courseId, String courseTitle) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.totalStudentsEnrolled = 0;
        this.studentsCompleted = 0;
        this.averageProgress = 0.0;
        this.averageQuizScore = 0.0;
        this.completionRate = 0.0;
        this.lessonStatistics = new HashMap<>();
    }
    
    public int getCourseId() {
        return courseId;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public int getTotalStudentsEnrolled() {
        return totalStudentsEnrolled;
    }
    
    public void setTotalStudentsEnrolled(int totalStudentsEnrolled) {
        this.totalStudentsEnrolled = totalStudentsEnrolled;
        calculateCompletionRate();
    }
    
    public int getStudentsCompleted() {
        return studentsCompleted;
    }
    
    public void setStudentsCompleted(int studentsCompleted) {
        this.studentsCompleted = studentsCompleted;
        calculateCompletionRate();
    }
    
    public double getAverageProgress() {
        return averageProgress;
    }
    
    public void setAverageProgress(double averageProgress) {
        this.averageProgress = averageProgress;
    }
    
    public double getAverageQuizScore() {
        return averageQuizScore;
    }
    
    public void setAverageQuizScore(double averageQuizScore) {
        this.averageQuizScore = averageQuizScore;
    }
    
    public double getCompletionRate() {
        return completionRate;
    }
    
    private void calculateCompletionRate() {
        if (totalStudentsEnrolled > 0) {
            this.completionRate = (studentsCompleted * 100.0) / totalStudentsEnrolled;
        } else {
            this.completionRate = 0.0;
        }
    }
    
    public Map<Integer, LessonStats> getLessonStatistics() {
        return lessonStatistics;
    }
    
    public void addLessonStats(int lessonId, LessonStats stats) {
        this.lessonStatistics.put(lessonId, stats);
    }
    // Inner class
    public static class LessonStats {
        private int lessonId;
        private String lessonTitle;
        private int studentsCompleted;
        private double averageQuizScore;
        private int quizAttempts;
        
        public LessonStats(int lessonId, String lessonTitle) {
            this.lessonId = lessonId;
            this.lessonTitle = lessonTitle;
            this.studentsCompleted = 0;
            this.averageQuizScore = 0.0;
            this.quizAttempts = 0;
        }
        
        public int getLessonId() {
            return lessonId;
        }
        
        public String getLessonTitle() {
            return lessonTitle;
        }
        
        public int getStudentsCompleted() {
            return studentsCompleted;
        }
        
        public void setStudentsCompleted(int studentsCompleted) {
            this.studentsCompleted = studentsCompleted;
        }
        
        public double getAverageQuizScore() {
            return averageQuizScore;
        }
        
        public void setAverageQuizScore(double averageQuizScore) {
            this.averageQuizScore = averageQuizScore;
        }
        
        public int getQuizAttempts() {
            return quizAttempts;
        }
        
        public void setQuizAttempts(int quizAttempts) {
            this.quizAttempts = quizAttempts;
        }
    }
}