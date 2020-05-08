 package epam.cdp.spring.task1.bean;
 
import javax.validation.constraints.Size;

 import org.hibernate.validator.constraints.NotBlank;
 
 public class RegistrationUserBean {
 
 	@NotBlank(message="login can not be blank")
	@Size(min=3, max=30, message="login should be from 3 to 30 symbols")
 	private String login;
 
 	@NotBlank (message="password can not be blank")
	@Size(min=3, max=25, message="password should be from 3 to 25 symbols")
 	private String password;
 
 	@NotBlank (message="passwordRepeat can not be blank")
	@Size(min=3, max=25, message="password should be from 3 to 25 symbols")
 	private String passwordRepeat;
 
 	public String getPasswordRepeat() {
 		return passwordRepeat;
 	}
 
 	public void setPasswordRepeat(String passwordRepeat) {
 		this.passwordRepeat = passwordRepeat;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 }
