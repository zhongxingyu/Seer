 package org.eclipse.stem.populationmodels.standard.impl;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.graph.IntegrationLabelValue;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.definitions.labels.AreaLabel;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Population Model Label Value</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelLabelValueImpl#getCount <em>Count</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelLabelValueImpl#getIncidence <em>Incidence</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelLabelValueImpl#getBirths <em>Births</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelLabelValueImpl#getDeaths <em>Deaths</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelLabelValueImpl#getDensity <em>Density</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class StandardPopulationModelLabelValueImpl extends PopulationModelLabelValueImpl implements StandardPopulationModelLabelValue {
 	/**
 	 * The default value of the '{@link #getCount() <em>Count</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCount()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double COUNT_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getCount() <em>Count</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCount()
 	 * @generated
 	 * @ordered
 	 */
 	protected double count = COUNT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getIncidence() <em>Incidence</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIncidence()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double INCIDENCE_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getIncidence() <em>Incidence</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIncidence()
 	 * @generated
 	 * @ordered
 	 */
 	protected double incidence = INCIDENCE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getBirths() <em>Births</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBirths()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double BIRTHS_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getBirths() <em>Births</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBirths()
 	 * @generated
 	 * @ordered
 	 */
 	protected double births = BIRTHS_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getDeaths() <em>Deaths</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDeaths()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double DEATHS_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getDeaths() <em>Deaths</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDeaths()
 	 * @generated
 	 * @ordered
 	 */
 	protected double deaths = DEATHS_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDensity() <em>Density</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDensity()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double DENSITY_EDEFAULT = 0.0;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected StandardPopulationModelLabelValueImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.STANDARD_POPULATION_MODEL_LABEL_VALUE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getCount() {
 		return count;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCount(double newCount) {
 		double oldCount = count;
 		count = newCount;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__COUNT, oldCount, count));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getIncidence() {
 		return incidence;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIncidence(double newIncidence) {
 		double oldIncidence = incidence;
 		incidence = newIncidence;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__INCIDENCE, oldIncidence, incidence));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getBirths() {
 		return births;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBirths(double newBirths) {
 		double oldBirths = births;
 		births = newBirths;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__BIRTHS, oldBirths, births));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getDeaths() {
 		return deaths;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDeaths(double newDeaths) {
 		double oldDeaths = deaths;
 		deaths = newDeaths;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DEATHS, oldDeaths, deaths));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public double getDensity() {
 		double population = this.count;
 		StandardPopulationModelLabel label = (StandardPopulationModelLabel)this.eContainer();
 		Node n= label.getNode();
 		double area = 0.0;
 		for(NodeLabel nodeLab : n.getLabels()) {
 			if(nodeLab instanceof AreaLabel) {
 				area = ((AreaLabel)nodeLab).getCurrentAreaValue().getArea();
 				break;
 			}
 		}
 		return (area > 0.0)? (population/area) : 0.0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isSetDensity() {
 		return true; // Density always set
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean adjustDelta(IntegrationLabelValue value) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
 		return false;
 	}
 
 	private Map<Node, Double>arrivals;
 	private Map<Node, Double>departures;
 	
 	/**
 	 * Get arrivals
 	 * @generated NOT
 	 */
 	public Map<Node, Double> getArrivals() {
 		return arrivals;
 	}
 
 	/**
 	 * Get departures
 	 * @generated NOT
 	 */
 	public Map<Node, Double> getDepartures() {
 		return departures;
 	}
 
 	public void setArrivals(Map<Node, Double> m) {
 		this.arrivals = m;
 	}
 	public void setDepartures(Map<Node, Double> m) {
 		this.departures = m;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__COUNT:
 				return getCount();
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__INCIDENCE:
 				return getIncidence();
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__BIRTHS:
 				return getBirths();
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DEATHS:
 				return getDeaths();
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DENSITY:
 				return getDensity();
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
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__COUNT:
 				setCount((Double)newValue);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__INCIDENCE:
 				setIncidence((Double)newValue);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__BIRTHS:
 				setBirths((Double)newValue);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DEATHS:
 				setDeaths((Double)newValue);
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
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__COUNT:
 				setCount(COUNT_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__INCIDENCE:
 				setIncidence(INCIDENCE_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__BIRTHS:
 				setBirths(BIRTHS_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DEATHS:
 				setDeaths(DEATHS_EDEFAULT);
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
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__COUNT:
 				return count != COUNT_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__INCIDENCE:
 				return incidence != INCIDENCE_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__BIRTHS:
 				return births != BIRTHS_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DEATHS:
 				return deaths != DEATHS_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DENSITY:
 				return isSetDensity();
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
 		result.append(" (count: ");
 		result.append(count);
 		result.append(", incidence: ");
 		result.append(incidence);
 		result.append(", births: ");
 		result.append(births);
 		result.append(", deaths: ");
 		result.append(deaths);
 		result.append(')');
 		return result.toString();
 	}
 
 	public IntegrationLabelValue abs() {
 		this.count = Math.abs(this.count);
 		this.births = Math.abs(this.births);
 		this.deaths = Math.abs(this.deaths);
 		return this;
 	}
 
 	public IntegrationLabelValue add(IntegrationLabelValue value) {
 		StandardPopulationModelLabelValue v = (StandardPopulationModelLabelValue)value;
 		this.count += v.getCount();
 		return this;
 	}
 
 	public IntegrationLabelValue add(double addition) {
 		this.count += addition;
 		return this;
 	}
 
 	public IntegrationLabelValue scale(double scaleFactor) {
 		this.count *= scaleFactor;
 		return this;
 	}
 
 	public IntegrationLabelValue set(IntegrationLabelValue value) {
 		StandardPopulationModelLabelValue v = (StandardPopulationModelLabelValue)value;
 		this.count = v.getCount();
 		this.births = v.getBirths();
 		this.deaths = v.getDeaths();
 		this.incidence = v.getIncidence();
 		return this;
 	}
 
 	public IntegrationLabelValue sub(IntegrationLabelValue value) {
 		StandardPopulationModelLabelValue v = (StandardPopulationModelLabelValue)value;
 		this.count -= v.getCount();
 		return this;
 	}
 
 	public IntegrationLabelValue divide(IntegrationLabelValue d) {
 		StandardPopulationModelLabelValue v = (StandardPopulationModelLabelValue)d;
 		this.count /= v.getCount();
 		return this;
 	}
 	
 	public double max() {
 		return count;
 	}
 	
 	@Override
 	public void reset() {
 		super.reset();
 		this.setCount(COUNT_EDEFAULT);
 		this.setBirths(BIRTHS_EDEFAULT);
 		this.setDeaths(DEATHS_EDEFAULT);
 		arrivals = null;
 		departures = null;
 	}
 } //StandardPopulationModelLabelValueImpl
