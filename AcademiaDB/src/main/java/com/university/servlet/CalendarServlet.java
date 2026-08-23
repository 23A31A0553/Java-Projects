package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;
import com.university.model.CalendarEvent;

@WebServlet("/CalendarServlet")
public class CalendarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"TEACHER".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        viewCalendar(request, response);
    }

    private void viewCalendar(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<CalendarEvent> events = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            
            // Get teacherId
            int teacherId = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id FROM teachers WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teacherId = rs.getInt("teacher_id");
                    }
                }
            }

            // 1. Fetch Class Tests as events
            String testSql = "SELECT test_title, test_date, subject, description FROM class_tests WHERE teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(testSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        CalendarEvent event = new CalendarEvent();
                        event.setTitle("Class Test: " + rs.getString("test_title"));
                        event.setEventDate(rs.getDate("test_date"));
                        event.setEventType("TEST");
                        event.setDescription("Subject: " + rs.getString("subject") + ". " + rs.getString("description"));
                        events.add(event);
                    }
                }
            }

            // 2. Fetch Assignments as events
            String assignmentSql = "SELECT title, due_date, subject, description FROM assignments WHERE teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(assignmentSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        CalendarEvent event = new CalendarEvent();
                        event.setTitle("Assignment Due: " + rs.getString("title"));
                        event.setEventDate(rs.getDate("due_date"));
                        event.setEventType("ASSIGNMENT");
                        event.setDescription("Subject: " + rs.getString("subject") + ". " + rs.getString("description"));
                        events.add(event);
                    }
                }
            }

            // 3. Fetch Calendar Events
            String customEventSql = "SELECT title, event_date, event_type, description FROM calendar_events WHERE teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(customEventSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        CalendarEvent event = new CalendarEvent();
                        event.setTitle(rs.getString("title"));
                        event.setEventDate(rs.getDate("event_date"));
                        event.setEventType(rs.getString("event_type"));
                        event.setDescription(rs.getString("description"));
                        events.add(event);
                    }
                }
            }

            // Sort events by date ascending
            Collections.sort(events, new Comparator<CalendarEvent>() {
                @Override
                public int compare(CalendarEvent e1, CalendarEvent e2) {
                    if (e1.getEventDate() == null || e2.getEventDate() == null) {
                        return 0;
                    }
                    return e1.getEventDate().compareTo(e2.getEventDate());
                }
            });

            request.setAttribute("calendarEvents", events);
            request.setAttribute("activePage", "calendar");
            request.getRequestDispatcher("calendar.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherServlet?error=database");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || !"TEACHER".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String title = request.getParameter("title");
        String dateStr = request.getParameter("eventDate");
        String type = request.getParameter("eventType");
        String description = request.getParameter("description");

        if (title == null || title.trim().isEmpty() || dateStr == null || dateStr.trim().isEmpty() || type == null) {
            response.sendRedirect("CalendarServlet?error=empty");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {
            // Get teacherId
            int teacherId = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id FROM teachers WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teacherId = rs.getInt("teacher_id");
                    }
                }
            }

            String sql = "INSERT INTO calendar_events (teacher_id, title, event_date, event_type, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, teacherId);
                ps.setString(2, title.trim());
                ps.setDate(3, Date.valueOf(dateStr));
                ps.setString(4, type);
                ps.setString(5, description != null ? description.trim() : "");
                ps.executeUpdate();
            }

            response.sendRedirect("CalendarServlet?success=added");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("CalendarServlet?error=database");
        }
    }
}
