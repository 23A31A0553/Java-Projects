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

    List<Map<String, Object>> departments =
            (List<Map<String, Object>>) request.getAttribute("departments");

    String search = request.getParameter("search");

    if (search == null) {
        search = "";
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>
        Department Management - BLUE RIDGE UNIVERSITY
    </title>

    <link rel="stylesheet" href="css/admin.css">

    <style>

        .department-container {
            width: 100%;
            max-width: 1200px;
            margin: auto;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .page-header h2 {
            color: #0d47a1;
            margin-bottom: 7px;
        }

        .page-header p {
            color: #666;
        }

        .add-button {
            background: #1565c0;
            color: white;
            border: none;
            padding: 11px 18px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
        }

        .add-button:hover {
            background: #0d47a1;
        }

        .form-card {
            display: none;
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
            margin-bottom: 25px;
        }

        .form-card h3 {
            color: #0d47a1;
            margin-bottom: 20px;
        }

        .department-form {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }

        .form-group label {
            display: block;
            font-weight: bold;
            color: #333;
            margin-bottom: 6px;
        }

        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 14px;
        }

        .form-group textarea {
            resize: vertical;
            min-height: 90px;
        }

        .full-width {
            grid-column: 1 / 3;
        }

        .form-buttons {
            grid-column: 1 / 3;
            display: flex;
            gap: 10px;
            margin-top: 5px;
        }

        .save-button {
            background: #1565c0;
            color: white;
            border: none;
            padding: 11px 20px;
            border-radius: 6px;
            cursor: pointer;
        }

        .cancel-button {
            background: #eeeeee;
            color: #333;
            border: none;
            padding: 11px 20px;
            border-radius: 6px;
            cursor: pointer;
        }

        .search-card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
            margin-bottom: 25px;
        }

        .search-form {
            display: flex;
            gap: 10px;
        }

        .search-form input {
            flex: 1;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 6px;
        }

        .search-button {
            background: #1565c0;
            color: white;
            border: none;
            padding: 11px 20px;
            border-radius: 6px;
            cursor: pointer;
        }

        .clear-button {
            display: inline-flex;
            align-items: center;
            background: #eeeeee;
            color: #333;
            padding: 11px 20px;
            border-radius: 6px;
            text-decoration: none;
        }

        .table-card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
            overflow-x: auto;
        }

        .department-table {
            width: 100%;
            border-collapse: collapse;
        }

        .department-table th,
        .department-table td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
            text-align: left;
            vertical-align: middle;
        }

        .department-table th {
            background: #1565c0;
            color: white;
        }

        .department-table tr:hover {
            background: #f7f9fc;
        }

        .department-code {
            font-weight: bold;
            color: #1565c0;
        }

        .status {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 15px;
            font-size: 11px;
            font-weight: bold;
        }

        .active-status {
            background: #d4edda;
            color: #155724;
        }

        .inactive-status {
            background: #eeeeee;
            color: #555;
        }

        .action-button {
            border: none;
            padding: 7px 10px;
            border-radius: 5px;
            cursor: pointer;
            margin: 2px;
            font-size: 12px;
        }

        .edit-button {
            background: #1976d2;
            color: white;
        }

        .activate-button {
            background: #2e7d32;
            color: white;
        }

        .deactivate-button {
            background: #757575;
            color: white;
        }

        .delete-button {
            background: #d32f2f;
            color: white;
        }

        .message {
            padding: 12px 15px;
            border-radius: 6px;
            margin-bottom: 20px;
        }

        .success-message {
            background: #d4edda;
            color: #155724;
        }

        .error-message {
            background: #f8d7da;
            color: #721c24;
        }

        .edit-row {
            display: none;
            background: #f8fafc;
        }

        .no-data {
            text-align: center;
            padding: 40px;
            color: #777;
        }

        @media(max-width: 700px) {

            .page-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }

            .department-form {
                grid-template-columns: 1fr;
            }

            .full-width,
            .form-buttons {
                grid-column: 1;
            }

            .search-form {
                flex-direction: column;
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

            <p>
                University Management System
            </p>

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

    <div class="department-container">


        <!-- =================================================
             PAGE HEADER
             ================================================= -->

        <div class="page-header">

            <div>

                <h2>
                    Department Management
                </h2>

                <p>
                    Add and manage university departments.
                </p>

            </div>


            <button
                type="button"
                class="add-button"
                onclick="showAddForm()">

                + Add Department

            </button>

        </div>



        <!-- =================================================
             MESSAGES
             ================================================= -->

        <% if ("added".equals(success)) { %>

            <div class="message success-message">
                Department added successfully.
            </div>

        <% } %>


        <% if ("updated".equals(success)) { %>

            <div class="message success-message">
                Department updated successfully.
            </div>

        <% } %>


        <% if ("deleted".equals(success)) { %>

            <div class="message success-message">
                Department deleted successfully.
            </div>

        <% } %>


        <% if ("activated".equals(success)) { %>

            <div class="message success-message">
                Department activated successfully.
            </div>

        <% } %>


        <% if ("deactivated".equals(success)) { %>

            <div class="message success-message">
                Department deactivated successfully.
            </div>

        <% } %>


        <% if ("duplicate".equals(error)) { %>

            <div class="message error-message">
                Department code or name already exists.
            </div>

        <% } %>


        <% if ("database".equals(error)) { %>

            <div class="message error-message">
                Database error. Please try again.
            </div>

        <% } %>



        <!-- =================================================
             ADD DEPARTMENT FORM
             ================================================= -->

        <div
            class="form-card"
            id="addForm">

            <h3>
                Add Department
            </h3>


            <form
                action="DepartmentServlet"
                method="post"
                class="department-form">


                <input
                    type="hidden"
                    name="action"
                    value="add">


                <!-- CODE -->

                <div class="form-group">

                    <label>
                        Department Code
                    </label>

                    <input
                        type="text"
                        name="department_code"
                        placeholder="Example: CSE"
                        maxlength="20"
                        required>

                </div>


                <!-- NAME -->

                <div class="form-group">

                    <label>
                        Department Name
                    </label>

                    <input
                        type="text"
                        name="department_name"
                        placeholder="Example: Computer Science and Engineering"
                        maxlength="150"
                        required>

                </div>


                <!-- DESCRIPTION -->

                <div class="form-group full-width">

                    <label>
                        Description
                    </label>

                    <textarea
                        name="description"
                        placeholder="Enter department description"></textarea>

                </div>


                <!-- BUTTONS -->

                <div class="form-buttons">

                    <button
                        type="submit"
                        class="save-button">

                        Add Department

                    </button>


                    <button
                        type="button"
                        class="cancel-button"
                        onclick="hideAddForm()">

                        Cancel

                    </button>

                </div>

            </form>

        </div>



        <!-- =================================================
             SEARCH
             ================================================= -->

        <div class="search-card">

            <form
                action="DepartmentServlet"
                method="get"
                class="search-form">


                <input
                    type="text"
                    name="search"
                    value="<%= escapeHtml(search) %>"
                    placeholder="Search by department name or code">


                <button
                    type="submit"
                    class="search-button">

                    Search

                </button>


                <a
                    href="DepartmentServlet"
                    class="clear-button">

                    Clear

                </a>

            </form>

        </div>



        <!-- =================================================
             DEPARTMENT TABLE
             ================================================= -->

        <div class="table-card">

            <table class="department-table">

                <thead>

                    <tr>

                        <th>
                            ID
                        </th>

                        <th>
                            Code
                        </th>

                        <th>
                            Department Name
                        </th>

                        <th>
                            Description
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
                    if (departments != null &&
                        !departments.isEmpty()) {

                        for (
                            Map<String, Object> department :
                            departments
                        ) {

                            Object id =
                                department.get(
                                    "departmentId"
                                );

                            String code =
                                String.valueOf(
                                    department.get(
                                        "departmentCode"
                                    )
                                );

                            String name =
                                String.valueOf(
                                    department.get(
                                        "departmentName"
                                    )
                                );

                            String description =
                                String.valueOf(
                                    department.get(
                                        "description"
                                    )
                                );

                            String status =
                                String.valueOf(
                                    department.get(
                                        "status"
                                    )
                                );
                %>


                    <!-- =================================================
                         NORMAL ROW
                         ================================================= -->

                    <tr>


                        <td>
                            <%= id %>
                        </td>


                        <td>

                            <span class="department-code">

                                <%= escapeHtml(code) %>

                            </span>

                        </td>


                        <td>

                            <strong>
                                <%= escapeHtml(name) %>
                            </strong>

                        </td>


                        <td>

                            <%= escapeHtml(description) %>

                        </td>


                        <td>

                            <% if (
                                "ACTIVE".equalsIgnoreCase(
                                    status
                                )
                            ) { %>

                                <span
                                    class="status active-status">

                                    ACTIVE

                                </span>

                            <% } else { %>

                                <span
                                    class="status inactive-status">

                                    INACTIVE

                                </span>

                            <% } %>

                        </td>


                        <td>


                            <!-- EDIT -->

                            <button
                                type="button"
                                class="action-button edit-button"
                                onclick="showEditForm('<%= id %>')">

                                Edit

                            </button>



                            <!-- ACTIVATE / DEACTIVATE -->

                            <% if (
                                "ACTIVE".equalsIgnoreCase(
                                    status
                                )
                            ) { %>


                                <form
                                    action="DepartmentServlet"
                                    method="post"
                                    style="display:inline;">

                                    <input
                                        type="hidden"
                                        name="action"
                                        value="deactivate">

                                    <input
                                        type="hidden"
                                        name="department_id"
                                        value="<%= id %>">


                                    <button
                                        type="submit"
                                        class="action-button deactivate-button"
                                        onclick="return confirm(
                                            'Deactivate this department?'
                                        );">

                                        Deactivate

                                    </button>

                                </form>


                            <% } else { %>


                                <form
                                    action="DepartmentServlet"
                                    method="post"
                                    style="display:inline;">

                                    <input
                                        type="hidden"
                                        name="action"
                                        value="activate">

                                    <input
                                        type="hidden"
                                        name="department_id"
                                        value="<%= id %>">


                                    <button
                                        type="submit"
                                        class="action-button activate-button"
                                        onclick="return confirm(
                                            'Activate this department?'
                                        );">

                                        Activate

                                    </button>

                                </form>


                            <% } %>



                            <!-- DELETE -->

                            <form
                                action="DepartmentServlet"
                                method="post"
                                style="display:inline;">

                                <input
                                    type="hidden"
                                    name="action"
                                    value="delete">

                                <input
                                    type="hidden"
                                    name="department_id"
                                    value="<%= id %>">


                                <button
                                    type="submit"
                                    class="action-button delete-button"
                                    onclick="return confirm(
                                        'Delete this department?'
                                    );">

                                    Delete

                                </button>

                            </form>


                        </td>

                    </tr>



                    <!-- =================================================
                         EDIT ROW
                         ================================================= -->

                    <tr
                        id="editRow<%= id %>"
                        class="edit-row">

                        <td colspan="6">


                            <form
                                action="DepartmentServlet"
                                method="post"
                                class="department-form">


                                <input
                                    type="hidden"
                                    name="action"
                                    value="update">


                                <input
                                    type="hidden"
                                    name="department_id"
                                    value="<%= id %>">


                                <div class="form-group">

                                    <label>
                                        Department Code
                                    </label>

                                    <input
                                        type="text"
                                        name="department_code"
                                        value="<%= escapeAttribute(code) %>"
                                        required>

                                </div>


                                <div class="form-group">

                                    <label>
                                        Department Name
                                    </label>

                                    <input
                                        type="text"
                                        name="department_name"
                                        value="<%= escapeAttribute(name) %>"
                                        required>

                                </div>


                                <div class="form-group full-width">

                                    <label>
                                        Description
                                    </label>

                                    <textarea
                                        name="description"><%= escapeHtml(description) %></textarea>

                                </div>


                                <div class="form-buttons">

                                    <button
                                        type="submit"
                                        class="save-button">

                                        Save Changes

                                    </button>


                                    <button
                                        type="button"
                                        class="cancel-button"
                                        onclick="hideEditForm('<%= id %>')">

                                        Cancel

                                    </button>

                                </div>

                            </form>

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
                                No departments found
                            </h3>

                            <p>
                                Add a department or change
                                your search.
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



<!-- =====================================================
     JAVASCRIPT
     ===================================================== -->

<script>

function showAddForm() {

    const form =
        document.getElementById(
            "addForm"
        );

    form.style.display = "block";

    form.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}


function hideAddForm() {

    document.getElementById(
        "addForm"
    ).style.display = "none";
}


function showEditForm(id) {

    const row =
        document.getElementById(
            "editRow" + id
        );

    if (row) {

        row.style.display = "table-row";

        row.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });
    }
}


function hideEditForm(id) {

    const row =
        document.getElementById(
            "editRow" + id
        );

    if (row) {

        row.style.display = "none";
    }
}

</script>


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


private String escapeAttribute(
        String value) {

    return escapeHtml(value);
}

%>