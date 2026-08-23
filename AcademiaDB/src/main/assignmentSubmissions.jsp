<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.university.model.AssignmentSubmission" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"TEACHER".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    int assignmentId = (Integer) request.getAttribute("assignmentId");
    String assignmentTitle = (String) request.getAttribute("assignmentTitle");
    String assignmentSubject = (String) request.getAttribute("assignmentSubject");
    String assignmentUnit = (String) request.getAttribute("assignmentUnit");

    int totalCount = (Integer) request.getAttribute("totalCount");
    int submittedCount = (Integer) request.getAttribute("submittedCount");
    int pendingCount = (Integer) request.getAttribute("pendingCount");
    int lateCount = (Integer) request.getAttribute("lateCount");

    List<AssignmentSubmission> submissions = (List<AssignmentSubmission>) request.getAttribute("submissions");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Submissions - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/assignments.css">
</head>
<body>

    <!-- Shared Header -->
    <jsp:include page="teacherHeader.jsp" />

    <div class="app-layout">
        <!-- Shared Sidebar -->
        <jsp:include page="teacherSidebar.jsp" />

        <main class="main-content-wrapper">
            <div class="teacher-container">

                <div class="page-header-actions">
                    <a href="AssignmentServlet" class="btn-back">⬅️ Back to Assignments</a>
                </div>

                <!-- Alert Messages -->
                <% if ("graded".equals(success)) { %>
                    <div class="alert alert-success">Submission graded successfully.</div>
                <% } else if ("negativemarks".equals(error)) { %>
                    <div class="alert alert-danger">Marks cannot be negative.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="alert alert-danger">Database error. Please try again.</div>
                <% } %>

                <!-- Assignment Metadata Card -->
                <div class="assignment-summary-card card">
                    <div class="summary-details">
                        <h2><%= assignmentTitle %></h2>
                        <p class="summary-subtitle">Subject: <%= assignmentSubject %> | Unit: <%= assignmentUnit %></p>
                    </div>
                    
                    <div class="summary-stats">
                        <div class="stat-bubble">
                            <span class="stat-num"><%= totalCount %></span>
                            <span class="stat-lbl">Total Students</span>
                        </div>
                        <div class="stat-bubble">
                            <span class="stat-num color-submitted"><%= submittedCount %></span>
                            <span class="stat-lbl">Submitted</span>
                        </div>
                        <div class="stat-bubble">
                            <span class="stat-num color-pending"><%= pendingCount %></span>
                            <span class="stat-lbl">Pending</span>
                        </div>
                        <div class="stat-bubble">
                            <span class="stat-num color-late"><%= lateCount %></span>
                            <span class="stat-lbl">Late</span>
                        </div>
                    </div>
                </div>

                <!-- Submissions List Card -->
                <div class="submissions-list-card card">
                    <h3 class="section-title">Student Submissions</h3>
                    
                    <div class="table-wrapper">
                        <table class="submissions-table">
                            <thead>
                                <tr>
                                    <th>Student</th>
                                    <th>Submission Date</th>
                                    <th>Status</th>
                                    <th>Document</th>
                                    <th>Marks</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    if (submissions == null || submissions.isEmpty()) {
                                %>
                                    <tr>
                                        <td colspan="6" class="no-data">No students registered in this class.</td>
                                    </tr>
                                <%
                                    } else {
                                        for (AssignmentSubmission s : submissions) {
                                %>
                                    <tr>
                                        <td>
                                            <strong class="student-name-text"><%= s.getStudentFirstName() %> <%= s.getStudentLastName() %></strong>
                                            <p class="student-username-text"><%= s.getStudentUsername() %></p>
                                        </td>
                                        <td><%= s.getSubmissionDate() != null ? s.getSubmissionDate() : "-" %></td>
                                        <td>
                                            <span class="status-badge status-<%= s.getStatus().toLowerCase() %>">
                                                <%= s.getStatus() %>
                                            </span>
                                        </td>
                                        <td>
                                            <% if (s.getFileName() != null) { %>
                                                <a href="StudyMaterialServlet?action=download&id=<%= s.getSubmissionId() %>" class="file-download-link">
                                                    📄 <%= s.getFileName() %>
                                                </a>
                                            <% } else { %>
                                                -
                                            <% } %>
                                        </td>
                                        <td>
                                            <%= s.getMarksObtained() != null ? s.getMarksObtained() : "Not Graded" %>
                                        </td>
                                        <td>
                                            <form action="AssignmentSubmissionServlet" method="post" class="grading-inline-form">
                                                <input type="hidden" name="action" value="grade">
                                                <input type="hidden" name="assignmentId" value="<%= assignmentId %>">
                                                <input type="hidden" name="studentId" value="<%= s.getStudentId() %>">
                                                <input type="number" step="0.1" name="marks" class="input-marks" 
                                                       value="<%= s.getMarksObtained() != null ? s.getMarksObtained() : "" %>" 
                                                       min="0" max="100" placeholder="Marks" required>
                                                <button type="submit" class="btn btn-primary btn-grade">Save</button>
                                            </form>
                                        </td>
                                    </tr>
                                <%
                                        }
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Shared Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
