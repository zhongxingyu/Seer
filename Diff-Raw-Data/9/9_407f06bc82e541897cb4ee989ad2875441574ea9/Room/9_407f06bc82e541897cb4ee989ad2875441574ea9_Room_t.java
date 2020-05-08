 package blue.hotel.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="room")
 public class Room implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private int id;
 	private String name;
 	private int maxPersons;
 	private double singlePrice;
 	private double doublePrice;
 	private double triplePrice;
 	private double singleTwoKidsPrice;
 	private double singleOneKidPrice;
 	private double doubleOneKidPrice;
 	
	@OneToMany(mappedBy="roomReservationId.room")
 	private List<RoomReservation> reservations;
 	
 	public String toString() {
 		return "Room #" + id + " / " + name;
 	}
 	
 	public Room() {
 	}
 
 	public Room(int id, String name, int maxPersons, double singlePrice,
 			double doublePrice, double triplePrice, double singleTwoKidsPrice,
 			double singleOneKidPrice, double doubleOneKidPrice) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.maxPersons = maxPersons;
 		this.singlePrice = singlePrice;
 		this.doublePrice = doublePrice;
 		this.triplePrice = triplePrice;
 		this.singleTwoKidsPrice = singleTwoKidsPrice;
 		this.singleOneKidPrice = singleOneKidPrice;
 		this.doubleOneKidPrice = doubleOneKidPrice;
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
 
 	public int getMaxPersons() {
 		return maxPersons;
 	}
 
 	public void setMaxPersons(int maxPersons) {
 		this.maxPersons = maxPersons;
 	}
 
 	public double getSinglePrice() {
 		return singlePrice;
 	}
 
 	public void setSinglePrice(double singlePrice) {
 		this.singlePrice = singlePrice;
 	}
 
 	public double getDoublePrice() {
 		return doublePrice;
 	}
 
 	public void setDoublePrice(double doublePrice) {
 		this.doublePrice = doublePrice;
 	}
 
 	public double getTriplePrice() {
 		return triplePrice;
 	}
 
 	public void setTriplePrice(double triplePrice) {
 		this.triplePrice = triplePrice;
 	}
 
 	public double getSingleTwoKidsPrice() {
 		return singleTwoKidsPrice;
 	}
 
 	public void setSingleTwoKidsPrice(double singleTwoKidsPrice) {
 		this.singleTwoKidsPrice = singleTwoKidsPrice;
 	}
 
 	public double getSingleOneKidPrice() {
 		return singleOneKidPrice;
 	}
 
 	public void setSingleOneKidPrice(double singleOneKidPrice) {
 		this.singleOneKidPrice = singleOneKidPrice;
 	}
 
 	public double getDoubleOneKidPrice() {
 		return doubleOneKidPrice;
 	}
 
 	public void setDoubleOneKidPrice(double doubleOneKidPrice) {
 		this.doubleOneKidPrice = doubleOneKidPrice;
 	}
 
 	public List<RoomReservation> getReservations() {
 		return reservations;
 	}
 
 	public void setReservations(List<RoomReservation> reservations) {
 		this.reservations = reservations;
 	}
 }
