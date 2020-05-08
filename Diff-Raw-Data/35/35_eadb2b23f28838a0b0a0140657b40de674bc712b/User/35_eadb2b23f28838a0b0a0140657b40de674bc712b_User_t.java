 package epam.cdp.spring.task1.bean;
 
import javax.validation.constraints.Size;

 import org.hibernate.validator.constraints.NotBlank;
 
 public class User {
 
 	@NotBlank(message="login can not be blank")
	@Size(min=3, max=30, message="login should be from 3 to 30 symbols")
 	private String login;
 
 	@NotBlank (message="password can not be blank")
	@Size(min=3, max=25, message="password should be from 3 to 25 symbols")
 	private String password;
 	
 	public User() {
 		
 	}
 
 	public User(String login, String password) {
 		this.login = login;
 		this.password = password;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((login == null) ? 0 : login.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (login == null) {
 			if (other.login != null)
 				return false;
 		} else if (!login.equals(other.login))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "User [name=" + login + ", password=" + password + "]";
 	}
 
 }
