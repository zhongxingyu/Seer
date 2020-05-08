 // ExternalDataSourceDiseaseModelImpl
 package org.eclipse.stem.diseasemodels.externaldatasource.impl;
 
 
 /*******************************************************************************
  * Copyright (c) 2007, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.ui.provider.PropertySource;
 import org.eclipse.stem.analysis.ScenarioInitializationException;
 import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl;
 import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl.ReferenceScenarioDataInstance;
 import org.eclipse.stem.analysis.util.CSVscenarioLoader;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.LabelValue;
import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.labels.AreaLabel;
 import org.eclipse.stem.definitions.labels.PopulationLabel;
 import org.eclipse.stem.diseasemodels.Activator;
 import org.eclipse.stem.diseasemodels.externaldatasource.ExternalDataSourceDiseaseModel;
 import org.eclipse.stem.diseasemodels.externaldatasource.ExternalDataSourcePackage;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelState;
 import org.eclipse.stem.diseasemodels.standard.Infector;
 import org.eclipse.stem.diseasemodels.standard.SEIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SILabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelState;
 import org.eclipse.stem.diseasemodels.standard.StandardFactory;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 import org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl;
 import org.eclipse.stem.diseasemodels.standard.impl.SEIRLabelValueImpl;
 import org.eclipse.stem.diseasemodels.standard.impl.SILabelValueImpl;
 import org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl;
 import org.eclipse.stem.diseasemodels.standard.provider.StandardItemProviderAdapterFactory;
 
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.externaldatasource.impl.ExternalDataSourceDiseaseModelImpl#getDataPath <em>Data Path</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.externaldatasource.impl.ExternalDataSourceDiseaseModelImpl#getDiseaseType <em>Disease Type</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ExternalDataSourceDiseaseModelImpl extends DiseaseModelImpl implements ExternalDataSourceDiseaseModel {
 	
     /**
 	 * The default value of the '{@link #getDataPath() <em>Data Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDataPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DATA_PATH_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getDataPath() <em>Data Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDataPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected String dataPath = DATA_PATH_EDEFAULT;
 	
	private final static String URI_PREFIX_PATTERN = "geo/region/";
 	
 	/**
 	 * The default value of the '{@link #getDiseaseType() <em>Disease Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDiseaseType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DISEASE_TYPE_EDEFAULT = null;
 	/**
 	 * This prefix precedes the node id and must be removed in the 
 	 * filterFilename method to auto-generate the output file name.
 	 */
 	public static final String LOCATIONID_PREFIX = "/node/geo/region/";
 	
 	
 	/**
 	 * Directory containing the scenario to import and play back
 	 */
 	public String dirName = DEFAULT_DIR;
 
 	/**
 	 * a map of maps (one for each location containing a data file
 	 */
 	private ReferenceScenarioDataMapImpl scenarioMap = null;
 	
 	/**
 	 * a list of data for each location keyed by nodeID
 	 */
 	private ReferenceScenarioDataInstance dataInstance = null;
 	
 	
 	/**
 	 * a false (Hidden) time counter used for this toy example
 	 */
 	private int fileLineCounter = 0;
 	
 	public static String labelS  = null ;
 	public static String labelE  = null ;
 	public static String labelI = null ;
 	public static String labelR  = null ;
 	
 	/**
      * not used
 	 */
 	private double totalPopulationCount = 0.0;
 	private double totalArea = 0.0;
 	
 	private String diseaseType;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public ExternalDataSourceDiseaseModelImpl() {
 		super();
 	}
 
 	
 	
 
 	/**
 	 * @see org.eclipse.stem.core.model.impl.DecoratorImpl#updateLabels(org.eclipse.stem.core.graph.Graph,
 	 *      org.eclipse.stem.core.model.STEMTime)
 	 */
 	@Override
 	public void updateLabels(final STEMTime time, final long timeDelta, int cycle) {
 		
 		
 		// MAD 10/29/2009
 		// Commented out code, moved to "calculateDelta" for IntegrationDecorator.
 		// Not sure what updates need to happen to "updateLabels".
 		
 		/*
 		// Iterate through each of the labels we need to update.
 		for (final Iterator<DynamicLabel> currentStateLabelIter = getLabelsToUpdate()
 				.iterator(); currentStateLabelIter.hasNext();) {
 			final StandardDiseaseModelLabel diseaseLabel = (StandardDiseaseModelLabel) currentStateLabelIter
 					.next();
 
 			assert diseaseLabel.getPopulationLabel().getPopulationIdentifier()
 					.equals(getPopulationIdentifier());
 
 			// This is the current state of the disease for this label
 			final StandardDiseaseModelLabelValue currentState = (StandardDiseaseModelLabelValue)diseaseLabel
 					.getCurrentValue();
 
 		
 			// 2) Compute the state transitions
 			final StandardDiseaseModelLabelValue nullAdditions = importDiseaseData(currentState, diseaseLabel, time, timeDelta);
 
 			
 			// This will be the state that we compute.
 			final StandardDiseaseModelLabelValue nextState = (StandardDiseaseModelLabelValue)diseaseLabel.getNextValue();
 
 			// Initialize the next state from the current state and then we'll
 			// make the changes to that.
 			nextState.set(currentState);
 			
 			
 			
 			assert nextState.sane():"disease state is insane after subtracting deaths: "+nextState.toString();
 			
 			
 			// The next value is valid now.
 			diseaseLabel.setNextValueValid(true);
 
 			
 		} // for
 		++fileLineCounter;
 		*/
 	} // updateLabels
 
 	/**
 	 * get the constants which identify the disease label state variables
 	 * @param diseaseLabel
 	 */
 	private static void setPropertyLabels() {
 		SEIRLabelValue diseaseLabel = StandardFactory.eINSTANCE.createSEIRLabel().getCurrentSEIRValue();
 		StandardItemProviderAdapterFactory itemProviderFactory = new StandardItemProviderAdapterFactory();
 		IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory.adapt(diseaseLabel, PropertySource.class);
 		List<IItemPropertyDescriptor> properties = propertySource.getPropertyDescriptors(null);
 		
 		IItemPropertyDescriptor propertyS = properties.get(StandardPackage.SEIR_LABEL_VALUE__S);
 		IItemPropertyDescriptor propertyE = properties.get(StandardPackage.SEIR_LABEL_VALUE__E);
 		IItemPropertyDescriptor propertyI = properties.get(StandardPackage.SEIR_LABEL_VALUE__I);
 		IItemPropertyDescriptor propertyR = properties.get(StandardPackage.SEIR_LABEL_VALUE__R);
 		
 		labelS = propertyS.getDisplayName(propertyS);
 		labelE = propertyE.getDisplayName(propertyE);
 		labelI = propertyI.getDisplayName(propertyI);
 		labelR = propertyR.getDisplayName(propertyR);
 	}// setPropertyLabels
 
 	
 	/**
 	 * 
 	 * This method reads the next state data from the external dataFile
 	 * 
 	 * @param currentState
 	 * @param diseaseLabel
 	 * @param time
 	 * @param timeDelta
 	 * @return
 	 */
 	public StandardDiseaseModelLabelValue importDiseaseData(
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, final STEMTime time, final long timeDelta) {
 		
 		if(labelS==null) setPropertyLabels();
 		
 	    // TODO
 		// the filename actually comes from the node itself
 		// we need to regen the code with a scenarioDirectory 
 		// specification as every node maps to a different data file. for now we will test
 		// with only one node and one file
 		// need a map of maps keyed by location name
 
 		
 		Identifiable ident = diseaseLabel.getIdentifiable();
 		String fileName = ident.getURI().toString();
 		int last = fileName.lastIndexOf(LOCATIONID_PREFIX);
 		last += LOCATIONID_PREFIX.length();
 		String key = fileName.substring(last,fileName.length());
 		if(scenarioMap == null) { // Not yet loaded
 			try {
 				CSVscenarioLoader loader = new CSVscenarioLoader(this.dataPath);
 				int maxresolution = loader.getMaxResolution();
 				scenarioMap = loader.parseAllFiles();
 				// Set the disease type here since we don't need that
 				// input from the end-user any longer
 				this.diseaseType = loader.getType().name();
 			} catch(ScenarioInitializationException sie) {
 				Activator.logError("Error reading scenario files", sie);
 				return null;
 			}
 		}
 
 		dataInstance = scenarioMap.getLocation(key);
 		// if no data
 		if((dataInstance==null)||(dataInstance.getSize() == 0)) {
 			// no data for location in questions
 			if (diseaseType.equals(IMPORT_TYPE_SI)) {
 				return new SILabelValueImpl(0.0 , 0.0, 0.0, 0.0);
 			} else if (diseaseType.equals(IMPORT_TYPE_SIR)) {
 				return new SIRLabelValueImpl(0.0, 0.0 , 0.0, 0.0, 0.0);
 			} else if (diseaseType.equals(IMPORT_TYPE_SEIR)) {
 				return new SEIRLabelValueImpl( 0.0, 0.0, 0.0 , 0.0, 0.0, 0.0);
 			} else {
 				throw new UnsupportedOperationException("ExternalDataSource Invalid Type "+diseaseType+" must be SI, SIR, or SEIR");
 			}
 		}
 		
 		// JHK Notes: HOW DO I GET THE CURRENT STEP??
 		//  
 		// node >> graph
 		// graph >> scenario using SimulationManger method
 		// SimulationManger.getManger()
 		// then call manager.mapGraphToSimulation(final Graph graph) {
 		// to get the rvhp you just have to adapt it again.
 		
 		// csv diseases read from file know their own
 		// time period property in millisecs
 		// This method is called by "updateLabels"
 		// should really override that instead fo compute transitions
 		// updateLabels has the TIME.
 		
 		// TODO
 		// remove filter in StandardRelativeValueProviderAdapterFactory 
 		// which is preventing disease deaths from being logged.
 		// method is StandardRelativeValueProviderAdapterFactory List<IItemPropertyDescriptor> getProperties()
 				
 		double s  = 0.0;
 		double e  = 0.0;
 		double i = 0.0;
 		double r  = 0.0;
 		
 		
 	
 //		String id = ident.getURI().toString();
 //		int strt = id.indexOf(URI_PREFIX_PATTERN);
 //		if(strt>=0) {
 //			id = id.substring(strt+URI_PREFIX_PATTERN.length(),id.length());
 //		}
 		
 //		if((dataInstance!=null)&&(dataInstance.instance.containsKey(id))) {
 		
 		if(dataInstance!=null) {
 			// compute the changes
 			if(dataInstance.getData(labelS).size() > fileLineCounter) {
 				String sString1 = dataInstance.getData(labelS).get(fileLineCounter);
 				s = new Double(sString1).doubleValue();
 			}// S 
 			
 			if (diseaseType.equals(IMPORT_TYPE_SEIR)) 
 				if(dataInstance.getData(labelE).size() > fileLineCounter) {
 					String eString1 = dataInstance.getData(labelE).get(fileLineCounter);
 					e = new Double(eString1).doubleValue();
 				}// E
 			
 			if(dataInstance.getData(labelI).size() > fileLineCounter) {
 				String iString1 = dataInstance.getData(labelI).get(fileLineCounter);
 				i = new Double(iString1).doubleValue();
 				// compute the changes
 			}//i
 					
 			if (diseaseType.equals(IMPORT_TYPE_SIR) || diseaseType.equals(IMPORT_TYPE_SEIR)) 
 				if(dataInstance.getData(labelR).size() > fileLineCounter) {
 					String rString1 = dataInstance.getData(labelR).get(fileLineCounter);
 					r = new Double(rString1).doubleValue();
 				}// R
 		
 			if (diseaseType.equals(IMPORT_TYPE_SI)) {
 				((SILabelValue)currentState).setS(s);
 				((SILabelValue)currentState).setI(i);
 				return new SILabelValueImpl( 0.0 , 0.0, 0.0, 0.0);
 			} else if (diseaseType.equals(IMPORT_TYPE_SIR)) {
 				((SIRLabelValue)currentState).setS(s);
 				((SIRLabelValue)currentState).setI(i);
 				((SIRLabelValue)currentState).setR(r);
 				return new SIRLabelValueImpl(0.0, 0.0 , 0.0, 0.0, 0.0);
 			} else if (diseaseType.equals(IMPORT_TYPE_SEIR)) {
 				((SEIRLabelValue)currentState).setS(s);
 				((SEIRLabelValue)currentState).setE(e);
 				((SEIRLabelValue)currentState).setI(i);
 				((SEIRLabelValue)currentState).setR(r);
 				return new SEIRLabelValueImpl(0.0, 0.0, 0.0 , 0.0, 0.0, 0.0);
 			} else {
 				throw new UnsupportedOperationException("ExternalDataSource Invalid Type "+diseaseType+" must be SI, SIR, or SEIR");
 			}
 		}else {
 			// if data == null
 			// no data for location in questions
 			if (diseaseType.equals(IMPORT_TYPE_SI)) {
 				return new SILabelValueImpl(0.0 , 0.0, 0.0, 0.0);
 			} else if (diseaseType.equals(IMPORT_TYPE_SIR)) {
 				return new SIRLabelValueImpl(0.0, 0.0 , 0.0, 0.0, 0.0);
 			} else if (diseaseType.equals(IMPORT_TYPE_SEIR)) {
 				return new SEIRLabelValueImpl( 0.0, 0.0, 0.0 , 0.0, 0.0, 0.0);
 			} else {
 				throw new UnsupportedOperationException("ExternalDataSource Invalid Type "+diseaseType+" must be SI, SIR, or SEIR");
 			}
 		}
 	
 		
 	} // importDiseaseData
 	
 	
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#createDiseaseModelState()
 	 */
 	@Override
 	public DiseaseModelState createDiseaseModelState() {
 		return StandardFactory.eINSTANCE.createSIDiseaseModelState();
 	} // createDiseaseModelState
 
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#createDiseaseModelLabel()
 	 */
 	@Override
 	public DiseaseModelLabel createDiseaseModelLabel() {
 		if (diseaseType==IMPORT_TYPE_SI) return StandardFactory.eINSTANCE.createSILabel();
 		if (diseaseType==IMPORT_TYPE_SIR) return StandardFactory.eINSTANCE.createSIRLabel();
 		// else default
 		return StandardFactory.eINSTANCE.createSEIRLabel();
 	} // createDiseaseModelLabel
 	
 	/**
 	 * 
 	 */
 	@Override
 	public DiseaseModelLabelValue createDiseaseModelLabelValue() {
 		return StandardFactory.eINSTANCE.createSEIRLabelValue();
 	} // createDiseaseModelLabelValue
 	
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelState,
 	 *      org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 */
 	@Override
 	public DiseaseModelState initializeDiseaseState(
 			final DiseaseModelState diseaseModelState,
 			final DiseaseModelLabel diseaseModelLabel) {
 
 		final PopulationLabel populationLabel = diseaseModelLabel
 				.getPopulationLabel();
 		final double populationCount = populationLabel
 				.getCurrentPopulationValue().getCount();
 
 		// Accumulate the population count in the disease model
 		addToTotalPopulationCount(populationCount);
 
 		double area = getArea(populationLabel);
 		// If we have a bad data set it could be that the area would be
 		// unspecified or zero.
 		// Do we have a bad area value?
 		if (area <= 0.0) {
 			// Yes
 			reportBadAreaValue(populationLabel, area);
 			area = 1.0;
 		} // if bad area value
 
 		// Accumulate the area in the disease model so we'll know the total when
 		// we do our next pass and compute the area ratio
 		addToTotalArea(area);
 
 		return diseaseModelState;
 	} // initializeDiseaseState
 	
 
 	/**
 	 * Here we compute and set the ratio between the total area and the area
 	 * used for this {@link DiseaseModelLabel}. This value is used to determine
 	 * the <em>transmission scale factor</em>.
 	 * 
 	 * @see #computeTransitions(StandardDiseaseModelLabelValue,
 	 *      StandardDiseaseModelLabel, long)
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 */
 	@Override
 	public void initializeDiseaseState(final DiseaseModelLabel diseaseModelLabel) {
 		final StandardDiseaseModelState sdms = (StandardDiseaseModelState) diseaseModelLabel
 				.getDiseaseModelState();
 		// Is there a population ?
 		if (totalPopulationCount > 0.0) {
 			// Yes
 			double area = getArea(diseaseModelLabel.getPopulationLabel());
 			// Do we have a bad area value?
 			if (area <= 0.0) {
 				// Yes
 				reportBadAreaValue(diseaseModelLabel.getPopulationLabel(), area);
 				area = 1.0;
 			} // if bad area value
 
 			final double ratio = totalArea / area;
 			sdms.setAreaRatio(ratio);
 		}
 	} // initializeDiseaseState
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#createInfector()
 	 */
 	@Override
 	public Infector createInfector() {
 		 throw new UnsupportedOperationException();
 	} // createInfector
 	
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addToTotalArea(double area) {
 		totalArea += area;
 	} // addToTotalArea
 
 	
 	
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addToTotalPopulationCount(double populationCount) {
 		totalPopulationCount += populationCount;
 	} // addToTotalPopulationCount
 
 	/**
 	 * @param populationLabel
 	 *            the population label that labels the node
 	 * @return the area of the node associated with the label
 	 */
 	private double getArea(final PopulationLabel populationLabel) {
 		double retValue = 0.0;
 
 		// The population label could have an area specified for the population
 		// that we should use instead of the area of the region labeled by the
 		// population label. This value would be specified if the population was
 		// densely packed into a small area of the larger region, for instance
 		// like a city in an otherwise large desert.
 
 		retValue = populationLabel.getPopulatedArea();
 
 		// Is there an area specified for the population?
 		if (retValue == 0.0) {
 			// No
 			// Ok, go find the area label and return the area of the region
 			for (final Iterator<NodeLabel> labelIter = populationLabel.getNode()
 					.getLabels().iterator(); labelIter.hasNext();) {
 				final NodeLabel nodeLabel = labelIter.next();
 				// Is this an area label?
 				if (nodeLabel instanceof AreaLabel) {
 					// Yes
 					final AreaLabel areaLabel = (AreaLabel) nodeLabel;
 					retValue = areaLabel.getCurrentAreaValue().getArea();
 					break;
 				}
 			} // for
 		} // If no population area specified
 
 		return retValue;
 	} // getArea
 
 
 		/**
 		 * @param populationLabel
 		 * @param area
 		 */
 		private void reportBadAreaValue(final PopulationLabel populationLabel,
 				double area) {
 			// The bad value could be specified for the node or be an overide
 			// value specified for the population.
 			// Is the bad value from the node?
 			if (populationLabel.getPopulatedArea() == 0.0) {
 				// Yes
 				Activator.logError("The area value of \"" + area
 						+ "\" specified for \""
 						+ populationLabel.getNode().toString()
 						+ "\" is not greater than zero (0.0)", null);
 			} // if bad value for node area
 			else {
 				Activator.logError("The area value of \"" + area
 						+ "\" specified for the population \""
 						+ populationLabel.getName() + "\" for the region \""
 						+ populationLabel.getNode().toString()
 						+ "\" is not greater than zero (0.0)", null);
 			}
 		} // reportBadAreaValue
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ExternalDataSourcePackage.Literals.EXTERNAL_DATA_SOURCE_DISEASE_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDataPath() {
 		return dataPath;
 	}
 
 
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDataPath(String newDataPath) {
 		String oldDataPath = dataPath;
 		dataPath = newDataPath;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DATA_PATH, oldDataPath, dataPath));
 	}
 
 
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDiseaseType() {
 		return diseaseType;
 	}
 
 
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDiseaseType(String newDiseaseType) {
 		String oldDiseaseType = diseaseType;
 		diseaseType = newDiseaseType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DISEASE_TYPE, oldDiseaseType, diseaseType));
 	}
 
 
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DATA_PATH:
 				return getDataPath();
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DISEASE_TYPE:
 				return getDiseaseType();
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
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DATA_PATH:
 				setDataPath((String)newValue);
 				return;
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DISEASE_TYPE:
 				setDiseaseType((String)newValue);
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
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DATA_PATH:
 				setDataPath(DATA_PATH_EDEFAULT);
 				return;
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DISEASE_TYPE:
 				setDiseaseType(DISEASE_TYPE_EDEFAULT);
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
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DATA_PATH:
 				return DATA_PATH_EDEFAULT == null ? dataPath != null : !DATA_PATH_EDEFAULT.equals(dataPath);
 			case ExternalDataSourcePackage.EXTERNAL_DATA_SOURCE_DISEASE_MODEL__DISEASE_TYPE:
 				return DISEASE_TYPE_EDEFAULT == null ? diseaseType != null : !DISEASE_TYPE_EDEFAULT.equals(diseaseType);
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
 		result.append(" (dataPath: ");
 		result.append(dataPath);
 		result.append(", diseaseType: ");
 		result.append(diseaseType);
 		result.append(')');
 		return result.toString();
 	}
 
 
 
 
 	public void applyExternalDeltas(STEMTime time, long timeDelta,
 			EList<DynamicLabel> labels) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 	public void calculateDelta(STEMTime time, long timeDelta,
 			EList<DynamicLabel> labels) {
 		// Iterate through each of the labels we need to update.
 		for (final DynamicLabel dynLabel : labels) {
 			final StandardDiseaseModelLabel diseaseLabel = (StandardDiseaseModelLabel) dynLabel;
 
 			
 			
 			assert diseaseLabel.getPopulationLabel().getPopulationIdentifier()
 					.equals(getPopulationIdentifier());
 
 			// This is the current state of the disease for this label
 			final StandardDiseaseModelLabelValue currentState = (StandardDiseaseModelLabelValue)diseaseLabel
 					.getCurrentValue();
 
 		
 			// 2) Compute the state transitions
 			final StandardDiseaseModelLabelValue nullAdditions = importDiseaseData(currentState, diseaseLabel, time, timeDelta);
 
 			
 			// This will be the state that we compute.
 			final StandardDiseaseModelLabelValue nextState = (StandardDiseaseModelLabelValue)diseaseLabel.getNextValue();
 
 			// Initialize the next state from the current state and then we'll
 			// make the changes to that.
 			nextState.set(currentState);
 			
 			
 			
 			assert nextState.sane():"disease state is insane after subtracting deaths: "+nextState.toString();
 			
 			
 			// The next value is valid now.
 			diseaseLabel.setNextValueValid(true);
 
 			
 		} // for
 		++fileLineCounter;		
 	}
 
 
 
 
 	public void doModelSpecificAdjustments(LabelValue label) {
 		// TODO Auto-generated method stub
 		
 	}
 
 } //ExternalDataSourceDiseaseModelImpl
