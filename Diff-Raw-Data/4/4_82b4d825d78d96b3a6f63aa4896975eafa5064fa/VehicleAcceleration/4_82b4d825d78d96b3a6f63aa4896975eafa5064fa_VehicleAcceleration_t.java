 package models.obdmedb.inertial;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 import play.db.jpa.Model;
 
 @Entity
 @Embeddable
 @Table(name="vehicleacceleration")
 @SuppressWarnings("serial")
 public class VehicleAcceleration extends Model {
 	
 	@Column(name="accel_x")
 	public float accel_x;
 	
 	@Column(name="accel_y")
 	public float accel_y;
 	
 	@Column(name="accel_z")
 	public float accel_z;
 	
 	@Column(name="linear_accel_x")
 	public float linear_accel_x;
 	
 	@Column(name="linear_accel_y")
 	public float linear_accel_y;
 	
 	@Column(name="linear_accel_z")
 	public float linear_accel_z;
 	
 	
 	public VehicleAcceleration(float accel_x, float accel_y, float accel_z, 
 			float linear_accel_x, float linear_accel_y, float linear_accel_z) {
 		
 		this.accel_x = accel_x;
 		this.accel_y = accel_y;
 		this.accel_z = accel_z;
		this.linear_accel_x = linear_accel_x;
		this.linear_accel_y = linear_accel_y;
		this.linear_accel_z = linear_accel_z;
 	}
 
 }
