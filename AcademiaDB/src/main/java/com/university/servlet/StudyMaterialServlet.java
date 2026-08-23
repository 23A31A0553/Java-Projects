package com.university.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import com.university.db.DBConnection;
import com.university.model.StudyMaterial;

@WebServlet("/StudyMaterialServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class StudyMaterialServlet extends HttpServlet {

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

        List<StudyMaterial> materials = new ArrayList<>();

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

            // Get uploaded materials
            String sql = "SELECT * FROM study_materials WHERE teacher_id = ? ORDER BY material_id DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        StudyMaterial material = new StudyMaterial();
                        material.setMaterialId(rs.getInt("material_id"));
                        material.setTeacherId(rs.getInt("teacher_id"));
                        material.setTitle(rs.getString("title"));
                        material.setSubject(rs.getString("subject"));
                        material.setUnit(rs.getString("unit"));
                        material.setDescription(rs.getString("description"));
                        material.setFileName(rs.getString("file_name"));
                        material.setFilePath(rs.getString("file_path"));
                        material.setUploadDate(rs.getTimestamp("upload_date"));
                        materials.add(material);
                    }
                }
            }

            request.setAttribute("materials", materials);
            request.setAttribute("activePage", "studyMaterials");
            request.getRequestDispatcher("studyMaterials.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("TeacherServlet?error=database");
        }
    }

    private void downloadMaterial(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("StudyMaterialServlet?error=invalid");
            return;
        }

        int materialId = Integer.parseInt(idParam);
        String fileName = "";
        String filePath = "";

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

            // Get material info
            try (PreparedStatement ps = con.prepareStatement("SELECT file_name, file_path FROM study_materials WHERE material_id = ? AND teacher_id = ?")) {
                ps.setInt(1, materialId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        fileName = rs.getString("file_name");
                        filePath = rs.getString("file_path");
                    } else {
                        response.sendRedirect("StudyMaterialServlet?error=notfound");
                        return;
                    }
                }
            }

            File file = new File(filePath);
            if (!file.exists()) {
                response.sendRedirect("StudyMaterialServlet?error=filenotfound");
                return;
            }

            // Stream file to client
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLength((int) file.length());

            try (InputStream is = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudyMaterialServlet?error=database");
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
        if ("upload".equalsIgnoreCase(action)) {
            uploadMaterial(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            deleteMaterial(request, response);
        } else {
            response.sendRedirect("StudyMaterialServlet");
        }
    }

    private void uploadMaterial(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String title = request.getParameter("title");
        String subject = request.getParameter("subject");
        String unit = request.getParameter("unit");
        String description = request.getParameter("description");
        Part filePart = request.getPart("file");

        if (title == null || title.trim().isEmpty() ||
            subject == null || subject.trim().isEmpty() ||
            unit == null || unit.trim().isEmpty() ||
            filePart == null || filePart.getSize() == 0) {
            response.sendRedirect("StudyMaterialServlet?error=empty");
            return;
        }

        String fileName = filePart.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty()) {
            response.sendRedirect("StudyMaterialServlet?error=empty");
            return;
        }

        // Security validation for executable/dangerous files
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".exe") || lowerName.endsWith(".bat") ||
            lowerName.endsWith(".sh") || lowerName.endsWith(".cmd") ||
            lowerName.endsWith(".msi") || lowerName.endsWith(".jar") ||
            lowerName.endsWith(".com")) {
            response.sendRedirect("StudyMaterialServlet?error=blockedfiletype");
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

            // Create uploads directory
            String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "study-materials";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Write file
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            String filePath = uploadPath + File.separator + uniqueFileName;
            filePart.write(filePath);

            // Save to DB
            String sql = "INSERT INTO study_materials (teacher_id, title, subject, unit, description, file_name, file_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, teacherId);
                ps.setString(2, title.trim());
                ps.setString(3, subject.trim());
                ps.setString(4, unit.trim());
                ps.setString(5, description != null ? description.trim() : "");
                ps.setString(6, fileName);
                ps.setString(7, filePath);
                ps.executeUpdate();
            }

            logActivity(con, userId, "UPLOAD_MATERIAL", "Teacher uploaded study notes: " + title);

            response.sendRedirect("StudyMaterialServlet?success=uploaded");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudyMaterialServlet?error=database");
        }
    }

    private void deleteMaterial(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String idParam = request.getParameter("materialId");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("StudyMaterialServlet?error=invalid");
            return;
        }

        int materialId = Integer.parseInt(idParam);

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

            // Get file path to delete from disk
            String filePath = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT file_path FROM study_materials WHERE material_id = ? AND teacher_id = ?")) {
                ps.setInt(1, materialId);
                ps.setInt(2, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        filePath = rs.getString("file_path");
                    } else {
                        response.sendRedirect("StudyMaterialServlet?error=notfound");
                        return;
                    }
                }
            }

            // Delete record
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM study_materials WHERE material_id = ? AND teacher_id = ?")) {
                ps.setInt(1, materialId);
                ps.setInt(2, teacherId);
                ps.executeUpdate();
            }

            // Delete physical file
            if (filePath != null && !filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }

            logActivity(con, userId, "DELETE_MATERIAL", "Teacher deleted study notes ID: " + materialId);

            response.sendRedirect("StudyMaterialServlet?success=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("StudyMaterialServlet?error=database");
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
