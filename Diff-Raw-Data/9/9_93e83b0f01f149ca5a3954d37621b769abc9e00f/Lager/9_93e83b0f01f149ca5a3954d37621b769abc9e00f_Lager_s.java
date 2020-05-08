 package de.shop.Artikelverwaltung.domain;
 
 import static de.shop.Util.Constants.KEINE_ID;
 import static de.shop.Util.Constants.LONG_ANZ_ZIFFERN;
 import static de.shop.Util.Constants.MIN_ID;
 import static javax.persistence.TemporalType.TIMESTAMP;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.Transient;
 import javax.validation.constraints.Min;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.hibernate.validator.constraints.NotBlank;
 
 import de.shop.Util.IdGroup;
 
 /**
  * The persistent class for the lager database table.
  * 
  */
 @Entity
 @Table(name = "Lager")
    	@NamedQueries({
    		@NamedQuery(name = Lager.FIND_LAGER_BY_ID, 
    					query = "SELECT a "
    					+ "FROM Lager a "
 					+ "WHERE a.id = :" + Lager.PARAM_ID),
 		@NamedQuery(name = Lager.FIND_LAGER_BY_BEZEICHNUNG, 
 					query = "SELECT a " 
 					+ "FROM Lager a "
 					+ "WHERE a.bezeichnung = :" + Lager.PARAM_BEZEICHNUNG),
 		@NamedQuery(name = Lager.FIND_LAGER_ALL,
 					query = "SELECT la FROM Lager la"),
 		@NamedQuery(name = Lager.FIND_LAGER_ALL_LAGERPOSITIONEN,
 		    		query = "SELECT a FROM Lager a JOIN a.lagerpositionen"),
    })
 
 @XmlRootElement
 public class Lager implements Serializable {	
 	
 	private static final String PREFIX = "Lager.";
 	
 	public static final String FIND_LAGER_ALL_LAGERPOSITIONEN =
 		PREFIX + "findLagerAllLagerpositionen";
 	public static final String FIND_LAGER_ALL =
 		PREFIX + "findLagerAll";
 	public static final String FIND_LAGER_BY_BEZEICHNUNG =
 		PREFIX + "findLagerByBezeichnung";
 	public static final String FIND_LAGER_BY_ID =
 		PREFIX + "findLagerByArtikelid";
 	public static final String PARAM_ID = "id";
 	public static final String PARAM_BEZEICHNUNG = "bezeichnung";
 
 	private static final long serialVersionUID = -2184864191060846895L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "lager_ID", nullable = false, updatable = false, precision = LONG_ANZ_ZIFFERN)
 	@Min(value = MIN_ID, message = "artikelverwaltung.lager.id.min", groups = IdGroup.class)
 	@XmlAttribute
 	private Long id = KEINE_ID;
 
 	@OneToMany(mappedBy = "lager")
 	@XmlTransient
 	private List<Lagerposition> lagerpositionen;
 	
 	@Transient
 	@XmlElement(name = "lagerpositionen")
 	private URI lagerpositionenUri;
 
 	@NotBlank(message = "artikelverwaltung.lager.bezeichnung.notBlank")
 	@XmlElement
 	private String bezeichnung;
 
 	@Column(name = "erstellt_am")
 	@Temporal(TIMESTAMP)
 	@XmlElement
 	private Date erstelltAm;
 
 	@Column(name = "geaendert_am")
 	@Temporal(TIMESTAMP)
 	@XmlElement
 	private Date geaendertAm;
 
 	public Lager() {
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getBezeichnung() {
 		return this.bezeichnung;
 	}
 
 	public void setBezeichnung(String bezeichung) {
 		this.bezeichnung = bezeichung;
 	}
 
 	public Date getErstelltAm() {
 		return erstelltAm == null ? null : (Date) erstelltAm.clone();
 	}
 
 	public void setErstelltAm(Date erstelltAm) {
 		this.erstelltAm = erstelltAm == null ? null : (Date) erstelltAm.clone();
 	}
 
 	public Date getGeaendertAm() {
 		return geaendertAm == null ? null : (Date) geaendertAm.clone();
 	}
 
 	public void setGeaendertAm(Date geaendertAm) {
 		this.geaendertAm = geaendertAm == null ? null : (Date) geaendertAm.clone();
 	}
 	
 	public List<Lagerposition> getLagerpositionen() {
 		return Collections.unmodifiableList(lagerpositionen);
 	}
 	
 	public void setLagerpositionen(List<Lagerposition> lagerpositionen) {
 		if (this.lagerpositionen == null) {
 			this.lagerpositionen = lagerpositionen;
 			return;
 		}
 	
 	this.lagerpositionen.clear();
 	if (lagerpositionen != null) {
 		this.lagerpositionen.addAll(lagerpositionen);
 	}
 	}
 	
 	public Lager addLagerpositionen(Lagerposition lagerposition) {
 		if (lagerpositionen == null) {
 			lagerpositionen = new ArrayList<>();
 		}
 		lagerpositionen.add(lagerposition);
 		return this;
 	}
 	
 	
 	public URI getLagerpositionenUri() {
 		return lagerpositionenUri;
 	}
 
 	public void setLagerpositionenUri(URI lagerpositionenUri) {
 		this.lagerpositionenUri = lagerpositionenUri;
 	}
 	
 	public void setValues(Lager l) {
 		bezeichnung = l.bezeichnung;
 		lagerpositionen = l.lagerpositionen;
 	}
 
 	@PrePersist
 	private void prePersist() {
 		erstelltAm = new Date();
 		geaendertAm = new Date();
 	}
 	
 	@PreUpdate
 	private void preUpdate() {
 		geaendertAm = new Date();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((bezeichnung == null) ? 0 : bezeichnung.hashCode());
 		result = prime * result
 				+ ((erstelltAm == null) ? 0 : erstelltAm.hashCode());
 		result = prime * result
 				+ ((geaendertAm == null) ? 0 : geaendertAm.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Lager other = (Lager) obj;
 		if (bezeichnung == null) {
 			if (other.bezeichnung != null)
 				return false;
 		} 
 		else if (!bezeichnung.equals(other.bezeichnung))
 			return false;
 		if (erstelltAm == null) {
 			if (other.erstelltAm != null)
 				return false;
 		} 
 		else if (!erstelltAm.equals(other.erstelltAm))
 			return false;
 		if (geaendertAm == null) {
 			if (other.geaendertAm != null)
 				return false;
 		} 
 		else if (!geaendertAm.equals(other.geaendertAm))
 			return false;
		if (id != other.id)
 			return false;
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Lager [lager_ID=" + id + ", bezeichung=" + bezeichnung
 				+ ", erstelltAm=" + erstelltAm + ", geaendertAm=" + geaendertAm
 				+ "]";
 	}
 
 	
 	
 	
 	
 	
 
 }
