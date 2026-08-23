<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String error = request.getParameter("error");

    List<Map<String, Object>> materialsList = (List<Map<String, Object>>) request.getAttribute("materialsList");
    List<String> subjectsList = (List<String>) request.getAttribute("subjectsList");
    List<String> unitsList = (List<String>) request.getAttribute("unitsList");

    String selectedSubject = (String) request.getAttribute("selectedSubject");
    String selectedUnit = (String) request.getAttribute("selectedUnit");
    String selectedType = (String) request.getAttribute("selectedType");

    if (selectedSubject == null) selectedSubject = "";
    if (selectedUnit == null) selectedUnit = "";
    if (selectedType == null) selectedType = "";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Study Materials - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .filter-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 15px;
            margin-bottom: 20px;
        }
        @media(max-width: 800px) {
            .filter-grid {
                grid-template-columns: 1fr 1fr;
            }
        }
        @media(max-width: 500px) {
            .filter-grid {
                grid-template-columns: 1fr;
            }
        }
        .form-select, .btn-search {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid var(--border-color);
            border-radius: var(--border-radius);
            font-size: 13px;
        }
        .btn-search {
            background-color: var(--primary-blue);
            color: white;
            font-weight: 600;
            border: none;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        .btn-search:hover {
            background-color: var(--dark-blue);
        }
        .materials-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        .material-card {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            border-top: 4px solid var(--primary-blue);
        }
        .material-header {
            margin-bottom: 12px;
        }
        .material-meta {
            font-size: 12px;
            color: var(--secondary-text);
            margin-bottom: 5px;
        }
        .material-meta strong {
            color: var(--text-color);
        }
        .file-badge {
            font-size: 10px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 4px;
            text-transform: uppercase;
            display: inline-block;
            margin-bottom: 8px;
        }
        .file-pdf { background-color: #FDE8E8; color: #9B1C1C; }
        .file-ppt { background-color: #FEF3C7; color: #92400E; }
        .file-doc { background-color: #EBF5FF; color: #1E429F; }
        .file-notes { background-color: #F3E8FF; color: #6B21A8; }
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
                
                <h2 class="section-title">📚 Study Materials & Resources</h2>

                <% if ("missingfile".equals(error)) { %>
                    <div class="message-alert alert-danger" style="background-color: #fde8e8; color: #9b1c1c; border: 1px solid #fbd5d5; padding: 15px; border-radius: var(--border-radius); margin-bottom: 20px;">
                        ⚠ File could not be found on the server. Please contact your instructor.
                    </div>
                <% } else if ("notfound".equals(error)) { %>
                    <div class="message-alert alert-danger" style="background-color: #fde8e8; color: #9b1c1c; border: 1px solid #fbd5d5; padding: 15px; border-radius: var(--border-radius); margin-bottom: 20px;">
                        ⚠ Resource record not found.
                    </div>
                <% } %>

                <!-- Filter Panel -->
                <div class="card">
                    <form action="StudentMaterialServlet" method="GET">
                        <div class="filter-grid">
                            
                            <!-- Subject filter -->
                            <div>
                                <select name="subject" class="form-select">
                                    <option value="">All Subjects</option>
                                    <% if (subjectsList != null) {
                                        for (String s : subjectsList) {
                                    %>
                                            <option value="<%= s %>" <%= s.equals(selectedSubject) ? "selected" : "" %>><%= s %></option>
                                    <%  }
                                    } %>
                                </select>
                            </div>

                            <!-- Unit filter -->
                            <div>
                                <select name="unit" class="form-select">
                                    <option value="">All Units</option>
                                    <% if (unitsList != null) {
                                        for (String u : unitsList) {
                                    %>
                                            <option value="<%= u %>" <%= u.equals(selectedUnit) ? "selected" : "" %>><%= u %></option>
                                    <%  }
                                    } %>
                                </select>
                            </div>

                            <!-- Type filter -->
                            <div>
                                <select name="fileType" class="form-select">
                                    <option value="">All Types</option>
                                    <option value="PDF" <%= "PDF".equals(selectedType) ? "selected" : "" %>>PDF Documents</option>
                                    <option value="PPT" <%= "PPT".equals(selectedType) ? "selected" : "" %>>PPT Slides</option>
                                    <option value="DOC" <%= "DOC".equals(selectedType) ? "selected" : "" %>>DOC Documents</option>
                                    <option value="Notes" <%= "Notes".equals(selectedType) ? "selected" : "" %>>General Notes</option>
                                </select>
                            </div>

                            <!-- Search Button -->
                            <div>
                                <button type="submit" class="btn-search">Filter Materials</button>
                            </div>

                        </div>
                    </form>
                </div>

                <!-- Materials Card Grid -->
                <% if (materialsList == null || materialsList.isEmpty()) { %>
                    <div class="card" style="margin-top: 20px;">
                        <p class="text-muted">No study materials available matching the filters.</p>
                    </div>
                <% } else { %>
                    <div class="materials-grid">
                        <% for (Map<String, Object> m : materialsList) { 
                            String type = (String) m.get("fileType");
                            String badgeClass = "file-" + type.toLowerCase();
                        %>
                            <div class="card material-card">
                                <div>
                                    <div class="material-header">
                                        <span class="file-badge <%= badgeClass %>"><%= type %></span>
                                        <h3 style="color: var(--dark-blue); font-size: 16px; font-weight: 700; margin-bottom: 5px;"><%= m.get("title") %></h3>
                                        <div style="font-size: 12px; color: var(--secondary-text);">
                                            Subject: <strong><%= m.get("subject") %></strong> | Unit: <strong><%= m.get("unit") %></strong>
                                        </div>
                                    </div>
                                    <p style="font-size: 13px; color: var(--text-color); margin-bottom: 15px; line-height: 1.5;"><%= m.get("description") %></p>
                                    
                                    <div class="material-meta">Uploaded By: <strong><%= m.get("teacherName") %></strong></div>
                                    <div class="material-meta">Upload Date: <strong><%= m.get("uploadDate") %></strong></div>
                                </div>
                                <div style="margin-top: 15px; border-top: 1px solid var(--border-color); padding-top: 15px;">
                                    <a href="StudentMaterialServlet?action=download&materialId=<%= m.get("materialId") %>" class="btn-action" style="width: 100%; text-align: center;">💾 Download File</a>
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
