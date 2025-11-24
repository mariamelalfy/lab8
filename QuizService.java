// ============================================================
// FILE: service/QuizService.java
// ============================================================
package service;

import database.JsonDatabaseManager;
import model.*;


import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

public class QuizService {

    private JsonDatabaseManager db;

    public QuizService() {
        this.db = new JsonDatabaseManager();
    }

    public QuizService(JsonDatabaseManager db) {
        this.db = db;
    }

    // Submit a quiz and save the attempt
    public QuizAttempt submitQuiz(String studentId, int courseId, int lessonId, 
                                   Quiz quiz, List<String> studentAnswers) {
        
        // Calculate score using the quiz's evaluate method
        double score = quiz.evaluate(studentAnswers);
        boolean passed = score >= quiz.getPassingScore();

        // Create the attempt
        QuizAttempt attempt = new QuizAttempt(
            db.generateAttemptId(),
            studentId,
            quiz.getQuizID(),
            lessonId,
            courseId,
            studentAnswers,
            score,
            passed,
            Instant.now()
        );

        // Save to database
        db.addQuizAttempt(attempt);

        return attempt;
    }

    // Get quiz for a specific lesson
    public Quiz getQuizForLesson(int courseId, int lessonId) {
        List<Course> courses = db.loadCourses();

        for (Course c : courses) {
            if (c.getCourseID() == courseId) {
                for (Lesson l : c.getLessons()) {
                    if (l.getLessonID() == lessonId) {
                        return l.getQuiz();
                    }
                }
            }
        }

        return null;
    }

    // Get all attempts for a student on a specific quiz
    public List<QuizAttempt> getStudentAttempts(String studentId, int quizId) {
        return db.getStudentQuizAttempts(studentId, quizId);
    }

    // Check if student can proceed to next lesson
    public boolean canProceedToNextLesson(String studentId, int courseId, int lessonId) {
        List<Course> courses = db.loadCourses();

        for (Course c : courses) {
            if (c.getCourseID() == courseId) {
                for (Lesson l : c.getLessons()) {
                    if (l.getLessonID() == lessonId) {
                        // If no quiz or quiz not required, can proceed
                        if (l.getQuiz() == null || !l.isQuizRequired()) {
                            return true;
                        }
                        // Check if passed the quiz
                        return db.hasPassedQuiz(studentId, lessonId, l.getQuiz().getQuizID());
                    }
                }
            }
        }

        return true;
    }

    // Get the best score for a student on a quiz
    public double getBestScore(String studentId, int quizId) {
        List<QuizAttempt> attempts = db.getStudentQuizAttempts(studentId, quizId);
        double best = 0.0;

        for (QuizAttempt a : attempts) {
            if (a.getScore() > best) {
                best = a.getScore();
            }
        }

        return best;
    }

    // Get number of attempts for a student on a quiz
    public int getAttemptCount(String studentId, int quizId) {
        return db.getStudentQuizAttempts(studentId, quizId).size();
    }

    // Check if student has passed a specific quiz
    public boolean hasPassed(String studentId, int lessonId, int quizId) {
        return db.hasPassedQuiz(studentId, lessonId, quizId);
    }
}