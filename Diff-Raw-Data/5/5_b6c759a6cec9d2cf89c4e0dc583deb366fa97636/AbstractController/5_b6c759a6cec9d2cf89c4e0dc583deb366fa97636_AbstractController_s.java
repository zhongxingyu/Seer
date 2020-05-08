 package com.pms.service.controller;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import com.pms.service.cfg.ConfigurationManager;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.status.ResponseCodeConstants;
 import com.pms.service.util.status.ResponseStatus;
 
 public abstract class AbstractController {
     private static Logger logger = LogManager.getLogger(AbstractController.class);
 
     public HashMap<String, Object> parserJsonParameters(HttpServletRequest request, boolean emptyParameter) {
         HashMap<String, Object> parametersMap = readParameters(request, emptyParameter);
         logger.debug(String.format("--------------Client post parameters for path [%s] is [%s]", request.getPathInfo(), parametersMap));
 
         return parametersMap;
     }
 
     @SuppressWarnings("unchecked")
     private HashMap<String, Object> readParameters(HttpServletRequest request, boolean emptyParameter) {
         HashMap<String, Object> parametersMap = new HashMap<String, Object>();
 
         String parameters = request.getParameter(ApiConstants.JSON_PARAMETERS_LABEL);
 
         if (parameters != null) {
             try {
                 parametersMap = new Gson().fromJson(parameters, HashMap.class);
             } catch (JsonSyntaxException e) {
                 // TODO
             }
 
         } else {
             Enumeration<?> parameterNames = request.getParameterNames();
             while (parameterNames.hasMoreElements()) {
                 String pName = parameterNames.nextElement().toString();
                 parametersMap.put(pName, request.getParameter(pName));
             }
         }
 
         if (!emptyParameter && (parametersMap == null || parametersMap.isEmpty())) {
             throw new ApiResponseException(String.format("Parameters required for path [%s]", request.getPathInfo()), ResponseCodeConstants.PARAMETERS_EMPTY.toString());
         }
 
         parametersMap.remove("_");
         parametersMap.remove("callback");
 
         if (parametersMap.get("models") != null) {
             String v = parametersMap.get("models").toString();
             if (v.startsWith("[")) {
                 v = v.substring(1);
             }
 
             if (v.endsWith("]")) {
                 v = v.substring(0, v.lastIndexOf("]"));
             }
             parametersMap = new Gson().fromJson(v, HashMap.class);
         }
 
         if (parametersMap.get("_id") != null) {
             if (ApiUtil.isEmpty(parametersMap.get("_id"))) {
                 parametersMap.remove("_id");
             }
         }
         return parametersMap;
 
     }
 
     protected void responseWithData(Map<String, Object> data, HttpServletRequest request, HttpServletResponse response) {
         responseMsg(data, ResponseStatus.SUCCESS, request, response, null);
     }
     
     protected void responseWithData(Map<String, Object> data, HttpServletRequest request, HttpServletResponse response, String msgKey) {
         responseMsg(data, ResponseStatus.SUCCESS, request, response, msgKey);
     }
 
     protected void responseWithKeyValue(String key, String value, HttpServletRequest request, HttpServletResponse response) {
         if (key == null) {
             responseWithData(null, request, response);
         } else {
             Map<String, Object> temp = new HashMap<String, Object>();
             temp.put(key, value);
             responseWithData(temp, request, response);
         }
     }
 
 
     /**
      * This function will return JSON data to Client
      * 
      * 
      * @param data
      *            data to return to client
      * @param dataKey
      *            if set dataKey, the JSON format use dataKey as the JSON key,
      *            data as it's value, and both the dataKey and "status" key are
      *            child of the JSON root node. If not set dataKey, the data and
      *            the "status" node are both the child of the JSON root node
      * @param status
      *            0:FAIL, 1: SUCCESS
      * @return
      */
     private void responseMsg(Map<String, Object> data, ResponseStatus status, HttpServletRequest request, HttpServletResponse response, String msgKey) {
 
         Map<String, Object> result = new HashMap<String, Object>();
 
         if (data != null) {
             data.put("status", status.toString());
             result = data;          
         } else {
             result.put("status", status.toString());
         }
 
         response.setContentType("text/plain;charset=UTF-8");
 
         String jsonReturn = new Gson().toJson(result);
         
         if (request != null) {
 
             if (request.getParameter("callback") != null) {
                 response.setContentType("application/x-javascript;charset=UTF-8");
                 String displayMsg = null;
                 if (msgKey != null) {
                     displayMsg = "displayMsg({\"msg\": \"" + ConfigurationManager.getSystemMessage(msgKey) + "\"});";
                 }
                 if (result.get("data") != null) {
                     jsonReturn = request.getParameter("callback") + "(" + new Gson().toJson(result.get("data")) + ");";
                 } else {
                     jsonReturn = request.getParameter("callback") + "([]);";
                 }
                jsonReturn = jsonReturn + displayMsg;
             }
         }
         response.addHeader("Accept-Encoding", "gzip, deflate");
         try {
             response.getWriter().write(jsonReturn);
         } catch (IOException e) {
             logger.fatal("Write response data to client failed!", e);
         }
 
     }
 
     protected void responseServerError(Throwable throwable, HttpServletRequest request, HttpServletResponse response) {
         Map<String, Object> temp = new HashMap<String, Object>();
 
         if (throwable instanceof ApiResponseException) {
             ApiResponseException apiException = (ApiResponseException) throwable;
             temp.put("msg", apiException.getTipMsg());
             logger.error(String.format(" =========== API Validation failed with tip msg [%s] and log msg [%s] ", apiException.getTipMsg(), apiException.getMessage()));
 
         } else {
             temp.put("msg", "System Error");
         }
         responseMsg(temp, ResponseStatus.FAIL, request, response, null);
 
     }
 
 }
