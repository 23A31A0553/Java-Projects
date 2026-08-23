package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;
import com.university.model.Notification;

@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {

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

        listNotifications(request, response);
    }

    private void listNotifications(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<Notification> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT * FROM notifications WHERE user_id = ? ORDER BY notification_id DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setUserId(rs.getInt("user_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }

            request.setAttribute("notifications", list);
            request.setAttribute("activePage", "notifications");
            request.getRequestDispatcher("notifications.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherServlet?error=database");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("index.jsp?error=session");
            return;
        }

        String action = request.getParameter("action");
        if ("read".equalsIgnoreCase(action)) {
            markAsRead(request, response);
        } else if ("readAll".equalsIgnoreCase(action)) {
            markAllAsRead(request, response);
        } else {
            response.sendRedirect("NotificationServlet");
        }
    }

    private void markAsRead(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String idParam = request.getParameter("notificationId");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("NotificationServlet?error=invalid");
            return;
        }

        int notificationId = Integer.parseInt(idParam);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE notifications SET is_read = TRUE WHERE notification_id = ? AND user_id = ?")) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            
            response.sendRedirect("NotificationServlet?success=read");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("NotificationServlet?error=database");
        }
    }

    private void markAllAsRead(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE notifications SET is_read = TRUE WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            
            response.sendRedirect("NotificationServlet?success=readall");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("NotificationServlet?error=database");
        }
    }
}
