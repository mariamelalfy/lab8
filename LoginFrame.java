package ui;

import java.awt.Color;
import javax.swing.*;
import service.*;
import model.*;
import javax.swing.Timer;
import service.StudentService; 
import database.*;

public class LoginFrame extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginFrame.class.getName());

    private int loginAttempts = 0;
    private final int MAX_ATTEMPTS = 3;

   // At the top of LoginFrame class
   private final UserService userService;

  
// In your constructor:
public LoginFrame() {
    JsonDatabaseManager db = new JsonDatabaseManager();
    userService = new UserService(db);
    
    setLocationRelativeTo(null);
    setTitle("Skill Forge - Login");
    setSize(350, 250);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initComponents();
}

   private void handleLogin() {
        // Get the input values
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        // Clear previous error message
       statusLabel.setText("");
        
        // Run all validations
        if (!checkEmptyFields(email, password)) {
            return; // Stop if validation fails
        }
        
        if (!checkMinimumLength(email, password)) {
            return; // Stop if validation fails
        }
        
        if (!checkMaximumLength(email, password)) {
            return; // Stop if validation fails
        }
        
        if (!checkEmailFormat(email)) {
            return; // Stop if validation fails
        }
        
       if (!checkRoleSelected()) {
        return; // Stop if validation fails
    }
        
        if (!checkCredentials(email, password)) {
            return; // Stop if validation fails
        }
    }

   private boolean checkEmptyFields(String email, String password) {
        // Check if both are empty
        if (email.isEmpty() && password.isEmpty()) {
            showError("Please enter username and password!");
            emailField.requestFocus(); // Put cursor in username field
            return false;
        }
        
        // Check if username is empty
        if (email.isEmpty()) {
            showError("Username cannot be empty!");
          emailField.requestFocus();
            return false;
        }
        
        // Check if password is empty
        if (password.isEmpty()) {
            showError("Password cannot be empty!");
            passwordField.requestFocus();
            return false;
            
            
        }
        
        return true; // All fields have values
    }
    
    private boolean checkMinimumLength(String username, String password) {
        // Username must be at least 3 characters
        if (username.length() < 3) {
            showError("Username must be at least 3 characters!");
            emailField.requestFocus();
            emailField.selectAll(); // Highlight the text
            return false;
        }
        
        // Password must be at least 4 characters
        if (password.length() < 4) {
            showError("Password must be at least 4 characters!");
            passwordField.requestFocus();
            passwordField.selectAll();
            return false;
        }
        
        return true; // Length is valid
    }
  
    private boolean checkMaximumLength(String username, String password) {
        // Username cannot be more than 20 characters
        if (username.length() > 20) {
            showError("Username cannot exceed 20 characters!");
            emailField.requestFocus();
           emailField.selectAll();
            return false;
        }
        
        // Password cannot be more than 20 characters
        if (password.length() > 20) {
            showError("Password cannot exceed 20 characters!");
            passwordField.requestFocus();
            passwordField.selectAll();
            return false;
        }
        
        return true; // Length is valid
    }
    
  private boolean checkEmailFormat(String email) {
    // Check if email is empty
    if (email == null || email.trim().isEmpty()) {
        showError("Email cannot be empty!");
        emailField.requestFocus();
        return false;
    }
    
    email = email.trim();
    
    // Check if email contains @
    if (!email.contains("@")) {
        showError("Email must contain @ symbol!");
        emailField.requestFocus();
        emailField.selectAll();
        return false;
    }
    
    // Check if email contains . (dot)
    if (!email.contains(".")) {
        showError("Email must contain a dot (.)!");
        emailField.requestFocus();
        emailField.selectAll();
        return false;
    }
    
    // Check if dot comes after @
    int atIndex = email.indexOf("@");
    int dotIndex = email.indexOf(".", atIndex);
    
    if (dotIndex == -1) {
        showError("Email must have a dot after @ (e.g., user@example.com)!");
        emailField.requestFocus();
        emailField.selectAll();
        return false;
    }
    
    // All checks passed!
    return true;
}
  
private boolean checkRoleSelected() {
    // Check if neither radio button is selected
    if (!studentRadio.isSelected() && !instructorRadio.isSelected() && !adminRadio.isSelected()) {
        showError("Please select your role (Admin- Student - Instructor)!");
        return false;
    }
    
    return true; // A role is selected
}

    
 private boolean checkCredentials(String email, String password) {
    String role;
    if (studentRadio.isSelected()) {
        role = "Student";
    } else if (instructorRadio.isSelected()) {
        role = "Instructor";
    } else if (adminRadio.isSelected()) {
        role = "Admin";
    } else {
        role = "";
    }
    
    // Use UserService to authenticate
    User user = userService.login(email, password);

    if (user == null) {
        loginAttempts++;
        handleFailedLogin("Invalid " + role + " email or password!");
        return false;
    }

    // Check if the user role matches the selected role
    if (!user.getRole().equalsIgnoreCase(role)) {
        loginAttempts++;
        handleFailedLogin("User role mismatch! Please select the correct role.");
        return false;
    }
    
    loginSuccess(user);
    return true;
}
    
    private void handleFailedLogin(String message) {
        int remainingAttempts = MAX_ATTEMPTS - loginAttempts;
        
        // Check if max attempts reached
        if (loginAttempts >= MAX_ATTEMPTS) {
            showError("Too many failed attempts! Application closing...");
            loginButton.setEnabled(false); // Disable login button
            clearButton.setEnabled(false); // Disable clear button
            
            // Show dialog and close
            JOptionPane.showMessageDialog(
                this,
                "You have exceeded maximum login attempts.\nApplication will now close.",
                "Security Alert",
                JOptionPane.WARNING_MESSAGE
            );
            
            System.exit(0); // Close application
        } else {
            // Show error with remaining attempts
            showError(message + " (" + remainingAttempts + " attempts left)");
        }
    }
    
    private void loginSuccess(User user) {
    // Reset login attempts
    loginAttempts = 0;
    
    // Show success message
    statusLabel.setText("✓ Login successful! Welcome ");
    statusLabel.setForeground(new Color(0, 150, 0)); // Green color
    
    // Small delay to show success message
    Timer timer = new Timer(500, e -> {
        this.dispose();
        
        // Open appropriate dashboard
        if (user instanceof Student) {
            openStudentDashboard((Student) user);
        } else if (user instanceof Instructor) {
            openInstructorDashboard((Instructor) user);
        } else if(user instanceof Admin){
           openAdminDashboard();
        }
    });
    timer.setRepeats(false);
    timer.start();
}

private void openStudentDashboard(Student student) {
        SwingUtilities.invokeLater(() -> {
            try {
                JsonDatabaseManager db = new JsonDatabaseManager();
                StudentService studentService = new StudentService(db);

                JFrame frame = new JFrame("SkillForge - Student Dashboard");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(900, 650);
                frame.setLocationRelativeTo(null);

                StudentDashboardFrame dashboard = new StudentDashboardFrame(student, studentService);
                frame.getContentPane().add(dashboard);

                frame.setVisible(true);

                System.out.println("Student dashboard opened for: " + student.getUsername());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error opening student dashboard: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

private void openInstructorDashboard(Instructor instructor) {
        SwingUtilities.invokeLater(() -> {
            try {
                InstructorDashboardFrame dashboard = new InstructorDashboardFrame(instructor);
                dashboard.setVisible(true);
                
                System.out.println("Instructor dashboard opened for: " + instructor.getUsername());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error opening instructor dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
private void openAdminDashboard() {
        SwingUtilities.invokeLater(() -> {
            try {
                AdminDashboardFrame adminDashboard = new AdminDashboardFrame();
                adminDashboard.setVisible(true);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error opening instructor dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void showError(String message) {
      statusLabel.setText("✗ " + message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void handleClear() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
        emailField.requestFocus();
    }
    private void openSignup() {
        new SignUpFrame().setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        status = new javax.swing.ButtonGroup();
        Title = new javax.swing.JLabel();
        emailLabel = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        loginButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        studentRadio = new javax.swing.JRadioButton();
        instructorRadio = new javax.swing.JRadioButton();
        signupLabel = new javax.swing.JLabel();
        signupButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        adminRadio = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Title.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        Title.setText("SkillForge Login");

        emailLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        emailLabel.setText("Email:");

        emailField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailFieldActionPerformed(evt);
            }
        });

        passwordLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        passwordLabel.setText("Password:");

        loginButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        clearButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        status.add(studentRadio);
        studentRadio.setText("Student");
        studentRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studentRadioActionPerformed(evt);
            }
        });

        status.add(instructorRadio);
        instructorRadio.setText("Instructor");

        signupLabel.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        signupLabel.setText("Don't have an account?");

        signupButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        signupButton.setText("Sign up");
        signupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signupButtonActionPerformed(evt);
            }
        });

        statusLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        statusLabel.setForeground(new java.awt.Color(255, 0, 0));
        statusLabel.setName(""); // NOI18N

        status.add(adminRadio);
        adminRadio.setText("Admin");
        adminRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(emailLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(passwordLabel)
                                    .addGap(18, 18, 18)
                                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(adminRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(studentRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(instructorRadio)
                                .addGap(15, 15, 15))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(132, 132, 132)
                        .addComponent(Title)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(loginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(signupLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(signupButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                        .addComponent(clearButton)
                        .addGap(15, 15, 15))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(Title)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(emailLabel)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studentRadio)
                    .addComponent(instructorRadio)
                    .addComponent(adminRadio))
                .addGap(50, 50, 50)
                .addComponent(statusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(clearButton)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(loginButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(signupButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(signupLabel))
                        .addGap(41, 41, 41))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        handleLogin();
    }//GEN-LAST:event_loginButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        handleClear();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void studentRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_studentRadioActionPerformed

    private void signupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signupButtonActionPerformed
        openSignup();
    }//GEN-LAST:event_signupButtonActionPerformed

    private void emailFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_emailFieldActionPerformed

    private void adminRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adminRadioActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Title;
    private javax.swing.JRadioButton adminRadio;
    private javax.swing.JButton clearButton;
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JRadioButton instructorRadio;
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JButton signupButton;
    private javax.swing.JLabel signupLabel;
    private javax.swing.ButtonGroup status;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JRadioButton studentRadio;
    // End of variables declaration//GEN-END:variables

}
