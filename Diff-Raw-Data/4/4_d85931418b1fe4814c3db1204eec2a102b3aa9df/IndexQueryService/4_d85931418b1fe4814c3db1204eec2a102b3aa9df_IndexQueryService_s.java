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
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.xml.security.utils.XMLUtils;
 import org.apache.xpath.XPathAPI;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.ValidationException;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.nchelp.meteor.aggregation.AggregatedLoanData;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.MeteorIndexResponse;
 import org.nchelp.meteor.message.response.Award;
 import org.nchelp.meteor.message.response.Default;
 import org.nchelp.meteor.message.response.MeteorDataProviderAwardDetails;
 import org.nchelp.meteor.message.response.MeteorDataProviderInfo;
 import org.nchelp.meteor.message.response.MeteorDataProviderMsg;
 import org.nchelp.meteor.message.response.MeteorRsMsg;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.DataProviderThread;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.registry.DistributedRegistry;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Cache;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.AuthenticationLevelException;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.IndexException;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.SignatureException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
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
 	 * that represents the summary information
 	 * @param ssn Social Security Number
 	 * @param dob Date Of Birth for this SSN
 	 * @return String
 	 */
 	public String query(MeteorParameters params) throws AuthenticationLevelException{
 		// check dataProviderCache
 		// if no providers in cache should 
 		// look for index providers in index provider cache
 		// if not found should look it up and put it in cache 
 
 		List dProviders = null;
 		List iProviders = null;
 		MeteorDataResponse mdr = null;
 		
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
 		SecurityToken token = null;
 		
 		// If a SecurityToken was passed in on the parameters
 		// then the user went through an authentication bump
 		
 		token = params.getSecurityToken();
 		
 		if(token == null){
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
 				log.error("Signature Exception getting a Security Token from the registry: " + e.getMessage());
 				e.printStackTrace();
 				
 				// There's really no point in going on here
 				mdr = new MeteorDataResponse();
 				mdr.createMinimalResponse();
 				mdr.setError("E", "Error connecting to Meteor Registry");
 				String resp = mdr.toString();
 				resp = this.removeDefaultNamespace(resp);
 				return resp;
 			}
 		} else {  // Token is passed as a parameter
 			log.debug("Using the security token passed as a parameter");
 			if(! registry.authenticateProvider(token)) {
 				// There's really no point in going on here
 				mdr = new MeteorDataResponse();
 				mdr.createMinimalResponse();
 				mdr.setError("E", "Invalid Security Token passed to Access Provider");
 				String resp = mdr.toString();
 				resp = this.removeDefaultNamespace(resp);
 				return resp;
 			}
 		}
 		
 		dProviders = (List)dProviderCache.cached(params.getSsn());
 
 		if (dProviders == null ){
 			dProviders = new ArrayList();
 		}
 		
 		if (dProviders.isEmpty()) {
 			iProviders = this.getIndexProviderList(registry);
 			if(iProviders == null){ iProviders = new ArrayList(); }
 			
 			// now request a list of data providers from each index provider.
 			Iterator iterator = iProviders.iterator();
 			IndexProvider iProvider = null;
 
 			while (iterator.hasNext()) {
 				
 				iProvider = (IndexProvider) iterator.next();
 				MeteorIndexResponse ipResp = null;
 				try{
 					log.assert(iProvider != null, "Ack! the IndexProvider object is null");
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
 			MeteorDataResponse respTemp = null;
 			
 			respTemp = dProvider.getResponse();
 			
 			if(respTemp == null){ continue; }
 			/* Loop through each Award type and set an ID
 			 */
 			MeteorRsMsg msg = respTemp.getRsMsg();
 			
 			int count = respTemp.getRsMsg().getMeteorDataProviderInfoCount();
 			for(int i = 0; i < count; i++){
 				MeteorDataProviderInfo mdpi = msg.getMeteorDataProviderInfo(i);
 				MeteorDataProviderAwardDetails mdpad = mdpi.getMeteorDataProviderAwardDetails();
 				if(mdpad == null) continue;
 				
 				Award[] awards = mdpad.getAward();
 				
 				for(int j=0; j < awards.length; j++){
 					awards[j].setAPSUniqueAwardID(BigDecimal.valueOf(awardID));
 					awardID++;
 					
 					Document awdDoc = null;
 
 					awdDoc = this.marshallObject(awards[j]);
 					
 					// Now add that document to the Aggregated
 					aggregatedData.add(awdDoc);
 				}
 			}
 			
 			
 			if(mdr == null){
 				mdr = respTemp;
 			} else {
 				MeteorRsMsg msgTemp = respTemp.getRsMsg();
 				MeteorRsMsg msgFinal = mdr.getRsMsg();
 				
 				for(int i=0; i < msgTemp.getMeteorDataProviderInfoCount(); i++){
 					msgFinal.addMeteorDataProviderInfo(msgTemp.getMeteorDataProviderInfo(i));
 				}
 			}
 		}
 			
 		
 		if(mdr == null){
 			mdr = new MeteorDataResponse();
 			MeteorRsMsg msg = mdr.getRsMsg();
 			MeteorDataProviderInfo mdpi = null;
 			if(msg.getMeteorDataProviderInfoCount() == 0){
 				mdpi = new MeteorDataProviderInfo();
 				msg.addMeteorDataProviderInfo(mdpi);
 			} else {
 				mdpi = msg.getMeteorDataProviderInfo(0);
 			}
 			MeteorDataProviderMsg mde = new MeteorDataProviderMsg();
 			mde.setRsMsg("No Data Found");
 			mde.setRsMsgLevel("I");
 			mdpi.addMeteorDataProviderMsg(mde);
 		} else {
 			// Somehow figure out which possible awards are
 			// duplicates
 			Object[] awards = aggregatedData.getBest();
 			
 			
 			// Store off all of the Unique IDs into a map
 			Map map = new HashMap();
 			for(int i=0; i < awards.length; i++){
 				Document doc = (Document)awards[i];
 				String value = XMLParser.getNodeValue(doc, "APSUniqueAwardID");
 				map.put(value, doc);
 				log.debug("Award ID: " + value + " is in the 'best award' list");
 			}	
 			
 			// loop through all of the awards and remove the
 			// ones that are not in the map we just created
 			MeteorRsMsg msg = mdr.getRsMsg();
 			int infoCount = msg.getMeteorDataProviderInfoCount();
 			for(int i=0; i < infoCount; i++){
 				MeteorDataProviderInfo mdpi = msg.getMeteorDataProviderInfo(i)	;
 				
 				MeteorDataProviderAwardDetails mdpad = mdpi.getMeteorDataProviderAwardDetails();
 				int awardCount = mdpad.getAwardCount();
 				for(int j=0; j < awardCount; j++){
 					Award a = mdpad.getAward(j);
 					String id = a.getAPSUniqueAwardID().toString();
 					if(! map.containsKey(id)){
 						mdpad.removeAward(j);
 						log.debug("Removing Award ID: " + id);
 					}
 				}
 				
 			}
 			
 		}
 		
 		mdr.createMinimalResponse();
 		
 		String xml = mdr.toString();
 		
 		
 		log.debug("Final XML Returned to browser: " + xml);
 		// Massive hack here!!!
 		// According to the XSLT 1.0 spec, a document
 		// cannot have a default namespace other than
 		// the one normally defined for xslt.
 		// So, until version 2.0 of the XSLT spec is 
 		// published and incorporated into Xalan,
 		// We have to strip out the default namespace
 		xml = this.removeDefaultNamespace(xml);
 
 		// Save it off for subsequent queries		
 		meteorDataCache.add(params.getSsn(), xml);
 		
 		return xml;
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
 			dataProviders = new Vector();
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
 	
 	/**
 	 * Massive hack here!!!
 	 * According to the XSLT 1.0 spec, a document
 	 * cannot have a default namespace other than
 	 * the one normally defined for xslt.
 	 * So, until version 2.0 of the XSLT spec is 
 	 * published and incorporated into Xalan,
 	 * We have to strip out the default namespace
 	 * @param xml
 	 * @return String
 	 */
 	private String removeDefaultNamespace(String xml){
 		if(xml == null) return xml;
 		
 		String nameSpace = " xmlns=\"http://schemas.pescxml.org\"";
 		
 		int pos = xml.indexOf(nameSpace);
 		if(pos < 0) return xml;
 		
 		xml = xml.substring(0, pos) + 
 		      xml.substring(pos + nameSpace.length());
 		return xml;
 	}
 
 	
 	private Document marshallObject(Object obj){
 		
 		Document doc = null;
 						
 		try{
 			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		} catch(ParserConfigurationException e){
 			log.warn("Error Creating new Document", e);
 		}
 		
 		// Now Marshall that Award object into a Document
 		try{
 			Marshaller m = new Marshaller(doc);
 			//m.setValidation(false);
 			m.marshal(obj);
 		} catch(MarshalException e){
 			log.warn("Error Marshalling Award Object", e);
 		} catch(ValidationException e){
 			log.warn("Error Validating Award Object", e);
 		}
 		
 		return doc;
 		
 	}
 }
