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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.*;
 import org.etudes.api.app.melete.*;
 
 import javax.faces.component.*;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.io.Serializable;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.application.FacesMessage;
 import javax.faces.model.ListDataModel;
 
 import org.sakaiproject.util.ResourceLoader; //import com.sun.faces.util.Util;
 import java.sql.Timestamp;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.sakaiproject.authz.cover.AuthzGroupService;
 import org.sakaiproject.authz.api.AuthzGroup;
 import javax.faces.event.*;
 import org.sakaiproject.tool.cover.ToolManager;
 
 public class ListModulesPage implements Serializable
 {
 	private boolean autonumber;
 	private Boolean autonumberMaterial = null;
 	private int bookmarkSectionId;
 	private BookmarkService bookmarkService;
 
 	private boolean closedModulesFlag;
 	private boolean expandAllFlag;
 	private String formName;
 	private String isNull = null;
 	private ListDataModel modDataModel;
 	private UIData modTable;
 	private ModuleService moduleService;
 	private Boolean nomodsFlag;
 	private Date nullDate = null;
 
 	private List nullList = null;
 	private Section nullSection = null;
 
 	private boolean printable;
 	private Boolean printMaterial = null;
 	private Integer printModuleId = null;
 	private UIData secTable;
 	/** identifier field */
 	private int showModuleId;
 	private int showPrereqModuleId;
 	private boolean showPrerequisiteFlag;
 	private boolean trueFlag = true;
 	private String typeEditor;
 
 	private String typeLink;
 	private String typeUpload;
 
 	private List viewModuleBeans = null;
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(ListModulesPage.class);
 	/** Dependency: The Melete Security service. */
 	protected MeleteSecurityService meleteSecurityService;
 
 	// This needs to be set later using Utils.getBinding
 	String courseId;
 
 	String userId;
 
 	public ListModulesPage()
 	{
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		// context.getViewRoot().setTransient(true);
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		courseId = null;
 		userId = null;
 		nomodsFlag = null;
 		closedModulesFlag = false;
 		setShowModuleId(-1);
 		expandAllFlag = false;
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(context);
 		String expFlag = preferencePage.getUserView();
 		if (expFlag.equals("true"))
 		{
 			expandAllFlag = true;
 		}
 		else
 		{
 			expandAllFlag = false;
 		}
 		setBookmarkSectionId(-1);
 	}
 
 	/**
 	 * @return expand or collapse all modules and return list_modules_inst or list_modules_student page
 	 */
 	public String expandCollapseAction()
 	{
 		if (getExpandAllFlag() == false)
 		{
 			setExpandAllFlag(true);
 		}
 		else
 		{
 			setExpandAllFlag(false);
 			setShowModuleId(-1);
 		}
 		return listViewAction();
 	}
 
 	/**
 	 * @return bookmarked section id
 	 */
 	public int getBookmarkSectionId()
 	{
 		this.bookmarkSectionId = bookmarkService.getLastVisitSectionId(isUserAuthor(), getUserId(), getCourseId());
 		return this.bookmarkSectionId;
 	}
 
 	/**
 	 * @return Returns the BookmarkService.
 	 */
 	public BookmarkService getBookmarkService()
 	{
 		return bookmarkService;
 	}
 
 	/**
 	 * @return value of flag to expand all modules/sections
 	 */
 	public boolean getExpandAllFlag()
 	{
 		return expandAllFlag;
 	}
 
 	/**
 	 * @return null string
 	 */
 	public String getIsNull()
 	{
 		return isNull;
 	}
 
 	/**
 	 * @return modDataModel to list information on list student page
 	 */
 	public ListDataModel getModDataModel()
 	{
 		this.modDataModel = new ListDataModel(getViewModuleBeans());
 		return this.modDataModel;
 	}
 
 	/**
 	 * @return value of mod table(datatable in which modules are rendered)
 	 */
 	public UIData getModTable()
 	{
 		return this.modTable;
 	}
 
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService()
 	{
 		return moduleService;
 	}
 
 	/**
 	 * @return value of nomodsFlag after getting view module beans true if there are no modules, false otherwise
 	 */
 	public boolean getNomodsFlag()
 	{
 		if (nomodsFlag == null) getViewModuleBeans();
 		return nomodsFlag;
 	}
 
 	/**
 	 * @return null date object
 	 */
 	public Date getNullDate()
 	{
 		return nullDate;
 	}
 
 	/**
 	 * @return null list
 	 */
 	public List getNullList()
 	{
 		return nullList;
 	}
 
 	/**
 	 * @return null section object
 	 */
 	public Section getNullSection()
 	{
 		return nullSection;
 	}
 
 	/**
 	 * @return module id to print
 	 */
 	public Integer getPrintModuleId()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		try
 		{
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table;
 			if (isUserAuthor())
 			{
 				table = (UIData) root.findComponent("listmodulesform").findComponent("StudentTable");
 			}
 			else
 				table = (UIData) root.findComponent("listmodulesStudentform").findComponent("table");
 			ViewModBean vmbean = (ViewModBean) table.getRowData();
 			printModuleId = vmbean.getModuleId();
 			return printModuleId;
 		}
 		catch (Exception me)
 		{
 			logger.error(me.toString());
 		}
 		return 0;
 	}
 
 	/**
 	 * @return value of sec table(datatable in which sections are rendered)
 	 */
 	public UIData getSecTable()
 	{
 		return this.secTable;
 	}
 
 	/**
 	 * @return showModuleId module id of expanded module
 	 */
 	public int getShowModuleId()
 	{
 		return this.showModuleId;
 	}
 
 	/**
 	 * @return module id for which to show prereqs
 	 */
 	public int getShowPrereqModuleId()
 	{
 		return showPrereqModuleId;
 	}
 
 	/**
 	 * @return true boolean value
 	 */
 	public boolean getTrueFlag()
 	{
 		return trueFlag;
 	}
 
 	/**
 	 * @return typeEditor string
 	 */
 	public String getTypeEditor()
 	{
 		return "typeEditor";
 	}
 
 	/**
 	 * @return typeLink string
 	 */
 	public String getTypeLink()
 	{
 		return "typeLink";
 	}
 
 	/**
 	 * @return typeUpload string
 	 */
 	public String getTypeUpload()
 	{
 		return "typeUpload";
 	}
 
 	/**
 	 * @return viewModulesBeans from database and sets flags
 	 */
 	public List getViewModuleBeans()
 	{
 		try
 		{
 			if (nomodsFlag == null || viewModuleBeans == null)
 				viewModuleBeans = getModuleService().getViewModules(getUserId(), getCourseId(), false, true);
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 
 		// If list of modules returned is zero or if all of them are hidden
 		if ((viewModuleBeans == null) || (viewModuleBeans.size() == 0))
 		{
 			nomodsFlag = true;
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			addNoModulesMessage(ctx);
 			viewModuleBeans = new ArrayList();
 		}
 		else
 		{
 			nomodsFlag = false;
 			for (ListIterator i = viewModuleBeans.listIterator(); i.hasNext();)
 			{
 				ViewModBean vmbean = (ViewModBean) i.next();
 				if (vmbean.isVisibleFlag() == false)
 				{
 					closedModulesFlag = true;
 					break;
 				}
 			}
 		}
 		return viewModuleBeans;
 	}
 
 	/**
 	 * @return Set up what's next page and return view_whats_next page
 	 */
 	public String goWhatsNext()
 	{
 		ViewModBean vmBean = null;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 		int selModIndex = 0, modSeqNo = -1;
 
 		if (params != null)
 		{
 			String modidxStr = (String) params.get("modidx2");
 			// This condition was added to fix ME-809 bug report issue
 			if ((modidxStr != null) && (modidxStr.length() > 0) && (!(modidxStr.equals("null"))))
 			{
 				selModIndex = Integer.parseInt(modidxStr);
 			}
 			else
 			{
 				selModIndex = 0;
 			}
 			String modSeqStr = (String) params.get("modseqno");
 			if ((modSeqStr != null) && (modSeqStr.length() > 0) && (!(modSeqStr.equals("null"))))
 			{
 				modSeqNo = Integer.parseInt(modSeqStr);
 			}
 			else
 			{
 				modSeqNo = -1;
 			}
 		}
 
 		ValueBinding binding = Util.getBinding("#{viewNextStepsPage}");
 		ViewNextStepsPage vnPage = (ViewNextStepsPage) binding.getValue(ctx);
 		vnPage.setModule(null);
 		if (isUserAuthor() || isUserStudent())
 		{
 			if ((viewModuleBeans != null) && (viewModuleBeans.size() > 0))
 			{
 				vmBean = (ViewModBean) viewModuleBeans.get(selModIndex);
 			}
 		}
 		int nextSeqNo = getModuleService().getNextSeqNo(getUserId(), getCourseId(), new Integer(modSeqNo));
 		vnPage.setNextSeqNo(nextSeqNo);
 		// vnPage.setModule(getModuleService().getModule(vmBean.getModuleId()));
 		if ((vmBean.getVsBeans() == null) || (vmBean.getVsBeans().size() == 0))
 		{
 			vnPage.setPrevSecId(0);
 			vnPage.setPrevModId(vmBean.getModuleId());
 		}
 		else
 		{
 			vnPage.setPrevModId(vmBean.getModuleId());
 			ViewSecBean vsBean = (ViewSecBean) vmBean.getVsBeans().get(vmBean.getVsBeans().size() - 1);
 			vnPage.setPrevSecId(vsBean.getSectionId());
 		}
 
 		return "view_whats_next";
 
 	}
 
 	/**
 	 * @return hide prerequisite dialog and return list_modules_inst or list_modules_student page
 	 */
 	public String hidePrerequisite()
 	{
 		showPrerequisiteFlag = false;
 		setShowPrereqModuleId(-1);
 		return listViewAction();
 	}
 
 	/**
 	 * @return value of auto numbering for course
 	 */
 	public boolean isAutonumber()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		try
 		{
 			if (autonumberMaterial == null)
 			{
 				String site_id = ToolManager.getCurrentPlacement().getContext();
 				ValueBinding binding = Util.getBinding("#{authorPreferences}");
 				AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 				autonumber = preferencePage.isMaterialAutonumber(site_id);
 				autonumberMaterial = new Boolean(autonumber);
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			autonumber = false;
 		}
 		return autonumber;
 	}
 
 	/**
 	 * @return closed modules flag value
 	 */
 	public boolean isClosedModulesFlag()
 	{
 		return this.closedModulesFlag;
 	}
 
 	/**
 	 * @return value of print preference for course
 	 */
 	public boolean isPrintable()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		try
 		{
 			if (printMaterial == null)
 			{
 				String site_id = ToolManager.getCurrentPlacement().getContext();
 				ValueBinding binding = Util.getBinding("#{authorPreferences}");
 				AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 				printable = preferencePage.isMaterialPrintable(site_id);
 				printMaterial = new Boolean(printable);
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			printable = false;
 		}
 		return printable;
 	}
 
 	/**
 	 * @return value to show prerequisites flag
 	 */
 	public boolean isShowPrerequisiteFlag()
 	{
 		return showPrerequisiteFlag;
 	}
 
 	/**
 	 * Check if the current user has permission as author.
 	 * 
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean isUserAuthor()
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
 
 	/**
 	 * Check if the current user has permission as student.
 	 * 
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean isUserStudent()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 		{
 			return meleteSecurityService.allowStudent(ToolManager.getCurrentPlacement().getContext());
 		}
 		catch (Exception e)
 		{
 			String errMsg = bundle.getString("auth_failed");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "auth_failed", errMsg));
 			logger.warn(e.toString());
 		}
 		return false;
 	}
 
 	/**
 	 * @return list_modules_inst or list_modules_student
 	 */
 	public String listViewAction()
 	{
 		if (isUserAuthor()) return "list_modules_inst";
		else return "list_modules_student";
 	}
 
 	/**
 	 * @return view_module
 	 */
 	public String redirectToViewModule()
 	{
 		String retVal = "view_module";
 		return retVal;
 	}
 
 	/**
 	 * @return view_section
 	 */
 	public String redirectToViewSection()
 	{
 		return "view_section";
 	}
 
 	/**
 	 * @return view_section
 	 */
 	public String redirectToViewSectionLink()
 	{
 		return "view_section";
 	}
 
 	/**
 	 * Reset all flags(expand,collapse,auto numbering) and set all indexes to -1(bookmarking,prereq etc)
 	 * 
 	 */
 	public void resetValues()
 	{
 		nomodsFlag = null;
 		closedModulesFlag = false;
 		printMaterial = null;
 		autonumberMaterial = null;
 		FacesContext context = FacesContext.getCurrentInstance();
 		// context.getViewRoot().setTransient(true);
 		expandAllFlag = false;
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(context);
 		String expFlag = preferencePage.getUserView();
 		if (expFlag.equals("true"))
 		{
 			expandAllFlag = true;
 		}
 		else
 		{
 			expandAllFlag = false;
 		}
 
 		setShowModuleId(-1);
 		setBookmarkSectionId(-1);
 		setShowPrereqModuleId(-1);
 		showPrerequisiteFlag = false;
 	}
 
 	/**
 	 * @param autonumberMaterial
 	 *        the autonumberMaterial to set
 	 */
 	public void setAutonumberMaterial(Boolean autonumberMaterial)
 	{
 		this.autonumberMaterial = autonumberMaterial;
 	}
 
 	/**
 	 * @param bookmarkSectionId
 	 *        bookmarked section id
 	 */
 	public void setBookmarkSectionId(int bookmarkSectionId)
 	{
 		this.bookmarkSectionId = bookmarkSectionId;
 	}
 
 	/**
 	 * @param bookmarkService
 	 *        The bookmarkService to set.
 	 */
 	public void setBookmarkService(BookmarkService bookmarkService)
 	{
 		this.bookmarkService = bookmarkService;
 	}
 
 	/**
 	 * @param closedModulesFlag
 	 *        value of closed modules flag
 	 */
 	public void setClosedModulesFlag(boolean closedModulesFlag)
 	{
 		this.closedModulesFlag = closedModulesFlag;
 	}
 
 	/**
 	 * @param expandAllFlag
 	 *        value of flag to expand all modules/sections
 	 */
 	public void setExpandAllFlag(boolean expandAllFlag)
 	{
 		this.expandAllFlag = expandAllFlag;
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
 	 * @param modDataModel
 	 *        ListDataModel to set
 	 */
 	public void setModDataModel(ListDataModel modDataModel)
 	{
 		this.modDataModel = modDataModel;
 	}
 
 	/**
 	 * @param modTable
 	 *        module datatable to set
 	 */
 	public void setModTable(UIData modTable)
 	{
 		this.modTable = modTable;
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
 	 * @param nomodsFlag
 	 *        value of nomodsFlag
 	 */
 	public void setNomodsFlag(boolean nomodsFlag)
 	{
 		this.nomodsFlag = nomodsFlag;
 	}
 
 	/**
 	 * @param nullList
 	 *        null list value
 	 */
 	public void setNullList(List nullList)
 	{
 		this.nullList = nullList;
 	}
 
 	/**
 	 * @param nullSection
 	 *        value of section object
 	 */
 	public void setNullSection(Section nullSection)
 	{
 		this.nullSection = nullSection;
 	}
 
 	/**
 	 * @param secTable
 	 *        section datatable to set
 	 */
 	public void setSecTable(UIData secTable)
 	{
 		this.secTable = secTable;
 	}
 
 	/**
 	 * @param moduleId
 	 *        module id of expanded module
 	 */
 	public void setShowModuleId(int moduleId)
 	{
 		this.showModuleId = moduleId;
 	}
 
 	/**
 	 * @param showPrereqModuleId
 	 *        module id for prereqs
 	 */
 	public void setShowPrereqModuleId(int showPrereqModuleId)
 	{
 		this.showPrereqModuleId = showPrereqModuleId;
 	}
 
 	/**
 	 * @param showPrerequisiteFlag
 	 *        the show prerequisites flag to set
 	 */
 	public void setShowPrerequisiteFlag(boolean showPrerequisiteFlag)
 	{
 		this.showPrerequisiteFlag = showPrerequisiteFlag;
 	}
 
 	/**
 	 * @param trueFlag
 	 *        set true flag
 	 */
 	public void setTrueFlag(boolean trueFlag)
 	{
 		this.trueFlag = trueFlag;
 	}
 
 	/**
 	 * @param typeEditor
 	 *        value of typeEditor string
 	 */
 	public void setTypeEditor(String typeEditor)
 	{
 		this.typeEditor = typeEditor;
 	}
 
 	/**
 	 * @param typeLink
 	 *        value of typeLink string
 	 */
 	public void setTypeLink(String typeLink)
 	{
 		this.typeLink = typeLink;
 	}
 
 	/**
 	 * @param typeUpload
 	 *        value of typeUpload string
 	 */
 	public void setTypeUpload(String typeUpload)
 	{
 		this.typeUpload = typeUpload;
 	}
 
 	/**
 	 * @param viewModuleBeansList
 	 *        view modules beans
 	 */
 	public void setViewModuleBeans(List viewModuleBeansList)
 	{
 		viewModuleBeans = viewModuleBeansList;
 	}
 
 	/**
 	 * @return show prerequisite dialog and return list_modules_inst or list_modules_student page
 	 */
 	public String showHidePrerequisite()
 	{
 		ViewModBean vmbean = null;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = null;
 		if (isUserAuthor())
 		{
 			table = (UIData) root.findComponent("listmodulesform").findComponent("StudentTable");
 		}
		else
 		{
 			table = (UIData) root.findComponent("listmodulesStudentform").findComponent("table");
 		}
 		vmbean = (ViewModBean) table.getRowData();
 		if (getShowPrereqModuleId() != vmbean.getModuleId())
 		{
 			setShowPrereqModuleId(vmbean.getModuleId());
 			showPrerequisiteFlag = true;
 		}
 
 		return listViewAction();
 	}
 
 	/**
 	 * @return expands individual sections and returns list_modules_inst or list_modules_student page
 	 */
 	public String showHideSections()
 	{
 		if (getExpandAllFlag() == true)
 		{
 			setShowModuleId(-1);
 			setExpandAllFlag(false);
 		}
 		else
 		{
 			ViewModBean vmbean = null;
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table = null;
 			if (isUserAuthor())
 			{
 				table = (UIData) root.findComponent("listmodulesform").findComponent("StudentTable");
 			}
			else
 			{
 				table = (UIData) root.findComponent("listmodulesStudentform").findComponent("table");
 			}
 			vmbean = (ViewModBean) table.getRowData();
 			if (getShowModuleId() != vmbean.getModuleId())
 			{
 				setShowModuleId(vmbean.getModuleId());
 			}
 			else
 			{
 				setShowModuleId(-1);
 				setExpandAllFlag(false);
 			}
 		}
 		return listViewAction();
 	}
 
 	/**
 	 * @param evt
 	 *        action event on clicking a module link
 	 */
 	public void viewModule(ActionEvent evt)
 	{
 		ViewModBean vmbean = null;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 		int selModIndex;
 		if (params != null && params.containsKey("modidx"))
 		{
 			String modidxStr = (String) params.get("modidx");
 			// This condition was added to fix ME-809 bug report issue
 			if ((modidxStr != null) && (modidxStr.length() > 0) && (!(modidxStr.equals("null"))))
 			{
 				selModIndex = Integer.parseInt(modidxStr);
 			}
 			else
 			{
 				selModIndex = 0;
 			}
 		}
 		else
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String Errmsg = bundle.getString("error_view_module");
 			FacesMessage msg = new FacesMessage(Errmsg);
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			ctx.addMessage(null, msg);
 			return;
 		}
 		ValueBinding binding = Util.getBinding("#{viewModulesPage}");
 		ViewModulesPage vmPage = (ViewModulesPage) binding.getValue(ctx);
 		vmPage.setPrintable(null);
 		if (isUserAuthor() || isUserStudent())
 		{
 			if ((viewModuleBeans != null) && (viewModuleBeans.size() > 0))
 			{
 				vmbean = (ViewModBean) viewModuleBeans.get(selModIndex);
 				vmPage.setModuleId(vmbean.getModuleId());
 				vmPage.setMdbean(null);
 				vmPage.setPrevMdbean(null);
 				vmPage.setModuleSeqNo(vmbean.getSeqNo());
 				vmPage.setAutonumber(null);
 			}
 		}
 
 	}
 
 	/**
 	 * @param evt
 	 *        action event on clicking a section link
 	 */
 	public void viewSection(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 		int selModIndex, selSecIndex;
 		if (params != null && params.containsKey("modidx") && params.containsKey("secidx"))
 		{
 			String modidxStr = (String) params.get("modidx");
 			// This condition was added to fix ME-809 bug report issue
 			if ((modidxStr != null) && (modidxStr.length() > 0) && (!(modidxStr.equals("null"))))
 			{
 				selModIndex = Integer.parseInt(modidxStr);
 			}
 			else
 			{
 				selModIndex = 0;
 			}
 			String secidxStr = (String) params.get("secidx");
 			// This condition was added to fix ME-809 bug report issue
 			if ((secidxStr != null) && (secidxStr.length() > 0) && (!(secidxStr.equals("null"))))
 			{
 				selSecIndex = Integer.parseInt(secidxStr);
 			}
 			else
 			{
 				selSecIndex = 0;
 			}
 
 		}
 		else
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String Errmsg = bundle.getString("error_view_section");
 			FacesMessage msg = new FacesMessage(Errmsg);
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			ctx.addMessage(null, msg);
 			return;
 		}
 		ModuleObjService mod = null;
 		ViewSecBean vsBean = null;
 		ViewModBean vmBean = null;
 		int modSeqNo = 0;
 
 		if (isUserAuthor() || isUserStudent())
 		{
 			if ((viewModuleBeans != null) && (viewModuleBeans.size() > 0))
 			{
 				vmBean = (ViewModBean) viewModuleBeans.get(selModIndex);
 				// Fix for ArrayIndexOutofBoundsException: -1
 				if ((selSecIndex < 0) || (selSecIndex >= vmBean.getVsBeans().size())) selSecIndex = 0;
 				vsBean = (ViewSecBean) vmBean.getVsBeans().get(selSecIndex);
 				modSeqNo = vmBean.getSeqNo();
 			}
 		}
 
 		ValueBinding binding = Util.getBinding("#{viewSectionsPage}");
 		ViewSectionsPage vsPage = (ViewSectionsPage) binding.getValue(ctx);
 		vsPage.resetValues();
 		vsPage.setSection(null);
 		vsPage.setModule(null);
 		if (vsBean != null)
 		{
 			// Section sec = vsBean.getSection();
 			vsPage.setModuleId(vmBean.getModuleId());
 			vsPage.setSectionId(vsBean.getSectionId());
 			// vsPage.setSection(sec);
 		}
 
 		vsPage.setModuleSeqNo(modSeqNo);
 	}
 
 	/**
 	 * @param ctx
 	 *        Add no modules message to context
 	 */
 	private void addNoModulesMessage(FacesContext ctx)
 	{
 		FacesMessage msg = new FacesMessage("No modules", "No modules are available for the course at this time.");
 		ctx.addMessage(null, msg);
 	}
 
 	/**
 	 * @return value of course id
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
 	 * @return value of user id
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
 }
