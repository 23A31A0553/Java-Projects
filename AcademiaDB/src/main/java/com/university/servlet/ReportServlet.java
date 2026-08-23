package com.university.servlet;

import com.university.db.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/ReportServlet")
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("index.jsp?error=session");
            return;
        }

        String role = (String) session.getAttribute("role");
        if ("ADMIN".equalsIgnoreCase(role)) {
            loadAdminReports(request, response);
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            loadTeacherReports(request, response);
        } else {
            response.sendRedirect("index.jsp?error=unauthorized");
        }
    }

    // =====================================================
    // LOAD TEACHER REPORTS
    // =====================================================
    private void loadTeacherReports(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String type = request.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            type = "student"; // Default
        }

        try (Connection con = DBConnection.getConnection()) {
            // Get teacher details
            int teacherId = 0;
            String teacherDept = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id, department FROM teachers WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teacherId = rs.getInt("teacher_id");
                        teacherDept = rs.getString("department");
                    }
                }
            }

            request.setAttribute("teacherDept", teacherDept);
            request.setAttribute("reportType", type);

            if ("student".equalsIgnoreCase(type)) {
                // List of all students in teacher's department
                List<Map<String, Object>> students = new ArrayList<>();
                String sql = "SELECT s.student_id, u.first_name, u.last_name, u.username, u.email, s.semester, u.status " +
                             "FROM students s INNER JOIN users u ON s.user_id = u.user_id " +
                             "WHERE s.department = ? ORDER BY u.first_name ASC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, teacherDept);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("studentId", rs.getInt("student_id"));
                            map.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                            map.put("username", rs.getString("username"));
                            map.put("email", rs.getString("email"));
                            map.put("semester", rs.getString("semester"));
                            map.put("status", rs.getString("status"));
                            students.add(map);
                        }
                    }
                }
                request.setAttribute("reportData", students);

            } else if ("attendance".equalsIgnoreCase(type)) {
                // List of students with present count and percentage
                List<Map<String, Object>> attendanceList = new ArrayList<>();
                String sql = 
                    "SELECT s.student_id, u.first_name, u.last_name, " +
                    "COUNT(a.attendance_id) AS total_classes, " +
                    "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count " +
                    "FROM students s INNER JOIN users u ON s.user_id = u.user_id " +
                    "LEFT JOIN attendance a ON s.student_id = a.student_id " +
                    "WHERE s.department = ? " +
                    "GROUP BY s.student_id, u.first_name, u.last_name ORDER BY u.first_name ASC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, teacherDept);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("studentId", rs.getInt("student_id"));
                            map.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                            int total = rs.getInt("total_classes");
                            int present = rs.getInt("present_count");
                            map.put("total", total);
                            map.put("present", present);
                            map.put("absent", total - present);
                            double pct = total > 0 ? (((double) present / total) * 100) : 100.0;
                            map.put("percentage", String.format("%.1f", pct));
                            attendanceList.add(map);
                        }
                    }
                }
                request.setAttribute("reportData", attendanceList);

            } else if ("assignment".equalsIgnoreCase(type)) {
                // List of assignments, submitted count, pending count
                List<Map<String, Object>> assignments = new ArrayList<>();
                String sql = 
                    "SELECT a.assignment_id, a.title, a.subject, a.due_date, " +
                    "(SELECT COUNT(*) FROM assignment_submissions sub WHERE sub.assignment_id = a.assignment_id) AS submitted_count, " +
                    "(SELECT COUNT(*) FROM students s WHERE s.department = ?) AS total_count " +
                    "FROM assignments a WHERE a.teacher_id = ? ORDER BY a.assignment_id DESC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, teacherDept);
                    ps.setInt(2, teacherId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("assignmentId", rs.getInt("assignment_id"));
                            map.put("title", rs.getString("title"));
                            map.put("subject", rs.getString("subject"));
                            map.put("dueDate", rs.getDate("due_date"));
                            int subCount = rs.getInt("submitted_count");
                            int totCount = rs.getInt("total_count");
                            map.put("submitted", subCount);
                            map.put("pending", Math.max(0, totCount - subCount));
                            assignments.add(map);
                        }
                    }
                }
                request.setAttribute("reportData", assignments);

            } else if ("test".equalsIgnoreCase(type)) {
                // List of class tests with average/highest/lowest score
                List<Map<String, Object>> tests = new ArrayList<>();
                String sql = "SELECT test_id, test_title, subject, total_marks, test_date FROM class_tests WHERE teacher_id = ? ORDER BY test_id DESC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, teacherId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int testId = rs.getInt("test_id");
                            int totalMarks = rs.getInt("total_marks");

                            Map<String, Object> map = new HashMap<>();
                            map.put("testId", testId);
                            map.put("title", rs.getString("test_title"));
                            map.put("subject", rs.getString("subject"));
                            map.put("totalMarks", totalMarks);
                            map.put("testDate", rs.getDate("test_date"));

                            // Stats for test
                            double avg = 0.0;
                            double max = 0.0;
                            double min = totalMarks;
                            int passCount = 0;
                            int totalGraded = 0;

                            try (PreparedStatement statPs = con.prepareStatement("SELECT marks_obtained FROM test_marks WHERE test_id = ?")) {
                                statPs.setInt(1, testId);
                                try (ResultSet statRs = statPs.executeQuery()) {
                                    while (statRs.next()) {
                                        totalGraded++;
                                        double score = statRs.getDouble("marks_obtained");
                                        avg += score;
                                        if (score > max) max = score;
                                        if (score < min) min = score;
                                        if (score >= (totalMarks * 0.40)) {
                                            passCount++;
                                        }
                                    }
                                }
                            }

                            if (totalGraded > 0) {
                                map.put("average", String.format("%.1f", avg / totalGraded));
                                map.put("highest", String.format("%.1f", max));
                                map.put("lowest", String.format("%.1f", min));
                                map.put("passPercentage", String.format("%.1f", ((double) passCount / totalGraded) * 100));
                            } else {
                                map.put("average", "0.0");
                                map.put("highest", "0.0");
                                map.put("lowest", "0.0");
                                map.put("passPercentage", "0.0");
                            }
                            map.put("graded", totalGraded);

                            tests.add(map);
                        }
                    }
                }
                request.setAttribute("reportData", tests);

            } else if ("performance".equalsIgnoreCase(type)) {
                // Student Performance summary rating
                List<Map<String, Object>> list = new ArrayList<>();
                String sql = "SELECT s.student_id, u.first_name, u.last_name, u.username FROM students s INNER JOIN users u ON s.user_id = u.user_id WHERE s.department = ? ORDER BY u.first_name ASC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, teacherDept);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int studentId = rs.getInt("student_id");
                            Map<String, Object> map = new HashMap<>();
                            map.put("studentId", studentId);
                            map.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                            map.put("username", rs.getString("username"));

                            // Calculations
                            double attendanceRate = 100.0;
                            int totalC = 0, presentC = 0;
                            try (PreparedStatement attPs = con.prepareStatement("SELECT COUNT(*), SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) FROM attendance WHERE student_id = ?")) {
                                attPs.setInt(1, studentId);
                                try (ResultSet attRs = attPs.executeQuery()) {
                                    if (attRs.next()) {
                                        totalC = attRs.getInt(1);
                                        presentC = attRs.getInt(2);
                                        if (totalC > 0) {
                                            attendanceRate = ((double) presentC / totalC) * 100;
                                        }
                                    }
                                }
                            }

                            double assignmentAvg = 100.0;
                            try (PreparedStatement assPs = con.prepareStatement("SELECT AVG(marks_obtained) FROM assignment_submissions sub INNER JOIN assignments a ON sub.assignment_id = a.assignment_id WHERE sub.student_id = ? AND a.teacher_id = ? AND sub.marks_obtained IS NOT NULL")) {
                                assPs.setInt(1, studentId);
                                assPs.setInt(2, teacherId);
                                try (ResultSet assRs = assPs.executeQuery()) {
                                    if (assRs.next()) {
                                        double aVal = assRs.getDouble(1);
                                        if (!assRs.wasNull()) {
                                            assignmentAvg = aVal;
                                        }
                                    }
                                }
                            }

                            double testAvg = 100.0;
                            try (PreparedStatement testPs = con.prepareStatement("SELECT AVG(m.marks_obtained / t.total_marks) * 100 FROM test_marks m INNER JOIN class_tests t ON m.test_id = t.test_id WHERE m.student_id = ? AND t.teacher_id = ?")) {
                                testPs.setInt(1, studentId);
                                testPs.setInt(2, teacherId);
                                try (ResultSet testRs = testPs.executeQuery()) {
                                    if (testRs.next()) {
                                        double tVal = testRs.getDouble(1);
                                        if (!testRs.wasNull()) {
                                            testAvg = tVal;
                                        }
                                    }
                                }
                            }

                            double overall = (attendanceRate + assignmentAvg + testAvg) / 3;
                            map.put("attendance", String.format("%.1f%%", attendanceRate));
                            map.put("assignment", String.format("%.1f", assignmentAvg));
                            map.put("test", String.format("%.1f", testAvg));
                            map.put("overall", String.format("%.1f", overall));

                            String performanceRating = "GOOD";
                            if (overall < 70.0) {
                                performanceRating = "CRITICAL";
                            } else if (overall < 85.0) {
                                performanceRating = "WARNING";
                            }
                            map.put("rating", performanceRating);

                            list.add(map);
                        }
                    }
                }
                request.setAttribute("reportData", list);
            }

            request.setAttribute("activePage", "reports");
            request.getRequestDispatcher("reports.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load reports");
        }
    }

    private void loadAdminReports(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();

            // USER COUNTS
            int totalUsers = getCount(connection, "SELECT COUNT(*) FROM users");
            int totalStudents = getCount(connection, "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'");
            int totalTeachers = getCount(connection, "SELECT COUNT(*) FROM users WHERE role = 'TEACHER'");
            int totalAdmins = getCount(connection, "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'");

            // STATUS COUNTS
            int activeUsers = getCount(connection, "SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'");
            int inactiveUsers = getCount(connection, "SELECT COUNT(*) FROM users WHERE status = 'INACTIVE'");
            int suspendedUsers = getCount(connection, "SELECT COUNT(*) FROM users WHERE status = 'SUSPENDED'");

            // REPORTS
            List<Map<String, Object>> studentDepartments = getStudentDepartments(connection);
            List<Map<String, Object>> teacherDepartments = getTeacherDepartments(connection);
            List<Map<String, Object>> studentSemesters = getStudentSemesters(connection);
            List<Map<String, Object>> recentUsers = getRecentUsers(connection);

            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("totalTeachers", totalTeachers);
            request.setAttribute("totalAdmins", totalAdmins);
            request.setAttribute("activeUsers", activeUsers);
            request.setAttribute("inactiveUsers", inactiveUsers);
            request.setAttribute("suspendedUsers", suspendedUsers);
            request.setAttribute("studentDepartments", studentDepartments);
            request.setAttribute("teacherDepartments", teacherDepartments);
            request.setAttribute("studentSemesters", studentSemesters);
            request.setAttribute("recentUsers", recentUsers);

            request.getRequestDispatcher("reports.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to generate reports");
        } finally {
            close(connection);
        }
    }

    private int getCount(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return result.getInt(1);
            }
        }
        return 0;
    }

    private List<Map<String, Object>> getStudentDepartments(Connection connection) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT department, COUNT(*) AS total FROM students GROUP BY department ORDER BY department";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("department", result.getString("department"));
                row.put("total", result.getInt("total"));
                list.add(row);
            }
        }
        return list;
    }

    private List<Map<String, Object>> getTeacherDepartments(Connection connection) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT department, COUNT(*) AS total FROM teachers GROUP BY department ORDER BY department";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("department", result.getString("department"));
                row.put("total", result.getInt("total"));
                list.add(row);
            }
        }
        return list;
    }

    private List<Map<String, Object>> getStudentSemesters(Connection connection) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT semester, COUNT(*) AS total FROM students GROUP BY semester ORDER BY semester";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("semester", result.getInt("semester"));
                row.put("total", result.getInt("total"));
                list.add(row);
            }
        }
        return list;
    }

    private List<Map<String, Object>> getRecentUsers(Connection connection) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status, created_at FROM users ORDER BY user_id DESC LIMIT 10";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("userId", result.getInt("user_id"));
                user.put("username", result.getString("username"));
                user.put("role", result.getString("role"));
                user.put("status", result.getString("status"));
                user.put("createdAt", result.getTimestamp("created_at"));
                list.add(user);
            }
        }
        return list;
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}