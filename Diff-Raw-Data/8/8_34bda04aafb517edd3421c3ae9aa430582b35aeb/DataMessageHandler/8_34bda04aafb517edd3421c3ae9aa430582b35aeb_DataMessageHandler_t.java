 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package org.nchelp.meteor.provider.data;
 
 import org.apache.log4j.Priority;
 import org.nchelp.hpc.HPCMessage;
 import org.nchelp.hpc.server.HPCMessageHandler;
 import org.nchelp.hpc.util.exception.CompressionException;
 import org.nchelp.hpc.util.exception.EncodingException;
 import org.nchelp.hpc.util.exception.ParsingException;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataRequest;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.response.Contacts;
 import org.nchelp.meteor.message.response.DataProviderData;
 import org.nchelp.meteor.message.response.MeteorDataProviderDetailInfo;
 import org.nchelp.meteor.message.response.MeteorDataProviderInfo;
 import org.nchelp.meteor.message.response.MeteorRsMsg;
 import org.nchelp.meteor.provider.MeteorContext;
 import org.nchelp.meteor.registry.DistributedRegistry;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 
 /**
 * This is the receiver class for Meteor Data requests.  
 *  
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class DataMessageHandler implements HPCMessageHandler {
 
 	private final Logger log = Logger.create(this.getClass());
 	private final String respTransactionType = "METEORDATARESP";
 
 	/**
 	 * Constructor for DataMessageHandler.
 	 */
 	public DataMessageHandler() {
 		super();
 	}
 
 	/*
 	 * @see HPCMessageHandler#handle(HPCMessage)
 	 */
 	public HPCMessage handle(HPCMessage message)
 		throws ParsingException, CompressionException, EncodingException {
 
 		String request = new String(message.getContent());
 		if(request == null){
 			throw new ParsingException(new Exception());
 		}
 		
 		MeteorDataRequest mdReq = null;
 		
 		try {
 			mdReq = new MeteorDataRequest(request);
 		} catch(org.nchelp.meteor.util.exception.ParsingException e) {
 			throw new org.nchelp.hpc.util.exception.ParsingException(e);
 		}
 		MeteorDataResponse mdResp = new MeteorDataResponse();
 
 
 		Resource res = ResourceFactory.createResource("dataprovider.properties");
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		SecurityToken token = mdReq.getSecurityToken();
 		
 		
 		//error checking here
 		if(! registry.authenticateProvider(token)){
 			// fail here.  Create a MeteorIndexResponse and say what happened
 			this.handleFatalError(mdResp, "Not Authorized", "Invalid Authentication credentials received by Data Provider");
 
 			HPCMessage errMessage = new HPCMessage();			
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 			
 			log.debug("Unauthorized: " + mdResp.toString());
 			return errMessage;
 		}
 		
 		// Look at the authentication token and compare it with
 		// the minimum allowed level
 		
 		int minimumLevelAllowed = 0;
 		String strMinimumAllowedLevel = res.getProperty("accessprovider.minimum.authentication.level");
 		try {
 			minimumLevelAllowed = Integer.parseInt(strMinimumAllowedLevel);
 		} catch(NumberFormatException e) {
 			log.fatal("accessprovider.minimum.authentication.level is not set as an integer in 'dataprovider.properties', it is '" + strMinimumAllowedLevel + "'");
 			this.handleFatalError(mdResp, "Data Provider Configuration Error", "Minimum Authentication Level is not configured correctly at this Data Provider");
 
 			HPCMessage errMessage = new HPCMessage();			
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 			
 			return errMessage;
 		}
 		
 		if(token.getCurrentAuthLevel() < minimumLevelAllowed){
 			log.info("Access Provider did not provide a sufficient authentication level");
 			mdResp.setError("Minimum Authentication Level", "Insufficient authentication level provided");
 
 			HPCMessage errMessage = new HPCMessage();			
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 			
 			return errMessage;
 		}
 		
 		
 		// Figure out which implementation of IndexServerAbstraction 
 		// to instantiate and call the getData() method
 		// Handle the returned data and put it in a MeteorIndexResponse
 		// object and return
 
 		String dataClass = res.getProperty("default.data.server");
 		
 		log.debug("Instantiating Data Provider class: " + dataClass);
 		// Now that the class name has been looked up in the properties
 		// file, go ahead and instantiate one of those and store it
 		// as a generic DataServerAbstraction object.
 		
 		DataServerAbstraction dsa = null;
 		try {
 
 			dsa = (DataServerAbstraction) Class.forName(dataClass).newInstance();
 
 		} catch (Exception e) {
 			log.debug("Throwing ParsingException: " + e.getMessage());
 			throw new ParsingException(e);
 		}
 		
 
 		// Set up the context object so the implementations
 		// will have access to everything they might concievably
 		// want to look at to make a decision to provide data
 		MeteorContext context = new MeteorContext();
 		context.setSecurityToken(mdReq.getSecurityToken());
 		context.setAccessProvider(mdReq.getAccessProvider());
 		
		try{
			mdResp = dsa.getData(context, mdReq.getSSN());
		} catch(Exception e){
			log.error("Unknown error in " + dsa.getClass().getName(), e);
		}
		
		log.debug("Meteor Data Response: " + mdResp.toString());
 
 		
 		HPCMessage response = new HPCMessage();			
 		response.setRecipientID(message.getRecipientID());
 		response.setContent(mdResp.toString(), respTransactionType);
 		response.setTransactionType(respTransactionType);
 			
 			
 		if(log.isEnabledFor(Priority.DEBUG)){
 			log.debug(response.toString());
 		}
 		
 		return response;	
 	}
 
 	/*
 	 * @see HPCMessageHandler#getMode()
 	 */
 	public String getMode() {
 		return this.SYNC;
 	}
 	
 	private void handleFatalError(MeteorDataResponse mdResp, String messageTitle, String messageDescription){
 		mdResp.setError(messageTitle, messageDescription);
 
 		Resource res = ResourceFactory.createResource("dataprovider.properties");
 		
 		String name = res.getProperty("DataProvider.Data.Name");
 		String type = res.getProperty("DataProvider.Data.Type");
 		String id = res.getProperty("DataProvider.Data.ID");
 		String url = res.getProperty("DataProvider.Data.URL");
 		
 		
 		MeteorRsMsg msg = mdResp.getRsMsg();
 		MeteorDataProviderInfo mdpi = new MeteorDataProviderInfo();
 		msg.addMeteorDataProviderInfo(mdpi);
 		MeteorDataProviderDetailInfo mdpdi = new MeteorDataProviderDetailInfo();
 		mdpi.setMeteorDataProviderDetailInfo(mdpdi);
 		
 		mdpdi.setDataProviderType(type);
 		
 		DataProviderData dpd = new DataProviderData();
 		dpd.setEntityName(name);
 		dpd.setEntityID(id);
 		dpd.setEntityURL(url);
 
 		
 		mdpdi.setDataProviderData(dpd);
 		
 		mdResp.createMinimalResponse();
 		return;
 	}
 }
 
