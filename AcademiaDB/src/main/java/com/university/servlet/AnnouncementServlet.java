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

@WebServlet("/AnnouncementServlet")
public class AnnouncementServlet extends HttpServlet {

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
        if (!"ADMIN".equalsIgnoreCase(role) && !"TEACHER".equalsIgnoreCase(role)) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if ("editForm".equalsIgnoreCase(action)) {
            showEditForm(request, response);
            return;
        }

        loadAnnouncements(request, response);
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

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equalsIgnoreCase(role) && !"TEACHER".equalsIgnoreCase(role)) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("AnnouncementServlet");
            return;
        }

        switch (action) {
            case "add":
                addAnnouncement(request, response);
                break;
            case "update":
                updateAnnouncement(request, response);
                break;
            case "delete":
                deleteAnnouncement(request, response);
                break;
            case "changeStatus":
                changeStatus(request, response);
                break;
            default:
                response.sendRedirect("AnnouncementServlet");
                break;
        }
    }

    private void loadAnnouncements(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        int loggedInUserId = (Integer) session.getAttribute("userId");

        String search = request.getParameter("search");
        if (search == null) {
            search = "";
        }

        List<Map<String, Object>> announcements = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection()) {
            String sql;
            PreparedStatement statement;

            if ("ADMIN".equalsIgnoreCase(role)) {
                // Admins load all announcements
                sql = "SELECT announcement_id, title, message, posted_by, status, audience, subject, created_at, updated_at " +
                      "FROM announcements " +
                      "WHERE title LIKE ? OR message LIKE ? " +
                      "ORDER BY announcement_id DESC";
                statement = connection.prepareStatement(sql);
                String searchValue = "%" + search.trim() + "%";
                statement.setString(1, searchValue);
                statement.setString(2, searchValue);
            } else {
                // Teachers load announcements they posted
                sql = "SELECT announcement_id, title, message, posted_by, status, audience, subject, created_at, updated_at " +
                      "FROM announcements " +
                      "WHERE posted_by = ? AND (title LIKE ? OR message LIKE ?) " +
                      "ORDER BY announcement_id DESC";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, loggedInUserId);
                String searchValue = "%" + search.trim() + "%";
                statement.setString(2, searchValue);
                statement.setString(3, searchValue);
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> announcement = new HashMap<>();
                    announcement.put("announcementId", result.getInt("announcement_id"));
                    announcement.put("title", result.getString("title"));
                    announcement.put("message", result.getString("message"));
                    announcement.put("postedBy", result.getInt("posted_by"));
                    announcement.put("status", result.getString("status"));
                    announcement.put("audience", result.getString("audience"));
                    announcement.put("subject", result.getString("subject"));
                    announcement.put("createdAt", result.getTimestamp("created_at"));
                    announcement.put("updatedAt", result.getTimestamp("updated_at"));
                    announcements.add(announcement);
                }
            }
            statement.close();

            request.setAttribute("announcements", announcements);
            request.setAttribute("search", search);
            request.setAttribute("activePage", "announcements");

            request.getRequestDispatcher("announcements.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load announcements");
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        int loggedInUserId = (Integer) session.getAttribute("userId");
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("AnnouncementServlet?error=invalid");
            return;
        }

        int announcementId = Integer.parseInt(idParam);

        try (Connection con = DBConnection.getConnection()) {
            Map<String, Object> announcement = null;
            String sql;
            PreparedStatement ps;

            if ("ADMIN".equalsIgnoreCase(role)) {
                sql = "SELECT * FROM announcements WHERE announcement_id = ?";
                ps = con.prepareStatement(sql);
                ps.setInt(1, announcementId);
            } else {
                sql = "SELECT * FROM announcements WHERE announcement_id = ? AND posted_by = ?";
                ps = con.prepareStatement(sql);
                ps.setInt(1, announcementId);
                ps.setInt(2, loggedInUserId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    announcement = new HashMap<>();
                    announcement.put("announcementId", rs.getInt("announcement_id"));
                    announcement.put("title", rs.getString("title"));
                    announcement.put("message", rs.getString("message"));
                    announcement.put("postedBy", rs.getInt("posted_by"));
                    announcement.put("status", rs.getString("status"));
                    announcement.put("audience", rs.getString("audience"));
                    announcement.put("subject", rs.getString("subject"));
                }
            }
            ps.close();

            if (announcement == null) {
                response.sendRedirect("AnnouncementServlet?error=notfound");
                return;
            }

            request.setAttribute("announcement", announcement);
            request.setAttribute("activePage", "announcements");
            
            // Reload all announcements for list view context
            loadAnnouncements(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AnnouncementServlet?error=database");
        }
    }

    private void addAnnouncement(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        int loggedInUserId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String title = request.getParameter("title");
        String message = request.getParameter("message");
        String audience = request.getParameter("audience");
        String subject = request.getParameter("subject");

        if (title == null || title.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            response.sendRedirect("AnnouncementServlet?error=empty");
            return;
        }

        if (audience == null) {
            audience = "ALL";
        }

        try (Connection connection = DBConnection.getConnection()) {
            String sql = "INSERT INTO announcements (title, message, posted_by, status, audience, subject) " +
                         "VALUES (?, ?, ?, 'PUBLISHED', ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, title.trim());
                statement.setString(2, message.trim());
                statement.setInt(3, loggedInUserId);
                statement.setString(4, audience);
                statement.setString(5, subject != null ? subject.trim() : null);
                statement.executeUpdate();
            }

            // Create notification for users
            String notifTitle = "New Announcement: " + title;
            String notifMessage = message;
            String notifySql;
            if ("ALL".equalsIgnoreCase(audience)) {
                notifySql = "INSERT INTO notifications (user_id, title, message) SELECT user_id, ?, ? FROM users WHERE status='ACTIVE'";
            } else if ("STUDENT".equalsIgnoreCase(audience)) {
                notifySql = "INSERT INTO notifications (user_id, title, message) SELECT user_id, ?, ? FROM users WHERE role='STUDENT' AND status='ACTIVE'";
            } else if ("TEACHER".equalsIgnoreCase(audience)) {
                notifySql = "INSERT INTO notifications (user_id, title, message) SELECT user_id, ?, ? FROM users WHERE role='TEACHER' AND status='ACTIVE'";
            } else {
                notifySql = "INSERT INTO notifications (user_id, title, message) SELECT user_id, ?, ? FROM users WHERE status='ACTIVE'";
            }
            
            try (PreparedStatement notifPs = connection.prepareStatement(notifySql)) {
                notifPs.setString(1, notifTitle);
                notifPs.setString(2, notifMessage);
                notifPs.executeUpdate();
            }

            logActivity(connection, loggedInUserId, "ADD_ANNOUNCEMENT", "Added announcement: " + title);

            response.sendRedirect("AnnouncementServlet?success=added");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AnnouncementServlet?error=database");
        }
    }

    private void updateAnnouncement(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        int loggedInUserId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String idParam = request.getParameter("announcement_id");
        String title = request.getParameter("title");
        String message = request.getParameter("message");
        String audience = request.getParameter("audience");
        String subject = request.getParameter("subject");

        if (idParam == null || idParam.trim().isEmpty() ||
            title == null || title.trim().isEmpty() ||
            message == null || message.trim().isEmpty()) {
            response.sendRedirect("AnnouncementServlet?error=invalid");
            return;
        }

        int announcementId = Integer.parseInt(idParam);
        if (audience == null) {
            audience = "ALL";
        }

        try (Connection connection = DBConnection.getConnection()) {
            String sql;
            PreparedStatement statement;

            if ("ADMIN".equalsIgnoreCase(role)) {
                sql = "UPDATE announcements SET title = ?, message = ?, audience = ?, subject = ?, updated_at = CURRENT_TIMESTAMP " +
                      "WHERE announcement_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, title.trim());
                statement.setString(2, message.trim());
                statement.setString(3, audience);
                statement.setString(4, subject != null ? subject.trim() : null);
                statement.setInt(5, announcementId);
            } else {
                sql = "UPDATE announcements SET title = ?, message = ?, audience = ?, subject = ?, updated_at = CURRENT_TIMESTAMP " +
                      "WHERE announcement_id = ? AND posted_by = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, title.trim());
                statement.setString(2, message.trim());
                statement.setString(3, audience);
                statement.setString(4, subject != null ? subject.trim() : null);
                statement.setInt(5, announcementId);
                statement.setInt(6, loggedInUserId);
            }

            int rows = statement.executeUpdate();
            statement.close();

            if (rows == 0) {
                response.sendRedirect("AnnouncementServlet?error=notfound");
                return;
            }

            logActivity(connection, loggedInUserId, "EDIT_ANNOUNCEMENT", "Updated announcement ID: " + announcementId);

            response.sendRedirect("AnnouncementServlet?success=updated");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AnnouncementServlet?error=database");
        }
    }

    private void deleteAnnouncement(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        int loggedInUserId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String idParam = request.getParameter("announcement_id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("AnnouncementServlet?error=invalid");
            return;
        }

        int announcementId = Integer.parseInt(idParam);

        try (Connection connection = DBConnection.getConnection()) {
            String sql;
            PreparedStatement statement;

            if ("ADMIN".equalsIgnoreCase(role)) {
                sql = "DELETE FROM announcements WHERE announcement_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, announcementId);
            } else {
                sql = "DELETE FROM announcements WHERE announcement_id = ? AND posted_by = ?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, announcementId);
                statement.setInt(2, loggedInUserId);
            }

            int rows = statement.executeUpdate();
            statement.close();

            if (rows == 0) {
                response.sendRedirect("AnnouncementServlet?error=notfound");
                return;
            }

            logActivity(connection, loggedInUserId, "DELETE_ANNOUNCEMENT", "Deleted announcement ID: " + announcementId);

            response.sendRedirect("AnnouncementServlet?success=deleted");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AnnouncementServlet?error=database");
        }
    }

    private void changeStatus(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        int loggedInUserId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String idParam = request.getParameter("announcement_id");
        String status = request.getParameter("status");

        if (idParam == null || idParam.trim().isEmpty() || status == null || status.trim().isEmpty()) {
            response.sendRedirect("AnnouncementServlet?error=invalid");
            return;
        }

        int announcementId = Integer.parseInt(idParam);

        try (Connection connection = DBConnection.getConnection()) {
            String sql;
            PreparedStatement statement;

            if ("ADMIN".equalsIgnoreCase(role)) {
                sql = "UPDATE announcements SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE announcement_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, status);
                statement.setInt(2, announcementId);
            } else {
                sql = "UPDATE announcements SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE announcement_id = ? AND posted_by = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, status);
                statement.setInt(2, announcementId);
                statement.setInt(3, loggedInUserId);
            }

            int rows = statement.executeUpdate();
            statement.close();

            if (rows == 0) {
                response.sendRedirect("AnnouncementServlet?error=notfound");
                return;
            }

            logActivity(connection, loggedInUserId, "CHANGE_ANNOUNCEMENT_STATUS", "Changed status of announcement ID: " + announcementId + " to " + status);

            response.sendRedirect("AnnouncementServlet?success=" + ("PUBLISHED".equalsIgnoreCase(status) ? "activated" : "deactivated"));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AnnouncementServlet?error=database");
        }
    }

    private void logActivity(
            Connection connection,
            int userId,
            String action,
            String description)
            throws Exception {

        String sql = "INSERT INTO activity_logs (user_id, action, description) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, action);
            statement.setString(3, description);
            statement.executeUpdate();
        }
    }
}