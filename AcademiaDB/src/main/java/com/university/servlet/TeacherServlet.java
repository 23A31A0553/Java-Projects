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

@WebServlet("/TeacherServlet")
public class TeacherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // =================================================
        // SESSION CHECK
        // =================================================
        if (session == null) {
            response.sendRedirect("index.jsp?error=session");
            return;
        }

        // =================================================
        // ROLE CHECK
        // =================================================
        String role = String.valueOf(session.getAttribute("role"));
        if (!"TEACHER".equalsIgnoreCase(role)) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        // =================================================
        // GET TEACHER USER ID
        // =================================================
        Object userIdObject = session.getAttribute("userId");
        if (userIdObject == null) {
            session.invalidate();
            response.sendRedirect("index.jsp?error=session");
            return;
        }

        int teacherUserId = Integer.parseInt(String.valueOf(userIdObject));

        try (Connection con = DBConnection.getConnection()) {

            // =================================================
            // GET TEACHER DETAILS
            // =================================================
            String teacherSql =
                "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, u.mobile, u.status, " +
                "t.teacher_id, t.department, t.employee_type " +
                "FROM users u " +
                "INNER JOIN teachers t ON u.user_id = t.user_id " +
                "WHERE u.user_id = ? AND u.role = 'TEACHER'";

            int teacherId = 0;
            String department = "";

            try (PreparedStatement teacherPs = con.prepareStatement(teacherSql)) {
                teacherPs.setInt(1, teacherUserId);
                try (ResultSet teacherRs = teacherPs.executeQuery()) {
                    if (teacherRs.next()) {
                        teacherId = teacherRs.getInt("teacher_id");
                        department = teacherRs.getString("department");

                        request.setAttribute("teacherUserId", teacherRs.getInt("user_id"));
                        request.setAttribute("teacherId", teacherId);
                        request.setAttribute("teacherUsername", teacherRs.getString("username"));
                        request.setAttribute("teacherFirstName", teacherRs.getString("first_name"));
                        request.setAttribute("teacherLastName", teacherRs.getString("last_name"));
                        request.setAttribute("teacherEmail", teacherRs.getString("email"));
                        request.setAttribute("teacherMobile", teacherRs.getString("mobile"));
                        request.setAttribute("teacherStatus", teacherRs.getString("status"));
                        request.setAttribute("teacherDepartment", department);
                        request.setAttribute("employeeType", teacherRs.getString("employee_type"));
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Teacher details not found");
                        return;
                    }
                }
            }

            // =================================================
            // STATISTICS QUERIES
            // =================================================
            int totalStudents = 0;
            int totalAssignments = 0;
            int totalClassTests = 0;
            int pendingSubmissions = 0;
            int upcomingTests = 0;

            // 1. Total Students in Department
            String studentsCountSql = "SELECT COUNT(*) FROM students WHERE department = ?";
            try (PreparedStatement ps = con.prepareStatement(studentsCountSql)) {
                ps.setString(1, department);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalStudents = rs.getInt(1);
                    }
                }
            }

            // 2. Total Assignments created by this Teacher
            String assignmentsCountSql = "SELECT COUNT(*) FROM assignments WHERE teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(assignmentsCountSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalAssignments = rs.getInt(1);
                    }
                }
            }

            // 3. Total Class Tests scheduled by this Teacher
            String classTestsCountSql = "SELECT COUNT(*) FROM class_tests WHERE teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(classTestsCountSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalClassTests = rs.getInt(1);
                    }
                }
            }

            // 4. Pending Submissions to grade
            String pendingSubmissionsSql = 
                "SELECT COUNT(*) FROM assignment_submissions sub " +
                "INNER JOIN assignments a ON sub.assignment_id = a.assignment_id " +
                "WHERE a.teacher_id = ? AND sub.marks_obtained IS NULL";
            try (PreparedStatement ps = con.prepareStatement(pendingSubmissionsSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pendingSubmissions = rs.getInt(1);
                    }
                }
            }

            // 5. Upcoming Tests (today or in future)
            String upcomingTestsSql = "SELECT COUNT(*) FROM class_tests WHERE teacher_id = ? AND test_date >= CURDATE()";
            try (PreparedStatement ps = con.prepareStatement(upcomingTestsSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        upcomingTests = rs.getInt(1);
                    }
                }
            }

            // Set statistics request attributes
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("totalAssignments", totalAssignments);
            request.setAttribute("totalClassTests", totalClassTests);
            request.setAttribute("pendingSubmissions", pendingSubmissions);
            request.setAttribute("upcomingTests", upcomingTests);

            // =================================================
            // RECENT ACTIVITY LOGS
            // =================================================
            List<Map<String, Object>> recentActivities = new ArrayList<>();
            String activitySql = 
                "SELECT description, created_at FROM activity_logs " +
                "WHERE user_id = ? OR action LIKE '%TEACHER%' " +
                "ORDER BY log_id DESC LIMIT 5";
            try (PreparedStatement ps = con.prepareStatement(activitySql)) {
                ps.setInt(1, teacherUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("description", rs.getString("description"));
                        activity.put("createdAt", rs.getTimestamp("created_at"));
                        recentActivities.add(activity);
                    }
                }
            }
            request.setAttribute("recentActivities", recentActivities);
            request.setAttribute("activePage", "dashboard");

            // =================================================
            // OPEN TEACHER DASHBOARD
            // =================================================
            request.getRequestDispatcher("teacher.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load teacher dashboard");
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