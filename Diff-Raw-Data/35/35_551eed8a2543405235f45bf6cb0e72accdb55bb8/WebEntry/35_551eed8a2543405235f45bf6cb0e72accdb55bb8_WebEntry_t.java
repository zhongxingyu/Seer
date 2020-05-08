 package com.mavenlab.jetset.model;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 @Entity
 @DiscriminatorValue("WEB")
 @NamedQueries({
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchByMsisdnDateRange", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt BETWEEN :startDate AND :endDate " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchByMsisdnStartDate", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt >= :startDate " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchByMsisdnEndDate", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt <= :endDate " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.countByMsisdnDateRange", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt BETWEEN :startDate AND :endDate " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.WebEntry.countByMsisdnStartDate", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt >= :startDate " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.WebEntry.countByMsisdnEndDate", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt <= :endDate " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.WebEntry.countByDateRange", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt BETWEEN :startDate AND :endDate"),
 	@NamedQuery(name = "jetset.query.WebEntry.countByStartDate", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt >= :startDate"),
 	@NamedQuery(name = "jetset.query.WebEntry.countByEndDate", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt <= :endDate"),
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchByDateRange", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt BETWEEN :startDate AND :endDate " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchByStartDate", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt >= :startDate " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchByEndDate", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt <= :endDate " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.countByMsisdn", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive' " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchByMsisdn", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.countAll", 
 			query = "Select COUNT(id) FROM WebEntry " +
 					"WHERE status != 'inactive'"),
 	@NamedQuery(name = "jetset.query.WebEntry.findFetchAll", 
 			query = "Select entry FROM WebEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.WebEntry.findById", 
			query = "SELECT entry FROM WebEntry entry " +
			"LEFT JOIN FETCH entry.station " +
			"LEFT JOIN FETCH entry.prize " +
			"WHERE entry.status != 'inactive' " +
			"AND entry.id = :id")
 })
 public class WebEntry extends Entry {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1047371976228046915L;
 
 	@Column(name = "name", length = 200, nullable = true)
 	private String name;
 
 	@Column(name = "payment_mode", length = 50)
 	private String paymentMode;
 	
 	@Column(name = "fuel_grade", length = 50)
 	private String fuelGrade;
 	
 	@Column(name = "cc_type", length = 50)
 	private String ccType;
 	
 	@Column(name = "email", length = 50)
 	private String email;
 	
 	@Column(name = "member")
 	private boolean member;
 
 	@Column(name = "agree")
 	private boolean agree;
 	
 	@Column(name = "subscribe")
 	private boolean subscribe;
 
 	/**
 	 * default constructor
 	 */
 	public WebEntry() {
 		super();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the paymentMode
 	 */
 	public String getPaymentMode() {
 		return paymentMode;
 	}
 
 	/**
 	 * @param paymentMode the paymentMode to set
 	 */
 	public void setPaymentMode(String paymentMode) {
 		this.paymentMode = paymentMode;
 	}
 
 	/**
 	 * @return the fuelGrade
 	 */
 	public String getFuelGrade() {
 		return fuelGrade;
 	}
 
 	/**
 	 * @param fuelGrade the fuelGrade to set
 	 */
 	public void setFuelGrade(String fuelGrade) {
 		this.fuelGrade = fuelGrade;
 	}
 
 	/**
 	 * @return the ccType
 	 */
 	public String getCcType() {
 		return ccType;
 	}
 
 	/**
 	 * @param ccType the ccType to set
 	 */
 	public void setCcType(String ccType) {
 		this.ccType = ccType;
 	}
 
 	/**
 	 * @return the email
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * @param email the email to set
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	/**
 	 * @return the member
 	 */
 	public boolean isMember() {
 		return member;
 	}
 
 	/**
 	 * @param member the member to set
 	 */
 	public void setMember(boolean member) {
 		this.member = member;
 	}
 
 	/**
 	 * @return the agree
 	 */
 	public boolean isAgree() {
 		return agree;
 	}
 
 	/**
 	 * @param agree the agree to set
 	 */
 	public void setAgree(boolean agree) {
 		this.agree = agree;
 	}
 
 	/**
 	 * @return the subscribe
 	 */
 	public boolean isSubscribe() {
 		return subscribe;
 	}
 
 	/**
 	 * @param subscribe the subscribe to set
 	 */
 	public void setSubscribe(boolean subscribe) {
 		this.subscribe = subscribe;
 	}
 	
 }
