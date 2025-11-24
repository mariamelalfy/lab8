package service;

import database.JsonDatabaseManager;
import model.*;

import java.util.*;

public class AnalyticsService {
    
    private JsonDatabaseManager db;
    
    public AnalyticsService(JsonDatabaseManager db) {
        this.db = db;
    }
    
    public List<StudentPerformance> getStudentPerformanceList(int courseId) {
        List<StudentPerformance> performanceList = new ArrayList<>();
        
        // Load course
        Course course = findCourse(courseId);
        if (course == null) {
            return performanceList;
        }
        
        // Load all users
        List<User> users = db.loadUsers();
        
        // Get enrolled students
        List<Integer> enrolledStudentIds = course.getEnrolledStudentIDs();
        
        for (Integer studentIdInt : enrolledStudentIds) {
            String studentId = String.valueOf(studentIdInt);
            
            // Find student object
            Student student = findStudent(studentId, users);
            if (student == null) continue;
            
            // Create performance object
            StudentPerformance perf = new StudentPerformance(
                studentId, 
                student.getUsername(), 
                courseId
            );
            
            perf.setStudentEmail(student.getEmail());
            
            // Calculate lessons completed
            int totalLessons = course.getLessons().size();
            perf.setTotalLessons(totalLessons);
            
            List<String> completedLessons = student.getCompletedLessons()
                .getOrDefault(String.valueOf(courseId), new ArrayList<>());
            perf.setLessonsCompleted(completedLessons.size());
            
            // Calculate average quiz score
            double avgQuizScore = calculateAverageQuizScore(studentId, courseId, course);
            perf.setAverageQuizScore(avgQuizScore);
            
            performanceList.add(perf);
        }
        
        return performanceList;
    }
    
    private double calculateAverageQuizScore(String studentId, int courseId, Course course) {
        List<Double> quizScores = new ArrayList<>();
        
        // Get all quizzes for this course
        List<Quiz> courseQuizzes = db.getQuizzesForCourse(courseId);
        
        if (courseQuizzes.isEmpty()) {
            return 0.0;
        }
        
        // For each quiz, get the best score
        for (Quiz quiz : courseQuizzes) {
            List<QuizAttempt> attempts = db.getStudentQuizAttempts(studentId, quiz.getQuizId());
            
            if (!attempts.isEmpty()) {
                // Find best score among all attempts
                double bestScore = 0.0;
                for (QuizAttempt attempt : attempts) {
                    if (attempt.getScore() > bestScore) {
                        bestScore = attempt.getScore();
                    }
                }
                quizScores.add(bestScore);
            }
        }
        
        // Calculate average of best scores
        if (quizScores.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (double score : quizScores) {
            sum += score;
        }
        
        return sum / quizScores.size();
    }
    
    public CourseStatistics getCourseStatistics(int courseId) {
        Course course = findCourse(courseId);
        if (course == null) {
            return null;
        }
        
        CourseStatistics stats = new CourseStatistics(courseId, course.getCourseTitle());
        
        List<StudentPerformance> performances = getStudentPerformanceList(courseId);
        
        stats.setTotalStudentsEnrolled(performances.size());
        
        // Calculate aggregates
        double totalProgress = 0;
        double totalQuizScore = 0;
        int completed = 0;
        
        for (StudentPerformance perf : performances) {
            totalProgress += perf.getCompletionPercentage();
            totalQuizScore += perf.getAverageQuizScore();
            
            if (perf.getCompletionPercentage() == 100.0) {
                completed++;
            }
        }
        
        if (!performances.isEmpty()) {
            stats.setAverageProgress(totalProgress / performances.size());
            stats.setAverageQuizScore(totalQuizScore / performances.size());
        }
        
        stats.setStudentsCompleted(completed);
        
        // Calculate lesson statistics
        for (Lesson lesson : course.getLessons()) {
            CourseStatistics.LessonStats lessonStats = 
                new CourseStatistics.LessonStats(lesson.getLessonID(), lesson.getLessonTitle());
            
            int studentsCompletedLesson = 0;
            
            for (StudentPerformance perf : performances) {
                Student student = findStudent(perf.getStudentId(), db.loadUsers());
                if (student != null && 
                    student.isLessonCompleted(String.valueOf(courseId), 
                                             String.valueOf(lesson.getLessonID()))) {
                    studentsCompletedLesson++;
                }
            }
            
            lessonStats.setStudentsCompleted(studentsCompletedLesson);
            stats.addLessonStats(lesson.getLessonID(), lessonStats);
        }
        
        return stats;
    }
    
    public Map<String, Double> getQuizAveragesByLesson(int courseId) {
        Map<String, Double> lessonAverages = new LinkedHashMap<>();
        
        Course course = findCourse(courseId);
        if (course == null) {
            return lessonAverages;
        }
        
        List<User> users = db.loadUsers();
        List<Integer> enrolledStudents = course.getEnrolledStudentIDs();
        
        // For each lesson with a quiz, calculate average score
        for (Lesson lesson : course.getLessons()) {
            Quiz quiz = lesson.getQuiz();
            
            if (quiz != null) {
                List<Double> allBestScores = new ArrayList<>();
                
                // For each enrolled student, get their best score on this quiz
                for (Integer studentIdInt : enrolledStudents) {
                    String studentId = String.valueOf(studentIdInt);
                    List<QuizAttempt> attempts = db.getStudentQuizAttempts(studentId, quiz.getQuizId());
                    
                    if (!attempts.isEmpty()) {
                        // Find best score among all attempts
                        double bestScore = 0.0;
                        for (QuizAttempt attempt : attempts) {
                            if (attempt.getScore() > bestScore) {
                                bestScore = attempt.getScore();
                            }
                        }
                        allBestScores.add(bestScore);
                    }
                }
                
                // Calculate average of all students' best scores
                double average = 0.0;
                if (!allBestScores.isEmpty()) {
                    double sum = 0.0;
                    for (double score : allBestScores) {
                        sum += score;
                    }
                    average = sum / allBestScores.size();
                }
                
                lessonAverages.put(lesson.getLessonTitle(), average);
            } else {
                // No quiz for this lesson
                lessonAverages.put(lesson.getLessonTitle(), 0.0);
            }
        }
        
        return lessonAverages;
    }
    
    public Map<String, Integer> getLessonCompletionStats(int courseId) {
        Map<String, Integer> completionStats = new LinkedHashMap<>();
        
        Course course = findCourse(courseId);
        if (course == null) {
            return completionStats;
        }
        
        List<User> users = db.loadUsers();
        List<Integer> enrolledStudents = course.getEnrolledStudentIDs();
        
        for (Lesson lesson : course.getLessons()) {
            int completedCount = 0;
            
            for (Integer studentIdInt : enrolledStudents) {
                Student student = findStudent(String.valueOf(studentIdInt), users);
                if (student != null && 
                    student.isLessonCompleted(String.valueOf(courseId), 
                                             String.valueOf(lesson.getLessonID()))) {
                    completedCount++;
                }
            }
            
            completionStats.put(lesson.getLessonTitle(), completedCount);
        }
        
        return completionStats;
    }
    
    public Map<String, Integer> getCourseCompletionBreakdown(int courseId) {
        Map<String, Integer> breakdown = new HashMap<>();
        breakdown.put("Completed", 0);
        breakdown.put("In Progress", 0);
        breakdown.put("Not Started", 0);
        
        List<StudentPerformance> performances = getStudentPerformanceList(courseId);
        
        for (StudentPerformance perf : performances) {
            String status = perf.getStatus();
            if (status.equals("Completed")) {
                breakdown.put("Completed", breakdown.get("Completed") + 1);
            } else if (status.equals("In Progress") || status.equals("Started")) {
                breakdown.put("In Progress", breakdown.get("In Progress") + 1);
            } else {
                breakdown.put("Not Started", breakdown.get("Not Started") + 1);
            }
        }
        
        return breakdown;
    }
    
    public int getTotalEnrollments(int courseId) {
        Course course = findCourse(courseId);
        return (course != null) ? course.getEnrolledStudentIDs().size() : 0;
    }
    
    public double getCourseCompletionRate(int courseId) {
        CourseStatistics stats = getCourseStatistics(courseId);
        return (stats != null) ? stats.getCompletionRate() : 0.0;
    }
    
    private Course findCourse(int courseId) {
        for (Course c : db.loadCourses()) {
            if (c.getCourseID() == courseId) {
                return c;
            }
        }
        return null;
    }
    
    private Student findStudent(String studentId, List<User> users) {
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                return (Student) u;
            }
        }
        return null;
    }
}
