 package de.shop.kundenverwaltung.domain;
 
 import static de.shop.util.Constants.ERSTE_VERSION;
 import static javax.persistence.EnumType.STRING;
 import static javax.persistence.FetchType.EAGER;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.persistence.Version;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.validator.constraints.Email;
 
 import de.shop.auth.service.jboss.AuthService.RolleType;
 
 
 /**
  * The persistent class for the kunde database table.
  * 
  */
 
 @Entity
 @Table(name = "kunde")
 @NamedQueries({
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL, 
 				query = "select kunde from Kunde as kunde where Kunde.email = :" + Kunde.PARAM_EMAIL),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME, 
 				query = "select kunde from Kunde as kunde where Kunde.nachname = :" + Kunde.PARAM_NACHNAME),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN, 
 				query = "select kunde from Kunde as kunde"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_ORDER_BY_ID, 
 				query = "select kunde from Kunde as kunde order by Kunde_ID"),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_ZAHLUNGSINFORMATION, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.zahlungsinformation WHERE K.kundeid = :" 
 						+ Kunde.PARAM_ID),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_ADRESSE, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.rechnungsadresse " 
 						+ "LEFT JOIN FETCH K.lieferadresse WHERE  K.kundeid = :" + Kunde.PARAM_ID),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.rechnungsadresse "
 						+ "LEFT JOIN FETCH K.lieferadresse LEFT JOIN FETCH K.zahlungsinformation WHERE  K.kundeid = :" 
 						+ Kunde.PARAM_ID),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL_FETCH_ZAHLUNGSINFORMATION, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.zahlungsinformation WHERE K.email = :" 
 						+ Kunde.PARAM_EMAIL),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL_FETCH_ADRESSE, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.rechnungsadresse " 
 						+ "LEFT JOIN FETCH K.lieferadresse WHERE K.email = :" 
 						+ Kunde.PARAM_EMAIL),
 	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.rechnungsadresse " 
 						+ "LEFT JOIN FETCH K.lieferadresse LEFT JOIN FETCH K.zahlungsinformation WHERE  K.email = :" 
 						+ Kunde.PARAM_EMAIL),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_ZAHLUNGSINFORMATION, 
 				query = "SELECT DISTINCT K FROM Kunde K " 
 						+ "LEFT JOIN FETCH K.zahlungsinformation WHERE UPPER(K.nachname) = UPPER(:" 
 						+ Kunde.PARAM_NACHNAME + ")"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_ADRESSE, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.rechnungsadresse " 
 						+ "LEFT JOIN FETCH K.lieferadresse WHERE UPPER(K.nachname) = UPPER(:" 
 						+ Kunde.PARAM_NACHNAME + ")"),
 	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION, 
 				query = "SELECT DISTINCT K FROM Kunde K LEFT JOIN FETCH K.rechnungsadresse " 
 						+ "LEFT JOIN FETCH K.lieferadresse " 
 						+ "LEFT JOIN FETCH K.zahlungsinformation WHERE  UPPER(K.nachname) = UPPER(:" 
 						+ Kunde.PARAM_NACHNAME + ")"),
 	@NamedQuery(name  = Kunde.FIND_IDS_BY_PREFIX,
 				query = "SELECT   k.kundeid FROM  Kunde k WHERE CONCAT('', k.kundeid) LIKE :" 
 						+ Kunde.PARAM_KUNDE_ID_PREFIX + " ORDER BY k.kundeid"),
 	@NamedQuery(name  = Kunde.FIND_NACHNAMEN_BY_PREFIX,
    	            query = "SELECT   DISTINCT k.nachname FROM  Kunde k WHERE UPPER(k.nachname) LIKE UPPER(:" 
    	            		+ Kunde.PARAM_KUNDE_NACHNAME_PREFIX + ")"),
    	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_USERNAME,
    	 	            query = "SELECT   k"
    	 				        + " FROM  Kunde k"
    	 	            		+ " WHERE CONCAT('', k.id) = :" + Kunde.PARAM_KUNDE_USERNAME),
    	@NamedQuery(name  = Kunde.FIND_USERNAME_BY_USERNAME_PREFIX,
    	   	            query = "SELECT   CONCAT('', k.id)"
    	   				        + " FROM  Kunde k"
    	    	            		+ " WHERE CONCAT('', k.id) LIKE :" + Kunde.PARAM_USERNAME_PREFIX)
 }
 	
 	)
 
 public class Kunde implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1867578666521060479L;
 	
 	
 	private static final String PREFIX = "KUNDE.";
 	public static final String FIND_KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";
 	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundeByNachname";
 	public static final String FIND_KUNDEN = PREFIX + "findKunden";
 	public static final String FIND_KUNDEN_ORDER_BY_ID = PREFIX + "findKundeOrderById";
 	public static final String FIND_KUNDEN_FETCH_BESTELLUNGEN = PREFIX + "findKundeFetchBestellungen";
 	
 	public static final String FIND_KUNDE_BY_ID_FETCH_ZAHLUNGSINFORMATION = PREFIX 
 											+ "findKundeByIdFetchZahlungsinformation";
 	
 	public static final String FIND_KUNDE_BY_ID_FETCH_ADRESSE = PREFIX + "findKundeByIdFetchAdresse";
 	
 	public static final String FIND_KUNDE_BY_ID_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION = PREFIX 
 											+ "findKundeByIdFetchAdresseUndZahlungsinformation";
 	
 	public static final String FIND_KUNDE_BY_EMAIL_FETCH_ZAHLUNGSINFORMATION = PREFIX 
 											+ "findKundeByEmailFetchZahlungsinformation";
 	
 	public static final String FIND_KUNDE_BY_EMAIL_FETCH_ADRESSE = PREFIX + "findKundeByEmailFetchAdresse";
 	
 	public static final String FIND_KUNDE_BY_EMAIL_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION = PREFIX 
 											+ "findKundeByEmailFetchAdresseUndZahlungsinformation";
 	
 	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_ZAHLUNGSINFORMATION = PREFIX 
 											+ "findKundeByNachnameFetchZahlungsinformation";
 	
 	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_ADRESSE = PREFIX + "findKundeByNachnameFetchAdresse";
 	
 	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION = PREFIX 
 											+ "findKundeByNachnameFetchAdresseUndZahlungsinformation";
 	
 	public static final String FIND_NACHNAMEN_BY_PREFIX = PREFIX + "findNachnameByPrefix";
 	public static final String FIND_IDS_BY_PREFIX = PREFIX + "findIdsByPrefix";
 	public static final String FIND_KUNDE_BY_USERNAME = PREFIX + "findKundeByUsername";
 	public static final String FIND_USERNAME_BY_USERNAME_PREFIX = PREFIX + "findKundeByUsernamePrefix";
 
 	
 	
 	public static final String PARAM_EMAIL = "email";
 	public static final String PARAM_ID = "id";
 	public static final String PARAM_NACHNAME = "nachname";
 	public static final String PARAM_KUNDE_NACHNAME_PREFIX = "nachnameprefix";
 	public static final String PARAM_KUNDE_ID_PREFIX = "idprefix";
 	public static final String PARAM_KUNDE_USERNAME = "username";
 	public static final String PARAM_USERNAME_PREFIX = "usernamePrefix";
 
 	public static final String FIND_KUNDEN_BY_ID_PREFIX = PREFIX + "findKundenByIdPrefix";
 	
 
 	
 	
 	@Id
 	@NotNull
 	@GeneratedValue
 	@Column(name = "kunde_id", unique = true, updatable = false, nullable = false)
 	private Long kundeid;
 	
 	@Version
 	@Basic(optional = false)
 	private int version = ERSTE_VERSION;
 
 	@NotNull
 	@Size(min = 2, max = 30, message = "{kundenverwaltung.kunde.vorname.length}")
 	@Pattern(regexp = "[A-Z][a-z]+")
 	@Column(length = 30, nullable = false)
 	private String vorname;
 
 	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
 	@Size(min = 2, max = 30, message = "{kundenverwaltung.kunde.nachname.length}")
 	@Pattern(regexp = "[A-Z][a-z]+", message = "{kundenverwaltung.kunde.nachname.pattern}")
 	@Column(length = 30, nullable = false)
 	private String nachname;
 
 	@NotNull(message ="{kundenverwaltung.kunde.email.notNull}")
 	@Email
 	@Size(min = 8, max = 45, message = "{kundenverwaltung.kunde.email.length}")
 	@Column(length = 45, nullable = false, updatable = false)
 	private String email;
 	
 	
 	@Column(length = 1, nullable = false)
 	@Enumerated(STRING)
 	private KundeGeschlechtType geschlecht;
 	
 	@NotNull
 	@Size(min = 6, max = 45)
 	@Column(length = 45, nullable = false)
 	private String passwort;
 	
 	@Transient
 	@JsonIgnore
 	private String passwortWdh;
 	
 	@NotNull
 	@Size(max = 45)
 	@Column(length = 45, nullable = false)
 	private String telefonnummer;
 
 	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
 	@OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.REMOVE })
 	@JoinColumn(name = "lieferadresse")
 	private Adresse lieferadresse;
 
 	@NotNull
 	@OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.REMOVE })
 	@JoinColumn(name = "rechnungsadresse")
 	private Adresse rechnungsadresse;
 
 	@NotNull
 	@OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.REMOVE })
 	@JoinColumn(name = "zahlungsinformation_ID")
 	private Zahlungsinformation zahlungsinformation;
 	
 	@NotNull
 	@Past
 	@Temporal(TemporalType.DATE)
 	@Column(nullable = false)
 	private Date geburtsdatum;
 	
 	@JsonIgnore
 	@NotNull
 	@Column(nullable = false)
 	private Timestamp aktualisiert;
 
 	@JsonIgnore
 	@NotNull
 	@Past(message = "{kundenverwaltung.kunde.erzeugt.past}")
 	@Column(nullable = false)
 	private Timestamp erzeugt;
 	
 	
 	@ElementCollection(fetch = EAGER)
 	@CollectionTable(name = "kunde_rolle",
 	                 joinColumns = @JoinColumn(name = "kunde_fk", nullable = false),
 	                 uniqueConstraints =  @UniqueConstraint(columnNames = { "kunde_fk", "rolle_fk" }))
 	@Column(table = "kunde_rolle", name = "rolle_fk", nullable = false)
 	private Set<RolleType> rollen;
 	
 	@Transient
 	private URI bestellungenUri;
 
 
 	@PrePersist
 	private void prePersist() {
 		erzeugt =  new Timestamp(new Date().getTime());
 		aktualisiert =  new Timestamp(new Date().getTime());
 		
 	}
 
 	@PreUpdate
 	private void preUpdate() {
 		aktualisiert =  new Timestamp(new Date().getTime());
 	}
 	
 	@PostLoad
 	protected void postLoad() {
 		passwortWdh = passwort;
 	}
 	
 	
 	
 	
 	public Long getKundeId() {
 		
 		
 		return this.kundeid;
 	}
 
 	public void setKundeId(Long kundeId) {
 		
 		
 		
 		this.kundeid = kundeId;
 	}
 
 	public Timestamp getAktualisiert() {
 		return aktualisiert == null ? null : (Timestamp) aktualisiert.clone();
 	}
 
 
 	public String getEmail() {
 		return this.email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public Timestamp getErzeugt() {
 		return erzeugt == null ? null : (Timestamp) erzeugt.clone();
 	}
 	
 	public void setErzeugt(Date erzeugt) {
 		this.erzeugt = erzeugt == null ? null : (Timestamp) erzeugt.clone();
 	}
 
 	public Date getGeburtsdatum() {
 		return new Date(geburtsdatum.getDate());
 	}
 
 	@SuppressWarnings("deprecation")
 	public void setGeburtsdatum(Date geburtsdatum) {
 		this.geburtsdatum = new Date(geburtsdatum.getDate());
 	}
 
 	public KundeGeschlechtType getGeschlecht() {
 		return this.geschlecht;
 	}
 
 	public void setGeschlecht(KundeGeschlechtType geschlecht) {
 		this.geschlecht = geschlecht;
 	}
 
 	public Adresse getLieferadresse() {
 		return this.lieferadresse;
 	}
 
 	public void setLieferadresse(Adresse lieferadresse) {
 		this.lieferadresse = lieferadresse;
 	}
 
 	public String getNachname() {
 		return this.nachname;
 	}
 
 	public void setNachname(String nachname) {
 		this.nachname = nachname;
 	}
 
 	public String getPasswort() {
 		return this.passwort;
 	}
 
 	public void setPasswort(String passwort) {
 		this.passwort = passwort;
 	}
 	
 	public String getPasswortWdh() {
 		return passwortWdh;
 	}
 	
 	public void setPasswortWdh(String passwordWdh) {
 		this.passwortWdh = passwordWdh;
 	}
 
 	public Adresse getRechnungsadresse() {
 		return this.rechnungsadresse;
 	}
 
 	public void setRechnungsadresse(Adresse rechnungsadresse) {
 		this.rechnungsadresse = rechnungsadresse;
 	}
 
 	public String getTelefonnummer() {
 		return this.telefonnummer;
 	}
 
 	public void setTelefonnummer(String telefonnummer) {
 		this.telefonnummer = telefonnummer;
 	}
 
 	public String getVorname() {
 		return this.vorname;
 	}
 
 	public void setVorname(String vorname) {
 		this.vorname = vorname;
 	}
 
 	public Zahlungsinformation getZahlungsinformation() {
 		return this.zahlungsinformation;
 	}
 
 	public void setZahlungsinformation(Zahlungsinformation zahlungsinformation) {
 		this.zahlungsinformation = zahlungsinformation;
 	}
 	
 	public URI getBestellungenUri() {
 		return bestellungenUri;
 	}
 
 
 
 	public void setBestellungenUri(URI bestellungenUri) {
 		this.bestellungenUri = bestellungenUri;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((email == null) ? 0 : email.hashCode());
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
 		Kunde other = (Kunde) obj;
 		if (email == null) {
 			if (other.email != null)
 				return false;
 		} 
 		else if (!email.equals(other.email))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Kunde [kundeId=" + kundeid + ", aktualisiert=" + aktualisiert
 				+ ", email=" + email + ", erzeugt=" + erzeugt
 				+ ", geburtsdatum=" + geburtsdatum + ", geschlecht="
 				+ geschlecht
 				+ ", nachname=" + nachname + ", passwort=" + passwort
 				+ ",passwortWdh=" + passwortWdh
 				+ ", telefonnummer="
 				+ telefonnummer + ", vorname=" + vorname + "]";
 	}
 
 
 
 	public void setValues(Kunde kunde) {
 			this.kundeid = kunde.kundeid;
 			this.vorname = kunde.vorname;
 			this.nachname = kunde.nachname;
 			this.email = kunde.email;
 			this.geschlecht = kunde.geschlecht;
 			this.passwort = kunde.passwort;
 			this.passwortWdh = kunde.passwortWdh;
 			this.telefonnummer = kunde.telefonnummer;
 			this.lieferadresse.setValues(kunde.lieferadresse);
 			this.rechnungsadresse.setValues(kunde.rechnungsadresse);
 			this.zahlungsinformation.setValues(kunde.zahlungsinformation);
 			this.geburtsdatum = kunde.geburtsdatum;
 			this.bestellungenUri = kunde.bestellungenUri;	
 			this.version = kunde.version;
 	}
 
 	public Set<RolleType> getRollen() {
 		return rollen;
 	}
 
 	public void setRollen(Set<RolleType> rollen) {
 		this.rollen = rollen;
 	}
 
 	public int getVersion() {
 		return version;
 	}
 
 	public void setVersion(int version) {
 		this.version = version;
 	}
 	
 	@Override
 	public Object clone() throws CloneNotSupportedException {
 		final Kunde neuesObjekt = (Kunde) super.clone();
 		neuesObjekt.kundeid = kundeid;
 		neuesObjekt.version = version;
 		neuesObjekt.nachname = nachname;
 		neuesObjekt.vorname = vorname;
 		//neuesObjekt.kategorie = kategorie;
 		//neuesObjekt.umsatz = umsatz;
 		neuesObjekt.email = email;
 		//neuesObjekt.newsletter = newsletter;
 		neuesObjekt.passwort = passwort;
 		neuesObjekt.passwortWdh = passwortWdh;
 		//neuesObjekt.agbAkzeptiert = agbAkzeptiert;
 		neuesObjekt.lieferadresse = lieferadresse;
 		neuesObjekt.rechnungsadresse = rechnungsadresse;
 		//neuesObjekt.bemerkungen = bemerkungen;
 		neuesObjekt.erzeugt = erzeugt;
 		neuesObjekt.aktualisiert = aktualisiert;
 		return neuesObjekt;
 	}
 	 
 }
