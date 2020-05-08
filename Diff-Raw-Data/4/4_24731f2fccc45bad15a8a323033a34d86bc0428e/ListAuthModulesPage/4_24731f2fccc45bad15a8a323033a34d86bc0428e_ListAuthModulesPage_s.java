 /**********************************************************************************
  *
  * $URL$
  *
  ***********************************************************************************
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIData;
 import javax.faces.component.UIInput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionBeanService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.component.app.melete.Module;
 import org.etudes.component.app.melete.ModuleDateBean;
 import org.etudes.component.app.melete.Section;
 import org.etudes.component.app.melete.SectionBean;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 
 /**
  * Gets the module beans for author
  *
  */
 
 class SecModObj
 {
 	int moduleId;
 	SectionBean secBean;
 	ModuleDateBean mdBean;
 	
 	public SecModObj(int modId, SectionBean sBean, ModuleDateBean mBean)
 	{
 		moduleId = modId;
 		secBean = sBean;
 		mdBean = mBean;
 	}
 
 	public int getModuleId()
 	{
 		return moduleId;
 	}
 	
 	public SectionBean getSecBean()
 	{
 		return secBean;
 	}
 
 	public ModuleDateBean getMdBean()
 	{
 		return mdBean;
 	}
 
 }
 
 public class ListAuthModulesPage implements Serializable
 {
 
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(ListAuthModulesPage.class);
 
 	private List moduleDateBeans = null;
 	
 	private Map mdbeansMap = null;
 	
 	private Map secObjMap = null;
 
 	/** identifier field */
 	private int showModuleId;
 
 	private String formName;
 
 	private Date currentDate;
 
 	private boolean selectedSection;
 
 	private Boolean nomodsFlag;
 
 	private boolean expandAllFlag = true;
 
 	private boolean autonumber;
 	
 	// This needs to be set later using Utils.getBinding
 	String courseId;
 
 	String userId;
 
 	// rashmi added
 	int count;
 
 	int selectedModId;
 
 	private List selectedModIds = null;
 
 	private List selectedSecIds = null;
 
 	boolean moduleSelected;
 
 	int selectedSecId;
 
 	boolean sectionSelected;
 	
 	boolean selectAllFlag;
 
 	private ModuleService moduleService;
 
 	private boolean trueFlag = true;
 
 	private List nullList = null;
 
 	private Integer printModuleId;
 	
 	int listSize;
 
 	// added by rashmi on apr 8
 	private String isNull = null;
 	
 	private boolean isInstructor;
 	
 	/** Dependency: The Melete Security service. */
 	protected MeleteSecurityService meleteSecurityService;
 
 	private UIData table;
 	private UIData secTable;
 	
 	/**
 	 * @return value of datatable (in which modules are rendered)
 	 */
 	public UIData getTable()
 	{
 		return table;
 	}
 
 	/**
 	 * @param table module datatable to set
 	 */
 	public void setTable(UIData table)
 	{
 		this.table = table;
 	}
 
 	public ListAuthModulesPage()
 	{
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
     	MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
     	
 		courseId = mPage.getCurrentSiteId();
 		userId = mPage.getCurrentUser().getId();
 		
 		Map params = context.getExternalContext().getRequestParameterMap();
 		if (((String) params.get("listauthmodulesform:lamexp")) == null) {
 			nomodsFlag = null;
 			setShowModuleId(-1);
 			count = 0;
 			selectedModId = -1;
 			moduleSelected = false;
 			selectedModIds = null;
 			selectedSecIds = null;
 
 			selectedSecId = -1;
 			sectionSelected = false;
 			selectAllFlag = false;
 			listSize = 0;
 			binding = Util.getBinding("#{authorPreferences}");
 			AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding
 					.getValue(context);
 			String expFlag = preferencePage.getUserView();
 			if (expFlag.equals("true")) {
 				expandAllFlag = true;
 			} else {
 				expandAllFlag = false;
 			}
 		}
 		
 	}
 
 	/**
 	 * Reset all flags(expand,collapse,auto numbering) and set all indexes to -1 and lists to null
 	 * 
 	 */
 	public void resetValues()
 	{
 		setShowModuleId(-1);
 		nomodsFlag = null;
 		count = 0;
 		selectedModId = -1;
 		moduleSelected = false;
 		selectedModIds = null;
 		selectedSecIds = null;
 
 		selectedSecId = -1;
 		sectionSelected = false;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 		String expFlag = preferencePage.getUserView();
 		if (expFlag.equals("true"))
 		{
 			expandAllFlag = true;
 		}
 		else
 		{
 			expandAllFlag = false;
 		}
                 String autonum = preferencePage.getMaterialAutonumber();
 		if (autonum.equals("true"))
 		{  autonumber = true;
 		} else {
 		   autonumber = false;
 		}
 		selectAllFlag = false;
 		listSize = 0;
 		moduleDateBeans = null;
 		mdbeansMap = null;
 		secObjMap = null;
 	}
 
 	/**
 	 * @return autoNumbering preference value
 	 */
 	public boolean isAutonumber()
 	{
 		return autonumber;
 	};
 	
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService()
 	{
 		return moduleService;
 	}
 
 	/**
 	 * @param moduleService
 	 *        The moduleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService)
 	{
 		this.moduleService = moduleService;
 	}
 
 	/**
 	 * @return boolean value of trueFlag
 	 */
 	public boolean getTrueFlag()
 	{
 		return trueFlag;
 	}
 
 	/**
 	 * @param trueFlag boolean value of trueFlag
 	 */
 	public void setTrueFlag(boolean trueFlag)
 	{
 		this.trueFlag = trueFlag;
 	}
 
 	/**
 	 * @return value of nullList
 	 */
 	public List getNullList()
 	{
 		return nullList;
 	}
 
 	/**
 	 * @param nullList value of nullList
 	 */
 	public void setNullList(List nullList)
 	{
 		this.nullList = nullList;
 	}
 
 	
 	/**Method that triggers when a module is selected
 	 * @param event ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectedModule(ValueChangeEvent event) throws AbortProcessingException
     {
 		if ((moduleDateBeans == null)||(moduleDateBeans.size() == 0)) return;
 		if (selectAllFlag == false) {
 			UIInput mod_Selected = (UIInput) event.getComponent();
 			if (((Boolean) mod_Selected.getValue()).booleanValue() == true)
 				count++;
 			else
 				count--;
 
 			String title = (String) mod_Selected.getAttributes().get("title");
 			if (title != null) {
 				selectedModId = Integer.parseInt(title);
 
 				if (selectedModIds == null) {
 					selectedModIds = new ArrayList();
 				}
 				selectedModIds.add(new Integer(selectedModId));
 				moduleSelected = true;
 			}
 		}
 		return;
 	}
 
 	/**Method that triggers when a section is selected
 	 * @param event ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectedSection(ValueChangeEvent event) throws AbortProcessingException
 	{
 		if ((secObjMap == null)||(secObjMap.size() == 0)) return;
 		
 		UIInput sec_Selected = (UIInput) event.getComponent();
 			
 		if (((Boolean) sec_Selected.getValue()).booleanValue() == true)
 			count++;
 		else
 			count--;
 		if (sec_Selected.getParent() != null) {
 			UIColumn secColumn = (UIColumn) sec_Selected.getParent();
 			List<UIComponent> secChildren = secColumn.getChildren();
 			if ((secChildren != null) && (secChildren.size() > 0)) {
 				for (Iterator itr = secChildren.listIterator(); itr.hasNext();) {
 					UIComponent comp = (UIComponent) itr.next();
 					if (comp.getId().equals("hacksecid")) {
 						UIInput hiddenSec = (UIInput) comp;
 						if (selectedSecIds == null) {
 							selectedSecIds = new ArrayList();
 						}
 						selectedSecIds.add((Integer) hiddenSec.getValue());
 					}
 				}
 
 			}
 		}
 		if ((selectedSecIds != null)&&(selectedSecIds.size() > 0)) 
 		{
 			sectionSelected = true;
 		}
 		if ((selectedSecIds != null)&&(selectedSecIds.size() == 1)) 
 		{
 			selectedModId = ((SecModObj)secObjMap.get(selectedSecIds.get(0))).getModuleId();
 		}
 		return;
 	}
 	
 	/**Method that triggers when all modules are selected
 	 * @param event ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectAllModules(ValueChangeEvent event)
 			throws AbortProcessingException {
 		ModuleDateBean mdbean = null;
 		selectAllFlag = true;
 		int k = 0;
 		if (selectedModIds == null) {
 			selectedModIds = new ArrayList();
 		}
 		if ((moduleDateBeans != null) && (moduleDateBeans.size() > 0)) {
 			for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext();) {
 				mdbean = (ModuleDateBean) i.next();
 				mdbean.setSelected(true);
 				selectedModIds.add(new Integer(mdbean.getModuleId()));
 			}
 			count = moduleDateBeans.size();
 			if (count == 1)
 				selectedModId = mdbean.getModuleId();
 			moduleSelected = true;
 		}
 		return;
 	}
 
 	/** Reset selected module lists and selectAllFlag to false
 	 * 
 	 */
 	public void resetSelectedLists()
 	{
 		selectedModIds = null;
 		selectedSecIds = null;
 		selectAllFlag = false;
 	}
 	
 	/** Get list of modules, flag modules with invalid dates
 	 * @return list of ModuleDateBean objects
 	 */
 	public List getModuleDateBeans()
 	{
 		resetSelectedLists();
 		setCurrentDate(Calendar.getInstance().getTime());
 		FacesContext context = FacesContext.getCurrentInstance();
 		// reset courseid. Its getting lost when edit_module called from coursemap calls TOC
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
     	MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
     	courseId = mPage.getCurrentSiteId();
 
     	try
 		{
 			ModuleService modServ = getModuleService();
 			// fetch beans
 			if (nomodsFlag == null || moduleDateBeans == null)
 				moduleDateBeans = modServ.getModuleDateBeans(userId, courseId);
 			// for bug reports
 			if (moduleDateBeans == null || moduleDateBeans.size() == 0) 
 			{
 				listSize = 0;
 				nomodsFlag = true;
 				mdbeansMap = null;
 				secObjMap = null;
 				return moduleDateBeans;
 			}
 			
 			if (moduleDateBeans != null && moduleDateBeans.size() > 0)
 			{
 				mdbeansMap = getMdbeansMap(moduleDateBeans);
 				secObjMap = getSecObjMap(moduleDateBeans);
 			}
 			
 			
 			// end
 			nomodsFlag = false;
 			listSize = moduleDateBeans.size();
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 
 		return moduleDateBeans;
 	}
 
 	/**
 	 * @return value of currentDate
 	 */
 	public Date getCurrentDate()
 	{
 		return currentDate;
 	}
 
 	/**
 	 * @param currentDate date value
 	 */
 	public void setCurrentDate(Date currentDate)
 	{
 		this.currentDate = currentDate;
 	}
 
 	/**
 	 * @param moduleDateBeansList list of ModuleDateBean objects
 	 */
 	public void setModuleDateBeans(List moduleDateBeansList)
 	{
 		moduleDateBeans = moduleDateBeansList;
 	}
 
 	/**
 	 * @return value of showModuleId
 	 */
 	public int getShowModuleId()
 	{
 		return this.showModuleId;
 	}
 
 	/**
 	 * @param moduleId integer value of moduleId
 	 */
 	public void setShowModuleId(int moduleId)
 	{
 		this.showModuleId = moduleId;
 	}
 
 	/**
 	 * @return boolean value of selectedSection
 	 */
 	public boolean getSelectedSection()
 	{
 		return selectedSection;
 	}
 
 	/**
 	 * @param selectedSection boolean value of selectedSection
 	 */
 	public void setSelectedSection(boolean selectedSection)
 	{
 		this.selectedSection = selectedSection;
 	}
 
 	/** Invokes getModuleDateBeans method to set nomodsFlag value
 	 * @return boolean value of nomodsFlag
 	 */
 	public boolean getNomodsFlag()
 	{
 		if (nomodsFlag == null) 
 		{
 			getModuleDateBeans();
 		}
 		return nomodsFlag;
 	}
 
 	/**
 	 * @param nomodsFlag boolean value
 	 */
 	public void setNomodsFlag(boolean nomodsFlag)
 	{
 		this.nomodsFlag = nomodsFlag;
 	}
 
 	/**
 	 * @return boolean value of expandAllFlag
 	 */
 	public boolean getExpandAllFlag()
 	{
 		return expandAllFlag;
 	}
 
 	/**
 	 * @param expandAllFlag boolean value
 	 */
 	public void setExpandAllFlag(boolean expandAllFlag)
 	{
 		this.expandAllFlag = expandAllFlag;
 	}
 	
 	/**
 	 * @return boolean value of selectAllFlag
 	 */
 	public boolean getSelectAllFlag()
 	{
 		return selectAllFlag;
 	}
 
 	/**
 	 * @param selectAllFlag boolean value
 	 */
 	public void setSelectAllFlag(boolean selectAllFlag)
 	{
 		this.selectAllFlag = selectAllFlag;
 	}	
 
 	/**
 	 * @return integer value of listSize
 	 */
 	public int getListSize()
 	{
 		return listSize;
 	}
 	
 	/**
 	 * @param listSize integer value
 	 */
 	public void setListSize(int listSize)
 	{
 		this.listSize = listSize;
 	}
 	
 	/** Method to expand or collapse individual modules' sections
 	 * @return list_auth_modules
 	 */
 	public String showHideSections()
 	{
 		resetSelectedLists();
 		if (getExpandAllFlag() == true)
 		{
 			setShowModuleId(-1);
 			setExpandAllFlag(false);
 		}
 		else
 		{
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 			ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 			if (getShowModuleId() != mdbean.getModuleId())
 			{	
 				setShowModuleId(mdbean.getModuleId());
 			}
 			else
 			{
 				setShowModuleId(-1);
 				setExpandAllFlag(false);
 			}
 		}
 		saveModuleDates();
 		return "list_auth_modules";
 	}
 	
 	/** Method to expand or collapse all modules' sections
 	 * @return list_auth_modules
 	 */
 	public String expandCollapseAction()
 	{
 		resetSelectedLists();
 	    saveModuleDates();
 		if (getExpandAllFlag() == false)
 		{		
 		  setExpandAllFlag(true);
 		}
 		else
 		{	
 		  setExpandAllFlag(false);
 		  setShowModuleId(-1);
 		}  
 		return "list_auth_modules";		
 	}
 	
 	
 	/**Reset selected lists and navigate to add module page
 	 * @return add_module
 	 */
 	public String AddModuleAction()
 	{
 		resetSelectedLists();
 		if (!saveModuleDates()) return "list_auth_modules";
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{addModulePage}");
 
 		AddModulePage amPage = (AddModulePage) binding.getValue(ctx);
 		amPage.setModuleNull();
 
 		return "add_module";
 	}
 
 	/** Reset selected lists and navigate to add section page
 	 * @return editmodulesections or list_auth_modules
 	 */
 	public void AddContentAction(ActionEvent evt)
 	{
 		if (!saveModuleDates()) return ;
 		if (moduleDateBeans == null || moduleDateBeans.size() == 0) return;
 		
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		if (count >= 2)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_error");
 			addMessage(ctx, "Select  ERROR", msg, FacesMessage.SEVERITY_ERROR);
 			count = 0;
 
 			moduleSelected = false;
 			sectionSelected = false;
 			return ;
 		}
 		count = 0;
 		
 		if ((moduleSelected == false) && (sectionSelected == false))
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_add");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 			return;
 		}
 		// module selected
 		if (moduleSelected || sectionSelected) {
 			if (selectedModId <= 0) return;
 			if ((moduleDateBeans != null) && (moduleDateBeans.size() > 0)) {
 				ModuleDateBean mdbean = (ModuleDateBean) mdbeansMap
 						.get(selectedModId);
 
 				if (mdbean != null) {
 					ValueBinding binding = Util
 							.getBinding("#{editSectionPage}");
 					FacesContext context = FacesContext.getCurrentInstance();
 					EditSectionPage editPage = (EditSectionPage) binding
 							.getValue(context);
 					editPage.setModule(mdbean.getModule());
 					Integer newSecId = editPage.addBlankSection();
 
 					count = 0;
 					moduleSelected = false;
 					// Mallika -3/24/05
 					sectionSelected = false;
 					try {
 						if (newSecId != null)
 							ctx.getExternalContext().redirect(
 									"editmodulesections.jsf?sectionId="
 											+ newSecId.toString());
 					} catch (Exception e) {
 						return;
 					}
 				}
 			}
 		}
 		return;
 	}
 
 	/** Inactivate modules and display a message about modules that have been inactivated
 	 * @return list_auth_modules
 	 */
 	public String InactivateAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		List selModBeans = null;
 		ModuleDateBean mdbean = null;
 		if (!saveModuleDates()) return "list_auth_modules";
 		if (moduleDateBeans == null || moduleDateBeans.size() == 0) return "list_auth_modules";
 		if (sectionSelected)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_module");
 			addMessage(ctx, "Select Module", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		// module selected
 		if (moduleSelected && selectedModIds != null)
 		{
 			if (selModBeans == null)
 			{
 				selModBeans = new ArrayList();
 			}
 			for (ListIterator i = selectedModIds.listIterator(); i.hasNext();)
 			{
 				mdbean = (ModuleDateBean) mdbeansMap.get(((Integer) i.next()).intValue());
 				if (mdbean != null) selModBeans.add(mdbean);
 			}
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			try
 			{
 				if ((selModBeans != null) && (selModBeans.size() > 0)
 						&& (moduleDateBeans != null)
 						&& (moduleDateBeans.size() > 0)) {
 					getModuleService().archiveModules(selModBeans,
 							moduleDateBeans, courseId);
 					StringBuffer modTitles = new StringBuffer();
 					mdbean = null;
 					for (ListIterator i = selModBeans.listIterator(); i
 							.hasNext();) {
 						mdbean = (ModuleDateBean) i.next();
 						modTitles.append(mdbean.getModule().getTitle());
 						modTitles.append(", ");
 					}
 					modTitles.delete(modTitles.toString().length() - 2,
 							modTitles.toString().length());
 					String msg1 = bundle.getString("inactivate_message1");
 					String msg2 = bundle.getString("inactivate_message2");
 					addMessage(ctx, "Inactivate Message",
 							msg1 + modTitles.toString() + msg2,
 							FacesMessage.SEVERITY_INFO);
 				}
 			}
 			catch (Exception ex)
 			{
 				logger.debug(ex.toString());
 				String errmsg = bundle.getString("archive_fail");
 				addMessage(ctx, "Error Message", errmsg, FacesMessage.SEVERITY_ERROR);
 				return "list_auth_modules";
 			}
 			count = 0;
 			// Mallika - 3/24/05 added this to prevent selected value from being stored
 			moduleSelected = false;
 			sectionSelected = false;
 			return "list_auth_modules";
 		}
 
 		if ((moduleSelected == false) && (sectionSelected == false))
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_module");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		count = 0;
 		moduleSelected = false;
 		sectionSelected = false;
 		return "list_auth_modules";
 	}
 	
 	/** Reset values and redirect to sort modules page
 	 * @return modules_sort
 	 */
 	public String goToSortModules(){
 		if (!saveModuleDates()) return "list_auth_modules";
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding smsBinding = Util.getBinding("#{sortModuleSectionPage}");
 		SortModuleSectionPage smsPage = (SortModuleSectionPage) smsBinding.getValue(context);
 		return smsPage.goToSortModules();
 
 	}
 	
 	/**
 	 * @return restore_modules page
 	 */
 	public String goToRestoreModules()
 	{
 		if (!saveModuleDates()) return "list_auth_modules";
 		return "restore_modules";
 	}
 	
 	/** 
 	 * @return importexportmodules page
 	 */
 	public String importExportModules()
 	{
 		if (!saveModuleDates()) return "list_auth_modules";
 		return "importexportmodules";
 	}
 
 	/**Redirect to edit module page
 	 * @return edit_module
 	 */
 	public String redirectToEditModule()
 	{
 		if (!saveModuleDates()) return "list_auth_modules";
 		return "edit_module";
 	}
 
 	/**
 	 * Redirect to edit module page with selected module id
 	 * 
 	 * @param evt
 	 *        ActionEvent object
 	 */
 	public void editModule(ActionEvent evt)
 	{
 		if (!saveModuleDates()) return;
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 		int selModId = Integer.parseInt((String) params.get("editmodid"));
 
 		ModuleDateBean mdbean = (ModuleDateBean) mdbeansMap.get(selModId);
 		ValueBinding binding = Util.getBinding("#{editModulePage}");
 		EditModulePage emPage = (EditModulePage) binding.getValue(ctx);
 		emPage.setEditInfo(mdbean);
 		emPage.resetModuleNumber();
 		try
 		{
 			FacesContext.getCurrentInstance().getExternalContext().redirect("edit_module.jsf?editmodid=" + selModId);
 		}
 		catch (Exception e)
 		{
 			e.getMessage();
 		}
 	}
 	
 	protected Map getMdbeansMap(List moduleDateBeans)
 	{
 		if ((moduleDateBeans == null)||(moduleDateBeans.size() == 0)) return null;
 		Map mdbeansMap = new LinkedHashMap<Integer, ModuleDateBean>();
 
 		for (Iterator itr = moduleDateBeans.listIterator(); itr.hasNext();)
 		{
 			ModuleDateBean mdbean = (ModuleDateBean) itr.next();
 		    mdbeansMap.put(mdbean.getModuleId(), mdbean);
 		}	
 		return mdbeansMap;
 	}
 	
 	protected Map getSecObjMap(List moduleDateBeans)
     {
 		if ((moduleDateBeans == null) || (moduleDateBeans.size() == 0))
 			return null;
 		Map secobjMap = new LinkedHashMap<Integer, SecModObj>();
 
 		for (Iterator itr = moduleDateBeans.listIterator(); itr.hasNext();) {
 			ModuleDateBean mdbean = (ModuleDateBean) itr.next();
 			List sectionBeans = mdbean.getSectionBeans();
 			if ((sectionBeans != null) && (sectionBeans.size() > 0)) {
 				int moduleId = mdbean.getModuleId();
 				for (Iterator sItr = sectionBeans.listIterator(); sItr
 						.hasNext();) {
 					SectionBean secbean = (SectionBean) sItr.next();
 					secobjMap
 							.put(secbean.getSection().getSectionId(),
 									new SecModObj(moduleId,
 											secbean, mdbean));
 				}
 			}
 		}
 		return secobjMap;
 	}
 
 	/** Redirect to edit section page
 	 * @return editmodulesections
 	 */
 	public String redirectToEditSection()
 	{
 		if (!saveModuleDates()) return "list_auth_modules";
 		return "editmodulesections";
 	}
 
 	/** Redirect to edit section page with selected section id
 	 * @param evt ActionEvent object
 	 */
 	public void editSection(ActionEvent evt)
 	{
 		if (!saveModuleDates()) return;
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 		int selModId = Integer.parseInt((String) params.get("editsecmodid"));
 		int selSecId = Integer.parseInt((String) params.get("sectionId"));
 
 		try
 		{
 			ctx.getExternalContext().redirect("editmodulesections.jsf?sectionId=" + selSecId);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/** Redirect to delete confirmation page with selected modules or sections
 	 * @return delete_module or list_auth_modules
 	 */
 	public String deleteAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		List delMods = null;
 		List delSecBeans = null;
 		count = 0;
 		if (!saveModuleDates()) return "list_auth_modules";
 		if ((moduleDateBeans == null)||(moduleDateBeans.size() == 0)) return "list_auth_modules";
 
 		// added by rashmi
 		if (!moduleSelected && !sectionSelected)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_delete");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		// add end
 		// module selected
 		if (moduleSelected)
 		{
 			ModuleDateBean mdbean = null;
 			if (delMods == null)
 			{
 				delMods = new ArrayList();
 			}
 			if (selectedModIds != null)
 			{
 			  for (ListIterator i = selectedModIds.listIterator(); i.hasNext();)
 			  {
 				mdbean = (ModuleDateBean) mdbeansMap.get(((Integer) i.next()).intValue());
 				delMods.add(mdbean.getModule());
 			  }
 			}
 			ValueBinding binding = Util.getBinding("#{deleteModulePage}");
 			DeleteModulePage dmPage = (DeleteModulePage) binding.getValue(ctx);
 			// dmPage.setMdbean(mdbean);
 			dmPage.setModules(delMods);
 			dmPage.setSectionBeans(new ArrayList<SectionBeanService>());
 		//	List <> allActivenArchvModules = moduleService.getAllActivenArchvModules();
 			dmPage.setModuleSelected(true);
 			count = 0;
 			moduleSelected = false;
 			selectedModIds = null;
 		//	delModBeans = null;
 			// We do not want to bypass processing of section if sections and modules are selected
 			if (sectionSelected == false)
 			{
 				return "delete_module";
 			}
 		}
 		if (sectionSelected)
 		{
 			if ((secObjMap == null)||(secObjMap.size() == 0)) return "list_auth_modules";
 			ModuleDateBean mdbean = null;
 			SectionBeanService secBean = null;
 			if (delSecBeans == null)
 			{
 				delSecBeans = new ArrayList();
 			}
 			if(selectedSecIds == null)
 			{
 				ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 				String msg = bundle.getString("select_one_delete");
 				addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 				moduleSelected = false;
 				sectionSelected = false;
 				resetSelectedLists();
 				return "list_auth_modules";
 			}
 
 			for (ListIterator i = selectedSecIds.listIterator(); i.hasNext();)
 			{
 				Integer sectionId = (Integer) i.next();
 				secBean = ((SecModObj)secObjMap.get(sectionId)).getSecBean();
 				if (secBean != null) delSecBeans.add(secBean);
 			}
 			count = 0;
 			sectionSelected = false;
 			selectedSecIds = null;
 			
 			if (delSecBeans != null)
 			{	
 			ValueBinding binding = Util.getBinding("#{deleteModulePage}");
 			DeleteModulePage dmPage = (DeleteModulePage) binding.getValue(ctx);
 			//dmPage.setSection((Section) secBean.getSection());
 			dmPage.setSectionBeans(delSecBeans);
 			dmPage.setSectionSelected(true);
 			return "delete_module";
 			}
 		}
 
 		moduleSelected = false;
 		sectionSelected = false;
 		resetSelectedLists();
 		return "list_auth_modules";
 	}
 	
 	public boolean saveModuleDates()
 	{
 		if (getIsInstructor())
 		{
 			if ((moduleDateBeans == null)||(moduleDateBeans.size() == 0)) return true;
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			try
 			{
 				getModuleService().updateProperties(moduleDateBeans, courseId, userId);
 			}
 			catch (Exception e)
 			{
 				logger.debug(e.toString());
 				String msg = bundle.getString("list_auth_modules_fail");
 				addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/** Resets lists and save changes
 	 * @return page that request came from
 	 */
 	public String saveChanges()
 	{
 		resetSelectedLists();
 		saveModuleDates();
 		return "#";
 	}
 
 	/** Redirect to list auth page
 	 * @return list_auth_modules
 	 */
 	public String cancelChanges()
 	{
 		return "list_auth_modules";
 	}
 
 	/** Reset selected lists and redirect to module_post_steps page
 	 * @return module_post_steps
 	 */
 	public void viewNextsteps(ActionEvent evt)
 	{
 		resetSelectedLists();
 		if (!saveModuleDates()) return;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 		ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 		Integer selModId = mdbean.getModuleId();
 		try
 		{
 			ctx.getExternalContext().redirect("module_post_steps.jsf?editmodid=" + selModId.toString());
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 	
 	/** Reset selected lists and redirect to list of special accesses page
 	 * @return list_special_access
 	 */
 /*	public String specialAccessAction()
 	{
 		resetSelectedLists();
 		if (!saveModuleDates()) return "list_auth_modules";
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		if (root != null)
 		{
 			UIComponent listAuthForm = (UIComponent) root.findComponent("listauthmodulesform");
 			if (listAuthForm != null)
 			{
 				UIData table = (UIData) listAuthForm.findComponent("table");
 				ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 				ValueBinding binding = Util.getBinding("#{specialAccessPage}");
 				SpecialAccessPage specialAccessPage = (SpecialAccessPage) binding.getValue(ctx);
 				specialAccessPage.setModuleId(mdbean.getModule().getModuleId().intValue());
 				specialAccessPage.setSaList(null);
 			}
 		}
 		return "list_special_access";
 	}	*/
 
 	public void specialAccessAction(ActionEvent evt)
 	{
 		resetSelectedLists();
 		if (!saveModuleDates()) return;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 		ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 		Integer selModId = mdbean.getModuleId();
 		try
 		{
 			ctx.getExternalContext().redirect("list_special_access.jsf?editmodid=" + selModId.toString());
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 	
 	/**
 	 * @return String value of isNull
 	 */
 	public String getIsNull()
 	{
 		return isNull;
 	}
 
 	/**Reset values, set selected flags to false, count to 0
 	 * and selectedSecIds to null
 	 */
 	private void resetSubSectionValues()
 	{
 		sectionSelected = false;
 		moduleSelected = false;
 		count = 0;
 		selectedSecIds = null;
 		moduleDateBeans = null;
 		mdbeansMap = null;
 		secObjMap = null;
 	}
 
 	/** Get indentation level of a section
 	 * @param indentBean SectionBeanService object
 	 * @param secBeans list of section bean objects
 	 * @return true if indentation is less than 10 levels, false otherwise
 	 */
 	private boolean findIndent(SectionBeanService indentBean, List secBeans)
 	{
 		String pattern = "\\.";
 		int occurs = indentBean.getDisplaySequence().split(pattern).length - 1;
 
 		if (occurs >= 10)
 		{
 			return false;
 		}
 		else
 		{
 			String indDispSeq = indentBean.getDisplaySequence();
 			for (ListIterator i = secBeans.listIterator(); i.hasNext();)
 			{
 				SectionBeanService secBean = (SectionBeanService) i.next();
 				String sbDispSeq = secBean.getDisplaySequence();
 				if (sbDispSeq.startsWith(indDispSeq))
 				{
 					occurs = 0;
 					occurs = sbDispSeq.split(pattern).length - 1;
 					if (occurs >= 10) return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	
 	/** Indent this section by first checking if indentation is less than 10 levels
 	 * @return list_auth_modules
 	 */
 	public String CreateSubSectionAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		if (!saveModuleDates()) return "list_auth_modules";
 		if ((moduleDateBeans == null)||(moduleDateBeans.size() == 0)) return "list_auth_modules";
 		
 		// if module is selected then throw message
 		if (moduleSelected)
 		{
 			String msg = bundle.getString("list_select_indent");
 			addMessage(ctx, "Select  Indent", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		if ((moduleSelected == false) && (sectionSelected == false))
 		{
 			String msg = bundle.getString("list_select_one_indent");
 			addMessage(ctx, "Select Indent", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 		if (sectionSelected == true)
 		{
 			if ((secObjMap == null) || (secObjMap.size() == 0))
 			{
 				resetSubSectionValues();
 				return "list_auth_modules";
 			}
 			SecModObj smObj = null;
 			ModuleDateBean mdbean = null;
 			SectionBeanService secBean = null;
 			List indentSecBeans = null;
 			if (indentSecBeans == null)
 			{
 				indentSecBeans = new ArrayList();
 			}
 
 			if (selectedSecIds == null) 
 			{
 				resetSubSectionValues();
 				return "list_auth_modules";
 			}
 			// If one section is selected, we check if its the first section
 			// or if it is too deep to indent
 			// If multiple sections are selected, we indent those that we can
 			// and leave the others alone
 			if (selectedSecIds.size() == 1)
 			{	
 				smObj = (SecModObj) secObjMap.get(selectedSecIds.get(0));
 				if ((smObj != null)&&(smObj.getMdBean() != null)&&(smObj.getSecBean()!=null))
 				{
 					mdbean = smObj.getMdBean();
 					if (mdbean.getSectionBeans().size() < 2)
 					{
 						logger.debug("First section can't be change to subsection");
 						resetSubSectionValues();
 						return "list_auth_modules";
 					}
 					else
 					{
 						secBean = smObj.getSecBean();
 	
 						boolean indentOk = findIndent(secBean, mdbean.getSectionBeans());
 						// Only allow indent upto 10 levels
 						if (indentOk)
 						{
 							try
 							{
 								indentSecBeans.add(secBean);
 								moduleService.createSubSection(mdbean.getModule(), indentSecBeans);
 							}
 							catch (MeleteException me)
 							{
 								logger.debug(me.toString());
 								String msg = bundle.getString("indent_right_fail");
 								addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 							}
 						}
 						else
 						{
 							String msg = bundle.getString("indent_right_toodeep");
 							addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 						}
 					}
 				}
 				else
 				{
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 			}
 			else
 			{
 				// Multiple indent code
 				boolean res = checkDifModules(selectedSecIds);
 				if (res == true)
 				{
 					String msg = bundle.getString("list_select_in_onemodule");
 					addMessage(ctx, "Select Indent", msg, FacesMessage.SEVERITY_ERROR);
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				else
 				{
 					mdbean = null;
 					secBean = null;
 
 					for (ListIterator i = selectedSecIds.listIterator(); i.hasNext();)
 					{
 						Integer sectionId = (Integer) i.next();	
 						smObj = (SecModObj) secObjMap.get(sectionId);
 						if ((smObj != null)&&(smObj.getMdBean() != null)&&(smObj.getSecBean()!=null))
 						{
 							mdbean = smObj.getMdBean();
 							secBean = smObj.getSecBean();
 							boolean indentOk = findIndent(secBean, mdbean.getSectionBeans());
 							if (indentOk)
 							{
 								indentSecBeans.add(secBean);
 							}
 							try
 							{
 								moduleService.createSubSection(mdbean.getModule(), indentSecBeans);
 							}
 							catch (MeleteException me)
 							{
 								logger.debug(me.toString());
 								String msg = bundle.getString("indent_right_fail");
 								addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 							}
 						}
 					}
 				}
 			}
 		}
 		resetSubSectionValues();
 		int saveShowId = showModuleId;
 		resetValues();
 		setShowModuleId(saveShowId);
 		return "list_auth_modules";
 	}
 
 	/** Bring section one level up in indentation
 	 * @return list_auth_modules
 	 */
 	public String BringSubSectionLevelUpAction() {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader(
 				"org.etudes.tool.melete.bundle.Messages");
 		if (!saveModuleDates())
 			return "list_auth_modules";
 		if ((moduleDateBeans == null) || (moduleDateBeans.size() == 0))
 			return "list_auth_modules";
 
 		if (count == 0) {
 			String msg = bundle.getString("list_select_one_indent");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		// if module is selected then throw message
 		if (moduleSelected) {
 			String msg = bundle.getString("list_select_indent");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		// section selected
 		/*
 		 * if(sectionSelected && count != 1) { String msg =
 		 * bundle.getString("list_select_one_indent"); addMessage(ctx,"Error
 		 * Message
 		 * ",msg,FacesMessage.SEVERITY_ERROR); resetSubSectionValues(); return "
 		 * list_auth_modules"; }
 		 */
 
 		if (sectionSelected == true) {
 			if ((secObjMap == null) || (secObjMap.size() == 0))
 				return "list_auth_modules";
 			SecModObj smObj = null;
 			ModuleDateBean mdbean = null;
 			SectionBeanService secBean = null;
 			List indentSecBeans = null;
 			if (indentSecBeans == null) {
 				indentSecBeans = new ArrayList();
 			}
 
 			if (selectedSecIds == null) {
 				resetSubSectionValues();
 				return "list_auth_modules";
 			}
 			// If one section is selected, we check if its the top level section
 			// If multiple sections are selected, we indent those that we can
 			// and leave the others alone
 			if (selectedSecIds.size() == 1) {
 				smObj = (SecModObj) secObjMap.get(selectedSecIds.get(0));
 				if ((smObj != null) && (smObj.getMdBean() != null)
 						&& (smObj.getSecBean() != null))
 					{
 					mdbean = (ModuleDateBean) smObj.getMdBean();
 					secBean = (SectionBeanService) smObj.getSecBean();
 					if (checkTopLevelSection(secBean.getDisplaySequence())) {
 						logger.debug("Top level section can't indent left more");
 						resetSubSectionValues();
 						return "list_auth_modules";
 					}
 					try {
 						indentSecBeans.add(secBean);
 						moduleService.bringOneLevelUp(mdbean.getModule(),
 								indentSecBeans);
 					} catch (MeleteException me) {
 						logger.debug(me.toString());
 						String msg = bundle.getString("indent_left_fail");
 						addMessage(ctx, "Error Message", msg,
 								FacesMessage.SEVERITY_ERROR);
 					}
 				}
 			} else {
 				// Multiple indent code
 				boolean res = checkDifModules(selectedSecIds);
 				if (res == true) {
 					String msg = bundle.getString("list_select_in_onemodule");
 					addMessage(ctx, "Select Indent", msg,
 							FacesMessage.SEVERITY_ERROR);
 					resetSubSectionValues();
 					return "list_auth_modules";
 				} else {
 					mdbean = null;
 					secBean = null;
 
 					for (ListIterator i = selectedSecIds.listIterator(); i
 							.hasNext();) {
 						Integer sectionId = (Integer) i.next();
 						smObj = (SecModObj) secObjMap.get(sectionId);
 						if ((smObj != null) && (smObj.getMdBean() != null)
 								&& (smObj.getSecBean() != null)) {
 							mdbean = smObj.getMdBean();
 							secBean = smObj.getSecBean();
 							indentSecBeans.add(secBean);
 						}
 					}
 					if ((indentSecBeans != null)
 							&& (indentSecBeans.size() != 0)) {
 						try {
 							moduleService.bringOneLevelUp(mdbean.getModule(),
 									indentSecBeans);
 						} catch (MeleteException me) {
 							logger.debug(me.toString());
 							String msg = bundle.getString("indent_left_fail");
 							addMessage(ctx, "Error Message", msg,
 									FacesMessage.SEVERITY_ERROR);
 						}
 					}
 
 				}
 			}
 		}
 
 		resetSubSectionValues();
 		int saveShowId = showModuleId;
 		resetValues();
 		setShowModuleId(saveShowId);
 		return "list_auth_modules";
 	}
 
 	/** Check if the current section is at the top level
 	 * @param dispSeq Display sequence of section being checked
 	 * @return true if its a top level section, false otherwise
 	 */
 	private boolean checkTopLevelSection(String dispSeq)
 	{
 		String pattern = "\\.";
 		int occurs = dispSeq.split(pattern).length - 1;
 		if (occurs == 1) return true;
 		return false;
 	}
 
 	/** Check if selected sections are in different modules
 	 * @param selectedSecIds list of SecMod objects
 	 * @return true if sections are in different modules, false otherwise
 	 */
 	private boolean checkDifModules(List selectedSecIds)
 	{
 		if ((moduleDateBeans == null)||(moduleDateBeans.size() == 0)) return false;
 		if ((secObjMap == null) || (secObjMap.size() == 0)) return false;
 		List moduleIdList = new ArrayList();
 		for (ListIterator i = selectedSecIds.listIterator(); i.hasNext();)
 		{
 			Integer sectionId = (Integer) i.next();	
 			SecModObj smObj = (SecModObj) secObjMap.get(sectionId);
 			if ((smObj != null)&&(smObj.getMdBean() != null)&&(smObj.getSecBean()!=null))
 			{
 				moduleIdList.add(smObj.getModuleId());
 			}
 		}	
 		if ((moduleIdList == null) || (moduleIdList.size() == 0)) return false;
 		Collections.sort(moduleIdList);
 		Integer firstMod = (Integer) moduleIdList.get(0);
 		Integer lastMod = (Integer) moduleIdList.get(moduleIdList.size() - 1);
 		if (!(firstMod.equals(lastMod)))
 			return true;
 		else
 			return false;
 	}
 
 	
 	
 
 	/** Make a copy of the selected module
 	 * @return list_auth_modules
 	 */
 	public String duplicateAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		if (!saveModuleDates()) return "list_auth_modules";
 		
 		try
 		{
 			resetSelectedLists();
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 			ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 			logger.debug("calling copy for " + mdbean.getModule().getTitle());
 			moduleService.copyModule((ModuleObjService) mdbean.getModule(), courseId, userId);
 		}
 		catch (MeleteException me)
 		{
 			logger.debug(me.toString());
 			String msg = bundle.getString("copy_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		resetValues();
 		return "list_auth_modules";
 	}
 	
 	/** Move sections from one module to another
 	 * @return list_auth_modules or move_section
 	 */
 	public String MoveSectionAction()
     {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader(
 				"org.etudes.tool.melete.bundle.Messages");
 		if (!saveModuleDates())
 			return "list_auth_modules";
 		if ((moduleDateBeans == null) || (moduleDateBeans.size() == 0))
 			return "list_auth_modules";
 
 		try {
 			if (count == 0 || moduleSelected) {
 				String msg = bundle.getString("select_mv_section");
 				addMessage(ctx, "Error Message", msg,
 						FacesMessage.SEVERITY_ERROR);
 				resetSubSectionValues();
 				return "list_auth_modules";
 			}
 
 			if (sectionSelected && selectedSecIds != null) {
 				if ((secObjMap == null) || (secObjMap.size() == 0)) {
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				ModuleDateBean mdbean = null;
 				SectionBean secBean = null;
 				ArrayList<SectionBean> moveSectionBeans = new ArrayList<SectionBean>(
 						0);
 
 				for (ListIterator<Integer> i = selectedSecIds.listIterator(); i
 						.hasNext();) {
 					Integer sectionId = (Integer) i.next();
 					SecModObj smObj = (SecModObj) secObjMap.get(sectionId);
 					if ((smObj != null) && (smObj.getMdBean() != null)
 							&& (smObj.getSecBean() != null)) {
 						mdbean = smObj.getMdBean();
 						secBean = smObj.getSecBean();
 						moveSectionBeans.add(secBean);
 					}
 				}
 				count = 0;
 				sectionSelected = false;
 				selectedSecIds = null;
 				if ((moveSectionBeans != null) && (moveSectionBeans.size() > 0)) {
 					ValueBinding binding = Util
 							.getBinding("#{moveSectionsPage}");
 					MoveSectionsPage mvPage = (MoveSectionsPage) binding
 							.getValue(ctx);
 					mvPage.resetValues();
 					mvPage.setSectionBeans(moveSectionBeans);
 					return "move_section";
 				}
 			}
 
 		} catch (Exception me) {
 			logger.debug(me.toString());
 			String msg = bundle.getString("copy_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 
 		return "list_auth_modules";
 	}
 	
 	/** Get module id to print
 	 * @return module id to print
 	 */
 	public Integer getPrintModuleId()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		
 		try
 		{
 			resetSelectedLists();
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 			ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 			printModuleId = mdbean.getModule().getModuleId();
 			return printModuleId;
 		}
 		catch (Exception me)
 		{
 			logger.error(me.toString());
 			String msg = bundle.getString("print_module_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		return 0;
 	}
 	
 	/**
 	 * Check if the current user has permission as author.
 	 * 
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean getIsInstructor()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 		{
 			return meleteSecurityService.allowAuthor(ToolManager.getCurrentPlacement().getContext());
 		}
 		catch (Exception e)
 		{
 			String errMsg = bundle.getString("auth_failed");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "auth_failed", errMsg));
 			logger.warn(e.toString());
 		}
 		return false;
 	}
 	
 
 /** Add message to context
 	 * @param ctx FacesContext object
 	 * @param msgName Message name
 	 * @param msgDetail Message detail
 	 * @param severity Severity of message
 	 */
 	private void addMessage(FacesContext ctx, String msgName, String msgDetail, FacesMessage.Severity severity)
 	{
 		FacesMessage msg = new FacesMessage(msgName, msgDetail);
 		msg.setSeverity(severity);
 		ctx.addMessage(null, msg);
 	}
 	
 	/**
 	 * @param meleteSecurityService
 	 *        The meleteSecurityService to set.
 	 */
 	public void setMeleteSecurityService(MeleteSecurityService meleteSecurityService)
 	{
 		this.meleteSecurityService = meleteSecurityService;
 	}
 	
 	/**
 	 * @return value of sec table(datatable in which sections are rendered)
 	 */
 	public UIData getSecTable()
 	{
 		return this.secTable;
 	}
 
 	/**
 	 * @param secTable
 	 *        section datatable to set
 	 */
 	public void setSecTable(UIData secTable)
 	{
 		this.secTable = secTable;
 	}
 
 }
