 /**********************************************************************************
 *
 * $Header: /usr/src/sakai/melete-2.4/melete-app/src/java/org/sakaiproject/tool/melete/ListModulesPage.java,v 1.15 2007/11/13 19:32:39 mallikat Exp $
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
 import org.sakaiproject.api.app.melete.*;
 
 import javax.faces.component.*;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.ListIterator;
 import java.util.Map;
 import java.io.Serializable;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.application.FacesMessage;
 
 //import com.sun.faces.util.Util;
 import java.sql.Timestamp;
 import org.sakaiproject.api.app.melete.ModuleService;
 import org.sakaiproject.component.app.melete.*;
 
 import javax.faces.event.*;
 
 /**
  * @author Faculty
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 /*
  * Mallika - 2/8/07 - consolidated view section pages into one
  */
 public class ListModulesPage implements Serializable{
 	/** Dependency:  The logging service. */
 	  protected Log logger = LogFactory.getLog(ListModulesPage.class);
 	  private List moduleDateBeans = null;
 	  private List moduleDatePrivBeans = null;
 	  /** identifier field */
       private int showModuleId;
 
       private String formName;
       private Date currentDate;
       private Timestamp currentTimestamp;
       private boolean instFlag;
       private boolean studFlag;
       private String role;
       private String typeEditor;
       private String typeLink;
       private String typeUpload;
       private boolean nomodsFlag;
       private boolean expandAllFlag;
       private boolean trueFlag = true;
 
       private ModuleStudentPrivsService  nullMsp = null;
       private ModuleService moduleService;
       private Section nullSection = null;
       private List nullList = null;
       private String isNull = null;
       private Date nullDate = null;
 
 
 	  //This needs to be set later using Utils.getBinding
 	  String courseId;
 	  String userId;
 
 	  public ListModulesPage(){
 
 	  	instFlag = true;
 	  	studFlag = false;
 	  	FacesContext context = FacesContext.getCurrentInstance();
 //	  	context.getViewRoot().setTransient(true);
 	  	Map sessionMap = context.getExternalContext().getSessionMap();
 	  	role = (String)sessionMap.get("role");
 	  	courseId = (String)sessionMap.get("courseId");
 	  	userId = (String)sessionMap.get("userId");
 	  	nomodsFlag = false;
 	  	setShowModuleId(-1);
 	  	if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 		{
 	  		 ValueBinding binding = Util.getBinding("#{authorPreferences}");
 	 		AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding.getValue(context);
 	 		String expFlag = preferencePage.getUserView();
 	 		if (expFlag.equals("true"))
 	 		{
 	 	      expandAllFlag = true;
 	 		}
 	 		else
 	 		{
 	 		  expandAllFlag = false;
 	 		}
 		}
 	  	else
 	  	{
 	  	  expandAllFlag = true;
 	  	}
 	  }
 
 	  public void resetValues()
 	  {
 	  	instFlag = true;
 	  	studFlag = false;
 	  	nomodsFlag = false;
 	  	FacesContext context = FacesContext.getCurrentInstance();
 //	  	context.getViewRoot().setTransient(true);
 		if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 		{
 	  		 ValueBinding binding = Util.getBinding("#{authorPreferences}");
 	 		AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding.getValue(context);
 	 		String expFlag = preferencePage.getUserView();
 	 		if (expFlag.equals("true"))
 	 		{
 	 	      expandAllFlag = true;
 	 		}
 	 		else
 	 		{
 	 		  expandAllFlag = false;
 	 		}
 		}
 	  	else
 	  	{
 	  	  expandAllFlag = true;
 	  	}
 	  	setShowModuleId(-1);
 	  }
 
 
  /**
 	   * @return Returns the ModuleService.
 	   */
 	  public ModuleService getModuleService() {
 		return moduleService;
 	  }
 
 	 /**
 	  * @param moduleService The moduleService to set.
 	  */
 	  public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	  }
 
 		/**
 		 * @param logger The logger to set.
 		 */
 		public void setLogger(Log logger) {
 			this.logger = logger;
 		}
 	  public String getRole() {
 	  	return role;
 	  }
 
 	  public void setRole(String role) {
 	  	this.role = role;
 	  }
 
 	  public boolean getInstFlag() {
 	  	return instFlag;
 	  }
 
 	  public void setInstFlag(boolean instFlag) {
 	  	this.instFlag = instFlag;
 	  }
 
 	  public boolean getStudFlag() {
 	  	return studFlag;
 	  }
 
 	  public void setStudFlag(boolean studFlag) {
 	  	this.studFlag = studFlag;
 	  }
 	  public List  getNullList() {
 	  	return nullList;
 	  }
 
 	  public void setNullList(List nullList) {
 	  	this.nullList = nullList;
 	  }
 
       public boolean  getTrueFlag() {
 	  	return trueFlag;
 	  }
 
 	  public void setTrueFlag(boolean  trueFlag) {
 	  	this.trueFlag = trueFlag;
       }
 
 	  public boolean getNomodsFlag() {
 	  	return nomodsFlag;
 	  }
 
 	  public void setNomodsFlag(boolean nomodsFlag) {
 	  	this.nomodsFlag = nomodsFlag;
 	  }
 
       public boolean getExpandAllFlag() {
 	  	return expandAllFlag;
 	  }
 
 	  public void setExpandAllFlag(boolean expandAllFlag) {
 	  	this.expandAllFlag = expandAllFlag;
 	  }
 
 	  public ModuleStudentPrivsService  getNullMsp() {
 	  	return nullMsp;
 	  }
 
 	  public void setNullMsp(ModuleStudentPrivsService  nullMsp) {
 	  	this.nullMsp = nullMsp;
 	  }
 
 	  public Section  getNullSection() {
 	  	return nullSection;
 	  }
 
 	  public Date getNullDate() {
 		  return nullDate;
 	  }
 	  public void setNullSection(Section  nullSection) {
 	  	this.nullSection = nullSection;
 	  }
 
 	  public String getTypeEditor(){
 	  	return "typeEditor";
 	  }
 	  public void setTypeEditor(String typeEditor){
 	  	this.typeEditor = typeEditor;
 	  }
 
 	  public String getTypeLink(){
 	  	return "typeLink";
 	  }
 	  public void setTypeLink(String typeLink){
 	  	this.typeLink = typeLink;
 	  }
 
 	  public String getTypeUpload(){
 	  	return "typeUpload";
 	  }
 	  public void setTypeUpload(String typeUpload){
 	  	this.typeUpload = typeUpload;
 	  }
 	  public String getIsNull()
 	  {
 	  	return isNull;
 	  }
 		  public List getModuleDateBeans() {
 	  	int hideFlagSize = 0;
 	   	setCurrentDate(Calendar.getInstance().getTime());
 	  	setCurrentTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
 	  	try {
 	  		moduleDateBeans = getModuleService().getModuleDateBeans(courseId);
 	  		if ((moduleDateBeans != null)&&(moduleDateBeans.size() > 0))
 	  		{	
 	  		  for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext(); ) 
 	  		  {
 		        ModuleDateBean mdbean = (ModuleDateBean) i.next();
 		        if (mdbean.getModuleShdate().isHideFlag() == true)
 		        {
 		        	hideFlagSize = hideFlagSize + 1;
 		        }
 	  		  }
 	  		}
 	  	}catch (Exception e)
 		{
 	  		//e.printStackTrace();
 	  		logger.error(e.toString());
 		}
 
 	  	//If list of modules returned is zero or if all of them are hidden
 	  	if ((moduleDateBeans == null)||(moduleDateBeans.size() == 0)||(moduleDateBeans.size() == hideFlagSize))
 	  	{
 	  	  nomodsFlag = true;
 	  	  FacesContext ctx = FacesContext.getCurrentInstance();
   		  addNoModulesMessage(ctx);
   		  moduleDateBeans = new ArrayList();
 	  	}
 		  	return moduleDateBeans;
 	  }
 
 	  public void setModuleDateBeans(List moduleDateBeansList) {
 	    moduleDateBeans = moduleDateBeansList;
 	  }
 
 	  public List getModuleDatePrivBeans() {
 		int hideFlagSize = 0;
 	  	setCurrentDate(Calendar.getInstance().getTime());
 	  	setCurrentTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
 	  	try {
 	  		moduleDatePrivBeans = getModuleService().getModuleDatePrivBeans(courseId);
 
 	  	}catch (Exception e)
 		{
 	  		//e.printStackTrace();
 	  		logger.error(e.toString());
 		}
 	  	if ((moduleDatePrivBeans == null)||(moduleDatePrivBeans.size() == 0))
 	  	{
 	  	  nomodsFlag = true;
 	  	  FacesContext ctx = FacesContext.getCurrentInstance();
   		  addNoModulesMessage(ctx);
   		  moduleDatePrivBeans = new ArrayList();
 	  	}
 		  	return moduleDatePrivBeans;
 	  }
 
 	  public void setModuleDatePrivBeans(List moduleDatePrivBeansList) {
 	    moduleDatePrivBeans = moduleDatePrivBeansList;
 	  }
 	  public Date getCurrentDate() {
 	  	return currentDate;
 	  }
 
 	  public void setCurrentDate(Date currentDate) {
 	  	this.currentDate = currentDate;
 	  }
 
 	  public Date getCurrentTimestamp() {
 	  	return currentTimestamp;
 	  }
 
 	  public void setCurrentTimestamp(java.sql.Timestamp currentTimestamp) {
 	  	this.currentTimestamp = currentTimestamp;
 	  }
 
 	  public int getShowModuleId() {
 	        return this.showModuleId;
 	  }
 
 	  public void setShowModuleId(int moduleId) {
 	        this.showModuleId = moduleId;
 	  }
 
 
 
 
 	  public String showSections() {
 	  	ModuleDatePrivBean mdpbean = null;
 	  	ModuleDateBean mdbean = null;
 	  	FacesContext ctx = FacesContext.getCurrentInstance();
 	  	 UIViewRoot root = ctx.getViewRoot();
 	        UIData table = (UIData)
 	            root.findComponent("listmodulesform").findComponent("table");
 	        ValueBinding binding =
 	            Util.getBinding("#{listModulesPage}");
 	        ListModulesPage lmPage = (ListModulesPage)
 	            binding.getValue(ctx);
 	        String retVal = "list_modules_student";
 	        if (getRole()!= null && getRole().equals("INSTRUCTOR")){
 	        	mdbean = (ModuleDateBean) table.getRowData();
 	        	lmPage.setShowModuleId(mdbean.getModuleId());
 	        	retVal = "list_modules_inst";
 	        }
 	        if (getRole()!= null && getRole().equals("STUDENT")) {
 	        	mdpbean = (ModuleDatePrivBean) table.getRowData();
 	        	lmPage.setShowModuleId(mdpbean.getModuleId());
 	        }
 
 	  	return retVal;
 	  }
 
 	  public String hideSections() {
 	  	setShowModuleId(-1);
         setExpandAllFlag(false);
 	  	String retVal = "list_modules_student";
 	  	 if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 	    {
 	    	retVal = "list_modules_inst";
 	    }
 		  	return retVal;
 	  }
 
      //Mallika - 6/7/06 - adding this method to expand all modules
       public String expandAllAction() {
 	  	FacesContext ctx = FacesContext.getCurrentInstance();
 	  	       ValueBinding binding =
 	            Util.getBinding("#{listModulesPage}");
 	        ListModulesPage lmPage = (ListModulesPage)
 	            binding.getValue(ctx);
             lmPage.setExpandAllFlag(true);
         String retVal = "list_modules_student";
         if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 	    {
 	    	retVal = "list_modules_inst";
 	    }
 
 	   return retVal;
 	  }
 
       public String collapseAllAction() {
 	  FacesContext ctx = FacesContext.getCurrentInstance();
 	  	       ValueBinding binding =
 	            Util.getBinding("#{listModulesPage}");
 	        ListModulesPage lmPage = (ListModulesPage)
 	            binding.getValue(ctx);
             lmPage.setExpandAllFlag(false);
 	        lmPage.setShowModuleId(-1);
 
 	  	String retVal = "list_modules_student";
 	    if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 		{
 		  retVal = "list_modules_inst";
 		}
 
 	  	return retVal;
 	  }
 	  //Mallika - new code end
 
       public String redirectToViewModule()
 	  {
 	  	String retVal = "view_module_student";
 	    if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 		{
 		  retVal = "view_module";
 	    }
 
 	  	return retVal;
 	  }
 
       public void viewModule(ActionEvent evt)
 	  {
 	    ModuleDatePrivBean mdpbean = null;
 	  	ModuleDateBean mdbean = null;
 	  	FacesContext ctx = FacesContext.getCurrentInstance();
         UICommand cmdLink = (UICommand)evt.getComponent();
 		String selclientId = cmdLink.getClientId(ctx);
 		selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 		selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 		String modId = selclientId.substring(0,selclientId.indexOf(':'));
 		int selModIndex = Integer.parseInt(modId);
 
 	    ValueBinding binding =
 	            Util.getBinding("#{viewModulesPage}");
 	    ViewModulesPage vmPage = (ViewModulesPage)
 	            binding.getValue(ctx);
 
         if (getRole()!= null && getRole().equals("INSTRUCTOR")){
         	if ((moduleDateBeans != null)&&(moduleDateBeans.size() > 0))
         	{
 	        	mdbean = (ModuleDateBean) moduleDateBeans.get(selModIndex);
 	        	vmPage.setModuleId(mdbean.getModuleId());
 	        	vmPage.setMdbean(null);
 	        	vmPage.setPrevMdbean(null);
 	          	CourseModuleService cmod = (CourseModuleService) mdbean.getCmod();
 	        	vmPage.setModuleSeqNo(cmod.getSeqNo());
         	}
 	        }
          else{
         	 if ((moduleDatePrivBeans != null)&&(moduleDatePrivBeans.size() > 0))
         	 {
 	        	mdpbean = (ModuleDatePrivBean) moduleDatePrivBeans.get(selModIndex);
 	        	vmPage.setModuleId(mdpbean.getModuleId());
 	        	vmPage.setMdbean(null);
 	        	vmPage.setPrevMdbean(null);
 	           	//Change without having to iterate through cmods
 	        	CourseModuleService cmod = (CourseModuleService) mdpbean.getCmod();
 	        	vmPage.setModuleSeqNo(cmod.getSeqNo());
         	 }
 	        }
       }
 
       public String redirectToViewSection()
 	  {
         String retVal = "view_section_student";
 	    //3/21/05 - Mallika - added this code in to handle linked and uploaded sections
         if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 	    {
 	    	retVal = "view_section";
 
 	    }
 
 	  	return retVal;
 	  }
 
       public String redirectToViewSectionLink()
 	  {
         String retVal = "view_section_student";
 	    //3/21/05 - Mallika - added this code in to handle linked and uploaded sections
         if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 	    {
 	    		retVal = "view_section";
 	    }
 
 	  	return retVal;
 	  }
 
 
 
       public void viewSection(ActionEvent evt)
 	  {
         FacesContext ctx = FacesContext.getCurrentInstance();
 
 		UICommand cmdLink = (UICommand)evt.getComponent();
 		String selclientId = null;
 		String modId = null;
 		int selModIndex = 0;
 		String sectionindex = null;
 		int selSecIndex = 0;
 
 	    selclientId = cmdLink.getClientId(ctx);
 	    if (selclientId != null)
 	    {
 	      int occurs = selclientId.split(":").length - 1;
 	      if (occurs == 5)
 	      {
 		  selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 	      selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 		  modId = selclientId.substring(0,selclientId.indexOf(':'));
 		  selModIndex = Integer.parseInt(modId);
 		  selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 		  selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 		  sectionindex=selclientId.substring(0,selclientId.indexOf(':'));
 		  selSecIndex = Integer.parseInt(sectionindex);
 	      }
 	    }
 		ModuleObjService mod = null;
 		SectionBean secBean = null;
 		int modSeqNo = 0;
 
 		if (getRole()!=null && getRole().equals("INSTRUCTOR")) {
			if ((moduleDateBeans != null)&&(moduleDateBeans.size() > 0))
 			{
 		    	ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selModIndex);
 			    mod = mdbean.getModule();
 		        secBean = (SectionBean) mdbean.getSectionBeans().get(selSecIndex);
 		        modSeqNo = mdbean.getCmod().getSeqNo();
 			}
 		}
 	    else {
	    	if ((moduleDatePrivBeans != null)&&(moduleDatePrivBeans.size() > 0))
 	    	{
 			    ModuleDatePrivBean mdpbean = (ModuleDatePrivBean) moduleDatePrivBeans.get(selModIndex);
 				mod = mdpbean.getModule();
 		        secBean = (SectionBean) mdpbean.getSectionBeans().get(selSecIndex);
 		        modSeqNo = mdpbean.getCmod().getSeqNo();
 	    	}
 	    }
 
 
         ValueBinding binding =
 	    Util.getBinding("#{viewSectionsPage}");
 	    ViewSectionsPage vsPage = (ViewSectionsPage)
 	    binding.getValue(ctx);
 	    if (secBean != null)
 	    {
 	     Section sec = secBean.getSection();
 	    vsPage.setModuleId(sec.getModuleId());
 	    vsPage.setSectionId(sec.getSectionId());
 	    }
 	     vsPage.setSection(null);
 	    vsPage.setModule(null);
 	    vsPage.setModuleSeqNo(modSeqNo);
 
    }
 
 	  private void addNoModulesMessage(FacesContext ctx){
 	  	FacesMessage msg =
 	  		new FacesMessage("No modules", "No modules are available for the course at this time.");
 	  	ctx.addMessage(null,msg);
 	  }
 
 }
