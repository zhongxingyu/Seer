 package com.planetpope.ehcache.dao;
 
 import org.apache.log4j.Logger;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Repository;
 
 import com.planetpope.ehcache.model.Person;
 
 @Repository("baseDao")
 public class PersonDaoImpl implements BaseDao {
 	
 	private static final Logger LOGGER = Logger.getLogger(PersonDaoImpl.class);
 	private static int cacheMisses = 0;
 
	@Cacheable(value="Person",key="#key")
 	public final Person getObject(String key) {		
 		cacheMisses++;
 		
 		Person person = buildPerson(key);
 		
 		LOGGER.debug("Cache has been missed " + cacheMisses + " time(s).") ;
 		
 		return person;		
 	}
 	
 	//stubbed data building
 	private static Person buildPerson(String key) {
 		LOGGER.debug("Building new Person object");
 		
 		Person person = new Person();
 		person.setPersonId(key);
 		person.setFirstName("Joe");
 		person.setLastName("Smith");
 		return person;
 	}
 	
 	
 	public int getExecuteCount() {
 		return cacheMisses;
 	}
 
 	@CacheEvict(value="Person", allEntries=true)
 	public void clearObjects() {
 		LOGGER.debug("Person cache evicted....");	
 	}
 	
	@CacheEvict(value="Person", key="#key")
 	public void clearObject(String key) {
 		LOGGER.debug("Person cache evicted....");	
 	}
 }
