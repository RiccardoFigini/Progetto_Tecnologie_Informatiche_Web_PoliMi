package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import exceptions.ErrorException;
import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.*;
import utils.*;
import beans.*;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CheckLogin() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = DBConnector.getConnection(servletContext);
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		UserDAO userDao = new UserDAO(connection);
		String username = null;
		String password = null;
		String path = null;
		User user = null;

		try
		{
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			if (username == null || password == null || username.isEmpty() || password.isEmpty())
			{
				throw new ErrorException("Failed login. Missing or empty credential value");
			}

			try {
				user = userDao.checkCredentials(username, password);
			} catch (SQLException e) {
				throw new ErrorException("Impossible to check credentials");
			}

			if(user==null)
			{
				throw new ErrorException("Incorrect username or password");
			}
			else
			{
				request.getSession().setAttribute("timeLogin", new Timestamp(System.currentTimeMillis()));
				request.getSession().setAttribute("user", user);
				path = getServletContext().getContextPath() + "/GoToHome";
				response.sendRedirect(path);
			}
		}catch (ErrorException e)
		{
			ServletContext servletContext = getServletContext();
			WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg", e.getMessage());
			path = "/login.html";
			templateEngine.process(path, ctx, response.getWriter());
		}
	}


	public void destroy() {
		try {
			DBConnector.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
