 /**********************************************************************************
  *
  * $URL$
  *
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
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
 
 package org.sakaiproject.tool.melete;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIInput;
 import javax.faces.component.html.HtmlPanelGrid;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 //import org.sakaiproject.jsf.ToolBean;
 import org.sakaiproject.api.app.melete.ModuleObjService;
 import org.sakaiproject.api.app.melete.ModuleService;
 import org.sakaiproject.api.app.melete.SectionService;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.component.app.melete.SectionBean;
 
 /**
  * @author Rashmi
  * Created on Jan 11, 2005
  * revised on 3/29 by rashmi to make restore button unclickable if no modules are present
  * Mallika - 4/22/05 - Added the association to go to module label
  * Rashmi - 5/23/05 - sort sections went blank when there are no modules
  * Rashmi - 8/7/06 - remove label and seq no from modules dropdown that appear on sort sections page
  * Mallika - 10/26/06 - adding code to set default module to the first one
  */
 public class ManageModulesPage implements Serializable/*,ToolBean*/{
 
 	// attributes
 	// restore modules
 	int count;
 	private List archiveModulesList;
 	private List restoreModulesList;
 	private boolean shouldRenderEmptyList;
 	private boolean falseBool = false;
 
 	//sort
 	private String formName;
 	private int showSize;
 	private String newSelectedModule = "1";
 	private List currModulesList;
 	private List currList;
 	private List newModulesList;
 	private List newList;
 
 	// sort sections
 	private List allModulesList;
 	private List allModules;
 	private String currModule;
 	private List currSectionsList;
 	private List currSecList;
 	private List newSectionsList;
 	private List newSecList;
 	private String newSelectedSection;
 
 	/** Dependency:  The logging service. */
 	 protected Log logger = LogFactory.getLog(ManageModulesPage.class);
 	protected ModuleService moduleService;
 	protected SectionService sectionService;
 	/**
 	 * constructor
 	 */
 	public ManageModulesPage() {
 		count=0;
 		restoreModulesList = new ArrayList();
 		shouldRenderEmptyList=false;
 		archiveModulesList = null;
 	}
 
 	/**
 	 * @return
 	 */
 	public List getArchiveModulesList()
 	{
 		try{
 			if(archiveModulesList == null)
 			{
 			restoreModulesList=null;
 			FacesContext context = FacesContext.getCurrentInstance();
 			Map sessionMap = context.getExternalContext().getSessionMap();
 
 			String courseId = (String)sessionMap.get("courseId");
 
 			archiveModulesList = getModuleService().getArchiveModules(courseId);
 			}
 		} catch(Exception e)
 		{
 			logger.error("getting archived modules list "+e.toString());
 		}
 		return archiveModulesList;
 	}
 
 	/**
 	 * @return
 	 */
 	public List getRestoreModulesList()
 	{
 		return restoreModulesList;
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean getShouldRenderEmptyList()
 	{
 		if (archiveModulesList == null || archiveModulesList.size() == 0)
 		{
 			return true;
 		}else return false;
 	}
 
 	/*
 	 * added by rashmi on 3/29 to not render restore commandlink if modules are not present
 	 */
 	public boolean getFalseBool()
 	{
 		return false;
 	}
 	/*
 	 * adding listener
 	 */
   public void selectedRestoreModule(ValueChangeEvent event)throws AbortProcessingException
 	{
   		FacesContext context = FacesContext.getCurrentInstance();
 		UIInput mod_Selected = (UIInput)event.getComponent();
 
 		if(((Boolean)mod_Selected.getValue()).booleanValue() == true)
 		  {
 			String selclientId = mod_Selected.getClientId(context);
 			selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 			selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 
 			String modId = selclientId.substring(0,selclientId.indexOf(':'));
 			int selectedModIndex=Integer.parseInt(modId);
 
 			if(restoreModulesList == null)
 				restoreModulesList = new ArrayList();
 
 			restoreModulesList.add(archiveModulesList.get(selectedModIndex));
 			count++;
 
 		  }
 		return;
 	}
 
 	  /**
 	 * @return
 	 */
 	public String restoreModules()
 	  {
 		FacesContext context = FacesContext.getCurrentInstance();
 	   ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 		if(count <=0)
 		{
 			String errMsg = bundle.getString("no_module_selected");
 			context.addMessage (null, new FacesMessage(errMsg));
 			return "restore_modules";
 		}
 		// 1. restore modules
 		try{
 			getModuleService().restoreModules(restoreModulesList);
 			count=0;
 			}
 		catch(Exception me)
 			{
 
 				String errMsg = bundle.getString("restore_module_fail");
 				context.addMessage (null, new FacesMessage(errMsg));
 				return "restore_modules";
 			}
 
 		// 2. clear archivelist
 			archiveModulesList = null;
 
 	  	return "confirm_restore_modules";
 	  }
 
 	/**
 	 * clear lists on cancel
 	 */
 	public String cancelRestoreModules()
 	{
 		archiveModulesList = null;
 		restoreModulesList= null;
 		count=0;
 		return "modules_author_manage";
 	}
 
 	/**
 	 *navigates manage modules page on cancel
 	 */
 	public String cancel()
 	{
 		return "modules_author_manage";
 	}
 
 	/**
 	 * sort in ascending order
 	 */
 	public String sortOnDate()
 	{
 		if(archiveModulesList == null)
 		{
 			return "restore_modules";
 		}
 		Collections.sort(archiveModulesList, new MeleteDateComparator());
 		return "restore_modules";
 	}
 
 	/*
 	 * navigation rule to go to restore page
 	 */
 	public String goToRestoreModules(){
 		return "restore_modules";
 	}
 
 	/*
 	 * navigation rule to go to manage content page
 	 */
 	public String goToManageContent(){
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{manageResourcesPage}");
 		ManageResourcesPage managePage = (ManageResourcesPage) binding.getValue(context);
 		managePage.resetValues();
 		return "manage_content";
 	}
 
 	/*
 	 * navigation rule for Return
 	 */
 	public String returnToModules(){
 		return "list_auth_modules";
 	}
 
 	/**
 	 * gets navigation page to import export modules
 	 * @return
 	 */
 	public String importExportModules(){
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{exportMeleteModules}");
 		ExportMeleteModules exportModules = (ExportMeleteModules) binding.getValue(context);
 		exportModules.resetValues();
 		return "importexportmodules";
 	}
 
 	/*
 	 * Reset all values
 	 */
 	public void resetValues()
 	{
 		archiveModulesList = null;
 		restoreModulesList= null;
 		count=0;
 		currModulesList = null;
 		currList= null;
 		showSize = 6;
 		newModulesList= null;
 		newList= null;
 		newSelectedModule="1";
 		currSectionsList = null;
 		currSecList= null;
 		newSectionsList= null;
 		newSecList= null;
 		allModules = null;
 		allModulesList = null;
 		currModule = null;
 	}
 
 	/* sort pages */
 
 	/*
 	 * get the current seq of modules
 	 */
 	public List getCurrList()
 	{
 		try{
 			if(currModulesList == null)
 			{
 			FacesContext context = FacesContext.getCurrentInstance();
 			Map sessionMap = context.getExternalContext().getSessionMap();
 
 			String courseId = (String)sessionMap.get("courseId");
 			currModulesList = new ArrayList();
 			currModulesList = getModuleService().getModules(courseId);
 			currList  = forSelectItemsList(currModulesList);
 			}
 		} catch(Exception e)
 		{
 			logger.error("error in getting currList "+e.toString());
 		}
 		return currList;
 	}
 
 	/**
 	 * @return Returns the newList.
 	 */
 	public List getNewList() {
 		try{
 			if(newModulesList == null)
 			{
 				newModulesList = currModulesList;
 				newList  = forSelectItemsList(newModulesList);
 			}
 
 		} catch(Exception e)
 		{
 			logger.error("error in getting currList "+e.toString());
 		}
 		return newList;
 	}
 	/**
 	 * @param newList The newList to set.
 	 */
 	public void setNewList(List newList) {
 		this.newList = newList;
 	}
 
 	/*
 	 * converts the coursemodule list to selectItems for displaying at
 	 * the list boxes in the JSF page
 	 */
 	private List forSelectItemsList(List list)
 	{
 		List selectList = new ArrayList();
 		   // Adding available list to select box
 	      if(list == null || list.size()==0)
 	      {
 	      	selectList.add(new SelectItem("0", "No Items"));
 	      	 return selectList;
 	      }
 
 	      Iterator itr = list.iterator();
 	 	  	  while (itr.hasNext()) {
 		  	  		ModuleObjService  mod = (ModuleObjService) itr.next();
 		  	  		int seq = mod.getCoursemodule().getSeqNo();
 			  	  	String value = new Integer(seq).toString();
 			  	  	String label = seq + ". " + mod.getTitle();
 			  	  	selectList.add(new SelectItem(value, label));
 		  		}
 
 	   	return selectList;
 	}
 
 	private List forSelectSectionsItemsList(List list)
 	{
 		List selectList = new ArrayList();
 		   // Adding available list to select box
 	      if(list == null || list.size()==0)
 	      {
 	      	selectList.add(new SelectItem("0", "No Items"));
 	      	 return selectList;
 	      }
 
 	      Iterator itr = list.iterator();
 	      int countidx = 0;
 	          	while (itr.hasNext()) {
 	         		SectionBean secBean= (SectionBean) itr.next();
	         		if (secBean != null)
	         		{	
 		  	  		String value = new Integer(countidx++).toString();
 		  	  		String label = secBean.getDisplaySequence() +" "+ secBean.getSection().getTitle();
 		  	 		selectList.add(new SelectItem(value, label));
	         		}
 		  		}
 
 	   	return selectList;
 	}
 	/**
 	 * @return Returns the newSelectedModule.
 	 */
 	public String getNewSelectedModule() {
 		return newSelectedModule;
 	}
 	/**
 	 * @param newSelectedModule The newSelectedModule to set.
 	 */
 	public void setNewSelectedModule(String newSelectedModule) {
 		this.newSelectedModule = newSelectedModule;
 	}
 
 
 	public int getShowSize()
 	{
 		showSize = 6;
 		if(formName != null && formName.equals("SortSectionForm"))
 		{
 			if (currSecList !=null && currSecList.size() >6)
 			showSize = currSecList.size();
 		}
 		else
 		{
 			if(currList !=null && currList.size() > 6)
 				showSize = currList.size();
 		}
 		return showSize;
 	}
 
 
 	public String MoveItemAllUpAction()
 	  {
 	  	FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 	  	Map sessionMap = ctx.getExternalContext().getSessionMap();
 		String courseId = (String)sessionMap.get("courseId");
 	  	// if module is selected then throw message
 	  	logger.debug("values" + newSelectedModule );
 
 		  try{
 		   	if(newSelectedModule != null)
 		   		{
 		   		int selIndex = (new Integer(newSelectedModule).intValue()) -1;
 
 		   		if(newModulesList.size() < 2 || selIndex == 0)
 		   		{
 		   			logger.debug("first module selected to move up");
 					return "modules_sort";
 		   		}
 		   		ModuleObjService  mod  = (ModuleObjService) newModulesList.get(selIndex);
 	  	  		logger.debug("calling sort for " + mod.getTitle());
 		   		moduleService.sortModule(mod,courseId,"allUp");
 		   		newModulesList = getModuleService().getModules(courseId);
 				newList  = forSelectItemsList(newModulesList);
 				newSelectedModule = new Integer(selIndex).toString();
 		   		}
 		  	}
 		  catch (MeleteException me)
 		  	{
 			logger.error(me.toString());
 			me.printStackTrace();
 	  		String ErrMsg = bundle.getString("sort_fail");
 	  		FacesMessage msg =new FacesMessage(ErrMsg);
 		  	msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			ctx.addMessage (null, msg);
 		  	}
 	 	  return "modules_sort";
 	  }
 
 
 	  public String MoveItemUpAction()
 	  {
 	  	FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 	  	Map sessionMap = ctx.getExternalContext().getSessionMap();
 		String courseId = (String)sessionMap.get("courseId");
 	  	// if module is selected then throw message
 	  	logger.debug("values" + newSelectedModule );
 
 		  try{
 		   	if(newSelectedModule != null)
 		   		{
 		   		int selIndex = (new Integer(newSelectedModule).intValue()) -1;
 
 		   		if(newModulesList.size() < 2 || selIndex == 0)
 		   		{
 		   			logger.debug("first module selected to move up");
 					return "modules_sort";
 		   		}
 		   		ModuleObjService  mod  = (ModuleObjService) newModulesList.get(selIndex);
 	  	  		logger.debug("calling sort for " + mod.getTitle());
 		   		moduleService.sortModule(mod,courseId,"up");
 		   		newModulesList = getModuleService().getModules(courseId);
 				newList  = forSelectItemsList(newModulesList);
 				newSelectedModule = new Integer(selIndex).toString();
 		   		}
 		  	}
 		  catch (MeleteException me)
 		  	{
 			logger.error(me.toString());
 			me.printStackTrace();
 	  		String ErrMsg = bundle.getString("sort_fail");
 	  		FacesMessage msg =new FacesMessage(ErrMsg);
 		  	msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			ctx.addMessage (null, msg);
 		  	}
 	 	  return "modules_sort";
 	  }
 
 	  public String MoveItemDownAction()
 	  {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 	  	Map sessionMap = ctx.getExternalContext().getSessionMap();
 		String courseId = (String)sessionMap.get("courseId");
 
 	  	try{
 			 	if(newSelectedModule != null)
 		   		{
 		   		int selIndex = (new Integer(newSelectedModule).intValue()) -1;
 
 		   		if(newModulesList.size() < 2 || (selIndex == newModulesList.size()-1))
 		   		{
 		   			logger.debug("last module selected to move down");
 					return "modules_sort";
 		   		}
 		   		ModuleObjService  mod  = (ModuleObjService) newModulesList.get(selIndex);
 	  	  		logger.debug("calling sort for " + mod.getTitle());
 		   		moduleService.sortModule(mod,courseId,"down");
 		   		newModulesList = getModuleService().getModules(courseId);
 				newList  = forSelectItemsList(newModulesList);
 				newSelectedModule = new Integer(selIndex+1).toString();
 		   		}
 		  	}
 		  catch (MeleteException me)
 		  	{
 			logger.error(me.toString());
 			String ErrMsg = bundle.getString("sort_fail");
 	  		FacesMessage msg =new FacesMessage(ErrMsg);
 		  	msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			ctx.addMessage (null, msg);
 		  	}
          return "modules_sort";
 	  }
 
 	  public String MoveItemAllDownAction()
 	  {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 	  	Map sessionMap = ctx.getExternalContext().getSessionMap();
 		String courseId = (String)sessionMap.get("courseId");
 
 	  	try{
 			 	if(newSelectedModule != null)
 		   		{
 		   		int selIndex = (new Integer(newSelectedModule).intValue()) -1;
 
 		   		if(newModulesList.size() < 2 || (selIndex == newModulesList.size()-1))
 		   		{
 		   			logger.debug("last module selected to move down");
 					return "modules_sort";
 		   		}
 		   		ModuleObjService  mod  = (ModuleObjService) newModulesList.get(selIndex);
 	  	  		logger.debug("calling sort for " + mod.getTitle());
 		   		moduleService.sortModule(mod,courseId,"allDown");
 		   		newModulesList = getModuleService().getModules(courseId);
 				newList  = forSelectItemsList(newModulesList);
 				newSelectedModule = new Integer(selIndex+1).toString();
 		   		}
 		  	}
 		  catch (MeleteException me)
 		  	{
 			logger.error(me.toString());
 			String ErrMsg = bundle.getString("sort_fail");
 	  		FacesMessage msg =new FacesMessage(ErrMsg);
 		  	msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			ctx.addMessage (null, msg);
 		  	}
          return "modules_sort";
 	  }
 
 	public String gotoSortSections()
 	{
 			resetValues();
 			//fetch the first module and its sections
 			setFormName("SortSectionForm");
 			return "sections_sort";
 	}
 
 	/**
 	 * @return navigation for sort modules
 	 */
 	public String goToSortModules(){
 		  resetValues();
 		   setFormName("");
 		   return "modules_sort";
 
 	}
 	/**
 	 * @return Returns the formName.
 	 */
 	public String getFormName() {
 		return formName;
 	}
 	/**
 	 * @param formName The formName to set.
 	 */
 	public void setFormName(String formName) {
 		this.formName = formName;
 	}
 
 	public List getAllModulesList()
 	{
 		if(allModules == null || allModulesList == null )
 		{
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		String courseId = (String)sessionMap.get("courseId");
 		allModules = new ArrayList();
 		allModules = moduleService.getModules(courseId);
 		 if (allModules.size() > 0)
 	      {
 	        if(currModule == null || currModule.length()==0)
 			{
 			  currModule = "1";
 			}
 	      }
 		}
 		allModulesList = forSelectItemsList(allModules);
 		return allModulesList;
 	}
 	/**
 	 * @return Returns the currModule.
 	 */
 	public String getCurrModule() {
 
 		if(currModule == null)
 		{
 			getAllModulesList();
 		}
 		return currModule;
 	}
 	/**
 	 * @param currModule The currModule to set.
 	 */
 	public void setCurrModule(String currModule) {
 		this.currModule = currModule;
 	}
 
 	public void nextModuleSections(ValueChangeEvent event)throws AbortProcessingException
 	{
 		UIInput moduleSelect = (UIInput)event.getComponent();
 		String selModId = (String)moduleSelect.getValue();
 
 		currSecList = null;
 		currSectionsList = null;
 		int modId= new Integer(selModId).intValue()-1;
 		if(modId > 0)
 		{
 		ModuleObjService  mod = (ModuleObjService) allModules.get(modId);
 		currSectionsList = new ArrayList();
 		currSectionsList = getSectionService().getSortSections(mod);
 		currSecList  = forSelectSectionsItemsList(currSectionsList);
 		}
 		newSectionsList = null;
 		newSecList = null;
 	}
 
 	public List getCurrSecList()
 	{
 		try{
 			if(currSectionsList == null)
 			{
 				if(allModules != null && allModules.size() > 0 )
 				{
 					if(currModule == null)	currModule = "1";
 					ModuleObjService  mod  = (ModuleObjService) allModules.get(new Integer(currModule).intValue() -1);
 					currSectionsList = new ArrayList();
 					currSectionsList = getSectionService().getSortSections(mod);
 				}
 			}
 			currSecList  = forSelectSectionsItemsList(currSectionsList);
 		} catch(Exception e)
 		{
 			logger.error("error in getting currSecList "+e.toString());
 			e.printStackTrace();
 		}
 		return currSecList;
 	}
 
 	/**
 	 * @return Returns the newList.
 	 */
 	public List getNewSecList() {
 		try{
 			if(newSectionsList == null)
 			{
 				newSectionsList = currSectionsList;
 				newSecList  = forSelectSectionsItemsList(newSectionsList);
 			}
 
 		} catch(Exception e)
 		{
 			logger.error("error in getting newSecList "+e.toString());
 			e.printStackTrace();
 		}
 		return newSecList;
 	}
 
 	public String MoveSectionItemAllUpAction()
 	  {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 
 	  	try{
 	  		if(newSelectedSection != null)
 				{
 				int selIndex = new Integer(newSelectedSection).intValue();
 				if((selIndex == 0) || newSectionsList == null || newSectionsList.size() < 2 )
 				{
 					logger.debug("one item in the list or first section is selected to move all up");
 			  		return "sections_sort";
 				}
 				ModuleObjService  mod  = (ModuleObjService) allModules.get(new Integer(currModule).intValue() -1);
 				String sort_sec_id = ((SectionBean)newSectionsList.get(selIndex)).getSection().getSectionId().toString();
 				moduleService.sortSectionItem(mod,sort_sec_id, "allUp");
 				newSectionsList = sectionService.getSortSections(mod);
 				newSecList = forSelectSectionsItemsList(newSectionsList);
 				newSelectedSection = new Integer(0).toString();
 				}
 		}
 	catch (MeleteException me)
 		{
 		logger.error(me.toString());
 		String ErrMsg = bundle.getString("sort_fail");
 		FacesMessage msg =new FacesMessage(ErrMsg);
 		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 		ctx.addMessage (null, msg);
 		}
 return "sections_sort";
 }
 	 public String MoveSectionItemUpAction()
 	  {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 
 	  	try{
 	  		logger.debug("new selected value is" + newSelectedSection);
 			if(newSelectedSection != null)
 				{
 				int selIndex = new Integer(newSelectedSection).intValue();
 				if((selIndex == 0) || newSectionsList == null || newSectionsList.size() < 2 )
 				{
 					logger.debug("one item in the list or first section is selected to move up");
 			  		return "sections_sort";
 				}
 				ModuleObjService  mod  = (ModuleObjService) allModules.get(new Integer(currModule).intValue() -1);
 				String sort_sec_id = ((SectionBean)newSectionsList.get(selIndex)).getSection().getSectionId().toString();
 				moduleService.sortSectionItem(mod,sort_sec_id, "up");
 				newSectionsList = sectionService.getSortSections(mod);
 				newSecList = forSelectSectionsItemsList(newSectionsList);
 				newSelectedSection = new Integer(selIndex - 1).toString();
 				}
 		}
 	catch (MeleteException me)
 		{
 		logger.error(me.toString());
 		String ErrMsg = bundle.getString("sort_fail");
 		FacesMessage msg =new FacesMessage(ErrMsg);
 		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 		ctx.addMessage (null, msg);
 		}
 return "sections_sort";
 }
 
   public String MoveSectionItemDownAction()
 	  {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 
 	  	try{
 
 			if(newSelectedSection != null)
 				{
 				int selIndex = new Integer(newSelectedSection).intValue();
 				if(newSectionsList == null || newSectionsList.size() < 2 || (selIndex == newSectionsList.size()-1))
 				{
 					logger.debug("one item in the list or last section is selected to move down");
 			  		return "sections_sort";
 				}
 				ModuleObjService  mod  = (ModuleObjService) allModules.get(new Integer(currModule).intValue() -1);
 				String sort_sec_id = ((SectionBean)newSectionsList.get(selIndex)).getSection().getSectionId().toString();
 				moduleService.sortSectionItem(mod,sort_sec_id, "down");
 				newSectionsList = sectionService.getSortSections(mod);
 				newSecList = forSelectSectionsItemsList(newSectionsList);
 				newSelectedSection = new Integer(selIndex + 1).toString();
 				}
 		}
 	catch (MeleteException me)
 		{
 		logger.error(me.toString());
 		String ErrMsg = bundle.getString("sort_fail");
 		FacesMessage msg =new FacesMessage(ErrMsg);
 		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 		ctx.addMessage (null, msg);
 		}
 return "sections_sort";
 }
 
   public String MoveSectionItemAllDownAction()
   {
 	FacesContext ctx = FacesContext.getCurrentInstance();
   	ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 
   	try{
 
 		if(newSelectedSection != null)
 			{
 			int selIndex = new Integer(newSelectedSection).intValue();
 			if(newSectionsList == null || newSectionsList.size() < 2 || (selIndex == newSectionsList.size()-1))
 			{
 				logger.debug("one item in the list or last section is selected to move down");
 		  		return "sections_sort";
 			}
 			ModuleObjService  mod  = (ModuleObjService) allModules.get(new Integer(currModule).intValue() -1);
 			String sort_sec_id = ((SectionBean)newSectionsList.get(selIndex)).getSection().getSectionId().toString();
 			moduleService.sortSectionItem(mod,sort_sec_id, "allDown");
 			newSectionsList = sectionService.getSortSections(mod);
 			newSecList = forSelectSectionsItemsList(newSectionsList);
 			newSelectedSection = new Integer(newSectionsList.size()-1).toString();
 			}
 	}
 catch (MeleteException me)
 	{
 	logger.error(me.toString());
 	String ErrMsg = bundle.getString("sort_fail");
 	FacesMessage msg =new FacesMessage(ErrMsg);
 	msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 	ctx.addMessage (null, msg);
 	}
 return "sections_sort";
 }
 	// end sort code
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService() {
 		return moduleService;
 	}
 	/**
 	 * @param ModuleService The ModuleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	}
 
 
 	public HtmlPanelGrid getSeqTable() {
 		return null;
 	}
 
 	/**
 	 * @return Returns the sectionService.
 	 */
 	public SectionService getSectionService() {
 		return sectionService;
 	}
 	/**
 	 * @param sectionService The sectionService to set.
 	 */
 	public void setSectionService(SectionService sectionService) {
 		this.sectionService = sectionService;
 	}
 	/**
 	 * @return Returns the newSelectedSection.
 	 */
 	public String getNewSelectedSection() {
 		return newSelectedSection;
 	}
 	/**
 	 * @param newSelectedSection The newSelectedSection to set.
 	 */
 	public void setNewSelectedSection(String newSelectedSection) {
 		this.newSelectedSection = newSelectedSection;
 	}
 }
