package com.university.servlet;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import com.university.db.DBConnection;

@WebServlet("/StudentAssignmentSubmissionServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class StudentAssignmentSubmissionServlet extends HttpServlet {

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

        String assignmentIdStr = request.getParameter("assignmentId");
        if (assignmentIdStr == null || assignmentIdStr.trim().isEmpty()) {
            response.sendRedirect("StudentAssignmentServlet?error=invalid");
            return;
        }

        int assignmentId = Integer.parseInt(assignmentIdStr);
        int userId = (Integer) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {
            // Get student core info
            int studentId = 0;
            String department = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT student_id, department FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        department = rs.getString("department");
                    }
                }
            }

            // Fetch assignment detail ensuring department matches
            String sql = 
                "SELECT a.assignment_id, a.subject, a.unit, a.title, a.description, a.due_date, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                "FROM assignments a " +
                "INNER JOIN teachers t ON a.teacher_id = t.teacher_id " +
                "INNER JOIN users u ON t.user_id = u.user_id " +
                "WHERE a.assignment_id = ? AND t.department = ?";
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, assignmentId);
                ps.setString(2, department);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        request.setAttribute("assignmentId", rs.getInt("assignment_id"));
                        request.setAttribute("subject", rs.getString("subject"));
                        request.setAttribute("unit", rs.getString("unit"));
                        request.setAttribute("title", rs.getString("title"));
                        request.setAttribute("description", rs.getString("description"));
                        request.setAttribute("dueDate", rs.getDate("due_date"));
                        request.setAttribute("teacherName", rs.getString("teacher_name"));
                    } else {
                        response.sendRedirect("StudentAssignmentServlet?error=unauthorized");
                        return;
                    }
                }
            }

            request.setAttribute("activePage", "assignments");
            request.getRequestDispatcher("assignmentSubmission.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentAssignmentServlet?error=database");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"STUDENT".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String assignmentIdStr = request.getParameter("assignmentId");
        if (assignmentIdStr == null || assignmentIdStr.trim().isEmpty()) {
            response.sendRedirect("StudentAssignmentServlet?error=invalid");
            return;
        }

        int assignmentId = Integer.parseInt(assignmentIdStr);
        Part filePart = request.getPart("file");

        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect("StudentAssignmentSubmissionServlet?assignmentId=" + assignmentId + "&error=nofile");
            return;
        }

        String rawFileName = getFileName(filePart);
        String fileExtension = getFileExtension(rawFileName);

        // Security check: Only allow PDF, DOC, DOCX, ZIP, PPT, PPTX
        String allowedExtensions = "pdf,doc,docx,zip,ppt,pptx";
        if (fileExtension == null || !allowedExtensions.contains(fileExtension.toLowerCase())) {
            response.sendRedirect("StudentAssignmentSubmissionServlet?assignmentId=" + assignmentId + "&error=invalidtype");
            return;
        }

        // Limit size: 10MB
        if (filePart.getSize() > 10 * 1024 * 1024) {
            response.sendRedirect("StudentAssignmentSubmissionServlet?assignmentId=" + assignmentId + "&error=too-large");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            
            // Get student ID and student name for naming the file uniquely
            int studentId = 0;
            String studentName = "";
            try (PreparedStatement ps = con.prepareStatement(
                "SELECT s.student_id, CONCAT(u.first_name, '_', u.last_name) AS full_name " +
                "FROM students s INNER JOIN users u ON s.user_id = u.user_id WHERE s.user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        studentName = rs.getString("full_name");
                    }
                }
            }

            // Create target folders
            String contextPath = request.getServletContext().getRealPath("");
            String uploadSubdir = "uploads" + File.separator + "submissions";
            String uploadPath = contextPath + File.separator + uploadSubdir;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Construct unique file name to avoid collisions
            String targetFileName = studentName + "_Assign_" + assignmentId + "_" + System.currentTimeMillis() + "." + fileExtension;
            String targetFilePath = uploadPath + File.separator + targetFileName;
            String dbRelativePath = uploadSubdir + File.separator + targetFileName;

            // Save file
            filePart.write(targetFilePath);

            // Check if submission already exists to decide INSERT or UPDATE
            int existingSubmissionId = 0;
            String checkSql = "SELECT submission_id FROM assignment_submissions WHERE assignment_id = ? AND student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingSubmissionId = rs.getInt("submission_id");
                    }
                }
            }

            // Check due date vs current date to set submission status
            java.sql.Date dueDate = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT due_date FROM assignments WHERE assignment_id = ?")) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dueDate = rs.getDate("due_date");
                    }
                }
            }

            String submissionStatus = "SUBMITTED";
            if (dueDate != null && dueDate.before(new java.util.Date())) {
                submissionStatus = "LATE";
            }

            if (existingSubmissionId > 0) {
                // Update existing submission
                String updateSql = "UPDATE assignment_submissions SET submission_date = CURRENT_TIMESTAMP, status = ?, file_name = ?, file_path = ? WHERE submission_id = ?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, submissionStatus);
                    ps.setString(2, targetFileName);
                    ps.setString(3, dbRelativePath);
                    ps.setInt(4, existingSubmissionId);
                    ps.executeUpdate();
                }
            } else {
                // Create new submission
                String insertSql = "INSERT INTO assignment_submissions (assignment_id, student_id, submission_date, status, file_name, file_path) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, assignmentId);
                    ps.setInt(2, studentId);
                    ps.setString(3, submissionStatus);
                    ps.setString(4, targetFileName);
                    ps.setString(5, dbRelativePath);
                    ps.executeUpdate();
                }
            }

            // Log activity log
            logActivity(con, userId, "SUBMIT_ASSIGNMENT", "Student submitted assignment ID: " + assignmentId);

            response.sendRedirect("StudentAssignmentServlet?success=submitted");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentAssignmentSubmissionServlet?assignmentId=" + assignmentId + "&error=database");
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String cd : contentDisposition.split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return null;
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return null;
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
