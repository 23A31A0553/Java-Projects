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

@WebServlet("/StudentPerformanceServlet")
public class StudentPerformanceServlet extends HttpServlet {

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
        List<Map<String, Object>> performanceList = new ArrayList<>();

        double totalAssignmentAvgSum = 0.0;
        int totalAssignmentAvgCount = 0;

        double totalTestAvgSum = 0.0;
        int totalTestAvgCount = 0;

        double totalAttendanceSum = 0.0;
        int totalAttendanceCount = 0;

        try (Connection con = DBConnection.getConnection()) {
            
            // Get student core info
            int studentId = 0;
            String department = "";
            String semester = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT student_id, department, semester FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        department = rs.getString("department");
                        semester = rs.getString("semester");
                    }
                }
            }

            // Fetch subjects for this student
            List<String> subjects = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement("SELECT subject_name FROM subjects WHERE department = ? AND semester = ?")) {
                ps.setString(1, department);
                ps.setString(2, semester);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subjects.add(rs.getString("subject_name"));
                    }
                }
            }

            // If no subjects mapped in database yet, fallback to subjects from active attendance/assignments logs
            if (subjects.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(
                    "SELECT DISTINCT subject FROM attendance WHERE student_id = ? " +
                    "UNION " +
                    "SELECT DISTINCT subject FROM assignments a INNER JOIN assignment_submissions sub ON a.assignment_id = sub.assignment_id WHERE sub.student_id = ?")) {
                    ps.setInt(1, studentId);
                    ps.setInt(2, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            subjects.add(rs.getString(1));
                        }
                    }
                }
            }

            // Calculate metrics for each subject
            for (String sub : subjects) {
                Map<String, Object> map = new HashMap<>();
                map.put("subject", sub);

                // 1. Assignment Average for this subject
                double assignmentAvg = 0.0;
                int assignmentCount = 0;
                String assignSql = 
                    "SELECT COUNT(*), AVG(sub.marks_obtained) FROM assignment_submissions sub " +
                    "INNER JOIN assignments a ON sub.assignment_id = a.assignment_id " +
                    "WHERE sub.student_id = ? AND a.subject = ? AND sub.marks_obtained IS NOT NULL";
                try (PreparedStatement ps = con.prepareStatement(assignSql)) {
                    ps.setInt(1, studentId);
                    ps.setString(2, sub);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            assignmentCount = rs.getInt(1);
                            assignmentAvg = rs.getDouble(2);
                        }
                    }
                }
                if (assignmentCount > 0) {
                    map.put("assignmentAvg", String.format("%.1f", assignmentAvg));
                    totalAssignmentAvgSum += assignmentAvg;
                    totalAssignmentAvgCount++;
                } else {
                    map.put("assignmentAvg", "-");
                }

                // 2. Test Average for this subject
                double testAvg = 0.0;
                int testCount = 0;
                String testSql = 
                    "SELECT COUNT(*), AVG(m.marks_obtained / ct.total_marks) * 100 FROM test_marks m " +
                    "INNER JOIN class_tests ct ON m.test_id = ct.test_id " +
                    "WHERE m.student_id = ? AND ct.subject = ?";
                try (PreparedStatement ps = con.prepareStatement(testSql)) {
                    ps.setInt(1, studentId);
                    ps.setString(2, sub);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            testCount = rs.getInt(1);
                            testAvg = rs.getDouble(2);
                        }
                    }
                }
                if (testCount > 0) {
                    map.put("testAvg", String.format("%.1f", testAvg));
                    totalTestAvgSum += testAvg;
                    totalTestAvgCount++;
                } else {
                    map.put("testAvg", "-");
                }

                // 3. Attendance Rate for this subject
                double attendanceRate = 100.0;
                int totalClasses = 0;
                int presentClasses = 0;
                String attSql = "SELECT COUNT(*), SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) FROM attendance WHERE student_id = ? AND subject = ?";
                try (PreparedStatement ps = con.prepareStatement(attSql)) {
                    ps.setInt(1, studentId);
                    ps.setString(2, sub);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            totalClasses = rs.getInt(1);
                            presentClasses = rs.getInt(2);
                            if (totalClasses > 0) {
                                attendanceRate = ((double) presentClasses / totalClasses) * 100;
                            }
                        }
                    }
                }
                if (totalClasses > 0) {
                    map.put("attendanceRate", String.format("%.1f", attendanceRate));
                    totalAttendanceSum += attendanceRate;
                    totalAttendanceCount++;
                } else {
                    map.put("attendanceRate", "-");
                }

                // 4. Overall score for subject (average of available metrics)
                double sum = 0.0;
                int count = 0;
                if (assignmentCount > 0) { sum += assignmentAvg; count++; }
                if (testCount > 0) { sum += testAvg; count++; }
                if (totalClasses > 0) { sum += attendanceRate; count++; }

                double overallSubjectScore = count > 0 ? sum / count : 100.0;
                map.put("overallScore", String.format("%.1f", overallSubjectScore));

                performanceList.add(map);
            }

            // Calculate overall consolidated averages
            double overallAssignment = totalAssignmentAvgCount > 0 ? totalAssignmentAvgSum / totalAssignmentAvgCount : 100.0;
            double overallTest = totalTestAvgCount > 0 ? totalTestAvgSum / totalTestAvgCount : 100.0;
            double overallAttendance = totalAttendanceCount > 0 ? totalAttendanceSum / totalAttendanceCount : 100.0;
            double overallScore = (overallAssignment + overallTest + overallAttendance) / 3.0;

            request.setAttribute("overallAssignment", String.format("%.1f", overallAssignment));
            request.setAttribute("overallTest", String.format("%.1f", overallTest));
            request.setAttribute("overallAttendance", String.format("%.1f", overallAttendance));
            request.setAttribute("overallScore", String.format("%.1f", overallScore));
            request.setAttribute("performanceList", performanceList);

            request.setAttribute("activePage", "performance");
            request.getRequestDispatcher("performance.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
