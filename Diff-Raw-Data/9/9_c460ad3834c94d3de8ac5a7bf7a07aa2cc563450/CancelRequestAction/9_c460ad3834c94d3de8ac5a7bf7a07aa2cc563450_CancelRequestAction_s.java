 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.view.actions.register;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.struts2.convention.annotation.ParentPackage;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Results;
 import org.glite.security.voms.admin.persistence.dao.generic.DAOFactory;
 import org.glite.security.voms.admin.persistence.dao.generic.RequestDAO;
 import org.glite.security.voms.admin.persistence.model.request.Request.STATUS;
 import org.glite.security.voms.admin.view.actions.BaseAction;
 
 @ParentPackage("base")
 @Results({
 	@Result(name=BaseAction.SUCCESS,location="registrationCancelled"),
 	@Result(name = BaseAction.ERROR, location = "registrationConfirmationError")
 })
 public class CancelRequestAction extends RegisterActionSupport {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public static final Logger log = LoggerFactory.getLogger(CancelRequestAction.class);
 	
 	String confirmationId;
 	
 	@Override
 	public String execute() throws Exception {
 		
 		if (!registrationEnabled())
 			return REGISTRATION_DISABLED;
 		
 		if (!request.getStatus().equals(STATUS.SUBMITTED)){
 			
			addActionError("Your request has already been canceled!");
 			return ERROR;
 		}
 			
 		
 		if (request.getConfirmId().equals(confirmationId)){
 			
 			RequestDAO rDAO = DAOFactory.instance().getRequestDAO();
 			log.info("Removing request '"+request+"' on user's request.");
 			
 			rDAO.makeTransient(request);
 			return SUCCESS;
 		}
 			
 		addActionError("Wrong confirmation id!");
 		return ERROR;
 	}
 
 	public String getConfirmationId() {
 		return confirmationId;
 	}
 
 	public void setConfirmationId(String confirmationId) {
 		this.confirmationId = confirmationId;
 	}
 	
 }
