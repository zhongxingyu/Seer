 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.diagnosiscapturerwanda;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.APIException;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.ModuleActivator;
 import org.openmrs.util.PrivilegeConstants;
 import org.springframework.context.ApplicationContext;
 
 /**
  * This class contains the logic that is run every time this module is either started or stopped.
  */
 public class DiagnosisCaptureRwandaActivator implements ModuleActivator, Runnable {
 	
 	protected Log log = LogFactory.getLog(getClass());
 		
 	/**
 	 * @see ModuleActivator#willRefreshContext()
 	 */
 	public void willRefreshContext() {
 		log.info("Refreshing diagnosiscapturerwanda Module");
 	}
 	
 	/**
 	 * @see ModuleActivator#contextRefreshed()
 	 */
 	public void contextRefreshed() {
 		log.info("diagnosiscapturerwanda Module refreshed");
 	}
 	
 	/**
 	 * @see ModuleActivator#willStart()
 	 */
 	public void willStart() {
 		log.info("Starting diagnosiscapturerwanda Module");
 	}
 	
 	/**
 	 * @see ModuleActivator#started()
 	 */
 	public void started() {
 		log.info("diagnosiscapturerwanda Module started");
 		Thread contextChecker = new Thread(this);
         contextChecker.start();
 	}
 	
 	/**
 	 * @see ModuleActivator#willStop()
 	 */
 	public void willStop() {
 		log.info("Stopping diagnosiscapturerwanda Module");
 	}
 	
 	/**
 	 * @see ModuleActivator#stopped()
 	 */
 	public void stopped() {
 		log.info("diagnosiscapturerwanda Module stopped");
 	}
 	
 	
 	 public final void run() {
 	        // Wait for context refresh to finish
 
 	        ApplicationContext ac = null;
 	        ConceptService cs = null;
 	        EncounterService es = null;
 	        try {
 	            while (ac == null || cs == null || es == null) {
 	                Thread.sleep(5000);
 	                if (DiagnosisCaptureRwandaContextAware.getApplicationContext() != null){
 	                    try{
 	                        log.info("DiagnosisCaptureRwanda still waiting for app context and services to load...");
 	                        ac = DiagnosisCaptureRwandaContextAware.getApplicationContext();
 	                        cs = Context.getConceptService();
 	                        es = Context.getEncounterService();
 	                        
 	                    } catch (APIException apiEx){
 	                    	log.error(apiEx);
 	                    }
 	                }
 	            }
 	        } catch (InterruptedException ex) {
 	        	log.error(ex);
 	        }
 	        try {
 	            Thread.sleep(10000);
 	            // Start new OpenMRS session on this thread
 	            Context.openSession();
 	            Context.addProxyPrivilege("View Concept Classes");
 	            Context.addProxyPrivilege("View Concepts");
 	            Context.addProxyPrivilege("Manage Concepts");
 	            Context.addProxyPrivilege("View Global Properties");
 	            Context.addProxyPrivilege("Manage Global Properties");
 	            Context.addProxyPrivilege("SQL Level Access");
 	            Context.addProxyPrivilege(PrivilegeConstants.VIEW_ENCOUNTER_TYPES);
 	            Context.addProxyPrivilege(PrivilegeConstants.VIEW_IDENTIFIER_TYPES);
 	            MetadataDictionary.getInstance();
 	        } catch (Exception ex) {
 	            log.error(ex);
 	            throw new RuntimeException("Could not pre-load concepts " + ex);
 	        } finally {
 	        	//test
 	        	//System.out.println("HERE " + MetadataDictionary.CONCEPT_DIAGNOSIS_ORDER.getName(new Locale("en")));
 	            Context.removeProxyPrivilege("SQL Level Access");
 	            Context.removeProxyPrivilege("View Concept Classes");
 	            Context.removeProxyPrivilege("View Concepts");
 	            Context.removeProxyPrivilege("Manage Concepts");
 	            Context.removeProxyPrivilege("View Global Properties");
 	            Context.removeProxyPrivilege("Manage Global Properties");
 	            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_ENCOUNTER_TYPES);
 	            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_IDENTIFIER_TYPES);
 	            Context.closeSession();
 	            
 	            log.info("Finished loading DiagnosisCaptureRwanda metadata.");
 	        }   
 	    }
 		
 }
