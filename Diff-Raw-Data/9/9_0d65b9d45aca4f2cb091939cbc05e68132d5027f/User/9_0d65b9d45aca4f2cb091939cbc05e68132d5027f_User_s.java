 package com.extedu.ticketson2.model.entity;
 
 import java.io.Serializable;
 
 public class User implements Serializable {
 
 	private static final long serialVersionUID = 7177014660621405534L;
 
 	private long id;
 	private String login;
 	private String password;
 
 	public User() {		
 	}
 
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public String getLogin() {
 		return login;
 	}
 	public void setLogin(String login) {
 		this.login = login;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}	
 }
