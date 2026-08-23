<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"TEACHER".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String teacherFirstName = (String) request.getAttribute("teacherFirstName");
    if (teacherFirstName == null) {
        teacherFirstName = (String) session.getAttribute("firstName");
    }
    if (teacherFirstName == null) teacherFirstName = "Teacher";

    // Statistics
    Integer totalStudents = (Integer) request.getAttribute("totalStudents");
    Integer totalAssignments = (Integer) request.getAttribute("totalAssignments");
    Integer totalClassTests = (Integer) request.getAttribute("totalClassTests");
    Integer pendingSubmissions = (Integer) request.getAttribute("pendingSubmissions");
    Integer upcomingTests = (Integer) request.getAttribute("upcomingTests");

    int studentsVal = totalStudents != null ? totalStudents : 0;
    int assignmentsVal = totalAssignments != null ? totalAssignments : 0;
    int testsVal = totalClassTests != null ? totalClassTests : 0;
    int pendingVal = pendingSubmissions != null ? pendingSubmissions : 0;
    int upcomingVal = upcomingTests != null ? upcomingTests : 0;

    List<Map<String, Object>> recentActivities = (List<Map<String, Object>>) request.getAttribute("recentActivities");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Teacher Dashboard - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
</head>
<body>

    <!-- Shared Topbar Header -->
    <jsp:include page="teacherHeader.jsp" />

    <div class="app-layout">
        <!-- Shared Sidebar Navigation -->
        <jsp:include page="teacherSidebar.jsp" />

        <!-- Main Workspace -->
        <main class="main-content-wrapper">
            <div class="teacher-container">
                
                <!-- Welcome Card -->
                <div class="welcome-card card">
                    <h2>Welcome, <%= teacherFirstName %></h2>
                    <p>Manage your students, assignments, tests, attendance, and academic activities from one place.</p>
                </div>

                <!-- Statistics Grid -->
                <div class="stats-grid">
                    <div class="stat-card card">
                        <div class="stat-icon icon-students">👥</div>
                        <div class="stat-info">
                            <span class="stat-number"><%= studentsVal %></span>
                            <span class="stat-label">Total Students</span>
                        </div>
                    </div>
                    <div class="stat-card card">
                        <div class="stat-icon icon-assignments">📝</div>
                        <div class="stat-info">
                            <span class="stat-number"><%= assignmentsVal %></span>
                            <span class="stat-label">Total Assignments</span>
                        </div>
                    </div>
                    <div class="stat-card card">
                        <div class="stat-icon icon-tests">🧪</div>
                        <div class="stat-info">
                            <span class="stat-number"><%= testsVal %></span>
                            <span class="stat-label">Class Tests</span>
                        </div>
                    </div>
                    <div class="stat-card card">
                        <div class="stat-icon icon-pending">⚠️</div>
                        <div class="stat-info">
                            <span class="stat-number"><%= pendingVal %></span>
                            <span class="stat-label">Pending Work</span>
                        </div>
                    </div>
                    <div class="stat-card card">
                        <div class="stat-icon icon-upcoming">📅</div>
                        <div class="stat-info">
                            <span class="stat-number"><%= upcomingVal %></span>
                            <span class="stat-label">Upcoming Tests</span>
                        </div>
                    </div>
                </div>

                <!-- Quick Actions & Recent Activity Flex Layout -->
                <div class="dashboard-flex-grid">
                    
                    <!-- Quick Actions Card -->
                    <div class="dashboard-section card">
                        <h3 class="section-title">⚡ Quick Actions</h3>
                        <div class="actions-list">
                            <a href="AssignmentServlet" class="action-btn-link">
                                <span class="btn-emoji">📝</span> Add Assignment
                            </a>
                            <a href="ClassTestServlet" class="action-btn-link">
                                <span class="btn-emoji">🧪</span> Create Class Test
                            </a>
                            <a href="AttendanceServlet?action=markForm" class="action-btn-link">
                                <span class="btn-emoji">📋</span> Mark Attendance
                            </a>
                            <a href="StudyMaterialServlet" class="action-btn-link">
                                <span class="btn-emoji">📚</span> Upload Material
                            </a>
                            <a href="AnnouncementServlet" class="action-btn-link">
                                <span class="btn-emoji">📢</span> Publish Announcement
                            </a>
                        </div>
                    </div>

                    <!-- Recent Activity Card -->
                    <div class="dashboard-section card">
                        <h3 class="section-title">🕒 Recent Activity</h3>
                        <div class="activity-feed">
                            <%
                                if (recentActivities == null || recentActivities.isEmpty()) {
                            %>
                                <p class="no-activity">No recent activities logged.</p>
                            <%
                                } else {
                                    for (Map<String, Object> activity : recentActivities) {
                                        String desc = (String) activity.get("description");
                                        Timestamp ts = (Timestamp) activity.get("createdAt");
                            %>
                                <div class="activity-item">
                                    <div class="activity-bullet"></div>
                                    <div class="activity-content">
                                        <p class="activity-desc"><%= desc %></p>
                                        <span class="activity-time"><%= ts %></span>
                                    </div>
                                </div>
                            <%
                                    }
                                }
                            %>
                        </div>
                    </div>

                </div>

            </div>
        </main>
    </div>

    <!-- Shared Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>