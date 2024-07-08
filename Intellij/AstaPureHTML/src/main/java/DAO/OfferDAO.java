package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import beans.AuctionBEAN;
import beans.OfferBEAN;
import beans.User;
import utils.TimeCalculator;

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

	public void makeAOffer(int idauction, User user, float offer) throws SQLException{
		String query = "INSERT INTO `offer` (`clientId`, `auctionId`, `price`, `dateTime`) VALUES (?, ?, ?, current_timestamp() )";
		PreparedStatement psInsertOffer = null;
		
		try {
			psInsertOffer = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			psInsertOffer.setInt(1,  user.getId() );
			psInsertOffer.setInt(2, idauction);
			psInsertOffer.setFloat(3, offer);
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
	}
}
