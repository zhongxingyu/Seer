 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.gmf.codegen.gmfgen.EntryBase;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Entry Base</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.EntryBaseImpl#getTitle <em>Title</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.EntryBaseImpl#getDescription <em>Description</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.EntryBaseImpl#getLargeIconPath <em>Large Icon Path</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.EntryBaseImpl#getSmallIconPath <em>Small Icon Path</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.EntryBaseImpl#getCreateMethodName <em>Create Method Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.EntryBaseImpl#getId <em>Id</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class EntryBaseImpl extends EObjectImpl implements EntryBase {
 	/**
 	 * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTitle()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TITLE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTitle()
 	 * @generated
 	 * @ordered
 	 */
 	protected String title = TITLE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getLargeIconPath() <em>Large Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLargeIconPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LARGE_ICON_PATH_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLargeIconPath() <em>Large Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLargeIconPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected String largeIconPath = LARGE_ICON_PATH_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getSmallIconPath() <em>Small Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSmallIconPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String SMALL_ICON_PATH_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getSmallIconPath() <em>Small Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSmallIconPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected String smallIconPath = SMALL_ICON_PATH_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreateMethodName() <em>Create Method Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreateMethodName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATE_METHOD_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCreateMethodName() <em>Create Method Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreateMethodName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String createMethodName = CREATE_METHOD_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getId()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getId()
 	 * @generated
 	 * @ordered
 	 */
 	protected String id = ID_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EntryBaseImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GMFGenPackage.eINSTANCE.getEntryBase();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTitle(String newTitle) {
 		String oldTitle = title;
 		title = newTitle;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.ENTRY_BASE__TITLE, oldTitle, title));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.ENTRY_BASE__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLargeIconPath() {
 		return largeIconPath;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLargeIconPath(String newLargeIconPath) {
 		String oldLargeIconPath = largeIconPath;
 		largeIconPath = newLargeIconPath;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.ENTRY_BASE__LARGE_ICON_PATH, oldLargeIconPath, largeIconPath));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getSmallIconPath() {
 		return smallIconPath;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSmallIconPath(String newSmallIconPath) {
 		String oldSmallIconPath = smallIconPath;
 		smallIconPath = newSmallIconPath;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.ENTRY_BASE__SMALL_ICON_PATH, oldSmallIconPath, smallIconPath));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreateMethodNameGen() {
 		return createMethodName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreateMethodName(String newCreateMethodName) {
 		String oldCreateMethodName = createMethodName;
 		createMethodName = newCreateMethodName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.ENTRY_BASE__CREATE_METHOD_NAME, oldCreateMethodName, createMethodName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getIdGen() {
 		return id;
 	}
 
 	public String getId() {
 		if (getIdGen() != null) {
 			return getIdGen();
 		}
		return getCreateMethodName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setId(String newId) {
 		String oldId = id;
 		id = newId;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.ENTRY_BASE__ID, oldId, id));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GMFGenPackage.ENTRY_BASE__TITLE:
 				return getTitle();
 			case GMFGenPackage.ENTRY_BASE__DESCRIPTION:
 				return getDescription();
 			case GMFGenPackage.ENTRY_BASE__LARGE_ICON_PATH:
 				return getLargeIconPath();
 			case GMFGenPackage.ENTRY_BASE__SMALL_ICON_PATH:
 				return getSmallIconPath();
 			case GMFGenPackage.ENTRY_BASE__CREATE_METHOD_NAME:
 				return getCreateMethodName();
 			case GMFGenPackage.ENTRY_BASE__ID:
 				return getId();
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
 			case GMFGenPackage.ENTRY_BASE__TITLE:
 				setTitle((String)newValue);
 				return;
 			case GMFGenPackage.ENTRY_BASE__DESCRIPTION:
 				setDescription((String)newValue);
 				return;
 			case GMFGenPackage.ENTRY_BASE__LARGE_ICON_PATH:
 				setLargeIconPath((String)newValue);
 				return;
 			case GMFGenPackage.ENTRY_BASE__SMALL_ICON_PATH:
 				setSmallIconPath((String)newValue);
 				return;
 			case GMFGenPackage.ENTRY_BASE__CREATE_METHOD_NAME:
 				setCreateMethodName((String)newValue);
 				return;
 			case GMFGenPackage.ENTRY_BASE__ID:
 				setId((String)newValue);
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
 			case GMFGenPackage.ENTRY_BASE__TITLE:
 				setTitle(TITLE_EDEFAULT);
 				return;
 			case GMFGenPackage.ENTRY_BASE__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 			case GMFGenPackage.ENTRY_BASE__LARGE_ICON_PATH:
 				setLargeIconPath(LARGE_ICON_PATH_EDEFAULT);
 				return;
 			case GMFGenPackage.ENTRY_BASE__SMALL_ICON_PATH:
 				setSmallIconPath(SMALL_ICON_PATH_EDEFAULT);
 				return;
 			case GMFGenPackage.ENTRY_BASE__CREATE_METHOD_NAME:
 				setCreateMethodName(CREATE_METHOD_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.ENTRY_BASE__ID:
 				setId(ID_EDEFAULT);
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
 			case GMFGenPackage.ENTRY_BASE__TITLE:
 				return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT.equals(title);
 			case GMFGenPackage.ENTRY_BASE__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
 			case GMFGenPackage.ENTRY_BASE__LARGE_ICON_PATH:
 				return LARGE_ICON_PATH_EDEFAULT == null ? largeIconPath != null : !LARGE_ICON_PATH_EDEFAULT.equals(largeIconPath);
 			case GMFGenPackage.ENTRY_BASE__SMALL_ICON_PATH:
 				return SMALL_ICON_PATH_EDEFAULT == null ? smallIconPath != null : !SMALL_ICON_PATH_EDEFAULT.equals(smallIconPath);
 			case GMFGenPackage.ENTRY_BASE__CREATE_METHOD_NAME:
 				return CREATE_METHOD_NAME_EDEFAULT == null ? createMethodName != null : !CREATE_METHOD_NAME_EDEFAULT.equals(createMethodName);
 			case GMFGenPackage.ENTRY_BASE__ID:
 				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
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
 		result.append(" (title: ");
 		result.append(title);
 		result.append(", description: ");
 		result.append(description);
 		result.append(", largeIconPath: ");
 		result.append(largeIconPath);
 		result.append(", smallIconPath: ");
 		result.append(smallIconPath);
 		result.append(", createMethodName: ");
 		result.append(createMethodName);
 		result.append(", id: ");
 		result.append(id);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * @return index of this entry in the parent container
 	 */
 	protected final int getEntryID() {
 		EReference r = eContainmentFeature();
 		if (r != null) {
 			if (eContainer().eGet(r) instanceof EList<?>) {
 				return 1 + ((EList<?>) eContainer().eGet(r)).indexOf(this);
 			}
 			return 1;
 		}
 		return hashCode();
 	}
 } //EntryBaseImpl
