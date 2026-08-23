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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");


        // =================================================
        // GET LOGIN DETAILS
        // =================================================

        String username =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        String selectedRole =
                request.getParameter("role");


        // =================================================
        // VALIDATION
        // =================================================

        if (username == null ||
            password == null ||
            selectedRole == null) {

            response.sendRedirect(
                "index.jsp?error=empty"
            );

            return;
        }


        username = username.trim();
        selectedRole = selectedRole.trim().toUpperCase();


        if (username.isEmpty() ||
            password.isEmpty()) {

            response.sendRedirect(
                "index.jsp?error=empty"
            );

            return;
        }


        // =================================================
        // VALID ROLE
        // =================================================

        if (!"ADMIN".equals(selectedRole) &&
            !"TEACHER".equals(selectedRole) &&
            !"STUDENT".equals(selectedRole)) {

            response.sendRedirect(
                "index.jsp?error=invalidrole"
            );

            return;
        }


        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try {

            // =================================================
            // DATABASE CONNECTION
            // =================================================

            con = DBConnection.getConnection();


            // =================================================
            // LOGIN QUERY
            // =================================================

            String sql =
                "SELECT user_id, username, password, role, " +
                "first_name, last_name, email, mobile, status " +
                "FROM users " +
                "WHERE username = ? " +
                "AND role = ?";


            ps = con.prepareStatement(sql);


            ps.setString(
                1,
                username
            );


            ps.setString(
                2,
                selectedRole
            );


            rs = ps.executeQuery();


            // =================================================
            // USER NOT FOUND
            // =================================================

            if (!rs.next()) {

                response.sendRedirect(
                    "index.jsp?error=invalid"
                );

                return;
            }


            // =================================================
            // GET DATABASE VALUES
            // =================================================

            int userId =
                rs.getInt("user_id");


            String dbUsername =
                rs.getString("username");


            String dbPassword =
                rs.getString("password");


            String dbRole =
                rs.getString("role");


            String firstName =
                rs.getString("first_name");


            String lastName =
                rs.getString("last_name");


            String email =
                rs.getString("email");


            String mobile =
                rs.getString("mobile");


            String status =
                rs.getString("status");


            // =================================================
            // PASSWORD CHECK
            // =================================================

            if (!password.equals(dbPassword)) {

                response.sendRedirect(
                    "index.jsp?error=invalid"
                );

                return;
            }


            // =================================================
            // ACCOUNT STATUS
            // =================================================

            if (!"ACTIVE".equalsIgnoreCase(status)) {

                response.sendRedirect(
                    "index.jsp?error=inactive"
                );

                return;
            }


            // =================================================
            // ROLE CHECK
            // =================================================

            if (!selectedRole.equalsIgnoreCase(dbRole)) {

                response.sendRedirect(
                    "index.jsp?error=invalid"
                );

                return;
            }


            // =================================================
            // CREATE SESSION
            // =================================================

            HttpSession oldSession =
                request.getSession(false);


            if (oldSession != null) {
                oldSession.invalidate();
            }


            HttpSession session =
                request.getSession(true);


            session.setAttribute(
                "userId",
                userId
            );


            session.setAttribute(
                "username",
                dbUsername
            );


            session.setAttribute(
                "role",
                dbRole
            );


            session.setAttribute(
                "firstName",
                firstName
            );


            session.setAttribute(
                "lastName",
                lastName
            );


            session.setAttribute(
                "email",
                email
            );


            session.setAttribute(
                "mobile",
                mobile
            );


            // =================================================
            // ROLE BASED REDIRECTION
            // =================================================

            if ("ADMIN".equalsIgnoreCase(dbRole)) {

                response.sendRedirect(
                    "AdminServlet"
                );

            } else if (
                "TEACHER".equalsIgnoreCase(dbRole)) {

                response.sendRedirect(
                    "TeacherServlet"
                );

            } else if (
                "STUDENT".equalsIgnoreCase(dbRole)) {

                response.sendRedirect(
                    "StudentServlet"
                );

            } else {

                session.invalidate();

                response.sendRedirect(
                    "index.jsp?error=invalidrole"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                "index.jsp?error=database"
            );


        } finally {

            // =================================================
            // CLOSE RESULT SET
            // =================================================

            try {

                if (rs != null) {
                    rs.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }


            // =================================================
            // CLOSE STATEMENT
            // =================================================

            try {

                if (ps != null) {
                    ps.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }


            // =================================================
            // CLOSE CONNECTION
            // =================================================

            try {

                if (con != null) {
                    con.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}