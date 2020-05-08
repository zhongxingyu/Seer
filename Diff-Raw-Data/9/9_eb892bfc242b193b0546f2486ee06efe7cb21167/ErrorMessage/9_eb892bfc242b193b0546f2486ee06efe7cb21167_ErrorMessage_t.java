 package com.onb.otp.domain;
 
import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
 
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
	@XmlAttribute(name="status")
 	public void setStatus(String status) {
 		this.status = status;
 	}
 	public String getMessage() {
 		return message;
 	}
	@XmlValue
 	public void setMessage(String message) {
 		this.message = message;
 	}
 }
