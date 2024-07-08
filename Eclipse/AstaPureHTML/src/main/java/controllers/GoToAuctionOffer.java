package controllers;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import exceptions.ErrorException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.ArticleBEAN;
import beans.AuctionBEAN;
import beans.OfferBEAN;
import beans.User;
import utils.DBConnector;

/**
 * Servlet implementation class GoToAuctionOffer
 */
@WebServlet("/GoToAuctionOffer")
public class GoToAuctionOffer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToAuctionOffer() {
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
		String path = "/WEB-INF/auctionOffer.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		try {
			/*Controllo se l'id dell'asta passato tramite metodo get è corretto. Quindi se l'asta esiste,
			 * se il parametro non è null*/
			AuctionBEAN auB;
			int id = 0;
			if (request.getParameter("auctionId") != null) {
				id = Integer.parseInt(request.getParameter("auctionId"));
				AuctionDAO auDAO = new AuctionDAO(connection);
				try {
					auB = auDAO.exist(id);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				if (auB == null)
					throw new ErrorException("Selected auction does not exists");
			} else {
				throw new ErrorException("Can not reach id auction");
			}

			/*Prendo la lista di tutte le offerti di una determinata asta, se non esistono scrivo come prezzo
			 * di partenza nel titolo pagina la base d'asta; altrimenti l'ultima offerta*/
			List<OfferBEAN> list = new ArrayList<>();
			OfferDAO offerDAO = new OfferDAO(connection);
			try {
				list = offerDAO.getAllAuctionsOffer(id);
				if (list == null || list.size() == 0) {
					ctx.setVariable("actualOffer", auB.getStartPrice());
				} else {
					ctx.setVariable("actualOffer", list.get(0).getPrice());
				}
			} catch (SQLException e) {
				throw new ErrorException("Error! database has some problem");
			}
			ctx.setVariable("offers", list);

			/*Prendo tutte le informazioni dell'asta, quindi nello specifico tutti gli articoli con la loro
			 * descrizione*/
			List<ArticleBEAN> listArticle = new ArrayList<>();
			ArticleDAO artDAO = new ArticleDAO(connection);
			try {
				listArticle = artDAO.getAllInformationFromAuctionId(id);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in article DB" + e);
				return;
			}

			if (request.getParameter("errorMessage") != null)
				ctx.setVariable("errorMessage", request.getParameter("errorMessage"));
			ctx.setVariable("articles", listArticle);
			ctx.setVariable("minimumRise", auB.getMinimumRise());
			ctx.setVariable("auctionId", id);
			ctx.setVariable("username", ((User) request.getSession().getAttribute("user")).getUsername());
			ctx.setVariable("timeLogin", request.getSession().getAttribute("timeLogin"));
			templateEngine.process(path, ctx, response.getWriter());
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
