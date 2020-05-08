 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.pubsub.PubSubManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.model.data.application.MDataBean;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * Servlet implementation class PubSubRequestHandler
  */
 @MultipartConfig
 @WebServlet("/PublishRequestHandler")
 public class PublishRequestHandler extends AbstractRequestHandler {
   /* --------------------------------------------------------- */
   /* Attributes */
   /* --------------------------------------------------------- */
   private static final long serialVersionUID = 1L;
 
   private PubSubManager pubsubManager;
 
   /* --------------------------------------------------------- */
   /* Constructors */
   /* --------------------------------------------------------- */
   /**
    * @throws ServletException
    * @see HttpServlet#HttpServlet()
    */
   public PublishRequestHandler() throws ServletException {
     super();
 
     try {
       pubsubManager = new PubSubManager();
     } catch (final InternalBackEndException e) {
       throw new ServletException("PubSubManager is not accessible because: " + e.getMessage());
     }
   }
 
   /* --------------------------------------------------------- */
   /* extends AbstractRequestHandler */
   /* --------------------------------------------------------- */
   @Override
   protected Map<String, String> getParameters(final HttpServletRequest request) throws AbstractMymedException {
 
     if (request.getContentType() != null && !request.getContentType().matches("multipart/form-data.*")) {
       return super.getParameters(request);
       // throw new
       // InternalBackEndException("PublishRequestHandler should use a multipart request!");
     }
 
     final Map<String, String> parameters = new HashMap<String, String>();
     try {
       for (final Part part : request.getParts()) {
         final String key = part.getName();
         final Scanner s = new Scanner(part.getInputStream());
         final String value = URLDecoder.decode(s.nextLine(), "UTF-8");
         parameters.put(key, value);
       }
     } catch (final Exception e) {
       e.printStackTrace();
       throw new InternalBackEndException("Error in getting arguments");
     }
 
     if (!parameters.containsKey("code")) {
       throw new InternalBackEndException("code argument is missing!");
     }
 
     return parameters;
   }
 
   /* --------------------------------------------------------- */
   /* extends HttpServlet */
   /* --------------------------------------------------------- */
   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
    *      response)
    */
   @Override
   protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
       IOException {
 
     final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
     try {
       final Map<String, String> parameters = getParameters(request);
       final RequestCode code = requestCodeMap.get(parameters.get("code"));
 
       // accessToken
       if (!parameters.containsKey("accessToken")) {
         throw new InternalBackEndException("accessToken argument is missing!");
       } else {
         tokenValidation(parameters.get("accessToken")); // Security Validation
       }
 
       switch (code) {
         case READ :
         case DELETE :
         default :
           throw new InternalBackEndException("PublishRequestHandler(" + code + ") not exist!");
       }
 
     } catch (final AbstractMymedException e) {
       LOGGER.info("Error in doGet operation");
       LOGGER.debug("Error in doGet operation", e);
       message.setStatus(e.getStatus());
       message.setDescription(e.getMessage());
     }
 
     printJSonResponse(message, response);
   }
 
   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
    *      response)
    */
   @Override
   protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
       IOException {
 
     final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
     try {
       final Map<String, String> parameters = getParameters(request);
       final RequestCode code = requestCodeMap.get(parameters.get("code"));
       String application, predicateListJson, user, data;
 
       // accessToken
       if (parameters.get("accessToken") == null) {
         throw new InternalBackEndException("accessToken argument is missing!");
       } else {
         tokenValidation(parameters.get("accessToken")); // Security Validation
       }
 
       switch (code) {
         case CREATE : // PUT
           message.setMethod("CREATE");
           if ((application = parameters.get("application")) == null) {
             throw new InternalBackEndException("missing application argument!");
           } else if ((predicateListJson = parameters.get("predicate")) == null) {
             throw new InternalBackEndException("missing predicate argument!");
           } else if ((user = parameters.get("user")) == null) {
             throw new InternalBackEndException("missing user argument!");
           } else if ((data = parameters.get("data")) == null) {
             throw new InternalBackEndException("missing data argument!");
           }
 
           try {
         	  
             final MUserBean userBean = getGson().fromJson(user, MUserBean.class);
             final Type dataType = new TypeToken<List<MDataBean>>() {
             }.getType();
             final List<MDataBean> dataList = getGson().fromJson(data, dataType);
             final List<MDataBean> predicateListObject = getGson().fromJson(predicateListJson, dataType);
 
             // construct the subPredicate
             String subPredicate = "";
             for (final MDataBean element : predicateListObject) {
               subPredicate += element.getKey() + "(" + element.getValue() + ")";
             }
 
             // construct the Predicate => broadcast algorithm
             final int broadcastSize = (int) Math.pow(2, predicateListObject.size());
             for (int i = 1; i < broadcastSize; i++) {
               int mask = i;
               String predicate = "";
               int j = 0;
               while (mask > 0) {
                 if ((mask & 1) == 1) {
                   final MDataBean element = predicateListObject.get(j);
                   predicate += element.getKey() + "(" + element.getValue() + ")";
                 }
                 mask >>= 1;
                 j++;
               }
               if (!predicate.equals("")) {
                 pubsubManager.create(application, predicate, subPredicate, userBean, dataList);
               }
             }
 
           } catch (final JsonSyntaxException e) {
             throw new InternalBackEndException("jSon format is not valid");
           } catch (final JsonParseException e) {
             throw new InternalBackEndException(e.getMessage());
           }
           break;
         default :
           throw new InternalBackEndException("PublishRequestHandler(" + code + ") not exist!");
       }
 
     } catch (final AbstractMymedException e) {
       LOGGER.info("Error in doPost operation");
       LOGGER.debug("Error in doPost operation", e);
       message.setStatus(e.getStatus());
       message.setDescription(e.getMessage());
     }
 
     printJSonResponse(message, response);
   }
 }
