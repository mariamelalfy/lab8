
package ui;

import javax.swing.*;
import model.Student;
import model.Lesson;
import model.Quiz;
import service.StudentService;

public class CourseDetailsDialog extends javax.swing.JDialog {
    private model.Student student;
    private service.StudentService service;
    private int courseId;
    private String courseName;
    
    public CourseDetailsDialog(java.awt.Frame parent, Student student, 
                           StudentService service, int courseId, String courseName) {
    super(parent, true);
    initComponents();
    this.student = student;
    this.service = service;
    this.courseId = courseId;
    this.courseName = courseName;
    
    setTitle("Course Details - " + courseName);
    setSize(550, 650); // Made taller
    setLocationRelativeTo(parent);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(true);
    
    courseTitleLabel.setText(courseName);
    loadLessons();
    updateProgress();
    
    lessonList.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            updateButtonStates();
        }
    });
    if (lessonList.getModel().getSize() > 0) {
        lessonList.setSelectedIndex(0);  // Select first lesson
    }
    updateButtonStates(); 
}
private void updateButtonStates() {
    String selected = lessonList.getSelectedValue();
    
    if (selected == null) {
        quizBtn.setEnabled(false);
        markBtn.setEnabled(false);
        return;
    }
    
    try {
        // Remove checkmark if present
        String cleanSelected = selected.replace(" ✓", "").replace("🔒 ", "").replace(" 📝", "");
        int lessonId = Integer.parseInt(cleanSelected.split(" - ")[0]);
        
        // Check if lesson is completed
        boolean completed = student.isLessonCompleted(
            String.valueOf(courseId), 
            String.valueOf(lessonId)
        );
        
        // Get the lesson to check for quiz
        java.util.List<Lesson> lessons = service.getLessons(courseId);
        Lesson selectedLesson = null;
        int selectedIndex = -1;
        
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonID() == lessonId) {
                selectedLesson = lessons.get(i);
                selectedIndex = i;
                break;
            }
        }
        
        if (selectedLesson != null) {
            Quiz quiz = selectedLesson.getQuiz();
            boolean hasQuiz = quiz != null;
            
            boolean canAccessLesson = canAccessLesson(lessons, selectedIndex);
            
            if (!canAccessLesson) {
                quizBtn.setEnabled(false);
                markBtn.setEnabled(false);
                markBtn.setText("🔒 Complete Previous Lesson");
                return;
            }
            
            // Enable quiz button if lesson has quiz
            if (hasQuiz) {
                quizBtn.setEnabled(true);
                if (selectedLesson.isQuizRequired()) {
                    quizBtn.setText("📝 Take Quiz (Required)");
                } else {
                    quizBtn.setText("📝 Take Quiz (Optional)");
                }
            } else {
                quizBtn.setEnabled(false);
                quizBtn.setText("📝 Take Quiz");
            }
            
            // Enable mark button based on quiz requirement
            if (completed) {
                markBtn.setEnabled(false);
                markBtn.setText("✓ Completed");
            } else if (hasQuiz && selectedLesson.isQuizRequired()) {
                // Check if student passed the quiz
                boolean passed = service.hasPassedQuiz(
                    student.getUserId(), 
                    lessonId, 
                    quiz.getQuizId()
                );
                
                if (passed) {
                    markBtn.setEnabled(true);
                    markBtn.setText("✓ Mark as completed");
                } else {
                    markBtn.setEnabled(false);
                    markBtn.setText("Pass Quiz First");
                }
            } else {
                // No required quiz, can complete
                markBtn.setEnabled(true);
                markBtn.setText("✓ Mark as completed");
            }
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        quizBtn.setEnabled(false);
        markBtn.setEnabled(false);
    }
}

private boolean canAccessLesson(java.util.List<Lesson> lessons, int lessonIndex) {
    // First lesson is always accessible
    if (lessonIndex == 0) {
        return true;
    }
    
    // Check if previous lesson is completed
    Lesson previousLesson = lessons.get(lessonIndex - 1);
    boolean previousCompleted = student.isLessonCompleted(
        String.valueOf(courseId),
        String.valueOf(previousLesson.getLessonID())
    );
    
    if (!previousCompleted) {
        return false;
    }
    
    // If previous lesson has a required quiz, check if it's passed
    if (previousLesson.getQuiz() != null && previousLesson.isQuizRequired()) {
        boolean passed = service.hasPassedQuiz(
            student.getUserId(),
            previousLesson.getLessonID(),
            previousLesson.getQuiz().getQuizId()
        );
        
        return passed;
    }
    
    // Previous lesson is completed (no quiz or optional quiz)
    return true;
}

private void refreshStudentData() {
    try {
        // Reload the student from the service to get updated completion data
        Student updatedStudent = service.getStudent(student.getUserId());
        if (updatedStudent != null) {
            this.student = updatedStudent;
        }
    } catch (Exception e) {
        System.err.println("Error refreshing student data: " + e.getMessage());
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

        courseTitleLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        lessonList = new javax.swing.JList<>();
        quizBtn = new javax.swing.JButton();
        lessonLabel = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();
        markBtn = new javax.swing.JButton();

        setBackground(new java.awt.Color(240, 242, 245));

        courseTitleLabel.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        courseTitleLabel.setForeground(new java.awt.Color(41, 128, 185));
        courseTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        courseTitleLabel.setText("Course Details");

        progressBar.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        progressBar.setForeground(new java.awt.Color(41, 128, 185));
        progressBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220)));
        progressBar.setPreferredSize(new java.awt.Dimension(250, 28));

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220)))));

        lessonList.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        lessonList.setFixedCellHeight(40);
        lessonList.setSelectionBackground(new java.awt.Color(52, 152, 219));
        lessonList.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(lessonList);

        quizBtn.setBackground(new java.awt.Color(0, 102, 204));
        quizBtn.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        quizBtn.setForeground(new java.awt.Color(255, 255, 255));
        quizBtn.setText("Take Quiz");
        quizBtn.setBorderPainted(false);
        quizBtn.setFocusPainted(false);
        quizBtn.setPreferredSize(new java.awt.Dimension(180, 38));
        quizBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quizBtnActionPerformed(evt);
            }
        });

        lessonLabel.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lessonLabel.setForeground(new java.awt.Color(52, 73, 94));
        lessonLabel.setText("Lessons");

        progressLabel.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        progressLabel.setForeground(new java.awt.Color(52, 73, 94));
        progressLabel.setText("Course Progress");

        markBtn.setBackground(new java.awt.Color(46, 204, 113));
        markBtn.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        markBtn.setForeground(new java.awt.Color(255, 255, 255));
        markBtn.setText("Mark as completed");
        markBtn.setBorderPainted(false);
        markBtn.setFocusPainted(false);
        markBtn.setPreferredSize(new java.awt.Dimension(180, 38));
        markBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(markBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(quizBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(161, 161, 161)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(72, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(238, 238, 238)
                .addComponent(lessonLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(courseTitleLabel)
                .addGap(183, 183, 183))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progressLabel)
                .addGap(197, 197, 197))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(courseTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lessonLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(progressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(markBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quizBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void quizBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quizBtnActionPerformed
String selected = lessonList.getSelectedValue();
    if (selected == null) {
        JOptionPane.showMessageDialog(this, 
            "Please select a lesson first!", 
            "No Selection", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    try {
        // Remove checkmark if present
        String cleanSelected = selected.replace(" ✓", "").replace("🔒 ", "").replace(" 📝", "");
        int lessonId = Integer.parseInt(cleanSelected.split(" - ")[0]);
        
        // Get the lesson and its quiz
        java.util.List<Lesson> lessons = service.getLessons(courseId);
        Lesson selectedLesson = null;
        int selectedIndex = -1;
        
        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);
            if (l.getLessonID() == lessonId) {
                selectedLesson = l;
                selectedIndex = i;
                break;
            }
        }
        
        if (selectedLesson == null) {
            JOptionPane.showMessageDialog(this, 
                "Lesson not found!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Quiz quiz = selectedLesson.getQuiz();
        if (quiz == null) {
            JOptionPane.showMessageDialog(this, 
                "This lesson does not have a quiz.", 
                "No Quiz", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Open Quiz Dialog
        java.awt.Frame parentFrame = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
        QuizDialog quizDialog = new QuizDialog(
            parentFrame, 
            true, 
            student, 
            service, 
            quiz, 
            courseId, 
            lessonId,
            selectedLesson.getLessonTitle()
        );
        
        quizDialog.setVisible(true);
        
        refreshStudentData();
        loadLessons();
        updateProgress();
        
        // Check if the current lesson is now completed
        boolean nowCompleted = student.isLessonCompleted(
            String.valueOf(courseId), 
            String.valueOf(lessonId)
        );
        
        // Auto-select next lesson if completed
        if (nowCompleted) {
            int nextIndex = selectedIndex + 1;
            if (nextIndex < lessons.size()) {
                lessonList.setSelectedIndex(nextIndex);
                lessonList.ensureIndexIsVisible(nextIndex);
            } else {
                lessonList.setSelectedIndex(selectedIndex);
            }
        } else {
            // Keep current selection
            lessonList.setSelectedIndex(selectedIndex);
        }
        
        updateButtonStates();
        
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Error opening quiz: " + e.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_quizBtnActionPerformed

    private void markBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markBtnActionPerformed
  String selected = lessonList.getSelectedValue();
    if (selected == null) {
        JOptionPane.showMessageDialog(this, 
            "Please select a lesson first!", 
            "No Selection", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    try {
        // Remove all icons for parsing
        String cleanSelected = selected.replace(" ✓", "").replace("🔒 ", "").replace(" 📝", "");
        int lessonId = Integer.parseInt(cleanSelected.split(" - ")[0]);
        
        java.util.List<Lesson> lessons = service.getLessons(courseId);
        int selectedIndex = -1;
        Lesson selectedLesson = null;
        
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonID() == lessonId) {
                selectedIndex = i;
                selectedLesson = lessons.get(i);
                break;
            }
        }
        
        if (selectedLesson == null) {
            JOptionPane.showMessageDialog(this, 
                "Lesson not found!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if lesson is accessible
        if (!canAccessLesson(lessons, selectedIndex)) {
            JOptionPane.showMessageDialog(this,
                "You must complete the previous lesson first!",
                "Lesson Locked",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if this lesson has a required quiz
        Quiz quiz = selectedLesson.getQuiz();
        if (quiz != null && selectedLesson.isQuizRequired()) {
            // Check if student has passed the quiz
            boolean passed = service.hasPassedQuiz(
                student.getUserId(), 
                lessonId, 
                quiz.getQuizId()
            );
            
            if (!passed) {
                JOptionPane.showMessageDialog(this,
                    "You must pass the quiz before marking this lesson as completed!",
                    "Quiz Required",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Mark lesson as completed
        boolean ok = service.markLessonCompleted(
            student.getUserId(), courseId, lessonId);
        
        if (ok) {
            refreshStudentData();
            
            JOptionPane.showMessageDialog(this, 
                "Lesson marked as completed!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        
            boolean isLastLesson = (selectedIndex == lessons.size() - 1);
            
            if (isLastLesson) {
                System.out.println("Last lesson completed! Checking certificate eligibility...");
                
                // Check if student is eligible for certificate
                database.JsonDatabaseManager db = new database.JsonDatabaseManager();
                service.CertificateService cs = new service.CertificateService(db);
                service.CourseCompletionTracker tracker = new service.CourseCompletionTracker(db);
                
                if (cs.isEligibleForCertificate(student.getUserId(), String.valueOf(courseId))) {
                    System.out.println("Student is eligible for certificate!");
                    
                    // Check if certificate already exists
                    model.Certificate existingCert = cs.getCertificateForCourse(
                        student.getUserId(), 
                        String.valueOf(courseId)
                    );
                    
                    if (existingCert == null) {
                        // Generate new certificate
                        model.Certificate newCert = cs.generateCertificate(
                            student.getUserId(), 
                            String.valueOf(courseId)
                        );
                        
                        if (newCert != null) {
                            System.out.println("Certificate generated successfully!");
                            
                            // Show congratulations message
                            JOptionPane.showMessageDialog(this,
                                "CONGRATULATIONS! \n\n" +
                                "You have completed the entire course!\n" +
                                "Your certificate has been generated.\n\n" +
                                "Final Score: " + String.format("%.1f%%", newCert.getFinalScore()) + "\n\n" +
                                "Check 'My Certificates' to view and download it!",
                                "Certificate Earned!",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            System.err.println("Failed to generate certificate");
                        }
                    } else {
                        System.out.println("Certificate already exists for this course");
                        
                        // Still show congratulations
                        JOptionPane.showMessageDialog(this,
                            " Course Completed! \n\n" +
                            "You have finished all lessons!\n" +
                            "Your certificate is available in 'My Certificates'.",
                            "Course Complete!",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    System.out.println("Student not eligible yet. Checking requirements...");
                    
                    // Debug: Check what's missing
                    boolean allLessons = tracker.areAllLessonsCompleted(student.getUserId(), String.valueOf(courseId));
                    boolean allQuizzes = tracker.areAllQuizzesPassed(student.getUserId(), String.valueOf(courseId));
                    
                    System.out.println("All lessons completed: " + allLessons);
                    System.out.println("All quizzes passed: " + allQuizzes);
                    
                    if (!allQuizzes) {
                        JOptionPane.showMessageDialog(this,
                            "Course completed, but you need to pass all quizzes\n" +
                            "to earn your certificate!",
                            "Almost There!",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
            // Reload lessons with updated student data
            loadLessons();
            updateProgress();
            
            // Auto-select next lesson or keep current
            if (!isLastLesson) {
                int nextIndex = selectedIndex + 1;
                lessonList.setSelectedIndex(nextIndex);
                lessonList.ensureIndexIsVisible(nextIndex);
            } else {
                // Last lesson - keep it selected
                lessonList.setSelectedIndex(selectedIndex);
            }
            
            updateButtonStates();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Error: Unable to mark lesson as completed.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "An error occurred: " + e.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_markBtnActionPerformed
private void loadLessons() {
    DefaultListModel<String> model = new DefaultListModel<>();
    
    try {
        java.util.List<Lesson> lessons = service.getLessons(courseId);
        
        if (lessons.isEmpty()) {
            model.addElement("No lessons available for this course");
        } else {
            for (int i = 0; i < lessons.size(); i++) {
                Lesson l = lessons.get(i);
                String lessonText = l.getLessonID() + " - " + l.getLessonTitle();
                
                // Check if lesson is completed
                boolean completed = student.isLessonCompleted(
                    String.valueOf(courseId), 
                    String.valueOf(l.getLessonID())
                );
                
                // Check if lesson is locked
                boolean locked = !canAccessLesson(lessons, i);
                
                // Add appropriate icon
                if (completed) {
                    lessonText += " ✓";
                } else if (locked) {
                    lessonText = "🔒 " + lessonText;
                } else if (l.getQuiz() != null && l.isQuizRequired()) {
                    lessonText += " 📝";
                }
                
                model.addElement(lessonText);
            }
        }
        
        lessonList.setModel(model);
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
            "Error loading lessons: " + e.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        model.addElement("Error loading lessons");
        lessonList.setModel(model);
    }
}

private void updateProgress() {
       try {
            int percent = (int) service.calculateProgress(
                student.getUserId(), String.valueOf(courseId));
            
            progressBar.setValue(percent);
            progressBar.setString(percent + "%");
            progressBar.setStringPainted(true);
            
            // Change color based on progress
            if (percent == 100) {
                progressBar.setForeground(new java.awt.Color(76, 175, 80)); // Green
            } else if (percent >= 50) {
                progressBar.setForeground(new java.awt.Color(255, 152, 0)); // Orange
            } else {
                progressBar.setForeground(new java.awt.Color(33, 150, 243)); // Blue
            }
            
        } catch (Exception e) {
            progressBar.setValue(0);
            progressBar.setString("Error calculating progress");
            progressBar.setStringPainted(true);
        }
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel courseTitleLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lessonLabel;
    private javax.swing.JList<String> lessonList;
    private javax.swing.JButton markBtn;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton quizBtn;
    // End of variables declaration//GEN-END:variables
}
