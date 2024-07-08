package com.example.Controller;
import com.example.Bean.AuctionBEAN;
import com.example.Bean.OfferBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.AuctionDAO;
import com.example.Dao.OfferDAO;
import com.example.Utility.DBConnector;
import com.example.exception.ErrorException;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
@WebServlet("/AuctionWinnerServlet")
@MultipartConfig
public class AuctionWinnerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    public AuctionWinnerServlet() {
        super();
    }

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        connection = DBConnector.getConnection(servletContext);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserBean user = (UserBean) session.getAttribute("user");
        int userId = user.getId();
        AuctionDAO auctionDao = new AuctionDAO(connection);
        AuctionBEAN auctionBean=null;
        OfferBEAN winnerOffer= null;
        Map<String, Object> jsonMap = new HashMap<>();

        try {


            /** Controllo che l'id passato da queryString sia un intero e non nullo*/
            Integer auctionId = null;
            try {
                auctionId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("ID")));
            } catch (NumberFormatException | NullPointerException e) {
                throw new ErrorException("Error! Incorrect value for parameter ID.");
            }


            /** Estraggo l'asta richiesta da DB */
            try {
                auctionBean = auctionDao.getAuctionDetailsFromId(auctionId);
            } catch (SQLException e) {
                throw new ErrorException("Error! Failure in auction details extraction.");
            }


            /** Se la query non trova nulla */
            if (auctionBean == null) {
                throw new ErrorException("Error! Auction not found.");
            }


            jsonMap.put("isClosed", auctionBean.getIsClosed());

            if(auctionBean.getIsClosed())
            {
                OfferDAO offerDAO = new OfferDAO(connection);
                try {
                    winnerOffer = offerDAO.getWinnerOfferFromAuction(auctionId);
                } catch (SQLException e) {
                    throw new ErrorException("Error! Failure in winner offers details extraction.");
                }
            }

        }
        catch (ErrorException e)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        jsonMap.put("winnerOffer", winnerOffer);
        String json = new Gson().toJson(jsonMap);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
        }
    }

}
