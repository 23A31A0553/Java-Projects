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

@WebServlet("/StudentAssignmentServlet")
public class StudentAssignmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"STUDENT".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String filterStatus = request.getParameter("status"); // all, pending, submitted, completed
        if (filterStatus == null) {
            filterStatus = "all";
        }

        List<Map<String, Object>> assignmentList = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            
            // Get student core info
            int studentId = 0;
            String department = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT student_id, department FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        department = rs.getString("department");
                    }
                }
            }

            // Get assignments and link with student's submissions
            String sql = 
                "SELECT a.assignment_id, a.subject, a.unit, a.title, a.description, a.due_date, a.created_date, " +
                "sub.submission_id, sub.submission_date, sub.status AS sub_status, sub.file_name, sub.marks_obtained, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                "FROM assignments a " +
                "INNER JOIN teachers t ON a.teacher_id = t.teacher_id " +
                "INNER JOIN users u ON t.user_id = u.user_id " +
                "LEFT JOIN assignment_submissions sub ON a.assignment_id = sub.assignment_id AND sub.student_id = ? " +
                "WHERE t.department = ? " +
                "ORDER BY a.due_date ASC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ps.setString(2, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("assignmentId", rs.getInt("assignment_id"));
                        map.put("subject", rs.getString("subject"));
                        map.put("unit", rs.getString("unit"));
                        map.put("title", rs.getString("title"));
                        map.put("description", rs.getString("description"));
                        map.put("createdDate", rs.getTimestamp("created_date"));
                        map.put("dueDate", rs.getDate("due_date"));
                        map.put("teacherName", rs.getString("teacher_name"));
                        
                        // submission details
                        int submissionId = rs.getInt("submission_id");
                        map.put("submissionId", submissionId);
                        
                        // Calculate display status
                        String displayStatus = "PENDING";
                        java.sql.Date dueDate = rs.getDate("due_date");
                        boolean isOverdue = dueDate != null && dueDate.before(new java.util.Date());

                        if (submissionId > 0) {
                            Double marks = rs.getObject("marks_obtained") != null ? rs.getDouble("marks_obtained") : null;
                            if (marks != null) {
                                displayStatus = "COMPLETED";
                            } else {
                                displayStatus = "SUBMITTED";
                            }
                            map.put("submissionDate", rs.getTimestamp("submission_date"));
                            map.put("fileName", rs.getString("file_name"));
                            map.put("marksObtained", marks);
                            map.put("subStatus", rs.getString("sub_status"));
                        } else {
                            if (isOverdue) {
                                displayStatus = "LATE";
                            } else {
                                displayStatus = "PENDING";
                            }
                        }
                        
                        map.put("status", displayStatus);

                        // Apply filters
                        if ("all".equalsIgnoreCase(filterStatus)) {
                            assignmentList.add(map);
                        } else if ("pending".equalsIgnoreCase(filterStatus) && ("PENDING".equals(displayStatus) || "LATE".equals(displayStatus))) {
                            assignmentList.add(map);
                        } else if ("submitted".equalsIgnoreCase(filterStatus) && "SUBMITTED".equals(displayStatus)) {
                            assignmentList.add(map);
                        } else if ("completed".equalsIgnoreCase(filterStatus) && "COMPLETED".equals(displayStatus)) {
                            assignmentList.add(map);
                        }
                    }
                }
            }

            request.setAttribute("assignments", assignmentList);
            request.setAttribute("filterStatus", filterStatus);
            request.setAttribute("activePage", "assignments");
            request.getRequestDispatcher("assignments.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
