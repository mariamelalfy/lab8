package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ChartFrame extends JFrame {
    
    // The main panel where charts will be drawn
    private JPanel chartPanel;
    
    // The title shown at the top of the chart
    private String chartTitle;
    
    public ChartFrame(String title) {
        this.chartTitle = title;
        
        // Set up the window
        setTitle(title);
        setSize(800, 600);  
        setLocationRelativeTo(null);  // Center the window on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Close only this window, not entire app
        
        // Create the main panel where we'll draw charts
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        
        // Add the panel to the window
        add(chartPanel);
    }
    
    public void displayBarChart(Map<String, Double> data, String yAxisLabel) {
        // Clear any existing chart
        chartPanel.removeAll();
        
        // Create title section
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(chartTitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        
        // Create the actual bar chart
        BarChartPanel barChart = new BarChartPanel(data, yAxisLabel);
        
        // Add both to the panel
        chartPanel.add(titlePanel, BorderLayout.NORTH);  // Title at top
        chartPanel.add(barChart, BorderLayout.CENTER);   // Chart in center
        
        // Refresh the display
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    public void displayPieChart(Map<String, Integer> data) {
        // Clear any existing chart
        chartPanel.removeAll();
        
        // Create title section
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(chartTitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        
        // Create the actual pie chart
        PieChartPanel pieChart = new PieChartPanel(data);
        
        // Add both to the panel
        chartPanel.add(titlePanel, BorderLayout.NORTH);  // Title at top
        chartPanel.add(pieChart, BorderLayout.CENTER);   // Chart in center
        
        // Refresh the display
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    /**
     * BarChartPanel - Inner class that draws vertical bar charts
     */
    private class BarChartPanel extends JPanel {
        private Map<String, Double> data;  // The data to display
        private String yAxisLabel;          // Label for vertical axis
        
        // Maximum number of students to show on Y-axis
        private static final double MAX_STUDENTS = 100.0;
        
        public BarChartPanel(Map<String, Double> data, String yAxisLabel) {
            this.data = data;
            this.yAxisLabel = yAxisLabel;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Convert to Graphics2D for better drawing quality
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Check if we have data to display
            if (data == null || data.isEmpty()) {
                g2d.drawString("No data available", getWidth()/2 - 50, getHeight()/2);
                return;
            }
            
            // Calculate dimensions
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int numberOfBars = data.size();
            int widthPerBar = (panelWidth - 100) / numberOfBars;
            int maxBarHeight = panelHeight - 100;  // Leave space for labels
            
            // Draw the Y-axis (vertical line on left)
            g2d.setColor(Color.BLACK);
            g2d.drawLine(50, 20, 50, panelHeight - 50);
            g2d.drawString(yAxisLabel, 5, 15);
            
            // Draw Y-axis labels (0, 4, 8, 12, 16, 20)
            for (int i = 0; i <= 5; i++) {
                int yPosition = panelHeight - 50 - (maxBarHeight * i / 5);
                int labelValue = (int)(MAX_STUDENTS * i / 5);
                
                // Draw the number
                g2d.drawString(String.valueOf(labelValue), 10, yPosition + 5);
                
                // Draw horizontal grid line
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(50, yPosition, panelWidth - 20, yPosition);
                g2d.setColor(Color.BLACK);
            }
            
            // Draw the X-axis (horizontal line at bottom)
            g2d.drawLine(50, panelHeight - 50, panelWidth - 20, panelHeight - 50);
            
            // Define colors for the bars (cycles through these colors)
            Color[] barColors = {
                new Color(52, 152, 219),   // Blue
                new Color(46, 204, 113),   // Green
                new Color(155, 89, 182),   // Purple
                new Color(241, 196, 15),   // Yellow
                new Color(231, 76, 60),    // Red
                new Color(26, 188, 156)    // Teal
            };
            
            // Draw each bar
            int xPosition = 60;  // Start position for first bar
            int colorIndex = 0;
            
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                String lessonName = entry.getKey();
                double studentCount = entry.getValue();
                
                // Cap the display value at MAX_STUDENTS (but show actual number)
                double displayValue = Math.min(studentCount, MAX_STUDENTS);
                
                // Calculate bar height based on value
                int barHeight = (int) ((displayValue / MAX_STUDENTS) * maxBarHeight);
                int barTopY = panelHeight - 50 - barHeight;
                
                // Draw the colored bar
                g2d.setColor(barColors[colorIndex % barColors.length]);
                g2d.fillRect(xPosition, barTopY, widthPerBar - 10, barHeight);
                
                // Draw the value on top of the bar
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                String valueText = String.format("%.0f", studentCount);
                g2d.drawString(valueText, xPosition + (widthPerBar - 10) / 2 - 10, barTopY - 5);
                
                // Draw the lesson name below the bar
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                
                // Shorten long lesson names
                String displayName = lessonName;
                if (displayName.length() > 12) {
                    displayName = displayName.substring(0, 10) + "..";
                }
                
                int labelX = xPosition + (widthPerBar - 10) / 2 - (displayName.length() * 3);
                g2d.drawString(displayName, labelX, panelHeight - 30);
                
                // Move to next bar position
                xPosition += widthPerBar;
                colorIndex++;
            }
        }
    }
    
    /**
     * PieChartPanel - Inner class that draws pie charts
     */
    private class PieChartPanel extends JPanel {
        private Map<String, Integer> data;  // The data to display
        
        public PieChartPanel(Map<String, Integer> data) {
            this.data = data;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Convert to Graphics2D for better drawing quality
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Check if we have data to display
            if (data == null || data.isEmpty()) {
                g2d.drawString("No data available", getWidth()/2 - 50, getHeight()/2);
                return;
            }
            
            // Calculate total number of students
            int totalStudents = 0;
            for (int count : data.values()) {
                totalStudents += count;
            }
            
            if (totalStudents == 0) {
                g2d.drawString("No students enrolled", getWidth()/2 - 60, getHeight()/2);
                return;
            }
            
            // Calculate circle dimensions
            int circleDiameter = Math.min(getWidth(), getHeight()) - 150;
            int circleX = (getWidth() - circleDiameter) / 2;
            int circleY = 50;
            
            // Define specific colors for each status
            Map<String, Color> statusColors = new java.util.HashMap<>();
            statusColors.put("Completed", new Color(46, 204, 113));      // Green
            statusColors.put("In Progress", new Color(52, 152, 219));    // Blue
            statusColors.put("Not Started", new Color(231, 76, 60));     // Red
            
            // Backup colors in case of unexpected categories
            Color[] backupColors = {
                new Color(155, 89, 182),   // Purple
                new Color(26, 188, 156),   // Teal
                new Color(230, 126, 34)    // Orange
            };
            
            // Draw each slice of the pie
            int startAngle = 0;  // Start at the top (0 degrees)
            int backupColorIndex = 0;
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String status = entry.getKey();
                int studentCount = entry.getValue();
                
                // Skip categories with no students
                if (studentCount == 0) continue;
                
                // Calculate the angle for this slice
                // 360 degrees total, divided proportionally
                int sliceAngle = (int) Math.round((studentCount * 360.0) / totalStudents);
                
                // Get the color for this status
                Color sliceColor = statusColors.getOrDefault(status, 
                    backupColors[backupColorIndex++ % backupColors.length]);
                
                // Draw the colored slice
                g2d.setColor(sliceColor);
                g2d.fillArc(circleX, circleY, circleDiameter, circleDiameter, 
                           startAngle, sliceAngle);
                
                // Draw white border around slice
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawArc(circleX, circleY, circleDiameter, circleDiameter, 
                           startAngle, sliceAngle);
                
                // Move to next slice
                startAngle += sliceAngle;
            }
            
            // Draw legend below the pie chart
            int legendY = circleY + circleDiameter + 30;
            int legendX = circleX;
            backupColorIndex = 0;
            
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String status = entry.getKey();
                int studentCount = entry.getValue();
                
                // Calculate percentage
                double percentage = (studentCount * 100.0) / totalStudents;
                
                // Get the matching color
                Color legendColor = statusColors.getOrDefault(status, 
                    backupColors[backupColorIndex++ % backupColors.length]);
                
                // Draw colored box
                g2d.setColor(legendColor);
                g2d.fillRect(legendX, legendY, 20, 20);
                
                // Draw text: "Completed: 5 (50.0%)"
                g2d.setColor(Color.BLACK);
                String legendText = String.format("%s: %d (%.1f%%)", 
                                                  status, studentCount, percentage);
                g2d.drawString(legendText, legendX + 30, legendY + 15);
                
                // Move to next legend item
                legendX += 190;
            }
        }
    }
}
