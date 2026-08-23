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

@WebServlet("/StudentProfileServlet")
public class StudentProfileServlet extends HttpServlet {

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

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, u.mobile, u.status, u.created_at, " +
                         "s.student_id, s.department, s.semester " +
                         "FROM users u INNER JOIN students s ON u.user_id = s.user_id " +
                         "WHERE u.user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        request.setAttribute("studentId", rs.getInt("student_id"));
                        request.setAttribute("username", rs.getString("username"));
                        request.setAttribute("firstName", rs.getString("first_name"));
                        request.setAttribute("lastName", rs.getString("last_name"));
                        request.setAttribute("email", rs.getString("email"));
                        request.setAttribute("mobile", rs.getString("mobile"));
                        request.setAttribute("status", rs.getString("status"));
                        request.setAttribute("createdAt", rs.getTimestamp("created_at"));
                        request.setAttribute("department", rs.getString("department"));
                        request.setAttribute("semester", rs.getString("semester"));
                    }
                }
            }
            request.setAttribute("activePage", "profile");
            request.getRequestDispatcher("studentProfile.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
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
        String email = request.getParameter("email");
        String mobile = request.getParameter("mobile");

        if (email == null || email.trim().isEmpty()) {
            response.sendRedirect("StudentProfileServlet?error=empty");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE users SET email = ?, mobile = ? WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                ps.setString(2, mobile != null ? mobile.trim() : "");
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
            
            // Update session attributes if needed
            session.setAttribute("email", email.trim());
            session.setAttribute("mobile", mobile != null ? mobile.trim() : "");

            response.sendRedirect("StudentProfileServlet?success=updated");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentProfileServlet?error=database");
        }
    }
}
