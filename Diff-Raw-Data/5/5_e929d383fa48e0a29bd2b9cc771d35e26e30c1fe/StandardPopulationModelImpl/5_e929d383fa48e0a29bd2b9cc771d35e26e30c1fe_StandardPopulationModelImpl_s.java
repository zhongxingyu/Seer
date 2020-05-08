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
 
 
 import java.util.Iterator;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.IntegrationLabel;
 import org.eclipse.stem.core.graph.LabelValue;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.graph.SimpleDataExchangeLabelValue;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabelValue;
 import org.eclipse.stem.populationmodels.standard.StandardFactory;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModel;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Population Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelImpl#getBirthRate <em>Birth Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelImpl#getDeathRate <em>Death Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelImpl#getTimePeriod <em>Time Period</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class StandardPopulationModelImpl extends PopulationModelImpl implements StandardPopulationModel {
 	/**
 	 * The default value of the '{@link #getBirthRate() <em>Birth Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBirthRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double BIRTH_RATE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getBirthRate() <em>Birth Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBirthRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected double birthRate = BIRTH_RATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDeathRate() <em>Death Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDeathRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double DEATH_RATE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getDeathRate() <em>Death Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDeathRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected double deathRate = DEATH_RATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTimePeriod() <em>Time Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTimePeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final long TIME_PERIOD_EDEFAULT = 86400000L;
 
 	/**
 	 * The cached value of the '{@link #getTimePeriod() <em>Time Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTimePeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected long timePeriod = TIME_PERIOD_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public StandardPopulationModelImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.STANDARD_POPULATION_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getBirthRate() {
 		return birthRate;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBirthRate(double newBirthRate) {
 		double oldBirthRate = birthRate;
 		birthRate = newBirthRate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE, oldBirthRate, birthRate));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getDeathRate() {
 		return deathRate;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDeathRate(double newDeathRate) {
 		double oldDeathRate = deathRate;
 		deathRate = newDeathRate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE, oldDeathRate, deathRate));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public long getTimePeriod() {
 		return timePeriod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTimePeriod(long newTimePeriod) {
 		long oldTimePeriod = timePeriod;
 		timePeriod = newTimePeriod;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD, oldTimePeriod, timePeriod));
 	}
 
 	/**
 	 * Decorate the graph for a standard population model
 	 * 
 	 */
 	@Override
 	public void decorateGraph() {
 		super.decorateGraph();
 	} // decorateGraph
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				return new Double(getBirthRate());
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				return new Double(getDeathRate());
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				return new Long(getTimePeriod());
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
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				setBirthRate(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				setDeathRate(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				setTimePeriod(((Long)newValue).longValue());
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
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				setBirthRate(BIRTH_RATE_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				setDeathRate(DEATH_RATE_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				setTimePeriod(TIME_PERIOD_EDEFAULT);
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
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				return birthRate != BIRTH_RATE_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				return deathRate != DEATH_RATE_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				return timePeriod != TIME_PERIOD_EDEFAULT;
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
 		result.append(" (birthRate: ");
 		result.append(birthRate);
 		result.append(", deathRate: ");
 		result.append(deathRate);
 		result.append(", timePeriod: ");
 		result.append(timePeriod);
 		result.append(')');
 		return result.toString();
 	}
 
 	@Override
 	public PopulationModelLabel createPopulationLabel() {
 		return StandardFactory.eINSTANCE.createStandardPopulationModelLabel();
 	}
 
 	@Override
 	public PopulationModelLabelValue createPopulationLabelValue() {
 		return StandardFactory.eINSTANCE.createStandardPopulationModelLabelValue();
 	}
 
 	public void calculateDelta(STEMTime time, long timeDelta,
 			EList<DynamicLabel> labels) {
 		// We simply calculate the change from the birth/death rate
 		// adjusted for the time period used in the simulation
 		
 		double adjustedBirthRate = adjustRate(this.getBirthRate(), this.getTimePeriod(), timeDelta);
 		double adjustedDeathRate = adjustRate(this.getDeathRate(), this.getTimePeriod(), timeDelta);
 		
 		for(DynamicLabel label:labels) {
 			StandardPopulationModelLabelImpl slabel = (StandardPopulationModelLabelImpl)label;
 			StandardPopulationModelLabelValue delta = slabel.getDeltaValue();
 			StandardPopulationModelLabelValue current = slabel.getProbeValue();
 			
 			double currentPopulation = current.getCount();
 			double births = currentPopulation * adjustedBirthRate;
 			double deaths = currentPopulation * adjustedDeathRate;
 			delta.setIncidence(births-deaths);
 			delta.setCount(births-deaths);
 			delta.setBirths(births);
 			delta.setDeaths(deaths);
 		}
 	}
 
 	public void applyExternalDeltas(STEMTime time, long timeDelta,
 			EList<DynamicLabel> labels) {
 		for (final Iterator<DynamicLabel> currentStateLabelIter = labels
 				.iterator(); currentStateLabelIter.hasNext();) {
 			final StandardPopulationModelLabel plabel = (StandardPopulationModelLabel) currentStateLabelIter
 					.next();
 			
 			StandardPopulationModelLabelValue myDelta = plabel.getDeltaValue();
 			Node n = plabel.getNode();
 			
 			// Find other labels on the node that wants to exchange data
 			
 			EList<NodeLabel> labs = n.getLabels();
 			for(NodeLabel l:labs) {
 				if(l instanceof IntegrationLabel && !l.equals(plabel)) {
 					SimpleDataExchangeLabelValue sdeLabelValue = (SimpleDataExchangeLabelValue)((IntegrationLabel)l).getDeltaValue();
 					double additions = sdeLabelValue.getAdditions();
 					double substractions = sdeLabelValue.getSubstractions();
 					
 					// Additions are births. Observe that additions should be 0 since 
 					// other decorators are disease models that don't cause an "increase"
 					// in births.
 					myDelta.setCount(myDelta.getCount()+additions);
 					
 					// Substractions are deaths 
 					myDelta.setCount(myDelta.getCount() - substractions);
 				}
 			}
 
 		}
 	}
 	
 	private double adjustRate(double rate, long ratePeriod, long actualPeriod) {
 		return rate * ((double)actualPeriod/(double)ratePeriod);
 	}
 	
 	public void doModelSpecificAdjustments(LabelValue label) {
 		// TODO Auto-generated method stub
 		
 	}
 
 } //StandardPopulationModelImpl
