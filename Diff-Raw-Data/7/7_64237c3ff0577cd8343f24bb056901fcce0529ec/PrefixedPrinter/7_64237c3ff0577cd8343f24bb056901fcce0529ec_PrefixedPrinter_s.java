 package org.example.spring.services.impl;
 
 import org.example.spring.services.Printer;
 import org.springframework.beans.factory.annotation.Value;
 
 public class PrefixedPrinter implements Printer {
 	@Value("#{configuration.title}")
 	private String title;
 	private String prefix;
 
 	public String getPrefix() {
 		return prefix;
 	}
 
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 
	public String getMessage() {
 		return title;
 	}
 
	public void setMessage(String message) {
		this.title = message;
 	}
 	
 	@Override
 	public void print(String message) {
 		System.out.println(title + ": " + prefix + message);
 	}
 }
