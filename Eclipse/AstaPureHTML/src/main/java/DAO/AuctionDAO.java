package DAO;

import java.sql.Connection;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import beans.*;
import utils.TimeCalculator;

public class AuctionDAO {
private final Connection connection;
	
	public AuctionDAO(Connection connection) {
		this.connection=connection;
	}
	
	public void createAuction(Float startPrice, int minimumRise, String startDate, String endDate, int creatorId,Integer[] articleCodes) throws SQLException
	{
		String insertAuction = "INSERT INTO auction (startPrice, minimumRise, startDate, endDate, userId) VALUES (?,?,?,?,?)";
		String insertIncludes = "INSERT INTO includes (articleCode,auctionId) VALUES (?,?)";
		
		PreparedStatement psInsertAuction = null;
		PreparedStatement psInsertIncludes = null;
		ResultSet rs = null;
	
		try {
			connection.setAutoCommit(false);
			psInsertAuction = connection.prepareStatement(insertAuction, Statement.RETURN_GENERATED_KEYS);
			psInsertIncludes = connection.prepareStatement(insertIncludes);
			
			psInsertAuction.setFloat(1, startPrice);
			psInsertAuction.setInt(2, minimumRise);
			psInsertAuction.setString(3, startDate);
			psInsertAuction.setString(4, endDate);
			psInsertAuction.setInt(5, creatorId);
			System.out.println(psInsertAuction.toString());
			psInsertAuction.executeUpdate(); 
			
			rs = psInsertAuction.getGeneratedKeys();
		    int auctionId = 0;
		    if (rs.next()) {
		        auctionId = rs.getInt(1);
		    }
		    
		    for(int i=0; i<articleCodes.length; i++)
		    {
		    	psInsertIncludes.setInt(1, articleCodes[i]);
		    	psInsertIncludes.setInt(2, auctionId);
		    	psInsertIncludes.executeUpdate();
		    }
		    connection.commit();
		}catch(SQLException e)
		{
			connection.rollback();
			throw new SQLException("Error accessing the DB while inserting new auction values");
		}finally {
			connection.setAutoCommit(true);
			try {
				psInsertAuction.close();
				psInsertIncludes.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while inserting new auction values");
			}
		}
	}
	
	public List<AuctionBEAN> openAuctionsWithKeyWord(String keyword, Timestamp time) throws SQLException{
		String var = "%"+keyword+"%";
		List<AuctionBEAN> list = new ArrayList<AuctionBEAN>();
		UserDAO user = new UserDAO(connection);
		ArticleDAO article = new ArticleDAO(connection);
		String query = 
				" (Select distinct id, au.startDate, au.endDate, au.minimumRise, o.price, au.startPrice, au.userId " +
						" from auction as au, offer as o , article as ar, includes as inc " +
						" where au.isClosed=0 and o.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = au.id) and " +
						" o.auctionId = au.id and ar.code=inc.articleCode and inc.auctionId=au.id and " +
						"                (ar.keyWord like ? or ar.description like ?) " +
						" union " +
						" select distinct id, au.startDate, au.endDate, au.minimumRise ,-1,  au.startPrice, au.userId " +
						" from auction as au left join offer as o on au.id = o.auctionId , article as ar, includes as inc " +
						" where au.isClosed=0 and au.id not in (select distinct o2.auctionId from offer as o2) " +
						"                and ar.code=inc.articleCode and inc.auctionId=au.id and " +
						"                (ar.keyWord like ? or ar.description like ?)) " +
						" order by endDate ";
		PreparedStatement pstatement = null;
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, var);
			pstatement.setString(2, var);
			pstatement.setString(3, var);
			pstatement.setString(4, var);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					AuctionBEAN auction = new AuctionBEAN();
					auction.setStartDate(Timestamp.valueOf(result.getString("startDate")));
					auction.setEndDate(Timestamp.valueOf(result.getString("endDate")));
					if(auction.getStartDate().compareTo(time)>0 || auction.getEndDate().compareTo(time)<0)
						auction = null;
					else {
						auction.setId(result.getInt("id"));
						auction.setStartPrice(result.getFloat("startPrice"));
						auction.setMinimumRise(result.getInt("minimumRise"));
						if(result.getFloat("price") == -1)
							auction.setMaxOffer(result.getFloat("startPrice"));
						else
							auction.setMaxOffer(result.getFloat("price"));
						auction.setUserIdOwner(result.getInt("userId"));
						auction.setUsernameOwner(user.getUserFromId(auction.getUserIdOwner()));
						auction.setListOfArticle(article.getNamesFromAuction(result.getInt("id")));
						auction.setRemainingTime(TimeCalculator.calculateRemainingTime(time ,auction.getEndDate()));
						list.add(auction);
					}
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting open auction in DB (openAuctionsWithKeyWord), " + e);
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting available articles in the DB");
			}
		}
		return list;
	}
	
	/*Prendo tutte le aste che sono aperte. Con "aperte" intendo che non hanno superato la data di scadenza 
	 * dell'asta. Quinid non sono incluse quelle aste chiuse temporalemente ma non ancora chiuse
	 * dal proprieario dell'asta*/
	public List<AuctionBEAN> allAuction(Timestamp time) throws SQLException{
		List<AuctionBEAN> list = new ArrayList<AuctionBEAN>();
		UserDAO user = new UserDAO(connection);
		PreparedStatement pstatement = null;
		ArticleDAO article = new ArticleDAO(connection);
		String query = "(Select distinct id, au.startDate, au.endDate, au.minimumRise, o.price, au.startPrice, au.userId "
				+ "from auction as au, offer as o "
				+ "where au.isClosed=0 and o.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = au.id) and "
				+ "o.auctionId = au.id "
				+ "union "
				+ "Select distinct id, au.startDate, au.endDate, au.minimumRise ,-1,  au.startPrice, au.userId "
				+ "from auction as au left join offer as o on au.id = o.auctionId "
				+ "where au.isClosed=0 and au.id not in (select distinct o2.auctionId from offer as o2) ) "
				+ "order by endDate ";
		try{
			pstatement = connection.prepareStatement(query);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					AuctionBEAN auction = new AuctionBEAN();
					auction.setStartDate(Timestamp.valueOf(result.getString("startDate")));
					auction.setEndDate(Timestamp.valueOf(result.getString("endDate")));
					if(auction.getStartDate().compareTo(time)>0 || auction.getEndDate().compareTo(time)<0 )
						auction = null;
					else {
						auction.setId(result.getInt("id"));
						auction.setStartPrice(result.getFloat("startPrice"));
						auction.setMinimumRise(result.getInt("minimumRise"));
						if(result.getFloat("price") == -1)
							auction.setMaxOffer(result.getFloat("startPrice"));
						else
							auction.setMaxOffer(result.getFloat("price"));
						auction.setUserIdOwner(result.getInt("userId"));
						auction.setUsernameOwner(user.getUserFromId(auction.getUserIdOwner()));
						auction.setListOfArticle(article.getNamesFromAuction(result.getInt("id")));
						auction.setRemainingTime(TimeCalculator.calculateRemainingTime(time ,auction.getEndDate()));
						list.add(auction);
					}
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting all open auction from DB (allAuction), "+ e);
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting available articles in the DB");
			}
		}
		return list;
	}
	
	/*Questa query restituisce le aste vinte da uno specifico utente in ingresso al metodo
	 * ATTENZIONE: da sostituire enddate con il flag che indica se l'asta è terminata o meno. Per ora prendo solo
	 * l'id associato ad un asta terminata con la massima offerta fatta. Che in teoria dovrebbe dirti chi è 
	 * aggiudicato quella determinata asta*/
	public List<AuctionBEAN> getEndAuction(int id) throws SQLException {
		List<AuctionBEAN> list = new ArrayList<AuctionBEAN>();
		ArticleDAO article = new ArticleDAO(connection);
		PreparedStatement pstatement = null;
		String query = "SELECT  max(price) as price, id, endDate "
				+ "				fROM auction, includes, article, offer "
				+ "				where auction.isClosed=1 AND includes.articleCode = article.code AND  "
				+ "				auction.id = includes.auctionId and offer.auctionId = auction.id and offer.clientId=?  "
				+ "                group by(auction.id) ";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					AuctionBEAN asta = new AuctionBEAN();
					asta.setMaxOffer(result.getFloat("price"));
					asta.setEndDate(Timestamp.valueOf(result.getString("endDate")));
					asta.setListOfArticle(article.getAllInformationFromAuctionId(result.getInt("id")));
					list.add(asta);
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting end auction in db (getEndAuction), " + e);
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting available articles in the DB");
			}
		}
		return list;
	}
	
	
	public List<AuctionBEAN> findAllAuctionFromCreatorId(int id, boolean isClosed) throws SQLException{
		List<AuctionBEAN> list = new ArrayList<AuctionBEAN>();
		ArticleDAO articleDao = new ArticleDAO(connection);
		PreparedStatement pstatement = null;
		String query 
				= "SELECT A.id, O.price, A.startDate, A.endDate, U.username "
				+ "FROM auction as A LEFT JOIN offer as O ON A.id = O.auctionId LEFT JOIN user as U ON O.clientId = U.id "
				+ "WHERE A.userId = ? AND A.isClosed = ? \n"
				+ "AND (O.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = A.id) OR O.price IS NULL) order by startDate ASC";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			pstatement.setBoolean(2, isClosed);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) 
				{
					AuctionBEAN auction = new AuctionBEAN();
						
					Float bestPrice = result.getFloat("O.price");
					if(result.wasNull()) bestPrice = null;
					
					auction.setId(result.getInt("A.id"));
					auction.setMaxOffer(bestPrice);
					auction.setStartDate(Timestamp.valueOf(result.getString("A.startdate")));
					auction.setEndDate(Timestamp.valueOf(result.getString("A.endDate")));
					auction.setUsernameOffer(result.getString("U.username"));
					auction.setListOfArticle(articleDao.getNamesFromAuction(result.getInt("A.id")));
					list.add(auction);
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error while getting user's auctions from DB");
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement --while getting user's auctions from DB--");
			}
		}
		return list;
	}

	/*Restituisce null solo se non esiste l'asta con un certo ID, ALtrimenti l'asta con solo un id dentro*/
	public AuctionBEAN exist(int id) throws SQLException{
		AuctionBEAN auc = new AuctionBEAN();
		auc.setId(-1);
		PreparedStatement pstatement = null;
		String query = "SELECT id, minimumRise, startPrice from auction WHERE id=?";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				if(result.next()) {
					auc.setId(result.getInt("id"));
					auc.setMinimumRise(result.getInt("minimumRise"));
					auc.setStartPrice(result.getFloat("startPrice"));
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting element from DB (exist), "+e);
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting open auctions");
			}
		}
		if(auc.getId()==-1)
			return null;
		return auc;
	}

	/*Restituisce una specifica asta richiesta*/
	public AuctionBEAN getAuction(int id) throws SQLException{
		AuctionBEAN auc = new AuctionBEAN();
		float maxOffer=0;
		auc.setId(-1);
		PreparedStatement pstatement = null;
		String query = "SELECT auction.*, price "
				+ "from auction , offer as o "
				+ "WHERE id=? and o.auctionId = id and o.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = id) "
				+ "union "
				+ "SELECT auction.*, price "
				+ "from auction left join offer as o3 on auction.id = o3.auctionId "
				+ "WHERE id=? and id not in (select distinct a2.id from auction as a2, offer o2 where o2.auctionId= a2.id) ";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			pstatement.setInt(2, id);
			try(ResultSet result = pstatement.executeQuery();){
				if(result.next()) {
					auc.setId(result.getInt("id"));
					auc.setEndDate(Timestamp.valueOf(result.getString("endDate")));
					if(result.getInt("isClosed")==0)
						auc.setIsClosed(false);
					else
						auc.setIsClosed(true);
					auc.setMinimumRise(result.getInt("minimumRise"));
					auc.setStartPrice(result.getFloat("startPrice"));
					auc.setStartDate(Timestamp.valueOf(result.getString("startDate")));
					maxOffer = result.getFloat("price");
					if(maxOffer == 0)
						auc.setMaxOffer(auc.getStartPrice());
					else
						auc.setMaxOffer(maxOffer);					
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting auction from DB (getAuction), "+e);
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting open auctions");
			}
		}
		if(auc.getId()==-1)
			return null;
		return auc;
	}
	
	public AuctionBEAN getAuctionDetailsFromId(int id) throws SQLException{
		ArticleDAO articleDao = new ArticleDAO(connection);
		AuctionBEAN auc= null;
		PreparedStatement pstatement = null;
		String query = "SELECT * from auction WHERE auction.id = ?";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				if(result.next()) 
				{
					auc = new AuctionBEAN();
					auc.setId(result.getInt("id"));
					auc.setMinimumRise(result.getInt("minimumRise"));
					auc.setStartPrice(result.getFloat("startPrice"));

					auc.setStartDate(Timestamp.valueOf(result.getString("startDate")));
					if(result.getInt("isClosed")==0)
						auc.setIsClosed(false);
					else
						auc.setIsClosed(true);
					auc.setUserIdOwner(result.getInt("userId"));

					auc.setEndDate(Timestamp.valueOf(result.getString("endDate")));
					auc.setListOfArticle(articleDao.getNamesFromAuction(result.getInt("id")));
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting auction from DB (GetAuctionDetailsFromID), "+e);
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting open auctions");
			}
		}
		return auc;
	}
	
	public void onlyCloseAction(int id) throws SQLException{
		PreparedStatement pstatement = null;
		int rowsAffected = 0;
		String query = "UPDATE auction SET isClosed = '1' WHERE id = ?";
		try {
		    pstatement = connection.prepareStatement(query);
		    pstatement.setInt(1, id);
		    rowsAffected = pstatement.executeUpdate();
		
			if(rowsAffected <= 0)  
			{
			    throw new SQLException("Update affected no rows");
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting auction from DB (GetAuctionDetailsFromID), "+e);
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting open auctions");
			}
		}
		return;
	}
	public void closeActionAndAssignArticles(int id) throws SQLException{
			String closeAuctionQuery = "UPDATE auction SET isClosed = '1' WHERE id = ?";
			String assignArticlesQuery = "UPDATE article SET winningAuctionId= ? WHERE code IN "
					+ "(select I.articleCode FROM includes I WHERE I.auctionId = ?)";
			
			PreparedStatement psCloseAuction = null;
			PreparedStatement psAssignArticles = null;
		
			try {
				connection.setAutoCommit(false);
				psCloseAuction = connection.prepareStatement(closeAuctionQuery);
				psAssignArticles = connection.prepareStatement(assignArticlesQuery);
				
				psCloseAuction.setInt(1, id);
				if(psCloseAuction.executeUpdate()==0)
				{
					throw new SQLException("No affected rows while closing auction");
				}
				
				psAssignArticles.setInt(1, id);
				psAssignArticles.setInt(2, id);
				if(psAssignArticles.executeUpdate()==0)
				{
					throw new SQLException("No affected rows while assigning articles");
				}
			    connection.commit();
			}catch(SQLException e)
			{
				connection.rollback();
				throw new SQLException("Error accessing the DB while inserting new auction values");
			}finally {
				connection.setAutoCommit(true);
				try {
					psCloseAuction.close();
					psAssignArticles.close();
				}catch (Exception e) {
					throw new SQLException("Error closing the statement while inserting new auction values");
				}
			}
		}
}







