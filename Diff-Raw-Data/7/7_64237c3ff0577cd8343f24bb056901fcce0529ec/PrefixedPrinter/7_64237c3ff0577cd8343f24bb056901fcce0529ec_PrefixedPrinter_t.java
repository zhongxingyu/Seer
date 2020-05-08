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
 
	public String getTitle() {
 		return title;
 	}
 
	public void setTitle(String title) {
		this.title = title;
 	}
 	
 	@Override
 	public void print(String message) {
 		System.out.println(title + ": " + prefix + message);
 	}
 }
