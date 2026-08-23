<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String username = (String) request.getAttribute("username");
    String firstName = (String) request.getAttribute("firstName");
    String lastName = (String) request.getAttribute("lastName");
    String email = (String) request.getAttribute("email");
    String mobile = (String) request.getAttribute("mobile");
    String status = (String) request.getAttribute("status");

    Object userId = request.getAttribute("userId");

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    if (username == null) username = "";
    if (firstName == null) firstName = "";
    if (lastName == null) lastName = "";
    if (email == null) email = "";
    if (mobile == null) mobile = "";
    if (status == null) status = "";
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>
        Admin Profile - BLUE RIDGE UNIVERSITY
    </title>

    <link rel="stylesheet" href="css/admin.css">

    <style>

        .profile-container {
            width: 100%;
            max-width: 950px;
            margin: auto;
        }

        .page-header {
            margin-bottom: 25px;
        }

        .page-header h2 {
            color: #0d47a1;
            margin-bottom: 7px;
        }

        .page-header p {
            color: #666;
        }

        .profile-header {
            background: white;
            padding: 28px;
            border-radius: 12px;
            box-shadow:
                0 4px 15px rgba(0,0,0,0.12);
            display: flex;
            align-items: center;
            gap: 20px;
            margin-bottom: 20px;
        }

        .admin-avatar {
            width: 85px;
            height: 85px;
            border-radius: 50%;
            background: #1565c0;
            color: white;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 32px;
            font-weight: bold;
        }

        .profile-header h2 {
            color: #0d47a1;
            margin-bottom: 6px;
        }

        .profile-header p {
            margin: 4px 0;
            color: #666;
        }

        .admin-badge {
            display: inline-block;
            margin-top: 7px;
            padding: 5px 12px;
            border-radius: 20px;
            background: #e3f2fd;
            color: #0d47a1;
            font-size: 12px;
            font-weight: bold;
        }

        .profile-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow:
                0 4px 15px rgba(0,0,0,0.12);
            margin-bottom: 20px;
        }

        .profile-card h3 {
            color: #0d47a1;
            padding-bottom: 12px;
            margin-bottom: 20px;
            border-bottom: 1px solid #eee;
        }

        .profile-form {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        .form-group label {
            display: block;
            font-weight: bold;
            color: #333;
            margin-bottom: 6px;
        }

        .form-group input {
            width: 100%;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 14px;
        }

        .form-group input:focus {
            outline: none;
            border-color: #1565c0;
            box-shadow:
                0 0 5px rgba(21,101,192,0.2);
        }

        .full-width {
            grid-column: 1 / 3;
        }

        .readonly {
            background: #f5f5f5;
            cursor: not-allowed;
        }

        .save-button {
            background: #1565c0;
            color: white;
            border: none;
            padding: 11px 20px;
            border-radius: 6px;
            cursor: pointer;
            margin-top: 5px;
        }

        .save-button:hover {
            background: #0d47a1;
        }

        .password-form {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        .password-form input {
            width: 100%;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 6px;
        }

        .password-form .full-width {
            grid-column: 1 / 3;
        }

        .password-button {
            background: #455a64;
            color: white;
            border: none;
            padding: 11px 20px;
            border-radius: 6px;
            cursor: pointer;
            margin-top: 5px;
        }

        .password-button:hover {
            background: #263238;
        }

        .account-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }

        .account-item {
            background: #f7f9fc;
            padding: 15px;
            border-radius: 7px;
        }

        .account-item span {
            display: block;
            color: #777;
            font-size: 12px;
            margin-bottom: 5px;
        }

        .account-item strong {
            color: #333;
        }

        .active {
            color: #2e7d32 !important;
        }

        .inactive {
            color: #d32f2f !important;
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

        .back-button {
            display: inline-block;
            background: #eeeeee;
            color: #333;
            text-decoration: none;
            padding: 10px 18px;
            border-radius: 6px;
            margin-bottom: 20px;
        }

        .back-button:hover {
            background: #dddddd;
        }

        .security-note {
            background: #e3f2fd;
            color: #0d47a1;
            padding: 13px;
            border-radius: 6px;
            font-size: 13px;
            margin-bottom: 18px;
            line-height: 1.5;
        }

        @media(max-width: 700px) {

            .profile-header {
                flex-direction: column;
                text-align: center;
            }

            .profile-form,
            .password-form,
            .account-grid {
                grid-template-columns: 1fr;
            }

            .full-width,
            .password-form .full-width {
                grid-column: 1;
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

    <div class="profile-container">


        <!-- =================================================
             PAGE HEADER
             ================================================= -->

        <div class="page-header">

            <h2>
                Administrator Profile
            </h2>

            <p>
                Manage your administrator account information
                and security settings.
            </p>

        </div>



        <!-- =================================================
             MESSAGES
             ================================================= -->

        <% if ("updated".equals(success)) { %>

            <div class="message success-message">

                Profile updated successfully.

            </div>

        <% } %>


        <% if ("password".equals(success)) { %>

            <div class="message success-message">

                Password changed successfully.

            </div>

        <% } %>


        <% if ("empty".equals(error)) { %>

            <div class="message error-message">

                Please fill in all required fields.

            </div>

        <% } %>


        <% if ("database".equals(error)) { %>

            <div class="message error-message">

                A database error occurred.
                Please try again.

            </div>

        <% } %>


        <% if ("wrongpassword".equals(error)) { %>

            <div class="message error-message">

                Current password is incorrect.

            </div>

        <% } %>


        <% if ("passwordmatch".equals(error)) { %>

            <div class="message error-message">

                New passwords do not match.

            </div>

        <% } %>


        <% if ("passwordlength".equals(error)) { %>

            <div class="message error-message">

                Password must contain at least
                6 characters.

            </div>

        <% } %>



        <!-- =================================================
             PROFILE HEADER
             ================================================= -->

        <div class="profile-header">


            <div class="admin-avatar">

                <%
                    String avatarLetter = "A";

                    if (!firstName.isEmpty()) {

                        avatarLetter =
                            firstName
                                .substring(0, 1)
                                .toUpperCase();

                    } else if (!username.isEmpty()) {

                        avatarLetter =
                            username
                                .substring(0, 1)
                                .toUpperCase();
                    }
                %>

                <%= avatarLetter %>

            </div>


            <div>

                <h2>

                    <% if (!firstName.isEmpty()) { %>

                        <%= escapeHtml(firstName) %>

                        <% if (!lastName.isEmpty()) { %>

                            <%= " " + escapeHtml(lastName) %>

                        <% } %>

                    <% } else { %>

                        <%= escapeHtml(username) %>

                    <% } %>

                </h2>


                <p>

                    Username:

                    <strong>
                        <%= escapeHtml(username) %>
                    </strong>

                </p>


                <span class="admin-badge">

                    ADMINISTRATOR

                </span>

            </div>

        </div>



        <!-- =================================================
             ACCOUNT INFORMATION
             ================================================= -->

        <div class="profile-card">

            <h3>
                Account Information
            </h3>


            <div class="account-grid">


                <div class="account-item">

                    <span>
                        User ID
                    </span>

                    <strong>
                        <%= userId != null
                            ? userId
                            : "N/A" %>
                    </strong>

                </div>


                <div class="account-item">

                    <span>
                        Username
                    </span>

                    <strong>
                        <%= escapeHtml(username) %>
                    </strong>

                </div>


                <div class="account-item">

                    <span>
                        Role
                    </span>

                    <strong>
                        ADMIN
                    </strong>

                </div>


                <div class="account-item">

                    <span>
                        Account Status
                    </span>

                    <strong
                        class="<%= "ACTIVE".equalsIgnoreCase(status)
                            ? "active"
                            : "inactive" %>">

                        <%= escapeHtml(status) %>

                    </strong>

                </div>


            </div>

        </div>



        <!-- =================================================
             PERSONAL INFORMATION
             ================================================= -->

        <div class="profile-card">

            <h3>
                Personal Information
            </h3>


            <form
                action="ProfileServlet"
                method="post"
                class="profile-form">


                <input
                    type="hidden"
                    name="action"
                    value="update">


                <!-- FIRST NAME -->

                <div class="form-group">

                    <label>
                        First Name
                    </label>

                    <input
                        type="text"
                        name="first_name"
                        value="<%= escapeAttribute(firstName) %>"
                        placeholder="Enter first name"
                        required>

                </div>


                <!-- LAST NAME -->

                <div class="form-group">

                    <label>
                        Last Name
                    </label>

                    <input
                        type="text"
                        name="last_name"
                        value="<%= escapeAttribute(lastName) %>"
                        placeholder="Enter last name"
                        required>

                </div>


                <!-- EMAIL -->

                <div class="form-group">

                    <label>
                        Email
                    </label>

                    <input
                        type="email"
                        name="email"
                        value="<%= escapeAttribute(email) %>"
                        placeholder="Enter email"
                        required>

                </div>


                <!-- MOBILE -->

                <div class="form-group">

                    <label>
                        Mobile Number
                    </label>

                    <input
                        type="text"
                        name="mobile"
                        value="<%= escapeAttribute(mobile) %>"
                        placeholder="Enter mobile number"
                        required>

                </div>


                <!-- USERNAME -->

                <div class="form-group">

                    <label>
                        Username
                    </label>

                    <input
                        type="text"
                        value="<%= escapeAttribute(username) %>"
                        class="readonly"
                        readonly>

                </div>


                <!-- ROLE -->

                <div class="form-group">

                    <label>
                        Role
                    </label>

                    <input
                        type="text"
                        value="ADMIN"
                        class="readonly"
                        readonly>

                </div>


                <!-- SAVE -->

                <div class="full-width">

                    <button
                        type="submit"
                        class="save-button">

                        Save Changes

                    </button>

                </div>

            </form>

        </div>



        <!-- =================================================
             PASSWORD
             ================================================= -->

        <div class="profile-card">

            <h3>
                Change Administrator Password
            </h3>


            <div class="security-note">

                For security reasons, enter your current
                password before creating a new password.

            </div>


            <form
                action="ProfileServlet"
                method="post"
                class="password-form">


                <input
                    type="hidden"
                    name="action"
                    value="changePassword">


                <!-- CURRENT PASSWORD -->

                <div class="form-group">

                    <label>
                        Current Password
                    </label>

                    <input
                        type="password"
                        name="current_password"
                        placeholder="Enter current password"
                        required>

                </div>


                <!-- NEW PASSWORD -->

                <div class="form-group">

                    <label>
                        New Password
                    </label>

                    <input
                        type="password"
                        name="new_password"
                        placeholder="Enter new password"
                        minlength="6"
                        required>

                </div>


                <!-- CONFIRM PASSWORD -->

                <div class="form-group full-width">

                    <label>
                        Confirm New Password
                    </label>

                    <input
                        type="password"
                        name="confirm_password"
                        placeholder="Re-enter new password"
                        minlength="6"
                        required>

                </div>


                <!-- BUTTON -->

                <div class="full-width">

                    <button
                        type="submit"
                        class="password-button">

                        Change Password

                    </button>

                </div>

            </form>

        </div>



        <!-- =================================================
             BACK
             ================================================= -->

        <a
            href="AdminServlet"
            class="back-button">

            ← Back to Admin Dashboard

        </a>

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

private String escapeHtml(String value) {

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


private String escapeAttribute(String value) {

    return escapeHtml(value);
}

%>