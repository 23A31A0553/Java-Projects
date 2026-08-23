<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Map<String, Object>> resultsList = (List<Map<String, Object>>) request.getAttribute("resultsList");
    String semesterAverage = (String) request.getAttribute("semesterAverage");
    String semester = (String) request.getAttribute("semester");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Results - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .results-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: #ffffff;
            box-shadow: var(--shadow);
            border-radius: var(--border-radius);
            overflow: hidden;
        }
        .results-table th, .results-table td {
            padding: 14px 18px;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }
        .results-table th {
            background-color: var(--primary-blue);
            color: #ffffff;
            font-weight: 600;
            font-size: 14px;
        }
        .results-table td {
            font-size: 14px;
        }
        .grade-badge {
            font-size: 12px;
            font-weight: 700;
            padding: 4px 10px;
            border-radius: 4px;
            text-transform: uppercase;
        }
        .grade-pass { background-color: #D4EDDA; color: #155724; }
        .grade-fail { background-color: #F8D7DA; color: #721C24; }
        .grade-pending { background-color: #FFF3CD; color: #856404; }
        
        .avg-panel {
            background-color: var(--light-blue);
            padding: 20px;
            border-radius: var(--border-radius);
            margin-top: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border: 1px solid var(--border-color);
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
                
                <h2 class="section-title">🎓 Semester Transcript Results</h2>

                <div class="card">
                    <h3 class="section-title" style="font-size: 18px; margin-bottom: 5px;">Semester <%= semester %> Academic Transcript</h3>
                    <p style="font-size: 13px; color: var(--secondary-text); margin-bottom: 15px;">Final subject evaluations computed from assignments and class test scores.</p>
                    
                    <% if (resultsList == null || resultsList.isEmpty()) { %>
                        <p class="text-muted">No academic results available.</p>
                    <% } else { %>
                        <table class="results-table">
                            <thead>
                                <tr>
                                    <th>Subject Code / Course Name</th>
                                    <th>Final Weighted Score</th>
                                    <th>Letter Grade</th>
                                    <th>Evaluation Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Map<String, Object> r : resultsList) { 
                                    String grade = (String) r.get("grade");
                                    double score = (Double) r.get("score");
                                    boolean isGraded = !"Pending".equals(grade);
                                    boolean isPass = isGraded && !"F".equals(grade);
                                %>
                                    <tr>
                                        <td><strong><%= r.get("subject") %></strong></td>
                                        <td>
                                            <% if (isGraded) { %>
                                                <%= String.format("%.1f", score) %>%
                                            <% } else { %>
                                                -
                                            <% } %>
                                        </td>
                                        <td>
                                            <% if (isGraded) { %>
                                                <span class="grade-badge <%= isPass ? "grade-pass" : "grade-fail" %>"><%= grade %></span>
                                            <% } else { %>
                                                <span class="grade-badge grade-pending">Pending</span>
                                            <% } %>
                                        </td>
                                        <td>
                                            <% if (isGraded) { %>
                                                <%= isPass ? "Passed Course Credit" : "Academic Fail / Retake Required" %>
                                            <% } else { %>
                                                Awaiting Coursework Marks
                                            <% } %>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>

                        <div class="avg-panel">
                            <div>
                                <h4 style="color: var(--dark-blue); font-size: 16px; margin-bottom: 4px;">Cumulative Semester Average</h4>
                                <p style="font-size: 12px; color: var(--secondary-text); margin: 0;">Weighted across all registered credits in Semester <%= semester %>.</p>
                            </div>
                            <div style="font-size: 24px; font-weight: 700; color: var(--primary-blue);">
                                <%= semesterAverage %>%
                            </div>
                        </div>
                    <% } %>
                </div>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
