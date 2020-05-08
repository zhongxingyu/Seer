 package de.shop.Artikelverwaltung.domain;
 
 import static de.shop.Util.Constants.KEINE_ID;
 import static de.shop.Util.Constants.LONG_ANZ_ZIFFERN;
 import static de.shop.Util.Constants.MIN_ID;
 import static javax.persistence.TemporalType.TIMESTAMP;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
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
 import javax.persistence.Lob;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.Transient;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import de.shop.Util.IdGroup;
 
 /**
  * The persistent class for the artikel database table.
  * 
  */
 @Entity
 @Table(name = "Artikel")
 @NamedQueries({
 	  @NamedQuery(name = Artikel.FIND_ARTIKEL_BY_BEZEICHNUNG,
 			      query = "FROM Artikel a WHERE a.bezeichnung = :" + Artikel.PARAM_BEZEICHNUNG),	
 	  @NamedQuery(name = Artikel.FIND_ARTIKEL_BY_ARTIKEL_ID,
 			  	  query = "FROM Artikel a WHERE a.id = :" + Artikel.PARAM_ID),
 	  @NamedQuery(name = Artikel.FIND_ARTIKEL_BY_ID_LAGERPOSITIONEN,
 				  query = "SELECT DISTINCT la "
 						+ "FROM Artikel la " 
 						+ "LEFT JOIN la.lagerpositionen " 
 						+ "WHERE la.id = :" + Lagerposition.PARAM_ID),
 	  @NamedQuery(name = Artikel.FIND_ARTIKEL_ALL,
 			      query = "SELECT la FROM Artikel la"),
 	  @NamedQuery(name = Artikel.FIND_ARTIKEL_ALL_LAGERPOSITIONEN,
 			      query = "SELECT a FROM Artikel a JOIN a.lagerpositionen"),
 	  @NamedQuery(name = Artikel.FIND_ARTIKEL_BY_ARTIKEL_IDS,
 	  			 query = "FROM Artikel a WHERE a.id IN (:" + Artikel.PARAM_ID + ")"),
   	  @NamedQuery(name = Artikel.FIND_ARTIKEL_BY_IDS_LAGERPOSITIONEN,
   	  		     query = "SELECT DISTINCT la " 
   	  		    		 +	"FROM Artikel la "
   	  		    		 +	"LEFT JOIN la.lagerpositionen " 
   	  		    		 +	"WHERE la.id IN (:" + Artikel.PARAM_ID + ")")
 	})
 
 @XmlRootElement
 public class Artikel implements Serializable {
 
 	private static final String PREFIX = "Artikel.";
 
 	public static final String FIND_ARTIKEL_BY_BEZEICHNUNG =
 		PREFIX + "findArtikelByBezeichnung";
 	public static final String FIND_ARTIKEL_BY_ARTIKEL_ID =
 		PREFIX + "findArtikelByArtikelid";
 	public static final String FIND_ARTIKEL_BY_ID_LAGERPOSITIONEN =
 		PREFIX + "findArtikelByIdLagerposition";
 	public static final String FIND_ARTIKEL_ALL =
 		PREFIX + "findArtikelAll";
 	public static final String FIND_ARTIKEL_ALL_LAGERPOSITIONEN = 
 		PREFIX + "findArtikellAllLagerpositionen";
 	public static final String FIND_ARTIKEL_BY_ARTIKEL_IDS =
 		PREFIX + "findArtikelByIDs";
 	public static final String FIND_ARTIKEL_BY_IDS_LAGERPOSITIONEN =
 		PREFIX + "findArtikelByIDsLagerpositionen";
 	
 	public static final String PARAM_ID = "id";
 	public static final String PARAM_BEZEICHNUNG = "bezeichnung";
 
 	private static final long serialVersionUID = 4651646021686650992L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "artikel_ID", nullable = false, updatable = false, precision = LONG_ANZ_ZIFFERN)
 	@Min(value = MIN_ID, message = "artikelverwaltung.artikel.id.min", groups = IdGroup.class)
 	@XmlAttribute
 	private Long id = KEINE_ID;
 
 
 	@OneToMany(mappedBy = "artikel")
 	@XmlTransient
 	private List<Lagerposition> lagerpositionen;
 	
 	@Transient
 	@XmlElement(name = "lagerpositionen")
 	private URI lagerpositionenUri; 
 
 	@Lob
 	@XmlElement
 	private String beschreibung;
 
 	@NotNull(message = "artikelverwaltung.artikel.bezeichnung.notNull")
 	@XmlElement
 	private String bezeichnung;
 
 	@XmlElement
 	private String bild;
 
 	@Column(name = "erstellt_am")
 	@Temporal(TIMESTAMP)
 	@XmlElement
 	private Date erstelltAm;
 	
 
 	@Column(name = "geaendert_am")
 	@Temporal(TIMESTAMP)
 	@XmlElement
 	private Date geaendertAm;
 
 	@NotNull(message = "artikelverwaltung.artikel.preis.notNull")
 	@XmlElement
 	private BigDecimal preis;
 
 	public Artikel() {
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getBeschreibung() {
 		return this.beschreibung;
 	}
 
 	public void setBeschreibung(String beschreibung) {
 		this.beschreibung = beschreibung;
 	}
 	
 	public URI getLagerpositionenUri() {
 		return lagerpositionenUri;
 	}
 
 	public void setLagerpositionenUri(URI lagerpositionenUri) {
 		this.lagerpositionenUri = lagerpositionenUri;
 	}
 
 	public String getBezeichnung() {
 		return this.bezeichnung;
 	}
 
 	public void setBezeichnung(String bezeichung) {
 		this.bezeichnung = bezeichung;
 	}
 
 	public String getBild() {
 		return this.bild;
 	}
 
 	public void setBild(String bild) {
 		this.bild = bild;
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
 
 	public BigDecimal getPreis() {
 		return this.preis;
 	}
 
 	public void setPreis(BigDecimal preis) {
 		this.preis = preis;
 	}
 	
 	public Artikel addLagerposition(Lagerposition lagerposition) {
 		if (lagerpositionen == null) {
 			lagerpositionen = new ArrayList<>();
 		}
 		
 		lagerpositionen.add(lagerposition);
 		return this;
 	}
 	
 	public List<Lagerposition> getLagerposition() {
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
 	
 	public void setValues(Artikel a) {
 		bezeichnung = a.bezeichnung;
 		beschreibung = a.beschreibung;
 		bild = a.bild;
 		preis = a.preis;
 		lagerpositionen = a.lagerpositionen;
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
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result
 				+ ((beschreibung == null) ? 0 : beschreibung.hashCode());
 		result = prime * result
 				+ ((bezeichnung == null) ? 0 : bezeichnung.hashCode());
 		result = prime * result + ((bild == null) ? 0 : bild.hashCode());
 		result = prime * result
 				+ ((erstelltAm == null) ? 0 : erstelltAm.hashCode());
 		result = prime * result
 				+ ((geaendertAm == null) ? 0 : geaendertAm.hashCode());
 		result = prime * result + ((preis == null) ? 0 : preis.hashCode());
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
 		Artikel other = (Artikel) obj;
		if (id.equals(other.id))
 			return false;
 		if (beschreibung == null) {
 			if (other.beschreibung != null)
 				return false;
 		} 
 		else if (!beschreibung.equals(other.beschreibung))
 			return false;
 		if (bezeichnung == null) {
 			if (other.bezeichnung != null)
 				return false;
 		} 
 		else if (!bezeichnung.equals(other.bezeichnung))
 			return false;
 		if (bild == null) {
 			if (other.bild != null)
 				return false;
 		} 
 		else if (!bild.equals(other.bild))
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
 		if (preis == null) {
 			if (other.preis != null)
 				return false;
 		} 
 		else if (!preis.equals(other.preis))
 			return false;
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Artikel [artikel_ID=" + id + ", beschreibung=" + beschreibung
 				+ ", bezeichung=" + bezeichnung + ", bild=" + bild
 				+ ", erstelltAm=" + erstelltAm + ", geaendertAm=" + geaendertAm
 				+ ", preis=" + preis + "]";
 	}
 
 	
 }
