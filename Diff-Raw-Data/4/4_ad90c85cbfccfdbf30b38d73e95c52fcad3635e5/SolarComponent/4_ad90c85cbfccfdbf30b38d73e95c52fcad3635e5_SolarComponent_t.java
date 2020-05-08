 /*
  * Solar power calculator
  * 
  * Copyright (C) 2012, ORANGE group.
  * See LICENSE.txt for license details.
  */
 
 package com.qut.spc.model;
 
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.xml.bind.annotation.XmlElement;
 
 import com.google.appengine.api.datastore.Key;
 import com.qut.spc.EMF;
 
 /**
  * Common interface for each component in solar system.
  * 
  * @author QuocViet
  */
 @Entity
 @MappedSuperclass
public abstract class SolarComponent {

 	@Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Key key;
 
 	// TODO: Create class Manufacture
 	private String manufacture;
 	
 	private double price;
 	
 	private double efficiencyDecrease;
 	
 	public SolarComponent() {
 		manufacture = "";
 		price = 0.0;
 		efficiencyDecrease = 0.0;
 	}
 
 	/**
 	 * @return Id of this component in database
 	 */
 	@XmlElement
 	public long getId() {
 		if (key != null) {
 			return key.getId();
 		}
 		return -1;
 	}
 	
 	/**
 	 * Stub method for setting ID, actually do nothing.
 	 * This will eliminate warning from DataNucleus
 	 * @param id ID to set
 	 */
 	public void setId(long id) {
 		// Do nothing
 	}
 	
 	/**
 	 * @return The name of manufacture
 	 */
 	@XmlElement
 	public String getManufacture() {
 		return this.manufacture;
 	}
 	
 	/**
 	 * @param efficiencyDecrease The efficiency to set
 	 */
 	public void setManufacture(String manufacture) {
 		this.manufacture = manufacture;
 	}
 	
 	/**
 	 * @return The price of this component
 	 */
 	@XmlElement
 	public double getPrice() {
 		return price;
 	}
 
 	/**
 	 * @param price The price to set
 	 * @throws Exception If price is negative
 	 */
 	public void setPrice(double price) throws Exception {
 		if (price < 0.0) {
 			throw new Exception("Price must not be negative");
 		}
 		this.price = price;
 	}
 
 	/**
 	 * @return The efficiency decrease linearly by each year
 	 */
 	@XmlElement
 	public double getEfficiencyDecrease() {
 		return efficiencyDecrease;
 	}
 
 	/**
 	 * @param efficiencyDecrease The efficiency to set
 	 * @throws Exception If efficiency less than 0 or greater than 100
 	 */
 	public void setEfficiencyDecrease(double efficiencyDecrease) throws Exception {
 		if (efficiencyDecrease < 0.0 || efficiencyDecrease > 100.0) {
 			throw new Exception("Efficiency must be from 0 to 100");
 		}
 		this.efficiencyDecrease = efficiencyDecrease;
 	}
 	
 	/**
 	 * Get the list of efficiency by years
 	 * @param years Number of years to retrieve
 	 * @return list of efficiency
 	 */
 	public double[] getEfficiencyByYear(int years) throws Exception {
 		if (years < 0) {
 			throw new Exception("Years must not be negative");
 		}
 		double listEff[] = new double[years];
 		double eff = 100.0;
 		
 		for (int i = 0; i < years; ++i) {
 			listEff[i] = eff;
 			eff -= efficiencyDecrease;
 		}
 		return listEff;
 	}
 	
 	public void save() {
 		saveComponent(this);
 	}
 	
 	protected static <T> T saveComponent(T self) {
 		EntityManager em = EMF.get().createEntityManager();
 		try {
 			em.persist(self);
 		} finally {
 			em.close();
 		}
 		return self;
 	}
 	
 	protected static <T> T loadComponent(Object id, Class<T> cls) {
 		EntityManager em = EMF.get().createEntityManager();
 		T self;
 		try {
 			self = em.find(cls, id);
 		} finally {
 			em.close();
 		}
 		return self;
 	}
 }
