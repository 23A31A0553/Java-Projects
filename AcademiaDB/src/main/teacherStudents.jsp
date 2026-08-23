<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.university.servlet.Student" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !"TEACHER".equalsIgnoreCase(role)) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<Student> students = (List<Student>) request.getAttribute("students");
    String search = (String) request.getAttribute("search");
    String selectedSemester = (String) request.getAttribute("selectedSemester");
    String teacherDept = (String) request.getAttribute("teacherDept");

    if (search == null) search = "";
    if (selectedSemester == null) selectedSemester = "";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Students - BLUE RIDGE UNIVERSITY</title>
    <link rel="stylesheet" href="css/teacher.css">
    <link rel="stylesheet" href="css/teacher-students.css">
</head>
<body>

    <!-- Shared Header -->
    <jsp:include page="teacherHeader.jsp" />

    <div class="app-layout">
        <!-- Shared Sidebar -->
        <jsp:include page="teacherSidebar.jsp" />

        <main class="main-content-wrapper">
            <div class="teacher-container">

                <div class="page-header">
                    <h2>My Students</h2>
                    <p>List of students registered in the department of <strong><%= teacherDept %></strong>.</p>
                </div>

                <!-- Filter Panel Card -->
                <div class="filter-card card">
                    <form action="TeacherStudentServlet" method="get" class="filter-form">
                        <div class="form-group flex-1">
                            <input type="text" name="search" value="<%= search %>" placeholder="Search by name, username, or email...">
                        </div>
                        <div class="form-group">
                            <select name="semester">
                                <option value="">All Semesters</option>
                                <option value="1" <%= "1".equals(selectedSemester) ? "selected" : "" %>>Semester 1</option>
                                <option value="2" <%= "2".equals(selectedSemester) ? "selected" : "" %>>Semester 2</option>
                                <option value="3" <%= "3".equals(selectedSemester) ? "selected" : "" %>>Semester 3</option>
                                <option value="4" <%= "4".equals(selectedSemester) ? "selected" : "" %>>Semester 4</option>
                                <option value="5" <%= "5".equals(selectedSemester) ? "selected" : "" %>>Semester 5</option>
                                <option value="6" <%= "6".equals(selectedSemester) ? "selected" : "" %>>Semester 6</option>
                                <option value="7" <%= "7".equals(selectedSemester) ? "selected" : "" %>>Semester 7</option>
                                <option value="8" <%= "8".equals(selectedSemester) ? "selected" : "" %>>Semester 8</option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-primary">Search</button>
                        <a href="TeacherStudentServlet" class="btn btn-secondary">Clear</a>
                    </form>
                </div>

                <!-- Student Listing Card -->
                <div class="students-list-card card">
                    <div class="table-wrapper">
                        <table class="students-table">
                            <thead>
                                <tr>
                                    <th>Student ID</th>
                                    <th>Name</th>
                                    <th>Username</th>
                                    <th>Email</th>
                                    <th>Semester</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    if (students == null || students.isEmpty()) {
                                %>
                                    <tr>
                                        <td colspan="7" class="no-data">No students found matching your criteria.</td>
                                    </tr>
                                <%
                                    } else {
                                        for (Student s : students) {
                                %>
                                    <tr>
                                        <td><%= s.getStudentId() %></td>
                                        <td class="student-name-cell"><%= s.getFirstName() %> <%= s.getLastName() %></td>
                                        <td><%= s.getUsername() %></td>
                                        <td><%= s.getEmail() %></td>
                                        <td>Semester <%= s.getSemester() %></td>
                                        <td>
                                            <span class="status-badge status-<%= s.getStatus().toLowerCase() %>">
                                                <%= s.getStatus() %>
                                            </span>
                                        </td>
                                        <td>
                                            <a href="TeacherStudentServlet?studentId=<%= s.getStudentId() %>" class="btn btn-table btn-view">👁️ View Details</a>
                                        </td>
                                    </tr>
                                <%
                                        }
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Shared Footer -->
    <jsp:include page="teacherFooter.jsp" />

</body>
</html>
