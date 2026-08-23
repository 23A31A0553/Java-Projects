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

@WebServlet("/StudentTestServlet")
public class StudentTestServlet extends HttpServlet {

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
        List<Map<String, Object>> upcomingTests = new ArrayList<>();
        List<Map<String, Object>> previousTests = new ArrayList<>();

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

            // Retrieve class tests
            String sql = 
                "SELECT ct.test_id, ct.subject, ct.unit, ct.test_title, ct.test_date, ct.total_marks, ct.description, " +
                "tm.marks_obtained, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                "FROM class_tests ct " +
                "INNER JOIN teachers t ON ct.teacher_id = t.teacher_id " +
                "INNER JOIN users u ON t.user_id = u.user_id " +
                "LEFT JOIN test_marks tm ON ct.test_id = tm.test_id AND tm.student_id = ? " +
                "WHERE t.department = ? " +
                "ORDER BY ct.test_date DESC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ps.setString(2, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("testId", rs.getInt("test_id"));
                        map.put("subject", rs.getString("subject"));
                        map.put("unit", rs.getString("unit"));
                        map.put("title", rs.getString("test_title"));
                        map.put("date", rs.getDate("test_date"));
                        map.put("totalMarks", rs.getInt("total_marks"));
                        map.put("description", rs.getString("description"));
                        map.put("teacherName", rs.getString("teacher_name"));
                        
                        Object marks = rs.getObject("marks_obtained");
                        map.put("marksObtained", marks);

                        java.sql.Date testDate = rs.getDate("test_date");
                        boolean isUpcoming = testDate != null && !testDate.before(new java.util.Date(System.currentTimeMillis() - 24*3600*1000)); // Today or future

                        if (isUpcoming) {
                            upcomingTests.add(map);
                        } else {
                            previousTests.add(map);
                        }
                    }
                }
            }

            request.setAttribute("upcomingTests", upcomingTests);
            request.setAttribute("previousTests", previousTests);
            request.setAttribute("activePage", "classTests");
            request.getRequestDispatcher("classTests.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
