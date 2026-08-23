<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String error = request.getParameter("error");
    String success = request.getParameter("success");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Change Password - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .password-layout {
            max-width: 500px;
            margin: 30px auto 0 auto;
        }
        .form-group {
            margin-bottom: 18px;
        }
        .form-group label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            color: var(--secondary-text);
            margin-bottom: 6px;
        }
        .form-control {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid var(--border-color);
            border-radius: var(--border-radius);
            font-size: 14px;
            color: var(--text-color);
        }
        .btn-submit {
            background-color: var(--primary-blue);
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: var(--border-radius);
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.2s;
            width: 100%;
            margin-top: 10px;
        }
        .btn-submit:hover {
            background-color: var(--dark-blue);
        }
        .message-alert {
            padding: 15px;
            border-radius: var(--border-radius);
            margin-bottom: 20px;
            font-size: 14px;
            font-weight: 500;
        }
        .alert-success {
            background-color: #DEF7EC;
            color: #03543F;
            border: 1px solid #BCF0DA;
        }
        .alert-danger {
            background-color: #FDE8E8;
            color: #9B1C1C;
            border: 1px solid #FBD5D5;
        }
    </style>
</head>
<body>

    <!-- Shared Header -->
    <jsp:include page="studentHeader.jsp" />

    <div class="app-layout">
        <!-- Shared Sidebar -->
        <jsp:include page="studentSidebar.jsp" />

        <main class="main-content-wrapper">
            <div class="teacher-container">
                
                <div class="password-layout">
                    <div class="card">
                        <h2 class="section-title" style="font-size: 20px; text-align: center; margin-bottom: 10px;">🔐 Change Password</h2>
                        <p style="text-align: center; color: var(--secondary-text); font-size: 13px; margin-bottom: 20px;">Protect your student account by configuring a new password.</p>

                        <% if ("changed".equals(success)) { %>
                            <div class="message-alert alert-success">✓ Your password has been changed successfully.</div>
                        <% } else if ("empty".equals(error)) { %>
                            <div class="message-alert alert-danger">⚠ All password fields are required.</div>
                        <% } else if ("mismatch".equals(error)) { %>
                            <div class="message-alert alert-danger">⚠ New password and confirmation do not match.</div>
                        <% } else if ("wrongcurrent".equals(error)) { %>
                            <div class="message-alert alert-danger">⚠ Incorrect current password. Please try again.</div>
                        <% } else if ("database".equals(error)) { %>
                            <div class="message-alert alert-danger">⚠ Database error updating password. Please try again.</div>
                        <% } %>

                        <form action="StudentChangePasswordServlet" method="POST">
                            
                            <div class="form-group">
                                <label>Current Password</label>
                                <input type="password" name="currentPassword" class="form-control" placeholder="••••••••" required>
                            </div>

                            <div class="form-group">
                                <label>New Password</label>
                                <input type="password" name="newPassword" class="form-control" placeholder="••••••••" required>
                            </div>

                            <div class="form-group">
                                <label>Confirm New Password</label>
                                <input type="password" name="confirmPassword" class="form-control" placeholder="••••••••" required>
                            </div>

                            <button type="submit" class="btn-submit">Update Password</button>

                        </form>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
