package model;

import java.util.ArrayList;
import java.util.List;

public class Quiz {

    private int quizID;
    private List<Question> questions;
    private int passingScore;
    private boolean required;
    private int courseID;
    private int lessonID;

// Keep a constructor that accepts an explicit quizID (used by DB when IDs are generated externally)
    public Quiz(int quizID) {
        this.quizID = quizID;
        this.questions = new ArrayList<>();
        this.passingScore = 60;
        this.required = false;
}
    

// Simple convenience: create a quiz where quizID==lessonID
    public Quiz(int lessonID, int passingScore, boolean required) {
        this.quizID = lessonID; // default: set quizID equal to lessonID
        this.lessonID = lessonID;
        this.questions = new ArrayList<>();
        this.passingScore = passingScore;
        this.required = required;
    }

// Getters / setters
    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getLessonID() {
        return lessonID;
    }

    public void setLessonID(int lessonID) {
        this.lessonID = lessonID;
    }

// Standard getter used by JsonDatabaseManager (method name expected in your DB code)
    public int getQuizId() {
        return quizID;
    }

// Keep older-named accessor if other code uses it
    public int getQuizID() {
        return quizID;
    }
// Standard getter used by JsonDatabaseManager (method name expected in your DB code)

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(int passingScore) {
        this.passingScore = passingScore;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public double evaluate(List<String> answers) {
        if (answers.size() != questions.size()) {
            return 0.0;
        }
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getCorrectAnswer().equalsIgnoreCase(answers.get(i))) {
                correct++;
            }
        }
        return (double) correct / questions.size() * 100.0;
    }
}
