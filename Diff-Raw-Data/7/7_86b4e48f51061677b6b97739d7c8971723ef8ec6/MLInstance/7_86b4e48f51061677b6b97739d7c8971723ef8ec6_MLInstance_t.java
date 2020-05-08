 package org.uncertweb.matlab.instance;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import matlabcontrol.MatlabConnectionException;
 import matlabcontrol.MatlabInvocationException;
 import matlabcontrol.MatlabProxy;
 import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
 import matlabcontrol.extensions.MatlabNumericArray;
 import matlabcontrol.extensions.MatlabTypeConverter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.uncertweb.matlab.MLConnectorException;
 import org.uncertweb.matlab.MLRequest;
 import org.uncertweb.matlab.MLResult;
 import org.uncertweb.matlab.value.MLArray;
 import org.uncertweb.matlab.value.MLCell;
 import org.uncertweb.matlab.value.MLMatrix;
 import org.uncertweb.matlab.value.MLScalar;
 import org.uncertweb.matlab.value.MLString;
 import org.uncertweb.matlab.value.MLStruct;
 import org.uncertweb.matlab.value.MLValue;
 
 public class MLInstance {
 
 	private final Logger logger = LoggerFactory.getLogger(MLInstance.class);
 
 	private MatlabProxy proxy;
 	private MatlabTypeConverter processor;		
 	private List<MLInstanceListener> listeners;
 	
 	private String baseDir;
 	
 	public MLInstance() throws MLConnectorException {
 		this(null);
 	}
 
 	public MLInstance(String baseDir) throws MLConnectorException {
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
                                                .setHidden(true)
                                                .build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
 		try {
 			proxy = factory.getProxy();
 			processor = new MatlabTypeConverter(proxy);
 			listeners = new ArrayList<MLInstanceListener>();
 		}
 		catch (MatlabConnectionException e) {
 			throw new MLConnectorException("Unable to connect to MATLAB.", e);
 		}
 		
 		this.baseDir = baseDir;
 	}
 
 	public void addListener(MLInstanceListener listener) {
 		listeners.add(listener);
 	}
 	
 	public void destroy() throws MLConnectorException {
 		try {
 			proxy.exit();
 		}
 		catch (MatlabInvocationException e) {
 			throw new MLConnectorException("Couldn't exit MATLAB.", e);
 		}
 	}
 
 	private void preHandle() throws MLConnectorException {
 		try {
 			if (baseDir != null) {
 				proxy.eval("cd('" + baseDir.replace("\\", "\\\\") + "')"); // matlab needs escaped slashes too
 				proxy.eval("addpath('.')");
 			}
 		}
 		catch (MatlabInvocationException e) {
 			throw new MLConnectorException("Unable to perform pre-request setup.", e);
 		}
 	}
 	
 	private void postHandle() throws MLConnectorException {
 		try {
 			proxy.eval("clear all");
 		}
 		catch (MatlabInvocationException e) {
 			throw new MLConnectorException("Unable to perform post-request clean.", e);
 		}
 	}
 
 	public MLResult handle(MLRequest request) throws MLConnectorException {
 		// anything we need to do before handling
 		preHandle();
 
 		// eval request
 		try {
 			logger.info("Evaluating function " + request.getFunction() + "...");
 			proxy.eval(request.toEvalString());
 
 			// get results
 			logger.info("Evaluation complete, parsing results...");
 			MLResult result = new MLResult();
 			for (int i = 0; i < request.getResultCount(); i++) {
 				String varName = "result" + (i + 1);
 
 				// parse
 				MLValue value = parseValue(varName);
 				result.addResult(value);
 			}
 
 			// notify			
 			return result;
 		}
 		catch (MatlabInvocationException e) {
 			throw new MLConnectorException("Unable to evaluate request.", e);
 		}
 		finally {
 			notifyListeners();
 			try {
 				postHandle();
 			}
 			catch (MLConnectorException e) {
 				// this isn't too important
 			}
 		}
 	}
 
 	private MLValue parseValue(String varName) throws MLConnectorException {
 		MLValue value;
 		try {
 			String type = (String)proxy.returningEval("class(" + varName + ")", 1)[0];
 			if (type.equals("double")) {
 				MatlabNumericArray array = processor.getNumericArray(varName);
 				int[] lengths = array.getLengths();
 				double[][] realArray = array.getRealArray2D();
 				if (lengths[0] == 1 && lengths[1] == 1) {
 					// scalar
 					value = new MLScalar(realArray[0][0]);
 				}
 				else if (lengths[0] == 1) {
 					// array
 					value = new MLArray(realArray[0]);
 				}
 				else {
 					// matrix
 					value = new MLMatrix(realArray);
 				}
 			}
 			else if (type.equals("char")) {
 				String string = (String)proxy.getVariable(varName);
 				value = new MLString(string);
 			}
 			else if (type.equals("cell")) {
 				// cell looks like ["key", ["another", 0.1]]
 				Object[] obj = (Object[])proxy.getVariable(varName);
 				MLValue[] cell = new MLValue[obj.length];
 				for (int i = 0; i < cell.length; i++) {
 					String subvarName = varName + "s";
 					proxy.eval(subvarName + "=" + varName + "{" + (i + 1) + "}");
 					cell[i] = parseValue(subvarName);
 				}
 				value = new MLCell(cell);
 			}
 			else if (type.equals("struct")) {
 				MLStruct struct = new MLStruct();
 				String[] names = (String[])(Object[])proxy.returningEval("fieldnames(" + varName + ")", 1)[0];
 				for (String name : names) {
 					String subvarName = varName + "s";
 					proxy.eval(subvarName + "=" + varName + "." + name);
 					struct.setField(name, parseValue(subvarName));
 				}
 				value = struct;
 			}
 			else {
 				throw new MLConnectorException("Unable to parse value of type " + type + ", unsupported.");
 			}
 		}
 		catch (MatlabInvocationException e) {
 			throw new MLConnectorException("Unable to parse value.", e);
 		}
 		return value;
 	}
 
 	private void notifyListeners() {
 		for (MLInstanceListener listener : listeners) {
 			listener.instanceFree();
 		}
 	}
 
 }
