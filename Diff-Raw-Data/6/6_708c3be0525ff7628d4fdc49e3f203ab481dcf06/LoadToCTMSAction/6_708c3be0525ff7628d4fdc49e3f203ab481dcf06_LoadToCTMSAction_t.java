 /**
  * 
  */
 package gov.nih.nci.caxchange.ctom.viewer.actions;
 
 import gov.nih.nci.c3d.webservices.client.C3DGridServiceClient;
 import gov.nih.nci.caxchange.ctom.viewer.constants.DisplayConstants;
 import gov.nih.nci.caxchange.ctom.viewer.constants.ForwardConstants;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LabActivitiesSearchResultForm;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LoginForm;
 import gov.nih.nci.caxchange.ctom.viewer.viewobjects.LabActivityResult;
 import gov.nih.nci.labhub.domain.II;
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 import gov.nih.nci.security.exceptions.CSException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 import webservices.Acknowledgement;
 import webservices.Activity;
 import webservices.Documentation;
 import webservices.LabResult;
 import webservices.Laboratory;
 import webservices.LoadLabsRequest;
 import webservices.ObservationResult;
 import webservices.Organization;
 import webservices.Participant;
 import webservices.StudyProtocol;
 import webservices.StudySubject;
 
 /**
  * This class performs the Load to CTMS action. It loads the selected form data to CTMS.
  * It checks if valid login information is in session; if not it redirects the user to login page.
  * 
  * @author asharma
  *
  */
 public class LoadToCTMSAction extends Action
 {
 	
 	private static final Logger logDB = Logger.getLogger(LoadToCTMSAction.class);
 
 	/* (non-Javadoc)
 	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		LabActivitiesSearchResultForm lForm = (LabActivitiesSearchResultForm) form;
 		  //if the session is new or the login object is null; redirects the user to login page  
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null))
 		{
 			if (logDB.isDebugEnabled())
 				logDB.debug("||"+lForm.getFormName()+"|create|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());		
 		
 		try
 		{  //calls the loadToCTMS method
 			loadToCTMS(request, lForm);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, "Messages Submitted to CTMS Successfully"));
 			saveMessages( request, messages );
 		}
		catch (Exception cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Error in Submitting Messages to CTMS"));
 			saveErrors( request,errors );
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+lForm.getFormName()+"|create|Failure|Error Adding the "+lForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		session.setAttribute(DisplayConstants.CURRENT_FORM, lForm);
 		
 		//if the login is valid and the selected form data is successfully loaded to CTMS; 
 		//it returns to the search results page and displays the load successful message
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+lForm.getFormName()+"|create|Success|Adding a  new "+lForm.getFormName()+" object|"
 				+form.toString()+"|");	
 		
 		return (mapping.findForward(ForwardConstants.LOAD_TO_CTMS_EVENT_SUCCESS));
 	}
 	
 	 /**
 	 * Collects the selectd form data and calls the EvenManager sendLabActivitiesmethod to
 	 * load the data to CTMS
 	 * @param request
 	 * @param form
 	 * @throws Exception
 	 */
 	private void loadToCTMS(HttpServletRequest request,ActionForm form) throws Exception
 	{
 	    LabActivitiesSearchResultForm lForm = (LabActivitiesSearchResultForm)form;
 		HashMap map = (HashMap) request.getSession().getAttribute("RESULT_SET");
 		ArrayList list = new ArrayList();
 		String test = lForm.getRecordId();
 		StringTokenizer stringTokenizer = new StringTokenizer(test, ",");
 		int count = stringTokenizer.countTokens();
 		
 		// Create the list of results to send
 		if (count >= 1)
 		{
 			while (stringTokenizer.hasMoreTokens())
 			{
 				list.add(map.get(stringTokenizer.nextToken()));
 			}
 		}
 		else
 		{
 			list.add(map.get(lForm.getRecordId()));
 		}
 
 		// Then create the request
 		String url = "http://137.187.183.154:8080/wsrf/services/cagrid/C3DGridService";
 		C3DGridServiceClient client = new C3DGridServiceClient(url);
 		LoadLabsRequest labRequest = new LoadLabsRequest();
 		
 		// Then for each lab selected set the lab information
 		LabResult labResults[]= new LabResult[list.size()];
 		int i = 0;
 		
 		for (Iterator labs = list.iterator(); labs.hasNext();)
 		{
 			LabActivityResult lab = (LabActivityResult)labs.next();
 			
 			// Populate the study information
 			StudyProtocol studyProtocol= new StudyProtocol();
 			Documentation documentation=new Documentation();
 			Organization organization= new Organization();
 			
 			String studyId = lab.getStudyId();
 			if (studyId != null)
 				documentation.setIdentifier(studyId);
 			studyProtocol.setDocumentation(documentation);
 			organization.setStudyProtocol(studyProtocol);
 			
 			// Then set the participant and study subject assignment identifiers
 			Participant participant= new Participant();
 			StudySubject studySubject= new StudySubject();
 			
 			Collection<II> studySubjectIds = lab.getSubjectAssignment().getStudySubjectIdentifier();
 			if (studySubjectIds != null && studySubjectIds.size() > 0)
 			{
 				Iterator<II> idIterator = studySubjectIds.iterator();
 				studySubject.setIdentifier(idIterator.next().getExtension());
 			}
 			studySubject.setOrganization(organization);
 			studySubject.setParticipant(participant);
 			
 			LabResult labResult = new LabResult();
 			
 			labResult.setStudySubject(studySubject);
 			
 			// Set the lab identifier
 			Laboratory laboratory= new Laboratory();
 			String labIdentifier = lab.getLabResult().getPerformingLaboratory().getIdentifier();
 			if (labIdentifier != null)
 				laboratory.setIdentifier(labIdentifier);
 			labResult.setLaboratory(laboratory);
 			
 			// Set the activity name
 			Activity activity= new Activity();
 			//activity.setName(testName);
 			labResult.setActivity(activity);
 			
 			// Set the observation result
 			ObservationResult observationResult= new ObservationResult();
 			Date labDate = lab.getActualDate();
 			if (labDate != null)
 				observationResult.setReportedDateTime(labDate);
 			labResult.setObservationResult(observationResult);
 			
 			// Set the lab result details
 			String testName = lab.getLabTestId();
 			if (testName != null)
 				labResult.setName(testName);
 			String result = lab.getNumericResult();
 			if (result != null)
 				labResult.setTextResult(result);
 			String labUom = lab.getUnitOfMeasure();
 			if (labUom != null)
 				labResult.setUnitOfMeasureCodingSchema(labUom);
 			String lowRange = lab.getLowRange();
 			if (lowRange != null)
 				labResult.setReferenceRangeLow(lowRange);
 			String highRange = lab.getHighRange();
 			if (highRange != null)
 				labResult.setReferenceRangeHigh(highRange);
 			
 			labResults[i] = labResult;
 			i++;
 		}
 		
 		labRequest.setLabResult(labResults);
 		
 		// Now send the load labs request
 		Acknowledgement acknowledgement = client.loadLabs(labRequest);
 	
 		lForm.setRecordId("");
 		lForm.setRecordId(null);
 	}	
 
 }
