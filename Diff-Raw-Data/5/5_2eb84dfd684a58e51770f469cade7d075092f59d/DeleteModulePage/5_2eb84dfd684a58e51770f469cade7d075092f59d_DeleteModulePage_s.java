 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.ModuleDateBeanService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionBeanService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.component.app.melete.Module;
 import org.etudes.component.app.melete.SectionBean;
 import org.sakaiproject.event.cover.EventTrackingService;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 
 /**
  * Delete Module Page is the backing bean for the page delete_module.jsp. It also connects to other jsp pages like confirm_delete.jsp and list_auth_modules.jsp
  */
 
 public class DeleteModulePage implements Serializable/* ,ToolBean */
 {
 	protected ModuleDateBeanService mdbean;
 	//protected SectionObjService section;
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(DeleteModulePage.class);
 	protected ModuleService moduleService;
 	protected SectionService sectionService;
 	private boolean success;
 	private boolean moduleSelected;
 	private boolean sectionSelected;
 	private List<Module> modules = null;
 	private List<SectionBeanService> sectionBeans = null;
 	String courseId;
 	String userId;
 	private boolean sameModuleSectionSelected;
 	private String fromPage;
 
 	/**
 	 * constructor
 	 */
 	public DeleteModulePage()
 	{
 		this.mdbean = null;
 		//this.section = null;
 		this.modules = null;
 		this.sectionBeans = null;
 		sameModuleSectionSelected = false;
 		courseId = null;
 		userId = null;
 		fromPage = null;
 	}
 
 	/**
 	 * Set Module date bean
 	 * 
 	 * @param mdbean
 	 *        ModuleDateBeanService
 	 */
 	public void setMdbean(ModuleDateBeanService mdbean)
 	{
 		this.mdbean = mdbean;
 	}
 
 	/**
 	 * Get ModuleDateBean
 	 * 
 	 * @return
 	 */
 	public ModuleDateBeanService getMdbean()
 	{
 		return this.mdbean;
 	}
 
 	/**
 	 * Set Modules to delete
 	 * 
 	 * @param modules
 	 *        List of ModuleDateBeanService objects
 	 */
 	public void setModules(List<Module> modules)
 	{
 		this.modules = modules;
 
 	}
 
 	/**
 	 * Get the list of to be deleted modules
 	 * 
 	 * @return
 	 */
 	public List<Module> getModules()
 	{
 		return this.modules;
 	}
 
 	/**
 	 * 
 	 * @param section
 	 *        SectionObjService
 	 */
 	/*public void setSection(SectionObjService section)
 	{
 		this.section = section;
 	}*/
 
 	/**
 	 * Get SectionObjService
 	 * 
 	 * @return
 	 */
 	/*public SectionObjService getSection()
 	{
 		return this.section;
 	}*/
 
 	/**
 	 * Set the list of sections to be deleted
 	 * 
 	 * @param sectionBeansList
 	 *        List of SectionBean objects
 	 */
 	public void setSectionBeans(List<SectionBeanService> sectionBeansList)
 	{
 		this.sectionBeans = sectionBeansList;
 	}
 
 	/**
 	 * Get the list of to be deleted sections
 	 * 
 	 * @return
 	 */
 	public List<SectionBeanService> getSectionBeans()
 	{
 		return this.sectionBeans;
 	}
 
 	/**
 	 * Set success flag to show message
 	 * 
 	 * @param success
 	 */
 	public void setSuccess(boolean success)
 	{
 		this.success = success;
 	}
 
 	/**
 	 * Get success flag
 	 * 
 	 * @return
 	 */
 	public boolean getSuccess()
 	{
 		return success;
 	}
 
 	/**
 	 * Deleted items are modules
 	 * 
 	 * @param moduleSelected
 	 */
 	public void setModuleSelected(boolean moduleSelected)
 	{
 		this.moduleSelected = moduleSelected;
 	}
 
 	/**
 	 * check if modules are picked to be deleted
 	 * 
 	 * @return
 	 */
 	public boolean getModuleSelected()
 	{
 		return moduleSelected;
 	}
 
 	/**
 	 * Deleted items selected are sections
 	 * 
 	 * @param sectionSelected
 	 */
 	public void setSectionSelected(boolean sectionSelected)
 	{
 		this.sectionSelected = sectionSelected;
 	}
 
 	/**
 	 * check if items selected are sections
 	 * 
 	 * @return
 	 */
 	public boolean getSectionSelected()
 	{
 		return sectionSelected;
 	}
 
 	/**
 	 * Checks the deleted items selected. List can have modules and sections selected. For sections selected
 	 * if they belong to the module selected then notify user.
 	 * 
 	 * @throws Exception
 	 */
 	private void CheckSectionsSelected() throws Exception
 	{
 		HashMap<Integer, Integer> modulekeys = new HashMap<Integer, Integer>();
 		for (int i = 0; i < modules.size(); i++)
 		{
 			Module mod = (Module) modules.get(i);
 			Integer mkey = new Integer(mod.getModuleId());
 			modulekeys.put(mkey, mkey);
 		}
 
 		for (int i = 0; i < sectionBeans.size(); i++)
 		{
 			SectionBeanService secbean = (SectionBeanService) sectionBeans.get(i);
 			Integer checkModId = new Integer(secbean.getSection().getModuleId());
 			if (modulekeys.containsKey(checkModId)) throw new MeleteException("same_module_section_selected");
 		}
 	}
 
 	/**
 	 * Removes sections from the delete items list which belong to module already listed in the List.
 	 * 
 	 * @throws Exception
 	 */
 	private void removeSectionsSelectedToModule() throws Exception
 	{
 		HashMap<Integer, Integer> modulekeys = new HashMap<Integer, Integer>();
 		for (int i = 0; i < modules.size(); i++)
 		{
 			Module mod = (Module) modules.get(i);
 			Integer mkey = new Integer(mod.getModuleId());
 			modulekeys.put(mkey, mkey);
 		}
 		Iterator<SectionBeanService> iter = sectionBeans.iterator();
 		while (iter != null && iter.hasNext())
 		{
 			SectionBeanService secbean = (SectionBeanService) iter.next();
 			Integer checkModId = new Integer(secbean.getSection().getModuleId());
 			if (modulekeys.containsKey(checkModId)) iter.remove();
 		}
 	}
 
 	/**
 	 * Reset values.
 	 */
 	public void resetDeleteValues()
 	{
 		// reset delete page members
 		setMdbean(null);
 		setModuleSelected(false);
 		//setSection(null);
 		setSectionSelected(false);
 		setModules(null);
 		setSectionBeans(null);
 	}
 
 	/**
 	 * Deletes the items. Tracks module delete and section delete event. Returns navigation to the start page. 
 	 * can be list or restore page.
 	 */
 	public String deleteAction()
 	{
 		if (moduleService == null) moduleService = getModuleService();
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		// actual delete
 		try
 		{
 
 			if (getModuleSelected() == true)
 			{
 				// check if sections of selected module are selected too
 				if (sectionBeans != null) CheckSectionsSelected();
 				moduleService.deleteModules(this.modules, getCourseId(), getUserId());
 
 				if (this.modules != null)
 				{
 					Iterator<Module> it = this.modules.iterator();
 					while (it.hasNext())
 					{
 						Module obj = (Module) it.next();
 						// Track the event
 						EventTrackingService.post(EventTrackingService.newEvent("melete.module.delete", ToolManager.getCurrentPlacement()
 								.getContext(), true));
 					}
 				}
 
 			}
 			if (getSectionSelected() == true)
 			{
 				sectionService.deleteSections(this.sectionBeans, getCourseId(), getUserId());
 
 				Iterator<SectionBeanService> it = this.sectionBeans.iterator();
 				if (this.sectionBeans != null)
 				{
 					while (it.hasNext())
 					{
 						SectionBeanService obj = (SectionBeanService) it.next();
 						// Track the event
 						EventTrackingService.post(EventTrackingService.newEvent("melete.section.delete", ToolManager.getCurrentPlacement()
 								.getContext(), true));
 					}
 				}	
 			}
 		}
 		catch (MeleteException me)
 		{
 			String errMsg = bundle.getString(me.getMessage());
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, me.getMessage(), errMsg));
 			sameModuleSectionSelected = true;
 			return "delete_module";
 		}
 		catch (Exception ex)
 		{
 			String errMsg = bundle.getString("delete_module_fail");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "delete_module_fail", errMsg));
 			return "delete_module";
 		}
 		setSuccess(true);
 		String deleteMsg = bundle.getString("confirm_delete_module_msg");
 		FacesMessage msg = new FacesMessage("Delete Confirmation", deleteMsg);
 		msg.setSeverity(FacesMessage.SEVERITY_INFO);
 		context.addMessage(null, msg);
 
 		if (fromPage != null && fromPage.equals("restore"))
 		{
 			ValueBinding binding = Util.getBinding("#{manageModulesPage}");
 			ManageModulesPage managePage = (ManageModulesPage) binding.getValue(context);
 			managePage.resetValues();
 
 			// reset delete page members
 			resetDeleteValues();
 			fromPage = null;
 			// navigate back to restore page
 			return "restore_modules";
 		}
 
 		ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage listAuthPage = (ListAuthModulesPage) binding.getValue(context);
 		listAuthPage.resetValues();
 		// reset delete page members
 		resetDeleteValues();
 		return "list_auth_modules";
 	}
 
 	/**
 	 * 
 	 * Sends another confirmation message if module and sections belonging to it both are selected. Makes user aware that the whole module will be deleted.
 	 */
 	public String reConfirmedDeleteAction()
 	{
 		if (moduleService == null) moduleService = getModuleService();
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		// actual delete
 		try
 		{
 
 			if (getModuleSelected() == true)
 			{
 				// check if sections of selected module are selected too
 				removeSectionsSelectedToModule();
 				moduleService.deleteModules(this.modules, getCourseId(), getUserId());
 				sameModuleSectionSelected = false;
 			}
 			if (getSectionSelected() == true)
 			{
 				sectionService.deleteSections(this.sectionBeans, getCourseId(), getUserId());
 			}
 		}
 		catch (Exception ex)
 		{
 			String errMsg = bundle.getString("delete_module_fail");
 			context.addMessage(null, new FacesMessage(errMsg));
 			return "delete_module";
 		}
 		setSuccess(true);
 		ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage listAuthPage = (ListAuthModulesPage) binding.getValue(context);
 		listAuthPage.resetValues();
 		// reset delete page members
 		resetDeleteValues();
 		return "list_auth_modules";
 	}
 
 	/**
 	 * Navigate back to start point.
 	 * 
 	 * @return
 	 */
 	public String backToModules()
 	{
 		resetDeleteValues();
 		sameModuleSectionSelected = false;
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		if (fromPage != null && fromPage.equals("restore"))
 		{
 			ValueBinding binding = Util.getBinding("#{manageModulesPage}");
 			ManageModulesPage managePage = (ManageModulesPage) binding.getValue(context);
 			managePage.resetValues();
 			fromPage = null;
 			// navigate back to restore page
 			return "restore_modules";
 		}
 
 		ValueBinding binding = Util.getBinding("#{listModulesPage}");
 		ListModulesPage listPage = (ListModulesPage) binding.getValue(context);
 		listPage.setViewModuleBeans(null);
 
 		binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage listAuthPage = (ListAuthModulesPage) binding.getValue(context);
 		listAuthPage.resetValues();
 		return "list_auth_modules";
 	}
 
 	/**
 	 * @return naviagtion rule on click of cancel button
 	 */
 	public String cancel()
 	{
 		resetDeleteValues();
 		sameModuleSectionSelected = false;
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		// navigate back to restore page
 		if (fromPage != null && fromPage.equals("restore"))
 		{
 			ValueBinding binding = Util.getBinding("#{manageModulesPage}");
 			ManageModulesPage managePage = (ManageModulesPage) binding.getValue(context);
 			managePage.resetValues();
 			fromPage = null;
 			return "restore_modules";
 		}
 		ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage listAuthPage = (ListAuthModulesPage) binding.getValue(context);
 		listAuthPage.resetValues();
 		return "list_auth_modules";
 	}
 
 	/**
 	 * Get the site Id.
 	 * 
 	 * @return
 	 */
 	private String getCourseId()
 	{
 		if (courseId == null)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			courseId = mPage.getCurrentSiteId();
 		}
 		return courseId;
 	}
 
 	/**
 	 * Get current user Id
 	 * 
 	 * @return
 	 */
 	private String getUserId()
 	{
 		if (userId == null)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			userId = mPage.getCurrentUser().getId();
 		}
 		return userId;
 	}
 
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService()
 	{
 		return moduleService;
 	}
 
 	/**
 	 * @param ModuleService
 	 *        The ModuleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService)
 	{
 		this.moduleService = moduleService;
 	}
 
 	/**
 	 * @return Returns the SectionService.
 	 */
 	public SectionService getSectionService()
 	{
 		return sectionService;
 	}
 
 	/**
 	 * @param SectionService
 	 *        The SectionService to set.
 	 */
 	public void setSectionService(SectionService sectionService)
 	{
 		this.sectionService = sectionService;
 	}
 
 	/**
 	 * @return Returns the sameModuleSectionSelected.
 	 */
 	public boolean isSameModuleSectionSelected()
 	{
 		return sameModuleSectionSelected;
 	}
 
 	/**
 	 * Get the start point
 	 * 
 	 * @return
 	 */
 	public String getFromPage()
 	{
 		return fromPage;
 	}
 
 	/**
 	 * Sets the start point
 	 * 
 	 * @param fromPage
 	 *        The start page
 	 */
 	public void setFromPage(String fromPage)
 	{
 		this.fromPage = fromPage;
 	}
 
 	public int getModuleSize()
 	{
 		if (modules != null ) return modules.size();
 		else return 0;
 	}
 
 	public int getSectionBeansSize()
 	{
 		if (sectionBeans != null ) return sectionBeans.size();
 		else return 0;
 	}
 
 }
