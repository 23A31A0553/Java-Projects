<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.university.model.StudyMaterial" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"TEACHER".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    List<StudyMaterial> list = (List<StudyMaterial>) request.getAttribute("materials");
    if (list == null) {
        response.sendRedirect("StudyMaterialServlet");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Study Materials - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/study-materials.css">
</head>
<body>

    <!-- Shared Header -->
    <jsp:include page="teacherHeader.jsp" />

    <div class="app-layout">
        <!-- Shared Sidebar -->
        <jsp:include page="teacherSidebar.jsp" />

        <main class="main-content-wrapper">
            <div class="teacher-container">

                <!-- Alert Messages -->
                <% if ("empty".equals(error)) { %>
                    <div class="alert alert-danger">Please fill in all fields and select a file to upload.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="alert alert-danger">Database error occurred. Please try again.</div>
                <% } else if ("blockedfiletype".equals(error)) { %>
                    <div class="alert alert-danger">Upload blocked: Executable files (.exe, .bat, .sh etc.) are not allowed for security reasons.</div>
                <% } %>

                <% if ("uploaded".equals(success)) { %>
                    <div class="alert alert-success">Study material uploaded successfully.</div>
                <% } else if ("deleted".equals(success)) { %>
                    <div class="alert alert-success">Study material deleted successfully.</div>
                <% } %>

                <div class="materials-layout-grid">
                    
                    <!-- Left Column: Upload New Material Form -->
                    <div class="form-container-card card">
                        <h3 class="section-title">📤 Upload Study Material</h3>
                        <form action="StudyMaterialServlet?action=upload" method="post" enctype="multipart/form-data" class="upload-form">
                            
                            <div class="form-group">
                                <label for="title">Title</label>
                                <input type="text" id="title" name="title" placeholder="e.g. Intro to Servlets PDF" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="subject">Subject</label>
                                <input type="text" id="subject" name="subject" placeholder="e.g. Java Programming" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="unit">Unit</label>
                                <input type="text" id="unit" name="unit" placeholder="e.g. Unit 1" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="description">Brief Description</label>
                                <textarea id="description" name="description" rows="3" placeholder="Enter notes or instructions..."></textarea>
                            </div>
                            
                            <div class="form-group">
                                <label for="file">Select File</label>
                                <input type="file" id="file" name="file" required>
                                <small class="file-help">Allowed files: PDF, DOCX, PPTX, TXT, ZIP, Images. Max size: 10MB.</small>
                            </div>
                            
                            <button type="submit" class="btn btn-primary">Upload File</button>
                        </form>
                    </div>

                    <!-- Right Column: Materials List -->
                    <div class="list-container-card card">
                        <h3 class="section-title">📋 Uploaded Resources</h3>
                        
                        <div class="table-wrapper">
                            <table class="materials-table">
                                <thead>
                                    <tr>
                                        <th>Material</th>
                                        <th>Subject</th>
                                        <th>Unit</th>
                                        <th>Date Uploaded</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        if (list.isEmpty()) {
                                    %>
                                        <tr>
                                            <td colspan="5" class="no-data">No study materials uploaded yet.</td>
                                        </tr>
                                    <%
                                        } else {
                                            for (StudyMaterial m : list) {
                                    %>
                                        <tr>
                                            <td>
                                                <strong class="material-title-text"><%= m.getTitle() %></strong>
                                                <p class="material-filename">📄 <%= m.getFileName() %></p>
                                                <p class="material-desc"><%= m.getDescription() %></p>
                                            </td>
                                            <td><%= m.getSubject() %></td>
                                            <td><%= m.getUnit() %></td>
                                            <td><%= m.getUploadDate() %></td>
                                            <td>
                                                <div class="action-btn-group">
                                                    <a href="StudyMaterialServlet?action=download&id=<%= m.getMaterialId() %>" class="btn btn-table btn-view">⬇️ Download</a>
                                                    <form action="StudyMaterialServlet" method="post" onsubmit="return confirm('Are you sure you want to delete this resource?');" style="display:inline;">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="materialId" value="<%= m.getMaterialId() %>">
                                                        <button type="submit" class="btn btn-table btn-delete">🗑️ Delete</button>
                                                    </form>
                                                </div>
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

            </div>
        </main>
    </div>

    <!-- Shared Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
