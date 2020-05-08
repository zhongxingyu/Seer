 package com.pjf.mat.api;
 
 import java.util.Collection;
 import java.util.Properties;
 
 /** @model */
 public interface MatModel {
 
 	public MatModel copy() throws Exception;
 
 	/**
 	 * return collection of mutable elements
 	 * @model type="Element" containment="true"
 	 */
 	public Collection<Element> getElements(); 
 
 	/**
 	 * Return element for this id or null
 	 * @model
 	 */
 	public Element getElement(int id); 
 
 	/**
 	 * @model
 	 */
 	public long getSWSignature();
 
 	/**
 	 * @model
 	 */
 	public Element getType(String typeName);
 	
 	
 	public Properties getProperties();
 }
