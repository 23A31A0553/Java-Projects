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

@WebServlet("/TeacherProfileServlet")
public class TeacherProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"TEACHER".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if ("changePasswordForm".equalsIgnoreCase(action)) {
            request.setAttribute("activePage", "changePassword");
            request.getRequestDispatcher("teacherProfile.jsp?tab=password").forward(request, response);
            return;
        }

        // Default: Load Profile
        loadProfile(request, response);
    }

    private void loadProfile(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, u.mobile, u.status, " +
                 "t.teacher_id, t.department, t.employee_type " +
                 "FROM users u " +
                 "INNER JOIN teachers t ON u.user_id = t.user_id " +
                 "WHERE u.user_id = ?")) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    request.setAttribute("profileUserId", rs.getInt("user_id"));
                    request.setAttribute("profileTeacherId", rs.getInt("teacher_id"));
                    request.setAttribute("profileUsername", rs.getString("username"));
                    request.setAttribute("profileFirstName", rs.getString("first_name"));
                    request.setAttribute("profileLastName", rs.getString("last_name"));
                    request.setAttribute("profileEmail", rs.getString("email"));
                    request.setAttribute("profileMobile", rs.getString("mobile"));
                    request.setAttribute("profileStatus", rs.getString("status"));
                    request.setAttribute("profileDepartment", rs.getString("department"));
                    request.setAttribute("profileEmployeeType", rs.getString("employee_type"));
                }
            }
            request.setAttribute("activePage", "profile");
            request.getRequestDispatcher("teacherProfile.jsp").forward(request, response);
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
        if (session == null || !"TEACHER".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if ("updateProfile".equalsIgnoreCase(action)) {
            updateProfile(request, response);
        } else if ("changePassword".equalsIgnoreCase(action)) {
            changePassword(request, response);
        } else {
            response.sendRedirect("TeacherProfileServlet");
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String email = request.getParameter("email");
        String mobile = request.getParameter("mobile");

        if (email == null || email.trim().isEmpty() || mobile == null || mobile.trim().isEmpty()) {
            response.sendRedirect("TeacherProfileServlet?error=empty");
            return;
        }

        email = email.trim();
        mobile = mobile.trim();

        // Basic validation
        if (!email.contains("@") || mobile.length() < 10) {
            response.sendRedirect("TeacherProfileServlet?error=invalidfields");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Check email uniqueness (excluding current user)
            String emailCheckSql = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
            try (PreparedStatement checkPs = con.prepareStatement(emailCheckSql)) {
                checkPs.setString(1, email);
                checkPs.setInt(2, userId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        response.sendRedirect("TeacherProfileServlet?error=emailexists");
                        return;
                    }
                }
            }

            // Update email & mobile
            String updateSql = "UPDATE users SET email = ?, mobile = ? WHERE user_id = ?";
            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                updatePs.setString(1, email);
                updatePs.setString(2, mobile);
                updatePs.setInt(3, userId);
                updatePs.executeUpdate();
            }

            // Update session attributes
            session.setAttribute("email", email);
            session.setAttribute("mobile", mobile);

            // Log activity
            logActivity(con, userId, "UPDATE_PROFILE", "Teacher updated contact profile: Email=" + email + ", Mobile=" + mobile);

            response.sendRedirect("TeacherProfileServlet?success=profileupdated");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherProfileServlet?error=database");
        }
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (currentPassword == null || currentPassword.isEmpty() ||
            newPassword == null || newPassword.isEmpty() ||
            confirmPassword == null || confirmPassword.isEmpty()) {
            response.sendRedirect("TeacherProfileServlet?action=changePasswordForm&error=empty");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect("TeacherProfileServlet?action=changePasswordForm&error=mismatch");
            return;
        }

        if (newPassword.length() < 6) {
            response.sendRedirect("TeacherProfileServlet?action=changePasswordForm&error=weak");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Verify old password
            String passQuery = "SELECT password FROM users WHERE user_id = ?";
            try (PreparedStatement passPs = con.prepareStatement(passQuery)) {
                passPs.setInt(1, userId);
                try (ResultSet rs = passPs.executeQuery()) {
                    if (rs.next()) {
                        String dbPass = rs.getString("password");
                        if (!currentPassword.equals(dbPass)) {
                            response.sendRedirect("TeacherProfileServlet?action=changePasswordForm&error=invalidcurrent");
                            return;
                        }
                    }
                }
            }

            // Update password
            String updatePassQuery = "UPDATE users SET password = ? WHERE user_id = ?";
            try (PreparedStatement updatePs = con.prepareStatement(updatePassQuery)) {
                updatePs.setString(1, newPassword);
                updatePs.setInt(2, userId);
                updatePs.executeUpdate();
            }

            // Log activity
            logActivity(con, userId, "CHANGE_PASSWORD", "Teacher changed account password");

            response.sendRedirect("TeacherProfileServlet?action=changePasswordForm&success=passwordchanged");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherProfileServlet?action=changePasswordForm&error=database");
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
