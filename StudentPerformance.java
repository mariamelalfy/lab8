package model;

import java.util.Date;

public class StudentPerformance {
    private String studentId;
    private String studentName;
    private String studentEmail;
    private int courseId;
    private double averageQuizScore;      // Average of all quiz scores
    private int lessonsCompleted;         // Number of lessons completed
    private int totalLessons;             // Total lessons in the course
    private double completionPercentage;  // (lessonsCompleted / totalLessons) * 100
    private Date lastActivityDate;        // Last time student did something
    private int quizzesTaken;             // Total quizzes attempted
    private int quizzesPassed;            // Quizzes with passing score
    
    public StudentPerformance(String studentId, String studentName, int courseId) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.averageQuizScore = 0.0;
        this.lessonsCompleted = 0;
        this.totalLessons = 0;
        this.completionPercentage = 0.0;
        this.lastActivityDate = new Date();
        this.quizzesTaken = 0;
        this.quizzesPassed = 0;
    }
    
    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getStudentEmail() {
        return studentEmail;
    }
    
    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
    
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public double getAverageQuizScore() {
        return averageQuizScore;
    }
    
    public void setAverageQuizScore(double averageQuizScore) {
        this.averageQuizScore = averageQuizScore;
    }
    
    public int getLessonsCompleted() {
        return lessonsCompleted;
    }
    
    public void setLessonsCompleted(int lessonsCompleted) {
        this.lessonsCompleted = lessonsCompleted;
        calculateCompletionPercentage();
    }
    
    public int getTotalLessons() {
        return totalLessons;
    }
    
    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
        calculateCompletionPercentage();
    }
    
    public double getCompletionPercentage() {
        return completionPercentage;
    }
    
    private void calculateCompletionPercentage() {
        if (totalLessons > 0) {
            this.completionPercentage = (lessonsCompleted * 100.0) / totalLessons;
        } else {
            this.completionPercentage = 0.0;
        }
    }
    
    public Date getLastActivityDate() {
        return lastActivityDate;
    }
    
    public void setLastActivityDate(Date lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    
    public int getQuizzesTaken() {
        return quizzesTaken;
    }
    
    public void setQuizzesTaken(int quizzesTaken) {
        this.quizzesTaken = quizzesTaken;
    }
    
    public int getQuizzesPassed() {
        return quizzesPassed;
    }
    
    public void setQuizzesPassed(int quizzesPassed) {
        this.quizzesPassed = quizzesPassed;
    }
    
    public String getStatus() {
        if (completionPercentage == 100.0) {
            return "Completed";
        } else if (completionPercentage >= 50.0) {
            return "In Progress";
        } else if (completionPercentage > 0) {
            return "Started";
        } else {
            return "Not Started";
        }
    }
    
    @Override
    public String toString() {
        return String.format("StudentPerformance{name='%s', course=%d, completion=%.1f%%, avgScore=%.1f}",
                studentName, courseId, completionPercentage, averageQuizScore);
    }
}