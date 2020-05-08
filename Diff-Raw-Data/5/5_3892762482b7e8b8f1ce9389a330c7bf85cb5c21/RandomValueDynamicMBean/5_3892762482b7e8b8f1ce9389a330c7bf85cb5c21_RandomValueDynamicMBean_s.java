 /*
  * Copyright (c) 2013, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /*
  * TestDynamicMBean.java
  *
  * Created on January 27, 2013, 1:10 PM
  */
 package org.jmxdatamart.JMXTestServer;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 
 import javax.management.Attribute;
 import javax.management.AttributeList;
 import javax.management.AttributeNotFoundException;
 import javax.management.DynamicMBean;
 import javax.management.InvalidAttributeValueException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.MBeanOperationInfo;
 import javax.management.ReflectionException;
 import org.slf4j.LoggerFactory;
 
 /**
  * Interface TestBeanMBean
  *
  * @author Tesca Fitzgerald <tesca@pdx.edu>
  */
 public class RandomValueDynamicMBean implements DynamicMBean {
     
     private Random random;
     private ArrayList<MBeanAttributeInfo> attributes;
     private ArrayList<MBeanOperationInfo> operations;
     private final org.slf4j.Logger logger = LoggerFactory.getLogger(RandomValueDynamicMBean.class);
     
     private Map<String, Object> data;
     
     public RandomValueDynamicMBean() {
     	random = new Random();
     	attributes = new ArrayList<MBeanAttributeInfo>();
     	operations = new ArrayList<MBeanOperationInfo>();
     	data = new TreeMap<String, Object>();
 
     	//Define the bean operation
         operations.add(new MBeanOperationInfo(
         		"randomize",                      
         		"Get a random number", 
         		null,        // parameter types
         		"int",       // return type
         		MBeanOperationInfo.ACTION));
     }
 
     /** Returns the MBeanInfo object for this dynamic mBean */
     public MBeanInfo getMBeanInfo() {
         return new MBeanInfo(this.getClass().getName(),
         		"Dynamic MBean Implementation",
         		attributes.toArray(new MBeanAttributeInfo[attributes.size()]),
         		null,	//constructor info
         		operations.toArray(new MBeanOperationInfo[operations.size()]),
               	new MBeanNotificationInfo[0]);
     }
     
     /** Get a list of attributes */
 	@Override
 	public AttributeList getAttributes(String[] attributes) {
 	    AttributeList attributesList = new AttributeList();	        
 	    for (int i = 0; i < attributes.length; i++) {
 			try {
 				attributesList.add(new Attribute(attributes[i], getAttribute((String) attributes[i])));
 			} catch (AttributeNotFoundException e) {
 				logger.error(e.getMessage());
 			}
 	    }
 	    
 	    return attributesList;
 	}
 	
 	/** Get a single attribute */
     public Object getAttribute(String attributeName) throws AttributeNotFoundException {
     	if(data.containsKey(attributeName))
     		return data.get(attributeName);
 
     	throw(new AttributeNotFoundException("Attribute '" + attributeName + "' not found in the data mapping"));
     }
     
 
     /** Set a list of attributes */
 	@Override
 	public AttributeList setAttributes(AttributeList attributes) {
 	    AttributeList attributesList = new AttributeList();
 
 	    if (attributes.isEmpty())
 	        return attributesList;
 
 	    for (int i = 0; i < attributes.size(); i++) {
 	        try {
 	            String name = ((Attribute) attributes.get(i)).getName();
 	            Object value = getAttribute(name); 
 	            attributesList.add(new Attribute(name, value));
 	        } catch(Exception e) {
				logger.error(e.getMessage());
 	        }
 	    }
 	    return attributesList;
 	}
 
 	/** Set a single attribute, adding it to the attributes list if necessary. Also puts given data to the data mapping. */
 	@Override
     public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException {
     	String name = attribute.getName();
     	Object value = attribute.getValue();
     	
     	int attributeIndex = -1;
    		for(int i = 0; (attributeIndex == -1) && (i < attributes.size()); i++)
   			if (attributes.get(i).getName().equals(name))
   				attributeIndex = i;
    		
    		if(attributeIndex == -1) {
    			attributeIndex = attributes.size();
    	        addAttribute(name,               
    	        	value.getClass().getCanonicalName(),     
    	        	null, 
    	       		true,    //isReadable                   
     	       	true,    //isWritable
     	       	false);  //isIs           
     	}
    		try {
    			if ((Class.forName(attributes.get(attributeIndex).getType()).isAssignableFrom(value.getClass())))
    				data.put(name, value);
     		else 
     			throw(new InvalidAttributeValueException("Cannot set attribute due to incorrect data type"));
     		return;
    		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
    		}
     	throw(new AttributeNotFoundException("Attribute not found"));
     }
 	
 	/** Add the given attribute to the attributes list */
 	public boolean addAttribute(String name, String className, String description, boolean isReadable, boolean isWritable, boolean isIs) {
  		if (name == null || data.containsKey(name) || className == null)
 			return false;
 
  		for(int i = 0; i < attributes.size(); i++)
    			if (attributes.get(i).getName().equals(name))
    				return false;
 		
 	    attributes.add(new MBeanAttributeInfo(
 	       		name,               
 	       		className,     
 	       		description, 
 	       		isReadable,    //isReadable                   
 	       		isWritable,    //isWritable
 	       		isIs));  //isIs           
 	    return true;
 	}
 
 	/** Remove the given attribute from the data mapping and from the attributes list */
 	public boolean removeAttribute(String name) {
     	int attributeIndex = -1;
    		for(int i = 0; (attributeIndex == -1) && (i < attributes.size()); i++)
    			if (attributes.get(i).getName().equals(name))
    				attributeIndex = i;
    		
 		if (attributeIndex == -1 || name == null || !data.containsKey(name)) {
 			System.out.print("Attribute " + name + " could not be deleted\n");
 			return false;
 		}
 		data.remove(name);
 	    attributes.remove(attributeIndex);         
 	    return true;
 	}
 	
 	/** Invoke a given operation */
 	@Override
 	public Object invoke(String operationName, Object params[], String signature[]) throws MBeanException, ReflectionException {
 	    if (operationName.equals("randomize")) {
 	    	this.randomize((String) params[0]);
 	    	return null;
 	    }
 	    //Add additional if-statement blocks here for other mbean operations
 	    
 	    throw new ReflectionException(new NoSuchMethodException(operationName), "Operation not found");
 	}
 
     private void randomize(String key) {
     	data.put(key, random.nextInt(100));
     }
     
 }
