 package org.endeios.penguineye.domain;
 
 import java.io.UnsupportedEncodingException;
 import java.text.MessageFormat;
 import java.util.List;
 
 import javassist.expr.Instanceof;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import org.endeios.penguineye.domain.validation.Email;
 import org.endeios.penguineye.service.PenguinUtil;
 
 @Entity
 public class Penguin{
 	
 	@Id
 	@GeneratedValue
 	private long id;
 	
 	@Email
 	private String email;
 	
 	private String username;
 	
 	private String password;
 	
 	@OneToMany(mappedBy="penguin",fetch=FetchType.EAGER)
 	private List<Zone> zones;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
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
 		try {
 			this.password = PenguinUtil.toSHA1(password.getBytes("UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public List<Zone> getZones() {
 		return zones;
 	}
 
 	public void setZones(List<Zone> zones) {
 		this.zones = zones;
 	}
 	
 	@Override
 	public String toString() {
 		return MessageFormat.format("({3}) {0} - {1} - {2}", email,username,password,id);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(!(obj instanceof Penguin))
 			return false;
 		Penguin p = (Penguin)obj;
 		return p.email==email && p.id==id && p.password==password&& p.username==username; 
 			
 	}
 
 }
