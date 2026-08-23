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

@WebServlet("/StudentCalendarServlet")
public class StudentCalendarServlet extends HttpServlet {

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
        List<Map<String, Object>> calendarEvents = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            
            // Get student core info
            String department = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT department FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        department = rs.getString("department");
                    }
                }
            }

            // Retrieve calendar events, assignments, and class tests consolidated
            String sql = 
                "SELECT 'EVENT' AS type, title, event_date AS date, description FROM calendar_events ce " +
                "INNER JOIN teachers t ON ce.teacher_id = t.teacher_id " +
                "WHERE t.department = ? " +
                "UNION ALL " +
                "SELECT 'ASSIGNMENT' AS type, title, due_date AS date, description FROM assignments a " +
                "INNER JOIN teachers t ON a.teacher_id = t.teacher_id " +
                "WHERE t.department = ? AND a.status = 'ACTIVE' " +
                "UNION ALL " +
                "SELECT 'TEST' AS type, test_title AS title, test_date AS date, description FROM class_tests ct " +
                "INNER JOIN teachers t ON ct.teacher_id = t.teacher_id " +
                "WHERE t.department = ? " +
                "ORDER BY date ASC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, department);
                ps.setString(2, department);
                ps.setString(3, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", rs.getString("type"));
                        map.put("title", rs.getString("title"));
                        map.put("date", rs.getDate("date"));
                        map.put("description", rs.getString("description") != null ? rs.getString("description") : "");
                        calendarEvents.add(map);
                    }
                }
            }

            request.setAttribute("calendarEvents", calendarEvents);
            request.setAttribute("activePage", "calendar");
            request.getRequestDispatcher("studentCalendar.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
