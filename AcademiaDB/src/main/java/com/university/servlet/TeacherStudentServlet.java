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

@WebServlet("/TeacherStudentServlet")
public class TeacherStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }
        String role = String.valueOf(session.getAttribute("role"));
        if (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect("index.jsp?error=unauthorized");
            return;
        }

        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.trim().isEmpty()) {
            int studentId = getStudentIdFromUserId(Integer.parseInt(userIdParam));
            if (studentId > 0) {
                viewStudentDetails(request, response, studentId);
            } else {
                response.sendRedirect("UserManagementServlet?error=notfound");
            }
            return;
        }

        String studentIdParam = request.getParameter("studentId");
        if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
            viewStudentDetails(request, response, Integer.parseInt(studentIdParam));
            return;
        }

        listStudents(request, response);
    }

    private int getStudentIdFromUserId(int userId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT student_id FROM students WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void listStudents(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String search = request.getParameter("search");
        String semesterFilter = request.getParameter("semester");
        
        if (search == null) search = "";
        if (semesterFilter == null) semesterFilter = "";

        List<Student> students = new ArrayList<>();
        List<String> departments = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            
            // Get teacher's department
            String teacherDept = "";
            int teacherId = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id, department FROM teachers WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teacherId = rs.getInt("teacher_id");
                        teacherDept = rs.getString("department");
                    }
                }
            }
            request.setAttribute("teacherId", teacherId);
            request.setAttribute("teacherDept", teacherDept);

            // Fetch available departments for dropdown filter
            try (PreparedStatement ps = con.prepareStatement("SELECT DISTINCT department_name FROM departments WHERE status='ACTIVE' ORDER BY department_name")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        departments.add(rs.getString("department_name"));
                    }
                }
            }

            // Build student listing SQL
            StringBuilder sql = new StringBuilder(
                "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, u.mobile, u.status, s.student_id, s.department, s.semester " +
                "FROM users u " +
                "INNER JOIN students s ON u.user_id = s.user_id " +
                "WHERE s.department = ? "
            );
            
            List<Object> params = new ArrayList<>();
            params.add(teacherDept);

            if (!search.trim().isEmpty()) {
                sql.append("AND (u.first_name LIKE ? OR u.last_name LIKE ? OR u.username LIKE ? OR u.email LIKE ?) ");
                String likeVal = "%" + search.trim() + "%";
                params.add(likeVal);
                params.add(likeVal);
                params.add(likeVal);
                params.add(likeVal);
            }

            if (!semesterFilter.trim().isEmpty()) {
                sql.append("AND s.semester = ? ");
                params.add(semesterFilter.trim());
            }

            sql.append("ORDER BY u.first_name ASC, u.last_name ASC");

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Student student = new Student();
                        student.setUserId(rs.getInt("user_id"));
                        student.setStudentId(rs.getInt("student_id"));
                        student.setUsername(rs.getString("username"));
                        student.setFirstName(rs.getString("first_name"));
                        student.setLastName(rs.getString("last_name"));
                        student.setEmail(rs.getString("email"));
                        student.setMobile(rs.getString("mobile"));
                        student.setStatus(rs.getString("status"));
                        student.setDepartment(rs.getString("department"));
                        student.setSemester(rs.getString("semester"));
                        students.add(student);
                    }
                }
            }

            request.setAttribute("students", students);
            request.setAttribute("departments", departments);
            request.setAttribute("search", search);
            request.setAttribute("selectedSemester", semesterFilter);
            request.setAttribute("activePage", "students");

            request.getRequestDispatcher("teacherStudents.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherServlet?error=database");
        }
    }

    private void viewStudentDetails(HttpServletRequest request, HttpServletResponse response, int studentId) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String userRole = String.valueOf(session.getAttribute("role"));

        try (Connection con = DBConnection.getConnection()) {
            
            Student student = null;
            int teacherId = 0;
            String teacherDept = "";

            if ("ADMIN".equalsIgnoreCase(userRole)) {
                // For admin, query student details first to get their department
                String studentSql = 
                    "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, u.mobile, u.status, s.student_id, s.department, s.semester " +
                    "FROM users u " +
                    "INNER JOIN students s ON u.user_id = s.user_id " +
                    "WHERE s.student_id = ?";
                try (PreparedStatement ps = con.prepareStatement(studentSql)) {
                    ps.setInt(1, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            student = new Student();
                            student.setUserId(rs.getInt("user_id"));
                            student.setStudentId(rs.getInt("student_id"));
                            student.setUsername(rs.getString("username"));
                            student.setFirstName(rs.getString("first_name"));
                            student.setLastName(rs.getString("last_name"));
                            student.setEmail(rs.getString("email"));
                            student.setMobile(rs.getString("mobile"));
                            student.setStatus(rs.getString("status"));
                            student.setDepartment(rs.getString("department"));
                            student.setSemester(rs.getString("semester"));
                        }
                    }
                }
                
                if (student != null) {
                    teacherDept = student.getDepartment();
                    // Lookup first teacher in that department to compute statistics
                    try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id FROM teachers WHERE department = ? LIMIT 1")) {
                        ps.setString(1, teacherDept);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                teacherId = rs.getInt("teacher_id");
                            }
                        }
                    }
                }
            } else {
                // For teacher, get teacher detail first
                try (PreparedStatement ps = con.prepareStatement("SELECT teacher_id, department FROM teachers WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            teacherId = rs.getInt("teacher_id");
                            teacherDept = rs.getString("department");
                        }
                    }
                }

                // Fetch Student Details limited to teacher's department
                String studentSql = 
                    "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, u.mobile, u.status, s.student_id, s.department, s.semester " +
                    "FROM users u " +
                    "INNER JOIN students s ON u.user_id = s.user_id " +
                    "WHERE s.student_id = ? AND s.department = ?";
                try (PreparedStatement ps = con.prepareStatement(studentSql)) {
                    ps.setInt(1, studentId);
                    ps.setString(2, teacherDept);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            student = new Student();
                            student.setUserId(rs.getInt("user_id"));
                            student.setStudentId(rs.getInt("student_id"));
                            student.setUsername(rs.getString("username"));
                            student.setFirstName(rs.getString("first_name"));
                            student.setLastName(rs.getString("last_name"));
                            student.setEmail(rs.getString("email"));
                            student.setMobile(rs.getString("mobile"));
                            student.setStatus(rs.getString("status"));
                            student.setDepartment(rs.getString("department"));
                            student.setSemester(rs.getString("semester"));
                        }
                    }
                }
            }

            if (student == null) {
                response.sendRedirect("UserManagementServlet?error=notfound");
                return;
            }

            // =================================================
            // ACADEMIC CALCULATIONS
            // =================================================
            
            // 1. Attendance Rate
            double attendanceRate = 100.0;
            int totalAttendance = 0;
            int presentAttendance = 0;
            String attendanceSql = "SELECT COUNT(*), SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) FROM attendance WHERE student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(attendanceSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalAttendance = rs.getInt(1);
                        presentAttendance = rs.getInt(2);
                        if (totalAttendance > 0) {
                            attendanceRate = ((double) presentAttendance / totalAttendance) * 100;
                        }
                    }
                }
            }

            // 2. Assignment Completion Rate
            double assignmentCompletion = 100.0;
            int totalAssignments = 0;
            int submittedAssignments = 0;
            
            // Total assignments in general created by teacher
            String totalAssignmentsSql = "SELECT COUNT(*) FROM assignments WHERE teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(totalAssignmentsSql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalAssignments = rs.getInt(1);
                    }
                }
            }
            
            // Total submitted by this student
            String submittedAssignmentsSql = 
                "SELECT COUNT(*) FROM assignment_submissions sub " +
                "INNER JOIN assignments a ON sub.assignment_id = a.assignment_id " +
                "WHERE sub.student_id = ? AND a.teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(submittedAssignmentsSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        submittedAssignments = rs.getInt(1);
                    }
                }
            }
            
            if (totalAssignments > 0) {
                assignmentCompletion = ((double) submittedAssignments / totalAssignments) * 100;
            }

            // 3. Class Test Marks Average
            double testAverage = 0.0;
            String testSql = 
                "SELECT AVG(m.marks_obtained / t.total_marks) * 100 FROM test_marks m " +
                "INNER JOIN class_tests t ON m.test_id = t.test_id " +
                "WHERE m.student_id = ? AND t.teacher_id = ?";
            try (PreparedStatement ps = con.prepareStatement(testSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        testAverage = rs.getDouble(1);
                    }
                }
            }

            // 4. Overall Performance Threshold
            double overallScore = (attendanceRate + assignmentCompletion + (testAverage > 0 ? testAverage : 100.0)) / 3;
            String performance = "GOOD";
            if (overallScore < 70.0) {
                performance = "CRITICAL";
            } else if (overallScore < 85.0) {
                performance = "WARNING";
            }

            request.setAttribute("student", student);
            request.setAttribute("attendanceRate", String.format("%.1f", attendanceRate));
            request.setAttribute("totalAttendance", totalAttendance);
            request.setAttribute("presentAttendance", presentAttendance);
            request.setAttribute("assignmentCompletion", String.format("%.1f", assignmentCompletion));
            request.setAttribute("totalAssignments", totalAssignments);
            request.setAttribute("submittedAssignments", submittedAssignments);
            request.setAttribute("testAverage", String.format("%.1f", testAverage));
            request.setAttribute("performanceStatus", performance);
            request.setAttribute("activePage", "students");

            request.getRequestDispatcher("teacherStudentDetails.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherStudentServlet?error=database");
        }
    }
}
