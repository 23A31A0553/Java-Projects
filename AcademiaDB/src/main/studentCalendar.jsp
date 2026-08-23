<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Map<String, Object>> calendarEvents = (List<Map<String, Object>>) request.getAttribute("calendarEvents");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Academic Calendar - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/studentLayout.css">
    <style>
        .calendar-timeline {
            display: flex;
            flex-direction: column;
            gap: 15px;
            margin-top: 20px;
        }
        .event-row {
            display: flex;
            background: #ffffff;
            border-radius: var(--border-radius);
            box-shadow: var(--shadow);
            border: 1px solid var(--border-color);
            overflow: hidden;
            transition: transform 0.2s;
        }
        .event-row:hover {
            transform: translateX(4px);
        }
        .event-date-box {
            width: 120px;
            background-color: var(--primary-blue);
            color: #ffffff;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 15px;
            font-weight: 700;
            text-align: center;
            flex-shrink: 0;
        }
        .event-type-test { background-color: var(--danger); }
        .event-type-assignment { background-color: var(--warning); }
        .event-type-event { background-color: var(--success); }

        .event-details {
            padding: 15px 20px;
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }
        .badge-type {
            font-size: 10px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 4px;
            text-transform: uppercase;
            display: inline-block;
            margin-bottom: 8px;
            width: fit-content;
        }
        .badge-type-test { background-color: #FDE8E8; color: #9B1C1C; }
        .badge-type-assignment { background-color: #FEF3C7; color: #92400E; }
        .badge-type-event { background-color: #DEF7EC; color: #03543F; }
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
                
                <h2 class="section-title">📅 Academic Calendar Agenda</h2>

                <% if (calendarEvents == null || calendarEvents.isEmpty()) { %>
                    <div class="card">
                        <p class="text-muted">No scheduled academic events or deadlines.</p>
                    </div>
                <% } else { %>
                    <div class="calendar-timeline">
                        <% for (Map<String, Object> e : calendarEvents) { 
                            String type = (String) e.get("type");
                            String colorClass = "event-type-" + type.toLowerCase();
                            String badgeClass = "badge-type-" + type.toLowerCase();
                            
                            java.sql.Date dateVal = (java.sql.Date) e.get("date");
                            java.text.SimpleDateFormat sdfMonth = new java.text.SimpleDateFormat("MMM");
                            java.text.SimpleDateFormat sdfDay = new java.text.SimpleDateFormat("dd");
                            java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("EEEE, dd MMM yyyy");
                            String month = sdfMonth.format(dateVal);
                            String day = sdfDay.format(dateVal);
                            String fullDateStr = sdfFull.format(dateVal);
                        %>
                            <div class="event-row">
                                <div class="event-date-box <%= colorClass %>">
                                    <div style="font-size: 26px; line-height: 1;"><%= day %></div>
                                    <div style="font-size: 13px; text-transform: uppercase; margin-top: 4px; letter-spacing: 1px;"><%= month %></div>
                                </div>
                                <div class="event-details">
                                    <span class="badge-type <%= badgeClass %>"><%= type %></span>
                                    <h3 style="font-size: 16px; font-weight: 700; color: var(--dark-blue); margin-bottom: 5px;"><%= e.get("title") %></h3>
                                    <p style="font-size: 13px; color: var(--secondary-text); margin-bottom: 8px;"><%= e.get("description") %></p>
                                    <span style="font-size: 11px; color: var(--secondary-text); font-weight: 600;">📅 Scheduled Date: <%= fullDateStr %></span>
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
