 /**
  * Copyright (c) 2010 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the MIT/X11 license. The text of the license can be
  * found at http://www.opensource.org/licenses/mit-license.php. 
  */
 
 package org.extensiblecatalog.ncip.v2.aleph;
 
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.extensiblecatalog.ncip.v2.service.AgencyId;
 import org.extensiblecatalog.ncip.v2.service.ItemOptionalFields;
 import org.extensiblecatalog.ncip.v2.service.ItemTransaction;
 import org.extensiblecatalog.ncip.v2.service.LookupItemInitiationData;
 import org.extensiblecatalog.ncip.v2.service.LookupItemResponseData;
 import org.extensiblecatalog.ncip.v2.service.LookupItemService;
 import org.extensiblecatalog.ncip.v2.service.Problem;
 import org.extensiblecatalog.ncip.v2.service.ProblemType;
 import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.service.ServiceContext;
 import org.extensiblecatalog.ncip.v2.service.ServiceError;
 import org.extensiblecatalog.ncip.v2.service.ServiceException;
 import org.extensiblecatalog.ncip.v2.service.UserIdentifierType;
 import org.extensiblecatalog.ncip.v2.service.CurrentBorrower;
 import org.extensiblecatalog.ncip.v2.service.CurrentRequester;
 import org.extensiblecatalog.ncip.v2.service.UserId;
 import org.extensiblecatalog.ncip.v2.aleph.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephUtil;
 import org.extensiblecatalog.ncip.v2.service.RequestId;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.user.AlephUser;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.AlephException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.GregorianCalendar;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 /**
  * This class implements the Lookup Item service for the Aleph back-end connector. Basically this just
  * calls the AlephRemoteServiceManager to get hard-coded data (e.g. title, call #, etc.).
  * <p/>
  * Note: If you're looking for a model of how to code your own ILS's NCIPService classes, do not
  * use this class as an example. See the NCIP toolkit Connector developer's documentation for guidance.
  */
 public class AlephLookupItemService implements LookupItemService {
 
     static Logger log = Logger.getLogger(AlephLookupItemService.class);
 
     /**
      * Construct a AlephRemoteServiceManager; this class is not configurable so there are no parameters.
      */
     public AlephLookupItemService() {
     }
 
     /**
      * Handles a NCIP LookupItem service by returning data from Aleph.
      *
      * @param initData       the LookupItemInitiationData
      * @param serviceManager provides access to remote services
      * @return LookupItemResponseData
      */
     @Override
 	public LookupItemResponseData performService(LookupItemInitiationData initData, ServiceContext serviceContext,
 			RemoteServiceManager serviceManager) throws ServiceException {
     	log.info("AlephLookupItemService.performService called");
 		LookupItemResponseData responseData = new LookupItemResponseData();
 		AlephRemoteServiceManager alephRemoteServiceManager = (AlephRemoteServiceManager) serviceManager;
 
 		boolean getBibDescription = initData
 				.getBibliographicDescriptionDesired();
 		boolean getCircStatus = initData.getCirculationStatusDesired();
 		boolean getElectronicResource = initData.getElectronicResourceDesired();
 		boolean getHoldQueueLength = initData.getHoldQueueLengthDesired();
 		boolean getItemDescription = initData.getItemDescriptionDesired();
 		boolean getLocation = initData.getLocationDesired();
 		boolean getCurrentBorrowers = initData.getCurrentBorrowerDesired();
 		boolean getCurrentRequesters = initData.getCurrentRequestersDesired();
 
 		if (alephRemoteServiceManager.getXServerName() == null || alephRemoteServiceManager.getXServerPort() == null) {
 		    throw new ServiceException(ServiceError.CONFIGURATION_ERROR,"Aleph X-Server name and/or port not set");
 		}
 		
 		if (initData.getItemId() == null) {
 		    throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST,"Item id is undefined.");
 		}
 		AlephItem alephItem = null;
                 // Execute request if agency Id is blank or NRU
 		if (initData.getItemId().getAgencyId() != null 
 		     && !initData.getItemId().getAgencyId().getValue().equalsIgnoreCase("") 
 		     && alephRemoteServiceManager.getAlephAgency(initData.getItemId().getAgencyId().getValue()) == null) {
 		    throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST,
 					       "This request cannot be processed. Agency ID is invalid or not found.");
 		}
 		
 		try {
 		    // TODO, get adm and bib from agency id...adm id translates to item
 		    // id in ncip item
 		    alephItem = null;
 		    // get bib library and adm library...just set to ND for now
 		    
 		    boolean getBibInformation = getBibDescription || getCircStatus
 			|| getElectronicResource || getItemDescription
 			|| getLocation;
 		    alephItem = alephRemoteServiceManager.lookupItemByItemId(initData.getItemId().getItemIdentifierValue(), 
									     "MZK", 
 									     getBibInformation, getHoldQueueLength, getCurrentBorrowers,
 									     getCurrentRequesters, getCircStatus);
 		    
 		    // update NCIP response data with aleph item data
 		    updateResponseData(initData, responseData, alephItem);
 		} catch (IOException ie) {
 		    Problem p = new Problem();
 		    p.setProblemType(new ProblemType("Procesing error"));
 		    p.setProblemDetail(ie.getMessage());
 		    List<Problem> problems = new ArrayList<Problem>();
 		    problems.add(p);
 		    responseData.setProblems(problems);
 		} catch (ParserConfigurationException pce) {
 		    Problem p = new Problem();
 		    p.setProblemType(new ProblemType("Procesing error"));
 		    p.setProblemDetail(pce.getMessage());
 		    List<Problem> problems = new ArrayList<Problem>();
 		    problems.add(p);
 		    responseData.setProblems(problems);
 		} catch (SAXException se) {
 		    Problem p = new Problem();
 		    p.setProblemType(new ProblemType("Procesing error"));
 		    p.setProblemDetail(se.getMessage());
 		    List<Problem> problems = new ArrayList<Problem>();
 		    problems.add(p);
 		    responseData.setProblems(problems);
 		} catch (AlephException ae) {
 		    Problem p = new Problem();
 		    p.setProblemType(new ProblemType("Procesing error"));
 		    p.setProblemDetail(ae.getMessage());
 		    List<Problem> problems = new ArrayList<Problem>();
 		    problems.add(p);
 		    responseData.setProblems(problems);
 		} catch (Exception e) {
 		    Problem p = new Problem();
 		    p.setProblemType(new ProblemType("Procesing error"));
 		    p.setProblemDetail(e.getMessage());
 		    List<Problem> problems = new ArrayList<Problem>();
 		    problems.add(p);
 		    responseData.setProblems(problems);
 		}
 
 		if (alephItem == null) {
 		    Problem p = new Problem();
 		    p.setProblemType(new ProblemType("Procesing error"));
 		    p.setProblemDetail("Unknown item for Item Id: "+initData.getItemId());
 		    List<Problem> problems = new ArrayList<Problem>();
 		    problems.add(p);
 		    responseData.setProblems(problems);
 		}
 
 		return responseData;
     }
 
     protected void updateResponseData(LookupItemInitiationData initData, LookupItemResponseData responseData, AlephItem alephItem) throws ServiceException {
    	if (responseData!=null&&alephItem!=null&&alephItem.getItemId().indexOf(initData.getItemId().getItemIdentifierValue()) != -1){
     		
     		if (alephItem.getDateAvailablePickup()!=null){
     			GregorianCalendar gc = new GregorianCalendar();
     			gc.setTime(alephItem.getDateAvailablePickup());
     			responseData.setHoldPickupDate(gc);
     		}
     		responseData.setItemId(initData.getItemId());
     		ItemOptionalFields iof = AlephUtil.getItemOptionalFields(alephItem);
     		if (initData.getBibliographicDescriptionDesired()){
     			if (iof == null) iof = new ItemOptionalFields();
     			iof.setBibliographicDescription(AlephUtil.getBibliographicDescription(alephItem,initData.getItemId().getAgencyId()));
     		}
 
     		
     		if (iof != null) responseData.setItemOptionalFields(iof);
     		ItemTransaction itemTransaction = null;
     		if (alephItem.getBorrowingUsers()!=null&&alephItem.getBorrowingUsers().size()>0){
     			if (itemTransaction==null) itemTransaction = new ItemTransaction();
     			CurrentBorrower borrower = new CurrentBorrower();
     			UserId userId = new UserId();
     			AlephUser alephUser = alephItem.getBorrowingUsers().get(0);
     	        userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
     	        userId.setUserIdentifierType(new UserIdentifierType("Username"));
     	        userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
     			borrower.setUserId(userId);
     			itemTransaction.setCurrentBorrower(borrower);
     		}
     		
     		if (alephItem.getRequestingUsers()!=null&&alephItem.getRequestingUsers().size()>0){
     			if (itemTransaction==null) itemTransaction = new ItemTransaction();
     			Iterator<AlephUser> i = alephItem.getRequestingUsers().iterator();
     			while (i.hasNext()){
     				AlephUser alephUser = i.next();
     				CurrentRequester requester = new CurrentRequester();
     				UserId userId = new UserId();
         	        userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
         	        userId.setUserIdentifierType(new UserIdentifierType("Username"));
         	        userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
         			requester.setUserId(userId);
         			List<CurrentRequester> requesters = itemTransaction.getCurrentRequesters();
         			if (requesters == null) requesters = new ArrayList<CurrentRequester>();
         			requesters.add(requester);
         			itemTransaction.setCurrentRequesters(requesters);
     			}
     			
     		}
 
     		if (alephItem.getHoldRequestId()!=null){
     			RequestId requestId = new RequestId();
     			requestId.setRequestIdentifierValue(alephItem.getHoldRequestId());
     			responseData.setRequestId(requestId);
     		}
     	}
     }
 
 }
