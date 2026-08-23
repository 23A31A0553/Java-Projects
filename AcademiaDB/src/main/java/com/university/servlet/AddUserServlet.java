package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;

@WebServlet("/AddUserServlet")
public class AddUserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


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

            response.sendRedirect("index.jsp");
            return;
        }


        request.setCharacterEncoding("UTF-8");


        // =================================================
        // GET FORM DATA
        // =================================================

        String username =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        String firstName =
                request.getParameter("first_name");

        String lastName =
                request.getParameter("last_name");

        String email =
                request.getParameter("email");

        String mobile =
                request.getParameter("mobile");

        String userRole =
                request.getParameter("role");

        String department =
                request.getParameter("department");

        String semester =
                request.getParameter("semester");

        String employeeType =
                request.getParameter("employee_type");


        // =================================================
        // VALIDATION
        // =================================================

        if (username == null ||
            password == null ||
            firstName == null ||
            lastName == null ||
            email == null ||
            mobile == null ||
            userRole == null) {

            response.sendRedirect(
                "AdminServlet?error=empty"
            );

            return;
        }


        username = username.trim();
        password = password.trim();
        firstName = firstName.trim();
        lastName = lastName.trim();
        email = email.trim();
        mobile = mobile.trim();
        userRole = userRole.trim().toUpperCase();


        if (department != null) {
            department = department.trim();
        }

        if (semester != null) {
            semester = semester.trim();
        }

        if (employeeType != null) {
            employeeType = employeeType.trim();
        }


        if (username.isEmpty() ||
            password.isEmpty() ||
            firstName.isEmpty() ||
            lastName.isEmpty() ||
            email.isEmpty() ||
            mobile.isEmpty()) {

            response.sendRedirect(
                "AdminServlet?error=empty"
            );

            return;
        }


        // =================================================
        // VALID ROLE
        // =================================================

        if (!"STUDENT".equals(userRole) &&
            !"TEACHER".equals(userRole)) {

            response.sendRedirect(
                "AdminServlet?error=invalidrole"
            );

            return;
        }


        // =================================================
        // DEPARTMENT
        // =================================================

        if (department == null ||
            department.isEmpty()) {

            response.sendRedirect(
                "AdminServlet?error=department"
            );

            return;
        }


        // =================================================
        // STUDENT SEMESTER
        // =================================================

        if ("STUDENT".equals(userRole)) {

            if (semester == null ||
                semester.isEmpty()) {

                response.sendRedirect(
                    "AdminServlet?error=semester"
                );

                return;
            }
        }


        // =================================================
        // TEACHER EMPLOYEE TYPE
        // =================================================

        if ("TEACHER".equals(userRole)) {

            if (employeeType == null ||
                employeeType.isEmpty()) {

                employeeType =
                    "Teaching Faculty";
            }
        }


        Connection con = null;


        try {

            con = DBConnection.getConnection();


            // =================================================
            // CHECK USERNAME
            // =================================================

            String checkUsernameSql =
                "SELECT user_id " +
                "FROM users " +
                "WHERE username = ?";


            PreparedStatement checkUsername =
                con.prepareStatement(
                    checkUsernameSql
                );


            checkUsername.setString(
                1,
                username
            );


            ResultSet usernameResult =
                checkUsername.executeQuery();


            if (usernameResult.next()) {

                usernameResult.close();
                checkUsername.close();
                con.close();


                response.sendRedirect(
                    "AdminServlet?error=usernameexists"
                );

                return;
            }


            usernameResult.close();
            checkUsername.close();


            // =================================================
            // CHECK EMAIL
            // =================================================

            String checkEmailSql =
                "SELECT user_id " +
                "FROM users " +
                "WHERE email = ?";


            PreparedStatement checkEmail =
                con.prepareStatement(
                    checkEmailSql
                );


            checkEmail.setString(
                1,
                email
            );


            ResultSet emailResult =
                checkEmail.executeQuery();


            if (emailResult.next()) {

                emailResult.close();
                checkEmail.close();
                con.close();


                response.sendRedirect(
                    "AdminServlet?error=emailexists"
                );

                return;
            }


            emailResult.close();
            checkEmail.close();


            // =================================================
            // START TRANSACTION
            // =================================================

            con.setAutoCommit(false);


            try {

                // =============================================
                // INSERT USER
                // =============================================

                String userSql =
                    "INSERT INTO users " +
                    "(username, password, role, " +
                    "first_name, last_name, email, " +
                    "mobile, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";


                PreparedStatement userStatement =
                    con.prepareStatement(
                        userSql,
                        Statement.RETURN_GENERATED_KEYS
                    );


                userStatement.setString(
                    1,
                    username
                );

                userStatement.setString(
                    2,
                    password
                );

                userStatement.setString(
                    3,
                    userRole
                );

                userStatement.setString(
                    4,
                    firstName
                );

                userStatement.setString(
                    5,
                    lastName
                );

                userStatement.setString(
                    6,
                    email
                );

                userStatement.setString(
                    7,
                    mobile
                );


                userStatement.executeUpdate();


                // =============================================
                // GET USER ID
                // =============================================

                ResultSet generatedKeys =
                    userStatement.getGeneratedKeys();


                int userId;


                if (generatedKeys.next()) {

                    userId =
                        generatedKeys.getInt(1);

                } else {

                    throw new Exception(
                        "Unable to create user ID"
                    );
                }


                generatedKeys.close();
                userStatement.close();


                // =============================================
                // STUDENT
                // =============================================

                if ("STUDENT".equals(userRole)) {

                    String studentSql =
                        "INSERT INTO students " +
                        "(user_id, department, semester) " +
                        "VALUES (?, ?, ?)";


                    PreparedStatement studentStatement =
                        con.prepareStatement(
                            studentSql
                        );


                    studentStatement.setInt(
                        1,
                        userId
                    );


                    studentStatement.setString(
                        2,
                        department
                    );


                    studentStatement.setString(
                        3,
                        semester
                    );


                    studentStatement.executeUpdate();

                    studentStatement.close();
                }


                // =============================================
                // TEACHER
                // =============================================

                if ("TEACHER".equals(userRole)) {

                    String teacherSql =
                        "INSERT INTO teachers " +
                        "(user_id, department, employee_type) " +
                        "VALUES (?, ?, ?)";


                    PreparedStatement teacherStatement =
                        con.prepareStatement(
                            teacherSql
                        );


                    teacherStatement.setInt(
                        1,
                        userId
                    );


                    teacherStatement.setString(
                        2,
                        department
                    );


                    teacherStatement.setString(
                        3,
                        employeeType
                    );


                    teacherStatement.executeUpdate();

                    teacherStatement.close();
                }


                // =============================================
                // COMMIT
                // =============================================

                con.commit();

                con.setAutoCommit(true);

                con.close();


                // =============================================
                // SUCCESS
                // =============================================

                response.sendRedirect(
                    "AdminServlet?success=useradded"
                );


            } catch (Exception e) {

                try {

                    con.rollback();

                } catch (Exception rollbackError) {

                    rollbackError.printStackTrace();
                }

                throw e;
            }


        } catch (Exception e) {

            e.printStackTrace();


            if (con != null) {

                try {

                    con.close();

                } catch (Exception closeError) {

                    closeError.printStackTrace();
                }
            }


            response.sendRedirect(
                "AdminServlet?error=database"
            );
        }
    }
}