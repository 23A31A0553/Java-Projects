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

@WebServlet("/ActivityLogServlet")
public class ActivityLogServlet extends HttpServlet {

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

        loadActivityLogs(
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


        if ("clear".equalsIgnoreCase(action)) {

            clearLogs(
                    request,
                    response
            );

            return;
        }


        response.sendRedirect(
                "ActivityLogServlet"
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
    // LOAD ACTIVITY LOGS
    // =====================================================

    private void loadActivityLogs(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String search =
                request.getParameter(
                        "search"
                );


        String action =
                request.getParameter(
                        "action"
                );


        String date =
                request.getParameter(
                        "date"
                );


        if (search == null) {
            search = "";
        }


        if (action == null ||
            action.trim().isEmpty()) {

            action = "ALL";
        }


        if (date == null) {
            date = "";
        }


        List<Map<String, Object>> logs =
                new ArrayList<>();


        try (
            Connection connection =
                    DBConnection.getConnection()
        ) {


            StringBuilder sql =
                    new StringBuilder();


            sql.append(
                    "SELECT "
                    + "a.log_id, "
                    + "a.user_id, "
                    + "a.action, "
                    + "a.description, "
                    + "a.created_at, "
                    + "u.username, "
                    + "u.role "
                    + "FROM activity_logs a "
                    + "LEFT JOIN users u "
                    + "ON a.user_id = u.user_id "
                    + "WHERE 1 = 1 "
            );


            List<String> parameters =
                    new ArrayList<>();


            // =================================================
            // SEARCH
            // =================================================

            if (!search.trim().isEmpty()) {

                sql.append(
                        "AND ("
                        + "a.description LIKE ? "
                        + "OR a.action LIKE ? "
                        + "OR u.username LIKE ? "
                        + ") "
                );


                String searchValue =
                        "%"
                        + search.trim()
                        + "%";


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


            // =================================================
            // ACTION FILTER
            // =================================================

            if (!action.equalsIgnoreCase("ALL")) {

                sql.append(
                        "AND a.action = ? "
                );


                parameters.add(
                        action
                );
            }


            // =================================================
            // DATE FILTER
            // =================================================

            if (!date.trim().isEmpty()) {

                sql.append(
                        "AND DATE(a.created_at) = ? "
                );


                parameters.add(
                        date
                );
            }


            sql.append(
                    "ORDER BY a.log_id DESC"
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

                        Map<String, Object> log =
                                new HashMap<>();


                        log.put(
                                "logId",
                                result.getInt(
                                        "log_id"
                                )
                        );


                        log.put(
                                "userId",
                                result.getInt(
                                        "user_id"
                                )
                        );


                        log.put(
                                "username",
                                result.getString(
                                        "username"
                                )
                        );


                        log.put(
                                "role",
                                result.getString(
                                        "role"
                                )
                        );


                        log.put(
                                "action",
                                result.getString(
                                        "action"
                                )
                        );


                        log.put(
                                "description",
                                result.getString(
                                        "description"
                                )
                        );


                        log.put(
                                "createdAt",
                                result.getTimestamp(
                                        "created_at"
                                )
                        );


                        logs.add(
                                log
                        );
                    }
                }
            }


            request.setAttribute(
                    "logs",
                    logs
            );


            request.setAttribute(
                    "search",
                    search
            );


            request.setAttribute(
                    "selectedAction",
                    action
            );


            request.setAttribute(
                    "selectedDate",
                    date
            );


            // LOAD AVAILABLE ACTIONS

            List<String> actions =
                    getActions(
                            connection
                    );


            request.setAttribute(
                    "actions",
                    actions
            );


            request.getRequestDispatcher(
                    "activityLogs.jsp"
            ).forward(
                    request,
                    response
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load activity logs"
            );
        }
    }


    // =====================================================
    // GET ACTION TYPES
    // =====================================================

    private List<String> getActions(
            Connection connection)
            throws Exception {

        List<String> actions =
                new ArrayList<>();


        String sql =
                "SELECT DISTINCT action "
                + "FROM activity_logs "
                + "ORDER BY action";


        try (
            PreparedStatement statement =
                    connection.prepareStatement(
                            sql
                    );

            ResultSet result =
                    statement.executeQuery()
        ) {


            while (result.next()) {

                actions.add(
                        result.getString(
                                "action"
                        )
                );
            }
        }


        return actions;
    }


    // =====================================================
    // CLEAR LOGS
    // =====================================================

    private void clearLogs(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            /*
             * Do not delete the current admin's
             * activity record before recording
             * the clear operation.
             */

            String sql =
                    "DELETE FROM activity_logs";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
            ) {

                statement.executeUpdate();
            }


            /*
             * Since the table was cleared, the
             * current operation can now be logged.
             */

            logActivity(
                    connection,
                    getLoggedInUserId(request),
                    "CLEAR_ACTIVITY_LOG",
                    "Cleared all activity logs"
            );


            response.sendRedirect(
                    "ActivityLogServlet"
                    + "?success=cleared"
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                    "ActivityLogServlet"
                    + "?error=database"
            );


        } finally {

            close(connection);
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