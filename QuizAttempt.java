package model;

import java.time.Instant;
import java.util.List;

public class QuizAttempt {

    private int attemptId;
    private String studentId;
    private int quizId;
    private int lessonId;
    private int courseId;
    private List<String> studentAnswers;
    private double score;
    private boolean passed;
    private Instant attemptDate;

    public QuizAttempt() {
    }

    public QuizAttempt(int attemptId, String studentId, int quizId, int lessonId,
            int courseId, List<String> studentAnswers, double score,
            boolean passed, Instant attemptDate) {
        this.attemptId = attemptId;
        this.studentId = studentId;
        this.quizId = quizId;
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.studentAnswers = studentAnswers;
        this.score = score;
        this.passed = passed;
        this.attemptDate = attemptDate;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public List<String> getStudentAnswers() {
        return studentAnswers;
    }

    public void setStudentAnswers(List<String> studentAnswers) {
        this.studentAnswers = studentAnswers;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Instant getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(Instant attemptDate) {
        this.attemptDate = attemptDate;
    }
}
