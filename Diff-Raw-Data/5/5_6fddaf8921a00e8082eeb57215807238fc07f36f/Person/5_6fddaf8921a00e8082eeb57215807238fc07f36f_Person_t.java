 package patel.dipesh.jsr303.model;
 
 import java.io.Serializable;
 
 /**
  * 
  * Sample model class representing a "Person"
  * @author Dipesh Patel
  *
  */
 public class Person implements Serializable{
 	
 	private static final long serialVersionUID = 1L;
 	
 	private String firstName ;	
 	private String lastName ;	
 	private String eMail ;	
 	private Integer age ;
 	private String phone ;
 	private Address address ;
 
 	
 	/*
 	 * Accessors for setting Person's first name.
 	 */
 	public void setFirstName(String inp) {		
 		this.firstName = inp ;		
 	}
 	
 	public String getFirstName() {
 		return this.firstName ;		
 	}
 	
 	
 	/*
 	 * Accessors for setting Person's last name.
 	 */
 	public void setLastName(String inp) {		
 		this.firstName = inp ;		
 	}
 	
 	public String getLastName() {
 		return this.lastName ;		
 	}
 	
 
 	/*
 	 * Accessors for setting Person's email address.
 	 */
 	public void setEmail(String inp) {		
 		this.eMail = inp ;		
 	}
 	
 	public String getEmail() {
 		return this.eMail ;		
 	}
 
 
 	/*
 	 * Accessors for setting Person's age.
 	 */
 	public void setAge(Integer inp) {		
 		this.age = inp ;		
 	}
 	
 	public Integer getAge() {
 		return this.age ;		
 	}
 
 	/*
 	 * Accessors for setting Person's phone number.
 	 */
 	public void setPhone(String inp) {		
 		this.phone = inp ;		
 	}
 	
 	public String getPhone() {
 		return this.phone ;		
 	}
 
 	
 	/*
 	 * Accessors for setting Person's Address.
 	 */
 	public void setAddress(Address inp) {		
 		this.address = inp ;		
 	}
 	
 	public Address getAddress() {
 		return this.address ;		
 	}
 
 }
