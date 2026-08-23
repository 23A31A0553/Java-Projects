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
import com.university.model.AssignmentSubmission;

@WebServlet("/AssignmentSubmissionServlet")
public class AssignmentSubmissionServlet extends HttpServlet {

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

        String idParam = request.getParameter("assignmentId");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("AssignmentServlet?error=invalid");
            return;
        }

        int assignmentId = Integer.parseInt(idParam);
        listSubmissions(request, response, assignmentId);
    }

    private void listSubmissions(HttpServletRequest request, HttpServletResponse response, int assignmentId) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<AssignmentSubmission> submissions = new ArrayList<>();
        String assignmentTitle = "";
        String subject = "";
        String unit = "";

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

            // Verify assignment ownership and get title
            try (PreparedStatement ps = con.prepareStatement("SELECT title, subject, unit FROM assignments WHERE assignment_id = ? AND teacher_id = ?")) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        assignmentTitle = rs.getString("title");
                        subject = rs.getString("subject");
                        unit = rs.getString("unit");
                    } else {
                        response.sendRedirect("AssignmentServlet?error=notfound");
                        return;
                    }
                }
            }

            // Fetch all students in the teacher's department and outer-join their submissions for this assignment
            String sql = 
                "SELECT s.student_id, u.username, u.first_name, u.last_name, " +
                "sub.submission_id, sub.submission_date, sub.status, sub.file_name, sub.file_path, sub.marks_obtained " +
                "FROM students s " +
                "INNER JOIN users u ON s.user_id = u.user_id " +
                "LEFT JOIN assignment_submissions sub ON s.student_id = sub.student_id AND sub.assignment_id = ? " +
                "WHERE s.department = ? " +
                "ORDER BY u.first_name ASC, u.last_name ASC";
            
            int total = 0;
            int submitted = 0;
            int pending = 0;
            int late = 0;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, assignmentId);
                ps.setString(2, teacherDept);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        total++;
                        AssignmentSubmission sub = new AssignmentSubmission();
                        sub.setAssignmentId(assignmentId);
                        sub.setStudentId(rs.getInt("student_id"));
                        sub.setStudentFirstName(rs.getString("first_name"));
                        sub.setStudentLastName(rs.getString("last_name"));
                        sub.setStudentUsername(rs.getString("username"));
                        
                        int subId = rs.getInt("submission_id");
                        if (subId > 0) {
                            sub.setSubmissionId(subId);
                            sub.setSubmissionDate(rs.getTimestamp("submission_date"));
                            sub.setStatus(rs.getString("status"));
                            sub.setFileName(rs.getString("file_name"));
                            sub.setFilePath(rs.getString("file_path"));
                            
                            double marks = rs.getDouble("marks_obtained");
                            if (!rs.wasNull()) {
                                sub.setMarksObtained(marks);
                            }
                            
                            submitted++;
                            if ("LATE".equalsIgnoreCase(rs.getString("status"))) {
                                late++;
                            }
                        } else {
                            sub.setStatus("PENDING");
                            pending++;
                        }
                        
                        submissions.add(sub);
                    }
                }
            }

            request.setAttribute("submissions", submissions);
            request.setAttribute("assignmentId", assignmentId);
            request.setAttribute("assignmentTitle", assignmentTitle);
            request.setAttribute("assignmentSubject", subject);
            request.setAttribute("assignmentUnit", unit);
            request.setAttribute("totalCount", total);
            request.setAttribute("submittedCount", submitted);
            request.setAttribute("pendingCount", pending);
            request.setAttribute("lateCount", late);
            request.setAttribute("activePage", "assignments");
            
            request.getRequestDispatcher("assignmentSubmissions.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AssignmentServlet?error=database");
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
        if ("grade".equalsIgnoreCase(action)) {
            gradeSubmission(request, response);
        } else {
            response.sendRedirect("AssignmentServlet");
        }
    }

    private void gradeSubmission(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String assignmentIdStr = request.getParameter("assignmentId");
        String studentIdStr = request.getParameter("studentId");
        String marksStr = request.getParameter("marks");

        if (assignmentIdStr == null || studentIdStr == null || marksStr == null) {
            response.sendRedirect("AssignmentServlet?error=invalid");
            return;
        }

        int assignmentId = Integer.parseInt(assignmentIdStr);
        int studentId = Integer.parseInt(studentIdStr);
        double marks = Double.parseDouble(marksStr);

        if (marks < 0) {
            response.sendRedirect("AssignmentSubmissionServlet?assignmentId=" + assignmentId + "&error=negativemarks");
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

            // Check if submission record exists
            int submissionId = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT submission_id FROM assignment_submissions WHERE assignment_id = ? AND student_id = ?")) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        submissionId = rs.getInt("submission_id");
                    }
                }
            }

            if (submissionId > 0) {
                // Update existing submission marks
                try (PreparedStatement ps = con.prepareStatement("UPDATE assignment_submissions SET marks_obtained = ? WHERE submission_id = ?")) {
                    ps.setDouble(1, marks);
                    ps.setInt(2, submissionId);
                    ps.executeUpdate();
                }
            } else {
                // Create a graded submission placeholder
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO assignment_submissions (assignment_id, student_id, status, marks_obtained) VALUES (?, ?, 'GRADED', ?)")) {
                    ps.setInt(1, assignmentId);
                    ps.setInt(2, studentId);
                    ps.setDouble(3, marks);
                    ps.executeUpdate();
                }
            }

            // Create notification for student
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO notifications (user_id, title, message) VALUES ((SELECT user_id FROM students WHERE student_id = ?), ?, ?)")) {
                ps.setInt(1, studentId);
                ps.setString(2, "Assignment Graded");
                ps.setString(3, "Your submission has been graded. Marks: " + marks);
                ps.executeUpdate();
            }

            logActivity(con, userId, "GRADE_SUBMISSION", "Teacher graded assignment ID " + assignmentId + " for student ID " + studentId + " with marks: " + marks);

            response.sendRedirect("AssignmentSubmissionServlet?assignmentId=" + assignmentId + "&success=graded");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AssignmentSubmissionServlet?assignmentId=" + assignmentId + "&error=database");
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
