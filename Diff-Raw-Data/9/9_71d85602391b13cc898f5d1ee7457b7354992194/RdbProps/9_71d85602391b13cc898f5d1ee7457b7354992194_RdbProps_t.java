 /**
  * 
  */
 package com.pace.base.db;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import com.pace.base.misc.KeyValue;
 
 /**
  * Relational DB properties
  * 
  * @author JWatkins
  *
  */
 public class RdbProps implements Serializable {
 	
 	private static final long serialVersionUID = -1796795377109159322L;
 	
 	private String name;
 	
 	private Properties hibernateProperties;
 	
 	private List<String> mappingResources;
 	
	private transient List<KeyValue> hibernatePropertyList;
 	
 	public RdbProps() {
 		
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the hibernateProperties
 	 */
 	public Properties getHibernateProperties() {
 		return hibernateProperties;
 	}
 
 	/**
 	 * @param hibernateProperties the hibernateProperties to set
 	 */
 	public void setHibernateProperties(Properties hibernateProperties) {
 		this.hibernateProperties = hibernateProperties;
 	}
 
 	/**
 	 * @return the mappingResources
 	 */
 	public List<String> getMappingResources() {
 		return mappingResources;
 	}
 
 	/**
 	 * @param mappingResources the mappingResources to set
 	 */
 	public void setMappingResources(List<String> mappingResources) {
 		this.mappingResources = mappingResources;
 	}
 
 	/**
 	 * @return the hibernatePropertyList
 	 */
 	public List<KeyValue> getHibernatePropertyList() {
 		
 		if ( hibernatePropertyList == null ) {
 			
 			hibernatePropertyList = new ArrayList<KeyValue>();
 			
 		}
 		
 		return hibernatePropertyList;
 	}
 
 	/**
 	 * @param hibernatePropertyList the hibernatePropertyList to set
 	 */
 	public void setHibernatePropertyList(List<KeyValue> hibernatePropertyList) {
 		this.hibernatePropertyList = hibernatePropertyList;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "RdbProps [name=" + name + ", hibernateProperties="
 				+ hibernateProperties + ", mappingResources="
 				+ mappingResources + ", hibernatePropertyList="
 				+ hibernatePropertyList + "]";
 	}
 
 	
 	
 
 }
