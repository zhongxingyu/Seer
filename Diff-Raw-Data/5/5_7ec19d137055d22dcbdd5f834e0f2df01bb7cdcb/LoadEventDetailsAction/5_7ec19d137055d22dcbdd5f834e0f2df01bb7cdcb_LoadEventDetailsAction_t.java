 
 package edu.wustl.clinportal.action;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
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
 
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.wustl.clinportal.action.annotations.AnnotationConstants;
 import edu.wustl.clinportal.actionForm.EventEntryForm;
 import edu.wustl.clinportal.bizlogic.AnnotationBizLogic;
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
 import edu.wustl.clinportal.util.global.Variables;
 import edu.wustl.common.action.BaseAction;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.bizlogic.AbstractBizLogic;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.dao.exception.DAOException;
 import edu.wustl.security.exception.UserNotAuthorizedException;
 
 /**
  * @author falguni_sachde
  *
  */
 public class LoadEventDetailsAction extends BaseAction
 {
 
 	int commonSerialNo;
 	private boolean readOnly = false;
 
 	/**
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 * @see edu.wustl.common.action.BaseAction#executeAction(
 	 * org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, 
 	 * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	protected ActionForward executeAction(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		// Getting required parameters to form XML/insert encountered date 
 
 		String target = null;
 		if (request.getParameter(Constants.OPERATION) != null
 				&& request.getParameter(Constants.OPERATION).equals(Constants.DELETE_XML))
 		{
 			String xmlFileName = request.getParameter("XMLFileName");
 			deleteXMLFilesIfExists(xmlFileName);
 		}
 		else
 		{
 			target = Constants.SUCCESS;
 			String eventId = request.getParameter(Constants.PROTOCOL_EVENT_ID);
 			String studyId = request.getParameter(Constants.CP_SEARCH_CP_ID);
 			String participantId = request.getParameter(Constants.CP_SEARCH_PARTICIPANT_ID);
 			String key = getTreeViewKey(request);
 			EventTreeObject eventTreeObj = new EventTreeObject();
 			eventTreeObj.populateObject(key);
 
 			// Check whether control is coming from DE			 
 			String strIsComingFromDE = request.getParameter("comingFromDE");
 			/*
 			 *  If control is coming from DE page, all parameters will be null
 			 *  So, get those from the session.
 			 */
 			if (strIsComingFromDE != null && Boolean.valueOf(strIsComingFromDE))
 			{
 				eventId = (String) request.getSession().getAttribute(Constants.PROTOCOL_EVENT_ID);
 				studyId = (String) request.getSession().getAttribute(Constants.CP_SEARCH_CP_ID);
 				participantId = (String) request.getSession().getAttribute(
 						AnnotationConstants.SELECTED_STATIC_ENTITY_RECORDID);
 
 				/*
 				 * If the form having single record then EventEntry node will be returned else
 				 * FormEntry node will be returned
 				 */
 				key = getKeyForEventOrFormEntry(eventTreeObj, key);
 
 				request.getSession().setAttribute(AnnotationConstants.TREE_VIEW_KEY, key);
 			}
 
 			EventEntryForm eventEntryForm = (EventEntryForm) form;
 
 			// Get the ClinicalStudyRegistration object 
 			ClinicalStudyRegistrationBizLogic regBizLogic = new ClinicalStudyRegistrationBizLogic();
 			ClinicalStudyRegistration cstudyReg = regBizLogic.getCSRegistration(Long
 					.valueOf(studyId), Long.valueOf(participantId));
 			
 			// Get the ClinicalStudyEvent object
 			ClinicalStudyEventBizlogic bizlogic = new ClinicalStudyEventBizlogic();
 			ClinicalStudyEvent event = bizlogic.getClinicalStudyEventById(Long.valueOf(eventId));
 			
 			EventEntry eventEntry = null;
 			if (eventEntryForm != null)
 			{
 				if (eventEntryForm.getEncounterDate() == null
 						|| eventEntryForm.getEncounterDate().equals(""))
 				{
 					eventEntryForm.setEncounterDate(edu.wustl.common.util.Utility
 							.parseDateToString(new Date(System.currentTimeMillis()),
 									Constants.DATE_PATTERN_DD_MM_YYYY));
 				}
 
 				String entryNumber = eventTreeObj.getEventEntryNumber(); //entryArr[2];
 
 				// Check the database whether the eventEntry object exists
 				eventEntry = getEventEntry(Integer.parseInt(entryNumber), eventEntry, cstudyReg,
 						event);
 				// If eventEntry exists update or else insert the eventEntry.
 				if (Boolean.valueOf(request.getParameter(Constants.IS_EVENT_DATE_SUBMITTED)))
 				{
 					String encounterDate = "";
 					if (eventEntryForm.getEncounterDate() != null)
 					{
 						encounterDate = eventEntryForm.getEncounterDate();
 					}
 
 					eventEntry = insertOrUpdateEventEntry(cstudyReg, event, eventEntry,
 							encounterDate, eventTreeObj, request);
 					eventTreeObj.setEventEntryId(String.valueOf(eventEntry.getId()));
 					request.getSession().setAttribute(AnnotationConstants.TREE_VIEW_KEY,
 							eventTreeObj.createKeyFromObject());
 				}
 
 				if (eventEntry != null)
 				{
 					eventEntryForm.setAllValues(eventEntry);
 				}
 				if (cstudyReg != null)
 				{
 					eventEntryForm.setCSRActivityStatus(cstudyReg.getActivityStatus());
 				}
 				getActivityStatus(eventEntryForm, participantId, studyId);
 
 			}
 
 			StringBuffer formsXML = new StringBuffer();
 			formsXML.append("<?xml version='1.0' encoding='UTF-8'?><rows>");
 
 			//if (eventList != null && !eventList.isEmpty())
 			if (event != null)
 			{
 				//When clicked node is Event
 				if (eventTreeObj.getObjectName().equals(Constants.CLINICAL_STUDY_EVENT_OBJECT))
 				{
 					formXMLForEvent(formsXML, studyId, participantId, event, eventEntry, request,
 							cstudyReg, eventTreeObj, eventEntryForm);
 				}
 				else
 				{
 					String entryNumber = eventTreeObj.getEventEntryNumber();
 					String nodeId = eventTreeObj.getEventEntryId();//entryArr[3];
 				    StringBuffer studyFormHQL = new StringBuffer(25);
                     studyFormHQL.append("from ").append(StudyFormContext.class.getName());
                     studyFormHQL.append(" as studyFrm where studyFrm.clinicalStudyEvent.id=");
                     studyFormHQL.append(event.getId());
                     studyFormHQL.append(" and studyFrm.activityStatus!='");
                     studyFormHQL.append(Constants.ACTIVITY_STATUS_DISABLED+ "'");
 
 					if (eventTreeObj.getObjectName() != null
 							&& eventTreeObj.getObjectName().equals(Constants.FORM_ENTRY_OBJECT)
 							|| eventTreeObj.getObjectName().equals(Constants.FORM_CONTEXT_OBJECT))
 					{
 						studyFormHQL.append(" and studyFrm.id=");
 						studyFormHQL.append(eventTreeObj.getFormContextId());
 
 					}
 					studyFormHQL.append(" order by studyFrm.id asc");
 					Collection<StudyFormContext> studyFormColl = (Collection<StudyFormContext>) Utility
 							.executeQuery(studyFormHQL.toString());
 					commonSerialNo = 0;
 					SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 							Constants.SESSION_DATA);
 					formsXML = generateFormXMLForEachEntry(formsXML, studyId, participantId,
 							Integer.parseInt(nodeId), studyFormColl, eventEntry, event, cstudyReg
 									.getId(), Integer.parseInt(entryNumber), eventTreeObj,
 							eventEntryForm, sessionDataBean);
 				}
 
 			}
 			formsXML.append("</rows>");
 			/*
 			 * This method generates XML file out of the StringBuffer(XML String). This file name will be
 			 * sent as request attribute to the JSP and that XML file will be displayed as grid.
 			 * 
 			 */
 			String genXMLFName = generateXMLFile(formsXML, request);
 			request.setAttribute("XMLFileName", genXMLFName);
 		}
 		return mapping.findForward(target);
 	}
 
 	/**
 	 * 
 	 * @param eventTreeObj
 	 * @param key
 	 * @return
 	 */
 	private String getKeyForEventOrFormEntry(EventTreeObject eventTreeObj, String key)
 	{
 		String returnKey;
 		if (eventTreeObj.getObjectName().equals(Constants.SINGLE_RECORD_FORM)
 				|| eventTreeObj.getObjectName().equals(Constants.SINGLE_RECORD_FORM_EDIT))
 		{
 			returnKey = eventTreeObj.getEntryKey(key);
 		}
 		else
 		{
 			returnKey = eventTreeObj.getFormEntryKey(key);
 		}
 
 		return returnKey;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 */
 	private String getTreeViewKey(HttpServletRequest request)
 	{
 		String key = request.getParameter(AnnotationConstants.TREE_VIEW_KEY);
 		if (key == null)//when comes from data entry key = null
 		{
 			key = (String) request.getSession().getAttribute(AnnotationConstants.TREE_VIEW_KEY);
 		}
 		else
 		{
 			request.getSession().setAttribute(AnnotationConstants.TREE_VIEW_KEY, key);
 		}
 
 		return key;
 	}
 
 	/**
 	 * 
 	 * @param eventEntryForm
 	 * @param participantId
 	 * @param studyId
 	 * @throws DAOException
 	 */
 	private void getActivityStatus(EventEntryForm eventEntryForm, String participantId,
 			String studyId) throws DAOException
 	{
 		DataEntryUtil dataUtil = new DataEntryUtil();
 		String hql = "select part.activityStatus from edu.wustl.clinportal.domain.Participant "
 				+ "as part where part.id=" + participantId;
 		List dataList = dataUtil.executeQuery(hql);
 		if (dataList != null && !dataList.isEmpty())
 		{
 			eventEntryForm.setParticipantActivityStatus(dataList.get(0).toString());
 		}
 		hql = "select cs.activityStatus from edu.wustl.clinportal.domain.ClinicalStudy "
 				+ "as cs where cs.id=" + studyId;
 		dataList = dataUtil.executeQuery(hql);
 
 		if (dataList != null && !dataList.isEmpty())
 		{
 			eventEntryForm.setCSActivityStatus(dataList.get(0).toString());
 		}
 	}
 
 	/**
 	 * This method insert or updates the event entry
 	 * @param csRegn
 	 * @param event
 	 * @param eventEntry
 	 * @param encounterDate
 	 * @param eventTreeObj
 	 * @param request
 	 * @return
 	 * @throws ParseException
 	 * @throws BizLogicException
 	 * @throws UserNotAuthorizedException
 	 * @throws DAOException 
 	 */
 	private EventEntry insertOrUpdateEventEntry(ClinicalStudyRegistration csRegn,
 			ClinicalStudyEvent event, EventEntry eventEntry, String encounterDate,
 			EventTreeObject eventTreeObj, HttpServletRequest request) throws ParseException,
 			BizLogicException, UserNotAuthorizedException, DAOException
 	{
 
 		String objectName = eventTreeObj.getObjectName();
 		if (objectName.equals(Constants.EVENT_ENTRY_OBJECT))
 		{
 			AbstractBizLogic bizlogic = new EventEntryBizlogic();
 			ActionErrors errors = new ActionErrors();
 			SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 					Constants.SESSION_DATA);
 			try
 			{
 				if (eventEntry != null && eventEntry.getId() != null)
 				{
 					List<EventEntry> eventList = bizlogic.retrieve(EventEntry.class.getName(),
 							"id", eventEntry.getId());
 					EventEntry eventEntryOld = null;
 					for (EventEntry entry : eventList)
 					{
 						eventEntryOld = entry;
 					}
 					eventEntry.setEncounterDate(edu.wustl.common.util.Utility
							.parseDate(encounterDate,Constants.DATE_PATTERN_DD_MM_YYYY));
 					bizlogic.update(eventEntry, eventEntryOld, Constants.HIBERNATE_DAO,
 							sessionDataBean);
 
 					errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError(
 							"eventEntry.date.update"));
 				}
 				else
 				{
 					String entryNumber = eventTreeObj.getEventEntryNumber();
 					eventEntry = new EventEntry();
 					eventEntry.setActivityStatus(Constants.ACTIVITY_STATUS_ACTIVE);
 					eventEntry.setClinicalStudyEvent(event);
 					eventEntry.setClinicalStudyRegistration(csRegn);
 					eventEntry.setEncounterDate(edu.wustl.common.util.Utility
							.parseDate(encounterDate,Constants.DATE_PATTERN_DD_MM_YYYY));
 					eventEntry.setEntryNumber(Integer.parseInt(entryNumber));
 					bizlogic.insert(eventEntry, sessionDataBean, Constants.HIBERNATE_DAO);
 					errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError(
 							"eventEntry.date.insert"));
 				}
 			}
 			catch (BizLogicException excp)
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item", excp
 						.getMessage()));
 				String key = (String) request.getSession().getAttribute(
 						AnnotationConstants.TREE_VIEW_KEY);
 				EventTreeObject treeObject = new EventTreeObject(key);
 				new DataEntryUtil().genrateEventKey(treeObject);
 				String treeViewKey = treeObject.createKeyFromObject();
 				request.getSession().setAttribute(AnnotationConstants.TREE_VIEW_KEY, treeViewKey);
 			}
 			saveErrors(request, errors);
 		}
 
 		return eventEntry;
 	}
 
 	/**
 	 * 
 	 * @param formsXML
 	 * @param studyId
 	 * @param participantId
 	 * @param studyEvent
 	 * @param eventEntry
 	 * @param request
 	 * @param csRegn
 	 * @param eventTreeObj
 	 * @param eventEntryForm
 	 * @throws DAOException
 	 * @throws CacheException 
 	 * @throws SQLException 
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws BizLogicException 
 	 * @throws UserNotAuthorizedException 
 	 * @throws ClassNotFoundException 
 	 */
 	private String formXMLForEvent(StringBuffer formsXML, String studyId, String participantId,
 			ClinicalStudyEvent studyEvent, EventEntry eventEntry, HttpServletRequest request,
 			ClinicalStudyRegistration csRegn, EventTreeObject eventTreeObj,
 			EventEntryForm eventEntryForm) throws DAOException, CacheException,
 			DynamicExtensionsSystemException, DynamicExtensionsApplicationException, SQLException,
 			UserNotAuthorizedException, BizLogicException, ClassNotFoundException
 	{
 		SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 				Constants.SESSION_DATA);
 
 		request.setAttribute(Constants.INFINITE_ENTRY, studyEvent.getIsInfiniteEntry());
 
 		DataEntryUtil dataEntryUtil = new DataEntryUtil();
 		
 		if (studyEvent.getIsInfiniteEntry() != null
 				&& studyEvent.getIsInfiniteEntry().booleanValue()
 				&& request.getParameter("addEntry") != null)
 		{
 			ActionErrors err = dataEntryUtil.getClosedStatus(request, participantId, studyId,
 					csRegn.getId().toString());
 			if (!err.isEmpty())
 			{
 				saveErrors(request, err);
 			}
 			else
 			{
 				List<EventEntry> eventEntryList = dataEntryUtil.getEventEntry(studyEvent, csRegn);
 				dataEntryUtil.createEventEntry(studyEvent, csRegn, eventEntryList.size() + 1,
 						request);
 			}
 			request.setAttribute(Constants.IS_TO_REFRESH_TREE, Constants.TRUE);
 		}
 
 		int noOfEntries = 1;
 		int nodeId = 1;
 
 		commonSerialNo = 0;
 		int entryCount = 0;
 		if (studyEvent.getIsInfiniteEntry() != null && studyEvent.getIsInfiniteEntry())
 		{
 			List<EventEntry> evntNtry = dataEntryUtil.getEventEntry(studyEvent, csRegn);
 			if (!evntNtry.isEmpty())
 			{
 				entryCount = evntNtry.size();
 			}
 		}
 		else
 		{
 			entryCount = studyEvent.getNoOfEntries();
 		}
 		
 		request.setAttribute("numberOfVisits", String.valueOf(entryCount));
 		//Map studyFormMap = new HashMap();
 		List studyFormMap = new ArrayList();
 
 		String userId = sessionDataBean.getUserId().toString();
 		String userName = sessionDataBean.getUserName().toString();
 		
 		for (; noOfEntries <= entryCount; noOfEntries++)
 		{
 
 			eventEntry = new EventEntry();
 			eventEntry = getEventEntry(noOfEntries, eventEntry, csRegn, studyEvent);
 			StringBuffer studyFormHQL = new StringBuffer();
             studyFormHQL.append("from ").append(StudyFormContext.class.getName());
             studyFormHQL.append(" as studyFrm where studyFrm.clinicalStudyEvent.id=");
             studyFormHQL.append(studyEvent.getId()).append(" and studyFrm.activityStatus!='");
             studyFormHQL.append(Constants.ACTIVITY_STATUS_DISABLED);
             studyFormHQL.append("' order by studyFrm.id asc");
      
             Collection<StudyFormContext> studyFormColl = (Collection<StudyFormContext>) Utility
 					.executeQuery(studyFormHQL.toString());
 			if (studyFormColl != null)
 			{
 				if(studyFormMap.isEmpty())
 				{
 					for (StudyFormContext studyFormContext : studyFormColl)
 					{
 						Map<String, Boolean> privMap = Utility.checkFormPrivileges(studyFormContext
 								.getId().toString(), userId, userName);
 						boolean hideForm = privMap.get(Constants.HIDEFORMS);
 						if (!hideForm)
 						{
 							NameValueBean nameValueBean = new NameValueBean();
 							nameValueBean.setName(studyFormContext.getId());
 							nameValueBean.setValue(studyFormContext.getStudyFormLabel());
 							studyFormMap.add(nameValueBean);
 						}
 					}
 					request.setAttribute("studyFormMap", studyFormMap);
 				}
 				formsXML = generateFormXMLForEachEntry(formsXML, studyId, participantId, nodeId,
 						studyFormColl, eventEntry, studyEvent, csRegn.getId(), noOfEntries,
 						eventTreeObj, eventEntryForm, sessionDataBean);
 			}
 			nodeId++;
 		}
 
 		return formsXML.toString();
 	}
 
 	/**
 	 * This method forms XML required to populate spreadsheet on event click
 	 * @param formsXML
 	 * @param studyId
 	 * @param participantId
 	 * @param nodeId
 	 * @param studyFormColl
 	 * @param eventEntry
 	 * @param studyEvent
 	 * @param regId
 	 * @param eventEntryCount
 	 * @param eventTreeObj
 	 * @param eventEntryForm
 	 * @param sessionDataBean
 	 * @return
 	 * @throws DAOException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws CacheException
 	 * @throws SQLException
 	 */
 	private StringBuffer generateFormXMLForEachEntry(StringBuffer formsXML, String studyId,
 			String participantId, int nodeId, Collection<StudyFormContext> studyFormColl,
 			EventEntry eventEntry, ClinicalStudyEvent studyEvent, Long regId, int eventEntryCount,
 			EventTreeObject eventTreeObj, EventEntryForm eventEntryForm,
 			SessionDataBean sessionDataBean) throws DAOException, DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException, CacheException, SQLException
 	{
 		String userId = sessionDataBean.getUserId().toString();
 		String userName = sessionDataBean.getUserName().toString();
 
 		CatissueCoreCacheManager cache = CatissueCoreCacheManager.getInstance();
 		String recEntryEntityId = cache.getObjectFromCache(
 				AnnotationConstants.RECORD_ENTRY_ENTITY_ID).toString();
 		Date encounterDate = null;
 
 		boolean isCsEventObject = false;
 		if (eventTreeObj.getObjectName() != null
 				&& eventTreeObj.getObjectName().equals(
 						Constants.CLINICAL_STUDY_EVENT_OBJECT))
 		{
 			isCsEventObject = true;
 		}
 		
 		Collection recIdList = new HashSet();
 		AnnotationBizLogic annoBizLogic = new AnnotationBizLogic();
 		for (StudyFormContext studyFormContext : studyFormColl)
 		{
 			Map<String, Boolean> privilegeMap = Utility.checkFormPrivileges(studyFormContext.getId()
 					.toString(), userId, userName);
 			boolean hideForm = privilegeMap.get(Constants.HIDEFORMS);
 			readOnly = privilegeMap.get(Constants.READ_ONLY);
 			if (!hideForm)
 			{
 
 				boolean flag = false;
 				String eventEntryId = "";
 				if (nodeId < 0)
 				{
 					eventEntryId = String.valueOf(nodeId);
 				}
 				else
 				{
 					eventEntryId = "-" + nodeId;
 				}
 
 				String recEntryId = "0";
 				String dateString = "";
 				String dynamicRecId = "0";
 				int frmCount = 0;
 				if (eventEntry != null && eventEntry.getId() != null)
 				{
 					eventEntryId = eventEntry.getId().toString();
 
 					encounterDate = eventEntry.getEncounterDate();
 					if (encounterDate != null)
 					{
 						SimpleDateFormat customFormat = new SimpleDateFormat("MMM-dd-yyyy");
 						dateString = dateString + customFormat.format(encounterDate);
 					}
 					if (eventEntry.getEntryNumber() != null)
 					{
 						frmCount = eventEntry.getEntryNumber();
 					}
 
 					//get records 
 					StringBuffer recEntryHQL = new StringBuffer();
 					recEntryHQL.append("select recEntry.id from ");
 					recEntryHQL.append(RecordEntry.class.getName());
 					recEntryHQL.append(" recEntry where recEntry.eventEntry.id=");
 					recEntryHQL.append(eventEntry.getId());
 					recEntryHQL.append(" and recEntry.studyFormContext.id=");
 					recEntryHQL.append(studyFormContext.getId());
 					recEntryHQL.append(" order by recEntry.id");
 
 					List<Long> recEntryColl = new DataEntryUtil().executeQuery(recEntryHQL
 							.toString());
 					if (recEntryColl != null && !recEntryColl.isEmpty())
 					{
 						commonSerialNo = 0;
 						for (Long entryId : recEntryColl)
 						{
 							commonSerialNo++;
 							flag = true;
 							recEntryId = entryId.toString();
 							recIdList = annoBizLogic.getDynamicRecordFromStaticId(recEntryId,
 									studyFormContext.getContainerId(), recEntryEntityId);
 							if (recIdList != null && !recIdList.isEmpty())
 							{
 								dynamicRecId = ((Long) recIdList.iterator().next()).toString();
 							}
 
 							String dyRecordurl = getDynamicRecordURL(studyId, participantId,
 									studyEvent.getId().toString(), studyFormContext, regId
 											.toString(), frmCount, eventEntryId, recEntryId,
 									dynamicRecId);
 
 							if (eventTreeObj.getObjectName() != null
 									&& eventTreeObj.getObjectName().equals(
 											Constants.CLINICAL_STUDY_EVENT_OBJECT))
 							{
 								getEntryURL(studyId, participantId, studyEvent.getId().toString(),
 										regId.toString(), eventEntryId, frmCount);
 							}
 
 							formsXML = formXml(formsXML, studyFormContext, dynamicRecId,
 									dyRecordurl, frmCount, commonSerialNo, dateString,
 									eventEntryForm, isCsEventObject);
 						}
 					}
 				}
 				else
 				{
 					frmCount = eventEntryCount;
 				}
 				if (!flag && !eventTreeObj.getObjectName().equals(Constants.FORM_ENTRY_OBJECT))
 				{
 					//commonSerialNo++;
 					commonSerialNo = 0;
 					String url = getDynamicRecordURL(studyId, participantId, studyEvent.getId()
 							.toString(), studyFormContext, regId.toString(), frmCount,
 							eventEntryId, recEntryId, dynamicRecId);
 
 					if (eventTreeObj.getObjectName() != null
 							&& eventTreeObj.getObjectName().equals(
 									Constants.CLINICAL_STUDY_EVENT_OBJECT))
 					{
 						getEntryURL(studyId, participantId, studyEvent.getId().toString(), regId
 								.toString(), eventEntryId, frmCount);
 					}
 
 					formsXML = formXml(formsXML, studyFormContext, dynamicRecId, url, frmCount,
 							commonSerialNo, dateString, eventEntryForm, isCsEventObject);
 				}
 			}
 		}
 		return formsXML;
 
 	}
 
 	/**
 	 * returns event entry object as per given entry number
 	 * @param noOfEntries
 	 * @param eventEntry
 	 * @param CSRegistration
 	 * @param event
 	 * @return
 	 * @throws BizLogicException 
 	 */
 	private EventEntry getEventEntry(Integer noOfEntries, EventEntry eventEntry,
 			ClinicalStudyRegistration CSRegistration, ClinicalStudyEvent event)
 			throws BizLogicException //throws DAOException
 	{
 
 		DataEntryUtil dataEntryUtil = new DataEntryUtil();
 		List<EventEntry> eventEntryCol = dataEntryUtil.getEventEntryFromEntryNum(event,
 				CSRegistration, noOfEntries);
 		if (eventEntryCol != null && !eventEntryCol.isEmpty())
 		{
 			eventEntry = eventEntryCol.get(0);
 		}
 		return eventEntry;
 	}
 
 	/**
 	 * This method forms XML format required to populate grid using DHTML 
 	 * @param formsXML
 	 * @param studyFormContext
 	 * @param dynamicRecId
 	 * @param url
 	 * @param frmCount
 	 * @param serialNo
 	 * @param dateString
 	 * @param eventEntryForm
 	 * @param isCsEventObj
 	 * @return
 	 */
 	private StringBuffer formXml(StringBuffer formsXML, StudyFormContext studyFormContext,
 			String dynamicRecId, String url, int frmCount, int serialNo, String dateString,
 			EventEntryForm eventEntryForm, boolean isCsEventObj)
 	{
 		String operation = "";
 		if ("0".equals(dynamicRecId))
 		{
 			operation = Constants.ADVANCED_QUERY_ADD;
 		}
 		else
 		{
 			String csStatus = eventEntryForm.getCSActivityStatus();
 			String csrStatus = eventEntryForm.getCSRActivityStatus();
 			String partiStatus = eventEntryForm.getParticipantActivityStatus();
 			if ((csStatus != null && csStatus.equals(Constants.ACTIVITY_STATUS_CLOSED))
 					|| (csrStatus != null && csrStatus.equals(Constants.ACTIVITY_STATUS_CLOSED))
 					|| (partiStatus != null && partiStatus.equals(Constants.ACTIVITY_STATUS_CLOSED)))
 			{
 				operation = Constants.VIEW;
 			}
 			else
 			{
 				operation = Constants.ADVANCED_QUERY_EDIT;
 			}
 		}
 		if (readOnly)
 		{
 			operation = Constants.VIEW;
 		}
 
 		//String entryText = "Visit-";//+ frmCount;
 		formsXML.append("<row><cell>");
 		if(isCsEventObj)
 		{
 			//String entryText = "Visit-" + frmCount + dateString;
 			formsXML.append(frmCount);
 			formsXML.append("</cell><cell>");
 			formsXML.append(dateString);
 			formsXML.append("</cell><cell>");
 		}
 		
 		formsXML.append(studyFormContext.getStudyFormLabel());
 		if(studyFormContext.getCanHaveMultipleRecords() && serialNo != 0)
 		{
 			formsXML.append(" (Record-" + serialNo + ")");
 		}
 		formsXML.append("</cell><cell>" + operation + "^" + url + "^_self</cell></row>");
 
 		return formsXML;
 
 	}
 
 	/**
 	 * @param studyId
 	 * @param participantId
 	 * @param eventId
 	 * @param regId
 	 * @param eventEntryId
 	 * @param entryNumber
 	 * @return
 	 */
 	private String getEntryURL(String studyId, String participantId, String eventId, String regId,
 			String eventEntryId, int entryNumber)
 	{
 		StringBuffer eventEntryURL = new StringBuffer("LoadEventDetails.do?");
 
 		StringBuffer treeKey = new StringBuffer(Constants.EVENT_ENTRY_OBJECT);
 		treeKey.append("_" + eventId + "_0_0_" + regId + "_" + eventEntryId + "_" + entryNumber
 				+ "_0_0");
 
 		eventEntryURL.append(Constants.CP_SEARCH_PARTICIPANT_ID);
 		eventEntryURL.append("=");
 		eventEntryURL.append(participantId);
 		eventEntryURL.append("&amp;");
 		eventEntryURL.append(Constants.CP_SEARCH_CP_ID);
 		eventEntryURL.append("=");
 		eventEntryURL.append(studyId);
 		eventEntryURL.append("&amp;");
 		eventEntryURL.append(Constants.PROTOCOL_EVENT_ID);
 		eventEntryURL.append("=");
 		eventEntryURL.append(eventId);
 		eventEntryURL.append("&amp;entryId=");
 		eventEntryURL.append(eventEntryId);
 		eventEntryURL.append("&amp;treeViewKey=");
 		eventEntryURL.append(treeKey.toString());
 		eventEntryURL.append("&amp;");
 		eventEntryURL.append(Utility.attachDummyParam());
 
 		return eventEntryURL.toString();
 	}
 
 	/**
 	 * 
 	 * @param studyId
 	 * @param participantId
 	 * @param eventId
 	 * @param formContext
 	 * @param regId
 	 * @param eventEntryId
 	 * @param recEntryId
 	 * @param dynamicRecId
 	 * @return
 	 */
 	private String getDynamicRecordURL(String studyId, String participantId, String eventId,
 			StudyFormContext formContext, String regId, int eventEntryCount, String eventEntryId,
 			String recEntryId, String dynamicRecId)
 	{
 		StringBuffer url = new StringBuffer();
 
 		String objectName = "";
 		url.append("LoadDynamicExtentionsDataEntryPage.do?formId=");
 		url.append(formContext.getContainerId());
 		url.append("&amp;" + Constants.CP_SEARCH_PARTICIPANT_ID + "=" + participantId);
 		url.append("&amp;" + Constants.CP_SEARCH_CP_ID + "=" + studyId);
 		url.append("&amp;" + AnnotationConstants.FORM_CONTEXT_ID + "=" + formContext.getId());
 		url.append("&amp;" + Constants.PROTOCOL_EVENT_ID + "=" + eventId);
 		if (dynamicRecId != null && !dynamicRecId.equals("0"))
 		{
 			url.append("&amp;recordId=");
 			url.append(dynamicRecId);
 			/*
 			 * If the study form is having single record then set it's object name as
 			 * SingleRecordFormEdit
 			 */
 			if (formContext.getCanHaveMultipleRecords())
 			{
 				objectName = Constants.FORM_CONTEXT_OBJECT;
 			}
 			else
 			{
 				objectName = Constants.SINGLE_RECORD_FORM_EDIT;
 			}
 		}
 		else
 		{
 			dynamicRecId = "0";
 			recEntryId = "0";
 			/*
 			 * If the study form is having single record then set it's object name as
 			 * SingleRecordForm
 			 */
 			if (formContext.getCanHaveMultipleRecords())
 			{
 				objectName = Constants.FORM_ENTRY_OBJECT;
 			}
 			else
 			{
 				objectName = Constants.SINGLE_RECORD_FORM;
 			}
 		}
 
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(objectName);
 		buffer.append('_');
 		buffer.append(eventId);
 		buffer.append('_');
 		buffer.append(formContext.getContainerId());
 		buffer.append('_');
 		buffer.append(formContext.getId());
 		buffer.append('_');
 		buffer.append(regId);
 		buffer.append('_');
 		buffer.append(eventEntryId);
 		buffer.append('_');
 		buffer.append(eventEntryCount);
 		buffer.append('_');
 		buffer.append(recEntryId);
 		buffer.append('_');
 		buffer.append(dynamicRecId);
 		String treeViewKey = buffer.toString();
 		url.append("&amp;");
 		url.append(AnnotationConstants.TREE_VIEW_KEY);
 		url.append("=");
 		url.append(treeViewKey);
 		url.append("&amp;");
 		url.append(Constants.FORWARD_CONTROLLER);
 		url.append("=CSBasedSearch&amp;");
 		url.append(Utility.attachDummyParam());
 
 		return url.toString();
 	}
 
 	/**
 	 * @param strBuffer
 	 * @param request
 	 * @return
 	 * @throws IOException
 	 */
 	private String generateXMLFile(StringBuffer strBuffer, HttpServletRequest request)
 			throws IOException
 	{
 		String xmlFileName = getFileName(request);
 		deleteXMLFilesIfExists(xmlFileName);
 		File outFile = new File(Variables.applicationHome + File.separator + xmlFileName);
 		FileWriter out = new FileWriter(outFile);
 		out.write(strBuffer.toString());
 		out.close();
 
 		return xmlFileName;
 
 	}
 
 	/**
 	 * returns xmlFileName
 	 * @param request
 	 * @return
 	 */
 	private String getFileName(HttpServletRequest request)
 	{
 		String sessionId = request.getSession().getId();
 		return "gridData" + sessionId + Math.random() + ".xml";
 	}
 
 	/**
 	 * Deleting existing XML file that starts with 'Test'
 	 * @param baseFolder
 	 */
 	public void deleteXMLFilesIfExists(String filename)
 	{
 
 		File file = new File(Variables.applicationHome + File.separator + filename);
 		if (file.exists())
 		{
 			file.delete();
 		}
 
 	}
 
 }
