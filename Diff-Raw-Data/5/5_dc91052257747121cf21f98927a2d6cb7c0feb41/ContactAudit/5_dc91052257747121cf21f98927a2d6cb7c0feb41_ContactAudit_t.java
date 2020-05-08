 /**
  * Created on Oct 12, 2011
  */
 package com.apress.prospring3.ch10.domain;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityResult;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.SqlResultSetMapping;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.hibernate.annotations.Type;
 import org.hibernate.envers.Audited;
 import org.hibernate.envers.NotAudited;
 import org.joda.time.DateTime;
 import org.springframework.data.domain.Auditable;
 
 /**
  * @author Clarence
  *
  */
 @Entity
 @Audited
 @Table(name = "contact_audit")
 @NamedQueries({
 	@NamedQuery(name="ContactAudit.findAll",
 			    query="select c from Contact c"), 
 	@NamedQuery(name="ContactAudit.findById", 
 			    query="select distinct c from Contact c left join fetch c.contactTelDetails t left join fetch c.hobbies h where c.id = :id"),
 	@NamedQuery(name="ContactAudit.findAllWithDetail", 
                 query="select distinct c from Contact c left join fetch c.contactTelDetails t left join fetch c.hobbies h")
 })
 @SqlResultSetMapping(
 		name="contactAuditResult",
 		entities=@EntityResult(entityClass=ContactAudit.class)
 )
 public class ContactAudit implements Auditable<String, Long>, Serializable {
 
 	private Long id;
 	private int version;
 	private String firstName;
 	private String lastName;
 	private Date birthDate;
 	private Set<Hobby> hobbies = new HashSet<Hobby>();
 	private Set<ContactTelDetail> contactTelDetails = new HashSet<ContactTelDetail>();
 
 	// Audit fields
 	private String createdBy;
 	private DateTime createdDate;	
 	private String lastModifiedBy;
 	private DateTime lastModifiedDate;
 	
 	public ContactAudit() {
 	}
 
 	public ContactAudit(String firstName, String lastName) {
 		this.firstName = firstName;
 		this.lastName = lastName;
 	}
 
 	public ContactAudit(String firstName, String lastName, Date birthDate,
 			Set<Hobby> hobbies, Set<ContactTelDetail> contactTelDetails) {
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.birthDate = birthDate;
 		this.hobbies = hobbies;
 		this.contactTelDetails = contactTelDetails;
 	}
 
 	@Id
 	@GeneratedValue(strategy = IDENTITY)
 	@Column(name = "ID")
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Version
 	@Column(name = "VERSION")
 	public int getVersion() {
 		return this.version;
 	}
 
 	public void setVersion(int version) {
 		this.version = version;
 	}
 
 	@Column(name = "FIRST_NAME")
 	public String getFirstName() {
 		return this.firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	@Column(name = "LAST_NAME")
 	public String getLastName() {
 		return this.lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "BIRTH_DATE")
 	public Date getBirthDate() {
 		return this.birthDate;
 	}
 
 	public void setBirthDate(Date birthDate) {
 		this.birthDate = birthDate;
 	}
 
 	@ManyToMany
 	@NotAudited
 	@JoinTable(name = "contact_hobby_detail", 
 	      joinColumns = @JoinColumn(name = "CONTACT_ID"), 
 	      inverseJoinColumns = @JoinColumn(name = "HOBBY_ID"))
 	public Set<Hobby> getHobbies() {
 		return this.hobbies;
 	}
 
 	public void setHobbies(Set<Hobby> hobbies) {
 		this.hobbies = hobbies;
 	}
 
 	@OneToMany(mappedBy = "contact", cascade=CascadeType.ALL, orphanRemoval=true)
 	@NotAudited
 	public Set<ContactTelDetail> getContactTelDetails() {
 		return this.contactTelDetails;
 	}
 
 	public void setContactTelDetails(Set<ContactTelDetail> contactTelDetails) {
 		this.contactTelDetails = contactTelDetails;
 	}
 
 	@Column(name="CREATED_BY")
 	public String getCreatedBy() {
 		return createdBy;
 	}
 
 	public void setCreatedBy(String createdBy) {
 		this.createdBy = createdBy;
 	}
 
 	@Column(name="CREATED_DATE")
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
 	public DateTime getCreatedDate() {
 		return createdDate;
 	}
 
 	public void setCreatedDate(DateTime createdDate) {
 		this.createdDate = createdDate;
 	}
 
 	@Column(name="LAST_MODIFIED_BY")
 	public String getLastModifiedBy() {
 		return lastModifiedBy;
 	}
 	
 	public void setLastModifiedBy(String lastModifiedBy) {
 		this.lastModifiedBy = lastModifiedBy;
 	}
 
 	@Column(name="LAST_MODIFIED_DATE")
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
 	public DateTime getLastModifiedDate() {
 		return lastModifiedDate;
 	}
 
 	public void setLastModifiedDate(DateTime lastModifiedDate) {
 		this.lastModifiedDate = lastModifiedDate;
 	}
 
 	@Transient
 	public boolean isNew() {
 		if (id == null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public String toString() {		
 		return "Contact - Id: " + id + ", First name: " + firstName 
 				+ ", Last name: " + lastName + ", Birthday: " + birthDate
 				+ ", Create by: " + createdBy + ", Create date: " + createdDate
 				+ ", Modified by: " + lastModifiedBy + ", Modified date: " + lastModifiedDate;
 	}	
 
 }
