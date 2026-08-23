<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String overallAssignment = (String) request.getAttribute("overallAssignment");
    String overallTest = (String) request.getAttribute("overallTest");
    String overallAttendance = (String) request.getAttribute("overallAttendance");
    String overallScoreStr = (String) request.getAttribute("overallScore");
    double overallScore = Double.parseDouble(overallScoreStr);
    
    List<Map<String, Object>> performanceList = (List<Map<String, Object>>) request.getAttribute("performanceList");
    
    String performanceStatus = "GOOD";
    String statusColor = "var(--success)";
    if (overallScore < 70.0) {
        performanceStatus = "CRITICAL";
        statusColor = "var(--danger)";
    } else if (overallScore < 85.0) {
        performanceStatus = "WARNING";
        statusColor = "var(--warning)";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Performance - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .performance-summary-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-top: 20px;
            margin-bottom: 25px;
        }
        @media(max-width: 900px) {
            .performance-summary-grid {
                grid-template-columns: 1fr 1fr;
            }
        }
        @media(max-width: 500px) {
            .performance-summary-grid {
                grid-template-columns: 1fr;
            }
        }
        .summary-card {
            background-color: #ffffff;
            border-radius: var(--border-radius);
            padding: 20px;
            text-align: center;
            box-shadow: var(--shadow);
            border: 1px solid var(--border-color);
        }
        .summary-val {
            font-size: 28px;
            font-weight: 700;
            color: var(--dark-blue);
            margin: 10px 0;
        }
        .summary-label {
            font-size: 12px;
            color: var(--secondary-text);
            text-transform: uppercase;
            font-weight: 600;
        }
        .performance-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: #ffffff;
            box-shadow: var(--shadow);
            border-radius: var(--border-radius);
            overflow: hidden;
        }
        .performance-table th, .performance-table td {
            padding: 14px 18px;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }
        .performance-table th {
            background-color: var(--primary-blue);
            color: #ffffff;
            font-weight: 600;
            font-size: 14px;
        }
        .performance-table td {
            font-size: 14px;
        }
        .mini-progress-bg {
            background-color: var(--border-color);
            width: 100px;
            height: 8px;
            border-radius: 4px;
            overflow: hidden;
            display: inline-block;
            vertical-align: middle;
            margin-right: 8px;
        }
        .mini-progress-fill {
            height: 100%;
            border-radius: 4px;
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
                
                <h2 class="section-title">📈 My Academic Performance</h2>

                <!-- Summary Cards Grid -->
                <div class="performance-summary-grid">
                    <div class="summary-card">
                        <div class="summary-label">Assignment Avg</div>
                        <div class="summary-val"><%= overallAssignment %>%</div>
                        <div style="font-size: 11px; color: var(--secondary-text);">Graded tasks</div>
                    </div>
                    <div class="summary-card">
                        <div class="summary-label">Class Test Avg</div>
                        <div class="summary-val"><%= overallTest %>%</div>
                        <div style="font-size: 11px; color: var(--secondary-text);">Written examinations</div>
                    </div>
                    <div class="summary-card">
                        <div class="summary-label">Attendance Rate</div>
                        <div class="summary-val"><%= overallAttendance %>%</div>
                        <div style="font-size: 11px; color: var(--secondary-text);">Lecture presence</div>
                    </div>
                    <div class="summary-card" style="border-top: 4px solid <%= statusColor %>;">
                        <div class="summary-label">Consolidated Score</div>
                        <div class="summary-val" style="color: <%= statusColor %>;"><%= overallScoreStr %>%</div>
                        <span class="status-badge" style="background: <%= statusColor %>15; color: <%= statusColor %>; font-size: 11px;"><%= performanceStatus %></span>
                    </div>
                </div>

                <!-- Subject Breakdown Table -->
                <div class="card">
                    <h3 class="section-title" style="font-size: 18px; margin-bottom: 10px;">📚 Subject Performance Analysis</h3>
                    <p style="font-size: 13px; color: var(--secondary-text); margin-bottom: 15px;">Detailed breakdown of graded course tasks and statistics across semester modules.</p>

                    <% if (performanceList == null || performanceList.isEmpty()) { %>
                        <p class="text-muted">No subject performance metrics available.</p>
                    <% } else { %>
                        <table class="performance-table">
                            <thead>
                                <tr>
                                    <th>Subject Course</th>
                                    <th>Assignment Avg</th>
                                    <th>Class Test Avg</th>
                                    <th>Attendance</th>
                                    <th>Overall Score</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Map<String, Object> p : performanceList) { 
                                    String scoreStr = (String) p.get("overallScore");
                                    double score = Double.parseDouble(scoreStr);
                                    
                                    String fillClass = "bar-good";
                                    if (score < 70.0) {
                                        fillClass = "bar-critical";
                                    } else if (score < 85.0) {
                                        fillClass = "bar-warning";
                                    }
                                %>
                                    <tr>
                                        <td><strong><%= p.get("subject") %></strong></td>
                                        <td><%= p.get("assignmentAvg") %><%= "-".equals(p.get("assignmentAvg")) ? "" : "%" %></td>
                                        <td><%= p.get("testAvg") %><%= "-".equals(p.get("testAvg")) ? "" : "%" %></td>
                                        <td><%= p.get("attendanceRate") %><%= "-".equals(p.get("attendanceRate")) ? "" : "%" %></td>
                                        <td>
                                            <div class="mini-progress-bg">
                                                <div class="mini-progress-fill progress-bar <%= fillClass %>" style="width: <%= score %>%;"></div>
                                            </div>
                                            <strong><%= scoreStr %>%</strong>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    <% } %>
                </div>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
