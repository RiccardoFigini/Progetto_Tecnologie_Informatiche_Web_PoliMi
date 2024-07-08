package com.example.Utility;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnector
{
    public static Connection getConnection(ServletContext context) throws UnavailableException
    {
        Connection connection = null;
        try
        {
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection=DriverManager.getConnection(url,user,password);
        } catch (ClassNotFoundException e) {
           throw new UnavailableException("DB driver loading failed");
        } catch (SQLException e) {
            throw new UnavailableException("Bad DB credentials");
        }
        return connection;
    }

    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}