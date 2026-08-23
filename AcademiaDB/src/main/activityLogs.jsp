<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Map<String, Object>> logs =
            (List<Map<String, Object>>) request.getAttribute("logs");

    String selectedRole =
            (String) request.getAttribute("selectedRole");

    String selectedAction =
            (String) request.getAttribute("selectedAction");

    String search =
            (String) request.getAttribute("search");

    if (selectedRole == null) {
        selectedRole = "";
    }

    if (selectedAction == null) {
        selectedAction = "";
    }

    if (search == null) {
        search = "";
    }
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Activity Logs - BLUE RIDGE UNIVERSITY</title>

    <link rel="stylesheet" href="css/admin.css">

    <style>

        .logs-container {
            width: 100%;
            max-width: 1300px;
            margin: auto;
        }

        .page-header {
            margin-bottom: 25px;
        }

        .page-header h2 {
            color: #0d47a1;
            margin-bottom: 8px;
        }

        .page-header p {
            color: #666;
        }

        .filter-card {
            background: white;
            padding: 22px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
            margin-bottom: 25px;
        }

        .filter-card h3 {
            color: #0d47a1;
            margin-bottom: 18px;
        }

        .filter-form {
            display: grid;
            grid-template-columns:
                1.5fr 1fr 1fr auto auto;
            gap: 12px;
            align-items: end;
        }

        .filter-group label {
            display: block;
            font-weight: bold;
            color: #333;
            margin-bottom: 6px;
        }

        .filter-group input,
        .filter-group select {
            width: 100%;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 14px;
            background: white;
        }

        .filter-button {
            background: #1565c0;
            color: white;
            border: none;
            padding: 10px 18px;
            border-radius: 6px;
            cursor: pointer;
        }

        .filter-button:hover {
            background: #0d47a1;
        }

        .clear-button {
            background: #eeeeee;
            color: #333;
            border: none;
            padding: 10px 18px;
            border-radius: 6px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }

        .clear-button:hover {
            background: #dddddd;
        }

        .log-summary {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }

        .log-summary h3 {
            color: #333;
        }

        .export-button {
            background: #2e7d32;
            color: white;
            padding: 9px 15px;
            border-radius: 6px;
            text-decoration: none;
        }

        .export-button:hover {
            background: #1b5e20;
        }

        .table-card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
            overflow-x: auto;
        }

        .logs-table {
            width: 100%;
            border-collapse: collapse;
        }

        .logs-table th,
        .logs-table td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
            text-align: left;
            vertical-align: top;
        }

        .logs-table th {
            background: #1565c0;
            color: white;
            white-space: nowrap;
        }

        .logs-table tr:hover {
            background: #f7f9fc;
        }

        .role-badge,
        .action-badge {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 14px;
            font-size: 11px;
            font-weight: bold;
            white-space: nowrap;
        }

        .admin-role {
            background: #e3f2fd;
            color: #0d47a1;
        }

        .teacher-role {
            background: #fff3e0;
            color: #e65100;
        }

        .student-role {
            background: #e8f5e9;
            color: #1b5e20;
        }

        .default-role {
            background: #eeeeee;
            color: #555;
        }

        .action-badge {
            background: #f1f3f4;
            color: #333;
        }

        .description {
            max-width: 350px;
            line-height: 1.5;
            color: #555;
        }

        .date-time {
            white-space: nowrap;
            color: #666;
            font-size: 13px;
        }

        .no-data {
            text-align: center;
            padding: 50px 20px;
            color: #777;
        }

        .no-data h3 {
            color: #555;
            margin-bottom: 8px;
        }

        @media(max-width: 900px) {

            .filter-form {
                grid-template-columns: 1fr 1fr;
            }

        }

        @media(max-width: 600px) {

            .filter-form {
                grid-template-columns: 1fr;
            }

            .log-summary {
                flex-direction: column;
                align-items: flex-start;
                gap: 12px;
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

            <h1>BLUE RIDGE UNIVERSITY</h1>

            <p>University Management System</p>

        </div>

    </div>


    <div class="admin-section">

        <a
            href="AdminServlet"
            class="profile-button">

            Dashboard

        </a>


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

<div class="main-content">

    <div class="logs-container">


        <!-- =================================================
             HEADER
             ================================================= -->

        <div class="page-header">

            <h2>Activity Logs</h2>

            <p>
                Monitor important activities performed
                by administrators, teachers and students.
            </p>

        </div>



        <!-- =================================================
             FILTER
             ================================================= -->

        <div class="filter-card">

            <h3>Filter Activity</h3>


            <form
                action="ActivityLogServlet"
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
                        value="<%= escapeHtml(search) %>"
                        placeholder="Username or description">

                </div>



                <!-- ROLE -->

                <div class="filter-group">

                    <label>
                        Role
                    </label>

                    <select name="role">

                        <option value="">
                            All Roles
                        </option>

                        <option
                            value="ADMIN"
                            <%= "ADMIN".equalsIgnoreCase(
                                selectedRole
                            ) ? "selected" : "" %>>

                            Admin

                        </option>

                        <option
                            value="TEACHER"
                            <%= "TEACHER".equalsIgnoreCase(
                                selectedRole
                            ) ? "selected" : "" %>>

                            Teacher

                        </option>

                        <option
                            value="STUDENT"
                            <%= "STUDENT".equalsIgnoreCase(
                                selectedRole
                            ) ? "selected" : "" %>>

                            Student

                        </option>

                    </select>

                </div>



                <!-- ACTION -->

                <div class="filter-group">

                    <label>
                        Action
                    </label>

                    <select name="action">

                        <option value="">
                            All Actions
                        </option>

                        <option
                            value="LOGIN"
                            <%= "LOGIN".equalsIgnoreCase(
                                selectedAction
                            ) ? "selected" : "" %>>

                            Login

                        </option>

                        <option
                            value="LOGOUT"
                            <%= "LOGOUT".equalsIgnoreCase(
                                selectedAction
                            ) ? "selected" : "" %>>

                            Logout

                        </option>

                        <option
                            value="ADD_STUDENT"
                            <%= "ADD_STUDENT".equalsIgnoreCase(
                                selectedAction
                            ) ? "selected" : "" %>>

                            Add Student

                        </option>

                        <option
                            value="ADD_TEACHER"
                            <%= "ADD_TEACHER".equalsIgnoreCase(
                                selectedAction
                            ) ? "selected" : "" %>>

                            Add Teacher

                        </option>

                        <option
                            value="UPDATE_PROFILE"
                            <%= "UPDATE_PROFILE".equalsIgnoreCase(
                                selectedAction
                            ) ? "selected" : "" %>>

                            Update Profile

                        </option>

                        <option
                            value="CHANGE_PASSWORD"
                            <%= "CHANGE_PASSWORD".equalsIgnoreCase(
                                selectedAction
                            ) ? "selected" : "" %>>

                            Change Password

                        </option>

                        <option
                            value="DELETE_USER"
                            <%= "DELETE_USER".equalsIgnoreCase(
                                selectedAction
                            ) ? "selected" : "" %>>

                            Delete User

                        </option>

                    </select>

                </div>



                <!-- FILTER BUTTON -->

                <div>

                    <button
                        type="submit"
                        class="filter-button">

                        Filter

                    </button>

                </div>



                <!-- CLEAR BUTTON -->

                <div>

                    <a
                        href="ActivityLogServlet"
                        class="clear-button">

                        Clear

                    </a>

                </div>

            </form>

        </div>



        <!-- =================================================
             SUMMARY
             ================================================= -->

        <div class="log-summary">

            <h3>
                System Activity
            </h3>


            <a
                href="ExportServlet?type=activity"
                class="export-button">

                Export CSV

            </a>

        </div>



        <!-- =================================================
             LOG TABLE
             ================================================= -->

        <div class="table-card">

            <table class="logs-table">

                <thead>

                    <tr>

                        <th>
                            Log ID
                        </th>

                        <th>
                            User
                        </th>

                        <th>
                            Role
                        </th>

                        <th>
                            Action
                        </th>

                        <th>
                            Description
                        </th>

                        <th>
                            Date & Time
                        </th>

                    </tr>

                </thead>


                <tbody>


                <%
                    if (logs != null &&
                        !logs.isEmpty()) {

                        for (
                            Map<String, Object> log :
                            logs
                        ) {

                            Object logId =
                                log.get("logId");

                            String username =
                                String.valueOf(
                                    log.get("username")
                                );

                            String userId =
                                String.valueOf(
                                    log.get("userId")
                                );

                            String logRole =
                                String.valueOf(
                                    log.get("role")
                                );

                            String action =
                                String.valueOf(
                                    log.get("action")
                                );

                            String description =
                                String.valueOf(
                                    log.get("description")
                                );

                            Object createdAt =
                                log.get("createdAt");
                %>


                    <tr>


                        <!-- LOG ID -->

                        <td>
                            <strong>
                                <%= logId %>
                            </strong>
                        </td>



                        <!-- USER -->

                        <td>

                            <strong>
                                <%= escapeHtml(username) %>
                            </strong>

                            <br>

                            <small>
                                ID:
                                <%= escapeHtml(userId) %>
                            </small>

                        </td>



                        <!-- ROLE -->

                        <td>

                            <%
                                String roleClass =
                                    "default-role";

                                if (
                                    "ADMIN".equalsIgnoreCase(
                                        logRole
                                    )
                                ) {

                                    roleClass =
                                        "admin-role";

                                } else if (
                                    "TEACHER".equalsIgnoreCase(
                                        logRole
                                    )
                                ) {

                                    roleClass =
                                        "teacher-role";

                                } else if (
                                    "STUDENT".equalsIgnoreCase(
                                        logRole
                                    )
                                ) {

                                    roleClass =
                                        "student-role";
                                }
                            %>


                            <span
                                class="role-badge <%= roleClass %>">

                                <%= escapeHtml(logRole) %>

                            </span>

                        </td>



                        <!-- ACTION -->

                        <td>

                            <span
                                class="action-badge">

                                <%= escapeHtml(action) %>

                            </span>

                        </td>



                        <!-- DESCRIPTION -->

                        <td>

                            <div
                                class="description">

                                <%= escapeHtml(
                                    description
                                ) %>

                            </div>

                        </td>



                        <!-- DATE -->

                        <td>

                            <span
                                class="date-time">

                                <%= createdAt %>

                            </span>

                        </td>


                    </tr>


                <%
                        }

                    } else {
                %>


                    <tr>

                        <td
                            colspan="6"
                            class="no-data">

                            <h3>
                                No activity found
                            </h3>

                            <p>
                                No activity logs match
                                the selected filters.
                            </p>

                        </td>

                    </tr>


                <%
                    }
                %>


                </tbody>

            </table>

        </div>

    </div>

</div>



<!-- =====================================================
     FOOTER
     ===================================================== -->

<footer class="footer">

    <p>
        © 2026 BLUE RIDGE UNIVERSITY.
        All Rights Reserved. Made by Penugonda Devashish.
    </p>

</footer>


</body>

</html>


<%!

private String escapeHtml(
        String value) {

    if (value == null) {
        return "";
    }

    return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;");
}

%>