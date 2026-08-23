<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList" %>
<%@ page import="com.university.servlet.User" %>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }


    ArrayList<User> users =
        (ArrayList<User>) request.getAttribute("users");


    if (users == null) {
        users = new ArrayList<User>();
    }


    String selectedRole =
        (String) request.getAttribute("selectedRole");


    String search =
        (String) request.getAttribute("search");


    if (selectedRole == null) {
        selectedRole = "";
    }


    if (search == null) {
        search = "";
    }


    String success =
        request.getParameter("success");


    String error =
        request.getParameter("error");
%>


<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        User Management - BLUE RIDGE UNIVERSITY
    </title>


    <link rel="stylesheet"
          href="css/admin.css">


    <style>

        /* =================================================
           CONTAINER
           ================================================= */

        .management-container {

            width: 100%;

            max-width: 1250px;

            margin: auto;
        }


        /* =================================================
           HEADER
           ================================================= */

        .page-card {

            background: white;

            padding: 25px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);

            margin-bottom: 20px;
        }


        .page-card h2 {

            color: #0d47a1;

            margin-bottom: 8px;
        }


        .page-card p {

            color: #666;

            margin: 0;
        }


        /* =================================================
           MESSAGES
           ================================================= */

        .message {

            padding: 13px 16px;

            border-radius: 6px;

            margin-bottom: 20px;

            font-size: 14px;
        }


        .success-message {

            background: #d4edda;

            color: #155724;
        }


        .error-message {

            background: #f8d7da;

            color: #721c24;
        }


        /* =================================================
           FILTER CARD
           ================================================= */

        .filter-card {

            background: white;

            padding: 20px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);

            margin-bottom: 20px;
        }


        .filter-form {

            display: grid;

            grid-template-columns:
                1fr 1fr auto auto;

            gap: 10px;

            align-items: end;
        }


        .filter-group label {

            display: block;

            color: #444;

            font-size: 12px;

            font-weight: bold;

            margin-bottom: 6px;
        }


        .filter-group input,
        .filter-group select {

            width: 100%;

            padding: 11px;

            border:
                1px solid #ccc;

            border-radius: 6px;

            font-size: 13px;

            outline: none;
        }


        .filter-group input:focus,
        .filter-group select:focus {

            border-color: #1565c0;
        }


        .search-button {

            padding: 11px 20px;

            border: none;

            border-radius: 6px;

            background: #1565c0;

            color: white;

            cursor: pointer;

            font-size: 13px;
        }


        .search-button:hover {

            background: #0d47a1;
        }


        .clear-button {

            padding: 11px 20px;

            border-radius: 6px;

            background: #eeeeee;

            color: #333;

            text-decoration: none;

            font-size: 13px;

            text-align: center;
        }


        .clear-button:hover {

            background: #dddddd;
        }


        /* =================================================
           FILTER BUTTONS
           ================================================= */

        .role-buttons {

            display: flex;

            gap: 8px;

            margin-top: 18px;

            flex-wrap: wrap;
        }


        .role-button {

            padding: 9px 15px;

            border:
                1px solid #1565c0;

            border-radius: 20px;

            color: #1565c0;

            text-decoration: none;

            font-size: 12px;

            background: white;
        }


        .role-button:hover {

            background: #e3f2fd;
        }


        .role-button.selected {

            background: #1565c0;

            color: white;
        }


        /* =================================================
           TABLE CARD
           ================================================= */

        .table-card {

            background: white;

            padding: 20px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);

            margin-bottom: 25px;
        }


        .table-header {

            display: flex;

            justify-content: space-between;

            align-items: center;

            margin-bottom: 18px;
        }


        .table-header h3 {

            color: #0d47a1;

            margin: 0;
        }


        .user-count {

            background: #e3f2fd;

            color: #1565c0;

            padding: 7px 12px;

            border-radius: 15px;

            font-size: 12px;

            font-weight: bold;
        }


        /* =================================================
           TABLE
           ================================================= */

        .table-wrapper {

            width: 100%;

            overflow-x: auto;
        }


        .user-table {

            width: 100%;

            min-width: 1100px;

            border-collapse: collapse;
        }


        .user-table th,
        .user-table td {

            padding: 12px;

            border-bottom:
                1px solid #ddd;

            text-align: left;

            font-size: 12px;

            vertical-align: middle;
        }


        .user-table th {

            background: #1565c0;

            color: white;

            white-space: nowrap;
        }


        .user-table tr:hover {

            background: #f7f9fc;
        }


        /* =================================================
           ROLE BADGES
           ================================================= */

        .role-badge {

            display: inline-block;

            padding: 5px 9px;

            border-radius: 15px;

            font-size: 10px;

            font-weight: bold;
        }


        .role-student {

            background: #e3f2fd;

            color: #1565c0;
        }


        .role-teacher {

            background: #fff3e0;

            color: #e65100;
        }


        .role-admin {

            background: #f3e5f5;

            color: #7b1fa2;
        }


        /* =================================================
           STATUS
           ================================================= */

        .status-badge {

            display: inline-block;

            padding: 5px 9px;

            border-radius: 15px;

            font-size: 10px;

            font-weight: bold;
        }


        .status-active {

            background: #d4edda;

            color: #155724;
        }


        .status-inactive {

            background: #eeeeee;

            color: #555;
        }


        /* =================================================
           ACTIONS
           ================================================= */

        .action-buttons {

            display: flex;

            gap: 5px;

            flex-wrap: wrap;
        }


        .action-form {

            display: inline;
        }


        .action-button {

            border: none;

            padding: 6px 9px;

            border-radius: 5px;

            cursor: pointer;

            font-size: 10px;
        }


        .activate-button {

            background: #d4edda;

            color: #155724;
        }


        .deactivate-button {

            background: #fff3cd;

            color: #856404;
        }


        .delete-button {

            background: #f8d7da;

            color: #721c24;
        }


        .reset-button {

            background: #e2e8f0;

            color: #475569;
        }


        .action-button:hover {

            opacity: 0.8;
        }


        /* =================================================
           EMPTY DATA
           ================================================= */

        .no-data {

            text-align: center;

            padding: 50px 20px;

            color: #777;
        }


        .back-button {

            display: inline-block;

            margin-top: 15px;

            padding: 10px 18px;

            background: #1565c0;

            color: white;

            text-decoration: none;

            border-radius: 6px;

            font-size: 13px;
        }


        .back-button:hover {

            background: #0d47a1;
        }


        /* =================================================
           FOOTER
           ================================================= */

        .footer {

            width: 100%;

            min-height: 45px;

            background: white;

            display: flex;

            justify-content: center;

            align-items: center;

            box-shadow:
                0 -2px 8px
                rgba(0,0,0,0.15);

            color: #555;

            font-size: 13px;
        }


        .footer p {

            margin: 0;
        }


        /* =================================================
           RESPONSIVE
           ================================================= */

        @media(max-width: 800px) {

            .filter-form {

                grid-template-columns:
                    1fr 1fr;
            }

        }


        @media(max-width: 550px) {

            .filter-form {

                grid-template-columns: 1fr;
            }


            .table-header {

                flex-direction: column;

                align-items: flex-start;

                gap: 10px;
            }

        }

    </style>

</head>


<body>


<!-- =====================================================
     TOP BAR
     ===================================================== -->

<header class="top-bar">


    <div class="college-section">


        <img
            src="images/logo.jpeg"
            alt="College Logo"
            class="college-logo">


        <div>

            <h1>
                BLUE RIDGE UNIVERSITY
            </h1>

            <p>
                University Management System
            </p>

        </div>

    </div>


    <div class="admin-section">


        <span class="admin-name">

            Administrator

        </span>


        <a
            href="LogoutServlet"
            class="logout-button">

            Logout

        </a>

    </div>

</header>



<!-- =====================================================
     MAIN CONTENT
     ===================================================== -->

<main class="main-content">


    <div class="management-container">


        <!-- =================================================
             MESSAGES
             ================================================= -->

        <% if ("activated".equals(success)) { %>

            <div class="message success-message">

                User activated successfully.

            </div>

        <% } %>


        <% if ("passwordreset".equals(success)) { %>

            <div class="message success-message">

                User password reset successfully.

            </div>

        <% } %>


        <% if ("deactivated".equals(success)) { %>

            <div class="message success-message">

                User deactivated successfully.

            </div>

        <% } %>


        <% if ("deleted".equals(success)) { %>

            <div class="message success-message">

                User deleted successfully.

            </div>

        <% } %>


        <% if ("selfdelete".equals(error)) { %>

            <div class="message error-message">

                You cannot delete your own administrator account.

            </div>

        <% } %>


        <% if ("notfound".equals(error)) { %>

            <div class="message error-message">

                User not found.

            </div>

        <% } %>


        <% if ("invalid".equals(error)) { %>

            <div class="message error-message">

                Invalid request.

            </div>

        <% } %>


        <% if ("database".equals(error)) { %>

            <div class="message error-message">

                Database error occurred.

            </div>

        <% } %>



        <!-- =================================================
             PAGE HEADER
             ================================================= -->

        <div class="page-card">

            <h2>
                User Management
            </h2>

            <p>
                Manage students, teachers and administrators.
            </p>

        </div>



        <!-- =================================================
             FILTER
             ================================================= -->

        <div class="filter-card">


            <form
                action="UserManagementServlet"
                method="get"
                class="filter-form">


                <!-- SEARCH -->

                <div class="filter-group">

                    <label>
                        Search
                    </label>

                    <input
                        type="text"
                        name="search"
                        value="<%= search %>"
                        placeholder="Username, name or email">

                </div>


                <!-- ROLE -->

                <div class="filter-group">

                    <label>
                        User Type
                    </label>

                    <select name="role">


                        <option
                            value=""
                            <%= selectedRole.isEmpty()
                                ? "selected"
                                : "" %>>

                            All Users

                        </option>


                        <option
                            value="STUDENT"
                            <%= "STUDENT".equalsIgnoreCase(
                                selectedRole)
                                ? "selected"
                                : "" %>>

                            Students

                        </option>


                        <option
                            value="TEACHER"
                            <%= "TEACHER".equalsIgnoreCase(
                                selectedRole)
                                ? "selected"
                                : "" %>>

                            Teachers

                        </option>


                        <option
                            value="ADMIN"
                            <%= "ADMIN".equalsIgnoreCase(
                                selectedRole)
                                ? "selected"
                                : "" %>>

                            Administrators

                        </option>


                    </select>

                </div>


                <!-- SEARCH BUTTON -->

                <button
                    type="submit"
                    class="search-button">

                    Search

                </button>


                <!-- CLEAR -->

                <a
                    href="UserManagementServlet"
                    class="clear-button">

                    Clear

                </a>


            </form>



            <!-- =================================================
                 QUICK ROLE FILTERS
                 ================================================= -->

            <div class="role-buttons">


                <a
                    href="UserManagementServlet"
                    class="role-button
                    <%= selectedRole.isEmpty()
                        ? "selected"
                        : "" %>">

                    All Users

                </a>


                <a
                    href="UserManagementServlet?role=STUDENT"
                    class="role-button
                    <%= "STUDENT".equalsIgnoreCase(
                        selectedRole)
                        ? "selected"
                        : "" %>">

                    Students

                </a>


                <a
                    href="UserManagementServlet?role=TEACHER"
                    class="role-button
                    <%= "TEACHER".equalsIgnoreCase(
                        selectedRole)
                        ? "selected"
                        : "" %>">

                    Teachers

                </a>


                <a
                    href="UserManagementServlet?role=ADMIN"
                    class="role-button
                    <%= "ADMIN".equalsIgnoreCase(
                        selectedRole)
                        ? "selected"
                        : "" %>">

                    Administrators

                </a>


            </div>


        </div>



        <!-- =================================================
             USER TABLE
             ================================================= -->

        <div class="table-card">


            <div class="table-header">


                <h3>
                    Registered Users
                </h3>


                <span class="user-count">

                    <%= users.size() %>
                    Users

                </span>


            </div>



            <div class="table-wrapper">


                <table class="user-table">


                    <thead>

                        <tr>

                            <th>
                                ID
                            </th>

                            <th>
                                Username
                            </th>

                            <th>
                                Name
                            </th>

                            <th>
                                Role
                            </th>

                            <th>
                                Email
                            </th>

                            <th>
                                Mobile
                            </th>

                            <th>
                                Department
                            </th>

                            <th>
                                Semester
                            </th>

                            <th>
                                Employee Type
                            </th>

                            <th>
                                Status
                            </th>

                            <th>
                                Actions
                            </th>

                        </tr>

                    </thead>


                    <tbody>


                    <%
                        if (users.isEmpty()) {
                    %>


                        <tr>

                            <td
                                colspan="11"
                                class="no-data">

                                <h3>
                                    No Users Found
                                </h3>

                                <p>
                                    Try changing the
                                    search or filter.
                                </p>

                            </td>

                        </tr>


                    <%
                        } else {

                            for (User user : users) {
                    %>


                        <tr>


                            <!-- ID -->

                            <td>

                                <%= user.getUserId() %>

                            </td>


                            <!-- USERNAME -->

                            <td>

                                <strong>

                                    <%= user.getUsername() == null
                                        ? ""
                                        : user.getUsername() %>

                                </strong>

                            </td>


                            <!-- NAME -->

                            <td>

                                <% if ("STUDENT".equalsIgnoreCase(user.getRole())) { %>
                                    <a href="TeacherStudentServlet?userId=<%= user.getUserId() %>" style="color: #1565c0; text-decoration: none; font-weight: 600;">
                                        <%= user.getFirstName() == null ? "" : user.getFirstName() %>
                                        <%= user.getLastName() == null ? "" : user.getLastName() %>
                                    </a>
                                <% } else { %>
                                    <%= user.getFirstName() == null ? "" : user.getFirstName() %>
                                    <%= user.getLastName() == null ? "" : user.getLastName() %>
                                <% } %>

                            </td>


                            <!-- ROLE -->

                            <td>


                                <% if (
                                    "STUDENT".equalsIgnoreCase(
                                        user.getRole())
                                ) { %>


                                    <span
                                        class="role-badge
                                               role-student">

                                        STUDENT

                                    </span>


                                <% } else if (
                                    "TEACHER".equalsIgnoreCase(
                                        user.getRole())
                                ) { %>


                                    <span
                                        class="role-badge
                                               role-teacher">

                                        TEACHER

                                    </span>


                                <% } else { %>


                                    <span
                                        class="role-badge
                                               role-admin">

                                        ADMIN

                                    </span>


                                <% } %>


                            </td>


                            <!-- EMAIL -->

                            <td>

                                <%= user.getEmail() == null
                                    ? ""
                                    : user.getEmail() %>

                            </td>


                            <!-- MOBILE -->

                            <td>

                                <%= user.getMobile() == null
                                    ? ""
                                    : user.getMobile() %>

                            </td>


                            <!-- DEPARTMENT -->

                            <td>

                                <%= user.getDepartment() == null
                                    ? "-"
                                    : user.getDepartment() %>

                            </td>


                            <!-- SEMESTER -->

                            <td>

                                <%= user.getSemester() == null
                                    ? "-"
                                    : user.getSemester() %>

                            </td>


                            <!-- EMPLOYEE TYPE -->

                            <td>

                                <%= user.getEmployeeType() == null
                                    ? "-"
                                    : user.getEmployeeType() %>

                            </td>


                            <!-- STATUS -->

                            <td>


                                <% if (
                                    "ACTIVE".equalsIgnoreCase(
                                        user.getStatus())
                                ) { %>


                                    <span
                                        class="status-badge
                                               status-active">

                                        ACTIVE

                                    </span>


                                <% } else { %>


                                    <span
                                        class="status-badge
                                               status-inactive">

                                        INACTIVE

                                    </span>


                                <% } %>


                            </td>


                            <!-- ACTIONS -->

                            <td>


                                <div class="action-buttons">


                                    <% if (
                                        "ACTIVE".equalsIgnoreCase(
                                            user.getStatus())
                                    ) { %>


                                        <form
                                            action="UserManagementServlet"
                                            method="post"
                                            class="action-form">


                                            <input
                                                type="hidden"
                                                name="action"
                                                value="deactivate">


                                            <input
                                                type="hidden"
                                                name="user_id"
                                                value="<%= user.getUserId() %>">


                                            <button
                                                type="submit"
                                                class="action-button
                                                       deactivate-button">

                                                Deactivate

                                            </button>


                                        </form>


                                    <% } else { %>


                                        <form
                                            action="UserManagementServlet"
                                            method="post"
                                            class="action-form">


                                            <input
                                                type="hidden"
                                                name="action"
                                                value="activate">


                                            <input
                                                type="hidden"
                                                name="user_id"
                                                value="<%= user.getUserId() %>">


                                            <button
                                                type="submit"
                                                class="action-button
                                                       activate-button">

                                                Activate

                                            </button>


                                        </form>


                                    <% } %>


                                    <!-- DELETE -->


                                    <form
                                        action="UserManagementServlet"
                                        method="post"
                                        class="action-form"
                                        onsubmit="return confirmDelete('<%= user.getUsername() %>');">


                                        <input
                                            type="hidden"
                                            name="action"
                                            value="delete">


                                        <input
                                            type="hidden"
                                            name="user_id"
                                            value="<%= user.getUserId() %>">


                                        <button
                                            type="submit"
                                            class="action-button
                                                   delete-button">

                                            Delete

                                        </button>


                                    </form>


                                    <!-- RESET PASSWORD -->

                                    <button
                                        type="button"
                                        class="action-button reset-button"
                                        onclick="resetPassword('<%= user.getUserId() %>', '<%= user.getUsername() %>');">

                                        Reset Pass

                                    </button>


                                </div>


                            </td>


                        </tr>


                    <%
                            }
                        }
                    %>


                    </tbody>


                </table>

            </div>


        </div>



        <!-- =================================================
             BACK TO ADMIN
             ================================================= -->

        <a
            href="AdminServlet"
            class="back-button">

            ← Back to Admin Dashboard

        </a>


    </div>

</main>



<!-- =====================================================
     FOOTER
     ===================================================== -->

<footer class="footer">

    <p>

        © 2026 BLUE RIDGE UNIVERSITY.
        All Rights Reserved. Made by Penugonda Devashish.

    </p>

</footer>



<script>

function confirmDelete(username) {

    return confirm(
        "Are you sure you want to delete user '" +
        username +
        "'?\n\n" +
        "This will also delete the related " +
        "student or teacher record."
    );
}


function resetPassword(userId, username) {

    var newPass = prompt("Enter new password for user '" + username + "':");

    if (newPass !== null && newPass.trim() !== "") {

        var form = document.createElement("form");

        form.method = "POST";

        form.action = "UserManagementServlet";


        var actionInput = document.createElement("input");

        actionInput.type = "hidden";

        actionInput.name = "action";

        actionInput.value = "resetPassword";

        form.appendChild(actionInput);


        var idInput = document.createElement("input");

        idInput.type = "hidden";

        idInput.name = "user_id";

        idInput.value = userId;

        form.appendChild(idInput);


        var passInput = document.createElement("input");

        passInput.type = "hidden";

        passInput.name = "password";

        passInput.value = newPass;

        form.appendChild(passInput);


        document.body.appendChild(form);

        form.submit();

    }

}

</script>


</body>

</html>