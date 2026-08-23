<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || (!"ADMIN".equalsIgnoreCase(role) && !"TEACHER".equalsIgnoreCase(role))) {
        response.sendRedirect("index.jsp");
        return;
    }

    boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

    // ADMIN DATA
    int totalUsers = 0;
    int totalStudents = 0;
    int totalTeachers = 0;
    int totalAdmins = 0;
    int activeUsers = 0;
    int inactiveUsers = 0;
    int suspendedUsers = 0;
    List<Map<String, Object>> studentDepartments = null;
    List<Map<String, Object>> teacherDepartments = null;
    List<Map<String, Object>> studentSemesters = null;
    List<Map<String, Object>> recentUsers = null;

    if (isAdmin) {
        if (request.getAttribute("totalUsers") != null) totalUsers = (Integer) request.getAttribute("totalUsers");
        if (request.getAttribute("totalStudents") != null) totalStudents = (Integer) request.getAttribute("totalStudents");
        if (request.getAttribute("totalTeachers") != null) totalTeachers = (Integer) request.getAttribute("totalTeachers");
        if (request.getAttribute("totalAdmins") != null) totalAdmins = (Integer) request.getAttribute("totalAdmins");
        if (request.getAttribute("activeUsers") != null) activeUsers = (Integer) request.getAttribute("activeUsers");
        if (request.getAttribute("inactiveUsers") != null) inactiveUsers = (Integer) request.getAttribute("inactiveUsers");
        if (request.getAttribute("suspendedUsers") != null) suspendedUsers = (Integer) request.getAttribute("suspendedUsers");
        studentDepartments = (List<Map<String, Object>>) request.getAttribute("studentDepartments");
        teacherDepartments = (List<Map<String, Object>>) request.getAttribute("teacherDepartments");
        studentSemesters = (List<Map<String, Object>>) request.getAttribute("studentSemesters");
        recentUsers = (List<Map<String, Object>>) request.getAttribute("recentUsers");
    }

    // TEACHER DATA
    String reportType = (String) request.getAttribute("reportType");
    String teacherDept = (String) request.getAttribute("teacherDept");
    List<Map<String, Object>> reportData = (List<Map<String, Object>>) request.getAttribute("reportData");

    if (reportType == null) reportType = "student";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports - BLUE RIDGE UNIVERSITY</title>
    <% if (isAdmin) { %>
        <link rel="stylesheet" href="css/admin.css">
    <% } else { %>
        <link rel="stylesheet" href="css/teacher.css">
        <link rel="stylesheet" href="css/teacher-reports.css">
    <% } %>

    <% if (isAdmin) { %>
    <style>
        .reports-container {
            width: 100%;
            max-width: 1200px;
            margin: auto;
        }
        .page-header {
            margin-bottom: 25px;
        }
        .page-header h2 {
            color: #0d47a1;
            margin-bottom: 8px;
        }
        .page-header p {
            color: #666;
        }
        .report-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 25px;
        }
        .report-button {
            background: #1565c0;
            color: white;
            text-decoration: none;
            padding: 10px 16px;
            border-radius: 6px;
        }
        .report-button:hover {
            background: #0d47a1;
        }
        .print-button {
            background: #455a64;
            border: none;
            color: white;
            padding: 10px 16px;
            border-radius: 6px;
            cursor: pointer;
        }
        .statistics-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 25px;
        }
        .report-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
            text-align: center;
        }
        .report-card h3 {
            color: #555;
            font-size: 15px;
            margin-bottom: 15px;
        }
        .report-number {
            color: #1565c0;
            font-size: 34px;
            font-weight: bold;
        }
        .report-description {
            margin-top: 8px;
            color: #888;
            font-size: 12px;
        }
        .status-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 25px;
        }
        .status-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
        }
        .status-card h3 {
            color: #0d47a1;
            margin-bottom: 20px;
        }
        .progress-container {
            width: 100%;
            height: 22px;
            background: #eeeeee;
            border-radius: 20px;
            overflow: hidden;
            margin: 12px 0;
        }
        .progress-active {
            height: 100%;
            background: #2e7d32;
        }
        .progress-inactive {
            height: 100%;
            background: #757575;
        }
        .status-info {
            display: flex;
            justify-content: space-between;
            font-size: 14px;
        }
        .report-section {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
            margin-bottom: 25px;
        }
        .report-section h3 {
            color: #0d47a1;
            margin-bottom: 20px;
        }
        .role-row {
            display: flex;
            align-items: center;
            margin-bottom: 18px;
            gap: 15px;
        }
        .role-name {
            width: 100px;
            font-weight: bold;
        }
        .role-bar-container {
            flex: 1;
            height: 18px;
            background: #eeeeee;
            border-radius: 20px;
            overflow: hidden;
        }
        .role-bar {
            height: 100%;
            background: #1565c0;
        }
        .role-count {
            width: 60px;
            text-align: right;
            font-weight: bold;
        }
        .quick-links {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
        }
        .quick-link {
            background: #f5f7fa;
            border: 1px solid #ddd;
            padding: 18px;
            border-radius: 8px;
            text-decoration: none;
            color: #333;
            transition: 0.2s;
        }
        .quick-link:hover {
            background: #e3f2fd;
            border-color: #1565c0;
        }
        .quick-link h4 {
            color: #1565c0;
            margin-bottom: 7px;
        }
        .quick-link p {
            font-size: 13px;
            color: #777;
            margin: 0;
        }
        @media(max-width: 900px) {
            .statistics-grid { grid-template-columns: repeat(2, 1fr); }
            .quick-links { grid-template-columns: repeat(2, 1fr); }
        }
        @media(max-width: 600px) {
            .statistics-grid { grid-template-columns: 1fr; }
            .status-grid { grid-template-columns: 1fr; }
            .quick-links { grid-template-columns: 1fr; }
            .role-name { width: 70px; }
        }
        @media print {
            .top-bar, .footer, .report-actions { display: none; }
            body { background: white; }
            .main-content { padding: 0; }
            .report-card, .status-card, .report-section { box-shadow: none; border: 1px solid #ddd; }
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
        <!-- ADMIN WRAPPER -->
        <div class="main-content">
            <div class="reports-container">
                <div class="page-header">
                    <h2>University Reports</h2>
                    <p>Overview of users and system statistics.</p>
                </div>

                <div class="report-actions">
                    <a href="ExportServlet?type=users" class="report-button">Export Users</a>
                    <a href="ExportServlet?type=students" class="report-button">Export Students</a>
                    <a href="ExportServlet?type=teachers" class="report-button">Export Teachers</a>
                    <button type="button" class="print-button" onclick="window.print()">Print Report</button>
                </div>

                <!-- Statistics Grid -->
                <div class="statistics-grid">
                    <div class="report-card">
                        <h3>Total Users</h3>
                        <div class="report-number"><%= totalUsers %></div>
                        <div class="report-description">All registered accounts</div>
                    </div>
                    <div class="report-card">
                        <h3>Students</h3>
                        <div class="report-number"><%= totalStudents %></div>
                        <div class="report-description">Student accounts</div>
                    </div>
                    <div class="report-card">
                        <h3>Teachers</h3>
                        <div class="report-number"><%= totalTeachers %></div>
                        <div class="report-description">Teacher accounts</div>
                    </div>
                    <div class="report-card">
                        <h3>Administrators</h3>
                        <div class="report-number"><%= totalAdmins %></div>
                        <div class="report-description">Admin accounts</div>
                    </div>
                </div>

                <!-- Active / Inactive Progress -->
                <div class="status-grid">
                    <div class="status-card">
                        <h3>Active Accounts</h3>
                        <div class="status-info">
                            <span>Active Users</span>
                            <strong><%= activeUsers %></strong>
                        </div>
                        <%
                            int activePercent = totalUsers > 0 ? (activeUsers * 100) / totalUsers : 0;
                        %>
                        <div class="progress-container">
                            <div class="progress-active" style="width:<%= activePercent %>%;"></div>
                        </div>
                        <small><%= activePercent %>% of all accounts are active.</small>
                    </div>

                    <div class="status-card">
                        <h3>Inactive Accounts</h3>
                        <div class="status-info">
                            <span>Inactive Users</span>
                            <strong><%= inactiveUsers %></strong>
                        </div>
                        <%
                            int inactivePercent = totalUsers > 0 ? (inactiveUsers * 100) / totalUsers : 0;
                        %>
                        <div class="progress-container">
                            <div class="progress-inactive" style="width:<%= inactivePercent %>%;"></div>
                        </div>
                        <small><%= inactivePercent %>% of all accounts are inactive.</small>
                    </div>
                </div>

                <!-- Distribution -->
                <div class="report-section">
                    <h3>User Distribution</h3>
                    <%
                        int maxUsers = Math.max(totalStudents, Math.max(totalTeachers, totalAdmins));
                        int studentPercent = maxUsers > 0 ? (totalStudents * 100) / maxUsers : 0;
                        int teacherPercent = maxUsers > 0 ? (totalTeachers * 100) / maxUsers : 0;
                        int adminPercent = maxUsers > 0 ? (totalAdmins * 100) / maxUsers : 0;
                    %>
                    <div class="role-row">
                        <div class="role-name">Students</div>
                        <div class="role-bar-container"><div class="role-bar" style="width:<%= studentPercent %>%;"></div></div>
                        <div class="role-count"><%= totalStudents %></div>
                    </div>
                    <div class="role-row">
                        <div class="role-name">Teachers</div>
                        <div class="role-bar-container"><div class="role-bar" style="width:<%= teacherPercent %>%;"></div></div>
                        <div class="role-count"><%= totalTeachers %></div>
                    </div>
                    <div class="role-row">
                        <div class="role-name">Admins</div>
                        <div class="role-bar-container"><div class="role-bar" style="width:<%= adminPercent %>%;"></div></div>
                        <div class="role-count"><%= totalAdmins %></div>
                    </div>
                </div>

                <!-- Management links -->
                <div class="report-section">
                    <h3>Management Reports</h3>
                    <div class="quick-links">
                        <a href="AdminServlet?action=getUsers&role=STUDENT" class="quick-link">
                            <h4>Student Report</h4>
                            <p>View all registered students.</p>
                        </a>
                        <a href="AdminServlet?action=getUsers&role=TEACHER" class="quick-link">
                            <h4>Teacher Report</h4>
                            <p>View all registered teachers.</p>
                        </a>
                        <a href="AdminServlet?action=getUsers&role=ADMIN" class="quick-link">
                            <h4>Admin Report</h4>
                            <p>View administrator accounts.</p>
                        </a>
                        <a href="ActivityLogServlet" class="quick-link">
                            <h4>Activity Report</h4>
                            <p>View system activity logs.</p>
                        </a>
                        <a href="AnnouncementServlet" class="quick-link">
                            <h4>Announcement Report</h4>
                            <p>Manage university announcements.</p>
                        </a>
                        <a href="AcademicServlet" class="quick-link">
                            <h4>Academic Report</h4>
                            <p>Manage academic years.</p>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <footer class="footer">
            <p>© 2026 BLUE RIDGE UNIVERSITY. All Rights Reserved. Made by Penugonda Devashish.</p>
        </footer>

    <% } else { %>
        <!-- TEACHER WRAPPER -->
        <div class="app-layout">
            <jsp:include page="teacherSidebar.jsp" />
            <main class="main-content-wrapper">
                <div class="teacher-container">
                    
                    <div class="page-header">
                        <h2>📊 Department Reports</h2>
                        <p>Generate academic summaries, grades trackers, and attendance reviews for <strong><%= teacherDept %></strong>.</p>
                    </div>

                    <!-- Tabs & Actions Panel -->
                    <div class="reports-nav-card card">
                        <div class="reports-tabs">
                            <a href="ReportServlet?type=student" class="tab-link <%= "student".equalsIgnoreCase(reportType) ? "active" : "" %>">👥 Student Directory</a>
                            <a href="ReportServlet?type=attendance" class="tab-link <%= "attendance".equalsIgnoreCase(reportType) ? "active" : "" %>">📋 Attendance Sheet</a>
                            <a href="ReportServlet?type=assignment" class="tab-link <%= "assignment".equalsIgnoreCase(reportType) ? "active" : "" %>">📝 Assignment Submissions</a>
                            <a href="ReportServlet?type=test" class="tab-link <%= "test".equalsIgnoreCase(reportType) ? "active" : "" %>">🧪 Class Tests stats</a>
                            <a href="ReportServlet?type=performance" class="tab-link <%= "performance".equalsIgnoreCase(reportType) ? "active" : "" %>">📈 Performance Ratings</a>
                        </div>
                        <button type="button" class="btn btn-primary btn-print" onclick="window.print()">🖨️ Print Report</button>
                    </div>

                    <!-- Print Header (Hidden on screen, visible on print) -->
                    <div class="print-only-header">
                        <h2>BLUE RIDGE UNIVERSITY</h2>
                        <h3>Academic Department Report - <%= teacherDept %></h3>
                        <p>Generated Date: <%= java.time.LocalDate.now().toString() %></p>
                        <hr>
                    </div>

                    <!-- Report Table Card -->
                    <div class="report-table-card card">
                        <h3 class="section-title">
                            <% 
                                if ("student".equalsIgnoreCase(reportType)) out.print("Student Directory Report");
                                else if ("attendance".equalsIgnoreCase(reportType)) out.print("Attendance Performance Report");
                                else if ("assignment".equalsIgnoreCase(reportType)) out.print("Assignment Grades tracker");
                                else if ("test".equalsIgnoreCase(reportType)) out.print("Class Test Statistics");
                                else if ("performance".equalsIgnoreCase(reportType)) out.print("Consolidated Performance Evaluation");
                            %>
                        </h3>

                        <div class="table-wrapper">
                            <table class="report-data-table">
                                
                                <!-- Student Table -->
                                <% if ("student".equalsIgnoreCase(reportType)) { %>
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Student Name</th>
                                            <th>Username</th>
                                            <th>Email</th>
                                            <th>Semester</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (reportData == null || reportData.isEmpty()) { %>
                                            <tr><td colspan="6" class="no-data">No students registered in this department.</td></tr>
                                        <% } else { 
                                            for (Map<String, Object> r : reportData) { %>
                                                <tr>
                                                    <td><%= r.get("studentId") %></td>
                                                    <td><strong><%= r.get("name") %></strong></td>
                                                    <td><%= r.get("username") %></td>
                                                    <td><%= r.get("email") %></td>
                                                    <td>Semester <%= r.get("semester") %></td>
                                                    <td><span class="status-badge status-<%= String.valueOf(r.get("status")).toLowerCase() %>"><%= r.get("status") %></span></td>
                                                </tr>
                                        <% } } %>
                                    </tbody>

                                <!-- Attendance Table -->
                                <% } else if ("attendance".equalsIgnoreCase(reportType)) { %>
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Student Name</th>
                                            <th>Total Classes</th>
                                            <th>Classes Present</th>
                                            <th>Classes Absent</th>
                                            <th>Attendance Percentage</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (reportData == null || reportData.isEmpty()) { %>
                                            <tr><td colspan="6" class="no-data">No attendance records.</td></tr>
                                        <% } else { 
                                            for (Map<String, Object> r : reportData) { %>
                                                <tr>
                                                    <td><%= r.get("studentId") %></td>
                                                    <td><strong><%= r.get("name") %></strong></td>
                                                    <td><%= r.get("total") %></td>
                                                    <td><%= r.get("present") %></td>
                                                    <td><%= r.get("absent") %></td>
                                                    <td>
                                                        <%
                                                            double pct = Double.parseDouble(String.valueOf(r.get("percentage")));
                                                            String pctClass = "text-success";
                                                            if (pct < 75.0) pctClass = "text-danger font-bold";
                                                            else if (pct < 90.0) pctClass = "text-warning";
                                                        %>
                                                        <span class="<%= pctClass %>"><%= r.get("percentage") %>%</span>
                                                    </td>
                                                </tr>
                                        <% } } %>
                                    </tbody>

                                <!-- Assignment Table -->
                                <% } else if ("assignment".equalsIgnoreCase(reportType)) { %>
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Assignment Title</th>
                                            <th>Subject Course</th>
                                            <th>Due Date</th>
                                            <th>Submissions Graded</th>
                                            <th>Pending Work</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (reportData == null || reportData.isEmpty()) { %>
                                            <tr><td colspan="6" class="no-data">No assignments created.</td></tr>
                                        <% } else { 
                                            for (Map<String, Object> r : reportData) { %>
                                                <tr>
                                                    <td><%= r.get("assignmentId") %></td>
                                                    <td><strong><%= r.get("title") %></strong></td>
                                                    <td><%= r.get("subject") %></td>
                                                    <td><%= r.get("dueDate") %></td>
                                                    <td><%= r.get("submitted") %></td>
                                                    <td><%= r.get("pending") %></td>
                                                </tr>
                                        <% } } %>
                                    </tbody>

                                <!-- Test Table -->
                                <% } else if ("test".equalsIgnoreCase(reportType)) { %>
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Test Title</th>
                                            <th>Course</th>
                                            <th>Test Date</th>
                                            <th>Max Marks</th>
                                            <th>Average Score</th>
                                            <th>Highest Mark</th>
                                            <th>Lowest Mark</th>
                                            <th>Pass Rate</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (reportData == null || reportData.isEmpty()) { %>
                                            <tr><td colspan="9" class="no-data">No class tests created.</td></tr>
                                        <% } else { 
                                            for (Map<String, Object> r : reportData) { %>
                                                <tr>
                                                    <td><%= r.get("testId") %></td>
                                                    <td><strong><%= r.get("title") %></strong></td>
                                                    <td><%= r.get("subject") %></td>
                                                    <td><%= r.get("testDate") %></td>
                                                    <td><%= r.get("totalMarks") %></td>
                                                    <td><%= r.get("average") %></td>
                                                    <td class="text-success"><%= r.get("highest") %></td>
                                                    <td class="text-danger"><%= r.get("lowest") %></td>
                                                    <td><strong><%= r.get("passPercentage") %>%</strong></td>
                                                </tr>
                                        <% } } %>
                                    </tbody>

                                <!-- Performance Table -->
                                <% } else if ("performance".equalsIgnoreCase(reportType)) { %>
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Student Name</th>
                                            <th>Username</th>
                                            <th>Attendance</th>
                                            <th>Assignment Avg</th>
                                            <th>Test Avg</th>
                                            <th>Overall score</th>
                                            <th>Rating Evaluation</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (reportData == null || reportData.isEmpty()) { %>
                                            <tr><td colspan="8" class="no-data">No students registered.</td></tr>
                                        <% } else { 
                                            for (Map<String, Object> r : reportData) { 
                                                String rat = String.valueOf(r.get("rating"));
                                                String ratClass = "status-good";
                                                if ("CRITICAL".equalsIgnoreCase(rat)) ratClass = "status-critical";
                                                else if ("WARNING".equalsIgnoreCase(rat)) ratClass = "status-warning";
                                            %>
                                                <tr>
                                                    <td><%= r.get("studentId") %></td>
                                                    <td><strong><%= r.get("name") %></strong></td>
                                                    <td><%= r.get("username") %></td>
                                                    <td><%= r.get("attendance") %></td>
                                                    <td><%= r.get("assignment") %>%</td>
                                                    <td><%= r.get("test") %>%</td>
                                                    <td><%= r.get("overall") %>%</td>
                                                    <td><span class="performance-badge <%= ratClass %>"><%= rat %></span></td>
                                                </tr>
                                        <% } } %>
                                    </tbody>
                                <% } %>

                            </table>
                        </div>
                    </div>

                </div>
            </main>
        </div>
        <jsp:include page="teacherFooter.jsp" />
    <% } %>

</body>
</html>