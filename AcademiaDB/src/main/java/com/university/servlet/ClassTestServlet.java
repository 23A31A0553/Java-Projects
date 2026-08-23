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
import com.university.model.ClassTest;

@WebServlet("/ClassTestServlet")
public class ClassTestServlet extends HttpServlet {

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
        if ("editForm".equalsIgnoreCase(action)) {
            showEditForm(request, response);
            return;
        }

        listTests(request, response);
    }

    private void listTests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<ClassTest> tests = new ArrayList<>();

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

            // Get tests created by this teacher
            String sql = "SELECT * FROM class_tests WHERE teacher_id = ? ORDER BY test_date DESC, test_id DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ClassTest test = new ClassTest();
                        test.setTestId(rs.getInt("test_id"));
                        test.setTeacherId(rs.getInt("teacher_id"));
                        test.setSubject(rs.getString("subject"));
                        test.setUnit(rs.getString("unit"));
                        test.setTestTitle(rs.getString("test_title"));
                        test.setTestDate(rs.getDate("test_date"));
                        test.setTotalMarks(rs.getInt("total_marks"));
                        test.setDescription(rs.getString("description"));
                        tests.add(test);
                    }
                }
            }

            request.setAttribute("classTests", tests);
            request.setAttribute("activePage", "classTests");
            request.getRequestDispatcher("classTests.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherServlet?error=database");
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("ClassTestServlet?error=invalid");
            return;
        }

        int testId = Integer.parseInt(idParam);

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

            // Get test detail
            ClassTest test = null;
            String sql = "SELECT * FROM class_tests WHERE test_id = ? AND teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, testId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        test = new ClassTest();
                        test.setTestId(rs.getInt("test_id"));
                        test.setTeacherId(rs.getInt("teacher_id"));
                        test.setSubject(rs.getString("subject"));
                        test.setUnit(rs.getString("unit"));
                        test.setTestTitle(rs.getString("test_title"));
                        test.setTestDate(rs.getDate("test_date"));
                        test.setTotalMarks(rs.getInt("total_marks"));
                        test.setDescription(rs.getString("description"));
                    }
                }
            }

            if (test == null) {
                response.sendRedirect("ClassTestServlet?error=notfound");
                return;
            }

            request.setAttribute("test", test);
            request.setAttribute("activePage", "classTests");
            request.getRequestDispatcher("classTests.jsp?action=edit").forward(request, response);

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

        String action = request.getParameter("action");
        if ("add".equalsIgnoreCase(action)) {
            addTest(request, response);
        } else if ("update".equalsIgnoreCase(action)) {
            updateTest(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            deleteTest(request, response);
        } else {
            response.sendRedirect("ClassTestServlet");
        }
    }

    private void addTest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String subject = request.getParameter("subject");
        String unit = request.getParameter("unit");
        String title = request.getParameter("title");
        String testDateStr = request.getParameter("testDate");
        String totalMarksStr = request.getParameter("totalMarks");
        String description = request.getParameter("description");

        if (subject == null || subject.trim().isEmpty() ||
            unit == null || unit.trim().isEmpty() ||
            title == null || title.trim().isEmpty() ||
            testDateStr == null || testDateStr.trim().isEmpty() ||
            totalMarksStr == null || totalMarksStr.trim().isEmpty()) {
            response.sendRedirect("ClassTestServlet?error=empty");
            return;
        }

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

            int totalMarks = Integer.parseInt(totalMarksStr);
            if (totalMarks <= 0) {
                response.sendRedirect("ClassTestServlet?error=invalidmarks");
                return;
            }

            String sql = "INSERT INTO class_tests (teacher_id, subject, unit, test_title, test_date, total_marks, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int testId = 0;
            try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, teacherId);
                ps.setString(2, subject.trim());
                ps.setString(3, unit.trim());
                ps.setString(4, title.trim());
                ps.setDate(5, Date.valueOf(testDateStr));
                ps.setInt(6, totalMarks);
                ps.setString(7, description != null ? description.trim() : "");
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        testId = rs.getInt(1);
                    }
                }
            }

            // Create notification for all students in teacher's department
            String notificationSql = 
                "INSERT INTO notifications (user_id, title, message) " +
                "SELECT u.user_id, ?, ? FROM users u " +
                "INNER JOIN students s ON u.user_id = s.user_id " +
                "WHERE s.department = (SELECT department FROM teachers WHERE teacher_id = ?)";
            try (PreparedStatement ps = con.prepareStatement(notificationSql)) {
                ps.setString(1, "New Class Test: " + title);
                ps.setString(2, "Subject: " + subject + ", Unit: " + unit + ". Scheduled for: " + testDateStr + " (Total marks: " + totalMarks + ")");
                ps.setInt(3, teacherId);
                ps.executeUpdate();
            }

            // Create Calendar Event
            String eventSql = "INSERT INTO calendar_events (teacher_id, title, event_date, event_type, description) VALUES (?, ?, ?, 'TEST', ?)";
            try (PreparedStatement ps = con.prepareStatement(eventSql)) {
                ps.setInt(1, teacherId);
                ps.setString(2, "Class Test: " + title);
                ps.setDate(3, Date.valueOf(testDateStr));
                ps.setString(4, "Subject: " + subject + ", Total marks: " + totalMarks);
                ps.executeUpdate();
            }

            logActivity(con, userId, "ADD_CLASS_TEST", "Teacher created class test: " + title);

            response.sendRedirect("ClassTestServlet?success=added");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ClassTestServlet?error=database");
        }
    }

    private void updateTest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String idParam = request.getParameter("testId");
        String subject = request.getParameter("subject");
        String unit = request.getParameter("unit");
        String title = request.getParameter("title");
        String testDateStr = request.getParameter("testDate");
        String totalMarksStr = request.getParameter("totalMarks");
        String description = request.getParameter("description");

        if (idParam == null || idParam.trim().isEmpty() ||
            subject == null || subject.trim().isEmpty() ||
            unit == null || unit.trim().isEmpty() ||
            title == null || title.trim().isEmpty() ||
            testDateStr == null || testDateStr.trim().isEmpty() ||
            totalMarksStr == null || totalMarksStr.trim().isEmpty()) {
            response.sendRedirect("ClassTestServlet?error=empty");
            return;
        }

        int testId = Integer.parseInt(idParam);
        int totalMarks = Integer.parseInt(totalMarksStr);

        if (totalMarks <= 0) {
            response.sendRedirect("ClassTestServlet?error=invalidmarks");
            return;
        }

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

            String sql = "UPDATE class_tests SET subject = ?, unit = ?, test_title = ?, test_date = ?, total_marks = ?, description = ? WHERE test_id = ? AND teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, subject.trim());
                ps.setString(2, unit.trim());
                ps.setString(3, title.trim());
                ps.setDate(4, Date.valueOf(testDateStr));
                ps.setInt(5, totalMarks);
                ps.setString(6, description != null ? description.trim() : "");
                ps.setInt(7, testId);
                ps.setInt(8, teacherId);
                ps.executeUpdate();
            }

            logActivity(con, userId, "UPDATE_CLASS_TEST", "Teacher updated class test ID: " + testId);

            response.sendRedirect("ClassTestServlet?success=updated");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ClassTestServlet?error=database");
        }
    }

    private void deleteTest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String idParam = request.getParameter("testId");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("ClassTestServlet?error=invalid");
            return;
        }

        int testId = Integer.parseInt(idParam);

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

            String sql = "DELETE FROM class_tests WHERE test_id = ? AND teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, testId);
                ps.setInt(2, teacherId);
                ps.executeUpdate();
            }

            logActivity(con, userId, "DELETE_CLASS_TEST", "Teacher deleted class test ID: " + testId);

            response.sendRedirect("ClassTestServlet?success=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ClassTestServlet?error=database");
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
