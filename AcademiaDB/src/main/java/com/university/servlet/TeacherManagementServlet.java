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

@WebServlet("/TeacherManagementServlet")
public class TeacherManagementServlet extends HttpServlet {

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

        loadTeachers(request, response);
    }


    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        String action =
                request.getParameter("action");

        if (action == null) {

            response.sendRedirect(
                    "TeacherManagementServlet"
            );

            return;
        }

        switch (action) {

            case "update":

                updateTeacher(
                        request,
                        response
                );

                break;


            case "delete":

                deleteTeacher(
                        request,
                        response
                );

                break;


            case "changeStatus":

                changeStatus(
                        request,
                        response
                );

                break;


            case "resetPassword":

                resetPassword(
                        request,
                        response
                );

                break;


            default:

                response.sendRedirect(
                        "TeacherManagementServlet"
                );

                break;
        }
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
    // LOAD TEACHERS
    // =====================================================

    private void loadTeachers(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String search =
                request.getParameter("search");

        String department =
                request.getParameter("department");


        if (search == null) {
            search = "";
        }

        if (department == null ||
            department.trim().isEmpty()) {

            department = "ALL";
        }


        List<Map<String, Object>> teachers =
                new ArrayList<>();


        try (
            Connection connection =
                    DBConnection.getConnection()
        ) {

            StringBuilder sql =
                    new StringBuilder();


            sql.append(
                    "SELECT "
                    + "u.user_id, "
                    + "u.username, "
                    + "u.status, "
                    + "u.first_name, "
                    + "u.last_name, "
                    + "u.email, "
                    + "u.mobile, "
                    + "u.created_at, "
                    + "t.teacher_id, "
                    + "t.department, "
                    + "t.employee_type "
                    + "FROM users u "
                    + "JOIN teachers t "
                    + "ON u.user_id = t.user_id "
                    + "WHERE u.role = 'TEACHER' "
            );


            List<String> parameters =
                    new ArrayList<>();


            // SEARCH

            if (!search.trim().isEmpty()) {

                sql.append(
                        "AND ("
                        + "u.username LIKE ? "
                        + "OR u.first_name LIKE ? "
                        + "OR u.last_name LIKE ? "
                        + "OR u.email LIKE ? "
                        + ") "
                );


                String searchValue =
                        "%" +
                        search.trim() +
                        "%";


                parameters.add(
                        searchValue
                );

                parameters.add(
                        searchValue
                );

                parameters.add(
                        searchValue
                );

                parameters.add(
                        searchValue
                );
            }


            // DEPARTMENT FILTER

            if (!department.equalsIgnoreCase("ALL")) {

                sql.append(
                        "AND t.department = ? "
                );

                parameters.add(
                        department
                );
            }


            sql.append(
                    "ORDER BY t.teacher_id DESC"
            );


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql.toString()
                        )
            ) {

                for (
                    int i = 0;
                    i < parameters.size();
                    i++
                ) {

                    statement.setString(
                            i + 1,
                            parameters.get(i)
                    );
                }


                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    while (result.next()) {

                        Map<String, Object> teacher =
                                new HashMap<>();


                        teacher.put(
                                "userId",
                                result.getInt(
                                        "user_id"
                                )
                        );


                        teacher.put(
                                "teacherId",
                                result.getInt(
                                        "teacher_id"
                                )
                        );


                        teacher.put(
                                "username",
                                result.getString(
                                        "username"
                                )
                        );


                        teacher.put(
                                "status",
                                result.getString(
                                        "status"
                                )
                        );


                        teacher.put(
                                "firstName",
                                result.getString(
                                        "first_name"
                                )
                        );


                        teacher.put(
                                "lastName",
                                result.getString(
                                        "last_name"
                                )
                        );


                        teacher.put(
                                "email",
                                result.getString(
                                        "email"
                                )
                        );


                        teacher.put(
                                "mobile",
                                result.getString(
                                        "mobile"
                                )
                        );


                        teacher.put(
                                "department",
                                result.getString(
                                        "department"
                                )
                        );


                        teacher.put(
                                "createdAt",
                                result.getTimestamp(
                                        "created_at"
                                )
                        );


                        teacher.put(
                                "employeeType",
                                result.getString(
                                        "employee_type"
                                )
                        );


                        teachers.add(
                                teacher
                        );
                    }
                }
            }


            response.sendRedirect(
                    "UserManagementServlet?role=TEACHER"
            );


        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load teachers"
            );
        }
    }


    // =====================================================
    // GET DEPARTMENTS
    // =====================================================

    private List<String> getDepartments(
            Connection connection)
            throws Exception {

        List<String> departments =
                new ArrayList<>();


        String sql =
                "SELECT department_name "
                + "FROM departments "
                + "WHERE status = 'ACTIVE' "
                + "ORDER BY department_name";


        try (
            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery()
        ) {

            while (result.next()) {

                departments.add(
                        result.getString(
                                "department_name"
                        )
                );
            }
        }


        return departments;
    }


    // =====================================================
    // UPDATE TEACHER
    // =====================================================

    private void updateTeacher(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int userId =
                getInt(
                        request,
                        "user_id"
                );


        String username =
                request.getParameter(
                        "username"
                );


        String firstName =
                request.getParameter(
                        "first_name"
                );


        String lastName =
                request.getParameter(
                        "last_name"
                );


        String email =
                request.getParameter(
                        "email"
                );


        String mobile =
                request.getParameter(
                        "mobile"
                );


        String department =
                request.getParameter(
                        "department"
                );


        if (userId <= 0 ||
            isEmpty(username) ||
            isEmpty(firstName) ||
            isEmpty(lastName) ||
            isEmpty(email) ||
            isEmpty(mobile) ||
            isEmpty(department)) {

            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=invalid"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);


            // UPDATE USERNAME

            String userSql =
                    "UPDATE users "
                    + "SET username = ? "
                    + "WHERE user_id = ? "
                    + "AND role = 'TEACHER'";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                userSql
                        )
            ) {

                statement.setString(
                        1,
                        username
                );

                statement.setInt(
                        2,
                        userId
                );

                statement.executeUpdate();
            }


            // UPDATE USER (first_name, last_name, email, mobile live in users table)

            String userDetailSql =
                    "UPDATE users SET "
                    + "first_name = ?, "
                    + "last_name = ?, "
                    + "email = ?, "
                    + "mobile = ? "
                    + "WHERE user_id = ? "
                    + "AND role = 'TEACHER'";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                userDetailSql
                        )
            ) {

                statement.setString(
                        1,
                        firstName
                );

                statement.setString(
                        2,
                        lastName
                );

                statement.setString(
                        3,
                        email
                );

                statement.setString(
                        4,
                        mobile
                );

                statement.setInt(
                        5,
                        userId
                );

                statement.executeUpdate();
            }


            // UPDATE TEACHER (department lives in teachers table)

            String teacherSql =
                    "UPDATE teachers SET "
                    + "department = ? "
                    + "WHERE user_id = ?";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                teacherSql
                        )
            ) {

                statement.setString(
                        1,
                        department
                );

                statement.setInt(
                        2,
                        userId
                );

                statement.executeUpdate();
            }


            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "EDIT_TEACHER",
                    "Updated teacher: "
                    + username
            );


            connection.commit();


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?success=updated"
            );


        } catch (Exception e) {

            rollback(
                    connection
            );

            e.printStackTrace();


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error="
                    + (isDuplicate(e)
                        ? "duplicate"
                        : "database")
            );


        } finally {

            close(
                    connection
            );
        }
    }


    // =====================================================
    // DELETE TEACHER
    // =====================================================

    private void deleteTeacher(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int userId =
                getInt(
                        request,
                        "user_id"
                );


        if (userId <= 0) {

            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=invalid"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String username =
                    getUsername(
                            connection,
                            userId
                    );


            String sql =
                    "DELETE FROM users "
                    + "WHERE user_id = ? "
                    + "AND role = 'TEACHER'";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        userId
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    response.sendRedirect(
                            "TeacherManagementServlet"
                            + "?error=notfound"
                    );

                    return;
                }
            }


            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "DELETE_TEACHER",
                    "Deleted teacher: "
                    + username
            );


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?success=deleted"
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=database"
            );


        } finally {

            close(
                    connection
            );
        }
    }


    // =====================================================
    // CHANGE STATUS
    // =====================================================

    private void changeStatus(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int userId =
                getInt(
                        request,
                        "user_id"
                );


        String status =
                request.getParameter(
                        "status"
                );


        int adminId =
                getLoggedInUserId(
                        request
                );


        if (userId <= 0 ||
            isEmpty(status)) {

            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=invalid"
            );

            return;
        }


        if (!status.equals("ACTIVE") &&
            !status.equals("INACTIVE") &&
            !status.equals("SUSPENDED")) {

            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=invalid"
            );

            return;
        }


        if (userId == adminId) {

            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=selfstatus"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String sql =
                    "UPDATE users "
                    + "SET status = ? "
                    + "WHERE user_id = ? "
                    + "AND role = 'TEACHER'";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        status
                );

                statement.setInt(
                        2,
                        userId
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    response.sendRedirect(
                            "TeacherManagementServlet"
                            + "?error=notfound"
                    );

                    return;
                }
            }


            logActivity(
                    connection,
                    adminId,
                    "CHANGE_TEACHER_STATUS",
                    "Changed teacher ID "
                    + userId
                    + " status to "
                    + status
            );


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?success=status"
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=database"
            );


        } finally {

            close(
                    connection
            );
        }
    }


    // =====================================================
    // RESET PASSWORD
    // =====================================================

    private void resetPassword(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int userId =
                getInt(
                        request,
                        "user_id"
                );


        String newPassword =
                request.getParameter(
                        "new_password"
                );


        if (userId <= 0 ||
            isEmpty(newPassword)) {

            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=empty"
            );

            return;
        }


        if (newPassword.length() < 6) {

            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=password"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String sql =
                    "UPDATE users "
                    + "SET password = ? "
                    + "WHERE user_id = ? "
                    + "AND role = 'TEACHER'";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        newPassword
                );

                statement.setInt(
                        2,
                        userId
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    response.sendRedirect(
                            "TeacherManagementServlet"
                            + "?error=notfound"
                    );

                    return;
                }
            }


            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "RESET_TEACHER_PASSWORD",
                    "Reset password for teacher ID: "
                    + userId
            );


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?success=password"
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                    "TeacherManagementServlet"
                    + "?error=database"
            );


        } finally {

            close(
                    connection
            );
        }
    }


    // =====================================================
    // ACTIVITY LOG
    // =====================================================

    private void logActivity(
            Connection connection,
            int userId,
            String action,
            String description)
            throws Exception {

        String sql =
                "INSERT INTO activity_logs "
                + "(user_id, action, description) "
                + "VALUES (?, ?, ?)";


        try (
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    userId
            );

            statement.setString(
                    2,
                    action
            );

            statement.setString(
                    3,
                    description
            );

            statement.executeUpdate();
        }
    }


    // =====================================================
    // GET USERNAME
    // =====================================================

    private String getUsername(
            Connection connection,
            int userId)
            throws Exception {

        String sql =
                "SELECT username "
                + "FROM users "
                + "WHERE user_id = ?";


        try (
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    userId
            );


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                if (result.next()) {

                    return result.getString(
                            "username"
                    );
                }
            }
        }


        return "Unknown";
    }


    // =====================================================
    // GET LOGGED-IN ADMIN ID
    // =====================================================

    private int getLoggedInUserId(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);


        if (session == null) {
            return -1;
        }


        Object value =
                session.getAttribute(
                        "userId"
                );


        try {

            return Integer.parseInt(
                    String.valueOf(value)
            );

        } catch (Exception e) {

            return -1;
        }
    }


    // =====================================================
    // INTEGER PARAMETER
    // =====================================================

    private int getInt(
            HttpServletRequest request,
            String parameter) {

        try {

            return Integer.parseInt(
                    request.getParameter(
                            parameter
                    )
            );

        } catch (Exception e) {

            return -1;
        }
    }


    // =====================================================
    // EMPTY CHECK
    // =====================================================

    private boolean isEmpty(
            String value) {

        return value == null ||
               value.trim().isEmpty();
    }


    // =====================================================
    // DUPLICATE CHECK
    // =====================================================

    private boolean isDuplicate(
            Exception e) {

        String message =
                e.getMessage();


        if (message == null) {
            return false;
        }


        return message.contains("Duplicate")
                ||
                message.contains("duplicate")
                ||
                message.contains("UNIQUE");
    }


    // =====================================================
    // ROLLBACK
    // =====================================================

    private void rollback(
            Connection connection) {

        if (connection != null) {

            try {

                connection.rollback();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    // =====================================================
    // CLOSE CONNECTION
    // =====================================================

    private void close(
            Connection connection) {

        if (connection != null) {

            try {

                connection.close();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}