 /**********************************************************************************
 *
 * $Header: /usr/src/sakai/melete-2.4/melete-app/src/java/org/sakaiproject/tool/melete/EditModulePage.java,v 1.10 2007/06/07 22:38:15 mallikat Exp $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/
 
 package org.sakaiproject.tool.melete;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.component.app.melete.*;
 
 import java.util.*;
 import java.io.Serializable;
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.el.ValueBinding;
 import org.sakaiproject.util.ResourceLoader;
 
 import org.sakaiproject.component.app.melete.ModuleDateBean;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 //import org.sakaiproject.jsf.ToolBean;
 /**
  * @author Mallika
  *
  * Edit Module Page is the backing bean for the page edit_module.jsp.
  * It also connects to other jsp pages like cclicenseform.jsp and
  * publicdomainform.jsp and license_results.jsp
  * 8/1/06 - Mallika - Removing IAgree code
  * 28/12/06 - Rashmi - remove license validations and clean up license code
  * 2/6/07 - Rashmi - set section breadcrumbs to text only for add section page
  * 3/6/07- Rashmi - remove section breadcrumbs
  *   */
 
 public class EditModulePage extends ModulePage implements Serializable/*, ToolBean*/ {
 
    /** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(EditModulePage.class);
    private boolean callFromAddContent = false;
    private boolean showLicenseFlag = true;
 
     public EditModulePage(){
       setFormName("EditModuleForm");
       setSuccess(false);
 
     }
     /**
 	 * @param logger The logger to set.
 	 */
 	public void setLogger(Log logger) {
 		this.logger = logger;
 	}
 
 	/*
 	 * Rashmi 12/21
 	 * modification details
 	 *
 	 */
 	public void setModification()
 	{
 		  FacesContext context = FacesContext.getCurrentInstance();
 	      Map sessionMap = context.getExternalContext().getSessionMap();
 	      module.setModifiedByFname((String)sessionMap.get("firstName"));
 	      module.setModifiedByLname((String)sessionMap.get("lastName"));
 	      module.setModificationDate(new Date());
 	}
 	 public boolean  getShowLicenseFlag() {
 	  	return showLicenseFlag;
 	  }
 
 	  public void setShowLicenseFlag(boolean  showLicenseFlag) {
 	  	this.showLicenseFlag = showLicenseFlag;
 	  }
 	/*
 	 * Rashmi -- 12/21 revised to put correct validations
 	 *
 	 */
 	public String savehere()
 	{
     	String errMsg = "";
         setSuccess(false);
         if(moduleService == null)
         	moduleService = getModuleService();
 
 	     FacesContext context = FacesContext.getCurrentInstance();
      	 ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 	    // rashmi added validations start
 //     	validation
       	module.setTitle(module.getTitle().trim());
       	ModuleValidator mv = new ModuleValidator();
       	mv.isValidTitle(module.getTitle());
 
       	// validation no 3
       	Date  d = new Date();
       	Date st = getModuleShdates().getStartDate();
       	Date end = getModuleShdates().getEndDate();
 
 //       validation no 4 b
     	if ((end != null) && (st != null))
 		{
			if (end.compareTo(st) <= 0)
 			{
 				errMsg = "";
 				errMsg = bundle.getString("end_date_before_start");
 				FacesMessage msg = new FacesMessage(errMsg);
 				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 				context.addMessage(null, msg);
 				return "failure";
 			}
 		}
  	    // rashmi added validations end
 
 	     // actual update
 		try{
 			setModification();
 			if(module.getKeywords() != null)
 			{
 				module.setKeywords(module.getKeywords().trim());
 			}
 			if(module.getKeywords() == null || (module.getKeywords().length() == 0) )
 				 	{
 						module.setKeywords(module.getTitle());
 					}
 			ModuleDateBean mdbean = new ModuleDateBean();
 			mdbean.setModuleId(getModule().getModuleId().intValue());
 			mdbean.setModule((Module)getModule());
 			mdbean.setModuleShdate((ModuleShdates)getModuleShdates());
 			mdbean.setDateFlag(false);
 			ArrayList mdbeanList = new ArrayList();
 			mdbeanList.add(mdbean);
 			moduleService.updateProperties(mdbeanList);
 
 			// add module to session
 			Map sessionMap = context.getExternalContext().getSessionMap();
 			sessionMap.put("currModule",module);
 
 
 		}
 		catch(MeleteException me)
 		{
 			logger.error("show error message for"+me.toString()+me.getMessage()+",");
 			errMsg = bundle.getString(me.getMessage());
 			FacesMessage msg = new FacesMessage(errMsg);
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			context.addMessage (null, msg);
 			return "failure";
 		}
 		catch(Exception ex)
 		{
 			logger.error("mbusiness insert module failed:" + ex);
 			//ex.printStackTrace();
 			logger.error(ex.toString());
 			errMsg = "";
 			errMsg = bundle.getString("edit_module_fail");
 	     	context.addMessage (null, new FacesMessage(errMsg));
 			return "failure";
 		}
 		if (callFromAddContent == false)
 		{
 		  FacesMessage msg = new FacesMessage(bundle.getString("edit_module_success"));
 		  msg.setSeverity(FacesMessage.SEVERITY_INFO);
 		  context.addMessage(null,msg);
 		}
 		setSuccess(true);
 		return "success";
 	 }
 
 	/*
 	 *
 	 */
 	public String save()
 	{
 		if(!savehere().equals("failure"))
 		{
     		callFromAddContent = false;
 		    setSuccess(true);
 		}
     	else
     	{
     		callFromAddContent = false;
 
     	}
 		return "edit_module";
 	}
 
     /*
      * Revision by rashmi on 12/20
      * in sbcPage settings ..change from getModule() to module
      * add section page instance to reset values
      */
     public String addContentSections()
     {
     	String errMsg = "";
 
         if(!getSuccess())
         {
         	callFromAddContent = true;
         	if(!savehere().equals("failure"))
     		{
         		callFromAddContent = false;
     		    setSuccess(true);
     		}
         	else
         	{
         		callFromAddContent = false;
         		return "edit_module";
         	}
         }
         //Revision -- 12/20 - to remove retaintion of values
         FacesContext context = FacesContext.getCurrentInstance();
         ValueBinding binding =Util.getBinding("#{addSectionPage}");
         AddSectionPage addPage = (AddSectionPage) binding.getValue(context);
         addPage.setSection(null);
         addPage.resetSectionValues();
         addPage.setModule(module);
 
         //Mallika - 10/18/06 - adding reference to checkUploadExists
         checkUploadExists();
 
        return "addmodulesections";
     }
 
     /*
      * Revised by Rashmi -- 12/21 to fix bug#189
      * reset values
      */
     public void setEditInfo(ModuleDateBean mdbean){
     	resetModuleValues();
     	setModuleDateBean(mdbean);
     	setModule(mdbean.getModule());
     	setModuleShdates(mdbean.getModuleShdate());
     }
 
  }
