package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;

@WebServlet("/StudentChangePasswordServlet")
public class StudentChangePasswordServlet extends HttpServlet {

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

        request.setAttribute("activePage", "changePassword");
        request.getRequestDispatcher("changePassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"STUDENT".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String currentPass = request.getParameter("currentPassword");
        String newPass = request.getParameter("newPassword");
        String confirmPass = request.getParameter("confirmPassword");

        if (currentPass == null || currentPass.trim().isEmpty() ||
            newPass == null || newPass.trim().isEmpty() ||
            confirmPass == null || confirmPass.trim().isEmpty()) {
            response.sendRedirect("StudentChangePasswordServlet?error=empty");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            response.sendRedirect("StudentChangePasswordServlet?error=mismatch");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            
            // Verify current password
            String currentDbPass = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT password FROM users WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentDbPass = rs.getString("password");
                    }
                }
            }

            if (!currentDbPass.equals(currentPass)) {
                response.sendRedirect("StudentChangePasswordServlet?error=wrongcurrent");
                return;
            }

            // Update to new password
            try (PreparedStatement ps = con.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?")) {
                ps.setString(1, newPass.trim());
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // Log activity log
            logActivity(con, userId, "CHANGE_PASSWORD", "Student changed password successfully.");

            response.sendRedirect("StudentChangePasswordServlet?success=changed");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentChangePasswordServlet?error=database");
        }
    }

    private void logActivity(Connection conn, int userId, String action, String description) throws Exception {
        String sql = "INSERT INTO activity_logs (user_id, action, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }
}
