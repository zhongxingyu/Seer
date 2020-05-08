 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  ******************************************************************************/
 package org.sociotech.communitymashup.data.impl;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.UniqueEList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EParameter;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
 import org.eclipse.emf.query.conditions.eobjects.EObjectTypeRelationCondition;
 import org.eclipse.emf.query.conditions.eobjects.structuralfeatures.EObjectAttributeValueCondition;
 import org.eclipse.emf.query.conditions.strings.StringValue;
 import org.eclipse.emf.query.ocl.conditions.BooleanOCLCondition;
 import org.eclipse.emf.query.statements.IQueryResult;
 import org.eclipse.ocl.Environment;
 import org.eclipse.ocl.ParserException;
 import org.eclipse.ocl.ecore.CallOperationAction;
 import org.eclipse.ocl.ecore.Constraint;
 import org.eclipse.ocl.ecore.EcoreEnvironment;
 import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
 import org.eclipse.ocl.ecore.SendSignalAction;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.Mashup;
 import org.sociotech.communitymashup.data.Attachment;
 import org.sociotech.communitymashup.data.Binary;
 import org.sociotech.communitymashup.data.Category;
 import org.sociotech.communitymashup.data.Classification;
 import org.sociotech.communitymashup.data.Connection;
 import org.sociotech.communitymashup.data.Content;
 import org.sociotech.communitymashup.data.DataFactory;
 import org.sociotech.communitymashup.data.DataPackage;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.Document;
 import org.sociotech.communitymashup.data.Email;
 import org.sociotech.communitymashup.data.Event;
 import org.sociotech.communitymashup.data.Extension;
 import org.sociotech.communitymashup.data.Identifier;
 import org.sociotech.communitymashup.data.Image;
 import org.sociotech.communitymashup.data.IndoorLocation;
 import org.sociotech.communitymashup.data.InformationObject;
 import org.sociotech.communitymashup.data.InstantMessenger;
 import org.sociotech.communitymashup.data.Item;
 import org.sociotech.communitymashup.data.Location;
 import org.sociotech.communitymashup.data.MetaInformation;
 import org.sociotech.communitymashup.data.MetaTag;
 import org.sociotech.communitymashup.data.Organisation;
 import org.sociotech.communitymashup.data.Person;
 import org.sociotech.communitymashup.data.Phone;
 import org.sociotech.communitymashup.data.Ranking;
 import org.sociotech.communitymashup.data.StarRanking;
 import org.sociotech.communitymashup.data.Tag;
 import org.sociotech.communitymashup.data.ThumbRanking;
 import org.sociotech.communitymashup.data.Transformation;
 import org.sociotech.communitymashup.data.Video;
 import org.sociotech.communitymashup.data.ViewRanking;
 import org.sociotech.communitymashup.data.WebAccount;
 import org.sociotech.communitymashup.data.WebSite;
 import org.sociotech.communitymashup.rest.ArgNotFoundException;
 import org.sociotech.communitymashup.rest.RequestType;
 import org.sociotech.communitymashup.rest.RestCommand;
 import org.sociotech.communitymashup.rest.RestUtil;
 import org.sociotech.communitymashup.rest.UnknownOperationException;
 import org.sociotech.communitymashup.rest.WrongArgCountException;
 import org.sociotech.communitymashup.rest.WrongArgException;
 
 
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Set</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.sociotech.communitymashup.data.impl.DataSetImpl#getItems <em>Items</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.DataSetImpl#getCacheFolder <em>Cache Folder</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.DataSetImpl#getCacheFileAttachements <em>Cache File Attachements</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.DataSetImpl#getSetUp <em>Set Up</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.DataSetImpl#getLastModified <em>Last Modified</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.DataSetImpl#getLogLevel <em>Log Level</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class DataSetImpl extends EObjectImpl implements DataSet {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\nContributors:\n \tPeter Lachenmaier - Design and initial implementation";
 	/**
 	 * Reference to singleton ocl environment. 
 	 */
 	private static EcoreEnvironment oclEnvironment = null;
 	private static long identCounter = 0;
 	private static final String idPrefix = "a_";
 	
 	/**
 	 * Log level, by default only warnings will be logged 
 	 */
 	private int logLevel = LogService.LOG_WARNING;
 	
 	/**
 	 * Creates the OCL Environment at first call and then returns the singleton instance.
 	 * 
 	 * @return The OCL singleton instance, null in error case.
 	 */
 	public static Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> getOclEnvironment() {
 		if(oclEnvironment == null)
 		{
 			oclEnvironment = (EcoreEnvironment) EcoreEnvironmentFactory.INSTANCE.createEnvironment();
 		}
 		return oclEnvironment;
 	}
 	
 	/**
 	 * The cached value of the '{@link #getItems() <em>Items</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getItems()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Item> items;
 
 	/**
 	 * The default value of the '{@link #getCacheFolder() <em>Cache Folder</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCacheFolder()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CACHE_FOLDER_EDEFAULT = null;
 	
 	/**
 	 * The path of the cache folder
 	 */
 	protected String cacheFolder = CACHE_FOLDER_EDEFAULT;
 	
 	/**
 	 * The default value of the '{@link #getCacheFileAttachements() <em>Cache File Attachements</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCacheFileAttachements()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Boolean CACHE_FILE_ATTACHEMENTS_EDEFAULT = Boolean.FALSE;
 
 	
 	/**
 	 * The cached value of the '{@link #getSetUp() <em>Set Up</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSetUp()
 	 * @generated
 	 * @ordered
 	 */
 	protected Mashup setUp;
 
 	/**
 	 * The default value of the '{@link #getLastModified() <em>Last Modified</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLastModified()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Date LAST_MODIFIED_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLastModified() <em>Last Modified</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLastModified()
 	 * @generated
 	 * @ordered
 	 */
 	protected Date lastModified = LAST_MODIFIED_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getLogLevel() <em>Log Level</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLogLevel()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Integer LOG_LEVEL_EDEFAULT = null;
 	
 	/**
 	 * True means that file attachment caching is on.
 	 */
 	protected boolean cacheFileAttachments = CACHE_FILE_ATTACHEMENTS_EDEFAULT;
 	
 	/**
 	 * Log service to be used for logging
 	 */
 	private LogService logService; 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DataSetImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return DataPackage.Literals.DATA_SET;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Item> getItems() {
 		if (items == null) {
 			items = new EObjectContainmentWithInverseEList<Item>(Item.class, this, DataPackage.DATA_SET__ITEMS, DataPackage.ITEM__DATA_SET);
 		}
 		return items;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public String getCacheFolder() {
 		return cacheFolder;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Sets the cache folder to the given new path.
 	 * <!-- end-user-doc -->
 	 */
 	public void setCacheFolder(String newCacheFolder) {	
 		cacheFolder = newCacheFolder;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 */
 	public Boolean getCacheFileAttachements() {
 		return cacheFileAttachments;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 */
 	public void setCacheFileAttachements(Boolean newCacheFileAttachements) {
 		cacheFileAttachments = newCacheFileAttachements;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Mashup getSetUp() {
 		if (setUp != null && setUp.eIsProxy()) {
 			InternalEObject oldSetUp = (InternalEObject)setUp;
 			setUp = (Mashup)eResolveProxy(oldSetUp);
 			if (setUp != oldSetUp) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DataPackage.DATA_SET__SET_UP, oldSetUp, setUp));
 			}
 		}
 		return setUp;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Mashup basicGetSetUp() {
 		return setUp;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSetUp(Mashup newSetUp) {
 		Mashup oldSetUp = setUp;
 		setUp = newSetUp;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.DATA_SET__SET_UP, oldSetUp, setUp));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Date getLastModified() {
 		return lastModified;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLastModified(Date newLastModified) {
 		Date oldLastModified = lastModified;
 		lastModified = newLastModified;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.DATA_SET__LAST_MODIFIED, oldLastModified, lastModified));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Integer getLogLevel() {
 		return this.logLevel;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public void setLogLevel(Integer newLogLevel) {
 		this.logLevel = newLogLevel;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns all items with the given String value.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Item> getItemsWithStringValue(String stringValue) {
 		if(stringValue == null)
 		{
 			return null;
 		}
 		
 		// create case sensitive string comparison condition
 		StringValue stringValueCondition = new StringValue(stringValue, true);
 		
 		// TODO: check if always the getStringValue() method gets called
 		EObjectAttributeValueCondition valueEqualCondition = new EObjectAttributeValueCondition(DataPackage.eINSTANCE.getItem_StringValue(), stringValueCondition);
 		
 		// query results matching condition
 		IQueryResult result = getItemsMatchingCondition(valueEqualCondition);
 		
 		// results are only items
 		@SuppressWarnings("unchecked")
 		EList<Item> items = new BasicEList<Item>((Collection<? extends Item>) result.getEObjects());
 		
 		return items;
 	}
 
 	/**
 	 * Queries the contained Items and returns all tags.
 	 * 
 	 * @return A list of all tags. Null in error case.
 	 */
 	public EList<Tag> getTags() {
 		IQueryResult result = getItemsOfType(DataPackage.eINSTANCE.getTag());
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only tags
 		@SuppressWarnings("unchecked")
 		EList<Tag> tags = new BasicEList<Tag>((Collection<? extends Tag>) result.getEObjects());
 		
 		return tags;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<MetaTag> getMetaTags() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getMetaTag());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(MetaTag.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<MetaTag>();
 		}
 		
 		// results are only MetaTags
 		@SuppressWarnings("unchecked")
 		EList<MetaTag> objects = new BasicEList<MetaTag>((Collection<? extends MetaTag>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Binary> getBinaries() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getBinary());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Binary.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Binary>();
 		}
 		
 		// results are only Binarys
 		@SuppressWarnings("unchecked")
 		EList<Binary> objects = new BasicEList<Binary>((Collection<? extends Binary>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case DataPackage.DATA_SET__ITEMS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getItems()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case DataPackage.DATA_SET__ITEMS:
 				return ((InternalEList<?>)getItems()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case DataPackage.DATA_SET__ITEMS:
 				return getItems();
 			case DataPackage.DATA_SET__CACHE_FOLDER:
 				return getCacheFolder();
 			case DataPackage.DATA_SET__CACHE_FILE_ATTACHEMENTS:
 				return getCacheFileAttachements();
 			case DataPackage.DATA_SET__SET_UP:
 				if (resolve) return getSetUp();
 				return basicGetSetUp();
 			case DataPackage.DATA_SET__LAST_MODIFIED:
 				return getLastModified();
 			case DataPackage.DATA_SET__LOG_LEVEL:
 				return getLogLevel();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case DataPackage.DATA_SET__ITEMS:
 				getItems().clear();
 				getItems().addAll((Collection<? extends Item>)newValue);
 				return;
 			case DataPackage.DATA_SET__CACHE_FOLDER:
 				setCacheFolder((String)newValue);
 				return;
 			case DataPackage.DATA_SET__CACHE_FILE_ATTACHEMENTS:
 				setCacheFileAttachements((Boolean)newValue);
 				return;
 			case DataPackage.DATA_SET__SET_UP:
 				setSetUp((Mashup)newValue);
 				return;
 			case DataPackage.DATA_SET__LAST_MODIFIED:
 				setLastModified((Date)newValue);
 				return;
 			case DataPackage.DATA_SET__LOG_LEVEL:
 				setLogLevel((Integer)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case DataPackage.DATA_SET__ITEMS:
 				getItems().clear();
 				return;
 			case DataPackage.DATA_SET__CACHE_FOLDER:
 				setCacheFolder(CACHE_FOLDER_EDEFAULT);
 				return;
 			case DataPackage.DATA_SET__CACHE_FILE_ATTACHEMENTS:
 				setCacheFileAttachements(CACHE_FILE_ATTACHEMENTS_EDEFAULT);
 				return;
 			case DataPackage.DATA_SET__SET_UP:
 				setSetUp((Mashup)null);
 				return;
 			case DataPackage.DATA_SET__LAST_MODIFIED:
 				setLastModified(LAST_MODIFIED_EDEFAULT);
 				return;
 			case DataPackage.DATA_SET__LOG_LEVEL:
 				setLogLevel(LOG_LEVEL_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case DataPackage.DATA_SET__ITEMS:
 				return items != null && !items.isEmpty();
 			case DataPackage.DATA_SET__CACHE_FOLDER:
 				return CACHE_FOLDER_EDEFAULT == null ? getCacheFolder() != null : !CACHE_FOLDER_EDEFAULT.equals(getCacheFolder());
 			case DataPackage.DATA_SET__CACHE_FILE_ATTACHEMENTS:
 				return CACHE_FILE_ATTACHEMENTS_EDEFAULT == null ? getCacheFileAttachements() != null : !CACHE_FILE_ATTACHEMENTS_EDEFAULT.equals(getCacheFileAttachements());
 			case DataPackage.DATA_SET__SET_UP:
 				return setUp != null;
 			case DataPackage.DATA_SET__LAST_MODIFIED:
 				return LAST_MODIFIED_EDEFAULT == null ? lastModified != null : !LAST_MODIFIED_EDEFAULT.equals(lastModified);
 			case DataPackage.DATA_SET__LOG_LEVEL:
 				return LOG_LEVEL_EDEFAULT == null ? getLogLevel() != null : !LOG_LEVEL_EDEFAULT.equals(getLogLevel());
 		}
 		return super.eIsSet(featureID);
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (lastModified: ");
 		result.append(lastModified);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * Returns all Persons matching the provided condition.
 	 *
 	 * @condition The condition to filter the Persons.
 	 * @return All Persons of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Person> getPersonsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Person.isTypeCondition.AND(condition));
 		EList<Person> resList = new BasicEList<Person>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Person) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all InformationObjects matching the provided condition.
 	 *
 	 * @condition The condition to filter the InformationObjects.
 	 * @return All InformationObjects of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<InformationObject> getInformationObjectsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(InformationObject.isTypeCondition.AND(condition));
 		EList<InformationObject> resList = new BasicEList<InformationObject>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((InformationObject) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Contents matching the provided condition.
 	 *
 	 * @condition The condition to filter the Contents.
 	 * @return All Contents of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Content> getContentsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Content.isTypeCondition.AND(condition));
 		EList<Content> resList = new BasicEList<Content>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Content) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Extensions.
 	 *
 	 * @return All Extensions of this DataSet.
 	 * @generated
 	 */
 	public EList<Extension> getExtensions() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getExtension());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Extension.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Extension>();
 		}
 		
 		// results are only Extensions
 		@SuppressWarnings("unchecked")
 		EList<Extension> objects = new BasicEList<Extension>((Collection<? extends Extension>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Image> getImages() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getImage());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Image.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Image>();
 		}
 		
 		// results are only Images
 		@SuppressWarnings("unchecked")
 		EList<Image> objects = new BasicEList<Image>((Collection<? extends Image>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Extensions matching the provided condition.
 	 *
 	 * @condition The condition to filter the Extensions.
 	 * @return All Extensions of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Extension> getExtensionsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Extension.isTypeCondition.AND(condition));
 		EList<Extension> resList = new BasicEList<Extension>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Extension) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Classifications.
 	 *
 	 * @return All Classifications of this DataSet.
 	 * @generated
 	 */
 	public EList<Classification> getClassifications() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getClassification());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Classification.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Classification>();
 		}
 		
 		// results are only Classifications
 		@SuppressWarnings("unchecked")
 		EList<Classification> objects = new BasicEList<Classification>((Collection<? extends Classification>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Connection> getConnections() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getConnection());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Connection.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Connection>();
 		}
 		
 		// results are only Connections
 		@SuppressWarnings("unchecked")
 		EList<Connection> objects = new BasicEList<Connection>((Collection<? extends Connection>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Document> getDocuments() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getDocument());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Document.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Document>();
 		}
 		
 		// results are only Documents
 		@SuppressWarnings("unchecked")
 		EList<Document> objects = new BasicEList<Document>((Collection<? extends Document>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Classifications matching the provided condition.
 	 *
 	 * @condition The condition to filter the Classifications.
 	 * @return All Classifications of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Classification> getClassificationsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Classification.isTypeCondition.AND(condition));
 		EList<Classification> resList = new BasicEList<Classification>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Classification) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Tags matching the provided condition.
 	 *
 	 * @condition The condition to filter the Tags.
 	 * @return All Tags of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Tag> getTagsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Tag.isTypeCondition.AND(condition));
 		EList<Tag> resList = new BasicEList<Tag>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Tag) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Organisations matching the provided condition.
 	 *
 	 * @condition The condition to filter the Organisations.
 	 * @return All Organisations of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Organisation> getOrganisationsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Organisation.isTypeCondition.AND(condition));
 		EList<Organisation> resList = new BasicEList<Organisation>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Organisation) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all MetaTags matching the provided condition.
 	 *
 	 * @condition The condition to filter the MetaTags.
 	 * @return All MetaTags of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<MetaTag> getMetaTagsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(MetaTag.isTypeCondition.AND(condition));
 		EList<MetaTag> resList = new BasicEList<MetaTag>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((MetaTag) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Phones.
 	 *
 	 * @return All Phones of this DataSet.
 	 * @generated
 	 */
 	public EList<Phone> getPhones() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getPhone());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Phone.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Phone>();
 		}
 		
 		// results are only Phones
 		@SuppressWarnings("unchecked")
 		EList<Phone> objects = new BasicEList<Phone>((Collection<? extends Phone>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Phones matching the provided condition.
 	 *
 	 * @condition The condition to filter the Phones.
 	 * @return All Phones of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Phone> getPhonesMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Phone.isTypeCondition.AND(condition));
 		EList<Phone> resList = new BasicEList<Phone>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Phone) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all InstantMessengers.
 	 *
 	 * @return All InstantMessengers of this DataSet.
 	 * @generated
 	 */
 	public EList<InstantMessenger> getInstantMessengers() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getInstantMessenger());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(InstantMessenger.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<InstantMessenger>();
 		}
 		
 		// results are only InstantMessengers
 		@SuppressWarnings("unchecked")
 		EList<InstantMessenger> objects = new BasicEList<InstantMessenger>((Collection<? extends InstantMessenger>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all InstantMessengers matching the provided condition.
 	 *
 	 * @condition The condition to filter the InstantMessengers.
 	 * @return All InstantMessengers of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<InstantMessenger> getInstantMessengersMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(InstantMessenger.isTypeCondition.AND(condition));
 		EList<InstantMessenger> resList = new BasicEList<InstantMessenger>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((InstantMessenger) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Emails.
 	 *
 	 * @return All Emails of this DataSet.
 	 * @generated
 	 */
 	public EList<Email> getEmails() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getEmail());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Email.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Email>();
 		}
 		
 		// results are only Emails
 		@SuppressWarnings("unchecked")
 		EList<Email> objects = new BasicEList<Email>((Collection<? extends Email>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Emails matching the provided condition.
 	 *
 	 * @condition The condition to filter the Emails.
 	 * @return All Emails of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Email> getEmailsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Email.isTypeCondition.AND(condition));
 		EList<Email> resList = new BasicEList<Email>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Email) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all WebAccounts.
 	 *
 	 * @return All WebAccounts of this DataSet.
 	 * @generated
 	 */
 	public EList<WebAccount> getWebAccounts() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getWebAccount());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(WebAccount.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<WebAccount>();
 		}
 		
 		// results are only WebAccounts
 		@SuppressWarnings("unchecked")
 		EList<WebAccount> objects = new BasicEList<WebAccount>((Collection<? extends WebAccount>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all WebAccounts matching the provided condition.
 	 *
 	 * @condition The condition to filter the WebAccounts.
 	 * @return All WebAccounts of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<WebAccount> getWebAccountsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(WebAccount.isTypeCondition.AND(condition));
 		EList<WebAccount> resList = new BasicEList<WebAccount>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((WebAccount) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all WebSites.
 	 *
 	 * @return All WebSites of this DataSet.
 	 * @generated
 	 */
 	public EList<WebSite> getWebSites() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getWebSite());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(WebSite.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<WebSite>();
 		}
 		
 		// results are only WebSites
 		@SuppressWarnings("unchecked")
 		EList<WebSite> objects = new BasicEList<WebSite>((Collection<? extends WebSite>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<InformationObject> getInformationObjectsWithAllMetaTags(EList<MetaTag> tags) {
 		// Check if input is defined
 		if(getItemsWithOneOfMetaTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getInformationObject());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItemsWithOneOfMetaTags(tags), oclCondition.AND(InformationObject.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<InformationObject>();
 		}
 		
 		// results are only InformationObjects
 		@SuppressWarnings("unchecked")
 		EList<InformationObject> objects = new BasicEList<InformationObject>((Collection<? extends InformationObject>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<InformationObject> getInformationObjectsWithOneOfMetaTags(EList<MetaTag> tags) {
 		// Check if input is defined
 		if(getItemsWithOneOfMetaTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getInformationObject());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItemsWithOneOfMetaTags(tags), oclCondition.AND(InformationObject.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<InformationObject>();
 		}
 		
 		// results are only InformationObjects
 		@SuppressWarnings("unchecked")
 		EList<InformationObject> objects = new BasicEList<InformationObject>((Collection<? extends InformationObject>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Identifier> getIdentifiers() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getIdentifier());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Identifier.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Identifier>();
 		}
 		
 		// results are only Identifiers
 		@SuppressWarnings("unchecked")
 		EList<Identifier> objects = new BasicEList<Identifier>((Collection<? extends Identifier>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Identifier> getIdentifiersWithKey(String key) {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.key='" + key + "'";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getIdentifier());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Identifier.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Identifier>();
 		}
 		
 		// results are only Identifiers
 		@SuppressWarnings("unchecked")
 		EList<Identifier> objects = new BasicEList<Identifier>((Collection<? extends Identifier>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Identifier getIdentifierWithKeyValue(String key, String value) {
 		
 		if(key == null || value == null)
 		{
 			return null;
 		}
 		
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.key='" + key + "' and self.value='" + value + "'";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getIdentifier());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement, LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Identifier.isTypeCondition));
 
 		if(result == null) {
 			return null;
 		}
 		
 		// results are only Identifiers
 		@SuppressWarnings("unchecked")
 		EList<Identifier> objects = new BasicEList<Identifier>((Collection<? extends Identifier>) result.getEObjects());
 		
 		if(objects.isEmpty())
 		{
 			return null;
 		}
 		
 		// return first one
 		// there should be only one
 		return objects.get(0);	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Item getItemWithIdentifier(String key, String value) {
 		
 		Identifier identifier = this.getIdentifierWithKeyValue(key, value);
 		
 		if(identifier == null)
 		{
 			return null;
 		}
 		
 		return identifier.getIdentified();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Person getPersonWithIdentifier(String key, String value) {
 		Item item = getItemWithIdentifier(key, value);
 
 		if(item instanceof Person)
 		{
 			return (Person) item;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Content getContentWithIdentifier(String key, String value) {
 		Item item = getItemWithIdentifier(key, value);
 
 		if(item instanceof Content)
 		{
 			return (Content) item;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Organisation getOrganisationWithIdentifier(String key, String value) {
 		Item item = getItemWithIdentifier(key, value);
 
 		if(item instanceof Organisation)
 		{
 			return (Organisation) item;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Location getLocationWithIdentifier(String key, String value) {
 		Item item = getItemWithIdentifier(key, value);
 
 		if(item instanceof Location)
 		{
 			return (Location) item;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public IndoorLocation getIndoorLocationWithIdentifier(String key, String value) {
 		Item item = getItemWithIdentifier(key, value);
 
 		if(item instanceof IndoorLocation)
 		{
 			return (IndoorLocation) item;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Image getImageWithIdentifier(String key, String value) {
 		Item item = getItemWithIdentifier(key, value);
 
 		if(item instanceof Image)
 		{
 			return (Image) item;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Item getEmptyItemWithIdent(String ident) {
 		
 		Item origItem = this.getItemsWithIdent(ident);
 		
 		if(origItem == null)
 		{
 			return null;
 		}
 		
 		// create empty item of the same type
 		Item emptyItem = (Item) DataFactory.eINSTANCE.create(origItem.eClass());
 		emptyItem.setIdent(ident);
 		
 		return emptyItem;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Tag> getTagsWithMoreThanXInformationObjects(Integer x) {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.getCount() > " + x;
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getTag());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Tag.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Tag>();
 		}
 		
 		// results are only Tags
 		@SuppressWarnings("unchecked")
 		EList<Tag> objects = new BasicEList<Tag>((Collection<? extends Tag>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Item> getItemsCreatedSince(Date date) {
 		if(date == null)
 		{
 			return null;
 		}	
 		
 		EList<Item> results = new BasicEList<Item>();
 		
 		EList<Item> itemList = this.getItems();
 		
 		// check creation date of all items
 		// TODO create more performant query with EMF Query or OCL
 		for(Item item : itemList)
 		{
 			Date creationDate = item.getCreated();
 			
 			if(creationDate != null && creationDate.after(date))
 			{
 				results.add(item);
 			}
 		}
 		
 		return results;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Item> searchItems(String term) {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.matchesSearch('" + term + "') = true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getItem());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Item.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Item>();
 		}
 		
 		// results are only Items
 		@SuppressWarnings("unchecked")
 		EList<Item> objects = new BasicEList<Item>((Collection<? extends Item>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<InformationObject> searchInformationObjects(String term) {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.matchesSearch('" + term + "') = true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getInformationObject());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(InformationObject.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<InformationObject>();
 		}
 		
 		// results are only InformationObjects
 		@SuppressWarnings("unchecked")
 		EList<InformationObject> objects = new BasicEList<InformationObject>((Collection<? extends InformationObject>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<InformationObject> getInformationObjectsWithAttachment(Attachment attachment) {
 		// Check if input is defined
 		if(getInformationObjects() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "((self.getAttachments())->notEmpty()) and (self.getAttachments()->exists(a | a.ident = '" + attachment.getIdent()  + "'))";;
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getInformationObject());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjects(), oclCondition.AND(InformationObject.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<InformationObject>();
 		}
 		
 		// results are only InformationObjects
 		@SuppressWarnings("unchecked")
 		EList<InformationObject> objects = new BasicEList<InformationObject>((Collection<? extends InformationObject>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Person> getPersonsWithAttachment(Attachment attachment) {
 		// Check if input is defined
 		if(getInformationObjectsWithAttachment(attachment) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getPerson());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAttachment(attachment), oclCondition.AND(Person.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Person>();
 		}
 		
 		// results are only Persons
 		@SuppressWarnings("unchecked")
 		EList<Person> objects = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Organisation> getOrganisationsWithAttachment(Attachment attachment) {
 		// Check if input is defined
 		if(getInformationObjectsWithAttachment(attachment) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getOrganisation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAttachment(attachment), oclCondition.AND(Organisation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Organisation>();
 		}
 		
 		// results are only Organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> objects = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Content> getContentsWithAttachment(Attachment attachment) {
 		// Check if input is defined
 		if(getInformationObjectsWithAttachment(attachment) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getContent());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAttachment(attachment), oclCondition.AND(Content.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Content>();
 		}
 		
 		// results are only Contents
 		@SuppressWarnings("unchecked")
 		EList<Content> objects = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Item getEqualItem(Item item) {
 		if(item == null)
 		{
 			return null;
 		}
 		
 		EList<Item> allItems = this.getItems();
 		
 		// run over all items
 		// TODO room for efficiency
 		for(Item currentItem : allItems)
 		{
 			if(currentItem.isEqualItem(item))
 			{
 				return currentItem;
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Boolean hasEqualItem(Item item) {
 		return getEqualItem(item) != null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Item getItemsWithIdent(String ident) {
 		for (Item  item: this.getItems()) {
 			if (item != null && item.getIdent() != null && item.getIdent().equals(ident)) return item;
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Attachment> getAttachmentsWithCachedFileName(String cachedFileName) {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.cachedFileName = '" + cachedFileName + "'";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getAttachment());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Attachment.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Attachment>();
 		}
 		
 		// results are only Attachments
 		@SuppressWarnings("unchecked")
 		EList<Attachment> objects = new BasicEList<Attachment>((Collection<? extends Attachment>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Event> getEventsAfter(Date date) {
 		if(date == null)
 		{
 			return null;
 		}
 		
 		EList<Event> allEvents = getEvents();
 		
 		EList<Event> result = new BasicEList<Event>();
 		
 		// step over all event and look if they are after the given date
 		for(Event event : allEvents) {
 			if(event.getDate() != null && event.getDate().after(date)) {
 				result.add(event);
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Event> getEventsBefore(Date date) {
 		if(date == null)
 		{
 			return null;
 		}
 		
 		EList<Event> allEvents = getEvents();
 		
 		EList<Event> result = new BasicEList<Event>();
 		
 		// step over all event and look if they are before the given date
 		for(Event event : allEvents) {
 			if(event.getDate() != null && event.getDate().after(date)) {
 				result.add(event);
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Event> getEventsBetweenDates(Date after, Date before) {
 		if(after == null)
 		{
 			return getEventsBefore(before);
 		}
 		
 		if(before == null)
 		{
 			return getEventsAfter(after);
 		}
 		
 		EList<Event> allEvents = getEvents();
 		
 		EList<Event> result = new BasicEList<Event>();
 		
 		// step over all event and look if they are between the given dates
 		for(Event event : allEvents) {
 			if(event.getDate() != null && event.getDate().after(after) && event.getDate().before(before)) {
 				result.add(event);
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public String getSpicynodesRepresentationOLD() {
 		
 		// TODO this method is only for testing and should be moved to an seperate interface with parameters
 		
 		StringBuffer representation = new StringBuffer();
 		
 		// get line separator
 		String ls = System.getProperty("line.separator");
 		
 		// add central (dataset) node
 		representation.append("+Home" + ls);
 		
 		// ...  and all tags
 		EList<Tag> allTags = this.getTags();
 		if(allTags != null && !allTags.isEmpty())
 		{
 			// add Tag node
 			representation.append("++Tags::All tags" + ls);
 			for(Tag tag : allTags)
 			{
 				// tag name
 				representation.append("+++" + tag.getName());
 				// tag ident for linking
 				representation.append("@@" + tag.getIdent());
 				// and links to all tagged information objects
 				EList<InformationObject> taggedInforamtionObjects = tag.getTagged();
 				if(taggedInforamtionObjects == null || taggedInforamtionObjects.isEmpty())
 				{
 					// add new line
 					representation.append(ls);
 					// nothing to link
 					continue;
 				}
 				// link all information objects
 				for(InformationObject io : taggedInforamtionObjects)
 				{
 					// add link
 					representation.append(">>" + io.getIdent());
 				}
 				// add new line
 				representation.append(ls);
 			}
 		}
 		
 		
 		// ...  and all categories
 		EList<Category> allCategories = this.getAllCategories();
 		if(allCategories != null && !allCategories.isEmpty())
 		{
 			// add Category node
 			representation.append("++Categories::All categories" + ls);
 			for(Category category : allCategories)
 			{
 				// category name
 				representation.append("+++" + category.getName());
 				// category ident for linking
 				representation.append("@@" + category.getIdent());
 				// and links to all categorized information objects
 				EList<InformationObject> categorizedInforamtionObjects = category.getCategorized();
 				if(categorizedInforamtionObjects == null || categorizedInforamtionObjects.isEmpty())
 				{
 					// add new line
 					representation.append(ls);
 					// nothing to link
 					continue;
 				}
 				// link all information objects
 				for(InformationObject io : categorizedInforamtionObjects)
 				{
 					// add link
 					representation.append(">>" + io.getIdent());
 				}
 				
 				// links to sub categories
 				EList<Category> subCategories = category.getCategories();
 				if(subCategories != null && !subCategories.isEmpty())
 				{	
 					for(Category subCategory : subCategories)
 					{
 						// add link
 						representation.append(">>" + subCategory.getIdent());
 					}
 				}
 				// add new line
 				representation.append(ls);
 			}
 		}
 		
 		// ...  and all persons
 		EList<Person> allPersons = this.getPersons();
 		if(allPersons != null && !allPersons.isEmpty())
 		{
 			// add Person node
 			representation.append("++Persons::All persons" + ls);
 			for(Person person : allPersons)
 			{
 				// person name
 				representation.append("+++" + person.getName());
 				// TODO: additional person informations
 				// person ident for linking
 				representation.append("@@" + person.getIdent());
 				
 				// links to other persons
 				// TODO add persons over connections
 				EList<Person> linkedPersons = person.getPersons();
 				if(linkedPersons != null && !linkedPersons.isEmpty())
 				{
 					// link all persons
 					for(Person linkedPerson : linkedPersons)
 					{
 						// add link
 						representation.append(">>" + linkedPerson.getIdent());
 					}
 				}
 				
 				// links to authored content
 				EList<Content> authoredContent = person.getAuthored();
 				if(authoredContent != null && !authoredContent.isEmpty())
 				{
 					// link all contents
 					for(Content linkedContent : authoredContent)
 					{
 						// add link
 						representation.append(">>" + linkedContent.getIdent());
 					}
 				}
 				
 				// links to contributed content
 				EList<Content> contributedContent = person.getContributed();
 				if(contributedContent != null && !contributedContent.isEmpty())
 				{
 					// link all contents
 					for(Content linkedContent : contributedContent)
 					{
 						// add link
 						representation.append(">>" + linkedContent.getIdent());
 					}
 				}
 				
 				// links to categories
 				EList<Category> categories = person.getCategories();
 				if(categories != null && !categories.isEmpty())
 				{
 					// link all categories
 					for(Category category : categories)
 					{
 						// add link
 						representation.append(">>" + category.getIdent());
 					}
 				}
 				
 				// links to tags
 				EList<Tag> tags = person.getTags();
 				if(tags != null && !tags.isEmpty())
 				{
 					// link all tags
 					for(Tag tag : tags)
 					{
 						// add link
 						representation.append(">>" + tag.getIdent());
 					}
 				}
 				
 				// links to organisations
 				EList<Organisation> organisations = person.getOrganisations();
 				if(organisations != null && !tags.isEmpty())
 				{
 					// link all organisations
 					for(Organisation org : organisations)
 					{
 						// add link
 						representation.append(">>" + org.getIdent());
 					}
 				}
 				
 				// add new line
 				representation.append(ls);
 			}
 		}
 		
 		// ...  and all organisations
 		EList<Organisation> allOrganisations = this.getAllOrganisations();
 		if(allOrganisations != null && !allOrganisations.isEmpty())
 		{
 			// add Organisation node
 			representation.append("++Organizations::All organizations" + ls);
 			for(Organisation organisation : allOrganisations)
 			{
 				// organisation name
 				representation.append("+++" + organisation.getName());
 				// TODO: additional organisation informations
 				// person ident for linking
 				representation.append("@@" + organisation.getIdent());
 				
 				// links to persons
 				// TODO decide between leader and participants
 				// TODO add persons over connections
 				EList<Person> linkedPersons = organisation.getPersons();
 				if(linkedPersons != null && !linkedPersons.isEmpty())
 				{
 					// link all persons
 					for(Person linkedPerson : linkedPersons)
 					{
 						// add link
 						representation.append(">>" + linkedPerson.getIdent());
 					}
 				}
 				
 				// links to authored and contributed content of organisation members
 				EList<Content> authoredContent = organisation.getContents();
 				if(authoredContent != null && !authoredContent.isEmpty())
 				{
 					// link all contents
 					for(Content linkedContent : authoredContent)
 					{
 						// add link
 						representation.append(">>" + linkedContent.getIdent());
 					}
 				}
 				
 				// links to categories
 				EList<Category> categories = organisation.getCategories();
 				if(categories != null && !categories.isEmpty())
 				{
 					// link all categories
 					for(Category category : categories)
 					{
 						// add link
 						representation.append(">>" + category.getIdent());
 					}
 				}
 				
 				// links to tags
 				EList<Tag> tags = organisation.getTags();
 				if(tags != null && !tags.isEmpty())
 				{
 					// link all tags
 					for(Tag tag : tags)
 					{
 						// add link
 						representation.append(">>" + tag.getIdent());
 					}
 				}
 				
 				// links to sub organisations
 				EList<Organisation> organisations = organisation.getOrganisations();
 				if(organisations != null && !tags.isEmpty())
 				{
 					// link all organisations
 					for(Organisation org : organisations)
 					{
 						// add link
 						representation.append(">>" + org.getIdent());
 					}
 				}
 				
 				// add new line
 				representation.append(ls);
 			}
 		}
 		
 		// ...  and main contens
 		EList<Content> mainContents = this.getAllContents();
 		if(mainContents != null && !mainContents.isEmpty())
 		{
 			// add Content node
 			representation.append("++Contents::All contents" + ls);
 			for(Content content : mainContents)
 			{
 				// name must be available
 				if(content.getName() == null || content.getName().isEmpty()) continue;
 				
 				// content name
 				representation.append("+++" + content.getName().replaceAll("\\W", " "));
 				// content ident for linking
 				representation.append("@@" + content.getIdent());
 				
 				// content value
 //				String value = content.getStringValue();
 //				if(value != null && !value.isEmpty())
 //				{
 //					// add new line
 //					representation.append(ls);
 //					representation.append("::" + value + "");
 //					// add new line
 //					representation.append(ls);
 //				}
 				
 				// links to persons
 				// TODO decide between author and contributors
 				// TODO add persons over connections
 				EList<Person> linkedPersons = content.getPersons();
 				if(linkedPersons != null && !linkedPersons.isEmpty())
 				{
 					// link all persons
 					for(Person linkedPerson : linkedPersons)
 					{
 						// add link
 						representation.append(">>" + linkedPerson.getIdent());
 					}
 				}
 				
 				
 				// links to categories
 				EList<Category> categories = content.getCategories();
 				if(categories != null && !categories.isEmpty())
 				{
 					// link all categories
 					for(Category category : categories)
 					{
 						// add link
 						representation.append(">>" + category.getIdent());
 					}
 				}
 				
 				// links to tags
 				EList<Tag> tags = content.getTags();
 				if(tags != null && !tags.isEmpty())
 				{
 					// link all tags
 					for(Tag tag : tags)
 					{
 						// add link
 						representation.append(">>" + tag.getIdent());
 					}
 				}
 				
 				
 				// direct subcontents
 				EList<Content> subContents = content.getContents();
 				if(subContents != null && !subContents.isEmpty())
 				{
 					// add all sub contents
 					for(Content subContent : subContents)
 					{
 						// add new line
 						representation.append(ls);
 						// content name
 						representation.append("++++" + subContent.getName());
 						// subcontent ident
 						representation.append("@@" + subContent.getIdent());
 						// add new line
 						representation.append(ls);
 						// content value
 						representation.append("::" + subContent.getStringValue());
 					}
 				}
 				
 				// add new line
 				representation.append(ls);
 			}
 		}
 		return representation.toString();	
 	}
 
 	/**
 	 * Returns all WebSites matching the provided condition.
 	 *
 	 * @condition The condition to filter the WebSites.
 	 * @return All WebSites of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<WebSite> getWebSitesMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(WebSite.isTypeCondition.AND(condition));
 		EList<WebSite> resList = new BasicEList<WebSite>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((WebSite) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Rankings.
 	 *
 	 * @return All Rankings of this DataSet.
 	 * @generated
 	 */
 	public EList<Ranking> getRankings() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getRanking());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Ranking.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Ranking>();
 		}
 		
 		// results are only Rankings
 		@SuppressWarnings("unchecked")
 		EList<Ranking> objects = new BasicEList<Ranking>((Collection<? extends Ranking>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Rankings matching the provided condition.
 	 *
 	 * @condition The condition to filter the Rankings.
 	 * @return All Rankings of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Ranking> getRankingsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Ranking.isTypeCondition.AND(condition));
 		EList<Ranking> resList = new BasicEList<Ranking>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Ranking) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Attachments.
 	 *
 	 * @return All Attachments of this DataSet.
 	 * @generated
 	 */
 	public EList<Attachment> getAttachments() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getAttachment());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Attachment.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Attachment>();
 		}
 		
 		// results are only Attachments
 		@SuppressWarnings("unchecked")
 		EList<Attachment> objects = new BasicEList<Attachment>((Collection<? extends Attachment>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Attachments matching the provided condition.
 	 *
 	 * @condition The condition to filter the Attachments.
 	 * @return All Attachments of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Attachment> getAttachmentsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Attachment.isTypeCondition.AND(condition));
 		EList<Attachment> resList = new BasicEList<Attachment>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Attachment) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Locations.
 	 *
 	 * @return All Locations of this DataSet.
 	 * @generated
 	 */
 	public EList<Location> getLocations() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getLocation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Location.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Location>();
 		}
 		
 		// results are only Locations
 		@SuppressWarnings("unchecked")
 		EList<Location> objects = new BasicEList<Location>((Collection<? extends Location>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<MetaInformation> getMetaInformations() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getMetaInformation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(MetaInformation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<MetaInformation>();
 		}
 		
 		// results are only MetaInformations
 		@SuppressWarnings("unchecked")
 		EList<MetaInformation> objects = new BasicEList<MetaInformation>((Collection<? extends MetaInformation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Locations matching the provided condition.
 	 *
 	 * @condition The condition to filter the Locations.
 	 * @return All Locations of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Location> getLocationsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Location.isTypeCondition.AND(condition));
 		EList<Location> resList = new BasicEList<Location>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Location) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Images matching the provided condition.
 	 *
 	 * @condition The condition to filter the Images.
 	 * @return All Images of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Image> getImagesMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Image.isTypeCondition.AND(condition));
 		EList<Image> resList = new BasicEList<Image>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Image) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Documents matching the provided condition.
 	 *
 	 * @condition The condition to filter the Documents.
 	 * @return All Documents of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Document> getDocumentsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Document.isTypeCondition.AND(condition));
 		EList<Document> resList = new BasicEList<Document>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Document) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all StarRankings.
 	 *
 	 * @return All StarRankings of this DataSet.
 	 * @generated
 	 */
 	public EList<StarRanking> getStarRankings() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getStarRanking());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(StarRanking.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<StarRanking>();
 		}
 		
 		// results are only StarRankings
 		@SuppressWarnings("unchecked")
 		EList<StarRanking> objects = new BasicEList<StarRanking>((Collection<? extends StarRanking>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<ThumbRanking> getThumbRankings() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getThumbRanking());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(ThumbRanking.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<ThumbRanking>();
 		}
 		
 		// results are only ThumbRankings
 		@SuppressWarnings("unchecked")
 		EList<ThumbRanking> objects = new BasicEList<ThumbRanking>((Collection<? extends ThumbRanking>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Transformation> getTransformations() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getTransformation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Transformation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Transformation>();
 		}
 		
 		// results are only Transformations
 		@SuppressWarnings("unchecked")
 		EList<Transformation> objects = new BasicEList<Transformation>((Collection<? extends Transformation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all StarRankings matching the provided condition.
 	 *
 	 * @condition The condition to filter the StarRankings.
 	 * @return All StarRankings of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<StarRanking> getStarRankingsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(StarRanking.isTypeCondition.AND(condition));
 		EList<StarRanking> resList = new BasicEList<StarRanking>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((StarRanking) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all ViewRankings matching the provided condition.
 	 *
 	 * @condition The condition to filter the ViewRankings.
 	 * @return All ViewRankings of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<ViewRanking> getViewRankingsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(ViewRanking.isTypeCondition.AND(condition));
 		EList<ViewRanking> resList = new BasicEList<ViewRanking>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((ViewRanking) eo);
 			}
 		}
 		return resList;
 	}
 
 
 	/**
 	 * Returns all ThumbRankings matching the provided condition.
 	 *
 	 * @condition The condition to filter the ThumbRankings.
 	 * @return All ThumbRankings of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<ThumbRanking> getThumbRankingsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(ThumbRanking.isTypeCondition.AND(condition));
 		EList<ThumbRanking> resList = new BasicEList<ThumbRanking>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((ThumbRanking) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Video> getVideos() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getVideo());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Video.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Video>();
 		}
 		
 		// results are only Videos
 		@SuppressWarnings("unchecked")
 		EList<Video> objects = new BasicEList<Video>((Collection<? extends Video>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<ViewRanking> getViewRankings() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getViewRanking());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(ViewRanking.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<ViewRanking>();
 		}
 		
 		// results are only ViewRankings
 		@SuppressWarnings("unchecked")
 		EList<ViewRanking> objects = new BasicEList<ViewRanking>((Collection<? extends ViewRanking>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Returns all Transformations matching the provided condition.
 	 *
 	 * @condition The condition to filter the Transformations.
 	 * @return All Transformations of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Transformation> getTransformationsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Transformation.isTypeCondition.AND(condition));
 		EList<Transformation> resList = new BasicEList<Transformation>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Transformation) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Videos matching the provided condition.
 	 *
 	 * @condition The condition to filter the Videos.
 	 * @return All Videos of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Video> getVideosMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Video.isTypeCondition.AND(condition));
 		EList<Video> resList = new BasicEList<Video>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Video) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Connections matching the provided condition.
 	 *
 	 * @condition The condition to filter the Connections.
 	 * @return All Connections of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Connection> getConnectionsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Connection.isTypeCondition.AND(condition));
 		EList<Connection> resList = new BasicEList<Connection>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Connection) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Binaries matching the provided condition.
 	 *
 	 * @condition The condition to filter the Binaries.
 	 * @return All Binaries of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Binary> getBinariesMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Binary.isTypeCondition.AND(condition));
 		EList<Binary> resList = new BasicEList<Binary>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Binary) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all MetaInformations matching the provided condition.
 	 *
 	 * @condition The condition to filter the MetaInformations.
 	 * @return All MetaInformations of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<MetaInformation> getMetaInformationsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(MetaInformation.isTypeCondition.AND(condition));
 		EList<MetaInformation> resList = new BasicEList<MetaInformation>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((MetaInformation) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all IndoorLocations.
 	 *
 	 * @return All IndoorLocations of this DataSet.
 	 * @generated
 	 */
 	public EList<IndoorLocation> getIndoorLocations() {
 		IQueryResult result = getItemsMatchingCondition(IndoorLocation.isTypeCondition);
 		EList<IndoorLocation> resList = new BasicEList<IndoorLocation>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((IndoorLocation) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all IndoorLocations matching the provided condition.
 	 *
 	 * @condition The condition to filter the IndoorLocations.
 	 * @return All IndoorLocations of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<IndoorLocation> getIndoorLocationsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(IndoorLocation.isTypeCondition.AND(condition));
 		EList<IndoorLocation> resList = new BasicEList<IndoorLocation>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((IndoorLocation) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Identifiers matching the provided condition.
 	 *
 	 * @condition The condition to filter the Identifiers.
 	 * @return All Identifiers of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Identifier> getIdentifiersMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Identifier.isTypeCondition.AND(condition));
 		EList<Identifier> resList = new BasicEList<Identifier>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Identifier) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Events.
 	 *
 	 * @return All Events of this DataSet.
 	 * @generated
 	 */
 	public EList<Event> getEvents() {
 		IQueryResult result = getItemsMatchingCondition(Event.isTypeCondition);
 		EList<Event> resList = new BasicEList<Event>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Event) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Returns all Events matching the provided condition.
 	 *
 	 * @condition The condition to filter the Events.
 	 * @return All Events of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Event> getEventsMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Event.isTypeCondition.AND(condition));
 		EList<Event> resList = new BasicEList<Event>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Event) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * Generates an EObjectCondition to check whether an Object is of the type DataSet.
 	 * 
 	 * @return An EObjectCondition whether the Object is of the type DataSet.
 	 * @generated
 	 */
 	public static EObjectCondition generateIsTypeCondition() {
 		return new EObjectTypeRelationCondition(DataPackageImpl.eINSTANCE.getDataSet());
 	}
 
 	/**
 	 * This method provides a generic access to the Getters of this class.
  	 * 
  	 * @param opName The name of the Feature to be gotten.
  	 *
  	 * @return The value of the Feature or null.
  	 * 
 	 * @generated
 	 */
 	protected Object getFeature(String featureName) throws UnknownOperationException {
 		if ( featureName.equalsIgnoreCase("items") )
 			return this.getItems();		
 		if ( featureName.equalsIgnoreCase("cacheFolder") )
 			return this.getCacheFolder();		
 		if ( featureName.equalsIgnoreCase("cacheFileAttachements") )
 			return this.getCacheFileAttachements();		
 		if ( featureName.equalsIgnoreCase("setUp") )
 			return this.getSetUp();		
 		if ( featureName.equalsIgnoreCase("lastModified") )
 			return this.getLastModified();		
 		if ( featureName.equalsIgnoreCase("logLevel") )
 			return this.getLogLevel();		
 		throw new UnknownOperationException(this, new RestCommand("get" + featureName)); 
 	}
 
 	/**
 	 * This method provides a generic access to the Setters of this class.
  	 * 
  	 * @param opName The name of the Feature to be set.
  	 * @param value The new value of the feature.
  	 * 
 	 * @generated
 	 */
 	protected Object setFeature(String featureName, Object value) throws WrongArgException, UnknownOperationException {
 		if ( featureName.equalsIgnoreCase("cacheFolder") ) {
 				java.lang.String fcacheFolder = null;
 				try {
 					fcacheFolder = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("DataSet.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setCacheFolder(fcacheFolder);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("cacheFileAttachements") ) {
 				java.lang.Boolean fcacheFileAttachements = null;
 				try {
 					try {
 						fcacheFileAttachements = (java.lang.Boolean)(RestUtil.fromInput(value));
 					} catch (ClassNotFoundException e) {
 						fcacheFileAttachements = (java.lang.Boolean)value;
 					}
 				} catch (ClassCastException e) {
 					throw new WrongArgException("DataSet.setFeature", "java.lang.Boolean",value.getClass().getName());
 				}
 				this.setCacheFileAttachements(fcacheFileAttachements);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("setUp") ) {
 				org.sociotech.communitymashup.application.Mashup fsetUp = null;
 				try {
 					try {
 						fsetUp = (org.sociotech.communitymashup.application.Mashup)(RestUtil.fromInput(value));
 					} catch (ClassNotFoundException e) {
 						fsetUp = (org.sociotech.communitymashup.application.Mashup)value;
 					}
 				} catch (ClassCastException e) {
 					throw new WrongArgException("DataSet.setFeature", "org.sociotech.communitymashup.application.Mashup",value.getClass().getName());
 				}
 				this.setSetUp(fsetUp);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("lastModified") ) {
 				java.util.Date flastModified = null;
 				try {
 					try {
 						flastModified = RestUtil.fromDateString((String) value);
 						if(flastModified == null) {
 							flastModified = (java.util.Date)(RestUtil.fromInput(value));
 						}
 					} catch (ClassNotFoundException e) {
 						flastModified = (java.util.Date)value;
 					}
 				} catch (ClassCastException e) {
 					throw new WrongArgException("DataSet.setFeature", "java.util.Date",value.getClass().getName());
 				}
 				this.setLastModified(flastModified);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("logLevel") ) {
 				java.lang.Integer flogLevel = null;
 				try {
 				try {
 					flogLevel = RestUtil.fromIntegerString((String) value);
 					if(flogLevel == null) {
 						flogLevel = (java.lang.Integer)(RestUtil.fromInput(value));
 					}
 				} catch (ClassNotFoundException e) {
 					flogLevel = (java.lang.Integer)value;
 				}
 				} catch (ClassCastException e) {
 					throw new WrongArgException("DataSet.setFeature", "java.lang.Integer",value.getClass().getName());
 				}
 				this.setLogLevel(flogLevel);
 			return this;
 			}		
 	throw new UnknownOperationException(this, new RestCommand("set" + featureName).addArg("value",value));
 	}
 
 	/**
 	 * This method provides a generic access to the Operations of this class.
  	 * 
  	 * @param opName The name of the requested Operation.
  	 * @param values The arguments to be used.
  	 * 
  	 * @return The result of the Operation or null.
  	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	protected Object doOperation(RestCommand command) throws ArgNotFoundException, WrongArgException, WrongArgCountException, UnknownOperationException {
 		if ( command.getCommand().equalsIgnoreCase("add")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			Item item = null;
 			try {
 				try {
 					item = (Item)(RestUtil.fromInput(command.getArg("item")));
 				} catch (ClassNotFoundException e) {
 					item = (Item)command.getArg("item");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "Item", command.getArg("item").getClass().getName());
 			}
 			return this.add(item);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getItemsWithStringValue")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String stringValue = null;
 			try {
 				stringValue = (java.lang.String)command.getArg("stringValue");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("stringValue").getClass().getName());
 			}
 			return this.getItemsWithStringValue(stringValue);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithName")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String name = null;
 			try {
 				name = (java.lang.String)command.getArg("name");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("name").getClass().getName());
 			}
 			return this.getPersonsWithName(name);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithLastname")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String lastname = null;
 			try {
 				lastname = (java.lang.String)command.getArg("lastname");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("lastname").getClass().getName());
 			}
 			return this.getPersonsWithLastname(lastname);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithFirstName")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String firstname = null;
 			try {
 				firstname = (java.lang.String)command.getArg("firstname");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("firstname").getClass().getName());
 			}
 			return this.getPersonsWithFirstName(firstname);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getTag")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String name = null;
 			try {
 				name = (java.lang.String)command.getArg("name");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("name").getClass().getName());
 			}
 			return this.getTag(name);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getMetaTag")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String name = null;
 			try {
 				name = (java.lang.String)command.getArg("name");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("name").getClass().getName());
 			}
 			return this.getMetaTag(name);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAllPersons")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAllPersons();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAllContents")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAllContents();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAllOrganisations")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAllOrganisations();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAllCategories")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAllCategories();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getCategory")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String name = null;
 			try {
 				name = (java.lang.String)command.getArg("name");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("name").getClass().getName());
 			}
 			return this.getCategory(name);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAllTags")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAllTags();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getItemsModifiedSince")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.util.Date date = null;
 			try {
 				date = (java.util.Date)(RestUtil.fromDateString((String)command.getArg("date")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.util.Date", command.getArg("date").getClass().getName());
 			}
 			return this.getItemsModifiedSince(date);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAllMetaTags")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAllMetaTags();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAllConnections")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAllConnections();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentWithIdent")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String ident = null;
 			try {
 				ident = (java.lang.String)command.getArg("ident");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("ident").getClass().getName());
 			}
 			return this.getContentWithIdent(ident);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonWithIdent")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String ident = null;
 			try {
 				ident = (java.lang.String)command.getArg("ident");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("ident").getClass().getName());
 			}
 			return this.getPersonWithIdent(ident);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationWithIdent")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String ident = null;
 			try {
 				ident = (java.lang.String)command.getArg("ident");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("ident").getClass().getName());
 			}
 			return this.getOrganisationWithIdent(ident);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAttachmentWithIdent")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String ident = null;
 			try {
 				ident = (java.lang.String)command.getArg("ident");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("ident").getClass().getName());
 			}
 			return this.getAttachmentWithIdent(ident);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsWithAllCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getInformationObjectsWithAllCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsWithOneOfCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getInformationObjectsWithOneOfCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentsWithAllCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getContentsWithAllCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentsWithOneOfCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getContentsWithOneOfCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithAllCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getPersonsWithAllCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithOneOfCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getPersonsWithOneOfCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationsWithAllCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getOrganisationsWithAllCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationsWithOneOfCategories")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Category> categories = null;
 			try {
 				try {
 					categories = (EList<Category>)(RestUtil.fromInput(command.getArg("categories")));
 				} catch (ClassNotFoundException e) {
 					categories = (EList<Category>)command.getArg("categories");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Category>", command.getArg("categories").getClass().getName());
 			}
 			return this.getOrganisationsWithOneOfCategories(categories);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsWithAllTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getInformationObjectsWithAllTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsWithOneOfTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getInformationObjectsWithOneOfTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationsWithAllTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getOrganisationsWithAllTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationsWithOneOfTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getOrganisationsWithOneOfTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithAllTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getPersonsWithAllTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithOneOfTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getPersonsWithOneOfTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentsWithAllTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getContentsWithAllTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentsWithOneOfTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<Tag> tags = null;
 			try {
 				try {
 					tags = (EList<Tag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<Tag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<Tag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getContentsWithOneOfTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentsWithName")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String name = null;
 			try {
 				name = (java.lang.String)command.getArg("name");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("name").getClass().getName());
 			}
 			return this.getContentsWithName(name);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationsWithName")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String name = null;
 			try {
 				name = (java.lang.String)command.getArg("name");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("name").getClass().getName());
 			}
 			return this.getOrganisationsWithName(name);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getItemsWithAllMetaTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<MetaTag> tags = null;
 			try {
 				try {
 					tags = (EList<MetaTag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<MetaTag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<MetaTag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getItemsWithAllMetaTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getItemsWithOneOfMetaTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<MetaTag> tags = null;
 			try {
 				try {
 					tags = (EList<MetaTag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<MetaTag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<MetaTag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getItemsWithOneOfMetaTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersons")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getPersons();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisations")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getOrganisations();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContents")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getContents();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjects")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getInformationObjects();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getTags")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getTags();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getMetaTags")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getMetaTags();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAttachments")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getAttachments();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getBinaries")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getBinaries();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getCategories")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getCategories();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getClassifications")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getClassifications();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getConnections")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getConnections();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getDocuments")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getDocuments();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getEmails")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getEmails();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getExtensions")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getExtensions();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getImages")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getImages();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInstantMessengers")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getInstantMessengers();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getLocations")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getLocations();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getMetaInformations")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getMetaInformations();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPhones")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getPhones();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getRankings")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getRankings();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getStarRankings")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getStarRankings();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getThumbRankings")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getThumbRankings();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getTransformations")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getTransformations();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getVideos")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getVideos();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getViewRankings")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getViewRankings();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getWebAccounts")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getWebAccounts();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getWebSites")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getWebSites();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsWithAllMetaTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<MetaTag> tags = null;
 			try {
 				try {
 					tags = (EList<MetaTag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<MetaTag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<MetaTag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getInformationObjectsWithAllMetaTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsWithOneOfMetaTags")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			EList<MetaTag> tags = null;
 			try {
 				try {
 					tags = (EList<MetaTag>)(RestUtil.fromInput(command.getArg("tags")));
 				} catch (ClassNotFoundException e) {
 					tags = (EList<MetaTag>)command.getArg("tags");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "EList<MetaTag>", command.getArg("tags").getClass().getName());
 			}
 			return this.getInformationObjectsWithOneOfMetaTags(tags);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getIdentifiers")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getIdentifiers();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getIdentifiersWithKey")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			return this.getIdentifiersWithKey(key);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getIdentifierWithKeyValue")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getIdentifierWithKeyValue(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getItemWithIdentifier")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getItemWithIdentifier(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonWithIdentifier")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getPersonWithIdentifier(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentWithIdentifier")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getContentWithIdentifier(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationWithIdentifier")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getOrganisationWithIdentifier(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getLocationWithIdentifier")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getLocationWithIdentifier(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getIndoorLocationWithIdentifier")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getIndoorLocationWithIdentifier(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getImageWithIdentifier")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.lang.String key = null;
 			try {
 				key = (java.lang.String)command.getArg("key");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("key").getClass().getName());
 			}
 			java.lang.String value = null;
 			try {
 				value = (java.lang.String)command.getArg("value");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("value").getClass().getName());
 			}
 			return this.getImageWithIdentifier(key, value);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getEmptyItemWithIdent")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String ident = null;
 			try {
 				ident = (java.lang.String)command.getArg("ident");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("ident").getClass().getName());
 			}
 			return this.getEmptyItemWithIdent(ident);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getTagsWithMoreThanXInformationObjects")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.Integer x = null;
 			try {
 				x = (java.lang.Integer)(RestUtil.fromIntegerString((String)command.getArg("x")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.Integer", command.getArg("x").getClass().getName());
 			}
 			return this.getTagsWithMoreThanXInformationObjects(x);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getItemsCreatedSince")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.util.Date date = null;
 			try {
 				date = (java.util.Date)(RestUtil.fromDateString((String)command.getArg("date")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.util.Date", command.getArg("date").getClass().getName());
 			}
 			return this.getItemsCreatedSince(date);
 		}
 		if ( command.getCommand().equalsIgnoreCase("searchItems")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String term = null;
 			try {
 				term = (java.lang.String)command.getArg("term");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("term").getClass().getName());
 			}
 			return this.searchItems(term);
 		}
 		if ( command.getCommand().equalsIgnoreCase("searchInformationObjects")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String term = null;
 			try {
 				term = (java.lang.String)command.getArg("term");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("term").getClass().getName());
 			}
 			return this.searchInformationObjects(term);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsWithAttachment")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			Attachment attachment = null;
 			try {
 				try {
 					attachment = (Attachment)(RestUtil.fromInput(command.getArg("attachment")));
 				} catch (ClassNotFoundException e) {
 					attachment = (Attachment)command.getArg("attachment");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "Attachment", command.getArg("attachment").getClass().getName());
 			}
 			return this.getInformationObjectsWithAttachment(attachment);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getPersonsWithAttachment")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			Attachment attachment = null;
 			try {
 				try {
 					attachment = (Attachment)(RestUtil.fromInput(command.getArg("attachment")));
 				} catch (ClassNotFoundException e) {
 					attachment = (Attachment)command.getArg("attachment");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "Attachment", command.getArg("attachment").getClass().getName());
 			}
 			return this.getPersonsWithAttachment(attachment);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getOrganisationsWithAttachment")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			Attachment attachment = null;
 			try {
 				try {
 					attachment = (Attachment)(RestUtil.fromInput(command.getArg("attachment")));
 				} catch (ClassNotFoundException e) {
 					attachment = (Attachment)command.getArg("attachment");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "Attachment", command.getArg("attachment").getClass().getName());
 			}
 			return this.getOrganisationsWithAttachment(attachment);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getContentsWithAttachment")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			Attachment attachment = null;
 			try {
 				try {
 					attachment = (Attachment)(RestUtil.fromInput(command.getArg("attachment")));
 				} catch (ClassNotFoundException e) {
 					attachment = (Attachment)command.getArg("attachment");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "Attachment", command.getArg("attachment").getClass().getName());
 			}
 			return this.getContentsWithAttachment(attachment);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getEqualItem")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			Item item = null;
 			try {
 				try {
 					item = (Item)(RestUtil.fromInput(command.getArg("item")));
 				} catch (ClassNotFoundException e) {
 					item = (Item)command.getArg("item");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "Item", command.getArg("item").getClass().getName());
 			}
 			return this.getEqualItem(item);
 		}
 		if ( command.getCommand().equalsIgnoreCase("hasEqualItem")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			Item item = null;
 			try {
 				try {
 					item = (Item)(RestUtil.fromInput(command.getArg("item")));
 				} catch (ClassNotFoundException e) {
 					item = (Item)command.getArg("item");
 				}
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "Item", command.getArg("item").getClass().getName());
 			}
 			return this.hasEqualItem(item);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getItemsWithIdent")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String ident = null;
 			try {
 				ident = (java.lang.String)command.getArg("ident");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("ident").getClass().getName());
 			}
 			return this.getItemsWithIdent(ident);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getAttachmentsWithCachedFileName")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.lang.String cachedFileName = null;
 			try {
 				cachedFileName = (java.lang.String)command.getArg("cachedFileName");
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.lang.String", command.getArg("cachedFileName").getClass().getName());
 			}
 			return this.getAttachmentsWithCachedFileName(cachedFileName);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getEventsAfter")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.util.Date date = null;
 			try {
 				date = (java.util.Date)(RestUtil.fromDateString((String)command.getArg("date")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.util.Date", command.getArg("date").getClass().getName());
 			}
 			return this.getEventsAfter(date);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getEventsBefore")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.util.Date date = null;
 			try {
 				date = (java.util.Date)(RestUtil.fromDateString((String)command.getArg("date")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.util.Date", command.getArg("date").getClass().getName());
 			}
 			return this.getEventsBefore(date);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getEventsBetweenDates")) {
 			if (command.getArgCount() != 2) throw new WrongArgCountException("DataSet.doOperation", 2, command.getArgCount()); 
 			java.util.Date after = null;
 			try {
 				after = (java.util.Date)(RestUtil.fromDateString((String)command.getArg("after")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.util.Date", command.getArg("after").getClass().getName());
 			}
 			java.util.Date before = null;
 			try {
 				before = (java.util.Date)(RestUtil.fromDateString((String)command.getArg("before")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.util.Date", command.getArg("before").getClass().getName());
 			}
 			return this.getEventsBetweenDates(after, before);
 		}
 		if ( command.getCommand().equalsIgnoreCase("getSpicynodesRepresentation")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getSpicynodesRepresentation();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getConnectionsBetweenInformationObjectsOfDifferentCategories")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("DataSet.doOperation", 0, command.getArgCount()); 
 			return this.getConnectionsBetweenInformationObjectsOfDifferentCategories();
 		}
 		if ( command.getCommand().equalsIgnoreCase("getInformationObjectsModifiedSince")) {
 			if (command.getArgCount() != 1) throw new WrongArgCountException("DataSet.doOperation", 1, command.getArgCount()); 
 			java.util.Date date = null;
 			try {
 				date = (java.util.Date)(RestUtil.fromDateString((String)command.getArg("date")));
 			} catch (ClassCastException e) {
 				throw new WrongArgException("DataSet.doOperation", "java.util.Date", command.getArg("date").getClass().getName());
 			}
 			return this.getInformationObjectsModifiedSince(date);
 		}
 		throw new UnknownOperationException(this, command);
 	}
 
 	/**
 	 * This method can be used to recursively and generically call the Getter, Setters and Operations of the generated classes.
 	 * 
 	 * @param input The commands to be processed.
 	 * @param requestType The HTTP-Method of the request.
 	 * 
 	 * @return The result of the Getter/Operation or null.
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object process(LinkedList<RestCommand> input, RequestType requestType) throws ArgNotFoundException, WrongArgException, WrongArgCountException, UnknownOperationException {
 		Object o = null;
 		RestCommand c = input.poll();
 		// check for HTTP-Request method
 		if (requestType == RequestType.rtGet) {
 			// only Getters are allowed -> side-effects...
 			if (c.getCommand().startsWith("get")) {
 				if (c.getArgCount() != 0) throw new WrongArgCountException(c.getCommand(), 0, c.getArgCount());
 				o = this.getFeature(c.getCommand().substring(3));
 			}
 		} else {
 			// everything is allowed - at least for now
 			try {
 				o = this.doOperation(c);
 			} catch(Exception e) {
 				if (c.getCommand().startsWith("get")) {
 					if (c.getArgCount() != 0) throw new WrongArgCountException(c.getCommand(), 0, c.getArgCount());
 					o = this.getFeature(c.getCommand().substring(3));
 				} else if (c.getCommand().startsWith("set")) {
 					if (c.getArgCount() != 1) throw new WrongArgCountException(c.getCommand(), 1, c.getArgCount());
 					Object so = c.getArg("new" + c.getCommand().substring(3));
 					o = this.setFeature(c.getCommand().substring(3), so);
 				} else {
 					if (e instanceof ArgNotFoundException)
 						throw (ArgNotFoundException)e;
 					if (e instanceof WrongArgException)
 						throw (WrongArgException)e;
 					if (e instanceof WrongArgCountException)
 						throw (WrongArgCountException)e;
 					if (e instanceof UnknownOperationException)
 						throw (UnknownOperationException)e;
 				}
 			}
 		}
 		if (input.isEmpty()) {
 			return o;
 		} else { 
 			if (o instanceof PersonImpl) {
 				return ((Person) o).process(input, requestType);
 			}
 			if (o instanceof InformationObjectImpl) {
 				return ((InformationObject) o).process(input, requestType);
 			}
 			if (o instanceof ContentImpl) {
 				return ((Content) o).process(input, requestType);
 			}
 			if (o instanceof DataSetImpl) {
 				return ((DataSet) o).process(input, requestType);
 			}
 			if (o instanceof ItemImpl) {
 				return ((Item) o).process(input, requestType);
 			}
 			if (o instanceof ExtensionImpl) {
 				return ((Extension) o).process(input, requestType);
 			}
 			if (o instanceof ClassificationImpl) {
 				return ((Classification) o).process(input, requestType);
 			}
 			if (o instanceof CategoryImpl) {
 				return ((Category) o).process(input, requestType);
 			}
 			if (o instanceof TagImpl) {
 				return ((Tag) o).process(input, requestType);
 			}
 			if (o instanceof OrganisationImpl) {
 				return ((Organisation) o).process(input, requestType);
 			}
 			if (o instanceof MetaTagImpl) {
 				return ((MetaTag) o).process(input, requestType);
 			}
 			if (o instanceof PhoneImpl) {
 				return ((Phone) o).process(input, requestType);
 			}
 			if (o instanceof InstantMessengerImpl) {
 				return ((InstantMessenger) o).process(input, requestType);
 			}
 			if (o instanceof EmailImpl) {
 				return ((Email) o).process(input, requestType);
 			}
 			if (o instanceof WebAccountImpl) {
 				return ((WebAccount) o).process(input, requestType);
 			}
 			if (o instanceof WebSiteImpl) {
 				return ((WebSite) o).process(input, requestType);
 			}
 			if (o instanceof RankingImpl) {
 				return ((Ranking) o).process(input, requestType);
 			}
 			if (o instanceof AttachmentImpl) {
 				return ((Attachment) o).process(input, requestType);
 			}
 			if (o instanceof LocationImpl) {
 				return ((Location) o).process(input, requestType);
 			}
 			if (o instanceof ImageImpl) {
 				return ((Image) o).process(input, requestType);
 			}
 			if (o instanceof DocumentImpl) {
 				return ((Document) o).process(input, requestType);
 			}
 			if (o instanceof StarRankingImpl) {
 				return ((StarRanking) o).process(input, requestType);
 			}
 			if (o instanceof ViewRankingImpl) {
 				return ((ViewRanking) o).process(input, requestType);
 			}
 			if (o instanceof ThumbRankingImpl) {
 				return ((ThumbRanking) o).process(input, requestType);
 			}
 			if (o instanceof TransformationImpl) {
 				return ((Transformation) o).process(input, requestType);
 			}
 			if (o instanceof VideoImpl) {
 				return ((Video) o).process(input, requestType);
 			}
 			if (o instanceof ConnectionImpl) {
 				return ((Connection) o).process(input, requestType);
 			}
 			if (o instanceof BinaryImpl) {
 				return ((Binary) o).process(input, requestType);
 			}
 			if (o instanceof MetaInformationImpl) {
 				return ((MetaInformation) o).process(input, requestType);
 			}
 			if (o instanceof IndoorLocationImpl) {
 				return ((IndoorLocation) o).process(input, requestType);
 			}
 			if (o instanceof IdentifierImpl) {
 				return ((Identifier) o).process(input, requestType);
 			}
 			if (o instanceof EventImpl) {
 				return ((Event) o).process(input, requestType);
 			}
 			if (o instanceof List) {
 				return RestUtil.listProcess((List<?>) o, input, requestType);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Content> getContentsWithAllCategories(EList<Category> categories) {
 		// Check if input is defined
 		if(getInformationObjectsWithAllCategories(categories) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getContent());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAllCategories(categories), oclCondition.AND(Content.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Content>();
 		}
 		
 		// results are only Contents
 		@SuppressWarnings("unchecked")
 		EList<Content> objects = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Content> getContentsWithOneOfCategories(EList<Category> categories) {
 		// Check if input is defined
 		if(getInformationObjectsWithOneOfCategories(categories) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getContent());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithOneOfCategories(categories), oclCondition.AND(Content.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Content>();
 		}
 		
 		// results are only Contents
 		@SuppressWarnings("unchecked")
 		EList<Content> objects = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Person> getPersonsWithAllCategories(EList<Category> categories) {
 		// Check if input is defined
 		if(getInformationObjectsWithAllCategories(categories) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getPerson());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAllCategories(categories), oclCondition.AND(Person.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Person>();
 		}
 		
 		// results are only Persons
 		@SuppressWarnings("unchecked")
 		EList<Person> objects = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Person> getPersonsWithOneOfCategories(EList<Category> categories) {
 		// Check if input is defined
 		if(getInformationObjectsWithOneOfCategories(categories) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getPerson());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithOneOfCategories(categories), oclCondition.AND(Person.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Person>();
 		}
 		
 		// results are only Persons
 		@SuppressWarnings("unchecked")
 		EList<Person> objects = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Organisation> getOrganisationsWithAllCategories(EList<Category> categories) {
 		// Check if input is defined
 		if(getInformationObjectsWithAllCategories(categories) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getOrganisation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAllCategories(categories), oclCondition.AND(Organisation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Organisation>();
 		}
 		
 		// results are only Organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> objects = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Organisation> getOrganisationsWithOneOfCategories(EList<Category> categories) {
 		// Check if input is defined
 		if(getInformationObjectsWithOneOfCategories(categories) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getOrganisation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithOneOfCategories(categories), oclCondition.AND(Organisation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Organisation>();
 		}
 		
 		// results are only Organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> objects = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<InformationObject> getInformationObjectsWithAllTags(EList<Tag> tags) {
 		if(tags == null || tags.isEmpty())
 		{
 			// return all information objects if no tags are defined.
 			return getInformationObjects();
 		}
 
 		// TODO maybe replace by a more efficent ocl query
 		EList<InformationObject> informationObjects = getInformationObjects();
 		EList<InformationObject> taggedInformationObjects = new BasicEList<InformationObject>();
 		
 		for(InformationObject io : informationObjects)
 		{
 			// check if this Information Object is tagged by all of the given tags
 			if(io.getTags().containsAll(tags))
 			{
 				taggedInformationObjects.add(io);
 			}
 		}
 		
 		return taggedInformationObjects;	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<InformationObject> getInformationObjectsWithOneOfTags(EList<Tag> tags) {
 		if(tags == null || tags.isEmpty())
 		{
 			// return all information objects if no tags are defined.
 			return getInformationObjects();
 		}
 
 		// TODO maybe replace by a more efficent ocl query
 		EList<InformationObject> informationObjects = getInformationObjects();
 		EList<InformationObject> taggedInformationObjects = new BasicEList<InformationObject>();
 		
 		for(InformationObject io : informationObjects)
 		{
 			// check if this Information Object is tagged by one of the given tags
 			for(Tag tag : tags)
 			{
 				if(io.getTags().contains(tag))
 				{
 					taggedInformationObjects.add(io);
 					break; // no need to check other tags
 				}
 			}
 		}
 		
 		return taggedInformationObjects;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Organisation> getOrganisationsWithAllTags(EList<Tag> tags) {
 		// Check if input is defined
 		if(getInformationObjectsWithAllTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getOrganisation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAllTags(tags), oclCondition.AND(Organisation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Organisation>();
 		}
 		
 		// results are only Organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> objects = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Organisation> getOrganisationsWithOneOfTags(EList<Tag> tags) {
 		// Check if input is defined
 		if(getInformationObjectsWithOneOfTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getOrganisation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithOneOfTags(tags), oclCondition.AND(Organisation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Organisation>();
 		}
 		
 		// results are only Organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> objects = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Person> getPersonsWithAllTags(EList<Tag> tags) {
 		// Check if input is defined
 		if(getInformationObjectsWithAllTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getPerson());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAllTags(tags), oclCondition.AND(Person.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Person>();
 		}
 		
 		// results are only Persons
 		@SuppressWarnings("unchecked")
 		EList<Person> objects = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Person> getPersonsWithOneOfTags(EList<Tag> tags) {
 		// Check if input is defined
 		if(getInformationObjectsWithOneOfTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getPerson());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithOneOfTags(tags), oclCondition.AND(Person.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Person>();
 		}
 		
 		// results are only Persons
 		@SuppressWarnings("unchecked")
 		EList<Person> objects = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Content> getContentsWithAllTags(EList<Tag> tags) {
 		// Check if input is defined
 		if(getInformationObjectsWithAllTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getContent());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithAllTags(tags), oclCondition.AND(Content.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Content>();
 		}
 		
 		// results are only Contents
 		@SuppressWarnings("unchecked")
 		EList<Content> objects = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Content> getContentsWithOneOfTags(EList<Tag> tags) {
 		// Check if input is defined
 		if(getInformationObjectsWithOneOfTags(tags) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getContent());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getInformationObjectsWithOneOfTags(tags), oclCondition.AND(Content.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Content>();
 		}
 		
 		// results are only Contents
 		@SuppressWarnings("unchecked")
 		EList<Content> objects = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Content> getContentsWithName(String name) {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.name='" + name + "'";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getContent());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Content.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Content>();
 		}
 		
 		// results are only Contents
 		@SuppressWarnings("unchecked")
 		EList<Content> objects = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Organisation> getOrganisationsWithName(String name) {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "self.name='" + name + "'";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getOrganisation());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Organisation.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Organisation>();
 		}
 		
 		// results are only Organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> objects = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Item> getItemsWithAllMetaTags(EList<MetaTag> tags) {
 		if(tags == null || tags.isEmpty())
 		{
 			// return all items if no tags are defined.
 			return getItems();
 		}
 
 		// TODO maybe replace by a more efficent ocl query
 		EList<Item> items = getItems();
 		EList<Item> taggedItems = new BasicEList<Item>();
 		
 		for(Item item : items)
 		{
 			// check if this item is tagged by all of the given tags
 			if(item.getMetaTags().containsAll(tags))
 			{
 				taggedItems.add(item);
 			}
 		}
 		
 		return taggedItems;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Item> getItemsWithOneOfMetaTags(EList<MetaTag> tags) {
 		if(tags == null || tags.isEmpty())
 		{
 			// return all items if no tags are defined.
 			return getItems();
 		}
 
 		// TODO maybe replace by a more efficent ocl query
 		EList<Item> items = getItems();
 		EList<Item> taggedItems = new BasicEList<Item>();
 		
 		for(Item item : items)
 		{
 			// check if this item is tagged by one of the given tags
 			for(MetaTag tag : tags)
 			{
 				if(item.getMetaTags().contains(tag))
 				{
 					taggedItems.add(item);
 					break; // no need to check other tags
 				}
 			}
 		}
 		
 		return taggedItems;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Person> getPersons() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getPerson());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(Person.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<Person>();
 		}
 		
 		// results are only Persons
 		@SuppressWarnings("unchecked")
 		EList<Person> objects = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Item add(Item item) {
 		
 		if(item != null)
 		{
 			Item equalItem = this.getEqualItem(item);
 			if(equalItem != null)
 			{
 				// TODO merge both
 				
 				// return the existing one
 				return equalItem;
 			}
 			
 			// provide a unique ident
 			if(item.getIdent() == null || item.getIdent().isEmpty() || item.getIdent().contains(" "))
 			{
 				// TODO more efficent ident generation
 				do
 				{
 					identCounter ++;
 				}
 				while(this.getItemsWithIdent(idPrefix + identCounter) != null);
 					
 				item.setIdent(idPrefix + identCounter); 
 			}	
 			
 			// set creation date to current time if it was not previously set
 			if(item.getCreated() == null)
 			{
 				Date d = new Date();
 				item.setCreated(d);
 			}
 			
 			// add to the same resource as data set
 			Resource resource = this.eResource();
 			if(resource != null)
 			{
 				resource.getContents().add(item);
 			}
 			
 			// add only valid items to data set
 			// TODO if getItems will be extended to deliver only copies this must be adapted
 			getItems().add(item);
 		}
 		
 		return item;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns the content with the given ident. Null if object does not exist or is not a instance of this type. 
 	 * <!-- end-user-doc -->
 	 */
 	public Content getContentWithIdent(String ident) {
 		
 		Item item = this.getItemsWithIdent(ident);
 		
 		if(item == null || !(item instanceof Content))
 		{
 			return null;
 		}
 		
 		return (Content) item;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns the person with the given ident. Null if object does not exist or is not a instance of this type. 
 	 * <!-- end-user-doc -->
 	 */
 	public Person getPersonWithIdent(String ident) {
 		
 		Item item = this.getItemsWithIdent(ident);
 		
 		if(item == null || !(item instanceof Person))
 		{
 			return null;
 		}
 		
 		return (Person) item;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns the organisation with the given ident. Null if object does not exist or is not a instance of this type. 
 	 * <!-- end-user-doc -->
 	 */
 	public Organisation getOrganisationWithIdent(String ident) {
 		
 		Item item = this.getItemsWithIdent(ident);
 		
 		if(item == null || !(item instanceof Organisation))
 		{
 			return null;
 		}
 		
 		return (Organisation) item;
 	}
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns the attachement with the given ident. Null if object does not exist or is not a instance of this type. 
 	 * <!-- end-user-doc -->
 	 */
 	public Attachment getAttachmentWithIdent(String ident) {
 		
 		Item item = this.getItemsWithIdent(ident);
 		
 		if(item == null || !(item instanceof Attachment))
 		{
 			return null;
 		}
 		
 		return (Attachment) item;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns all information object which are in all of the given categories. If the list is empty or undefined then
 	 * all information objects will be returned.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<InformationObject> getInformationObjectsWithAllCategories(EList<Category> categories) {
 		
 		if(categories == null || categories.isEmpty())
 		{
 			// return all information objects if no categories are defined.
 			return getInformationObjects();
 		}
 
 		// TODO maybe replace by a more efficent ocl query
 		EList<InformationObject> informationObjects = getInformationObjects();
 		EList<InformationObject> categorizedInformationObjects = new BasicEList<InformationObject>();
 		
 		for(InformationObject io : informationObjects)
 		{
 			// check if this Information Object is categorized by all of the given categories
 			if(io.getCategories().containsAll(categories))
 			{
 				categorizedInformationObjects.add(io);
 			}
 		}
 		
 		return categorizedInformationObjects;	
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns all information object which are in one of the given categories. If the list is empty or undefined then
 	 * all information objects will be returned.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<InformationObject> getInformationObjectsWithOneOfCategories(EList<Category> categories) {
 		if(categories == null || categories.isEmpty())
 		{
 			// return all information objects if no categories are defined.
 			return getInformationObjects();
 		}
 
 		// TODO maybe replace by a more efficent ocl query
 		EList<InformationObject> informationObjects = getInformationObjects();
 		EList<InformationObject> categorizedInformationObjects = new BasicEList<InformationObject>();
 		
 		for(InformationObject io : informationObjects)
 		{
 			// check if this Information Object is categorized by one of the given categories
 			for(Category cat : categories)
 			{
 				if(io.getCategories().contains(cat))
 				{
 					categorizedInformationObjects.add(io);
 					break; // no need to check other categories
 				}
 			}
 		}
 		
 		return categorizedInformationObjects;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Person> getPersonsWithName(String name) {
 		
 		// create name comparison condition
 		EObjectCondition nameEqualCondition = InformationObjectImpl.hasEqualNameCondition(name);
 		
 		if(nameEqualCondition == null)
 		{
 			return null;
 		}
 		
 		// person and name
 		IQueryResult result = getItemsMatchingCondition(nameEqualCondition.AND(Person.isTypeCondition));
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only persons
 		@SuppressWarnings("unchecked")
 		EList<Person> persons = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return persons;
 	}
 
 	/**
 	 * Queries the contained Items and returns all organisations.
 	 * 
 	 * @return A list of all organisations. Null in error case.
 	 */
 	public EList<Organisation> getOrganisations()
 	{
 		IQueryResult result = getItemsMatchingCondition(Organisation.isTypeCondition);
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> organisations = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return organisations;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Content> getContents() {
 		IQueryResult result = getItemsMatchingCondition(ContentImpl.isMainContentCondition());
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only contents
 		@SuppressWarnings("unchecked")
 		EList<Content> contents = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return contents;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<InformationObject> getInformationObjects() {
 		// Check if input is defined
 		if(getItems() == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getInformationObject());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItems(), oclCondition.AND(InformationObject.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<InformationObject>();
 		}
 		
 		// results are only InformationObjects
 		@SuppressWarnings("unchecked")
 		EList<InformationObject> objects = new BasicEList<InformationObject>((Collection<? extends InformationObject>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/**
 	 * Queries the contained Items and returns all main categories.
 	 * 
 	 * @return A list of all categories. Null in error case.
 	 */
 	public EList<Category> getCategories()
 	{
 		IQueryResult result = getItemsMatchingCondition(CategoryImpl.isMainCategoryCondition());
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only categories
 		@SuppressWarnings("unchecked")
 		EList<Category> categories = new BasicEList<Category>((Collection<? extends Category>) result.getEObjects());
 		
 		return categories;
 	}
 
 	/**
 	 * Returns all Categories matching the provided condition.
 	 *
 	 * @condition The condition to filter the Categories.
 	 * @return All Categories of this DataSet matching the condition.
 	 * @generated
 	 */
 	public EList<Category> getCategoriesMatchingCondition(EObjectCondition condition) {
 		if (condition == null) return null;
 		IQueryResult result = getItemsMatchingCondition(Category.isTypeCondition.AND(condition));
 		EList<Category> resList = new BasicEList<Category>();
 		if (result != null) {
 			for (EObject eo: result.getEObjects()) {
 				resList.add((Category) eo);
 			}
 		}
 		return resList;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 *  * Finds the meta tag with the given name in the items contained in this data set. The comparison is case sensitive.
 	 * 
 	 * @return The meta tag, or null if it doesn't exist.
 	 * <!-- end-user-doc -->
 	 */
 	public MetaTag getMetaTag(String name) {
 		if(name == null)
 		{
 			return null;
 		}
 		
 		// create case sensitive string comparison condition
 		StringValue stringValueCondition = new StringValue(name, true);
 		
 		// create a condition that every item of type MetaTag matches
 		EObjectCondition isMetaTagCondition = MetaTag.isTypeCondition;
 		
 		EObjectAttributeValueCondition valueEqualCondition = new EObjectAttributeValueCondition(DataPackage.eINSTANCE.getMetaTag_Name(), stringValueCondition);
 		
 		// query results matching both condition (and)
 		IQueryResult result = getItemsMatchingCondition(isMetaTagCondition.AND(valueEqualCondition));
 		
 		if(result != null && result.iterator().hasNext())
 		{
 			// There should be exactly one tag, return the first in error case
 			return (MetaTag)result.iterator().next();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Finds the Tag with the given name in the items contained in this data set. The comparison is case sensitive.
 	 * 
 	 * @return The tag, or null if it doesn't exist.
 	 * <!-- end-user-doc -->
 	 */
 	public Tag getTag(String name) {
 		if(name == null)
 		{
 			return null;
 		}
 		
 		// create case sensitive string comparison condition
 		StringValue stringValueCondition = new StringValue(name, true);
 		
 		// create a condition that every item of type Tag matches
 		EObjectCondition isTagCondition = Tag.isTypeCondition;
 		
 		EObjectAttributeValueCondition valueEqualCondition = new EObjectAttributeValueCondition(DataPackage.eINSTANCE.getClassification_Name(), stringValueCondition);
 		
 		// query results matching both condition (and)
 		IQueryResult result = getItemsMatchingCondition(isTagCondition.AND(valueEqualCondition));
 		
 		if(result != null && result.iterator().hasNext())
 		{
 			// There should be exactly one tag, return the first in error case
 			return (Tag)result.iterator().next();
 		}
 		
 		return null;
 	}
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Finds the Category with the given name in the items contained in this data set. The comparison is case sensitive.
 	 * 
 	 * @return The Category, or null if it doesn't exist.
 	 * <!-- end-user-doc -->
 	 */
 	public Category getCategory(String name) {
 
 		// TODO: check if it shouldn't be possible that maybe more categories with the same
 		// name could exist if they have different parent categories !!
 		
 		if(name == null)
 		{
 			return null;
 		}
 		
 		// create case sensitive string comparison condition
 		StringValue stringValueCondition = new StringValue(name, true);
 		
 		// create a condition that every item of type Category matches
 		EObjectCondition itemCondition = Category.isTypeCondition;
 		
 		EObjectAttributeValueCondition valueEqualCondition = new EObjectAttributeValueCondition(DataPackage.eINSTANCE.getClassification_Name(), stringValueCondition);
 		
 		// query results matching both condition (and)
 		IQueryResult result = getItemsMatchingCondition(itemCondition.AND(valueEqualCondition));
 		
 		if(result != null && result.iterator().hasNext())
 		{
 			// There should be exactly one Category, return the first in error case
 			return (Category)result.iterator().next();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Equal to {@link DataSet#getPersons()}. Method exists just for naming conventions.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Person> getAllPersons() {
 		return getPersons();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Queries all Items and returns all content objects.
 	 * @see DataSet#getContents() to get the main contents.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Content> getAllContents() {
 		IQueryResult result = getItemsMatchingCondition(Content.isTypeCondition);
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only contents
 		@SuppressWarnings("unchecked")
 		EList<Content> contents = new BasicEList<Content>((Collection<? extends Content>) result.getEObjects());
 		
 		return contents;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Queries the contained Items and returns all organisations.
 	 * 
 	 * @return A list of all organisations. Null in error case.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Organisation> getAllOrganisations() {
 		IQueryResult result = getItemsMatchingCondition(Organisation.isTypeCondition);
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only organisations
 		@SuppressWarnings("unchecked")
 		EList<Organisation> organisations = new BasicEList<Organisation>((Collection<? extends Organisation>) result.getEObjects());
 		
 		return organisations;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Queries the contained Items and returns all categories.
 	 * 
 	 * @return A list of all categories. Null in error case.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Category> getAllCategories() {
 		IQueryResult result = getItemsMatchingCondition(Category.isTypeCondition);
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only categories
 		@SuppressWarnings("unchecked")
 		EList<Category> categories = new BasicEList<Category>((Collection<? extends Category>) result.getEObjects());
 		
 		return categories;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Equals to {@link DataSetImpl#getTags()}, duplicated cause of naming conventions.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Tag> getAllTags() {
 		return getTags();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Item> getItemsModifiedSince(Date date) {
 		if(date == null)
 		{
 			return null;
 		}	
 		
 		EList<Item> results = new BasicEList<Item>();
 		
 		EList<Item> itemList = this.getItems();
 		
 		// check modification date of all items
 		// TODO create more performant query with EMF Query or OCL
 		for(Item item : itemList)
 		{
 			Date modificationDate = item.getLastModified();
 			
 			if(modificationDate != null && modificationDate.after(date))
 			{
 				results.add(item);
 			}
 		}
 		
 		return results;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Equals to {@link DataSetImpl#getMetaTags()}, duplicated cause of naming conventions.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<MetaTag> getAllMetaTags() {
 		return getMetaTags();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Equals to {@link DataSetImpl#getConnections()}, duplicated cause of naming conventions.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Connection> getAllConnections() {
 		return getConnections();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Logs the message with level {@link LogService#LOG_DEBUG}.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void log(String message) {
 		this.log(message, LogService.LOG_DEBUG);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void log(String message, Integer level) {
		if(level > this.logLevel)
 		{
 			// dont log
 			return;
 		}
 		
 		if (logService != null)
 		{
			logService.log(level, message);
 		} 
 		else
 		{
 			System.out.println(message);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Searches for Persons with the given lastname.
 	 * 
 	 * @return A list of persons. Null in error case.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Person> getPersonsWithLastname(String lastname) {
 			
 		// create case sensitive string comparison condition
 		StringValue stringValueCondition = new StringValue(lastname, true);
 		
 		EObjectAttributeValueCondition valueEqualCondition = new EObjectAttributeValueCondition(DataPackage.eINSTANCE.getPerson_Lastname(), stringValueCondition);
 		
 		IQueryResult result = getItemsMatchingCondition(Person.isTypeCondition.AND(valueEqualCondition));
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only persons
 		@SuppressWarnings("unchecked")
 		EList<Person> persons = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return persons;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Searches for Persons with the given firstname.
 	 * 
 	 * @return A list of persons. Null in error case.
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Person> getPersonsWithFirstName(String firstname) {
 		// create case sensitive string comparison condition
 		StringValue stringValueCondition = new StringValue(firstname, true);
 		
 		EObjectAttributeValueCondition valueEqualCondition = new EObjectAttributeValueCondition(DataPackage.eINSTANCE.getPerson_Firstname(), stringValueCondition);
 		
 		IQueryResult result = getItemsMatchingCondition(Person.isTypeCondition.AND(valueEqualCondition));
 		
 		if(result == null)
 		{
 			return null;
 		}
 		
 		// results are only persons
 		@SuppressWarnings("unchecked")
 		EList<Person> persons = new BasicEList<Person>((Collection<? extends Person>) result.getEObjects());
 		
 		return persons;
 	}
 	
 	/**
 	 * Uses @see {@link DataSetImpl#getItemsMatchingCondition(EObjectCondition)} to deliver items of a given type.
 	 * 
 	 * @param itemType Type of objects wich should be delivered.  
 	 * @return The Items of the given type.
 	 */
 	private IQueryResult getItemsOfType(EClass itemType)
 	{
 		// create a condition that every item with the given type matches
 		EObjectCondition itemCondition = DataPackageImpl.isTypeCondition(itemType);
 		
 		return getItemsMatchingCondition(itemCondition);
 	}
 	
 	/**
 	 * Uses EMF Query to find all Items matching the given condition.
 	 * 
 	 * @param condition The condition all of the resulting items have to match.
 	 * @return The query result with all items matching the given condition. Null if DataSet contains no items or an error occurred.
 	 */
 	private IQueryResult getItemsMatchingCondition(EObjectCondition condition)
 	{
 		return DataPackageImpl.filterItemsMatchingCondition(getItems(), condition);
 	}
 
 	/**
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 */
 		public String getSpicynodesRepresentation() {
 			
 			// TODO this method is only for testing and should be moved to an seperate interface with parameters
 			
 			StringBuffer representation = new StringBuffer();
 			
 			// get line separator
 			String ls = System.getProperty("line.separator");
 			
 			// xml type
 			representation.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + ls);
 			
 			// add central (dataset) node
 			representation.append("<node label=\"Home\">" + ls);
 			
 			// ...  and all tags
 			EList<Tag> allTags = this.getTags();
 			if(allTags != null && !allTags.isEmpty())
 			{
 				// add Tag node
 				representation.append("\t<node label=\"Tags\">" + ls);
 				for(Tag tag : allTags)
 				{
 					representation.append("\t\t<node ");
 					// tag ident for linking
 					representation.append("id=\"" + tag.getIdent() + "\">" + ls);
 					
 					// tag name as label
 					representation.append("\t\t\t<label>" + ls);
 					representation.append("\t\t\t\t<![CDATA[" + tag.getName() + "]]>" + ls);
 					representation.append("\t\t\t</label>" + ls);
 					
 					// and links to all tagged information objects
 					EList<InformationObject> taggedInforamtionObjects = tag.getTagged();
 					if(taggedInforamtionObjects != null && !taggedInforamtionObjects.isEmpty())
 					{
 						// link all information objects
 						for(InformationObject io : taggedInforamtionObjects)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + io.getIdent() + "\" />" + ls);
 						}
 					}
 					// close tag node
 					representation.append("\t\t</node>");
 					// add new line
 					representation.append(ls);
 				}
 				// close all tags node
 				representation.append("\t</node>" + ls);
 			}
 			
 			
 			// ...  and all categories
 			EList<Category> allCategories = this.getAllCategories();
 			if(allCategories != null && !allCategories.isEmpty())
 			{
 				// add Category node
 				representation.append("\t<node label=\"Categories\">" + ls);
 				for(Category category : allCategories)
 				{
 					// category name
 					representation.append("\t\t<node ");
 					// category ident for linking
 					representation.append("id=\"" + category.getIdent() + "\">" + ls);
 					
 					// category name as label
 					representation.append("\t\t\t<label>" + ls);
 					representation.append("\t\t\t\t<![CDATA[" + category.getName() + "]]>" + ls);
 					representation.append("\t\t\t</label>" + ls);
 					
 					// and links to all categorized information objects
 					EList<InformationObject> categorizedInforamtionObjects = category.getCategorized();
 					if(categorizedInforamtionObjects != null && !categorizedInforamtionObjects.isEmpty())
 					{
 						// link all information objects
 						for(InformationObject io : categorizedInforamtionObjects)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + io.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to sub categories
 					EList<Category> subCategories = category.getCategories();
 					if(subCategories != null && !subCategories.isEmpty())
 					{	
 						for(Category subCategory : subCategories)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + subCategory.getIdent() + "\" />" + ls);
 						}
 					}
 					// close category node
 					representation.append("\t\t</node>");
 					
 					// add new line
 					representation.append(ls);
 				}
 				// close all categories node
 				representation.append("\t</node>" + ls);
 			}
 			
 			// ...  and all persons
 			EList<Person> allPersons = this.getPersons();
 			if(allPersons != null && !allPersons.isEmpty())
 			{
 				// add Person node
 				representation.append("\t<node label=\"Persons\">" + ls);
 				for(Person person : allPersons)
 				{
 					representation.append("\t\t<node ");
 					// TODO: additional person informations
 					// person ident for linking
 					representation.append("id=\"" + person.getIdent() + "\">" + ls);
 					
 					// person name as label
 					representation.append("\t\t\t<label>" + ls);
 					representation.append("\t\t\t\t<![CDATA[" + person.getName() + "]]>" + ls);
 					representation.append("\t\t\t</label>" + ls);
 					
 					// links to other persons
 					// TODO add persons over connections
 					EList<Person> linkedPersons = person.getPersons();
 					if(linkedPersons != null && !linkedPersons.isEmpty())
 					{
 						// link all persons
 						for(Person linkedPerson : linkedPersons)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + linkedPerson.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to authored content
 					EList<Content> authoredContent = person.getAuthored();
 					if(authoredContent != null && !authoredContent.isEmpty())
 					{
 						// link all contents
 						for(Content linkedContent : authoredContent)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + linkedContent.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to contributed content
 					EList<Content> contributedContent = person.getContributed();
 					if(contributedContent != null && !contributedContent.isEmpty())
 					{
 						// link all contents
 						for(Content linkedContent : contributedContent)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + linkedContent.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to categories
 					EList<Category> categories = person.getCategories();
 					if(categories != null && !categories.isEmpty())
 					{
 						// link all categories
 						for(Category category : categories)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + category.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to tags
 					EList<Tag> tags = person.getTags();
 					if(tags != null && !tags.isEmpty())
 					{
 						// link all tags
 						for(Tag tag : tags)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + tag.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to organisations
 					EList<Organisation> organisations = person.getOrganisations();
 					if(organisations != null && !tags.isEmpty())
 					{
 						// link all organisations
 						for(Organisation org : organisations)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + org.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// close person node
 					representation.append("\t\t</node>" + ls);
 				}
 				// close persons node
 				representation.append("\t</node>" + ls);
 			}
 			
 			// ...  and all organisations
 			EList<Organisation> allOrganisations = this.getAllOrganisations();
 			if(allOrganisations != null && !allOrganisations.isEmpty())
 			{
 				// add Organisation node
 				representation.append("\t<node label=\"Organizations\">" + ls);
 				for(Organisation organisation : allOrganisations)
 				{
 					representation.append("\t\t<node ");
 					// TODO: additional organisation informations
 					// organisation ident for linking
 					representation.append("id=\"" + organisation.getIdent() + "\">" + ls);
 					
 					// organisation name as label
 					representation.append("\t\t\t<label>" + ls);
 					representation.append("\t\t\t\t<![CDATA[" + organisation.getName() + "]]>" + ls);
 					representation.append("\t\t\t</label>" + ls);
 					
 					// links to persons
 					// TODO decide between leader and participants
 					// TODO add persons over connections
 					EList<Person> linkedPersons = organisation.getPersons();
 					if(linkedPersons != null && !linkedPersons.isEmpty())
 					{
 						// link all persons
 						for(Person linkedPerson : linkedPersons)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + linkedPerson.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to authored and contributed content of organisation members
 					EList<Content> authoredContent = organisation.getContents();
 					if(authoredContent != null && !authoredContent.isEmpty())
 					{
 						// link all contents
 						for(Content linkedContent : authoredContent)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + linkedContent.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to categories
 					EList<Category> categories = organisation.getCategories();
 					if(categories != null && !categories.isEmpty())
 					{
 						// link all categories
 						for(Category category : categories)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + category.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to tags
 					EList<Tag> tags = organisation.getTags();
 					if(tags != null && !tags.isEmpty())
 					{
 						// link all tags
 						for(Tag tag : tags)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + tag.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to sub organisations
 					EList<Organisation> organisations = organisation.getOrganisations();
 					if(organisations != null && !tags.isEmpty())
 					{
 						// link all organisations
 						for(Organisation org : organisations)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + org.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// close orgnanisation node
 					representation.append("\t\t</node>" + ls);
 				}
 				// close organisations node
 				representation.append("\t</node>" + ls);
 			}
 			
 			// ...  and main contents
 			EList<Content> allContents = this.getContents();
 			if(allContents != null && !allContents.isEmpty())
 			{
 				// add Content node
 				representation.append("\t<node label=\"Contents\">" + ls);
 				for(Content content : allContents)
 				{
 					// name must be available
 					if(content.getName() == null || content.getName().isEmpty()) continue;
 					
 					representation.append("\t\t<node ");
 					// content ident for linking
 					representation.append("id=\"" + content.getIdent() + "\">" + ls);
 					
 					// content name as label
 					representation.append("\t\t\t<label>" + ls);
 					representation.append("\t\t\t\t<![CDATA[" + content.getName() + "]]>" + ls);
 					representation.append("\t\t\t</label>" + ls);
 					
 					// content value
 	//				String value = content.getStringValue();
 	//				if(value != null && !value.isEmpty())
 	//				{
 	//					// add new line
 	//					representation.append(ls);
 	//					representation.append("::" + value + "");
 	//					// add new line
 	//					representation.append(ls);
 	//				}
 					
 					// links to persons
 					// TODO decide between author and contributors
 					// TODO add persons over connections
 					EList<Person> linkedPersons = content.getPersons();
 					if(linkedPersons != null && !linkedPersons.isEmpty())
 					{
 						// link all persons
 						for(Person linkedPerson : linkedPersons)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + linkedPerson.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					
 					// links to categories
 					EList<Category> categories = content.getCategories();
 					if(categories != null && !categories.isEmpty())
 					{
 						// link all categories
 						for(Category category : categories)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + category.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					// links to tags
 					EList<Tag> tags = content.getTags();
 					if(tags != null && !tags.isEmpty())
 					{
 						// link all tags
 						for(Tag tag : tags)
 						{
 							// add link
 							representation.append("\t\t\t<node href=\"" + tag.getIdent() + "\" />" + ls);
 						}
 					}
 					
 					
 					// direct subcontents
 					EList<Content> subContents = content.getContents();
 					if(subContents != null && !subContents.isEmpty())
 					{
 						// add all sub contents
 						for(Content subContent : subContents)
 						{
 							// content name
 							representation.append("\t\t\t<node label=\"" + subContent.getName() + "\" ");
 							// content ident for linking
 							representation.append("id=\"" + subContent.getIdent() + "\">" + ls);
 							
 //							// content value
 //							representation.append("::" + subContent.getStringValue());
 						}
 					}
 					
 					// close content node
 					representation.append("\t\t</node>" + ls);
 				}
 				// close contents node
 				representation.append("\t</node>" + ls);
 			}
 			// close central node
 			representation.append("</node>" + ls);
 			
 //			try {
 				return representation.toString();
 				//return new String(representation.toString().getBytes("UTF-8"));
 //			} catch (UnsupportedEncodingException e) {
 //				// return empty result
 //				return "";
 //			}	
 		}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EList<Connection> getConnectionsBetweenInformationObjectsOfDifferentCategories() {
 		
 		EList<Connection> allConnections = this.getConnections();
 		if(allConnections == null || allConnections.isEmpty())
 		{
 			// nothing to do
 			return allConnections;
 		}
 		
 		EList<Connection> resultingConnections = new UniqueEList<Connection>();
 		
 		// look at all existing connections
 		for(Connection connection : allConnections)
 		{
 			InformationObject from = connection.getFrom();
 			InformationObject to   = connection.getTo();
 			
 			if(from == null || to == null)
 			{
 				// could not ceck
 				continue;
 			}
 			
 			EList<Category> fromCategories = from.getCategories();
 			if(fromCategories == null || fromCategories.isEmpty())
 			{
 				// no categories
 				continue;
 			}
 			
 			EList<Category> toCategories   = to.getCategories();
 			if(toCategories == null || toCategories.isEmpty())
 			{
 				// no categories
 				continue;
 			}
 			
 			// calculate common categories
 			EList<Category> commonCategories = new BasicEList<Category>(toCategories);
 			commonCategories.retainAll(fromCategories);
 			if(commonCategories.isEmpty())
 			{
 				// they have no common categories but everyone has at least one
 				resultingConnections.add(connection);
 			}
 		}
 		
 		return resultingConnections;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<InformationObject> getInformationObjectsModifiedSince(Date date) {
 		// Check if input is defined
 		if(getItemsModifiedSince(date) == null) {
 			return null;
 		}
 		
 		EObjectCondition oclCondition = null;
 		String oclStatement = "true";
 		try {
 			oclCondition = new BooleanOCLCondition<EClassifier, EClass, EObject>( 	getOclEnvironment(),
 																					oclStatement,
 																					DataPackageImpl.eINSTANCE.getInformationObject());		
 		}
 		catch (ParserException e) {
 			log("Malformed ocl statement: " + oclStatement + " (" + e.getMessage() + ")", LogService.LOG_ERROR);
 			return null;
 		}
 	
 		IQueryResult result = DataPackageImpl.filterItemsMatchingCondition(getItemsModifiedSince(date), oclCondition.AND(InformationObject.isTypeCondition));
 
 		if(result == null) {
 			return new BasicEList<InformationObject>();
 		}
 		
 		// results are only InformationObjects
 		@SuppressWarnings("unchecked")
 		EList<InformationObject> objects = new BasicEList<InformationObject>((Collection<? extends InformationObject>) result.getEObjects());
 		
 		return objects;	
 	
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.data.DataSet#setLogService(org.osgi.service.log.LogService)
 	 */
 	public void setLogService(LogService logService)
 	{
 		this.logService = logService;
 	}
 	
 } //DataSetImpl
