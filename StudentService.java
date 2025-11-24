package service;

import database.JsonDatabaseManager;
import model.Course;
import model.Lesson;
import model.Student;
import model.User;
import model.Quiz;
import model.QuizAttempt;

import java.util.ArrayList;
import java.util.List;

public class StudentService {

    private JsonDatabaseManager db;
    private QuizService quizService;

    public StudentService(JsonDatabaseManager db) {
        this.db = db;
        this.quizService = new QuizService(db);
    }

    public List<Course> browseAvailableCourses() {
        List<Course> allCourses = db.loadCourses();
        List<Course> approvedCourses = new ArrayList<>();
        
        for (Course course : allCourses) {
            if (course.isApproved()) {
                approvedCourses.add(course);
            }
        }
        
        return approvedCourses;
    }

    public boolean enrollInCourse(String studentId, int courseId) {
        List<User> users = db.loadUsers();
        List<Course> courses = db.loadCourses();

        Student student = null;
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                student = (Student) u;
                break;
            }
        }
        if (student == null) {
            System.err.println("Student not found: " + studentId);
            return false;
        }

        Course course = null;
        for (Course c : courses) {
            if (c.getCourseID() == courseId) {
                course = c;
                break;
            }
        }
        if (course == null) {
            System.err.println("Course not found: " + courseId);
            return false;
        }

        if (!course.isApproved()) {
            System.err.println("Cannot enroll in course that is not approved: " + courseId);
            return false;
        }

        if (student.isEnrolledIn(String.valueOf(courseId))) {
            System.out.println("Student already enrolled in course " + courseId);
            return false;
        }

        student.enrollCourse(String.valueOf(courseId));
        
        int studentIdInt = Integer.parseInt(student.getUserId());
        if (!course.getEnrolledStudentIDs().contains(studentIdInt)) {
            course.enrollStudent(studentIdInt);
        }

        db.saveUsers(users);
        db.saveCourses(courses);

        return true;
    }

    public List<Course> getEnrolledCourses(String studentId) {
        Student student = db.findStudent(studentId);
        List<Course> enrolled = new ArrayList<>();
        if (student == null) return enrolled;

        List<Course> courses = db.loadCourses();
        for (String cid : student.getEnrolledCourses()) {
            int id = Integer.parseInt(cid);
            for (Course c : courses) {
                if (c.getCourseID() == id) {
                    enrolled.add(c);
                    break;
                }
            }
        }
        return enrolled;
    }

    public List<Lesson> getLessons(int courseId) {
        Course c = db.getCourseById(courseId);
        if (c == null) {
            System.err.println("Course not found: " + courseId);
            return new ArrayList<>();
        }
        return c.getLessons();
    }

    public boolean markLessonCompleted(String studentId, int courseId, int lessonId) {
        List<User> users = db.loadUsers();
        
        Student student = null;
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                student = (Student) u;
                break;
            }
        }
        
        if (student == null) {
            System.err.println("Student not found: " + studentId);
            return false;
        }

        if (!student.isEnrolledIn(String.valueOf(courseId))) {
            System.err.println("Student not enrolled in course: " + courseId);
            return false;
        }

        if (student.isLessonCompleted(String.valueOf(courseId), String.valueOf(lessonId))) {
            System.out.println("Lesson already completed: " + lessonId);
            return false; 
        }

        Course course = db.getCourseById(courseId);
        if (course == null) {
            System.err.println("Course not found: " + courseId);
            return false;
        }
        
        Lesson lesson = null;
        for (Lesson l : course.getLessons()) {
            if (l.getLessonID() == lessonId) {
                lesson = l;
                break;
            }
        }
        
        if (lesson == null) {
            System.err.println("Lesson not found in course: " + lessonId);
            return false;
        }

        // Check if lesson has a required quiz
        if (lesson.getQuiz() != null && lesson.isQuizRequired()) {
            boolean passed = db.hasPassedQuiz(studentId, lessonId, lesson.getQuiz().getQuizId());
            if (!passed) {
                System.err.println("Student must pass quiz before completing lesson: " + lessonId);
                return false;
            }
        }

        String courseIdStr = String.valueOf(courseId);
        String lessonIdStr = String.valueOf(lessonId);
        
        List<String> completed = student.getCompletedLessons()
            .getOrDefault(courseIdStr, new ArrayList<>());
        
        if (!completed.contains(lessonIdStr)) {
            completed.add(lessonIdStr);
            student.getCompletedLessons().put(courseIdStr, completed);
        }

        db.saveUsers(users);
        return true;
    }

    public double calculateProgress(String studentId, String courseId) {
        Student student = db.findStudent(studentId);
        Course course = db.getCourseById(Integer.parseInt(courseId));
        
        if (student == null) {
            System.err.println("Student not found: " + studentId);
            return 0;
        }
        
        if (course == null) {
            System.err.println("Course not found: " + courseId);
            return 0;
        }

        List<String> completed = student.getCompletedLessons()
            .getOrDefault(courseId, new ArrayList<>());
        
        int total = course.getLessons().size();
        
        if (total == 0) {
            return 0;
        }

        return (completed.size() * 100.0) / total;
    }

    public Quiz getQuizForLesson(int courseId, int lessonId) {
        return db.getQuizByCourseAndLessonId(courseId, lessonId);
    }

    public boolean lessonHasQuiz(int courseId, int lessonId) {
        return db.hasQuiz(courseId, lessonId);
    }

    public QuizAttempt submitQuiz(String studentId, int courseId, int lessonId, 
                                   Quiz quiz, List<String> answers) {
        return quizService.submitQuiz(studentId, courseId, lessonId, quiz, answers);
    }

    public List<QuizAttempt> getQuizAttempts(String studentId, int quizId) {
        return db.getStudentQuizAttempts(studentId, quizId);
    }

    public boolean hasPassedQuiz(String studentId, int lessonId, int quizId) {
        return db.hasPassedQuiz(studentId, lessonId, quizId);
    }

    public double getBestQuizScore(String studentId, int quizId) {
        return quizService.getBestScore(studentId, quizId);
    }

    public int getQuizAttemptCount(String studentId, int quizId) {
        return quizService.getAttemptCount(studentId, quizId);
    }
    
    public Student getStudent(String studentId) {
        return db.findStudent(studentId);
    }
}