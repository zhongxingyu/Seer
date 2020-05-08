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
 
 package org.openmrs.module.feedback.web;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.feedback.Feedback;
 import org.openmrs.module.feedback.FeedbackService;
 import org.openmrs.notification.Message;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 public class AddFeedbackFormController extends SimpleFormController {
 	
     /** Logger for this class and subclasses */
     protected final Log log = LogFactory.getLog(getClass());
 
 	@Override
 	protected Boolean formBackingObject(HttpServletRequest request) throws Exception {
             
             /*To check wheather or not the subject , severity and feedback is empty or not*/
                 Boolean feedbackMessage = false ;
 		String text = "";
 
                 if (request.getParameter("subject") != null && request.getParameter("severity") != null  && request.getParameter("feedback") != null  )
                 {
                     Object o = Context.getService(FeedbackService.class);
                     FeedbackService service = (FeedbackService)o;                 
                     Feedback s = new Feedback() ;
                     s.setSubject(request.getParameter("subject"));
                     s.setSeverity(request.getParameter("severity"));
                     
                     /*To get the Stacktrace of the page from which the feedback is submitted*/
                     
                     StackTraceElement[] c = Thread.currentThread().getStackTrace() ;
                     String feedback =    request.getParameter("feedback")  ;
                     for (int i = 0 ; i < c.length ; i++ ){
                         feedback = feedback + "\n" + c[i].getFileName() + c[i].getMethodName() + c[i].getClass() + c[i].getLineNumber() ;
                     }
                     
                     /*The feedback content length can't be greater then the 5000 characters , in case it is more then that then it is truncated to the first 5000 characters*/
 
                     s.setContent( feedback );
                                          
                     /*file upload in multiplerequest*/
                     if (request instanceof MultipartHttpServletRequest) 
                     {
                         MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                         MultipartFile file = (MultipartFile) multipartRequest.getFile("file");
                         
                         if ( !file.isEmpty() )
                         {
                             if (file.getSize() <= 5242880 )
                             {
                                 if (file.getOriginalFilename().endsWith(".jpeg")||file.getOriginalFilename().endsWith(".jpg")|| file.getOriginalFilename().endsWith(".gif")|| file.getOriginalFilename().endsWith(".png"))
                                 {
                                     s.setMessage(file.getBytes());
                                 }
                                 else
                                 {
                                     return false ;
                                 }
                             }
                             else
                             {
                                 return false ;
                             }
                         }
                         
                     }
                     /*Save the Feedback*/
                     service.saveFeedback(s) ;  
 		    
 		    if(	"Yes".equals(Context.getUserContext().getAuthenticatedUser().getUserProperty("feedback_notificationReceipt")))
 		    {
 			try {
 			// Create Message
 			Message message = new Message();
 			message.setSender( Context.getAdministrationService().getGlobalProperty("feedback.notification.email") );
 			message.setRecipients( Context.getUserContext().getAuthenticatedUser().getUserProperty("feedback_email") );
 			message.setSubject( "Feedback submission confirmation mail" ); 
 			message.setContent( Context.getAdministrationService().getGlobalProperty("feedback.notification") + "Ticket Number: " + s.getFeedbackId() + " Subject :" + s.getSubject()   );
 			message.setSentDate( new Date() );
 		        // Send message 
 			Context.getMessageService().send( message );
 		    }
 		    catch (Exception e)
 		    {
 			    log.error("Unable to sent the email to the Email : " + Context.getUserContext().getAuthenticatedUser().getUserProperty("feedback_email") ) ;
 		    } 
 		    }
 		    		    
 		try {
 			// Create Message
 			Message message = new Message();
 			message.setSender( Context.getAdministrationService().getGlobalProperty("feedback.notification.email") );
 			message.setRecipients( Context.getAdministrationService().getGlobalProperty("feedback.admin.notification.email") );
			message.setSubject( "New feedback submiited" ); 
			message.setContent( Context.getAdministrationService().getGlobalProperty("feedback.admin.notification") + "Ticket Number: " + s.getFeedbackId() + " Subject :" + s.getSubject()   );
 			message.setSentDate( new Date() );
 		        // Send message 
 			Context.getMessageService().send( message );
 		    }
 		    catch (Exception e)
 		    {
 			    log.error("Unable to sent the email to the Email : " + Context.getUserContext().getAuthenticatedUser().getUserProperty("feedback.admin.notification.email") ) ;
 		    } 		    
 		    		                     
                     feedbackMessage = true;
 
                     
                 }
                 /*Reserved for future use for showing that the data is saved and the feedback is submitted*/			
 		
 		log.debug("Returning hello world text: " + text);
 		
 		return feedbackMessage;
 		
 	}
 
 	@Override
 	protected Map referenceData(HttpServletRequest req) throws Exception {
 		
 		Map<String, Object> map = new HashMap<String, Object>();
 		/*Return List of Predefined Subjects and Severities for the feedback submission form*/
                 
 		FeedbackService hService = (FeedbackService)Context.getService(FeedbackService.class);
 		map.put("predefinedsubjects", hService.getPredefinedSubjects()) ;
                 map.put("severities", hService.getSeverities() ) ;		
                 if (req.getParameter("feedbackPageMessage")!= null && ServletRequestUtils.getBooleanParameter(req, "feedbackPageMessage")) 
                 {
                       map.put("feedbackPageMessage", Context.getAdministrationService().getGlobalProperty("feedback.ui.notification") ) ;		
                 }
                 else if (req.getParameter("feedbackPageMessage")!= null &&  !ServletRequestUtils.getBooleanParameter(req, "feedbackPageMessage"))
                 {
                       map.put("feedbackPageMessage", "feedback.notification.feedback.error" ) ;		
                 }
                 else
                 {
                     map.put("feedbackPageMessage", "" ) ;	
                 }
                 return map;
 		
 	}
 	
 }
