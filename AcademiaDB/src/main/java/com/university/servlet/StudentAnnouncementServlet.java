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

@WebServlet("/StudentAnnouncementServlet")
public class StudentAnnouncementServlet extends HttpServlet {

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
        List<Map<String, Object>> announcementsList = new ArrayList<>();

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

            // Retrieve announcements matching student audience filters
            String sql = 
                "SELECT a.announcement_id, a.title, a.message, a.status, a.audience, a.subject, a.created_at, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS poster_name " +
                "FROM announcements a " +
                "INNER JOIN users u ON a.posted_by = u.user_id " +
                "WHERE a.status = 'PUBLISHED' " +
                "AND (a.audience = 'ALL' OR a.audience = 'STUDENT' OR a.audience = ?) " +
                "ORDER BY a.created_at DESC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("announcementId", rs.getInt("announcement_id"));
                        map.put("title", rs.getString("title"));
                        map.put("message", rs.getString("message"));
                        map.put("audience", rs.getString("audience"));
                        map.put("subject", rs.getString("subject") != null ? rs.getString("subject") : "General");
                        map.put("createdAt", rs.getTimestamp("created_at"));
                        map.put("posterName", rs.getString("poster_name"));
                        announcementsList.add(map);
                    }
                }
            }

            request.setAttribute("announcementsList", announcementsList);
            request.setAttribute("activePage", "announcements");
            request.getRequestDispatcher("studentAnnouncements.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
