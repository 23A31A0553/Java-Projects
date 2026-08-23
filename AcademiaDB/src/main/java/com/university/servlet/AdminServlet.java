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

@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);


        // =================================================
        // SESSION CHECK
        // =================================================

        if (session == null) {

            response.sendRedirect(
                "index.jsp?error=session"
            );

            return;
        }


        // =================================================
        // ROLE CHECK
        // =================================================

        String role =
                String.valueOf(
                    session.getAttribute("role")
                );


        if (!"ADMIN".equalsIgnoreCase(role)) {

            response.sendRedirect(
                "index.jsp?error=unauthorized"
            );

            return;
        }


        // =================================================
        // GET USER ID
        // =================================================

        Object userIdObject =
                session.getAttribute("userId");


        if (userIdObject == null) {

            session.invalidate();

            response.sendRedirect(
                "index.jsp?error=session"
            );

            return;
        }


        int userId;

        try {

            userId =
                Integer.parseInt(
                    String.valueOf(userIdObject)
                );

        } catch (NumberFormatException e) {

            session.invalidate();

            response.sendRedirect(
                "index.jsp?error=session"
            );

            return;
        }


        // =================================================
        // LOAD ADMIN DETAILS
        // =================================================

        try {

            Connection con =
                DBConnection.getConnection();


            String sql =
                "SELECT user_id, username, " +
                "first_name, last_name, email, " +
                "mobile, status, created_at " +
                "FROM users " +
                "WHERE user_id = ? " +
                "AND role = 'ADMIN'";


            PreparedStatement ps =
                con.prepareStatement(sql);


            ps.setInt(
                1,
                userId
            );


            ResultSet rs =
                ps.executeQuery();


            if (!rs.next()) {

                rs.close();
                ps.close();
                con.close();

                session.invalidate();

                response.sendRedirect(
                    "index.jsp?error=unauthorized"
                );

                return;
            }            // =================================================
            // ADMIN DETAILS
            // =================================================

            request.setAttribute(
                "adminUserId",
                rs.getInt("user_id")
            );


            request.setAttribute(
                "adminUsername",
                rs.getString("username")
            );


            request.setAttribute(
                "adminFirstName",
                rs.getString("first_name")
            );


            request.setAttribute(
                "adminLastName",
                rs.getString("last_name")
            );


            request.setAttribute(
                "adminEmail",
                rs.getString("email")
            );


            request.setAttribute(
                "adminMobile",
                rs.getString("mobile")
            );


            request.setAttribute(
                "adminStatus",
                rs.getString("status")
            );


            request.setAttribute(
                "adminCreatedAt",
                rs.getTimestamp("created_at")
            );


            rs.close();
            ps.close();


            // =================================================
            // STATISTICS COUNTS
            // =================================================

            String countSql =
                    "SELECT "
                    + "COUNT(*) AS total, "
                    + "SUM(CASE WHEN role = 'STUDENT' "
                    + "THEN 1 ELSE 0 END) AS students, "
                    + "SUM(CASE WHEN role = 'TEACHER' "
                    + "THEN 1 ELSE 0 END) AS teachers, "
                    + "SUM(CASE WHEN status = 'ACTIVE' "
                    + "THEN 1 ELSE 0 END) AS active "
                    + "FROM users";

            PreparedStatement countPs =
                    con.prepareStatement(countSql);

            ResultSet countRs =
                    countPs.executeQuery();

            int totalUsers = 0;
            int totalStudents = 0;
            int totalTeachers = 0;
            int activeUsers = 0;

            if (countRs.next()) {

                totalUsers =
                        countRs.getInt("total");

                totalStudents =
                        countRs.getInt("students");

                totalTeachers =
                        countRs.getInt("teachers");

                activeUsers =
                        countRs.getInt("active");
            }

            countRs.close();
            countPs.close();
            con.close();


            request.setAttribute(
                "totalUsers",
                totalUsers
            );

            request.setAttribute(
                "totalStudents",
                totalStudents
            );

            request.setAttribute(
                "totalTeachers",
                totalTeachers
            );

            request.setAttribute(
                "activeUsers",
                activeUsers
            );


            // =================================================
            // OPEN ADMIN PAGE
            // =================================================

            request.getRequestDispatcher(
                "admin.jsp"
            ).forward(
                request,
                response
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                "index.jsp?error=database"
            );
        }
    }


    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        doGet(
            request,
            response
        );
    }
}