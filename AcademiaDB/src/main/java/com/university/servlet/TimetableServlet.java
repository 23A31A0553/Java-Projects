package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;
import com.university.model.Timetable;

@WebServlet("/TimetableServlet")
public class TimetableServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"STUDENT".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        
        // Structure to store schedule: Day -> Time Slot -> Timetable Slot
        Map<String, Map<String, Timetable>> scheduleGrid = new HashMap<>();

        // Days List & Time Slots List
        List<String> days = List.of("MON", "TUE", "WED", "THU", "FRI");
        List<String> timeSlots = List.of("09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00");

        for (String day : days) {
            scheduleGrid.put(day, new HashMap<>());
        }

        try (Connection con = DBConnection.getConnection()) {
            
            // Get student core info
            String department = "";
            String semester = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT department, semester FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        department = rs.getString("department");
                        semester = rs.getString("semester");
                    }
                }
            }

            // Fetch weekly schedule lookup
            String sql = 
                "SELECT t.timetable_id, t.day_of_week, t.time_slot, t.room, t.subject_id, " +
                "s.subject_name, s.subject_code, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                "FROM timetable t " +
                "INNER JOIN subjects s ON t.subject_id = s.subject_id " +
                "LEFT JOIN teachers tr ON s.teacher_id = tr.teacher_id " +
                "LEFT JOIN users u ON tr.user_id = u.user_id " +
                "WHERE t.department = ? AND t.semester = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, department);
                ps.setString(2, semester);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timetable slot = new Timetable();
                        slot.setTimetableId(rs.getInt("timetable_id"));
                        slot.setDepartment(department);
                        slot.setSemester(semester);
                        
                        String day = rs.getString("day_of_week").toUpperCase();
                        slot.setDayOfWeek(day);
                        
                        String time = rs.getString("time_slot");
                        slot.setTimeSlot(time);
                        
                        slot.setSubjectId(rs.getInt("subject_id"));
                        slot.setSubjectName(rs.getString("subject_name"));
                        slot.setSubjectCode(rs.getString("subject_code"));
                        slot.setTeacherName(rs.getString("teacher_name") != null ? rs.getString("teacher_name") : "Not Assigned");
                        slot.setRoom(rs.getString("room"));

                        // Place in grid
                        if (scheduleGrid.containsKey(day)) {
                            scheduleGrid.get(day).put(time, slot);
                        }
                    }
                }
            }

            request.setAttribute("scheduleGrid", scheduleGrid);
            request.setAttribute("days", days);
            request.setAttribute("timeSlots", timeSlots);
            
            request.setAttribute("activePage", "timetable");
            request.getRequestDispatcher("timetable.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
