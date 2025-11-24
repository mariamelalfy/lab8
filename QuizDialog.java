package ui;

import model.*;
import service.StudentService;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDialog extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(QuizDialog.class.getName());
    
    private Quiz quiz;
    private Student student;
    private StudentService service;
    private int courseId;
    private int lessonId;
    private String lessonTitle;
    private List<ButtonGroup> answerGroups;
    private List<JRadioButton> selectedAnswers;
    
    private JPanel jPanel1;
    private JLabel jLabel1;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JPanel jPanel3;
    private JButton jButton1;

    public QuizDialog(java.awt.Frame parent, boolean modal, Student student, 
                      StudentService service, Quiz quiz, int courseId, 
                      int lessonId, String lessonTitle) {
        super(parent, modal);
        this.student = student;
        this.service = service;
        this.quiz = quiz;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.answerGroups = new ArrayList<>();
        this.selectedAnswers = new ArrayList<>();
        
        initComponents();  
        setupQuiz();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Initialize components
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jPanel2 = new JPanel();
        jScrollPane1 = new JScrollPane();
        jPanel3 = new JPanel();
        jButton1 = new JButton();

        // Configure the dialog window
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Quiz");
        setPreferredSize(new java.awt.Dimension(700, 700));

        // Configure the title label
        jLabel1.setFont(new java.awt.Font("Helvetica Neue", Font.BOLD, 18));
        jLabel1.setText("Quiz - [Lesson Name]");

        // Layout for top panel (jPanel1) - contains the title
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        // Add top panel to dialog
        getContentPane().add(jPanel1, BorderLayout.PAGE_START);

        // Configure submit button
        jButton1.setBackground(new Color(0, 204, 0));
        jButton1.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
        jButton1.setForeground(Color.WHITE);
        jButton1.setText("Submit Quiz");
        jButton1.addActionListener(evt -> jButton1ActionPerformed(evt));

        // Layout for button panel (jPanel3)
        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(250, 250, 250)
                .addComponent(jButton1, 150, 150, 150)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jButton1, 40, 40, 40)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        // Layout for center panel (jPanel2)
        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        // Add center panel to dialog
        getContentPane().add(jPanel2, BorderLayout.CENTER);

        // Pack the dialog to fit all components
        pack();
    }

    private void setupQuiz() {
        // Update title
        jLabel1.setText("Quiz - " + lessonTitle);
        
        // Create quiz panel with questions
        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        List<Question> questions = quiz.getQuestions();
        
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            
            // Question panel
            JPanel questionPanel = new JPanel();
            questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
            questionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 5, 10, 5),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
            ));
            questionPanel.setBackground(Color.WHITE);
            
            // Question text
            JLabel questionLabel = new JLabel((i + 1) + ". " + q.getQuestionText());
            questionLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            questionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            questionPanel.add(questionLabel);
            
            // Answer choices
            ButtonGroup group = new ButtonGroup();
            answerGroups.add(group);
            
            // Create radio buttons for options A, B, C, D
            String[] options = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
            for (String option : options) {
                if (option != null && !option.trim().isEmpty()) {
                    JRadioButton radioButton = new JRadioButton(option);
                    radioButton.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
                    radioButton.setBorder(BorderFactory.createEmptyBorder(3, 20, 3, 5));
                    radioButton.setBackground(Color.WHITE);
                    group.add(radioButton);
                    questionPanel.add(radioButton);
                }
            }
            
            questionsPanel.add(questionPanel);
            questionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        jScrollPane1.setViewportView(questionsPanel);
        
        // Show quiz info
        int attemptCount = service.getQuizAttemptCount(student.getUserId(), quiz.getQuizId());
        double bestScore = service.getBestQuizScore(student.getUserId(), quiz.getQuizId());
        
        if (attemptCount > 0) {
            JOptionPane.showMessageDialog(this,
                "Previous Attempts: " + attemptCount + "\n" +
                "Best Score: " + String.format("%.1f%%", bestScore),
                "Quiz History",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // Collect answers
        List<String> studentAnswers = new ArrayList<>();
        
        for (int i = 0; i < answerGroups.size(); i++) {
            ButtonGroup group = answerGroups.get(i);
            String answer = null;
            
            // Find selected answer
            for (java.util.Enumeration<AbstractButton> buttons = group.getElements(); 
                 buttons.hasMoreElements();) {
                AbstractButton button = buttons.nextElement();
                if (button.isSelected()) {
                    answer = button.getText();
                    break;
                }
            }
            
            if (answer == null) {
                JOptionPane.showMessageDialog(this,
                    "Please answer all questions before submitting!",
                    "Incomplete Quiz",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            studentAnswers.add(answer);
        }
        
        // Submit quiz
        try {
            QuizAttempt attempt = service.submitQuiz(
                student.getUserId(), 
                courseId, 
                lessonId, 
                quiz, 
                studentAnswers
            );
            
            // Show results
            String message = String.format(
                "Quiz Completed!\n\n" +
                "Your Score: %.1f%%\n" +
                "Passing Score: %d%%\n" +
                "Status: %s\n\n" +
                "Total Attempts: %d",
                attempt.getScore(),
                quiz.getPassingScore(),
                attempt.isPassed() ? "PASSED ✓" : "FAILED ✗",
                service.getQuizAttemptCount(student.getUserId(), quiz.getQuizId())
            );
            
            int messageType = attempt.isPassed() ? 
                JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            
            JOptionPane.showMessageDialog(this, message, "Quiz Results", messageType);
            
            // Show correct answers if failed
            if (!attempt.isPassed()) {
                int showAnswers = JOptionPane.showConfirmDialog(this,
                    "Would you like to see the correct answers?",
                    "Review Quiz",
                    JOptionPane.YES_NO_OPTION);
                
                if (showAnswers == JOptionPane.YES_OPTION) {
                    showCorrectAnswers(studentAnswers);
                }
            }
            
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error submitting quiz: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    private void showCorrectAnswers(List<String> studentAnswers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Correct Answers:\n\n");
        
        List<Question> questions = quiz.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            sb.append((i + 1)).append(". ").append(q.getQuestionText()).append("\n");
            sb.append("   Your Answer: ").append(studentAnswers.get(i)).append("\n");
            sb.append("   Correct Answer: ").append(q.getCorrectAnswer()).append("\n");
            
            // Show explanation if available
            if (q.getExplanation() != null && !q.getExplanation().trim().isEmpty()) {
                sb.append("   Explanation: ").append(q.getExplanation()).append("\n");
            }
            sb.append("\n");
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Quiz Review", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            // Create mock data for testing
            database.JsonDatabaseManager db = database.JsonDatabaseManager.getInstance();
            service.StudentService service = new service.StudentService(db);
            
            Student testStudent = new Student("1", "TestStudent", "test@test.com", "hash");
            
            Quiz testQuiz = new Quiz(1, 60, true);
            
            // Create test questions with the new Question structure
            Question q1 = new Question(
                1,
                "What is 2 + 2?",
                "2",
                "3",
                "4",
                "5",
                "4",
                "Basic addition: 2 + 2 equals 4"
            );
            
            Question q2 = new Question(
                2,
                "What is the capital of France?",
                "London",
                "Paris",
                "Berlin",
                "Madrid",
                "Paris",
                "Paris is the capital and largest city of France"
            );
            
            testQuiz.addQuestion(q1);
            testQuiz.addQuestion(q2);
            
            QuizDialog dialog = new QuizDialog(
                new javax.swing.JFrame(), 
                true, 
                testStudent, 
                service, 
                testQuiz, 
                1, 
                1, 
                "Test Lesson"
            );
            
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            
            dialog.setVisible(true);
        });
    }
}
