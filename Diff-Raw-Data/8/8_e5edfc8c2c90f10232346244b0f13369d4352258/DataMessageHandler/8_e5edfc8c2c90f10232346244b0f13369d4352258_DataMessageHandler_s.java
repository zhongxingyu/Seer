 /**
  * 
  * Copyright 2002 - 2007 NCHELP
  * 
  * Author:	Tim Bornholtz, The Bornholtz Group
  *          Priority Technologies, Inc.
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
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.soap.rpc.SOAPContext;
 import org.nchelp.hpc.HPCMessage;
 import org.nchelp.hpc.server.HPCMessageHandler;
 import org.nchelp.hpc.util.exception.CompressionException;
 import org.nchelp.hpc.util.exception.EncodingException;
 import org.nchelp.hpc.util.exception.ParsingException;
 import org.nchelp.meteor.message.MeteorDataRequest;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.response.Award;
 import org.nchelp.meteor.message.response.DataProviderData;
 import org.nchelp.meteor.message.response.MeteorDataProviderAwardDetails;
 import org.nchelp.meteor.message.response.MeteorDataProviderDetailInfo;
 import org.nchelp.meteor.message.response.MeteorDataProviderInfo;
 import org.nchelp.meteor.message.response.MeteorRsMsg;
 import org.nchelp.meteor.message.response.OrgType;
 import org.nchelp.meteor.provider.AccessProvider;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.MeteorContext;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Messages;
 import org.nchelp.meteor.util.MeteorConstants;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.exception.DirectoryException;
 
 
 /**
  * This is the receiver class for Meteor Data requests.
  * 
  * @version $Revision$ $Date$
  * @since Meteor1.0
  *  
  */
 public class DataMessageHandler implements HPCMessageHandler
 {
 	private static transient final Log log = LogFactory.getLog(DataMessageHandler.class);
 	private final String respTransactionType = "METEORDATARESP";
 	private String resourceFile = "dataprovider.properties";
 
 	/**
 	 * Constructor for DataMessageHandler.
 	 */
 	public DataMessageHandler (String resourceFile)
 	{
 		this.resourceFile = resourceFile;
 	}
 
 	/**
 	 * Constructor for DataMessageHandler.
 	 */
 	public DataMessageHandler ()
 	{
 		super();
 	}
 
 	/**
 	 * Handle the High Performance Channel message for Meteor.  
 	 * This class performs all necessary security checks for ensuring that
 	 * the Access Provider request is valid.
 	 * If the request is valid then this class will instantiate the 
 	 * implementation of the <code>DataServerAbstraction</code>
 	 * @see HPCMessageHandler#handle(HPCMessage)
 	 */
 	public HPCMessage handle (HPCMessage message) throws ParsingException, CompressionException, EncodingException
 	{
 		String request = new String(message.getContent());
 		if (request == null)
 		{
 			throw new ParsingException(new Exception());
 		}
 
 		MeteorDataRequest mdReq = null;
 
 		try
 		{
 			mdReq = new MeteorDataRequest(request);
 		}
 		catch (org.nchelp.meteor.util.exception.ParsingException ex)
 		{
 			throw new org.nchelp.hpc.util.exception.ParsingException(ex);
 		}
 
 		if (log.isInfoEnabled())
 		{
 			AccessProvider ap = mdReq.getAccessProvider();
 			String apID = null;
 			if (ap != null)
 			{
 				apID = ap.getID();
 			}
 
 			SecurityToken token = mdReq.getSecurityToken();
 			String userid = null;
 			if (token != null)
 			{
 				userid = token.getUserid();
 			}
 			
 			SOAPContext soapContext = message.getSOAPCOntext();
 			String clientIP = "Unknown";
 			
 			if(soapContext != null){
 				HttpServletRequest req =
 					(HttpServletRequest)soapContext.getProperty(org.apache.soap.Constants.BAG_HTTPSERVLETREQUEST);
 				clientIP = req.getRemoteAddr();
 			}
 
 			log.info("Received request from Access Provider: " + apID
 					+ " with the IP address: " + clientIP
 					+ " with the user handle: " + userid + " and role: "
 					+ token.getRole() + " for the SSN: " + mdReq.getSSN());
 		}
 
 		MeteorDataResponse mdResp = new MeteorDataResponse();
 
 		Resource res = ResourceFactory.createResource(resourceFile);
 		
 		String sleep = res.getProperty("test.dp.sleep", "0");
 		if(sleep != null){
 			try{
 				int sleepint = Integer.parseInt(sleep);
 				log.info("Debugging Meteor and sleeping for " + sleep + " milliseconds");
 				Thread.sleep(sleepint);
 			} catch(NumberFormatException e){
 				//Oh well, no good value there.  Do nothing.
 			} catch (InterruptedException e) {
 				// Sleep failed.  Do nothing.
 			}
 		}
 		
 		
 		
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		SecurityToken token = mdReq.getSecurityToken();
 
 		// Check the signature on the message
 		String requireSignedMessage = res.getProperty("DataProvider.request.signaturerequired", "false");
 		Boolean required = new Boolean(requireSignedMessage);
 		boolean validMsg = mdReq.validateMessage(required.booleanValue());
 		if(! validMsg){
 			// fail here. Create a MeteorIndexResponse and say what happened
 			String errorMessage = Messages.getMessage("access.invalidmessagesignature");
 			mdResp.setError("Not Authorized", errorMessage);
 
 			HPCMessage errMessage = new HPCMessage();
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 
 			return errMessage;
 		}
 
 		//error checking here
 		if (!registry.authenticateProvider(token))
 		{
 			// fail here. Create a MeteorIndexResponse and say what happened
 			String errorMessage = Messages.getMessage("access.invalidtoken");
 			this.handleFatalError(mdResp, "Not Authorized", errorMessage);
 
 			HPCMessage errMessage = new HPCMessage();
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 
 			log.debug("Request Unauthorized:\n" + mdResp.toString());
 			return errMessage;
 		}
 
 		// Look at the authentication token and compare it with
 		// the minimum allowed level
 
 		int minimumLevelAllowed = 0;
 		String strMinimumAllowedLevel = res.getProperty("accessprovider.minimum.authentication.level");
 		try
 		{
 			minimumLevelAllowed = Integer.parseInt(strMinimumAllowedLevel);
 		}
 		catch (NumberFormatException ex)
 		{
 			log.fatal("accessprovider.minimum.authentication.level is not set as an integer in '"
 							+ resourceFile + "', it is '" + strMinimumAllowedLevel + "'");
 			String errorMessage = Messages.getMessage("data.nominimumlevel");
 			this.handleFatalError(mdResp, "Data Provider Configuration Error", errorMessage);
 
 			HPCMessage errMessage = new HPCMessage();
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 
 			return errMessage;
 		}
 
 		if (token.getCurrentAuthLevel() < minimumLevelAllowed)
 		{
 			log.info("Access Provider did not provide a sufficient authentication level");
 			String errorMessage = Messages.getMessage("data.insufficientlevel");
 			this.handleFatalError(mdResp, "Minimum Authentication Level", errorMessage);
 
 			HPCMessage errMessage = new HPCMessage();
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 
 			return errMessage;
 		}
 
 		// Check if the data provider supports this role
 		String role = token.getRole();
 		String strSupportRole = res.getProperty("dataprovider.role." + role.toLowerCase() + ".support");
 
 		// If the value is null (not in the property file at all)
 		// Then it *is* supported.
 		if (strSupportRole != null && !Boolean.valueOf(strSupportRole).booleanValue())
 		{
 			log.info("Data Provider does not support the " + role + " role");
 			String errorMessage = Messages.getMessage("data.role.notsupported");
 			this.handleFatalError(mdResp, "Unsupported Role", errorMessage);
 
 			HPCMessage errMessage = new HPCMessage();
 			errMessage.setRecipientID(message.getRecipientID());
 			errMessage.setContent(mdResp.toString(), respTransactionType);
 
 			return errMessage;
 		}
 
 		if (SecurityToken.roleBORROWER.equals(token.getRole()))
 		{
 			// Check to make sure that the SSN they are requesting is
 			// the same ssn that is in the assertion.
 
 			String assertionSSN = token.getAttributes("SSN");
 			String queriedSSN = mdReq.getSSN();
 
 			if (assertionSSN == null)
 			{
 				log.info("No SSN was passed as an attribute of the Security Token for the Borrower,"
 				       + " but this is a required element for all borrowers");
 
 				String errorMessage = Messages.getMessage("data.nossn");
 				this.handleFatalError(mdResp, "Missing SSN", errorMessage);
 
 				HPCMessage errMessage = new HPCMessage();
 				errMessage.setRecipientID(message.getRecipientID());
 				errMessage.setContent(mdResp.toString(), respTransactionType);
 
 				return errMessage;
 			}
 
 			if (!assertionSSN.equals(queriedSSN))
 			{
 				log.info("User requested data for the ssn '" + queriedSSN + "' but according to their assertion"
 						+ " they are only valid to view the ssn '" + assertionSSN + "'");
 
 				String errorMessage = Messages.getMessage("data.ssn.notauthorized");
 				this.handleFatalError(mdResp, "Invalid SSN", errorMessage);
 
 				HPCMessage errMessage = new HPCMessage();
 				errMessage.setRecipientID(message.getRecipientID());
 				errMessage.setContent(mdResp.toString(), respTransactionType);
 
 				return errMessage;
 			}
 		}
 
 		// Only the HELPDESK can make the STATUSQUERY and the HELPDESK
 		// can only make the STATUSQUERY
 		
 		if(SecurityToken.roleHELPDESK.equals(token.getRole())){
 			// After all of the security checks have passed, 
 			// see if this is really a status request.  If 
 			// so then just return a blank response with a message
 			if(MeteorConstants.STATUSQUERY.equals(mdReq.getSSN())){
 				this.handleStatusRequest(mdResp);
 	
 				HPCMessage msg = new HPCMessage();
 				msg.setRecipientID(message.getRecipientID());
 				msg.setContent(mdResp.toString(), respTransactionType);
 	
 				return msg;
 			} else {
 				// Helpdesk can't get real data.  Tell them no!
 				String errorMessage = Messages.getMessage("data.ssn.notauthorized");
 				this.handleFatalError(mdResp, "Helpdesk is not authorized to query with SSN", errorMessage);
 
 				HPCMessage errMessage = new HPCMessage();
 				errMessage.setRecipientID(message.getRecipientID());
 				errMessage.setContent(mdResp.toString(), respTransactionType);
 
 				return errMessage;
 			}
 		}
 		
 		// Figure out which implementation of DataServerAbstraction
 		// to instantiate and call the getData() method
 		// Handle the returned data and put it in a MeteorDataResponse
 		// object and return
 
 		String dataClass = res.getProperty("default.data.server");
 
 		log.debug("Instantiating Data Provider class: " + dataClass);
 		// Now that the class name has been looked up in the properties
 		// file, go ahead and instantiate one of those and store it
 		// as a generic DataServerAbstraction object.
 
 		DataServerAbstraction dsa = null;
 		try
 		{
 			dsa = (DataServerAbstraction)Class.forName(dataClass).newInstance();
 		}
 		catch (Throwable ex)
 		{
 			log.debug("Throwing ParsingException: Source - " + ex.getClass().getName() + ": " + ex.getMessage());
 			if (ex instanceof Exception)
 			{
 				throw new ParsingException((Exception)ex);
 			}
 			else
 			{
 				throw new ParsingException("Fatal Error: " + ex.getMessage());
 			}
 		}
 
 		// Set up the context object so the implementations
 		// will have access to everything they might concievably
 		// want to look at to make a decision to provide data
 		MeteorContext context = new MeteorContext();
 		context.setSecurityToken(mdReq.getSecurityToken());
 		context.setAccessProvider(mdReq.getAccessProvider());
 
 		try
 		{
 			mdResp = dsa.getData(context, mdReq.getSSN());
 		}
 		catch (Throwable ex)
 		{
 			log.error("Unknown error in " + dsa.getClass().getName(), ex);
 		}
 
 		if (mdResp == null)
 		{
 			mdResp = new MeteorDataResponse();
 		}
 		mdResp.createMinimalResponse();
 
 		// just to make sure that it didn't get mucked with earlier
 		role = token.getRole();
 
 		// if the role is APCSR or LENDER, loop through the awards and make sure
 		// they are all coming from the AP or one of its aliases
 		if (SecurityToken.roleAPCSR.equals(role) || SecurityToken.roleLENDER.equals(role))
 		{
 			MeteorDataProviderInfo[] infos = mdResp.getRsMsg().getMeteorDataProviderInfo();
 			if (infos != null)
 			{
 				List aliases = null;
 				String id;
 				String type;
 
 				if (SecurityToken.roleLENDER.equals(role))
 				{
 					id = token.getAttributes(SecurityToken.roleLENDER);
 					type = Directory.TYPE_LENDER;
 				}
 				else
 				{
 					id = mdReq.getAccessProvider().getID();
 					type = Directory.TYPE_ACCESS_PROVIDER;
 				}
 
 				log.debug("Filtering data based on aliases for ID " + id + " type: " + type);
 
 				try
 				{
 					aliases = DirectoryFactory.getInstance().getDirectory().getAliases(id, type);
 
 					// Don't add the access provider to the alias list. it must be
 					// explicitly set in the registry
 					//aliases.add(id);
 				}
 				catch (DirectoryException ex)
 				{
					ex.printStackTrace();
 					log.info("Unable to retrieve aliases for ID: " + id);
 					String errorMessage = Messages.getMessage("access.aliaserror");
 					this.handleFatalError(mdResp, "Alias Retrieval", errorMessage);
 
 					HPCMessage errMessage = new HPCMessage();
 					errMessage.setRecipientID(message.getRecipientID());
 					errMessage.setContent(mdResp.toString(), respTransactionType);
 
 					return errMessage;
 				}
 
 				for (int i = 0; i < infos.length; i++)
 				{
 					// For each mdpi, if every award is filtered then remove the messages too
 					boolean shouldFilterMessages = true;
 
 					// If the aliases is in the Data Provider, then it is OK too
 					try
 					{
 						String dpID = infos[i].getMeteorDataProviderDetailInfo().getDataProviderData().getEntityID();
 						if (aliases.contains(dpID))
 						{
 							//yep it is the same, so all of these awards are good
 							continue;
 						}
 					}
 					catch (NullPointerException ex)
 					{
 						// do nothing here. This means that we coundn't
 						// get the Data Provider's Entity ID.
 					}
 
 					MeteorDataProviderAwardDetails awardDetails = infos[i].getMeteorDataProviderAwardDetails();
 
 					if (awardDetails != null)
 					{
 						Award[] awards = awardDetails.getAward();
 
 						for (int j = awards.length - 1; j >= 0; j--)
 						{
 							OrgType org = awards[j].getSchool();
 							if (org != null && aliases.contains(org.getEntityID()))
 							{
 								shouldFilterMessages = false;
 								continue;
 							}
 
 							org = awards[j].getLender();
 							if (org != null && aliases.contains(org.getEntityID()))
 							{
 								shouldFilterMessages = false;
 								continue;
 							}
 
 							org = awards[j].getGuarantor();
 							if (org != null && aliases.contains(org.getEntityID()))
 							{
 								shouldFilterMessages = false;
 								continue;
 							}
 
 							org = awards[j].getServicer();
 							if (org != null && aliases.contains(org.getEntityID()))
 							{
 								shouldFilterMessages = false;
 								continue;
 							}
 
 							org = awards[j].getConsolLender();
 							if (org != null && aliases.contains(org.getEntityID()))
 							{
 								shouldFilterMessages = false;
 								continue;
 							}
 
 							org = awards[j].getDisbursingAgent();
 							if (org != null && aliases.contains(org.getEntityID()))
 							{
 								shouldFilterMessages = false;
 								continue;
 							}
 
 							// Otherwise, it wasn't in the list of aliases. Remove this award
 							log.info("Filtering award " + j + " because it is not in the alias list for ID: " + id);
 							awardDetails.removeAward(awards[j]);
 						}
 
 						if (shouldFilterMessages)
 						{
 							log.debug("Removing all DataProviderMsg blocks since all Awards are filtered");
 							infos[i].clearMeteorDataProviderMsg();
 						}
 					}
 				}
 			}
 		}
 
 		String mdRespString = mdResp.toString();
 
 		log.debug("Meteor Data Response:\n" + mdRespString);
 
 		HPCMessage response = new HPCMessage();
 		response.setRecipientID(message.getRecipientID());
 		response.setContent(mdRespString, respTransactionType);
 		response.setTransactionType(respTransactionType);
 
 		return response;
 	}
 
 	/**
 	 * @see HPCMessageHandler#getMode()
 	 */
 	public String getMode ()
 	{
 		return SYNC;
 	}
 
 	private void handleFatalError (MeteorDataResponse mdResp, String messageTitle, String messageDescription)
 	{
 		mdResp.setError(messageTitle, messageDescription);
 
 		Resource res = ResourceFactory.createResource(resourceFile);
 
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
 
 	private void handleStatusRequest (MeteorDataResponse mdResp)
 	{
 		mdResp.setError(MeteorConstants.STATUSQUERY, new Version().toString());
 
 		Resource res = ResourceFactory.createResource(resourceFile);
 
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
