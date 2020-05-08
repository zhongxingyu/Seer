 package com.poliformas.warehouse.integration.dao.impl;
 
 import java.util.Properties;
 
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import com.poliformas.warehouse.integration.dao.PropertyDAO;
 import com.poliformas.warehouse.integration.entity.Property;
 
 
 public class PropertyDAOImpl extends HibernateDaoSupport implements PropertyDAO{
 
 	public void saveProperty(Property property){
 		getHibernateTemplate().save(property);
 		}
 		public void updateProperty(Property property){
 		getHibernateTemplate().update(property);
 		}
 		public void deleteProperty(Property property){
 		getHibernateTemplate().delete(property);
 		}
 		public Property findProperty(Property property) { 	
	return (Property)getHibernateTemplate().find(String.valueOf(property.getIdProperty)).get(0);
	}
