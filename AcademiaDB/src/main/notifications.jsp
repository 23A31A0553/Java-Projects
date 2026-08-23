<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.university.model.Notification" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    List<Notification> list = (List<Notification>) request.getAttribute("notifications");
    if (list == null) {
        response.sendRedirect("NotificationServlet");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/notifications.css">
</head>
<body>

    <!-- Shared Header selection -->
    <% if ("ADMIN".equalsIgnoreCase(role)) { %>
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

    <div class="app-layout">
        <!-- Sidebar selection -->
        <% if ("ADMIN".equalsIgnoreCase(role)) { %>
            <!-- Admin sidebar is handled inside Admin views, but notifications page is shared, so we wrap appropriately or let admins see it -->
        <% } else { %>
            <jsp:include page="teacherSidebar.jsp" />
        <% } %>

        <main class="main-content-wrapper">
            <div class="teacher-container">

                <div class="notifications-header-panel card">
                    <div class="header-details">
                        <h2>🔔 Notifications Center</h2>
                        <p>Inbox for your class activity alerts, student submissions, and announcements.</p>
                    </div>
                    <% if (!list.isEmpty()) { %>
                        <form action="NotificationServlet" method="post" style="margin: 0;">
                            <input type="hidden" name="action" value="readAll">
                            <button type="submit" class="btn btn-primary btn-mark-all">✓ Mark All as Read</button>
                        </form>
                    <% } %>
                </div>

                <!-- Alert Messages -->
                <% if ("read".equals(success)) { %>
                    <div class="alert alert-success">Notification marked as read.</div>
                <% } else if ("readall".equals(success)) { %>
                    <div class="alert alert-success">All notifications marked as read.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="alert alert-danger">Database error. Please try again.</div>
                <% } %>

                <!-- Notifications List -->
                <div class="notifications-list-wrapper">
                    <%
                        if (list.isEmpty()) {
                    %>
                        <div class="no-notifications-card card">
                            <div class="no-notif-icon">🔔</div>
                            <h3>All Caught Up!</h3>
                            <p>You have no new notifications at the moment.</p>
                        </div>
                    <%
                        } else {
                            for (Notification n : list) {
                                String unreadClass = n.isRead() ? "" : "unread-notification";
                    %>
                        <div class="notification-item-card card <%= unreadClass %>">
                            <div class="notif-bullet-indicator"></div>
                            
                            <div class="notif-content-area">
                                <h4 class="notif-title"><%= n.getTitle() %></h4>
                                <p class="notif-msg"><%= n.getMessage() %></p>
                                <span class="notif-timestamp">🕒 <%= n.getCreatedAt() %></span>
                            </div>
                            
                            <div class="notif-action-area">
                                <% if (!n.isRead()) { %>
                                    <form action="NotificationServlet" method="post" style="margin: 0;">
                                        <input type="hidden" name="action" value="read">
                                        <input type="hidden" name="notificationId" value="<%= n.getNotificationId() %>">
                                        <button type="submit" class="btn btn-table btn-view">Mark Read</button>
                                    </form>
                                <% } else { %>
                                    <span class="read-indicator-tag">Read</span>
                                <% } %>
                            </div>
                        </div>
                    <%
                            }
                        }
                    %>
                </div>

            </div>
        </main>
    </div>

    <!-- Footer selection -->
    <% if ("ADMIN".equalsIgnoreCase(role)) { %>
        <footer class="footer">
            <p>© 2026 BLUE RIDGE UNIVERSITY. All Rights Reserved. Made by Penugonda Devashish.</p>
        </footer>
    <% } else { %>
        <jsp:include page="teacherFooter.jsp" />
    <% } %>

</body>
</html>
