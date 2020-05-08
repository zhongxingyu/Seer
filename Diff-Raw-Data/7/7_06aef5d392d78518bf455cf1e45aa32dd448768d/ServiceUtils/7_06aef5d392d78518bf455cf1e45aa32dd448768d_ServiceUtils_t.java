 package com.own.controller.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import com.own.common.constants.ErrorConstants;
 import com.own.merchant.model.ServiceResponse;
 
 public class ServiceUtils {
 
 
 	public static ServiceResponse composeServiceResponse(String respCode,
 			Map<String, List<String>> messages, Object response) {
 
 		ServiceResponse sResponse = new ServiceResponse();
 		sResponse.setResponseCode(respCode);
 		sResponse.setMessage(messages);
 		sResponse.setResponse(response);
 		return sResponse;
 
 	}
 	
 	public static ServiceResponse composeServiceResponse(String respCode,
 			String errorMessage, Object response) {
 		return composeServiceResponse(respCode, "ERROR",errorMessage, response);
 	}
 	
 	public static ServiceResponse composeServiceResponse(String respCode,
 			String errorCode,String errorMessage, Object response) {
 		Map<String, List<String>> errorMap = new HashMap<String, List<String>>();
 		List<String> errorList = new ArrayList<String>();
 		errorList.add(errorMessage);
 		errorMap.put(errorCode, errorList);
		
		List<String> errorList1 = new ArrayList<String>();
		errorList.add("Custom dummy method");
		errorMap.put(String.valueOf(ErrorConstants.ACTIVATION_EXPIRED), errorList1);
		
 		ServiceResponse sResponse = new ServiceResponse();
 		sResponse.setResponseCode(respCode);
 		sResponse.setMessage(errorMap);
 		sResponse.setResponse(response);
 		return sResponse;
 	}
 
 
 	/**
 	 * This method compares two strings and checks if both are equal or not
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	public static boolean compareString(String one, String two) {
 		return false;
 	}
 
 
 
 }
