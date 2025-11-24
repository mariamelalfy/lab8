package gui;

import java.util.ArrayList;
import database.JsonDatabaseManager;
import model.*;
import service.CourseService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.awt.*;
public class InstructorDashboardFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(InstructorDashboardFrame.class.getName());
    private Instructor currentInstructor;
    private JsonDatabaseManager dbManager;
    private CourseService courseService;
    private DefaultTableModel coursesTableModel;
    private DefaultTableModel lessonsTableModel;
    private DefaultTableModel quizQuestionsTableModel;
    private Quiz currentQuiz; // the Quiz object being edited
    private List<Question> currentQuizQuestions = new ArrayList<>(); // questions of that quiz
    private int currentEditingRow = -1; // index of the question being edited
    private int questionCounter = 1;
    private final int passingScore = 60;
    boolean quizRequired;
    private boolean isLoadingQuizCourses = false;
    private int lessonID;

    public InstructorDashboardFrame(Instructor instructor) {
        this.currentInstructor = instructor;
        this.dbManager = new JsonDatabaseManager();
        this.courseService = new CourseService(dbManager);
        this.quizRequired = true;

        initComponents();
        setupTables();
        loadInstructorData();
        initializeCreateCourseForm();
        setupQuizTable();
        loadCoursesForQuiz();

        lblWelcome.setText("Welcome, " + currentInstructor.getUsername());
        setLocationRelativeTo(null);
        InstructorInsightsPanel insightsPanel = new InstructorInsightsPanel(currentInstructor, dbManager);
        JScrollPane scrollPane = new JScrollPane(insightsPanel);
        tabbedPane.addTab("Course Analytics", scrollPane);
    }

   private void setupTables() {
    // Setup My Courses Table with Status column
    String[] courseColumns = {"Course ID", "Title", "Description", "Students", "Status"};
    coursesTableModel = new DefaultTableModel(courseColumns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    tblMyCourses.setModel(coursesTableModel);

    // Set column widths for courses table
    tblMyCourses.getColumnModel().getColumn(0).setPreferredWidth(80);   // Course ID
    tblMyCourses.getColumnModel().getColumn(1).setPreferredWidth(180);  // Title
    tblMyCourses.getColumnModel().getColumn(2).setPreferredWidth(250);  // Description
    tblMyCourses.getColumnModel().getColumn(3).setPreferredWidth(80);   // Students
    tblMyCourses.getColumnModel().getColumn(4).setPreferredWidth(100);  // Status

    // Setup Lessons Table
    String[] lessonColumns = {"Lesson ID", "Title", "Content Preview"};
    lessonsTableModel = new DefaultTableModel(lessonColumns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    tblLessons.setModel(lessonsTableModel);

    // Set column widths for lessons table
    tblLessons.getColumnModel().getColumn(0).setPreferredWidth(80);
    tblLessons.getColumnModel().getColumn(1).setPreferredWidth(200);
    tblLessons.getColumnModel().getColumn(2).setPreferredWidth(400);

    // Set row height for better readability
    tblLessons.setRowHeight(25);
    tblMyCourses.setRowHeight(25);
}

private void loadMyCourses() {
    coursesTableModel.setRowCount(0);

    try {
        List<Course> allCourses = dbManager.loadCourses();
        int instructorId = Integer.parseInt(currentInstructor.getUserId());

        for (Course course : allCourses) {
            if (course.getInstructorID() == instructorId) {
                // Get status with emoji/icon
                String status = getStatusDisplay(course);
                
                Object[] row = {
                    course.getCourseID(),
                    course.getCourseTitle(),
                    course.getCourseDescription(),
                    course.getEnrolledStudentIDs().size(),
                    status
                };
                coursesTableModel.addRow(row);
            }
        }
    } catch (Exception e) {
        System.err.println("Error loading courses: " + e.getMessage());
        e.printStackTrace();
    }
}

/**
 * Get a user-friendly status display with icons
 */
private String getStatusDisplay(Course course) {
    switch (course.getApprovalStatus()) {
        case PENDING:
            return "PENDING";
        case APPROVED:
            return "APPROVED";
        case REJECTED:
            return "REJECTED";
        default:
            return "UNKNOWN";
    }
}

    private void setupQuizTable() {
        // Define columns
        String[] columns = {"Q#", "Question", "Correct Answer"};

        // Create table model
        quizQuestionsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Set model to table
        tblQuizQuestions.setModel(quizQuestionsTableModel);

        // Set column widths
        tblQuizQuestions.getColumnModel().getColumn(0).setPreferredWidth(50);   // Q#
        tblQuizQuestions.getColumnModel().getColumn(1).setPreferredWidth(400);  // Question
        tblQuizQuestions.getColumnModel().getColumn(2).setPreferredWidth(100);  // Answer

        // Set row height
        tblQuizQuestions.setRowHeight(25);
    }

    private void loadInstructorData() {
        loadMyCourses();
        loadCoursesComboBox();
    }

    private void initializeCreateCourseForm() {
        // Populate instructor info
        txtInstructorID.setText(currentInstructor.getUserId());
        txtInstructorName.setText(currentInstructor.getUsername() + " (" + currentInstructor.getEmail() + ")");

        // Course ID will be generated when course is created
        txtCourseID.setText("(Auto-generated)");
    }

    private Course getSelectedCourse() {
        int selectedRow = tblMyCourses.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int courseId = (int) coursesTableModel.getValueAt(selectedRow, 0);
        List<Course> courses = dbManager.loadCourses();

        for (Course course : courses) {
            if (course.getCourseID() == courseId) {
                return course;
            }
        }
        return null;
    }

    private void loadCoursesComboBox() {
        cmbCourses.removeAllItems();

        try {
            List<Course> allCourses = dbManager.loadCourses();
            int instructorId = Integer.parseInt(currentInstructor.getUserId());

            for (Course course : allCourses) {
                if (course.getInstructorID() == instructorId) {
                    cmbCourses.addItem(course.getCourseID() + " - " + course.getCourseTitle());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading courses combo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLessonsForCourse(int courseId) {
        lessonsTableModel.setRowCount(0);

        try {
            List<Course> courses = dbManager.loadCourses();

            for (Course course : courses) {
                if (course.getCourseID() == courseId) {
                    for (Lesson lesson : course.getLessons()) {
                        String preview = lesson.getLessonContent().length() > 50
                                ? lesson.getLessonContent().substring(0, 50) + "..."
                                : lesson.getLessonContent();

                        Object[] row = {
                            lesson.getLessonID(),
                            lesson.getLessonTitle(),
                            preview
                        };
                        lessonsTableModel.addRow(row);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading lessons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCoursesForQuiz() {
        isLoadingQuizCourses = true;
        cmbQuizCourse.removeAllItems();

        try {
            List<Course> allCourses = dbManager.loadCourses();
            int instructorId = Integer.parseInt(currentInstructor.getUserId());

            boolean hasAnyCourses = false;
            for (Course c : allCourses) {
                if (c.getInstructorID() == instructorId) {
                    cmbQuizCourse.addItem(c.getCourseID() + " - " + c.getCourseTitle());
                    hasAnyCourses = true;
                }
            }

            if (!hasAnyCourses) {
                cmbQuizCourse.addItem("No courses available");
            }
        } catch (Exception e) {
            System.err.println("Error loading courses for quiz: " + e.getMessage());
        } finally {
            isLoadingQuizCourses = false;
        }
    }

    private void clearQuestionInputs() {
        txtQuestionInput.setText("");
        txtOptionA.setText("");
        txtOptionB.setText("");
        txtOptionC.setText("");
        txtOptionD.setText("");
        txtExplanationInput.setText("");

        // Clear radio button selection
        btnGroupCorrectAnswer.clearSelection();

        // Focus back to question input
        txtQuestionInput.requestFocus();
    }

    private void loadQuestionsIntoTable(java.util.List<Question> questions) {
        quizQuestionsTableModel.setRowCount(0);

        if (questions == null || questions.isEmpty()) {
            return;
        }

        // ⭐ FIX: Always display questions numbered 1, 2, 3...
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            quizQuestionsTableModel.addRow(new Object[]{
                i + 1, // Display number (always sequential)
                q.getQuestionText().length() > 60
                ? q.getQuestionText().substring(0, 60) + "..."
                : q.getQuestionText(),
                q.getCorrectAnswer()
            });
        }
    }
    private boolean canEditCourse(Course course) {
    switch (course.getApprovalStatus()) {
        case PENDING:
            // Can edit pending courses freely
            return true;
            
        case APPROVED:
            // Warn before editing approved courses
            int choice = JOptionPane.showConfirmDialog(this,
                "This course is APPROVED and visible to students.\n\n"
                + "Editing it may affect enrolled students.\n"
                + "Current enrollments: " + course.getEnrolledStudentIDs().size() + " students\n\n"
                + "Do you want to continue editing?",
                "Edit Approved Course",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            return choice == JOptionPane.YES_OPTION;
            
        case REJECTED:
            // Allow editing rejected courses to resubmit
            int resubmit = JOptionPane.showConfirmDialog(this,
                "This course was REJECTED by an admin.\n\n"
                + "Rejection reason:\n" + course.getRejectionReason() + "\n\n"
                + "Do you want to edit and resubmit it for approval?",
                "Edit Rejected Course",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            return resubmit == JOptionPane.YES_OPTION;
            
        default:
            return false;
    }
}

private EditCourseDialog showEditCourseDialog(Course course) {
    JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Title field
    JLabel titleLabel = new JLabel("Course Title:");
    JTextField titleField = new JTextField(course.getCourseTitle(), 30);
    titleField.setToolTipText("5-100 characters");
    
    // Description area
    JLabel descLabel = new JLabel("Course Description:");
    JTextArea descArea = new JTextArea(course.getCourseDescription(), 5, 30);
    descArea.setLineWrap(true);
    descArea.setWrapStyleWord(true);
    descArea.setToolTipText("20-500 characters");
    JScrollPane descScroll = new JScrollPane(descArea);
    
    // Character counters
    JLabel titleCounter = new JLabel(course.getCourseTitle().length() + "/100 characters");
    JLabel descCounter = new JLabel(course.getCourseDescription().length() + "/500 characters");
    
    // Update counters on text change
    titleField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        private void update() {
            int len = titleField.getText().length();
            titleCounter.setText(len + "/100 characters");
            titleCounter.setForeground(len > 100 ? Color.RED : Color.GRAY);
        }
    });
    
    descArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        private void update() {
            int len = descArea.getText().length();
            descCounter.setText(len + "/500 characters");
            descCounter.setForeground(len > 500 ? Color.RED : Color.GRAY);
        }
    });
    
    // Add components
    panel.add(titleLabel);
    panel.add(titleField);
    panel.add(titleCounter);
    panel.add(new JLabel(" ")); // Spacer
    panel.add(descLabel);
    panel.add(descScroll);
    panel.add(descCounter);
    
    // Show dialog
    int result = JOptionPane.showConfirmDialog(
        this,
        panel,
        "Edit Course - " + course.getCourseTitle(),
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );
    
    return new EditCourseDialog(
        result == JOptionPane.OK_OPTION,
        titleField.getText().trim(),
        descArea.getText().trim()
    );
}

private boolean validateCourseEdits(Course original, String newTitle, String newDescription) {
    // Check if anything changed
    if (newTitle.equals(original.getCourseTitle()) && 
        newDescription.equals(original.getCourseDescription())) {
        JOptionPane.showMessageDialog(this,
            "No changes were made to the course.",
            "No Changes",
            JOptionPane.INFORMATION_MESSAGE);
        return false;
    }
    
    // Validate title
    if (newTitle.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Course title cannot be empty!",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    if (newTitle.length() < 5) {
        JOptionPane.showMessageDialog(this,
            "Course title must be at least 5 characters long!\n"
            + "Current length: " + newTitle.length() + " characters",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    if (newTitle.length() > 100) {
        JOptionPane.showMessageDialog(this,
            "Course title cannot exceed 100 characters!\n"
            + "Current length: " + newTitle.length() + " characters",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    // Check for valid characters in title
    if (!newTitle.matches("[a-zA-Z0-9\\s.,!?'\"()-]+")) {
        JOptionPane.showMessageDialog(this,
            "Course title contains invalid characters!\n"
            + "Only letters, numbers, spaces, and basic punctuation are allowed.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    // Validate description
    if (newDescription.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Course description cannot be empty!",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    if (newDescription.length() < 20) {
        JOptionPane.showMessageDialog(this,
            "Course description must be at least 20 characters long!\n"
            + "Current length: " + newDescription.length() + " characters\n"
            + "Please provide a detailed description.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    if (newDescription.length() > 500) {
        JOptionPane.showMessageDialog(this,
            "Course description cannot exceed 500 characters!\n"
            + "Current length: " + newDescription.length() + " characters",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    // Check for duplicate titles (only if title changed)
    if (!newTitle.equals(original.getCourseTitle())) {
        List<Course> allCourses = dbManager.loadCourses();
        int instructorId = Integer.parseInt(currentInstructor.getUserId());
        
        for (Course course : allCourses) {
            if (course.getCourseID() != original.getCourseID() &&
                course.getInstructorID() == instructorId && 
                course.getCourseTitle().equalsIgnoreCase(newTitle)) {
                
                int choice = JOptionPane.showConfirmDialog(this,
                    "You already have another course with this title:\n\n"
                    + "Course ID: " + course.getCourseID() + "\n"
                    + "Status: " + course.getApprovalStatus() + "\n\n"
                    + "Are you sure you want to use the same title?",
                    "Duplicate Title Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
        }
    }
    
    // Check significance of changes for approved courses
    if (original.isApproved() && original.getEnrolledStudentIDs().size() > 0) {
        boolean significantChange = 
            !newTitle.equalsIgnoreCase(original.getCourseTitle()) ||
            Math.abs(newDescription.length() - original.getCourseDescription().length()) > 50;
        
        if (significantChange) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Warning: Significant changes detected!\n\n"
                + "This course has " + original.getEnrolledStudentIDs().size() + " enrolled students.\n"
                + "Large changes may confuse students who are already taking the course.\n\n"
                + "Do you want to continue?",
                "Significant Changes Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }
        }
    }
    
    return true;
}

private boolean applyCourseEdits(Course course, String newTitle, String newDescription) {
    try {
        // Store old values for logging
        String oldTitle = course.getCourseTitle();
        String oldDescription = course.getCourseDescription();
        
        // Apply changes
        course.setCourseTitle(newTitle);
        course.setCourseDescription(newDescription);
        
        // If course was rejected and now edited, reset to PENDING for re-review
        if (course.isRejected()) {
            course.setApprovalStatus(Course.ApprovalStatus.PENDING);
            course.setRejectionReason(null);
            course.setReviewedBy(null);
            course.setApprovalDate(null);
            
            JOptionPane.showMessageDialog(this,
                "Course status changed from REJECTED to PENDING.\n"
                + "Your edits will be reviewed by an admin.",
                "Status Changed",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Save to database
        List<Course> courses = dbManager.loadCourses();
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseID() == course.getCourseID()) {
                courses.set(i, course);
                break;
            }
        }
        dbManager.saveCourses(courses);
        
        // Log the change
        System.out.println("Course updated - ID: " + course.getCourseID());
        System.out.println("  Title: " + oldTitle + " -> " + newTitle);
        System.out.println("  Description changed: " + !oldDescription.equals(newDescription));
        System.out.println("  Status: " + course.getApprovalStatus());
        
        return true;
        
    } catch (Exception e) {
        System.err.println("Error updating course: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

/**
 * Helper class to hold edit dialog results
 */
private class EditCourseDialog {
    private final boolean confirmed;
    private final String newTitle;
    private final String newDescription;
    
    public EditCourseDialog(boolean confirmed, String newTitle, String newDescription) {
        this.confirmed = confirmed;
        this.newTitle = newTitle;
        this.newDescription = newDescription;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getNewTitle() {
        return newTitle;
    }
    
    public String getNewDescription() {
        return newDescription;
    }
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroupCorrectAnswer = new javax.swing.ButtonGroup();
        topPanel = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        myCoursesPanel = new javax.swing.JPanel();
        tblMyCourses1 = new javax.swing.JScrollPane();
        tblMyCourses = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        btnEditCourse = new javax.swing.JButton();
        btnViewStudents = new javax.swing.JButton();
        btnDeleteButton = new javax.swing.JButton();
        manageLessonsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbCourses = new javax.swing.JComboBox<>();
        tblL = new javax.swing.JScrollPane();
        tblLessons = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        btnAddLesson = new javax.swing.JButton();
        btnDeleteLesson = new javax.swing.JButton();
        btnEditLesson = new javax.swing.JButton();
        btnRefreshLessons = new javax.swing.JButton();
        createCoursePanel = new javax.swing.JPanel();
        lblCourseTitle = new javax.swing.JLabel();
        txtCourseTitle = new javax.swing.JTextField();
        lblCourseDescription = new javax.swing.JLabel();
        txtCourseDescription = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtInstructorName = new javax.swing.JTextField();
        txtInstructorID = new javax.swing.JTextField();
        btnCreateCourse = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        lblCourseDescription1 = new javax.swing.JLabel();
        txtCourseID = new javax.swing.JTextField();
        createQuizPanel = new javax.swing.JPanel();
        lblSelectLesson = new javax.swing.JLabel();
        lblSelectCourse = new javax.swing.JLabel();
        cmbQuizCourse = new javax.swing.JComboBox<>();
        cmbQuizLesson = new javax.swing.JComboBox<>();
        scrollQuizPanel = new javax.swing.JScrollPane();
        tblQuizQuestions = new javax.swing.JTable();
        questionPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtQuestionInput = new javax.swing.JTextArea();
        radioOptionA = new javax.swing.JRadioButton();
        radioOptionC = new javax.swing.JRadioButton();
        radioOptionD = new javax.swing.JRadioButton();
        radioOptionB = new javax.swing.JRadioButton();
        txtOptionD = new javax.swing.JTextField();
        txtOptionA = new javax.swing.JTextField();
        txtOptionB = new javax.swing.JTextField();
        txtOptionC = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtExplanationInput = new javax.swing.JTextField();
        btnClearQuestionInForm = new javax.swing.JButton();
        btnAddQuestion = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnEditQuestion = new javax.swing.JButton();
        btnSaveQuiz = new javax.swing.JButton();
        btnDeleteQuestion = new javax.swing.JButton();
        btnClearAllQuestions = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Instructor Dashboard - SkillForge");

        lblWelcome.setFont(new java.awt.Font("Helvetica Neue", 3, 16)); // NOI18N
        lblWelcome.setText("Welcome, Instructor");

        btnLogout.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(lblWelcome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout))
        );

        tabbedPane.setFont(new java.awt.Font("Helvetica Neue", 0, 15)); // NOI18N

        tblMyCourses.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        tblMyCourses.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Course ID", "Title", "Description", "Students", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblMyCourses1.setViewportView(tblMyCourses);
        if (tblMyCourses.getColumnModel().getColumnCount() > 0) {
            tblMyCourses.getColumnModel().getColumn(3).setHeaderValue("Students");
        }

        btnEditCourse.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnEditCourse.setText("Edit Course");
        btnEditCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditCourseActionPerformed(evt);
            }
        });

        btnViewStudents.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnViewStudents.setText("View Students");
        btnViewStudents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewStudentsActionPerformed(evt);
            }
        });

        btnDeleteButton.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnDeleteButton.setText("Delete Course");
        btnDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(btnEditCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(88, 88, 88)
                .addComponent(btnDeleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
                .addComponent(btnViewStudents, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnViewStudents, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout myCoursesPanelLayout = new javax.swing.GroupLayout(myCoursesPanel);
        myCoursesPanel.setLayout(myCoursesPanelLayout);
        myCoursesPanelLayout.setHorizontalGroup(
            myCoursesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(myCoursesPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(98, Short.MAX_VALUE))
            .addGroup(myCoursesPanelLayout.createSequentialGroup()
                .addComponent(tblMyCourses1)
                .addContainerGap())
        );
        myCoursesPanelLayout.setVerticalGroup(
            myCoursesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(myCoursesPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(tblMyCourses1, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("My Courses", myCoursesPanel);

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel1.setText("Select Course:");

        cmbCourses.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbCourses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCoursesActionPerformed(evt);
            }
        });

        tblLessons.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        tblLessons.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Lesson ID", "Title", "Content Preview"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblL.setViewportView(tblLessons);

        btnAddLesson.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnAddLesson.setText("Add Lesson");
        btnAddLesson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddLessonActionPerformed(evt);
            }
        });

        btnDeleteLesson.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnDeleteLesson.setText("Delete Lesson");
        btnDeleteLesson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteLessonActionPerformed(evt);
            }
        });

        btnEditLesson.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        btnEditLesson.setText("Edit Lesson");
        btnEditLesson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditLessonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(btnAddLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(88, 88, 88)
                .addComponent(btnEditLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDeleteLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDeleteLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(49, Short.MAX_VALUE))
        );

        btnRefreshLessons.setText("Refresh Lessons");
        btnRefreshLessons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshLessonsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout manageLessonsPanelLayout = new javax.swing.GroupLayout(manageLessonsPanel);
        manageLessonsPanel.setLayout(manageLessonsPanelLayout);
        manageLessonsPanelLayout.setHorizontalGroup(
            manageLessonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageLessonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manageLessonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(manageLessonsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(108, 108, 108)
                        .addComponent(btnRefreshLessons, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(manageLessonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tblL, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)))
                .addContainerGap(106, Short.MAX_VALUE))
        );
        manageLessonsPanelLayout.setVerticalGroup(
            manageLessonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageLessonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manageLessonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(manageLessonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnRefreshLessons, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tblL, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("Manage Lessons", manageLessonsPanel);

        lblCourseTitle.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        lblCourseTitle.setText("Course Title:");

        lblCourseDescription.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        lblCourseDescription.setText("Course Description:");

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel3.setText("Instructor Name:");

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel4.setText("Instructor ID:");

        txtInstructorName.setEditable(false);

        txtInstructorID.setEditable(false);

        btnCreateCourse.setFont(new java.awt.Font("Helvetica Neue", 0, 15)); // NOI18N
        btnCreateCourse.setText("Create Course");
        btnCreateCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateCourseActionPerformed(evt);
            }
        });

        btnClear.setFont(new java.awt.Font("Helvetica Neue", 0, 15)); // NOI18N
        btnClear.setText("Clear Form");

        lblCourseDescription1.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        lblCourseDescription1.setText("Course ID:");

        txtCourseID.setEditable(false);

        javax.swing.GroupLayout createCoursePanelLayout = new javax.swing.GroupLayout(createCoursePanel);
        createCoursePanel.setLayout(createCoursePanelLayout);
        createCoursePanelLayout.setHorizontalGroup(
            createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(createCoursePanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(createCoursePanelLayout.createSequentialGroup()
                        .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCourseTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCourseDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCourseDescription1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 180, Short.MAX_VALUE)
                        .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCourseTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, createCoursePanelLayout.createSequentialGroup()
                                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtCourseID, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtInstructorName, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtInstructorID, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCourseDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(63, 63, 63))))
                    .addGroup(createCoursePanelLayout.createSequentialGroup()
                        .addComponent(btnCreateCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        createCoursePanelLayout.setVerticalGroup(
            createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(createCoursePanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCourseTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCourseTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCourseDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCourseDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCourseID, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCourseDescription1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInstructorName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtInstructorID, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(createCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreateCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(162, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Create Course", createCoursePanel);

        lblSelectLesson.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblSelectLesson.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSelectLesson.setText("Select Lesson:");

        lblSelectCourse.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblSelectCourse.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSelectCourse.setText("Select Course:");

        cmbQuizCourse.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbQuizCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbQuizCourseActionPerformed(evt);
            }
        });

        cmbQuizLesson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbQuizLessonActionPerformed(evt);
            }
        });

        tblQuizQuestions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Q#", "question", "answer"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollQuizPanel.setViewportView(tblQuizQuestions);

        jLabel2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel2.setText("Question Text");

        txtQuestionInput.setColumns(20);
        txtQuestionInput.setRows(5);
        jScrollPane1.setViewportView(txtQuestionInput);

        btnGroupCorrectAnswer.add(radioOptionA);
        radioOptionA.setText("A:");
        radioOptionA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioOptionAActionPerformed(evt);
            }
        });

        btnGroupCorrectAnswer.add(radioOptionC);
        radioOptionC.setText("C:");
        radioOptionC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioOptionCActionPerformed(evt);
            }
        });

        btnGroupCorrectAnswer.add(radioOptionD);
        radioOptionD.setText("D:");

        btnGroupCorrectAnswer.add(radioOptionB);
        radioOptionB.setText("B:");

        txtOptionA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOptionAActionPerformed(evt);
            }
        });

        jLabel5.setText("Explanation(optional):");

        btnClearQuestionInForm.setText("Clear Question");
        btnClearQuestionInForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearQuestionInFormActionPerformed(evt);
            }
        });

        btnAddQuestion.setBackground(new java.awt.Color(0, 153, 0));
        btnAddQuestion.setText("Add Question");
        btnAddQuestion.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddQuestion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddQuestionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout questionPanelLayout = new javax.swing.GroupLayout(questionPanel);
        questionPanel.setLayout(questionPanelLayout);
        questionPanelLayout.setHorizontalGroup(
            questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(questionPanelLayout.createSequentialGroup()
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(questionPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(questionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(questionPanelLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(radioOptionA)
                                    .addComponent(radioOptionC)
                                    .addComponent(radioOptionD)
                                    .addComponent(radioOptionB))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtOptionC, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtOptionA, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtOptionB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtOptionD, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(questionPanelLayout.createSequentialGroup()
                                .addGap(210, 210, 210)
                                .addComponent(btnClearQuestionInForm, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnAddQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(questionPanelLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtExplanationInput, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        questionPanelLayout.setVerticalGroup(
            questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(questionPanelLayout.createSequentialGroup()
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(questionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioOptionA)
                    .addComponent(txtOptionA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioOptionB)
                    .addComponent(txtOptionB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioOptionC)
                    .addComponent(txtOptionC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOptionD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radioOptionD))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtExplanationInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(questionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddQuestion)
                    .addComponent(btnClearQuestionInForm))
                .addContainerGap())
        );

        btnEditQuestion.setText("Edit Selected Question");
        btnEditQuestion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditQuestionActionPerformed(evt);
            }
        });

        btnSaveQuiz.setBackground(new java.awt.Color(0, 153, 0));
        btnSaveQuiz.setText("Save Quiz");
        btnSaveQuiz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveQuizActionPerformed(evt);
            }
        });

        btnDeleteQuestion.setText("Delete Selected Question");
        btnDeleteQuestion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteQuestionActionPerformed(evt);
            }
        });

        btnClearAllQuestions.setBackground(new java.awt.Color(153, 0, 0));
        btnClearAllQuestions.setText("Delete Quiz");
        btnClearAllQuestions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAllQuestionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addComponent(btnDeleteQuestion)
                .addGap(18, 18, 18)
                .addComponent(btnEditQuestion)
                .addGap(18, 18, 18)
                .addComponent(btnClearAllQuestions)
                .addGap(18, 18, 18)
                .addComponent(btnSaveQuiz)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 5, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditQuestion)
                    .addComponent(btnDeleteQuestion)
                    .addComponent(btnSaveQuiz)
                    .addComponent(btnClearAllQuestions))
                .addGap(29, 29, 29))
        );

        javax.swing.GroupLayout createQuizPanelLayout = new javax.swing.GroupLayout(createQuizPanel);
        createQuizPanel.setLayout(createQuizPanelLayout);
        createQuizPanelLayout.setHorizontalGroup(
            createQuizPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(createQuizPanelLayout.createSequentialGroup()
                .addGroup(createQuizPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, createQuizPanelLayout.createSequentialGroup()
                        .addGap(0, 8, Short.MAX_VALUE)
                        .addComponent(lblSelectCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbQuizCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblSelectLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbQuizLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(questionPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(createQuizPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(createQuizPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrollQuizPanel)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        createQuizPanelLayout.setVerticalGroup(
            createQuizPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(createQuizPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(createQuizPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(createQuizPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmbQuizCourse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSelectCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(createQuizPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSelectLesson, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbQuizLesson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(questionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollQuizPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(124, 124, 124))
        );

        tabbedPane.addTab("Create Quiz", createQuizPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tabbedPane)
                    .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 568, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnEditCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCourseActionPerformed
        Course selected = getSelectedCourse();
    if (selected == null) {
        JOptionPane.showMessageDialog(this,
            "Please select a course to edit!",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Check if course can be edited based on approval status
    if (!canEditCourse(selected)) {
        return;
    }

    // Show edit dialog with current values
    EditCourseDialog editDialog = showEditCourseDialog(selected);
    
    if (editDialog.isConfirmed()) {
        String newTitle = editDialog.getNewTitle();
        String newDescription = editDialog.getNewDescription();
        
        // Validate changes
        if (!validateCourseEdits(selected, newTitle, newDescription)) {
            return;
        }
        
        // Apply changes
        boolean success = applyCourseEdits(selected, newTitle, newDescription);
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Course updated successfully!\n\n"
                + "Title: " + newTitle + "\n"
                + "Status: " + selected.getApprovalStatus() + "\n\n"
                + (selected.isPending() ? 
                    "Changes saved. Course is still pending admin approval." :
                    selected.isApproved() ? 
                        "Changes saved. Course remains visible to students." :
                        "Changes saved. Course is still rejected."),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            loadMyCourses();
            loadCoursesComboBox();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update course!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    }//GEN-LAST:event_btnEditCourseActionPerformed

    private void btnDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteButtonActionPerformed
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to delete!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this course?\nThis action cannot be undone!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            List<Course> courses = dbManager.loadCourses();
            courses.removeIf(c -> c.getCourseID() == selected.getCourseID());
            dbManager.saveCourses(courses);

            currentInstructor.removeCourse(String.valueOf(selected.getCourseID()));
            List<User> users = dbManager.loadUsers();
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserId().equals(currentInstructor.getUserId())) {
                    users.set(i, currentInstructor);
                    break;
                }
            }
            dbManager.saveUsers(users);

            JOptionPane.showMessageDialog(this,
                    "Course deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            loadMyCourses();
            loadCoursesComboBox();
        }

    }//GEN-LAST:event_btnDeleteButtonActionPerformed

    private void btnViewStudentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewStudentsActionPerformed
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> users = dbManager.loadUsers();
        StringBuilder studentList = new StringBuilder();
        studentList.append("Enrolled Students for: ").append(selected.getCourseTitle()).append("\n\n");

        if (selected.getEnrolledStudentIDs().isEmpty()) {
            studentList.append("No students enrolled yet.");
        } else {
            for (Integer studentId : selected.getEnrolledStudentIDs()) {
                for (User user : users) {
                    if (user instanceof Student
                            && user.getUserId().equals(String.valueOf(studentId))) {
                        studentList.append("- ").append(user.getUsername())
                                .append(" (").append(user.getEmail()).append(")\n");
                        break;
                    }
                }
            }
        }

        JOptionPane.showMessageDialog(this,
                studentList.toString(),
                "Enrolled Students",
                JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnViewStudentsActionPerformed

    private void btnCreateCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateCourseActionPerformed
        // Validate inputs
    if (!validateCreateCourseInputs()) {
        return;
    }

    try {
        // Get input values
        String title = txtCourseTitle.getText().trim();
        String description = txtCourseDescription.getText().trim();

        // Use the Instructor's createCourse method (which sets PENDING status)
        Course newCourse = currentInstructor.createCourse(title, description);
        
        if (newCourse == null) {
            JOptionPane.showMessageDialog(this,
                "Failed to create course!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Display the generated ID
        txtCourseID.setText(String.valueOf(newCourse.getCourseID()));

        // Show success message with PENDING status info
        JOptionPane.showMessageDialog(this,
            "Course submitted successfully!\n\n"
            + "Course ID: " + newCourse.getCourseID() + "\n"
            + "Title: " + title + "\n"
            + "Description: " + (description.length() > 50 ? description.substring(0, 50) + "..." : description) + "\n"
            + "Status: PENDING\n\n"
            + "Your course will be visible to students once an admin approves it.\n"
            + "You will be notified of the approval status.",
            "Course Submitted for Approval",
            JOptionPane.INFORMATION_MESSAGE);


        // Refresh tables (will show course even though it's pending)
        loadMyCourses();
        loadCoursesComboBox();

        // Switch to "My Courses" tab
        tabbedPane.setSelectedIndex(0);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Error creating course: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
    }
    private boolean validateCreateCourseInputs() {
    // Check course title
    String title = txtCourseTitle.getText().trim();
    if (title.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Course title is required!",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        txtCourseTitle.requestFocus();
        return false;
    }

    if (title.length() < 5) {
        JOptionPane.showMessageDialog(this,
            "Course title must be at least 5 characters long!",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        txtCourseTitle.requestFocus();
        return false;
    }
    
    if (title.length() > 100) {
        JOptionPane.showMessageDialog(this,
            "Course title cannot exceed 100 characters!",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        txtCourseTitle.requestFocus();
        return false;
    }

    // Check for valid characters (letters, numbers, spaces, common punctuation)
    if (!title.matches("[a-zA-Z0-9\\s.,!?'\"()-]+")) {
        JOptionPane.showMessageDialog(this,
            "Course title contains invalid characters!\n"
            + "Only letters, numbers, spaces, and basic punctuation are allowed.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        txtCourseTitle.requestFocus();
        return false;
    }

    // Check course description
    String description = txtCourseDescription.getText().trim();
    if (description.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Course description is required!",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        txtCourseDescription.requestFocus();
        return false;
    }

    if (description.length() < 20) {
        JOptionPane.showMessageDialog(this,
            "Course description must be at least 20 characters long!\n"
            + "Please provide a detailed description of what students will learn.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        txtCourseDescription.requestFocus();
        return false;
    }
    
    if (description.length() > 500) {
        JOptionPane.showMessageDialog(this,
            "Course description cannot exceed 500 characters!\n"
            + "Current length: " + description.length() + " characters.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        txtCourseDescription.requestFocus();
        return false;
    }

    // Check for duplicate course titles (same instructor)
    List<Course> existingCourses = dbManager.loadCourses();
    int instructorId = Integer.parseInt(currentInstructor.getUserId());
    
    for (Course course : existingCourses) {
        if (course.getInstructorID() == instructorId && 
            course.getCourseTitle().equalsIgnoreCase(title)) {
            
            int choice = JOptionPane.showConfirmDialog(this,
                "You already have a course with this title:\n\n"
                + "\"" + course.getCourseTitle() + "\"\n"
                + "Status: " + course.getApprovalStatus() + "\n\n"
                + "Do you want to create another course with the same title?",
                "Duplicate Title Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (choice != JOptionPane.YES_OPTION) {
                txtCourseTitle.requestFocus();
                return false;
            }
        }
    }

    // Check instructor ID
    if (txtInstructorID.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Instructor information is missing!",
            "Error",
            JOptionPane.ERROR_MESSAGE);
        return false;
    }

    return true;
    }//GEN-LAST:event_btnCreateCourseActionPerformed

    private void btnAddLessonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddLessonActionPerformed
        if (cmbCourses.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course first!",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = (String) cmbCourses.getSelectedItem();
        int courseId = Integer.parseInt(selected.split(" - ")[0]);

        String lessonTitle = JOptionPane.showInputDialog(this, "Enter lesson title:");
        if (lessonTitle == null || lessonTitle.trim().isEmpty()) {
            return;
        }

        String lessonContent = JOptionPane.showInputDialog(this, "Enter lesson content:");
        if (lessonContent == null || lessonContent.trim().isEmpty()) {
            return;
        }

        // Use CourseService
        boolean success = courseService.addLesson(courseId, lessonTitle.trim(), lessonContent.trim());

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Lesson added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadLessonsForCourse(courseId);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to add lesson!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_btnAddLessonActionPerformed

    private void btnDeleteLessonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteLessonActionPerformed
        int selectedRow = tblLessons.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a lesson to delete!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this lesson?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String selected = (String) cmbCourses.getSelectedItem();
            int courseId = Integer.parseInt(selected.split(" - ")[0]);
            int lessonId = (int) lessonsTableModel.getValueAt(selectedRow, 0);

            // Use CourseService
            boolean success = courseService.deleteLesson(courseId, lessonId);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Lesson deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadLessonsForCourse(courseId);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete lesson!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteLessonActionPerformed

    private void btnEditLessonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditLessonActionPerformed
        int selectedRow = tblLessons.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a lesson to edit!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = (String) cmbCourses.getSelectedItem();
        int courseId = Integer.parseInt(selected.split(" - ")[0]);
        int lessonId = (int) lessonsTableModel.getValueAt(selectedRow, 0);
        String currentTitle = (String) lessonsTableModel.getValueAt(selectedRow, 1);

        // Get current lesson details
        Course course = courseService.getCourseById(courseId);
        Lesson currentLesson = null;
        for (Lesson l : course.getLessons()) {
            if (l.getLessonID() == lessonId) {
                currentLesson = l;
                break;
            }
        }

        if (currentLesson == null) {
            return;
        }

        String newTitle = JOptionPane.showInputDialog(this,
                "Enter new lesson title:", currentLesson.getLessonTitle());
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return;
        }

        String newContent = JOptionPane.showInputDialog(this,
                "Enter new lesson content:", currentLesson.getLessonContent());
        if (newContent == null || newContent.trim().isEmpty()) {
            return;
        }

        // Use CourseService
        boolean success = courseService.editLesson(courseId, lessonId, newTitle.trim(), newContent.trim());

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Lesson updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadLessonsForCourse(courseId);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update lesson!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_btnEditLessonActionPerformed

    private void cmbCoursesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCoursesActionPerformed
        String selected = (String) cmbCourses.getSelectedItem();
        if (selected != null && selected.contains(" - ")) {
            try {
                int courseId = Integer.parseInt(selected.split(" - ")[0]);
                loadLessonsForCourse(courseId);
            } catch (Exception e) {
                System.err.println("Error in combo box action: " + e.getMessage());
            }
        }

    }//GEN-LAST:event_cmbCoursesActionPerformed

    private void btnRefreshLessonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshLessonsActionPerformed
        loadCoursesComboBox();

        String selected = (String) cmbCourses.getSelectedItem();
        if (selected != null && !selected.equals("No courses available")) {
            int courseId = Integer.parseInt(selected.split(" - ")[0]);
            loadLessonsForCourse(courseId);
        }

        JOptionPane.showMessageDialog(this,
                "Lessons refreshed successfully!",
                "Refreshed",
                JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnRefreshLessonsActionPerformed

    private void cmbQuizCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbQuizCourseActionPerformed
        if (isLoadingQuizCourses) {
            return;  // Don't process during loading
        }

        // Clear lesson dropdown
        cmbQuizLesson.removeAllItems();

        // ⭐ CLEAR ALL QUIZ STATE COMPLETELY
        currentQuiz = null;
        currentQuizQuestions = new ArrayList<>();
        quizQuestionsTableModel.setRowCount(0);
        questionCounter = 1;
        currentEditingRow = -1;
        clearQuestionInputs();

        if (cmbQuizCourse.getSelectedItem() == null
                || cmbQuizCourse.getSelectedItem().equals("No courses available")) {
            return;
        }

        try {
            String selected = (String) cmbQuizCourse.getSelectedItem();
            int courseId = Integer.parseInt(selected.split(" - ")[0]);

            Course course = courseService.getCourseById(courseId);
            if (course != null && !course.getLessons().isEmpty()) {
                for (Lesson lesson : course.getLessons()) {
                    // ⭐ FIX: Check if quiz exists using BOTH courseId and lessonId
                    boolean hasQuiz = dbManager.hasQuiz(courseId, lesson.getLessonID());
                    String hasQuizIndicator = hasQuiz ? " [Has Quiz]" : "";

                    cmbQuizLesson.addItem(lesson.getLessonID() + " - "
                            + lesson.getLessonTitle() + hasQuizIndicator);
                }
            } else {
                cmbQuizLesson.addItem("No lessons available");
            }
        } catch (Exception e) {
            System.err.println("Error loading lessons: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_cmbQuizCourseActionPerformed

    private void cmbQuizLessonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbQuizLessonActionPerformed
        if (cmbQuizLesson.getSelectedItem() == null
                || cmbQuizLesson.getSelectedItem().equals("No lessons available")) {
            return;
        }

        if (cmbQuizCourse.getSelectedItem() == null
                || cmbQuizCourse.getSelectedItem().equals("No courses available")) {
            return;
        }

        try {
            // ⭐ FIX: Get BOTH course and lesson IDs
            String courseSelected = (String) cmbQuizCourse.getSelectedItem();
            int courseId = Integer.parseInt(courseSelected.split(" - ")[0]);

            String lessonSelected = (String) cmbQuizLesson.getSelectedItem();
            int lessonId = Integer.parseInt(lessonSelected.split(" - ")[0]);

            // ⭐ FIX: Load quiz using BOTH courseId and lessonId
            Quiz quiz = dbManager.getQuizByCourseAndLessonId(courseId, lessonId);

            if (quiz == null) {
                // No quiz exists - clear form for new quiz creation
                currentQuiz = null;
                currentQuizQuestions = new ArrayList<>();
                quizQuestionsTableModel.setRowCount(0);
                questionCounter = 1;
                currentEditingRow = -1;
                clearQuestionInputs();
            } else {
                // Quiz exists - load it for editing
                currentQuiz = quiz;
                currentQuizQuestions = new ArrayList<>(quiz.getQuestions());

                // Renumber questions starting from 1
                for (int i = 0; i < currentQuizQuestions.size(); i++) {
                    currentQuizQuestions.get(i).setQuestionID(i + 1);
                }

                questionCounter = currentQuizQuestions.size() + 1;
                loadQuestionsIntoTable(currentQuizQuestions);
                clearQuestionInputs();
            }

        } catch (Exception e) {
            System.err.println("Error loading quiz for lesson: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_cmbQuizLessonActionPerformed

    private void radioOptionCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioOptionCActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioOptionCActionPerformed

    private void radioOptionAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioOptionAActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioOptionAActionPerformed

    private void btnClearQuestionInFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearQuestionInFormActionPerformed
        clearQuestionInputs();
    }//GEN-LAST:event_btnClearQuestionInFormActionPerformed

    private void btnAddQuestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddQuestionActionPerformed
        String questionText = txtQuestionInput.getText().trim();
        String optionA = txtOptionA.getText().trim();
        String optionB = txtOptionB.getText().trim();
        String optionC = txtOptionC.getText().trim();
        String optionD = txtOptionD.getText().trim();
        String explanation = txtExplanationInput.getText().trim();
        String correctAnswer = "";

        if (radioOptionA.isSelected()) {
            correctAnswer = optionA;
        } else if (radioOptionB.isSelected()) {
            correctAnswer = optionB;
        } else if (radioOptionC.isSelected()) {
            correctAnswer = optionC;
        } else if (radioOptionD.isSelected()) {
            correctAnswer = optionD;
        }

        // Validation
        if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty()
                || optionC.isEmpty() || optionD.isEmpty() || correctAnswer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled and correct answer selected!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentEditingRow == -1) {
            // Add new question
            Question q = new Question(
                    questionCounter++,
                    questionText,
                    optionA, optionB, optionC, optionD,
                    correctAnswer,
                    explanation.isEmpty() ? null : explanation
            );

            currentQuizQuestions.add(q);
            quizQuestionsTableModel.addRow(new Object[]{
                q.getQuestionID(),
                q.getQuestionText().length() > 60 ? q.getQuestionText().substring(0, 60) + "..." : q.getQuestionText(),
                q.getCorrectAnswer()
            });
        } else {
            // Edit existing question
            Question q = currentQuizQuestions.get(currentEditingRow);
            q.setQuestionText(questionText);
            q.setOptionA(optionA);
            q.setOptionB(optionB);
            q.setOptionC(optionC);
            q.setOptionD(optionD);
            q.setCorrectAnswer(correctAnswer);
            q.setExplanation(explanation.isEmpty() ? null : explanation);

            // Update table row
            quizQuestionsTableModel.setValueAt(currentEditingRow + 1, currentEditingRow, 0);
            quizQuestionsTableModel.setValueAt(questionText.length() > 60 ? questionText.substring(0, 60) + "..." : questionText, currentEditingRow, 1);
            quizQuestionsTableModel.setValueAt(correctAnswer, currentEditingRow, 2);

            currentEditingRow = -1;
        }

        // DON'T save to file here - just keep in memory
        // Will be saved when user clicks "Save Quiz"
        clearQuestionInputs();
    }//GEN-LAST:event_btnAddQuestionActionPerformed

    private void btnDeleteQuestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteQuestionActionPerformed
        int selectedRow = tblQuizQuestions.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a question to delete!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this question?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Remove from list and table
        currentQuizQuestions.remove(selectedRow);
        quizQuestionsTableModel.removeRow(selectedRow);

        // Renumber questions
        for (int i = 0; i < currentQuizQuestions.size(); i++) {
            currentQuizQuestions.get(i).setQuestionID(i + 1);
            quizQuestionsTableModel.setValueAt(i + 1, i, 0);
        }

        // Reset counter
        questionCounter = currentQuizQuestions.size() + 1;

        // DON'T save to file here - just keep in memory
        // Will be saved when user clicks "Save Quiz"
        JOptionPane.showMessageDialog(this,
                "Question removed from quiz! Click 'Save Quiz' to persist changes.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnDeleteQuestionActionPerformed

    private void btnEditQuestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditQuestionActionPerformed
        int selectedRow = tblQuizQuestions.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a question to edit!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentEditingRow = selectedRow;

        Question q = currentQuizQuestions.get(selectedRow);
        txtQuestionInput.setText(q.getQuestionText());
        txtOptionA.setText(q.getOptionA());
        txtOptionB.setText(q.getOptionB());
        txtOptionC.setText(q.getOptionC());
        txtOptionD.setText(q.getOptionD());
        txtExplanationInput.setText(q.getExplanation() != null ? q.getExplanation() : "");

        btnGroupCorrectAnswer.clearSelection();
        switch (q.getCorrectAnswer()) {
            case "A":
                radioOptionA.setSelected(true);
                break;
            case "B":
                radioOptionB.setSelected(true);
                break;
            case "C":
                radioOptionC.setSelected(true);
                break;
            case "D":
                radioOptionD.setSelected(true);
                break;
        }

        // When you click "Add Question" button, it will update this question
        // Changes saved to file when "Save Quiz" is clicked
    }//GEN-LAST:event_btnEditQuestionActionPerformed

    private void btnClearAllQuestionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAllQuestionsActionPerformed
        if (currentQuizQuestions.isEmpty() && currentQuiz == null) {
            JOptionPane.showMessageDialog(this, "No questions to clear!");
            return;
        }

        // Show different messages based on whether quiz is saved or not
        String message;
        if (currentQuiz != null) {
            message = "This will permanently delete the quiz from the database!\n"
                    + "All questions will be removed and this action cannot be undone.\n\n"
                    + "Do you want to continue?";
        } else {
            message = "Clear all questions from the form?\n"
                    + "(This quiz hasn't been saved yet)";
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                message,
                "Confirm Clear/Delete Quiz",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // If quiz exists in database, delete it
            if (currentQuiz != null) {
                boolean deleted = dbManager.deleteQuiz(currentQuiz.getQuizId());

                if (deleted) {
                    JOptionPane.showMessageDialog(this,
                            "Quiz deleted successfully from database!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Clear form
                    currentQuiz = null;
                    currentQuizQuestions.clear();
                    quizQuestionsTableModel.setRowCount(0);
                    questionCounter = 1;
                    currentEditingRow = -1;
                    clearQuestionInputs();

                    // Refresh dropdowns to update [Has Quiz] indicators
                    if (cmbQuizCourse.getSelectedItem() != null) {
                        String courseStr = (String) cmbQuizCourse.getSelectedItem();
                        int courseId = Integer.parseInt(courseStr.split(" - ")[0]);

                        String lessonStr = (String) cmbQuizLesson.getSelectedItem();
                        int lessonId = Integer.parseInt(lessonStr.split(" - ")[0]);

                        isLoadingQuizCourses = true;
                        loadCoursesForQuiz();
                        isLoadingQuizCourses = false;

                        // Restore selections
                        for (int i = 0; i < cmbQuizCourse.getItemCount(); i++) {
                            if (cmbQuizCourse.getItemAt(i).startsWith(courseId + " - ")) {
                                cmbQuizCourse.setSelectedIndex(i);
                                break;
                            }
                        }

                        javax.swing.SwingUtilities.invokeLater(() -> {
                            for (int i = 0; i < cmbQuizLesson.getItemCount(); i++) {
                                if (cmbQuizLesson.getItemAt(i).startsWith(lessonId + " - ")) {
                                    cmbQuizLesson.setSelectedIndex(i);
                                    break;
                                }
                            }
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete quiz from database!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // No saved quiz - just clear form
                currentQuizQuestions.clear();
                quizQuestionsTableModel.setRowCount(0);
                questionCounter = 1;
                currentEditingRow = -1;
                clearQuestionInputs();

                JOptionPane.showMessageDialog(this,
                        "All questions cleared from form!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnClearAllQuestionsActionPerformed

    private void btnSaveQuizActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveQuizActionPerformed
        if (cmbQuizCourse.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cmbQuizLesson.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a lesson!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get IDs
            String courseStr = (String) cmbQuizCourse.getSelectedItem();
            int courseId = Integer.parseInt(courseStr.split(" - ")[0]);

            String lessonStr = (String) cmbQuizLesson.getSelectedItem();
            int lessonId = Integer.parseInt(lessonStr.split(" - ")[0]);

            // Renumber questions before saving (1, 2, 3...)
            List<Question> questionsToSave = new ArrayList<>();
            for (int i = 0; i < currentQuizQuestions.size(); i++) {
                Question q = currentQuizQuestions.get(i);
                q.setQuestionID(i + 1);
                questionsToSave.add(q);
            }

            // Check if we're updating existing quiz or creating new one
            if (currentQuiz != null) {
                // VERIFY: Make sure we're updating the right quiz
                if (currentQuiz.getCourseID() != courseId || currentQuiz.getLessonID() != lessonId) {
                    JOptionPane.showMessageDialog(this,
                            "Error: Quiz mismatch detected!\n"
                            + "The loaded quiz doesn't match the selected course/lesson.\n"
                            + "Please refresh and try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Updating existing quiz
                currentQuiz.setQuestions(questionsToSave);
                dbManager.saveQuiz(currentQuiz);

                String questionCountMsg = questionsToSave.isEmpty()
                        ? "No questions (empty quiz)"
                        : "Questions: " + questionsToSave.size();

                JOptionPane.showMessageDialog(this,
                        "Quiz updated successfully!\n\n"
                        + "Course ID: " + courseId + "\n"
                        + "Lesson ID: " + lessonId + "\n"
                        + questionCountMsg,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // DON'T CLEAR - Keep the quiz loaded for continued editing
                // Just reload it to show the updated [Has Quiz] indicator
                isLoadingQuizCourses = true;
                loadCoursesForQuiz();
                isLoadingQuizCourses = false;

                // Restore selections
                for (int i = 0; i < cmbQuizCourse.getItemCount(); i++) {
                    if (cmbQuizCourse.getItemAt(i).startsWith(courseId + " - ")) {
                        cmbQuizCourse.setSelectedIndex(i);
                        break;
                    }
                }

                // Trigger lesson reload and selection
                javax.swing.SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < cmbQuizLesson.getItemCount(); i++) {
                        if (cmbQuizLesson.getItemAt(i).startsWith(lessonId + " - ")) {
                            cmbQuizLesson.setSelectedIndex(i);
                            break;
                        }
                    }
                });

                return; // Don't clear form

            } else {
                // Creating new quiz - check using BOTH courseId and lessonId
                Quiz existingQuiz = dbManager.getQuizByCourseAndLessonId(courseId, lessonId);

                if (existingQuiz != null) {
                    // Quiz exists but wasn't loaded
                    int choice = JOptionPane.showConfirmDialog(this,
                            "This lesson already has a quiz!\n\n"
                            + "Do you want to replace it with a new quiz?\n"
                            + "(The existing quiz will be deleted)\n\n"
                            + "Click 'Yes' to replace, 'No' to cancel.",
                            "Quiz Exists",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }

                    // User wants to replace - delete old quiz first
                    dbManager.deleteQuiz(existingQuiz.getQuizId());
                }

                // Generate a new quiz ID
                int newQuizId = dbManager.generateQuizId();

                // Create quiz with BOTH courseId and lessonId
                Quiz quiz = new Quiz(newQuizId, passingScore, quizRequired);
                quiz.setCourseID(courseId);
                quiz.setLessonID(lessonId);
                quiz.setQuestions(questionsToSave);

                // Save quiz to quizzes.json
                dbManager.saveQuiz(quiz);

                // Set as current quiz
                currentQuiz = quiz;

                String questionCountMsg = questionsToSave.isEmpty()
                        ? "No questions (empty quiz)"
                        : "Questions: " + questionsToSave.size();

                JOptionPane.showMessageDialog(this,
                        "Quiz created successfully!\n\n"
                        + "Course ID: " + courseId + "\n"
                        + "Lesson ID: " + lessonId + "\n"
                        + questionCountMsg + "\n"
                        + "Passing Score: " + passingScore + "%\n"
                        + "Required: " + (quizRequired ? "Yes" : "No"),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reload the quiz to keep it displayed
                currentQuizQuestions = new ArrayList<>(quiz.getQuestions());
                questionCounter = currentQuizQuestions.size() + 1;
                loadQuestionsIntoTable(currentQuizQuestions);

                // Refresh dropdowns
                isLoadingQuizCourses = true;
                loadCoursesForQuiz();
                isLoadingQuizCourses = false;

                // Restore selections
                for (int i = 0; i < cmbQuizCourse.getItemCount(); i++) {
                    if (cmbQuizCourse.getItemAt(i).startsWith(courseId + " - ")) {
                        cmbQuizCourse.setSelectedIndex(i);
                        break;
                    }
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < cmbQuizLesson.getItemCount(); i++) {
                        if (cmbQuizLesson.getItemAt(i).startsWith(lessonId + " - ")) {
                            cmbQuizLesson.setSelectedIndex(i);
                            break;
                        }
                    }
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving quiz: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnSaveQuizActionPerformed

    private void txtOptionAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOptionAActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOptionAActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> {
            // This is just for testing the UI in NetBeans designer
            // Don't use this in production - always pass a real instructor
            Instructor testInstructor = new Instructor("1", "test", "test@test.com", "test");
            new InstructorDashboardFrame(testInstructor).setVisible(true);
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddLesson;
    private javax.swing.JButton btnAddQuestion;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnClearAllQuestions;
    private javax.swing.JButton btnClearQuestionInForm;
    private javax.swing.JButton btnCreateCourse;
    private javax.swing.JButton btnDeleteButton;
    private javax.swing.JButton btnDeleteLesson;
    private javax.swing.JButton btnDeleteQuestion;
    private javax.swing.JButton btnEditCourse;
    private javax.swing.JButton btnEditLesson;
    private javax.swing.JButton btnEditQuestion;
    private javax.swing.ButtonGroup btnGroupCorrectAnswer;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnRefreshLessons;
    private javax.swing.JButton btnSaveQuiz;
    private javax.swing.JButton btnViewStudents;
    private javax.swing.JComboBox<String> cmbCourses;
    private javax.swing.JComboBox<String> cmbQuizCourse;
    private javax.swing.JComboBox<String> cmbQuizLesson;
    private javax.swing.JPanel createCoursePanel;
    private javax.swing.JPanel createQuizPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCourseDescription;
    private javax.swing.JLabel lblCourseDescription1;
    private javax.swing.JLabel lblCourseTitle;
    private javax.swing.JLabel lblSelectCourse;
    private javax.swing.JLabel lblSelectLesson;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel manageLessonsPanel;
    private javax.swing.JPanel myCoursesPanel;
    private javax.swing.JPanel questionPanel;
    private javax.swing.JRadioButton radioOptionA;
    private javax.swing.JRadioButton radioOptionB;
    private javax.swing.JRadioButton radioOptionC;
    private javax.swing.JRadioButton radioOptionD;
    private javax.swing.JScrollPane scrollQuizPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JScrollPane tblL;
    private javax.swing.JTable tblLessons;
    private javax.swing.JTable tblMyCourses;
    private javax.swing.JScrollPane tblMyCourses1;
    private javax.swing.JTable tblQuizQuestions;
    private javax.swing.JPanel topPanel;
    private javax.swing.JTextField txtCourseDescription;
    private javax.swing.JTextField txtCourseID;
    private javax.swing.JTextField txtCourseTitle;
    private javax.swing.JTextField txtExplanationInput;
    private javax.swing.JTextField txtInstructorID;
    private javax.swing.JTextField txtInstructorName;
    private javax.swing.JTextField txtOptionA;
    private javax.swing.JTextField txtOptionB;
    private javax.swing.JTextField txtOptionC;
    private javax.swing.JTextField txtOptionD;
    private javax.swing.JTextArea txtQuestionInput;
    // End of variables declaration//GEN-END:variables
}
