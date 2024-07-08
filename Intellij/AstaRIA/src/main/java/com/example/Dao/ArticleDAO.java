package com.example.Dao;

import java.sql.*;


import java.util.ArrayList;
import java.util.List;

import com.example.Bean.ArticleBEAN;

public class ArticleDAO {
	private Connection connection;

	public ArticleDAO(Connection connection) {
		this.connection = connection;
	}

	// LOMI
	//
	//Metodo utilizzato per creare un articolo
	//
	public ArticleBEAN createArticle(String articleName, String description, Float minimumPrice, String keyWord, int creatorId, String imgPath ) throws SQLException
	{
		ArticleBEAN articleBEAN= null;
		String query = "INSERT INTO article (name, description, minimumPrice, keyWord, creatorId, imagePath) VALUES (?,?,?,?,?,?)";
		PreparedStatement pstatement =null;
		try
		{
			pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pstatement.setString(1, articleName);
			pstatement.setString(2, description);
			pstatement.setFloat(3, minimumPrice);
			pstatement.setString(4, keyWord);
			pstatement.setInt(5, creatorId);
			pstatement.setString(6, imgPath);
			pstatement.executeUpdate();
			ResultSet generatedKeys = pstatement.getGeneratedKeys();
			if (generatedKeys.next()) {
				articleBEAN = new ArticleBEAN();
				articleBEAN.setCode(generatedKeys.getInt(1));
				articleBEAN.setName(articleName);
				articleBEAN.setDescription(description);
				articleBEAN.setMinimumPrice(minimumPrice);
				articleBEAN.setKeyWord(keyWord);
				articleBEAN.setImagePath(imgPath);
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error inserting the article in the DB");
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {
				throw new SQLException("Error closing the statement while inserting the article in the DB");
			}
		}
		return articleBEAN;
	}
	/**Prende tutte le informazioni degli articoli appartentni ad un asta*/
	public List<ArticleBEAN> getInformationAboutArticleFromAuctionId(int id) throws SQLException {
		List<ArticleBEAN> list = new ArrayList<ArticleBEAN>();
		ArticleBEAN art;
		String query = "SELECT article.code, article.name, article.description, article.minimumPrice, article.imagePath" +
				" FROM article, includes WHERE includes.articleCode = article.code AND includes.auctionId = ? ";
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					art = new ArticleBEAN();
					art.setCode(result.getInt("article.code"));
					art.setName(result.getString("article.name"));
					art.setDescription(result.getString("article.description"));
					art.setMinimumPrice(result.getFloat("article.minimumPrice"));
					art.setImagePath(result.getString("article.imagePath"));
					list.add(art);
				}
			}
		} catch (SQLException e) {
			throw new SQLException("Error getting item's name of auction, " + e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {
				throw new SQLException("Error closing the statement while getting available articles in the DB");
			}
		}
		return list;
	}

	//	LOMI
	//Con questo metodo ottieni una lista non null di articoli disponibili per l'inserimento in una asta.
	//Bisogna passare l'ID del creatore degli articoli
	//Un articolo è disponibile se non ha winningAuction e se nel corso della sua storia non è mai stato in aste con almeno un offerta
	public List<ArticleBEAN> findUserArticles(int creatorId) throws SQLException {
		List<ArticleBEAN> list = new ArrayList<>();
		String query =
				"SELECT name, code, description, minimumPrice, keyWord, imagePath FROM article A1 WHERE A1.creatorId=? AND A1.code NOT IN"
						+ "(SELECT A.code FROM article A, includes I,auction AU WHERE A.code=I.articleCode AND I.auctionId = AU.id AND "
						+ "(A.winningAuctionId IS NOT NULL OR AU.isClosed = 0))";


		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, creatorId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					ArticleBEAN article = new ArticleBEAN();
					article.setName(result.getString("name"));
					article.setCode(result.getInt("code"));
					article.setDescription(result.getString("description"));
					article.setMinimumPrice(result.getFloat("minimumPrice"));
					article.setKeyWord(result.getString("keyWord"));
					article.setImagePath(result.getString("imagePath"));
					list.add(article);
				}
			}
		} catch (SQLException e) {
			throw new SQLException("Error while getting user's available articles from DB");
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {
				throw new SQLException("Error closing the statement --while getting user's available articles from DB--");
			}
		}
		return list;
	}


	public List<ArticleBEAN> getAllInformationFromAuctionId(int int1) throws SQLException {
		List<ArticleBEAN> list = new ArrayList<ArticleBEAN>();
		PreparedStatement pstatement = null;
		ArticleBEAN art;
		String query = "SELECT * FROM article, includes WHERE includes.articleCode = article.code AND includes.auctionId = ? ";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, int1);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					art = new ArticleBEAN();
					art.setCode(result.getInt("article.code"));
					art.setName(result.getString("article.name"));
					art.setDescription(result.getString("article.description"));
					art.setMinimumPrice(result.getFloat("article.minimumPrice"));
					art.setKeyWord(result.getString("article.KeyWord"));
					art.setImagePath(result.getString("imagePath"));
					list.add(art);
				}
			}
		} catch (SQLException e) {
			throw new SQLException("Error getting available articles in the DB");
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {
				throw new SQLException("Error closing the statement while getting available articles in the DB");
			}
		}
		return list;
	}
}

	
	