package service;

import database.JsonDatabaseManager;
import model.*;

import java.util.ArrayList;
import java.util.List;

public class CourseService {

    private final JsonDatabaseManager db;

    public CourseService(JsonDatabaseManager db) {
        this.db = db;
    }

    // Load all courses
    public List<Course> loadCourses() {
        return db.loadCourses();
    }

    // Save all courses (overwrite)
    private void saveCourses(List<Course> courses) {
        db.saveCourses(courses);
    }

    // Get course by numeric id (course.getCourseID())
    public Course getCourseById(int courseId) {
        List<Course> courses = db.loadCourses();
        for (Course c : courses) {
            if (c.getCourseID() == courseId) return c;
        }
        return null;
    }

    // Create a course (returns created Course)
    public Course createCourse(Instructor instructor, String title, String description) {
        List<Course> courses = db.loadCourses();
        int newId = db.generateCourseId(courses); // your JsonDatabaseManager already has generateCourseId(list)
        Course c = new Course(newId, title, description, Integer.parseInt(instructor.getUserId()));
        // ensure lessons and students lists exist (constructor should do that)
        courses.add(c);
        saveCourses(courses);
        return c;
    }

    // Edit course (by int id)
    public boolean editCourse(int courseId, String newTitle, String newDescription) {
        List<Course> courses = db.loadCourses();
        for (Course c : courses) {
            if (c.getCourseID() == courseId) {
                c.setCourseTitle(newTitle);
                c.setCourseDescription(newDescription);
                saveCourses(courses);
                return true;
            }
        }
        return false;
    }

    // Delete course
    public boolean deleteCourse(int courseId) {
        List<Course> courses = db.loadCourses();
        boolean removed = courses.removeIf(c -> c.getCourseID() == courseId);
        if (removed) saveCourses(courses);
        return removed;
    }

    // Add lesson to course
    public boolean addLesson(int courseId, String title, String content) {
        List<Course> courses = db.loadCourses();
        for (Course c : courses) {
            if (c.getCourseID() == courseId) {
                int newLessonId = db.generateLessonId(c); // uses your JsonDatabaseManager method
                Lesson lesson = new Lesson(newLessonId, title, content);
                c.getLessons().add(lesson);
                saveCourses(courses);
                return true;
            }
        }
        return false;
    }

    // Edit lesson
    public boolean editLesson(int courseId, int lessonId, String newTitle, String newContent) {
        Course course = getCourseById(courseId);
        if (course == null) return false;
        for (Lesson l : course.getLessons()) {
            if (l.getLessonID() == lessonId) {
                l.setLessonTitle(newTitle);
                l.setLessonContent(newContent);
                saveCourses(db.loadCourses()); // write updated list
                return true;
            }
        }
        return false;
    }

    // Delete lesson
    public boolean deleteLesson(int courseId, int lessonId) {
        Course course = getCourseById(courseId);
        if (course == null) return false;
        boolean removed = course.getLessons().removeIf(l -> l.getLessonID() == lessonId);
        if (removed) saveCourses(db.loadCourses());
        return removed;
    }

    // Enroll a student (studentId is numeric string or numeric id depending on your models)
    // This adds student id to both student's enrolled list (handled elsewhere) and course.enrolledStudentIDs
    public boolean enrollStudentToCourse(int courseId, int studentNumericId) {
        List<Course> courses = db.loadCourses();
        for (Course c : courses) {
            if (c.getCourseID() == courseId) {
                if (!c.getEnrolledStudentIDs().contains(studentNumericId)) {
                    c.getEnrolledStudentIDs().add(studentNumericId);
                    saveCourses(courses);
                    return true;
                } else {
                    return false; // already enrolled
                }
            }
        }
        return false;
    }

    // Unenroll student
    public boolean unenrollStudentFromCourse(int courseId, int studentNumericId) {
        List<Course> courses = db.loadCourses();
        for (Course c : courses) {
            if (c.getCourseID() == courseId) {
                boolean removed = c.getEnrolledStudentIDs().removeIf(id -> id == studentNumericId);
                if (removed) saveCourses(courses);
                return removed;
            }
        }
        return false;
    }

    // Get lessons for a course
    public List<Lesson> getLessons(int courseId) {
        Course c = getCourseById(courseId);
        return c == null ? new ArrayList<>() : c.getLessons();
    }

    // View enrolled student IDs (integers)
    public List<Integer> viewEnrolledStudentIds(int courseId) {
        Course c = getCourseById(courseId);
        return c == null ? new ArrayList<>() : new ArrayList<>(c.getEnrolledStudentIDs());
    }

    // Get courses by instructor id (numeric)
    public List<Course> getCoursesByInstructor(int instructorNumericId) {
        List<Course> result = new ArrayList<>();
        for (Course c : db.loadCourses()) {
            if (c.getInstructorID() == instructorNumericId) result.add(c);
        }
        return result;
    }
}
