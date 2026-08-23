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
                <a href="TeacherServlet">
                    <span class="nav-icon">📊</span> Dashboard
                </a>
            </li>
            <li class="<%= "profile".equals(activePage) ? "active" : "" %>">
                <a href="TeacherProfileServlet">
                    <span class="nav-icon">👤</span> My Profile
                </a>
            </li>
            <li class="<%= "students".equals(activePage) ? "active" : "" %>">
                <a href="TeacherStudentServlet">
                    <span class="nav-icon">👥</span> My Students
                </a>
            </li>
            <li class="<%= "assignments".equals(activePage) ? "active" : "" %>">
                <a href="AssignmentServlet">
                    <span class="nav-icon">📝</span> Assignments
                </a>
            </li>
            <li class="<%= "classTests".equals(activePage) ? "active" : "" %>">
                <a href="ClassTestServlet">
                    <span class="nav-icon">🧪</span> Class Tests
                </a>
            </li>
            <li class="<%= "attendance".equals(activePage) ? "active" : "" %>">
                <a href="AttendanceServlet">
                    <span class="nav-icon">📋</span> Attendance
                </a>
            </li>
            <li class="<%= "studyMaterials".equals(activePage) ? "active" : "" %>">
                <a href="StudyMaterialServlet">
                    <span class="nav-icon">📚</span> Study Materials
                </a>
            </li>
            <li class="<%= "announcements".equals(activePage) ? "active" : "" %>">
                <a href="AnnouncementServlet">
                    <span class="nav-icon">📢</span> Announcements
                </a>
            </li>
            <li class="<%= "calendar".equals(activePage) ? "active" : "" %>">
                <a href="CalendarServlet">
                    <span class="nav-icon">📅</span> Calendar
                </a>
            </li>
            <li class="<%= "performance".equals(activePage) ? "active" : "" %>">
                <a href="PerformanceServlet">
                    <span class="nav-icon">📈</span> Performance
                </a>
            </li>
            <li class="<%= "reports".equals(activePage) ? "active" : "" %>">
                <a href="ReportServlet">
                    <span class="nav-icon">📊</span> Reports
                </a>
            </li>
            <li class="<%= "notifications".equals(activePage) ? "active" : "" %>">
                <a href="NotificationServlet">
                    <span class="nav-icon">🔔</span> Notifications 
                    <% if (unreadNotifications > 0) { %>
                        <span class="badge badge-unread"><%= unreadNotifications %></span>
                    <% } %>
                </a>
            </li>
            <li class="<%= "changePassword".equals(activePage) ? "active" : "" %>">
                <a href="TeacherProfileServlet?action=changePasswordForm">
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
