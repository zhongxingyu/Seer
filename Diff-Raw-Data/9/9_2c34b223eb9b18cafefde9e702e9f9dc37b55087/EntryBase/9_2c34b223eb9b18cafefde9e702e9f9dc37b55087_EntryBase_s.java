 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen;
 
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Entry Base</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getTitle <em>Title</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getDescription <em>Description</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getLargeIconPath <em>Large Icon Path</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getSmallIconPath <em>Small Icon Path</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getCreateMethodName <em>Create Method Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getId <em>Id</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getEntryBase()
  * @model abstract="true"
  * @generated
  */
 public interface EntryBase extends EObject {
 	/**
 	 * Returns the value of the '<em><b>Title</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Title</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Title</em>' attribute.
 	 * @see #setTitle(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getEntryBase_Title()
 	 * @model
 	 * @generated
 	 */
 	String getTitle();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getTitle <em>Title</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Title</em>' attribute.
 	 * @see #getTitle()
 	 * @generated
 	 */
 	void setTitle(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Description</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Description</em>' attribute.
 	 * @see #setDescription(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getEntryBase_Description()
 	 * @model
 	 * @generated
 	 */
 	String getDescription();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getDescription <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Description</em>' attribute.
 	 * @see #getDescription()
 	 * @generated
 	 */
 	void setDescription(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Large Icon Path</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Large Icon Path</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Large Icon Path</em>' attribute.
 	 * @see #setLargeIconPath(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getEntryBase_LargeIconPath()
 	 * @model
 	 * @generated
 	 */
 	String getLargeIconPath();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getLargeIconPath <em>Large Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Large Icon Path</em>' attribute.
 	 * @see #getLargeIconPath()
 	 * @generated
 	 */
 	void setLargeIconPath(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Small Icon Path</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Small Icon Path</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Small Icon Path</em>' attribute.
 	 * @see #setSmallIconPath(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getEntryBase_SmallIconPath()
 	 * @model
 	 * @generated
 	 */
 	String getSmallIconPath();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getSmallIconPath <em>Small Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Small Icon Path</em>' attribute.
 	 * @see #getSmallIconPath()
 	 * @generated
 	 */
 	void setSmallIconPath(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Create Method Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Create Method Name</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Create Method Name</em>' attribute.
 	 * @see #setCreateMethodName(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getEntryBase_CreateMethodName()
 	 * @model
 	 * @generated
 	 */
 	String getCreateMethodName();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getCreateMethodName <em>Create Method Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Create Method Name</em>' attribute.
 	 * @see #getCreateMethodName()
 	 * @generated
 	 */
 	void setCreateMethodName(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * <!-- begin-model-doc -->
	 * Non-mandatory identification of the entry. Defaults to the same value as createMethodName, clients may override. Set to blank string if don't need the identity
 	 * <!-- end-model-doc -->
 	 * @return the value of the '<em>Id</em>' attribute.
 	 * @see #setId(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getEntryBase_Id()
 	 * @model
 	 * @generated
 	 */
 	String getId();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.EntryBase#getId <em>Id</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Id</em>' attribute.
 	 * @see #getId()
 	 * @generated
 	 */
 	void setId(String value);
 
 } // EntryBase
