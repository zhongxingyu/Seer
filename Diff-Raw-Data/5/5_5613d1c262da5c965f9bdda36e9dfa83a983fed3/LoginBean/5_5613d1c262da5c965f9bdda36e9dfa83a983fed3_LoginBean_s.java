 package cz.cvut.fel.jee.labEshop.web;
 
 import java.io.Serializable;
 import java.security.Principal;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import cz.cvut.fel.jee.labEshop.manager.UserManager;
 import cz.cvut.fel.jee.labEshop.model.Role;
 import cz.cvut.fel.jee.labEshop.model.User;
 import cz.cvut.fel.jee.labEshop.util.LabEshopConstants;
 
 /**
  * This controller contains and work with actual logged user. Controller provide
  * actual logged user, and information if user is admin or customer.
  * 
  * @author Tom
  */
 @Named("loginBean")
 @SessionScoped
 public class LoginBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Actually looged user, if user is not logged then is null.
 	 */
 	private User loggedUser = null;
 
 	@Inject
 	private UserManager userManager;
 	// //////////////////////////////
 	// // Logged user role type ////
 	// /////////////////////////////
 
 	/**
 	 * If user is logged and has admin role then is set to true otherwise false.
 	 */
 	private boolean isAdmin = false;
 
 	/**
 	 * If user is logged and has customer role then is set to true otherwise
 	 * false.
 	 */
 	private boolean isCustomer = false;
 
 	/**
 	 * If user is logged then is set to true otherwise false.
 	 */
 	private boolean isUserLogged = false;
 
 	@PostConstruct
 	public void init() {
 		recognizeUser();
 	}
 
 	/**
 	 * Method check if user is logged and determine if user is customer or admin
 	 * or has both roles. When user is not logged then loggedUser is null.
 	 */
 	public void recognizeUser() {
 		Principal p = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
 		if (p != null && loggedUser == null) {
 			loggedUser = userManager.findUserByUsername(p.getName());
 		} else if (p == null) {
 			loggedUser = null;
 		}
 		if (loggedUser == null || loggedUser.getRoles() == null || loggedUser.getRoles().isEmpty()) {
 			isAdmin = false;
 			isCustomer = false;
 			isUserLogged = false;
 			return;
 		}
 		Iterator<Role> roleIt = loggedUser.getRoles().iterator();
 		while (roleIt.hasNext()) {
 			Role userRole = roleIt.next();
 			if (userRole.getRole().equals(LabEshopConstants.CUSTOMER_ROLE)) {
 				isCustomer = true;
 				isUserLogged = true;
 			} else if (userRole.getRole().equals(LabEshopConstants.ADMINISTRATOR_ROLE)) {
 				isAdmin = true;
 				isUserLogged = true;
 			}
 		}
 	}
 
 	/**
 	 * This method logout actual looged user from application and invalidate all
 	 * sessions.
 	 */
 	public String logout() {
 		FacesContext facescontext = FacesContext.getCurrentInstance();
 		HttpServletRequest ref = (HttpServletRequest) facescontext.getExternalContext().getRequest();
 		try {
 			ref.logout();
 			HttpSession session = (HttpSession) facescontext.getExternalContext().getSession(false);
 			session.invalidate();
			return "logout";
 		} catch (ServletException ex) {
 			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
			return "logout";
 		}
 	}
 
 	public User getLoggedUser() {
 		return loggedUser;
 	}
 
 	public void setLoggedUser(User loggedUser) {
 		this.loggedUser = loggedUser;
 	}
 
 	public boolean isCustomer() {
 		recognizeUser();
 		return isCustomer;
 	}
 
 	public void setCustomer(boolean isCustomer) {
 		this.isCustomer = isCustomer;
 	}
 
 	public boolean isAdmin() {
 		recognizeUser();
 		return isAdmin;
 	}
 
 	public void setAdmin(boolean isAdmin) {
 		this.isAdmin = isAdmin;
 	}
 
 	public boolean isUserLogged() {
 		recognizeUser();
 		if (isUserLogged || isAdmin || isCustomer) {
 			isUserLogged = true;
 		}
 		return isUserLogged;
 	}
 
 	public void setUserLogged(boolean isUserLogged) {
 		this.isUserLogged = isUserLogged;
 	}
 
 }
