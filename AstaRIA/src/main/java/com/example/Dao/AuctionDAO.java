package com.example.Dao;
import com.example.Bean.ArticleBEAN;
import com.example.Bean.AuctionBEAN;
import com.example.Utility.TimeCalculator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class AuctionDAO {
private final Connection connection;
	
	public AuctionDAO(Connection connection) {
		this.connection=connection;
	}
	
	public AuctionBEAN createAuction(Float startPrice, int minimumRise, String startDate, String endDate, int creatorId,Integer[] articleCodes) throws SQLException
	{
		String insertAuction = "INSERT INTO auction (startPrice, minimumRise, startDate, endDate, userId) VALUES (?,?,?,?,?)";
		String insertIncludes = "INSERT INTO includes (articleCode,auctionId) VALUES (?,?)";
		
		PreparedStatement psInsertAuction = null;
		PreparedStatement psInsertIncludes = null;
		ResultSet rs = null;
		AuctionBEAN auctionBEAN = null;
		ArticleDAO articleDAO = new ArticleDAO(connection);
	
		try {
			connection.setAutoCommit(false);
			psInsertAuction = connection.prepareStatement(insertAuction, Statement.RETURN_GENERATED_KEYS);
			psInsertIncludes = connection.prepareStatement(insertIncludes);
			
			psInsertAuction.setFloat(1, startPrice);
			psInsertAuction.setInt(2, minimumRise);
			psInsertAuction.setString(3, startDate);
			psInsertAuction.setString(4, endDate);
			psInsertAuction.setInt(5, creatorId);
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

			auctionBEAN = new AuctionBEAN();
			auctionBEAN.setId(auctionId);
			auctionBEAN.setStartDate(Timestamp.valueOf(startDate));
			auctionBEAN.setEndDate(Timestamp.valueOf(endDate));
			auctionBEAN.setListOfArticle(articleDAO.getInformationAboutArticleFromAuctionId(auctionId));

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
		return auctionBEAN;
	}

	public List<AuctionBEAN> openAuctionsWithKeyWord(String keyword, Timestamp time) throws SQLException{
		String var = "%"+keyword+"%";
		List<AuctionBEAN> auctionList = new ArrayList<AuctionBEAN>();
		AuctionBEAN auctionBEAN=null;
		ArticleBEAN articleBEAN=null;
		String query =
				" ((Select  au.id, au.startDate, au.endDate, au.minimumRise, o.price, au.startPrice, au.userId , us.username, article2.* " +
						"from auction as au, offer as o , article as ar, includes as inc , user as us, article as article2, includes as includes2 " +
						"where au.isClosed=0 and o.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = au.id) and  o.auctionId = au.id " +
						"and ar.code=inc.articleCode and inc.auctionId=au.id and us.id = au.userId and (ar.keyWord like ? or ar.description like ? ) " +
						"and au.endDate>? and au.startDate<? and article2.code = includes2.articleCode and includes2.auctionId=au.id " +
						"union " +
						"select  au.id, au.startDate, au.endDate, au.minimumRise ,-1,  au.startPrice, au.userId , us.username , article2.*" +
						"from auction as au left join offer as o on au.id = o.auctionId , article as ar, includes as inc , user as us, article as article2, includes as includes2" +
						" where au.isClosed=0 and au.id not in (select distinct o2.auctionId from offer as o2) and ar.code=inc.articleCode" +
						" and inc.auctionId=au.id and us.id = au.userId and (ar.keyWord like ? or ar.description like ? ) and au.endDate>? " +
						"and au.startDate<? and article2.code = includes2.articleCode and includes2.auctionId=au.id) " +
						"order by endDate)";
		PreparedStatement pstatement = null;
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, var);
			pstatement.setString(2, var);
			pstatement.setString(3, time.toString());
			pstatement.setString(4, time.toString());
			pstatement.setString(5, var);
			pstatement.setString(6, var);
			pstatement.setString(7, time.toString());
			pstatement.setString(8, time.toString());
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					int newId = result.getInt("id");
					if(auctionBEAN == null || auctionBEAN.getId()!=newId) {
						auctionBEAN = new AuctionBEAN();
						auctionBEAN.setId(newId);
						auctionBEAN.setStartDate(Timestamp.valueOf(result.getString("startDate")));
						auctionBEAN.setEndDate(Timestamp.valueOf(result.getString("endDate")));
						auctionBEAN.setStartPrice(result.getFloat("startPrice"));
						auctionBEAN.setMinimumRise(result.getInt("minimumRise"));
						if (result.getFloat("price") == -1)
							auctionBEAN.setMaxOffer(result.getFloat("startPrice"));
						else
							auctionBEAN.setMaxOffer(result.getFloat("price"));
						auctionBEAN.setUserIdOwner(result.getInt("userId"));
						auctionBEAN.setUsernameOwner(result.getString("username"));
						auctionBEAN.setRemainingTime(TimeCalculator.calculateRemainingTime(time, auctionBEAN.getEndDate()));
						auctionList.add(auctionBEAN);
					}
					articleBEAN = new ArticleBEAN();
					articleBEAN.setCode(result.getInt("code"));
					articleBEAN.setName(result.getString("name"));
					articleBEAN.setDescription(result.getString("description"));
					articleBEAN.setMinimumPrice(result.getFloat("minimumPrice"));
					articleBEAN.setImagePath(result.getString("imagePath"));
					auctionBEAN.addArticle(articleBEAN);
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
		return auctionList;
	}

	/** Questa query prende le aste vinte dall'utente passato come parametro, complete di lista di articoli*/
	public List<AuctionBEAN> getEndAuction(int id) throws SQLException {
		List<AuctionBEAN> auctionList = new ArrayList<AuctionBEAN>();
		AuctionBEAN auctionBEAN=null;
		ArticleBEAN articleBEAN=null;
		PreparedStatement pstatement = null;
		String query =
				"SELECT  id, offer.price, endDate, article.code, article.name, article.description, article.minimumPrice, article.keyWord, article.imagePath " +
				"from auction, offer, includes, article " +
				"where  offer.auctionId = auction.id and offer.clientId= ? and includes.auctionId = auction.id and includes.articleCode=article.code " +
				"and auction.isClosed=1 AND offer.price = (select max(offer2.price) from offer as offer2 where offer2.auctionId = auction.id) ";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);

			try(ResultSet result = pstatement.executeQuery();)
			{
				while(result.next())
				{
					int newId = result.getInt("id");
					if(auctionBEAN == null || auctionBEAN.getId()!=newId)
					{
						auctionBEAN=new AuctionBEAN();
						auctionBEAN.setId(newId);
						auctionBEAN.setMaxOffer(result.getFloat("price"));
						auctionBEAN.setEndDate(Timestamp.valueOf(result.getString("endDate")));
						auctionList.add(auctionBEAN);
					}
					articleBEAN = new ArticleBEAN();
					articleBEAN.setCode(result.getInt("article.code"));
					articleBEAN.setName(result.getString("article.name"));
					articleBEAN.setDescription(result.getString("article.description"));
					articleBEAN.setMinimumPrice(result.getFloat("article.minimumPrice"));
					articleBEAN.setKeyWord(result.getString("article.KeyWord"));
					articleBEAN.setImagePath(result.getString("imagePath"));
					auctionBEAN.addArticle(articleBEAN);
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
		return auctionList;
	}

	/** Questa query prende le aste create dall'utente passato come parametro con i relativi articoli. Le aste sono aperte se isClosed=false*/
	public List<AuctionBEAN> findAllAuctionFromCreatorId(int id, boolean isClosed) throws SQLException{
		List<AuctionBEAN> auctionList = new ArrayList<AuctionBEAN>();
		AuctionBEAN auctionBEAN=null;
		ArticleBEAN articleBEAN=null;
		PreparedStatement pstatement = null;
		String query =
				"SELECT A.id, O.price, A.startDate, A.endDate, U.username, article.code, article.name, article.description, article.minimumPrice, article.imagePath\n" +
						"FROM article, includes, auction as A LEFT JOIN offer as O ON A.id = O.auctionId LEFT JOIN user as U ON O.clientId = U.id \n" +
						"WHERE A.userId = ? AND A.isClosed = ? and includes.auctionId=A.id and article.code=includes.articleCode\n" +
						"AND (O.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = A.id) OR O.price IS NULL) order by endDate asc,A.id asc;";

		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			pstatement.setBoolean(2, isClosed);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) 
				{
					int newId = result.getInt("A.id");
					if(auctionBEAN == null || auctionBEAN.getId()!=newId)
					{
						auctionBEAN=new AuctionBEAN();
						auctionBEAN.setId(newId);

						Float bestPrice = result.getFloat("O.price");
						if(result.wasNull()) bestPrice = null;

						auctionBEAN.setMaxOffer(bestPrice);
						auctionBEAN.setStartDate(Timestamp.valueOf(result.getString("A.startdate")));
						auctionBEAN.setEndDate(Timestamp.valueOf(result.getString("A.endDate")));
						auctionBEAN.setUsernameOffer(result.getString("U.username"));
						auctionList.add(auctionBEAN);
					}
					articleBEAN = new ArticleBEAN();
					articleBEAN.setCode(result.getInt("article.code"));
					articleBEAN.setName(result.getString("article.name"));
					articleBEAN.setDescription(result.getString("article.description"));
					articleBEAN.setMinimumPrice(result.getFloat("article.minimumPrice"));
					articleBEAN.setImagePath(result.getString("article.imagePath"));
					auctionBEAN.addArticle(articleBEAN);
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
		return auctionList;
	}

	/*Restituisce null solo se non esiste l'asta con un certo ID, ALtrimenti l'asta con solo un id dentro*/
	public AuctionBEAN exist(int id) throws SQLException{
		AuctionBEAN auc = new AuctionBEAN();
		auc.setId(-1);
		PreparedStatement pstatement = null;
		String query = "SELECT id, minimumRise, startPrice, endDate, startDate from auction WHERE id=?";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				if(result.next()) {
					auc.setId(result.getInt("id"));
					auc.setMinimumRise(result.getInt("minimumRise"));
					auc.setStartPrice(result.getFloat("startPrice"));
					auc.setEndDate(Timestamp.valueOf(result.getString("endDate")));
					auc.setStartDate(Timestamp.valueOf(result.getString("startDate")));
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

	/*Restituisce una specifica asta richiesta e l'ultima offerta svolta se è stata fatta*/
	public AuctionBEAN getAuction(int id) throws SQLException{
		AuctionBEAN auc = new AuctionBEAN();
		float maxOffer=0;
		auc.setId(-1);
		PreparedStatement pstatement = null;
		String query =
				"SELECT auction.*, price "
				+ "from auction , offer as o "
				+ "WHERE id=? and o.auctionId = id and o.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = id) "
				+ "union "
				+ "SELECT auction.*, -1 "
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
					if(maxOffer == -1) {
						auc.setMaxOffer(auc.getStartPrice());
						auc.setAlreadyOffer(false);
					}
					else {
						auc.setMaxOffer(maxOffer);
						auc.setAlreadyOffer(true);
					}
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

	/** Prende tutti i dettagli dell'asta nel parametro con anche gli articoli */
	public AuctionBEAN getAuctionDetailsFromId(int id) throws SQLException{
		AuctionBEAN auctionBEAN=null;
		ArticleBEAN articleBEAN;
		PreparedStatement pstatement = null;
		String query =
				"SELECT A.id, A.startPrice, A.minimumRise, A.startDate, A.endDate, A.userId,A.isClosed, \n" +
				"AR.code, AR.name, AR.description, AR.minimumPrice, AR.imagePath\n" +
				"from auction A, article AR, includes I \n" +
				"WHERE A.id = ?  and I.auctionId=A.id and I.articleCode=AR.code";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next())
				{
					if(auctionBEAN == null )
					{
						auctionBEAN=new AuctionBEAN();
						auctionBEAN.setId(result.getInt("A.id"));
						auctionBEAN.setMinimumRise(result.getInt("A.minimumRise"));
						auctionBEAN.setStartPrice(result.getFloat("A.startPrice"));

						auctionBEAN.setStartDate(Timestamp.valueOf(result.getString("A.startDate")));
						if(result.getInt("A.isClosed")==0)
							auctionBEAN.setIsClosed(false);
						else
							auctionBEAN.setIsClosed(true);
						auctionBEAN.setUserIdOwner(result.getInt("A.userId"));

						auctionBEAN.setEndDate(Timestamp.valueOf(result.getString("A.endDate")));
					}
					articleBEAN = new ArticleBEAN();
					articleBEAN.setCode(result.getInt("AR.code"));
					articleBEAN.setName(result.getString("AR.name"));
					articleBEAN.setDescription(result.getString("AR.description"));
					articleBEAN.setMinimumPrice(result.getFloat("AR.minimumPrice"));
					articleBEAN.setImagePath(result.getString("AR.imagePath"));
					auctionBEAN.addArticle(articleBEAN);
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
		return auctionBEAN;
	}
	
	public void onlyCloseAuction(int id) throws SQLException{
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
	public void closeAuctionAndAssignArticles(int id) throws SQLException {
		String closeAuctionQuery = "UPDATE auction SET isClosed = '1' WHERE id = ?";
		String assignArticlesQuery = "UPDATE article SET winningAuctionId= ? WHERE code IN "
				+ "(select I.articleCode FROM includes I WHERE I.auctionId = ?)";

		PreparedStatement psCloseAuction = null;
		PreparedStatement psAssignArticles = null;

		try {
			/*Imposto a false perché non voglio che siano eseguite le due query in modo
			 * separato. Devo effettuare una modifica contemporaneamente e in modo atomico
			 * Tutti gli statements preparati vengono eseguiti assieme*/
			connection.setAutoCommit(false);
			psCloseAuction = connection.prepareStatement(closeAuctionQuery);
			psAssignArticles = connection.prepareStatement(assignArticlesQuery);

			psCloseAuction.setInt(1, id);
			if (psCloseAuction.executeUpdate() == 0) {
				throw new SQLException("No affected rows while closing auction");
			}

			psAssignArticles.setInt(1, id);
			psAssignArticles.setInt(2, id);
			if (psAssignArticles.executeUpdate() == 0) {
				throw new SQLException("No affected rows while assigning articles");
			}
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw new SQLException("Error accessing the DB while inserting new auction values");
		} finally {
			connection.setAutoCommit(true);
			try {
				psCloseAuction.close();
				psAssignArticles.close();
			} catch (Exception e) {
				throw new SQLException("Error closing the statement while inserting new auction values");
			}
		}
	}
	public AuctionBEAN verifyStillOpen(int id, Timestamp time) throws SQLException {
		AuctionBEAN auctionBEAN = null;
		ArticleBEAN articleBEAN= null;
		PreparedStatement pstatement = null;
		String query =
				"SELECT  tt.username, au.*, o.price, ar.* " +
						"from auction as au, offer as o , article as ar, includes as inc, user as tt " +
						"where au.isClosed=0 and o.price = (SELECT max(O2.price) FROM offer as O2 WHERE O2.auctionId = au.id) and " +
						"o.auctionId = au.id and ar.code=inc.articleCode and inc.auctionId=au.id and au.userId = tt.id and au.id=? " +
						"union " +
						"select  tt.username, au.*, -1, ar.* " +
						"from auction as au left join offer as o on au.id = o.auctionId , article as ar, includes as inc , user as tt  " +
						"where au.isClosed=0 and au.id not in (select distinct o2.auctionId from offer as o2)  " +
						"and ar.code=inc.articleCode and inc.auctionId=au.id and au.id=? and tt.id=au.userId  ";
		try {
			pstatement = connection.prepareStatement(query);

			pstatement.setInt(1, id);
			pstatement.setInt(2, id);
			ResultSet result = pstatement.executeQuery();
			while (result.next())
			{
				if (auctionBEAN == null) {
					auctionBEAN= new AuctionBEAN();
					auctionBEAN.setStartDate(Timestamp.valueOf(result.getString("startDate")));
					auctionBEAN.setEndDate(Timestamp.valueOf(result.getString("endDate")));
					if (auctionBEAN.getStartDate().compareTo(time) > 0 || auctionBEAN.getEndDate().compareTo(time) < 0)
						return null;
					else {
						auctionBEAN.setId(result.getInt("id"));
						auctionBEAN.setStartPrice(result.getFloat("startPrice"));
						auctionBEAN.setMinimumRise(result.getInt("minimumRise"));
						if (result.getFloat("price") == -1)
							auctionBEAN.setMaxOffer(result.getFloat("startPrice"));
						else
							auctionBEAN.setMaxOffer(result.getFloat("price"));
						auctionBEAN.setUserIdOwner(result.getInt("userId"));
						auctionBEAN.setUsernameOwner(result.getString("username"));
						auctionBEAN.setRemainingTime(TimeCalculator.calculateRemainingTime(time, auctionBEAN.getEndDate()));
					}
				}
				articleBEAN = new ArticleBEAN();
				articleBEAN.setCode(result.getInt("code"));
				articleBEAN.setName(result.getString("name"));
				articleBEAN.setDescription(result.getString("description"));
				articleBEAN.setMinimumPrice(result.getFloat("minimumPrice"));
				articleBEAN.setImagePath(result.getString("imagePath"));
				auctionBEAN.addArticle(articleBEAN);

			}

		} catch (SQLException e) {
			throw new SQLException("Error verify still open auction, " + e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {
				throw new SQLException("Error closing the statement while verify still open auction");
			}
		}
		return auctionBEAN;
	}


	public AuctionBEAN checkClosableByUser(int creatorId, int auctionId) throws SQLException {
		String query ="SELECT A.id, A.endDate FROM auction as A WHERE A.userId = ? AND A.isClosed = 0 AND A.id= ?";
		PreparedStatement pstatement = null;
		AuctionBEAN auctionBEAN = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, creatorId);
			pstatement.setInt(2, auctionId);
			try(ResultSet result = pstatement.executeQuery();){
				if(result.next())
				{
					auctionBEAN= new AuctionBEAN();
					auctionBEAN.setId(result.getInt("id"));
					auctionBEAN.setEndDate(Timestamp.valueOf(result.getString("endDate")));
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error checking closable auction by user" +e);
		}
		finally
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while checking closable auction by user");
			}
		}
		return auctionBEAN;
	}
}







