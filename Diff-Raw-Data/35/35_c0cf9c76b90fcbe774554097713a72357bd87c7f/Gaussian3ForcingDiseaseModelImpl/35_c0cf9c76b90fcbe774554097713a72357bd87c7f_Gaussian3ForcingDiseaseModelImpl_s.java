 package org.eclipse.stem.diseasemodels.forcing.impl;
 
 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.diseasemodels.forcing.ForcingPackage;
 import org.eclipse.stem.diseasemodels.forcing.Gaussian3ForcingDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Gaussian3 Forcing Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian3ForcingDiseaseModelImpl#getSigma2_3 <em>Sigma2 3</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian3ForcingDiseaseModelImpl#getTransmissionRate2 <em>Transmission Rate2</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian3ForcingDiseaseModelImpl#getTransmissionRate3 <em>Transmission Rate3</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian3ForcingDiseaseModelImpl#getModulationFloor_2 <em>Modulation Floor 2</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class Gaussian3ForcingDiseaseModelImpl extends Gaussian2ForcingDiseaseModelImpl implements Gaussian3ForcingDiseaseModel {
 	/**
 	 * The default value of the '{@link #getSigma2_3() <em>Sigma2 3</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2_3()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double SIGMA2_3_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getSigma2_3() <em>Sigma2 3</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2_3()
 	 * @generated
 	 * @ordered
 	 */
 	protected double sigma2_3 = SIGMA2_3_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTransmissionRate2() <em>Transmission Rate2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTransmissionRate2()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double TRANSMISSION_RATE2_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getTransmissionRate2() <em>Transmission Rate2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTransmissionRate2()
 	 * @generated
 	 * @ordered
 	 */
 	protected double transmissionRate2 = TRANSMISSION_RATE2_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTransmissionRate3() <em>Transmission Rate3</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTransmissionRate3()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double TRANSMISSION_RATE3_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getTransmissionRate3() <em>Transmission Rate3</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTransmissionRate3()
 	 * @generated
 	 * @ordered
 	 */
 	protected double transmissionRate3 = TRANSMISSION_RATE3_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModulationFloor_2() <em>Modulation Floor 2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationFloor_2()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_FLOOR_2_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getModulationFloor_2() <em>Modulation Floor 2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationFloor_2()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationFloor_2 = MODULATION_FLOOR_2_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Gaussian3ForcingDiseaseModelImpl() {
 		super();
 	}
 
 	private Long firstTime = new Long(Long.MAX_VALUE);
 	private Calendar calendar = Calendar.getInstance();
 	private Calendar calendar2 = Calendar.getInstance();
 	private static final double MILLIS_PER_DAY = 1000.0*60.0*60.0*24.0;
 
 	private ArrayList<Long> writtedTimes = new ArrayList<Long>();
 	private FileWriter fw;
	private FileWriter fw2;
 	
	private final int WINDOWSIZE = 30;
 	
 	enum HYP {H0, H1, H2};
 	HYP currentHypothesis = HYP.H2;
 	
 	// private int [] whichGaussian = {0, 0, 0, 1, 0, 2, 0, 0, 2, 0, 1}; // 0=A/H3N2 1=A/H1N1 2=B
 	private int [] whichGaussian = {0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0}; // 0=A 1=B
 	
 	@Override
 	public StandardDiseaseModelLabelValue computeDiseaseDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, final long timeDelta, DiseaseModelLabelValue returnValue) {
 		final SIRLabelValue currentSIR = (SIRLabelValue) currentState;
 		
 		long currentMillis = time.getTime().getTime();
 		double modulationPeriod = getModulationPeriod();
 		double phase = getModulationPhaseShift();	
 		double sigma2 = getSigma2();		
 		double sigma2_2 = getSigma2_2();
 		
 		synchronized(this) {
 			if(firstTime.longValue() == Long.MAX_VALUE)
				firstTime = new Long(time.getTime().getTime());
 		}
 		
 		// Get the day from time and adjust for the phase	
 		double day=0;
 		// Shared calendar object not thread safe
 		synchronized(calendar) {
 			calendar.setTimeInMillis(currentMillis+(long)(phase*MILLIS_PER_DAY));
 			calendar2.setTimeInMillis(firstTime.longValue());
 			day = (calendar.getTimeInMillis() - calendar2.getTimeInMillis())/MILLIS_PER_DAY;
 		}
 		int year = (int)(day / modulationPeriod);
 		int nextDayYear = (int)((day+1) / modulationPeriod);
 		
 		double fday = ((day % modulationPeriod)-modulationPeriod/2.0)/modulationPeriod;
 		
 		int gaussian = whichGaussian[year];
 
 		double f1=0;
 		switch(currentHypothesis) {
 			case H0:
 				f1 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday,2))/(2*sigma2)));
 				break;	
 			case H1:
 				if(gaussian == 0) { // A
 					f1 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday,2))/(2*sigma2)));
 				} else if(gaussian == 1) {
 					f1 = (getAdjustedTransmissionRate(getTransmissionRate2(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday,2))/(2*sigma2)));
 				}
 				break;	
 			case H2:
 				if(gaussian == 0) {
 					f1 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (getModulationFloor() + (1-getModulationFloor())*Math.exp(-(Math.pow(fday,2))/(2*sigma2)));
 				} else if(gaussian==1) {
 					f1 = (getAdjustedTransmissionRate(getTransmissionRate2(),timeDelta))  * (getModulationFloor_2() + (1-getModulationFloor_2())*Math.exp(-(Math.pow(fday,2))/(2*sigma2_2)));
 				}
 				break;
 				
 			default:
 				break;
 		}
 		
 		double modulatedTransmissionRate = 0;
 		// Smoothing
 		if(day % modulationPeriod < WINDOWSIZE || day % modulationPeriod > modulationPeriod-WINDOWSIZE) {
 			
 			double pos;
 			double f2=0, f3=0;
 			int nextGaussian = 0;
 			if(year == whichGaussian.length-1) nextGaussian = gaussian;
 			else nextGaussian = whichGaussian[year+1];
 			
 			int prevGaussian = 0;
 			if(year == 0) prevGaussian = gaussian;
 			else prevGaussian = whichGaussian[year-1];
 			
 			switch(currentHypothesis) {
 				
 				case H0:
 					f2 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday-1,2))/(2*sigma2)));
 					f3 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday+1,2))/(2*sigma2)));
 					break;	
 				case H1:
 					if(nextGaussian == 0)
 						f2 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday-1,2))/(2*sigma2)));
 					else if(nextGaussian == 1) 
 						f2 = (getAdjustedTransmissionRate(getTransmissionRate2(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday-1,2))/(2*sigma2)));
 					if(prevGaussian == 0)
 						f3 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday+1,2))/(2*sigma2)));
 					else if(prevGaussian == 1)
 						f3 = (getAdjustedTransmissionRate(getTransmissionRate2(),timeDelta))  * (modulationFloor + (1-modulationFloor)*Math.exp(-(Math.pow(fday+1,2))/(2*sigma2)));			
 									
 					break;	
 				case H2:
 					if(nextGaussian == 0) 
 						f2 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (getModulationFloor() + (1-getModulationFloor())*Math.exp(-(Math.pow(fday-1,2))/(2*sigma2)));
 					else if(nextGaussian == 1) 
 						f2 = (getAdjustedTransmissionRate(getTransmissionRate2(),timeDelta))  * (getModulationFloor_2() + (1-getModulationFloor_2())*Math.exp(-(Math.pow(fday-1,2))/(2*sigma2_2)));
 					if(prevGaussian == 0)
 						f3 = (getAdjustedTransmissionRate(getTransmissionRate(),timeDelta))  * (getModulationFloor() + (1-getModulationFloor())*Math.exp(-(Math.pow(fday+1,2))/(2*sigma2)));
 					else if(prevGaussian == 1)
 						f3 = (getAdjustedTransmissionRate(getTransmissionRate2(),timeDelta))  * (getModulationFloor_2() + (1-getModulationFloor_2())*Math.exp(-(Math.pow(fday+1,2))/(2*sigma2_2)));			
 					break;
 				
 				default:
 					break;
 			}
 			
 			if((day % modulationPeriod) > modulationPeriod - WINDOWSIZE)
 				pos = WINDOWSIZE - (modulationPeriod - (day % modulationPeriod));
 			else pos = WINDOWSIZE+(day % modulationPeriod);
 			
 			pos = Math.floor(pos);
 			double smooth = 0;
 			if(pos == WINDOWSIZE && nextDayYear == year)
 				smooth = 0.5*f1 + 0.5*f3;
 			else if(pos == WINDOWSIZE && nextDayYear == year+1)
 				smooth = 0.5*f1 +0.5*f2;
 			else if (pos < WINDOWSIZE)
 				smooth = ((2*WINDOWSIZE-pos)/(2*WINDOWSIZE))*f1 + (pos/(2*WINDOWSIZE))*f2;
 			else 
 				smooth = (pos/(2*WINDOWSIZE))*f1 + ((2*WINDOWSIZE-pos)/(2*WINDOWSIZE))*f3;
 
 			modulatedTransmissionRate = smooth;
 		}  else {
 			modulatedTransmissionRate = f1;
 		}
 		
 		
 		
 //		if(!this.isFrequencyDependent())  transmissionRate *= getTransmissionRateScaleFactor(diseaseLabel);
 		
 		// population used in teh computation of bets*S*i where i = Ieffective/Pop.
 		// This includes a correction to the current
 		// infectious population (Ieffective) based on the conserved exchange of people (circulation)
 		// between regions. Note that this is no the "arrivals" and "departures" which are
 		// a different process.
 		final double effectiveInfectious = getNormalizedEffectiveInfectious(diseaseLabel.getNode(), diseaseLabel, currentSIR.getI());
 
 		
 		synchronized(writtedTimes) {
 			if(!writtedTimes.contains(time.getTime().getTime()) && diseaseLabel.getNode().getURI().lastSegment().contains("IL-03-111")) {
 				try {
 				if(fw == null) fw = new FileWriter("beta.csv");
 //				if(fw2 == null) fw2= new FileWriter("r0.csv");
 				fw.write(time.getTime().getTime()+","+modulatedTransmissionRate+"\n");
 //				fw2.write(time.getTime().getTime()+","+transmissionRate+","+((transmissionRate / getAdjustedRecoveryRate(timeDelta)) *currentSIR.getS() / effectivePopulation)+","+getAdjustedRecoveryRate(timeDelta)+","+getAdjustedImmunityLossRate(timeDelta)+","+effectiveInfectious+","+currentSIR.getPopulationCount()+"\n");
 				
 				fw.flush();
 //				fw2.flush();
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 				writtedTimes.add(time.getTime().getTime());
 			}
 		}
 		
 		/*
 		 * Compute state transitions
 		 * 
 		 * Regarding computing the number of transitions from Susceptible to Exposed:
 		 * In a linear model the "effective" number of infectious people is just
 		 * the number of infectious people In a nonlinear model we have a
 		 * nonLinearity exponent that is > 1 this models the effect of immune
 		 * system saturation when Susceptible people are exposed to large
 		 * numbers of infectious people. then the "effective" number of
 		 * infectious people is I^nonLinearity exponent to allow for either
 		 * linear or nonlinear models we always calculate I^nonLinearity
 		 * exponent and allow nonLinearity exponent >= 1.0
 		 */
 		double numberOfInfectedToRecovered = getAdjustedRecoveryRate(timeDelta)
 		* currentSIR.getI();
 		double numberOfRecoveredToSusceptible = getAdjustedImmunityLossRate(timeDelta)
 		* currentSIR.getR();
 		// Need to checked what do do here. If non linear coefficient is not 1 and
 		// the effective infectious is negative (which is possible), what do do? 
 		// Let's fall back on the linear method for now.
 		double numberOfSusceptibleToInfected = 0.0;
 		if(getNonLinearityCoefficient() != 1.0 && effectiveInfectious >= 0.0)
 			numberOfSusceptibleToInfected = modulatedTransmissionRate
 			* currentSIR.getS()* Math.pow(effectiveInfectious, getNonLinearityCoefficient());
 		else
 			numberOfSusceptibleToInfected = modulatedTransmissionRate
 			* currentSIR.getS()* effectiveInfectious;
 		
 		
 		// Determine delta S
 		final double deltaS = numberOfRecoveredToSusceptible - numberOfSusceptibleToInfected;
 			// Determine delta I
 		final double deltaI = numberOfSusceptibleToInfected- numberOfInfectedToRecovered;
 		// Determine delta R
 		final double deltaR = numberOfInfectedToRecovered - numberOfRecoveredToSusceptible;
 		
 		SIRLabelValueImpl ret = (SIRLabelValueImpl)returnValue;
 		ret.setS(deltaS);
 		ret.setI(deltaI);
 		ret.setIncidence(numberOfSusceptibleToInfected);
 		ret.setR(deltaR);
 		ret.setDiseaseDeaths(0);
 		return ret;
 	} // computeTransitions
 	
 	public double getAdjustedTransmissionRate(double tr, long timeDelta) {
 		return tr
 				* ((double) timeDelta / (double) getTimePeriod());
 	} // getAdjustedTransmissionRate
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ForcingPackage.Literals.GAUSSIAN3_FORCING_DISEASE_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getSigma2_3() {
 		return sigma2_3;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSigma2_3(double newSigma2_3) {
 		double oldSigma2_3 = sigma2_3;
 		sigma2_3 = newSigma2_3;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__SIGMA2_3, oldSigma2_3, sigma2_3));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getTransmissionRate2() {
 		return transmissionRate2;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTransmissionRate2(double newTransmissionRate2) {
 		double oldTransmissionRate2 = transmissionRate2;
 		transmissionRate2 = newTransmissionRate2;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE2, oldTransmissionRate2, transmissionRate2));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getTransmissionRate3() {
 		return transmissionRate3;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTransmissionRate3(double newTransmissionRate3) {
 		double oldTransmissionRate3 = transmissionRate3;
 		transmissionRate3 = newTransmissionRate3;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE3, oldTransmissionRate3, transmissionRate3));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationFloor_2() {
 		return modulationFloor_2;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationFloor_2(double newModulationFloor_2) {
 		double oldModulationFloor_2 = modulationFloor_2;
 		modulationFloor_2 = newModulationFloor_2;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__MODULATION_FLOOR_2, oldModulationFloor_2, modulationFloor_2));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__SIGMA2_3:
 				return getSigma2_3();
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE2:
 				return getTransmissionRate2();
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE3:
 				return getTransmissionRate3();
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__MODULATION_FLOOR_2:
 				return getModulationFloor_2();
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
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__SIGMA2_3:
 				setSigma2_3((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE2:
 				setTransmissionRate2((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE3:
 				setTransmissionRate3((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__MODULATION_FLOOR_2:
 				setModulationFloor_2((Double)newValue);
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
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__SIGMA2_3:
 				setSigma2_3(SIGMA2_3_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE2:
 				setTransmissionRate2(TRANSMISSION_RATE2_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE3:
 				setTransmissionRate3(TRANSMISSION_RATE3_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__MODULATION_FLOOR_2:
 				setModulationFloor_2(MODULATION_FLOOR_2_EDEFAULT);
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
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__SIGMA2_3:
 				return sigma2_3 != SIGMA2_3_EDEFAULT;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE2:
 				return transmissionRate2 != TRANSMISSION_RATE2_EDEFAULT;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__TRANSMISSION_RATE3:
 				return transmissionRate3 != TRANSMISSION_RATE3_EDEFAULT;
 			case ForcingPackage.GAUSSIAN3_FORCING_DISEASE_MODEL__MODULATION_FLOOR_2:
 				return modulationFloor_2 != MODULATION_FLOOR_2_EDEFAULT;
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
 		result.append(" (sigma2_3: "); //$NON-NLS-1$
 		result.append(sigma2_3);
 		result.append(", transmissionRate2: "); //$NON-NLS-1$
 		result.append(transmissionRate2);
 		result.append(", transmissionRate3: "); //$NON-NLS-1$
 		result.append(transmissionRate3);
 		result.append(", modulationFloor_2: "); //$NON-NLS-1$
 		result.append(modulationFloor_2);
 		result.append(')');
 		return result.toString();
 	}
 
 } //Gaussian3ForcingDiseaseModelImpl
