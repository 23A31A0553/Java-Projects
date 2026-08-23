<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    int studentId = (Integer) request.getAttribute("studentId");
    String username = (String) request.getAttribute("username");
    String firstName = (String) request.getAttribute("firstName");
    String lastName = (String) request.getAttribute("lastName");
    String email = (String) request.getAttribute("email");
    String mobile = (String) request.getAttribute("mobile");
    String status = (String) request.getAttribute("status");
    String department = (String) request.getAttribute("department");
    String semester = (String) request.getAttribute("semester");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .profile-layout {
            display: grid;
            grid-template-columns: 1fr 2fr;
            gap: 25px;
            margin-top: 20px;
        }
        @media(max-width: 800px) {
            .profile-layout {
                grid-template-columns: 1fr;
            }
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
            background-color: #fcfcfc;
        }
        .form-control[readonly] {
            background-color: #f1f5f9;
            color: #475569;
            cursor: not-allowed;
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
                
                <h2 class="section-title">👤 My Profile</h2>

                <% if ("updated".equals(success)) { %>
                    <div class="message-alert alert-success">✓ Profile updated successfully.</div>
                <% } else if ("empty".equals(error)) { %>
                    <div class="message-alert alert-danger">⚠ Email field cannot be empty.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="message-alert alert-danger">⚠ Error updating database. Please try again.</div>
                <% } %>

                <div class="profile-layout">
                    
                    <!-- Left column: Avatar card -->
                    <div class="card" style="text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 250px;">
                        <div class="student-avatar" style="width: 100px; height: 100px; font-size: 40px; margin-bottom: 15px;">
                            <%= firstName.substring(0,1).toUpperCase() %>
                        </div>
                        <h3 style="margin-bottom: 5px;"><%= firstName %> <%= lastName %></h3>
                        <p style="color: var(--secondary-text); font-size: 14px; margin-bottom: 15px;"><%= department %></p>
                        <span class="status-badge status-active" style="text-transform: uppercase;"><%= status %></span>
                    </div>

                    <!-- Right column: Details and Edit Form -->
                    <div class="card">
                        <form action="StudentProfileServlet" method="POST">
                            
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                                <div class="form-group">
                                    <label>Student ID</label>
                                    <input type="text" class="form-control" value="<%= studentId %>" readonly>
                                </div>
                                <div class="form-group">
                                    <label>Username</label>
                                    <input type="text" class="form-control" value="<%= username %>" readonly>
                                </div>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                                <div class="form-group">
                                    <label>First Name</label>
                                    <input type="text" class="form-control" value="<%= firstName %>" readonly>
                                </div>
                                <div class="form-group">
                                    <label>Last Name</label>
                                    <input type="text" class="form-control" value="<%= lastName %>" readonly>
                                </div>
                            </div>

                            <div class="form-group">
                                <label>Email Address</label>
                                <input type="email" name="email" class="form-control" value="<%= email %>" required>
                            </div>

                            <div class="form-group">
                                <label>Mobile Number</label>
                                <input type="text" name="mobile" class="form-control" value="<%= mobile != null ? mobile : "" %>">
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                                <div class="form-group">
                                    <label>Department</label>
                                    <input type="text" class="form-control" value="<%= department %>" readonly>
                                </div>
                                <div class="form-group">
                                    <label>Current Semester</label>
                                    <input type="text" class="form-control" value="Semester <%= semester %>" readonly>
                                </div>
                            </div>

                            <div style="margin-top: 20px;">
                                <button type="submit" class="btn-submit">Save Profile Changes</button>
                            </div>

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
