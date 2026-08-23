<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String firstName = (String) request.getAttribute("firstName");
    String lastName = (String) request.getAttribute("lastName");
    String department = (String) request.getAttribute("department");
    String semester = (String) request.getAttribute("semester");
    
    String attendanceRate = (String) request.getAttribute("attendanceRate");
    int presentClasses = (Integer) request.getAttribute("presentClasses");
    int totalClasses = (Integer) request.getAttribute("totalClasses");
    
    int totalAssignments = (Integer) request.getAttribute("totalAssignments");
    int submittedAssignments = (Integer) request.getAttribute("submittedAssignments");
    
    int totalTests = (Integer) request.getAttribute("totalTests");
    int completedTests = (Integer) request.getAttribute("completedTests");
    
    String overallPerformance = (String) request.getAttribute("overallPerformance");
    
    List<Map<String, String>> upcomingActivities = (List<Map<String, String>>) request.getAttribute("upcomingActivities");
    List<String> recentActivities = (List<String>) request.getAttribute("recentActivities");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        /* Specific page styles for the dashboard grid layout */
        .dashboard-layout {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 25px;
            margin-top: 25px;
        }
        @media(max-width: 900px) {
            .dashboard-layout {
                grid-template-columns: 1fr;
            }
        }
        .activity-item {
            padding: 12px 15px;
            border-bottom: 1px solid var(--border-color);
            font-size: 14px;
        }
        .activity-item:last-child {
            border-bottom: none;
        }
        .activity-title {
            font-weight: 600;
            color: var(--primary-blue);
        }
        .activity-date {
            font-size: 12px;
            color: var(--secondary-text);
            margin-top: 4px;
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
                
                <!-- Welcome section -->
                <div class="card welcome-card">
                    <h2>Welcome, <%= firstName %> <%= lastName %>!</h2>
                    <p>Track your academic progress, assignments, tests, attendance, and university activities in one unified place.</p>
                </div>

                <!-- Stats Grid -->
                <div class="stats-grid">
                    <!-- Attendance Card -->
                    <div class="card stat-card">
                        <div class="stat-icon icon-students">📅</div>
                        <div>
                            <div class="stat-title">Attendance Rate</div>
                            <div class="stat-number"><%= attendanceRate %>%</div>
                            <small class="text-muted"><%= presentClasses %> of <%= totalClasses %> classes</small>
                        </div>
                    </div>
                    
                    <!-- Assignments Card -->
                    <div class="card stat-card">
                        <div class="stat-icon icon-assignments">📝</div>
                        <div>
                            <div class="stat-title">Assignments</div>
                            <div class="stat-number"><%= submittedAssignments %>/<%= totalAssignments %></div>
                            <small class="text-muted">Submitted</small>
                        </div>
                    </div>

                    <!-- Class Tests Card -->
                    <div class="card stat-card">
                        <div class="stat-icon icon-tests">🧪</div>
                        <div>
                            <div class="stat-title">Class Tests</div>
                            <div class="stat-number"><%= completedTests %>/<%= totalTests %></div>
                            <small class="text-muted">Completed & Graded</small>
                        </div>
                    </div>

                    <!-- Performance Card -->
                    <div class="card stat-card">
                        <div class="stat-icon">📈</div>
                        <div>
                            <div class="stat-title">Overall Performance</div>
                            <div class="stat-number"><%= overallPerformance %>%</div>
                            <small class="text-muted">Weighted Average</small>
                        </div>
                    </div>
                </div>

                <!-- Main Dashboard flex layout -->
                <div class="dashboard-layout">
                    
                    <!-- Left: Upcoming activities -->
                    <div class="card">
                        <h3 class="section-title">📅 Upcoming Activities</h3>
                        <% if (upcomingActivities == null || upcomingActivities.isEmpty()) { %>
                            <p class="text-muted" style="padding: 10px 0;">No upcoming activities or tests scheduled.</p>
                        <% } else { %>
                            <div style="display: flex; flex-direction: column; gap: 15px; margin-top: 15px;">
                                <% for (Map<String, String> act : upcomingActivities) { %>
                                    <div style="background: var(--bg-color); padding: 15px; border-radius: var(--border-radius); border-left: 4px solid var(--primary-blue); display: flex; justify-content: space-between; align-items: center;">
                                        <div>
                                            <span style="font-size: 11px; font-weight: 700; background: var(--light-blue); color: var(--primary-blue); padding: 3px 8px; border-radius: 12px; text-transform: uppercase;">
                                                <%= act.get("type") %>
                                            </span>
                                            <h4 style="margin: 8px 0 4px 0; font-size: 16px;"><%= act.get("title") %></h4>
                                            <span style="font-size: 13px; color: var(--secondary-text);">Subject: <%= act.get("subject") %></span>
                                        </div>
                                        <div style="text-align: right;">
                                            <span style="font-weight: 600; font-size: 14px; color: var(--danger);"><%= act.get("date") %></span>
                                        </div>
                                    </div>
                                <% } %>
                            </div>
                        <% } %>
                    </div>

                    <!-- Right: Recent activities -->
                    <div class="card">
                        <h3 class="section-title">📢 Recent Activity</h3>
                        <% if (recentActivities == null || recentActivities.isEmpty()) { %>
                            <p class="text-muted" style="padding: 10px 0;">No recent activity.</p>
                        <% } else { %>
                            <div style="display: flex; flex-direction: column; margin-top: 15px;">
                                <% for (String act : recentActivities) { %>
                                    <div class="activity-item">
                                        <%= act %>
                                    </div>
                                <% } %>
                            </div>
                        <% } %>
                    </div>

                </div>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>