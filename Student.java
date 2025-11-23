package model;

import database.JsonDatabaseManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student extends User {

    private List<String> enrolledCourses;                    // list of courseIds
    private Map<String, List<String>> completedLessons;      // courseId -> list of lessonIds
    private Map<String, Double> quizScores;                  // lessonId -> score
    private List<String> earnedCertificates;                 // list of certificateIds

    private JsonDatabaseManager dbManager;

    public Student(String userId, String username, String email, String passwordHash) {
        super(userId, username, email, passwordHash, "Student");
        this.enrolledCourses = new ArrayList<>();
        this.completedLessons = new HashMap<>();
        this.quizScores = new HashMap<>();
        this.earnedCertificates = new ArrayList<>();
        this.dbManager = JsonDatabaseManager.getInstance();
    }

    @Override
    public void displayDashboard() {
        System.out.println("Student Dashboard for: " + username);
    }


    public boolean enrollCourse(String courseId) {
        // Check if course exists and is approved
        Course course = dbManager.getCourseById(Integer.parseInt(courseId));

        if (course == null) {
            System.out.println("Course not found!");
            return false;
        }

        if (!course.isApproved()) {
            System.out.println("Cannot enroll: Course is not approved yet!");
            return false;
        }

        if (!enrolledCourses.contains(courseId)) {
            enrolledCourses.add(courseId);
            completedLessons.put(courseId, new ArrayList<>());

            // Add student to the course’s enrolled list
            course.enrollStudent(Integer.parseInt(this.getUserId()));
            dbManager.updateCourse(course);

            // Update student in database
            List<User> users = dbManager.loadUsers();
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserId().equals(this.getUserId())) {
                    users.set(i, this);
                    break;
                }
            }
            dbManager.saveUsers(users);

            System.out.println("Successfully enrolled in course: " + course.getCourseTitle());
            return true;
        }

        return false;
    }

    public boolean isEnrolledIn(String courseId) {
        return enrolledCourses.contains(courseId);
    }


    public List<Course> getAvailableCourses() {
        List<Course> allCourses = dbManager.getAllCourses();
        List<Course> approvedCourses = new ArrayList<>();

        for (Course course : allCourses) {
            if (course.isApproved()) {
                approvedCourses.add(course);
            }
        }
        return approvedCourses;
    }

    public void browseCourses() {
        List<Course> available = getAvailableCourses();
        System.out.println("=== Available Courses (Approved Only) ===");
        for (Course course : available) {
            System.out.println("- " + course.getCourseTitle() +
                    " (ID: " + course.getCourseID() + ")");
        }
    }

    public void viewCourseContent(int courseID) {
        Course course = dbManager.getCourseById(courseID);

        if (course == null) {
            System.out.println("Course not found!");
            return;
        }

        if (!course.isApproved()) {
            System.out.println("Cannot view: Course is not approved!");
            return;
        }

        System.out.println("Viewing course content: " + courseID);
    }


    public void takeQuiz(int quizID) {
        System.out.println("Taking quiz: " + quizID);
    }

    public void recordQuizScore(String lessonId, double score) {
        quizScores.put(lessonId, score);
    }

    public double getQuizScore(String lessonId) {
        return quizScores.getOrDefault(lessonId, 0.0);
    }

    public boolean hasPassedQuiz(String lessonId) {
        return quizScores.getOrDefault(lessonId, 0.0) >= 60.0;
    }

    public Map<String, Double> getQuizScores() {
        return quizScores;
    }

    public void setQuizScores(Map<String, Double> quizScores) {
        this.quizScores = quizScores != null ? quizScores : new HashMap<>();
    }

    public void earnCertificate(int courseID) {
        System.out.println("Earning certificate for course: " + courseID);
    }

    public void addCertificate(String certificateId) {
        if (!earnedCertificates.contains(certificateId)) {
            earnedCertificates.add(certificateId);
        }
    }

    public boolean hasCertificate(String certificateId) {
        return earnedCertificates.contains(certificateId);
    }

    public List<String> getEarnedCertificates() {
        return earnedCertificates;
    }

    public void setEarnedCertificates(List<String> earnedCertificates) {
        this.earnedCertificates = earnedCertificates != null ?
                earnedCertificates : new ArrayList<>();
    }

   
    public void markLessonComplete(String courseId, String lessonId) {
        if (!completedLessons.containsKey(courseId)) {
            completedLessons.put(courseId, new ArrayList<>());
        }
        List<String> lessons = completedLessons.get(courseId);
        if (!lessons.contains(lessonId)) {
            lessons.add(lessonId);
        }
    }

    public boolean isLessonCompleted(String courseId, String lessonId) {
        return completedLessons.containsKey(courseId) &&
                completedLessons.get(courseId).contains(lessonId);
    }

    public double getCourseProgress(String courseId, int totalLessons) {
        if (!completedLessons.containsKey(courseId) || totalLessons == 0) {
            return 0.0;
        }
        return (double) completedLessons.get(courseId).size() / totalLessons * 100;
    }

    
    public List<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses != null ?
                enrolledCourses : new ArrayList<>();
    }

    public Map<String, List<String>> getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(Map<String, List<String>> completedLessons) {
        this.completedLessons = completedLessons != null ?
                completedLessons : new HashMap<>();
    }
}
