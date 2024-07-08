package com.example.Controller;

import com.example.Bean.AuctionBEAN;
import com.example.Dao.AuctionDAO;
import com.example.Utility.DBConnector;
import com.example.exception.ErrorException;
import com.google.gson.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@MultipartConfig
@WebServlet("/VerifyStillOpen")
public class VerifyStillOpen extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public void init() throws UnavailableException {
        ServletContext servletContext = getServletContext();
        connection = DBConnector.getConnection(servletContext);
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            Gson gson = new Gson();
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            JsonArray jsonArray=null;
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String jsonStr = sb.toString();
                jsonArray = gson.fromJson(jsonStr, JsonArray.class);
                if (jsonArray == null || jsonArray.size() == 0) {
                    Boolean empty = true;
                    String listToReturn = gson.toJson(empty);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().println(gson.toJson(listToReturn));
                    return;
                }
            }
            catch (Exception e)
            {
                throw new ErrorException("Error while reading json file");
            }




            AuctionDAO auctionDAO = new AuctionDAO(connection);
            List<AuctionBEAN> listToReturn = new ArrayList<>();
            for (int i=0; i<jsonArray.size(); i++) {
                try {
                    AuctionBEAN auctionBEAN;
                    auctionBEAN = auctionDAO.verifyStillOpen(jsonArray.get(i).getAsInt(), new Timestamp(System.currentTimeMillis()));
                    if(auctionBEAN!=null)
                        listToReturn.add(auctionBEAN);
                } catch (SQLException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    throw new ErrorException("Error! Db has some problem");
                }
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(gson.toJson(listToReturn));
        }
        catch (ErrorException e){
            String error = e.getMessage();
            response.getWriter().println(error);
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
