 package se.enbohms.hhcib.facade.mypages;
 
 import java.io.Serializable;
 
 import javax.enterprise.context.RequestScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import se.enbohms.hhcib.entity.Email;
 import se.enbohms.hhcib.entity.validator.NotNullOrEmpty;
 import se.enbohms.hhcib.entity.validator.Password;
 import se.enbohms.hhcib.service.api.UserService;
 import se.enbohms.hhcib.service.impl.UserServiceUtil;
 
 /**
  * JSF facade handling user sign ups
  * 
  */
 @Named
 @RequestScoped
 public class SignUpFacade implements Serializable {
 
 	private static final long serialVersionUID = -2711087335089608672L;
 
 	@NotNullOrEmpty(message = "Användarnamn kan inte vara tomt")
 	private String userName;
 
 	@se.enbohms.hhcib.entity.validator.Email(message = "Ange en giltig E-post adress")
 	private String email;
 
 	@Password(message = "Lösenord måste var minst 4 tecken långt")
 	private String password;
 
 	private String repeatedPassword;
 
 	@Inject
 	private UserService loginService;
 
 	@Inject
 	private UserServiceUtil userServiceUtil;
 
 	/**
 	 * Creates a new user with the supplied username, email and password
 	 */
 	public void signUp() {
 		if (!password.equals(repeatedPassword)) {
 			addPasswordDiffersMessage();
		} else if (!userServiceUtil.unique(getUserName())) {
			addMessageUserNameNotUnique();
 		} else {
 			handleCreateUser();
 		}
 	}
 
 	private void handleCreateUser() {
 		loginService.createUser(userName, Email.of(email), password);
 		addSuccessMesssage();
 		resetForm();
 	}
 
 	private void resetForm() {
 		setUserName(null);
 		setEmail(null);
 	}
 
 	/**
 	 * Handles JSF ajax event for determine if supplied email exist
 	 * 
 	 * @param event
 	 */
 	public void userNameExist(AjaxBehaviorEvent event) {
 		checkUniqueUserName();
 	}
 
 	private void checkUniqueUserName() {
 		if (!userServiceUtil.unique(getUserName())) {
 			addMessageUserNameNotUnique();
 		}
 	}
 
 	private void addMessageUserNameNotUnique() {
 		FacesContext.getCurrentInstance().addMessage(
 				null,
 				new FacesMessage(FacesMessage.SEVERITY_ERROR,
 						"Användarnamnet är upptaget",
 						"Användarnamnet är upptaget"));
 	}
 
 	private void addPasswordDiffersMessage() {
 		FacesContext.getCurrentInstance().addMessage(
 				null,
 				new FacesMessage(FacesMessage.SEVERITY_ERROR,
 						"Lösenorden skiljer sig åt",
 						"Lösenorden skiljer sig åt"));
 	}
 
 	private void addSuccessMesssage() {
 		FacesContext.getCurrentInstance().addMessage(
 				null,
 				new FacesMessage(FacesMessage.SEVERITY_INFO,
 						"En ny användare har skapats",
 						"En ny användare har skapats"));
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getRepeatedPassword() {
 		return repeatedPassword;
 	}
 
 	public void setRepeatedPassword(String repeatedPassword) {
 		this.repeatedPassword = repeatedPassword;
 	}
 }
