package db.servlet;

import db.dao.PortalUserDao;
import db.dao.impl.PortalUserDaoImpl;
import db.dto.PortalUserFilter;
import db.entity.PortalUserEntity;
import db.service.PortalUserService;
import db.service.impl.PortalUserServiceImpl;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "PortalUser", urlPatterns = "/users")
public class PortalUserServlet extends HttpServlet {

    private final PortalUserDaoImpl portalUserDao = PortalUserDaoImpl.getInstance();
    private final PortalUserService portalUserService = PortalUserServiceImpl.getInstance();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        resp.setContentType("text/html");

        try {
            if (id != null) {
                int userId = Integer.parseInt(id);
                PortalUserFilter user = portalUserService.findById(userId);
                resp.getWriter().write("<h1>User Details</h1>");
                resp.getWriter().write("<p>ID: " + user.getId() + "</p>");
                resp.getWriter().write("<p>Name: " + user.getFirstName() + "</p>");
                resp.getWriter().write("<p>Email: " + user.getEmail() + "</p>");
            } else {
                List<PortalUserFilter> users = portalUserService.findAll();
                resp.getWriter().write("<h1>All Users</h1>");
                resp.getWriter().write("<ul>");
                for (PortalUserFilter user : users) {
                    resp.getWriter().write("<li>" + user.getFirstName() + " " + user.getLastName() + "</li>");
                }
                resp.getWriter().write("</ul>");
            }
        } catch (Exception e) {
            resp.getWriter().write("<h1>Error: " + e.getMessage() + "</h1>");
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}




