package database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import model.Course;
import model.Course.ApprovalStatus;
import model.Instructor;
import model.Lesson;
import model.Student;
import model.User;
import model.Admin;
import org.json.JSONException;

public class JsonDatabaseManager {
    
    private static JsonDatabaseManager instance;
    private static final String USERS_FILE = "database/users.json";
    private static final String COURSES_FILE = "database/courses.json";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   
    public static JsonDatabaseManager getInstance() {
        if (instance == null) {
            instance = new JsonDatabaseManager();
        }
        return instance;
    }

    private String readFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                // Return appropriate empty structure based on file type
                if (path.contains("users")) {
                    return "{\"users\":[]}";
                } else {
                    return "{\"courses\":[]}";
                }
            }
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            if (path.contains("users")) {
                return "{\"users\":[]}";
            } else {
                return "{\"courses\":[]}";
            }
        }
    }

    private void writeFile(String path, String content) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        try {
            String json = readFile(USERS_FILE);
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.getJSONArray("users");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String role = obj.getString("role");
                String userId = obj.getString("userId");
                String username = obj.getString("username");
                String email = obj.getString("email");
                String pass = obj.getString("passwordHash");

                if (role.equals("Student")) {
                    Student s = new Student(userId, username, email, pass);

                    // enrolledCourses
                    if (obj.has("enrolledCourses")) {
                        JSONArray enrolled = obj.getJSONArray("enrolledCourses");
                        List<String> enrolledList = new ArrayList<>();
                        for (int j = 0; j < enrolled.length(); j++) {
                            enrolledList.add(enrolled.getString(j));
                        }
                        s.setEnrolledCourses(enrolledList);
                    }

                    // completedLessons
                    if (obj.has("completedLessons")) {
                        JSONObject completedObj = obj.getJSONObject("completedLessons");
                        Map<String, List<String>> completedMap = new HashMap<>();
                        Iterator<String> keys = completedObj.keys();
                        while (keys.hasNext()) {
                            String courseId = keys.next();
                            JSONArray lessons = completedObj.getJSONArray(courseId);
                            List<String> lessonList = new ArrayList<>();
                            for (int j = 0; j < lessons.length(); j++) {
                                lessonList.add(lessons.getString(j));
                            }
                            completedMap.put(courseId, lessonList);
                        }
                        s.setCompletedLessons(completedMap);
                    }

                    users.add(s);

                } else if (role.equals("Instructor")) {
                    Instructor t = new Instructor(userId, username, email, pass);

                    if (obj.has("createdCourses")) {
                        JSONArray created = obj.getJSONArray("createdCourses");
                        List<String> createdList = new ArrayList<>();
                        for (int j = 0; j < created.length(); j++) {
                            createdList.add(created.getString(j));
                        }
                        t.setCreatedCourses(createdList);
                    }

                    users.add(t);
                    
                } else if (role.equals("Admin")) {
                    // Handle Admin users
                    Admin admin = new Admin(userId, username, email, pass);
                    
                    if (obj.has("managedCourses")) {
                        JSONArray managed = obj.getJSONArray("managedCourses");
                        List<String> managedList = new ArrayList<>();
                        for (int j = 0; j < managed.length(); j++) {
                            managedList.add(managed.getString(j));
                        }
                        admin.setManagedCourses(managedList);
                    }
                    
                    users.add(admin);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void saveUsers(List<User> users) {
        JSONArray arr = new JSONArray();

        for (User u : users) {
            JSONObject obj = new JSONObject();

            obj.put("userId", u.getUserId());
            obj.put("username", u.getUsername());
            obj.put("email", u.getEmail());
            obj.put("passwordHash", u.getPasswordHash());
            obj.put("role", u.getRole());

            if (u instanceof Student) {
                Student s = (Student) u;
                obj.put("enrolledCourses", new JSONArray(s.getEnrolledCourses()));

                JSONObject completedObj = new JSONObject();
                for (String courseId : s.getCompletedLessons().keySet()) {
                    completedObj.put(courseId, new JSONArray(s.getCompletedLessons().get(courseId)));
                }
                obj.put("completedLessons", completedObj);

            } else if (u instanceof Instructor) {
                Instructor t = (Instructor) u;
                obj.put("createdCourses", new JSONArray(t.getCreatedCourses()));
                
            } else if (u instanceof Admin) {
                // Save Admin-specific data
                Admin admin = (Admin) u;
                obj.put("managedCourses", new JSONArray(admin.getManagedCourses()));
            }

            arr.put(obj);
        }

        JSONObject root = new JSONObject();
        root.put("users", arr);

        writeFile(USERS_FILE, root.toString(4));
    }


    public List<Course> loadCourses() {
        List<Course> courses = new ArrayList<>();

        try {
            String json = readFile(COURSES_FILE);
            
            JSONObject root = new JSONObject(json);
            if (!root.has("courses")) {
                return courses;
            }
            
            JSONArray arr = root.getJSONArray("courses");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject cobj = arr.getJSONObject(i);

                Course c = new Course(
                    cobj.getInt("courseId"),
                    cobj.getString("title"),
                    cobj.getString("description"),
                    cobj.getInt("instructorId")
                );

                // Load approval status (with backward compatibility)
                if (cobj.has("approvalStatus")) {
                    String statusStr = cobj.getString("approvalStatus");
                    c.setApprovalStatus(ApprovalStatus.valueOf(statusStr));
                } else {
                    // Default for old courses without approval status
                    c.setApprovalStatus(ApprovalStatus.APPROVED);
                }
                
                // Load approval-related fields
                if (cobj.has("rejectionReason") && !cobj.isNull("rejectionReason")) {
                    c.setRejectionReason(cobj.getString("rejectionReason"));
                }
                
                if (cobj.has("reviewedBy") && !cobj.isNull("reviewedBy")) {
                    c.setReviewedBy(cobj.getString("reviewedBy"));
                }
                
                // Load dates
                if (cobj.has("submissionDate") && !cobj.isNull("submissionDate")) {
                    try {
                        Date submissionDate = dateFormat.parse(cobj.getString("submissionDate"));
                        c.setSubmissionDate(submissionDate);
                    } catch (ParseException e) {
                        System.err.println("Error parsing submission date: " + e.getMessage());
                    }
                }
                
                if (cobj.has("approvalDate") && !cobj.isNull("approvalDate")) {
                    try {
                        Date approvalDate = dateFormat.parse(cobj.getString("approvalDate"));
                        c.setApprovalDate(approvalDate);
                    } catch (ParseException e) {
                        System.err.println("Error parsing approval date: " + e.getMessage());
                    }
                }

                // lessons
                if (cobj.has("lessons")) {
                    JSONArray lessonsArr = cobj.getJSONArray("lessons");
                    for (int j = 0; j < lessonsArr.length(); j++) {
                        JSONObject lobj = lessonsArr.getJSONObject(j);

                        Lesson l = new Lesson(
                            lobj.getInt("lessonId"),
                            lobj.getString("title"),
                            lobj.getString("content")
                        );

                        c.getLessons().add(l);
                    }
                }

                // students
                if (cobj.has("students")) {
                    JSONArray studs = cobj.getJSONArray("students");
                    List<Integer> studentIds = new ArrayList<>();
                    for (int j = 0; j < studs.length(); j++) {
                        studentIds.add(studs.getInt(j));
                    }
                    c.setEnrolledStudentIDs(studentIds);
                }

                courses.add(c);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return courses;
    }

    public void saveCourses(List<Course> courses) {
        JSONArray arr = new JSONArray();

        for (Course c : courses) {
            JSONObject obj = new JSONObject();

            obj.put("courseId", c.getCourseID());
            obj.put("title", c.getCourseTitle());
            obj.put("description", c.getCourseDescription());
            obj.put("instructorId", c.getInstructorID());
            
            // Save approval status fields
            obj.put("approvalStatus", c.getApprovalStatus().toString());
            
            if (c.getRejectionReason() != null) {
                obj.put("rejectionReason", c.getRejectionReason());
            } else {
                obj.put("rejectionReason", JSONObject.NULL);
            }
            
            if (c.getReviewedBy() != null) {
                obj.put("reviewedBy", c.getReviewedBy());
            } else {
                obj.put("reviewedBy", JSONObject.NULL);
            }
            
            if (c.getSubmissionDate() != null) {
                obj.put("submissionDate", dateFormat.format(c.getSubmissionDate()));
            } else {
                obj.put("submissionDate", JSONObject.NULL);
            }
            
            if (c.getApprovalDate() != null) {
                obj.put("approvalDate", dateFormat.format(c.getApprovalDate()));
            } else {
                obj.put("approvalDate", JSONObject.NULL);
            }

            JSONArray lessons = new JSONArray();
            for (Lesson l : c.getLessons()) {
                JSONObject lo = new JSONObject();
                lo.put("lessonId", l.getLessonID());
                lo.put("title", l.getLessonTitle());
                lo.put("content", l.getLessonContent());
                lessons.put(lo);
            }
            obj.put("lessons", lessons);

            obj.put("students", new JSONArray(c.getEnrolledStudentIDs()));

            arr.put(obj);
        }

        JSONObject root = new JSONObject();
        root.put("courses", arr);

        writeFile(COURSES_FILE, root.toString(4));
    }


    public int generateUserId(List<User> users) {
        int maxId = 0;
        for (User user : users) {
            try {
                int id = Integer.parseInt(user.getUserId());
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric IDs
            }
        }
        return maxId + 1;
    }

    public int generateCourseId(List<Course> courses) {
        return courses.stream().mapToInt(Course::getCourseID).max().orElse(0) + 1;
    }

    public int generateLessonId(Course course) {
        return course.getLessons().stream()
            .mapToInt(Lesson::getLessonID)
            .max().orElse(0) + 1;
    }
    
    public Student findStudent(String studentId) {
        for (User u : loadUsers()) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                return (Student) u;
            }
        }
        return null; 
    }

    public Course findCourse(String courseId) {
        for (Course c : loadCourses()) {
            if (String.valueOf(c.getCourseID()).equals(courseId)) {
                return c;
            }
        }
        return null;
    }
    
    // ============= NEW METHODS FOR ADMIN FUNCTIONALITY =============
    
    /**
     * Get all courses from database
     * @return List of all courses
     */
    public List<Course> getAllCourses() {
        return loadCourses();
    }
    
    /**
     * Get a course by its ID
     * @param courseId The course ID
     * @return Course object or null if not found
     */
    public Course getCourseById(int courseId) {
        List<Course> courses = loadCourses();
        
        for (Course course : courses) {
            if (course.getCourseID() == courseId) {
                return course;
            }
        }
        
        return null;
    }
    
    /**
     * Update a course in the database
     * This replaces the existing course with the same ID
     * @param updatedCourse The course to update
     */
    public void updateCourse(Course updatedCourse) {
        List<Course> courses = loadCourses();
        boolean found = false;
        
        // Find and replace the course
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseID() == updatedCourse.getCourseID()) {
                courses.set(i, updatedCourse);
                found = true;
                break;
            }
        }
        
        if (!found) {
            System.err.println("Course not found for update: " + updatedCourse.getCourseID());
            return;
        }
        
        // Save all courses back to file
        saveCourses(courses);
    }
    
    /**
     * Get courses by approval status
     * @param status The approval status to filter by
     * @return List of courses with the specified status
     */
    public List<Course> getCoursesByStatus(ApprovalStatus status) {
        List<Course> allCourses = loadCourses();
        List<Course> filteredCourses = new ArrayList<>();
        
        for (Course course : allCourses) {
            if (course.getApprovalStatus() == status) {
                filteredCourses.add(course);
            }
        }
        
        return filteredCourses;
    }
    
    /**
     * Find admin by username
     * @param username The admin's username
     * @return Admin object or null if not found
     */
    public Admin findAdminByUsername(String username) {
        List<User> users = loadUsers();
        
        for (User user : users) {
            if (user instanceof Admin && user.getUsername().equals(username)) {
                return (Admin) user;
            }
        }
        
        return null;
    }
    
    /**
     * Save a single admin user
     * @param admin The admin to save
     */
    public void saveAdmin(Admin admin) {
        List<User> users = loadUsers();
        boolean found = false;
        
        // Update existing admin or add new one
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(admin.getUserId())) {
                users.set(i, admin);
                found = true;
                break;
            }
        }
        
        if (!found) {
            users.add(admin);
        }
        
        saveUsers(users);
    }
}