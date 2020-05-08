 package com.cerberus.server.persistence.DAO;
 
import com.cerberus.server.persistence.beans.Current;
 
 
public class CurrentDAO extends GenericDAO<Current, Integer> {
 
 	public CurrentDAO(){
 		super();
 	}
 	
 	
 	/***/
 	public Current getById(Integer id){
		return getById(getClazz(), id);
 	}
 
 }
