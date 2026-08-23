package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;

@WebServlet("/StudentServlet")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // =================================================
        // SESSION & ROLE CHECK
        // =================================================
        if (session == null) {
            response.sendRedirect("index.jsp?error=session");
            return;
        }

        String role = String.valueOf(session.getAttribute("role"));
        if (!"STUDENT".equalsIgnoreCase(role)) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        Object userIdObject = session.getAttribute("userId");
        if (userIdObject == null) {
            session.invalidate();
            response.sendRedirect("index.jsp?error=session");
            return;
        }

        int userId = (Integer) userIdObject;

        try (Connection con = DBConnection.getConnection()) {
            
            // 1. Fetch Student Core Info
            int studentId = 0;
            String department = "";
            String semester = "";
            String firstName = "";
            String lastName = "";
            String email = "";
            String mobile = "";
            String username = "";
            String status = "";
            java.sql.Timestamp createdAt = null;

            String sql = "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, u.mobile, u.status, u.created_at, " +
                         "s.student_id, s.department, s.semester " +
                         "FROM users u INNER JOIN students s ON u.user_id = s.user_id " +
                         "WHERE u.user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        department = rs.getString("department");
                        semester = rs.getString("semester");
                        firstName = rs.getString("first_name");
                        lastName = rs.getString("last_name");
                        email = rs.getString("email");
                        mobile = rs.getString("mobile");
                        username = rs.getString("username");
                        status = rs.getString("status");
                        createdAt = rs.getTimestamp("created_at");
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student profile not found.");
                        return;
                    }
                }
            }

            // Set profile attributes
            request.setAttribute("studentUserId", userId);
            request.setAttribute("studentId", studentId);
            request.setAttribute("username", username);
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("email", email);
            request.setAttribute("mobile", mobile);
            request.setAttribute("status", status);
            request.setAttribute("createdAt", createdAt);
            request.setAttribute("department", department);
            request.setAttribute("semester", semester);

            // =================================================
            // 2. DYNAMIC DASHBOARD STATS CALCULATIONS
            // =================================================

            // Stat 1: Attendance Percentage
            double attendanceRate = 100.0;
            int totalClasses = 0;
            int presentClasses = 0;
            String attSql = "SELECT COUNT(*), SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) FROM attendance WHERE student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(attSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalClasses = rs.getInt(1);
                        presentClasses = rs.getInt(2);
                        if (totalClasses > 0) {
                            attendanceRate = ((double) presentClasses / totalClasses) * 100;
                        }
                    }
                }
            }
            request.setAttribute("attendanceRate", String.format("%.1f", attendanceRate));
            request.setAttribute("presentClasses", presentClasses);
            request.setAttribute("totalClasses", totalClasses);

            // Stat 2: Assignments Count (Submitted / Total for Department)
            int totalAssignments = 0;
            int submittedAssignments = 0;
            String assignTotalSql = "SELECT COUNT(*) FROM assignments a INNER JOIN teachers t ON a.teacher_id = t.teacher_id WHERE t.department = ?";
            try (PreparedStatement ps = con.prepareStatement(assignTotalSql)) {
                ps.setString(1, department);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalAssignments = rs.getInt(1);
                    }
                }
            }
            String assignSubSql = "SELECT COUNT(*) FROM assignment_submissions WHERE student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(assignSubSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        submittedAssignments = rs.getInt(1);
                    }
                }
            }
            request.setAttribute("totalAssignments", totalAssignments);
            request.setAttribute("submittedAssignments", submittedAssignments);

            // Stat 3: Class Tests Count (Graded / Total for Department)
            int totalTests = 0;
            int completedTests = 0;
            String testTotalSql = "SELECT COUNT(*) FROM class_tests ct INNER JOIN teachers t ON ct.teacher_id = t.teacher_id WHERE t.department = ?";
            try (PreparedStatement ps = con.prepareStatement(testTotalSql)) {
                ps.setString(1, department);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalTests = rs.getInt(1);
                    }
                }
            }
            String testSubSql = "SELECT COUNT(*) FROM test_marks WHERE student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(testSubSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        completedTests = rs.getInt(1);
                    }
                }
            }
            request.setAttribute("totalTests", totalTests);
            request.setAttribute("completedTests", completedTests);

            // Stat 4: Overall Performance Calculation
            double assignmentAvg = 0.0;
            int gradedAssignments = 0;
            String assignAvgSql = "SELECT COUNT(*), AVG(marks_obtained) FROM assignment_submissions WHERE student_id = ? AND marks_obtained IS NOT NULL";
            try (PreparedStatement ps = con.prepareStatement(assignAvgSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gradedAssignments = rs.getInt(1);
                        assignmentAvg = rs.getDouble(2); // In % already (out of 100)
                    }
                }
            }
            if (gradedAssignments == 0) {
                assignmentAvg = 100.0; // Assume full start if no assignments graded yet
            }

            double testAvg = 0.0;
            int gradedTests = 0;
            String testAvgSql = "SELECT COUNT(*), AVG(m.marks_obtained / ct.total_marks) * 100 FROM test_marks m INNER JOIN class_tests ct ON m.test_id = ct.test_id WHERE m.student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(testAvgSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gradedTests = rs.getInt(1);
                        testAvg = rs.getDouble(2);
                    }
                }
            }
            if (gradedTests == 0) {
                testAvg = 100.0;
            }

            double overallPerformance = (attendanceRate + assignmentAvg + testAvg) / 3.0;
            request.setAttribute("overallPerformance", String.format("%.1f", overallPerformance));

            // =================================================
            // 3. UPCOMING ACTIVITIES (Limit 3)
            // =================================================
            List<Map<String, String>> upcomingList = new ArrayList<>();
            String upcomingSql = 
                "SELECT 'Assignment' AS type, title, subject, due_date AS date FROM assignments a " +
                "INNER JOIN teachers t ON a.teacher_id = t.teacher_id " +
                "WHERE t.department = ? AND a.due_date >= CURDATE() AND a.status = 'ACTIVE' " +
                "UNION ALL " +
                "SELECT 'Class Test' AS type, test_title AS title, subject, test_date AS date FROM class_tests ct " +
                "INNER JOIN teachers t ON ct.teacher_id = t.teacher_id " +
                "WHERE t.department = ? AND ct.test_date >= CURDATE() " +
                "ORDER BY date ASC LIMIT 3";
            try (PreparedStatement ps = con.prepareStatement(upcomingSql)) {
                ps.setString(1, department);
                ps.setString(2, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> item = new HashMap<>();
                        item.put("type", rs.getString("type"));
                        item.put("title", rs.getString("title"));
                        item.put("subject", rs.getString("subject"));
                        item.put("date", String.valueOf(rs.getDate("date")));
                        upcomingList.add(item);
                    }
                }
            }
            request.setAttribute("upcomingActivities", upcomingList);

            // =================================================
            // 4. RECENT ACTIVITY FEED (Limit 5)
            // =================================================
            List<String> recentActivities = new ArrayList<>();
            
            // Query 1: Graded Assignments
            String recentGradedAssignSql = 
                "SELECT a.title, sub.marks_obtained, sub.submission_date FROM assignment_submissions sub " +
                "INNER JOIN assignments a ON sub.assignment_id = a.assignment_id " +
                "WHERE sub.student_id = ? AND sub.marks_obtained IS NOT NULL " +
                "ORDER BY sub.submission_date DESC LIMIT 3";
            try (PreparedStatement ps = con.prepareStatement(recentGradedAssignSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        recentActivities.add("★ Assignment graded: " + rs.getString("title") + " (Marks: " + rs.getDouble("marks_obtained") + ")");
                    }
                }
            }

            // Query 2: Submissions
            String recentSubmissionsSql = 
                "SELECT a.title, sub.submission_date FROM assignment_submissions sub " +
                "INNER JOIN assignments a ON sub.assignment_id = a.assignment_id " +
                "WHERE sub.student_id = ? " +
                "ORDER BY sub.submission_date DESC LIMIT 3";
            try (PreparedStatement ps = con.prepareStatement(recentSubmissionsSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        recentActivities.add("✓ Assignment submitted: " + rs.getString("title"));
                    }
                }
            }

            // Query 3: New Study Materials Uploaded
            String newMaterialsSql = 
                "SELECT m.title, m.subject FROM study_materials m " +
                "INNER JOIN teachers t ON m.teacher_id = t.teacher_id " +
                "WHERE t.department = ? " +
                "ORDER BY m.upload_date DESC LIMIT 3";
            try (PreparedStatement ps = con.prepareStatement(newMaterialsSql)) {
                ps.setString(1, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        recentActivities.add("📚 New study material: " + rs.getString("title") + " (" + rs.getString("subject") + ")");
                    }
                }
            }

            // Cap recent activities to 5 items
            if (recentActivities.size() > 5) {
                recentActivities = recentActivities.subList(0, 5);
            }
            request.setAttribute("recentActivities", recentActivities);

            request.setAttribute("activePage", "dashboard");
            request.getRequestDispatcher("student.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load student details");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}