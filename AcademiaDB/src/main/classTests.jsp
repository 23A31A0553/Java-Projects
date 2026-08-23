<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Map<String, Object>> upcomingTests = (List<Map<String, Object>>) request.getAttribute("upcomingTests");
    List<Map<String, Object>> previousTests = (List<Map<String, Object>>) request.getAttribute("previousTests");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Class Tests - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .test-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 20px;
            margin-top: 15px;
            margin-bottom: 30px;
        }
        .test-card {
            border-left: 5px solid var(--primary-blue);
        }
        .test-meta {
            font-size: 13px;
            color: var(--secondary-text);
            margin-bottom: 6px;
        }
        .test-meta strong {
            color: var(--text-color);
        }
        .test-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
            background: #ffffff;
            box-shadow: var(--shadow);
            border-radius: var(--border-radius);
            overflow: hidden;
        }
        .test-table th, .test-table td {
            padding: 14px 18px;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }
        .test-table th {
            background-color: var(--primary-blue);
            color: #ffffff;
            font-weight: 600;
            font-size: 14px;
        }
        .test-table td {
            font-size: 14px;
        }
        .badge-pass {
            background-color: #D4EDDA;
            color: #155724;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
        }
        .badge-fail {
            background-color: #F8D7DA;
            color: #721C24;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
        }
        .badge-pending {
            background-color: #FFF3CD;
            color: #856404;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
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
                
                <h2 class="section-title">🧪 Class Tests & Grades</h2>

                <!-- Upcoming Tests Section -->
                <h3 class="section-title" style="font-size: 18px; margin-top: 10px; margin-bottom: 5px; color: var(--dark-blue);">📅 Upcoming Class Tests</h3>
                <% if (upcomingTests == null || upcomingTests.isEmpty()) { %>
                    <div class="card" style="margin-bottom: 30px;">
                        <p class="text-muted">No upcoming class tests scheduled.</p>
                    </div>
                <% } else { %>
                    <div class="test-grid">
                        <% for (Map<String, Object> test : upcomingTests) { %>
                            <div class="card test-card">
                                <span style="font-size: 11px; font-weight: 700; background: var(--light-blue); color: var(--primary-blue); padding: 3px 8px; border-radius: 12px; text-transform: uppercase; display: inline-block; margin-bottom: 8px;">
                                    <%= test.get("subject") %>
                                </span>
                                <h4 style="color: var(--dark-blue); font-size: 16px; margin-bottom: 10px;"><%= test.get("title") %></h4>
                                
                                <div class="test-meta">Unit: <strong><%= test.get("unit") %></strong></div>
                                <div class="test-meta">Instructor: <strong><%= test.get("teacherName") %></strong></div>
                                <div class="test-meta">Total Marks: <strong><%= test.get("totalMarks") %> Marks</strong></div>
                                <div class="test-meta" style="color: var(--danger); font-weight: 600; margin-top: 10px;">
                                    ⏰ Scheduled: <%= test.get("date") %>
                                </div>
                                <% if (test.get("description") != null && !((String)test.get("description")).trim().isEmpty()) { %>
                                    <p style="font-size: 13px; color: var(--secondary-text); margin-top: 10px; border-top: 1px dashed var(--border-color); padding-top: 8px;">
                                        <%= test.get("description") %>
                                    </p>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                <% } %>

                <!-- Previous Tests Section -->
                <h3 class="section-title" style="font-size: 18px; margin-bottom: 5px; color: var(--dark-blue);">🎓 Previous Tests & Marks</h3>
                <% if (previousTests == null || previousTests.isEmpty()) { %>
                    <div class="card">
                        <p class="text-muted">No historical class test records available.</p>
                    </div>
                <% } else { %>
                    <table class="test-table">
                        <thead>
                            <tr>
                                <th>Subject</th>
                                <th>Test Title</th>
                                <th>Date</th>
                                <th>Marks Obtained</th>
                                <th>Percentage</th>
                                <th>Result / Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Map<String, Object> test : previousTests) { 
                                Object marksObj = test.get("marksObtained");
                                int totalMarks = (Integer) test.get("totalMarks");
                            %>
                                <tr>
                                    <td><strong><%= test.get("subject") %></strong></td>
                                    <td><%= test.get("title") %> (Unit: <%= test.get("unit") %>)</td>
                                    <td><%= test.get("date") %></td>
                                    <td>
                                        <% if (marksObj != null) { %>
                                            <strong><%= marksObj %></strong> / <%= totalMarks %>
                                        <% } else { %>
                                            -
                                        <% } %>
                                    </td>
                                    <td>
                                        <% if (marksObj != null) { 
                                            double percentage = (Double.parseDouble(String.valueOf(marksObj)) / totalMarks) * 100;
                                        %>
                                            <strong><%= String.format("%.1f", percentage) %>%</strong>
                                        <% } else { %>
                                            -
                                        <% } %>
                                    </td>
                                    <td>
                                        <% if (marksObj != null) { 
                                            double percentage = (Double.parseDouble(String.valueOf(marksObj)) / totalMarks) * 100;
                                            if (percentage >= 40.0) {
                                        %>
                                                <span class="badge-pass">Pass</span>
                                            <% } else { %>
                                                <span class="badge-fail">Fail</span>
                                            <% } %>
                                        <% } else { %>
                                            <span class="badge-pending">Not Graded</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
