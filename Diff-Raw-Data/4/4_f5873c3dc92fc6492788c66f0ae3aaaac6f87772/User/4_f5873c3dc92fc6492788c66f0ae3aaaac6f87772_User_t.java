 package rdproject.model;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 /**
  * 
  * @author Samuel Aquino
  *
  */
 @Entity
 @Table(name="USERS")
 public class User 
 {
 	private Long id;
 	private String username;
 	private String password;
 	private String role;
 	
 	public User()
 	{
 	}
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Long getId()
 	{
 		return id;
 	}
 	public void setId(Long id)
 	{
 		this.id = id;
 	}
 	@Column(name="username")
 	public String getUsername()
 	{
 		return username;
 	}
 	public void setUsername(String username)
 	{
 		this.username = username;
 	}
 	@Column(name="password")
 	public String getPassword()
 	{
 		return password;
 	}
 	public void setPassword(String password)
 	{
 		this.password = password;
 	}
 	@Column(name="role")
 	public String getRole()
 	{
 		return role;
 	}
 	public void setRole(String role)
 	{
 		this.role = role;
 	}
 }
