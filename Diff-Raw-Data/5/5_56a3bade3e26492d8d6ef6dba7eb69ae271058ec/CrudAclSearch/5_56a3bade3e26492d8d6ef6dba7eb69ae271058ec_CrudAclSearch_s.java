 package edu.swmed.qbrc.auth.cashmac.server.acl;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import javax.persistence.EntityManager;
 import org.apache.log4j.Logger;
 import edu.swmed.qbrc.auth.cashmac.shared.annotations.*;
 import edu.swmed.qbrc.auth.cashmac.shared.constants.CasHmacAccessLevels;
 
 public class CrudAclSearch {
 
 	private static final Logger log = Logger.getLogger(CrudAclSearch.class);
 	
 	private final CrudAclSearchFactory crudAclSearchFactory;
 	private final Boolean hasNeccessaryAcl;
 	private final Class<? extends Annotation> entityManagerAnnotation;
 	
 	// The two following annotations are expected only once per class.
 	private AnnotationAndValue casHmacPKFieldAnn = null;
 	private AnnotationAndValue casHmacWriteAclAnn = null;
 
 	/**
 	 * Returns a new search that is marked as having all necessary ACLs, but
 	 * without actually searching for ACLs.  This is used by CrudAclSearchFactory
 	 * when a PreAuth has been run successfully, in which case we don't need any
 	 * further checks.
 	 * @param ok
 	 */
 	public CrudAclSearch(Boolean ok) {
 		this.crudAclSearchFactory = null;
 		this.entityManagerAnnotation = null;
 		this.hasNeccessaryAcl = ok;
 	}
 	
 	/**
 	 * Instantiate a new search for a primary object.
 	 * @param crudAclSearchFactory
 	 * @param entity
 	 * @param id
 	 * @param access
 	 * @param currentState
 	 * @param previousState
 	 * @param propertyNames
 	 */
 	public CrudAclSearch(CrudAclSearchFactory crudAclSearchFactory, Object entity, Object id, String access, Object[] currentState, Object[] previousState, String[] propertyNames) {
 		this.crudAclSearchFactory = crudAclSearchFactory; 
 		this.entityManagerAnnotation = null;
 		this.hasNeccessaryAcl = searchForAcl(entity, id, access, currentState, previousState, propertyNames);
 	}
 	
 	/**
 	 * Instantiate a new search for a foreign key referenced object
 	 * @param crudAclSearchFactory
 	 * @param entity
 	 * @param id
 	 * @param access
 	 * @param entityManagerAnnotation
 	 */
 	public CrudAclSearch(CrudAclSearchFactory crudAclSearchFactory, Object entity, Object id, String access, Class<? extends Annotation> entityManagerAnnotation) {
 		this.crudAclSearchFactory = crudAclSearchFactory; 
 		this.entityManagerAnnotation = entityManagerAnnotation;
 		this.hasNeccessaryAcl = searchForForeignAcl(entity, id, access);
 	}
 
 	/**
 	 * Instantiate a new object that adds an ACL (called by CasHmacPostInsertListener)
 	 * @param crudAclSearchFactory
 	 * @param entity
 	 * @param id
 	 */
 	public CrudAclSearch(CrudAclSearchFactory crudAclSearchFactory, Object entity, Object id) {
 		this.crudAclSearchFactory = crudAclSearchFactory;
 		this.entityManagerAnnotation = null;
 		this.hasNeccessaryAcl = false;
 		searchAndAddAcls(entity, id);
 	}
 	
 	/**
 	 * Instantiate a new object that simply deletes all ACLs for an object
 	 * (called by CasHmacPostDeleteListener)
 	 * @param crudAclSearchFactory
 	 * @param entity
 	 * @param id
 	 * @param delete
 	 */
 	public CrudAclSearch(CrudAclSearchFactory crudAclSearchFactory, Object entity, Object id, Boolean delete) {
 		this.crudAclSearchFactory = crudAclSearchFactory;
 		this.entityManagerAnnotation = null;
 		this.hasNeccessaryAcl = false;
 		deleteAcls(entity, id);
 	}
 
 	/**
 	 * Search for appropriate ACLs and set object properties accordingly.
 	 * @param entity
 	 * @param access
 	 */
 	private Boolean searchForAcl(Object entity, Object id, String access, Object[] currentState, Object[] previousState, String[] propertyNames) {
 		Boolean retval = findClassAndForeignKeyAcls(entity, id, access, currentState, previousState, propertyNames);
 		log.trace("searchForAcl(" + entity.getClass().getSimpleName() + " " + id + " returned " + retval.toString());
 		return retval;
 	}
 	
 	private Boolean searchForForeignAcl(Object entity, Object id, String access) {
 		Boolean retval = findClassAndForeignKeyAcls(entity, id, access, null, null, null);
 		log.trace("searchForForeignAcl(" + entity.getClass().getSimpleName() + " " + id + " returned " + retval.toString());
 		return retval;
 	}
 	
 	/**
 	 * Search for all ACLs for an object and delete them all.
 	 * @param entity
 	 * @param id
 	 */
 	private void deleteAcls(Object entity, Object id) {
 		// Delete any ACLs for a particular object when deleting the object
 		crudAclSearchFactory.getCasHmacValidation().deleteAllAclsForObject(entity.getClass(), id);
 	}
 
 	/**
 	 * Search for needed ACLs for a new object and add them
 	 * @param entity
 	 * @param id
 	 */
 	private void searchAndAddAcls(Object entity, Object id) {
 		CasHmacObjectAcl casHmacObjectAcl = entity.getClass().getAnnotation(CasHmacObjectAcl.class);
 		
 		// Initialize fields
 		findPropertyAcls(entity, id, null, null, null);
 		
 		if (casHmacObjectAcl != null && casHmacPKFieldAnn != null) {
 			// Process Write ACLs (ACLs to write when object is saved) if saving a new item
 			if (casHmacWriteAclAnn != null) {
 				for (CasHmacWriteAclParameter param : ((CasHmacWriteAcl)casHmacWriteAclAnn.getAnnotation()).value()) {
 					crudAclSearchFactory.getCasHmacValidation().addAcl(param.access(), entity.getClass(), casHmacPKFieldAnn.getValue(), param.roles());
 				}
 			}
 		}
 	}
 	
 	public Boolean getHasNeccessaryAcl() {
 		return hasNeccessaryAcl;
 	}
 	
 	/**
 	 * Process ACLs for annotations applied directly to a class.
 	 * @param entity
 	 * @param access
 	 * @return
 	 */
 	private Boolean findClassAndForeignKeyAcls(Object entity, Object id, String access, Object[] currentState, Object[] previousState, String[] propertyNames) {
 
 		// Return true by default (unless we found unfulfilled ACLs)
 		Boolean returnValue = true;
 		
 		// Load entity, if necessary
 		if (this.entityManagerAnnotation != null && needsEntityLoad(entity)) {
 			EntityManager em = this.crudAclSearchFactory.getEntityManager(this.entityManagerAnnotation);
 			try {
 				entity = em.find(entity.getClass(), id);
				log.trace("============= Loaded entity of class " + entity.getClass().getSimpleName() + " with id " + id.toString());
 			} catch (Exception e) {
 				if (entity != null)
 					log.trace("============= Unable to load entity " + entity.getClass().getSimpleName() + " with id " + id);
 				log.trace("Error:\n" + e.getMessage());
 				return false;
 			}
 		}
 
 		// Are ACLs stored for this object?
 		CasHmacObjectAcl casHmacObjectAcl = entity.getClass().getAnnotation(CasHmacObjectAcl.class);
 		
 		// Initialize fields
 		findPropertyAcls(entity, id, currentState, previousState, propertyNames);
 
 		/*
 		 *  If an object doesn't have an Object ACL, we know immediately that no ACLs will be read or written
 		 * for the object directly.  There could still be, however, foreign key ACLs for this object.
 		 * Process CRUD ACLs for this object only if the class has an Object ACL.
 		 * 
 		 * We don't check for a CREATE access level ACL here, since all ACLs are tied to a PK value, and there
 		 * isn't a PK yet for a new object.  You'll need to check for CREATE permission with @RolesRequired
 		 * annotation on the RESTful method.  Please note, however, that the findPropertyAcls method (called
 		 * here) does check for @CasHmacForeignFieldCreate annotations on foreign key fields in the entity.
 		 * For example, you could use @CasHmacForeignFieldCreate to configure a Customer object to require
 		 * CREATE or UPDATE access to the Store object referenced in its Customer.storeId field. 
 		 * 
 		 */
 		if (casHmacObjectAcl != null && casHmacPKFieldAnn != null) {
 
 			if (access.equals(CasHmacAccessLevels.READ)) {
 				CasHmacObjectRead casHmacObjectRead = entity.getClass().getAnnotation(CasHmacObjectRead.class);
 				if (returnValue && casHmacObjectRead != null) {
 					if (! crudAclSearchFactory.getCasHmacValidation().verifyAcl(casHmacObjectRead.accessLevel(), casHmacObjectRead.objectClass(), casHmacPKFieldAnn.getValue(), crudAclSearchFactory))
 						returnValue = false;
 				}
 			}
 			
 			else if (access.equals(CasHmacAccessLevels.UPDATE)) {
 				CasHmacObjectUpdate casHmacObjectUpdate = entity.getClass().getAnnotation(CasHmacObjectUpdate.class);
 				if (returnValue && casHmacObjectUpdate != null) {
 					if (! crudAclSearchFactory.getCasHmacValidation().verifyAcl(casHmacObjectUpdate.accessLevel(), casHmacObjectUpdate.objectClass(), casHmacPKFieldAnn.getValue(), crudAclSearchFactory))
 						returnValue = false;
 				}
 			}
 			
 			else if (access.equals(CasHmacAccessLevels.DELETE)) {
 				CasHmacObjectDelete casHmacObjectDelete = entity.getClass().getAnnotation(CasHmacObjectDelete.class);
 				if (returnValue && casHmacObjectDelete != null) {
 					if (! crudAclSearchFactory.getCasHmacValidation().verifyAcl(casHmacObjectDelete.accessLevel(), casHmacObjectDelete.objectClass(), casHmacPKFieldAnn.getValue(), crudAclSearchFactory))
 						returnValue = false;
 				}
 			}
 		}
 
 		// Check for foreign key ACLs
 		if (returnValue) {
 			for (Field field : entity.getClass().getDeclaredFields()) {
 				
 				if (returnValue) {
 				
 					log.trace("-------------- Checking foreign field for " + entity.getClass().getSimpleName() + "." + field.getName() + " ---------------");
 					
 					if (access.equals(CasHmacAccessLevels.CREATE)) {
 						AnnotationAndValue casHmacFFCreateAnn = getPropertyAnnotation(CasHmacForeignFieldCreate.class, field, entity, id, currentState, propertyNames);
 						if (returnValue && casHmacFFCreateAnn != null)
 							if (!getForeignAcl(((CasHmacForeignFieldCreate)casHmacFFCreateAnn.getAnnotation()).objectClass(), casHmacFFCreateAnn.getValue(), ((CasHmacForeignFieldCreate)casHmacFFCreateAnn.getAnnotation()).accessLevel(), ((CasHmacForeignFieldCreate)casHmacFFCreateAnn.getAnnotation()).foreignEntityManager()))
 								returnValue = false;
 					}
 					
 					else if (access.equals(CasHmacAccessLevels.READ)) {
 						AnnotationAndValue   casHmacFFReadAnn = getPropertyAnnotation(CasHmacForeignFieldRead.class,   field, entity, id, currentState, propertyNames);
 						if (returnValue && casHmacFFReadAnn != null) {
 							if (!getForeignAcl(((CasHmacForeignFieldRead)casHmacFFReadAnn.getAnnotation()).objectClass(), casHmacFFReadAnn.getValue(), ((CasHmacForeignFieldRead)casHmacFFReadAnn.getAnnotation()).accessLevel(), ((CasHmacForeignFieldRead)casHmacFFReadAnn.getAnnotation()).foreignEntityManager()))
 								returnValue = false;
 						}
 					}
 					
 					else if (access.equals(CasHmacAccessLevels.UPDATE)) {
 						AnnotationAndValue casHmacFFUpdatePreAnn = getPropertyAnnotation(CasHmacForeignFieldUpdate.class, field, entity, id, previousState, propertyNames);
 						AnnotationAndValue casHmacFFUpdateNowAnn = getPropertyAnnotation(CasHmacForeignFieldUpdate.class, field, entity, id, currentState, propertyNames);
 						if (returnValue && casHmacFFUpdatePreAnn != null) 
 							if (!getForeignAcl(((CasHmacForeignFieldUpdate)casHmacFFUpdatePreAnn.getAnnotation()).objectClass(), casHmacFFUpdatePreAnn.getValue(), ((CasHmacForeignFieldUpdate)casHmacFFUpdatePreAnn.getAnnotation()).accessLevel(), ((CasHmacForeignFieldUpdate)casHmacFFUpdatePreAnn.getAnnotation()).foreignEntityManager()))
 								returnValue = false;
 						if (returnValue && casHmacFFUpdateNowAnn != null) 
 							if (!getForeignAcl(((CasHmacForeignFieldUpdate)casHmacFFUpdateNowAnn.getAnnotation()).objectClass(), casHmacFFUpdateNowAnn.getValue(), ((CasHmacForeignFieldUpdate)casHmacFFUpdateNowAnn.getAnnotation()).accessLevel(), ((CasHmacForeignFieldUpdate)casHmacFFUpdateNowAnn.getAnnotation()).foreignEntityManager()))
 								returnValue = false;
 					}
 					
 					else if (access.equals(CasHmacAccessLevels.DELETE)) {
 						AnnotationAndValue casHmacFFDeleteAnn = getPropertyAnnotation(CasHmacForeignFieldDelete.class, field, entity, id, currentState, propertyNames);
 						if (returnValue && casHmacFFDeleteAnn != null)
 							if (!getForeignAcl(((CasHmacForeignFieldDelete)casHmacFFDeleteAnn.getAnnotation()).objectClass(), casHmacFFDeleteAnn.getValue(), ((CasHmacForeignFieldDelete)casHmacFFDeleteAnn.getAnnotation()).accessLevel(), ((CasHmacForeignFieldDelete)casHmacFFDeleteAnn.getAnnotation()).foreignEntityManager()))
 								returnValue = false;
 					}
 					
 				} // End if (returnValue)
 			}
 		}
 		
 		return returnValue;
 		
 		
 	}
 	
 	/**
 	 * Searches an entity for any @CasHmacForeignField**** annotations and returns
 	 * true if any are found.  Otherwise, returns false.
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	private Boolean needsEntityLoad(Object entity) {
 		for (Field field : entity.getClass().getDeclaredFields()) {
 			if (field.isAnnotationPresent(CasHmacForeignFieldRead.class))
 				return true;
 			else if (field.isAnnotationPresent(CasHmacForeignFieldUpdate.class))
 				return true;
 			else if (field.isAnnotationPresent(CasHmacForeignFieldDelete.class))
 				return true;
 			else if (field.isAnnotationPresent(CasHmacForeignFieldCreate.class))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Find annotations applied to object properties.  Be sure to use this method before the
 	 * findClassAcls method, as this method sets the casHmacPKField and casHmacWriteAcl fields
 	 * of this class, which are needed in the findClassAcls method.
 	 * @param entity
 	 * @param id
 	 * @param currentState
 	 * @param previousState
 	 * @param propertyNames
 	 */
 	private void findPropertyAcls(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames) {
 		/*
 		 * First, look at the entity's fields and attempt to gather relevant annotations and field
 		 * values from the private fields.
 		 */
 		for (Field field : entity.getClass().getDeclaredFields()) {
 			// If, for some reason, the next two annotations appear more than once, only use the first one.
 			if (casHmacPKFieldAnn == null) {
 				casHmacPKFieldAnn = getPropertyAnnotation(CasHmacPKField.class, field, entity, id, currentState, propertyNames);
 				if (casHmacPKFieldAnn != null) {
 					log.trace("============Found PK! - " + entity.getClass().getName() + ": " + casHmacPKFieldAnn.getValue());
 				}
 			}
 			
 			if (casHmacWriteAclAnn == null)
 				casHmacWriteAclAnn = getPropertyAnnotation(CasHmacWriteAcl.class, field, entity, id, currentState, propertyNames);
 			
 			// If both annotations have been found, we can escape this loop and continue.
 			if (casHmacPKFieldAnn != null && casHmacWriteAclAnn != null) {
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Looks up an ACL for a foreign field.  We do this by simply creating a new ACL
 	 * from the CrudAclSearchFactory.  This ensures that any cascading dependencies
 	 * are considered in order, and it ensures that the ACL is cached.
 	 * @param foreignEntity
 	 * @param accessLevel
 	 * @return
 	 */
 	private Boolean getForeignAcl(Class<?> foreignClass, Object foreignValue, String accessLevel, Class<? extends Annotation> entityManagerAnnotation) {
 		Boolean returnValue = true;
 		
 		if (foreignValue != null && foreignClass != null) {
 			
 			Object newEntity = null;
 			try {
 				newEntity = foreignClass.newInstance();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 
 			if (newEntity != null) {
 				log.trace("About to look up Foreign ACL for: " + foreignClass.getSimpleName() + " - " + accessLevel + ": " + foreignValue);
 				CrudAclSearch subAcl = crudAclSearchFactory.find(newEntity, foreignValue, accessLevel, entityManagerAnnotation);
 				log.trace("Looked up Foreign ACL for: " + foreignClass.getSimpleName() + " - " + accessLevel + ": " + foreignValue + " = " + subAcl.hasNeccessaryAcl);
 				if (! subAcl.getHasNeccessaryAcl())
 					returnValue = false;
 			}
 		}
 		
 		return returnValue;
 	}
 	
 	/**
 	 * Private class used to return both an annotation and the property value
 	 * of the field it annotates from the getPropertyAnnotation method.
 	 * @author JYODE1
 	 *
 	 */
 	private class AnnotationAndValue {
 		private final Annotation annotation;
 		private final Object value;
 		public AnnotationAndValue(Annotation annotation, Object value) {
 			this.annotation = annotation;
 			this.value = value;
 		}
 		public Annotation getAnnotation() {
 			return this.annotation;
 		}
 		public Object getValue() {
 			return this.value;
 		}
 	}
 
 	/**
 	 * Returns an annotation for a field.  It looks at the following items (in order) while attempting
 	 * to find the annotation:
 	 * 	1. Field
 	 *  2. Getter
 	 *  3. Setter
 	 * @param annotationClass
 	 * @param pd
 	 * @return
 	 */
 	private AnnotationAndValue getPropertyAnnotation(Class<? extends Annotation> annotationClass, Field field, Object entity, Object id, Object[] state, String[] propertyNames) {
 		Annotation annotation = field.getAnnotation(annotationClass);
 		field.setAccessible(true); // Make field accessible (if it's private)
 		if (annotation != null)
 			if (annotation instanceof CasHmacPKField && id != null)
 				return new AnnotationAndValue(annotation, id);
 			else
 				if (propertyNames != null && state != null)
 					return new AnnotationAndValue(annotation, getFieldValue(field.getName(), state, propertyNames));
 				else {
 					Object value = null;
 					try {
 						value = field.get(entity);
 					} catch (IllegalArgumentException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 					if (value != null)
 						return new AnnotationAndValue(annotation, value);
 					else
 						return null;
 				}
 						
 		else
 			return null;
 	}
 	
 	/**
 	 * Returns the value of a field
 	 * @param propertyName
 	 * @param state
 	 * @param PropertyNames
 	 * @return
 	 */
 	private Object getFieldValue(String propertyName, Object[] state, String[] propertyNames) {
 		Object retval = null;
 		
 		for (int i=0; i<propertyNames.length; i++) {
 			Object property = propertyNames[i];
 			if (property.equals(propertyName)) {
 				retval = state[i];
 				break;
 			}
 		}
 		
 		return retval;
 	}
 
 }
