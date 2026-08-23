<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String error = request.getParameter("error");

    int assignmentId = (Integer) request.getAttribute("assignmentId");
    String subject = (String) request.getAttribute("subject");
    String unit = (String) request.getAttribute("unit");
    String title = (String) request.getAttribute("title");
    String description = (String) request.getAttribute("description");
    java.sql.Date dueDate = (java.sql.Date) request.getAttribute("dueDate");
    String teacherName = (String) request.getAttribute("teacherName");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Submit Assignment - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .submission-layout {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 25px;
            margin-top: 20px;
        }
        @media(max-width: 800px) {
            .submission-layout {
                grid-template-columns: 1fr;
            }
        }
        .meta-row {
            margin-bottom: 12px;
            font-size: 14px;
        }
        .meta-row strong {
            color: var(--dark-blue);
        }
        .file-upload-box {
            border: 2px dashed var(--border-color);
            padding: 30px;
            border-radius: var(--border-radius);
            text-align: center;
            background-color: #fafbfc;
            cursor: pointer;
            transition: border-color 0.2s;
        }
        .file-upload-box:hover {
            border-color: var(--primary-blue);
        }
        .file-upload-input {
            display: none;
        }
        .upload-icon {
            font-size: 40px;
            color: var(--primary-blue);
            margin-bottom: 10px;
        }
        .btn-submit {
            background-color: var(--primary-blue);
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: var(--border-radius);
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.2s;
            width: 100%;
            margin-top: 15px;
        }
        .btn-submit:hover {
            background-color: var(--dark-blue);
        }
        .message-alert {
            padding: 15px;
            border-radius: var(--border-radius);
            margin-bottom: 20px;
            font-size: 14px;
            font-weight: 500;
        }
        .alert-danger {
            background-color: #FDE8E8;
            color: #9B1C1C;
            border: 1px solid #FBD5D5;
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
                
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="section-title">📝 Submit Course Assignment</h2>
                    <a href="StudentAssignmentServlet" class="logout-button" style="background-color: #f1f5f9; color: var(--secondary-text); border: 1px solid var(--border-color); text-decoration: none;">⬅ Back to Assignments</a>
                </div>

                <% if ("nofile".equals(error)) { %>
                    <div class="message-alert alert-danger">⚠ No file selected. Please select a valid file to upload.</div>
                <% } else if ("invalidtype".equals(error)) { %>
                    <div class="message-alert alert-danger">⚠ Invalid file type. Only PDF, DOC, DOCX, ZIP, PPT, PPTX are allowed.</div>
                <% } else if ("too-large".equals(error)) { %>
                    <div class="message-alert alert-danger">⚠ File is too large. Maximum file size allowed is 10MB.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="message-alert alert-danger">⚠ Database error during submission. Please try again.</div>
                <% } %>

                <div class="submission-layout">
                    
                    <!-- Left: Metadata card -->
                    <div class="card">
                        <h3 style="color: var(--dark-blue); font-size: 20px; margin-bottom: 15px;"><%= title %></h3>
                        
                        <div class="meta-row">Subject: <strong><%= subject %></strong></div>
                        <div class="meta-row">Unit: <strong><%= unit %></strong></div>
                        <div class="meta-row">Instructor: <strong><%= teacherName %></strong></div>
                        <div class="meta-row" style="color: var(--danger); font-weight: 600;">Due Date: <strong><%= dueDate %></strong></div>
                        
                        <div style="margin-top: 15px; border-top: 1px solid var(--border-color); padding-top: 15px;">
                            <h4 style="margin-bottom: 5px; color: var(--secondary-text); font-size: 13px;">Description</h4>
                            <p style="font-size: 14px; line-height: 1.6;"><%= description %></p>
                        </div>
                    </div>

                    <!-- Right: Submission form -->
                    <div class="card">
                        <form action="StudentAssignmentSubmissionServlet" method="POST" enctype="multipart/form-data">
                            <input type="hidden" name="assignmentId" value="<%= assignmentId %>">
                            
                            <h3 style="margin-bottom: 15px; font-size: 16px;">Upload Assignment File</h3>
                            
                            <div class="file-upload-box" onclick="document.getElementById('fileInput').click();">
                                <div class="upload-icon">📤</div>
                                <h4 id="fileNameDisplay" style="font-size: 15px; margin-bottom: 5px; color: var(--primary-blue);">Click to browse and upload file</h4>
                                <p style="font-size: 12px; color: var(--secondary-text);">Allowed files: PDF, DOC, DOCX, ZIP, PPT, PPTX (Max 10MB)</p>
                                <input type="file" id="fileInput" name="file" class="file-upload-input" onchange="fileSelected(this);" required>
                            </div>

                            <button type="submit" class="btn-submit">Submit Assignment File</button>
                        </form>
                    </div>

                </div>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

    <script>
        function fileSelected(input) {
            var file = input.files[0];
            if (file) {
                document.getElementById('fileNameDisplay').innerText = "Selected: " + file.name;
                document.getElementById('fileNameDisplay').style.color = "var(--success)";
            }
        }
    </script>

</body>
</html>
