package com.example.Controller;


import com.example.Bean.AuctionBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.AuctionDAO;
import com.example.Utility.DBConnector;
import com.example.Utility.TimeCalculator;
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
import java.sql.Timestamp;
import java.util.List;


@WebServlet("/MyClosedAuctionServlet")
@MultipartConfig
public class MyClosedAuctionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    public MyClosedAuctionServlet() {
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
		AuctionDAO auctionDao = new AuctionDAO(connection);


		List<AuctionBEAN> closedAuctions = null;
		try {
			closedAuctions = auctionDao.findAllAuctionFromCreatorId(userId,true);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in user's closed auctions database extraction");
			return;
		}


		String json = new Gson().toJson(closedAuctions);
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
		} catch (SQLException ignored) {
		}
	}

}
