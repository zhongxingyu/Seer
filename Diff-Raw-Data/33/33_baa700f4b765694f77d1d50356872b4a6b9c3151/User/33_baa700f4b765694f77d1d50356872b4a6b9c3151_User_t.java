 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: Ort Braude College
  * License Type: Academic
  */
 package icm.dao;
 
 import java.io.Serializable;
 import javax.persistence.*;
 @Entity
 @org.hibernate.annotations.Proxy(lazy=false)
 @Table(name="`User`")
 public class User implements Serializable {
 	public User() {
 	}
 	
 	private java.util.Set this_getSet (int key) {
 		if (key == icm.dao.ORMConstants.KEY_USER_INITIATEDREQUESTS) {
 			return ORM_initiatedRequests;
 		}
 		else if (key == icm.dao.ORMConstants.KEY_USER_MAILS) {
 			return ORM_mails;
 		}
 		
 		return null;
 	}
 	
 	@Transient	
 	org.orm.util.ORMAdapter _ormAdapter = new org.orm.util.AbstractORMAdapter() {
 		public java.util.Set getSet(int key) {
 			return this_getSet(key);
 		}
 		
 	};
 	
 	@Column(name="UserID", nullable=false)	
 	@Id	
 	@GeneratedValue(generator="ICM_DAO_USER_USERID_GENERATOR")	
 	@org.hibernate.annotations.GenericGenerator(name="ICM_DAO_USER_USERID_GENERATOR", strategy="native")	
 	private int userID;
 	
 	@Column(name="Password", nullable=true, length=255)	
 	private String password;
 	
 	@Column(name="Name", nullable=true, length=255)	
 	private String name;
 	
 	@Column(name="Surname", nullable=true, length=255)	
 	private String surname;
 	
 	@Column(name="Email", nullable=true, length=255)	
 	private String email;
 	
 	@Column(name="Type", nullable=true, length=10)	
	@org.hibernate.annotations.Type(type="icm.dao.UserTypeUserType")	
 	@Enumerated(EnumType.STRING)
 	private icm.dao.UserType type;
 	
 	@OneToMany(mappedBy="initiator", targetEntity=icm.dao.Request.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_initiatedRequests = new java.util.HashSet();
 	
 	@OneToMany(mappedBy="owner", targetEntity=icm.dao.UserMail.class)	
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.LOCK})	
 	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.TRUE)	
 	private java.util.Set ORM_mails = new java.util.HashSet();
 	
 	private void setUserID(int value) {
 		this.userID = value;
 	}
 	
 	public int getUserID() {
 		return userID;
 	}
 	
 	public int getORMID() {
 		return getUserID();
 	}
 	
 	public void setPassword(String value) {
 		this.password = value;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setName(String value) {
 		this.name = value;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setSurname(String value) {
 		this.surname = value;
 	}
 	
 	public String getSurname() {
 		return surname;
 	}
 	
 	public void setEmail(String value) {
 		this.email = value;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 	
 	public void setType(icm.dao.UserType value) {
 		this.type = value;
 	}
 	
 	public icm.dao.UserType getType() {
 		return type;
 	}
 	
 	private void setORM_InitiatedRequests(java.util.Set value) {
 		this.ORM_initiatedRequests = value;
 	}
 	
 	private java.util.Set getORM_InitiatedRequests() {
 		return ORM_initiatedRequests;
 	}
 	
 	@Transient	
 	public final icm.dao.RequestSetCollection initiatedRequests = new icm.dao.RequestSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_USER_INITIATEDREQUESTS, icm.dao.ORMConstants.KEY_REQUEST_INITIATOR, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	private void setORM_Mails(java.util.Set value) {
 		this.ORM_mails = value;
 	}
 	
 	private java.util.Set getORM_Mails() {
 		return ORM_mails;
 	}
 	
 	@Transient	
 	public final icm.dao.UserMailSetCollection mails = new icm.dao.UserMailSetCollection(this, _ormAdapter, icm.dao.ORMConstants.KEY_USER_MAILS, icm.dao.ORMConstants.KEY_USERMAIL_OWNER, icm.dao.ORMConstants.KEY_MUL_ONE_TO_MANY);
 	
 	public String toString() {
 		return String.valueOf(getUserID());
 	}
 	
 }
