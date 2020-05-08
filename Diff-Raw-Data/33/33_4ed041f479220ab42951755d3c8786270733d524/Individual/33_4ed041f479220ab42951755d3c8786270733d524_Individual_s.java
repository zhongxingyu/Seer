 package fr.cg95.cvq.business.users;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import net.sf.oval.constraint.AssertValid;
 import net.sf.oval.constraint.Future;
 import net.sf.oval.constraint.NotEmpty;
 import net.sf.oval.constraint.NotNull;
 import net.sf.oval.constraint.Past;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.text.WordUtils;
 
 import fr.cg95.cvq.business.QoS;
 import fr.cg95.cvq.xml.common.BirthPlaceType;
 import fr.cg95.cvq.xml.common.IndividualRoleType;
 import fr.cg95.cvq.xml.common.IndividualType;
 
 /**
  * @author bor@zenexity.fr
  */
 @Entity
 @Inheritance(strategy=InheritanceType.JOINED)
 @Table(name="individual")
 public abstract class Individual implements Serializable {
 
     // Search fields used in DAO and Service Layer
 
     public static final String SEARCH_BY_FIRSTNAME = "firstName";
     public static final String SEARCH_BY_LASTNAME = "lastName";
     public static final String SEARCH_BY_STREET_NAME = "streetName";
     public static final String SEARCH_BY_STREET_NUMBER = "streetNumber";
     public static final String SEARCH_BY_BIRTHDATE = "birthDate";
     public static final String SEARCH_BY_INDIVIDUAL_ID = "individualId";
     public static final String SEARCH_BY_USER_STATE = "userState";
     public static final String SEARCH_BY_HOME_FOLDER_ID = "homeFolderId";
     public static final String SEARCH_BY_HOME_FOLDER_STATE = "homeFolderState";
     public static final String SEARCH_IS_HOME_FOLDER_RESPONSIBLE = "isHomeFolderResponsible";
     public static final String SEARCH_IS_TEMPORARY = "isTemporary";
     public static final String SEARCH_IS_DUPLICATE_ALERT = "isDuplicateAlert";
     
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
     
     /**
      * the external identifier that is dynamically set for each external service
      * that provide us this information. It is not persisted.
      */
     @Transient
     private String externalId;
     
     /**
      * the external CapDemat identifier that is dynamically set before
      * talking to an external service.
      */
     @Transient
     private String externalCapDematId;
 
     /** Liberty Alliance federation key */
     @Column(name="federation_key",length=64,unique=true)
     private String federationKey;
 
     @NotNull(message = "lastName")
     @NotEmpty(message = "lastName")
     @Column(name="last_name",length=38)
     private String lastName;
 
     @NotNull(message = "firstName", when = "groovy:(_this instanceof fr.cg95.cvq.business.users.Child && _this.born) || _this instanceof fr.cg95.cvq.business.users.Adult")
     @NotEmpty(message = "firstName", when = "groovy:(_this instanceof fr.cg95.cvq.business.users.Child && _this.born) || _this instanceof fr.cg95.cvq.business.users.Adult")
     @Column(name="first_name", length=38)
     private String firstName;
 
     @NotEmpty(message = "firstName2")
     @Column(name="first_name_2",length=38)
     private String firstName2;
 
     @NotEmpty(message = "firstName3")
     @Column(name="first_name_3",length=38)
     private String firstName3;
 
     @NotNull(message = "birthDate", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Child")
     @Past(message = "birthDate", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Child && _this.born")
     @Future(message = "birthDate", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Child && !_this.born")
     @Column(name="birth_date")
     private Date birthDate;
 
     @Column(name="birth_country")
     private String birthCountry;
 
     @Column(name="birth_city",length=32)
     private String birthCity;
 
     @Column(name="birth_postal_code",length=5)
     private String birthPostalCode;
 
     @Column(name="creation_date")
     private Date creationDate;
 
     @Enumerated(EnumType.STRING)
     @Column(length=16,nullable=false)
     private UserState state;
 
     @Column(name="last_modification_date")
     private Date lastModificationDate;
 
     @Enumerated(EnumType.STRING)
     @Column(name="q_o_s",length=16)
     private QoS qoS;
 
     @NotNull(message = "address", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Adult")
     @AssertValid(message = "address", when = "groovy:_this instanceof fr.cg95.cvq.business.users.Adult")
     @ManyToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
     @JoinColumn(name="address_id")
     private Address address;
 
     @ManyToOne
     @JoinColumn(name="home_folder_id")
     private HomeFolder homeFolder;
 
     @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
     @OrderBy("id asc")
     @JoinColumn(name="owner_id")
     private Set<IndividualRole> individualRoles;
 
     @Column(name="duplicate_alert")
     private boolean duplicateAlert;
     
     @Column(name="duplicate_data", columnDefinition="TEXT")
     private String duplicateData;
 
     public Individual() {
         individualRoles = new HashSet<IndividualRole>();
     }
 
     protected void fillCommonXmlInfo(IndividualType individualType) {
 
         Calendar calendar = Calendar.getInstance();
 
         if (this.id != null)
             individualType.setId(this.id.longValue());
         individualType.setLastName(this.lastName);
         individualType.setFirstName(this.firstName);
         if (this.firstName2 != null)
             individualType.setFirstName2(this.firstName2);
         if (this.firstName3 != null)
             individualType.setFirstName3(this.firstName3);
         if (this.birthDate != null) {
             calendar.setTime(birthDate);
             individualType.setBirthDate(calendar);
         }
 
         if (this.birthCity != null || this.birthPostalCode != null) {
             BirthPlaceType birthPlaceType = individualType.addNewBirthPlace();
             birthPlaceType.setCity(this.birthCity);
             birthPlaceType.setPostalCode(this.birthPostalCode);
         }
         if (this.creationDate != null) {
             calendar.setTime(this.creationDate);
             individualType.setCreationDate(calendar);
         }
         if (this.address != null)
             individualType.setAddress(Address.modelToXml(this.address));
         if (this.state != null)
             individualType.setState(fr.cg95.cvq.xml.common.UserStateType.Enum.forString(this.state.toString()));
         IndividualRoleType roles[] = new IndividualRoleType[individualRoles.size()];
         int i = 0;
         for (IndividualRole individualRole : individualRoles) {
             roles[i++] = IndividualRole.modelToXml(individualRole);
         }
         individualType.setRoleArray(roles);
     }
 
     protected void fillCommonModelInfo(IndividualType individualType) {
 
         if (individualType.getId() == 0)
             setId(null);
         else
             setId(new Long(individualType.getId()));
         setLastName(individualType.getLastName());
         setFirstName(individualType.getFirstName());
         setFirstName2(individualType.getFirstName2());
         setFirstName3(individualType.getFirstName3());
         if (individualType.getBirthDate() != null)
             setBirthDate(individualType.getBirthDate().getTime());
         if (individualType.getBirthPlace() != null) {
             setBirthCountry(individualType.getBirthPlace().getCity());
             setBirthPostalCode(individualType.getBirthPlace().getPostalCode());
         }
         if (individualType.getCreationDate() != null) {
             setCreationDate(individualType.getCreationDate().getTime());
         }
         if (individualType.getState() != null)
             setState(UserState.forString(individualType.getState().toString()));
         setAddress(Address.xmlToModel(individualType.getAddress()));
         Set<IndividualRole> roles = new HashSet<IndividualRole>();
         for (IndividualRoleType roleType : individualType.getRoleArray()) {
             IndividualRole role = new IndividualRole();
             if (roleType.getHomeFolderId() != 0)
                 role.setHomeFolderId(roleType.getHomeFolderId());
             if (roleType.getIndividualId() != 0)
                 role.setIndividualId(roleType.getIndividualId());
             role.setRole(RoleType.forString(roleType.getRoleName().toString()));
             role.setIndividualName(roleType.getIndividualName());
             roles.add(role);
         }
         setIndividualRoles(roles);
     }
 
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getExternalId() {
         return externalId;
     }
 
     public void setExternalId(String externalId) {
         this.externalId = externalId;
     }
 
     public String getExternalCapDematId() {
         return externalCapDematId;
     }
 
     public void setExternalCapDematId(String externalCapDematId) {
         this.externalCapDematId = externalCapDematId;
     }
 
     public String getFederationKey() {
         return this.federationKey;
     }
 
     public void setFederationKey(String federationKey) {
         this.federationKey = federationKey;
     }
 
     public String getLastName() {
         return this.lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = StringUtils.upperCase(lastName);
     }
 
     public String getFirstName() {
         return this.firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = WordUtils.capitalizeFully(firstName, new char[]{' ','-','\''});
     }
 
     public String getFirstName2() {
         return this.firstName2;
     }
 
     public void setFirstName2(String firstName2) {
         this.firstName2 = firstName2;
     }
 
     public String getFirstName3() {
         return this.firstName3;
     }
 
     public void setFirstName3(String firstName3) {
         this.firstName3 = firstName3;
     }
 
     public Date getBirthDate() {
         return this.birthDate;
     }
 
     public void setBirthDate(Date birthDate) {
         this.birthDate = birthDate;
     }
 
     public void setBirthDate(String birthDate) {
         if (birthDate == null || birthDate.equals("")) {
             this.birthDate = null;
         } else {
             DateFormat df = DateFormat.getDateInstance();
             try {
                 this.birthDate = df.parse(birthDate);
             } catch (java.text.ParseException pe) {
                 // hmm, worrying isn't it ?
             }
         }
     }
 
     public String getBirthCountry() {
         return this.birthCountry;
     }
 
     public void setBirthCountry(String birthCountry) {
         this.birthCountry = birthCountry;
     }
 
     public String getBirthCity() {
         return this.birthCity;
     }
 
     public void setBirthCity(String birthCity) {
         this.birthCity = StringUtils.upperCase(birthCity);
     }
 
     public String getBirthPostalCode() {
         return this.birthPostalCode;
     }
 
     public void setBirthPostalCode(String birthPostalCode) {
         this.birthPostalCode = birthPostalCode;
     }
 
     public Date getCreationDate() {
         return this.creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
         this.lastModificationDate = creationDate;
     }
 
     public void setCreationDate(String creationDate) {
         if (creationDate == null || creationDate.equals("")) {
             this.creationDate = null;
         } else {
             DateFormat df = DateFormat.getDateInstance();
             try {
                 this.creationDate = df.parse(creationDate);
                 this.lastModificationDate = this.creationDate;
             } catch (java.text.ParseException pe) {
                 // hmm, worrying isn't it ?
             }
         }
     }
 
     public Date getLastModificationDate() {
         return this.lastModificationDate;
     }
 
     public void setLastModificationDate(Date lastModificationDate) {
         this.lastModificationDate = lastModificationDate;
     }
 
     public void setLastModificationDate(String lastModificationDate) {
         if (lastModificationDate == null || lastModificationDate.equals("")) {
             this.lastModificationDate = null;
         } else {
             DateFormat df = DateFormat.getDateInstance();
             try {
                 this.lastModificationDate = df.parse(lastModificationDate);
             } catch (java.text.ParseException pe) {
                 // hmm, worrying isn't it ?
             }
         }
     }
 
     public UserState getState() {
         return this.state;
     }
 
     public void setState(UserState state) {
         this.state = state;
     }
 
     public void setState(String state) {
         this.state = UserState.forString(state);
     }
 
     public Address getAddress() {
         return this.address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
 
     public HomeFolder getHomeFolder() {
         return this.homeFolder;
     }
 
     public void setHomeFolder(HomeFolder homeFolder) {
         this.homeFolder = homeFolder;
     }
 
     public Set<IndividualRole> getIndividualRoles() {
         return individualRoles;
     }
 
     public Set<IndividualRole> getIndividualRoles(final Long individualId) {
         Set<IndividualRole> result = new HashSet<IndividualRole>();
         for (IndividualRole individualRole : individualRoles) {
             if (individualRole.getIndividualId() != null
                     && individualRole.getIndividualId().equals(individualId))
                 result.add(individualRole);
         }
         
         return result;
     }
 
     public Set<IndividualRole> getHomeFolderRoles(Long homeFolderId) {
         if (homeFolderId == null) return Collections.emptySet();
         Set<IndividualRole> result = new HashSet<IndividualRole>();
         for (IndividualRole individualRole : individualRoles) {
             if (homeFolderId.equals(individualRole.getHomeFolderId()))
                 result.add(individualRole);
         }
         return result;
     }
 
     public void setIndividualRoles(Set<IndividualRole> individualRoles) {
         this.individualRoles = individualRoles;
     }
 
     public QoS getQoS() {
         return qoS;
     }
 
     public void setQoS(QoS qoS) {
         this.qoS = qoS;
     }
 
     public String getFullName() {
         return getLastName() + (getFirstName() != null ? " " + getFirstName() : "");
     }
     
     @Override
     public String toString() {
         return new ToStringBuilder(this)
             .append("name", getFullName())
             .append("id", getId())
             .toString();
     }
 
     public boolean isDuplicateAlert() {
         return duplicateAlert;
     }
 
     public void setDuplicateAlert(boolean duplicateAlert) {
         this.duplicateAlert = duplicateAlert;
     }
 
     public String getDuplicateData() {
         return duplicateData;
     }
 
     public void setDuplicateData(String duplicateData) {
         this.duplicateData = duplicateData;
     }
 }
