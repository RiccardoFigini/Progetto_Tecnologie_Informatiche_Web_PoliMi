package com.example.Controller;

import com.example.Bean.AuctionBEAN;
import com.example.Bean.OfferBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.AuctionDAO;
import com.example.Dao.OfferDAO;
import com.example.Dao.UserDAO;
import com.example.Utility.DBConnector;
import com.example.exception.ErrorException;
import com.google.gson.Gson;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class MakeAOffer
 */
@WebServlet("/LogoutServlet")
@MultipartConfig
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    public LogoutServlet() {
        super();
    }
    public void init() throws UnavailableException {
        ServletContext servletContext = getServletContext();
        connection = DBConnector.getConnection(servletContext);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("user");
        session.removeAttribute("timeLogin");
        session.invalidate();
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
