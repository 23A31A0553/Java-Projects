<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Map<String, Object>> notificationsList = (List<Map<String, Object>>) request.getAttribute("notificationsList");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .notifications-container {
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin-top: 20px;
        }
        .notification-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #ffffff;
            padding: 15px 20px;
            border-radius: var(--border-radius);
            box-shadow: var(--shadow);
            border: 1px solid var(--border-color);
            transition: all 0.2s;
        }
        .notification-row.unread {
            background-color: var(--light-blue);
            border-left: 4px solid var(--primary-blue);
        }
        .notification-content {
            flex: 1;
        }
        .notification-title {
            font-size: 15px;
            font-weight: 700;
            color: var(--dark-blue);
            margin-bottom: 5px;
        }
        .notification-msg {
            font-size: 13px;
            color: var(--text-color);
            margin-bottom: 4px;
        }
        .notification-date {
            font-size: 11px;
            color: var(--secondary-text);
        }
        .btn-mark {
            background-color: #ffffff;
            border: 1px solid var(--border-color);
            color: var(--primary-blue);
            padding: 6px 12px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s;
        }
        .btn-mark:hover {
            background-color: var(--primary-blue);
            color: #ffffff;
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
                
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <h2 class="section-title">🔔 My Notifications</h2>
                    <% if (notificationsList != null && !notificationsList.isEmpty()) { %>
                        <a href="StudentNotificationServlet?action=markAllAsRead" class="logout-button" style="text-decoration: none;">Mark All as Read</a>
                    <% } %>
                </div>

                <% if (notificationsList == null || notificationsList.isEmpty()) { %>
                    <div class="card">
                        <p class="text-muted">You are all caught up! No notifications available.</p>
                    </div>
                <% } else { %>
                    <div class="notifications-container">
                        <% for (Map<String, Object> n : notificationsList) { 
                            boolean isRead = (Boolean) n.get("isRead");
                        %>
                            <div class="notification-row <%= !isRead ? "unread" : "" %>">
                                <div class="notification-content">
                                    <div class="notification-title">
                                        <%= !isRead ? "🔵 " : "" %><%= n.get("title") %>
                                    </div>
                                    <div class="notification-msg"><%= n.get("message") %></div>
                                    <div class="notification-date"><%= n.get("createdAt") %></div>
                                </div>
                                <div style="margin-left: 20px;">
                                    <% if (!isRead) { %>
                                        <a href="StudentNotificationServlet?action=markAsRead&notificationId=<%= n.get("notificationId") %>" class="btn-mark">Mark as Read</a>
                                    <% } %>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } %>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
