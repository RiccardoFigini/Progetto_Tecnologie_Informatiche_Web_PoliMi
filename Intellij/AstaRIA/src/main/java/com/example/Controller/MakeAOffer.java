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
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class MakeAOffer
 */
@WebServlet("/MakeAOffer")
public class MakeAOffer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MakeAOffer() {
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
		response.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		String auctionId_s = StringEscapeUtils.escapeJava(request.getParameter("auctionId"));
		float offer;
		int id;
		Float lastOffer;
		try {
			try {
				lastOffer = Float.parseFloat(request.getParameter("lastOffer"));
				offer = Float.parseFloat(StringEscapeUtils.escapeJava(request.getParameter("offer")));
			}
			catch (Exception exc)
			{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new ErrorException("Bad parsing for parameters");
			}

			/*Prendo l'utente*/
			UserBean userBean = (UserBean)session.getAttribute("user");
			/*
			 * Prendo l'asta di cui voglio fare l'offerta, mi servono alcuni suoi parametri, quindi non posso
			 * verificare solamente che l'asta effettivamente esiste come nel caso della GoToAuctionOffer
			 * */
			OfferDAO offerDao = new OfferDAO(connection);
			AuctionBEAN aubean;
			AuctionDAO audao = new AuctionDAO(connection);
			if (auctionId_s != null) {
				id = Integer.parseInt(auctionId_s);
				try {
					aubean = audao.getAuction(id);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					throw new ErrorException("Database has some problem");
				}
				if (aubean == null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new ErrorException("Auction does not exist");
				}
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new ErrorException("Auction id parameter is null");
			}
			/*
			 * Vedo se l'offerta è valida, in questo caso se non lo è non è un problema del server o del database, ma dell'inserimento
			 * dell'utente. Quindi al posto di rimandarlo alla pagina iniziale gli do la possibilità di far di nuovo l'offerta
			 * avvisandolo che ci sono stati dei problemi nella precedente*/
			Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

			if (aubean.getIsClosed()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new ErrorException("Auction is closed");
			}
			else if (aubean.getStartDate().compareTo(timeStamp) > 0 || aubean.getEndDate().compareTo(timeStamp)<0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new ErrorException("Auction is not open at this moment");
			}
			/* Svolgo finalmente l'offerta*/
			if (!(
					offer<0 || (!aubean.isAlreadyOffer() &&( offer<aubean.getStartPrice())) ||
					(aubean.isAlreadyOffer() && (offer<=aubean.getMaxOffer() ||
					offer-aubean.getMaxOffer() < aubean.getMinimumRise()))
					)
			)
			{
				try {
					offerDao.makeAOffer(aubean.getId(), userBean, offer);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					throw new ErrorException("DB has problem during offer's creation");
				}
			}
			/*Concludo prendendo tutta la lista delle offerte della rispettiva asta dopo il valore passato come get*/
			List<OfferBEAN> listOfOffer;
			try{
				listOfOffer = offerDao.getOfferAfterValue(id, lastOffer);
			}
			catch (SQLException e){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				throw new ErrorException("Error! DB has problem");
			}

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			Gson gson = new Gson();
			String json = gson.toJson(listOfOffer);
			response.getWriter().println(json);
		}
		catch (ErrorException e){
			response.getWriter().println("Error! "+ e.getMessage());
		}
		catch (NumberFormatException | NullPointerException e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String error = "Error! Parsing error or element is null";
			response.getWriter().println(error);
		}
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
