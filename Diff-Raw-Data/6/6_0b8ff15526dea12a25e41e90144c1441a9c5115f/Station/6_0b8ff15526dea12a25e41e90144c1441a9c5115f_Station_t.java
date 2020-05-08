 package beans;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name="station")
 public class Station {	

 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	@Column(name="idstation" )
 	private Integer idStation ;
 	@Column(name="nomstation")
 	private String nomStation;
 	@Column(name="commentaire")
 	private String commentaireStation;
 	@Column(name="latitude")
 	private double latitude;
 	@Column(name="longitude")
 	private double longitude;	
 	@OneToMany(fetch=FetchType.EAGER,mappedBy="train")
 	private Set<TrainHoraireStation> TrainHoraireStationList = new HashSet<TrainHoraireStation>();
 
 
    
 	@ManyToOne(
 			cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}
 	)
 	@JoinColumn(name="ligne_idligne")
 	private Ligne ligne;
 	
 
 	@ManyToMany(fetch = FetchType.EAGER,
 			cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
 	@JoinTable(name="station_has_station",
 	 joinColumns=@JoinColumn(name="station_idstation1"),
 	 inverseJoinColumns=@JoinColumn(name="station_idstation2")
 	)
 	private List<Station> stationAller;
 	
 	
 	@ManyToMany(
 			cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
 	@JoinTable(name="station_has_station",
 	 joinColumns=@JoinColumn(name="station_idstation2"),
 	 inverseJoinColumns=@JoinColumn(name="station_idstation1")
 	)
 	private List<Station> stationRetour;
 	
	public Station() {
		super();
		// TODO Auto-generated constructor stub
	}
 
 
 	public Integer getIdStation() {
 		return idStation;
 	}
 
 
 	public void setIdStation(Integer idStation) {
 		this.idStation = idStation;
 	}
 
 
 	public String getNomStation() {
 		return nomStation;
 	}
 
 
 	public void setNomStation(String nomStation) {
 		this.nomStation = nomStation;
 	}
 
 
 	public String getCommentaireStation() {
 		return commentaireStation;
 	}
 
 
 	public void setCommentaireStation(String commentaireStation) {
 		this.commentaireStation = commentaireStation;
 	}
 
 
 	public double getLatitude() {
 		return latitude;
 	}
 
 
 	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 
 
 	public double getLongitude() {
 		return longitude;
 	}
 
 
 	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 
 
 	public Ligne getLigne() {
 		return ligne;
 	}
 
 
 	public void setLigne(Ligne ligne) {
 		this.ligne = ligne;
 	}
 
 
 	public List<Station> getStationAller() {
 		return stationAller;
 	}
 
 
 	public void setStationAller(List<Station> stationAlle) {
 		this.stationAller = stationAlle;
 	}
 
 
 	public List<Station> getStationRetour() {
 		return stationRetour;
 	}
 
 
 	public void setStationRetour(List<Station> stationRetour) {
 		this.stationRetour = stationRetour;
 	}
 
 
 	public Set<TrainHoraireStation> getTrainHoraireStationList() {
 		return TrainHoraireStationList;
 	}
 
 
 	public void setTrainHoraireStationList(
 			Set<TrainHoraireStation> trainHoraireStationList) {
 		TrainHoraireStationList = trainHoraireStationList;
 	}
 
 
 
 
 }
