<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
    String error = request.getParameter("error");
    String logout = request.getParameter("logout");
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        Login - BLUE RIDGE UNIVERSITY
    </title>

    <link rel="stylesheet"
          href="css/login.css">

    <style>

        * {
            box-sizing: border-box;
        }


body {
    margin: 0;

    min-height: 100vh;

    font-family:
        Arial,
        Helvetica,
        sans-serif;

    background-image:
        linear-gradient(
            rgba(0, 0, 0, 0.45),
            rgba(0, 0, 0, 0.45)
        ),
        url("images/background.png");

    background-size: cover;

    background-position: center;

    background-repeat: no-repeat;

    background-attachment: fixed;

    display: flex;

    justify-content: center;

    align-items: center;
}


        .login-container {

            width: 100%;

            max-width: 430px;

            padding: 20px;
        }


        .login-card {

            background: white;

            border-radius: 12px;

            padding: 35px;

            box-shadow:
                0 8px 30px
                rgba(0,0,0,0.15);
        }


        .logo-area {

            text-align: center;

            margin-bottom: 25px;
        }


        .college-logo {

            width: 80px;

            height: 80px;

            object-fit: contain;

            border-radius: 50%;

            margin-bottom: 12px;
        }


        .logo-area h1 {

            margin: 0;

            color: #0d47a1;

            font-size: 22px;
        }


        .logo-area p {

            margin-top: 7px;

            color: #777;

            font-size: 13px;
        }


        /* ================================================
           MESSAGES
           ================================================ */

        .message {

            padding: 12px;

            border-radius: 6px;

            margin-bottom: 18px;

            font-size: 13px;

            text-align: center;
        }


        .error-message {

            background: #f8d7da;

            color: #721c24;
        }


        .success-message {

            background: #d4edda;

            color: #155724;
        }


        /* ================================================
           FORM
           ================================================ */

        .form-group {

            margin-bottom: 18px;
        }


        .form-group label {

            display: block;

            margin-bottom: 7px;

            color: #333;

            font-size: 13px;

            font-weight: bold;
        }


        .form-group input,
        .form-group select {

            width: 100%;

            padding: 12px;

            border:
                1px solid #ccc;

            border-radius: 6px;

            outline: none;

            font-size: 14px;
        }


        .form-group input:focus,
        .form-group select:focus {

            border-color: #1565c0;

            box-shadow:
                0 0 0 2px
                rgba(21,101,192,0.1);
        }


        .login-button {

            width: 100%;

            padding: 13px;

            border: none;

            border-radius: 6px;

            background: #1565c0;

            color: white;

            font-size: 15px;

            font-weight: bold;

            cursor: pointer;

            margin-top: 5px;
        }


        .login-button:hover {

            background: #0d47a1;
        }


        .footer-text {

            text-align: center;

            color: #888;

            font-size: 11px;

            margin-top: 25px;
        }


        @media(max-width: 480px) {

            .login-card {

                padding: 25px 20px;
            }

        }

    </style>

</head>


<body>


<div class="login-container">


    <div class="login-card">


        <!-- ============================================
             LOGO
             ============================================ -->

        <div class="logo-area">


            <img
                src="images/logo.jpeg"
                alt="University Logo"
                class="college-logo">


            <h1>
                BLUE RIDGE UNIVERSITY
            </h1>


            <p>
                University Management System
            </p>


        </div>



        <!-- ============================================
             SUCCESS MESSAGE
             ============================================ -->

        <% if ("success".equals(logout)) { %>

            <div class="message success-message">

                You have been logged out successfully.

            </div>

        <% } %>



        <!-- ============================================
             ERROR MESSAGES
             ============================================ -->

        <% if ("empty".equals(error)) { %>

            <div class="message error-message">

                Please enter username and password.

            </div>

        <% } %>


        <% if ("invalid".equals(error)) { %>

            <div class="message error-message">

                Invalid username, password or role.

            </div>

        <% } %>


        <% if ("inactive".equals(error)) { %>

            <div class="message error-message">

                Your account is inactive.
                Please contact the administrator.

            </div>

        <% } %>


        <% if ("invalidrole".equals(error)) { %>

            <div class="message error-message">

                Please select a valid user type.

            </div>

        <% } %>


        <% if ("unauthorized".equals(error)) { %>

            <div class="message error-message">

                You are not authorized to access
                that page.

            </div>

        <% } %>


        <% if ("session".equals(error)) { %>

            <div class="message error-message">

                Your session has expired.
                Please login again.

            </div>

        <% } %>


        <% if ("database".equals(error)) { %>

            <div class="message error-message">

                Database connection error.
                Please try again.

            </div>

        <% } %>



        <!-- ============================================
             LOGIN FORM
             ============================================ -->

        <form
            action="LoginServlet"
            method="post">


            <!-- USERNAME -->

            <div class="form-group">

                <label for="username">

                    Username

                </label>


                <input
                    type="text"
                    id="username"
                    name="username"
                    placeholder="Enter username"
                    autocomplete="username"
                    required>

            </div>



            <!-- PASSWORD -->

            <div class="form-group">

                <label for="password">

                    Password

                </label>


                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter password"
                    autocomplete="current-password"
                    required>

            </div>



            <!-- ROLE -->

            <div class="form-group">

                <label for="role">

                    Login As

                </label>


                <select
                    id="role"
                    name="role"
                    required>


                    <option value="">

                        Select User Type

                    </option>


                    <option value="ADMIN">

                        Administrator

                    </option>


                    <option value="TEACHER">

                        Teacher

                    </option>


                    <option value="STUDENT">

                        Student

                    </option>


                </select>

            </div>



            <!-- LOGIN -->

            <button
                type="submit"
                class="login-button">

                Login

            </button>


        </form>



        <div class="footer-text">

            © 2026 BLUE RIDGE UNIVERSITY

        </div>


    </div>

</div>


</body>

</html>