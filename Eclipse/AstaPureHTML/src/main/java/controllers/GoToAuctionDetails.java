package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import exceptions.ErrorException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.AuctionBEAN;
import beans.OfferBEAN;
import beans.User;
import utils.DBConnector;

@WebServlet("/GoToAuctionDetails")
public class GoToAuctionDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
    private Connection connection;
    public GoToAuctionDetails() {
        super();
    }
    
    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = DBConnector.getConnection(servletContext);
	}
    

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		int userId = user.getId();
		AuctionDAO auctionDao = new AuctionDAO(connection);
		String path=null;
		AuctionBEAN auctionBean=null;
		List<OfferBEAN> list= null;
		
		try {


			/** Controllo che l'id passato da queryString sia un intero e non nullo*/
			Integer auctionId = null;
			try {
				auctionId = Integer.parseInt(request.getParameter("ID"));
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

			/** Se l'utente in sessione non è il proprietario dell'asta */
			if (auctionBean.getUserIdOwner() != userId) {
				throw new ErrorException("Error! Permission denied.");
			}


			/** Estraggo tutte le offerte ricevute per l'asta ordinate in ordine decrescente di prezzo,
			 * quindi la prima è la vincente*/
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
			/** Torna indietro con un errorMsg*/
			path = getServletContext().getContextPath() + "/GoToSell";
			path+="?errorMsg="+e.getMessage();
			response.sendRedirect(path);
			return;
		}
		
		path = "/WEB-INF/AuctionDetails.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("auction", auctionBean);
		ctx.setVariable("offers", list );
		ctx.setVariable("username", user.getUsername());
		ctx.setVariable("timeLogin",  request.getSession().getAttribute("timeLogin") );
		templateEngine.process(path, ctx, response.getWriter());
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
