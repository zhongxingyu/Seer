 package de.shop.bestellverwaltung.domain;
 
 import static de.shop.util.Constants.ERSTE_VERSION;
 import static javax.persistence.EnumType.STRING;
 import static javax.persistence.TemporalType.TIMESTAMP;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.net.URI;
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.PostPersist;
 import javax.persistence.PostUpdate;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.jboss.logging.Logger;
 
 import de.shop.artikelverwaltung.domain.Artikel;
 import de.shop.util.TechnicalDate;
 
 
 /**
  * The persistent class for the bestellposition database table.
  * 
  */
 
 @Entity
 @Table (name = "bestellposition")
 @NamedQueries({
 	@NamedQuery(name = Bestellposition.FIND_BESTELLPOSITIONEN_BY_ARTIKEL,
             query = "SELECT bp"
 			        + " FROM   Bestellposition bp"
             		+ " WHERE  bp.artikel.id = :" + Bestellposition.PARAM_BESTELLPOSITION_ARTIKEL)
 })
 @Cacheable
 public class Bestellposition implements Serializable {
 	
 	private static final String PREFIX = "Bestellposition.";
 	public static final String FIND_BESTELLPOSITIONEN_BY_ARTIKEL = PREFIX + "findBestellpositionenByArtikel";
 	
 	public static final String PARAM_BESTELLPOSITION_ARTIKEL = "artikel";
 	
 	private static final long serialVersionUID = 9164003138551866949L;
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 	
 	private static final int ANZAHL_MIN = 1;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "bestellpos_id", unique = true, nullable = false, updatable = false)
 	@NotNull(groups = TechnicalDate.class)
 	private Long id;
 
 	@Version
 	@Basic(optional = false)
 	private int version = ERSTE_VERSION;
 	
 	@Column(nullable = false)
 	@Min(value = ANZAHL_MIN, message = "{bestellverwaltung.bestellposition.anzahl.min}")
 	private int bestellmenge;
 
 	@Column(name = "status")
 	@Enumerated(STRING)
 	private BestellpositionstatusType status;
 	
 	@Column(nullable = false)
 	@NotNull(message = "{bestellverwaltung.bestellposition.erzeugt.notNull}", groups = TechnicalDate.class)
 	@Temporal(TIMESTAMP)
 	private Date erzeugt;
 	
 	@Column(nullable = false)
 	@NotNull(message = "{bestellverwaltung.bestellposition.aktualisiert.notNull}", groups = TechnicalDate.class)
 	@Temporal(TIMESTAMP)
 	private Date aktualisiert;
 
 	@ManyToOne(optional = false)
 	@JoinColumn(name = "artikel_id", nullable = false)
 	@NotNull(message = "{bestellverwaltung.bestellposition.artikel.notNull}")
 	@JsonIgnore
 	private Artikel artikel;
 
 	@Transient
 	private URI artikelUri;
 	
 	
 	public Bestellposition() {
 		super();
 	}
 	
 	public Bestellposition(Artikel artikel) {
 		super();
 		this.artikel = artikel;
 		this.bestellmenge = 1;
 	}
 	
 	public Bestellposition(Artikel artikel, int bestellmenge, BestellpositionstatusType status) {
 		super();
 		this.artikel = artikel;
 		this.bestellmenge = bestellmenge;
 		this.status = status;
 	}
 	
 	@PrePersist
 	private void prePersist() {
 		erzeugt = new Date();
 		aktualisiert = new Date();
 	}
 	
 	@PostPersist
 	private void postPersist() {
 		LOGGER.debugf("Neue Bestellposition mit ID=%d", id);
 	}
 	
 
 	
 	@PreUpdate
 	private void preUpdate() {
 		aktualisiert = new Date();
 	}
 	
 	@PostUpdate
 	private void postUpdate() {
 		LOGGER.debugf("Bestellposition mit ID=%s aktualisiert: version=%d", id, version);
 	}
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 
 	public Artikel getArtikel() {
 		return this.artikel;
 	}
 
 	public void setArtikel(Artikel artikel) {
 		this.artikel = artikel;
 	}
 	
 	public URI getArtikelUri() {
 		return artikelUri;
 	}
 	
 	public void setArtikelUri(URI artikelUri) {
 		this.artikelUri = artikelUri;
 	}
 
 	public int getBestellmenge() {
 		return this.bestellmenge;
 	}
 
 	public void setBestellmenge(int bestellmenge) {
 		this.bestellmenge = bestellmenge;
 	}
 	
 	public BestellpositionstatusType getStatus() {
 		return this.status;
 	}
 
 	public void setStatus(BestellpositionstatusType status) {
 		this.status = status;
 	}
 	
 	public Date getAktualisiert() {
 		return this.aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 
 	public void setAktualisiert(Date aktualisiert) {
 		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 	
 	public Date getErzeugt() {
 		return aktualisiert == null ? null : (Date) aktualisiert.clone();
 	}
 	
 	public void setErzeugt(Date erzeugt) {
 		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
 	}
 
 	
 	@Override
 	public String toString() {
 		return "Bestellposition [id=" + id
 				+ ", aktualisiert=" + aktualisiert
 				+ ", bestellmenge=" + bestellmenge
 				+ ", erzeugt=" + erzeugt + ", status="
 				+ status + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 	
 		result = prime * result
 				+ ((artikel == null) ? 0 : artikel.hashCode());
 		result = prime * result;
 		result = prime * result
 				+ bestellmenge;
 		result = prime * result
 				+ ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
 		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
 		final Bestellposition other = (Bestellposition) obj;
 		if (aktualisiert == null) {
 			if (other.aktualisiert != null)
 				return false;
 		} 
 		else if (!aktualisiert.equals(other.aktualisiert))
 			return false;
 		if (artikel == null) {
 			if (other.artikel != null)
 				return false;
 		} 
 		else if (!artikel.equals(other.artikel))
 			return false;
 		if (bestellmenge != other.bestellmenge) {
 			return false;
 		}	
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} 
 		else if (!id.equals(other.id))
 			return false;
 		if (erzeugt == null) {
 			if (other.erzeugt != null)
 				return false;
 		} 
 		else if (!erzeugt.equals(other.erzeugt))
 			return false;
 		if (status == null) {
 			if (other.status != null)
 				return false;
 		} 
 		else if (!status.equals(other.status))
 			return false;
 		return true;
 	}
 }

