package ui;

public class InstructorInsightsPanel extends javax.swing.JPanel{

    private model.Instructor instructor;
    private database.JsonDatabaseManager db;
    private service.AnalyticsService analyticsService;
    private int selectedCourseId = -1;
    private javax.swing.table.DefaultTableModel tableModel;

public InstructorInsightsPanel(model.Instructor instructor, database.JsonDatabaseManager db) {
    
    this.instructor = instructor;
    this.db = db;
    this.analyticsService = new service.AnalyticsService(db);
    
    initComponents();
    setupTableModel();
    loadCourses();
    autoLoadFirstCourse();
}

private void setupTableModel() {
    String[] columnNames = {"Student Name", "Email", "Lessons Completed", "Progress %", "Status"};
    
    tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; 
        }
    };
    studentPerformanceTable.setModel(tableModel);
    
    studentPerformanceTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Name
    studentPerformanceTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Email
    studentPerformanceTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Lessons
    studentPerformanceTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Progress
    studentPerformanceTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
}
private void autoLoadFirstCourse() {
    if (courseComboBox.getItemCount() > 0) {
        String firstItem = courseComboBox.getItemAt(0);
        
        if (firstItem != null && !firstItem.equals("No courses available")) {
            courseComboBox.setSelectedIndex(0);
            onCourseSelected();
        } else {
            resetStatsCards();
        }
    } else {
        resetStatsCards();
    }
}
private void resetStatsCards() {
    lblTotalStudents.setText("0");
    lblAvgProgress.setText("0.0%");
    lblCompletionRate.setText("0.0%");
}
private void loadCourses() {
    courseComboBox.removeAllItems();
    
    java.util.List<model.Course> allCourses = db.loadCourses();
    int instructorId = Integer.parseInt(instructor.getUserId());
    
    boolean hasAnyCourse = false;
    for (model.Course course : allCourses) {
        if (course.getInstructorID() == instructorId) {
            courseComboBox.addItem(course.getCourseID() + " - " + course.getCourseTitle());
            hasAnyCourse = true;
        }
    }
    
    if (!hasAnyCourse) {
        courseComboBox.addItem("No courses available");
    }
}

public void onCourseSelected() {
    String selected = (String) courseComboBox.getSelectedItem();
    
    if (selected == null || selected.equals("No courses available") || selected.equals("Error loading courses")) {
        resetStatsCards();
        tableModel.setRowCount(0);
        return;
    }
    
    try {
        // Extract course ID from "ID - Title" format
        String[] parts = selected.split(" - ");
        selectedCourseId = Integer.parseInt(parts[0]);
        
        // Load analytics for this course
        loadAnalytics();
        
    } catch (NumberFormatException e) {
        javax.swing.JOptionPane.showMessageDialog(this,
            "Invalid course selection format",
            "Error",
            javax.swing.JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this,
            "Error loading course analytics: " + e.getMessage(),
            "Error",
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}


private void loadAnalytics() {
    if (selectedCourseId == -1) return;
    
    // Load course statistics
    model.CourseStatistics stats = analyticsService.getCourseStatistics(selectedCourseId);
    
    if (stats != null) {
        lblTotalStudents.setText(String.valueOf(stats.getTotalStudentsEnrolled()));
        lblAvgProgress.setText(String.format("%.1f%%", stats.getAverageProgress()));
        lblCompletionRate.setText(String.format("%.1f%%", stats.getCompletionRate()));
    }
    
    // Load student performance table
    tableModel.setRowCount(0);
    java.util.List<model.StudentPerformance> performances = 
        analyticsService.getStudentPerformanceList(selectedCourseId);
    
    for (model.StudentPerformance perf : performances) {
        Object[] row = {
            perf.getStudentName(),
            perf.getStudentEmail(),
            perf.getLessonsCompleted() + " / " + perf.getTotalLessons(),
            String.format("%.1f%%", perf.getCompletionPercentage()),
            perf.getStatus()
        };
        tableModel.addRow(row);
    }
}

private void refreshData() {
    loadCourses();
    if (selectedCourseId != -1) {
        loadAnalytics();
    }
    javax.swing.JOptionPane.showMessageDialog(this, 
        "Analytics refreshed!", 
        "Refreshed", 
        javax.swing.JOptionPane.INFORMATION_MESSAGE);
}

private void showLessonCompletionChart() {
    if (selectedCourseId == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Please select a course first!", 
            "No Course Selected", 
            javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    java.util.Map<String, Integer> data = analyticsService.getLessonCompletionStats(selectedCourseId);
    
    if (data.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "No data available for this course", 
            "No Data", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    // Convert Integer to Double for bar chart
    java.util.Map<String, Double> chartData = new java.util.LinkedHashMap<>();
    for (java.util.Map.Entry<String, Integer> entry : data.entrySet()) {
        chartData.put(entry.getKey(), entry.getValue().doubleValue());
    }
    
    ChartFrame chartFrame = new ChartFrame("Lesson Completion Statistics");
    chartFrame.displayBarChart(chartData, "Students Completed");
    chartFrame.setVisible(true);
}

private void showCompletionBreakdownChart() {
    if (selectedCourseId == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Please select a course first!", 
            "No Course Selected", 
            javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    java.util.Map<String, Integer> data = 
        analyticsService.getCourseCompletionBreakdown(selectedCourseId);
    
    ChartFrame chartFrame = new ChartFrame("Course Completion Breakdown");
    chartFrame.displayPieChart(data);
    chartFrame.setVisible(true);
}

private void showQuizScoresChart() {
    if (selectedCourseId == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Please select a course first!", 
            "No Course Selected", 
            javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    java.util.Map<String, Double> data = analyticsService.getQuizAveragesByLesson(selectedCourseId);
    
    if (data.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "No quiz data available yet.\nQuizzes will be implemented by Team Member 2.", 
            "Coming Soon", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    ChartFrame chartFrame = new ChartFrame("Average Quiz Scores by Lesson");
    chartFrame.displayBarChart(data, "Average Score");
    chartFrame.setVisible(true);
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bottomPanel = new javax.swing.JPanel();
        btnLessonChart = new javax.swing.JButton();
        btnBreakdownChart = new javax.swing.JButton();
        btnQuizChart = new javax.swing.JButton();
        topPanel = new javax.swing.JPanel();
        lblSelectCourse = new javax.swing.JLabel();
        courseComboBox = new javax.swing.JComboBox<>();
        btnRefresh = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        statsPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        card1Panel = new javax.swing.JPanel();
        lblCardTitle1 = new javax.swing.JLabel();
        lblTotalStudents = new javax.swing.JLabel();
        card2Panel = new javax.swing.JPanel();
        lblCardTitle2 = new javax.swing.JLabel();
        lblAvgProgress = new javax.swing.JLabel();
        card3Panel = new javax.swing.JPanel();
        lblCardTitle3 = new javax.swing.JLabel();
        lblCompletionRate = new javax.swing.JLabel();
        tablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        studentPerformanceTable = new javax.swing.JTable();

        btnLessonChart.setBackground(new java.awt.Color(0, 153, 255));
        btnLessonChart.setFont(new java.awt.Font("Helvetica Neue", 0, 15)); // NOI18N
        btnLessonChart.setForeground(new java.awt.Color(255, 255, 255));
        btnLessonChart.setText("Lesson Completion Chart");
        btnLessonChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLessonChartActionPerformed(evt);
            }
        });

        btnBreakdownChart.setBackground(new java.awt.Color(0, 102, 204));
        btnBreakdownChart.setFont(new java.awt.Font("Helvetica Neue", 0, 15)); // NOI18N
        btnBreakdownChart.setForeground(new java.awt.Color(255, 255, 255));
        btnBreakdownChart.setText("Completion Breakdown");
        btnBreakdownChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBreakdownChartActionPerformed(evt);
            }
        });

        btnQuizChart.setBackground(new java.awt.Color(51, 51, 255));
        btnQuizChart.setFont(new java.awt.Font("Helvetica Neue", 0, 15)); // NOI18N
        btnQuizChart.setForeground(new java.awt.Color(255, 255, 255));
        btnQuizChart.setText("Quiz Scores by Lesson");
        btnQuizChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuizChartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnLessonChart, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(btnBreakdownChart, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btnQuizChart, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addContainerGap())
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBreakdownChart, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLessonChart, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuizChart, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        lblSelectCourse.setFont(new java.awt.Font("Helvetica Neue", 1, 15)); // NOI18N
        lblSelectCourse.setText("Select Course:");

        courseComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                courseComboBoxActionPerformed(evt);
            }
        });

        btnRefresh.setFont(new java.awt.Font("Helvetica Neue", 0, 15)); // NOI18N
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblSelectCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(courseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnRefresh)
                .addGap(17, 17, 17))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSelectCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(courseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statsPanel.setLayout(new java.awt.GridLayout(1, 0));

        card1Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 255)));

        lblCardTitle1.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lblCardTitle1.setForeground(new java.awt.Color(0, 153, 255));
        lblCardTitle1.setText("Total Students");

        lblTotalStudents.setFont(new java.awt.Font("Helvetica Neue", 0, 16)); // NOI18N
        lblTotalStudents.setText("0");

        javax.swing.GroupLayout card1PanelLayout = new javax.swing.GroupLayout(card1Panel);
        card1Panel.setLayout(card1PanelLayout);
        card1PanelLayout.setHorizontalGroup(
            card1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1PanelLayout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addGroup(card1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card1PanelLayout.createSequentialGroup()
                        .addComponent(lblCardTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card1PanelLayout.createSequentialGroup()
                        .addComponent(lblTotalStudents, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(79, 79, 79))))
        );
        card1PanelLayout.setVerticalGroup(
            card1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCardTitle1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalStudents)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        card2Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 204)));

        lblCardTitle2.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lblCardTitle2.setForeground(new java.awt.Color(0, 102, 204));
        lblCardTitle2.setText("Average Progress");

        lblAvgProgress.setFont(new java.awt.Font("Helvetica Neue", 0, 16)); // NOI18N
        lblAvgProgress.setText("0");

        javax.swing.GroupLayout card2PanelLayout = new javax.swing.GroupLayout(card2Panel);
        card2Panel.setLayout(card2PanelLayout);
        card2PanelLayout.setHorizontalGroup(
            card2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card2PanelLayout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addGroup(card2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card2PanelLayout.createSequentialGroup()
                        .addComponent(lblAvgProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card2PanelLayout.createSequentialGroup()
                        .addComponent(lblCardTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40))))
        );
        card2PanelLayout.setVerticalGroup(
            card2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card2PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCardTitle2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAvgProgress)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        card3Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255)));

        lblCardTitle3.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lblCardTitle3.setForeground(new java.awt.Color(0, 51, 255));
        lblCardTitle3.setText("Completion Rate");

        lblCompletionRate.setFont(new java.awt.Font("Helvetica Neue", 0, 16)); // NOI18N
        lblCompletionRate.setText("0");

        javax.swing.GroupLayout card3PanelLayout = new javax.swing.GroupLayout(card3Panel);
        card3Panel.setLayout(card3PanelLayout);
        card3PanelLayout.setHorizontalGroup(
            card3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, card3PanelLayout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addGroup(card3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblCompletionRate, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCardTitle3, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40))
        );
        card3PanelLayout.setVerticalGroup(
            card3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCardTitle3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCompletionRate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(31, 31, 31))
        );

        studentPerformanceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Student Name", "Email", "Lessons Completed", "Progress %", "Status"
            }
        ));
        jScrollPane1.setViewportView(studentPerformanceTable);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(card1Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(card2Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(card3Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(card1Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(card2Panel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(card3Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        statsPanel.add(jPanel1);

        javax.swing.GroupLayout centerPanelLayout = new javax.swing.GroupLayout(centerPanel);
        centerPanel.setLayout(centerPanelLayout);
        centerPanelLayout.setHorizontalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        centerPanelLayout.setVerticalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(centerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLessonChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLessonChartActionPerformed
        showLessonCompletionChart();
    }//GEN-LAST:event_btnLessonChartActionPerformed

    private void btnBreakdownChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBreakdownChartActionPerformed
        showCompletionBreakdownChart();
    }//GEN-LAST:event_btnBreakdownChartActionPerformed

    private void btnQuizChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuizChartActionPerformed
        showQuizScoresChart();
    }//GEN-LAST:event_btnQuizChartActionPerformed

    private void courseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_courseComboBoxActionPerformed
        onCourseSelected();
    }//GEN-LAST:event_courseComboBoxActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshData();
    }//GEN-LAST:event_btnRefreshActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton btnBreakdownChart;
    private javax.swing.JButton btnLessonChart;
    private javax.swing.JButton btnQuizChart;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel card1Panel;
    private javax.swing.JPanel card2Panel;
    private javax.swing.JPanel card3Panel;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JComboBox<String> courseComboBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAvgProgress;
    private javax.swing.JLabel lblCardTitle1;
    private javax.swing.JLabel lblCardTitle2;
    private javax.swing.JLabel lblCardTitle3;
    private javax.swing.JLabel lblCompletionRate;
    private javax.swing.JLabel lblSelectCourse;
    private javax.swing.JLabel lblTotalStudents;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JTable studentPerformanceTable;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
