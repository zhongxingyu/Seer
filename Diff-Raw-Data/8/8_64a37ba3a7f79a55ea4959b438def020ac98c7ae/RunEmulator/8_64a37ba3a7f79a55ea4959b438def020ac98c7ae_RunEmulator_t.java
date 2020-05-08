 package org.uncertweb.ps.processes;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.uncertweb.et.design.Design;
 import org.uncertweb.et.emulator.Emulator;
 import org.uncertweb.et.emulator.EmulatorEvaluationResult;
 import org.uncertweb.et.emulator.EmulatorEvaluator;
 import org.uncertweb.et.emulator.EmulatorEvaluatorException;
 import org.uncertweb.et.parameter.ParameterDescription;
 import org.uncertweb.et.parameter.VariableInput;
 import org.uncertweb.ps.data.DataDescription;
 import org.uncertweb.ps.data.Input;
 import org.uncertweb.ps.data.Metadata;
 import org.uncertweb.ps.data.ProcessInputs;
 import org.uncertweb.ps.data.ProcessOutputs;
 import org.uncertweb.ps.data.SingleOutput;
 import org.uncertweb.ps.process.AbstractProcess;
 import org.uncertweb.ps.process.ProcessException;
 
 /**
  * RunEmulator is a special process. It'll never be exposed by the name RunEmulator.
  * 
  * @author Richard Jones
  *
  */
 public class RunEmulator extends AbstractProcess {
 
 	private final Logger logger = Logger.getLogger(RunEmulator.class);
 
 	private Map<String, DataDescription> inputMap;
 	private Map<String, DataDescription> outputMap;
 
 	private String identifier;
 	private Emulator emulator;
 
 	public RunEmulator(String identifier, Emulator emulator) {
 		// setup maps, treemap for ordering
 		inputMap = new TreeMap<String, DataDescription>();
 		outputMap = new TreeMap<String, DataDescription>();
 
 		// set identifier and emulator
 		this.identifier = identifier;
 		this.emulator = emulator;
 
 		// inputs
 		for (String inputIdentifier : emulator.getDesign().getInputIdentifiers()) {
 			inputMap.put(inputIdentifier, new DataDescription(Double[].class));
 		}
 
 		// outputs
 		for (String outputIdentifier : emulator.getEvaluationResult().getOutputIdentifiers()) {
 			outputMap.put(outputIdentifier + "Mean", new DataDescription(Double[].class));
 			outputMap.put(outputIdentifier + "Covariance", new DataDescription(Double[].class));
 		}
 	}
 
 	@Override
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	@Override
 	public DataDescription getInputDataDescription(String arg0) {
 		return inputMap.get(arg0);
 	}
 
 	@Override
 	public List<String> getInputIdentifiers() {
 		return new ArrayList<String>(inputMap.keySet());
 	}
 
 	@Override
 	public List<Metadata> getInputMetadata(String arg0) {
 		List<Metadata> metadata = new ArrayList<Metadata>();
 		for (org.uncertweb.et.parameter.Input input : emulator.getInputs()) {
 			if (input.getIdentifier().equals(arg0)) {
 				// min max
				if (input.isVariableInput()) {
					VariableInput asVariable = input.getAsVariableInput();
					metadata.add(new Metadata("minimum-value", String.valueOf(asVariable.getMin())));
					metadata.add(new Metadata("maximum-value", String.valueOf(asVariable.getMax())));
				}
 				
 				// any additional?
 				ParameterDescription description = input.getDescription();
 				if (description != null) {
 					metadata.add(new Metadata("uom", description.getUom()));
 					metadata.add(new Metadata("description", description.getDetail()));
 				}
 			}
 		}
 		return metadata;
 	}
 
 	@Override
 	public DataDescription getOutputDataDescription(String arg0) {
 		return outputMap.get(arg0);
 	}
 
 	@Override
 	public List<String> getOutputIdentifiers() {
 		return new ArrayList<String>(outputMap.keySet());
 	}
 
 	@Override
 	public List<Metadata> getOutputMetadata(String arg0) {
 		List<Metadata> metadata = new ArrayList<Metadata>();
 		for (org.uncertweb.et.parameter.Output output : emulator.getOutputs()) {
 			if (output.getIdentifier().equals(arg0)) {
 				ParameterDescription description = output.getDescription();
 				if (description != null) {
 					metadata.add(new Metadata("uom", description.getUom()));
 					metadata.add(new Metadata("description", description.getDetail()));
 				}
 			}
 		}
 		return metadata;
 	}
 
 	@Override
 	public ProcessOutputs run(ProcessInputs inputs) throws ProcessException {
 		// get inputs values
 		Design design = null;
 		for (Input input : inputs) {
 			Double[] points = input.getAsSingleInput().getObjectAs(Double[].class);
 			if (design == null) {
 				design = new Design(points.length);
 			}
 			design.addPoints(input.getIdentifier(), points);
 		}
 		
 		// run emulator
 		logger.info("Running emulator for " + this.getIdentifier() + "...");
 		try {
 			// run emulator request
 			EmulatorEvaluationResult result = EmulatorEvaluator.run(emulator, design);
 			
 			// create output
 			ProcessOutputs outputs = new ProcessOutputs();
 			
 			// only supports 1d output at the moment
 			String outputIdentifier = emulator.getOutputs().get(0).getIdentifier();
 			Double[] predictedMean = result.getMeanResults(outputIdentifier);
 			Double[] predictedCov = result.getCovarianceResults(outputIdentifier);
 			outputs.add(new SingleOutput(outputIdentifier + "Mean", predictedMean));
 			outputs.add(new SingleOutput(outputIdentifier + "Covariance", predictedCov));
 			
 			return outputs;
 		}
 		catch (EmulatorEvaluatorException e) {
 			throw new ProcessException("Couldn't run emulator. " + e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public List<Metadata> getMetadata() {
 		return null;
 	}
 
 }
