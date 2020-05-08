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
 package com.mymed.controller.core.requesthandler.v2;
 
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.reputation.api.mymed_ids.MymedAppUserId;
 import com.mymed.controller.core.manager.reputation.api.mymed_ids.ReputationRole;
 import com.mymed.controller.core.manager.reputation.api.recommendation_manager.ReputationManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.model.data.reputation.MReputationBean;
 /**
  * Servlet implementation class ReputationRequestHandler
  */
 @WebServlet("/v2/ReputationRequestHandler")
 public class ReputationRequestHandler extends AbstractRequestHandler {
     /**
      * Generated serial ID.
      */
     private static final long serialVersionUID = -1758781146254746346L;
 
     /**
      * The JSON 'reputation' attribute.
      */
     private static final String JSON_REPUTATION = JSON.get("json.reputation");
 
     /**
     * The JSON 'consumer' attribute.
     */
    //private static final String JSON_CONSUMER = JSON.get("json.consumer");

    /**
      * The JSON 'producer' attribute.
      */
     private static final String JSON_PRODUCER = JSON.get("json.producer");
 
     private final ReputationManager reputationManager;
 
     public ReputationRequestHandler() throws ServletException {
         super();
         reputationManager = new ReputationManager();
     }
 
     /*
      * (non-Javadoc)
      * @see
      * com.mymed.controller.core.requesthandler.AbstractRequestHandler#doGet
      * (javax.servlet.http.HttpServletRequest,
      * javax.servlet.http.HttpServletResponse)
      */
     @Override
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessage<Object> message = new JsonMessage<Object>(200, this.getClass().getName());
 
         try {
             final Map<String, String> parameters = getParameters(request);
             // Check the access token
             checkToken(parameters);
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
             final String 
             	application = parameters.get(JSON_APPLICATION), 
            	producer = parameters.get(JSON_PRODUCER)/*, 
            	consumer = parameters.get(JSON_CONSUMER)*/;
 
             switch (code) {
                 case READ :
                     if (application == null)
                         throw new InternalBackEndException("missing application argument!");
                     else if (producer == null)
                         throw new InternalBackEndException("missing producer argument!");
                     
                     final MymedAppUserId user = new MymedAppUserId(application, producer, ReputationRole.Producer);
                     final MReputationBean reputation = reputationManager.read(user);
 
                     message.addDataObject(JSON_REPUTATION, reputation);
                     break;
                 case DELETE :
                     break;
                 default :
                     throw new InternalBackEndException("ReputationRequestHandler.doGet(" + code + ") not exist!");
             }
         } catch (final AbstractMymedException e) {
             LOGGER.debug("Error in doGet operation", e);
             message.setStatus(e.getStatus());
             message.setDescription(e.getMessage());
         }
 
         printJSonResponse(message, response);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * com.mymed.controller.core.requesthandler.AbstractRequestHandler#doPost
      * (javax.servlet.http.HttpServletRequest,
      * javax.servlet.http.HttpServletResponse)
      */
     @Override
     protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessage<Object> message = new JsonMessage<Object>(200, this.getClass().getName());
 
         try {
             final Map<String, String> parameters = getParameters(request);
             // Check the access token
             checkToken(parameters);
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
 
             switch (code) {
                 case CREATE :
                 case UPDATE :
                     break;
                 default :
                     throw new InternalBackEndException("ReputationRequestHandler.doPost(" + code + ") not exist!");
             }
         } catch (final AbstractMymedException e) {
             LOGGER.debug("Error in doPost operation", e);
             message.setStatus(e.getStatus());
             message.setDescription(e.getMessage());
         }
 
         printJSonResponse(message, response);
     }
 }
