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
 
 package org.nchelp.meteor.provider.access;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.xpath.XPathAPI;
 import org.nchelp.meteor.aggregation.AggregatedLoanData;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorIndexResponse;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.DataProviderThread;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Cache;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.AssertionException;
 import org.nchelp.meteor.util.exception.AuthenticationLevelException;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.nchelp.meteor.util.exception.IndexException;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.nchelp.meteor.util.exception.SignatureException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 
 /**
  *   Class IndexQueryService.java
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Meteor 1.0
  */
 public class IndexQueryService {
 
 	private final Logger log = Logger.create(this.getClass().getName());
 
 	private static Cache meteorDataCache = new Cache();
 	private static Cache iProviderCache = new Cache();
 	private static Cache dProviderCache = new Cache();
 	
 	private AggregatedLoanData aggregatedData;
 
 
 
 	/**
 	 * Public Constructor
 	 * This will initialize the aggregation objects
 	 * 
 	 * 
 	 */
 	public IndexQueryService(){
 		
 		/* Set up the Aggregation object here 
 		 * Figure out which files to use for best source 
 		 * and for duplicate logic
 		 */
 		Resource res = ResourceFactory.createResource("accessprovider.properties");
 		String bestsource = res.getProperty("meteor.aggregation.bestsource");
 		String duplicate = res.getProperty("meteor.aggregation.duplicateaward");
 
 		try{
 			AggregatedLoanData.setXMLEngines(duplicate, bestsource);
 		} catch(ParsingException e){
 			log.error("Error initializing XMLEngines", e);
 		}
 		
 		aggregatedData = new AggregatedLoanData();
 		
 	}
 
 	/**
 	 * Main method for Meteor queries.  This will return an XML document
	 * that represents the information
	 * @param params MeterParameters that describe which data should be
	 * displayed
 	 * @return String
 	 */
 	public String query(MeteorParameters params) throws AuthenticationLevelException{
 		// check dataProviderCache
 		// if no providers in cache should 
 		// look for index providers in index provider cache
 		// if not found should look it up and put it in cache 
 
 		List dProviders = null;
 		List iProviders = null;
 		
 		
 		Document xmlDocument = null;
 		String   xmlString = null;
 		
 		
 		int awardID = 1;
 		
 		// Check to see if this is a subsequent request for data that we've 
 		// already looked up.  If the ForceRefresh is true, then no matter
 		// what we won't use cached data.
 		String cachedData = (String)meteorDataCache.cached(params.getSsn());
 		
 		if(cachedData != null){
 			if(params.getForceRefresh()){
 				log.debug("Data is cached but ForceRefresh is true so clearing cache and requerying");
 				// Clear the cache if it is there
 				meteorDataCache.remove(params.getSsn())	;
 			} else {
 				log.debug("Data is cached.");
 				return cachedData;
 			}
 			
 		}
 
 		DistributedRegistry registry = DistributedRegistry.singleton();
 
 		SecurityToken token;
 		try {
 			token = this.getSecurityToken(params);
 		} catch (AssertionException e) {
 			return this.createErrorXML("E", e.getMessage());
 		}
 				
 		dProviders = (List)dProviderCache.cached(params.getSsn());
 
 		if (dProviders == null ){
 			dProviders = new ArrayList();
 		}
 		
 		if (dProviders.isEmpty()) {
 			iProviders = this.getIndexProviderList(registry);
 			if(iProviders == null){ iProviders = new ArrayList(); }
 			
 			if(iProviders.isEmpty()){
 				// There's really no point in going on here
 				return this.createErrorXML("E", "No Index Providers in Meteor Registry.");
 			}
 
 			// now request a list of data providers from each index provider.
 			Iterator iterator = iProviders.iterator();
 			IndexProvider iProvider = null;
 
 			while (iterator.hasNext()) {
 				
 				iProvider = (IndexProvider) iterator.next();
 				MeteorIndexResponse ipResp = null;
 				try{
 					if(iProvider == null){
 						log.error("Ack! the IndexProvider object is null");
 						continue;
 					}
 					ipResp = iProvider.getDataProviders(token, params);
 				} catch(IndexException e){
 					// Is this really something we want to show to the user?!?!
 					// I think not.
 					log.error(e);
 				}
 				if(ipResp != null){
 					dProviders = this.aggregateList(dProviders, ipResp.getDataProviderList());
 				}
 			}
 		}
 
 		// Check the current Authentication level against the
 		// minimum level required to contact every Data Provider
 		
 		// Also set the parameters for each Data Provider while
 		// I'm already looping through them
 		Iterator iterator = dProviders.iterator();
 		DataProvider dProvider = null;
 		
 		int currentLevel = token.getCurrentAuthLevel();
 		int minimumLevel = -1;
 		while(iterator.hasNext()){
 			dProvider = (DataProvider) iterator.next();
 			
 			// If the current level is less than the minimum level 
 			// then throw the exception
 			int level = dProvider.getMinimumAuthenticationLevel();
 			if(level > minimumLevel){ minimumLevel = level;}
 			
 			
 			/* If this particular Data Provider requires a higher
 			 * level than the user has and the user chose to
 			 * override the Minimum Authentication Level stuff then
 			 * don't call this Data Provider.
 			 * 
 			 * This will only happen when the user is requested to bump
 			 * their authentication level and they choose not to 
 			 * bump but go ahead with the data they are already 
 			 * authorized to see.  
 			 */
 			if(level > currentLevel && params.isOverrideMinimumAuthenticationLevel()){
 				iterator.remove();
 				continue;	
 			}
 			
 			dProvider.setToken(token);
 			dProvider.setParams(params);
 		}
 
 		if(currentLevel < minimumLevel && (! params.isOverrideMinimumAuthenticationLevel())){
 			AuthenticationLevelException ale = new AuthenticationLevelException("Current authentication level is insufficient to contact all necessary Data Providers");
 			ale.setMinimumAuthenticationLevel(minimumLevel);
 			log.warn("User does not have a sufficient authorization level");
 			throw ale;
 		}
 		
 
 		// Make all of the calls.  This is multithreaded.
 		this.callDataProviders(dProviders);
 		
 		iterator = dProviders.iterator();
 		while(iterator.hasNext()){
 			dProvider = (DataProvider) iterator.next();
 			String respTemp = null;
 			
 			respTemp = dProvider.getResponse();
 			
 			if(respTemp == null){ continue; }
 			/* 
 			 * Loop through each Award type and set an ID
 			 */
 			respTemp = XMLParser.removeDefaultNamespace(respTemp);
 			
 			Document resp;
 			try {
 				resp = XMLParser.parseXML(respTemp);
 			} catch (ParsingException e) {
 				log.debug("Parsing Error", e);
 				continue;
 			}			
 			
 			NodeList awds;
 			try {
 				awds = XPathAPI.selectNodeList(resp, "//Award");
 			} catch (TransformerException e) {
 				log.debug("Transforming Error", e);
 				continue;			
 			}
 			
 			for(int k=0; k < awds.getLength(); k++){
 				Node awardNode = awds.item(k);
 				Document award;
 				try {
 					award = XMLParser.createDocument(awardNode);
 				} catch (ParsingException e) {
 					log.debug("Parsing Error", e);
 					continue;
 				}
 				
 				Node idNode = XMLParser.getNode(award, "APSUniqueAwardID");
 				if(idNode == null){
 						Element rootElem = award.getDocumentElement();
 						Node tmpIdNode = award.createElement("APSUniqueAwardID");
 						idNode = award.createTextNode(Integer.toString(awardID));
 						rootElem.appendChild(tmpIdNode);
 						tmpIdNode.appendChild(idNode);
 				} else {				
 					idNode.setNodeValue(Integer.toString(awardID));
 				}
 				
 				awardID++;
 				
 				aggregatedData.add(award);
 			}
 			
 			
 			if(xmlDocument == null){
 				xmlDocument = resp;
 			} else {
 				// Find all of the MeteorDataProviderInfo tags and add
 				// them to the xmlDocument
 				NodeList mdpiNodeList = null;
 				try {
 					mdpiNodeList =
 						XPathAPI.selectNodeList(resp, "//MeteorDataProviderInfo");
 				} catch (TransformerException e) {
 					log.error("Error selecting the list of MeteorDataProviderInfo nodes: " + e);
 				}
 				
 				if(mdpiNodeList == null){
 					// then the exception was just thrown
 					continue;	
 				}
 				
 				
 				for(int i=0; i< mdpiNodeList.getLength(); i++){
 					Node mdpi = mdpiNodeList.item(i);
 					
 					if(mdpi == null){  continue; }
 					
 					// This makes it allowable to be imported into the document
 					Node tmpNode = xmlDocument.importNode(mdpi, true);
 					//Now import it after the root node
 					xmlDocument.getDocumentElement().appendChild(tmpNode);	
 				}
 			}
 		}
 			
 		aggregatedData.aggregateLoans();
 		
 		if(xmlDocument == null){
 			xmlString = this.createErrorXML("I", "No Data Found");
 		} else {
 			// Somehow figure out which possible awards are
 			// duplicates
 			Object[] awards = aggregatedData.getBest();
 			
 			String tmpString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 		             "<PESCXML:MeteorRsMsg xmlns:PESCXML=\"http://schemas.pescxml.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.pescxml.org Meteor_Schema_1-0-0.xsd\" PESCXMLVersion=\"1.0.0\">\n" +
 		             "</PESCXML:MeteorRsMsg>";
 		             
 			Document tmpDocument = null;
 			try {
 				tmpDocument = XMLParser.parseXML(tmpString);
 			} catch (ParsingException e) {
 				// we just made the XML string
 				// how could it not parse???
 			}
 		   
 			for(int i=0; i < awards.length; i++){
 				
 				Document awardNode = (Document)awards[i];
 				Node tmpNode = tmpDocument.importNode(awardNode.getDocumentElement(), true);
 				
 				tmpDocument.getDocumentElement().appendChild(tmpNode);	
 			}
 			
 			xmlDocument = tmpDocument;
 			xmlString = XMLParser.XMLToString(xmlDocument);
 		}
 		
 		if(xmlString == null && xmlDocument != null){
 			xmlString = XMLParser.XMLToString(xmlDocument);
 		}
 
 		// if it is still null we're in trouble
 		if(xmlString == null) {
 			log.error("We got to the end of processing in the query method and the xmlString is still null");
 			xmlString = this.createErrorXML("E", "Error Retrieving Data");
 		}
 		
 		// Save it off for subsequent queries		
 		meteorDataCache.add(params.getSsn(), xmlString);
 		
 		//log.debug("Final XML Returned to browser: " + xmlString);
 		return xmlString;
 	}
 	
 	
 	/**
 	 * Get the list of Index Providers.  Cache them in this 
 	 * method if necessary
 	 * 
 	 * @param registry
 	 * @return List
 	 */
 	private List getIndexProviderList(DistributedRegistry registry){
 		List iProviders = (List)iProviderCache.cached("");
 
 		if (iProviders == null || iProviders.isEmpty()) {
 
 			iProviders = registry.getIndexProviders();
 			if(iProviders != null){
 				iProviderCache.add("", iProviders);
 			}
 		}
 		
 		return iProviders;
 		
 	}
 
 	/**
 	 * There are a few configuration parameters
 	 * available in the authentication.properties
 	 * that determine the functionality here.
 	 * 
 	 * First: meteor.authentication.requirepassedassertion
 	 * If this is "Yes", then the access provider
 	 * will never create its own assertion
 	 * The default is "No"
 	 * 
 	 * Second: meteor.authentication.acceptassertions
 	 * If this is "No" then this access provider
 	 * cannot be the recipient of a "bump"
 	 * The default is "Yes" 
 	 *
 	 * @param params MeteorParameters containing everything passed
 	 * into the Meteor Access Provider software
 	 * @return SecurityToken
 	 */
 	private SecurityToken getSecurityToken(MeteorParameters params) throws AssertionException{
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		
 		Resource res =
 			ResourceFactory.createResource("accessprovider.properties");
 			
 		String strRequireAssertion = res.getProperty(
 				"meteor.authentication.requirepassedassertion",
 				"No");
 		boolean requirePassedAssertion =
 			("Yes".equalsIgnoreCase(strRequireAssertion) ? true : false);
 
 		String strAcceptAssertion =	res.getProperty(
 		        "meteor.authentication.acceptassertions", "Yes");
 		boolean acceptPassedAssertion =
 			("Yes".equalsIgnoreCase(strAcceptAssertion) ? true : false);
 		
 		
 		SecurityToken token = null;
 		
 		// If a SecurityToken was passed in on the parameters
 		// then the user went through an authentication bump
 		
 		token = params.getSecurityToken();
 		
 		// If there was no assertion passed and this is allowable,
 		// then create one.
 		if( token == null && ! requirePassedAssertion){
 			try{
 				token = registry.getAuthentication();
 				token.setRole(params.getRole());
 			} catch(ParameterException e){
 				log.error("Invalid Role Defined", e);	
 			} catch(SignatureException e){
 				// I know that the token isn't signed here
 				// So, the only way a SignatureException 
 				// can happen here is if there was some 
 				// problem contacting the registry
 				log.error("Signature Exception getting a Security Token from the registry: " + e);
 				
 				
 				AssertionException ae = new AssertionException("Error connecting to Meteor Registry");
 				ae.setOriginalException(e);
 				throw ae;
 			}
 		// Token is passed as a parameter	
 		} else if( token != null && acceptPassedAssertion) {  
 			log.debug("Using the security token passed as a parameter");
 			if(! registry.authenticateProvider(token)) {
 				// There's really no point in going on here
 				AssertionException ae =
 					new AssertionException("Invalid Security Token passed to Access Provider");
 				throw ae;
 			}
 			
 		// Else we have a conflict with the allowable parameters
 		} else if( token != null && ! acceptPassedAssertion) {
 			throw new AssertionException("Security Token is not an an allowable parameter to this Access Provider");
 			
 		} else if( token == null && requirePassedAssertion) {
 			throw new AssertionException("No Security Token was passed to this Access Provider");
 		}
 		
 		return token;
 	}
 	
 	/**
 	 * As each of the calls to AccessProvider.getDataProviders() returns
 	 * call this method to eliminate any of the duplicate Data
 	 * Providers
 	 * @param dataProviders
 	 * @return List
 	 */
 	private List aggregateList(List dataProviders, List newDataProviders){
 		// easiest way to do this is to cast this to a Set and add them then turn it back into a List
 
 		if(newDataProviders == null){
 			return dataProviders;
 		}
 		
 		if(dataProviders == null){
 			dataProviders = new ArrayList();
 		}
 		
 		// Loop through the newDataProviders and make sure they are real data providers
 		Iterator iter = newDataProviders.iterator();
 		while(iter.hasNext()){
 			DataProvider dp = (DataProvider)iter.next();
 			
 			String status;
 			try {
 				Directory dir = DirectoryFactory.getInstance().getDirectory();
 				status = dir.getStatus(dp.getId(), Directory.TYPE_DATA_PROVIDER);
 			} catch (DirectoryException e) {
 				// Error here, we cannot accept this as a valid data provider
 				log.error("DirectoryException while validating the status of the Data Provider: " + dp.getId() +
 				          ". This Data Provider is being removed from the list of providers. Message: " + e.getMessage());
 				iter.remove();
 				continue;
 			}
 			
 			if(! "AC".equals(status)){
 				log.info("Data Provider: " + dp.getId() + " does not have a status of 'AC' (Active).  It has a status of " +
 				         status + ".  It is being removed from the list of Data Providers returned by the Index Provider");
 				iter.remove();
 			}
 		}
 		
 		dataProviders.addAll(newDataProviders);
 		return dataProviders;
 	}
 	
 	private void callDataProviders(List  providers){
 		int defaultTimeoutValue = 20000;
 		
 		ThreadGroup tg = new ThreadGroup("Meteor Data Providers ThreadGroup");
 		
 		Resource res = ResourceFactory.createResource("accessprovider.properties");
 		
 		// Get the timeout value with the default of 20 seonds
 		String strTimeout = res.getProperty("dataprovider.timeout");
 		int timeout = defaultTimeoutValue;
 		
 		try{
 			timeout = Integer.parseInt(strTimeout);
 		} catch(NumberFormatException e){
 			// If this throws an exception because the number
 			// wasn't correct, then rest it to the default
 			timeout = defaultTimeoutValue;
 		}
 		
 		Iterator i = providers.iterator();
 		
 		while(i.hasNext()){
 			DataProvider dp = (DataProvider) i.next();
 			DataProviderThread dpt = new DataProviderThread(tg, dp);
 			dpt.start();
 		}
 		
 		int elapsedTime = 0;
 		while(tg.activeCount() > 0 && elapsedTime < timeout){
 			try {
 				Thread.sleep (100);
 			}
 			catch (InterruptedException e) {
 				log.error("Sleep was Interrupted!!");
 				break;
 			}
 			elapsedTime += 100;
 			
 			if(elapsedTime >= timeout){
 				log.error("Threadgroup terminating with an active count of: " + tg.activeCount());
 				
 				Thread[] t = new Thread[tg.activeCount()];
 				tg.enumerate(t);
 				for(int j = 0; j < t.length; j++){
 					Thread thread = t[j];
 					if(thread == null) continue;
 					String name = thread.getName();
 					log.error("Thread " + (name == null ? "'Unknown'": name) + " is being forcefully interrupted");
 					thread.interrupt();
 				}
 				
 			}
 		}
 		
 	}
 	
 	private String createErrorXML(String severity, String message){
 		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 		             "<PESCXML:MeteorRsMsg xmlns:PESCXML=\"http://schemas.pescxml.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.pescxml.org Meteor_Schema_1-0-0.xsd\" PESCXMLVersion=\"1.0.0\">\n" +
 		             "   <MeteorDataProviderInfo>\n" +
 		             "      <MeteorDataProviderDetailInfo>\n" +
 		             "         <DataProviderType>G</DataProviderType>\n" +
 		             "         <DataProviderData>\n" +
 		             "            <Contacts/>\n" +
 		             "         </DataProviderData>\n" +
 		             "         <DataProviderAggregateTotal/>\n" + 
 		             "      </MeteorDataProviderDetailInfo>\n" +
 		             "      <MeteorDataProviderMsg>\n" +
 		             "         <RsMsg>" + message + "</RsMsg>\n" +
 		             "         <RsMsgLevel>" + severity + "</RsMsgLevel>\n" + 
 		             "      </MeteorDataProviderMsg>\n" +
 		             "   </MeteorDataProviderInfo>\n" +
 		             "</PESCXML:MeteorRsMsg>";
 
 		return xml;
 	}	
 }
