package gui;
import java.awt.Dimension;
import model.Admin;
import model.Course;
import model.Course.ApprovalStatus;
import service.AdminService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import javax.swing.Timer; 
import java.awt.BorderLayout;
import java.util.List;
public class AdminDashboardFrame extends javax.swing.JFrame {
    private Admin admin;
    private AdminService adminService;
    private SimpleDateFormat dateFormat;
    private DefaultTableModel tableModel;

   public AdminDashboardFrame() {
this.admin = new Admin("A001", "admin", "admin@example.com", "password123");
    
    this.adminService = new AdminService();
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    initComponents();
    setupCourseDetailsPanel();
    setupTableInTabs();
    setupTable();
    setupListeners();
    loadPendingCourses();
    updateStats();
}

public AdminDashboardFrame(Admin admin) {
    this.admin = admin;
    this.adminService = new AdminService();
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
initComponents();
setupCourseDetailsPanel();
setupTableInTabs();
setupTable();
    setupListeners();
    loadPendingCourses();
    updateStats();}

private void setupTable() {
    
    String[] columns = {"Course ID", "Title", "Instructor ID", "Submission Date", "Status"};
    tableModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    courseTable.setModel(tableModel);
    courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    courseTable.setRowHeight(25);
}

private void setupListeners() {
    
    tabbedpane.addChangeListener(e -> {
        int tab = tabbedpane.getSelectedIndex();
        if (tab == 0) {
            loadPendingCourses();
        } else if (tab == 1) {
            loadApprovedCourses();
        } else if (tab == 2) {
            loadRejectedCourses();
        } else {
            loadAllCourses();
        }
    });
    
    // Table selection listener
    courseTable.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            showCourseDetails();
        }
    });
}

private void loadPendingCourses() {
    tableModel.setRowCount(0);
    List<Course> courses = adminService.getPendingCourses();
    for (Course c : courses) {
        Object[] row = {
            c.getCourseID(),
            c.getCourseTitle(),
            c.getInstructorID(),
            c.getSubmissionDate() != null ? dateFormat.format(c.getSubmissionDate()) : "N/A",
            c.getApprovalStatus()
        };
        tableModel.addRow(row);
    }
}

private void loadApprovedCourses() {
    tableModel.setRowCount(0);
    List<Course> courses = adminService.getApprovedCourses();
    for (Course c : courses) {
        Object[] row = {
            c.getCourseID(),
            c.getCourseTitle(),
            c.getInstructorID(),
            c.getSubmissionDate() != null ? dateFormat.format(c.getSubmissionDate()) : "N/A",
            c.getApprovalStatus()
        };
        tableModel.addRow(row);
    }
}

private void loadRejectedCourses() {
    tableModel.setRowCount(0);
    List<Course> courses = adminService.getRejectedCourses();
    for (Course c : courses) {
        Object[] row = {
            c.getCourseID(),
            c.getCourseTitle(),
            c.getInstructorID(),
            c.getSubmissionDate() != null ? dateFormat.format(c.getSubmissionDate()) : "N/A",
            c.getApprovalStatus()
        };
        tableModel.addRow(row);
    }
}

private void loadAllCourses() {
    tableModel.setRowCount(0);
    List<Course> courses = adminService.getAllCourses();
    for (Course c : courses) {
        Object[] row = {
            c.getCourseID(),
            c.getCourseTitle(),
            c.getInstructorID(),
            c.getSubmissionDate() != null ? dateFormat.format(c.getSubmissionDate()) : "N/A",
            c.getApprovalStatus()
        };
        tableModel.addRow(row);
    }
}

private void showCourseDetails() {
    int row = courseTable.getSelectedRow();
    if (row == -1) {
        return;
    }
    
    int courseId = (int) tableModel.getValueAt(row, 0);
    Course course = adminService.getCourseById(courseId);
    
    if (course != null) {
        txtCourseID.setText(String.valueOf(course.getCourseID()));
        txtCourseTitle.setText(course.getCourseTitle());
        txtCourseDescription.setText(course.getCourseDescription());
        txtInstructor.setText("ID: " + course.getInstructorID());
        txtStatus.setText(course.getApprovalStatus().toString());
        txtSubmissionDate.setText(course.getSubmissionDate() != null ? 
            dateFormat.format(course.getSubmissionDate()) : "N/A");
    }
}

private void updateStats() {
    AdminService.AdminStatistics stats = adminService.getStatistics();
    lblTotalCourses.setText("Total: " + stats.totalCourses);
    lblPendingCourses.setText("Pending: " + stats.pendingCourses);
    lblApprovedCourses.setText("Approved: " + stats.approvedCourses);
    lblRejectedCourses.setText("Rejected: " + stats.rejectedCourses);
}

private void clearFields() {
    txtCourseID.setText("");
    txtCourseTitle.setText("");
    txtCourseDescription.setText("");
    txtInstructor.setText("");
    txtStatus.setText("");
    txtSubmissionDate.setText("");
}
private void setupTableInTabs() {
    // Remove the table from its current parent
    scrollPane.setViewportView(courseTable);
    
    // Add the scroll pane to the first tab (Pending)
    Pending.removeAll();
    Pending.setLayout(new java.awt.BorderLayout());
    Pending.add(scrollPane, java.awt.BorderLayout.CENTER);
    
    // Add listener to move table when tab changes
    tabbedpane.addChangeListener(e -> {
        int selectedIndex = tabbedpane.getSelectedIndex();
        javax.swing.JPanel selectedPanel = (javax.swing.JPanel) tabbedpane.getComponentAt(selectedIndex);
        selectedPanel.removeAll();
        selectedPanel.setLayout(new java.awt.BorderLayout());
        selectedPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
        selectedPanel.revalidate();
        selectedPanel.repaint();
    });
}  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedpane = new javax.swing.JTabbedPane();
        Pending = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Approved = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        courseTable = new javax.swing.JTable();
        Rejected = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        AllCourses = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        statsPanel = new javax.swing.JPanel();
        lblTotalCourses = new javax.swing.JLabel();
        lblPendingCourses = new javax.swing.JLabel();
        lblApprovedCourses = new javax.swing.JLabel();
        lblRejectedCourses = new javax.swing.JLabel();
        btnLogOut = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        CourseDetails = new javax.swing.JPanel();
        txtCourseID = new javax.swing.JTextField();
        txtCourseTitle = new javax.swing.JTextField();
        txtInstructor = new javax.swing.JTextField();
        txtStatus = new javax.swing.JTextField();
        txtSubmissionDate = new javax.swing.JTextField();
        txtCourseDescription = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        btnApprove = new javax.swing.JButton();
        btnReject = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 600));

        Pending.setName("Pending Courses"); // NOI18N

        javax.swing.GroupLayout PendingLayout = new javax.swing.GroupLayout(Pending);
        Pending.setLayout(PendingLayout);
        PendingLayout.setHorizontalGroup(
            PendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PendingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE))
        );
        PendingLayout.setVerticalGroup(
            PendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
        );

        tabbedpane.addTab("Pending Courses ", Pending);

        courseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollPane.setViewportView(courseTable);

        javax.swing.GroupLayout ApprovedLayout = new javax.swing.GroupLayout(Approved);
        Approved.setLayout(ApprovedLayout);
        ApprovedLayout.setHorizontalGroup(
            ApprovedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
        );
        ApprovedLayout.setVerticalGroup(
            ApprovedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ApprovedLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedpane.addTab("Approved Courses", Approved);

        javax.swing.GroupLayout RejectedLayout = new javax.swing.GroupLayout(Rejected);
        Rejected.setLayout(RejectedLayout);
        RejectedLayout.setHorizontalGroup(
            RejectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 546, Short.MAX_VALUE)
            .addGroup(RejectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RejectedLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        RejectedLayout.setVerticalGroup(
            RejectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 142, Short.MAX_VALUE)
            .addGroup(RejectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RejectedLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        tabbedpane.addTab("Rejected Courses ", Rejected);

        javax.swing.GroupLayout AllCoursesLayout = new javax.swing.GroupLayout(AllCourses);
        AllCourses.setLayout(AllCoursesLayout);
        AllCoursesLayout.setHorizontalGroup(
            AllCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
        );
        AllCoursesLayout.setVerticalGroup(
            AllCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AllCoursesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedpane.addTab("All Courses ", AllCourses);

        lblTotalCourses.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalCourses.setText("Total:0");

        lblPendingCourses.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPendingCourses.setText("Pending:0");

        lblApprovedCourses.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblApprovedCourses.setText("Approved:0");

        lblRejectedCourses.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRejectedCourses.setText("Rejected:0");

        btnLogOut.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogOut.setText("Log Out");
        btnLogOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogOutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout statsPanelLayout = new javax.swing.GroupLayout(statsPanel);
        statsPanel.setLayout(statsPanelLayout);
        statsPanelLayout.setHorizontalGroup(
            statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblApprovedCourses)
                    .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(statsPanelLayout.createSequentialGroup()
                            .addComponent(lblRejectedCourses)
                            .addGap(182, 182, 182))
                        .addGroup(statsPanelLayout.createSequentialGroup()
                            .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblTotalCourses)
                                .addComponent(lblPendingCourses))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLogOut)
                            .addGap(37, 37, 37)))))
        );
        statsPanelLayout.setVerticalGroup(
            statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statsPanelLayout.createSequentialGroup()
                        .addComponent(lblTotalCourses)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPendingCourses))
                    .addComponent(btnLogOut, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblApprovedCourses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRejectedCourses)
                .addContainerGap(75, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel2.setText("Admin Dashboard");

        txtCourseID.setEditable(false);

        txtCourseTitle.setEditable(false);

        txtInstructor.setEditable(false);
        txtInstructor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtInstructorActionPerformed(evt);
            }
        });

        txtStatus.setEditable(false);

        txtSubmissionDate.setEditable(false);

        txtCourseDescription.setEditable(false);
        txtCourseDescription.setColumns(20);
        txtCourseDescription.setRows(5);

        javax.swing.GroupLayout CourseDetailsLayout = new javax.swing.GroupLayout(CourseDetails);
        CourseDetails.setLayout(CourseDetailsLayout);
        CourseDetailsLayout.setHorizontalGroup(
            CourseDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CourseDetailsLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(txtCourseID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCourseTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtInstructor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtSubmissionDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(172, Short.MAX_VALUE))
            .addGroup(CourseDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CourseDetailsLayout.createSequentialGroup()
                    .addContainerGap(115, Short.MAX_VALUE)
                    .addComponent(txtCourseDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(21, Short.MAX_VALUE)))
        );
        CourseDetailsLayout.setVerticalGroup(
            CourseDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CourseDetailsLayout.createSequentialGroup()
                .addGroup(CourseDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCourseID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCourseTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInstructor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSubmissionDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(95, Short.MAX_VALUE))
            .addGroup(CourseDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CourseDetailsLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtCourseDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(27, Short.MAX_VALUE)))
        );

        btnApprove.setText("Approve");
        btnApprove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApproveActionPerformed(evt);
            }
        });

        btnReject.setText("Reject");
        btnReject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRejectActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(btnApprove)
                .addGap(18, 18, 18)
                .addComponent(btnReject)
                .addGap(18, 18, 18)
                .addComponent(btnRefresh)
                .addGap(0, 21, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnApprove)
                    .addComponent(btnReject)
                    .addComponent(btnRefresh))
                .addGap(33, 33, 33))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(tabbedpane))
                        .addGap(18, 18, 18)
                        .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(CourseDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14))))
            .addGroup(layout.createSequentialGroup()
                .addGap(242, 242, 242)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabbedpane, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CourseDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(96, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName("AdminDashboardFrame");

        pack();
    }// </editor-fold>//GEN-END:initComponents
private JTable getCurrentTable(){
    return courseTable;
}
private void setupCourseDetailsPanel() {
    // Clear existing layout issues
    CourseDetails.removeAll();
    CourseDetails.setLayout(null); // Use absolute layout for full control
    CourseDetails.setBorder(javax.swing.BorderFactory.createTitledBorder("Course Details"));
    
    int labelX = 10;
    int fieldX = 120;
    int y = 25;
    int gap = 35;
    
    // Course ID
    javax.swing.JLabel lblID = new javax.swing.JLabel("Course ID:");
    lblID.setBounds(labelX, y, 100, 20);
    CourseDetails.add(lblID);
    txtCourseID.setBounds(fieldX, y, 150, 25);
    CourseDetails.add(txtCourseID);
    y += gap;
    
    // Title
    javax.swing.JLabel lblTitle = new javax.swing.JLabel("Title:");
    lblTitle.setBounds(labelX, y, 100, 20);
    CourseDetails.add(lblTitle);
    txtCourseTitle.setBounds(fieldX, y, 300, 25);
    CourseDetails.add(txtCourseTitle);
    y += gap;
    
    // Description - NO SCROLLING
    javax.swing.JLabel lblDesc = new javax.swing.JLabel("Description:");
    lblDesc.setBounds(labelX, y, 100, 20);
    CourseDetails.add(lblDesc);
    
    txtCourseDescription.setLineWrap(true);
    txtCourseDescription.setWrapStyleWord(true);
    txtCourseDescription.setBounds(fieldX, y, 350, 100);
    CourseDetails.add(txtCourseDescription);
    y += 110;
    
    // Instructor
    javax.swing.JLabel lblInst = new javax.swing.JLabel("Instructor:");
    lblInst.setBounds(labelX, y, 100, 20);
    CourseDetails.add(lblInst);
    txtInstructor.setBounds(fieldX, y, 200, 25);
    CourseDetails.add(txtInstructor);
    y += gap;
    
    // Status
    javax.swing.JLabel lblStatus = new javax.swing.JLabel("Status:");
    lblStatus.setBounds(labelX, y, 100, 20);
    CourseDetails.add(lblStatus);
    txtStatus.setBounds(fieldX, y, 150, 25);
    CourseDetails.add(txtStatus);
    y += gap;
    
    // Submission Date
    javax.swing.JLabel lblDate = new javax.swing.JLabel("Submission Date:");
    lblDate.setBounds(labelX, y, 100, 20);
    CourseDetails.add(lblDate);
    txtSubmissionDate.setBounds(fieldX, y, 180, 25);
    CourseDetails.add(txtSubmissionDate);
    
    // Make panel size appropriate
    CourseDetails.setPreferredSize(new Dimension(600, 320));
    CourseDetails.revalidate();
    CourseDetails.repaint();
}

private void addField(String label, javax.swing.JTextField field, int y, int width) {
    javax.swing.JLabel lbl = new javax.swing.JLabel(label);
    lbl.setBounds(10, y, 100, 20);
    field.setBounds(120, y, width, 25);
    CourseDetails.add(lbl);
    CourseDetails.add(field);
}
    private void btnRejectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRejectActionPerformed
       int row = courseTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a course first!");
        return;
    }
    
    int courseId = (int) tableModel.getValueAt(row, 0);
    String title = (String) tableModel.getValueAt(row, 1);
    
    String reason = JOptionPane.showInputDialog(this,
        "Enter rejection reason for: " + title,
        "Reject Course",
        JOptionPane.PLAIN_MESSAGE);
    
    if (reason != null && !reason.trim().isEmpty()) {
if (admin==null){
JOptionPane.showMessageDialog(this,"Error admin not logged in");
return;
}
        boolean success = adminService.rejectCourse(String.valueOf(courseId), reason,admin.getUsername());
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Course rejected.");
int tab=tabbedpane.getSelectedIndex();
if (tab==0)loadPendingCourses();
else if (tab==1)loadApprovedCourses();
else if (tab==2)loadRejectedCourses();
else 
            loadPendingCourses();
            updateStats();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reject course.");
        }
    } else if (reason != null) {
        JOptionPane.showMessageDialog(this, "Reason cannot be empty!");
    }
    }//GEN-LAST:event_btnRejectActionPerformed

    private void btnApproveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApproveActionPerformed

     int row = courseTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a course first!");
        return;
    }
    
    int courseId = (int) tableModel.getValueAt(row, 0);
    String title = (String) tableModel.getValueAt(row, 1);
    
    int choice = JOptionPane.showConfirmDialog(this,
        "Do you want to approve this course?\n" + title,
        "Approve Course",
        JOptionPane.YES_NO_OPTION);
    
    if (choice == JOptionPane.YES_OPTION) {
if (admin==null){
JOptionPane.showMessageDialog(this,"Error admin not logged in");
return;
}
        boolean success = adminService.approveCourse(String.valueOf(courseId),admin.getUsername());
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Course approved!");
        int tab =tabbedpane.getSelectedIndex();
if (tab==0)loadPendingCourses();
else if (tab==1)loadApprovedCourses();
else if (tab==2)loadRejectedCourses();
else 
            loadPendingCourses();
            updateStats();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to approve course.");
        }
    }
                                    
    }//GEN-LAST:event_btnApproveActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed

    int tab = tabbedpane.getSelectedIndex();
    if (tab == 0) loadPendingCourses();
    else if (tab == 1) loadApprovedCourses();
    else if (tab == 2) loadRejectedCourses();
    else loadAllCourses();
    updateStats();
    JOptionPane.showMessageDialog(this, "Refreshed!");

    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnLogOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogOutActionPerformed
            new LoginFrame().setVisible(true);
            this.dispose();
    }//GEN-LAST:event_btnLogOutActionPerformed

    private void txtInstructorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInstructorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtInstructorActionPerformed

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
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboardFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AllCourses;
    private javax.swing.JPanel Approved;
    private javax.swing.JPanel CourseDetails;
    private javax.swing.JPanel Pending;
    private javax.swing.JPanel Rejected;
    private javax.swing.JButton btnApprove;
    private javax.swing.JButton btnLogOut;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReject;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JTable courseTable;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblApprovedCourses;
    private javax.swing.JLabel lblPendingCourses;
    private javax.swing.JLabel lblRejectedCourses;
    private javax.swing.JLabel lblTotalCourses;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JTabbedPane tabbedpane;
    private javax.swing.JTextArea txtCourseDescription;
    private javax.swing.JTextField txtCourseID;
    private javax.swing.JTextField txtCourseTitle;
    private javax.swing.JTextField txtInstructor;
    private javax.swing.JTextField txtStatus;
    private javax.swing.JTextField txtSubmissionDate;
    // End of variables declaration//GEN-END:variables
}
