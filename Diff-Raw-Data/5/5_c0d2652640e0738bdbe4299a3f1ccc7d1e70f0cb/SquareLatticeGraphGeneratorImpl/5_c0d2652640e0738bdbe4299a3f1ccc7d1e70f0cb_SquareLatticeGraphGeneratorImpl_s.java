 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.stem.graphgenerators.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.Utility;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.definitions.lattice.impl.SqrLatticeGeneratorImpl;
 import org.eclipse.stem.graphgenerators.GraphgeneratorsPackage;
 import org.eclipse.stem.graphgenerators.SquareLatticeGraphGenerator;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Square Lattice Graph Generator</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.graphgenerators.impl.SquareLatticeGraphGeneratorImpl#getArea <em>Area</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class SquareLatticeGraphGeneratorImpl extends LatticeGraphGeneratorImpl implements SquareLatticeGraphGenerator {
 	/**
 	 * The default value of the '{@link #getArea() <em>Area</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getArea()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double AREA_EDEFAULT = 2025.0;
 	/**
 	 * The cached value of the '{@link #getArea() <em>Area</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getArea()
 	 * @generated
 	 * @ordered
 	 */
 	protected double area = AREA_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public SquareLatticeGraphGeneratorImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GraphgeneratorsPackage.Literals.SQUARE_LATTICE_GRAPH_GENERATOR;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getArea() {
 		return area;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setArea(double newArea) {
 		double oldArea = area;
 		area = newArea;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphgeneratorsPackage.SQUARE_LATTICE_GRAPH_GENERATOR__AREA, oldArea, area));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GraphgeneratorsPackage.SQUARE_LATTICE_GRAPH_GENERATOR__AREA:
 				return getArea();
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
 			case GraphgeneratorsPackage.SQUARE_LATTICE_GRAPH_GENERATOR__AREA:
 				setArea((Double)newValue);
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
 			case GraphgeneratorsPackage.SQUARE_LATTICE_GRAPH_GENERATOR__AREA:
 				setArea(AREA_EDEFAULT);
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
 			case GraphgeneratorsPackage.SQUARE_LATTICE_GRAPH_GENERATOR__AREA:
 				return area != AREA_EDEFAULT;
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
 		result.append(" (area: ");
 		result.append(area);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
	 * 
 	 */
 	@Override
 	public Graph getGraph() {
 		SqrLatticeGeneratorImpl slgi = new SqrLatticeGeneratorImpl();
 		// When Jamie's ready Graph g = slgi.getGraph(this.getXSize(), this.getYSize(), this.isUseNearestNeighbors(), this.isUseNextNearestNeighbors(), this.isPeriodicBoundaries(), this.getArea());
		Graph g = slgi.getGraph(this.getXSize(), this.getYSize(), this.isUseNearestNeighbors(), this.isUseNextNearestNeighbors(), this.isPeriodicBoundaries());
 		
 //		System.out.println("graph built ..now save it");
 		
 //		String graphUriString = "platform:/resource/play/graphs/sqrLatticeGraph.graph";
 //		g.setURI(URI.createURI(graphUriString));
 //		URI outputURI = URI.createFileURI("/Users/jhkauf/Documents/runtime-stemMacOS.product/play/graphs/sqrLatticeGraph.graph");
 		
 //		try {
 //			Utility.serializeIdentifiable(g, outputURI);
 //		} catch(Exception e) {
 //			e.printStackTrace();
 //		}
 	
 		return g;
 		
 
 	}
 } //SquareLatticeGraphGeneratorImpl
