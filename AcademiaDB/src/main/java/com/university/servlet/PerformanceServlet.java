package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
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

@WebServlet("/PerformanceServlet")
public class PerformanceServlet extends HttpServlet {

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

        viewPerformance(request, response);
    }

    private void viewPerformance(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String studentIdParam = request.getParameter("studentId");
        List<Student> students = new ArrayList<>();

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

            // Get students in this department
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT s.student_id, u.first_name, u.last_name FROM students s " +
                    "INNER JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.department = ? " +
                    "ORDER BY u.first_name ASC")) {
                ps.setString(1, teacherDept);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Student s = new Student();
                        s.setStudentId(rs.getInt("student_id"));
                        s.setFirstName(rs.getString("first_name"));
                        s.setLastName(rs.getString("last_name"));
                        students.add(s);
                    }
                }
            }

            int selectedStudentId = 0;
            if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
                selectedStudentId = Integer.parseInt(studentIdParam);
            } else if (!students.isEmpty()) {
                selectedStudentId = students.get(0).getStudentId();
            }

            request.setAttribute("students", students);
            request.setAttribute("selectedStudentId", selectedStudentId);

            if (selectedStudentId > 0) {
                // Fetch student metadata
                Student student = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT s.student_id, u.first_name, u.last_name, u.username, s.semester, s.department " +
                        "FROM students s INNER JOIN users u ON s.user_id = u.user_id WHERE s.student_id = ?")) {
                    ps.setInt(1, selectedStudentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            student = new Student();
                            student.setStudentId(rs.getInt("student_id"));
                            student.setFirstName(rs.getString("first_name"));
                            student.setLastName(rs.getString("last_name"));
                            student.setUsername(rs.getString("username"));
                            student.setSemester(rs.getString("semester"));
                            student.setDepartment(rs.getString("department"));
                        }
                    }
                }
                request.setAttribute("selectedStudent", student);

                // Calculations
                // 1. Attendance percentage
                double attendanceRate = 100.0;
                int totalClasses = 0;
                int presentClasses = 0;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*), SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) FROM attendance WHERE student_id = ?")) {
                    ps.setInt(1, selectedStudentId);
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

                // 2. Assignment Average Grade
                double assignmentAvg = 0.0;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT AVG(marks_obtained) FROM assignment_submissions sub " +
                        "INNER JOIN assignments a ON sub.assignment_id = a.assignment_id " +
                        "WHERE sub.student_id = ? AND a.teacher_id = ? AND sub.marks_obtained IS NOT NULL")) {
                    ps.setInt(1, selectedStudentId);
                    ps.setInt(2, teacherId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            assignmentAvg = rs.getDouble(1);
                        }
                    }
                }

                // 3. Test average percentage
                double testAvg = 0.0;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT AVG(m.marks_obtained / t.total_marks) * 100 FROM test_marks m " +
                        "INNER JOIN class_tests t ON m.test_id = t.test_id " +
                        "WHERE m.student_id = ? AND t.teacher_id = ?")) {
                    ps.setInt(1, selectedStudentId);
                    ps.setInt(2, teacherId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            testAvg = rs.getDouble(1);
                        }
                    }
                }

                // Overall score calculation
                double divisor = 3;
                double sum = attendanceRate + (assignmentAvg > 0 ? assignmentAvg : 100.0) + (testAvg > 0 ? testAvg : 100.0);
                double overall = sum / divisor;

                String performanceRating = "GOOD";
                if (overall < 70.0) {
                    performanceRating = "CRITICAL";
                } else if (overall < 85.0) {
                    performanceRating = "WARNING";
                }

                request.setAttribute("attendanceRate", String.format("%.1f", attendanceRate));
                request.setAttribute("assignmentAvg", String.format("%.1f", assignmentAvg));
                request.setAttribute("testAvg", String.format("%.1f", testAvg));
                request.setAttribute("overallScore", String.format("%.1f", overall));
                request.setAttribute("performanceRating", performanceRating);
            }

            request.setAttribute("activePage", "performance");
            request.getRequestDispatcher("performance.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherServlet?error=database");
        }
    }
}
