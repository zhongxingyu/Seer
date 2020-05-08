 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package server;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 
 import data.User;
 import ctrl.DBUser;
 
 import javax.faces.application.ConfigurableNavigationHandler;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ComponentSystemEvent;
 
 import util.PasswordHash;
 
 @ManagedBean(name = "LoginBean")
 @SessionScoped
 public class LoginBean {
 	private String email;
 	private String password;
 	private User dbUser;
 	private boolean loggedIn = false;
 	private String msg;
 	FacesContext fc;
 	ConfigurableNavigationHandler nav;
 
 	public LoginBean() {
 		fc = FacesContext.getCurrentInstance();
		nav = (ConfigurableNavigationHandler) fc.getApplication()
				.getNavigationHandler();
 	}
 
 	/**
 	 * loads the user with the specified email from the database
 	 * 
 	 * @param email
 	 * @return boolean
 	 */
 	private boolean loadUser(String email) {
 		dbUser = DBUser.loadUser(email);
 		if (dbUser != null)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * logs out the current user
 	 * 
 	 */
 	public void logout() {
 		dbUser = null;
 		loggedIn = false;
 		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		nav.performNavigation("test-index");
 	}
 
 	/**
 	 * checks if the login failed or was valid
 	 * 
 	 * @return either success page or the login-failed page
 	 */
 	public void checkSuccess() {
 		if (checkValidUser()) {
 			loggedIn = true;
 			// set session attributes for current user
 			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("role", dbUser.getRole());
 			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("email", dbUser.getEmail());
 			switch(dbUser.getRole()){
 			case "Administrator": 
 				nav.performNavigation("admin-index");
 				break;
 			case "Redakteur": 
 				nav.performNavigation("redakteur-index");
 				break;
 			case "Modulverantwortlicher": 
 				nav.performNavigation("modulverantwortlicher-index");
 				break;
 			case "Dezernat": 
 				nav.performNavigation("dezernat-index");
 				break;
 			case "Dekan": 
 				nav.performNavigation("dekan-index");
 				break;
 			}
 			
 		} else {
 			loggedIn = false;
 			addErrorMessage("login", "Login failed", "Please check your input.");
 		}
 	}
 	
 	/**
 	 * Resets the password of the selected user
 	 */
 	public void resetUserPass() {
 		String currentmail=email;
 		System.out.println("reset! " + currentmail);
 		if (loadUser(currentmail)) {
 			User tmp = DBUser.loadUser(currentmail);
 			System.out.println("reset password of user: " + tmp.toString());
 			addMessage("login",	"Reset success!",currentmail
 							+ " your password reset was successful - check your emails.");
 			tmp.resetPassword(10);
 			DBUser.updateUser(tmp, currentmail);
 		} else
 			addErrorMessage("login", "User does not Exist!",
 					"reset could not be completed - check your input");
 	}
 
 	/**
 	 * checks if the user and email exists
 	 * 
 	 * @return true if the user exists or false if he does not exist
 	 */
 	public boolean checkValidUser() {
 		if (email == "" || password == "")
 			return false;
 		if (loadUser(email)) {
 			// TODO validatePassFILLER => remove filler
 			try {
 				if (email.equalsIgnoreCase(dbUser.getEmail())
 						&& PasswordHash.validatePassword(password,
 								dbUser.getPassword())) {
 					System.out.println("valid");
 					return true;
 				} else {
 					System.out.println("invalid");
 					return false;
 				}
 			} catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvalidKeySpecException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		System.out.println("invalid");
 		return false;
 	}
 	
 
 	public void checkLoggedIn(ComponentSystemEvent event) {
 		System.out.println("checkLoggedIn");
 
 		if (!loggedIn) {
 			nav.performNavigation("login");
 		}
 		switch (dbUser.getRole()) {
 		case "Administrator": {
 			nav.performNavigation("admin-index");
 		}
 		}
 	}
 
 	/**
 	 * if anything goes wrong - display an Error. - target null equals all
 	 * message and growl elements else you can distinct messages through the
 	 * "for=<target>" in the xhtml
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addErrorMessage(String target, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(target,
 				new FacesMessage(FacesMessage.SEVERITY_FATAL, title, msg));
 	}
 
 	/**
 	 * Informs the User via message. - target null equals all message and growl
 	 * elements else you can distinct messages through the "for=<target>" in the
 	 * xhtml
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addMessage(String target, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(target,
 				new FacesMessage(FacesMessage.SEVERITY_INFO, title, msg));
 	}
 
 	/**
 	 * @return the loggedIn
 	 */
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 
 	/**
 	 * @return password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * @param password
 	 *            the password to set
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * @return email
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * @param email
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	/**
 	 * @return email the email to set
 	 */
 	public User getDbUser() {
 		return dbUser;
 	}
 
 	/**
 	 * @return the msg
 	 */
 	public String getMsg() {
 		return msg;
 	}
 
 	/**
 	 * @param msg
 	 *            the msg to set
 	 */
 	public void setMsg(String msg) {
 		this.msg = msg;
 	}
 }
