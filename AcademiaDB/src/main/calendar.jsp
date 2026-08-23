<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.university.model.CalendarEvent" %>
<%@ page import="java.time.LocalDate" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"TEACHER".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    List<CalendarEvent> events = (List<CalendarEvent>) request.getAttribute("calendarEvents");
    if (events == null) {
        response.sendRedirect("CalendarServlet");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Academic Calendar - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/calendar.css">
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
                    <div class="alert alert-danger">Please fill in all required fields.</div>
                <% } else if ("database".equals(error)) { %>
                    <div class="alert alert-danger">Database error. Please try again.</div>
                <% } %>

                <% if ("added".equals(success)) { %>
                    <div class="alert alert-success">Calendar event added successfully.</div>
                <% } %>

                <div class="calendar-layout-grid">
                    
                    <!-- Left Column: Add Custom Event Form -->
                    <div class="form-container-card card">
                        <h3 class="section-title">📅 Add Calendar Event</h3>
                        <form action="CalendarServlet" method="post" class="calendar-form">
                            
                            <div class="form-group">
                                <label for="title">Event Title</label>
                                <input type="text" id="title" name="title" placeholder="e.g. Semester Project Review" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="eventDate">Event Date</label>
                                <input type="date" id="eventDate" name="eventDate" value="<%= LocalDate.now().toString() %>" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="eventType">Event Type</label>
                                <select id="eventType" name="eventType" required>
                                    <option value="EVENT">General Event</option>
                                    <option value="MEETING">Meeting</option>
                                    <option value="HOLIDAY">Holiday</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label for="description">Description (Optional)</label>
                                <textarea id="description" name="description" rows="3" placeholder="Enter additional details..."></textarea>
                            </div>
                            
                            <button type="submit" class="btn btn-primary">Add Event</button>
                        </form>
                    </div>

                    <!-- Right Column: Unified Schedule Timeline -->
                    <div class="timeline-container-card card">
                        <h3 class="section-title">📋 Schedule Timeline</h3>
                        
                        <div class="timeline">
                            <%
                                if (events.isEmpty()) {
                            %>
                                <p class="no-data">No schedule items or events found.</p>
                            <%
                                } else {
                                    for (CalendarEvent e : events) {
                                        String type = e.getEventType();
                                        String badgeClass = "badge-event";
                                        String typeEmoji = "📅";
                                        
                                        if ("TEST".equalsIgnoreCase(type)) {
                                            badgeClass = "badge-test";
                                            typeEmoji = "🧪";
                                        } else if ("ASSIGNMENT".equalsIgnoreCase(type)) {
                                            badgeClass = "badge-assignment";
                                            typeEmoji = "📝";
                                        } else if ("MEETING".equalsIgnoreCase(type)) {
                                            badgeClass = "badge-meeting";
                                            typeEmoji = "👥";
                                        } else if ("HOLIDAY".equalsIgnoreCase(type)) {
                                            badgeClass = "badge-holiday";
                                            typeEmoji = "🎉";
                                        }
                            %>
                                <div class="timeline-item">
                                    <div class="timeline-badge <%= badgeClass %>">
                                        <%= typeEmoji %>
                                    </div>
                                    <div class="timeline-content card">
                                        <div class="timeline-header">
                                            <span class="timeline-type-tag tag-<%= type.toLowerCase() %>"><%= type %></span>
                                            <span class="timeline-date"><%= e.getEventDate() %></span>
                                        </div>
                                        <h4 class="timeline-title"><%= e.getTitle() %></h4>
                                        <p class="timeline-desc"><%= e.getDescription() %></p>
                                    </div>
                                </div>
                            <%
                                    }
                                }
                            %>
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
