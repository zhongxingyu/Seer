 package nz.ac.victoria.ecs.kpsmart.entities.state;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Embeddable;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @Entity
 public final class Route extends StorageEntity implements Serializable {
 	@Cascade(CascadeType.ALL)
 	@OneToOne
 	private EntityID uid;
 	
 	@Id
 	private RoutePK primaryKey = new RoutePK();
 	
 	private float carrierWeightUnitCost;
 	
 	private float carrierVolumeUnitCost;
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date startingTime;
 	
 	private int frequency;
 	
 	private int duration;
 	
 	@Embeddable
 	public static final class RoutePK implements Serializable {
 		@Enumerated(EnumType.STRING)
 		private TransportMeans transportMeans;
 		
 		@OneToOne
 		private Location startPoint;
 		
 		@OneToOne
 		private Location endPoint;
 		
 		@ManyToOne
 		private Carrier carrier;
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((carrier == null) ? 0 : carrier.hashCode());
 			result = prime * result
 					+ ((endPoint == null) ? 0 : endPoint.hashCode());
 			result = prime * result
 					+ ((startPoint == null) ? 0 : startPoint.hashCode());
 			result = prime
 					* result
 					+ ((transportMeans == null) ? 0 : transportMeans.hashCode());
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
 			RoutePK other = (RoutePK) obj;
 			if (carrier == null) {
 				if (other.carrier != null)
 					return false;
 			} else if (!carrier.equals(other.carrier))
 				return false;
 			if (endPoint == null) {
 				if (other.endPoint != null)
 					return false;
 			} else if (!endPoint.equals(other.endPoint))
 				return false;
 			if (startPoint == null) {
 				if (other.startPoint != null)
 					return false;
 			} else if (!startPoint.equals(other.startPoint))
 				return false;
 			if (transportMeans != other.transportMeans)
 				return false;
 			return true;
 		}
 	}
 	
 	private Route() {}
 	public Route(final TransportMeans trans, final Location start, final Location end, final Carrier carrier) {
 		this.primaryKey.transportMeans = trans;
 		this.primaryKey.startPoint = start;
 		this.primaryKey.endPoint = end;
 		this.primaryKey.carrier = carrier;
 		this.uid = new EntityID();
 	}
 	
 	public static Route newInstance() {
 		Route r = new Route();
 		r.uid = new EntityID();
 		return r;
 	}
 	
 	public boolean isInternational() {
 		return this.getPrimaryKey().startPoint.isInternational() || this.getPrimaryKey().endPoint.isInternational();
 	}
 	
 	public long getId() {
 		return (uid == null)?0:uid.getId();
 	}
 
 	public EntityID getUid() {
 		return uid;
 	}
 
 	public void setUid(EntityID uid) {
 		this.uid = uid;
 	}
 
 	public TransportMeans getTransportMeans() {
 		return getPrimaryKey().transportMeans;
 	}
 
 	public void setTransportMeans(TransportMeans transportMeans) {
 		this.getPrimaryKey().transportMeans = transportMeans;
 	}
 
 	public Location getStartPoint() {
 		return getPrimaryKey().startPoint;
 	}
 
 	public void setStartPoint(Location startPoint) {
 		this.getPrimaryKey().startPoint = startPoint;
 	}
 
 	public Location getEndPoint() {
 		return getPrimaryKey().endPoint;
 	}
 
 	public void setEndPoint(Location endPoint) {
 		this.getPrimaryKey().endPoint = endPoint;
 	}
 
 	public Carrier getCarrier() {
 		return getPrimaryKey().carrier;
 	}
 
 	public void setCarrier(Carrier carrier) {
 		this.getPrimaryKey().carrier = carrier;
 	}
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public String toString() {
 		return "Route [id=" + getId() + ", transportMeans=" + getPrimaryKey().transportMeans
 				+ ", startPoint=" + getPrimaryKey().startPoint + ", endPoint=" + getPrimaryKey().endPoint
 				+ ", carrier=" + getPrimaryKey().carrier + ", carrierWeightUnitCost="
 				+ carrierWeightUnitCost + ", carrierVolumeUnitCost="
 				+ carrierVolumeUnitCost + ", startingTime=" + startingTime
 				+ ", frequency=" + frequency + ", duration=" + duration
 				+ ", disabled=" + disabled + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((getPrimaryKey().carrier == null) ? 0 : getPrimaryKey().carrier.hashCode());
 		result = prime * result + Float.floatToIntBits(carrierVolumeUnitCost);
 		result = prime * result + Float.floatToIntBits(carrierWeightUnitCost);
 		result = prime * result
 				+ ((disabled == null) ? 0 : disabled.hashCode());
 		result = prime * result + duration;
 		result = prime * result
 				+ ((getPrimaryKey().endPoint == null) ? 0 : getPrimaryKey().endPoint.hashCode());
 		result = prime * result + frequency;
 		result = prime * result + (int) (getId() ^ (getId() >>> 32));
 		result = prime * result
 				+ ((getPrimaryKey().startPoint == null) ? 0 : getPrimaryKey().startPoint.hashCode());
 		result = prime * result
 				+ ((startingTime == null) ? 0 : startingTime.hashCode());
 		result = prime * result
 				+ ((getPrimaryKey().transportMeans == null) ? 0 : getPrimaryKey().transportMeans.hashCode());
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
 		Route other = (Route) obj;
 		if (getPrimaryKey().carrier == null) {
 			if (other.getPrimaryKey().carrier != null)
 				return false;
 		} else if (!getPrimaryKey().carrier.equals(other.getPrimaryKey().carrier))
 			return false;
 		if (Float.floatToIntBits(carrierVolumeUnitCost) != Float
 				.floatToIntBits(other.carrierVolumeUnitCost))
 			return false;
 		if (Float.floatToIntBits(carrierWeightUnitCost) != Float
 				.floatToIntBits(other.carrierWeightUnitCost))
 			return false;
 		if (disabled != other.disabled)
 			return false;
 		if (duration != other.duration)
 			return false;
 		if (getPrimaryKey().endPoint == null) {
 			if (other.getPrimaryKey().endPoint != null)
 				return false;
 		} else if (!getPrimaryKey().endPoint.equals(other.getPrimaryKey().endPoint))
 			return false;
 		if (frequency != other.frequency)
 			return false;
 		if (getId() != other.getId())
 			return false;
 		if (getPrimaryKey().startPoint == null) {
 			if (other.getPrimaryKey().startPoint != null)
 				return false;
 		} else if (!getPrimaryKey().startPoint.equals(other.getPrimaryKey().startPoint))
 			return false;
 		if (startingTime == null) {
 			if (other.startingTime != null)
 				return false;
 		} else if (!startingTime.equals(other.startingTime))
 			return false;
 		if (getPrimaryKey().transportMeans != other.getPrimaryKey().transportMeans)
 			return false;
 		return true;
 	}
 
 	public float getCarrierWeightUnitCost() {
 		return carrierWeightUnitCost;
 	}
 
 	public void setCarrierWeightUnitCost(float carrierWeightUnitCost) {
 		this.carrierWeightUnitCost = carrierWeightUnitCost;
 	}
 
 	public float getCarrierVolumeUnitCost() {
 		return carrierVolumeUnitCost;
 	}
 
 	public void setCarrierVolumeUnitCost(float carrierVolumeUnitCost) {
 		this.carrierVolumeUnitCost = carrierVolumeUnitCost;
 	}
 
 	public Date getStartingTime() {
 		return startingTime;
 	}
 
 	public void setStartingTime(Date startingTime) {
 		this.startingTime = startingTime;
 	}
 
 	public int getFrequency() {
 		return frequency;
 	}
 
 	public void setFrequency(int frequency) {
 		this.frequency = frequency;
 	}
 
 	/**
	 * get the duration of the delivery in ms
 	 * @return
 	 */
 	public int getDuration() {
 		return duration;
 	}
 
 	/**
	 * set the duration of the delivery in ms
 	 * @param duration
 	 */
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	public RoutePK getPrimaryKey() {
 		return primaryKey;
 	}
 
 	public void setPrimaryKey(RoutePK primaryKey) {
 		this.primaryKey = primaryKey;
 	}
 	
 	public float getCost(MailDelivery mail) {
 		return Math.max(mail.getVolume() * this.getCarrierVolumeUnitCost(), mail.getWeight() * this.getCarrierWeightUnitCost());
 	}
 }
