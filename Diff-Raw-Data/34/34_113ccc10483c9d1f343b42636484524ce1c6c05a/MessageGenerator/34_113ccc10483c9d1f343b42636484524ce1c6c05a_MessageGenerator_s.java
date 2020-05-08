 package sk.folki.aspectlogger.core;
 
 import static org.apache.log4j.Level.DEBUG;
 import static org.apache.log4j.Level.INFO;
 
 import org.apache.log4j.Level;
 
 class MessageGenerator {
 
 	public Message generateMessage(LoggableMethodLoggerConfig loggableMethodLoggerConfig, LoggableMethodDescription loggableMethod, LoggableMethodInvocation processingResult) {
 		String logMessage = null;
 		String messageLevel = null;
 		Level logLevel = loggableMethodLoggerConfig.getLevel();
 		if (processingResult.isSuccessfull()) {
 			messageLevel = "info";
 			if (logLevel == INFO) {
 				logMessage = generateOkInfoMessage(loggableMethod);
 			} else if (logLevel == DEBUG) {
 				logMessage = generateOkDebugMessage(loggableMethod, processingResult);
 			} 			
 		} else {
 			messageLevel = "error";
 			if (logLevel == INFO) {
 				logMessage = generateErrorInfoMessage(loggableMethod, processingResult);
 			} else if (logLevel == DEBUG) {
 				logMessage = generateErrorDebugMessage(loggableMethod, processingResult);
 			}				
 		}
 		return new Message(messageLevel, logMessage);
 	}
 	
 	private String generateOkInfoMessage(LoggableMethodDescription loggableMethod) {
 		String logMessage = assembleOkInfoLogMessage(loggableMethod);
 		return logMessage;
 	}
 	
 	private String assembleOkInfoLogMessage(LoggableMethodDescription loggableMethod) {
 		String infoMessage = getLoggableOperationMessage(loggableMethod);
 		return infoMessage;
 	}
 	
 	private String getLoggableOperationMessage(LoggableMethodDescription loggableMethod) {
 		return loggableMethod.getInfoMessage();
 	}
 	
 	private String generateOkDebugMessage(LoggableMethodDescription loggableMethod, LoggableMethodInvocation processingResult) {
 		Parameters loggableOperationParameters = loggableMethod.getParameters();
 		String logMessage = assembleOkDebugLogMessage(loggableOperationParameters, loggableMethod, processingResult);
 		return logMessage;
 	}
 
 	private String assembleOkDebugLogMessage(Parameters loggableOperationParameters, LoggableMethodDescription loggableMethod, LoggableMethodInvocation processingResult) {
 		// Service operation WITH PARAMS 0=Parameter type instance RETURNED Return type instance
 		String loggableOperationMessage = getLoggableOperationMessage(loggableMethod);
 		String loggableOperationParametersStringChain = convertParametersToStringChain(loggableOperationParameters);
 		String loggableOperationReturnedValue = getValueReturnedFromProcessedLoggableMethod(processingResult);
 		String errorDebugLogMessage = 
 			loggableOperationMessage + " WITH PARAMS " + loggableOperationParametersStringChain + " RETURNED " + loggableOperationReturnedValue;
 		return errorDebugLogMessage;
 	}
 	
 	private String convertParametersToStringChain(Parameters loggableOperationParameters) {
 		String parametersStringChain = "";
 		for (String parameterName: loggableOperationParameters.getNames()) {
 			Object parameterValue = loggableOperationParameters.getValue(parameterName);
 			parametersStringChain += parameterName + "=" + parameterValue + ", ";
 		}
		parametersStringChain = parametersStringChain.substring(0, parametersStringChain.length() - 2);
 		return parametersStringChain;
 	}
 	
 	private String getValueReturnedFromProcessedLoggableMethod(LoggableMethodInvocation processingResult) {
		return processingResult.getReturnedObject().toString();
 	}
 	
 	private String generateErrorInfoMessage(LoggableMethodDescription loggableMethod, LoggableMethodInvocation processingResult) {
 		String logMessage = assembleErrorInfoLogMessage(loggableMethod);
 		return logMessage;
 	}
 
 	private String assembleErrorInfoLogMessage(LoggableMethodDescription loggableMethod) {
 		String infoMessage = loggableMethod.getInfoMessage();
 		String errorMessage = "Error: " + infoMessage;
 		return errorMessage;
 	}
 	
 	private String generateErrorDebugMessage(LoggableMethodDescription loggableMethod, LoggableMethodInvocation processingResult) {
 		Parameters loggableOperationParameters = loggableMethod.getParameters();
 		String logMessage = assembleErrorDebugLogMessage(loggableOperationParameters, loggableMethod, processingResult);
 		return logMessage;
 	}
 
 	private String assembleErrorDebugLogMessage(Parameters loggableOperationParameters, LoggableMethodDescription loggableMethod, LoggableMethodInvocation processingResult) {
 		// Service operation WITH PARAMS parameter=Parameter type instance THROWS Error message.
 		String loggableOperationMessage = getLoggableOperationMessage(loggableMethod);
 		String loggableOperationParametersStringChain = convertParametersToStringChain(loggableOperationParameters);
 		String loggableOperationOccuredError = getErrorOccuredDurringProcessingLoggableMethod(processingResult);
 		String errorDebugLogMessage = 
 			loggableOperationMessage + " WITH PARAMS " + loggableOperationParametersStringChain + " THROWN " + loggableOperationOccuredError ;
 		return errorDebugLogMessage;
 	}
 	
 	private String getErrorOccuredDurringProcessingLoggableMethod(LoggableMethodInvocation processingResult) {
 		return processingResult.getOccuredError().getMessage();
 	}
 }
