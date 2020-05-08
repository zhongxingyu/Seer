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
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
 import org.eclipse.emf.query.conditions.eobjects.EObjectTypeRelationCondition;
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
  * An implementation of the model object '<em><b>Location</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getStreet <em>Street</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getHouseNumber <em>House Number</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getZipCode <em>Zip Code</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getCountry <em>Country</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getLongitude <em>Longitude</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getLatitude <em>Latitude</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getCity <em>City</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getIndoorLocations <em>Indoor Locations</em>}</li>
  *   <li>{@link org.sociotech.communitymashup.data.impl.LocationImpl#getState <em>State</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class LocationImpl extends MetaInformationImpl implements Location {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\nContributors:\n \tPeter Lachenmaier - Design and initial implementation";
 	/**
 	 * The default value of the '{@link #getStreet() <em>Street</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getStreet()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String STREET_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getStreet() <em>Street</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getStreet()
 	 * @generated
 	 * @ordered
 	 */
 	protected String street = STREET_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getHouseNumber() <em>House Number</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getHouseNumber()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String HOUSE_NUMBER_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getHouseNumber() <em>House Number</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getHouseNumber()
 	 * @generated
 	 * @ordered
 	 */
 	protected String houseNumber = HOUSE_NUMBER_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getZipCode() <em>Zip Code</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getZipCode()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ZIP_CODE_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getZipCode() <em>Zip Code</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getZipCode()
 	 * @generated
 	 * @ordered
 	 */
 	protected String zipCode = ZIP_CODE_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getCountry() <em>Country</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCountry()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String COUNTRY_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getCountry() <em>Country</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCountry()
 	 * @generated
 	 * @ordered
 	 */
 	protected String country = COUNTRY_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getLongitude() <em>Longitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLongitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LONGITUDE_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getLongitude() <em>Longitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLongitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected String longitude = LONGITUDE_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getLatitude() <em>Latitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLatitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LATITUDE_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getLatitude() <em>Latitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLatitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected String latitude = LATITUDE_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getCity() <em>City</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCity()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CITY_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getCity() <em>City</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCity()
 	 * @generated
 	 * @ordered
 	 */
 	protected String city = CITY_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getIndoorLocations() <em>Indoor Locations</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIndoorLocations()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<IndoorLocation> indoorLocations;
 
 	/**
 	 * The default value of the '{@link #getState() <em>State</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getState()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String STATE_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getState() <em>State</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getState()
 	 * @generated
 	 * @ordered
 	 */
 	protected String state = STATE_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected LocationImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return DataPackage.Literals.LOCATION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getStreet() {
 		return street;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setStreet(String newStreet) {
 		String oldStreet = street;
 		street = newStreet;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__STREET, oldStreet, street));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getHouseNumber() {
 		return houseNumber;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setHouseNumber(String newHouseNumber) {
 		String oldHouseNumber = houseNumber;
 		houseNumber = newHouseNumber;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__HOUSE_NUMBER, oldHouseNumber, houseNumber));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getZipCode() {
 		return zipCode;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setZipCode(String newZipCode) {
 		String oldZipCode = zipCode;
 		zipCode = newZipCode;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__ZIP_CODE, oldZipCode, zipCode));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCountry() {
 		return country;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCountry(String newCountry) {
 		String oldCountry = country;
 		country = newCountry;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__COUNTRY, oldCountry, country));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLongitude() {
 		return longitude;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLongitude(String newLongitude) {
 		String oldLongitude = longitude;
 		longitude = newLongitude;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__LONGITUDE, oldLongitude, longitude));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLatitude() {
 		return latitude;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLatitude(String newLatitude) {
 		String oldLatitude = latitude;
 		latitude = newLatitude;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__LATITUDE, oldLatitude, latitude));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCity() {
 		return city;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCity(String newCity) {
 		String oldCity = city;
 		city = newCity;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__CITY, oldCity, city));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<IndoorLocation> getIndoorLocations() {
 		if (indoorLocations == null) {
 			indoorLocations = new EObjectWithInverseResolvingEList<IndoorLocation>(IndoorLocation.class, this, DataPackage.LOCATION__INDOOR_LOCATIONS, DataPackage.INDOOR_LOCATION__LOCATION);
 		}
 		return indoorLocations;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getState() {
 		return state;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setState(String newState) {
 		String oldState = state;
 		state = newState;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.LOCATION__STATE, oldState, state));
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
 			case DataPackage.LOCATION__INDOOR_LOCATIONS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getIndoorLocations()).basicAdd(otherEnd, msgs);
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
 			case DataPackage.LOCATION__INDOOR_LOCATIONS:
 				return ((InternalEList<?>)getIndoorLocations()).basicRemove(otherEnd, msgs);
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
 			case DataPackage.LOCATION__STREET:
 				return getStreet();
 			case DataPackage.LOCATION__HOUSE_NUMBER:
 				return getHouseNumber();
 			case DataPackage.LOCATION__ZIP_CODE:
 				return getZipCode();
 			case DataPackage.LOCATION__COUNTRY:
 				return getCountry();
 			case DataPackage.LOCATION__LONGITUDE:
 				return getLongitude();
 			case DataPackage.LOCATION__LATITUDE:
 				return getLatitude();
 			case DataPackage.LOCATION__CITY:
 				return getCity();
 			case DataPackage.LOCATION__INDOOR_LOCATIONS:
 				return getIndoorLocations();
 			case DataPackage.LOCATION__STATE:
 				return getState();
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
 			case DataPackage.LOCATION__STREET:
 				setStreet((String)newValue);
 				return;
 			case DataPackage.LOCATION__HOUSE_NUMBER:
 				setHouseNumber((String)newValue);
 				return;
 			case DataPackage.LOCATION__ZIP_CODE:
 				setZipCode((String)newValue);
 				return;
 			case DataPackage.LOCATION__COUNTRY:
 				setCountry((String)newValue);
 				return;
 			case DataPackage.LOCATION__LONGITUDE:
 				setLongitude((String)newValue);
 				return;
 			case DataPackage.LOCATION__LATITUDE:
 				setLatitude((String)newValue);
 				return;
 			case DataPackage.LOCATION__CITY:
 				setCity((String)newValue);
 				return;
 			case DataPackage.LOCATION__INDOOR_LOCATIONS:
 				getIndoorLocations().clear();
 				getIndoorLocations().addAll((Collection<? extends IndoorLocation>)newValue);
 				return;
 			case DataPackage.LOCATION__STATE:
 				setState((String)newValue);
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
 			case DataPackage.LOCATION__STREET:
 				setStreet(STREET_EDEFAULT);
 				return;
 			case DataPackage.LOCATION__HOUSE_NUMBER:
 				setHouseNumber(HOUSE_NUMBER_EDEFAULT);
 				return;
 			case DataPackage.LOCATION__ZIP_CODE:
 				setZipCode(ZIP_CODE_EDEFAULT);
 				return;
 			case DataPackage.LOCATION__COUNTRY:
 				setCountry(COUNTRY_EDEFAULT);
 				return;
 			case DataPackage.LOCATION__LONGITUDE:
 				setLongitude(LONGITUDE_EDEFAULT);
 				return;
 			case DataPackage.LOCATION__LATITUDE:
 				setLatitude(LATITUDE_EDEFAULT);
 				return;
 			case DataPackage.LOCATION__CITY:
 				setCity(CITY_EDEFAULT);
 				return;
 			case DataPackage.LOCATION__INDOOR_LOCATIONS:
 				getIndoorLocations().clear();
 				return;
 			case DataPackage.LOCATION__STATE:
 				setState(STATE_EDEFAULT);
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
 			case DataPackage.LOCATION__STREET:
 				return STREET_EDEFAULT == null ? street != null : !STREET_EDEFAULT.equals(street);
 			case DataPackage.LOCATION__HOUSE_NUMBER:
 				return HOUSE_NUMBER_EDEFAULT == null ? houseNumber != null : !HOUSE_NUMBER_EDEFAULT.equals(houseNumber);
 			case DataPackage.LOCATION__ZIP_CODE:
 				return ZIP_CODE_EDEFAULT == null ? zipCode != null : !ZIP_CODE_EDEFAULT.equals(zipCode);
 			case DataPackage.LOCATION__COUNTRY:
 				return COUNTRY_EDEFAULT == null ? country != null : !COUNTRY_EDEFAULT.equals(country);
 			case DataPackage.LOCATION__LONGITUDE:
 				return LONGITUDE_EDEFAULT == null ? longitude != null : !LONGITUDE_EDEFAULT.equals(longitude);
 			case DataPackage.LOCATION__LATITUDE:
 				return LATITUDE_EDEFAULT == null ? latitude != null : !LATITUDE_EDEFAULT.equals(latitude);
 			case DataPackage.LOCATION__CITY:
 				return CITY_EDEFAULT == null ? city != null : !CITY_EDEFAULT.equals(city);
 			case DataPackage.LOCATION__INDOOR_LOCATIONS:
 				return indoorLocations != null && !indoorLocations.isEmpty();
 			case DataPackage.LOCATION__STATE:
 				return STATE_EDEFAULT == null ? state != null : !STATE_EDEFAULT.equals(state);
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
 		result.append(" (street: ");
 		result.append(street);
 		result.append(", houseNumber: ");
 		result.append(houseNumber);
 		result.append(", zipCode: ");
 		result.append(zipCode);
 		result.append(", country: ");
 		result.append(country);
 		result.append(", longitude: ");
 		result.append(longitude);
 		result.append(", latitude: ");
 		result.append(latitude);
 		result.append(", city: ");
 		result.append(city);
 		result.append(", state: ");
 		result.append(state);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * Generates an EObjectCondition to check whether an Object is of the type Location.
 	 * 
 	 * @return An EObjectCondition whether the Object is of the type Location.
 	 * @generated
 	 */
 	public static EObjectCondition generateIsTypeCondition() {
 		return new EObjectTypeRelationCondition(DataPackageImpl.eINSTANCE.getLocation());
 	}
 
 	public String toAttributeMapString() {
 		StringBuffer result = new StringBuffer();
 		result.append(street);
 		result.append(" ");
 		result.append(houseNumber);
 		result.append(", ");
 		result.append(zipCode);
 		result.append(" ");
 		result.append(city);
 		result.append(", ");
 		result.append(country);
 		return result.toString();
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
 		if ( featureName.equalsIgnoreCase("street") )
 			return this.getStreet();		
 		if ( featureName.equalsIgnoreCase("houseNumber") )
 			return this.getHouseNumber();		
 		if ( featureName.equalsIgnoreCase("zipCode") )
 			return this.getZipCode();		
 		if ( featureName.equalsIgnoreCase("country") )
 			return this.getCountry();		
 		if ( featureName.equalsIgnoreCase("longitude") )
 			return this.getLongitude();		
 		if ( featureName.equalsIgnoreCase("latitude") )
 			return this.getLatitude();		
 		if ( featureName.equalsIgnoreCase("city") )
 			return this.getCity();		
 		if ( featureName.equalsIgnoreCase("indoorLocations") )
 			return this.getIndoorLocations();		
 		if ( featureName.equalsIgnoreCase("state") )
 			return this.getState();			
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
 		if ( featureName.equalsIgnoreCase("street") ) {
 				java.lang.String fstreet = null;
 				try {
 					fstreet = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setStreet(fstreet);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("houseNumber") ) {
 				java.lang.String fhouseNumber = null;
 				try {
 					fhouseNumber = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setHouseNumber(fhouseNumber);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("zipCode") ) {
 				java.lang.String fzipCode = null;
 				try {
 					fzipCode = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setZipCode(fzipCode);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("country") ) {
 				java.lang.String fcountry = null;
 				try {
 					fcountry = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setCountry(fcountry);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("longitude") ) {
 				java.lang.String flongitude = null;
 				try {
 					flongitude = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setLongitude(flongitude);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("latitude") ) {
 				java.lang.String flatitude = null;
 				try {
 					flatitude = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setLatitude(flatitude);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("city") ) {
 				java.lang.String fcity = null;
 				try {
 					fcity = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setCity(fcity);
 			return this;
 			}		
 		if ( featureName.equalsIgnoreCase("state") ) {
 				java.lang.String fstate = null;
 				try {
 					fstate = (java.lang.String)value;
 				} catch (ClassCastException e) {
 					throw new WrongArgException("Location.setFeature", "java.lang.String",value.getClass().getName());
 				}
 				this.setState(fstate);
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
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.data.impl.MetaInformationImpl#deleteIfUnused()
 	 */
 	@Override
 	protected void deleteIfUnused() {
 		if(getInformationObjects().isEmpty() && getIndoorLocations().isEmpty())
 		{
 			// delete if no more information objects are extended and no indoor locations connected
 			this.delete();
 		}
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
 		Location location = (Location) item;
 		
 		try
 		{
			EList<InformationObject> myIOs    = this.getDataSet().getInformationObjects();
			EList<InformationObject> otherIOs = this.getDataSet().getInformationObjects();
 			
 			if(!myIOs.containsAll(otherIOs))
 			{
 				// must belong to the same ios
 				return false;
 			}
 		}
 		catch (Exception e)
 		{
 			// if not all accesses work, they are not equal
 			return false;
 		}
 		
 		// two locations are equal if they are on the same position
 		if(location.getLatitude()  != null && !location.getLatitude().isEmpty()  && location.getLatitude().equals(this.getLatitude()) &&
 		   location.getLongitude() != null && !location.getLongitude().isEmpty() && location.getLongitude().equals(this.getLongitude()))
 		{
 			return true;
 		}
 		
 		// or when they have the same non empty string value
 		if(location.getStringValue() != null && !location.getStringValue().isEmpty() && location.getStringValue().equals(this.getStringValue()))
 		{
 			return true;
 		}
 		
 		// or when they have the same address and all is set
 		if(location.getCity()        != null && !location.getCity().isEmpty()        && location.getCity().equals(this.getCity()) &&
 		   location.getZipCode()     != null && !location.getZipCode().isEmpty()     && location.getZipCode().equals(this.getZipCode()) &&
 		   location.getStreet()      != null && !location.getStreet().isEmpty()      && location.getStreet().equals(this.getStreet()) &&
 		   location.getHouseNumber() != null && !location.getHouseNumber().isEmpty() && location.getHouseNumber().equals(this.getHouseNumber()))
 			
 		{
 			return true;
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
 } //LocationImpl
