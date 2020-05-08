 package lib.entities;
 
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import lib.utils.Utility;
 
 
 @Entity
 @Table(name = "request", schema = "public")
 public class Request implements java.io.Serializable {
 	private static final long serialVersionUID = -2326932267771781244L;
 
 	private int id;
 	private User userBySenderId;
 	private User userBySolverId;
 	private Date created;
 	private Date resent;
 	private Date solved;
 	private String solution;
 	
 	public Request() {
 	}
 
 	public Request(int id, User userBySenderId, Date created) {
 		this.id = id;
 		this.userBySenderId = userBySenderId;
 		this.created = created;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	@Id
 	@Column(name = "id", unique = true, nullable = false)
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	public int getId() {
 		return this.id;
 	}
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "sender_id", nullable = false)
 	public User getUserBySenderId() {
 		return this.userBySenderId;
 	}
 
 	public void setUserBySenderId(User userBySenderId) {
 		this.userBySenderId = userBySenderId;
 	}
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "solver_id")
 	public User getUserBySolverId() {
 		return this.userBySolverId;
 	}
 
 	public void setUserBySolverId(User userBySolverId) {
 		this.userBySolverId = userBySolverId;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "created", nullable = false, length = 29)
 	public Date getCreated() {
 		return this.created;
 	}
 
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "resent", length = 29)
 	public Date getResent() {
 		return this.resent;
 	}
 
 	public void setResent(Date resent) {
 		this.resent = resent;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "solved", length = 29)
 	public Date getSolved() {
 		return this.solved;
 	}
 
 	public void setSolved(Date solved) {
 		this.solved = solved;
 	}
 
 	
	@Column(name = "solution")
 	public String getSolution() {
 		return this.solution;
 	}
 
	public void setSolution(String solution) {
 		this.solution = solution;
 	}
 
 	public String serialize() {
 		try {
 			return Utility.toString(this);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}	
 }
