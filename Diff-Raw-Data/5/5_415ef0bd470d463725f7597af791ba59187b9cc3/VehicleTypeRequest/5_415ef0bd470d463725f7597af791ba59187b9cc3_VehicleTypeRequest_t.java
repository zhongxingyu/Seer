 package coop.tecso.donde.estaciono.communication.model;
 
import org.codehaus.jackson.map.annotate.JsonSerialize;


 /**
  * 
  * @author joel.delvalle
  * 
  */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
 public class VehicleTypeRequest {
 
 	private Long id;
 
 	private String parkingIdentificationCode;
 
 	private String description;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getParkingIdentificationCode() {
 		return parkingIdentificationCode;
 	}
 
 	public void setParkingIdentificationCode(String parkingIdentificationCode) {
 		this.parkingIdentificationCode = parkingIdentificationCode;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 }
