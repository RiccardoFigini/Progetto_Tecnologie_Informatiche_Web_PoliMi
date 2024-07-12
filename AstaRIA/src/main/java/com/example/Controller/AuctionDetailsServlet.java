package com.example.Controller;
import com.example.Bean.AuctionBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.AuctionDAO;
import com.example.Utility.DBConnector;
import com.example.Utility.ImageEncoder;
import com.example.exception.ErrorException;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;

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
@WebServlet("/AuctionDetailsServlet")
@MultipartConfig
public class AuctionDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    public AuctionDetailsServlet() {
        super();
    }

    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		UserBean user = (UserBean) session.getAttribute("user");
		int userId = user.getId();
		AuctionDAO auctionDao = new AuctionDAO(connection);
		AuctionBEAN auctionBean=null;

		try {


			/** Controllo che l'id passato da queryString sia un intero e non nullo*/
			Integer auctionId = null;
			try {
				auctionId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("ID")));
			} catch (NumberFormatException | NullPointerException e) {
				throw new ErrorException("Error! Incorrect value for parameter ID.");
			}


			/** Estraggo l'asta richiesta da DB. Estraggo anche gli articoli con una
			 * sotto query con le relative immagine e informazioni */
			try {
				auctionBean = auctionDao.getAuctionDetailsFromId(auctionId);
			} catch (SQLException e) {
				throw new ErrorException("Error! Failure in auction details extraction.");
			}


			/** Se la query non trova nulla */
			if (auctionBean == null) {
				throw new ErrorException("Error! Auction not found.");
			}

			/** Se l'utente in sessione non è il proprietario dell'asta */
			if (auctionBean.getUserIdOwner() != userId) {
				throw new ErrorException("Error! Permission denied.");
			}

			/** Codifica le immagini nelle liste di articoli contenuti in ogni asta in base-64
			 * in modo tale da poter salvare l'immagine su una stringa*/
			auctionBean.getListOfArticle().stream().forEach(x -> {
				x.setEncodedImage(ImageEncoder.encodeImage(getServletContext(),x.getImagePath()));
			});

		}
		catch (ErrorException e)
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}

		/*Se è tutto okay ritorno quindi i dettagli dell'asta*/
		String json = new Gson().toJson(auctionBean);
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
