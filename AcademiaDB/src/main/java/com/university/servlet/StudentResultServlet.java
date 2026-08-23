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

@WebServlet("/StudentResultServlet")
public class StudentResultServlet extends HttpServlet {

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
        List<Map<String, Object>> resultsList = new ArrayList<>();
        double semesterTotal = 0.0;
        int subjectCount = 0;

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

            // Retrieve subjects mapping to this dept and sem
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

            // If no subjects mapped in database yet, fallback
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

            // Compute performance grade for each subject
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

                // Calculate final weighted score: 40% Assignments, 60% Tests
                double finalScore = 0.0;
                if (assignmentCount > 0 && testCount > 0) {
                    finalScore = (assignmentAvg * 0.4) + (testAvg * 0.6);
                } else if (assignmentCount > 0) {
                    finalScore = assignmentAvg;
                } else if (testCount > 0) {
                    finalScore = testAvg;
                } else {
                    finalScore = 0.0; // Graded components not found
                }

                // Calculate Grade Letters
                String grade = "F";
                if (finalScore >= 90.0) grade = "A+";
                else if (finalScore >= 80.0) grade = "A";
                else if (finalScore >= 70.0) grade = "B+";
                else if (finalScore >= 60.0) grade = "B";
                else if (finalScore >= 50.0) grade = "C";
                else if (finalScore >= 40.0) grade = "D";
                else grade = "F";

                map.put("score", finalScore);
                map.put("grade", (assignmentCount == 0 && testCount == 0) ? "Pending" : grade);

                resultsList.add(map);

                if (assignmentCount > 0 || testCount > 0) {
                    semesterTotal += finalScore;
                    subjectCount++;
                }
            }

            double semesterAverage = subjectCount > 0 ? semesterTotal / subjectCount : 0.0;
            request.setAttribute("semesterAverage", String.format("%.1f", semesterAverage));
            request.setAttribute("resultsList", resultsList);
            request.setAttribute("semester", semester);

            request.setAttribute("activePage", "results");
            request.getRequestDispatcher("results.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }
}
