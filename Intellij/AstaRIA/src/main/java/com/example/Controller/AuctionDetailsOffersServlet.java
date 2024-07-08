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
import java.util.ArrayList;
import java.util.List;


@WebServlet("/AuctionOffersServlet")
@MultipartConfig
public class AuctionDetailsOffersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    public AuctionDetailsOffersServlet() {
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
		List<OfferBEAN> list= null;

		try {


			/** Controllo che l'id passato da queryString sia un intero e non nullo*/
			Integer auctionId = null;
			try {
				auctionId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("ID")));
			} catch (NumberFormatException | NullPointerException e) {
				throw new ErrorException("Error! Incorrect value for parameter ID.");
			}


			/** Estraggo l'asta richiesta da DB*/
			try {
				auctionBean = auctionDao.exist(auctionId);
			} catch (SQLException e) {
				throw new ErrorException("Error! Failure in auction details extraction.");
			}


			/** Se la query non trova nulla */
			if (auctionBean == null) {
				throw new ErrorException("Error! Auction not found.");
			}


			/** Estraggo tutte le offerte ricevute per l'asta ordinate in ordine decrescente di prezzo,
			 * quindi la prima è la vincente. Dell'offerta vengono estratte tutte le informazioni*/
			list = new ArrayList<>();
			OfferDAO offerDAO = new OfferDAO(connection);
			try {
				list = offerDAO.getAllAuctionsOffer(auctionId);
			} catch (SQLException e) {
				throw new ErrorException("Error! Failure in offers details extraction.");
			}

		}
		catch (ErrorException e)
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		/*Se è tutto okay ritorna la lista delle offerte*/
		String json = new Gson().toJson(list);
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
