<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String overallRateStr = (String) request.getAttribute("overallRate");
    double overallRate = Double.parseDouble(overallRateStr);
    int totalClasses = (Integer) request.getAttribute("totalClasses");
    int presentClasses = (Integer) request.getAttribute("presentClasses");
    
    List<Map<String, Object>> subjectBreakdown = (List<Map<String, Object>>) request.getAttribute("subjectBreakdown");
    List<Map<String, Object>> attendanceHistory = (List<Map<String, Object>>) request.getAttribute("attendanceHistory");
    List<String> subjectsList = (List<String>) request.getAttribute("subjectsList");
    String selectedSubject = (String) request.getAttribute("selectedSubject");
    if (selectedSubject == null) selectedSubject = "";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Attendance - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .attendance-dashboard {
            display: grid;
            grid-template-columns: 1fr 2fr;
            gap: 25px;
            margin-top: 20px;
        }
        @media (max-width: 800px) {
            .attendance-dashboard {
                grid-template-columns: 1fr;
            }
        }
        .progress-bar-container {
            width: 100%;
            height: 16px;
            background-color: var(--border-color);
            border-radius: 8px;
            overflow: hidden;
            margin: 15px 0;
        }
        .progress-bar {
            height: 100%;
            border-radius: 8px;
            transition: width 0.3s;
        }
        .bar-good { background-color: var(--success); }
        .bar-warning { background-color: var(--warning); }
        .bar-critical { background-color: var(--danger); }

        .attendance-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
            background: #ffffff;
            box-shadow: var(--shadow);
            border-radius: var(--border-radius);
            overflow: hidden;
        }
        .attendance-table th, .attendance-table td {
            padding: 12px 16px;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }
        .attendance-table th {
            background-color: var(--primary-blue);
            color: #ffffff;
            font-weight: 600;
            font-size: 13px;
        }
        .attendance-table td {
            font-size: 13px;
        }
        .status-present {
            color: var(--success);
            font-weight: 600;
        }
        .status-absent {
            color: var(--danger);
            font-weight: 600;
        }
        .warning-box {
            background-color: #FEF3C7;
            border-left: 4px solid #D97706;
            color: #92400E;
            padding: 15px;
            border-radius: var(--border-radius);
            margin-bottom: 20px;
            font-size: 14px;
            font-weight: 500;
        }
        .form-select {
            padding: 8px 14px;
            border: 1px solid var(--border-color);
            border-radius: var(--border-radius);
            font-size: 13px;
            background-color: #ffffff;
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
                
                <h2 class="section-title">📋 My Attendance Record</h2>

                <!-- 1. Check & Render Attendance Warnings (< 75%) -->
                <% if (subjectBreakdown != null) { 
                    for (Map<String, Object> sb : subjectBreakdown) {
                        double pct = (Double) sb.get("percentage");
                        if (pct < 75.0) {
                %>
                            <div class="warning-box">
                                ⚠ Attendance Warning: Your attendance in <strong><%= sb.get("subject") %></strong> is <strong><%= String.format("%.1f", pct) %>%</strong>. Please attend classes to maintain the required minimum threshold of 75%.
                            </div>
                <%      }
                    }
                } %>

                <div class="attendance-dashboard">
                    
                    <!-- Left: Overall Rate -->
                    <div>
                        <div class="card" style="text-align: center; margin-bottom: 20px;">
                            <h3 class="section-title" style="font-size: 16px; margin-bottom: 5px;">Overall Attendance</h3>
                            <div style="font-size: 40px; font-weight: 700; color: var(--dark-blue); margin: 15px 0 5px 0;">
                                <%= String.format("%.1f", overallRate) %>%
                            </div>
                            <p style="color: var(--secondary-text); font-size: 13px;">
                                Present in <%= presentClasses %> of <%= totalClasses %> lecture periods
                            </p>
                            
                            <%
                                String barClass = "bar-good";
                                if (overallRate < 75.0) {
                                    barClass = "bar-critical";
                                } else if (overallRate < 90.0) {
                                    barClass = "bar-warning";
                                }
                            %>
                            <div class="progress-bar-container">
                                <div class="progress-bar <%= barClass %>" style="width: <%= overallRate %>%;"></div>
                            </div>
                            <span style="font-size: 12px; font-weight: 600; color: <%= overallRate < 75.0 ? "var(--danger)" : (overallRate < 90.0 ? "var(--warning)" : "var(--success)") %>;">
                                <%= overallRate < 75.0 ? "Critical Attendance Warning" : (overallRate < 90.0 ? "Good Attendance" : "Excellent Attendance") %>
                            </span>
                        </div>

                        <!-- Subject Wise list -->
                        <div class="card">
                            <h3 class="section-title" style="font-size: 16px; margin-bottom: 15px;">Subject Breakdowns</h3>
                            <% if (subjectBreakdown == null || subjectBreakdown.isEmpty()) { %>
                                <p class="text-muted">No attendance logs available.</p>
                            <% } else { %>
                                <div style="display: flex; flex-direction: column; gap: 15px;">
                                    <% for (Map<String, Object> sb : subjectBreakdown) { 
                                        double pct = (Double) sb.get("percentage");
                                        String sbBarClass = "bar-good";
                                        if (pct < 75.0) {
                                            sbBarClass = "bar-critical";
                                        } else if (pct < 90.0) {
                                            sbBarClass = "bar-warning";
                                        }
                                    %>
                                        <div>
                                            <div style="display: flex; justify-content: space-between; font-size: 13px;">
                                                <span style="font-weight: 600;"><%= sb.get("subject") %></span>
                                                <span style="font-weight: 700; color: var(--primary-blue);"><%= String.format("%.1f", pct) %>%</span>
                                            </div>
                                            <div class="progress-bar-container" style="height: 8px; margin: 6px 0;">
                                                <div class="progress-bar <%= sbBarClass %>" style="width: <%= pct %>%;"></div>
                                            </div>
                                            <div style="font-size: 11px; color: var(--secondary-text);">
                                                Attended <%= sb.get("present") %> / <%= sb.get("total") %> classes
                                            </div>
                                        </div>
                                    <% } %>
                                </div>
                            <% } %>
                        </div>
                    </div>

                    <!-- Right: Historical Log -->
                    <div class="card">
                        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 15px;">
                            <h3 class="section-title" style="font-size: 16px; margin-bottom: 0;">📜 History Logs</h3>
                            
                            <form action="StudentAttendanceServlet" method="GET" id="filterForm">
                                <select name="subject" class="form-select" onchange="document.getElementById('filterForm').submit();">
                                    <option value="">All Subjects</option>
                                    <% if (subjectsList != null) {
                                        for (String s : subjectsList) {
                                    %>
                                            <option value="<%= s %>" <%= s.equals(selectedSubject) ? "selected" : "" %>><%= s %></option>
                                    <%  }
                                    } %>
                                </select>
                            </form>
                        </div>

                        <% if (attendanceHistory == null || attendanceHistory.isEmpty()) { %>
                            <p class="text-muted" style="margin-top: 20px;">No attendance log records found.</p>
                        <% } else { %>
                            <table class="attendance-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Subject</th>
                                        <th>Instructor</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Map<String, Object> record : attendanceHistory) { %>
                                        <tr>
                                            <td><%= record.get("date") %></td>
                                            <td><strong><%= record.get("subject") %></strong></td>
                                            <td><%= record.get("teacherName") %></td>
                                            <td>
                                                <% if ("PRESENT".equals(record.get("status"))) { %>
                                                    <span class="status-present">Present</span>
                                                <% } else { %>
                                                    <span class="status-absent">Absent</span>
                                                <% } %>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
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
