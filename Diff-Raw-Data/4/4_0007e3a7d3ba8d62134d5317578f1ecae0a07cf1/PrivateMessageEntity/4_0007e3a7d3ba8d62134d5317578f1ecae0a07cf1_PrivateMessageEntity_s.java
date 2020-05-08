 package de.deyovi.chat.core.entities;
 
 import java.sql.Timestamp;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "privatemessage")
 
 @NamedQueries({
 	@NamedQuery(name="findBySender", 
 				query="SELECT m " //
 					+ "FROM PrivateMessageEntity m " //
 					+ "WHERE m.sender = :sender "//
 					+ "ORDER BY m.date desc"
 	),
 	@NamedQuery(name="findByRecipient", 
 				query="SELECT m " //
 					+ "FROM PrivateMessageEntity m " //
 					+ "WHERE m.recipient = :recipient "//
 					+ "ORDER BY m.date desc"
 	),
 	@NamedQuery(name="countOutboxBySender", 
 		query="SELECT count(m.id) " //
 			+ "FROM PrivateMessageEntity m " //
 			+ "WHERE m.sender = :user " //
 	),
 	@NamedQuery(name="countInboxByRecipient", 
 	query="SELECT count(m.id) " //
 		+ "FROM PrivateMessageEntity m " //
 		+ "WHERE m.recipient = :user " //
 	),
 	@NamedQuery(name="countInboxUnreadByRecipient", 
 	query="SELECT count(m.id) " //
 		+ "FROM PrivateMessageEntity m " //
 		+ "WHERE m.recipient = :user " //
		+ "AND m.read = false" //
)
 })
 public class PrivateMessageEntity {
 
 	@Id
 	@GeneratedValue( strategy = GenerationType.IDENTITY )
 	private long id;
 	@Basic
 	@Column(length=40)
 	private String subject;
 	@Basic(fetch=FetchType.LAZY)
 	@Column(length=4000)
 	private String body;
 	@Basic
 	private Timestamp date;
 	@Basic
 	private boolean read;
 	@ManyToOne
 	private ChatUserEntity sender;
 	@Basic
 	@Column(length=20)
 	private String senderName;
 	@ManyToOne
 	private ChatUserEntity recipient;
 	@Basic
 	@Column(length=20)
 	private String recipientName;
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public void setSubject(String param) {
 		this.subject = param;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public void setBody(String param) {
 		this.body = param;
 	}
 
 	public String getBody() {
 		return body;
 	}
 	
 	public void setDate(Timestamp param) {
 		this.date = param;
 	}
 	
 	public Timestamp getDate() {
 		return date;
 	}
 	
 	public void setRead(boolean param) {
 		this.read = param;
 	}
 	
 	public boolean isRead() {
 		return read;
 	}
 
 	public ChatUserEntity getSender() {
 	    return sender;
 	}
 
 	public void setSender(ChatUserEntity param) {
 	    this.sender = param;
 	}
 
 	public String getSenderName() {
 		return senderName;
 	}
 	
 	public void setSenderName(String senderName) {
 		this.senderName = senderName;
 	}
 	
 	public ChatUserEntity getRecipient() {
 	    return recipient;
 	}
 
 	public void setRecipient(ChatUserEntity param) {
 	    this.recipient = param;
 	}
 	
 	public String getRecipientName() {
 		return recipientName;
 	}
 	
 	public void setRecipientName(String recipientName) {
 		this.recipientName = recipientName;
 	}
 	
 
 }
