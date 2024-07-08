package com.example.Controller;
import com.example.Bean.ArticleBEAN;
import com.example.Bean.UserBean;
import com.example.Dao.ArticleDAO;
import com.example.Utility.DBConnector;
import com.example.Utility.ImageEncoder;
import com.example.exception.ErrorException;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
@WebServlet("/CreateArticleServlet")
@MultipartConfig
public class CreateArticleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	String folderPath = "";
	public CreateArticleServlet() {
		super();
	}
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		/*Come nel caso della connessione al DB prendo il percorso in cui salvare le immagini
		* dal file web. Ricordo che è salvato tramite una mappa chiave-valore*/
		folderPath = servletContext.getInitParameter("images_folder");
		connection = DBConnector.getConnection(servletContext);
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		/*Estrazione di tutti gli elementi passati tramite form*/
		String articleName = StringEscapeUtils.escapeJava(request.getParameter("articleName"));
		String description = StringEscapeUtils.escapeJava(request.getParameter("description"));
		String minPrice_s = StringEscapeUtils.escapeJava(request.getParameter("minimumPrice"));
		String keyWord = StringEscapeUtils.escapeJava(request.getParameter("keyWord"));
		Part filePart = null;
		String path = null;
		ArticleBEAN articleBEAN = null;

		UserBean user = (UserBean) session.getAttribute("user");
		ArticleDAO articleDAO = new ArticleDAO(connection);
		
		try {

			/* Controlli sui dati forniti dal form */

			int creatorId = user.getId();
			float minimumPrice;
			try {
				if (articleName == null || description == null || keyWord == null
						|| articleName.isEmpty() || description.isEmpty() || keyWord.isEmpty()) {
					throw new NullPointerException();
				}
				minimumPrice = Float.parseFloat(minPrice_s);
				if (minimumPrice <= 0) {
					throw new IllegalArgumentException();
				}
			} catch (NumberFormatException e) {
				throw new ErrorException("Error! Incorrect format for minimum price.");
			} catch (NullPointerException e1) {
				throw new ErrorException("Error! Some required field were submitted empty.");
			} catch (IllegalArgumentException e) {
				throw new ErrorException("Error! Minimum price can't be less or equal to zero.");
			}

			/* Memorizzerò la keyWord in minuscolo, così la ricerca tramite essa è piu efficace*/
			keyWord = keyWord.toLowerCase();



			/* Controllo che il file fornito non sia vuoto e sia una immagine*/
			try
			{
				filePart=request.getPart("articleIMG");
			}
			catch (Exception e){
				throw new ErrorException("Error! filePart conversion went wrong");
			}
			if (filePart == null || filePart.getSize() <= 0) {
				throw new ErrorException("Error! An image for the article is required.");
			}
			String contentType = filePart.getContentType();
			if (contentType == null || !contentType.startsWith("image")) {
				throw new ErrorException("Error! Please submit a file with image format.");
			}

			/* Creo un nome univoco per il file sfruttando il timestamp del momento della creazione*/
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String timestamp = sdf.format(new Date());
			String fileName = "local_filename_" + timestamp+".png";
			String imgPath = folderPath + fileName;

			/* Salvo il file ricevuto dall'utente in locale
			 *  Ho deciso di farlo prima della query perchè l'apertura di un file può andare male,
			 *  se avessi fatto prima la query e poi l'apertura del file fosse andata male, avrei
			 *  dovuto cancellare l'inserimento nel DB.
			 * */
			File file = new File(imgPath);
			try (InputStream fileContent = filePart.getInputStream()) {
				Files.copy(fileContent, file.toPath());
			} catch (IOException e) {
				if (file.exists()) {
					file.delete();
				}
				throw new ErrorException("Error! While saving the image file in the local folder.");
			}



			/** Creazione dell'articolo. Distruggo il file in caso di errore nella query*/
			try {
				articleBEAN = articleDAO.createArticle(articleName, description, minimumPrice, keyWord, creatorId, fileName);
			} catch (SQLException e) {
				if (file.exists()) {
					file.delete();
				}
				throw new ErrorException("Error! While creating the article.");
			}

			if(articleBEAN!=null)
				articleBEAN.setEncodedImage(ImageEncoder.encodeImage(getServletContext(),articleBEAN.getImagePath()));
		}
		catch (ErrorException e)
		{
			/* Torna indietro con un errorMsg*/
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}



		String json = new Gson().toJson(articleBEAN);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(json);
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