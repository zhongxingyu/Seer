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
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
 import org.eclipse.emf.query.conditions.eobjects.EObjectTypeRelationCondition;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.data.Attachment;
 import org.sociotech.communitymashup.data.Binary;
 import org.sociotech.communitymashup.data.Category;
 import org.sociotech.communitymashup.data.Classification;
 import org.sociotech.communitymashup.data.Connection;
 import org.sociotech.communitymashup.data.Content;
 import org.sociotech.communitymashup.data.DataPackage;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.DeletedItem;
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
  * An implementation of the model object '<em><b>Attachment</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.sociotech.communitymashup.data.impl.AttachmentImpl#getFileUrl <em>File Url</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.AttachmentImpl#getCachedFileUrl <em>Cached File Url</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.AttachmentImpl#getCachedOnly <em>Cached Only</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.AttachmentImpl#getFileExtension <em>File Extension</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.AttachmentImpl#getFileIdentifier <em>File Identifier</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.AttachmentImpl#getCachedFileName <em>Cached File Name</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class AttachmentImpl extends ExtensionImpl implements Attachment {
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\nContributors:\n \tPeter Lachenmaier - Design and initial implementation";
 	/**
 	 * The default value of the '{@link #getFileUrl() <em>File Url</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFileUrl()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String FILE_URL_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getFileUrl() <em>File Url</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFileUrl()
 	 * @generated
 	 * @ordered
 	 */
 	protected String fileUrl = FILE_URL_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCachedFileUrl() <em>Cached File Url</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCachedFileUrl()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CACHED_FILE_URL_EDEFAULT = null;
 
 	/**
 	 * The default value of the '{@link #getCachedOnly() <em>Cached Only</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCachedOnly()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Boolean CACHED_ONLY_EDEFAULT = Boolean.FALSE;
 	/**
 	 * The cached value of the '{@link #getCachedOnly() <em>Cached Only</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCachedOnly()
 	 * @generated
 	 * @ordered
 	 */
 	protected Boolean cachedOnly = CACHED_ONLY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getFileExtension() <em>File Extension</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFileExtension()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String FILE_EXTENSION_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getFileExtension() <em>File Extension</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFileExtension()
 	 * @generated
 	 * @ordered
 	 */
 	protected String fileExtension = FILE_EXTENSION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getFileIdentifier() <em>File Identifier</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFileIdentifier()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String FILE_IDENTIFIER_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getFileIdentifier() <em>File Identifier</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFileIdentifier()
 	 * @generated
 	 * @ordered
 	 */
 	protected String fileIdentifier = FILE_IDENTIFIER_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCachedFileName() <em>Cached File Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCachedFileName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CACHED_FILE_NAME_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getCachedFileName() <em>Cached File Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCachedFileName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String cachedFileName = CACHED_FILE_NAME_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected AttachmentImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->	
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return DataPackage.Literals.ATTACHMENT;
 	}
 
 	/**
 	 * Shows if a download was attempted. 
 	 */
 	private boolean triedToDownload = false;
 	
 	/**
 	 * Shows if cached file exists 
 	 */
 	private boolean cachedFileExists = false;
 	
 	/**
 	 * Non persistent url of the cached file 
 	 */
 	private String cachedFileUrl = null;
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public String getFileUrl() {
 		
 		DataSet dataSet = this.getDataSet();
 		
 		if(getCachedOnly()) {
 			// if only cache can be valid
 			return getCachedFileUrl();
 		}
 		
 		if(dataSet == null || dataSet.getCacheFileAttachements() == false)
 		{
 			return fileUrl;
 		}	
 		
 		String cachedFileUrl = this.getCachedFileUrl();
 		if(cachedFileUrl != null)
 		{
 			// if cache is available return the url of the cached file
 			return cachedFileUrl;
 		}
 		
 		// return file url if not yet cached
 		return fileUrl;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public void setFileUrl(String newFileUrl) {
 		String oldFileUrl = getFileUrl();
 		if(oldFileUrl != null && oldFileUrl.equals(newFileUrl))
 		{
 			return;
 		}
 		
 		fileUrl = newFileUrl;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.ATTACHMENT__FILE_URL, oldFileUrl, fileUrl));
 		
 		// dont know if file exists
 		cachedFileExists = false;
 		triedToDownload = false;
 				
 		// start downloading immediately if url set and this file should be available only localy
 		if(getCachedOnly())
 		{
 			this.cacheFile();
 		}
 	}
 
 	/**
 	 * Starts file caching
 	 */
 	private void cacheFile() {
 		// simply access getCachedFileUrl
 		this.getCachedFileUrl();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public String getCachedFileUrl() {
 		// url string of file
 		DataSet dataSet = this.getDataSet();
 		
 		if(dataSet == null || fileUrl == null || fileUrl.equals(""))
 		{
 			return null;
 		}
 		
 		if(dataSet.getCacheFileAttachements() == false)
 		{
 			// caching disabled
 			return null;
 		}
 		
 		if(cachedFileUrl != null) {
 			return cachedFileUrl;
 		}
 		
 		String cacheFileName = this.cachedFileName;
 		
 		if(cacheFileName == null)
 		{
 			// get original url
 			URL origURL = null;
 			try 
 			{
 				origURL = new URL(fileUrl);
 			}
 			catch (MalformedURLException e)
 			{
 				// no correct url set
 				return null;
 			}
 			
 			// create the unique file name
 			cacheFileName = createCacheFileName(origURL);
 		}
 		
 		String cacheFolder = dataSet.getCacheFolder();
 		
 		if(cacheFolder == null || cacheFolder.equals(""))
 		{
 			// set cache folder to system temporary directory
 			cacheFolder = System.getProperty("java.io.tmpdir");
 		}
 		
 		if(!cacheFolder.endsWith("/"))
 		{
 			// folder should always end with a slash
 			cacheFolder += "/";
 		}
 		
 		if(!cacheFolder.startsWith("/"))
 		{
 			// folder should always start with a slash
 			cacheFolder = "/" + cacheFolder;
 		}
 		
 		// determine cache file url
 		
 		URL newCacheFileURL;
 		// check url
 		try
 		{
 			newCacheFileURL = new URL("file://" + cacheFolder + cacheFileName);
 		}
 		catch (MalformedURLException e) {
 			// could not create Url
 			return null;
 		}
 		
 		cachedFileUrl = newCacheFileURL.toString();
 		// download file if not happened before
 		this.cacheFile(cachedFileUrl);
 		
 		return cachedFileUrl;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Boolean getCachedOnly() {
 		return cachedOnly;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public void setCachedOnly(Boolean newCachedOnly) {
 		Boolean oldCachedOnly = cachedOnly;
 		cachedOnly = newCachedOnly;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.ATTACHMENT__CACHED_ONLY, oldCachedOnly, cachedOnly));
 		
 		// start downloading immediately if url set to get local version
 		this.cacheFile();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getFileExtension() {
 		return fileExtension;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public void setFileExtension(String newFileExtension) {
 		String oldFileExtension = fileExtension;
 		
 		// replace all dots and set it to lowercase
 		if(fileExtension != null)
 		{
 			newFileExtension.replaceAll(".", "");
 			newFileExtension = newFileExtension.toLowerCase();
 		}
 		
 		fileExtension = newFileExtension;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.ATTACHMENT__FILE_EXTENSION, oldFileExtension, fileExtension));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getFileIdentifier() {
 		return fileIdentifier;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFileIdentifier(String newFileIdentifier) {
 		String oldFileIdentifier = fileIdentifier;
 		fileIdentifier = newFileIdentifier;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.ATTACHMENT__FILE_IDENTIFIER, oldFileIdentifier, fileIdentifier));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public String getCachedFileName() {
 		if(this.getDataSet() != null && this.getDataSet().getCacheFileAttachements() == false)
 		{
 			// caching disabled
 			// reset cached file name
			//this.cachedFileName = null;
 			return null;
 		}
 		// starting caching
 		cacheFile();
 		
 		return cachedFileName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCachedFileName(String newCachedFileName) {
 		String oldCachedFileName = cachedFileName;
 		cachedFileName = newCachedFileName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.ATTACHMENT__CACHED_FILE_NAME, oldCachedFileName, cachedFileName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public String getOriginalFileUrl() {
 		return fileUrl;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Attachment reloadFile() {
 		if(getCachedOnly()) {
 			// do nothing when cached only is active
 			// file cannot be loaded
 			return this;
 		}
 		
 		if(cachedFileExists || cachedFileUrl != null) {
 			File existingFile;
 			try {
 				existingFile = new File(new URI(cachedFileUrl));
 				if(existingFile.exists()) {
 					// delete old cache file
 					existingFile.delete();
 				}
 			} catch (Exception e) {
 				// do nothing
 				// cached file will be invalidated
 			}// (cachedFileUrl.replaceFirst("file:", ""));
 			// reset indicators
 			triedToDownload = false;
 			cachedFileExists = false;
 			cachedFileUrl = null;
 		}
 		
 		// load by getting file url
 		getFileUrl();
 		
 		return this;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case DataPackage.ATTACHMENT__FILE_URL:
 				return getFileUrl();
 			case DataPackage.ATTACHMENT__CACHED_FILE_URL:
 				return getCachedFileUrl();
 			case DataPackage.ATTACHMENT__CACHED_ONLY:
 				return getCachedOnly();
 			case DataPackage.ATTACHMENT__FILE_EXTENSION:
 				return getFileExtension();
 			case DataPackage.ATTACHMENT__FILE_IDENTIFIER:
 				return getFileIdentifier();
 			case DataPackage.ATTACHMENT__CACHED_FILE_NAME:
 				return getCachedFileName();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case DataPackage.ATTACHMENT__FILE_URL:
 				setFileUrl((String)newValue);
 				return;
 			case DataPackage.ATTACHMENT__CACHED_ONLY:
 				setCachedOnly((Boolean)newValue);
 				return;
 			case DataPackage.ATTACHMENT__FILE_EXTENSION:
 				setFileExtension((String)newValue);
 				return;
 			case DataPackage.ATTACHMENT__FILE_IDENTIFIER:
 				setFileIdentifier((String)newValue);
 				return;
 			case DataPackage.ATTACHMENT__CACHED_FILE_NAME:
 				setCachedFileName((String)newValue);
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
 			case DataPackage.ATTACHMENT__FILE_URL:
 				setFileUrl(FILE_URL_EDEFAULT);
 				return;
 			case DataPackage.ATTACHMENT__CACHED_ONLY:
 				setCachedOnly(CACHED_ONLY_EDEFAULT);
 				return;
 			case DataPackage.ATTACHMENT__FILE_EXTENSION:
 				setFileExtension(FILE_EXTENSION_EDEFAULT);
 				return;
 			case DataPackage.ATTACHMENT__FILE_IDENTIFIER:
 				setFileIdentifier(FILE_IDENTIFIER_EDEFAULT);
 				return;
 			case DataPackage.ATTACHMENT__CACHED_FILE_NAME:
 				setCachedFileName(CACHED_FILE_NAME_EDEFAULT);
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
 			case DataPackage.ATTACHMENT__FILE_URL:
 				return FILE_URL_EDEFAULT == null ? fileUrl != null : !FILE_URL_EDEFAULT.equals(fileUrl);
 			case DataPackage.ATTACHMENT__CACHED_FILE_URL:
 				return CACHED_FILE_URL_EDEFAULT == null ? getCachedFileUrl() != null : !CACHED_FILE_URL_EDEFAULT.equals(getCachedFileUrl());
 			case DataPackage.ATTACHMENT__CACHED_ONLY:
 				return CACHED_ONLY_EDEFAULT == null ? cachedOnly != null : !CACHED_ONLY_EDEFAULT.equals(cachedOnly);
 			case DataPackage.ATTACHMENT__FILE_EXTENSION:
 				return FILE_EXTENSION_EDEFAULT == null ? fileExtension != null : !FILE_EXTENSION_EDEFAULT.equals(fileExtension);
 			case DataPackage.ATTACHMENT__FILE_IDENTIFIER:
 				return FILE_IDENTIFIER_EDEFAULT == null ? fileIdentifier != null : !FILE_IDENTIFIER_EDEFAULT.equals(fileIdentifier);
 			case DataPackage.ATTACHMENT__CACHED_FILE_NAME:
 				return CACHED_FILE_NAME_EDEFAULT == null ? cachedFileName != null : !CACHED_FILE_NAME_EDEFAULT.equals(cachedFileName);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
 		switch (operationID) {
 			case DataPackage.ATTACHMENT___GET_ORIGINAL_FILE_URL:
 				return getOriginalFileUrl();
 			case DataPackage.ATTACHMENT___RELOAD_FILE:
 				return reloadFile();
 		}
 		return super.eInvoke(operationID, arguments);
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
 		result.append(" (fileUrl: ");
 		result.append(fileUrl);
 		result.append(", cachedOnly: ");
 		result.append(cachedOnly);
 		result.append(", fileExtension: ");
 		result.append(fileExtension);
 		result.append(", fileIdentifier: ");
 		result.append(fileIdentifier);
 		result.append(", cachedFileName: ");
 		result.append(cachedFileName);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * Generates an EObjectCondition to check whether an Object is of the type Attachment.
 	 * 
 	 * @return An EObjectCondition whether the Object is of the type Attachment.
 	 * @generated
 	 */
 	public static EObjectCondition generateIsTypeCondition() {
 		EObjectCondition result = new EObjectTypeRelationCondition(DataPackageImpl.eINSTANCE.getAttachment());
 		return result.OR(Image.isTypeCondition).OR(Document.isTypeCondition).OR(Transformation.isTypeCondition).OR(Video.isTypeCondition).OR(Binary.isTypeCondition);
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
 		if ( featureName.equalsIgnoreCase("fileUrl") )
 			return this.getFileUrl();		
 		if ( featureName.equalsIgnoreCase("cachedFileUrl") )
 			return this.getCachedFileUrl();		
 		if ( featureName.equalsIgnoreCase("cachedOnly") )
 			return this.getCachedOnly();		
 		if ( featureName.equalsIgnoreCase("fileExtension") )
 			return this.getFileExtension();		
 		if ( featureName.equalsIgnoreCase("fileIdentifier") )
 			return this.getFileIdentifier();		
 		if ( featureName.equalsIgnoreCase("cachedFileName") )
 			return this.getCachedFileName();			
 		return super.getFeature(featureName); 
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
 		if ( featureName.equalsIgnoreCase("fileUrl") ) {
 				java.lang.String ffileUrl = null;
 				try {
 					ffileUrl = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Attachment.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setFileUrl(ffileUrl);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("cachedOnly") ) {
 				java.lang.Boolean fcachedOnly = null;
 				try {
 					try {
 						fcachedOnly = (java.lang.Boolean)(RestUtil.fromInput(value));
 					} catch (ClassNotFoundException e) {
 						fcachedOnly = (java.lang.Boolean)value;
 					}
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Attachment.setFeature", "java.lang.Boolean",value.getClass().getName());
 				}
 				this.setCachedOnly(fcachedOnly);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("fileExtension") ) {
 				java.lang.String ffileExtension = null;
 				try {
 					ffileExtension = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Attachment.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setFileExtension(ffileExtension);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("fileIdentifier") ) {
 				java.lang.String ffileIdentifier = null;
 				try {
 					ffileIdentifier = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Attachment.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setFileIdentifier(ffileIdentifier);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("cachedFileName") ) {
 				java.lang.String fcachedFileName = null;
 				try {
 					fcachedFileName = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Attachment.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setCachedFileName(fcachedFileName);
 			return this;
 			}			
 		super.setFeature(featureName, value);
 		return this; 
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
 	protected Object doOperation(RestCommand command) throws ArgNotFoundException, WrongArgException, WrongArgCountException, UnknownOperationException {
 		if ( command.getCommand().equalsIgnoreCase("getOriginalFileUrl")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("Attachment.doOperation", 0, command.getArgCount()); 
 			return this.getOriginalFileUrl();
 		}
 		if ( command.getCommand().equalsIgnoreCase("reloadFile")) {
 			if (command.getArgCount() != 0) throw new WrongArgCountException("Attachment.doOperation", 0, command.getArgCount()); 
 			return this.reloadFile();
 		}	
 		return super.doOperation(command);
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
 			if (o instanceof DeletedItemImpl) {
 				return ((DeletedItem) o).process(input, requestType);
 			}
 			if (o instanceof List) {
 				return RestUtil.listProcess((List<?>) o, input, requestType);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Caches the attached filed and returns the url of the new cached file. Caches only if necessary. Caching needs
 	 * to be activated with {@link DataSet#setCacheFileAttachements(boolean)}
 	 * 
 	 * @return The Url of the new cached file or null if no caching happened.
 	 */
 	protected String cacheFile(String newCacheFileUrl)
 	{	
 		if(cachedFileExists) {
 			// quickly return
 			return newCacheFileUrl;
 		}
 		// TODO do the file caching in a new thread
 	
 		// download file from orig url and write it to the newCacheFileURL
 		try
 		{
 			downloadFile(fileUrl, newCacheFileUrl);
 		}
 		catch (Exception e)
 		{
 			log("could not download file from: " + fileUrl + " to " + newCacheFileUrl + " due to exception ( " + e.getMessage() + "). Deleting attachement.", LogService.LOG_ERROR); 
 			//e.printStackTrace();
 			this.delete();
 			return null;
 		}
 		
 		log("cached file from: " + fileUrl + " to " + newCacheFileUrl, LogService.LOG_DEBUG);
 		
 		return newCacheFileUrl;
 	}
 
 	/**
 	 * Creates the file name for the cached file like <b>filename_md5(url)_extension</b>. If the original file
 	 * has no extension, the cache file gets the extension <b>unknown</b>
 	 * 
 	 * @param origURL The url of the original file
 	 * @return A filename that can be used for local caching.
 	 */
 	private String createCacheFileName(URL origURL)
 	{
 		String cachedfileName = origURL.getFile();
 		
 		// the path is contained in the file name -> strip it
 		if(cachedfileName.contains("/"))
 		{
 			cachedfileName = cachedfileName.substring(cachedfileName.lastIndexOf("/") + 1);
 		}
 		
 		// ecode it to avoid possible query parmeters in file name
 		try {
 			cachedfileName = URLEncoder.encode(cachedfileName, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			log("Could not econde file name " + cachedfileName, LogService.LOG_ERROR);
 		}
 		
 		// split file name to get file extension
 		String[] fileNameParts = cachedfileName.split("\\.");
 		
 		String fileExtension = "unknown";
 		
 		if(fileNameParts.length >= 2)
 		{
 			// default name + extension
 			cachedfileName = fileNameParts[0];
 			fileExtension = fileNameParts[fileNameParts.length-1].trim();
 			
 			// allow only 4 character extensions
 			if(fileExtension.length() > 4)
 			{
 				fileExtension = "unknown";
 			}
 		}
 		
 		// check if file extension manually set
 		if(this.getFileExtension() != null)
 		{
 			fileExtension = this.getFileExtension();
 		}
 		
 		String identifier = this.getFileIdentifier();
 		
 		if(identifier == null || identifier.isEmpty())
 		{
 			// calculate md5 of orig url
 			identifier = origURL.toString();
 		}
 		
 		// calculate md5 to normalize
 		identifier = getMd5Digest(identifier);
 		
 		// create complete filename with extension
 		cachedfileName = "attachement_" + identifier + "." + fileExtension;
 		
 		// set as attribute
 		this.setCachedFileName(cachedfileName);
 		
 		return cachedfileName;
 	}
 	
 	/**
 	 * Calculates the md5 for a given String.
 	 * 
 	 * @param pInput The String to calculate the md5 for.
 	 * @return The calculated md5 as String, null in error case.
 	 */
 	private static String getMd5Digest(String pInput)
 	{
 		try
 		{
 			MessageDigest lDigest = MessageDigest.getInstance("MD5");
 			lDigest.update(pInput.getBytes());
 			BigInteger lHashInt = new BigInteger(1, lDigest.digest());
 			return String.format("%1$032X", lHashInt);
 		}
 		catch(NoSuchAlgorithmException lException)
 		{
 			return null;
 		}
 	}
 	
 	/**
 	 * Uses {@link #downloadFile(String,String,boolean)} to download file.
 	 * Force Download is set to false.
 	 */
 	private void downloadFile(String sourceUrl, String targetUrl)
 	{
 		this.downloadFile(sourceUrl, targetUrl, false);
 	}
 
 	/**
 	 * Downloads a file from the given source url to the given target url.
 	 * 
 	 * @param sourceUrl The url of the source file.
 	 * @param targetUrl The url of the target file. This must be a location with write access.
 	 * @param forceDownload If set to true, the file will be downloaded only if it doesn't still exists. False will overwrite existing files.
 	 */
 	private void downloadFile(String sourceUrl, String targetUrl, boolean forceDownload)
 	{
 		if(sourceUrl == null || targetUrl == null)
 		{
 			return;
 		}
 		
 		if(cachedFileExists && !forceDownload) {
 			return;
 		}
 		
 		URI targetURI;
 		File targetFile;
 		// get file uri
 		try {
 			targetURI =  new URI(targetUrl);
 		} catch (URISyntaxException e) {
 			log("Could not create URI for url " + targetUrl + " (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			return;
 		}
 		
 		// TODO implement forceDownload
 		
 		targetFile = new File(targetURI);
 		
 		// check if file is already downloaded
 		if(targetFile.exists() && !forceDownload)
 		{
 			// setting existance to true to avoid to many file checks
 			cachedFileExists = true;
 			log("target file already exists: " + targetUrl, LogService.LOG_DEBUG);
 			return;
 		}
 		
 		//if(this.cachedOnly && triedToDownload && !forceDownload)
 		if(triedToDownload && !forceDownload)
 		{
 			// don't try downloading again
 			// cached only files may not be accessed again
 			log("skipping download attempt for cache only file: " + sourceUrl, LogService.LOG_DEBUG);
 			return;
 		}
 		
 		// setting tried field to true
 		triedToDownload = true;
 		
 		log("Starting download from " + sourceUrl + " to " + targetFile.getAbsolutePath(), LogService.LOG_DEBUG);
 		
 		try {
 			targetFile.createNewFile();
 		} catch (IOException e) {
 			log("Could not create target file " + targetUrl  + " (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			return;
 		}
 		
 		URL url;
 		try {
 			url = new URL(sourceUrl.replace(" ", "%20"));
 		} catch (MalformedURLException e) {
 			log("Could not create source url " + sourceUrl  + " (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			// need to delete file before returning
 			targetFile.delete();
 			return;
 		}
 		
 		// open remote file
 		URLConnection conn;
 		try {
 			conn = url.openConnection();
 			conn.connect();
 		} catch (IOException e) {
 			log("Could not create connection to url " + url.toString()  + " (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			// need to delete file before returning
 			targetFile.delete();
 			return;
 		}
 		
 		
 		FileOutputStream fos;
 		try {
 			// open target file stream
 			fos = new FileOutputStream(targetFile);
 			byte tmp_buffer[] = new byte[4096];
 
 			InputStream is = conn.getInputStream();
 
 			int n;
 
 			while ((n = is.read(tmp_buffer)) > 0) {
 				fos.write(tmp_buffer, 0, n);
 				fos.flush();
 			}
 			
 			fos.close();
 			
 		} catch (Exception e) {
 			log("Could not create output file " + targetFile.getAbsolutePath()  + " (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			// need to delete file before returning
 			targetFile.delete();
 			return;
 		} 
 	
 		log("Finished download from " + sourceUrl + " to " + targetFile.getAbsolutePath(), LogService.LOG_DEBUG);
 		
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public Boolean isEqualItem(Item item) {
 		
 		if(super.isEqualItem(item))
 		{
 			return true;
 		}
 		
 		if(this == item)
 		{
 			return true;
 		}
 		else if (item == null)
 		{
 			return false;
 		}
 		else if (this.eClass() != item.eClass())
 		{
 			return false;
 		}
 		
 		// cast
 		Attachment attachment = (Attachment) item;
 		
 		if(attachment.getFileUrl() != null && !attachment.getFileUrl().isEmpty() && attachment.getOriginalFileUrl().equals(this.getOriginalFileUrl()))
 		{
 			// attachments are equal if they have the same url and are attached to the same information object(s)
 			// TODO maintain bidirectional reference
 			try
 			{
 				EList<InformationObject> myIOs    = this.getDataSet().getInformationObjectsWithAttachment(this);
 				EList<InformationObject> otherIOs = this.getDataSet().getInformationObjectsWithAttachment(attachment);
 				
 				return myIOs.containsAll(otherIOs);
 			}
 			catch (Exception e)
 			{
 				// if not all accesses work, they are not equal
 				return false;
 			}
 		}
 		
 		// not equal
 		return false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.data.Item#canHaveEqualItem()
 	 */
 	@Override
 	public boolean canHaveEqualItem() {
 		return true;
 	}
 	
 } //AttachmentImpl
