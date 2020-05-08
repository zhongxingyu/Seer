 package org.eclipse.stem.analysis.automaticexperiment;
 
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
 
 /**
  * Implementation was taken from: http://www.ee.ucl.ac.uk/~mflanaga/java/Minimisation.html#simplex
  */
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.stem.analysis.ErrorFunction;
 import org.eclipse.stem.analysis.ErrorResult;
 import org.eclipse.stem.analysis.ScenarioInitializationException;
 import org.eclipse.stem.analysis.automaticexperiment.views.AutoExpControl;
 import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl;
 import org.eclipse.stem.analysis.util.CSVscenarioLoader;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.IntegrationDecorator;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModel;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.jobs.simulation.Simulation;
 import org.eclipse.stem.util.loggers.views.CustomCSVLogger;
 
 import automaticexperiment.AutomaticExperiment;
 import automaticexperiment.ModifiableParameter;
 
 public class SimplexAlgorithmExecuter 
 	extends AbstractErrorAnalysisAlgorithm {
 	
 	private ISimulation simulation = null;
 	private CustomCSVLogger csvLogger = null;
 	private final static String SIMULATION_OUTPUT_DIR = Platform.getLocation() + File.separator + "AutoExpTempDir";
 	private final static String LOG_FILE_NAME = SIMULATION_OUTPUT_DIR+File.separator+"resultLog.csv";
 	static String LS = System.getProperty("line.separator"); 
 	private final CustomSimulationManager simMgr = CustomSimulationManager.getManager();
 	private ReferenceScenarioDataMapImpl ref;
 	private static AutoExpControl aeControl = null;
 	
 	
 	/**
 	 * @see org.eclipse.stem.analysis.automaticexperiment.AbstractErrorAnalysisAlgorithm#execute()
 	 */
 	@Override
 	public void execute() {
 		double prevmin = Double.MAX_VALUE;
 		double [] prevvals = new double[initialParamsValues.length];
 		double [] minvals;
 		String [] parameterNames=null;
 		if (parameters != null) {
 			parameterNames = new String[parameters.size()];
 			int i=0;
 			for (final ModifiableParameter param:parameters) {
 				parameterNames[i++] = param.getFeatureName();
 			}
 		}
 		
 		for(;;) {
 			for(int i=0;i<initialParamsValues.length;++i) prevvals[i] = initialParamsValues[i];
 			long before = System.currentTimeMillis();
 			simplexAlgorithm.execute(simplexFnToMinimize,  initialParamsValues, paramsInitialSteps, tolerance, this.maxNumOfIterations);
 			long after = System.currentTimeMillis();
 			if(AutomaticExperimentManager.QUIT_NOW)
 				AutomaticExperimentManager.stopLatch.countDown();
 			if(!this.repeat || AutomaticExperimentManager.QUIT_NOW) {
 				System.out.println("\n\nTime to execute the Nedler-Mead Algorithm: " + (after-before)/1000 + " seconds");
 				System.out.println("Minimum value: " + simplexAlgorithm.getMinimumFunctionValue());
				System.out.println("Validation error: " + simplexAlgorithm.getMinimumErrorResult().getValidationError());
 				System.out.println("Parameters values: " + Arrays.toString(simplexAlgorithm.getMinimumParametersValues()));
 				minvals = simplexAlgorithm.getMinimumParametersValues();
 				break;
 			}
 			double newmin = simplexAlgorithm.getMinimumFunctionValue();
 			if(newmin >= prevmin) {
 				System.out.println("\n\nTime to execute the Nedler-Mead Algorithm: " + (after-before)/1000 + " seconds");
 				System.out.println("Minimum value: " + prevmin);
				System.out.println("Validation error: " + simplexAlgorithm.getMinimumErrorResult().getValidationError());
 				System.out.println("Parameters values: " + Arrays.toString(prevvals));
 				minvals = prevvals;
 				break; // we couldn't improve
 			}
 			// Not same, reinit with new minimum
 			prevmin = simplexAlgorithm.getMinimumFunctionValue();
 			
 			for(int i=0;i<initialParamsValues.length;++i){
 				initialParamsValues[i] = simplexAlgorithm.getMinimumParametersValues()[i];
 				prevvals[i] =  simplexAlgorithm.getMinimumParametersValues()[i];
 			}
 			ErrorAnalysisAlgorithmEvent newEvent = new ErrorAnalysisAlgorithmEvent(simplexAlgorithm.getMinimumErrorResult(), ALGORITHM_STATUS.RESTARTED_ALGORITHM);
 			newEvent.parameterNames = parameterNames;
 			newEvent.parameterValues = simplexAlgorithm.getMinimumParametersValues();
 			this.fireEvent(newEvent);
 
 		}
 		ErrorAnalysisAlgorithmEvent newEvent = new ErrorAnalysisAlgorithmEvent(simplexAlgorithm.getMinimumErrorResult(), ALGORITHM_STATUS.FINISHED_ALGORITHM);
 		newEvent.parameterNames = parameterNames;
 		newEvent.parameterValues = minvals;
 		this.fireEvent(newEvent);
 	}
 	
 	public void init(AutomaticExperiment automaticExperiment, ErrorAnalysisAlgorithm alg) {
 		super.init(automaticExperiment);
 		simplexFnToMinimize = new NedlearMeadSimplexFunction(parameters, baseScenario, errorFunction, alg);
 		try {
 			CSVscenarioLoader loader1 = new CSVscenarioLoader(referenceDataDirectory);
 			ref = loader1.parseAllFiles(2);
 		} catch (ScenarioInitializationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void setParameters(List<ModifiableParameter> parameters) {
 		super.setParameters(parameters);
 		
 		//Set lower and upper bounds constraints for the parameters
 		int paramIndex=0;
 		for (final ModifiableParameter param:parameters) {
 			simplexAlgorithm.setParameterLimits(paramIndex, param.getLowerBound(), param.getUpperBound());
 			paramIndex++;
 		}
 	}
 	
 	/**
 	 *
 	 */
 	public class NedlearMeadSimplexFunction
 		implements SimplexFunction 
 	{	
 	    private ErrorFunction errorFunction = null;
 	    private String[] parameterNames = null;
 	    private FileWriter resultWriter;
 	    private URI[] decoratorURIs = null;
 	    private ErrorAnalysisAlgorithm algorithm = null;
 	    
 		public NedlearMeadSimplexFunction(
 				final List<ModifiableParameter> parameters,
 				final Scenario pBaseScenario, 
 				final ErrorFunction errorFunction, 
 				final ErrorAnalysisAlgorithm algorithm) {
 			
 			try {
 				resultWriter = new FileWriter(LOG_FILE_NAME);
 				baseScenario = pBaseScenario;
 				this.errorFunction = errorFunction;
 				this.algorithm = algorithm;
 				
 				if (parameters != null) {
 					parameterNames = new String[parameters.size()];
 					decoratorURIs = new URI[parameters.size()];
 					int i=0;
 					for (final ModifiableParameter param:parameters) {
 						decoratorURIs[i]=param.getTargetURI();
 						parameterNames[i++] = param.getFeatureName();
 						resultWriter.write(parameterNames[i-1]);
 						resultWriter.write(",");
 					}
 					resultWriter.write("error");
 					resultWriter.write(LS);
 				}
 			} catch(IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 		
 		public ErrorResult getValue(double[] parameters) {
 			
 			
 			simulation = createSimulation(baseScenario);
 			simulation.setSequenceNumber(simulation.getSequenceNumber()+1);
 			
 			// Descend into the Scenario looking for something with a double field
 			final EList<Decorator> decs = baseScenario.getCanonicalGraph().getDecorators();
 	
 			Decorator defaultDecorator = null;
 			for (Decorator decorator : decs) {
 				if (decorator instanceof DiseaseModel) {
 					defaultDecorator = decorator; 
 					break;
 				}
 			}
 			
 			for(int i=0;i<parameterNames.length;++i) {
 				Decorator decorator = defaultDecorator;
 				if(decoratorURIs[i] != null) 
 					for(Decorator scenarioDec:decs) 
 						if(decoratorURIs[i].toString().equals(scenarioDec.getURI().toString())) {
 							decorator = scenarioDec;
 							break;
 						}
 				for (EAttribute attribute : decorator.eClass().getEAllAttributes()) 
 					if(attribute.getName().equals(parameterNames[i])) 
 						decorator.eSet(attribute, new Double(parameters[i]));
 			}
 			
 			csvLogger = new CustomCSVLogger(SIMULATION_OUTPUT_DIR, simulation, (IntegrationDecorator) defaultDecorator);
 
 			// This will reinit the infectors/inoc etc to the new values 
 			baseScenario.reset();
 
 
 			//Run the simulation with the new parameters and return the error value
 			System.out.println("Running the simulation with the following parameters: ");
 			System.out.println("\tParameters Names: " + Arrays.toString(parameterNames));
 			System.out.println("\tParameters Values: " + Arrays.toString(parameters));
 			double error = 0.0;
 			ErrorResult result = null;
 			try {
 				for(double val:parameters) resultWriter.write(val+",");
 				ErrorAnalysisAlgorithmEvent newEvent = new ErrorAnalysisAlgorithmEvent(null, ALGORITHM_STATUS.STARTING_SIMULATION);
 				newEvent.parameterNames = parameterNames;
 				newEvent.parameterValues = parameters;
 				((AbstractErrorAnalysisAlgorithm)algorithm).fireEvent(
 						newEvent);
 				runSimulation(simulation);
 			
 				result =  getErrorValue(simulation.getUniqueIDString());
 			
 				newEvent = new ErrorAnalysisAlgorithmEvent(result, ALGORITHM_STATUS.FINISHED_SIMULATION);
 				newEvent.parameterNames = parameterNames;
 				newEvent.parameterValues = parameters;
 				
 				((AbstractErrorAnalysisAlgorithm)algorithm).fireEvent(
 						newEvent);
 				
 				error = result.getError();
 				
 				System.out.println(" Error is: " + error);
				System.out.println(" Error in validation data set is: " + result.getValidationError());
 				resultWriter.write(error+"");
 				resultWriter.write(LS);
 				resultWriter.flush();
 				
 				// TODO: GUI output here
 				
 				
 		 		
 			} catch(IOException ioe) {
 				ioe.printStackTrace();
 			}
 			cleanup();
 			
 			return result;
 		}
 		
 		private ErrorResult getErrorValue(String simulationUniqueId) {
 			ErrorResult result = null;
 			try {
 				final EList<Decorator> decs = baseScenario.getCanonicalGraph().getDecorators();
 				
 				DiseaseModel defaultDecorator = null;
 				for (Decorator decorator : decs) {
 					if (decorator instanceof DiseaseModel) {
 						defaultDecorator = (DiseaseModel)decorator; 
 						break;
 					}
 				}
 				
 				CSVscenarioLoader loader2 = new CSVscenarioLoader(SIMULATION_OUTPUT_DIR + File.separator + simulationUniqueId +"/"+defaultDecorator.getDiseaseName()+"/"+defaultDecorator.getPopulationIdentifier());
 				ReferenceScenarioDataMapImpl data = loader2.parseAllFiles(2);
 	
 				result = errorFunction.calculateError(ref, data);
 			} catch (ScenarioInitializationException e) {
 				e.printStackTrace();
 			}
 			return result;
 		}
 		
 		private void runSimulation(final ISimulation simulationToRun) {
 			long before = System.currentTimeMillis();
 			simulationToRun.run();
 			try {
 				simulationToRun.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			simulationToRun.stop();			
 			
 			long after = System.currentTimeMillis();
 			System.out.println("It took " + (after-before)/1000 + " seconds to run the simulation");
 		}
 		
 		private void cleanup() {
 			if (csvLogger != null) {
 				csvLogger.close();
 				csvLogger = null;
 			}
 			simMgr.removeActiveSimulation(simulation);
 			((Simulation)simulation).destroy();
 			simulation = null;
 			CustomSimulationManager.resetSimulationManager();
 		}
 		
 		private ISimulation createSimulation(Scenario scenario) {
 			Simulation simulation = (Simulation)simMgr.createSimulation(scenario, null);
 			simulation.simulationSleep = false;
 			return simulation;
 		}
 	}
 }
 
 
