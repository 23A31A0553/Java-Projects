package com.university.servlet;

import com.university.db.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/ExportServlet")
public class ExportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        String type =
                request.getParameter("type");

        if (type == null) {
            type = "students";
        }

        switch (type.toLowerCase()) {

            case "students":

                exportStudents(
                        request,
                        response
                );

                break;

            case "teachers":

                exportTeachers(
                        request,
                        response
                );

                break;

            case "users":

                exportUsers(
                        request,
                        response
                );

                break;

            case "activity":

                exportActivityLogs(
                        request,
                        response
                );

                break;

            default:

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid export type"
                );

                break;
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


    // =====================================================
    // ADMIN SECURITY
    // =====================================================

    private boolean isAdmin(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            response.sendRedirect(
                    "index.jsp"
            );

            return false;
        }

        String role =
                (String)
                session.getAttribute("role");

        if (role == null ||
            !"ADMIN".equalsIgnoreCase(role)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied"
            );

            return false;
        }

        return true;
    }


    // =====================================================
    // EXPORT STUDENTS
    // =====================================================

    private void exportStudents(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "text/csv"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=students.csv"
        );


        String sql =
                "SELECT "
                + "u.user_id, "
                + "u.username, "
                + "u.status, "
                + "s.student_id, "
                + "s.first_name, "
                + "s.last_name, "
                + "s.email, "
                + "s.mobile, "
                + "s.department, "
                + "s.semester "
                + "FROM users u "
                + "JOIN students s "
                + "ON u.user_id = s.user_id "
                + "WHERE u.role = 'STUDENT' "
                + "ORDER BY s.student_id";


        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            PrintWriter writer =
                    response.getWriter()
        ) {

            writer.println(
                    "User ID,Username,Status,"
                    + "Student ID,First Name,Last Name,"
                    + "Email,Mobile,Department,Semester"
            );


            while (result.next()) {

                writer.println(
                        csv(result.getString("user_id"))
                        + ","
                        + csv(result.getString("username"))
                        + ","
                        + csv(result.getString("status"))
                        + ","
                        + csv(result.getString("student_id"))
                        + ","
                        + csv(result.getString("first_name"))
                        + ","
                        + csv(result.getString("last_name"))
                        + ","
                        + csv(result.getString("email"))
                        + ","
                        + csv(result.getString("mobile"))
                        + ","
                        + csv(result.getString("department"))
                        + ","
                        + csv(result.getString("semester"))
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // EXPORT TEACHERS
    // =====================================================

    private void exportTeachers(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "text/csv"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=teachers.csv"
        );


        String sql =
                "SELECT "
                + "u.user_id, "
                + "u.username, "
                + "u.status, "
                + "t.teacher_id, "
                + "t.first_name, "
                + "t.last_name, "
                + "t.email, "
                + "t.mobile, "
                + "t.department "
                + "FROM users u "
                + "JOIN teachers t "
                + "ON u.user_id = t.user_id "
                + "WHERE u.role = 'TEACHER' "
                + "ORDER BY t.teacher_id";


        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            PrintWriter writer =
                    response.getWriter()
        ) {

            writer.println(
                    "User ID,Username,Status,"
                    + "Teacher ID,First Name,Last Name,"
                    + "Email,Mobile,Department"
            );


            while (result.next()) {

                writer.println(
                        csv(result.getString("user_id"))
                        + ","
                        + csv(result.getString("username"))
                        + ","
                        + csv(result.getString("status"))
                        + ","
                        + csv(result.getString("teacher_id"))
                        + ","
                        + csv(result.getString("first_name"))
                        + ","
                        + csv(result.getString("last_name"))
                        + ","
                        + csv(result.getString("email"))
                        + ","
                        + csv(result.getString("mobile"))
                        + ","
                        + csv(result.getString("department"))
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // EXPORT USERS
    // =====================================================

    private void exportUsers(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "text/csv"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=users.csv"
        );


        String sql =
                "SELECT "
                + "user_id, "
                + "username, "
                + "role, "
                + "status, "
                + "created_at "
                + "FROM users "
                + "ORDER BY user_id";


        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            PrintWriter writer =
                    response.getWriter()
        ) {

            writer.println(
                    "User ID,Username,Role,Status,Created At"
            );


            while (result.next()) {

                writer.println(
                        csv(result.getString("user_id"))
                        + ","
                        + csv(result.getString("username"))
                        + ","
                        + csv(result.getString("role"))
                        + ","
                        + csv(result.getString("status"))
                        + ","
                        + csv(result.getString("created_at"))
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // EXPORT ACTIVITY LOGS
    // =====================================================

    private void exportActivityLogs(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "text/csv"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=activity_logs.csv"
        );


        String sql =
                "SELECT "
                + "a.log_id, "
                + "a.user_id, "
                + "u.username, "
                + "u.role, "
                + "a.action, "
                + "a.description, "
                + "a.created_at "
                + "FROM activity_logs a "
                + "LEFT JOIN users u "
                + "ON a.user_id = u.user_id "
                + "ORDER BY a.log_id DESC";


        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            PrintWriter writer =
                    response.getWriter()
        ) {

            writer.println(
                    "Log ID,User ID,Username,"
                    + "Role,Action,Description,Created At"
            );


            while (result.next()) {

                writer.println(
                        csv(result.getString("log_id"))
                        + ","
                        + csv(result.getString("user_id"))
                        + ","
                        + csv(result.getString("username"))
                        + ","
                        + csv(result.getString("role"))
                        + ","
                        + csv(result.getString("action"))
                        + ","
                        + csv(result.getString("description"))
                        + ","
                        + csv(result.getString("created_at"))
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // CSV VALUE
    // =====================================================

    private String csv(
            String value) {

        if (value == null) {
            return "";
        }

        value =
                value.replace(
                        "\"",
                        "\"\""
                );

        return "\"" + value + "\"";
    }
}