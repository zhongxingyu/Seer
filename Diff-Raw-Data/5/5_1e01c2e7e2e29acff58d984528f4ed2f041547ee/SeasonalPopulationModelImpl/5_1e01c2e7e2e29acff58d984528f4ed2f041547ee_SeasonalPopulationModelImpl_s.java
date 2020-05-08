 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.stem.populationmodels.standard.impl;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.populationmodels.standard.SeasonalPopulationModel;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Seasonal Population Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.SeasonalPopulationModelImpl#getPhase <em>Phase</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.SeasonalPopulationModelImpl#getModulationAmplitude <em>Modulation Amplitude</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.SeasonalPopulationModelImpl#getFrequency <em>Frequency</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.SeasonalPopulationModelImpl#isUseLatitude <em>Use Latitude</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class SeasonalPopulationModelImpl extends StandardPopulationModelImpl implements SeasonalPopulationModel {
 	/**
 	 * constant
 	 */
 	private static final double MILLIS_PER_DAY = 1000.0*60.0*60.0*24.0;
 	/**
 	 * The default value of the '{@link #getPhase() <em>Phase</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPhase()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double PHASE_EDEFAULT = 0.0; // TODO The default value literal "" is not valid.
 
 	/**
 	 * The cached value of the '{@link #getPhase() <em>Phase</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPhase()
 	 * @generated
 	 * @ordered
 	 */
 	protected double phase = PHASE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModulationAmplitude() <em>Modulation Amplitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationAmplitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_AMPLITUDE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getModulationAmplitude() <em>Modulation Amplitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationAmplitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationAmplitude = MODULATION_AMPLITUDE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getFrequency() <em>Frequency</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFrequency()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double FREQUENCY_EDEFAULT = 365.25;
 
 	/**
 	 * The cached value of the '{@link #getFrequency() <em>Frequency</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFrequency()
 	 * @generated
 	 * @ordered
 	 */
 	protected double frequency = FREQUENCY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isUseLatitude() <em>Use Latitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isUseLatitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean USE_LATITUDE_EDEFAULT = true;
 
 	/**
 	 * The cached value of the '{@link #isUseLatitude() <em>Use Latitude</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isUseLatitude()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean useLatitude = USE_LATITUDE_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated
 	 */
	protected SeasonalPopulationModelImpl() {
 		super();
 	}
 	
 	
 	/**
 	 * Compute the changes in the population 
 	 * The birth rate and death rates are modified in each time step by a seasonal modulation function
 	 * The birth/death rates are NOT constant
 	 * @param time
 	 * @param timeDelta
 	 * @param labels
 	 * @generated NOT
 	 */
 	@Override
 	public void calculateDelta(STEMTime time, long timeDelta,
 			EList<DynamicLabel> labels) {
 		// We simply calculate the change from the birth/death rate
 		// adjusted for the time period used in the simulation
 		
 		double adjustedBirthRate = adjustRate(this.getBirthRate(), this.getTimePeriod(), timeDelta);
 		double adjustedDeathRate = adjustRate(this.getDeathRate(), this.getTimePeriod(), timeDelta);
 		
 		for(DynamicLabel label:labels) {
 			StandardPopulationModelLabelImpl slabel = (StandardPopulationModelLabelImpl)label;
 			StandardPopulationModelLabelValueImpl delta = (StandardPopulationModelLabelValueImpl)slabel.getDeltaValue();
 			StandardPopulationModelLabelValue current = slabel.getProbeValue();
 			
 			double currentPopulation = current.getCount();
 			if(useLatitude) {
 				// TODO not yet implemented
 			} else {
 				
 				double currentMillis = time.getTime().getTime();
 				double modulationPeriod = frequency; // TODO this is names wrong. Change from frequency to period!!
 				
 				double seasonality = (-1.0 * modulationAmplitude) * Math.cos(phase + 2.0*Math.PI*currentMillis/(modulationPeriod*MILLIS_PER_DAY) );
 				
 				System.out.println(""+time.getTime().getMonth()+"/"+time.getTime().getDate()+"  "+seasonality);
 				
 				if(seasonality >=0.0) {
 					adjustedBirthRate+= seasonality;
 				} else {
 					adjustedDeathRate-= seasonality;
 				}
 				
 				
 			}
 			
 			double births = currentPopulation * adjustedBirthRate;
 			double deaths = currentPopulation * adjustedDeathRate;
 			
 			delta.setIncidence(births-deaths);
 			delta.setCount(births-deaths);
 			delta.setBirths(births);
 			delta.setDeaths(deaths);
 			
 			if(delta.getArrivals() == null) delta.setArrivals(new HashMap<Node,Double>());
 			if(delta.getDepartures() == null) delta.setDepartures(new HashMap<Node,Double>());
 			
 			delta.getArrivals().put((Node)label.getIdentifiable(), births);
 			delta.getDepartures().put((Node)label.getIdentifiable(), deaths);
 			
 			handleMigration(slabel, delta.getArrivals(),delta.getDepartures(), this.getTimePeriod(), timeDelta, delta);
 			handlePipeTransport(slabel, delta.getArrivals(),delta.getDepartures(), timeDelta, delta);
 		}
 	}// calculateDelta
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.SEASONAL_POPULATION_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getPhase() {
 		return phase;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPhase(double newPhase) {
 		double oldPhase = phase;
 		phase = newPhase;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SEASONAL_POPULATION_MODEL__PHASE, oldPhase, phase));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationAmplitude() {
 		return modulationAmplitude;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationAmplitude(double newModulationAmplitude) {
 		double oldModulationAmplitude = modulationAmplitude;
 		modulationAmplitude = newModulationAmplitude;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SEASONAL_POPULATION_MODEL__MODULATION_AMPLITUDE, oldModulationAmplitude, modulationAmplitude));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getFrequency() {
 		return frequency;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isUseLatitude() {
 		return useLatitude;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUseLatitude(boolean newUseLatitude) {
 		boolean oldUseLatitude = useLatitude;
 		useLatitude = newUseLatitude;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SEASONAL_POPULATION_MODEL__USE_LATITUDE, oldUseLatitude, useLatitude));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.SEASONAL_POPULATION_MODEL__PHASE:
 				return getPhase();
 			case StandardPackage.SEASONAL_POPULATION_MODEL__MODULATION_AMPLITUDE:
 				return getModulationAmplitude();
 			case StandardPackage.SEASONAL_POPULATION_MODEL__FREQUENCY:
 				return getFrequency();
 			case StandardPackage.SEASONAL_POPULATION_MODEL__USE_LATITUDE:
 				return isUseLatitude();
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
 			case StandardPackage.SEASONAL_POPULATION_MODEL__PHASE:
 				setPhase((Double)newValue);
 				return;
 			case StandardPackage.SEASONAL_POPULATION_MODEL__MODULATION_AMPLITUDE:
 				setModulationAmplitude((Double)newValue);
 				return;
 			case StandardPackage.SEASONAL_POPULATION_MODEL__USE_LATITUDE:
 				setUseLatitude((Boolean)newValue);
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
 			case StandardPackage.SEASONAL_POPULATION_MODEL__PHASE:
 				setPhase(PHASE_EDEFAULT);
 				return;
 			case StandardPackage.SEASONAL_POPULATION_MODEL__MODULATION_AMPLITUDE:
 				setModulationAmplitude(MODULATION_AMPLITUDE_EDEFAULT);
 				return;
 			case StandardPackage.SEASONAL_POPULATION_MODEL__USE_LATITUDE:
 				setUseLatitude(USE_LATITUDE_EDEFAULT);
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
 			case StandardPackage.SEASONAL_POPULATION_MODEL__PHASE:
 				return phase != PHASE_EDEFAULT;
 			case StandardPackage.SEASONAL_POPULATION_MODEL__MODULATION_AMPLITUDE:
 				return modulationAmplitude != MODULATION_AMPLITUDE_EDEFAULT;
 			case StandardPackage.SEASONAL_POPULATION_MODEL__FREQUENCY:
 				return frequency != FREQUENCY_EDEFAULT;
 			case StandardPackage.SEASONAL_POPULATION_MODEL__USE_LATITUDE:
 				return useLatitude != USE_LATITUDE_EDEFAULT;
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
 		result.append(" (phase: ");
 		result.append(phase);
 		result.append(", modulationAmplitude: ");
 		result.append(modulationAmplitude);
 		result.append(", frequency: ");
 		result.append(frequency);
 		result.append(", useLatitude: ");
 		result.append(useLatitude);
 		result.append(')');
 		return result.toString();
 	}
 
 } //SeasonalPopulationModelImpl
