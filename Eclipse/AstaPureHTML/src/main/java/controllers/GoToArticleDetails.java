package controllers;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.ArticleDAO;
import beans.ArticleBEAN;
import beans.User;
import utils.DBConnector;

/**
 * Servlet implementation class GoToArticleDetails
 */
@WebServlet("/GoToArticleDetails")
public class GoToArticleDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToArticleDetails() {
        super();
        // TODO Auto-generated constructor stub
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
		ArticleDAO audao = new ArticleDAO(connection);	
		int id = 0;
		
		try {
			id = Integer.parseInt(request.getParameter("itemId"));
			System.out.print(id);
			if(audao.exist(id)== -1)
				throw new Exception("Article does not exists");
		}
		catch(Exception e) {
			//Capire se gestire qualcosa si particolare
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage() + " from GoToArticleDetails");
			return;
		}
		
		ArticleBEAN arBE = null;
		try {
			arBE = audao.getAllInformation(id);
			if(arBE == null)
				throw new Exception("Article does not exists");
		}
		catch(Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage() + " from GoToArticleDetails");
			return;
		}
		
		
		String owner = null;
		try {
			owner = audao.getOwnerName(id);
		}
		catch(Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage() + " from GoToArticleDetails");
			return;
		}
		
		String path = "/WEB-INF/articleDetails.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		

		ctx.setVariable("Owner", owner);
		ctx.setVariable("article", arBE);
		ctx.setVariable("auctionId", request.getParameter("auctionId"));
		ctx.setVariable("username", ((User) request.getSession().getAttribute("user")).getUsername()  );
		ctx.setVariable("timeLogin",  request.getSession().getAttribute("timeLogin") );
		
		templateEngine.process(path, ctx, response.getWriter());

		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
