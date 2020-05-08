 package com.friends.charity.model.farzand;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Proxy;
 
 import com.friends.charity.business.logic.CalendarFormat;
 import com.friends.charity.model.BaseEntity;
 
 @Entity
 @Table(name = "FARZANDAN")
 @Inheritance(strategy = InheritanceType.JOINED)
 @Proxy(lazy = false)
 public class Farzandan extends BaseEntity {
 	@Column(name = "FIRST_NAME")
 	private String firstname;
 	@Column(name = "LAST_NAME")
 	private String lastname;
 	@Column(name = "BIRTHDAY")
 	@Temporal(TemporalType.DATE)
 	private Calendar birthday;
 	@Column(name = "GENDER")
 	@Enumerated(EnumType.STRING)
 	private Gender gender;
 	@Column(name = "SHOGHL")
 	private String shoghl;
 	@Column(name = "MAHARAT")
 	private String maharat;
 	// @ManyToOne
 	// private MoshakhasateMotaghazi user;
 	@Transient
 	private Date date;
 	@Transient
 	private String strDate;
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 
 	public String getLastname() {
 		return lastname;
 	}
 
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	public Calendar getBirthday() {
 		return birthday;
 	}
 
 	public void setBirthday(Calendar birthday) {
 		this.birthday = birthday;
 	}
 
 	public Gender getGender() {
 		return gender;
 	}
 
 	public void setGender(Gender gender) {
 		this.gender = gender;
 	}
 
 	public String getShoghl() {
 		return shoghl;
 	}
 
 	public void setShoghl(String shoghl) {
 		this.shoghl = shoghl;
 	}
 
 	public String getMaharat() {
 		return maharat;
 	}
 
 	public void setMaharat(String maharat) {
 		this.maharat = maharat;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public String getStrDate() {
		strDate = CalendarFormat.getStrForDT(getDate());
 		return strDate;
 	}
 
 	public void setStrDate(String strDate) {
 		this.strDate = strDate;
 	}
 
 	// public MoshakhasateMotaghazi getUser() {
 	// if (user == null) {
 	// user = new MoshakhasateMotaghazi();
 	// }
 	// return user;
 	// }
 	//
 	// public void setUser(MoshakhasateMotaghazi user) {
 	// this.user = user;
 	// }
 
 }
