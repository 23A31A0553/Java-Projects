<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.university.model.Timetable" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    Map<String, Map<String, Timetable>> scheduleGrid = (Map<String, Map<String, Timetable>>) request.getAttribute("scheduleGrid");
    List<String> days = (List<String>) request.getAttribute("days");
    List<String> timeSlots = (List<String>) request.getAttribute("timeSlots");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Weekly Timetable - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .timetable-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: #ffffff;
            box-shadow: var(--shadow);
            border-radius: var(--border-radius);
            overflow: hidden;
        }
        .timetable-table th, .timetable-table td {
            padding: 18px;
            text-align: center;
            border: 1px solid var(--border-color);
            vertical-align: middle;
        }
        .timetable-table th {
            background-color: var(--primary-blue);
            color: #ffffff;
            font-weight: 600;
            font-size: 14px;
            width: 17%;
        }
        .timetable-table th.time-col {
            background-color: var(--dark-blue);
            width: 15%;
        }
        .time-slot-label {
            font-weight: 700;
            color: var(--secondary-text);
            font-size: 13px;
        }
        .class-box {
            background-color: var(--light-blue);
            border-left: 4px solid var(--primary-blue);
            padding: 12px;
            border-radius: 4px;
            text-align: left;
        }
        .class-subject {
            font-weight: 700;
            color: var(--dark-blue);
            font-size: 14px;
            margin-bottom: 5px;
        }
        .class-meta {
            font-size: 11px;
            color: var(--secondary-text);
            margin-bottom: 2px;
        }
        .class-meta strong {
            color: var(--text-color);
        }
        .empty-slot {
            font-size: 12px;
            color: var(--secondary-text);
            font-style: italic;
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
                
                <h2 class="section-title">🗓️ Weekly Timetable</h2>

                <div class="card">
                    <h3 class="section-title" style="font-size: 18px; margin-bottom: 5px;">Class Schedule Grid</h3>
                    <p style="font-size: 13px; color: var(--secondary-text); margin-bottom: 15px;">Assigned class schedules and lecture hall locations.</p>
                    
                    <table class="timetable-table">
                        <thead>
                            <tr>
                                <th class="time-col">Time Slot</th>
                                <% for (String day : days) { %>
                                    <th><%= day %></th>
                                <% } %>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (String time : timeSlots) { %>
                                <tr>
                                    <td class="time-slot-label">⏰ <%= time %></td>
                                    <% for (String day : days) { 
                                        Timetable slot = (scheduleGrid != null && scheduleGrid.containsKey(day)) ? scheduleGrid.get(day).get(time) : null;
                                    %>
                                        <td>
                                            <% if (slot != null) { %>
                                                <div class="class-box">
                                                    <div class="class-subject"><%= slot.getSubjectName() %></div>
                                                    <div class="class-meta">Code: <strong><%= slot.getSubjectCode() %></strong></div>
                                                    <div class="class-meta">Instructor: <strong><%= slot.getTeacherName() %></strong></div>
                                                    <div class="class-meta" style="color: var(--primary-blue); font-weight: 600; margin-top: 5px;">
                                                        🚪 Room: <%= slot.getRoom() %>
                                                    </div>
                                                </div>
                                            <% } else { %>
                                                <span class="empty-slot">-</span>
                                            <% } %>
                                        </td>
                                    <% } %>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

            </div>
        </main>
    </div>

    <!-- Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
