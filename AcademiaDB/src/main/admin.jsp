<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String firstName =
        (String) request.getAttribute("adminFirstName");

    String lastName =
        (String) request.getAttribute("adminLastName");

    String username =
        (String) request.getAttribute("adminUsername");

    String email =
        (String) request.getAttribute("adminEmail");

    String mobile =
        (String) request.getAttribute("adminMobile");

    String status =
        (String) request.getAttribute("adminStatus");

    if (firstName == null) {
        firstName =
            (String) session.getAttribute("firstName");
    }

    if (lastName == null) {
        lastName =
            (String) session.getAttribute("lastName");
    }

    if (username == null) {
        username =
            (String) session.getAttribute("username");
    }

    if (email == null) {
        email =
            (String) session.getAttribute("email");
    }

    if (mobile == null) {
        mobile =
            (String) session.getAttribute("mobile");
    }

    if (status == null) {
        status = "ACTIVE";
    }

    Integer totalUsersAttr = (Integer) request.getAttribute("totalUsers");
    Integer totalStudentsAttr = (Integer) request.getAttribute("totalStudents");
    Integer totalTeachersAttr = (Integer) request.getAttribute("totalTeachers");
    Integer activeUsersAttr = (Integer) request.getAttribute("activeUsers");

    String totalUsersVal = (totalUsersAttr != null) ? String.valueOf(totalUsersAttr) : "0";
    String totalStudentsVal = (totalStudentsAttr != null) ? String.valueOf(totalStudentsAttr) : "0";
    String totalTeachersVal = (totalTeachersAttr != null) ? String.valueOf(totalTeachersAttr) : "0";
    String activeUsersVal = (activeUsersAttr != null) ? String.valueOf(activeUsersAttr) : "0";
%>


<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Admin Dashboard - BLUE RIDGE UNIVERSITY
    </title>

    <link rel="stylesheet"
          href="css/admin.css">


    <style>

        * {
            box-sizing: border-box;
        }


        body {
            margin: 0;
        }


        /* =================================================
           MAIN CONTAINER
           ================================================= */

        .admin-container {

            width: 100%;

            max-width: 1250px;

            margin: auto;
        }


        /* =================================================
           WELCOME
           ================================================= */

        .welcome-card {

            background: white;

            padding: 25px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);

            margin-bottom: 25px;
        }


        .welcome-card h2 {

            color: #0d47a1;

            margin: 0 0 8px 0;
        }


        .welcome-card p {

            color: #666;

            margin: 0;
        }


        /* =================================================
           STATISTICS
           ================================================= */

        .stats-grid {

            display: grid;

            grid-template-columns:
                repeat(4, 1fr);

            gap: 18px;

            margin-bottom: 25px;
        }


        .stat-card {

            background: white;

            padding: 22px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);
        }


        .stat-title {

            color: #777;

            font-size: 12px;

            margin-bottom: 8px;
        }


        .stat-number {

            color: #0d47a1;

            font-size: 28px;

            font-weight: bold;
        }


        .stat-description {

            color: #999;

            font-size: 11px;

            margin-top: 5px;
        }


        /* =================================================
           QUICK ACTIONS
           ================================================= */

        .section-card {

            background: white;

            padding: 25px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);

            margin-bottom: 25px;
        }


        .section-title {

            color: #0d47a1;

            font-size: 20px;

            margin: 0 0 20px 0;
        }


        .actions-grid {

            display: grid;

            grid-template-columns:
                repeat(3, 1fr);

            gap: 15px;
        }


        .action-card {

            text-decoration: none;

            background: #f7f9fc;

            border:
                1px solid #e2e6ea;

            padding: 20px;

            border-radius: 8px;

            transition:
                0.2s ease;

            color: #333;
        }


        .action-card:hover {

            transform:
                translateY(-2px);

            border-color:
                #1565c0;

            box-shadow:
                0 5px 15px
                rgba(21,101,192,0.12);
        }


        .action-icon {

            width: 45px;

            height: 45px;

            border-radius: 8px;

            background: #e3f2fd;

            color: #1565c0;

            display: flex;

            align-items: center;

            justify-content: center;

            font-size: 20px;

            margin-bottom: 12px;
        }


        .action-card h3 {

            margin: 0 0 7px 0;

            color: #0d47a1;

            font-size: 16px;
        }


        .action-card p {

            margin: 0;

            color: #777;

            font-size: 12px;

            line-height: 1.5;
        }


        /* =================================================
           ADD USER
           ================================================= */

        .add-user-card {

            background: white;

            padding: 25px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);

            margin-bottom: 25px;
        }


        .role-selector {

            display: flex;

            gap: 10px;

            margin-bottom: 20px;

            flex-wrap: wrap;
        }


        .role-selector button {

            padding: 10px 20px;

            border:
                1px solid #1565c0;

            border-radius: 20px;

            background: white;

            color: #1565c0;

            cursor: pointer;

            font-size: 13px;
        }


        .role-selector button.active {

            background: #1565c0;

            color: white;
        }


        .user-form {

            display: none;
        }


        .user-form.active {

            display: block;
        }


        .form-grid {

            display: grid;

            grid-template-columns:
                repeat(2, 1fr);

            gap: 15px;
        }


        .form-group label {

            display: block;

            margin-bottom: 6px;

            font-size: 12px;

            font-weight: bold;

            color: #444;
        }


        .form-group input,
        .form-group select {

            width: 100%;

            padding: 11px;

            border:
                1px solid #ccc;

            border-radius: 6px;

            outline: none;
        }


        .form-group input:focus,
        .form-group select:focus {

            border-color:
                #1565c0;
        }


        .full-width {

            grid-column: 1 / -1;
        }


        .add-button {

            margin-top: 20px;

            padding: 12px 22px;

            background: #1565c0;

            color: white;

            border: none;

            border-radius: 6px;

            cursor: pointer;

            font-size: 13px;
        }


        .add-button:hover {

            background: #0d47a1;
        }


        /* =================================================
           MESSAGES
           ================================================= */

        .message {

            padding: 13px 16px;

            border-radius: 6px;

            margin-bottom: 20px;

            font-size: 13px;
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
           PROFILE
           ================================================= */

        .profile-grid {

            display: grid;

            grid-template-columns:
                repeat(4, 1fr);

            gap: 12px;
        }


        .profile-item {

            background: #f7f9fc;

            padding: 14px;

            border-radius: 7px;
        }


        .profile-label {

            display: block;

            color: #777;

            font-size: 11px;

            margin-bottom: 5px;
        }


        .profile-value {

            color: #333;

            font-size: 13px;

            font-weight: bold;

            word-break: break-word;
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

        @media(max-width: 950px) {

            .stats-grid {

                grid-template-columns:
                    repeat(2, 1fr);
            }


            .actions-grid {

                grid-template-columns:
                    repeat(2, 1fr);
            }


            .profile-grid {

                grid-template-columns:
                    repeat(2, 1fr);
            }

        }


        @media(max-width: 600px) {

            .stats-grid,
            .actions-grid,
            .profile-grid,
            .form-grid {

                grid-template-columns: 1fr;
            }


            .full-width {

                grid-column: auto;
            }

        }

    </style>

</head>


<body>


<!-- =====================================================
     HEADER
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

            Admin

        </span>


        <a
            href="LogoutServlet"
            class="logout-button">

            Logout

        </a>

    </div>

</header>



<!-- =====================================================
     MAIN
     ===================================================== -->

<main class="main-content">


    <div class="admin-container">


        <!-- =================================================
             MESSAGES
             ================================================= -->

        <%
            String success =
                request.getParameter("success");

            String error =
                request.getParameter("error");
        %>


        <% if ("useradded".equals(success)) { %>

            <div class="message success-message">

                User added successfully.

            </div>

        <% } %>


        <% if ("usernameexists".equals(error)) { %>

            <div class="message error-message">

                Username already exists.

            </div>

        <% } %>


        <% if ("emailexists".equals(error)) { %>

            <div class="message error-message">

                Email already exists.

            </div>

        <% } %>


        <% if ("empty".equals(error)) { %>

            <div class="message error-message">

                Please fill in all required fields.

            </div>

        <% } %>


        <% if ("database".equals(error)) { %>

            <div class="message error-message">

                Database error occurred.

            </div>

        <% } %>



        <!-- =================================================
             WELCOME
             ================================================= -->

        <div class="welcome-card">

            <h2>

                Welcome,
                <%= firstName == null
                    ? "Administrator"
                    : firstName %>

            </h2>

            <p>

                Manage students, teachers,
                users and academic information
                from one place.

            </p>

        </div>



        <!-- =================================================
             STATISTICS
             ================================================= -->

        <div class="stats-grid">


            <div class="stat-card">

                <div class="stat-title">
                    TOTAL USERS
                </div>

                <div
                    class="stat-number"
                    id="totalUsers">

                    <%= totalUsersVal %>

                </div>

                <div class="stat-description">
                    Registered accounts
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-title">
                    STUDENTS
                </div>

                <div
                    class="stat-number"
                    id="totalStudents">

                    <%= totalStudentsVal %>

                </div>

                <div class="stat-description">
                    Student accounts
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-title">
                    TEACHERS
                </div>

                <div
                    class="stat-number"
                    id="totalTeachers">

                    <%= totalTeachersVal %>

                </div>

                <div class="stat-description">
                    Teaching accounts
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-title">
                    ACTIVE USERS
                </div>

                <div
                    class="stat-number"
                    id="activeUsers">

                    <%= activeUsersVal %>

                </div>

                <div class="stat-description">
                    Active accounts
                </div>

            </div>


        </div>



        <!-- =================================================
             QUICK ACTIONS
             ================================================= -->

        <div class="section-card">


            <h2 class="section-title">

                Admin Controls

            </h2>


            <div class="actions-grid">


                <!-- USER MANAGEMENT -->

                <a
                    href="UserManagementServlet"
                    class="action-card">


                    <div class="action-icon">
                        👥
                    </div>


                    <h3>
                        User Management
                    </h3>


                    <p>

                        View, search, activate,
                        deactivate and delete users.

                    </p>


                </a>



                <!-- ACADEMIC -->

                <a
                    href="AcademicServlet"
                    class="action-card">


                    <div class="action-icon">
                        📚
                    </div>


                    <h3>
                        Academic Management
                    </h3>


                    <p>

                        Manage academic years,
                        semesters and dates.

                    </p>


                </a>



                <!-- STUDENTS -->

                <a
                    href="UserManagementServlet?role=STUDENT"
                    class="action-card">


                    <div class="action-icon">
                        🎓
                    </div>


                    <h3>
                        Students
                    </h3>


                    <p>

                        View and manage
                        registered students.

                    </p>


                </a>



                <!-- TEACHERS -->

                <a
                    href="UserManagementServlet?role=TEACHER"
                    class="action-card">


                    <div class="action-icon">
                        👨‍🏫
                    </div>


                    <h3>
                        Teachers
                    </h3>


                    <p>

                        View and manage
                        registered teachers.

                    </p>


                </a>



                <!-- ADMINS -->

                <a
                    href="UserManagementServlet?role=ADMIN"
                    class="action-card">


                    <div class="action-icon">
                        🛡
                    </div>


                    <h3>
                        Administrators
                    </h3>


                    <p>

                        View administrator
                        accounts.

                    </p>


                </a>



                <!-- REFRESH -->

                <a
                    href="AdminServlet"
                    class="action-card">


                    <div class="action-icon">
                        🔄
                    </div>


                    <h3>
                        Refresh Dashboard
                    </h3>


                    <p>

                        Reload the latest
                        administrator information.

                    </p>


                </a>


            </div>

        </div>



        <!-- =================================================
             ADD USER
             ================================================= -->

        <div class="add-user-card">


            <h2 class="section-title">

                Add User

            </h2>


            <p
                style="
                    color:#777;
                    font-size:13px;
                    margin-bottom:20px;
                ">

                Select a user type.
                The required fields will appear automatically.

            </p>



            <!-- ROLE SELECTOR -->

            <div class="role-selector">


                <button
                    type="button"
                    id="studentButton"
                    onclick="showForm('student')">

                    Add Student

                </button>


                <button
                    type="button"
                    id="teacherButton"
                    onclick="showForm('teacher')">

                    Add Teacher

                </button>


            </div>



            <!-- =================================================
                 STUDENT FORM
                 ================================================= -->

            <form
                id="studentForm"
                class="user-form"
                action="AddUserServlet"
                method="post">


                <input
                    type="hidden"
                    name="role"
                    value="STUDENT">


                <div class="form-grid">


                    <div class="form-group">

                        <label>
                            Username *
                        </label>

                        <input
                            type="text"
                            name="username"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Password *
                        </label>

                        <input
                            type="password"
                            name="password"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            First Name *
                        </label>

                        <input
                            type="text"
                            name="first_name"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Last Name *
                        </label>

                        <input
                            type="text"
                            name="last_name"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Email *
                        </label>

                        <input
                            type="email"
                            name="email"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Mobile *
                        </label>

                        <input
                            type="text"
                            name="mobile"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Department *
                        </label>

                        <select
                            name="department"
                            required>


                            <option value="">
                                Select Department
                            </option>


                            <option value="Computer Science and Engineering">
                                Computer Science and Engineering
                            </option>


                            <option value="Information Technology">
                                Information Technology
                            </option>


                            <option value="Electronics and Communication Engineering">
                                Electronics and Communication Engineering
                            </option>


                            <option value="Electrical Engineering">
                                Electrical Engineering
                            </option>


                            <option value="Mechanical Engineering">
                                Mechanical Engineering
                            </option>


                            <option value="Civil Engineering">
                                Civil Engineering
                            </option>


                        </select>

                    </div>


                    <div class="form-group">

                        <label>
                            Semester *
                        </label>

                        <select
                            name="semester"
                            required>


                            <option value="">
                                Select Semester
                            </option>


                            <option value="1">
                                Semester 1
                            </option>


                            <option value="2">
                                Semester 2
                            </option>


                            <option value="3">
                                Semester 3
                            </option>


                            <option value="4">
                                Semester 4
                            </option>


                            <option value="5">
                                Semester 5
                            </option>


                            <option value="6">
                                Semester 6
                            </option>


                            <option value="7">
                                Semester 7
                            </option>


                            <option value="8">
                                Semester 8
                            </option>


                        </select>

                    </div>


                </div>


                <button
                    type="submit"
                    class="add-button">

                    Add Student

                </button>


            </form>



            <!-- =================================================
                 TEACHER FORM
                 ================================================= -->

            <form
                id="teacherForm"
                class="user-form"
                action="AddUserServlet"
                method="post">


                <input
                    type="hidden"
                    name="role"
                    value="TEACHER">


                <div class="form-grid">


                    <div class="form-group">

                        <label>
                            Username *
                        </label>

                        <input
                            type="text"
                            name="username"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Password *
                        </label>

                        <input
                            type="password"
                            name="password"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            First Name *
                        </label>

                        <input
                            type="text"
                            name="first_name"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Last Name *
                        </label>

                        <input
                            type="text"
                            name="last_name"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Email *
                        </label>

                        <input
                            type="email"
                            name="email"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Mobile *
                        </label>

                        <input
                            type="text"
                            name="mobile"
                            required>

                    </div>


                    <div class="form-group">

                        <label>
                            Department *
                        </label>

                        <select
                            name="department"
                            required>


                            <option value="">
                                Select Department
                            </option>


                            <option value="Computer Science and Engineering">
                                Computer Science and Engineering
                            </option>


                            <option value="Information Technology">
                                Information Technology
                            </option>


                            <option value="Electronics and Communication Engineering">
                                Electronics and Communication Engineering
                            </option>


                            <option value="Electrical Engineering">
                                Electrical Engineering
                            </option>


                            <option value="Mechanical Engineering">
                                Mechanical Engineering
                            </option>


                            <option value="Civil Engineering">
                                Civil Engineering
                            </option>


                        </select>

                    </div>


                    <div class="form-group">

                        <label>
                            Employee Type *
                        </label>

                        <select
                            name="employee_type"
                            required>


                            <option value="">
                                Select Employee Type
                            </option>


                            <option value="Teaching Faculty">
                                Teaching Faculty
                            </option>


                            <option value="Assistant Professor">
                                Assistant Professor
                            </option>


                            <option value="Associate Professor">
                                Associate Professor
                            </option>


                            <option value="Professor">
                                Professor
                            </option>


                            <option value="Visiting Faculty">
                                Visiting Faculty
                            </option>


                        </select>

                    </div>


                </div>


                <button
                    type="submit"
                    class="add-button">

                    Add Teacher

                </button>


            </form>


        </div>



        <!-- =================================================
             ADMIN PROFILE
             ================================================= -->

        <div class="section-card">


            <h2 class="section-title">

                Administrator Profile

            </h2>


            <div class="profile-grid">


                <div class="profile-item">

                    <span class="profile-label">
                        Name
                    </span>

                    <span class="profile-value">

                        <%= firstName == null
                            ? ""
                            : firstName %>

                        <%= lastName == null
                            ? ""
                            : lastName %>

                    </span>

                </div>


                <div class="profile-item">

                    <span class="profile-label">
                        Username
                    </span>

                    <span class="profile-value">

                        <%= username == null
                            ? ""
                            : username %>

                    </span>

                </div>


                <div class="profile-item">

                    <span class="profile-label">
                        Email
                    </span>

                    <span class="profile-value">

                        <%= email == null
                            ? ""
                            : email %>

                    </span>

                </div>


                <div class="profile-item">

                    <span class="profile-label">
                        Status
                    </span>

                    <span class="profile-value">

                        <%= status %>

                    </span>

                </div>


            </div>

        </div>


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


/* =====================================================
   SHOW USER FORM
   ===================================================== */

function showForm(type) {


    var studentForm =
        document.getElementById(
            "studentForm"
        );


    var teacherForm =
        document.getElementById(
            "teacherForm"
        );


    var studentButton =
        document.getElementById(
            "studentButton"
        );


    var teacherButton =
        document.getElementById(
            "teacherButton"
        );


    studentForm.classList.remove(
        "active"
    );


    teacherForm.classList.remove(
        "active"
    );


    studentButton.classList.remove(
        "active"
    );


    teacherButton.classList.remove(
        "active"
    );


    if (type === "student") {

        studentForm.classList.add(
            "active"
        );

        studentButton.classList.add(
            "active"
        );

    }


    if (type === "teacher") {

        teacherForm.classList.add(
            "active"
        );

        teacherButton.classList.add(
            "active"
        );

    }

}


/* =====================================================
   SIMPLE CLIENT-SIDE STATISTICS
   ===================================================== */

function loadStatistics() {

    fetch(
        "UserManagementServlet"
    )
    .then(function(response) {

        return response.text();

    })
    .then(function() {

        /*
         * Statistics can be connected to a dedicated
         * servlet later. The dashboard intentionally
         * does not guess database counts.
         */

    })
    .catch(function(error) {

        console.log(
            "Statistics unavailable"
        );

    });

}


</script>


</body>

</html>