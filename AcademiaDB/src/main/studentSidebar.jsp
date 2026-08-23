<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="com.university.db.DBConnection" %>
<%
    String activePage = (String) request.getAttribute("activePage");
    if (activePage == null) {
        activePage = "";
    }
    
    int sidebarUserId = 0;
    Object userIdObj = session.getAttribute("userId");
    if (userIdObj != null) {
        sidebarUserId = (Integer) userIdObj;
    }
    
    int unreadNotifications = 0;
    if (sidebarUserId > 0) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE")) {
            ps.setInt(1, sidebarUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    unreadNotifications = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            // Silence log errors in view
        }
    }
%>
<aside class="sidebar">
    <nav class="sidebar-nav">
        <ul>
            <li class="<%= "dashboard".equals(activePage) ? "active" : "" %>">
                <a href="StudentServlet">
                    <span class="nav-icon">📊</span> Dashboard
                </a>
            </li>
            <li class="<%= "profile".equals(activePage) ? "active" : "" %>">
                <a href="StudentProfileServlet">
                    <span class="nav-icon">👤</span> My Profile
                </a>
            </li>
            <li class="<%= "subjects".equals(activePage) ? "active" : "" %>">
                <a href="SubjectServlet">
                    <span class="nav-icon">📚</span> My Subjects
                </a>
            </li>
            <li class="<%= "assignments".equals(activePage) ? "active" : "" %>">
                <a href="StudentAssignmentServlet">
                    <span class="nav-icon">📝</span> Assignments
                </a>
            </li>
            <li class="<%= "classTests".equals(activePage) ? "active" : "" %>">
                <a href="StudentTestServlet">
                    <span class="nav-icon">🧪</span> Class Tests
                </a>
            </li>
            <li class="<%= "attendance".equals(activePage) ? "active" : "" %>">
                <a href="StudentAttendanceServlet">
                    <span class="nav-icon">📋</span> Attendance
                </a>
            </li>
            <li class="<%= "performance".equals(activePage) ? "active" : "" %>">
                <a href="StudentPerformanceServlet">
                    <span class="nav-icon">📈</span> Performance
                </a>
            </li>
            <li class="<%= "studyMaterials".equals(activePage) ? "active" : "" %>">
                <a href="StudentMaterialServlet">
                    <span class="nav-icon">📚</span> Study Materials
                </a>
            </li>
            <li class="<%= "announcements".equals(activePage) ? "active" : "" %>">
                <a href="StudentAnnouncementServlet">
                    <span class="nav-icon">📢</span> Announcements
                </a>
            </li>
            <li class="<%= "calendar".equals(activePage) ? "active" : "" %>">
                <a href="StudentCalendarServlet">
                    <span class="nav-icon">📅</span> Calendar
                </a>
            </li>
            <li class="<%= "results".equals(activePage) ? "active" : "" %>">
                <a href="StudentResultServlet">
                    <span class="nav-icon">🎓</span> Results
                </a>
            </li>
            <li class="<%= "timetable".equals(activePage) ? "active" : "" %>">
                <a href="TimetableServlet">
                    <span class="nav-icon">🗓️</span> Timetable
                </a>
            </li>
            <li class="<%= "notifications".equals(activePage) ? "active" : "" %>">
                <a href="StudentNotificationServlet">
                    <span class="nav-icon">🔔</span> Notifications 
                    <% if (unreadNotifications > 0) { %>
                        <span class="badge badge-unread"><%= unreadNotifications %></span>
                    <% } %>
                </a>
            </li>
            <li class="<%= "changePassword".equals(activePage) ? "active" : "" %>">
                <a href="StudentChangePasswordServlet">
                    <span class="nav-icon">🔐</span> Change Password
                </a>
            </li>
            <li class="logout-link">
                <a href="LogoutServlet">
                    <span class="nav-icon">🚪</span> Logout
                </a>
            </li>
        </ul>
    </nav>
</aside>
