package com.example.Controller;


import com.example.Bean.ArticleBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.ArticleDAO;
import com.example.Utility.DBConnector;
import com.example.Utility.ImageEncoder;
import com.google.gson.Gson;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


@WebServlet("/MyAvailableArticlesServlet")
@MultipartConfig
public class MyAvailableArticlesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    public MyAvailableArticlesServlet() {
        super();
    }

    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		UserBean user = (UserBean) session.getAttribute("user");
		int userId= user.getId();

		ArticleDAO articleDAO = new ArticleDAO(connection);

		/** Prende gli articoli disponibili, che l'utente può inserire nelle aste che vuole creare*/
		List<ArticleBEAN> availableArticles = null;
		try {
			availableArticles = articleDAO.findUserArticles(userId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in user's articles database extraction");
			return;
		}
		if(availableArticles!=null)
		availableArticles.stream().forEach(x -> {
			x.setEncodedImage(ImageEncoder.encodeImage(getServletContext(),x.getImagePath()));
		});


		String json = new Gson().toJson(availableArticles);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(json);
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
