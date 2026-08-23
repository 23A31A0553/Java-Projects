<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.university.model.Subject" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String action = request.getParameter("action");
    List<Subject> subjectsList = (List<Subject>) request.getAttribute("subjectsList");
    Subject subject = (Subject) request.getAttribute("subject");
    
    Integer assignmentCount = (Integer) request.getAttribute("assignmentCount");
    Integer testCount = (Integer) request.getAttribute("testCount");
    Integer materialCount = (Integer) request.getAttribute("materialCount");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Subjects - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .subjects-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        .subject-card {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            min-height: 200px;
        }
        .subject-header {
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 12px;
            margin-bottom: 15px;
        }
        .subject-code {
            font-size: 11px;
            font-weight: 700;
            background: var(--light-blue);
            color: var(--primary-blue);
            padding: 3px 8px;
            border-radius: 12px;
            display: inline-block;
            margin-bottom: 6px;
        }
        .subject-title {
            font-size: 18px;
            font-weight: 700;
            color: var(--dark-blue);
        }
        .subject-meta {
            font-size: 13px;
            color: var(--secondary-text);
            margin-bottom: 6px;
        }
        .subject-meta strong {
            color: var(--text-color);
        }
        .btn-view {
            display: inline-block;
            background-color: var(--primary-blue);
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: var(--border-radius);
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            text-align: center;
            transition: background-color 0.2s;
            margin-top: 15px;
        }
        .btn-view:hover {
            background-color: var(--dark-blue);
        }
        .details-container {
            display: grid;
            grid-template-columns: 1.5fr 1fr;
            gap: 25px;
            margin-top: 20px;
        }
        @media (max-width: 800px) {
            .details-container {
                grid-template-columns: 1fr;
            }
        }
        .units-list {
            margin-top: 15px;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }
        .unit-item {
            background-color: var(--bg-color);
            padding: 15px;
            border-radius: var(--border-radius);
            border-left: 4px solid var(--primary-blue);
        }
        .unit-item h4 {
            margin-bottom: 5px;
            color: var(--dark-blue);
        }
        .unit-item p {
            font-size: 13px;
            color: var(--secondary-text);
        }
        .stats-summary-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
            margin-top: 15px;
        }
        .summary-stat-card {
            background-color: var(--bg-color);
            padding: 15px;
            border-radius: var(--border-radius);
            text-align: center;
        }
        .summary-stat-value {
            font-size: 24px;
            font-weight: 700;
            color: var(--primary-blue);
            margin-top: 5px;
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

                <% if ("details".equals(action) && subject != null) { %>
                    <!-- SUBJECT DETAILS VIEW -->
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <h2 class="section-title">📚 Subject Details</h2>
                        <a href="SubjectServlet" class="logout-button" style="background-color: #f1f5f9; color: var(--secondary-text); border: 1px solid var(--border-color); text-decoration: none;">⬅ Back to Subjects</a>
                    </div>

                    <div class="details-container">
                        
                        <!-- Left Panel: Units List -->
                        <div class="card">
                            <h3 class="section-title" style="font-size: 18px; margin-bottom: 10px;">📖 Course Units</h3>
                            <p class="subject-meta" style="margin-bottom: 20px;">Syllabus structured mapping across semester units.</p>
                            
                            <div class="units-list">
                                <div class="unit-item">
                                    <h4>Unit 1: Introduction & Fundamentals</h4>
                                    <p>Covers structural foundation, syntax rules, architectural basics, and initial setup models.</p>
                                </div>
                                <div class="unit-item">
                                    <h4>Unit 2: System Architecture & Design</h4>
                                    <p>Covers components, controllers, core design principles, and intermediate lifecycle patterns.</p>
                                </div>
                                <div class="unit-item">
                                    <h4>Unit 3: Database & Backend Integration</h4>
                                    <p>Covers connection pools, relational mapping, schema optimization, and transaction logs.</p>
                                </div>
                                <div class="unit-item">
                                    <h4>Unit 4: Advanced Middleware & Security</h4>
                                    <p>Covers filters, access control guards, sessions, and request interception protocols.</p>
                                </div>
                                <div class="unit-item">
                                    <h4>Unit 5: Production Deployment & Verification</h4>
                                    <p>Covers optimization audits, automated checks, environment profiles, and compile targets.</p>
                                </div>
                            </div>
                        </div>

                        <!-- Right Panel: Info & Counts -->
                        <div>
                            <div class="card" style="margin-bottom: 20px;">
                                <div class="subject-code"><%= subject.getSubjectCode() %></div>
                                <h3 style="color: var(--dark-blue); font-size: 20px; margin-bottom: 15px;"><%= subject.getSubjectName() %></h3>
                                
                                <div class="subject-meta">Instructor: <strong><%= subject.getTeacherName() %></strong></div>
                                <div class="subject-meta">Department: <strong><%= subject.getDepartment() %></strong></div>
                                <div class="subject-meta">Semester: <strong>Semester <%= subject.getSemester() %></strong></div>
                                <div class="subject-meta">Credits/Units: <strong><%= subject.getUnits() %> Credits</strong></div>
                            </div>

                            <div class="card">
                                <h3 class="section-title" style="font-size: 16px; margin-bottom: 15px;">📊 Academic Activity Summary</h3>
                                <div class="stats-summary-grid">
                                    <div class="summary-stat-card">
                                        <div style="font-size: 11px; color: var(--secondary-text); text-transform: uppercase;">Assignments</div>
                                        <div class="summary-stat-value"><%= assignmentCount %></div>
                                    </div>
                                    <div class="summary-stat-card">
                                        <div style="font-size: 11px; color: var(--secondary-text); text-transform: uppercase;">Class Tests</div>
                                        <div class="summary-stat-value"><%= testCount %></div>
                                    </div>
                                    <div class="summary-stat-card">
                                        <div style="font-size: 11px; color: var(--secondary-text); text-transform: uppercase;">Materials</div>
                                        <div class="summary-stat-value"><%= materialCount %></div>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                <% } else { %>
                    <!-- LIST VIEW -->
                    <h2 class="section-title">📚 My Registered Subjects</h2>
                    
                    <% if (subjectsList == null || subjectsList.isEmpty()) { %>
                        <div class="card">
                            <p class="text-muted">No subjects registered for your current semester.</p>
                        </div>
                    <% } else { %>
                        <div class="subjects-grid">
                            <% for (Subject s : subjectsList) { %>
                                <div class="card subject-card">
                                    <div>
                                        <div class="subject-header">
                                            <span class="subject-code"><%= s.getSubjectCode() %></span>
                                            <h3 class="subject-title"><%= s.getSubjectName() %></h3>
                                        </div>
                                        <div class="subject-meta">Instructor: <strong><%= s.getTeacherName() %></strong></div>
                                        <div class="subject-meta">Credits/Units: <strong><%= s.getUnits() %></strong></div>
                                    </div>
                                    <a href="SubjectServlet?subjectId=<%= s.getSubjectId() %>" class="btn-view">View Details</a>
                                </div>
                            <% } %>
                        </div>
                    <% } %>
                <% } %>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
