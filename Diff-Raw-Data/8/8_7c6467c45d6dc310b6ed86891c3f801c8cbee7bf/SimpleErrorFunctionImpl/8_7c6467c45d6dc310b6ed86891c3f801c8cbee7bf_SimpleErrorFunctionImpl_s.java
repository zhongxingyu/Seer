 package org.eclipse.stem.analysis.impl;
 
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
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.stem.analysis.AnalysisFactory;
 import org.eclipse.stem.analysis.AnalysisPackage;
 import org.eclipse.stem.analysis.ErrorResult;
 import org.eclipse.stem.analysis.ReferenceScenarioDataMap;
 import org.eclipse.stem.analysis.SimpleErrorFunction;
 import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl.ReferenceScenarioDataInstance;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Simple Error Function</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * </p>
  *
  * @generated
  */
 public class SimpleErrorFunctionImpl extends ErrorFunctionImpl implements SimpleErrorFunction {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected SimpleErrorFunctionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return AnalysisPackage.Literals.SIMPLE_ERROR_FUNCTION;
 	}
 
 	/**
 	 * input
 	 */
 	Map<String,List<Double>> commonInfectiousLocationsA = new HashMap<String,List<Double>>();
 	Map<String,List<Double>> commonInfectiousLocationsB = new HashMap<String,List<Double>>();
 	Map<String,List<Double>> commonPopulationLocationsA = new HashMap<String,List<Double>>();
 	Map<String,List<Double>> commonPopulationLocationsB = new HashMap<String,List<Double>>();
 
 	Map<String,Double> commonAvgPopulationLocationsA = new HashMap<String,Double>();
 	Map<String,Double> commonAvgPopulationLocationsB = new HashMap<String,Double>();
 	Map<String, Double> commonMaxLocationsA = new HashMap<String, Double>();
 	
 	/**
 	 * number common locations with nonzero Inf count at time t
 	 */
 	public double[] locationCount;
 	/**
 	 * The Result
 	 */
 	public double[] meanSqDiff;
 	/*
 	 * time
 	 */
 	public double[] time;
 	
 	protected AnalysisFactory aFactory = new AnalysisFactoryImpl();
 
 	// Set to true to weight the average by population size
 	private static boolean WEIGHTED_AVERAGE = true;
 	private static boolean FIT_INCIDENCE = true;
 	private static boolean USE_THRESHOLD = true;
 	private static double THRESHOLD = 0.1;
 	
 	/**
 	 * calculate delta for a simple error function
 	 * 
 	 * 
 	 * @override
 	 */
 	
 	@Override
 	public ErrorResult calculateError(ReferenceScenarioDataMap reference, ReferenceScenarioDataMap data) {
 		final ReferenceScenarioDataMapImpl _ref = (ReferenceScenarioDataMapImpl)reference;
 		final ReferenceScenarioDataMapImpl _data = (ReferenceScenarioDataMapImpl)data;
 
 		// clear
 		time = null;
 		
 		Iterator<String> iteratorA = _ref.getLocations().iterator();
 		int maxTime = -1;
 		while(iteratorA.hasNext()) {
 			String id = iteratorA.next();
 					
 			if(_data.containsLocation(id)) {
 				// get the lists of data only for those locations that are common to both maps						ReferenceScenarioDataInstance dataMapA = mapA.getLocation(id);
 				ReferenceScenarioDataInstance dataMapA = _ref.getLocation(id);
 				List<Double> dataAI = null;
 				if(FIT_INCIDENCE) dataAI = getIncidence(dataMapA); 
 				else dataAI = getInfectious(dataMapA);
 				List<Double> dataAP = getPopulation(dataMapA);
 				commonInfectiousLocationsA.put(id,dataAI);
 				commonPopulationLocationsA.put(id, dataAP);							
 							
 				// Map B
 				ReferenceScenarioDataInstance dataMapB = _data.getLocation(id);
 				List<Double> dataBI = null;
 				if(FIT_INCIDENCE) dataBI = getIncidence(dataMapB); 
 				else dataBI = getInfectious(dataMapB);
 				List<Double> dataBP = getPopulation(dataMapB);
 				commonInfectiousLocationsB.put(id,dataBI);
 				commonPopulationLocationsB.put(id, dataBP);
 				
 				// init the array size
 				if (maxTime == -1) maxTime = dataAI.size();
 						
 				// dimension the arrays to the length of the SMALLEST array for which we have data
 				if(maxTime >= dataBI.size() ) maxTime = dataBI.size();
 				if(maxTime >= dataAI.size() ) maxTime = dataAI.size();
 			}// if
 		}// while
 		if(maxTime<=0) maxTime = 0;
 		if(time==null) {
 			time = new double[maxTime];
 			meanSqDiff = new double[maxTime];
 			locationCount = new double[maxTime];
 			for(int i = 0; i < maxTime; i ++) {
 				time[i] = i;
 				meanSqDiff[i] = 0.0;
 				locationCount[i] = 0.0;
 			}
 		}
 		
 		// Now figure out the actual error
 		
 		double [] Xref = new double[time.length];
 		double [] Xdata = new double[time.length];
 		double [] nrmse_loc = new double[commonInfectiousLocationsA.keySet().size()];  
 		
 		double finalerror = 0.0;
 		BasicEList<Double> list = new BasicEList<Double>();
 		for(int i=0;i<time.length;++i)list.add(0.0);
 		
 		// Get the average population for each location
 		for(String loc:commonPopulationLocationsA.keySet()) {
 			List<Double>ld = commonPopulationLocationsA.get(loc);
 			double sum = 0;for(double d:ld)sum+=d;
 			sum /= (double)ld.size();
 			commonAvgPopulationLocationsA.put(loc, sum);
 		}		
 
 		// Get the average population for each location
 		for(String loc:commonPopulationLocationsB.keySet()) {
 			List<Double>ld = commonPopulationLocationsB.get(loc);
 			double sum = 0;for(double d:ld)sum+=d;
 			sum /= (double)ld.size();
 			commonAvgPopulationLocationsB.put(loc, sum);
 		}		
 
 		// Get the maximum value for the A series (reference)
 		for(String loc:commonPopulationLocationsA.keySet()) {
			List<Double>ld = commonPopulationLocationsA.get(loc);
 			double max = Double.MIN_VALUE;
 			for(double d:ld)if(d >max)max=d;
 			commonMaxLocationsA.put(loc, max);
 		}
 		
 		// Calculate the normalized root mean square error for each location, then
 		// divide by the number of locatins
 		
 		double weighted_denom = 0.0;
 		
 		int loc_iter = 0;
 		for(String loc:commonInfectiousLocationsA.keySet()) {
 			double maxRef = 0.0;
 			double minRef = Double.MAX_VALUE;
 			// Get the numbers at each time step for the location
 			for(int icount =0; icount < time.length; icount ++) {
 				List<Double> dataAI = commonInfectiousLocationsA.get(loc);
 				List<Double> dataBI = commonInfectiousLocationsB.get(loc);
 												
 				double iA = dataAI.get(icount).doubleValue();
 				double iB = dataBI.get(icount).doubleValue();
 			
 				Xref[icount]=iA;
 				Xdata[icount]=iB;
 			}
 		
 			double nominator = 0.0; 
 			double timesteps = 0;
 			for(int icount =0; icount < time.length; icount ++) {
				if(USE_THRESHOLD && Xdata[icount]<THRESHOLD*commonMaxLocationsA.get(loc)) continue;
				nominator = nominator + Math.pow(Xref[icount]-Xdata[icount], 2);
 				if(Xref[icount]>maxRef)maxRef = Xref[icount];
 				if(Xref[icount]<minRef)minRef = Xref[icount];
 				list.set(icount, list.get(icount)+Math.abs(Xref[icount]-Xdata[icount]));
 				++timesteps;
 			}
 			System.out.println("timesteps:"+timesteps);
 			double error = Double.MAX_VALUE;
 		    if(timesteps > 0) error = Math.sqrt(nominator/timesteps);
 			error = error / (maxRef-minRef);
 			if(WEIGHTED_AVERAGE) finalerror += commonAvgPopulationLocationsA.get(loc) * error;
 			else finalerror += error;
 			weighted_denom += commonAvgPopulationLocationsA.get(loc);
 			nrmse_loc[loc_iter++] = error;
 		
 		}
 		
 		// Divide the error by the number of locations
 		if(WEIGHTED_AVERAGE) finalerror /= weighted_denom; 
 		else finalerror /= (double)nrmse_loc.length;
 		
 		ErrorResult resultobj = aFactory.createErrorResult();
 		resultobj.setErrorByTimeStep(list);
 		resultobj.setError(finalerror);
 		
 		return resultobj;	
 	}
 } //SimpleErrorFunctionImpl
