 package com.varian.production.web.podplugin;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import com.sap.me.common.ObjectReference;
 import com.sap.me.datacollection.CollectDataAt;
 import com.sap.me.datacollection.DCGroupToCollectResponse;
 import com.sap.me.datacollection.DCGroupsToCollectResponse;
 import com.sap.me.datacollection.DCParameterResponse;
 import com.sap.me.datacollection.DataCollectionServiceInterface;
 import com.sap.me.datacollection.OperationRequest;
 import com.sap.me.datacollection.OperationSfc;
 import com.sap.me.demand.SFCBOHandle;
 import com.sap.me.extension.Services;
 import com.sap.me.frame.Data;
 import com.sap.me.frame.SystemBase;
 import com.sap.me.frame.domain.BusinessException;
 import com.sap.me.frame.jdbc.DynamicQuery;
 import com.sap.me.frame.jdbc.DynamicQueryFactory;
 import com.sap.me.plant.ResourceBOHandle;
 import com.sap.me.plant.ResourceKeyData;
 import com.sap.me.plant.WorkCenterBOHandle;
 import com.sap.me.productdefinition.AttachedToContext;
 import com.sap.me.productdefinition.AttachmentType;
 import com.sap.me.productdefinition.BOMConfigurationException;
 import com.sap.me.productdefinition.FindAttachmentByAttachedToContextRequest;
 import com.sap.me.productdefinition.FindAttachmentByAttachedToContextResponse;
 import com.sap.me.productdefinition.FoundReferencesResponse;
 import com.sap.me.productdefinition.OperationKeyData;
 import com.sap.me.productdefinition.WorkInstructionKeyData;
 import com.sap.me.production.podclient.BasePodPlugin;
 import com.sap.me.production.podclient.PodSelectionModelInterface;
 import com.sap.me.production.podclient.RefreshWorklistEvent;
 import com.sap.me.production.podclient.RefreshWorklistListenerInterface;
 import com.sap.me.production.podclient.SfcChangeEvent;
 import com.sap.me.production.podclient.SfcChangeListenerInterface;
 import com.sap.me.production.podclient.SfcSelection;
 import com.sap.me.wpmf.TableConfigurator;
 import com.sap.me.wpmf.util.FacesUtility;
 import com.sap.me.wpmf.util.MessageHandler;
 import com.varian.lsf.SfcStatusItem;
 import com.visiprise.frame.service.ext.SecurityContext;
 import com.sap.me.production.BuyoffStateEnum;
 import com.sap.me.production.FindBuyoffsBySfcData;
 import com.sap.me.production.FindBuyoffsBySfcRequest;
 import com.sap.me.production.FindBuyoffsBySfcResponse;
 import com.sap.me.production.GroupUnfilledAsBuiltComponentsRequest;
 import com.sap.me.production.InvalidSfcException;
 import com.sap.me.production.NoComponentsToAssembleException;
 import com.sap.me.production.SfcBasicData;
 import com.sap.me.production.SfcIdentifier;
 import com.sap.me.production.SfcStateServiceInterface;
 import com.sap.me.production.SfcStep;
 import com.sap.me.production.UnfilledComponent;
 import com.sap.me.production.UnfilledComponentsResponse;
 import com.sap.me.status.StatusBasicConfiguration;
 import com.sap.me.status.StatusServiceInterface;
 import com.sap.me.tooling.GetLoggedQuantityRequest;
 import com.sap.me.production.BuyoffServiceInterface;
 import com.sap.me.productdefinition.AttachmentConfigurationServiceInterface;
 import com.sap.me.tooling.ToolLogServiceInterface;
 import com.sap.me.production.RetrieveComponentsServiceInterface;
 import com.sap.me.productdefinition.WorkInstructionConfigurationServiceInterface;
 
 
 public class VisualStatusIndicatorPlugin extends BasePodPlugin implements SfcChangeListenerInterface,RefreshWorklistListenerInterface {
 
     private static final String WORK_INSTRUCTION_CONFIGURATION_SERVICE = "WorkInstructionConfigurationService";
 	private static final String RETRIEVE_COMPONENTS_SERVICE = "RetrieveComponentsService";
 	private static final String TOOL_LOG_SERVICE = "ToolLogService";
 	private static final String COM_SAP_ME_TOOLING = "com.sap.me.tooling";
 	private static final String ATTACHMENT_CONFIGURATION_SERVICE = "AttachmentConfigurationService";
 	private static final String BUYOFF_SERVICE = "BuyoffService";
 	private static final String COM_SAP_ME_STATUS = "com.sap.me.status";
 	private static final String STATUS_SERVICE = "StatusService";
 	private static final String SFC_STATE_SERVICE = "SfcStateService";
 	private static final String COM_SAP_ME_PRODUCTION = "com.sap.me.production";
 	private static final long serialVersionUID = -7968380049713345742L;
 	private static final String COM_SAP_ME_PRODUCTDEFINITION = "com.sap.me.productdefinition";
 	private static final String DATA_COLLECTION_SERVICE = "DataCollectionService";
 	private static final String COM_SAP_ME_DATACOLLECTION = "com.sap.me.datacollection";
 	private DataCollectionServiceInterface dataCollectionService;
 	private TableConfigurator tableConfigBean = null;
 	private List<SfcStatusItem> sfcStatusList;
     private String sfc =  null;
     private String sfcStatus =  null;
     private String startStatus = null;
     private String datacollection =  null;
     private String dcStatus = null;
     private String buyoffstate =  null;
     private String buyoffStatus = null;
     private String toolstate =  null;
     private String toolStatus = null;
     private String assemblystate =  null;
     private String assemblystatus =  null;
     private String workInststate = null;
 	private String workInststatus = null;
 	private String completeStatus = null;    
 	private String currResref = null;
     private String currOpRef= null;
     private SfcStateServiceInterface sfcStateService;
 	private StatusServiceInterface statusService;
 	private BuyoffServiceInterface buyoffService;
 	private AttachmentConfigurationServiceInterface attachmentConfigurationService;
 	private ToolLogServiceInterface toolLogService;
 	private RetrieveComponentsServiceInterface retrieveComponentsService;
 	private WorkInstructionConfigurationServiceInterface workInstructionConfigurationService;
 	String[] columnDefs = new String[] {};
 	String[] listColumnNames = new String[] {};
 	
 	@Override
 	public void beforeLoad() throws Exception {
 		TableConfigurator table = (TableConfigurator) FacesUtility.resolveExpression("#{sfcStatusTableBeanConfigurator}");
 		setTableConfigBean(table);
 
 	}
 	protected HashMap<String, String> getColumnFieldMaping() {
 		HashMap<String, String> columnFieldMap = new HashMap<String, String>();
 		String[] columns = columnDefs;
 		for (int i = 0; i < columns.length; i++) {
 			columnFieldMap.put(listColumnNames[i], columns[i]);
 		}
 		return columnFieldMap;
 	}
 	
 	public String getStartStatus() {
 		return startStatus;
 	}
 	public void setStartStatus(String startStatus) {
 		this.startStatus = startStatus;
 	}
 	public String getCompleteStatus() {
 		return completeStatus;
 	}
 	public void setCompleteStatus(String completeStatus) {
 		this.completeStatus = completeStatus;
 	}
 	public String getWorkInststatus() {
 		return workInststatus;
 	}
 
 
 	public void setWorkInststatus(String workInststatus) {
 		this.workInststatus = workInststatus;
 	}
 
 	public String getWorkInststate() {
 		return workInststate;
 	}
 
 	public String setWorkInststate (String sfcRef, String currentOpRef) throws BusinessException {
 		initServices();
 		String userRef = null;
 		int pendingCount = 0;
 		int workInsReadCount = 0;
 		SfcIdentifier sfcIdentifier = new SfcIdentifier(sfcRef);
 	    Collection<SfcStep> sfcStepColl = sfcStateService.findStepsSFCIsInWorkFor(sfcIdentifier);
 	    Iterator<SfcStep> sfcStepIterator = sfcStepColl.iterator();
 		while (sfcStepIterator.hasNext()){
 		SfcStep sfcStepdetail = (SfcStep) sfcStepIterator.next();
 		String inWorkOper = sfcStepdetail.getOperationRef();
		String currOpRefHash = currentOpRef.substring(0,currentOpRef.length()-1)+ "#";	
 		if (inWorkOper.equals(currOpRefHash)){
 			userRef = sfcStepdetail.getUserRef();	
 		}
 		}
 		if (userRef == null){
 			workInststate = "N/A";
 			return workInststate;
 		}
 		AttachedToContext attachedToContext = new AttachedToContext();
 		attachedToContext.setOperationRef(currentOpRef);
 		FindAttachmentByAttachedToContextRequest attachreq = new FindAttachmentByAttachedToContextRequest();
 		attachreq.setAttachedToContext(attachedToContext);
 		attachreq.setSkipRevision(false);
 		attachreq.setIgnoreExtraAttach(true);
 		attachreq.setAttachmentType(AttachmentType.WORKINSTRUCTION);
 		FindAttachmentByAttachedToContextResponse attachResp = new FindAttachmentByAttachedToContextResponse();
 		try {
 			attachResp = attachmentConfigurationService.findAttachmentByAttacheds(attachreq);
 		} catch (BusinessException e) {
 			e.printStackTrace();
 		}
 		List<FoundReferencesResponse> attachList = new ArrayList<FoundReferencesResponse>();
 		attachList = attachResp.getFoundReferencesResponseList();
 		if (attachList!=  null && attachList.size()>0){
 	
 	
 		for (FoundReferencesResponse attachment:attachList ){
 			List<String> workInsList = new ArrayList<String>();
 			workInsList = attachment.getRefList();	
 			String workInstRef =  workInsList.get(0);		
 			ObjectReference objref =  new ObjectReference(workInstRef);
 			WorkInstructionKeyData wiKeyData =  workInstructionConfigurationService.findWorkInstructionKeyDataByRef(objref);
 			String workIns = wiKeyData.getWorkInstruction();
 			String workInsRev = wiKeyData.getRevision();
 			Data queryData = null;
 			SystemBase sysBase = SystemBase.createDefaultSystemBase();
 			DynamicQuery selWorkIns = DynamicQueryFactory.newInstance();	
 			selWorkIns.append("select COUNT(*) AS COUNT from WORK_INSTRUCTION_LOG where "+
 					"WORK_INSTRUCTION = '"+workIns+"' AND REVISION = '"+workInsRev+"' "+ 
 					"AND USER_BO = '"+userRef+"' and VIEWING_CONTEXT_GBO = '"+sfcRef+"'");					
 			queryData = sysBase.executeQuery(selWorkIns);
 			if(queryData.size() > 0){
 				workInsReadCount = Integer.parseInt(queryData.getString("COUNT", ""));				
 			}	
 			if (workInsReadCount == 0) {
 				pendingCount = 1;
 			}
 		}
 		} else {
 			workInststate = "N/A";
 			return workInststate;
 		}
 		
 		if (pendingCount == 0) {
 			workInststate = "Closed";
 		} else {
 			workInststate = "Pending";
 		}
 		
 		return workInststate;
 	}
 
 	public String getAssemblystatus() {
 		return assemblystatus;
 	}
 
 
 	public void setAssemblystatus(String assemblystatus) {
 		this.assemblystatus = assemblystatus;
 	}
 
 
 	public String getAssemblystate() {
 		return assemblystate;
 	}
 
 
 	public String setAssemblystate(String sfcRef, String currentOpRef) throws NoComponentsToAssembleException, InvalidSfcException, BOMConfigurationException, BusinessException {
 		initServices();		
 		
 		List<SfcIdentifier> sfcIdentifierList = new ArrayList<SfcIdentifier>();
 		SfcIdentifier sfcIdentifier = new SfcIdentifier();
 		sfcIdentifier.setSfcRef(sfcRef);
 		sfcIdentifierList.add(sfcIdentifier);
 		 
 		GroupUnfilledAsBuiltComponentsRequest grpUnfilledasBuiltCompReq = new GroupUnfilledAsBuiltComponentsRequest();
 		grpUnfilledasBuiltCompReq.setEnforceAssyStates(false);
 		grpUnfilledasBuiltCompReq.setOperationRef(currentOpRef);
 		grpUnfilledasBuiltCompReq.setOperationRequired(true);
 		grpUnfilledasBuiltCompReq.setSfcList(sfcIdentifierList);
 		UnfilledComponentsResponse unfilledCompResp1 = new UnfilledComponentsResponse();
 		try {
 		unfilledCompResp1 = retrieveComponentsService.findUnfilledComponents(grpUnfilledasBuiltCompReq);
 		} catch (Exception e){
 			assemblystate = "N/A";
 			return assemblystate;
 		}
 		List<UnfilledComponent> unfilledCompList1 = new ArrayList<UnfilledComponent>();
 		unfilledCompList1 = unfilledCompResp1.getUnfilledComps();
 		
 		if (unfilledCompList1.size()>0){
 			assemblystate = "Pending";
 		} else {
 			assemblystate = "Closed";
 		}
 		return assemblystate;
 	}
     
 
 	public String getDcStatus() {
 		return dcStatus;
 	}
 
 
 	public void setDcStatus(String dcStatus) {
 		this.dcStatus = dcStatus;
 	}
 
 
 	public String getDatacollection() {		
 		return datacollection;
 	}
 
 
 	public String setDatacollection(String sfcRef,String currentOpRef, String CurrentResRef){
 		initServices();		
 			String site = SecurityContext.instance().getSite();
 			WorkCenterBOHandle wcRef = new WorkCenterBOHandle(site,"DUMMY");
 			ResourceBOHandle resRef1 = new ResourceBOHandle(site,CurrentResRef);
 			OperationSfc opSfc = new OperationSfc();
 			opSfc.setSfcRef(sfcRef);
 			List<OperationSfc> opSfcList = new ArrayList <OperationSfc> ();
 			opSfcList.add(opSfc);
 			OperationRequest opReq = new OperationRequest();
 			opReq.setOperationRef(currentOpRef);
 			opReq.setResourceRef(resRef1.toString());
 			opReq.setSfcList(opSfcList);
 			opReq.setCollectDataAt(CollectDataAt.ANYTIME);
 			opReq.setWorkCenterRef(wcRef.toString());
 			DCGroupsToCollectResponse response = new DCGroupsToCollectResponse();
 			try {
 				response = dataCollectionService.findDcGroupsToCollect(opReq);				
 			} catch (BusinessException e) {
 				e.printStackTrace();
 			}
 			if(response != null){
 			List<DCGroupToCollectResponse> dcgrplist = new ArrayList <DCGroupToCollectResponse> ();
 			dcgrplist = response.getDcGroupList();
 			int dcPendingCount = 0;
 				for (DCGroupToCollectResponse dcval:dcgrplist){			
 						List<DCParameterResponse> dcParamList = new ArrayList <DCParameterResponse> ();	
 						dcParamList = dcval.getDcParameterList();
 				for (DCParameterResponse dcParamVal:dcParamList){
 					String dcParamName = dcParamVal.getParameterName();
 					int count = 0;				
 					DynamicQuery sqlString = DynamicQueryFactory.newInstance();		
 					Data queryData2 = null;
 					Data parametricData1 = null;
 					BigDecimal timesProcessed = dcval.getTimesProcessed();
 					sqlString.append("SELECT COUNT(*) AS COUNT  FROM PARAMETRIC, PARAMETRIC_MEASURE WHERE " +
 							"PARAMETRIC_MEASURE.EDITED<>'C'  AND PARAMETRIC.PARA_CONTEXT_GBO = '"+sfcRef+"' "+
 							"AND PARAMETRIC_MEASURE.PARAMETRIC_BO = PARAMETRIC.HANDLE AND "+
 							"PARAMETRIC_MEASURE.MEASURE_NAME = '"+dcParamName+"' AND "+
 							"PARAMETRIC.times_processed = '"+timesProcessed+"'");
 					SystemBase sysBase = SystemBase.createDefaultSystemBase();
 					queryData2 = sysBase.executeQuery(sqlString);		
 									
 					if(queryData2.size() > 0){
 						Iterator<Data> dataIterator = queryData2.iterator();					
 						parametricData1 = dataIterator.next();
 						count = parametricData1.getInteger("COUNT");
 						if(count == 0){	
 							dcPendingCount = dcPendingCount + 1;
 						}					
 					}				
 				}			
 			}
 			  if (dcPendingCount > 0) {
 				  this.datacollection = "Pending";
 			} else{
 				 this.datacollection = "Closed";
 			}
 			} else{
 				this.datacollection = "N/A";
 			}
 			return datacollection;
 		} 
 
 
 	public String getToolStatus() {
 		return toolStatus;
 	}
 
 
 	public void setToolStatus(String toolStatus) {
 		this.toolStatus = toolStatus;
 	}
 
 	public String getToolstate() {
 		return toolstate;
 	}
 
 
 	public String setToolstate(String sfcRef,String currentOpRef) {
 		initServices();
 		AttachedToContext attachedToContext = new AttachedToContext();
 		attachedToContext.setOperationRef(currentOpRef);
 		FindAttachmentByAttachedToContextRequest attachreq = new FindAttachmentByAttachedToContextRequest();
 		attachreq.setAttachedToContext(attachedToContext);
 		attachreq.setSkipRevision(false);
 		attachreq.setIgnoreExtraAttach(true);
 		attachreq.setAttachmentType(AttachmentType.TOOLING);
 		FindAttachmentByAttachedToContextResponse attachResp = new FindAttachmentByAttachedToContextResponse();
 		try {
 			attachResp = attachmentConfigurationService.findAttachmentByAttacheds(attachreq);
 		} catch (BusinessException e) {
 			e.printStackTrace();
 		}
 		List<FoundReferencesResponse> attachList = new ArrayList<FoundReferencesResponse>();
 		attachList = attachResp.getFoundReferencesResponseList();
 		if (attachList!=  null && attachList.size()>0){
 		int pendingCount = 0;
 		for (FoundReferencesResponse attachment:attachList ){
 			List<String> toolGrprefList = new ArrayList<String>();
 			toolGrprefList = attachment.getRefList();	
 			String toolGrpref =  toolGrprefList.get(0);				
 			Data queryData = null;
 			String attachmentRef = null;		
			String currOpRefHash = currentOpRef.substring(0,currentOpRef.length()-1)+ "#";	
 			SystemBase sysBase = SystemBase.createDefaultSystemBase();
 			DynamicQuery selAttachmentBo = DynamicQueryFactory.newInstance();	
 			selAttachmentBo.append("select ATTACHMENT_BO from attached where "+
 					"ATTACHED_TO_GBO = '"+currOpRefHash+"' AND "+
 					"ATTACHMENT_TYPE = 'T' and ATTACHMENT_BO like '%"+toolGrpref+"%'");					
 			queryData = sysBase.executeQuery(selAttachmentBo);
 			if(queryData.size() > 0){
 				attachmentRef = queryData.getString("ATTACHMENT_BO", "");				
 			}	
 			
 			Data queryData1 = null;
 			String qtyRequired = null;
 			SystemBase sysBase1 = SystemBase.createDefaultSystemBase();
 			DynamicQuery selQtyRequired = DynamicQueryFactory.newInstance();	
 			selQtyRequired.append("select QTY_REQ from attachment where "+
 					"HANDLE = '"+attachmentRef+"'");					
 			queryData1 = sysBase1.executeQuery(selQtyRequired);
 			if(queryData1.size() > 0){
 				qtyRequired = queryData1.getString("QTY_REQ", "");				
 			}	
 			
 			
 			GetLoggedQuantityRequest logQtyReq = new GetLoggedQuantityRequest();
 			logQtyReq.setAttachmentRef(attachmentRef);
 			logQtyReq.setOperationRef(currentOpRef);
 			logQtyReq.setSfcRef(sfcRef);
 			BigDecimal loggedQty;
 			try {
 				loggedQty = toolLogService.getLoggedQuantity(logQtyReq);
 				if (!loggedQty.toString().equals(qtyRequired)){
 					pendingCount = pendingCount +1;
 				}
 			} catch (BusinessException e) {
 				e.printStackTrace();
 			}			
 		}
 		if (pendingCount != 0) {
 			toolstate = "Pending" ;
 		} else {
 			toolstate = "Closed";
 		}
 		} else {
 			toolstate = "N/A";
 		}
 		return toolstate;
 	}
 
 	public String getBuyoffStatus() {
 		return buyoffStatus;
 	}
 
 
 	public void setBuyoffStatus(String buyoffStatus) {
 		this.buyoffStatus = buyoffStatus;
 	}
 	
 	
 	public String getBuyoffstate() {
 		return buyoffstate;
 	}
 
 
 	public String setBuyoffstate(String sfcRef,String currentOpRef,String currentResRef) {
 		initServices();
 		FindBuyoffsBySfcData findbuyoffbysfcdata = new FindBuyoffsBySfcData();
 		findbuyoffbysfcdata.setSfcRef(sfcRef);
 		List<FindBuyoffsBySfcData> sfcReqList = new ArrayList<FindBuyoffsBySfcData>();
 		sfcReqList.add(findbuyoffbysfcdata);
 		FindBuyoffsBySfcRequest findbuyoffbysfcrequest = new FindBuyoffsBySfcRequest();
 		findbuyoffbysfcrequest.setOperationRef(currentOpRef);
 		findbuyoffbysfcrequest.setSfcList(sfcReqList);
 		findbuyoffbysfcrequest.setResourceRef(currentResRef);
 		List<FindBuyoffsBySfcResponse> buyofflist =  new ArrayList<FindBuyoffsBySfcResponse>();
 		try {
 			buyofflist = buyoffService.findBuyoffsForSfcs(findbuyoffbysfcrequest);
 		} catch (BusinessException e) {
 			e.printStackTrace();
 		}
 		if (buyofflist!= null && buyofflist.size()>0){			
 			int pendingcount = 0;
 			for(FindBuyoffsBySfcResponse buyoffRecord:buyofflist){
 				BuyoffStateEnum state = buyoffRecord.getState();
 				String actualstate = state.value();
 				if (actualstate.equals("O") || actualstate.equals("P")){
 					pendingcount = pendingcount + 1;
 				} 
 			} 
 			if (pendingcount != 0) {
 				buyoffstate = "Pending";
 			} else {
 				buyoffstate = "Closed";
 			}
 		} else {
 			buyoffstate = "N/A";
 		}
 		
 		return buyoffstate;
 	}
 
 
 	public String getSfc() {
 		return sfc;
 	}
 
 	public void setSfc(String sfc) {
 		this.sfc = sfc;
 	}
 
     public VisualStatusIndicatorPlugin() {
         super();
         getPluginEventManager().addPluginListeners(this.getClass());
     }
 
     @Override
     public Boolean isExecutionPlugin() {
         return true;
     }
 
     public void processSfcChange(SfcChangeEvent event) {    
         	sfcStatusList = new ArrayList<SfcStatusItem>();
         	initServices();
             MessageHandler.clear(this);
             List<SfcSelection> sfcList;
 			try {
 				sfcList = getPodSelectionModel().getResolvedSfcs();			
             if(sfcList!= null && sfcList.size() > 0) {
             	ObjectReference sfcRef1 = new ObjectReference(sfcList.get(0).getSfc().getSfcRef());	
     			SfcBasicData sfcBasicData = sfcStateService.findSfcDataByRef(sfcRef1);
     			sfc = sfcBasicData.getSfc();
     			ObjectReference statusRef = new ObjectReference(sfcBasicData.getStatusRef());
     			StatusBasicConfiguration statusBasicConfiguration = statusService.findStatusByRef(statusRef);
     			sfcStatus = statusBasicConfiguration.getStatusDescription();
     			PodSelectionModelInterface selectionModel = getPodSelectionModel();
     				OperationKeyData opKeydata = selectionModel.getResolvedOperation();
     				ResourceKeyData resKeyData = selectionModel.getResolvedResource();			
     				currResref = resKeyData.getRef();
     				currOpRef = opKeydata.getRef();
     			if (sfcStatus.equals("Active") || sfcStatus.equals("Hold")){
             		setSfc(sfc);	
         			String site = SecurityContext.instance().getSite();
         			SFCBOHandle sfcRef = new SFCBOHandle(site, getSfc()); 
         				String workins = setWorkInststate(sfcRef.toString(),currOpRef);
         				String assem = setAssemblystate(sfcRef.toString(), currOpRef);
         				String datacoll  = setDatacollection(sfcRef.toString(),currOpRef, currResref);
         				String tool = setToolstate(sfcRef.toString(),currOpRef);
         				String buyoff = setBuyoffstate(sfcRef.toString(),currOpRef,currResref);
         				startStatus = "VALID";
         				completeStatus = "INVALID";
         				if (workins.equals("Closed")){
         					workInststatus = "VALID";
         				} else if (workins.equals("Pending")){
         					workInststatus = "INVALID";
         				} else if (workins.equals("N/A")){
         					workInststatus = "NOTEXISTS";
         				}
         				if (assem.equals("Closed")){
         					assemblystatus = "VALID";
         				} else if (assem.equals("Pending")){
         					assemblystatus = "INVALID";
         				} else if (assem.equals("N/A")){
         					assemblystatus = "NOTEXISTS";
         				} 				
         				if (datacoll.equals("Closed")){
         					dcStatus = "VALID";
         				} else if (datacoll.equals("Pending")){
         					dcStatus = "INVALID";
         				} else if (datacoll.equals("N/A")){
         					dcStatus = "NOTEXISTS";
         				}
         			
         				if (tool.equals("Closed")){
         					toolStatus = "VALID";
         				} else if (tool.equals("Pending")){
         					toolStatus = "INVALID";
         				} else if (tool.equals("N/A")){
         					toolStatus = "NOTEXISTS";
         				}
         				if (buyoff.equals("Closed")){
         					buyoffStatus = "VALID";
         				} else if (buyoff.equals("Pending")){
         					buyoffStatus = "INVALID";
         				} else if (buyoff.equals("N/A")){
         					buyoffStatus = "NOTEXISTS";
         				}
         				SfcStatusItem sfcStatusitem = new SfcStatusItem();
         				sfcStatusList.add(sfcStatusitem);   				  
         				FacesUtility.removeSessionMapValue("sfcStatusTableBeanConfigurator");
         				TableConfigurator table = (TableConfigurator) FacesUtility.resolveExpression("#{sfcStatusTableBeanConfigurator}");
         				setTableConfigBean(table);
     			} 
             	}else {
             		startStatus = "NONE";
     				completeStatus = "NONE";
             		workInststatus = "NONE";
             		assemblystatus = "NONE";
             		buyoffStatus = "NONE";
             		dcStatus = "NONE";
             		toolStatus = "NONE";
             		SfcStatusItem sfcStatusitem = new SfcStatusItem();
             		sfcStatusList.add(sfcStatusitem);   				  
             		FacesUtility.removeSessionMapValue("sfcStatusTableBeanConfigurator");
             		TableConfigurator table = (TableConfigurator) FacesUtility.resolveExpression("#{sfcStatusTableBeanConfigurator}");
             		setTableConfigBean(table);
             	}
 			} catch (BusinessException e) {
 				e.printStackTrace();
 			}
     }
 
 	private void initServices(){
 		dataCollectionService = Services.getService(COM_SAP_ME_DATACOLLECTION,DATA_COLLECTION_SERVICE);
 		sfcStateService = Services.getService(COM_SAP_ME_PRODUCTION,SFC_STATE_SERVICE);
 		statusService = Services.getService(COM_SAP_ME_STATUS,STATUS_SERVICE);
 		buyoffService = Services.getService(COM_SAP_ME_PRODUCTION,BUYOFF_SERVICE);
 		attachmentConfigurationService = Services.getService(COM_SAP_ME_PRODUCTDEFINITION,ATTACHMENT_CONFIGURATION_SERVICE);
 		toolLogService = Services.getService(COM_SAP_ME_TOOLING,TOOL_LOG_SERVICE);
 		retrieveComponentsService = Services.getService(COM_SAP_ME_PRODUCTION,RETRIEVE_COMPONENTS_SERVICE);
 		workInstructionConfigurationService = Services.getService(COM_SAP_ME_PRODUCTDEFINITION,WORK_INSTRUCTION_CONFIGURATION_SERVICE);
 	}
 
 
 	public void setTableConfigBean(TableConfigurator tableConfigBean) {
 		this.tableConfigBean = tableConfigBean;
 		if (tableConfigBean.getColumnBindings() == null || tableConfigBean.getColumnBindings().size() < 1) {
 			tableConfigBean.setListName(null);
 			tableConfigBean.setColumnBindings(getColumnFieldMaping());
 			tableConfigBean.setListColumnNames(listColumnNames);
 			tableConfigBean.setAllowSelections(false);
 			tableConfigBean.setMultiSelectType(false);					
 			tableConfigBean.configureTable();
 		}
 	}
 
 
 	public TableConfigurator getTableConfigBean() {
 		return tableConfigBean;
 	}
 
 	
 	public List<SfcStatusItem> getSfcStatusList() {
 		return sfcStatusList;
 	}
 
 
 	public void setSfcStatusList(List<SfcStatusItem> sfcStatusList) {
 		this.sfcStatusList = sfcStatusList;
 	}
 
 	public void processRefreshWorklist(RefreshWorklistEvent event) {
 		SfcChangeEvent event1 = null;
 		processSfcChange(event1);		
 	}
 	public void statusRefresh() {
 		SfcChangeEvent event1 = null;
 		processSfcChange(event1);
 	}
 }
 
 
