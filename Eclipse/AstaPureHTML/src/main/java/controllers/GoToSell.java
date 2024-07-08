package controllers;



import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.*;
import beans.*;
import utils.DBConnector;
import utils.TimeCalculator;


@WebServlet("/GoToSell")
public class GoToSell extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
    public GoToSell() {
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
		String errorMsg = request.getParameter("errorMsg");
		User user = (User) session.getAttribute("user");
		Timestamp timeLogin = (Timestamp) session.getAttribute("timeLogin");
		int userId= user.getId();

		ArticleDAO articleDAO = new ArticleDAO(connection);
		AuctionDAO auctionDao = new AuctionDAO(connection);



		/** Restituisce una lista aventualmente vuota contenete aste aperte e ne setta il tempo rimanente rispetto al login */
		List<AuctionBEAN> openAuctions = null;
		try {
			openAuctions = auctionDao.findAllAuctionFromCreatorId(userId,false);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in user's open auctions database extraction");
			return;
		}
		/** Inserisce il tempo rimanente nei beans delle aste */
		if(openAuctions != null)
		{
			for(AuctionBEAN a : openAuctions)
			{
				a.setRemainingTime(TimeCalculator.calculateRemainingTime(timeLogin,a.getEndDate()));
			}
		}
		
		
		/** Restituisce una lista aventualmente vuota contenete aste chiuse*/
		List<AuctionBEAN> closedAuctions = null;
		try {
			closedAuctions = auctionDao.findAllAuctionFromCreatorId(userId,true);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in user's closed auctions database extraction");
			return;
		}

		/** Prende gli articoli disponibili, che l'utente può inserire nelle aste che vuole creare*/
		List<ArticleBEAN> availableArticles = null;
		try {
			availableArticles = articleDAO.findUserArticles(userId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in user's articles database extraction");
			return;
		}

		
		String path = "/WEB-INF/Sell.html";
		ServletContext servletContext = getServletContext();
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		ctx.setVariable("errorMsg", errorMsg);
		ctx.setVariable("openAuctions", openAuctions);
		ctx.setVariable("closedAuctions", closedAuctions);
		ctx.setVariable("availableArticles", availableArticles);
		ctx.setVariable("username", user.getUsername());
		ctx.setVariable("timeLogin",  timeLogin);

		templateEngine.process(path, ctx, response.getWriter());
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
