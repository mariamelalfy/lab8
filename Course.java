package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Course {
   
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
    
    private int courseID;
    private String courseTitle;
    private String courseDescription;
    private List<Lesson> lessons;
    private List<Quiz> quizzes;
    private int instructorID;
    private List<Integer> enrolledStudentIDs;
    
   
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private Date submissionDate;
    private Date approvalDate;
    private String reviewedBy; // Admin username who reviewed the course
    
    // Constructor - sets default status to PENDING
    public Course(int courseID, String courseTitle, String courseDescription, int instructorID) {
        this.courseID = courseID;
        this.courseTitle = courseTitle;
        this.courseDescription = courseDescription;
        this.instructorID = instructorID;
        this.lessons = new ArrayList<>();
        this.quizzes = new ArrayList<>();
        this.enrolledStudentIDs = new ArrayList<>();
        
       
        this.approvalStatus = ApprovalStatus.PENDING;
        this.submissionDate = new Date();
        this.rejectionReason = null;
        this.approvalDate = null;
        this.reviewedBy = null;
    }
    
    
    public int getCourseID() {
        return courseID;
    }
    
    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
    
    public String getCourseDescription() {
        return courseDescription;
    }
    
    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }
    
    public List<Lesson> getLessons() {
        return lessons;
    }
    
    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }
    
    public List<Quiz> getQuizzes() {
        return quizzes;
    }
    
    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
    }
    
    public int getInstructorID() {
        return instructorID;
    }
    
    public void setInstructorID(int instructorID) {
        this.instructorID = instructorID;
    }
    
    public List<Integer> getEnrolledStudentIDs() {
        return enrolledStudentIDs;
    }
    
    public void setEnrolledStudentIDs(List<Integer> enrolledStudentIDs) {
        this.enrolledStudentIDs = enrolledStudentIDs;
    }
    
    // New getters and setters for approval workflow
    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public Date getSubmissionDate() {
        return submissionDate;
    }
    
    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }
    
    public Date getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public String getReviewedBy() {
        return reviewedBy;
    }
    
    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }
    
    
    public void approve(String adminUsername) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvalDate = new Date();
        this.reviewedBy = adminUsername;
        this.rejectionReason = null; // Clear any previous rejection reason
    }
    
    public void reject(String adminUsername, String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvalDate = new Date();
        this.reviewedBy = adminUsername;
        this.rejectionReason = reason;
    }
    
    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }
    
    public boolean isPending() {
        return this.approvalStatus == ApprovalStatus.PENDING;
    }
    
    public boolean isRejected() {
        return this.approvalStatus == ApprovalStatus.REJECTED;
    }
    
    // Check if course is visible to students (only approved courses)
    public boolean isVisibleToStudents() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }
    
   
    public double getProgress(int studentID) {
        // Calculate progress based on completed lessons
        if (lessons.isEmpty()) {
            return 0.0;
        }
        
        return 0.0;
    }
    
    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
    }
    
    public void addQuiz(Quiz quiz) {
        this.quizzes.add(quiz);
    }
    
    public void enrollStudent(int studentID) {
        // Only allow enrollment if course is approved
        if (this.isApproved() && !enrolledStudentIDs.contains(studentID)) {
            enrolledStudentIDs.add(studentID);
        }
    }
    
    @Override
    public String toString() {
        return "Course{" +
                "courseID=" + courseID +
                ", courseTitle='" + courseTitle + '\'' +
                ", instructorID=" + instructorID +
                ", approvalStatus=" + approvalStatus +
                ", submissionDate=" + submissionDate +
                '}';
    }
}
