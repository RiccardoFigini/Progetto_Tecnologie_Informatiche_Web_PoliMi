package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import exceptions.ErrorException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.*;
import utils.*;
import beans.*;

@WebServlet("/CreateArticle")
@MultipartConfig
public class CreateArticle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	String folderPath = "";

	public CreateArticle() {
		super();
	}

	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		folderPath = servletContext.getInitParameter("images_folder");
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = DBConnector.getConnection(servletContext);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String articleName = request.getParameter("articleName");
		String description = request.getParameter("description");
		String minPrice_s = request.getParameter("minimumPrice");
		String keyWord = request.getParameter("keyWord");
		String path = null;

		Part filePart = request.getPart("articleIMG"); 
		User user = (User) session.getAttribute("user");
		ArticleDAO articleDAO = new ArticleDAO(connection);
		
		try {


			/** Controlli sui dati forniti dal form */
			int creatorId = user.getId();
			float minimumPrice = 0;
			try {
				if (articleName == null || description == null || keyWord == null
						|| articleName.isBlank() || description.isBlank() || keyWord.isBlank()) {
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



			/** Memorizzerò la keyWord in minuscolo, così la ricerca tramite essa è piu utile*/
			keyWord = keyWord.toLowerCase();



			/** Controllo che il file fornito non sia vuoto e sia una immagine*/
			if (filePart == null || filePart.getSize() <= 0) {
				throw new ErrorException("Error! An image for the article is required.");
			}
			String contentType = filePart.getContentType();
			if (contentType == null || !contentType.startsWith("image")) {
				throw new ErrorException("Error! Please submit a file with image format.");
			}



			/** Creo un nome univoco per il file sfruttando il timestamp del momento della creazione*/
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String timestamp = sdf.format(new Date());
			String fileName = "local_filename_" + timestamp;
			String imgPath = folderPath + fileName;



			/** Salvo il file ricevuto dall'utente in locale
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
				articleDAO.createArticle(articleName, description, minimumPrice, keyWord, creatorId, fileName);
			} catch (SQLException e) {
				if (file.exists()) {
					file.delete();
				}
				throw new ErrorException("Error! While creating the article.");
			}
		}
		catch (ErrorException e)
		{
			/** Torna indietro con un errorMsg*/
			path = getServletContext().getContextPath() + "/GoToSell";
			path+="?errorMsg="+e.getMessage();
			response.sendRedirect(path);
			return;
		}


		path = getServletContext().getContextPath() + "/GoToSell";
		response.sendRedirect(path);
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