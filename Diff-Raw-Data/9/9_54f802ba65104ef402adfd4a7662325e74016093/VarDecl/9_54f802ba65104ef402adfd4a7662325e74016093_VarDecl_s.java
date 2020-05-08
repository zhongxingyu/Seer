 /**
  */
 package kompren;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Var Decl</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link kompren.VarDecl#getVarName <em>Var Name</em>}</li>
  *   <li>{@link kompren.VarDecl#getType <em>Type</em>}</li>
  * </ul>
  * </p>
  *
  * @see kompren.KomprenPackage#getVarDecl()
  * @model
  * @generated
  */
 public interface VarDecl extends EObject {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String copyright = "Inria/IRISA Diverse Team";
 
 	/**
 	 * Returns the value of the '<em><b>Var Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Var Name</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Var Name</em>' attribute.
 	 * @see #setVarName(String)
 	 * @see kompren.KomprenPackage#getVarDecl_VarName()
 	 * @model required="true"
 	 * @generated
 	 */
 	String getVarName();
 
 	/**
 	 * Sets the value of the '{@link kompren.VarDecl#getVarName <em>Var Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Var Name</em>' attribute.
 	 * @see #getVarName()
 	 * @generated
 	 */
 	void setVarName(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Type</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Type</em>' reference isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Type</em>' reference.
 	 * @see #isSetType()
 	 * @see kompren.KomprenPackage#getVarDecl_Type()
	 * @model unsettable="true" required="true" changeable="false" volatile="true" derived="true"
 	 * @generated
 	 */
 	EClass getType();
 
 	/**
 	 * Returns whether the value of the '{@link kompren.VarDecl#getType <em>Type</em>}' reference is set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return whether the value of the '<em>Type</em>' reference is set.
 	 * @see #getType()
 	 * @generated
 	 */
 	boolean isSetType();
 
 } // VarDecl
