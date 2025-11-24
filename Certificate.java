package model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Certificate {
    private int certificateID;
    private Date issueDate;
    private int studentID;
    private int courseID;
    private String studentName;
    private String courseTitle;
    private String instructorName;
    private double finalScore;
    
    public Certificate(int certificateID, int studentID, int courseID, 
                      String studentName, String courseTitle, String instructorName, 
                      double finalScore) {
        this.certificateID = certificateID;
        this.studentID = studentID;
        this.courseID = courseID;
        this.issueDate = new Date();
        this.studentName = studentName;
        this.courseTitle = courseTitle;
        this.instructorName = instructorName;
        this.finalScore = finalScore;
    }
    
    
    public Certificate() {// this is an empty constructor for json parsing
        this.issueDate = new Date();
    }
    
    public int getCertificateID() {
        return certificateID;
    }
    
    public void setCertificateID(int certificateID) {
        this.certificateID = certificateID;
    }
    
    public Date getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
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
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
    
    public String getInstructorName() {
        return instructorName;
    }
    
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
    
    public double getFinalScore() {
        return finalScore;
    }
    
    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return "Certificate{" +
                "ID=" + certificateID +
                ", student='" + studentName + '\'' +
                ", course='" + courseTitle + '\'' +
                ", instructor='" + instructorName + '\'' +
                ", score=" + String.format("%.1f", finalScore) +
                ", issued=" + sdf.format(issueDate) +
                '}';
    }
}