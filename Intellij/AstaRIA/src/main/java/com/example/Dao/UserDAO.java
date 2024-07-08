package com.example.Dao;
import com.example.Bean.UserBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class UserDAO {
    private final Connection connection;
    public UserDAO(Connection connection) {
        this.connection=connection;
    }
    public UserBean checkCredentials(String username, String password) throws SQLException
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
                    UserBean user = new UserBean();
                    user.setId(result.getInt("id"));
                    user.setUsername(result.getString("username"));
                    user.setName(result.getString("name"));
                    user.setSurname(result.getString("surname"));
                    return user;
                }
            }
        }
    }
}
