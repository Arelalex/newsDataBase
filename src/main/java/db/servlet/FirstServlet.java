package db.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet(name = "MySer", urlPatterns = "/example")
public class FirstServlet extends HttpServlet {

   /* @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }*/


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        try {
            response.getWriter().println("<h1>Hello from MyServlet!</h1>");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("<h1>Error: " + e.getMessage() + "</h1>");
        }
    }



/*    @Override
    public void destroy() {
        super.destroy();
    }*/
}
