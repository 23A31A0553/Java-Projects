<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Map<String, Object>> assignments = (List<Map<String, Object>>) request.getAttribute("assignments");
    String filterStatus = (String) request.getAttribute("filterStatus");
    if (filterStatus == null) filterStatus = "all";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Assignments - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .filter-container {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
            flex-wrap: wrap;
        }
        .filter-btn {
            background-color: #ffffff;
            border: 1px solid var(--border-color);
            color: var(--secondary-text);
            padding: 8px 16px;
            border-radius: 20px;
            text-decoration: none;
            font-size: 13px;
            font-weight: 600;
            transition: all 0.2s;
        }
        .filter-btn:hover, .filter-btn.active {
            background-color: var(--primary-blue);
            color: #ffffff;
            border-color: var(--primary-blue);
        }
        .assignments-list {
            display: flex;
            flex-direction: column;
            gap: 15px;
        }
        .assignment-card {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-left: 5px solid var(--border-color);
        }
        .assignment-card.status-pending { border-left-color: #ff9800; }
        .assignment-card.status-submitted { border-left-color: #2e7d32; }
        .assignment-card.status-late { border-left-color: #c62828; }
        .assignment-card.status-completed { border-left-color: #1565c0; }

        .assignment-info {
            flex: 1;
        }
        .badge-status {
            font-size: 11px;
            font-weight: 700;
            padding: 4px 10px;
            border-radius: 12px;
            text-transform: uppercase;
            display: inline-block;
            margin-bottom: 8px;
        }
        .badge-pending { background-color: #fff3cd; color: #856404; }
        .badge-submitted { background-color: #d4edda; color: #155724; }
        .badge-late { background-color: #f8d7da; color: #721c24; }
        .badge-completed { background-color: #cce5ff; color: #004085; }

        .btn-action {
            display: inline-block;
            background-color: var(--primary-blue);
            color: #ffffff;
            padding: 10px 20px;
            border-radius: var(--border-radius);
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            transition: background-color 0.2s;
            cursor: pointer;
            border: none;
        }
        .btn-action:hover {
            background-color: var(--dark-blue);
        }
        .btn-secondary-action {
            background-color: #f1f5f9;
            color: var(--secondary-text);
            border: 1px solid var(--border-color);
        }
        .btn-secondary-action:hover {
            background-color: #e2e8f0;
        }
        .sub-details {
            margin-top: 10px;
            padding: 10px;
            background-color: var(--bg-color);
            border-radius: var(--border-radius);
            font-size: 13px;
            color: var(--secondary-text);
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
                
                <h2 class="section-title">📝 My Course Assignments</h2>

                <!-- Filters -->
                <div class="filter-container">
                    <a href="StudentAssignmentServlet?status=all" class="filter-btn <%= "all".equals(filterStatus) ? "active" : "" %>">All</a>
                    <a href="StudentAssignmentServlet?status=pending" class="filter-btn <%= "pending".equals(filterStatus) ? "active" : "" %>">Pending / Late</a>
                    <a href="StudentAssignmentServlet?status=submitted" class="filter-btn <%= "submitted".equals(filterStatus) ? "active" : "" %>">Submitted</a>
                    <a href="StudentAssignmentServlet?status=completed" class="filter-btn <%= "completed".equals(filterStatus) ? "active" : "" %>">Completed & Graded</a>
                </div>

                <!-- Assignment Card List -->
                <% if (assignments == null || assignments.isEmpty()) { %>
                    <div class="card">
                        <p class="text-muted">No assignments matching the selected filter.</p>
                    </div>
                <% } else { %>
                    <div class="assignments-list">
                        <% for (Map<String, Object> a : assignments) { 
                            String status = (String) a.get("status");
                            String cardClass = "status-" + status.toLowerCase();
                            String badgeClass = "badge-" + status.toLowerCase();
                        %>
                            <div class="card assignment-card <%= cardClass %>">
                                <div class="assignment-info">
                                    <span class="badge-status <%= badgeClass %>"><%= status %></span>
                                    <h3 style="color: var(--dark-blue); font-size: 18px; margin-bottom: 5px;"><%= a.get("title") %></h3>
                                    <div style="font-size: 13px; color: var(--secondary-text); margin-bottom: 8px;">
                                        Subject: <strong><%= a.get("subject") %></strong> | Unit: <strong><%= a.get("unit") %></strong> | Instructor: <strong><%= a.get("teacherName") %></strong>
                                    </div>
                                    <p style="font-size: 14px; color: var(--text-color); margin-bottom: 10px;"><%= a.get("description") %></p>
                                    <div style="font-size: 13px; font-weight: 500; color: var(--danger);">
                                        📅 Due Date: <%= a.get("dueDate") %>
                                    </div>

                                    <% if (a.get("submissionId") != null && (Integer) a.get("submissionId") > 0) { %>
                                        <!-- Submission Details Sub-panel -->
                                        <div class="sub-details">
                                            <div>File: <strong><%= a.get("fileName") %></strong></div>
                                            <div>Submitted: <strong><%= a.get("submissionDate") %></strong></div>
                                            <% if (a.get("marksObtained") != null) { %>
                                                <div style="color: var(--success); font-weight: 600; margin-top: 5px;">
                                                    ⭐ Grade/Marks: <%= a.get("marksObtained") %> / 100
                                                </div>
                                            <% } else { %>
                                                <div style="color: var(--warning); font-weight: 600; margin-top: 5px;">
                                                    ⏳ Grading Pending
                                                </div>
                                            <% } %>
                                        </div>
                                    <% } %>
                                </div>

                                <div style="margin-left: 20px;">
                                    <% if ("PENDING".equals(status) || "LATE".equals(status)) { %>
                                        <a href="StudentAssignmentSubmissionServlet?assignmentId=<%= a.get("assignmentId") %>" class="btn-action">Submit</a>
                                    <% } else { %>
                                        <button class="btn-action btn-secondary-action" disabled>✓ Submitted</button>
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
