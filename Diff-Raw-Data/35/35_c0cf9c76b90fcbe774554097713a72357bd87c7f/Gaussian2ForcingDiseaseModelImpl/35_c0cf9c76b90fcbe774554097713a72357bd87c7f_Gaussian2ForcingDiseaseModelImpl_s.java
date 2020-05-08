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
 import org.eclipse.stem.diseasemodels.forcing.Gaussian2ForcingDiseaseModel;
 
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SILabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl;
 import org.eclipse.stem.diseasemodels.standard.impl.StochasticSIRDiseaseModelImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Gaussian2 Forcing Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getSigma2_2 <em>Sigma2 2</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getSigma2 <em>Sigma2</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getModulationPeriod <em>Modulation Period</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getModulationPhaseShift <em>Modulation Phase Shift</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getModulationFloor <em>Modulation Floor</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getAtt1 <em>Att1</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getAtt2 <em>Att2</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getAtt3 <em>Att3</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.Gaussian2ForcingDiseaseModelImpl#getAtt4 <em>Att4</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class Gaussian2ForcingDiseaseModelImpl extends StochasticSIRDiseaseModelImpl implements Gaussian2ForcingDiseaseModel {
 	/**
 	 * The default value of the '{@link #getSigma2_2() <em>Sigma2 2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2_2()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double SIGMA2_2_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getSigma2_2() <em>Sigma2 2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2_2()
 	 * @generated
 	 * @ordered
 	 */
 	protected double sigma2_2 = SIGMA2_2_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getSigma2() <em>Sigma2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double SIGMA2_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getSigma2() <em>Sigma2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2()
 	 * @generated
 	 * @ordered
 	 */
 	protected double sigma2 = SIGMA2_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModulationPeriod() <em>Modulation Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_PERIOD_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getModulationPeriod() <em>Modulation Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationPeriod = MODULATION_PERIOD_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModulationPhaseShift() <em>Modulation Phase Shift</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPhaseShift()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_PHASE_SHIFT_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getModulationPhaseShift() <em>Modulation Phase Shift</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPhaseShift()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationPhaseShift = MODULATION_PHASE_SHIFT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModulationFloor() <em>Modulation Floor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationFloor()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_FLOOR_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getModulationFloor() <em>Modulation Floor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationFloor()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationFloor = MODULATION_FLOOR_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAtt1() <em>Att1</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt1()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double ATT1_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getAtt1() <em>Att1</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt1()
 	 * @generated
 	 * @ordered
 	 */
 	protected double att1 = ATT1_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAtt2() <em>Att2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt2()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double ATT2_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getAtt2() <em>Att2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt2()
 	 * @generated
 	 * @ordered
 	 */
 	protected double att2 = ATT2_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAtt3() <em>Att3</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt3()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double ATT3_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getAtt3() <em>Att3</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt3()
 	 * @generated
 	 * @ordered
 	 */
 	protected double att3 = ATT3_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAtt4() <em>Att4</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt4()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double ATT4_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getAtt4() <em>Att4</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAtt4()
 	 * @generated
 	 * @ordered
 	 */
 	protected double att4 = ATT4_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Gaussian2ForcingDiseaseModelImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ForcingPackage.Literals.GAUSSIAN2_FORCING_DISEASE_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getSigma2_2() {
 		return sigma2_2;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSigma2_2(double newSigma2_2) {
 		double oldSigma2_2 = sigma2_2;
 		sigma2_2 = newSigma2_2;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2_2, oldSigma2_2, sigma2_2));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getSigma2() {
 		return sigma2;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSigma2(double newSigma2) {
 		double oldSigma2 = sigma2;
 		sigma2 = newSigma2;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2, oldSigma2, sigma2));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationPeriod() {
 		return modulationPeriod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationPeriod(double newModulationPeriod) {
 		double oldModulationPeriod = modulationPeriod;
 		modulationPeriod = newModulationPeriod;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PERIOD, oldModulationPeriod, modulationPeriod));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationPhaseShift() {
 		return modulationPhaseShift;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationPhaseShift(double newModulationPhaseShift) {
 		double oldModulationPhaseShift = modulationPhaseShift;
 		modulationPhaseShift = newModulationPhaseShift;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT, oldModulationPhaseShift, modulationPhaseShift));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationFloor() {
 		return modulationFloor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationFloor(double newModulationFloor) {
 		double oldModulationFloor = modulationFloor;
 		modulationFloor = newModulationFloor;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_FLOOR, oldModulationFloor, modulationFloor));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getAtt1() {
 		return att1;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAtt1(double newAtt1) {
 		double oldAtt1 = att1;
 		att1 = newAtt1;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT1, oldAtt1, att1));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getAtt2() {
 		return att2;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAtt2(double newAtt2) {
 		double oldAtt2 = att2;
 		att2 = newAtt2;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT2, oldAtt2, att2));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getAtt3() {
 		return att3;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAtt3(double newAtt3) {
 		double oldAtt3 = att3;
 		att3 = newAtt3;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT3, oldAtt3, att3));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getAtt4() {
 		return att4;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAtt4(double newAtt4) {
 		double oldAtt4 = att4;
 		att4 = newAtt4;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT4, oldAtt4, att4));
 	}
 
 	private Long firstTime = new Long(Long.MAX_VALUE);
 	private Calendar calendar = Calendar.getInstance();
 	private Calendar calendar2 = Calendar.getInstance();
 	private static final double MILLIS_PER_DAY = 1000.0*60.0*60.0*24.0;
 
 	private ArrayList<Long> writtedTimes = new ArrayList<Long>();
 	private FileWriter fw;
 	@Override
 	public StandardDiseaseModelLabelValue computeDiseaseDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, final long timeDelta, DiseaseModelLabelValue returnValue) {
 final SIRLabelValue currentSIR = (SIRLabelValue) currentState;
 		
 		long currentMillis = time.getTime().getTime();
 		double seasonalModulationFloor = getModulationFloor();
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
 			calendar.setTimeInMillis(currentMillis);
 			calendar2.setTimeInMillis(firstTime.longValue());
 			day = (calendar.getTimeInMillis() - calendar2.getTimeInMillis())/MILLIS_PER_DAY;
 		}
 		
 		
 		double mu1 = phase;
 		double mu2 = phase + modulationPeriod;
 		
 		double modulation1 = (1/Math.sqrt(2*Math.PI*sigma2))*Math.exp(-(Math.pow(day-mu1,2))/(2*sigma2));						
 		double modulation2 = (1/Math.sqrt(2*Math.PI*sigma2_2))*Math.exp(-(Math.pow(day-mu2,2))/(2*sigma2_2));
 		
 		// This is beta*
 		double transmissionRate = seasonalModulationFloor + (getAdjustedTransmissionRate(timeDelta)) * (modulation1+modulation2);
 
 		if(day >= 116 && day <= 119) // April 24 - 27
 			transmissionRate = transmissionRate * this.getAtt1();
 		else 	if(day >= 120 && day <= 127) // April 28 - May 5
 			transmissionRate = transmissionRate * this.getAtt2();
 		else if(day >= 128 && day <= 132) // May 6 - May 10
 			transmissionRate = transmissionRate * this.getAtt3();
 		
 		synchronized(writtedTimes) {
 			if(!writtedTimes.contains(time.getTime().getTime())) {
 				try {
 				if(fw == null) fw = new FileWriter("beta.csv");
 				fw.write(time.getTime().getTime()+","+transmissionRate+"\n");
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 				writtedTimes.add(time.getTime().getTime());
 			}
 		}
 		
 		if(!this.isFrequencyDependent())  transmissionRate *= getTransmissionRateScaleFactor(diseaseLabel);
 		
 		// The effective Infectious population  is a dimensionles number normalize by total
 		// population used in teh computation of bets*S*i where i = Ieffective/Pop.
 		// This includes a correction to the current
 		// infectious population (Ieffective) based on the conserved exchange of people (circulation)
 		// between regions. Note that this is no the "arrivals" and "departures" which are
 		// a different process.
 		final double effectiveInfectious = getNormalizedEffectiveInfectious(diseaseLabel.getNode(), diseaseLabel, currentSIR.getI());
 
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
 			numberOfSusceptibleToInfected = transmissionRate
 			* currentSIR.getS()* Math.pow(effectiveInfectious, getNonLinearityCoefficient());
 		else
 			numberOfSusceptibleToInfected = transmissionRate
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
 		ret.setIncidence(numberOfInfectedToRecovered);
 		ret.setR(deltaR);
 		ret.setDiseaseDeaths(0);
 		return ret;
 	} // computeTransitions
 	
 	/**
 	 * ModelSpecificAdjustments for a Stochastic model adds noise to or adjusts 
 	 * the disease state transition values by multiplying
 	 * the additions by a random variable r ~ (1+/-x) with x small.
 	 * The requirements that no more individuals can be moved from a state than are
 	 * already in that state is still enforced.
 	 * 
 	 */
 	@Override
 	public void doModelSpecificAdjustments(
 			final StandardDiseaseModelLabelValue state) {
 			final SILabelValue currentSI = (SILabelValue) state;
 			double oldI = currentSI.getI();
 			double Inoisy = currentSI.getI()* computeNoise();
 			double change = oldI-Inoisy;
 			currentSI.setI(Inoisy);
 			double newS = currentSI.getS() + change;
 			if(newS < 0.0) {
 				// Need to rescale
 				double scale = (currentSI.getS() + newS) / currentSI.getS();
 				currentSI.setI(Inoisy*scale);
 			} else  currentSI.setS(newS);
 			return;
 	} // doModelSpecificAdjustments
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2_2:
 				return getSigma2_2();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2:
 				return getSigma2();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				return getModulationPeriod();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				return getModulationPhaseShift();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				return getModulationFloor();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT1:
 				return getAtt1();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT2:
 				return getAtt2();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT3:
 				return getAtt3();
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT4:
 				return getAtt4();
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
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2_2:
 				setSigma2_2((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2:
 				setSigma2((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				setModulationPeriod((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				setModulationPhaseShift((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				setModulationFloor((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT1:
 				setAtt1((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT2:
 				setAtt2((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT3:
 				setAtt3((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT4:
 				setAtt4((Double)newValue);
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
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2_2:
 				setSigma2_2(SIGMA2_2_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2:
 				setSigma2(SIGMA2_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				setModulationPeriod(MODULATION_PERIOD_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				setModulationPhaseShift(MODULATION_PHASE_SHIFT_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				setModulationFloor(MODULATION_FLOOR_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT1:
 				setAtt1(ATT1_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT2:
 				setAtt2(ATT2_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT3:
 				setAtt3(ATT3_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT4:
 				setAtt4(ATT4_EDEFAULT);
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
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2_2:
 				return sigma2_2 != SIGMA2_2_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__SIGMA2:
 				return sigma2 != SIGMA2_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				return modulationPeriod != MODULATION_PERIOD_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				return modulationPhaseShift != MODULATION_PHASE_SHIFT_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				return modulationFloor != MODULATION_FLOOR_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT1:
 				return att1 != ATT1_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT2:
 				return att2 != ATT2_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT3:
 				return att3 != ATT3_EDEFAULT;
 			case ForcingPackage.GAUSSIAN2_FORCING_DISEASE_MODEL__ATT4:
 				return att4 != ATT4_EDEFAULT;
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
 		result.append(" (sigma2_2: "); //$NON-NLS-1$
 		result.append(sigma2_2);
 		result.append(", sigma2: "); //$NON-NLS-1$
 		result.append(sigma2);
 		result.append(", modulationPeriod: "); //$NON-NLS-1$
 		result.append(modulationPeriod);
 		result.append(", modulationPhaseShift: "); //$NON-NLS-1$
 		result.append(modulationPhaseShift);
 		result.append(", modulationFloor: "); //$NON-NLS-1$
 		result.append(modulationFloor);
 		result.append(", att1: "); //$NON-NLS-1$
 		result.append(att1);
 		result.append(", att2: "); //$NON-NLS-1$
 		result.append(att2);
 		result.append(", att3: "); //$NON-NLS-1$
 		result.append(att3);
 		result.append(", att4: "); //$NON-NLS-1$
 		result.append(att4);
 		result.append(')');
 		return result.toString();
 	}
 
 } //Gaussian2ForcingDiseaseModelImpl
