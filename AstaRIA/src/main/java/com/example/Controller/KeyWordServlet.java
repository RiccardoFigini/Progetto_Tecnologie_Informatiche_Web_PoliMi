package com.example.Controller;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.example.Bean.AuctionBEAN;
import com.example.Dao.AuctionDAO;
import com.example.Utility.DBConnector;
import com.example.exception.ErrorException;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;


/**
 * Servlet implementation class KeyWordServlet
 */
@WebServlet("/KeyWordServlet")
@MultipartConfig
public class KeyWordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KeyWordServlet() {
		super();
	}

	public void init() throws UnavailableException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			/*Questa servlet, a differenza della corrispondente nel progetto versione html, estrae solo le aste aperte
			 * con una determinata parola nella descrizione o come keyword */
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			List<AuctionBEAN> listOfAuction;
			AuctionDAO astaDao = new AuctionDAO(connection);
			String paramFromUser = StringEscapeUtils.escapeJava(request.getParameter("keyword"));
			if (paramFromUser != null && paramFromUser.length()!=0) {
				try {
					listOfAuction = astaDao.openAuctionsWithKeyWord(paramFromUser.toLowerCase(), new Timestamp(System.currentTimeMillis()));
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					throw new ErrorException("Error! Db has problem");
				}
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new ErrorException("Error! Parameter is null or empty");
			}
			String json = new Gson().toJson(listOfAuction);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(json);
		}
		catch (ErrorException e){
			String error = e.getMessage();
			response.getWriter().println(error);
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
