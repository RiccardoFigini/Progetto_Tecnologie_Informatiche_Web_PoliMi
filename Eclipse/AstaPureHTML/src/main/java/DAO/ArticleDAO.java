package DAO;

import java.sql.Connection;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.*;
public class ArticleDAO {
	private Connection connection;

	public ArticleDAO(Connection connection) {
		this.connection=connection;
	}
	
	// LOMI
	//
	//Metodo utilizzato per creare un articolo
	//
	public void createArticle(String articleName, String description, Float minimumPrice, String keyWord, int creatorId, String imgPath ) throws SQLException
	{
		String query = "INSERT INTO article (name, description, minimumPrice, keyWord, creatorId, imagePath) VALUES (?,?,?,?,?,?)";
		PreparedStatement pstatement =null;
		try
		{
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, articleName);
			pstatement.setString(2, description);
			pstatement.setFloat(3, minimumPrice);
			pstatement.setString(4, keyWord);
			pstatement.setInt(5, creatorId);
			pstatement.setString(6, imgPath);
			pstatement.executeUpdate(); 
		}
		catch(SQLException e)
		{
			throw new SQLException("Error inserting the article in the DB");
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while inserting the article in the DB");
			}
		}
	}

	public List<ArticleBEAN> getNamesFromAuction(int id) throws SQLException{
		List<ArticleBEAN> list = new ArrayList<ArticleBEAN>();
		ArticleBEAN art;
		String query = "SELECT article.code, article.name, article.description, article.minimumPrice FROM article, includes WHERE includes.articleCode = article.code AND includes.auctionId = ? ";
		PreparedStatement pstatement = null;
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					art = new ArticleBEAN();
					art.setCode(result.getInt("article.code"));
					art.setName(result.getString("article.name"));
					art.setDescription(result.getString("article.description"));
					art.setMinimumPrice(result.getFloat("article.minimumPrice"));
					list.add(art);
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting item's name of auction, " + e);
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
	
	//	LOMI
	//Con questo metodo ottieni una lista non null di articoli disponibili per l'inserimento in una asta.
	//Bisogna passare l'ID del creatore degli articoli
	//Un articolo è disponibile se non ha winningAuction e se nel corso della sua storia non è mai stato in aste con almeno un offerta
	public List<ArticleBEAN> findUserArticles(int creatorId) throws SQLException{
		List<ArticleBEAN> list = new ArrayList<>();
		String query = 
				"SELECT name, code, description, minimumPrice, keyWord FROM article A1 WHERE A1.creatorId=? AND A1.code NOT IN"
				+ "(SELECT A.code FROM article A, includes I,auction AU WHERE A.code=I.articleCode AND I.auctionId = AU.id AND "
				+ "(A.winningAuctionId IS NOT NULL OR AU.isClosed = 0))";
		
		
		PreparedStatement pstatement=null;
		try
		{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, creatorId);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) 
				{
					ArticleBEAN article = new ArticleBEAN();
					article.setName(result.getString("name"));
					article.setCode(result.getInt("code"));
					article.setDescription(result.getString("description"));
					article.setMinimumPrice(result.getFloat("minimumPrice"));
					article.setKeyWord(result.getString("keyWord"));
					list.add(article);
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error while getting user's available articles from DB");
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement --while getting user's available articles from DB--");
			}
		}
		return list;
	}
	
	
	public List<ArticleBEAN> getAllInformationFromAuctionId(int int1) throws SQLException{
		List<ArticleBEAN> list = new ArrayList<ArticleBEAN>();
		PreparedStatement pstatement = null;
		ArticleBEAN art;
		String query = "SELECT * FROM article, includes WHERE includes.articleCode = article.code AND includes.auctionId = ? ";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, int1);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					art = new ArticleBEAN();
					art.setCode(result.getInt("article.code"));
					art.setName(result.getString("article.name"));
					art.setDescription(result.getString("article.description"));
					art.setMinimumPrice(result.getFloat("article.minimumPrice"));
					art.setKeyWord(result.getString("article.KeyWord"));
					list.add(art);
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting available articles in the DB");
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

	public String getImagePath(int articleId) throws SQLException
	{
		String path= null;
		PreparedStatement pstatement = null;
		String query = "SELECT article.imagePath FROM article WHERE article.code = ?";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, articleId);
			try(ResultSet result = pstatement.executeQuery();){
				if(result.next()) 
				{
					path=result.getString("imagePath");
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting image");
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting image");
			}
		}
		return path;
	}

	public int exist(int id) throws SQLException {
		String query = "SELECT code from article where code = ?";
		PreparedStatement pstatement = null;

		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.next())
					id=-1;
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error in verifing if article exsist" + e.getMessage());
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting article (exists)");
			}
		}
		return id;
	}

	public ArticleBEAN getAllInformation(int id) throws SQLException {
		String query = "SELECT * from article where code = ?";
		PreparedStatement pstatement = null;
		ArticleBEAN ar = new ArticleBEAN();

		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.next()) 
					ar = null;
				else {
					ar.setCode(id);
					ar.setName(result.getString("name"));
					ar.setDescription(result.getString("description"));
					ar.setMinimumPrice(result.getFloat("minimumPrice"));
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error in extracting all element from db" + e.getMessage());
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting article (get all information)");
			}
		}
		return ar;
	}

	public String getOwnerName(int code) throws SQLException {
		String query = "SELECT username from user, article where user.id = article.creatorId and article.code = ?";
		PreparedStatement pstatement = null;
		String name;
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, code);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.next()) 
					name = null;
				else 
					name = result.getString("username");
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error in extracting all element from db" + e.getMessage());
		}
		finally 
		{
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while getting article (get all information)");
			}
		}
		return name;
	}
}


	
	