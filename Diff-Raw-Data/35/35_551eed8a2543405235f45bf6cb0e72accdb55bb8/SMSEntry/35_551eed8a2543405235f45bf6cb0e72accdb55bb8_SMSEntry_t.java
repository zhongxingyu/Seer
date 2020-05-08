 package com.mavenlab.jetset.model;
 
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 @Entity
 @DiscriminatorValue("SMS")
 @NamedQueries({
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchByMsisdnDateRange", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt BETWEEN :startDate AND :endDate " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchByMsisdnStartDate", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt >= :startDate " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchByMsisdnEndDate", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt <= :endDate " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countByMsisdnDateRange", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt BETWEEN :startDate AND :endDate " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countByMsisdnStartDate", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt >= :startDate " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countByMsisdnEndDate", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt <= :endDate " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countByDateRange", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt BETWEEN :startDate AND :endDate"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countByStartDate", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt >= :startDate"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countByEndDate", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive' " +
 					"AND createdAt <= :endDate"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchByDateRange", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt BETWEEN :startDate AND :endDate " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchByStartDate", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt >= :startDate " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchByEndDate", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.createdAt <= :endDate " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countByMsisdn", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive' " +
 					"AND msisdn LIKE '%' || :msisdn || '%'"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchByMsisdn", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"AND entry.msisdn LIKE '%' || :msisdn || '%' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.countAll", 
 			query = "Select COUNT(id) FROM SMSEntry " +
 					"WHERE status != 'inactive'"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findFetchAll", 
 			query = "Select entry FROM SMSEntry entry " +
 					"LEFT JOIN FETCH entry.station " +
 					"LEFT JOIN FETCH entry.prize " +
 					"WHERE entry.status != 'inactive' " +
 					"ORDER BY entry.id"),
 	@NamedQuery(name = "jetset.query.SMSEntry.findById", 
			query = "SELECT entry FROM SMSEntry entry " +
					"LEFT JOIN FETCH entry.station " +
					"LEFT JOIN FETCH entry.prize " +
					"WHERE entry.status != 'inactive' " +
					"AND entry.id = :id")
 })
@XmlRootElement(name = "sms")
 public class SMSEntry extends Entry {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1047371976228046915L;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "mo_log_id", nullable = true)
 	private MOLog moLog;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "mt_log_id", nullable = true)
 	private MTLog mtLog;
 
 	/**
 	 * default constructor
 	 */
 	public SMSEntry() {
 		super();
 	}
 
 	/**
 	 * @return the moLog
 	 */
 	@XmlTransient
 	@JsonIgnore
 	public MOLog getMoLog() {
 		return moLog;
 	}
 
 	/**
 	 * @param moLog the moLog to set
 	 */
 	public void setMoLog(MOLog moLog) {
 		this.moLog = moLog;
 	}
 
 	/**
 	 * @return the mtLog
 	 */
 	@XmlTransient
 	@JsonIgnore
 	public MTLog getMtLog() {
 		return mtLog;
 	}
 
 	/**
 	 * @param mtLog the mtLog to set
 	 */
 	public void setMtLog(MTLog mtLog) {
 		this.mtLog = mtLog;
 	}
 }
