 package cz.fi.muni.pv243.eshop.controller;
 
 import java.io.Serializable;
 
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import cz.fi.muni.pv243.eshop.data.Credentials;
 import cz.fi.muni.pv243.eshop.model.User;
 import cz.fi.muni.pv243.eshop.service.UserManager;
 
 @SessionScoped
 @Named
 public class Login implements Serializable {
 
 	private static final long serialVersionUID = 7965455427888195913L;
 
 	@Inject
 	private Credentials credentials;
 
 	@Inject
 	private UserManager userManager;
 
 	private User currentUser;
 
 	public void login() throws Exception {
 		User user = userManager.findUser(credentials.getEmail(),
 				credentials.getPassword());
 		if (user != null) {
 			this.currentUser = user;
 			FacesContext.getCurrentInstance().addMessage(null,
 					new FacesMessage("Welcome, " + currentUser.getName()));
		} else {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(
							"Non existing user, or passoword or both :)"));
 		}
 	}
 
 	public void logout() {
 		FacesContext.getCurrentInstance().addMessage(null,
 				new FacesMessage("Goodbye, " + currentUser.getName()));
 		currentUser = null;
 	}
 
 	public boolean isLoggedIn() {
 		return currentUser != null;
 	}
 
 	@Produces
 	@LoggedIn
 	public User getCurrentUser() {
 		return currentUser;
 	}
 
 }
