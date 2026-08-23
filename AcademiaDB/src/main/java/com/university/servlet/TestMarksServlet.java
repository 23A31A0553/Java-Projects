package com.university.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.university.db.DBConnection;
import com.university.model.TestMark;

@WebServlet("/TestMarksServlet")
public class TestMarksServlet extends HttpServlet {

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

        String testIdParam = request.getParameter("testId");
        if (testIdParam == null || testIdParam.trim().isEmpty()) {
            response.sendRedirect("ClassTestServlet?error=invalid");
            return;
        }

        int testId = Integer.parseInt(testIdParam);
        showMarksForm(request, response, testId);
    }

    private void showMarksForm(HttpServletRequest request, HttpServletResponse response, int testId) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<TestMark> testMarks = new ArrayList<>();
        String testTitle = "";
        int totalMarks = 0;
        String subject = "";
        String unit = "";

        try (Connection con = DBConnection.getConnection()) {
            // Get teacher department and teacherId
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

            // Verify test details and ownership
            try (PreparedStatement ps = con.prepareStatement("SELECT test_title, total_marks, subject, unit FROM class_tests WHERE test_id = ? AND teacher_id = ?")) {
                ps.setInt(1, testId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        testTitle = rs.getString("test_title");
                        totalMarks = rs.getInt("total_marks");
                        subject = rs.getString("subject");
                        unit = rs.getString("unit");
                    } else {
                        response.sendRedirect("ClassTestServlet?error=notfound");
                        return;
                    }
                }
            }

            // Load students from teacher's department and outer-join their test marks
            String sql = 
                "SELECT s.student_id, u.username, u.first_name, u.last_name, " +
                "m.mark_id, m.marks_obtained " +
                "FROM students s " +
                "INNER JOIN users u ON s.user_id = u.user_id " +
                "LEFT JOIN test_marks m ON s.student_id = m.student_id AND m.test_id = ? " +
                "WHERE s.department = ? " +
                "ORDER BY u.first_name ASC, u.last_name ASC";
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, testId);
                ps.setString(2, teacherDept);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TestMark mark = new TestMark();
                        mark.setTestId(testId);
                        mark.setStudentId(rs.getInt("student_id"));
                        mark.setStudentFirstName(rs.getString("first_name"));
                        mark.setStudentLastName(rs.getString("last_name"));
                        mark.setStudentUsername(rs.getString("username"));
                        mark.setTestTitle(testTitle);
                        mark.setTotalMarks(totalMarks);

                        int markId = rs.getInt("mark_id");
                        if (markId > 0) {
                            mark.setMarkId(markId);
                            mark.setMarksObtained(rs.getDouble("marks_obtained"));
                        } else {
                            mark.setMarkId(0);
                            mark.setMarksObtained(-1); // Indicator for no marks entered yet
                        }
                        testMarks.add(mark);
                    }
                }
            }

            // Calculate test result stats
            double highest = 0;
            double lowest = totalMarks;
            double sum = 0;
            int passCount = 0;
            int failCount = 0;
            int gradedStudents = 0;

            for (TestMark m : testMarks) {
                if (m.getMarkId() > 0) {
                    gradedStudents++;
                    double score = m.getMarksObtained();
                    sum += score;
                    if (score > highest) highest = score;
                    if (score < lowest) lowest = score;
                    
                    // Pass threshold: 40% of total marks
                    if (score >= (totalMarks * 0.40)) {
                        passCount++;
                    } else {
                        failCount++;
                    }
                }
            }

            if (gradedStudents == 0) {
                lowest = 0;
            }

            double average = gradedStudents > 0 ? (sum / gradedStudents) : 0.0;

            request.setAttribute("testMarks", testMarks);
            request.setAttribute("testId", testId);
            request.setAttribute("testTitle", testTitle);
            request.setAttribute("testSubject", subject);
            request.setAttribute("testUnit", unit);
            request.setAttribute("totalMarks", totalMarks);
            
            request.setAttribute("gradedStudents", gradedStudents);
            request.setAttribute("highestMark", String.format("%.2f", highest));
            request.setAttribute("lowestMark", String.format("%.2f", lowest));
            request.setAttribute("averageMark", String.format("%.2f", average));
            request.setAttribute("passCount", passCount);
            request.setAttribute("failCount", failCount);
            
            request.setAttribute("activePage", "classTests");
            request.getRequestDispatcher("testMarks.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ClassTestServlet?error=database");
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

        String testIdParam = request.getParameter("testId");
        if (testIdParam == null || testIdParam.trim().isEmpty()) {
            response.sendRedirect("ClassTestServlet?error=invalid");
            return;
        }

        int testId = Integer.parseInt(testIdParam);
        saveMarks(request, response, testId);
    }

    private void saveMarks(HttpServletRequest request, HttpServletResponse response, int testId) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

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

            // Get total marks
            int totalMarks = 0;
            String testTitle = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT total_marks, test_title FROM class_tests WHERE test_id = ? AND teacher_id = ?")) {
                ps.setInt(1, testId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalMarks = rs.getInt("total_marks");
                        testTitle = rs.getString("test_title");
                    } else {
                        response.sendRedirect("ClassTestServlet?error=notfound");
                        return;
                    }
                }
            }

            // Find all students in teacher's department to grade them
            List<Integer> studentIds = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement("SELECT student_id FROM students WHERE department = ?")) {
                ps.setString(1, teacherDept);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        studentIds.add(rs.getInt("student_id"));
                    }
                }
            }

            con.setAutoCommit(false);
            try {
                String upsertSql = "INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (?, ?, ?) " +
                                   "ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)";
                
                try (PreparedStatement ps = con.prepareStatement(upsertSql)) {
                    for (int studentId : studentIds) {
                        String markInput = request.getParameter("marks_" + studentId);
                        if (markInput != null && !markInput.trim().isEmpty()) {
                            double mark = Double.parseDouble(markInput.trim());
                            if (mark < 0 || mark > totalMarks) {
                                con.rollback();
                                response.sendRedirect("TestMarksServlet?testId=" + testId + "&error=invalidrange");
                                return;
                            }
                            ps.setInt(1, testId);
                            ps.setInt(2, studentId);
                            ps.setDouble(3, mark);
                            ps.addBatch();
                            
                            // Send notification to student dynamically inside the loop
                            try (PreparedStatement notifPs = con.prepareStatement(
                                    "INSERT INTO notifications (user_id, title, message) VALUES ((SELECT user_id FROM students WHERE student_id = ?), ?, ?)")) {
                                notifPs.setInt(1, studentId);
                                notifPs.setString(2, "Test Marks Declared");
                                notifPs.setString(3, "Marks for " + testTitle + " are declared. You obtained " + mark + "/" + totalMarks);
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

            logActivity(con, userId, "ENTER_TEST_MARKS", "Teacher recorded marks for test ID: " + testId);

            response.sendRedirect("TestMarksServlet?testId=" + testId + "&success=saved");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TestMarksServlet?testId=" + testId + "&error=database");
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
