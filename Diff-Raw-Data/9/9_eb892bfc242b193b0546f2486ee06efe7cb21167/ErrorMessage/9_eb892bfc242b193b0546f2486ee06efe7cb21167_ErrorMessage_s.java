 package com.onb.otp.domain;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement(name = "error")
 public class ErrorMessage {
 	private String status;
 	private String message;
 	
 	public ErrorMessage() {
 	}
 	
 	public ErrorMessage(String status, String message) {
 		this.status = status;
 		this.message = message;
 	}
 	
 	public String getStatus() {
 		return status;
 	}
	@XmlElement
 	public void setStatus(String status) {
 		this.status = status;
 	}
 	public String getMessage() {
 		return message;
 	}
	@XmlElement
 	public void setMessage(String message) {
 		this.message = message;
 	}
 }
