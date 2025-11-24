package database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import model.*;
import model.Course.ApprovalStatus;
import org.json.JSONException;

public class JsonDatabaseManager {
    
    private static JsonDatabaseManager instance;
    private static final String USERS_FILE = "database/users.json";
    private static final String COURSES_FILE = "database/courses.json";
    private static final String QUIZZES_FILE = "database/quizzes.json";
    private static final String CERTIFICATES_FILE = "database/certificates.json";
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
                } else if (path.contains("courses")) {
                    return "{\"courses\":[]}";
                } else if (path.contains("quizzes")) {
                    return "{\"quizzes\":[],\"attempts\":[]}";
                } else if (path.contains("certificates")) {
                    return "{\"certificates\":[]}";
                } else {
                    return "{}";
                }
            }
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            if (path.contains("users")) {
                return "{\"users\":[]}";
            } else if (path.contains("courses")) {
                return "{\"courses\":[]}";
            } else if (path.contains("quizzes")) {
                return "{\"quizzes\":[],\"attempts\":[]}";
            } else if (path.contains("certificates")) {
                return "{\"certificates\":[]}";
            } else {
                return "{}";
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

                    // quizScores - ADDED
                    if (obj.has("quizScores")) {
                        JSONObject scoresObj = obj.getJSONObject("quizScores");
                        Map<String, Double> scoresMap = new HashMap<>();
                        Iterator<String> scoreKeys = scoresObj.keys();
                        while (scoreKeys.hasNext()) {
                            String lessonId = scoreKeys.next();
                            scoresMap.put(lessonId, scoresObj.getDouble(lessonId));
                        }
                        s.setQuizScores(scoresMap);
                    }

                    // earnedCertificates - ADDED
                    if (obj.has("earnedCertificates")) {
                        JSONArray certsArr = obj.getJSONArray("earnedCertificates");
                        List<String> certsList = new ArrayList<>();
                        for (int j = 0; j < certsArr.length(); j++) {
                            certsList.add(certsArr.getString(j));
                        }
                        s.setEarnedCertificates(certsList);
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

                // quizScores - ADDED
                JSONObject scoresObj = new JSONObject();
                for (String lessonId : s.getQuizScores().keySet()) {
                    scoresObj.put(lessonId, s.getQuizScores().get(lessonId));
                }
                obj.put("quizScores", scoresObj);

                // earnedCertificates - ADDED
                obj.put("earnedCertificates", new JSONArray(s.getEarnedCertificates()));

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
    
    List<Quiz> allQuizzes = loadQuizzes();

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

                    for (Quiz quiz : allQuizzes) {
                        if (quiz.getCourseID() == c.getCourseID() 
                            && quiz.getLessonID() == l.getLessonID()) {
                            l.setQuiz(quiz);
                            l.setQuizRequired(quiz.isRequired());
                            break;
                        }
                    }

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

    return courses;}
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

    public List<Quiz> loadQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();

        try {
            String json = readFile(QUIZZES_FILE);
            JSONObject root = new JSONObject(json);

            if (!root.has("quizzes")) {
                return quizzes;
            }

            JSONArray arr = root.getJSONArray("quizzes");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject quizObj = arr.getJSONObject(i);

                Quiz quiz = new Quiz(
                        quizObj.getInt("quizId"),
                        quizObj.getInt("passingScore"),
                        quizObj.getBoolean("required")
                );

                // Set additional fields
                quiz.setCourseID(quizObj.getInt("courseId"));
                quiz.setLessonID(quizObj.getInt("lessonId"));

                // Load questions
                if (quizObj.has("questions")) {
                    JSONArray questionsArr = quizObj.getJSONArray("questions");
                    List<Question> questions = new ArrayList<>();

                    for (int j = 0; j < questionsArr.length(); j++) {
                        JSONObject qobj = questionsArr.getJSONObject(j);

                        Question question = new Question(
                                qobj.getInt("questionId"),
                                qobj.getString("questionText"),
                                qobj.getString("optionA"),
                                qobj.getString("optionB"),
                                qobj.getString("optionC"),
                                qobj.getString("optionD"),
                                qobj.getString("correctAnswer"),
                                qobj.has("explanation") && !qobj.isNull("explanation")
                                        ? qobj.getString("explanation") : null
                        );

                        questions.add(question);
                    }

                    quiz.setQuestions(questions);
                }

                quizzes.add(quiz);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return quizzes;
    }

    private void saveQuizzesAndAttempts(List<Quiz> quizzes, List<QuizAttempt> attempts) {
        JSONObject root = new JSONObject();

        // Save quizzes
        JSONArray quizzesArr = new JSONArray();
        for (Quiz quiz : quizzes) {
            JSONObject obj = new JSONObject();

            obj.put("quizId", quiz.getQuizId());
            obj.put("courseId", quiz.getCourseID());
            obj.put("lessonId", quiz.getLessonID());
            obj.put("passingScore", quiz.getPassingScore());
            obj.put("required", quiz.isRequired());

            // Save questions
            JSONArray questionsArr = new JSONArray();
            for (Question q : quiz.getQuestions()) {
                JSONObject qobj = new JSONObject();
                qobj.put("questionId", q.getQuestionID());
                qobj.put("questionText", q.getQuestionText());
                qobj.put("optionA", q.getOptionA());
                qobj.put("optionB", q.getOptionB());
                qobj.put("optionC", q.getOptionC());
                qobj.put("optionD", q.getOptionD());
                qobj.put("correctAnswer", q.getCorrectAnswer());
                qobj.put("explanation", q.getExplanation());

                questionsArr.put(qobj);
            }

            obj.put("questions", questionsArr);
            quizzesArr.put(obj);
        }
        root.put("quizzes", quizzesArr);

        // Save attempts
        JSONArray attemptsArr = new JSONArray();
        for (QuizAttempt a : attempts) {
            JSONObject obj = new JSONObject();

            obj.put("attemptId", a.getAttemptId());
            obj.put("studentId", a.getStudentId());
            obj.put("quizId", a.getQuizId());
            obj.put("lessonId", a.getLessonId());
            obj.put("courseId", a.getCourseId());
            obj.put("studentAnswers", new JSONArray(a.getStudentAnswers()));
            obj.put("score", a.getScore());
            obj.put("passed", a.isPassed());
            obj.put("attemptDate", a.getAttemptDate().toString());

            attemptsArr.put(obj);
        }
        root.put("attempts", attemptsArr);

        writeFile(QUIZZES_FILE, root.toString(4));
    }

    public void saveQuizzes(List<Quiz> quizzes) {
        List<QuizAttempt> attempts = loadQuizAttempts();
        saveQuizzesAndAttempts(quizzes, attempts);
    }

    public void saveQuiz(Quiz quiz) {
        List<Quiz> quizzes = loadQuizzes();

        // Remove existing quiz with same ID (if any)
        quizzes.removeIf(q -> q.getQuizId() == quiz.getQuizId());

        // Add the new/updated quiz
        quizzes.add(quiz);

        // Save all
        saveQuizzes(quizzes);
    }

    public Quiz getQuizById(int quizId) {
        List<Quiz> quizzes = loadQuizzes();
        for (Quiz q : quizzes) {
            if (q.getQuizId() == quizId) {
                return q;
            }
        }
        return null;
    }
    
    public Quiz getQuizByCourseAndLessonId(int courseId, int lessonId) {
        List<Quiz> quizzes = loadQuizzes();
        for (Quiz q : quizzes) {
            if (q.getCourseID() == courseId && q.getLessonID() == lessonId) {
                return q;
            }
        }
        return null;
    }

    public List<Quiz> getQuizzesForCourse(int courseID) {
        List<Quiz> result = new ArrayList<>();
        List<Quiz> allQuizzes = loadQuizzes();

        for (Quiz q : allQuizzes) {
            if (q.getCourseID() == courseID) {
                result.add(q);
            }
        }

        return result;
    }

    public boolean deleteQuiz(int quizId) {
        List<Quiz> quizzes = loadQuizzes();
        boolean removed = quizzes.removeIf(q -> q.getQuizId() == quizId);

        if (removed) {
            saveQuizzes(quizzes);
        }

        return removed;
    }

    public boolean deleteQuizByLessonId(int lessonID) {
        List<Quiz> quizzes = loadQuizzes();
        boolean removed = quizzes.removeIf(q -> q.getLessonID() == lessonID);

        if (removed) {
            saveQuizzes(quizzes);
        }

        return removed;
    }

    public boolean hasQuiz(int courseId, int lessonId) {
        return getQuizByCourseAndLessonId(courseId, lessonId) != null;
    }

    public int generateQuizId() {
        List<Quiz> quizzes = loadQuizzes();
        int maxId = 0;
        for (Quiz q : quizzes) {
            if (q.getQuizId() > maxId) {
                maxId = q.getQuizId();
            }
        }
        return maxId + 1;
    }

    
    public List<QuizAttempt> loadQuizAttempts() {
        List<QuizAttempt> attempts = new ArrayList<>();

        try {
            String json = readFile(QUIZZES_FILE);
            JSONObject root = new JSONObject(json);

            if (!root.has("attempts")) {
                return attempts;
            }

            JSONArray arr = root.getJSONArray("attempts");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                List<String> answers = new ArrayList<>();
                JSONArray answersArr = obj.getJSONArray("studentAnswers");
                for (int j = 0; j < answersArr.length(); j++) {
                    answers.add(answersArr.getString(j));
                }

                QuizAttempt a = new QuizAttempt(
                        obj.getInt("attemptId"),
                        obj.getString("studentId"),
                        obj.getInt("quizId"),
                        obj.getInt("lessonId"),
                        obj.getInt("courseId"),
                        answers,
                        obj.getDouble("score"),
                        obj.getBoolean("passed"),
                        java.time.Instant.parse(obj.getString("attemptDate"))
                );

                attempts.add(a);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return attempts;
    }

    public void saveQuizAttempts(List<QuizAttempt> attempts) {
        List<Quiz> quizzes = loadQuizzes();
        saveQuizzesAndAttempts(quizzes, attempts);
    }

    public void addQuizAttempt(QuizAttempt attempt) {
        List<QuizAttempt> list = loadQuizAttempts();
        list.add(attempt);
        saveQuizAttempts(list);
    }

    public List<QuizAttempt> getStudentQuizAttempts(String studentId, int quizId) {
        List<QuizAttempt> all = loadQuizAttempts();
        List<QuizAttempt> result = new ArrayList<>();

        for (QuizAttempt a : all) {
            if (a.getStudentId().equals(studentId) && a.getQuizId() == quizId) {
                result.add(a);
            }
        }

        return result;
    }

    public boolean hasPassedQuiz(String studentId, int lessonId, int quizId) {
        List<QuizAttempt> all = loadQuizAttempts();

        for (QuizAttempt a : all) {
            if (a.getStudentId().equals(studentId)
                    && a.getLessonId() == lessonId
                    && a.getQuizId() == quizId
                    && a.isPassed()) {
                return true;
            }
        }

        return false;
    }

    public int generateAttemptId() {
        List<QuizAttempt> attempts = loadQuizAttempts();
        int maxId = 0;
        for (QuizAttempt attempt : attempts) {
            try {
                int id = attempt.getAttemptId();
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric IDs
            }
        }
        return maxId + 1;
    }

    public List<Certificate> loadCertificates() {
        List<Certificate> certificates = new ArrayList<>();
        
        try {
            String json = readFile(CERTIFICATES_FILE);
            JSONObject root = new JSONObject(json);
            
            if (!root.has("certificates")) {
                return certificates;
            }
            
            JSONArray arr = root.getJSONArray("certificates");
            
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                
                Certificate cert = new Certificate();
                cert.setCertificateID(obj.getInt("certificateId"));
                cert.setStudentID(obj.getInt("studentId"));
                cert.setCourseID(obj.getInt("courseId"));
                cert.setStudentName(obj.getString("studentName"));
                cert.setCourseTitle(obj.getString("courseTitle"));
                cert.setInstructorName(obj.getString("instructorName"));
                cert.setFinalScore(obj.getDouble("finalScore"));
                
                // Parse date
                if (obj.has("issueDate")) {
                    long timestamp = obj.getLong("issueDate");
                    cert.setIssueDate(new java.util.Date(timestamp));
                }
                
                certificates.add(cert);
            }
            
        } catch (JSONException e) {
            System.err.println("Error loading certificates: " + e.getMessage());
            e.printStackTrace();
        }
        
        return certificates;
    }

    public void saveCertificates(List<Certificate> certificates) {
        JSONArray arr = new JSONArray();
        
        for (Certificate cert : certificates) {
            JSONObject obj = new JSONObject();
            obj.put("certificateId", cert.getCertificateID());
            obj.put("studentId", cert.getStudentID());
            obj.put("courseId", cert.getCourseID());
            obj.put("studentName", cert.getStudentName());
            obj.put("courseTitle", cert.getCourseTitle());
            obj.put("instructorName", cert.getInstructorName());
            obj.put("finalScore", cert.getFinalScore());
            obj.put("issueDate", cert.getIssueDate().getTime());
            
            arr.put(obj);
        }
        
        JSONObject root = new JSONObject();
        root.put("certificates", arr);
        
        writeFile(CERTIFICATES_FILE, root.toString(4));
    }

    public void saveCertificate(Certificate cert) {
        List<Certificate> certificates = loadCertificates();
        certificates.add(cert);
        saveCertificates(certificates);
    }

    public int generateCertificateId(List<Certificate> certificates) {
        int maxId = 0;
        for (Certificate cert : certificates) {
            if (cert.getCertificateID() > maxId) {
                maxId = cert.getCertificateID();
            }
        }
        return maxId + 1;
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
    
    public List<Course> getAllCourses() {
        return loadCourses();
    }
    
    public Course getCourseById(int courseId) {
        List<Course> courses = loadCourses();
        
        for (Course course : courses) {
            if (course.getCourseID() == courseId) {
                return course;
            }
        }
        
        return null;
    }
    
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
    
    public Admin findAdminByUsername(String username) {
        List<User> users = loadUsers();
        
        for (User user : users) {
            if (user instanceof Admin && user.getUsername().equals(username)) {
                return (Admin) user;
            }
        }
        
        return null;
    }
    
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
