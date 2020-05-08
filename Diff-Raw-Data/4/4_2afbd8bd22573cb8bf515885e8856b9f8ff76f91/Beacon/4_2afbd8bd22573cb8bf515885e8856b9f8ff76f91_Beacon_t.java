 package ca.idrc.tagin.model;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import com.google.appengine.api.datastore.Key;
 
 @Entity
 public class Beacon implements Comparable<Beacon> {
 
 	public static final Double NULL_RSSI = -1.0;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Key key;
 
 	@Basic
 	private String id; // represented by BSSID + frequency
 
 	@Basic
 	private Double rssi;
 
 	@Basic
 	private Double rank;
 
 	public Beacon() {
 		this.id = null;
 		this.rssi = null;
 		this.rank = null;
 	}
 
 	public Beacon(String bssid, Integer frequency, Integer dbm) {
 		this.id = bssid + ";" + frequency;
 		this.rssi = Util.dBm2Power(dbm);
 		this.rank = null;
 	}
 	
 	public void updateRank(Double maxRssi) {
 		this.rank = Util.calculateRank(rssi, maxRssi);
 	}
 
 	public void setId(String bssid, Integer frequency) {
 		this.id = bssid + ";" + frequency;
 	}
 
 	public void setRssi(Double rssi) {
 		this.rssi = rssi;
 	}
 
 	public void setRank(Double rank) {
 		this.rank = rank;
 	}
 
 	public Double getRank() {
 		return rank;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public Double getRssi() {
 		return rssi;
 	}
 
 	@Override
 	public int compareTo(Beacon beacon) {
		if (beacon.getRssi() > getRssi()) return 1;
		if (beacon.getRssi() < getRssi()) return -1;
		return 0;
 	}
 
 	public String toString() {
 		return getClass().getName() + "[" + 
 				"ID: " + getId() + 
 				", RSSI: " + getRssi() + 
 				", rank: " + getRank() + "]";
 	}
 
 }
