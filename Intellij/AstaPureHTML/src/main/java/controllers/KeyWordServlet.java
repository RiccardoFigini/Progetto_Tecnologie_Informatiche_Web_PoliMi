package controllers;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import DAO.*;
import beans.*;
import utils.*;
/**
 * Servlet implementation class KeyWordServlet
 */
@WebServlet("/KeyWordServlet")
public class KeyWordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KeyWordServlet() {
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
		/*
		 * Selezione tutte le aste con la condizione che la keyword passata nel get compaia nella descrizione o sia la keyword
		 * di uno degli articoli */
		try {
			List<AuctionBEAN> listOfAuction = new ArrayList<>();
			AuctionDAO astaDao = new AuctionDAO(connection);
			String paramFromUser = StringEscapeUtils.escapeJava(request.getParameter("keyword"));
			if (paramFromUser != null) {
				try {
					listOfAuction = astaDao.openAuctionsWithKeyWord(paramFromUser.toLowerCase(), new Timestamp(System.currentTimeMillis()));
				} catch (SQLException e) {
					throw new ErrorException("Error! Database has some problems");
				}
			} else {
				throw new ErrorException("Error! Param keyword is null");
			}

			//Stringhe di "bellezza"
			String path = "/WEB-INF/buying.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("text", "Auction with keywrod: " + paramFromUser);
			ctx.setVariable("listOfAuction", listOfAuction);

			/*Selezione lo storico delle aste dell'utente. Devono essere sempre presenti nella pagina*/
			User user = (User) session.getAttribute("user");
			List<AuctionBEAN> listOfAuctionEnded = null;
			try {
				listOfAuctionEnded = astaDao.getEndAuction(user.getId());
			} catch (SQLException e) {
				throw new ErrorException("Error! Database has some problems");
			}
			ctx.setVariable("listOfEndedAuction", listOfAuctionEnded);
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
