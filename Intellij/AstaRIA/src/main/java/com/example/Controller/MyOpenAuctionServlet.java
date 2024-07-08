package com.example.Controller;


import com.example.Bean.AuctionBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.ArticleDAO;
import com.example.Dao.AuctionDAO;
import com.example.Utility.DBConnector;
import com.example.Utility.TimeCalculator;
import com.google.gson.Gson;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

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
import java.sql.Timestamp;
import java.util.List;


@WebServlet("/MyOpenAuctionServlet")
@MultipartConfig
public class MyOpenAuctionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    public MyOpenAuctionServlet() {
        super();
    }

    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		UserBean user = (UserBean) session.getAttribute("user");
		Timestamp timeLogin = (Timestamp) session.getAttribute("timeLogin");
		int userId= user.getId();

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


		String json = new Gson().toJson(openAuctions);
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
