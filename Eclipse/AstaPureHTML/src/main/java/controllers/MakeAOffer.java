package controllers;

import java.io.IOException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import exceptions.ErrorException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.AuctionBEAN;
import beans.User;
import utils.DBConnector;

/**
 * Servlet implementation class MakeAOffer
 */
@WebServlet("/MakeAOffer")
public class MakeAOffer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MakeAOffer() {
        super();
    }
    public void init() throws UnavailableException {
    	ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		float offer = 0;
		int id = 0;
		try {
			/*
			 * Prendo l'asta di cui voglio fare l'offerta, mi servono alcuni suoi parametri, quindi non posso
			 * verificare solamente che l'asta effettivamente esiste come nel caso della GoToAuctionOffer
			 * */
			OfferDAO offerDao = new OfferDAO(connection);
			AuctionBEAN aubean = new AuctionBEAN();
			AuctionDAO audao = new AuctionDAO(connection);
			if (request.getParameter("auctionId") != null) {
				id = Integer.parseInt(request.getParameter("auctionId"));
				try {
					aubean = audao.getAuction(id);
				} catch (SQLException e) {
					throw new ErrorException("Error! database has some problem");
				}
				if (aubean == null)
					throw new ErrorException("Error! Auction does not exist");
			} else {
				throw new ErrorException("Error! Auction id parameter does not exists");
			}

			/**
			 * Vedo se l'offerta è valida, in questo caso se non lo è non è un problema del server o del database, ma dell'inserimento
			 * dell'utente. Quindi al posto di rimandarlo alla pagina iniziale gli do la possibilità di far di nuovo l'offerta
			 * avvisandolo che ci sono stati dei problemi nella precedente*/
			float actualOffer = 0;
			try {
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				offer = Float.parseFloat(request.getParameter("offer"));
				actualOffer = aubean.getMaxOffer();
				if (aubean.getIsClosed())
					throw new Exception("Auction is closed");
				else if (aubean.getStartDate().compareTo(timeStamp) > 0)
					throw new Exception("Auction is not open yet");
				if (offer < 0 || offer < actualOffer || offer - actualOffer < aubean.getMinimumRise()) {
					throw new Exception("Offer value not valid, your offer " + offer + ", actual offer: " + actualOffer);
				}
			} catch (Exception e) {
				String path = "GoToAuctionOffer?errorMessage=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8)
						+ "&auctionId=" + URLEncoder.encode(request.getParameter("auctionId"), StandardCharsets.UTF_8);
				response.sendRedirect(path);
				return;
			}

			/*
			 * Svolgo finalmente l'offerta*/
			try {
				offerDao.makeAOffer(aubean.getId(), (User) session.getAttribute("user"), offer);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure of offer creation in database, " + e);
				return;
			}
			response.sendRedirect("GoToAuctionOffer?auctionId=" + URLEncoder.encode(request.getParameter("auctionId"), StandardCharsets.UTF_8));
		}
		catch (ErrorException e){
			String pathError = getServletContext().getContextPath() + "/GoToBuy";
			pathError+="?errorMsg="+e.getMessage();
			response.sendRedirect(pathError);
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
