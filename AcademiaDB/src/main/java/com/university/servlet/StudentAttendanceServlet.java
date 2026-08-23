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

@WebServlet("/StudentAttendanceServlet")
public class StudentAttendanceServlet extends HttpServlet {

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
        String subjectFilter = request.getParameter("subject");

        double overallRate = 100.0;
        int totalClasses = 0;
        int presentClasses = 0;

        List<Map<String, Object>> subjectBreakdown = new ArrayList<>();
        List<Map<String, Object>> attendanceHistory = new ArrayList<>();
        List<String> distinctSubjectsList = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            
            // Get student core info
            int studentId = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT student_id FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                    }
                }
            }

            // Get Overall rate
            String overallSql = "SELECT COUNT(*), SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) FROM attendance WHERE student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(overallSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalClasses = rs.getInt(1);
                        presentClasses = rs.getInt(2);
                        if (totalClasses > 0) {
                            overallRate = ((double) presentClasses / totalClasses) * 100;
                        }
                    }
                }
            }

            // Get Subject-wise breakdown
            String breakdownSql = 
                "SELECT subject, COUNT(*) AS total, SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) AS present " +
                "FROM attendance WHERE student_id = ? " +
                "GROUP BY subject ORDER BY subject ASC";
            try (PreparedStatement ps = con.prepareStatement(breakdownSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        String sub = rs.getString("subject");
                        int tot = rs.getInt("total");
                        int pres = rs.getInt("present");
                        double pct = tot > 0 ? ((double) pres / tot) * 100 : 100.0;
                        
                        map.put("subject", sub);
                        map.put("total", tot);
                        map.put("present", pres);
                        map.put("percentage", pct);
                        subjectBreakdown.add(map);
                        
                        distinctSubjectsList.add(sub);
                    }
                }
            }

            // Get attendance history
            StringBuilder historySql = new StringBuilder(
                "SELECT a.attendance_date, a.subject, a.status, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                "FROM attendance a " +
                "INNER JOIN teachers t ON a.teacher_id = t.teacher_id " +
                "INNER JOIN users u ON t.user_id = u.user_id " +
                "WHERE a.student_id = ? "
            );
            List<Object> params = new ArrayList<>();
            params.add(studentId);

            if (subjectFilter != null && !subjectFilter.trim().isEmpty()) {
                historySql.append("AND a.subject = ? ");
                params.add(subjectFilter.trim());
            }

            historySql.append("ORDER BY a.attendance_date DESC");

            try (PreparedStatement ps = con.prepareStatement(historySql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> record = new HashMap<>();
                        record.put("date", rs.getDate("attendance_date"));
                        record.put("subject", rs.getString("subject"));
                        record.put("status", rs.getString("status"));
                        record.put("teacherName", rs.getString("teacher_name"));
                        attendanceHistory.add(record);
                    }
                }
            }

            request.setAttribute("overallRate", String.format("%.1f", overallRate));
            request.setAttribute("totalClasses", totalClasses);
            request.setAttribute("presentClasses", presentClasses);
            request.setAttribute("subjectBreakdown", subjectBreakdown);
            request.setAttribute("attendanceHistory", attendanceHistory);
            request.setAttribute("subjectsList", distinctSubjectsList);
            request.setAttribute("selectedSubject", subjectFilter);
            
            request.setAttribute("activePage", "attendance");
            request.getRequestDispatcher("attendance.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
