package com.university.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        // ================================================
        // GET CURRENT SESSION
        // ================================================

        HttpSession session =
                request.getSession(false);


        // ================================================
        // DESTROY SESSION
        // ================================================

        if (session != null) {

            session.invalidate();
        }


        // ================================================
        // PREVENT CACHED PAGES
        // ================================================

        response.setHeader(
            "Cache-Control",
            "no-cache, no-store, must-revalidate"
        );

        response.setHeader(
            "Pragma",
            "no-cache"
        );

        response.setDateHeader(
            "Expires",
            0
        );


        // ================================================
        // REDIRECT TO LOGIN
        // ================================================

        response.sendRedirect(
            "index.jsp?logout=success"
        );
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        doGet(
            request,
            response
        );
    }
}