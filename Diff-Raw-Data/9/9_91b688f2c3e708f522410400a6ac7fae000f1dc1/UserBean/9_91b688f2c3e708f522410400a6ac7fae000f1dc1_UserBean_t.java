 package net.latroquette.web.beans.profile;
 
 import java.io.Serializable;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.sql.Timestamp;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ComponentSystemEvent;
 import javax.servlet.http.HttpServletRequest;
 
 import net.latroquette.common.database.data.profile.User;
 import net.latroquette.common.database.data.profile.UsersService;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.adi3000.common.util.security.Security;
 import com.adi3000.common.web.faces.FacesUtils;
 import com.adi3000.common.web.jsf.UtilsBean;
 
 
 @ManagedBean(name = "userBean")
 @SessionScoped
 public class UserBean implements Serializable, com.adi3000.common.util.security.User{
 	
 	public static final int ANONYMOUS = -1;
 	public static final int NOT_LOGGED_IN = 0;
 	public static final int NEW_USER = 1;
 	public static final int LOGGED_IN = 2;
 	public static final String LOGIN_VIEW_URI = "profile/login";
 	public static final String PARAMETER_REQUEST_URI = "u";
 	public static final String PARAMETER_QUERY_STRING = "qs";
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 173422903879328102L;
 	private transient String passwordConfirm;
 	private transient String mailConfirm;
 	private User user;
 	private int loginState;
 	private String previousURI;
 	private String previousQueryString;
 	
 	public UserBean(){
 		user = new User();
 		loginState = ANONYMOUS;
 	}
 
 	/**
 	 * @return the passwordConfirm
 	 */
 	public String getPasswordConfirm() {
 		return passwordConfirm;
 	}
 	/**
 	 * @param passwordConfirm the passwordConfirm to set
 	 */
 	public void setPasswordConfirm(String passwordConfirm) {
 		this.passwordConfirm = passwordConfirm;
 	}
 	/**
 	 * @return the mailConfirm
 	 */
 	public String getMailConfirm() {
 		return mailConfirm;
 	}
 	/**
 	 * @param mailConfirm the mailConfirm to set
 	 */
 	public void setMailConfirm(String mailConfirm) {
 		this.mailConfirm = mailConfirm;
 	}
 
 	/**
 	 * @return the newUser
 	 */
 	public int getLoginState() {
 		return loginState;
 	}
 	/**
 	 * @param newUser the newUser to set
 	 */
 	public void getLoginState(int loginState) {
 		this.loginState = loginState;
 	}
 	public void validateAddressMail(ComponentSystemEvent event){
 
 		FacesContext fc = FacesContext.getCurrentInstance();
 
 		UIComponent components = event.getComponent();
 
 		//get textbox1 value
 		UIInput uiText1 = (UIInput)components.findComponent("addressMailField");
 		
 
 		//get textbox2 value
 		UIInput uiText2 = (UIInput)components.findComponent("addressMailFieldConfirmation");
 		if(uiText1 != null && uiText2 != null && uiText2.getLocalValue() != null && uiText1.getLocalValue() != null){
 			String text1 = uiText1.getLocalValue().toString();
 			String text2 = uiText2.getLocalValue().toString();
 	
 			if(!text1.equals(text2)){
 	
 				FacesMessage msg = new FacesMessage("Email check failed", 
 						"Address mail do not match with previous given");
 	
 				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 	
 				fc.addMessage(components.getClientId(), msg);
 	
 				//passed to the Render Response phase
 				fc.renderResponse();
 			}
 		}
 	}
 	
 	public void validatePassword(ComponentSystemEvent event){
 
 		FacesContext fc = FacesContext.getCurrentInstance();
 
 		UIComponent components = event.getComponent();
 
 		//get textbox1 value
 		UIInput uiText1 = (UIInput)components.findComponent("passwordField");
 		
 
 		//get textbox2 value
 		UIInput uiText2 = (UIInput)components.findComponent("passwordFieldConfirmation");
 		if(uiText1 != null && uiText2 != null && uiText2.getLocalValue() != null && uiText1.getLocalValue() != null){
 			String text2 = uiText2.getLocalValue().toString();
 			String text1 = uiText1.getLocalValue().toString();
 			if(!text1.equals(text2)){
 	
 				FacesMessage msg = new FacesMessage("Password check failed", 
 						"Password do not match with previous given");
 	
 				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 	
 				fc.addMessage(components.getClientId(), msg);
 	
 				//passed to the Render Response phase
 				fc.renderResponse();
 			}
 		}
 	}
 	
 	public String registerUser()
 	{
 		UsersService usersService = new UsersService();
 		User newUser = new User();
 		loginState = NOT_LOGGED_IN;
 		
 		
 		FacesContext fc = FacesContext.getCurrentInstance();
 		if(StringUtils.isEmpty(getMail())){
 			FacesMessage msg = new FacesMessage("Registring error[301]", 
 					"Email Empty : Registering failed, please try later or ask for support");
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			fc.addMessage(null, msg);
 			fc.validationFailed();
 		}
 		if(StringUtils.isEmpty(passwordConfirm)){
 			FacesMessage msg = new FacesMessage("Registring error[302]", 
 					"Confirm password Empty : Registering failed, please try later or ask for support");
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			fc.addMessage(null, msg);
 			fc.validationFailed();
 		}
 		if(fc.isValidationFailed()){
 			return null;
 		}
 		newUser.setLogin(this.getLogin());
 		newUser.setPassword(this.getPassword());
 		this.setLogginUserInfo(newUser);
 		if(!usersService.registerNewUser(newUser)){
 			FacesMessage msg = new FacesMessage("Registring error[303]", 
 					"User already usedRegistering failed, please try later or ask for support");
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			fc.addMessage(null, msg);
 			fc.validationFailed();
 		}
 		usersService.close();
 		this.loginState = NEW_USER;
 		return "/index";
 
 	}
 	
 	public String loginUser()
 	{
 		initPreviousURL();
 		String forwardUrl = "index";
 		if(!StringUtils.isEmpty(previousURI) && !previousQueryString.contains(LOGIN_VIEW_URI)){
 			forwardUrl = previousURI;
 			if(!StringUtils.isEmpty(previousQueryString)){
 				forwardUrl = forwardUrl.concat("?faces-redirect=true&").concat(UtilsBean.urlDecode(previousQueryString));
 			}
 		}
 		loginState = NOT_LOGGED_IN;
 		UsersService userSearch = new UsersService();
		user = userSearch.getUserByLogin(this.getLogin());
 		if(user != null && StringUtils.equals(user.getPassword(), this.getPassword())){
 			this.setLogginUserInfo(user);
 			this.loginState = LOGGED_IN;
 			userSearch.updateUser(user);
 		}
 		userSearch.close();
 		switch (loginState) {
 			case LOGGED_IN:
 				break;
 			default:
 				forwardUrl = "login?logInFail="+loginState;
 				if(!StringUtils.isEmpty(previousURI) && !previousQueryString.contains(LOGIN_VIEW_URI)){
 					forwardUrl = forwardUrl.concat("&"+PARAMETER_REQUEST_URI+"=").concat(previousURI);
 					if(!StringUtils.isEmpty(previousQueryString)){
 						forwardUrl = forwardUrl.concat("&"+PARAMETER_QUERY_STRING+"=").concat(previousQueryString);;
 					}
 				}
 				break;
 		}
 		return forwardUrl;
 	}
 	
 	public String logoutUser()
 	{
 		UsersService userSearch = new UsersService();
 		User user = userSearch.getUserByLogin(this.getLogin());
 		user.setToken(null);
 		userSearch.updateUser(user);
 		userSearch.close();
 		//Unset properties of this user
 		this.user = new User();
 		loginState = ANONYMOUS;
 		return "index";
 	}
 	
 	private void initPreviousURL(){
 		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
 		previousURI = params.get(PARAMETER_REQUEST_URI);
 		previousQueryString = params.get(PARAMETER_QUERY_STRING);
 	}
 	
 	private void setLogginUserInfo(User user){
 		Timestamp now = new Timestamp(new java.util.Date().getTime());
 		ExternalContext externe = FacesContext.getCurrentInstance().getExternalContext();
 		String ip = ((HttpServletRequest)externe.getRequest()).getRemoteAddr();
 		String host = ((HttpServletRequest)externe.getRequest()).getRemoteHost();
 		user.setLastDateLogin(now);
 		user.setLastIpLogin(ip);
 		user.setLastHostNameLogin(host);
 		user.setToken(Security.generateSessionID(user.hashCode(), user) );
 	}
 	
 	
 	public boolean getLoggedIn()
 	{
 		return (loginState == LOGGED_IN || loginState == NEW_USER) ;
 	}
 	
 	public boolean checkUserLogged(){
 		return checkUserLogged(this);
 	}
 	
 	public static boolean checkUserLogged(UserBean user){
 		if (!Security.isUserLogged(user) || !user.getLoggedIn()){
 			FacesUtils.navigationRedirect("/profile/login");
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public Integer getId() {
 		return user.getId();
 	}
 
 	@Override
 	public String getLogin() {
 		return user.getLogin();
 	}
 	
 	public void setLogin(String login) {
 		user.setLogin(login);
 	}
 
 	@Override
 	public Integer getToken() {
 		return user.getToken();
 	}
 
 	@Override
 	public String getPassword() {
 		return user.getPassword();
 	}
 
 	public void setPassword(String password) {
 		user.setPassword(password);
 	}
 
 	@Override
 	public String getLastHostNameLogin() {
 		return user.getLastHostNameLogin();
 	}
 
 	@Override
 	public String getMail() {
 		return user.getMail();
 	}
 	public void setMail(String mail) {
 		user.setMail(mail);
 	}
 
 	@Override
 	public Timestamp getLastDateLogin() {
 		return user.getLastDateLogin();
 	}
 
 	@Override
 	public String getLastIpLogin() {
 		return user.getLastIpLogin();
 	}
 
 	/**
 	 * @return the user
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * @return the previousQueryString
 	 */
 	public String getPreviousQueryString() {
 		return previousQueryString;
 	}
 
 	/**
 	 * @param previousQueryString the previousQueryString to set
 	 */
 	public void setPreviousQueryString(String previousQueryString) {
 		this.previousQueryString = previousQueryString;
 	}
 
 	/**
 	 * @return the previousURI
 	 */
 	public String getPreviousURI() {
 		return previousURI;
 	}
 
 	/**
 	 * @param previousURI the previousURI to set
 	 */
 	public void setPreviousURI(String previousURI) {
 		this.previousURI = previousURI;
 	}
 	
 	
 	
 }
