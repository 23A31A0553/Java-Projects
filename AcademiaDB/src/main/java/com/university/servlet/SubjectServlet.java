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
import com.university.model.Subject;

@WebServlet("/SubjectServlet")
public class SubjectServlet extends HttpServlet {

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

        String subjectIdParam = request.getParameter("subjectId");
        if (subjectIdParam != null && !subjectIdParam.trim().isEmpty()) {
            viewSubjectDetails(request, response, Integer.parseInt(subjectIdParam));
            return;
        }

        listSubjects(request, response);
    }

    private void listSubjects(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        List<Subject> subjectsList = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            
            // Get student's department and semester
            String dept = "";
            String sem = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT department, semester FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dept = rs.getString("department");
                        sem = rs.getString("semester");
                    }
                }
            }

            // Retrieve subjects mapping to this dept and sem
            String sql = "SELECT s.subject_id, s.subject_code, s.subject_name, s.department, s.semester, s.units, s.teacher_id, " +
                         "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                         "FROM subjects s " +
                         "LEFT JOIN teachers t ON s.teacher_id = t.teacher_id " +
                         "LEFT JOIN users u ON t.user_id = u.user_id " +
                         "WHERE s.department = ? AND s.semester = ? " +
                         "ORDER BY s.subject_name ASC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, dept);
                ps.setString(2, sem);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Subject s = new Subject();
                        s.setSubjectId(rs.getInt("subject_id"));
                        s.setSubjectCode(rs.getString("subject_code"));
                        s.setSubjectName(rs.getString("subject_name"));
                        s.setDepartment(rs.getString("department"));
                        s.setSemester(rs.getString("semester"));
                        s.setUnits(rs.getInt("units"));
                        s.setTeacherId(rs.getInt("teacher_id"));
                        s.setTeacherName(rs.getString("teacher_name") != null ? rs.getString("teacher_name") : "Not Assigned");
                        subjectsList.add(s);
                    }
                }
            }

            request.setAttribute("subjectsList", subjectsList);
            request.setAttribute("activePage", "subjects");
            request.getRequestDispatcher("subjects.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }

    private void viewSubjectDetails(HttpServletRequest request, HttpServletResponse response, int subjectId)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {
            
            // Fetch student core details for department checking
            String studentDept = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT department FROM students WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentDept = rs.getString("department");
                    }
                }
            }

            // Fetch subject details
            Subject s = null;
            String sql = "SELECT s.subject_id, s.subject_code, s.subject_name, s.department, s.semester, s.units, s.teacher_id, " +
                         "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                         "FROM subjects s " +
                         "LEFT JOIN teachers t ON s.teacher_id = t.teacher_id " +
                         "LEFT JOIN users u ON t.user_id = u.user_id " +
                         "WHERE s.subject_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, subjectId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        s = new Subject();
                        s.setSubjectId(rs.getInt("subject_id"));
                        s.setSubjectCode(rs.getString("subject_code"));
                        s.setSubjectName(rs.getString("subject_name"));
                        s.setDepartment(rs.getString("department"));
                        s.setSemester(rs.getString("semester"));
                        s.setUnits(rs.getInt("units"));
                        s.setTeacherId(rs.getInt("teacher_id"));
                        s.setTeacherName(rs.getString("teacher_name") != null ? rs.getString("teacher_name") : "Not Assigned");
                    }
                }
            }

            // Prevent students from viewing subjects belonging to other departments
            if (s == null || !s.getDepartment().equalsIgnoreCase(studentDept)) {
                response.sendRedirect("SubjectServlet?error=unauthorized");
                return;
            }

            // Query stats for this subject
            int assignmentCount = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM assignments WHERE subject = ?")) {
                ps.setString(1, s.getSubjectName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        assignmentCount = rs.getInt(1);
                    }
                }
            }

            int testCount = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM class_tests WHERE subject = ?")) {
                ps.setString(1, s.getSubjectName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        testCount = rs.getInt(1);
                    }
                }
            }

            int materialCount = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM study_materials WHERE subject = ?")) {
                ps.setString(1, s.getSubjectName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        materialCount = rs.getInt(1);
                    }
                }
            }

            request.setAttribute("subject", s);
            request.setAttribute("assignmentCount", assignmentCount);
            request.setAttribute("testCount", testCount);
            request.setAttribute("materialCount", materialCount);
            request.setAttribute("activePage", "subjects");
            request.getRequestDispatcher("subjects.jsp?action=details").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("SubjectServlet?error=database");
        }
    }
}
