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
  * Technologies, Inc. (PTI). This version of the code is for preliminary
  * testing purposes only, and may not be used for any other purpose 
  * except to test and validate the Meteor design and implementation. 
  * Please direct inquiries to NCHELP at 1100 Connecticut Avenue,
  * NW; Washington, DC 20036; (202) 822-2106.
  */
 
 package org.nchelp.meteor.provider.access;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.ValidationException;
 import org.nchelp.hpc.util.exception.ParsingException;
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
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.registry.DistributedRegistry;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Cache;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.IndexException;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 public class IndexQueryService {
 
 	private final Logger log = Logger.create(this.getClass());
 
 //	private static Cache meteorDataCache = new Cache();
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
 	public String query(MeteorParameters params){
 		// check dataProviderCache
 		// if no providers in cache should 
 		// look for index providers in index provider cache
 		// if not found should look it up and put it in cache 
 
 		List dProviders = null;
 		List iProviders = null;
 		MeteorDataResponse mdr = null;
 		
 		int awardID = 1;
 		
 /*		// Check to see if this is a subsequent request for data that we've 
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
 */
 
 		
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		SecurityToken token = registry.getAuthentication();
 		try{
 			token.setRole(params.getRole());
 		} catch(ParameterException e){
 			log.error("Invalid Role Defined", e);	
 		}
 
 		dProviders = (List)dProviderCache.cached(params.getSsn());
 
 		if (dProviders == null ){
 			dProviders = new ArrayList();
 		}
 		
 		if (dProviders.isEmpty()) {
 			iProviders = this.getIndexProviderList(registry);
 
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
 		
 		// Now interate through each of these and make the real call
 		Iterator iterator = dProviders.iterator();
 		DataProvider dProvider = null;
 		
 		
 		while(iterator.hasNext()){
 			dProvider = (DataProvider) iterator.next();
 			
 			MeteorDataResponse respTemp = null;
 			
 			try{
 				respTemp = dProvider.getData(token, params);
 				
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
 						
 						
 						// If there is anything to doto that document before 
 						// passing it on to the aggregatedData object, 
 						// do it in the fixDocument method
 						this.fixDocument(respTemp, mdpi, awdDoc);
 						
 						
 						// Now add that document to the Aggregated
 						aggregatedData.add(awdDoc);
 					}
 				}
 			} catch(DataException e){
 				log.error(e);
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
 			
 			/* Each award was assigned a unique ID
 			 * Loop through all of the duplicates 
 			 * and remove them from the meteor message
 			 */
 			
 			for(int i = 0; i < awards.length; i++){
 				Object[] dupes = aggregatedData.getDuplicates(awards[i].hashCode());	
 				
 				for(int j = 0; j < dupes.length; j++){
 					log.debug("Removing duplicate award with hashcode " + dupes[j].hashCode());	
 				}
 				
 			}
 			
 			
 			
 		}
 		
 		mdr.createMinimalResponse();
 		
 		String xml = mdr.toString();
 		
 		
 		
 		// Massive hack here!!!
 		// According to the XSLT 1.0 spec, a document
 		// cannot have a default namespace other than
 		// the one normally defined for xslt.
 		// So, until version 2.0 of the XSLT spec is 
 		// published and incorporated into Xalan,
 		// We have to strip out the default namespace
 		xml = this.removeDefaultNamespace(xml);
 
 /*		// Save it off for subsequent queries		
 		meteorDataCache.add(params.getSsn(), xml);
 */		
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
 			iProviderCache.add("", iProviders);
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
 
 	/**
 	 * This method should be called before the Award DOM Document is passed
 	 * to the AggregatedLoanData.  If there are any specific things that 
 	 * must be added to the award so that everything can be computed correctly
 	 * then put the logic here
 	 * 
 	 * 
 	 * @param resp  MeteorDataResponse returned from the call to the Data Provider
 	 * @param doc   DOM Document object that represents the Award tag as the root element
 	 */
 	private void fixDocument(MeteorDataResponse resp, MeteorDataProviderInfo mdpi, Document doc){
 		
 		/* 
 		 * Need to take the Default data and put it into
 		 * each of the Award DOM trees so we can use this 
 		 * in the rules
 		 */
 		
 		MeteorDataProviderAwardDetails mdpad = mdpi.getMeteorDataProviderAwardDetails();		
 		
 		if(mdpad == null) return;
 		
 		
 		/*  I don't know what to do with anything
 		 * other than the first occurrance
 		 */
 		Default[] def = mdpad.getDefault();
 		
 		// If there aren't any then don't need to do anything
 		if(def.length == 0) return;
 		
 		Document defDoc = this.marshallObject(def);
 		
 		Node root = doc.getDocumentElement();
 		
 		Node newNode = doc.importNode(defDoc, true);
 		
 		root.appendChild(newNode);
 
 		log.debug("After appended stuff: " + XMLParser.XMLToString(doc));
 		
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
