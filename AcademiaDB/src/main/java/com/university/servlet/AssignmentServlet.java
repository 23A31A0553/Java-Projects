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
import com.university.model.Assignment;

@WebServlet("/AssignmentServlet")
public class AssignmentServlet extends HttpServlet {

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

        listAssignments(request, response);
    }

    private void listAssignments(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<Assignment> assignments = new ArrayList<>();

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

            // Get assignments created by this teacher
            String sql = "SELECT * FROM assignments WHERE teacher_id = ? ORDER BY assignment_id DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Assignment assignment = new Assignment();
                        assignment.setAssignmentId(rs.getInt("assignment_id"));
                        assignment.setTeacherId(rs.getInt("teacher_id"));
                        assignment.setSubject(rs.getString("subject"));
                        assignment.setUnit(rs.getString("unit"));
                        assignment.setTitle(rs.getString("title"));
                        assignment.setDescription(rs.getString("description"));
                        assignment.setCreatedDate(rs.getTimestamp("created_date"));
                        assignment.setDueDate(rs.getDate("due_date"));
                        assignment.setStatus(rs.getString("status"));
                        assignments.add(assignment);
                    }
                }
            }

            request.setAttribute("assignments", assignments);
            request.setAttribute("activePage", "assignments");
            request.getRequestDispatcher("assignments.jsp").forward(request, response);

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
            response.sendRedirect("AssignmentServlet?error=invalid");
            return;
        }

        int assignmentId = Integer.parseInt(idParam);

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

            // Get assignment detail ensuring owner matches
            Assignment assignment = null;
            String sql = "SELECT * FROM assignments WHERE assignment_id = ? AND teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        assignment = new Assignment();
                        assignment.setAssignmentId(rs.getInt("assignment_id"));
                        assignment.setTeacherId(rs.getInt("teacher_id"));
                        assignment.setSubject(rs.getString("subject"));
                        assignment.setUnit(rs.getString("unit"));
                        assignment.setTitle(rs.getString("title"));
                        assignment.setDescription(rs.getString("description"));
                        assignment.setCreatedDate(rs.getTimestamp("created_date"));
                        assignment.setDueDate(rs.getDate("due_date"));
                        assignment.setStatus(rs.getString("status"));
                    }
                }
            }

            if (assignment == null) {
                response.sendRedirect("AssignmentServlet?error=notfound");
                return;
            }

            request.setAttribute("assignment", assignment);
            request.setAttribute("activePage", "assignments");
            request.getRequestDispatcher("assignments.jsp?action=edit").forward(request, response);

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
        if ("add".equalsIgnoreCase(action)) {
            addAssignment(request, response);
        } else if ("update".equalsIgnoreCase(action)) {
            updateAssignment(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            deleteAssignment(request, response);
        } else {
            response.sendRedirect("AssignmentServlet");
        }
    }

    private void addAssignment(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String subject = request.getParameter("subject");
        String unit = request.getParameter("unit");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDateStr = request.getParameter("dueDate");

        if (subject == null || subject.trim().isEmpty() ||
            unit == null || unit.trim().isEmpty() ||
            title == null || title.trim().isEmpty() ||
            dueDateStr == null || dueDateStr.trim().isEmpty()) {
            response.sendRedirect("AssignmentServlet?error=empty");
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

            String sql = "INSERT INTO assignments (teacher_id, subject, unit, title, description, due_date, status) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, teacherId);
                ps.setString(2, subject.trim());
                ps.setString(3, unit.trim());
                ps.setString(4, title.trim());
                ps.setString(5, description != null ? description.trim() : "");
                ps.setDate(6, Date.valueOf(dueDateStr));
                ps.executeUpdate();
            }

            // Create notification for all students in teacher's department
            String notificationSql = 
                "INSERT INTO notifications (user_id, title, message) " +
                "SELECT u.user_id, ?, ? FROM users u " +
                "INNER JOIN students s ON u.user_id = s.user_id " +
                "WHERE s.department = (SELECT department FROM teachers WHERE teacher_id = ?)";
            try (PreparedStatement ps = con.prepareStatement(notificationSql)) {
                ps.setString(1, "New Assignment: " + title);
                ps.setString(2, "Subject: " + subject + ", Unit: " + unit + ". Due date: " + dueDateStr);
                ps.setInt(3, teacherId);
                ps.executeUpdate();
            }

            logActivity(con, userId, "ADD_ASSIGNMENT", "Teacher created assignment: " + title);

            response.sendRedirect("AssignmentServlet?success=added");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AssignmentServlet?error=database");
        }
    }

    private void updateAssignment(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String idParam = request.getParameter("assignmentId");
        String subject = request.getParameter("subject");
        String unit = request.getParameter("unit");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDateStr = request.getParameter("dueDate");
        String status = request.getParameter("status");

        if (idParam == null || idParam.trim().isEmpty() ||
            subject == null || subject.trim().isEmpty() ||
            unit == null || unit.trim().isEmpty() ||
            title == null || title.trim().isEmpty() ||
            dueDateStr == null || dueDateStr.trim().isEmpty()) {
            response.sendRedirect("AssignmentServlet?error=empty");
            return;
        }

        int assignmentId = Integer.parseInt(idParam);

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

            String sql = "UPDATE assignments SET subject = ?, unit = ?, title = ?, description = ?, due_date = ?, status = ? WHERE assignment_id = ? AND teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, subject.trim());
                ps.setString(2, unit.trim());
                ps.setString(3, title.trim());
                ps.setString(4, description != null ? description.trim() : "");
                ps.setDate(5, Date.valueOf(dueDateStr));
                ps.setString(6, status);
                ps.setInt(7, assignmentId);
                ps.setInt(8, teacherId);
                ps.executeUpdate();
            }

            logActivity(con, userId, "UPDATE_ASSIGNMENT", "Teacher updated assignment ID: " + assignmentId);

            response.sendRedirect("AssignmentServlet?success=updated");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AssignmentServlet?error=database");
        }
    }

    private void deleteAssignment(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String idParam = request.getParameter("assignmentId");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("AssignmentServlet?error=invalid");
            return;
        }

        int assignmentId = Integer.parseInt(idParam);

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

            String sql = "DELETE FROM assignments WHERE assignment_id = ? AND teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, teacherId);
                ps.executeUpdate();
            }

            logActivity(con, userId, "DELETE_ASSIGNMENT", "Teacher deleted assignment ID: " + assignmentId);

            response.sendRedirect("AssignmentServlet?success=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AssignmentServlet?error=database");
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
