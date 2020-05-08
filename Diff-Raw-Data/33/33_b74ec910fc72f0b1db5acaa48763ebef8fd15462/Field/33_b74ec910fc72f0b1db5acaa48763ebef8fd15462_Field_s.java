 package com.nacre.service.vo;
 
 public abstract class Field {
 	private String id;
 	private String name;
 	private int minOccurs;
 	private int maxOccurs;
 	private Decoration decoration;
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public Decoration getDecoration() {
 		return decoration;
 	}
 
 	public void setDecoration(Decoration decoration) {
 		this.decoration = decoration;
 	}
 
 	public int getMinOccurs() {
 		return minOccurs;
 	}
 	
 	public boolean isRequired() {
 		return minOccurs > 0;
 	}
 
 	public void setMinOccurs(int minOccurs) {
 		this.minOccurs = minOccurs;
 	}
 
 	public int getMaxOccurs() {
 		return maxOccurs;
 	}
 
 	public void setMaxOccurs(int maxOccurs) {
 		this.maxOccurs = maxOccurs;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public abstract FieldType getFieldType();
 
 	public static enum FieldType {
 		ComplexType, SimpleType;
 	}
 
 }
