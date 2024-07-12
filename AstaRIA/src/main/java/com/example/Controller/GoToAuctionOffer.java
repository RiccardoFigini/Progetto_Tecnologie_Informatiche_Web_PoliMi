package com.example.Controller;
import java.io.IOException;
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
import com.example.Bean.ArticleBEAN;
import com.example.Bean.AuctionBEAN;
import com.example.Bean.OfferBEAN;
import com.example.Dao.ArticleDAO;
import com.example.Dao.AuctionDAO;
import com.example.Dao.OfferDAO;
import com.example.PackWeb.PackAuctionInformation;
import com.example.Utility.DBConnector;
import com.example.Utility.ImageEncoder;
import com.example.exception.ErrorException;
import com.google.gson.*;
import org.apache.commons.lang.StringEscapeUtils;


/**
 * Servlet implementation class GoToAuctionOffer
 */
@MultipartConfig
@WebServlet("/GoToAuctionOffer")
public class GoToAuctionOffer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

	public void init() throws UnavailableException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
	}

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToAuctionOffer() {
        super();
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			/*Controllo se l'id dell'asta passato tramite metodo get è corretto. Quindi se l'asta esiste,
			 * se il parametro non è null*/
			AuctionBEAN auB;
			int id;
			String auctionId_s = StringEscapeUtils.escapeJava(request.getParameter("auctionId"));
			if (auctionId_s != null) {
				try {
					id = Integer.parseInt(auctionId_s);
					AuctionDAO auDAO = new AuctionDAO(connection);
					//Verifico che l'asta di cui voglio le informazioni esiste effettivamente
					auB = auDAO.exist(id);
				}catch (NumberFormatException | NullPointerException e ) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new ErrorException("Error! Bad parsing for the ID");
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new ErrorException("Error! DB has some problem");
				}
				if (auB == null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new ErrorException("Selected auction does not exists");
				}
				Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
				if (auB.getStartDate().compareTo(timeStamp) > 0 || auB.getEndDate().compareTo(timeStamp) < 0) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					throw new ErrorException("Auction is not open at this moment");
				}
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				throw new ErrorException("Can not reach id auction");
			}

			/*Prendo la lista di tutte le offerte di una determinata asta, se non esistono scrivo come prezzo
			 * di partenza nel titolo pagina la base d'asta; altrimenti l'ultima offerta*/
			List<OfferBEAN> list;
			OfferDAO offerDAO = new OfferDAO(connection);
			try {
				list = offerDAO.getAllAuctionsOffer(id);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				throw new ErrorException("Error! database has some problem");
			}
			/*Prendo tutte le informazioni dell'asta, quindi nello specifico tutti gli articoli con la loro
			 * descrizione*/
			List<ArticleBEAN> listArticle;
			ArticleDAO artDAO = new ArticleDAO(connection);
			try {
				listArticle = artDAO.getAllInformationFromAuctionId(id);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				throw new ErrorException("Error! DB cannot extract article");
			}


			//Aggiungo l'immagine "tradotta" in base64 in modo tale da poterla spedire tramite json
			if(listArticle!=null)
			listArticle.stream().forEach(x -> {
				x.setEncodedImage(ImageEncoder.encodeImage(getServletContext(),x.getImagePath()));
			});

			Gson gson = new Gson();
			PackAuctionInformation packAuctionInformation = new PackAuctionInformation(listArticle, list, auB.getMinimumRise(), auB.getStartPrice() );
			String json = gson.toJson(packAuctionInformation);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
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
