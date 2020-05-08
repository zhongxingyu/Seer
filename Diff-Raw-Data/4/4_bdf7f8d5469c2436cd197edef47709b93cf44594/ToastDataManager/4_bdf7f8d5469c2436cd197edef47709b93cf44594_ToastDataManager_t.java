 /*******************************************************************************
 * Copyright (c) 2009 Oracle and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors: 
  *     Shaun Smith - initial API and implementation
 *     EclipseSource - Additional work
  *******************************************************************************/
 package org.eclipse.examples.toast.backend.data.persistence;
 
 import java.util.Collection;
 import java.util.Map;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.NonUniqueResultException;
 import org.eclipse.examples.toast.backend.controlcenter.IData;
 import org.eclipse.examples.toast.backend.data.IDriver;
 import org.eclipse.examples.toast.backend.data.IVehicle;
 import org.eclipse.examples.toast.backend.data.IWaybill;
 import org.eclipse.examples.toast.backend.data.internal.Vehicle;
 import org.eclipse.persistence.config.PersistenceUnitProperties;
 import org.eclipse.persistence.jpa.osgi.PersistenceProvider;
 
 public class ToastDataManager implements IData {
 
 	private static EntityManagerFactory emf;
 	protected String databaseLocation;
 
 	public ToastDataManager() {
 	}
 
 	private EntityManagerFactory initEntityManagerFactory(Map<String, Object> properties) {
 		if (emf == null) {
 			ClassLoader classLoader = this.getClass().getClassLoader();
 			properties.put(PersistenceUnitProperties.CLASSLOADER, classLoader);
 			addPersistenceUnitProperties(properties);
 			emf = new PersistenceProvider().createEntityManagerFactory(getPersistenceUnitName(), properties);
 		}
 		return emf;
 	}
 
 	protected void addPersistenceUnitProperties(Map<String, Object> properties) {
 	}
 
 	protected String getPersistenceUnitName() {
 		return "toast";
 	}
 
 	public void update(IVehicle object) {
 		update((Object) object);
 	}
 
 	public void update(Object object) {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			em.getTransaction().begin();
 			em.merge(object);
 			em.getTransaction().commit();
 		} finally {
 			em.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public Collection<IVehicle> getVehicles() {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			return em.createNamedQuery("Vehicle.findAll").getResultList();
 		} finally {
 			em.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public Collection<IWaybill> getWaybills() {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			return em.createNamedQuery("Waybill.findAll").getResultList();
 		} finally {
 			em.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public Collection<IDriver> getDrivers() {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			return em.createNamedQuery("Driver.findAll").getResultList();
 		} finally {
 			em.close();
 		}
 	}
 
 	public IVehicle getVehicleFor(IWaybill waybill) {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			return (IVehicle) em.createNamedQuery("Vehicle.findByWaybillId").setParameter("waybillId", waybill.getId()).getSingleResult();
 		} catch (NoResultException e) {
 			return null;
 		} finally {
 			em.close();
 		}
 	}
 
 	public IVehicle getVehicleFor(IDriver driver) {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			return (IVehicle) em.createNamedQuery("Vehicle.findByDriverId").setParameter("driverId", driver.getId()).getSingleResult();
 		} catch (NoResultException e) {
 			return null;
 		} finally {
 			em.close();
 		}
 	}
 
 	public void persist(IVehicle object) {
 		persist((Object) object);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void persist(Object object) {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			em.getTransaction().begin();
 			em.persist(object);
 			em.getTransaction().commit();
 		} finally {
 			if (em != null) {
 				em.close();
 			}
 		}
 	}
 
 	public void startup(Map properties) {
 		initEntityManagerFactory(properties);
 	}
 
 	public void shutdown() {
 		emf.close();
 	}
 
 	public Collection getVehicleNames() {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			return em.createQuery("select v.name from Vehicle").getResultList();
 		} finally {
 			em.close();
 		}
 	}
 
 	public IVehicle getVehicle(String name) {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			return (IVehicle) em.createQuery("select v from Vehicle v where v.name = :name").setParameter("name", name).getSingleResult();
 		} finally {
 			em.close();
 		}
 	}
 
 	public void removeVehicle(String name) {
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			em.getTransaction().begin();
 			Vehicle vehicle = (Vehicle) em.createQuery("select v from Vehicle v where v.name = :name").setParameter("name", name).getSingleResult();
 			em.remove(vehicle);
 			em.getTransaction().commit();
 		} catch (NoResultException e) {
 			// TODO what to do in case Vehicle to remove does not exist?
 		} catch (NonUniqueResultException e) {
 			// TODO what to do in case multiple Vehicles have same name?
 		} finally {
 			em.close();
 		}
 	}
 }
