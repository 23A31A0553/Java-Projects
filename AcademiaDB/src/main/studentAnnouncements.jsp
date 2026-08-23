<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Map<String, Object>> announcementsList = (List<Map<String, Object>>) request.getAttribute("announcementsList");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Announcements - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .announcements-timeline {
            display: flex;
            flex-direction: column;
            gap: 20px;
            margin-top: 20px;
        }
        .announcement-card {
            border-left: 5px solid var(--primary-blue);
        }
        .announcement-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 12px;
            margin-bottom: 15px;
            flex-wrap: wrap;
            gap: 10px;
        }
        .announcement-meta {
            font-size: 13px;
            color: var(--secondary-text);
        }
        .announcement-meta strong {
            color: var(--text-color);
        }
        .announcement-msg {
            font-size: 14px;
            line-height: 1.6;
            color: var(--text-color);
        }
        .badge-audience {
            font-size: 10px;
            font-weight: 700;
            background-color: var(--light-blue);
            color: var(--primary-blue);
            padding: 3px 8px;
            border-radius: 12px;
            text-transform: uppercase;
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
                
                <h2 class="section-title">📢 University Announcements</h2>

                <% if (announcementsList == null || announcementsList.isEmpty()) { %>
                    <div class="card">
                        <p class="text-muted">No announcements posted yet.</p>
                    </div>
                <% } else { %>
                    <div class="announcements-timeline">
                        <% for (Map<String, Object> a : announcementsList) { %>
                            <div class="card announcement-card">
                                
                                <div class="announcement-header">
                                    <div>
                                        <span class="badge-audience"><%= a.get("subject") %></span>
                                        <h3 style="color: var(--dark-blue); font-size: 18px; margin-top: 8px; font-weight: 700;"><%= a.get("title") %></h3>
                                    </div>
                                    <div class="announcement-meta" style="text-align: right;">
                                        By: <strong><%= a.get("posterName") %></strong><br>
                                        <span style="font-size: 12px; color: var(--secondary-text);"><%= a.get("createdAt") %></span>
                                    </div>
                                </div>

                                <div class="announcement-msg">
                                    <p><%= ((String)a.get("message")).replace("\n", "<br>") %></p>
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
