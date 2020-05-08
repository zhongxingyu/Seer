 /*
  * Created on January 17, 2007
  * @author
  *
  */
 
 package edu.wustl.clinportal.action.annotations;
 
 import java.text.ParseException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.ehcache.CacheException;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.wustl.clinportal.actionForm.AnnotationDataEntryForm;
 import edu.wustl.clinportal.bizlogic.ClinicalStudyEventBizlogic;
 import edu.wustl.clinportal.bizlogic.ClinicalStudyRegistrationBizLogic;
 import edu.wustl.clinportal.bizlogic.EventEntryBizlogic;
 import edu.wustl.clinportal.domain.ClinicalStudyEvent;
 import edu.wustl.clinportal.domain.ClinicalStudyRegistration;
 import edu.wustl.clinportal.domain.EventEntry;
 import edu.wustl.clinportal.domain.RecordEntry;
 import edu.wustl.clinportal.domain.StudyFormContext;
 import edu.wustl.clinportal.util.CatissueCoreCacheManager;
 import edu.wustl.clinportal.util.DataEntryUtil;
 import edu.wustl.clinportal.util.EventTreeObject;
 import edu.wustl.clinportal.util.global.Constants;
 import edu.wustl.clinportal.util.global.Utility;
 import edu.wustl.common.action.BaseAction;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.bizlogic.AbstractBizLogic;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.global.Validator;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.exception.DAOException;
 import edu.wustl.security.exception.UserNotAuthorizedException;
 
 public class LoadDynamicExtentionsDataEntryPageAction extends BaseAction
 {
 
 	private transient Logger logger = Logger
 			.getCommonLogger(LoadDynamicExtentionsDataEntryPageAction.class);
 
 	/**
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 * @see edu.wustl.common.action.BaseAction#executeAction(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected ActionForward executeAction(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		AnnotationDataEntryForm annoDataEtryFrm = (AnnotationDataEntryForm) form;
 		String treeViewKey = request.getParameter(AnnotationConstants.TREE_VIEW_KEY);
 		EventTreeObject eventTreeObject = new EventTreeObject(treeViewKey);
 		
 		// If coming from Add Visit page
 		if("0".equals(eventTreeObject.getFormContextId()))
 		{
 			String studyFormId = request.getParameter("studyForm");
 			eventTreeObject.setFormContextId(studyFormId);
 		}
 		
 		// Pass the Study form label to DE -- for displaying as the form header
 		DataEntryUtil dataEntryUtil = new DataEntryUtil();
 		StudyFormContext studyFormContext = dataEntryUtil.getStudyFormContext(eventTreeObject
 				.getEventId(), eventTreeObject.getFormContextId());
 		request.getSession().setAttribute("OverrideCaption",
 				studyFormContext.getStudyFormLabel() + ":" + studyFormContext.getContainerId());
 		
 		// If coming from Add Visit page
 		if (treeViewKey.startsWith("ClinicalStudyEvent"))
 		{
 			String encounterDate = request.getParameter("encounterDate");
 			if (encounterDate != null && !"".equals(encounterDate))
 			{
 				boolean isValidDate = validateEncounteredDate(encounterDate, request);
 				if (!isValidDate)
 				{
 					return mapping.findForward(Constants.FAILURE);
 				}
 			}
 				
 			treeViewKey = handleVisitAndFormSelection(studyFormContext, eventTreeObject, request);
 		}
 		
 		if (treeViewKey != null)
 		{
 			request.getSession().setAttribute(AnnotationConstants.TREE_VIEW_KEY, treeViewKey);
 		}
 		
 		updateCache(request, annoDataEtryFrm, eventTreeObject);
 
 		String comingFrom = request.getParameter(Constants.FORWARD_CONTROLLER);
 		request.getSession().setAttribute(Constants.FORWARD_CONTROLLER, comingFrom);
 		
 		//Set as request attribute
 		String dEDataEntryURL = getDynamicExtensionsDataEntryURL(request, annoDataEtryFrm);
 		request
 				.setAttribute(AnnotationConstants.DYNAMIC_EXTN_DATA_ENTRY_URL_ATTRIB,
 						dEDataEntryURL);
 		
 		String dynExtRecordId = request.getParameter("recordId");
 		if (dynExtRecordId == null && checkForClosedStatus(request, true))
 		{
 			return mapping.findForward(Constants.FAILURE);
 		}
 		request.setAttribute(Constants.OPERATION, "DefineDynExtDataForAnnotations");
 		return mapping.findForward(Constants.SUCCESS);
 
 	}
 
 	/**
 	 * 
 	 * @param studyFormContext
 	 * @param eventTreeObject
 	 * @param request
 	 * @return
 	 * @throws Exception
 	 */
 	private String handleVisitAndFormSelection(StudyFormContext studyFormContext,
 			EventTreeObject eventTreeObject, HttpServletRequest request)
 			throws Exception
 	{
 		String objectName = "";
 		eventTreeObject.setRecEntryId("0");
 		eventTreeObject.setDynamicRecId("0");
 		eventTreeObject.setContainerId(studyFormContext.getContainerId().toString());
 
 		String eventId = request.getParameter(Constants.PROTOCOL_EVENT_ID);
 		eventTreeObject.setEventId(eventId);
 		String csId = request.getParameter(Constants.CP_SEARCH_CP_ID);
 		String participantId = request.getParameter(Constants.CP_SEARCH_PARTICIPANT_ID);
 		ClinicalStudyRegistration csReg = getCSRegistration(csId, participantId);
 		eventTreeObject.setRegistrationId(csReg.getId().toString());
 
 		ClinicalStudyEventBizlogic bizLogic = new ClinicalStudyEventBizlogic();
 		ClinicalStudyEvent csEvent = bizLogic.getClinicalStudyEventById(Long.valueOf(eventId));
 		
 		String visitNumber = request.getParameter("visitNumber");
 
 		// If add new visit option selected
 		// If coming from Add Visit page - for forms that can have infinite visits
 		if ("0".equals(visitNumber))
 		{
 			EventEntry eventEntry = createEventEntry(csEvent, csReg, request, visitNumber);
 			eventTreeObject.setEventEntryId(eventEntry.getId().toString());
 			eventTreeObject.setEventEntryNumber(eventEntry.getEntryNumber().toString());
 			objectName = getObjectNameForEventTree(studyFormContext, true, false);
 			eventTreeObject.setObjectName(objectName);
 		}
 		else
 		{// If a visit is selected
 			DataEntryUtil dataEntryUtil = new DataEntryUtil();
 			List<EventEntry> eventEntryCollection = dataEntryUtil.getEventEntryFromEntryNum(
 					csEvent, csReg, Integer.parseInt(visitNumber));
 
 			// If no data entry done or the encountered date is not set
 			// i.e. no EventEntry object created for the visit
 			if (eventEntryCollection == null || eventEntryCollection.isEmpty())
 			{
 				objectName = getObjectNameForEventTree(studyFormContext, false, false);
 				eventTreeObject.setObjectName(objectName);
 				eventTreeObject.setEventEntryNumber(visitNumber);
 
 				String encounterDate = request.getParameter("encounterDate");
 				if (encounterDate != null && !"".equals(encounterDate))
 				{
 					EventEntry eventEntry = createEventEntry(csEvent, csReg, request, visitNumber);
 					eventTreeObject.setEventEntryId(eventEntry.getId().toString());
 				}
 				else
 				{
 					eventTreeObject.setEventEntryId("-" + visitNumber);
 				}
 			}
 			else
 			{ // if EventEntry object is present
 				EventEntry selectedEventEntry = eventEntryCollection.get(0);
 
 				// update with date
 				String encounterDate = request.getParameter("encounterDate");
 				if (encounterDate != null && !"".equals(encounterDate))
 				{
 					EventEntry newEntry =  eventEntryCollection.get(0);
 					
 					newEntry.setEncounterDate(edu.wustl.common.util.Utility
							.parseDate(request.getParameter("encounterDate")));
 					AbstractBizLogic eventEntryBizLogic = new EventEntryBizlogic();
 					SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 							Constants.SESSION_DATA);
 					eventEntryBizLogic.update(newEntry, selectedEventEntry, sessionDataBean);
 				}
 
 				eventTreeObject.setEventEntryId(selectedEventEntry.getId().toString());
 				eventTreeObject.setEventEntryNumber(selectedEventEntry.getEntryNumber().toString());
 
 				List<RecordEntry> recordEntryCollection = dataEntryUtil.getRecordEntry(
 						selectedEventEntry.getId(), studyFormContext.getId());
 
 				boolean hasRecords = false;
 				if (recordEntryCollection != null && !recordEntryCollection.isEmpty())
 				{
 					hasRecords = true;
 				}
 
 				objectName = getObjectNameForEventTree(studyFormContext, false, hasRecords);
 				eventTreeObject.setObjectName(objectName);
 
 				if (hasRecords && !studyFormContext.getCanHaveMultipleRecords())
 				{
 					RecordEntry recordEntry = recordEntryCollection.get(0);
 					CatissueCoreCacheManager cache = CatissueCoreCacheManager.getInstance();
 					String recEntryEntityId = cache.getObjectFromCache(
 							AnnotationConstants.RECORD_ENTRY_ENTITY_ID).toString();
 
 					Long dynamicRecId = dataEntryUtil.getDERecordId(recordEntry.getId(), Long
 							.valueOf(eventTreeObject.getContainerId()), Long
 							.valueOf(recEntryEntityId));
 
 					eventTreeObject.setDynamicRecId(dynamicRecId.toString());
 					eventTreeObject.setRecEntryId(recordEntry.getId().toString());
 					request.setAttribute("recordId", dynamicRecId.toString());
 				}
 			}
 		}
 
 		request.setAttribute(Constants.IS_TO_REFRESH_TREE, Constants.TRUE);
 		request.getSession().setAttribute(AnnotationConstants.FORM_ID,
 				Long.decode(studyFormContext.getContainerId().toString()));
 
 		return eventTreeObject.createKeyFromObject();
 	}
 
 	/**
 	 * 
 	 * @param studyFormContext
 	 * @param isAddVisitOperation
 	 * @param hasRecords
 	 * @return
 	 */
 	private String getObjectNameForEventTree(StudyFormContext studyFormContext,
 			boolean isAddVisitOperation, boolean hasRecords)
 	{
 		String objectName = null;
 
 		if (studyFormContext.getCanHaveMultipleRecords())
 		{
 			objectName = "FormEntry";
 		}
 		else
 		{
 			if (isAddVisitOperation || !hasRecords)
 			{
 				objectName = "SingleRecordForm";
 			}
 			else
 			{
 				objectName = "SingleRecordFormEdit";
 			}
 
 		}
 
 		return objectName;
 	}
 	
 	/**
 	 * 
 	 * @param encounterDate
 	 * @param request
 	 * @return
 	 * @throws BizLogicException
 	 */
 	private boolean validateEncounteredDate(String encounterDate, HttpServletRequest request)
 			throws BizLogicException
 	{
 		edu.wustl.clinportal.util.global.Validator validator = new edu.wustl.clinportal.util.global.Validator();
 		String errorKey = validator.validateDate(encounterDate, true);
 		if (errorKey.length() > 0)
 		{
 			ActionErrors errors = new ActionErrors();
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item",
 					ApplicationProperties.getValue("eventEntry.date.invalid", "Encountered Date")));
 			
 			String eventId = request.getParameter(Constants.PROTOCOL_EVENT_ID);
 			String key = "ClinicalStudyEvent_" + eventId + "_0_0_0_0_0_0_0";
 			EventTreeObject treeObject = new EventTreeObject(key);
 			new DataEntryUtil().genrateEventKey(treeObject);
 			String treeViewKey = treeObject.createKeyFromObject();
 			request.getSession().setAttribute(AnnotationConstants.TREE_VIEW_KEY, treeViewKey);
 			saveErrors(request, errors);
 			return false;
 		}
 		return true;
 	}
 	
 
 	/**
 	 * 
 	 * @param studyId
 	 * @param participantId
 	 * @return
 	 * @throws BizLogicException
 	 */
 	private ClinicalStudyRegistration getCSRegistration(String studyId, String participantId)
 			throws BizLogicException
 	{
 		ClinicalStudyRegistrationBizLogic regBizLogic = new ClinicalStudyRegistrationBizLogic();
 		return regBizLogic.getCSRegistration(Long.valueOf(studyId), Long.valueOf(participantId));
 	}
 
 	/**
 	 * 
 	 * @param csEvent
 	 * @param csReg
 	 * @param request
 	 * @param eventNumber
 	 * @return
 	 * @throws BizLogicException
 	 * @throws UserNotAuthorizedException
 	 * @throws DAOException
 	 * @throws ParseException
 	 */
 	private EventEntry createEventEntry(ClinicalStudyEvent csEvent,
 			ClinicalStudyRegistration csReg, HttpServletRequest request, String eventNumber)
 			throws BizLogicException, UserNotAuthorizedException, DAOException, ParseException
 	{
 		SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 				Constants.SESSION_DATA);
 		AbstractBizLogic bizLogic = new EventEntryBizlogic();
 		EventEntry eventEntry = new EventEntry();
 		eventEntry.setActivityStatus(Constants.ACTIVITY_STATUS_ACTIVE);
 
 		if ("0".equals(eventNumber))
 		{
 			DataEntryUtil dataEntryUtil = new DataEntryUtil();
 			Collection<EventEntry> eventEntryCollection = dataEntryUtil.getEventEntry(csEvent,
 					csReg);
 			eventEntry.setEntryNumber(Integer.valueOf(eventEntryCollection.size() + 1));
 		}
 		else
 		{
 			eventEntry.setEntryNumber(Integer.valueOf(eventNumber));
 		}
 
 		eventEntry.setClinicalStudyRegistration(csReg);
 		eventEntry.setClinicalStudyEvent(csEvent);
 
 		String encounterDate = request.getParameter("encounterDate");
 		if (encounterDate != null && !"".equals(encounterDate))
 		{
 			eventEntry.setEncounterDate(edu.wustl.common.util.Utility.parseDate(request
					.getParameter("encounterDate")));
 		}
 
 		bizLogic.insert(eventEntry, sessionDataBean, Constants.HIBERNATE_DAO);
 
 		return eventEntry;
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 * @param saveErr
 	 * @throws DAOException
 	 * @throws BizLogicException
 	 */
 	private boolean checkForClosedStatus(HttpServletRequest request, boolean saveErr)
 			throws DAOException, BizLogicException
 	{
 		String studyId = request.getParameter(Constants.CP_SEARCH_CP_ID);
 		DataEntryUtil dataEntryUtil = new DataEntryUtil();
 		String participantId = request.getParameter(Constants.CP_SEARCH_PARTICIPANT_ID);
 		String regId = getRegId(studyId, participantId);
 		ActionErrors err = dataEntryUtil.getClosedStatus(request, participantId, studyId, regId);
 		boolean flag = false;
 		if (!err.isEmpty())
 		{
 			if (saveErr)
 			{
 				saveErrors(request, err);
 			}
 			flag = true;
 		}
 		if (!flag && !saveErr)
 		{
 			flag = checkforPrivileges(request);
 		}
 		return flag;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 * @throws DAOException
 	 */
 	private boolean checkforPrivileges(HttpServletRequest request) throws DAOException
 	{
 		SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 				Constants.SESSION_DATA);
 		String frmCntxtId = request.getParameter(AnnotationConstants.FORM_CONTEXT_ID);
 		if("0".equals(frmCntxtId))
 		{
 			frmCntxtId = request.getParameter("studyForm");
 		}
 		String userId = sessionDataBean.getUserId().toString();
 		String userName = sessionDataBean.getUserName().toString();
 		Map<String, Boolean> privMap = Utility.checkFormPrivileges(frmCntxtId, userId, userName);
 		return privMap.get(Constants.READ_ONLY);
 	}
 
 	/**
 	 * 
 	 * @param studyId
 	 * @param participantId
 	 * @return
 	 * @throws DAOException
 	 */
 	private String getRegId(String studyId, String participantId) throws DAOException
 	{
 		String regId = null;
 		String activityStatusHQL = "select csr.id from edu.wustl.clinportal.domain.ClinicalStudyRegistration "
 				+ "as csr where csr.participant.id="
 				+ participantId
 				+ " and csr.clinicalStudy.id="
 				+ studyId;
 		List actStatusLst = new DataEntryUtil().executeQuery(activityStatusHQL);
 		if (actStatusLst != null && !actStatusLst.isEmpty())
 		{
 			regId = actStatusLst.get(0).toString();
 		}
 		return regId;
 	}
 
 	/**
 	 * @param request
 	 * @param annotDataEtryFrm 
 	 * @param eventTreeObject
 	 * @throws CacheException 
 	 */
 	private void updateCache(HttpServletRequest request, AnnotationDataEntryForm annotDataEtryFrm,
 			EventTreeObject eventTreeObject) throws CacheException
 	{
 		//String recordId = request.getParameter("recordId");
 
 		String eventId = request.getParameter(Constants.PROTOCOL_EVENT_ID);
 		if (eventId != null)
 		{
 			request.getSession().setAttribute(Constants.PROTOCOL_EVENT_ID, eventId);
 		}
 
 		/*String staticEntityId = annotDataEtryFrm.getParentEntityId();
 		if (recordId == null && staticEntityId == null)//&& !recordId.equals("0") )
 		{
 			CatissueCoreCacheManager cacheManager = CatissueCoreCacheManager.getInstance();
 			//staticEntityId = 
 			cacheManager.getObjectFromCache(AnnotationConstants.RECORD_ENTRY_ENTITY_ID).toString();
 		}*/
 
 		//Set into Cache
 		String selStaticEntyId = annotDataEtryFrm.getSelectedStaticEntityId();
 		if (selStaticEntyId == null)
 		{
 			selStaticEntyId = CatissueCoreCacheManager.getInstance().getObjectFromCache(
 					AnnotationConstants.RECORD_ENTRY_ENTITY_ID).toString();
 		}
 		request.getSession().setAttribute(AnnotationConstants.SELECTED_STATIC_ENTITYID,
 				selStaticEntyId);
 		String staticEntyRecId = annotDataEtryFrm.getSelectedStaticEntityRecordId();
 		if (staticEntyRecId == null)
 		{
 			staticEntyRecId = request.getParameter(Constants.CP_SEARCH_PARTICIPANT_ID);
 		}
 		request.getSession().setAttribute(AnnotationConstants.SELECTED_STATIC_ENTITY_RECORDID,
 				staticEntyRecId);
 
 		request.getSession().setAttribute(AnnotationConstants.FORM_CONTEXT_ID,
 				eventTreeObject.getFormContextId());
 
 		//set the studyId in session.
 		request.getSession().setAttribute(Constants.CP_SEARCH_CP_ID,
 				request.getParameter(Constants.CP_SEARCH_CP_ID));
 
 	}
 
 	/**
 	 * @param request
 	 * @param annotDataEnryFrm
 	 * @return
 	 * @throws CacheException 
 	 * @throws BizLogicException 
 	 * @throws DAOException 
 	 */
 	private String getDynamicExtensionsDataEntryURL(HttpServletRequest request,
 			AnnotationDataEntryForm annotDataEnryFrm) throws CacheException, DAOException,
 			BizLogicException
 	{
 		//Append container id
 		logger.info("Load data entry page for Dynamic Extension Entity ["
 				+ annotDataEnryFrm.getSelectedAnnotation() + "]");
 		String dynEntContainerId = annotDataEnryFrm.getSelectedAnnotation();
 		if (dynEntContainerId == null)
 		{
 			dynEntContainerId = request.getParameter(AnnotationConstants.FORM_ID);
 		}
 
 		if (dynEntContainerId == null || "0".equals(dynEntContainerId))
 		{
 
 			dynEntContainerId = request.getSession().getAttribute(AnnotationConstants.FORM_ID)
 					.toString();
 		}
 		else
 		{
 			request.getSession().setAttribute(AnnotationConstants.FORM_ID,
 					Long.decode(dynEntContainerId));
 		}
 
 		String equalSign = "=";
 		String andSign = "&";
 		StringBuffer dExtDataEtryURL = new StringBuffer(request.getContextPath());
 		dExtDataEtryURL.append(WebUIManager.getLoadDataEntryFormActionURL());
 		dExtDataEtryURL.append(andSign);
 		dExtDataEtryURL.append(WebUIManager.CONATINER_IDENTIFIER_PARAMETER_NAME);
 		dExtDataEtryURL.append(equalSign);
 		dExtDataEtryURL.append(dynEntContainerId);
 
 		String dynExtRecordId = request.getParameter("recordId");
 
 		if (dynExtRecordId == null)
 		{
 			dynExtRecordId = (String) request.getAttribute("recordId");
 		}
 		if (dynExtRecordId != null)
 		{
 			logger.info("Loading details of record id [" + dynExtRecordId + "]");
 			dExtDataEtryURL.append(andSign);
 			dExtDataEtryURL.append(WebUIManager.RECORD_IDENTIFIER_PARAMETER_NAME);
 			dExtDataEtryURL.append(equalSign);
 			dExtDataEtryURL.append(dynExtRecordId);
 		}
 		//append call back URL
 		dExtDataEtryURL.append(andSign);
 		dExtDataEtryURL.append(WebUIManager.getCallbackURLParamName());
 		dExtDataEtryURL.append(equalSign);
 		dExtDataEtryURL.append(request.getContextPath());
 		dExtDataEtryURL.append(AnnotationConstants.CALLBACK_URL_PATH_ANNOTATION_DATA_ENTRY);
 
 		//append User Id
 		SessionDataBean dataBean = (SessionDataBean) request.getSession().getAttribute(
 				Constants.SESSION_DATA);
 
 		dExtDataEtryURL.append(andSign);
 		dExtDataEtryURL.append(WebUIManager.getUserIdParameterName());
 		dExtDataEtryURL.append(equalSign);
 		dExtDataEtryURL.append(dataBean.getCsmUserId());
 
 		if (checkForClosedStatus(request, false))
 		{
 			dExtDataEtryURL.append(andSign);
 			dExtDataEtryURL.append(WebUIManager.MODE_PARAM_NAME);
 			dExtDataEtryURL.append("=view");
 		}
 		return dExtDataEtryURL.toString();
 	}
 
 }
