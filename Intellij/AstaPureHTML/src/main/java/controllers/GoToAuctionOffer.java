package controllers;

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
import exceptions.ErrorException;
import org.apache.commons.lang.StringEscapeUtils;
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
import utils.ImageEncoder;

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
			 * se il parametro non è null ed anche se l'asta è disponibile (aperta). COntorllo quindi che la
			 * data attuale sia oltre a quella di inizio e prima di quella di fine*/
			AuctionBEAN auB;
			int id;

			if (StringEscapeUtils.escapeJava(request.getParameter("auctionId")) != null) {
				try {
					id = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("auctionId")));
					AuctionDAO auDAO = new AuctionDAO(connection);
					auB = auDAO.exist(id);
				}catch (NumberFormatException | NullPointerException e ) {
					throw new ErrorException("Error! Bad parsing for the ID");
				} catch (SQLException e) {
					throw new ErrorException("Error! Database has some problems");
				}
				if (auB == null)
					throw new ErrorException("Selected auction does not exists, id: " + id);
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				if (auB.getStartDate().compareTo(timeStamp) > 0 || auB.getEndDate().compareTo(timeStamp) < 0)
					throw new ErrorException("Auction is not open at this moment");
			} else {
				throw new ErrorException("Can not reach id auction");
			}

			/*Prendo la lista di tutte le offerti di una determinata asta, se non esistono scrivo come prezzo
			 * di partenza nel titolo pagina la base d'asta; altrimenti l'ultima offerta*/
			List<OfferBEAN> list;
			OfferDAO offerDAO = new OfferDAO(connection);
			try {
				list = offerDAO.getAllAuctionsOffer(id);
				/*Questo controllo serve per impostare il titolo della pagina, quindi a seconda se sono già
				* state svolte offerte o meno assegna un valore all'offerta attuale*/
				if (list == null || list.size() == 0) {
					ctx.setVariable("actualOffer", auB.getStartPrice());
				} else {
					ctx.setVariable("actualOffer", list.get(0).getPrice());
				}
			} catch (SQLException e) {
				throw new ErrorException("Error! database has some problem. Details");
			}
			ctx.setVariable("offers", list);

			/*Prendo tutti gli articoli, con le loro informazioni, dell'asta*/
			List<ArticleBEAN> listArticle;
			ArticleDAO artDAO = new ArticleDAO(connection);
			try {
				listArticle = artDAO.getAllInformationFromAuctionId(id);
			} catch (SQLException e) {
				throw new ErrorException("Error! database has some problem");
			}

			/** Metto le immagini negli articoli */
			if(listArticle != null) {
				listArticle.stream().forEach(x ->
				{
					x.setEncodedImage(ImageEncoder.encodeImage(getServletContext(), x.getImagePath()));
				});
			}

			/*Quando viene fatta un'offerta, per inserirla all'interno della pagina, viene richiamata questa servlet.
			* quindi quetsa servlet è richiamata sia dalla pagina con la lista delle aste sia da make a offer
			* Se l'offerta presenta degli errori questi sono inoltrati (metodo get). Quindi questo if risulta vero
			* solo in caso di offerta non valida*/
			if (StringEscapeUtils.escapeJava(request.getParameter("errorMessage")) != null)
				ctx.setVariable("errorMessage", StringEscapeUtils.escapeJava(request.getParameter("errorMessage")));

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
