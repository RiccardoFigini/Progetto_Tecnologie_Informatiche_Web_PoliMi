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
import org.apache.commons.lang.StringEscapeUtils;
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
		String auctionId_s = StringEscapeUtils.escapeJava(request.getParameter("auctionId"));
		try {
			/*
			 * Prendo l'asta relativa all'offerta, mi servono alcuni suoi parametri, quindi non posso
			 * verificare solamente che l'asta effettivamente esiste come nel caso della GoToAuctionOffer.
			 * Mi servono dati come minimum rise e il prezzo attuale
			 * */
			OfferDAO offerDao = new OfferDAO(connection);
			AuctionBEAN aubean = new AuctionBEAN();
			AuctionDAO audao = new AuctionDAO(connection);
			if (auctionId_s != null) {
				try {
					id = Integer.parseInt(auctionId_s);
					aubean = audao.getAuction(id);
				} catch (SQLException e) {
					throw new ErrorException("Error! database has some problem");
				}
				catch (NumberFormatException e){
					throw new ErrorException("Error! Auction id is not a number");
				}
				if (aubean == null)
					throw new ErrorException("Error! Auction does not exist");
			} else {
				throw new ErrorException("Error! Auction id parameter does not exists");
			}

			/**
			 * Vedo se l'offerta è valida. In questo caso se non lo è non è un problema del server o del database, ma dell'inserimento
			 * dell'utente. Quindi al posto di rimandarlo alla pagina iniziale gli do la possibilità di far di nuovo l'offerta
			 * avvisandolo che ci sono stati dei problemi nella precedente*/
			float actualOffer;
			try {
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				offer = Float.parseFloat(StringEscapeUtils.escapeJava(request.getParameter("offer")));
				actualOffer = aubean.getMaxOffer();
				if (aubean.getIsClosed())
					throw new Exception("Auction is closed");
				else if (aubean.getStartDate().compareTo(timeStamp) > 0 || aubean.getEndDate().compareTo(timeStamp) < 0)
					throw new Exception("Auction is not open at this moment");
				if (	(
						offer<0 ||
								(!aubean.isAlreadyOffer() &&( offer<aubean.getStartPrice()))
								||(aubean.isAlreadyOffer() && (offer<=aubean.getMaxOffer() ||
								offer-aubean.getMaxOffer() < aubean.getMinimumRise()))
				))  {
					throw new Exception("Offer value not valid, your offer " + offer + ", actual offer: " + actualOffer);
				}
			} catch (Exception e) {
				String path = "GoToAuctionOffer?errorMessage=" + URLEncoder.encode("Error! " +e.getMessage(), StandardCharsets.UTF_8)
						+ "&auctionId=" + URLEncoder.encode(auctionId_s, StandardCharsets.UTF_8);
				response.sendRedirect(path);
				return;
			}

			/*
			 * Svolgo finalmente l'offerta se tutti i controlli sono andati a buon fine*/
			try {
				offerDao.makeAOffer(aubean.getId(), (User) session.getAttribute("user"), offer);
			} catch (SQLException e) {
				throw new ErrorException("Error! Database has some problem");
			}
			response.sendRedirect("GoToAuctionOffer?auctionId=" + URLEncoder.encode(auctionId_s, StandardCharsets.UTF_8));
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
