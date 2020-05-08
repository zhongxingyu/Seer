 
 package axirassa.services.sentinel.model;
 
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.UniqueConstraint;
 
 @Entity
 @Table(name = "SentinelNetworkStats", uniqueConstraints = { @UniqueConstraint(columnNames = {
         "`Machine_ID`", "`Date`", "`Device`" }) })
 public class SentinelNetworkStatisticModel implements SentinelStatisticModel {
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	public Long id;
 
 	@Basic
 	@Column(name = "`Machine_ID`", nullable = false)
 	public int machineid;
 
 	@Basic
 	@Column(name = "`Date`", nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	public Date date;
 
 	@Basic
 	@Column(name = "`Device`", nullable = false)
 	public String device;
 
 	@Basic
	@Column(name = "`Send`", nullable = false)
	public long send;
 
 	@Basic
	@Column(name = "`Receive`", nullable = false)
	public long receive;
 }
