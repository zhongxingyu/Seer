 package de.htwg.seapal.person.models.impl;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import de.htwg.seapal.person.models.AbstractPerson;
 import de.htwg.seapal.person.models.IPerson;
 
 @Entity
 public class Person extends AbstractPerson {
 	
 	@Id @GeneratedValue(strategy=GenerationType.AUTO) 
 	private String id;

 	@Column
 	private String firstname = null;
 	
 	@Column
 	private String lastname= null;
 
 	@Column
 	private Date birth;
 	
 	@Column
 	private Date registration;
 	
 	@Column
 	private int age = 0;
 	
 	@Column
 	private String nationality = null;
 	
 	@Column
 	private String email = null;
 	
 	@Column
 	private String telephone = null;
 	
 	@Column
 	private String mobile = null;
 	
 	@Column
 	private String street = null;
 	
 	@Column
 	private int postcode = 0;
 	
 	@Column
 	private String city = null;
 	
 	@Column
 	private String country = null;
 	
 	public Person() {
 		
 	}
 	
 	public Person(IPerson person) {
 		id = person.getId();
 		
 		firstname = person.getFirstname();
 		lastname = person.getLastname();
 		birth = person.getBirth();
 		registration = person.getRegistration();
 		age = person.getAge();
 		nationality = person.getNationality();
 		
 		email = person.getEmail();
 		telephone = person.getTelephone();
 		mobile = person.getMobile();
 		
 		street = person.getStreet();
 		postcode = person.getPostcode();
 		city = person.getCity();
 		country = person.getCountry();
 	}
 	
 	public Person(int id) {
 		this.id = "PERSON-" + id;
 	}
 	
 	public String getId() {
 		return id;
 	}
 	
	public void setId(String id) {
		this.id = id;
	}
	
 	public String getFirstname() {
 		return firstname;
 	}
 	
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 	
 	public String getLastname() {
 		return lastname;
 	}
 	
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 	
 	public Date getBirth() {
 		return birth;
 	}
 	
 	public void setBirth(Date birth) {
 		this.birth = birth;
 	}
 	
 	public Date getRegistration() {
 		return registration;
 	}
 
 	public void setRegistration(Date registration) {
 		this.registration = registration;
 	}
 	
 	public int getAge() {
 		return age;
 	}
 	
 	public void setAge(int age) {
 		this.age = age;
 	}
 	
 	public String getNationality() {
 		return nationality;
 	}
 
 	public void setNationality(String nationality) {
 		this.nationality = nationality;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 	
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 	public String getTelephone() {
 		return telephone;
 	}
 	
 	public void setTelephone(String telephone) {
 		this.telephone = telephone;
 	}
 	
 	public String getMobile() {
 		return mobile;
 	}
 	
 	public void setMobile(String mobile) {
 		this.mobile = mobile;
 	}
 	
 	public String getStreet() {
 		return street;
 	}
 	
 	public void setStreet(String street) {
 		this.street = street;
 	}
 	
 	public int getPostcode() {
 		return postcode;
 	}
 	
 	public void setPostcode(int postcode) {
 		this.postcode = postcode;
 	}
 	
 	public String getCity() {
 		return city;
 	}
 	
 	public void setCity(String city) {
 		this.city = city;
 	}
 	
 	public String getCountry() {
 		return country;
 	}
 	
 	public void setCountry(String country) {
 		this.country = country;
 	}
 }
