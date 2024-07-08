package DAO;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import beans.*;
public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection=connection;
	}
	
	public User checkCredentials(String username, String password) throws SQLException
	{
		String query = "SELECT id,username,name,surname FROM user WHERE username = ? AND password = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);)
		{
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			try(ResultSet result = pstatement.executeQuery();)
			{
				if(!result.isBeforeFirst())
					return null;
				else
				{
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					return user;
				}
			}
		}
	}

	/*Questo metodo, dato un id in ingresso, fa la query che restituisce semplicemente
	 * l'username. In caso non esiste nessun utente con l'id richiesto restituisce null*/
	public String getUserFromId(int userIdOwner) {
		String query = "SELECT username FROM user WHERE id=?";
		try(PreparedStatement pstatement = connection.prepareStatement(query);)
		{
			pstatement.setInt(1, userIdOwner);
			try(ResultSet result = pstatement.executeQuery();)
			{
				result.next();
				return result.getString("username");
			}
		}
		catch(Exception e) {
			return null;
		}
	}

}
