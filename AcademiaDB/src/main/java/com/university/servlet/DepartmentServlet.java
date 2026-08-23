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

@WebServlet("/DepartmentServlet")
public class DepartmentServlet extends HttpServlet {

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

        loadDepartments(
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

        if (!isAdmin(request, response)) {
            return;
        }

        String action =
                request.getParameter("action");


        if (action == null) {

            response.sendRedirect(
                    "DepartmentServlet"
            );

            return;
        }


        switch (action) {

            case "add":

                addDepartment(
                        request,
                        response
                );

                break;


            case "update":

                updateDepartment(
                        request,
                        response
                );

                break;


            case "delete":

                deleteDepartment(
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


            default:

                response.sendRedirect(
                        "DepartmentServlet"
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
                session.getAttribute(
                        "role"
                );


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
    // LOAD DEPARTMENTS
    // =====================================================

    private void loadDepartments(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String search =
                request.getParameter(
                        "search"
                );


        if (search == null) {
            search = "";
        }


        List<Map<String, Object>> departments =
                new ArrayList<>();


        try (
            Connection connection =
                    DBConnection.getConnection()
        ) {


            String sql =
                    "SELECT "
                    + "department_id, "
                    + "department_code, "
                    + "department_name, "
                    + "hod_name, "
                    + "status, "
                    + "created_at "
                    + "FROM departments "
                    + "WHERE department_code LIKE ? "
                    + "OR department_name LIKE ? "
                    + "OR hod_name LIKE ? "
                    + "ORDER BY department_id DESC";


            String searchValue =
                    "%" +
                    search.trim() +
                    "%";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
            ) {


                statement.setString(
                        1,
                        searchValue
                );


                statement.setString(
                        2,
                        searchValue
                );


                statement.setString(
                        3,
                        searchValue
                );


                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {


                    while (result.next()) {


                        Map<String, Object> department =
                                new HashMap<>();


                        department.put(
                                "departmentId",
                                result.getInt(
                                        "department_id"
                                )
                        );


                        department.put(
                                "departmentCode",
                                result.getString(
                                        "department_code"
                                )
                        );


                        department.put(
                                "departmentName",
                                result.getString(
                                        "department_name"
                                )
                        );


                        department.put(
                                "hodName",
                                result.getString(
                                        "hod_name"
                                )
                        );


                        department.put(
                                "status",
                                result.getString(
                                        "status"
                                )
                        );


                        department.put(
                                "createdAt",
                                result.getTimestamp(
                                        "created_at"
                                )
                        );


                        departments.add(
                                department
                        );
                    }
                }
            }


            request.setAttribute(
                    "departments",
                    departments
            );


            request.setAttribute(
                    "search",
                    search
            );


            request.getRequestDispatcher(
                    "departments.jsp"
            ).forward(
                    request,
                    response
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load departments"
            );
        }
    }


    // =====================================================
    // ADD DEPARTMENT
    // =====================================================

    private void addDepartment(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String code =
                request.getParameter(
                        "department_code"
                );


        String name =
                request.getParameter(
                        "department_name"
                );


        String hod =
                request.getParameter(
                        "hod_name"
                );


        if (isEmpty(code) ||
            isEmpty(name)) {

            redirect(
                    response,
                    "empty"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String sql =
                    "INSERT INTO departments "
                    + "(department_code, "
                    + "department_name, "
                    + "hod_name, "
                    + "status) "
                    + "VALUES (?, ?, ?, 'ACTIVE')";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
            ) {


                statement.setString(
                        1,
                        code.trim()
                );


                statement.setString(
                        2,
                        name.trim()
                );


                if (isEmpty(hod)) {

                    statement.setString(
                            3,
                            null
                    );

                } else {

                    statement.setString(
                            3,
                            hod.trim()
                    );
                }


                statement.executeUpdate();
            }


            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "ADD_DEPARTMENT",
                    "Added department: "
                    + name
            );


            redirectSuccess(
                    response,
                    "added"
            );


        } catch (Exception e) {

            e.printStackTrace();


            if (isDuplicate(e)) {

                redirect(
                        response,
                        "duplicate"
                );

            } else {

                redirect(
                        response,
                        "database"
                );
            }


        } finally {

            close(connection);
        }
    }


    // =====================================================
    // UPDATE DEPARTMENT
    // =====================================================

    private void updateDepartment(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int departmentId =
                getInt(
                        request,
                        "department_id"
                );


        String code =
                request.getParameter(
                        "department_code"
                );


        String name =
                request.getParameter(
                        "department_name"
                );


        String hod =
                request.getParameter(
                        "hod_name"
                );


        if (departmentId <= 0 ||
            isEmpty(code) ||
            isEmpty(name)) {

            redirect(
                    response,
                    "invalid"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String sql =
                    "UPDATE departments SET "
                    + "department_code = ?, "
                    + "department_name = ?, "
                    + "hod_name = ? "
                    + "WHERE department_id = ?";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
            ) {


                statement.setString(
                        1,
                        code.trim()
                );


                statement.setString(
                        2,
                        name.trim()
                );


                if (isEmpty(hod)) {

                    statement.setString(
                            3,
                            null
                    );

                } else {

                    statement.setString(
                            3,
                            hod.trim()
                    );
                }


                statement.setInt(
                        4,
                        departmentId
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    redirect(
                            response,
                            "notfound"
                    );

                    return;
                }
            }


            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "EDIT_DEPARTMENT",
                    "Updated department ID: "
                    + departmentId
            );


            redirectSuccess(
                    response,
                    "updated"
            );


        } catch (Exception e) {

            e.printStackTrace();


            if (isDuplicate(e)) {

                redirect(
                        response,
                        "duplicate"
                );

            } else {

                redirect(
                        response,
                        "database"
                );
            }


        } finally {

            close(connection);
        }
    }


    // =====================================================
    // DELETE DEPARTMENT
    // =====================================================

    private void deleteDepartment(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int departmentId =
                getInt(
                        request,
                        "department_id"
                );


        if (departmentId <= 0) {

            redirect(
                    response,
                    "invalid"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String departmentName =
                    getDepartmentName(
                            connection,
                            departmentId
                    );


            String sql =
                    "DELETE FROM departments "
                    + "WHERE department_id = ?";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
            ) {


                statement.setInt(
                        1,
                        departmentId
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    redirect(
                            response,
                            "notfound"
                    );

                    return;
                }
            }


            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "DELETE_DEPARTMENT",
                    "Deleted department: "
                    + departmentName
            );


            redirectSuccess(
                    response,
                    "deleted"
            );


        } catch (Exception e) {

            e.printStackTrace();


            redirect(
                    response,
                    "database"
            );


        } finally {

            close(connection);
        }
    }


    // =====================================================
    // CHANGE STATUS
    // =====================================================

    private void changeStatus(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int departmentId =
                getInt(
                        request,
                        "department_id"
                );


        String status =
                request.getParameter(
                        "status"
                );


        if (departmentId <= 0 ||
            isEmpty(status)) {

            redirect(
                    response,
                    "invalid"
            );

            return;
        }


        if (!status.equals("ACTIVE") &&
            !status.equals("INACTIVE")) {

            redirect(
                    response,
                    "invalid"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String sql =
                    "UPDATE departments "
                    + "SET status = ? "
                    + "WHERE department_id = ?";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
            ) {


                statement.setString(
                        1,
                        status
                );


                statement.setInt(
                        2,
                        departmentId
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    redirect(
                            response,
                            "notfound"
                    );

                    return;
                }
            }


            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "CHANGE_DEPARTMENT_STATUS",
                    "Changed department ID "
                    + departmentId
                    + " status to "
                    + status
            );


            redirectSuccess(
                    response,
                    "status"
            );


        } catch (Exception e) {

            e.printStackTrace();


            redirect(
                    response,
                    "database"
            );


        } finally {

            close(connection);
        }
    }


    // =====================================================
    // GET DEPARTMENT NAME
    // =====================================================

    private String getDepartmentName(
            Connection connection,
            int departmentId)
            throws Exception {

        String sql =
                "SELECT department_name "
                + "FROM departments "
                + "WHERE department_id = ?";


        try (
            PreparedStatement statement =
                    connection.prepareStatement(
                            sql
                    )
        ) {


            statement.setInt(
                    1,
                    departmentId
            );


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {


                if (result.next()) {

                    return result.getString(
                            "department_name"
                    );
                }
            }
        }


        return "Unknown";
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
                    connection.prepareStatement(
                            sql
                    )
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


        return message.contains(
                    "Duplicate"
                )
                ||
                message.contains(
                    "duplicate"
                )
                ||
                message.contains(
                    "UNIQUE"
                );
    }


    // =====================================================
    // REDIRECT
    // =====================================================

    private void redirect(
            HttpServletResponse response,
            String error)
            throws IOException {

        response.sendRedirect(
                "DepartmentServlet?error="
                + error
        );
    }


    // =====================================================
    // SUCCESS REDIRECT
    // =====================================================

    private void redirectSuccess(
            HttpServletResponse response,
            String success)
            throws IOException {

        response.sendRedirect(
                "DepartmentServlet?success="
                + success
        );
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