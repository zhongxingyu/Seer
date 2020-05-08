 /**
  * Copyright (c) 2012 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.reqif10.impl;
 
 import java.util.UUID;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 
 import org.eclipse.rmf.reqif10.AlternativeID;
 import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Identifiable</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.rmf.reqif10.impl.IdentifiableImpl#getDesc <em>Desc</em>}</li>
  *   <li>{@link org.eclipse.rmf.reqif10.impl.IdentifiableImpl#getIdentifier <em>Identifier</em>}</li>
  *   <li>{@link org.eclipse.rmf.reqif10.impl.IdentifiableImpl#getLastChange <em>Last Change</em>}</li>
  *   <li>{@link org.eclipse.rmf.reqif10.impl.IdentifiableImpl#getLongName <em>Long Name</em>}</li>
  *   <li>{@link org.eclipse.rmf.reqif10.impl.IdentifiableImpl#getAlternativeID <em>Alternative ID</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class IdentifiableImpl extends EObjectImpl implements Identifiable {
 	/**
 	 * The default value of the '{@link #getDesc() <em>Desc</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDesc()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESC_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDesc() <em>Desc</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDesc()
 	 * @generated
 	 * @ordered
 	 */
 	protected String desc = DESC_EDEFAULT;
 
 	/**
 	 * This is true if the Desc attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean descESet;
 
 	/**
 	 * The default value of the '{@link #getIdentifier() <em>Identifier</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIdentifier()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String IDENTIFIER_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getIdentifier() <em>Identifier</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIdentifier()
 	 * @generated
 	 * @ordered
 	 */
 	protected String identifier = IDENTIFIER_EDEFAULT;
 
 	/**
 	 * This is true if the Identifier attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean identifierESet;
 
 	/**
 	 * The default value of the '{@link #getLastChange() <em>Last Change</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLastChange()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final XMLGregorianCalendar LAST_CHANGE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLastChange() <em>Last Change</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLastChange()
 	 * @generated
 	 * @ordered
 	 */
 	protected XMLGregorianCalendar lastChange = LAST_CHANGE_EDEFAULT;
 
 	/**
 	 * This is true if the Last Change attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean lastChangeESet;
 
 	/**
 	 * The default value of the '{@link #getLongName() <em>Long Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLongName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LONG_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLongName() <em>Long Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLongName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String longName = LONG_NAME_EDEFAULT;
 
 	/**
 	 * This is true if the Long Name attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean longNameESet;
 
 	/**
 	 * The cached value of the '{@link #getAlternativeID() <em>Alternative ID</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAlternativeID()
 	 * @generated
 	 * @ordered
 	 */
 	protected AlternativeID alternativeID;
 
 	/**
 	 * This is true if the Alternative ID containment reference has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean alternativeIDESet;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected IdentifiableImpl() {
 		super();
 		// FIXME (mj) Hard-coded ID-Generation, until we have an extension point for ID generators
		setIdentifier("rmf-" + UUID.randomUUID().toString());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ReqIF10Package.Literals.IDENTIFIABLE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDesc() {
 		return desc;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDesc(String newDesc) {
 		String oldDesc = desc;
 		desc = newDesc;
 		boolean oldDescESet = descESet;
 		descESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ReqIF10Package.IDENTIFIABLE__DESC, oldDesc, desc, !oldDescESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetDesc() {
 		String oldDesc = desc;
 		boolean oldDescESet = descESet;
 		desc = DESC_EDEFAULT;
 		descESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, ReqIF10Package.IDENTIFIABLE__DESC, oldDesc, DESC_EDEFAULT, oldDescESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetDesc() {
 		return descESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIdentifier(String newIdentifier) {
 		String oldIdentifier = identifier;
 		identifier = newIdentifier;
 		boolean oldIdentifierESet = identifierESet;
 		identifierESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ReqIF10Package.IDENTIFIABLE__IDENTIFIER, oldIdentifier, identifier, !oldIdentifierESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetIdentifier() {
 		String oldIdentifier = identifier;
 		boolean oldIdentifierESet = identifierESet;
 		identifier = IDENTIFIER_EDEFAULT;
 		identifierESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, ReqIF10Package.IDENTIFIABLE__IDENTIFIER, oldIdentifier, IDENTIFIER_EDEFAULT, oldIdentifierESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetIdentifier() {
 		return identifierESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public XMLGregorianCalendar getLastChange() {
 		return lastChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLastChange(XMLGregorianCalendar newLastChange) {
 		XMLGregorianCalendar oldLastChange = lastChange;
 		lastChange = newLastChange;
 		boolean oldLastChangeESet = lastChangeESet;
 		lastChangeESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ReqIF10Package.IDENTIFIABLE__LAST_CHANGE, oldLastChange, lastChange, !oldLastChangeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetLastChange() {
 		XMLGregorianCalendar oldLastChange = lastChange;
 		boolean oldLastChangeESet = lastChangeESet;
 		lastChange = LAST_CHANGE_EDEFAULT;
 		lastChangeESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, ReqIF10Package.IDENTIFIABLE__LAST_CHANGE, oldLastChange, LAST_CHANGE_EDEFAULT, oldLastChangeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetLastChange() {
 		return lastChangeESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLongName() {
 		return longName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLongName(String newLongName) {
 		String oldLongName = longName;
 		longName = newLongName;
 		boolean oldLongNameESet = longNameESet;
 		longNameESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ReqIF10Package.IDENTIFIABLE__LONG_NAME, oldLongName, longName, !oldLongNameESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetLongName() {
 		String oldLongName = longName;
 		boolean oldLongNameESet = longNameESet;
 		longName = LONG_NAME_EDEFAULT;
 		longNameESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, ReqIF10Package.IDENTIFIABLE__LONG_NAME, oldLongName, LONG_NAME_EDEFAULT, oldLongNameESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetLongName() {
 		return longNameESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AlternativeID getAlternativeID() {
 		return alternativeID;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAlternativeID(AlternativeID newAlternativeID, NotificationChain msgs) {
 		AlternativeID oldAlternativeID = alternativeID;
 		alternativeID = newAlternativeID;
 		boolean oldAlternativeIDESet = alternativeIDESet;
 		alternativeIDESet = true;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID, oldAlternativeID, newAlternativeID, !oldAlternativeIDESet);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAlternativeID(AlternativeID newAlternativeID) {
 		if (newAlternativeID != alternativeID) {
 			NotificationChain msgs = null;
 			if (alternativeID != null)
 				msgs = ((InternalEObject)alternativeID).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID, null, msgs);
 			if (newAlternativeID != null)
 				msgs = ((InternalEObject)newAlternativeID).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID, null, msgs);
 			msgs = basicSetAlternativeID(newAlternativeID, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else {
 			boolean oldAlternativeIDESet = alternativeIDESet;
 			alternativeIDESet = true;
 			if (eNotificationRequired())
 				eNotify(new ENotificationImpl(this, Notification.SET, ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID, newAlternativeID, newAlternativeID, !oldAlternativeIDESet));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicUnsetAlternativeID(NotificationChain msgs) {
 		AlternativeID oldAlternativeID = alternativeID;
 		alternativeID = null;
 		boolean oldAlternativeIDESet = alternativeIDESet;
 		alternativeIDESet = false;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID, oldAlternativeID, null, oldAlternativeIDESet);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetAlternativeID() {
 		if (alternativeID != null) {
 			NotificationChain msgs = null;
 			msgs = ((InternalEObject)alternativeID).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID, null, msgs);
 			msgs = basicUnsetAlternativeID(msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else {
 			boolean oldAlternativeIDESet = alternativeIDESet;
 			alternativeIDESet = false;
 			if (eNotificationRequired())
 				eNotify(new ENotificationImpl(this, Notification.UNSET, ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID, null, null, oldAlternativeIDESet));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetAlternativeID() {
 		return alternativeIDESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID:
 				return basicUnsetAlternativeID(msgs);
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
 			case ReqIF10Package.IDENTIFIABLE__DESC:
 				return getDesc();
 			case ReqIF10Package.IDENTIFIABLE__IDENTIFIER:
 				return getIdentifier();
 			case ReqIF10Package.IDENTIFIABLE__LAST_CHANGE:
 				return getLastChange();
 			case ReqIF10Package.IDENTIFIABLE__LONG_NAME:
 				return getLongName();
 			case ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID:
 				return getAlternativeID();
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
 			case ReqIF10Package.IDENTIFIABLE__DESC:
 				setDesc((String)newValue);
 				return;
 			case ReqIF10Package.IDENTIFIABLE__IDENTIFIER:
 				setIdentifier((String)newValue);
 				return;
 			case ReqIF10Package.IDENTIFIABLE__LAST_CHANGE:
 				setLastChange((XMLGregorianCalendar)newValue);
 				return;
 			case ReqIF10Package.IDENTIFIABLE__LONG_NAME:
 				setLongName((String)newValue);
 				return;
 			case ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID:
 				setAlternativeID((AlternativeID)newValue);
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
 			case ReqIF10Package.IDENTIFIABLE__DESC:
 				unsetDesc();
 				return;
 			case ReqIF10Package.IDENTIFIABLE__IDENTIFIER:
 				unsetIdentifier();
 				return;
 			case ReqIF10Package.IDENTIFIABLE__LAST_CHANGE:
 				unsetLastChange();
 				return;
 			case ReqIF10Package.IDENTIFIABLE__LONG_NAME:
 				unsetLongName();
 				return;
 			case ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID:
 				unsetAlternativeID();
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
 			case ReqIF10Package.IDENTIFIABLE__DESC:
 				return isSetDesc();
 			case ReqIF10Package.IDENTIFIABLE__IDENTIFIER:
 				return isSetIdentifier();
 			case ReqIF10Package.IDENTIFIABLE__LAST_CHANGE:
 				return isSetLastChange();
 			case ReqIF10Package.IDENTIFIABLE__LONG_NAME:
 				return isSetLongName();
 			case ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID:
 				return isSetAlternativeID();
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
 		result.append(" (desc: ");
 		if (descESet) result.append(desc); else result.append("<unset>");
 		result.append(", identifier: ");
 		if (identifierESet) result.append(identifier); else result.append("<unset>");
 		result.append(", lastChange: ");
 		if (lastChangeESet) result.append(lastChange); else result.append("<unset>");
 		result.append(", longName: ");
 		if (longNameESet) result.append(longName); else result.append("<unset>");
 		result.append(')');
 		return result.toString();
 	}
 
 } //IdentifiableImpl
