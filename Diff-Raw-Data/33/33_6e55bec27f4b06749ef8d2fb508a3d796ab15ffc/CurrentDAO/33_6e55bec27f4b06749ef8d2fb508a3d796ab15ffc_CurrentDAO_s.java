 package com.cerberus.server.persistence.DAO;
 
import java.io.Serializable;
import java.util.List;
 
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
 
public class CurrentDAO<Current, Integer> extends GenericDAO<Current, Integer> {
 
 	public CurrentDAO(){
 		super();
 	}
 	
 	
 	/***/
 	public Current getById(Integer id){
		return getById(Class<Current>, id);
 	}
 
 }
