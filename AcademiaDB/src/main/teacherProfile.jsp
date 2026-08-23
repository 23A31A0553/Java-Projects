<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"TEACHER".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String tab = request.getParameter("tab");
    if (tab == null) tab = "details";

    String error = request.getParameter("error");
    String success = request.getParameter("success");

    // Profile Details
    String profileUsername = (String) request.getAttribute("profileUsername");
    String profileFirstName = (String) request.getAttribute("profileFirstName");
    String profileLastName = (String) request.getAttribute("profileLastName");
    String profileEmail = (String) request.getAttribute("profileEmail");
    String profileMobile = (String) request.getAttribute("profileMobile");
    String profileStatus = (String) request.getAttribute("profileStatus");
    String profileDepartment = (String) request.getAttribute("profileDepartment");
    String profileEmployeeType = (String) request.getAttribute("profileEmployeeType");

    if (profileUsername == null) {
        // Redirect to servlet to load details if they aren't loaded in request
        response.sendRedirect("TeacherProfileServlet");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/teacher-profile.css">
</head>
<body>

    <!-- Shared Header -->
    <jsp:include page="teacherHeader.jsp" />

    <div class="app-layout">
        <!-- Shared Sidebar -->
        <jsp:include page="teacherSidebar.jsp" />

        <main class="main-content-wrapper">
            <div class="teacher-container">

                <!-- Alert Messages -->
                <% if ("empty".equals(error)) { %>
                    <div class="alert alert-danger">Please fill in all fields.</div>
                <% } else if ("mismatch".equals(error)) { %>
                    <div class="alert alert-danger">New passwords do not match.</div>
                <% } else if ("weak".equals(error)) { %>
                    <div class="alert alert-danger">Password must be at least 6 characters long.</div>
                <% } else if ("invalidcurrent".equals(error)) { %>
                    <div class="alert alert-danger">Current password is incorrect.</div>
                <% } else if ("invalidfields".equals(error)) { %>
                    <div class="alert alert-danger">Please enter a valid email and mobile number.</div>
                <% } else if ("emailexists".equals(error)) { %>
                    <div class="alert alert-danger">Email already exists.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="alert alert-danger">Database error. Please try again.</div>
                <% } %>

                <% if ("profileupdated".equals(success)) { %>
                    <div class="alert alert-success">Profile updated successfully.</div>
                <% } else if ("passwordchanged".equals(success)) { %>
                    <div class="alert alert-success">Password changed successfully.</div>
                <% } %>

                <!-- Profile Container Card -->
                <div class="profile-card card">
                    
                    <!-- Tabs Header -->
                    <div class="profile-tabs">
                        <button class="tab-btn <%= "details".equals(tab) ? "active" : "" %>" onclick="switchTab('details')">📋 Profile Details</button>
                        <button class="tab-btn <%= "edit".equals(tab) ? "active" : "" %>" onclick="switchTab('edit')">✏️ Edit Contact info</button>
                        <button class="tab-btn <%= "password".equals(tab) ? "active" : "" %>" onclick="switchTab('password')">🔐 Change Password</button>
                    </div>

                    <!-- Tab content: Details -->
                    <div id="tab-details" class="tab-content <%= "details".equals(tab) ? "active" : "" %>">
                        <h3 class="section-title">My Profile</h3>
                        <div class="profile-details-grid">
                            <div class="profile-detail-item">
                                <span class="detail-label">Full Name</span>
                                <span class="detail-value"><%= profileFirstName %> <%= profileLastName %></span>
                            </div>
                            <div class="profile-detail-item">
                                <span class="detail-label">Username</span>
                                <span class="detail-value"><%= profileUsername %></span>
                            </div>
                            <div class="profile-detail-item">
                                <span class="detail-label">Email Address</span>
                                <span class="detail-value"><%= profileEmail %></span>
                            </div>
                            <div class="profile-detail-item">
                                <span class="detail-label">Mobile Number</span>
                                <span class="detail-value"><%= profileMobile %></span>
                            </div>
                            <div class="profile-detail-item">
                                <span class="detail-label">Department</span>
                                <span class="detail-value"><%= profileDepartment %></span>
                            </div>
                            <div class="profile-detail-item">
                                <span class="detail-label">Employee Type</span>
                                <span class="detail-value"><%= profileEmployeeType %></span>
                            </div>
                            <div class="profile-detail-item">
                                <span class="detail-label">Account Status</span>
                                <span class="detail-value status-badge status-<%= profileStatus.toLowerCase() %>"><%= profileStatus %></span>
                            </div>
                        </div>
                    </div>

                    <!-- Tab content: Edit Contact -->
                    <div id="tab-edit" class="tab-content <%= "edit".equals(tab) ? "active" : "" %>">
                        <h3 class="section-title">Update Contact Information</h3>
                        <form action="TeacherProfileServlet" method="post" class="profile-form">
                            <input type="hidden" name="action" value="updateProfile">
                            
                            <div class="form-group">
                                <label for="email">Email Address</label>
                                <input type="email" id="email" name="email" value="<%= profileEmail %>" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="mobile">Mobile Number</label>
                                <input type="text" id="mobile" name="mobile" value="<%= profileMobile %>" required>
                            </div>
                            
                            <button type="submit" class="btn btn-primary">Save Changes</button>
                        </form>
                    </div>

                    <!-- Tab content: Change Password -->
                    <div id="tab-password" class="tab-content <%= "password".equals(tab) ? "active" : "" %>">
                        <h3 class="section-title">Change Account Password</h3>
                        <form action="TeacherProfileServlet" method="post" class="profile-form">
                            <input type="hidden" name="action" value="changePassword">
                            
                            <div class="form-group">
                                <label for="currentPassword">Current Password</label>
                                <input type="password" id="currentPassword" name="currentPassword" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="newPassword">New Password</label>
                                <input type="password" id="newPassword" name="newPassword" placeholder="Minimum 6 characters" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="confirmPassword">Confirm New Password</label>
                                <input type="password" id="confirmPassword" name="confirmPassword" required>
                            </div>
                            
                            <button type="submit" class="btn btn-primary">Update Password</button>
                        </form>
                    </div>

                </div>

            </div>
        </main>
    </div>

    <!-- Shared Footer -->
    <jsp:include page="teacherFooter.jsp" />

    <script>
        function switchTab(tabId) {
            // Remove active classes
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

            // Find current clicked button and tab content
            const clickedBtn = Array.from(document.querySelectorAll('.tab-btn')).find(btn => btn.innerText.toLowerCase().includes(tabId));
            if(clickedBtn) clickedBtn.classList.add('active');

            const activeContent = document.getElementById('tab-' + tabId);
            if(activeContent) activeContent.classList.add('active');
            
            // Update URL without reload to remember active tab on error/success redirects
            const url = new URL(window.location);
            url.searchParams.set('tab', tabId);
            window.history.pushState({}, '', url);
        }
    </script>
</body>
</html>
