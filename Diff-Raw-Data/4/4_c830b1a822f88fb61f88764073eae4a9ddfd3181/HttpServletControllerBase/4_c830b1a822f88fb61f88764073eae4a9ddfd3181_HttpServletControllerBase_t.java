 package de.fhb.mp3.controler.web;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Abstrakte Basisklasse fr Servlets
  * @author diesel
  *
  */
 public abstract class HttpServletControllerBase extends HttpServlet
 {

	private static final long serialVersionUID = 449784229878654344L;
 	
 	protected HashMap actions;
 	HttpSession session;
 
 	/**
 	 * Initialisierungsmethode
 	 * 
 	 * @param conf die Servlet-Konfiguration
 	 */
 	public void init(ServletConfig conf) throws ServletException
 	{
 
 	}
 
 	
 	/**
 	 * Diese Methode wird bei einer GET-Operation des Client-Browser ausgefhrt
 	 * 
 	 * @param request der HTTP-Request
 	 * @param response die HTTP-Response
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)  throws IOException, ServletException 
 	{
 
 	}
 	  
 	/**
 	 * Diese Methode wird bei einer POST-Operation des Client-Browser ausgefhrt
 	 * 
 	 * @param request der HTTP-Request
 	 * @param response die HTTP-Response
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)  throws IOException, ServletException 
 	{
 
 	}
 	
 	/** Methode muss noch definiert werden, um die Kennung der 
 	  * Operation aus der URL zu lesen
 	  * @param req Http-Request
 	  * @return Name der Aktion, die ausgefuehrt werden soll
 	  */
 	protected abstract String getOperation(HttpServletRequest req);
 }
