<%@ page pageEncoding="UTF-8"%>
<%
    String headerRole = (String) session.getAttribute("role");
    if (headerRole == null || (!"TEACHER".equalsIgnoreCase(headerRole) && !"ADMIN".equalsIgnoreCase(headerRole))) {
        response.sendRedirect("index.jsp");
        return;
    }
    String headerFirstName = (String) session.getAttribute("firstName");
    String headerLastName = (String) session.getAttribute("lastName");
    if (headerFirstName == null) headerFirstName = "User";
    if (headerLastName == null) headerLastName = "";
%>
<header class="top-bar">
    <div class="college-section">
        <button class="mobile-nav-toggle" type="button" aria-label="Toggle navigation">
            <span class="bar"></span>
            <span class="bar"></span>
            <span class="bar"></span>
        </button>
        <img src="images/logo.jpeg" alt="Blue Ridge University Logo" class="college-logo">
        <div>
            <h1>BLUE RIDGE UNIVERSITY</h1>
            <p>University Management System</p>
        </div>
    </div>
    <div class="teacher-section">
        <span class="teacher-name">
            <i class="emoji-icon">👤</i> <%= headerFirstName %> <%= headerLastName %>
        </span>
        <a href="LogoutServlet" class="logout-button">Logout</a>
    </div>
</header>

<script>
document.addEventListener("DOMContentLoaded", function() {
    var toggleBtn = document.querySelector(".mobile-nav-toggle");
    var sidebar = document.querySelector(".sidebar");
    var appLayout = document.querySelector(".app-layout");
    
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener("click", function(e) {
            e.stopPropagation();
            sidebar.classList.toggle("active");
            toggleBtn.classList.toggle("active");
            if (appLayout) {
                appLayout.classList.toggle("sidebar-active");
            }
        });
        
        document.addEventListener("click", function(e) {
            if (sidebar.classList.contains("active") && !sidebar.contains(e.target) && !toggleBtn.contains(e.target)) {
                sidebar.classList.remove("active");
                toggleBtn.classList.remove("active");
                if (appLayout) {
                    appLayout.classList.remove("sidebar-active");
                }
            }
        });
    }
});
</script>
