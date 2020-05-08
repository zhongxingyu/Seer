 /*
 
 Copyright (c) 2012 Roger Sicart. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are
 met:
 
     (1) Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer. 
 
     (2) Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the
     distribution.  
     
     (3)The name of the author may not be used to
     endorse or promote products derived from this software without
     specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
  
  */
 
 /*
  * @author R.Sicart
  */
 
 package com.mpwc.worker;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletException;
 import javax.portlet.PortletSession;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 
 import com.liferay.counter.service.CounterLocalServiceUtil;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.json.JSONArray;
 import com.liferay.portal.kernel.json.JSONException;
 import com.liferay.portal.kernel.json.JSONFactoryUtil;
 import com.liferay.portal.kernel.json.JSONObject;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 import com.mpwc.model.Worker;
 import com.mpwc.service.WorkerLocalServiceUtil;
 
 /**
  * Portlet implementation class WorkerPortlet
  */
 public class WorkerPortlet extends MVCPortlet {
 
     public void addWorker(ActionRequest actionRequest, ActionResponse actionResponse)
    	       throws IOException, PortletException{
      	
     	ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
     	
     	long liferayUserId = Long.parseLong(actionRequest.getParameter("liferayUserId"));
      	String name = actionRequest.getParameter("name");
      	String surname = actionRequest.getParameter("surname");
      	String nif = actionRequest.getParameter("nif");
      	String email = actionRequest.getParameter("email");
      	String phone = actionRequest.getParameter("phone");
      	long status = Long.parseLong(actionRequest.getParameter("status"));
 
      	String comments = actionRequest.getParameter("comments");
      	Date now = new Date();
      	
      	if(		name != null && !name.isEmpty() &&
      			surname != null && !surname.isEmpty() &&
      			email != null && !email.isEmpty() && email.indexOf("@") > 0 &&
      			nif != null && !nif.isEmpty() 
      		){
      		
  	    	Worker w;
  			try {
  				long workerId = CounterLocalServiceUtil.increment(Worker.class.getName());
  				w = WorkerLocalServiceUtil.createWorker(workerId);
  				w.setName(name);
  		    	w.setSurname(surname);
  		    	w.setNif(nif);
  		    	w.setEmail(email);
  		    	if( phone != null && !phone.isEmpty() ){ w.setPhone(phone); }
  		    	if( status > 0 ){ w.setStatusId(status); }
  		    	if( comments != null && !comments.isEmpty() ){ w.setComments(comments); }
  		    	w.setCreateDate(now);
  		    	
  				//w.setUserName(themeDisplay.getUserName());
  				w.setCompanyId(themeDisplay.getCompanyId());
  				//w.setGroupId(themeDisplay.getCompanyGroupId());
  				w.setGroupId(themeDisplay.getScopeGroupId());
  				w.setUserId(liferayUserId);
  		    	
  		    	//WorkerLocalServiceUtil.addWorker(w);
  		    	WorkerLocalServiceUtil.addWorker(w, liferayUserId);
  		    	
  		    	System.out.println("addWorker userId" + liferayUserId + "groupId:" + w.getGroupId() + "companyId:" + w.getCompanyId());
  		    	
  			} catch (SystemException e) {
  				System.out.println("addWorker exception:" + e.getMessage());
  			} catch (PortalException e) {
  				System.out.println("addWorker exception2:" + e.getMessage());
 			}
 
      	}
 
      	// gracefully redirecting to the default portlet view
      	String redirectURL = actionRequest.getParameter("redirectURL");
      	actionResponse.sendRedirect(redirectURL);
       	
       }
 
      
      public void editWorker(ActionRequest actionRequest, ActionResponse actionResponse)
      	       throws IOException, PortletException {
      	
      	long workerId = Long.valueOf( actionRequest.getParameter("workerId") );
      	String name = actionRequest.getParameter("name");
      	String surname = actionRequest.getParameter("surname");
      	String nif = actionRequest.getParameter("nif");
      	String email = actionRequest.getParameter("email");
      	String phone = actionRequest.getParameter("phone");
      	long status = Long.parseLong(actionRequest.getParameter("status"));
      	String comments = actionRequest.getParameter("comments");
      	Date now = new Date();
      	
      	if( workerId > 0 ){
      		
  	    	Worker w;
  			try {			
  				w = WorkerLocalServiceUtil.getWorker(workerId);
  				if( name != null && !name.isEmpty() ){ w.setName(name); }
  				if( surname != null && !surname.isEmpty() ){ w.setSurname(surname); }
  				if( nif != null && !nif.isEmpty() ){ w.setNif(nif); }
  				if( email != null && !email.isEmpty() && email.indexOf("@") > 0 ){ w.setEmail(email); }
  		    	if( phone != null && !phone.isEmpty() ){ w.setPhone(phone); }
  		    	if( status > 0 ){ w.setStatusId(status); }
  		    	if( comments != null && !comments.isEmpty() ){ w.setComments(comments); }
  		    	w.setModifiedDate(now);
  		    	WorkerLocalServiceUtil.updateWorker(w);
  			} catch (SystemException e) {
  				System.out.println("editWorker exception:" + e.getMessage());
  			} catch (PortalException e) {
  				System.out.println("editWorker exception:" + e.getMessage());
  			}
 
      	}
 
      	// gracefully redirecting to the default portlet view
      	String redirectURL = actionRequest.getParameter("redirectURL");
      	actionResponse.sendRedirect(redirectURL);
 
      }
      
      public void deleteWorker(ActionRequest actionRequest, ActionResponse actionResponse)
   	       throws IOException, PortletException, PortalException, SystemException {
   	
  	    //Do not delete, mark as deleted
      	
  	 	long workerId = Long.valueOf( actionRequest.getParameter("workerId") );
  	
  	 	long status = 100; //deleted status
  	 	String comments = "Deleted worker.";
  	 	Date now = new Date();
  	 	
  	 	if( workerId > 0 ){
  			try{
  				WorkerLocalServiceUtil.deleteWorker(workerId);
  				//WorkerLocalServiceUtil.delete(workerId);
  			} catch (SystemException e) {
  				System.out.println("deleteWorker exception:" + e.getMessage());
  			}
  	 	}
  	
  	 	// gracefully redirecting to the default portlet view
  	 	String redirectURL = actionRequest.getParameter("redirectURL");
  	 	actionResponse.sendRedirect(redirectURL);
 
      }
      
      public void deleteWorkers(ActionRequest actionRequest, ActionResponse actionResponse)
    	       throws IOException, PortletException {
    	
   	    //Do not delete, mark as deleted
       	
   	 	String jsonWorkerIds = actionRequest.getParameter("jsonWorkerIds");
   	
   	 	long status = 100; //deleted status
   	 	String comments = "Deleted worker.";
   	 	Date now = new Date();
   	 	
   	 	JSONArray jsonArrayWorkers = null;
  		try {
  			jsonArrayWorkers = JSONFactoryUtil.createJSONArray(jsonWorkerIds);
  		} catch (JSONException e1) {
  			System.out.println("deleteWorkers exception:" + e1.getMessage());
  		}
   	 	
   	 	if( jsonWorkerIds != null && jsonArrayWorkers != null ){ 		
   	 		
   	 		System.out.println("deleteWorkers: jsonWorkerIds ->" +jsonWorkerIds);
   	 		System.out.println("deleteWorkers: jsonArrayWorkers ->" +jsonArrayWorkers.toString());
   	 		
   	 		for(int i=0; i<jsonArrayWorkers.length(); i++){
   	 		
   		    	Worker w;
   				try {			
   					w = WorkerLocalServiceUtil.getWorker(jsonArrayWorkers.getLong(i));
   			    	w.setStatusId(status);
   			    	w.setComments(comments);
   			    	w.setModifiedDate(now);
   			    	WorkerLocalServiceUtil.updateWorker(w);
   				} catch (SystemException e) {
   					System.out.println("deleteWorkers exception:" + e.getMessage());
   				} catch (PortalException e) {
   					System.out.println("deleteWorkers exception:" + e.getMessage());
   				}
   	 			
   	 		}
   	
   	 	}
   	 	else{
   	 		System.out.println("deleteWorkers: No workers to delete");
   	 	}
   	
   	 	// gracefully redirecting to the default portlet view
   	 	String redirectURL = actionRequest.getParameter("redirectURL");
   	 	actionResponse.sendRedirect(redirectURL);
 
       }
      
      public void getWorkersByFilters(ActionRequest actionRequest, ActionResponse actionResponse)
      	       throws IOException, PortletException{
     	 try{
  			//get params
  			String desc = actionRequest.getParameter("ftrdesc");
  			String nif = actionRequest.getParameter("ftrnif");
  			String name = actionRequest.getParameter("ftrname");
  			String surname = actionRequest.getParameter("ftrsurname");
  			String email = actionRequest.getParameter("ftremail");
  			String phone = actionRequest.getParameter("ftrphone");
  			
  			System.out.println("getWorkersByFilters params-> desc:"+desc+" - nif:"+nif+" - name:"+name+" - surname:"+surname+" - email:"+email+" - phone:"+phone);
  			
  			//set session params
  			actionRequest.getPortletSession().setAttribute("ftrDesc", desc, PortletSession.PORTLET_SCOPE);
  			actionRequest.getPortletSession().setAttribute("ftrNif", nif, PortletSession.PORTLET_SCOPE);
  			actionRequest.getPortletSession().setAttribute("ftrName", name, PortletSession.PORTLET_SCOPE);
 			actionRequest.getPortletSession().setAttribute("ftrSurname", surname, PortletSession.PORTLET_SCOPE);
 			actionRequest.getPortletSession().setAttribute("ftrEmail", email, PortletSession.PORTLET_SCOPE);
 			actionRequest.getPortletSession().setAttribute("ftrPhone", phone, PortletSession.PORTLET_SCOPE);			
  			
     	 } catch (Exception e) {
 	     		System.out.println("Action getWorkersByFilters Error: " + e.getMessage() );
 	     }
      }
      
      /*
       * action used to serve data to AJAX requests (i.e. jqgrid requests)
       * */
      public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
      		throws IOException, PortletException {
 
      	String jspPage = resourceRequest.getParameter("name");
  		System.out.println("jspPage"+jspPage);
  		
  		String resourceId = resourceRequest.getResourceID();
  		System.out.println("resourceId: "+resourceId);
  		
  		if( "getWorkersByFiltersJSON".equalsIgnoreCase(resourceId) ){
 	     	
  			System.out.println("case: "+resourceId);
  			
  			try {
 	 			//int end = WorkerLocalServiceUtil.getWorkersCount();
 	 			//List<Worker> objectList=WorkerLocalServiceUtil.getWorkers(0, end);
 	 			//List<Worker> objectList = WorkerLocalServiceUtil.getWorkersByName("Marc");
 	 			//List<Worker> objectList = WorkerLocalServiceUtil.getWorkersByStatusDesc("Active");
 	 			/*
 	 			String desc = "Active";
 	 			desc = null;
 	 			String nif = "39733";
 	 			String name = "R";
 	 			String surname = "S";
 	 			String email = "@";
 	 			String phone = "";
 	 			*/
 	 			
 	 			//get params
 	 			String desc = resourceRequest.getParameter("desc");
 	 			String nif = resourceRequest.getParameter("nif");
 	 			String name = resourceRequest.getParameter("name");
 	 			String surname = resourceRequest.getParameter("surname");
 	 			String email = resourceRequest.getParameter("email");
 	 			String phone = resourceRequest.getParameter("phone");
 	 			
 	 			System.out.println("params-> desc:"+desc+" - nif:"+nif+" - name:"+name+" - surname:"+surname+" - email:"+email+" - phone:"+phone);
 	 			
 	 			//call finder
 	 			List<Worker> objectList = WorkerLocalServiceUtil.getWorkersByFilters(desc, nif, name, surname, email, phone);
 	 			
 	 			JSONObject recordsjsonObject=JSONFactoryUtil.createJSONObject();
 	 			JSONArray rowjsonObject=null;
 	 			JSONObject cell=null;
 	 			JSONArray recordsjsonArray=JSONFactoryUtil.createJSONArray();
 	 			
 	 			recordsjsonObject.put("page","1");
 	 			double total_pages = Math.ceil(objectList.size()/10);
 	 			recordsjsonObject.put("total",String.valueOf(total_pages));
 	 			recordsjsonObject.put("records ", String.valueOf(objectList.size()));
 	 			
 	 			if(!objectList.isEmpty()){
 	 				for(int i=0;i<objectList.size();i++){    
 	 					Worker w = objectList.get(i);
 	 					rowjsonObject=JSONFactoryUtil.createJSONArray();
 	 					cell=JSONFactoryUtil.createJSONObject();
 	 					rowjsonObject.put( String.valueOf( w.getWorkerId() ) );
 	 					rowjsonObject.put(String.valueOf( w.getName() ) );
 	 					rowjsonObject.put( String.valueOf( w.getSurname() ) );
 	 					rowjsonObject.put( String.valueOf( w.getEmail() ) );
 	 					rowjsonObject.put( String.valueOf( w.getNif() ) );
 	 					rowjsonObject.put( String.valueOf( w.getStatusId() ) );
 	
 	 					cell.put("id", String.valueOf( w.getWorkerId() ) );
 	 					cell.put("cell",rowjsonObject);
 	 					recordsjsonArray.put(cell); 
 	 				}
 	
 	 				recordsjsonObject.put("rows",recordsjsonArray);
 	 			}
 	 			resourceResponse.getWriter().print(recordsjsonObject.toString());
 	 			System.out.println(recordsjsonObject.toString());
 	     	} catch (SystemException e) {
 	     		System.out.println("serveResource Error: " + e.getMessage() );
 	     	} catch (PortalException e1) {
 	     		System.out.println("serveResource Error: " + e1.getMessage() );
 			}
  		}
   
      }
 
 }
