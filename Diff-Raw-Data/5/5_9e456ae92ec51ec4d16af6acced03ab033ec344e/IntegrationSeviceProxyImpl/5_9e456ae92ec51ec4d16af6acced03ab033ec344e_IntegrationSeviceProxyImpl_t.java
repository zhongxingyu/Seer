 /*
  * Copyright 2007 EDL FOUNDATION
  *
  * Licensed under the EUPL, Version 1.1 or - as soon they
  * will be approved by the European Commission - subsequent
  * versions of the EUPL (the "Licence");
  * you may not use this work except in compliance with the
  * Licence.
  * You may obtain a copy of the Licence at:
  *
  * http://ec.europa.eu/idabc/eupl
  *
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the Licence is
  * distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied.
  * See the Licence for the specific language governing
  * permissions and limitations under the Licence.
  */
 package eu.europeana.uim.gui.cp.server;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import org.joda.time.DateTime;
 import com.google.gwt.user.client.ui.DialogBox;
 import eu.europeana.uim.storage.StorageEngine;
 import eu.europeana.uim.storage.StorageEngineException;
 import eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxy;
 import eu.europeana.uim.gui.cp.client.utils.EuropeanaClientConstants;
 import eu.europeana.uim.gui.cp.client.utils.RepoxOperationType;
 import eu.europeana.uim.gui.cp.server.engine.ExpandedOsgiEngine;
 import eu.europeana.uim.gui.cp.shared.HarvestingStatusDTO;
 import eu.europeana.uim.gui.cp.shared.HarvestingStatusDTO.STATUS;
 import eu.europeana.uim.gui.cp.shared.ImportResultDTO;
 import eu.europeana.uim.gui.cp.shared.IntegrationStatusDTO;
 import eu.europeana.uim.gui.cp.shared.IntegrationStatusDTO.TYPE;
 import eu.europeana.uim.gui.cp.shared.RepoxExecutionStatusDTO;
 import eu.europeana.uim.gui.cp.shared.SugarCRMRecordDTO;
 import eu.europeana.uim.repox.AggregatorOperationException;
 import eu.europeana.uim.repox.DataSourceOperationException;
 import eu.europeana.uim.repox.HarvestingOperationException;
 import eu.europeana.uim.repox.ProviderOperationException;
 import eu.europeana.uim.repox.RepoxUIMService;
 import eu.europeana.uim.repox.model.HarvestingState;
 import eu.europeana.uim.repox.model.RepoxHarvestingStatus;
 import eu.europeana.uim.repox.model.ScheduleInfo;
 import eu.europeana.uim.repoxclient.jibxbindings.Success;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.sugar.LoginFailureException;
 import eu.europeana.uim.sugar.SugarCrmService;
 import eu.europeana.uim.sugar.SugarCrmRecord;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaDatasetStates;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaRetrievableField;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaUpdatableField;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.CustomSugarCrmQuery;
 import eu.europeana.uim.sugar.QueryResultException;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 /**
  * Exposes the "integration plugins" methods to the client.
  * 
  * @author Georgios Markakis
  */
 public class IntegrationSeviceProxyImpl extends
 		IntegrationServicesProviderServlet implements IntegrationSeviceProxy {
 
 	private static final long serialVersionUID = 1L;
 	private String repoxURL;
 	private String sugarCrmURL;	
 	DialogBox importDialog;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxy#
 	 * processSelectedRecord(eu.europeana.uim.gui.cp.shared.SugarCRMRecordDTO)
 	 */
 	@Override
 	public ImportResultDTO processSelectedRecord(SugarCRMRecordDTO record) {
 
 		ExpandedOsgiEngine engine = getEngine();
 		SugarCrmService sugService = engine.getSugarCrmService();
 		RepoxUIMService repoxService = engine.getRepoxService();
 		ImportResultDTO result = new ImportResultDTO();
 		result.setResult(EuropeanaClientConstants.SUCCESSIMAGELOC);		
 		result.setCollectionName(record.getName());
 
 		String id = record.getId();
 
 		try {
 
 			// Retrieve the original SugarCRM record for a collection entry
 			SugarCrmRecord originalRec = sugService.retrieveRecord(id);
 
 			// Create the UIM provider object by information provided by this
 			// record
 			Provider prov = sugService.updateProviderFromRecord(originalRec);
 
 			// Get the current coutry for the given provider
 			String aggrID = prov.getValue(ControlledVocabularyProxy.PROVIDERCOUNTRY).toLowerCase();
 
 			// Create a dummy aggregator that uses the country prefix as an ID
 			if (!repoxService.aggregatorExists(aggrID)) {
 				repoxService.createAggregator(aggrID,null);
 			}
 
 			// Create a REPOX provider from an already existing UIM provider
 			if (!repoxService.providerExists(prov)) {
 				repoxService.createProviderfromUIMObj(prov);
 			} else {
 				// Or update an already existing REPOX provider from an
 				// (updated) existing UIM provider
 				repoxService.updateProviderfromUIMObj(prov);
 			}
 
 			Collection coll = sugService.updateCollectionFromRecord(
 					originalRec, prov);
 
 			if (!repoxService.datasourceExists(coll)) {
 				repoxService.createDatasourcefromUIMObj(coll, prov);
 				result.setDescription("Datasource Created Successfully.");
 			} else {
 				repoxService.updateDatasourcefromUIMObj(coll);
 				result.setDescription("Datasource Updated Successfully.");
 			}
 
 		} catch (QueryResultException e) {
 			result.setDescription("Import failed while accessing SugarCRM.");
 			result.setCause(e.getMessage());
 			result.setResult(EuropeanaClientConstants.ERRORIMAGELOC);
 		} catch (StorageEngineException e) {
 			result.setDescription("Import failed while storing in UIM.");
 			result.setCause(e.getMessage());
 			result.setResult(EuropeanaClientConstants.ERRORIMAGELOC);
 		} catch (AggregatorOperationException e) {
 			result.setDescription("Import failed while creating an Aggregator in Repox.");
 			result.setCause(e.getMessage());
 			result.setResult(EuropeanaClientConstants.ERRORIMAGELOC);
 		} catch (ProviderOperationException e) {
 			result.setDescription("Import failed while creating a Provider in Repox.");
 			result.setCause(e.getMessage());
 			result.setResult(EuropeanaClientConstants.ERRORIMAGELOC);
 		} catch (DataSourceOperationException e) {
 			result.setDescription("Import failed while creating a DataSource in Repox.");
 			result.setCause(e.getMessage());
 			result.setResult(EuropeanaClientConstants.ERRORIMAGELOC);
 		} 
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxy#
 	 * executeSugarCRMQuery(java.lang.String)
 	 */
 	@Override
 	public List<SugarCRMRecordDTO> executeSugarCRMQuery(String query) {
 
 		ArrayList<SugarCRMRecordDTO> guiobjs = new ArrayList<SugarCRMRecordDTO>();
 
 		ExpandedOsgiEngine engine = getEngine();
 		SugarCrmService sugService = engine.getSugarCrmService();
 
 		CustomSugarCrmQuery queryObj = new CustomSugarCrmQuery(query);
 		queryObj.setMaxResults(10000000);
 		queryObj.setOffset(0);
 		queryObj.setOrderBy(EuropeanaRetrievableField.ID);
 
 		try {
 			sugService.updateSession(null,null);
 			
 			ArrayList<SugarCrmRecord> results = (ArrayList<SugarCrmRecord>) sugService
 					.retrieveRecords(queryObj);
 			guiobjs = (ArrayList<SugarCRMRecordDTO>) convertSugarObj2GuiObj(results);
 			results = null;
 		} catch (QueryResultException e) {
 			e.printStackTrace();
 		} catch (StorageEngineException e) {
 			e.printStackTrace();
 		} catch (LoginFailureException e) {
 
 		}
 
 		return guiobjs;
 	}
 
 	
 	
 	
 	/**
 	 * Converts a SugarCRM object (query result) into an object suitable for GWT
 	 * visualization purposes.
 	 * 
 	 * @param toconvert
 	 *            a SugarCRM query result object
 	 * @return a GWT object
 	 * @throws StorageEngineException
 	 */	
 	private List<SugarCRMRecordDTO> convertSugarObj2GuiObj(
 			ArrayList<SugarCrmRecord> toconvert) throws StorageEngineException {
 		ArrayList<SugarCRMRecordDTO> converted = new ArrayList<SugarCRMRecordDTO>();
 
 		ExpandedOsgiEngine engine = getEngine();
 
 		StorageEngine<?> resengine = engine.getRegistry().getStorageEngine();
 
 		for (SugarCrmRecord originalrecord : toconvert) {
 			SugarCRMRecordDTO guirecord = new SugarCRMRecordDTO();
 
 			
 			Collection colexists = resengine
 					.findCollection(originalrecord.getItemValue(
 							EuropeanaRetrievableField.NAME).split("_")[0]);
 
 			if (colexists == null) {
 				guirecord.setImportedIMG(EuropeanaClientConstants.ERRORIMAGELOC);
 			} else {
 				
 				String sugid =colexists.getValue(ControlledVocabularyProxy.SUGARCRMID);
 				String repoxid = colexists.getValue(ControlledVocabularyProxy.REPOXID);
 				if(sugid == null || repoxid == null){
 					guirecord.setImportedIMG(EuropeanaClientConstants.PROBLEMIMAGELOC);
 				}
 				else{
 					guirecord.setImportedIMG(EuropeanaClientConstants.SUCCESSIMAGELOC);
 				}
 
 			}
 
 			guirecord.setId(originalrecord
 					.getItemValue(EuropeanaRetrievableField.ID));
 
 			guirecord
 					.setAssigned_user_name(originalrecord
 							.getItemValue(EuropeanaRetrievableField.ASSIGNED_USER_NAME));
 			guirecord.setCountry_c(originalrecord
 					.getItemValue(EuropeanaRetrievableField.COUNTRY));
 			
 			guirecord
 					.setExpected_ingestion_date(originalrecord
 							.getItemValue(EuropeanaRetrievableField.EXPECTED_INGESTION_DATE));
 			guirecord.setIngested_total_c(originalrecord
 					.getItemValue(EuropeanaUpdatableField.TOTAL_INGESTED));
 			guirecord.setName(originalrecord
 					.getItemValue(EuropeanaRetrievableField.NAME));
 			guirecord.setOrganization_name(originalrecord
 					.getItemValue(EuropeanaRetrievableField.ORGANIZATION_NAME));
 			
 			
 			//Display the proper dataset name state here			
 			guirecord.setStatus(translateStatus(originalrecord.getItemValue(EuropeanaUpdatableField.STATUS)));
 
 			converted.add(guirecord);
 		}
 
 		return converted;
 
 	}
 
 	
 	
 	
 	/**
 	 * Translates the status code returned by SugarCRM into a human readable form.
 	 * 
 	 * @param sugarcrmStatusStr
 	 * @return
 	 */
 	private String translateStatus(String sugarcrmStatusStr){
 		
 		if(sugarcrmStatusStr != null){
 		sugarcrmStatusStr = sugarcrmStatusStr.replace(" ","%");
 		EuropeanaDatasetStates actualvalue = null;
 		for(EuropeanaDatasetStates e : EuropeanaDatasetStates.values()){
 			if(e.getSysId().equals(sugarcrmStatusStr)){
 				actualvalue = e;
 			}
 		}
 		
 		if(actualvalue != null){
 			return actualvalue.getDescription();
 		}
 		else{
 			return "Unknown State";
 		}
 		}
 		else{
 			return "No State Defined";
 		}
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxy#
 	 * retrieveIntegrationInfo(java.lang.Long, java.lang.Long)
 	 */
 	@Override
 	public IntegrationStatusDTO retrieveIntegrationInfo(String provider,
 			String collection) {
 		ExpandedOsgiEngine engine = getEngine();
 		RepoxUIMService repoxService = engine.getRepoxService();
 		
 		if(repoxURL == null){
 		    repoxURL = repoxService.showConnectionStatus().getDefaultURI();
 		}
 		
 		if(sugarCrmURL == null){
 			SugarCrmService sugService = engine.getSugarCrmService();
 			sugarCrmURL = sugService.showConnectionStatus().getDefaultURI();
 		}
 		
 		StorageEngine<?> stengine = engine.getRegistry().getStorageEngine();
 		IntegrationStatusDTO ret = new IntegrationStatusDTO();
 
 		//In case that a Workflow is selected
 		if (provider == null && collection == null) {
 			ret.setType(TYPE.WORKFLOW);
 		} else {
 
 			//If a Provider is selected
 			if (provider != null && collection == null) {
 				try {
 					Provider<?> prov = stengine.findProvider(provider);
 					try {
 						String description =  URLDecoder.decode(prov.getValue(ControlledVocabularyProxy.PROVIDERDESCRIPTION), "UTF-8");
 						ret.setDescription(description);
 					} catch (UnsupportedEncodingException e) {
 						ret.setDescription("");
 					}
 
 					ret.setType(TYPE.PROVIDER);
 					ret.setId(provider);
 					ret.setInfo(prov.getName());
 					ret.setSugarCRMID(prov.getValue(ControlledVocabularyProxy.SUGARCRMID));
 					ret.setRepoxID(prov.getValue(ControlledVocabularyProxy.REPOXID));
 					ret.setMintID(prov.getValue(ControlledVocabularyProxy.MINTID));
 					
 					
 					if(prov.getValue(ControlledVocabularyProxy.SUGARCRMID) != null){
 						ret.setSugarURL(sugarCrmURL.split("/soap.php")[0] + "?module=Accounts&action=DetailView&record=" + prov.getValue(ControlledVocabularyProxy.SUGARCRMID));
 					}
 					
 					if(prov.getValue(ControlledVocabularyProxy.REPOXID) != null){
						ret.setRepoxURL(repoxURL.split("/rest")[0] + "/?locale=en#EDIT_DP?id=" + prov.getValue(ControlledVocabularyProxy.REPOXID) );
 					}	
 
 					ControlledVocabularyProxy[] values = ControlledVocabularyProxy.values();
 					
 					for(int i=0;i<values.length;i++){
 						String value = prov.getValue(values[i]);
 						if(value != null){
 							ret.getResourceProperties().put(values[i].toString(),value);	
 						}
 					}
 				} catch (StorageEngineException e) {
 
 					ret.setType(TYPE.UNIDENTIFIED);
 					return ret;
 				}
 				
 			// If a Collection is selected
 			} else if (provider != null && collection != null) {
 				try {
 
 					Collection<?> col = stengine.findCollection(collection);
 					ret.setType(TYPE.COLLECTION);
 					ret.setId(collection);
 
 					
 					try {
 						String description =  URLDecoder.decode(col.getValue(ControlledVocabularyProxy.DESCRIPTION), "UTF-8");
 						ret.setDescription(description);
 					} catch (UnsupportedEncodingException e) {
 					
 						ret.setDescription("");
 					}
 					
 					
 					ret.setState(translateStatus(col.getValue(ControlledVocabularyProxy.STATUS)));
 					
 					ret.setSugarCRMID(col.getValue(ControlledVocabularyProxy.SUGARCRMID));
 					ret.setRepoxID(col.getValue(ControlledVocabularyProxy.REPOXID));
 					ret.setMintID(col.getValue(ControlledVocabularyProxy.MINTID));
 					
 					ret.setInfo(col.getName());
 					ret.setHarvestingStatus(null);
 
 					
 					ControlledVocabularyProxy[] values = ControlledVocabularyProxy.values();
 					
 					for(int i=0;i<values.length;i++){
 						
 						String value = col.getValue(values[i]);
 						
 						if(value != null){
 							ret.getResourceProperties().put(values[i].toString(),value);
 							
 						}
 	
 					}
 					
 					
 					
 					if (col.getValue(ControlledVocabularyProxy.REPOXID) != null) {
 						try {
 							RepoxHarvestingStatus result = repoxService.getHarvestingStatus(col);
 
 							HarvestingState status = result.getStatus();
 
 
 							
 							HarvestingStatusDTO statusobj = new HarvestingStatusDTO();
 
 							switch(status){
 							case OK:
 								statusobj.setStatus(HarvestingStatusDTO.STATUS.OK);
 								break;
 							case CANCELED:
 								statusobj.setStatus(HarvestingStatusDTO.STATUS.CANCELLED);
 								break;
 							case ERROR:
 								statusobj.setStatus(HarvestingStatusDTO.STATUS.ERROR);
 								break;
 							case RUNNING:
 								statusobj.setStatus(HarvestingStatusDTO.STATUS.RUNNING);
 								break;
 							case undefined:
 								statusobj.setStatus(HarvestingStatusDTO.STATUS.UNDEFINED);
 								break;
 							case WARNING:
 								statusobj.setStatus(HarvestingStatusDTO.STATUS.WARNING);
 								break;
 							}
 							
 							statusobj.setPercentage(result.getPercentage());
 							statusobj.setNoRecords(result.getRecords());
 							statusobj.setTimeleft(result.getTimeLeft());
 							ret.setHarvestingStatus(statusobj);
 							//Scheduled Sessions
 							
 							HashSet<ScheduleInfo> scheduled =  (HashSet<ScheduleInfo>) repoxService.getScheduledHarvestingSessions(col);
 							
 							if(!scheduled.isEmpty()){
 								
 								for(ScheduleInfo info : scheduled){
 									//info.
 								}
 							}
 							
 						} catch (HarvestingOperationException e) {
 
 							HarvestingStatusDTO status = new HarvestingStatusDTO();
 
 							status.setStatus(HarvestingStatusDTO.STATUS.SYSTEM_ERROR);
 
 							ret.setHarvestingStatus(status);
 						}
 					}
 					
 					
 					if(col.getValue(ControlledVocabularyProxy.SUGARCRMID) != null){
 						ret.setSugarURL(sugarCrmURL.split("/soap.php")[0] + "?module=Opportunities&action=DetailView&record=" + col.getValue(ControlledVocabularyProxy.SUGARCRMID) );
 					}
 					
 					
 					if(col.getValue(ControlledVocabularyProxy.REPOXID) != null){
						ret.setRepoxURL(repoxURL.split("/rest")[0] + "/?locale=en#VIEW_DS?id=" + col.getValue("repoxID"));
 					}	
 					
 				} catch (StorageEngineException e) {
 
 					ret.setType(TYPE.UNIDENTIFIED);
 					return ret;
 				}
 			}
 		}
 
 
 		return ret;
 
 	}
 
 
 	
 
 	
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxy#
 	 * performRepoxRemoteOperation(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public RepoxExecutionStatusDTO performRepoxRemoteOperation(
 			RepoxOperationType operationType, String collectionID) {
 
 		ExpandedOsgiEngine engine = getEngine();
 		RepoxUIMService repoxService = engine.getRepoxService();
 		StorageEngine<?> stengine = engine.getRegistry().getStorageEngine();
 
 		RepoxExecutionStatusDTO result = new RepoxExecutionStatusDTO();
 		Collection<?> coll = null;
 
 		try {
 			coll = stengine.findCollection(collectionID);
 		} catch (StorageEngineException e) {
 
 			e.printStackTrace();
 
 			return result;
 		}
 
 		switch (operationType) {
 
 		case VIEW_HARVEST_LOG:
 			 try {
 				 String log = repoxService.getHarvestLog(coll);
 				 result.setOperationMessage("Successfully Fetched Latest Harvest Log for Current Collection"); 
 				 result.setLogMessage(log);
 			} catch (HarvestingOperationException e) {
 				
 				result.setOperationMessage("Error fetching Harvest Log!"); 
 				result.setLogMessage(e.getMessage());
 			}
 			break;
 
 		case INITIATE_COMPLETE_HARVESTING:
 			try {
 				repoxService.initiateHarvestingfromUIMObj(coll,true);
 				result.setOperationMessage("Successfully initiated FULL harvesting for " + coll.getName()); 
 				result.setLogMessage("Harvesting initiated.");
 			} catch (HarvestingOperationException e) {
 				result.setOperationMessage("Error initiating a harvesting process for the current collection!"); 
 				result.setLogMessage(e.getMessage());
 			}
 			
 			break;
 			
 			
 		case INITIATE_INCREMENTAL_HARVESTING:
 			try {
 				repoxService.initiateHarvestingfromUIMObj(coll,true);
 				result.setOperationMessage("Successfully initiated INCREMENTAL harvesting for " + coll.getName()); 
 				result.setLogMessage("Harvesting initiated.");
 			} catch (HarvestingOperationException e) {
 				result.setOperationMessage("Error initiating a harvesting process for the current collection!"); 
 				result.setLogMessage(e.getMessage());
 			}
 			
 			break;
 
 		case SCHEDULE_HARVESTING:
 
 			DateTime ingestionDate = new DateTime();
 
 			try {
 				ScheduleInfo info = new ScheduleInfo();
 				repoxService.scheduleHarvestingfromUIMObj(coll, info );
 				result.setOperationMessage("Successfully Performed Scheduling for given dataset "); 
 				result.setLogMessage("Harvesting Date: " + ingestionDate.toString());
 			} catch (HarvestingOperationException e) {
 				result.setOperationMessage("Scheduling failed for given dataset "); 
 				result.setLogMessage(e.getMessage());
 			}
 			break;
 
 		}
 
 
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxy#getSugarCrmURI()
 	 */
 	@Override
 	public String getSugarCrmURI() {
 		ExpandedOsgiEngine engine = getEngine();
 		SugarCrmService sugService = engine.getSugarCrmService();
 		
 		String retstring = sugService.showConnectionStatus().getDefaultURI().split("soap.php")[0];
 		return retstring;
 	}
 
 }
