package controllers;



import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DAO.ArticleDAO;
import beans.User;
import utils.DBConnector;

/**
 * Servlet implementation class GetFile
 */
@WebServlet("/GetImage/*")
public class GetImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Connection connection;
	String default_filePath;

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
		default_filePath="defaultImage.png";
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String articleId_s = request.getParameter("articleID");
		String path= getServletContext().getInitParameter("images_folder");
		ArticleDAO articleDao = new ArticleDAO(connection);
		int articleId=0;
		String imagePath=null;

		/** Faccio il parsing dell'id fornito nella queryString*/
		try
		{
			articleId = Integer.valueOf(articleId_s);
		}
		catch(NumberFormatException | NullPointerException e)
		{
			getImage(path+default_filePath,response);
			return;
		}

		/** Prendo il percorso dell'immagine dell'articolo*/
		try
		{
			imagePath=articleDao.getImagePath(articleId);
		}
		catch (SQLException e)
		{
			getImage(path+default_filePath,response);
			return;
		}

		/** Se imagePath è null significa che la queryString non ha trovato l'articolo o il percorso nel DB*/
		if(imagePath==null) {
			getImage(path+default_filePath,response);
		}
		else {
			getImage(path+imagePath,response);
		}
		
	}

	private void getImage(String path,HttpServletResponse response) throws IOException 
	{
		File file = new File(path); 

		if (!file.exists() || file.isDirectory()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Default image not found");
			return;
		}
		response.setHeader("Content-Type", getServletContext().getMimeType(path));
		response.setHeader("Content-Length", String.valueOf(file.length()));
		response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");												
		Files.copy(file.toPath(), response.getOutputStream());	
	}
}
