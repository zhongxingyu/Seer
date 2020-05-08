 package nz.ac.victoria.ecs.kpsmart.entities.state;
 
 import java.io.Serializable;
 
 import javax.persistence.Embeddable;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 @Entity
 @Table(uniqueConstraints=@UniqueConstraint(columnNames={"name"}))
 public final class Location extends StorageEntity implements Serializable {
 //	@Cascade(CascadeType.ALL)
 //	@OneToOne
 //	private EntityID id;
 	@Id
 	@GeneratedValue
 	private long id;
 	
 	private String name;
 	
 	
 	private LocationPK primaryKey = new LocationPK();
 	
 	@Enumerated(EnumType.STRING)
 	private Bool international = Bool.True;
 	
 	@Override
 	public boolean isNonUnique(StorageEntity entity) {
 		if (!(entity instanceof Location))
 			return false;
 		Location l = (Location) entity;
 		
 		return  this.primaryKey.latitude == l.primaryKey.latitude &&
 				this.primaryKey.longitude == l.primaryKey.longitude;
 	}
 	
 	@Embeddable
 	public static final class LocationPK implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		private double latitude;
 		
 		private double longitude;
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			long temp;
 			temp = Double.doubleToLongBits(latitude);
 			result = prime * result + (int) (temp ^ (temp >>> 32));
 			temp = Double.doubleToLongBits(longitude);
 			result = prime * result + (int) (temp ^ (temp >>> 32));
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			LocationPK other = (LocationPK) obj;
 			if (Double.doubleToLongBits(latitude) != Double
 					.doubleToLongBits(other.latitude))
 				return false;
 			if (Double.doubleToLongBits(longitude) != Double
 					.doubleToLongBits(other.longitude))
 				return false;
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return "LocationPK [latitude=" + latitude + ", longitude="
 					+ longitude + "]";
 		}
 	}
 	
 	Location() {}
	public Location(final String name, final double latitude, final double longitude, final boolean international) {		
 		this.name = name;
 		this.primaryKey.latitude = latitude;
 		this.primaryKey.longitude = longitude;
 		this.international = international ? Bool.True : Bool.False;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public double getLatitude() {
 		return getPrimaryKey().latitude;
 	}
 
 	public void setLatitude(double latitude) {
 		this.getPrimaryKey().latitude = latitude;
 	}
 
 	public double getLongitude() {
 		return getPrimaryKey().longitude;
 	}
 
 	public void setLongitude(double longitude) {
 		this.getPrimaryKey().longitude = longitude;
 	}
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public String toString() {
 		return "Location [name=" + name + ", primaryKey=" + primaryKey
 				+ ", international=" + international + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((international == null) ? 0 : international.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result
 				+ ((primaryKey == null) ? 0 : primaryKey.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Location other = (Location) obj;
 		if (international != other.international)
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (primaryKey == null) {
 			if (other.primaryKey != null)
 				return false;
 		} else if (!primaryKey.equals(other.primaryKey))
 			return false;
 		return true;
 	}
 
 	public boolean isInternational() {
 		return international == Bool.True;
 	}
 
 	public void setInternational(boolean international) {
 		this.international = international ? Bool.True : Bool.False;
 	}
 
 	public LocationPK getPrimaryKey() {
 		return primaryKey;
 	}
 
 	public void setPrimaryKey(LocationPK primaryKey) {
 		this.primaryKey = primaryKey;
 	}
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 }
