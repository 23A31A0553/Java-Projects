package com.university.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

@WebServlet("/StudentMaterialServlet")
public class StudentMaterialServlet extends HttpServlet {

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

        String action = request.getParameter("action");
        if ("download".equalsIgnoreCase(action)) {
            downloadMaterial(request, response);
            return;
        }

        listMaterials(request, response);
    }

    private void listMaterials(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String selectedSubject = request.getParameter("subject");
        String selectedUnit = request.getParameter("unit");
        String selectedType = request.getParameter("fileType");

        List<Map<String, Object>> materialsList = new ArrayList<>();
        List<String> distinctSubjects = new ArrayList<>();
        List<String> distinctUnits = new ArrayList<>();

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

            // Retrieve distinct subjects and units for dropdown filters
            try (PreparedStatement ps = con.prepareStatement("SELECT DISTINCT subject, unit FROM study_materials m INNER JOIN teachers t ON m.teacher_id = t.teacher_id WHERE t.department = ?")) {
                ps.setString(1, department);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String s = rs.getString("subject");
                        String u = rs.getString("unit");
                        if (!distinctSubjects.contains(s)) distinctSubjects.add(s);
                        if (!distinctUnits.contains(u)) distinctUnits.add(u);
                    }
                }
            }

            // Fetch materials mapping to department
            StringBuilder sql = new StringBuilder(
                "SELECT m.material_id, m.title, m.subject, m.unit, m.description, m.file_name, m.file_path, m.upload_date, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS teacher_name " +
                "FROM study_materials m " +
                "INNER JOIN teachers t ON m.teacher_id = t.teacher_id " +
                "INNER JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.department = ? "
            );
            List<Object> params = new ArrayList<>();
            params.add(department);

            if (selectedSubject != null && !selectedSubject.trim().isEmpty()) {
                sql.append("AND m.subject = ? ");
                params.add(selectedSubject.trim());
            }
            if (selectedUnit != null && !selectedUnit.trim().isEmpty()) {
                sql.append("AND m.unit = ? ");
                params.add(selectedUnit.trim());
            }

            sql.append("ORDER BY m.upload_date DESC");

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("materialId", rs.getInt("material_id"));
                        map.put("title", rs.getString("title"));
                        map.put("subject", rs.getString("subject"));
                        map.put("unit", rs.getString("unit"));
                        map.put("description", rs.getString("description"));
                        
                        String fileName = rs.getString("file_name");
                        map.put("fileName", fileName);
                        map.put("filePath", rs.getString("file_path"));
                        map.put("uploadDate", rs.getTimestamp("upload_date"));
                        map.put("teacherName", rs.getString("teacher_name"));

                        // Detect file type from extension
                        String fileType = "Notes";
                        if (fileName != null && fileName.contains(".")) {
                            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                            if ("pdf".equals(ext)) fileType = "PDF";
                            else if ("ppt".equals(ext) || "pptx".equals(ext)) fileType = "PPT";
                            else if ("doc".equals(ext) || "docx".equals(ext)) fileType = "DOC";
                        }
                        map.put("fileType", fileType);

                        // Filter by type if selected
                        if (selectedType == null || selectedType.trim().isEmpty() || selectedType.equalsIgnoreCase(fileType)) {
                            materialsList.add(map);
                        }
                    }
                }
            }

            request.setAttribute("materialsList", materialsList);
            request.setAttribute("subjectsList", distinctSubjects);
            request.setAttribute("unitsList", distinctUnits);
            
            request.setAttribute("selectedSubject", selectedSubject);
            request.setAttribute("selectedUnit", selectedUnit);
            request.setAttribute("selectedType", selectedType);

            request.setAttribute("activePage", "studyMaterials");
            request.getRequestDispatcher("studentMaterials.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentServlet?error=database");
        }
    }

    private void downloadMaterial(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String materialIdStr = request.getParameter("materialId");
        if (materialIdStr == null || materialIdStr.trim().isEmpty()) {
            response.sendRedirect("StudentMaterialServlet?error=invalid");
            return;
        }

        int materialId = Integer.parseInt(materialIdStr);

        try (Connection con = DBConnection.getConnection()) {
            String fileName = "";
            String filePath = "";
            
            try (PreparedStatement ps = con.prepareStatement("SELECT file_name, file_path FROM study_materials WHERE material_id = ?")) {
                ps.setInt(1, materialId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        fileName = rs.getString("file_name");
                        filePath = rs.getString("file_path");
                    }
                }
            }

            if (fileName.isEmpty() || filePath.isEmpty()) {
                response.sendRedirect("StudentMaterialServlet?error=notfound");
                return;
            }

            // Resolve file location
            String contextPath = request.getServletContext().getRealPath("");
            String fullPath = contextPath + File.separator + filePath;
            File downloadFile = new File(fullPath);

            if (!downloadFile.exists()) {
                // Try direct upload path if it's absolute
                downloadFile = new File(filePath);
                if (!downloadFile.exists()) {
                    response.sendRedirect("StudentMaterialServlet?error=missingfile");
                    return;
                }
            }

            // Set content type and headers
            String mimeType = request.getServletContext().getMimeType(fullPath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);
            response.setContentLength((int) downloadFile.length());
            
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
            response.setHeader(headerKey, headerValue);

            // Write to output stream
            try (FileInputStream inStream = new FileInputStream(downloadFile);
                 OutputStream outStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudentMaterialServlet?error=database");
        }
    }
}
