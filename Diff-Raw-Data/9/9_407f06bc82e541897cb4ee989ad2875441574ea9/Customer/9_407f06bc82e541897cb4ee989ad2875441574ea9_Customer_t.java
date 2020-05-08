 package blue.hotel.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="customer")
 public class Customer implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private int id;
 	
 	private String name;
 	private String address;
 	private String company;
 	private String notes;
 	private double discount;
 	private String phone;
 	private String email;
 	private String web;
 	private String fax;
 	
	@ManyToMany(mappedBy="customers")
 	private List<Reservation> reservations;
 	
 	public String toString() {
 		return "Customer #" + id + " / " + name + " (" + address + ")";
 	}
 	
 	public Customer() {
 		
 	}
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	public String getCompany() {
 		return company;
 	}
 	public void setCompany(String company) {
 		this.company = company;
 	}
 	public String getNotes() {
 		return notes;
 	}
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 	public double getDiscount() {
 		return discount;
 	}
 	public void setDiscount(double discount) {
 		this.discount = discount;
 	}
 	public String getPhone() {
 		return phone;
 	}
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public String getWeb() {
 		return web;
 	}
 	public void setWeb(String web) {
 		this.web = web;
 	}
 	public String getFax() {
 		return fax;
 	}
 	public void setFax(String fax) {
 		this.fax = fax;
 	}
 
 	public List<Reservation> getReservations() {
 		return reservations;
 	}
 
 	public void setReservations(List<Reservation> reservations) {
 		this.reservations = reservations;
 	}
 }
