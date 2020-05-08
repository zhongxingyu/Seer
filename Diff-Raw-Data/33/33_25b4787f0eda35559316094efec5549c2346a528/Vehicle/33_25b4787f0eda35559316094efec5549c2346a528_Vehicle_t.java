 package models.obdmedb.vehicles;
 
 import play.*;
 import play.data.validation.Required;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 
 import java.util.*;
 
 @Entity
 @Table(name="vehicle")
 public class Vehicle extends Model {
 
 	@Required
 	@Column(name="vin", unique=true)
 	public String VIN;
 	
 	@Column(name="maker")
 	public String maker;
 	
 	@Column(name="model")
 	public String model;
 	
 	@Column(name="year")
 	public String year;
 	
	@Column(name="avatar512", columnDefinition="BLOB")
 	public Blob avatar512;
 
	@Column(name="avatar256", columnDefinition="BLOB")
 	public Blob avatar256;
 	
	@Column(name="avatar128", columnDefinition="BLOB")
 	public Blob avatar128;
 	
	@Column(name="avatar64", columnDefinition="BLOB")
 	public Blob avatar64;
 	
	@Column(name="avatar32", columnDefinition="BLOB")
 	public Blob avatar32;
 	
 	public String getVIN() {
 		return VIN;
 	}
 
 	public void setVIN(String VIN) {
 		this.VIN = VIN;
 	}
 
 	private Vehicle(String VIN) {
 		this.VIN = VIN;
 	}
 	
 	public static Vehicle addVehicleIfNotExist(String VIN) {
 		Vehicle veh = findByVIN(VIN);
 		if (veh == null) {
 			veh = new Vehicle(VIN.toUpperCase());
 			veh.validateAndSave();
 		}
 		return veh;
 	}
 	
 	public static Vehicle findByVIN(String VIN) {
 		return find("vin", VIN).first();
 	}	
 	
 	public static Vehicle findById(long id) {
 		return find("id", id).first();
 	}
 	
 	public String getMaker() {
 		return maker;
 	}
 
 	public void setMaker(String maker) {
 		this.maker = maker;
 	}
 
 	public String getModel() {
 		return model;
 	}
 
 	public void setModel(String model) {
 		this.model = model;
 	}
 
 	public String getYear() {
 		return year;
 	}
 
 	public void setYear(String year) {
 		this.year = year;
 	}
 
 	public Blob getAvatar512() {
 		return avatar512;
 	}
 
 	public void setAvatar512(Blob avatar512) {
 		this.avatar512 = avatar512;
 	}
 
 	public Blob getAvatar256() {
 		return avatar256;
 	}
 
 	public void setAvatar256(Blob avatar256) {
 		this.avatar256 = avatar256;
 	}
 
 	public Blob getAvatar128() {
 		return avatar128;
 	}
 
 	public void setAvatar128(Blob avatar128) {
 		this.avatar128 = avatar128;
 	}
 
 	public Blob getAvatar64() {
 		return avatar64;
 	}
 
 	public void setAvatar64(Blob avatar64) {
 		this.avatar64 = avatar64;
 	}
 
 	public Blob getAvatar32() {
 		return avatar32;
 	}
 
 	public void setAvatar32(Blob avatar32) {
 		this.avatar32 = avatar32;
 	}
     
 }
