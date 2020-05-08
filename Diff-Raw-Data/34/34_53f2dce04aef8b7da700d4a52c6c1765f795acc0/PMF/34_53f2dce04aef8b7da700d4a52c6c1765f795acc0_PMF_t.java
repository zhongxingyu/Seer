 package com.opensajux.dao;
 
 import java.io.Serializable;
 import java.util.logging.Logger;
 
 import javax.enterprise.inject.Produces;
 import javax.inject.Singleton;
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManagerFactory;
 
 @Singleton
 public final class PMF implements Serializable {
 	private static final long serialVersionUID = 5432595071277388739L;
 
 	private static final PersistenceManagerFactory pmfInstance = JDOHelper
 			.getPersistenceManagerFactory("transactions-optional");
	transient Logger log = Logger.getLogger(PMF.class.getName());
 
 	public static @Produces
 	PersistenceManagerFactory getPMF() {
 		return pmfInstance;
 	}
 
 }
