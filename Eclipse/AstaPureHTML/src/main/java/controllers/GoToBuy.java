package controllers;

import java.io.IOException;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import beans.AuctionBEAN;
import beans.User;
import utils.DBConnector;

/**
 * Servlet implementation class GoToBuy
 */
@WebServlet("/GoToBuy")
public class GoToBuy extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	
    public GoToBuy() {
        super();
    }

    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}
    
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		try {
			/**
			 * Quando apro la pagina la prima volta mostro la lista di tutte le aste in generale; non facendo riferimento
			 * a nessuna keyeord che deve essere ancora inserita*/
			List<AuctionBEAN> listOfAuction;
			AuctionDAO astaDao = new AuctionDAO(connection);
			try {
				listOfAuction = astaDao.allAuction(new Timestamp(System.currentTimeMillis()));
			} catch (SQLException e) {
				throw new ErrorException("Error, database has some problem");
			}
			//Stringhe di "bellezza"
			String text = "You have not searched something yet, here all open auction";
			ctx.setVariable("listOfAuction", listOfAuction);
			ctx.setVariable("introduction", "There are " + listOfAuction.size() + " open auction!");
			ctx.setVariable("text", text);

			/*Prendo le aste già vinte dall'utente di cui so l'id perché è in sessione*/
			User user = (User) session.getAttribute("user");
			List<AuctionBEAN> listOfAuctionEnded = null;
			try {
				listOfAuctionEnded = astaDao.getEndAuction(user.getId());
			} catch (SQLException e) {
				throw new ErrorException("Error, database has some problem");
			}
			ctx.setVariable("listOfEndedAuction", listOfAuctionEnded);

			/*Imposto quello che poi finirà in "aside"*/
			ctx.setVariable("username", ((User) session.getAttribute("user")).getUsername());
			ctx.setVariable("timeLogin", session.getAttribute("timeLogin"));
			ctx.setVariable("errorMsg", request.getParameter("errorMsg"));
			String path = "/WEB-INF/buying.html";
			templateEngine.process(path, ctx, response.getWriter());
		}
		catch (ErrorException e){
			String path = getServletContext().getContextPath() + "/GoToHome";
			path+="?errorMsg="+e.getMessage();
			response.sendRedirect(path);
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
