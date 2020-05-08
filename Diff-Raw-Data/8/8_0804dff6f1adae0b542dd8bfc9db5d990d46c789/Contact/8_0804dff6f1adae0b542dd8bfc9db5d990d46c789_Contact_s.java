 package fr.esieaprojet.business;
 
 
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.UUID;
 
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Size;
 
 import org.hibernate.validator.constraints.Email;
 import org.springframework.context.annotation.Scope;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.stereotype.Component;
 
 
 public class Contact {
 
 	private String id = UUID.randomUUID().toString();
 	
 	private String firstName;
 	
 	private String lastName;
 	
 	private String mail;
 	
 	private String birthday;
 	
 	private boolean active = false;
 	
 	private String activeMessage = "inactif";
 	
 	private ArrayList<DeliveryAdress> deliveryAdresses;
 	
 	private BillingAdress bilingAdress;
 	
 	private String password;
 	
 	public Contact () {
 		bilingAdress = new BillingAdress();
 		deliveryAdresses = new ArrayList<DeliveryAdress>();
 		deliveryAdresses.add(new DeliveryAdress());
 		deliveryAdresses.add(new DeliveryAdress());
 		deliveryAdresses.add(new DeliveryAdress());
 	}
 	
 	private String normalize ( String string ) {
 		/*
 		return Normalizer
 		           .normalize(string, Normalizer.Form.NFD)
 		           .replaceAll("[^\\p{ASCII}]", "");
 		           */
		return string;
 	}
 	
 	
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	public void setFirstName(String firstName) {
 		this.firstName = normalize(firstName.toLowerCase());
 	}
 	
 	public String getLastName() {
 		return lastName;
 	}
 	
 	public void setLastName(String lastName) {
 		this.lastName = normalize(lastName.toLowerCase());
 	}
 
 	public String getMail() {
 		return mail;
 	}
 
 	public void setMail(String mail) {
 		this.mail = mail;
 	}
 
 	public String getBirthday() {
 		return birthday;
 	}
 
 	public void setBirthday(String birthday) {
 		this.birthday = birthday;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 		if(active == false)
 			setActiveMessage("inactif");
 		else
 			setActiveMessage("actif");
 	}
 
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public BillingAdress getBillingAdress() {
 		return bilingAdress;
 	}
 
 	public void setBilingAdress(BillingAdress bilingAdress) {
 		this.bilingAdress = bilingAdress;
 	}
 	
 	
 	public ArrayList<DeliveryAdress> getDeliveryAdresses() {
 		return deliveryAdresses;
 	}
 
 	public void setDeliveryAdresses(ArrayList<DeliveryAdress> deliveryAdresses) {
 		this.deliveryAdresses = deliveryAdresses;
 	}
 
 	public String getActiveMessage() {
 		return activeMessage;
 	}
 
 	public void setActiveMessage(String activeMessage) {
 		this.activeMessage = activeMessage;
 	}
 }
