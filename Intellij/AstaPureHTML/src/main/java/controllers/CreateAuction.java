package controllers;

import java.io.IOException;


import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import DAO.ArticleDAO;
import DAO.AuctionDAO;
import beans.ArticleBEAN;
import beans.User;
import exceptions.ErrorException;
import org.apache.commons.lang.StringEscapeUtils;
import utils.DBConnector;

@WebServlet("/CreateAuction")
public class CreateAuction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
   
    public CreateAuction() {
        super();
    }

    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String minimumRise_s = StringEscapeUtils.escapeJava(request.getParameter("minimumRise"));
		String startDate_s = StringEscapeUtils.escapeJava(request.getParameter("startDate"));
		String startTime_s = StringEscapeUtils.escapeJava(request.getParameter("startTime"))+":00";
		String endDate_s = StringEscapeUtils.escapeJava(request.getParameter("endDate"));
		String endTime_s = StringEscapeUtils.escapeJava(request.getParameter("endTime"))+":00";
		String[] selectedArticlesCodes_s = request.getParameterValues("articleCode");
		String path = null;
		
		User user = (User) session.getAttribute("user");
		int creatorId =user.getId();
		ArticleDAO articleDao = new ArticleDAO(connection);
		
		int minimumRise = 0;
		Date startDate = null;
		Date endDate = null;
		Time startTime = null;
		Time endTime = null;
		String startDateTime = null;
		String endDateTime = null;
		Integer[] selectedArticlesCodes = null;


		try {



			/** Controllo che ci sia almeno un codice, che siano degli Int e li converto*/
			if (selectedArticlesCodes_s == null || selectedArticlesCodes_s.length == 0) {
				throw new ErrorException("Error! No articles selected for the auction.");
			} else {
				try {
					selectedArticlesCodes = new Integer[selectedArticlesCodes_s.length];
					for (int i = 0; i < selectedArticlesCodes_s.length; i++) {
						selectedArticlesCodes[i] = Integer.parseInt(selectedArticlesCodes_s[i]);
					}
				} catch (NumberFormatException | NullPointerException e) {
					throw new ErrorException("Error! Bad parsing of articles id");
				}
			}



			/** Controlli sugli altri dati del form*/
			try {
				if (minimumRise_s == null || startDate_s == null || startTime_s == null || endDate_s == null || endTime_s == null
						|| minimumRise_s.isBlank() || startDate_s.isBlank() || startTime_s.isBlank() || endDate_s.isBlank() || endTime_s.isBlank()) {
					throw new ErrorException("Error! Some field were submitted empty.");
				}

				minimumRise = Integer.parseInt(minimumRise_s);
				if (minimumRise <= 0) {
					throw new ErrorException("Error! Minimum rise must be greater than zero.");
				}
				startDate = Date.valueOf(startDate_s);
				endDate = Date.valueOf(endDate_s);
				startTime = Time.valueOf(startTime_s);
				endTime = Time.valueOf(endTime_s);
				if (!correctDates(startDate, startTime, endDate, endTime)) {
					throw new ErrorException("Error! Bad choice of dates and/or times.");
				}
				startDateTime = startDate.toString() + " " + startTime.toString();
				endDateTime = endDate.toString() + " " + endTime.toString();
			} catch (NumberFormatException e) {
				throw new ErrorException("Error! Bad parsing for minimum rise.");
			} catch (IllegalArgumentException e2) {
				throw new ErrorException("Error! Dates and/or times were not in the correct format.");
			}



			/** Cerco nel database la lista degli articoli disponibili che l'utente avrebbe potuto mettere nell'asta.
			 *  Controllo che per ogni articolo selezionato dall'utente sia in questa lista e che l'utente non abbia messo duplicati
			 *  availableCodes è un set in cui inserisco man mano i codici degli articoli, se l'inserimento restituisce FALSE allora
			 *  l'utente ha scelto dei duplicati.*/
			List<ArticleBEAN> availableArticles = null;
			Set<Integer> availableCodes = new HashSet<>();
			for (int item : selectedArticlesCodes) {
				try {
					availableArticles = articleDao.findUserArticles(creatorId);
				} catch (SQLException e) {
					throw new ErrorException("Error! Failure while checking articles in the database.");
				}

				if (availableArticles == null ||
						(availableArticles.stream().map(x -> x.getCode()).collect(Collectors.toList()).contains(item) == false) ||
						!(availableCodes.add(item)))
				{
					throw new ErrorException("Error! Unexpected choice of articles");
				}

			}


			/** Sommo i prezzi degli articoli elezionati */
			List<Integer> selectedArticlesList = Arrays.asList(selectedArticlesCodes);
			Float startPrice = availableArticles.stream()
					.filter(x -> selectedArticlesList.contains(x.getCode()))
					.map(x -> x.getMinimumPrice())
					.reduce(0.0f, (a, b) -> a + b);
			/** Se si inseriscono talmente tanti articoli da superare il valore massimo dei float (improbabile) startPrice diventa negativo*/
			if(startPrice<=0)
			{
				throw new ErrorException("Error! The amount of articles exceeded the maximum price");
			}


			/** Inserisco l'asta e le assegno gli articoli*/
			AuctionDAO auctionDAO = new AuctionDAO(connection);
			try {
				auctionDAO.createAuction(startPrice, minimumRise, startDateTime, endDateTime, creatorId, selectedArticlesCodes);
			} catch (SQLException e) {
				throw new ErrorException("Error! Failure of auction creation in database");
			}
		}
		catch (ErrorException e)
		{
			path = getServletContext().getContextPath() + "/GoToSell";
			path+="?errorMsg="+e.getMessage();
			response.sendRedirect(path);
			return;
		}

		path = getServletContext().getContextPath() + "/GoToSell";
		response.sendRedirect(path);
	}


	private boolean correctDates(Date startDate, Time startTime, Date endDate, Time endTime) 
	{
		/**
		 * Le Date inserite dall'utente hanno i secondi azzerati, ma la data di sistema no.
		 * Questo crea problemi di confronto fra le date, proprio perchè una ha i secondi
		 * mentre l'altra no.
		 *
		 * Lo stesso problema si presenta per i Time. I time inseriti dall'utente non hanno la data
		 * ma il tempo di sistema ha dentro anche la data, questo crea problemi nei confronti.
		 *
		 * Uso quindi Calendar che mi permette di azzerare la data oppure il tempo dei Date e dei Time.
		 * Ai Time azzero la data, alle Date azzero il tempo.
		 */
		Calendar todayD = Calendar.getInstance();
	    todayD.set(Calendar.HOUR_OF_DAY, 0);
	    todayD.set(Calendar.MINUTE, 0);
	    todayD.set(Calendar.SECOND, 0);
	    todayD.set(Calendar.MILLISECOND, 0);
	    Calendar startD = Calendar.getInstance();
	    startD.setTime(startDate);
	    startD.set(Calendar.HOUR_OF_DAY, 0);
	    startD.set(Calendar.MINUTE, 0);
	    startD.set(Calendar.SECOND, 0);
	    startD.set(Calendar.MILLISECOND, 0);
	    Calendar endD = Calendar.getInstance();
	    endD.setTime(endDate);
	    endD.set(Calendar.HOUR_OF_DAY, 0);
	    endD.set(Calendar.MINUTE, 0);
	    endD.set(Calendar.SECOND, 0);
	    endD.set(Calendar.MILLISECOND, 0);
	    
	    Calendar todayT = Calendar.getInstance();
	    todayT.set(Calendar.DAY_OF_MONTH, 0);
	    todayT.set(Calendar.MONTH, 0);
	    todayT.set(Calendar.YEAR, 0);
	    Calendar startT = Calendar.getInstance();
	    startT.setTime(startTime);
	    startT.set(Calendar.DAY_OF_MONTH, 0);
	    startT.set(Calendar.MONTH, 0);
	    startT.set(Calendar.YEAR, 0);
	    Calendar endT = Calendar.getInstance();
	    endT.setTime(endTime);
	    endT.set(Calendar.DAY_OF_MONTH, 0);
	    endT.set(Calendar.MONTH, 0);
	    endT.set(Calendar.YEAR, 0);

		/**
		 * Controllo dell'ordine cronologico delle date e i tempi
		 */
		if(endD.compareTo(startD)<0)
	    {
	        return false;
	    }
	    else if (endD.compareTo(startD)==0)
	    {
	    	if(endT.compareTo(startT)<=0)
	    	{
	    		return false;
	    	}
	    }
	    
	    if(startD.compareTo(todayD)<0) 
	    {
	        return false;
	    }
	    else if (startD.compareTo(todayD)==0)
	    {
	    	if(startT.compareTo(todayT)<0)
	    	{
		        return false;
	    	}
	    } 
	    
	    return true;
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
