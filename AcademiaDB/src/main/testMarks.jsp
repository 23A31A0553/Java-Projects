<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.university.model.TestMark" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"TEACHER".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    int testId = (Integer) request.getAttribute("testId");
    String testTitle = (String) request.getAttribute("testTitle");
    String testSubject = (String) request.getAttribute("testSubject");
    String testUnit = (String) request.getAttribute("testUnit");
    int totalMarks = (Integer) request.getAttribute("totalMarks");

    int gradedStudents = (Integer) request.getAttribute("gradedStudents");
    String highestMark = (String) request.getAttribute("highestMark");
    String lowestMark = (String) request.getAttribute("lowestMark");
    String averageMark = (String) request.getAttribute("averageMark");
    int passCount = (Integer) request.getAttribute("passCount");
    int failCount = (Integer) request.getAttribute("failCount");

    List<TestMark> testMarks = (List<TestMark>) request.getAttribute("testMarks");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Enter Test Marks - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/class-tests.css">
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
                    <a href="ClassTestServlet" class="btn-back">⬅️ Back to Class Tests</a>
                </div>

                <!-- Alert Messages -->
                <% if ("saved".equals(success)) { %>
                    <div class="alert alert-success">Test marks saved successfully.</div>
                <% } else if ("invalidrange".equals(error)) { %>
                    <div class="alert alert-danger">Marks must be between 0 and <%= totalMarks %>.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="alert alert-danger">Database error. Please try again.</div>
                <% } %>

                <!-- Test Performance Statistics Card -->
                <div class="test-stats-card card">
                    <div class="test-header-info">
                        <h2><%= testTitle %></h2>
                        <p class="summary-subtitle">Subject: <%= testSubject %> | Unit: <%= testUnit %> | Total Marks: <%= totalMarks %></p>
                    </div>

                    <div class="stats-row">
                        <div class="stat-item">
                            <span class="stat-val"><%= gradedStudents %></span>
                            <span class="stat-lbl">Students Graded</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-val"><%= averageMark %></span>
                            <span class="stat-lbl">Average Mark</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-val text-success"><%= highestMark %></span>
                            <span class="stat-lbl">Highest Mark</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-val text-danger"><%= lowestMark %></span>
                            <span class="stat-lbl">Lowest Mark</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-val text-success"><%= passCount %></span>
                            <span class="stat-lbl">Passed (>=40%)</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-val text-danger"><%= failCount %></span>
                            <span class="stat-lbl">Failed (<40%)</span>
                        </div>
                    </div>
                </div>

                <!-- Entering Marks Sheet Card -->
                <div class="marks-sheet-card card">
                    <h3 class="section-title">Class Test Marks Sheet</h3>
                    
                    <form action="TestMarksServlet" method="post" class="marks-form">
                        <input type="hidden" name="testId" value="<%= testId %>">
                        
                        <div class="table-wrapper">
                            <table class="marks-table">
                                <thead>
                                    <tr>
                                        <th>Student</th>
                                        <th>Username</th>
                                        <th>Marks Obtained (Max: <%= totalMarks %>)</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        if (testMarks == null || testMarks.isEmpty()) {
                                    %>
                                        <tr>
                                            <td colspan="4" class="no-data">No students registered in this class.</td>
                                        </tr>
                                    <%
                                        } else {
                                            for (TestMark m : testMarks) {
                                                double score = m.getMarksObtained();
                                                boolean isGraded = m.getMarkId() > 0;
                                    %>
                                        <tr>
                                            <td>
                                                <strong class="student-name-text"><%= m.getStudentFirstName() %> <%= m.getStudentLastName() %></strong>
                                            </td>
                                            <td><%= m.getStudentUsername() %></td>
                                            <td>
                                                <input type="number" step="0.1" name="marks_<%= m.getStudentId() %>" 
                                                       value="<%= isGraded ? score : "" %>" 
                                                       min="0" max="<%= totalMarks %>" 
                                                       class="input-test-mark" placeholder="Enter Score">
                                            </td>
                                            <td>
                                                <% if (isGraded) { %>
                                                    <span class="status-badge status-graded">GRADED</span>
                                                <% } else { %>
                                                    <span class="status-badge status-pending">PENDING</span>
                                                <% } %>
                                            </td>
                                        </tr>
                                    <%
                                            }
                                        }
                                    %>
                                </tbody>
                            </table>
                        </div>
                        
                        <% if (testMarks != null && !testMarks.isEmpty()) { %>
                            <div class="form-actions">
                                <button type="submit" class="btn btn-primary">Save Marks Sheet</button>
                                <a href="ClassTestServlet" class="btn btn-secondary">Cancel</a>
                            </div>
                        <% } %>
                    </form>
                </div>

            </div>
        </main>
    </div>

    <!-- Shared Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
