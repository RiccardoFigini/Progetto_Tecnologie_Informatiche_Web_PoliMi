package com.example.Controller;
import com.example.Bean.UserBean;
import com.example.Dao.UserDAO;
import com.example.PackWeb.PackLogin;
import com.example.Utility.DBConnector;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
    private Connection connection = null;
    public CheckLogin() {
        super();
    }
    public void init() throws ServletException {
        try {
            ServletContext servletContext = getServletContext();
            connection = DBConnector.getConnection(servletContext);
        }catch (Exception e)
        {
            throw new RuntimeException("Controlla il file WEB");
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username;
        String password;
        try {
            username = StringEscapeUtils.escapeJava(request.getParameter("username"));
            password = StringEscapeUtils.escapeJava(request.getParameter("password"));

            if (username == null || password == null || username.isEmpty() || password.isEmpty())
            {
                throw new Exception("Failed login.\nMissing or empty credential value");
            }

        } catch (Exception e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        /*Oltre a controllare le credenziale vengono restituite tutte le informazioni dell'utente
        * così da poterle salvare nella sessione ed utilizzarle quando serviranno*/
        UserDAO userDao = new UserDAO(connection);
        UserBean user;
        try {
            user = userDao.checkCredentials(username, password);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
            return;
        }

        HttpSession session = request.getSession();
        Timestamp timeLogin=new Timestamp(System.currentTimeMillis());
        session.setAttribute("user", user);
        session.setAttribute("timeLogin", new Timestamp(System.currentTimeMillis()));
        String packetUser = new Gson().toJson(new PackLogin(user.getId(), user.getUsername(), timeLogin));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(packetUser);
    }

    public void destroy() {
        try {
            DBConnector.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
