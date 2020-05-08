 package com.mavenlab.jetset.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.QueryHint;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "stations")
 
 @NamedQueries({
 	@NamedQuery(name = "jetset.query.Station.findActive", 
 			query = "FROM Station WHERE status = 'active' " +
 					"ORDER BY name ASC",
			hints = {
					@QueryHint(name = "org.hibernate.cacheable", value = "true")
 			}),
 	@NamedQuery(name = "jetset.query.Station.findById", 
 			query = "FROM Station WHERE id = :id " +
 					"AND status = 'active' " +
 					"ORDER BY id ASC")
 })
 public class Station extends EntityBase{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 790503819392315123L;
 
 	@Id
 	@Column(name = "id", nullable = false)
 	private int id;
 	
 	@Column(name = "name", nullable = false)
 	private String name;
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(int number) {
 		this.id = number;
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
 }
