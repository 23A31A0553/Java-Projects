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

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {

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


        if (session == null) {

            response.sendRedirect(
                    "index.jsp"
            );

            return;
        }


        Object userIdObject =
                session.getAttribute(
                        "userId"
                );


        if (userIdObject == null) {

            response.sendRedirect(
                    "index.jsp"
            );

            return;
        }


        int userId;


        try {

            userId =
                    Integer.parseInt(
                            String.valueOf(
                                    userIdObject
                            )
                    );

        } catch (Exception e) {

            response.sendRedirect(
                    "index.jsp"
            );

            return;
        }


        loadProfile(
                request,
                response,
                userId
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


        if (session == null) {

            response.sendRedirect(
                    "index.jsp"
            );

            return;
        }


        Object userIdObject =
                session.getAttribute(
                        "userId"
                );


        if (userIdObject == null) {

            response.sendRedirect(
                    "index.jsp"
            );

            return;
        }


        int userId;


        try {

            userId =
                    Integer.parseInt(
                            String.valueOf(
                                    userIdObject
                            )
                    );

        } catch (Exception e) {

            response.sendRedirect(
                    "index.jsp"
            );

            return;
        }


        String action =
                request.getParameter(
                        "action"
                );


        if (action == null) {

            response.sendRedirect(
                    "ProfileServlet"
            );

            return;
        }


        switch (action) {

            case "update":

                updateProfile(
                        request,
                        response,
                        userId
                );

                break;


            case "changePassword":

                changePassword(
                        request,
                        response,
                        userId
                );

                break;


            default:

                response.sendRedirect(
                        "ProfileServlet"
                );

                break;
        }
    }


    // =====================================================
    // LOAD PROFILE
    // =====================================================

    private void loadProfile(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId)
            throws ServletException, IOException {

        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String role =
                    getUserRole(
                            connection,
                            userId
                    );


            if (role == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "User not found"
                );

                return;
            }


            request.setAttribute(
                    "role",
                    role
            );


            if ("STUDENT".equalsIgnoreCase(role)) {

                loadStudentProfile(
                        request,
                        connection,
                        userId
                );

            } else if (
                    "TEACHER".equalsIgnoreCase(role)) {

                loadTeacherProfile(
                        request,
                        connection,
                        userId
                );

            } else if (
                    "ADMIN".equalsIgnoreCase(role)) {

                loadAdminProfile(
                        request,
                        connection,
                        userId
                );
            }


            request.getRequestDispatcher(
                    "profile.jsp"
            ).forward(
                    request,
                    response
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load profile"
            );


        } finally {

            close(connection);
        }
    }


    // =====================================================
    // STUDENT PROFILE
    // =====================================================

    private void loadStudentProfile(
            HttpServletRequest request,
            Connection connection,
            int userId)
            throws Exception {

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
                + "WHERE u.user_id = ? "
                + "AND u.role = 'STUDENT'";


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


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                if (result.next()) {

                    request.setAttribute(
                            "userId",
                            result.getInt(
                                    "user_id"
                            )
                    );

                    request.setAttribute(
                            "username",
                            result.getString(
                                    "username"
                            )
                    );

                    request.setAttribute(
                            "status",
                            result.getString(
                                    "status"
                            )
                    );

                    request.setAttribute(
                            "studentId",
                            result.getInt(
                                    "student_id"
                            )
                    );

                    request.setAttribute(
                            "firstName",
                            result.getString(
                                    "first_name"
                            )
                    );

                    request.setAttribute(
                            "lastName",
                            result.getString(
                                    "last_name"
                            )
                    );

                    request.setAttribute(
                            "email",
                            result.getString(
                                    "email"
                            )
                    );

                    request.setAttribute(
                            "mobile",
                            result.getString(
                                    "mobile"
                            )
                    );

                    request.setAttribute(
                            "department",
                            result.getString(
                                    "department"
                            )
                    );

                    request.setAttribute(
                            "semester",
                            result.getInt(
                                    "semester"
                            )
                    );
                }
            }
        }
    }


    // =====================================================
    // TEACHER PROFILE
    // =====================================================

    private void loadTeacherProfile(
            HttpServletRequest request,
            Connection connection,
            int userId)
            throws Exception {

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
                + "WHERE u.user_id = ? "
                + "AND u.role = 'TEACHER'";


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


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                if (result.next()) {

                    request.setAttribute(
                            "userId",
                            result.getInt(
                                    "user_id"
                            )
                    );

                    request.setAttribute(
                            "username",
                            result.getString(
                                    "username"
                            )
                    );

                    request.setAttribute(
                            "status",
                            result.getString(
                                    "status"
                            )
                    );

                    request.setAttribute(
                            "teacherId",
                            result.getInt(
                                    "teacher_id"
                            )
                    );

                    request.setAttribute(
                            "firstName",
                            result.getString(
                                    "first_name"
                            )
                    );

                    request.setAttribute(
                            "lastName",
                            result.getString(
                                    "last_name"
                            )
                    );

                    request.setAttribute(
                            "email",
                            result.getString(
                                    "email"
                            )
                    );

                    request.setAttribute(
                            "mobile",
                            result.getString(
                                    "mobile"
                            )
                    );

                    request.setAttribute(
                            "department",
                            result.getString(
                                    "department"
                            )
                    );
                }
            }
        }
    }


    // =====================================================
    // ADMIN PROFILE
    // =====================================================

    private void loadAdminProfile(
            HttpServletRequest request,
            Connection connection,
            int userId)
            throws Exception {

        String sql =
                "SELECT "
                + "user_id, "
                + "username, "
                + "status "
                + "FROM users "
                + "WHERE user_id = ? "
                + "AND role = 'ADMIN'";


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


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                if (result.next()) {

                    request.setAttribute(
                            "userId",
                            result.getInt(
                                    "user_id"
                            )
                    );

                    request.setAttribute(
                            "username",
                            result.getString(
                                    "username"
                            )
                    );

                    request.setAttribute(
                            "status",
                            result.getString(
                                    "status"
                            )
                    );
                }
            }
        }
    }


    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    private void updateProfile(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId)
            throws IOException {

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


        if (isEmpty(firstName) ||
            isEmpty(lastName) ||
            isEmpty(email) ||
            isEmpty(mobile)) {

            response.sendRedirect(
                    "ProfileServlet?error=empty"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String role =
                    getUserRole(
                            connection,
                            userId
                    );


            if (role == null) {

                response.sendRedirect(
                        "index.jsp"
                );

                return;
            }


            if ("STUDENT".equalsIgnoreCase(role)) {

                updateStudentProfile(
                        connection,
                        userId,
                        firstName,
                        lastName,
                        email,
                        mobile
                );

            } else if (
                    "TEACHER".equalsIgnoreCase(role)) {

                updateTeacherProfile(
                        connection,
                        userId,
                        firstName,
                        lastName,
                        email,
                        mobile
                );

            } else if (
                    "ADMIN".equalsIgnoreCase(role)) {

                /*
                 * Admin accounts in the current
                 * database structure do not have
                 * a separate profile table.
                 *
                 * Therefore only username is
                 * handled separately for Admin.
                 */

                updateAdminProfile(
                        connection,
                        userId,
                        email
                );
            }


            logActivity(
                    connection,
                    userId,
                    "UPDATE_PROFILE",
                    "Updated own profile"
            );


            response.sendRedirect(
                    "ProfileServlet?success=updated"
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                    "ProfileServlet?error=database"
            );


        } finally {

            close(connection);
        }
    }


    // =====================================================
    // UPDATE STUDENT
    // =====================================================

    private void updateStudentProfile(
            Connection connection,
            int userId,
            String firstName,
            String lastName,
            String email,
            String mobile)
            throws Exception {

        String sql =
                "UPDATE students SET "
                + "first_name = ?, "
                + "last_name = ?, "
                + "email = ?, "
                + "mobile = ? "
                + "WHERE user_id = ?";


        try (
            PreparedStatement statement =
                    connection.prepareStatement(
                            sql
                    )
        ) {

            statement.setString(
                    1,
                    firstName.trim()
            );

            statement.setString(
                    2,
                    lastName.trim()
            );

            statement.setString(
                    3,
                    email.trim()
            );

            statement.setString(
                    4,
                    mobile.trim()
            );

            statement.setInt(
                    5,
                    userId
            );

            statement.executeUpdate();
        }
    }


    // =====================================================
    // UPDATE TEACHER
    // =====================================================

    private void updateTeacherProfile(
            Connection connection,
            int userId,
            String firstName,
            String lastName,
            String email,
            String mobile)
            throws Exception {

        String sql =
                "UPDATE teachers SET "
                + "first_name = ?, "
                + "last_name = ?, "
                + "email = ?, "
                + "mobile = ? "
                + "WHERE user_id = ?";


        try (
            PreparedStatement statement =
                    connection.prepareStatement(
                            sql
                    )
        ) {

            statement.setString(
                    1,
                    firstName.trim()
            );

            statement.setString(
                    2,
                    lastName.trim()
            );

            statement.setString(
                    3,
                    email.trim()
            );

            statement.setString(
                    4,
                    mobile.trim()
            );

            statement.setInt(
                    5,
                    userId
            );

            statement.executeUpdate();
        }
    }


    // =====================================================
    // UPDATE ADMIN
    // =====================================================

    private void updateAdminProfile(
            Connection connection,
            int userId,
            String email)
            throws Exception {

        /*
         * Your current users table does not
         * contain an email column in the
         * structure used by the previous
         * servlets.
         *
         * So this method intentionally does
         * not modify users.email.
         */
    }


    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    private void changePassword(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId)
            throws IOException {

        String currentPassword =
                request.getParameter(
                        "current_password"
                );

        String newPassword =
                request.getParameter(
                        "new_password"
                );

        String confirmPassword =
                request.getParameter(
                        "confirm_password"
                );


        if (isEmpty(currentPassword) ||
            isEmpty(newPassword) ||
            isEmpty(confirmPassword)) {

            response.sendRedirect(
                    "ProfileServlet?error=passwordempty"
            );

            return;
        }


        if (newPassword.length() < 6) {

            response.sendRedirect(
                    "ProfileServlet?error=passwordlength"
            );

            return;
        }


        if (!newPassword.equals(
                confirmPassword
        )) {

            response.sendRedirect(
                    "ProfileServlet?error=passwordmatch"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String oldPassword =
                    getPassword(
                            connection,
                            userId
                    );


            if (oldPassword == null ||
                !oldPassword.equals(
                        currentPassword
                )) {

                response.sendRedirect(
                        "ProfileServlet?error=wrongpassword"
                );

                return;
            }


            String sql =
                    "UPDATE users "
                    + "SET password = ? "
                    + "WHERE user_id = ?";


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
            ) {

                statement.setString(
                        1,
                        newPassword
                );

                statement.setInt(
                        2,
                        userId
                );

                statement.executeUpdate();
            }


            logActivity(
                    connection,
                    userId,
                    "CHANGE_PASSWORD",
                    "Changed own password"
            );


            response.sendRedirect(
                    "ProfileServlet?success=password"
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.sendRedirect(
                    "ProfileServlet?error=database"
            );


        } finally {

            close(connection);
        }
    }


    // =====================================================
    // GET USER ROLE
    // =====================================================

    private String getUserRole(
            Connection connection,
            int userId)
            throws Exception {

        String sql =
                "SELECT role "
                + "FROM users "
                + "WHERE user_id = ?";


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


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                if (result.next()) {

                    return result.getString(
                            "role"
                    );
                }
            }
        }


        return null;
    }


    // =====================================================
    // GET PASSWORD
    // =====================================================

    private String getPassword(
            Connection connection,
            int userId)
            throws Exception {

        String sql =
                "SELECT password "
                + "FROM users "
                + "WHERE user_id = ?";


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


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                if (result.next()) {

                    return result.getString(
                            "password"
                    );
                }
            }
        }


        return null;
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
    // EMPTY CHECK
    // =====================================================

    private boolean isEmpty(
            String value) {

        return value == null ||
               value.trim().isEmpty();
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