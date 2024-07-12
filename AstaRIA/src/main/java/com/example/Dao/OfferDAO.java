package com.example.Dao;
import com.example.Bean.OfferBEAN;
import com.example.Bean.UserBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OfferDAO {
	private final Connection connection;
	public OfferDAO(Connection co) {
		this.connection = co;
	}
	
	/*Prende tutte le offerte di un'asta specifica in ingrsso tramite id*/
	public List<OfferBEAN> getAllAuctionsOffer(int id) throws SQLException{
		String query = "select o.*, offerente.username, offerente.address " +
				"from offer AS o, user as offerente " +
				"where o.clientId = offerente.id and o.auctionId =? " +
				"order by dateTime desc";
		PreparedStatement pstatement = null;
		OfferBEAN offerBean = null;
		List<OfferBEAN> list = new ArrayList<>();
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					offerBean = new OfferBEAN();
					offerBean.setPrice(result.getFloat("price"));

					offerBean.setDatatime(Timestamp.valueOf(result.getString("dateTime")));
					offerBean.setUserOffer(result.getString("username"));
					offerBean.setUserAddress(result.getString("address"));
					list.add(offerBean);
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting available element in DB");
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

	public Timestamp makeAOffer(int idauction, UserBean user, float offer) throws SQLException{
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String time = timestamp.toString().replace("fffffffff", "");
		String query = "INSERT INTO `offer` (`clientId`, `auctionId`, `price`, `dateTime`) VALUES (?, ?, ?, ? )";
		PreparedStatement psInsertOffer = null;

		try {
			psInsertOffer = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			psInsertOffer.setInt(1,  user.getId() );
			psInsertOffer.setInt(2, idauction);
			psInsertOffer.setFloat(3, offer);
			psInsertOffer.setString(4,time);
			psInsertOffer.executeUpdate();
		}catch(SQLException e)
		{
			throw new SQLException("Error accessing the DB while inserting new offer values, " + e);
		}finally {
			try {
				psInsertOffer.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while inserting new offering values, " + e);
			}
		}
		return timestamp;
	}

    public OfferBEAN getWinnerOfferFromAuction(Integer auctionId) throws SQLException{
		String query = "SELECT U.username, O.price, U.address FROM auction A JOIN offer O ON A.id=O.auctionId JOIN user U ON U.id = O.clientId WHERE " +
				"A.id = ? AND A.isClosed = 1 AND O.price = (SELECT MAX(O1.price) FROM offer O1 WHERE O1.auctionId=A.id)";
		PreparedStatement psWinnerOffer = null;
		OfferBEAN winnerOffer = null;
		try {
			psWinnerOffer=connection.prepareStatement(query);
			psWinnerOffer.setInt(1,  auctionId);
			try(ResultSet result = psWinnerOffer.executeQuery();){
				while(result.next()) {
					winnerOffer = new OfferBEAN();
					winnerOffer.setUserOffer(result.getString("U.username"));
					winnerOffer.setPrice(result.getFloat("O.price"));
					winnerOffer.setUserAddress(result.getString("U.address"));
				}
			}
		}catch(SQLException e)
		{
			throw new SQLException("Error accessing the DB while extraction winner," + e);
		}
		finally {
			try {
				psWinnerOffer.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement while extraction winner," + e);
			}
		}
		return winnerOffer;
	}

    public List<OfferBEAN> getOfferAfterValue(int auctionId, float lastOffer) throws SQLException{
		String query = "SELECT offer.*, username " +
				"FROM offer, user " +
				"WHERE offer.auctionId = ? and offer.price>? and user.id = offer.clientId ";
		PreparedStatement pstatement = null;
		OfferBEAN offerBean;
		List<OfferBEAN> list = new ArrayList<>();
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, auctionId);
			pstatement.setFloat(2, lastOffer);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					offerBean = new OfferBEAN();
					offerBean.setPrice(result.getFloat("price"));
					offerBean.setDatatime(Timestamp.valueOf(result.getString("dateTime")));
					offerBean.setUserOffer(result.getString("username"));
					list.add(offerBean);
				}
			}
		}
		catch(SQLException e)
		{
			throw new SQLException("Error getting available element in DB");
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
}
