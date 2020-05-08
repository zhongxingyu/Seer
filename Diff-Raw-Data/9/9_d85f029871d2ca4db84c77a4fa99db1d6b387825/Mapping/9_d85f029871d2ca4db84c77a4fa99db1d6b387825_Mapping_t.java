 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.mappings;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.tooldef.StyleSelector;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Mapping</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.gmf.mappings.Mapping#getNodes <em>Nodes</em>}</li>
  *   <li>{@link org.eclipse.gmf.mappings.Mapping#getLinks <em>Links</em>}</li>
  *   <li>{@link org.eclipse.gmf.mappings.Mapping#getDiagram <em>Diagram</em>}</li>
  *   <li>{@link org.eclipse.gmf.mappings.Mapping#getAppearanceStyles <em>Appearance Styles</em>}</li>
  *   <li>{@link org.eclipse.gmf.mappings.Mapping#getAudits <em>Audits</em>}</li>
  *   <li>{@link org.eclipse.gmf.mappings.Mapping#getMetrics <em>Metrics</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.gmf.mappings.GMFMapPackage#getMapping()
 * @model annotation="http://www.eclipse.org/gmf/2005/constraints ocl='nodes->forAll(n|n.containmentFeature.oclIsUndefined() and not n.child.domainMetaElement.oclIsUndefined() implies links->exists(let r:ecore::EReference= linkMetaFeature.oclAsType(ecore::EReference) in r.containment and r.eReferenceType.isSuperTypeOf(n.child.domainMetaElement)))' description='Phantom nodes that are not targeted by a link mapping representing containment reference present in model'"
  * @generated
  */
 public interface Mapping extends EObject {
 	/**
 	 * Returns the value of the '<em><b>Nodes</b></em>' containment reference list.
 	 * The list contents are of type {@link org.eclipse.gmf.mappings.TopNodeReference}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Nodes</em>' containment reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Nodes</em>' containment reference list.
 	 * @see org.eclipse.gmf.mappings.GMFMapPackage#getMapping_Nodes()
 	 * @model containment="true"
 	 * @generated
 	 */
 	EList<TopNodeReference> getNodes();
 
 	/**
 	 * Returns the value of the '<em><b>Links</b></em>' containment reference list.
 	 * The list contents are of type {@link org.eclipse.gmf.mappings.LinkMapping}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Links</em>' containment reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Links</em>' containment reference list.
 	 * @see org.eclipse.gmf.mappings.GMFMapPackage#getMapping_Links()
 	 * @model containment="true"
 	 * @generated
 	 */
 	EList<LinkMapping> getLinks();
 
 	/**
 	 * Returns the value of the '<em><b>Diagram</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Diagram</em>' containment reference isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Diagram</em>' containment reference.
 	 * @see #setDiagram(CanvasMapping)
 	 * @see org.eclipse.gmf.mappings.GMFMapPackage#getMapping_Diagram()
 	 * @model containment="true" required="true"
 	 * @generated
 	 */
 	CanvasMapping getDiagram();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.mappings.Mapping#getDiagram <em>Diagram</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Diagram</em>' containment reference.
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	void setDiagram(CanvasMapping value);
 
 	/**
 	 * Returns the value of the '<em><b>Appearance Styles</b></em>' containment reference list.
 	 * The list contents are of type {@link org.eclipse.gmf.tooldef.StyleSelector}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Appearance Styles</em>' containment reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Appearance Styles</em>' containment reference list.
 	 * @see org.eclipse.gmf.mappings.GMFMapPackage#getMapping_AppearanceStyles()
 	 * @model containment="true"
 	 * @generated
 	 */
 	EList<StyleSelector> getAppearanceStyles();
 
 	/**
 	 * Returns the value of the '<em><b>Audits</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Audits</em>' containment reference.
 	 * @see #setAudits(AuditContainer)
 	 * @see org.eclipse.gmf.mappings.GMFMapPackage#getMapping_Audits()
 	 * @model containment="true"
 	 * @generated
 	 */
 	AuditContainer getAudits();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.mappings.Mapping#getAudits <em>Audits</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Audits</em>' containment reference.
 	 * @see #getAudits()
 	 * @generated
 	 */
 	void setAudits(AuditContainer value);
 
 	/**
 	 * Returns the value of the '<em><b>Metrics</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Metrics</em>' containment reference isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Metrics</em>' containment reference.
 	 * @see #setMetrics(MetricContainer)
 	 * @see org.eclipse.gmf.mappings.GMFMapPackage#getMapping_Metrics()
 	 * @model containment="true"
 	 * @generated
 	 */
 	MetricContainer getMetrics();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.mappings.Mapping#getMetrics <em>Metrics</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Metrics</em>' containment reference.
 	 * @see #getMetrics()
 	 * @generated
 	 */
 	void setMetrics(MetricContainer value);
 
 } // Mapping
