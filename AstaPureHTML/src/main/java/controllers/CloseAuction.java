package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DAO.*;
import exceptions.ErrorException;
import org.apache.commons.lang.StringEscapeUtils;
import utils.*;
import beans.*;

@WebServlet("/CloseAuction")
@MultipartConfig
public class CloseAuction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	public CloseAuction() {
		super();
	}

	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String auctionId_s = StringEscapeUtils.escapeJava(request.getParameter("auctionId"));
		User user = (User) session.getAttribute("user");
		Timestamp loginDate = (Timestamp) session.getAttribute("timeLogin");
		AuctionDAO auctionDao = new AuctionDAO(connection);
		String path = null;
		int creatorId = user.getId();
		int auctionId;



		try {


			/** Controllo che l'id passato da queryString sia un intero e non nullo*/
			try {
				auctionId = Integer.parseInt(auctionId_s);
			} catch (NumberFormatException | NullPointerException e) {
				throw new ErrorException("Error! Bad value of ID to close the auction");
			}


			AuctionBEAN auction = null;
			try {
				auction = auctionDao.checkClosableByUser(creatorId,auctionId);
			} catch (SQLException e) {
				throw new ErrorException("Error checking user's closable auction");
			}
			/** Se entro in questo significa che l'utente ha cercato di chiudere un'asta non chiudibile da lui*/
			if (auction==null) {
				throw new ErrorException("Error! You tried to close a not closable auction.");
			} else {
				/** Se entro in questo if significa che l'utente si è loggato prima di poter chiudere l'asta.
				 *  per chiudere un'asta scaduto bisogna loggarsi dopo la scadenza (Pura scelta implementativa)*/
				if (loginDate.compareTo(auction.getEndDate()) < 0) {
					throw new ErrorException("Error! The auction is not expired. (Based on your login date)");
				}
			}


			/** Prendo la lista delle offerte che l'asta ha ricevuto*/
			List<OfferBEAN> list = null;
			OfferDAO offerDAO = new OfferDAO(connection);
			try {
				list = offerDAO.getAllAuctionsOffer(auctionId);
			} catch (SQLException e) {
				throw new ErrorException("Failure in getting auction offers from database");
			}

			try {
				/** Se entro in questo if significa che l'asta non ha mai ricevuto offerte
				 *  di conseguenza gli oggetti non saranno vinti da nessuno e torneranno disponibili
				 *  al loro possessore per nuove aste.
				 *  La query si limita solo a chiudere l'asta
				 */
				if (list.size() == 0) {
					auctionDao.onlyCloseAuction(auctionId);
				} else {
					/** Qui invece utilizzo una query che chiude l'asta e dichiara vinci in questa asta
					 * tutti gli oggetti che comprendeva */
					auctionDao.closeAuctionAndAssignArticles(auctionId);
				}
			} catch (SQLException e) {
				throw new ErrorException("Failure in closing the auction");
			}

		} catch (ErrorException e) {
			path = getServletContext().getContextPath() + "/GoToSell";
			path+="?errorMsg="+e.getMessage();
			response.sendRedirect(path);
			return;
		}

		path = getServletContext().getContextPath() + "/GoToSell";
		response.sendRedirect(path);
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