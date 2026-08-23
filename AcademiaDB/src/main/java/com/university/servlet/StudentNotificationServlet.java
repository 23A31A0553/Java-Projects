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

@WebServlet("/StudentNotificationServlet")
public class StudentNotificationServlet extends HttpServlet {

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
        String action = request.getParameter("action");

        if ("markAsRead".equalsIgnoreCase(action)) {
            markAsRead(request, response, userId);
            return;
        } else if ("markAllAsRead".equalsIgnoreCase(action)) {
            markAllAsRead(request, response, userId);
            return;
        }

        listNotifications(request, response, userId);
    }

    private void listNotifications(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        List<Map<String, Object>> notificationsList = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT notification_id, title, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("notificationId", rs.getInt("notification_id"));
                        map.put("title", rs.getString("title"));
                        map.put("message", rs.getString("message"));
                        map.put("isRead", rs.getBoolean("is_read"));
                        map.put("createdAt", rs.getTimestamp("created_at"));
                        notificationsList.add(map);
                    }
                }
            }

            request.setAttribute("notificationsList", notificationsList);
            request.setAttribute("activePage", "notifications");
            request.getRequestDispatcher("studentNotifications.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }

    private void markAsRead(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String idStr = request.getParameter("notificationId");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect("StudentNotificationServlet?error=invalid");
            return;
        }

        int notificationId = Integer.parseInt(idStr);

        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ? AND user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            response.sendRedirect("StudentNotificationServlet");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentNotificationServlet?error=database");
        }
    }

    private void markAllAsRead(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            response.sendRedirect("StudentNotificationServlet");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentNotificationServlet?error=database");
        }
    }
}
