<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || (!"ADMIN".equalsIgnoreCase(role) && !"TEACHER".equalsIgnoreCase(role))) {
        response.sendRedirect("index.jsp");
        return;
    }

    boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

    List<Map<String, Object>> announcements = (List<Map<String, Object>>) request.getAttribute("announcements");
    String search = request.getParameter("search");
    if (search == null) {
        search = "";
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    Map<String, Object> editAnnouncement = (Map<String, Object>) request.getAttribute("announcement");
    boolean editMode = editAnnouncement != null;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Announcements - BLUE RIDGE UNIVERSITY</title>
    <% if (isAdmin) { %>
        <link rel="stylesheet" href="css/admin.css">
    <% } else { %>
        <link rel="stylesheet" href="css/teacher.css">
        <link rel="stylesheet" href="css/announcements.css">
    <% } %>
    
    <% if (isAdmin) { %>
    <style>
        .announcement-container {
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
        .announcement-form {
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
        .form-group select,
        .form-group textarea {
            width: 100%;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 14px;
        }
        .form-group textarea {
            min-height: 130px;
            resize: vertical;
        }
        .full-width {
            grid-column: 1 / 3;
        }
        .form-buttons {
            grid-column: 1 / 3;
            display: flex;
            gap: 10px;
        }
        .save-button {
            background: #1565c0;
            color: white;
            border: none;
            padding: 11px 20px;
            border-radius: 6px;
            cursor: pointer;
        }
        .save-button:hover {
            background: #0d47a1;
        }
        .cancel-button {
            background: #eeeeee;
            color: #333;
            border: none;
            padding: 11px 20px;
            border-radius: 6px;
            cursor: pointer;
        }
        .cancel-button:hover {
            background: #dddddd;
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
        .announcement-table {
            width: 100%;
            border-collapse: collapse;
        }
        .announcement-table th,
        .announcement-table td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
            text-align: left;
            vertical-align: top;
        }
        .announcement-table th {
            background: #1565c0;
            color: white;
        }
        .announcement-table tr:hover {
            background: #f7f9fc;
        }
        .announcement-title {
            color: #0d47a1;
            font-weight: bold;
        }
        .announcement-message {
            max-width: 350px;
            line-height: 1.5;
            color: #555;
        }
        .audience {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 15px;
            background: #e3f2fd;
            color: #0d47a1;
            font-size: 11px;
            font-weight: bold;
            white-space: nowrap;
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
        .no-data {
            text-align: center;
            padding: 45px 20px;
            color: #777;
        }
        .edit-form-card {
            background: #f7f9fc;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            border: 1px solid #ddd;
        }
        .edit-form-card h3 {
            color: #0d47a1;
            margin-bottom: 18px;
        }
        .date-time {
            white-space: nowrap;
            font-size: 12px;
            color: #777;
        }
        @media(max-width: 750px) {
            .page-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }
            .announcement-form {
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
    <% } %>
</head>
<body>

    <!-- Header selection -->
    <% if (isAdmin) { %>
        <header class="top-bar">
            <div class="college-section">
                <img src="images/logo.jpeg" alt="College Logo" class="college-logo">
                <div>
                    <h1>BLUE RIDGE UNIVERSITY</h1>
                    <p>University Management System</p>
                </div>
            </div>
            <div class="admin-section">
                <a href="AdminServlet" class="profile-button">Dashboard</a>
                <a href="LogoutServlet" class="logout-button">Logout</a>
            </div>
        </header>
    <% } else { %>
        <jsp:include page="teacherHeader.jsp" />
    <% } %>

    <% if (isAdmin) { %>
        <!-- ADMIN CONTAINER WRAPPER -->
        <div class="main-content">
    <% } else { %>
        <!-- TEACHER CONTAINER WRAPPER -->
        <div class="app-layout">
            <jsp:include page="teacherSidebar.jsp" />
            <main class="main-content-wrapper">
    <% } %>

            <div class="announcement-container">

                <div class="page-header">
                    <div>
                        <h2>Announcements</h2>
                        <p>Create and manage university announcements.</p>
                    </div>
                    <button type="button" class="add-button" onclick="showAddForm()">+ Add Announcement</button>
                </div>

                <!-- Messages -->
                <% if ("added".equals(success)) { %>
                    <div class="message success-message">Announcement added successfully.</div>
                <% } %>
                <% if ("updated".equals(success)) { %>
                    <div class="message success-message">Announcement updated successfully.</div>
                <% } %>
                <% if ("deleted".equals(success)) { %>
                    <div class="message success-message">Announcement deleted successfully.</div>
                <% } %>
                <% if ("activated".equals(success)) { %>
                    <div class="message success-message">Announcement activated successfully.</div>
                <% } %>
                <% if ("deactivated".equals(success)) { %>
                    <div class="message success-message">Announcement deactivated successfully.</div>
                <% } %>
                <% if ("empty".equals(error)) { %>
                    <div class="message error-message">Please fill in all required fields.</div>
                <% } %>
                <% if ("notfound".equals(error)) { %>
                    <div class="message error-message">Announcement was not found.</div>
                <% } %>
                <% if ("database".equals(error)) { %>
                    <div class="message error-message">Database error. Please try again.</div>
                <% } %>

                <!-- Add Form -->
                <div class="form-card" id="addForm">
                    <h3>Add Announcement</h3>
                    <form action="AnnouncementServlet" method="post" class="announcement-form">
                        <input type="hidden" name="action" value="add">
                        
                        <div class="form-group">
                            <label>Announcement Title</label>
                            <input type="text" name="title" maxlength="200" placeholder="Enter announcement title" required>
                        </div>
                        
                        <div class="form-group">
                            <label>Audience</label>
                            <select name="audience" required>
                                <% if (isAdmin) { %>
                                    <option value="ALL">Everyone</option>
                                    <option value="STUDENT">Students</option>
                                    <option value="TEACHER">Teachers</option>
                                    <option value="ADMIN">Administrators</option>
                                <% } else { %>
                                    <option value="STUDENT">My Students</option>
                                    <option value="ALL">Everyone</option>
                                <% } %>
                            </select>
                        </div>

                        <% if (!isAdmin) { %>
                            <div class="form-group full-width">
                                <label>Subject (Optional)</label>
                                <input type="text" name="subject" placeholder="e.g. CS-301, General">
                            </div>
                        <% } %>

                        <div class="form-group full-width">
                            <label>Announcement Message</label>
                            <textarea name="message" placeholder="Write your announcement here..." required></textarea>
                        </div>
                        
                        <div class="form-buttons">
                            <button type="submit" class="save-button">Publish Announcement</button>
                            <button type="button" class="cancel-button" onclick="hideAddForm()">Cancel</button>
                        </div>
                    </form>
                </div>

                <!-- Edit Form -->
                <% if (editMode) { %>
                    <div class="edit-form-card">
                        <h3>Edit Announcement</h3>
                        <form action="AnnouncementServlet" method="post" class="announcement-form">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="announcement_id" value="<%= editAnnouncement.get("announcementId") %>">
                            
                            <div class="form-group">
                                <label>Announcement Title</label>
                                <input type="text" name="title" value="<%= escapeAttribute(String.valueOf(editAnnouncement.get("title"))) %>" maxlength="200" required>
                            </div>
                            
                            <div class="form-group">
                                <label>Audience</label>
                                <select name="audience" required>
                                    <%
                                        String editAudience = String.valueOf(editAnnouncement.get("audience"));
                                    %>
                                    <% if (isAdmin) { %>
                                        <option value="ALL" <%= "ALL".equalsIgnoreCase(editAudience) ? "selected" : "" %>>Everyone</option>
                                        <option value="STUDENT" <%= "STUDENT".equalsIgnoreCase(editAudience) ? "selected" : "" %>>Students</option>
                                        <option value="TEACHER" <%= "TEACHER".equalsIgnoreCase(editAudience) ? "selected" : "" %>>Teachers</option>
                                        <option value="ADMIN" <%= "ADMIN".equalsIgnoreCase(editAudience) ? "selected" : "" %>>Administrators</option>
                                    <% } else { %>
                                        <option value="STUDENT" <%= "STUDENT".equalsIgnoreCase(editAudience) ? "selected" : "" %>>My Students</option>
                                        <option value="ALL" <%= "ALL".equalsIgnoreCase(editAudience) ? "selected" : "" %>>Everyone</option>
                                    <% } %>
                                </select>
                            </div>

                            <% if (!isAdmin) { %>
                                <div class="form-group full-width">
                                    <label>Subject (Optional)</label>
                                    <input type="text" name="subject" value="<%= editAnnouncement.get("subject") != null ? escapeAttribute(String.valueOf(editAnnouncement.get("subject"))) : "" %>">
                                </div>
                            <% } %>

                            <div class="form-group full-width">
                                <label>Announcement Message</label>
                                <textarea name="message" required><%= escapeHtml(String.valueOf(editAnnouncement.get("message"))) %></textarea>
                            </div>
                            
                            <div class="form-buttons">
                                <button type="submit" class="save-button">Save Changes</button>
                                <a href="AnnouncementServlet" class="clear-button">Cancel</a>
                            </div>
                        </form>
                    </div>
                <% } %>

                <!-- Search Card -->
                <div class="search-card">
                    <form action="AnnouncementServlet" method="get" class="search-form">
                        <input type="text" name="search" value="<%= escapeAttribute(search) %>" placeholder="Search announcements...">
                        <button type="submit" class="search-button">Search</button>
                        <a href="AnnouncementServlet" class="clear-button">Clear</a>
                    </form>
                </div>

                <!-- Table Card -->
                <div class="table-card card">
                    <table class="announcement-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Message</th>
                                <th>Audience</th>
                                <% if (!isAdmin) { %>
                                    <th>Subject</th>
                                <% } %>
                                <th>Status</th>
                                <th>Created</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <%
                            if (announcements != null && !announcements.isEmpty()) {
                                for (Map<String, Object> announcement : announcements) {
                                    Object id = announcement.get("announcementId");
                                    String title = String.valueOf(announcement.get("title"));
                                    String message = String.valueOf(announcement.get("message"));
                                    String audience = String.valueOf(announcement.get("audience"));
                                    String subject = announcement.get("subject") != null ? String.valueOf(announcement.get("subject")) : "-";
                                    String status = String.valueOf(announcement.get("status"));
                                    Object createdAt = announcement.get("createdAt");
                        %>
                            <tr>
                                <td><%= id %></td>
                                <td><div class="announcement-title"><%= escapeHtml(title) %></div></td>
                                <td><div class="announcement-message"><%= escapeHtml(message) %></div></td>
                                <td><span class="audience"><%= escapeHtml(audience) %></span></td>
                                <% if (!isAdmin) { %>
                                    <td><%= escapeHtml(subject) %></td>
                                <% } %>
                                <td>
                                    <% if ("ACTIVE".equalsIgnoreCase(status) || "PUBLISHED".equalsIgnoreCase(status)) { %>
                                        <span class="status active-status">ACTIVE</span>
                                    <% } else { %>
                                        <span class="status inactive-status">INACTIVE</span>
                                    <% } %>
                                </td>
                                <td><span class="date-time"><%= createdAt %></span></td>
                                <td>
                                    <div class="action-btn-group">
                                        <a href="AnnouncementServlet?action=editForm&id=<%= id %>" class="btn btn-table btn-edit">Edit</a>
                                        
                                        <% if ("ACTIVE".equalsIgnoreCase(status) || "PUBLISHED".equalsIgnoreCase(status)) { %>
                                            <form action="AnnouncementServlet" method="post" style="display:inline;">
                                                <input type="hidden" name="action" value="changeStatus">
                                                <input type="hidden" name="status" value="DRAFT">
                                                <input type="hidden" name="announcement_id" value="<%= id %>">
                                                <button type="submit" class="btn btn-table btn-delete" onclick="return confirm('Deactivate this announcement?');">Deactivate</button>
                                            </form>
                                        <% } else { %>
                                            <form action="AnnouncementServlet" method="post" style="display:inline;">
                                                <input type="hidden" name="action" value="changeStatus">
                                                <input type="hidden" name="status" value="PUBLISHED">
                                                <input type="hidden" name="announcement_id" value="<%= id %>">
                                                <button type="submit" class="btn btn-table btn-view" onclick="return confirm('Activate this announcement?');">Activate</button>
                                            </form>
                                        <% } %>

                                        <form action="AnnouncementServlet" method="post" style="display:inline;">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="announcement_id" value="<%= id %>">
                                            <button type="submit" class="btn btn-table btn-delete" onclick="return confirm('Delete this announcement permanently?');">Delete</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="<%= !isAdmin ? "8" : "7" %>" class="no-data">
                                    <h3>No Announcements Found</h3>
                                    <p>Create an announcement using the Add Announcement button.</p>
                                </td>
                            </tr>
                        <%
                            }
                        %>
                        </tbody>
                    </table>
                </div>

            </div>

    <% if (isAdmin) { %>
        </div>
        <footer class="footer">
            <p>© 2026 BLUE RIDGE UNIVERSITY. All Rights Reserved. Made by Penugonda Devashish.</p>
        </footer>
    <% } else { %>
            </main>
        </div>
        <jsp:include page="teacherFooter.jsp" />
    <% } %>

    <script>
        function showAddForm() {
            const form = document.getElementById("addForm");
            form.style.display = "block";
            form.scrollIntoView({ behavior: "smooth", block: "center" });
        }
        function hideAddForm() {
            document.getElementById("addForm").style.display = "none";
        }
    </script>
</body>
</html>
<%!
private String escapeHtml(String value) {
    if (value == null) return "";
    return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
}
private String escapeAttribute(String value) {
    return escapeHtml(value);
}
%>