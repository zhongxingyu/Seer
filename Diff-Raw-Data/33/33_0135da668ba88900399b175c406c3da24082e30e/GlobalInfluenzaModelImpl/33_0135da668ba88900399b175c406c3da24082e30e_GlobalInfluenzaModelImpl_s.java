 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.stem.diseasemodels.globalinfluenzamodel.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.data.geography.centers.GeographicCenters;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProvider;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProviderAdapter;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProviderAdapterFactory;
 import org.eclipse.stem.definitions.nodes.impl.RegionImpl;
 import org.eclipse.stem.diseasemodels.Activator;
 import org.eclipse.stem.diseasemodels.globalinfluenzamodel.GlobalInfluenzaModel;
 import org.eclipse.stem.diseasemodels.globalinfluenzamodel.GlobalinfluenzamodelPackage;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SILabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl;
 import org.eclipse.stem.diseasemodels.standard.impl.StochasticSIRDiseaseModelImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Global Influenza Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.globalinfluenzamodel.impl.GlobalInfluenzaModelImpl#getLatitudeSigmoidWidth <em>Latitude Sigmoid Width</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.globalinfluenzamodel.impl.GlobalInfluenzaModelImpl#getSeasonalModulationExponent <em>Seasonal Modulation Exponent</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.globalinfluenzamodel.impl.GlobalInfluenzaModelImpl#getModulationPeriod <em>Modulation Period</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.globalinfluenzamodel.impl.GlobalInfluenzaModelImpl#getModulationPhaseShift <em>Modulation Phase Shift</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.globalinfluenzamodel.impl.GlobalInfluenzaModelImpl#getSeasonalModulationFloor <em>Seasonal Modulation Floor</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class GlobalInfluenzaModelImpl extends StochasticSIRDiseaseModelImpl implements GlobalInfluenzaModel {
 
 	private static final double MILLIS_PER_DAY = 1000.0*60.0*60.0*24.0;
 	
 	/**
 	 * The default value of the '{@link #getLatitudeSigmoidWidth() <em>Latitude Sigmoid Width</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLatitudeSigmoidWidth()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double LATITUDE_SIGMOID_WIDTH_EDEFAULT = 4.5;
 	
 	/**
 	 * the equator
 	 */
 	private static final double EQUATOR_LATITUDE = 0.0;
 	
 
 	/**
 	 * The cached value of the '{@link #getLatitudeSigmoidWidth() <em>Latitude Sigmoid Width</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLatitudeSigmoidWidth()
 	 * @generated
 	 * @ordered
 	 */
 	protected double latitudeSigmoidWidth = LATITUDE_SIGMOID_WIDTH_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getSeasonalModulationExponent() <em>Seasonal Modulation Exponent</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSeasonalModulationExponent()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double SEASONAL_MODULATION_EXPONENT_EDEFAULT = 2.0;
 
 	/**
 	 * The cached value of the '{@link #getSeasonalModulationExponent() <em>Seasonal Modulation Exponent</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSeasonalModulationExponent()
 	 * @generated
 	 * @ordered
 	 */
 	protected double seasonalModulationExponent = SEASONAL_MODULATION_EXPONENT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModulationPeriod() <em>Modulation Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_PERIOD_EDEFAULT = 365.256363051;
 
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
 	 * The default value of the '{@link #getSeasonalModulationFloor() <em>Seasonal Modulation Floor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSeasonalModulationFloor()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double SEASONAL_MODULATION_FLOOR_EDEFAULT = 0.6;
 
 	/**
 	 * The cached value of the '{@link #getSeasonalModulationFloor() <em>Seasonal Modulation Floor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSeasonalModulationFloor()
 	 * @generated
 	 * @ordered
 	 */
 	protected double seasonalModulationFloor = SEASONAL_MODULATION_FLOOR_EDEFAULT;
 	
 	public static long firstDay = -1;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public GlobalInfluenzaModelImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GlobalinfluenzamodelPackage.Literals.GLOBAL_INFLUENZA_MODEL;
 	}
 
 	
 
 	/**
 	 * To model influenza on a global scale, this method takes into account
 	 * the modulation of the transmission coefficient of the virus as a function of Latitude.
 	 * Near the tropics the transmission coefficient is almost constant (and at a value
 	 * called the SEASONAL_MODULATION_FLOOR.
 	 * As one moved in latitude away from the tropics the modulation (and MAXIMUM) amplitude of the 
 	 * transmission coefficient increases. We model this seasonal increase 	 
 	 * We model this with a sigmoidal function that goes from zero to one
 	 * with center at the tropic of cancer, and the tropic of Capricorn.
 	 * The default width factor is 4.5 which gives the sigmoid a slope proportional to the 
 	 * observed variation in seasonal influenza epidemics as a function of latitude in Brazil.
 	 * See: http://aje.oxfordjournals.org/cgi/content/full/165/12/1434
 	 * 
 	 * As our sigmoid we use the basic logistic function 1/1+exp(-x) 
 	 * so
 	 * Sigmoid Function (lat)  = SeasonalModulationFloor/(1.0 + exp((TROPIC_OF_CANCER_LATITUDE - abs(Lat))./4.5) );
 	 * This is near zero near the equator and goes to SeasonalModulationFloor above and below 
 	 * the tropics.
 	 * 
 	 * 
 	 * @param time
 	 * 			  current time
 	 * @param currentState
 	 *            the current state of the population
 	 * @param diseaseLabel
 	 *            the disease label for which the state transitions are being
 	 *            computed.
 	 * @param timeDelta
 	 *            the time period (milliseconds) over which the population
 	 *            members transition to new states          
 	 * @return a disease state label value that contains the number of
 	 *         delta changes in each disease state
 	 * 
 	 * Users can modify the method below to create their own model.
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIImpl#computeDiseaseDeltas(StandardDiseaseModelLabelValue,
 	 *      StandardDiseaseModelLabel, long)
 	 */
 	@Override
 	public StandardDiseaseModelLabelValue computeDiseaseDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, final long timeDelta, DiseaseModelLabelValue returnValue) {
 			final SIRLabelValue currentSIR = (SIRLabelValue) currentState;
 		
 		long currentMillis = time.getTime().getTime();
 		double seasonalModulationExponent = getSeasonalModulationExponent();
 		double seasonalModulationFloor = getSeasonalModulationFloor();
 		double modulationPeriod = getModulationPeriod();
 		final double absolutePhase = getModulationPhaseShift();	
 		
 		
 		/**
 		 * the latitude of this regions
 		 */
 		double latitude = EQUATOR_LATITUDE;
 
 		
 		/**
 		 * the coordinates of the center of this regions
 		 */
 		double[] lat_long = null;
 		
 		if (firstDay == -1) {
 			firstDay = currentMillis;
 		}
 		
 		Node node = diseaseLabel.getNode();
 		
 		if(node instanceof RegionImpl) {
 			String nodeURI = node.getURI().lastSegment();
 			lat_long = GeographicCenters.getCenter(nodeURI);
 			// still null? Compute it
 			if(lat_long==null) {
 				// Get the lat/long of the center of the node
 				final LatLongProviderAdapter latLongProviderB = (LatLongProviderAdapter) LatLongProviderAdapterFactory.INSTANCE
 						.adapt(node, LatLongProvider.class);
 				latLongProviderB.setTarget(node);
 				lat_long = latLongProviderB.getCenter();
 			}
 			if (lat_long == null) {
 				Activator.logError("Cannot find latitude for "+ nodeURI, null);
 			}
 		} else {
 			lat_long = new double[2];
 			lat_long[0] = EQUATOR_LATITUDE;
 			lat_long[1] = EQUATOR_LATITUDE; // not used. sets it to 0,0
 		}
 
 
 		// get the latitude
 		if(lat_long != null) {
 			latitude = lat_long[0] ;
 		} 
 		
 		/////////////////////////////////////////////////////
 		// corrects phase for northern vs southern hemisphere
 		double phase = absolutePhase;
 		if(latitude<0.0) phase += Math.PI/2.0;
 		/////////////////////////////////////////////////////
 		
 		
 		//////////////////////////////////////////
 		// now compute the transmission rate
 		// with the seasonal forcing function
 		// adjusted by latitude
 		double latFactor  = seasonalModulationFloor/(1.0 + Math.exp((TROPIC_OF_CANCER_LATITUDE - Math.abs(latitude))/latitudeSigmoidWidth) );
 		latFactor *= Math.pow(Math.abs(Math.cos(phase + Math.PI*currentMillis/(modulationPeriod*MILLIS_PER_DAY))), seasonalModulationExponent);
 		double modulation = ( (1.0-(seasonalModulationFloor/2.0)) + (latFactor/2.0) );
 		// This is beta
 		double transmissionRate = modulation * (getAdjustedTransmissionRate(timeDelta));
 		/////////////////////////////////////////
 		
 		//LogWriter.write(latitude +","+ Math.round((currentMillis - firstDay)/86400000) +"," + transmissionRate);
 		
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
 		
 		// For the finite difference method make sure we don't exceed the number of people
 		// available in any state
 		
 		if(this.isFiniteDifference()) {
 			if(numberOfRecoveredToSusceptible > currentSIR.getR()) numberOfRecoveredToSusceptible = currentSIR.getR();
 			if(numberOfSusceptibleToInfected > currentSIR.getS()) numberOfSusceptibleToInfected = currentSIR.getS();
 			if(numberOfInfectedToRecovered > currentSIR.getI()) numberOfInfectedToRecovered = currentSIR.getI();
 		}
 		
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
 		ret.setBirths(0);
 		ret.setDeaths(0);
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
 	public double getLatitudeSigmoidWidth() {
 		return latitudeSigmoidWidth;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLatitudeSigmoidWidth(double newLatitudeSigmoidWidth) {
 		double oldLatitudeSigmoidWidth = latitudeSigmoidWidth;
 		latitudeSigmoidWidth = newLatitudeSigmoidWidth;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__LATITUDE_SIGMOID_WIDTH, oldLatitudeSigmoidWidth, latitudeSigmoidWidth));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getSeasonalModulationExponent() {
 		return seasonalModulationExponent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSeasonalModulationExponent(double newSeasonalModulationExponent) {
 		double oldSeasonalModulationExponent = seasonalModulationExponent;
 		seasonalModulationExponent = newSeasonalModulationExponent;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_EXPONENT, oldSeasonalModulationExponent, seasonalModulationExponent));
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
 			eNotify(new ENotificationImpl(this, Notification.SET, GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PERIOD, oldModulationPeriod, modulationPeriod));
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
 			eNotify(new ENotificationImpl(this, Notification.SET, GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PHASE_SHIFT, oldModulationPhaseShift, modulationPhaseShift));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getSeasonalModulationFloor() {
 		return seasonalModulationFloor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSeasonalModulationFloor(double newSeasonalModulationFloor) {
 		double oldSeasonalModulationFloor = seasonalModulationFloor;
 		seasonalModulationFloor = newSeasonalModulationFloor;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_FLOOR, oldSeasonalModulationFloor, seasonalModulationFloor));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__LATITUDE_SIGMOID_WIDTH:
 				return getLatitudeSigmoidWidth();
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_EXPONENT:
 				return getSeasonalModulationExponent();
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PERIOD:
 				return getModulationPeriod();
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PHASE_SHIFT:
 				return getModulationPhaseShift();
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_FLOOR:
 				return getSeasonalModulationFloor();
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
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__LATITUDE_SIGMOID_WIDTH:
 				setLatitudeSigmoidWidth((Double)newValue);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_EXPONENT:
 				setSeasonalModulationExponent((Double)newValue);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PERIOD:
 				setModulationPeriod((Double)newValue);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PHASE_SHIFT:
 				setModulationPhaseShift((Double)newValue);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_FLOOR:
 				setSeasonalModulationFloor((Double)newValue);
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
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__LATITUDE_SIGMOID_WIDTH:
 				setLatitudeSigmoidWidth(LATITUDE_SIGMOID_WIDTH_EDEFAULT);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_EXPONENT:
 				setSeasonalModulationExponent(SEASONAL_MODULATION_EXPONENT_EDEFAULT);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PERIOD:
 				setModulationPeriod(MODULATION_PERIOD_EDEFAULT);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PHASE_SHIFT:
 				setModulationPhaseShift(MODULATION_PHASE_SHIFT_EDEFAULT);
 				return;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_FLOOR:
 				setSeasonalModulationFloor(SEASONAL_MODULATION_FLOOR_EDEFAULT);
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
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__LATITUDE_SIGMOID_WIDTH:
 				return latitudeSigmoidWidth != LATITUDE_SIGMOID_WIDTH_EDEFAULT;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_EXPONENT:
 				return seasonalModulationExponent != SEASONAL_MODULATION_EXPONENT_EDEFAULT;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PERIOD:
 				return modulationPeriod != MODULATION_PERIOD_EDEFAULT;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__MODULATION_PHASE_SHIFT:
 				return modulationPhaseShift != MODULATION_PHASE_SHIFT_EDEFAULT;
 			case GlobalinfluenzamodelPackage.GLOBAL_INFLUENZA_MODEL__SEASONAL_MODULATION_FLOOR:
 				return seasonalModulationFloor != SEASONAL_MODULATION_FLOOR_EDEFAULT;
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
 		result.append(" (latitudeSigmoidWidth: ");
 		result.append(latitudeSigmoidWidth);
 		result.append(", seasonalModulationExponent: ");
 		result.append(seasonalModulationExponent);
 		result.append(", modulationPeriod: ");
 		result.append(modulationPeriod);
 		result.append(", modulationPhaseShift: ");
 		result.append(modulationPhaseShift);
 		result.append(", seasonalModulationFloor: ");
 		result.append(seasonalModulationFloor);
 		result.append(')');
 		return result.toString();
 	}
 
 } //GlobalInfluenzaModelImpl
