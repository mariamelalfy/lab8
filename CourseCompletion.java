package model;

import java.util.Date;

public class CourseCompletion {
    private int studentID;
    private int courseID;
    private Date completionDate;
    private double averageScore;
    private boolean certificateIssued;
    private int certificateID;
    
    public CourseCompletion(int studentID, int courseID, double averageScore) {
        this.studentID = studentID;
        this.courseID = courseID;
        this.completionDate = new Date();
        this.averageScore = averageScore;
        this.certificateIssued = false;
        this.certificateID = -1;
    }
    
     public CourseCompletion() {
        this.completionDate = new Date();
    }
    
    public int getStudentID() {
        return studentID;
    }
    
    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }
    
    public int getCourseID() {
        return courseID;
    }
    
    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }
    
    public Date getCompletionDate() {
        return completionDate;
    }
    
    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }
    
    public double getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
    
    public boolean isCertificateIssued() {
        return certificateIssued;
    }
    
    public void setCertificateIssued(boolean certificateIssued) {
        this.certificateIssued = certificateIssued;
    }
    
    public int getCertificateID() {
        return certificateID;
    }
    
    public void setCertificateID(int certificateID) {
        this.certificateID = certificateID;
        this.certificateIssued = true;
    }
    
}
