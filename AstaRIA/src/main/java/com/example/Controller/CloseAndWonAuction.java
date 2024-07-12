package com.example.Controller;

import com.example.Bean.AuctionBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.AuctionDAO;
import com.example.Utility.DBConnector;
import com.example.exception.ErrorException;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class GoToBuy
 */
@WebServlet("/closeAndWonAuction")
public class CloseAndWonAuction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        connection = DBConnector.getConnection(servletContext);
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();

            /*Prendo le aste già vinte dall'utente di cui so l'id perché è in sessione. In questo caso
            * l'id dell'utente è preso direttamente dalla sessione*/
            AuctionDAO astaDao = new AuctionDAO(connection);
            List<AuctionBEAN> listOfAuctionEnded;
            try {
                UserBean userBean = (UserBean)session.getAttribute("user");
                int id = userBean.getId();
                listOfAuctionEnded = astaDao.getEndAuction(id);
            } catch (SQLException e) {
                throw new ErrorException("Error, database has some problem");
            }
            String json = new Gson().toJson(listOfAuctionEnded);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(json);
        }
        catch (ErrorException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {

        }
    }
}
