package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;

@WebServlet("/UserManagementServlet")
public class UserManagementServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String role =
                String.valueOf(session.getAttribute("role"));

        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String roleFilter = request.getParameter("role");
        String searchFilter = request.getParameter("search");

        List<User> users = new ArrayList<User>();

        int totalUsers = 0;
        int totalStudents = 0;
        int totalTeachers = 0;
        int activeUsers = 0;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            con = DBConnection.getConnection();

            String sql = "SELECT u.user_id, u.username, u.role, u.first_name, u.last_name, u.email, u.mobile, u.status, "
                       + "s.department AS student_dept, s.semester, "
                       + "t.department AS teacher_dept, t.employee_type "
                       + "FROM users u "
                       + "LEFT JOIN students s ON u.user_id = s.user_id "
                       + "LEFT JOIN teachers t ON u.user_id = t.user_id ";

            List<Object> params = new ArrayList<>();
            List<String> conditions = new ArrayList<>();

            if (roleFilter != null && !roleFilter.trim().isEmpty()) {
                conditions.add("u.role = ?");
                params.add(roleFilter.trim().toUpperCase());
            }

            if (searchFilter != null && !searchFilter.trim().isEmpty()) {
                String searchLike = "%" + searchFilter.trim() + "%";
                conditions.add("(u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?)");
                params.add(searchLike);
                params.add(searchLike);
                params.add(searchLike);
                params.add(searchLike);
            }

            if (!conditions.isEmpty()) {
                sql += "WHERE " + String.join(" AND ", conditions) + " ";
            }

            sql += "ORDER BY u.user_id DESC";

            ps = con.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setMobile(rs.getString("mobile"));
                user.setStatus(rs.getString("status"));

                if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                    user.setDepartment(rs.getString("student_dept"));
                    user.setSemester(rs.getString("semester"));
                    user.setEmployeeType(null);
                } else if ("TEACHER".equalsIgnoreCase(user.getRole())) {
                    user.setDepartment(rs.getString("teacher_dept"));
                    user.setSemester(null);
                    user.setEmployeeType(rs.getString("employee_type"));
                } else {
                    user.setDepartment(null);
                    user.setSemester(null);
                    user.setEmployeeType(null);
                }

                users.add(user);
            }

            rs.close();
            rs = null;

            ps.close();
            ps = null;

            request.setAttribute("search", searchFilter);


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


            request.setAttribute(
                "users",
                users
            );

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

            request.setAttribute(
                "selectedRole",
                roleFilter
            );


            request.getRequestDispatcher(
                "userManagement.jsp"
            ).forward(
                request,
                response
            );


        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                "AdminServlet?error=database"
            );

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || !"ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");

        if ("delete".equalsIgnoreCase(action)) {
            deleteUser(request, response);
        } else if ("activate".equalsIgnoreCase(action)) {
            updateUserStatus(request, response, "ACTIVE");
        } else if ("deactivate".equalsIgnoreCase(action)) {
            updateUserStatus(request, response, "INACTIVE");
        } else if ("resetPassword".equalsIgnoreCase(action)) {
            resetUserPassword(request, response);
        } else {
            response.sendRedirect("UserManagementServlet");
        }
    }

    private void updateUserStatus(
            HttpServletRequest request,
            HttpServletResponse response,
            String newStatus)
            throws IOException {

        String id = request.getParameter("user_id");
        if (id == null || id.trim().isEmpty()) {
            id = request.getParameter("id");
        }

        if (id == null || id.trim().isEmpty()) {
            response.sendRedirect("UserManagementServlet?error=invalid");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            String sql = "UPDATE users SET status = ? WHERE user_id = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, Integer.parseInt(id));

            int result = ps.executeUpdate();
            if (result > 0) {
                response.sendRedirect("UserManagementServlet?success=" + 
                    ("ACTIVE".equals(newStatus) ? "activated" : "deactivated"));
            } else {
                response.sendRedirect("UserManagementServlet?error=failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("UserManagementServlet?error=database");
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }

    private void resetUserPassword(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String id = request.getParameter("user_id");
        String newPass = request.getParameter("password");
        if (id == null || id.trim().isEmpty()) {
            id = request.getParameter("id");
        }

        if (id == null || id.trim().isEmpty() || newPass == null || newPass.trim().isEmpty()) {
            response.sendRedirect("UserManagementServlet?error=invalid");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            String sql = "UPDATE users SET password = ? WHERE user_id = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, newPass.trim());
            ps.setInt(2, Integer.parseInt(id));

            int result = ps.executeUpdate();
            if (result > 0) {
                response.sendRedirect("UserManagementServlet?success=passwordreset");
            } else {
                response.sendRedirect("UserManagementServlet?error=failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("UserManagementServlet?error=database");
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }


    private void deleteUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String id = request.getParameter("id");
        if (id == null || id.trim().isEmpty()) {
            id = request.getParameter("user_id");
        }

        if (id == null || id.trim().isEmpty()) {
            response.sendRedirect("UserManagementServlet?error=invalid");
            return;
        }

        Connection con = null;
        PreparedStatement checkPs = null;
        PreparedStatement deletePs = null;
        ResultSet rs = null;

        try {

            con =
                DBConnection.getConnection();


            String currentUsername =
                    String.valueOf(
                        request.getSession()
                               .getAttribute(
                                   "username"
                               )
                    );


            String checkSql =
                    "SELECT username, role "
                    + "FROM users "
                    + "WHERE user_id = ?";

            checkPs =
                    con.prepareStatement(checkSql);

            checkPs.setInt(
                1,
                Integer.parseInt(id)
            );

            rs =
                    checkPs.executeQuery();


            if (!rs.next()) {

                response.sendRedirect(
                    "UserManagementServlet?error=notfound"
                );

                return;
            }


            String username =
                    rs.getString("username");

            String userRole =
                    rs.getString("role");


            if ("ADMIN".equalsIgnoreCase(userRole) &&
                username.equals(currentUsername)) {

                response.sendRedirect(
                    "UserManagementServlet?error=selfdelete"
                );

                return;
            }


            rs.close();
            rs = null;

            checkPs.close();
            checkPs = null;


            String deleteSql =
                    "DELETE FROM users WHERE user_id = ?";

            deletePs =
                    con.prepareStatement(deleteSql);

            deletePs.setInt(
                1,
                Integer.parseInt(id)
            );

            int result =
                    deletePs.executeUpdate();


            if (result > 0) {

                response.sendRedirect(
                    "UserManagementServlet?success=deleted"
                );

            } else {

                response.sendRedirect(
                    "UserManagementServlet?error=failed"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                "UserManagementServlet?error=database"
            );

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (checkPs != null) {
                    checkPs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (deletePs != null) {
                    deletePs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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
