 /**
  * 
  */
 package com.isesalud.controller.security;
 
 import java.security.Principal;
 import java.util.Date;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.jboss.security.auth.spi.Util;
 
 import com.isesalud.ejb.query.UserEjb;
 import com.isesalud.model.User;
 import com.isesalud.support.JSFUtil;
 import com.isesalud.support.components.BaseComponent;
 import com.isesalud.support.exceptions.BaseException;
 import com.isesalud.support.exceptions.UserDisabledException;
 
 /**
  * @author Jesus Espinoza Hernandez
  *
  */
 @Named
 @SessionScoped
 public class SecurityComponent extends BaseComponent {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6777251583611335065L;
 	
 	/**
 	 * Inject the HttpRequest
 	 */
 	@Inject
 	private HttpServletRequest httpRequest;
 	
 	/**
 	 * Inject the HttpSession
 	 */
 	@Inject private HttpSession session;
 	
 	/**
 	 * Inject the UserEjb to fetch user entities
 	 */
 	@EJB
 	UserEjb userEjb;
 	
 	private String username;
 	private String password;
 	
 	/**
 	 * Flag that indicates if the current user is logged in or not
 	 */
 	private boolean logged;
 	
 	private User user = new User();
 	
 	private User currentUser;
 	
 	public User getUser() {
 		return user;
 	}
 	
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	public boolean isLogged() {
 		return logged;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	public User getCurrentUser() {
 		return currentUser;
 	}
 	
 	@Override
 	protected void init() throws BaseException {
 		if(session != null)
 			session.invalidate();
 		this.logged = false;
 	}
 	
 	/*
 	 * Do programmatic login
 	 */
 	public String login(){
 		try{
 			//Login via Servlet Context
 			httpRequest.login(username, password);
 			
 			//Retrieve the user principal
 			Principal principal = httpRequest.getUserPrincipal();
 			
 			//Get the user from the database
 			user.setUsername(principal.getName());
 			currentUser = userEjb.getUserByUsername(getUser());
 			
 			if(!currentUser.isEnabled()){
 				throw new UserDisabledException("User is disabled. Cannot log in");
 			}
 			
 			logged = true;
 			
 			log.infov("{0} logged in at {1}", currentUser.getName(), new Date());
 			return "/pacientes/busqueda?faces-redirect=true";
 		}catch (ServletException e){
 			logged = false;
 			JSFUtil.error("No se logro verificar su identidad");
 			log.error(e.getMessage());
 		}catch (UserDisabledException e){
 			logged = false;
 			JSFUtil.error("El usuario esta deshabilitado");
 			logout();
 			log.error(e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	/*
 	 * Do programmatic logout
 	 */
 	public String logout(){
 		try {
 			//Do logout
 			httpRequest.logout();
 			if(session != null)
 				//Invalidate the session
 				session.invalidate();
 		} catch (ServletException e) {
 			log.error(e.getMessage());
 			JSFUtil.error("Error interno del sistema");
 		}
 		this.logged = false;
 		//Redirect user to the home page
 		return "/home?faces-redirect=true";
 	}
 	
 	public static void main(String[] args){
 		final String password = "radiologia123";
 		
 		String passwordHash = Util.createPasswordHash("SHA-256", Util.BASE64_ENCODING, null, null, password);
 		System.out.println("Password hash: " + passwordHash);
 		
 	}
 
 }
