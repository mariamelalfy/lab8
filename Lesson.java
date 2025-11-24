package model;

import java.util.ArrayList;
import java.util.List;

public class Lesson {

    private int lessonID;
    private String lessonTitle;
    private String lessonContent;
    private List<String> resources;
    private Quiz quiz;
    private boolean quizRequired;

    public Lesson(int lessonID, String lessonTitle, String lessonContent, Quiz quiz, boolean quizRequired) {
        this.lessonID = lessonID;
        this.lessonTitle = lessonTitle;
        this.lessonContent = lessonContent;
        this.quiz = null;
        this.quizRequired = false;
    }
    
     public Lesson(int lessonID, String lessonTitle, String lessonContent) {
        this.lessonID = lessonID;
        this.lessonTitle = lessonTitle;
        this.lessonContent = lessonContent;
    }

    public int getLessonID() {
        return lessonID;
    }

    public void setLessonID(int lessonID) {
        this.lessonID = lessonID;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public String getLessonContent() {
        return lessonContent;
    }

    public void setLessonContent(String lessonContent) {
        this.lessonContent = lessonContent;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public void addResource(String resource) {
        this.resources.add(resource);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public boolean isQuizRequired() {
        return quizRequired;
    }

    public void setQuizRequired(boolean quizRequired) {
        this.quizRequired = quizRequired;
    }
}

