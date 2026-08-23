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

@WebServlet("/AcademicServlet")
public class AcademicServlet extends HttpServlet {

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
        // ADMIN CHECK
        // =================================================

        if (session == null ||
            !"ADMIN".equalsIgnoreCase(
                String.valueOf(
                    session.getAttribute("role")))) {

            response.sendRedirect(
                "index.jsp?error=unauthorized"
            );

            return;
        }

        // =================================================
        // OPEN ACADEMIC PAGE
        // =================================================

        request.getRequestDispatcher(
            "academic.jsp"
        ).forward(
            request,
            response
        );
    }


    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        // =================================================
        // ADMIN CHECK
        // =================================================

        if (session == null ||
            !"ADMIN".equalsIgnoreCase(
                String.valueOf(
                    session.getAttribute("role")))) {

            response.sendRedirect(
                "index.jsp?error=unauthorized"
            );

            return;
        }

        request.setCharacterEncoding("UTF-8");

        // =================================================
        // GET ACTION
        // =================================================

        String action =
                request.getParameter("action");

        if (action == null) {

            response.sendRedirect(
                "AcademicServlet?error=invalid"
            );

            return;
        }

        // =================================================
        // ADD ACADEMIC YEAR
        // =================================================

        if ("add".equalsIgnoreCase(action)) {

            addAcademicYear(
                request,
                response
            );

        } else {

            response.sendRedirect(
                "AcademicServlet?error=invalid"
            );
        }
    }


    // =====================================================
    // ADD ACADEMIC YEAR
    // =====================================================

    private void addAcademicYear(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String academicYear =
                request.getParameter(
                    "academic_year"
                );

        String semester =
                request.getParameter(
                    "semester"
                );

        String startDate =
                request.getParameter(
                    "start_date"
                );

        String endDate =
                request.getParameter(
                    "end_date"
                );


        // =================================================
        // EMPTY CHECK
        // =================================================

        if (academicYear == null ||
            semester == null ||
            startDate == null ||
            endDate == null) {

            response.sendRedirect(
                "AcademicServlet?error=empty"
            );

            return;
        }


        academicYear =
                academicYear.trim();

        semester =
                semester.trim();

        startDate =
                startDate.trim();

        endDate =
                endDate.trim();


        if (academicYear.isEmpty() ||
            semester.isEmpty() ||
            startDate.isEmpty() ||
            endDate.isEmpty()) {

            response.sendRedirect(
                "AcademicServlet?error=empty"
            );

            return;
        }


        // =================================================
        // DATE VALIDATION
        // =================================================

        if (endDate.compareTo(startDate) <= 0) {

            response.sendRedirect(
                "AcademicServlet?error=dates"
            );

            return;
        }


        Connection con = null;

        PreparedStatement checkPs = null;

        PreparedStatement insertPs = null;

        ResultSet rs = null;


        try {

            con =
                DBConnection.getConnection();


            // =================================================
            // CHECK DUPLICATE
            // =================================================

            String checkSql =
                "SELECT academic_id " +
                "FROM academic_years " +
                "WHERE academic_year = ? " +
                "AND semester = ?";


            checkPs =
                con.prepareStatement(
                    checkSql
                );


            checkPs.setString(
                1,
                academicYear
            );


            checkPs.setString(
                2,
                semester
            );


            rs =
                checkPs.executeQuery();


            if (rs.next()) {

                response.sendRedirect(
                    "AcademicServlet?error=exists"
                );

                return;
            }


            // =================================================
            // INSERT
            // =================================================

            String insertSql =
                "INSERT INTO academic_years " +
                "(academic_year, semester, " +
                "start_date, end_date, status) " +
                "VALUES (?, ?, ?, ?, 'ACTIVE')";


            insertPs =
                con.prepareStatement(
                    insertSql
                );


            insertPs.setString(
                1,
                academicYear
            );


            insertPs.setString(
                2,
                semester
            );


            insertPs.setString(
                3,
                startDate
            );


            insertPs.setString(
                4,
                endDate
            );


            int rows =
                insertPs.executeUpdate();


            if (rows > 0) {

                response.sendRedirect(
                    "AcademicServlet?success=added"
                );

            } else {

                response.sendRedirect(
                    "AcademicServlet?error=failed"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                "AcademicServlet?error=database"
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
            // CLOSE CHECK STATEMENT
            // =================================================

            try {

                if (checkPs != null) {
                    checkPs.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }


            // =================================================
            // CLOSE INSERT STATEMENT
            // =================================================

            try {

                if (insertPs != null) {
                    insertPs.close();
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