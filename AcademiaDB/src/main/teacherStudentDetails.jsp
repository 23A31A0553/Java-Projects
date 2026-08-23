<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.university.servlet.Student" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role))) {
        response.sendRedirect("index.jsp");
        return;
    }

    Student student = (Student) request.getAttribute("student");
    String attendanceRate = (String) request.getAttribute("attendanceRate");
    int totalAttendance = (Integer) request.getAttribute("totalAttendance");
    int presentAttendance = (Integer) request.getAttribute("presentAttendance");
    
    String assignmentCompletion = (String) request.getAttribute("assignmentCompletion");
    int totalAssignments = (Integer) request.getAttribute("totalAssignments");
    int submittedAssignments = (Integer) request.getAttribute("submittedAssignments");
    
    String testAverage = (String) request.getAttribute("testAverage");
    String performanceStatus = (String) request.getAttribute("performanceStatus");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Details - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/teacher-students.css">
</head>
<body>

    <!-- Shared Header -->
    <jsp:include page="teacherHeader.jsp" />

    <div class="app-layout">
        <!-- Shared Sidebar -->
        <% if ("TEACHER".equalsIgnoreCase(role)) { %>
            <jsp:include page="teacherSidebar.jsp" />
        <% } %>

        <main class="main-content-wrapper">
            <div class="teacher-container">

                <div class="page-header-actions">
                    <% if ("ADMIN".equalsIgnoreCase(role)) { %>
                        <a href="UserManagementServlet" class="btn-back">⬅️ Back to User Management</a>
                    <% } else { %>
                        <a href="TeacherStudentServlet" class="btn-back">⬅️ Back to Students</a>
                    <% } %>
                </div>

                <div class="student-profile-flex">
                    
                    <!-- Left Column: Student Details Card -->
                    <div class="student-info-card card flex-1">
                        <div class="student-avatar-large">
                            <%= student.getFirstName().substring(0, 1).toUpperCase() %>
                        </div>
                        <h3 class="student-fullname"><%= student.getFirstName() %> <%= student.getLastName() %></h3>
                        <p class="student-id-label">Student ID: <%= student.getStudentId() %></p>
                        
                        <div class="details-list">
                            <div class="detail-row">
                                <span class="row-label">Username</span>
                                <span class="row-val"><%= student.getUsername() %></span>
                            </div>
                            <div class="detail-row">
                                <span class="row-label">Email Address</span>
                                <span class="row-val"><%= student.getEmail() %></span>
                            </div>
                            <div class="detail-row">
                                <span class="row-label">Mobile Number</span>
                                <span class="row-val"><%= student.getMobile() != null ? student.getMobile() : "-" %></span>
                            </div>
                            <div class="detail-row">
                                <span class="row-label">Department</span>
                                <span class="row-val"><%= student.getDepartment() %></span>
                            </div>
                            <div class="detail-row">
                                <span class="row-label">Current Semester</span>
                                <span class="row-val">Semester <%= student.getSemester() %></span>
                            </div>
                            <div class="detail-row">
                                <span class="row-label">Status</span>
                                <span class="row-val status-badge status-<%= student.getStatus().toLowerCase() %>"><%= student.getStatus() %></span>
                            </div>
                        </div>
                    </div>

                    <!-- Right Column: Academic Summary Card -->
                    <div class="student-academic-card card flex-2">
                        <h3 class="section-title">🎓 Academic Performance Summary</h3>
                        
                        <div class="academic-metrics-grid">
                            
                            <!-- Attendance Metric -->
                            <%
                                double attVal = Double.parseDouble(attendanceRate);
                                String attClass = "metric-good";
                                if (attVal < 75.0) {
                                    attClass = "metric-critical";
                                } else if (attVal < 90.0) {
                                    attClass = "metric-warning";
                                }
                            %>
                            <div class="metric-card <%= attClass %>">
                                <span class="metric-label">📅 Attendance Rate</span>
                                <span class="metric-value"><%= attendanceRate %>%</span>
                                <span class="metric-desc"><%= presentAttendance %> / <%= totalAttendance %> classes present</span>
                            </div>

                            <!-- Assignment Completion Metric -->
                            <div class="metric-card">
                                <span class="metric-label">📝 Assignment Completion</span>
                                <span class="metric-value"><%= assignmentCompletion %>%</span>
                                <span class="metric-desc"><%= submittedAssignments %> / <%= totalAssignments %> submitted</span>
                            </div>

                            <!-- Test Average Metric -->
                            <div class="metric-card">
                                <span class="metric-label">🧪 Class Test Average</span>
                                <span class="metric-value"><%= testAverage %>%</span>
                                <span class="metric-desc">Average percentage across tests</span>
                            </div>

                            <!-- Overall Performance Metric -->
                            <%
                                String ratingClass = "rating-good";
                                if ("CRITICAL".equalsIgnoreCase(performanceStatus)) {
                                    ratingClass = "rating-critical";
                                } else if ("WARNING".equalsIgnoreCase(performanceStatus)) {
                                    ratingClass = "rating-warning";
                                }
                            %>
                            <div class="metric-card rating-card <%= ratingClass %>">
                                <span class="metric-label">⭐ Overall Rating</span>
                                <span class="metric-value"><%= performanceStatus %></span>
                                <span class="metric-desc">Consolidated student evaluation</span>
                            </div>

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
