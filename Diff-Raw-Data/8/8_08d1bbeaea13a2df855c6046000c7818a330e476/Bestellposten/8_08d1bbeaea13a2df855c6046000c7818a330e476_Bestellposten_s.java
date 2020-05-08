 // Autor - Dennis
 
 package de.shop.bestellverwaltung.domain;
 import java.io.Serializable;
 
 import javax.persistence.NamedQuery;
 
 import de.shop.produktverwaltung.domain.Produktdaten;
 import de.shop.util.IdGroup;
 
 import java.util.Date;
 
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 
 
 /**
  * Die Klasse Bestellposten reprsentiert einen Bestellposten des Shops.
  * 
  * @see Bestellposten
  * @author Dennis Brull
  * 
  */
 @Entity
 @Table(name = "Bestellposten")
 @Cacheable
 @NamedQueries({
 	@NamedQuery(
 			name = Bestellposten.ALL_BESTELLPOSTEN,
 			query = "FROM Bestellposten"),
 	@NamedQuery(
 			name = Bestellposten.NUR_BESTELLPOSTEN_NACH_ID,
			query = "from Bestellposten where bestellposten_ID = :id"),
 	@NamedQuery(
 			name = Bestellposten.BESTELLPOSTEN_MIT_BESTELLUNG,
			query = "SELECT distinct b, bs FROM Bestellposten b JOIN b.bestellung bs where bestellposten_Id = :id"),
 	@NamedQuery(
 			name = Bestellposten.BESTELLPOSTEN_NACH_BESTELLUNG,
 			query = "from Bestellposten b where b.bestellung.bestellungID = :bestellungFk"),
 	@NamedQuery(
 			name = Bestellposten.BESTELLPOSTEN_NACH_PRODUKTDATEN,
 			query = "from Bestellposten b where b.produktdaten.produktdatenID = :produktdatenFk"),
 	@NamedQuery(
 			name = Bestellposten.BESTELLPOSTEN_NACH_ANZAHL,
 			query = "from Bestellposten where anzahl = :anzahl"),
 	@NamedQuery(
 			name = Bestellposten.BESTELLPOSTEN_UPDATE_FK,
			query = "update Bestellposten set bestellung_fk = :bestellungfk where bestellposten_id = :id")
 })
 public class Bestellposten implements Serializable {
 	
 	// /////////////////////////////////////////////////////////////////////
 	// ATTRIBUTES
 	// /////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * SerialVersionUID
 	 */
 	private static final long serialVersionUID = -235431191239938542L;
 
 	private static final int PAUSE = 2000;
 	/**
 	 * Prefix dieser Klasse fr die Namen der NamedQueries
 	 */
 	public static final String PREFIX = "Bestellposten.";
 	
 	/**
 	 * Namen fr NamedQueries
 	 */
 	public static final String BESTELLPOSTEN_UPDATE_FK = PREFIX
 			+ "BestellpostenChangeFk";
 	public static final String ALL_BESTELLPOSTEN = PREFIX
 			+ "AllBestellposten";
 	public static final String NUR_BESTELLPOSTEN_NACH_ID = PREFIX
 			+ "SucheNachId";
 	
 	public static final String BESTELLPOSTEN_MIT_BESTELLUNG = PREFIX
 			+ "SucheMitBestellung";
 	
 	public static final String BESTELLPOSTEN_NACH_BESTELLUNG = PREFIX
 			+ "SucheNachBestellung";
 	
 	public static final String BESTELLPOSTEN_NACH_PRODUKTDATEN = PREFIX
 			+ "SucheNachProduktdaten";
 	
 	public static final String BESTELLPOSTEN_NACH_ANZAHL = PREFIX
 			+ "SucheNachAnzahl";
 
 	/**
 	 * SerialVersionUID
 	 */
 
 	/**
 	 * ID des Bestellpostens
 	 */
 	@Id
 	@GeneratedValue
 	@Column(name = "Bestellposten_ID", updatable = false, nullable = false, unique = true)
 	@Min(value = 0, groups = IdGroup.class, message = "{bestellverwaltung.bestellposten.id.NotEmpty}")
 	@NotNull(groups = IdGroup.class, message = "{bestellverwaltung.bestellposten.id.NotEmpty}")
 	private Integer bestellpostenID;
 	
 	/**
 	 * Anzahl der Produkte im Bestellposten
 	 */
 	@Column(name = "Anzahl")
 	@Min(value = 1, message = "{bestellverwaltung.bestellposten.anzahl.NotEmpty}")
 	private int anzahl;
 
 	/**
 	 * Erstelldatum
 	 */
 	@Column(name = "Erstellt", nullable = false, updatable = false)
 	@Past(message = "{bestellverwaltung.bestellposten.seit.past}")
 	@JsonIgnore
 	private Date erstellt;
 
 	/**
 	 * nderungsdatum
 	 */
 	@Column(name = "Geaendert")
 	@Past
 	@JsonIgnore
 	private Date geaendert;
 
 	/**
 	 * Bestellung, zu der dieser Bestellposten gehrt
 	 */
 	@ManyToOne
 	@JoinColumn(name = "Bestellung_FK")
 	@Valid
 	private Bestellung bestellung;
 	
 	/**
 	 * Produktdaten zu dem Bestellposten
 	 */
 	@ManyToOne
 	@JoinColumn(name = "Produktdaten_FK")
 	private Produktdaten produktdaten;
 
 	/**
 	 * Wird vor dem Speichern in die DB aufgerufen
 	 */
 	@PrePersist
 	private void bereiteSpeichernVor() {
 		this.erstellt = new Date();
 		this.geaendert = new Date();
 		this.erstellt.setTime(this.erstellt.getTime() - PAUSE);
 		this.geaendert.setTime(this.geaendert.getTime() - PAUSE);
 	}
 
 	/**
 	 * Wird vor dem Aktualisieren/Updaten in der DB aufgerufen
 	 */
 	@PreUpdate
 	private void bereiteUpdateVor() {
 		this.geaendert = new Date();
 		this.geaendert.setTime(this.geaendert.getTime() - PAUSE);
 	}
 	
 	// /////////////////////////////////////////////////////////////////////
 	// CONSTRUCTOR
 	// /////////////////////////////////////////////////////////////////////
 	/**
 	 * Standart Konstruktur, der alle Felder (leer) bzw. bei Anzahl 0 initialisiert
 	 */
 	public Bestellposten() {
 		
 
 		this.anzahl = 1;
 		this.erstellt = new Date();
 		this.geaendert = new Date();
 
 	}
 	
 	/**
 	 * Konstruktor mit Parameter fr Anzahl, Bestellung_FK und Produktdaten_FK
 	 * 
 	 * @param Bestellung_FK
 	 *            FK der zugehrigen Bestellung
 	 * @param Produktdaten_FK
 	 *            FK der zugehrigen Produktdaten
 	 * @param anzahl
 	 * 				Anzahl der Bestellposten           
 	 */
 	public Bestellposten(Bestellung bestellung, Produktdaten produktdaten, int anzahl) {
 
 		this.anzahl = anzahl;
 		this.bestellung = bestellung;
 		this.produktdaten = produktdaten;
 		
 	}
 
 	public Bestellposten setValues(Bestellposten bestellposten) {
 		this.produktdaten = bestellposten.getProduktdaten();
 		this.bestellung = bestellposten.getBestellung();	
 		this.anzahl = bestellposten.getAnzahl();
 		return this;
 	}
 	// /////////////////////////////////////////////////////////////////////
 	// GETTER & SETTER
 	// /////////////////////////////////////////////////////////////////////
 	
 	public Integer getBestellpostenID() {
 		return this.bestellpostenID;
 	}
 
 	public void setBestellpostenID(Integer bestellpostenID) {
 		this.bestellpostenID = bestellpostenID;
 	}
 
 	public int getAnzahl() {
 		return this.anzahl;
 	}
 
 	public void setAnzahl(int anzahl) {
 		this.anzahl = anzahl;
 	}
 	
 	public Date getErstellt() {
 		return erstellt == null ? null : (Date) erstellt.clone();
 	}
 
 	public void setErstellt(Date erstellt) {
 		this.erstellt = erstellt == null ? null : (Date) erstellt.clone();
 	}
 
 	public Date getGeaendert() {
 		return geaendert == null ? null : (Date) geaendert.clone();
 	}
 
 	public void setGeaendert(Date geaendert) {
 		this.geaendert = geaendert == null ? null : (Date) geaendert.clone();
 	}
 
 	public Bestellung getBestellung() {
 		return this.bestellung;
 	}
 
 	public void setBestellung(Bestellung bestellung) {
 		this.bestellung = bestellung;
 	}
 
 	public Produktdaten getProduktdaten() {
 		return this.produktdaten;
 	}
 
 	public void setProduktdaten(Produktdaten produktdaten) {
 		this.produktdaten = produktdaten;
 	}
 	
 	@Override
 	public String toString() {
 		return "Bestellposten [bestellpostenID=" + bestellpostenID
 				+ ", anzahl=" + anzahl + ", erstellt=" + erstellt
 				+ ", geaendert=" + geaendert + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + anzahl;
 		result = prime * result
 				+ ((erstellt == null) ? 0 : erstellt.hashCode());
 		result = prime * result
 				+ ((geaendert == null) ? 0 : geaendert.hashCode());
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
 		final Bestellposten other = (Bestellposten) obj;
 		if (anzahl != other.anzahl)
 			return false;
 		if (!bestellpostenID.equals(other.bestellpostenID))
 			return false;
 		if (erstellt == null) {
 			if (other.erstellt != null)
 				return false;
 		}
 		else if (!erstellt.equals(other.erstellt))
 			return false;
 		if (geaendert == null) {
 			if (other.geaendert != null)
 				return false;
 		}
 		else if (!geaendert.equals(other.geaendert))
 			return false;
 		return true;
 	}
 
 }
 
