 package org.fiteagle.delivery.xmlrpc.util;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.fiteagle.interactors.sfa.ISFA;
 import org.fiteagle.interactors.sfa.SFAInteractor_v3;
 import org.fiteagle.interactors.sfa.common.AMCode;
 import org.fiteagle.interactors.sfa.common.AMResult;
 import org.fiteagle.interactors.sfa.common.Credentials;
 import org.fiteagle.interactors.sfa.common.GENI_CodeEnum;
 import org.fiteagle.interactors.sfa.common.GeniAvailableOption;
 import org.fiteagle.interactors.sfa.common.Geni_RSpec_Version;
 import org.fiteagle.interactors.sfa.common.ListCredentials;
 import org.fiteagle.interactors.sfa.listresources.GeniCompressedOption;
 import org.fiteagle.interactors.sfa.listresources.ListResourceOptions;
 import org.slf4j.Logger;
 
 import redstone.xmlrpc.XmlRpcArray;
 import redstone.xmlrpc.XmlRpcInvocationHandler;
 import redstone.xmlrpc.XmlRpcStruct;
 
 import com.fasterxml.jackson.annotation.JsonInclude.Include;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class SFAHandler implements XmlRpcInvocationHandler {
 
 	ISFA interactor;
 
 	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
 	
 	public SFAHandler(SFAInteractor_v3 sfaInteractor_v3) {
 		this.interactor = sfaInteractor_v3;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object invoke(String methodName, List parameters) throws Throwable {
 		
 		Map<String, Object> response = null;
 
 		try {
 			Method knownMethod = getMethod(methodName);
 			AMResult result = getMethodCallResult(knownMethod, parameters);
 			response = createResponse(result);
 		} catch (ParsingException e) {
 			response = createErrorResponse(e);
 		}
 		return response;
 
 	}
 
 	private Map<String, Object> createErrorResponse(ParsingException e) throws IOException {
 		AMResult errorResult = getErrorResult(e);
 		Map<String, Object> errorResponse = introspect(errorResult);
 		return errorResponse;
 	}
 
 	private AMResult getErrorResult(ParsingException e) {
 		AMResult result = new AMResult();
 		result.setOutput(e.getMessage());
 		AMCode errorCode = getErrorCode(e);
 		result.setCode(errorCode);
 		return result;
 		
 	}
 
 	private AMCode getErrorCode(ParsingException e) {
 		AMCode code = new AMCode();
 		code.setGeni_code(e.getErrorCode());
 		return code;
 	}
 
 	private Map<String, Object> createResponse(AMResult result) {
 		Map<String, Object> response = new HashMap<>();
 		try {
 			response = introspect(result);
 		} catch (IOException ioException) {
 			log.error(ioException.getMessage(),ioException);
 		}
 		return response;
 	}
 
 	private AMResult getMethodCallResult(Method knownMethod, List parameters)
 			throws IllegalAccessException, InvocationTargetException,
 			InstantiationException {
 		AMResult result = null;
 
 		Class<?>[] parameterClasses = knownMethod.getParameterTypes();
 		if (parameterClasses.length == 0) {
 			result = (AMResult) knownMethod.invoke(interactor, (Object[]) null);
 		} else {
 			
 			List<Object> methodParameters = createEmptyMethodParameters(parameterClasses);
 
 			for (int i = 0; i < parameterClasses.length; i++) {
 				xmlStructToObject(parameters.get(i), methodParameters.get(i));
 			}
 
 			result = (AMResult) knownMethod.invoke(interactor,
 					methodParameters.toArray());
 
 		}
 		return result;
 	}
 
 	private Method getMethod(String methodName) {
 		Method knownMethod = null;
 		Method[] methodsFromHandler = interactor.getClass().getMethods();
 
 		for (int i = 0; i < methodsFromHandler.length; i++) {
 			if (methodsFromHandler[i].getName().equals(methodName)) {
 				// Critical assumption !!! Only one method which equals the
 				// methodname exists!
 				// failure prone
 				knownMethod = methodsFromHandler[i];
 			}
 		}
 		if (knownMethod == null){
 			ParsingException e = new MethodNotFound(methodName);
 			throw e;
 		}
 		return knownMethod;
 	}
 
 	private void xmlStructToObject(Object from, Object to) {
 		if (to.getClass().isAssignableFrom(ListResourceOptions.class)) {
 			XmlRpcStruct listResourceOptionsStruct = (XmlRpcStruct) from;
 			ListResourceOptions listResourceOptions = (ListResourceOptions) to;
 			listResourceOptions.setGeni_available(new GeniAvailableOption(
 					listResourceOptionsStruct.getBoolean("geni_available")));
 			listResourceOptions.setGeni_compressed(new GeniCompressedOption(
 					listResourceOptionsStruct.getBoolean("geni_compressed")));
 
 			XmlRpcStruct geni_rspec_version_struct = listResourceOptionsStruct
 					.getStruct("geni_rspec_version");
 			if (geni_rspec_version_struct != null) {
 				String type = geni_rspec_version_struct.getString("type");
 				String version = geni_rspec_version_struct.getString("version");
 				Geni_RSpec_Version geni_RSpec_Version = new Geni_RSpec_Version();
 				geni_RSpec_Version.setType(type);
 				geni_RSpec_Version.setVersion(version);
 
 				listResourceOptions.setGeni_rspec_version(geni_RSpec_Version);
 
 			} else {
 				// TODO error handling throw exception => set corresponding
 				// error code of response
 			}
 
 		}
 		if (to.getClass().isAssignableFrom(ListCredentials.class)) {
 			try {
 				XmlRpcArray listCredentialsArray = (XmlRpcArray) from;
 				ListCredentials listCredentials = (ListCredentials) to;
 				if (listCredentialsArray.size() > 0) {
 					for (int i = 0; i < listCredentialsArray.size(); i++) {
 						XmlRpcStruct credentialsStruct = (XmlRpcStruct) listCredentialsArray
 								.get(i);
 						Credentials credentials = new Credentials();
 						if (credentialsStruct.getString("geni_type") != null) {
 							credentials.setGeni_type(credentialsStruct
 									.getString("geni_type"));
 						} else {
							// TODO error handling
 						}
 						if (credentialsStruct.getString("geni_version") != null) {
 							credentials.setGeni_version(credentialsStruct
 									.getString("geni_version"));
 						} else {
							// TODO error handling
 						}
 						if (credentialsStruct.getString("geni_value") != null) {
 							credentials.setGeni_value(credentialsStruct
 									.getString("geni_value"));
 						} else {
							// TODO error handling
 						}
 
 						listCredentials.addCredentials(credentials);
 
 					}
 				}
 			} catch (ClassCastException e) {
 				throw new CredentialsNotValid();
 			}
 		}
 
 	}
 
 	private List<Object> createEmptyMethodParameters(Class<?>[] parameterClasses)
 			throws InstantiationException, IllegalAccessException {
 
 		List<Object> returnList = new ArrayList<>();
 		for (int i = 0; i < parameterClasses.length; i++) {
 			Object o = parameterClasses[i].newInstance();
 			returnList.add(o);
 		}
 
 		return returnList;
 	}
 
 
 
 	@SuppressWarnings("unchecked")
 	private Map<String, Object> introspect(Object result) throws IOException {
 		final ObjectMapper mapper = new ObjectMapper();
 		mapper.setSerializationInclusion(Include.NON_NULL);
 		final StringWriter writer = new StringWriter();
 
 		mapper.writeValue(writer, result);
 		final Map<String, Object> response = mapper.readValue(
 				writer.toString(), Map.class);
 
 		return response;
 	}
 
 	private class ParsingException extends RuntimeException {
 		private GENI_CodeEnum errorCode = GENI_CodeEnum.ERROR;
 		private String errorMessage = "Error";
 		private static final long serialVersionUID = 1L;
 		public void setErrorCode(GENI_CodeEnum errorCode) {
 			this.errorCode = errorCode;
 		}
 		
 		public GENI_CodeEnum getErrorCode() {
 			return errorCode;
 		}
 		
 		public String getMessage(){
 			return this.errorMessage;
 		}
 		
 		public void setMessage(String message){
 			this.errorMessage = message;
 		}
 	}
 
 	private class CredentialsNotValid extends ParsingException {
 
 		private static final long serialVersionUID = 1L;
 		public CredentialsNotValid() {
 			setErrorCode(GENI_CodeEnum.BADARGS);
 			setMessage("Credentials not Valid");
 		}
 
 	}
 
 	private class MethodNotFound extends ParsingException {
 
 		private static final long serialVersionUID = 2409993059634896770L;
 
 		public MethodNotFound(String methodName) {
 			setErrorCode(GENI_CodeEnum.RPCERROR);
 			setMessage("Method "+methodName +" not found");
 		}
 
 	}
 }
