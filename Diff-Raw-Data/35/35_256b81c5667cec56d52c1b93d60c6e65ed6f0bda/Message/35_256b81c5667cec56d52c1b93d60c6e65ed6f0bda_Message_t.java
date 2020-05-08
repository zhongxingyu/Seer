 package com.seventysevenagency.chat.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="messages")
 public class Message {
 	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name="id")
 	private int id;
 	
 	@Column(name="conversation_id")
 	private int conversationId;
 	
 	@ManyToOne
     @JoinColumn(name="user_id")
 	private User user;
 	
 	@Column(name="text")
 	private String text;
 	
 	@Column(name="date")
 	private long date;
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public int getConversationId() {
 		return conversationId;
 	}
 
 	public void setConversationId(int mConversationId) {
 		this.conversationId = mConversationId;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public long getDate() {
 		return date;
 	}
 
 	public void setDate(long unixTime) {
 		this.date = unixTime;
 	}
 
 }
