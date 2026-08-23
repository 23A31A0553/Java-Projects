package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;
import com.university.model.Attendance;

@WebServlet("/AttendanceServlet")
public class AttendanceServlet extends HttpServlet {

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

        String action = request.getParameter("action");
        if ("markForm".equalsIgnoreCase(action)) {
            showMarkForm(request, response);
            return;
        }

        viewSummary(request, response);
    }

    private void viewSummary(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<Attendance> summaries = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            // Get teacherId and department
            int teacherId = 0;
            String teacherDept = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id, department FROM teachers WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teacherId = rs.getInt("teacher_id");
                        teacherDept = rs.getString("department");
                    }
                }
            }

            // Calculate attendance stats per student in the teacher's department
            String sql = 
                "SELECT s.student_id, u.username, u.first_name, u.last_name, " +
                "COUNT(a.attendance_id) AS total_classes, " +
                "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count " +
                "FROM students s " +
                "INNER JOIN users u ON s.user_id = u.user_id " +
                "LEFT JOIN attendance a ON s.student_id = a.student_id " +
                "WHERE s.department = ? " +
                "GROUP BY s.student_id, u.username, u.first_name, u.last_name " +
                "ORDER BY u.first_name ASC";
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, teacherDept);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Attendance summary = new Attendance();
                        summary.setStudentId(rs.getInt("student_id"));
                        summary.setStudentFirstName(rs.getString("first_name"));
                        summary.setStudentLastName(rs.getString("last_name"));
                        summary.setStudentUsername(rs.getString("username"));
                        
                        int total = rs.getInt("total_classes");
                        int present = rs.getInt("present_count");
                        
                        summary.setTotalClasses(total);
                        summary.setPresentCount(present);
                        summary.setAbsentCount(total - present);
                        
                        double percentage = total > 0 ? (((double) present / total) * 100) : 100.0;
                        summary.setAttendancePercentage(percentage);
                        
                        summaries.add(summary);
                    }
                }
            }

            request.setAttribute("attendanceSummaries", summaries);
            request.setAttribute("activePage", "attendance");
            request.getRequestDispatcher("attendance.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherServlet?error=database");
        }
    }

    private void showMarkForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String subject = request.getParameter("subject");
        String dateStr = request.getParameter("date");
        String semester = request.getParameter("semester");

        if (subject == null || subject.trim().isEmpty() ||
            dateStr == null || dateStr.trim().isEmpty() ||
            semester == null || semester.trim().isEmpty()) {
            request.setAttribute("activePage", "attendance");
            request.getRequestDispatcher("attendance.jsp?action=setup").forward(request, response);
            return;
        }

        List<Attendance> studentAttendanceList = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            // Get teacherId and department
            int teacherId = 0;
            String teacherDept = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id, department FROM teachers WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teacherId = rs.getInt("teacher_id");
                        teacherDept = rs.getString("department");
                    }
                }
            }

            // Load students matching department and semester, outer joining attendance for the selected date and subject
            String sql = 
                "SELECT s.student_id, u.username, u.first_name, u.last_name, " +
                "a.attendance_id, a.status " +
                "FROM students s " +
                "INNER JOIN users u ON s.user_id = u.user_id " +
                "LEFT JOIN attendance a ON s.student_id = a.student_id AND a.attendance_date = ? AND a.subject = ? " +
                "WHERE s.department = ? AND s.semester = ? " +
                "ORDER BY u.first_name ASC";
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(dateStr));
                ps.setString(2, subject.trim());
                ps.setString(3, teacherDept);
                ps.setString(4, semester.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Attendance record = new Attendance();
                        record.setStudentId(rs.getInt("student_id"));
                        record.setStudentFirstName(rs.getString("first_name"));
                        record.setStudentLastName(rs.getString("last_name"));
                        record.setStudentUsername(rs.getString("username"));
                        record.setSubject(subject);
                        record.setAttendanceDate(Date.valueOf(dateStr));
                        
                        int attId = rs.getInt("attendance_id");
                        if (attId > 0) {
                            record.setAttendanceId(attId);
                            record.setStatus(rs.getString("status"));
                        } else {
                            record.setAttendanceId(0);
                            record.setStatus("PRESENT"); // Default checked
                        }
                        studentAttendanceList.add(record);
                    }
                }
            }

            request.setAttribute("studentAttendanceList", studentAttendanceList);
            request.setAttribute("selectedSubject", subject);
            request.setAttribute("selectedDate", dateStr);
            request.setAttribute("selectedSemester", semester);
            request.setAttribute("activePage", "attendance");
            
            request.getRequestDispatcher("attendance.jsp?action=mark").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AttendanceServlet?error=database");
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

        saveAttendance(request, response);
    }

    private void saveAttendance(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String subject = request.getParameter("subject");
        String dateStr = request.getParameter("date");
        String semester = request.getParameter("semester");

        if (subject == null || dateStr == null || semester == null) {
            response.sendRedirect("AttendanceServlet?error=invalid");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Get teacherId and department
            int teacherId = 0;
            String teacherDept = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id, department FROM teachers WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teacherId = rs.getInt("teacher_id");
                        teacherDept = rs.getString("department");
                    }
                }
            }

            // Find all student IDs matching department and semester
            List<Integer> studentIds = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement("SELECT student_id FROM students WHERE department = ? AND semester = ?")) {
                ps.setString(1, teacherDept);
                ps.setString(2, semester.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        studentIds.add(rs.getInt("student_id"));
                    }
                }
            }

            con.setAutoCommit(false);
            try {
                String upsertSql = 
                    "INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE status = VALUES(status)";
                
                try (PreparedStatement ps = con.prepareStatement(upsertSql)) {
                    for (int studentId : studentIds) {
                        String statusVal = request.getParameter("attendance_" + studentId);
                        if (statusVal == null) {
                            statusVal = "ABSENT"; // Unchecked means absent
                        }
                        
                        ps.setInt(1, studentId);
                        ps.setInt(2, teacherId);
                        ps.setString(3, subject.trim());
                        ps.setDate(4, Date.valueOf(dateStr));
                        ps.setString(5, statusVal);
                        ps.addBatch();

                        // Notify if absent
                        if ("ABSENT".equalsIgnoreCase(statusVal)) {
                            try (PreparedStatement notifPs = con.prepareStatement(
                                    "INSERT INTO notifications (user_id, title, message) VALUES ((SELECT user_id FROM students WHERE student_id = ?), ?, ?)")) {
                                notifPs.setInt(1, studentId);
                                notifPs.setString(2, "Attendance Marked: Absent");
                                notifPs.setString(3, "You were marked absent in " + subject + " on " + dateStr);
                                notifPs.executeUpdate();
                            }
                        }
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

            logActivity(con, userId, "MARK_ATTENDANCE", "Teacher marked attendance for " + subject + " on " + dateStr);

            response.sendRedirect("AttendanceServlet?success=saved");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AttendanceServlet?error=database");
        }
    }

    private void logActivity(Connection conn, int userId, String action, String description) throws Exception {
        String sql = "INSERT INTO activity_logs (user_id, action, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }
}
