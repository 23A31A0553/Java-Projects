<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
    String role = (String) session.getAttribute("role");

    if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Academic Management - BLUE RIDGE UNIVERSITY
    </title>

    <link rel="stylesheet"
          href="css/admin.css">


    <style>

        .academic-container {

            width: 100%;

            max-width: 1100px;

            margin: auto;
        }


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


        .form-card {

            background: white;

            padding: 25px;

            border-radius: 10px;

            box-shadow:
                0 4px 15px
                rgba(0,0,0,0.12);

            margin-bottom: 20px;
        }


        .section-title {

            color: #0d47a1;

            margin-bottom: 20px;
        }


        .form-grid {

            display: grid;

            grid-template-columns:
                repeat(2, 1fr);

            gap: 18px;
        }


        .form-group label {

            display: block;

            margin-bottom: 7px;

            color: #444;

            font-size: 13px;

            font-weight: bold;
        }


        .form-group input,
        .form-group select {

            width: 100%;

            padding: 11px;

            border:
                1px solid #ccc;

            border-radius: 6px;

            box-sizing: border-box;

            outline: none;
        }


        .form-group input:focus,
        .form-group select:focus {

            border-color: #1565c0;
        }


        .full-width {

            grid-column: 1 / -1;
        }


        .save-button {

            margin-top: 20px;

            padding: 12px 22px;

            border: none;

            border-radius: 6px;

            background: #1565c0;

            color: white;

            cursor: pointer;

            font-size: 14px;
        }


        .save-button:hover {

            background: #0d47a1;
        }


        .back-button {

            display: inline-block;

            padding: 10px 18px;

            background: #eeeeee;

            color: #333;

            text-decoration: none;

            border-radius: 6px;

            font-size: 13px;
        }


        .back-button:hover {

            background: #dddddd;
        }


        .info-card {

            background: #e3f2fd;

            padding: 20px;

            border-radius: 8px;

            color: #1565c0;

            margin-bottom: 20px;
        }


        .info-card strong {

            display: block;

            margin-bottom: 7px;
        }


        @media(max-width: 650px) {

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
     MAIN
     ===================================================== -->

<main class="main-content">


    <div class="academic-container">


        <!-- PAGE TITLE -->

        <div class="page-card">

            <h2>
                Academic Management
            </h2>

            <p>
                Manage academic years and semesters.
            </p>

        </div>



        <!-- INFORMATION -->

        <div class="info-card">

            <strong>
                Academic Year Setup
            </strong>

            Add the academic year, semester,
            start date and end date.

        </div>



        <!-- FORM -->

        <div class="form-card">


            <h2 class="section-title">

                Add Academic Year

            </h2>


            <form
                action="AcademicServlet"
                method="post">


                <input
                    type="hidden"
                    name="action"
                    value="add">


                <div class="form-grid">


                    <!-- ACADEMIC YEAR -->

                    <div class="form-group">

                        <label>
                            Academic Year
                        </label>

                        <input
                            type="text"
                            name="academic_year"
                            placeholder="2026-2027"
                            required>

                    </div>


                    <!-- SEMESTER -->

                    <div class="form-group">

                        <label>
                            Semester
                        </label>

                        <select
                            name="semester"
                            required>

                            <option value="">
                                Select Semester
                            </option>

                            <option value="Semester 1">
                                Semester 1
                            </option>

                            <option value="Semester 2">
                                Semester 2
                            </option>

                            <option value="Semester 3">
                                Semester 3
                            </option>

                            <option value="Semester 4">
                                Semester 4
                            </option>

                            <option value="Semester 5">
                                Semester 5
                            </option>

                            <option value="Semester 6">
                                Semester 6
                            </option>

                            <option value="Semester 7">
                                Semester 7
                            </option>

                            <option value="Semester 8">
                                Semester 8
                            </option>

                        </select>

                    </div>


                    <!-- START DATE -->

                    <div class="form-group">

                        <label>
                            Start Date
                        </label>

                        <input
                            type="date"
                            name="start_date"
                            required>

                    </div>


                    <!-- END DATE -->

                    <div class="form-group">

                        <label>
                            End Date
                        </label>

                        <input
                            type="date"
                            name="end_date"
                            required>

                    </div>


                </div>


                <button
                    type="submit"
                    class="save-button">

                    Add Academic Year

                </button>


            </form>

        </div>



        <!-- BACK -->

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


</body>

</html>