 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
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
 package org.etudes.tool.melete;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.*;
 import org.etudes.api.app.melete.*;
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.application.Application;
 import javax.faces.component.html.*;
 import javax.faces.component.*;
 
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.io.*;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.ActionEvent;
 
 //import com.sun.faces.util.Util;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionService;
 //import org.sakaiproject.jsf.ToolBean;
 
 //Adding to test
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 /**
  * @author Faculty
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 /*
  * Mallika - 4/26/07 - Backing bean for whats next page
  **/
 public class ViewNextStepsPage implements Serializable/*,ToolBean */{
 
 
 	  /** identifier field */
 	  private int prevSecId;
 	  private int prevModId;
 	  private int moduleSeqNo;
 	  private int nextSeqNo;
 	  private ModuleObjService module;
 	  private ModuleService moduleService;
 	  
 	  String courseId;
 	  String userId;
 	  public HtmlPanelGroup secpgroup;
 
 
       /** Dependency:  The logging service. */
       protected Log logger = LogFactory.getLog(ViewNextStepsPage.class);
 
 
 	  public ViewNextStepsPage(){
 
 	  	FacesContext context = FacesContext.getCurrentInstance();
 	  	Map sessionMap = context.getExternalContext().getSessionMap();
 	  	courseId = null;
 	  	userId = null;
 	  }
 
 
 public String goPrevItem()
 {
 	FacesContext context = FacesContext.getCurrentInstance();
 	courseId = getCourseId();
 	int prevSeqNo;
     prevSeqNo = getModuleService().getPrevSeqNo(getUserId(), courseId,this.nextSeqNo);
 
 	if (this.prevSecId == 0)
 	{
 	  ValueBinding binding =
         Util.getBinding("#{viewModulesPage}");
       ViewModulesPage vmPage = (ViewModulesPage)
         binding.getValue(context);
       vmPage.setModuleId(this.prevModId);
       vmPage.setPrintable(null);
   	  vmPage.setMdbean(null);
   	  vmPage.setAutonumber(null);
   	  if (this.nextSeqNo > 1)
   	  {
         vmPage.setModuleSeqNo(prevSeqNo);
   	  }
       vmPage.setPrevMdbean(null);
     	return "view_module";
 	
 	}
 	else
 	{
 		ValueBinding binding =
 	            Util.getBinding("#{viewSectionsPage}");
 
 	    ViewSectionsPage vsPage = (ViewSectionsPage)
 	            binding.getValue(context);
 
 	    vsPage.setSectionId(this.prevSecId);
 	    vsPage.setModuleId(this.prevModId);
 	    //This condition occurs when whats next is after
 	    //the last section of the last module
 	    if (prevSeqNo == -1)
 	    {
 	    	vsPage.setModuleSeqNo(this.moduleSeqNo);
 	    }
 	    else
 	    {
 	       vsPage.setModuleSeqNo(prevSeqNo);
 	    }
 	    vsPage.setSection(null);
 	    //added by rashmi on 6/14/05
 	    vsPage.setModule(null);
 	    vsPage.setAutonumber(null);
 	    return "view_section";
 	}
 }
 
 public String goNextModule()
 {
 	FacesContext context = FacesContext.getCurrentInstance();
 	//this.module = null;
 	int nextSeqNo = new Integer(((String)context.getExternalContext().getRequestParameterMap().get("modseqno"))).intValue();
 	this.module = null;
 	ValueBinding binding =
         Util.getBinding("#{viewModulesPage}");
       ViewModulesPage vmPage = (ViewModulesPage)
         binding.getValue(context);
   //  vmPage.setModuleId(nextMdBean.getModuleId());
     vmPage.setModuleId(0);
   	vmPage.setMdbean(null);
   	vmPage.setPrevMdbean(null);
     vmPage.setModuleSeqNo(nextSeqNo);
     vmPage.setPrintable(null);
     vmPage.setAutonumber(null);
  	return "view_module";
 }
 
 public String gotoMyBookmarks()
 {
 	FacesContext context = FacesContext.getCurrentInstance();
 	ValueBinding binding =
         Util.getBinding("#{bookmarkPage}");
     BookmarkPage bmPage = (BookmarkPage)binding.getValue(context);
     bmPage.resetValues();
 	return "list_bookmarks";
 }
 
 
 /*
  * section breadcrumps in format module title >> section title
  */
 public HtmlPanelGroup getSecpgroup() {
 	  return null;
     }
 
 public void setSecpgroup(HtmlPanelGroup secpgroup)
 {
 
 	FacesContext context = FacesContext.getCurrentInstance();
 	ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 	 Application app = context.getApplication();
 
 	 List list = secpgroup.getChildren();
 	 list.clear();
 
 	 //1. add module as commandlink and it takes to view module page
 	 Class[] param = new Class[1];
 	 HtmlCommandLink modLink = new HtmlCommandLink();
      param[0] = new ActionEvent(modLink).getClass();
      modLink.setId("modlink");
      modLink.setActionListener(app.createMethodBinding("#{viewModulesPage.viewModule}", param));
      modLink.setAction(app.createMethodBinding("#{viewModulesPage.redirectToViewModule}", null));
     //1a . add outputtext to display module title
      HtmlOutputText outModule = new HtmlOutputText();
      outModule.setId("modtext");
      if(this.module == null) getModule();
      if (this.module != null) outModule.setValue(this.module.getTitle());
      //1b. param to set module id
      UIParameter modidParam = new UIParameter();
      modidParam.setName("modid");
      if (this.module != null) modidParam.setValue(this.module.getModuleId());
      modLink.getChildren().add(outModule);
      modLink.getChildren().add(modidParam);
      list.add(modLink);
 
      //2. add >>
      HtmlOutputText seperatorText = new HtmlOutputText();
      seperatorText.setId("sep1");
      seperatorText.setTitle(" "+(char)187+" ");
      seperatorText.setValue(" "+(char)187+" ");
      list.add(seperatorText);
 
 	 // note: when subsections are in place then find all parents of subsection
 	 // and in a while or for loop create commandlink with action/action listener as viewSection
 
 	 //3. add current section title
 	 HtmlOutputText currSectionText = new HtmlOutputText();
 	 currSectionText.setId("currsectext");
      currSectionText.setValue(bundle.getString("view_whats_next_whats_next"));
 
 	 list.add(currSectionText);
 
 	 this.secpgroup = secpgroup;
 }
 public String goTOC()
 {
 	FacesContext context = FacesContext.getCurrentInstance();
 	ValueBinding binding = Util.getBinding("#{listModulesPage}");
 	ListModulesPage listPage = (ListModulesPage)
         binding.getValue(context);
 	listPage.setViewModuleBeans(null);
 	listPage.setAutonumberMaterial(null);
 	return listPage.listViewAction();
 }
 public int getNextSeqNo() {
 	return nextSeqNo;
 }
 
 public void setNextSeqNo(int nextSeqNo) {
 	this.nextSeqNo = nextSeqNo;
 }
 
 public int getPrevModId() {
 	return prevModId;
 }
 
 public void setPrevModId(int prevModId) {
 	this.prevModId = prevModId;
 }
 
 public int getPrevSecId() {
 	return prevSecId;
 }
 
 public void setPrevSecId(int prevSecId) {
 	this.prevSecId = prevSecId;
 }
 
 public ModuleObjService getModule()
 {
 	  if (this.module == null)
 	  {
 		  this.module = getModuleService().getModule(this.prevModId);
		  this.moduleSeqNo = this.module.getCoursemodule().getSeqNo();
 	  }
 	  return this.module;
 }
 
 public void setModule(ModuleObjService module)
 {
 	  this.module = module;
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
 
 
 public int getModuleSeqNo()
 {
 	return this.moduleSeqNo;
 }
 
 
 public void setModuleSeqNo(int moduleSeqNo)
 {
 	this.moduleSeqNo = moduleSeqNo;
 }
 private String getCourseId()
 {
 	if (courseId == null)
 	{
 	FacesContext context = FacesContext.getCurrentInstance();
   	Map sessionMap = context.getExternalContext().getSessionMap();
 	courseId = (String)sessionMap.get("courseId");
 	}
 	return courseId;
 }
 private String getUserId()
 {
 	if (userId == null)
 	{
 	FacesContext context = FacesContext.getCurrentInstance();
   	Map sessionMap = context.getExternalContext().getSessionMap();
 	userId = (String)sessionMap.get("userId");
 	}
 	return userId;
 }
 
 }
