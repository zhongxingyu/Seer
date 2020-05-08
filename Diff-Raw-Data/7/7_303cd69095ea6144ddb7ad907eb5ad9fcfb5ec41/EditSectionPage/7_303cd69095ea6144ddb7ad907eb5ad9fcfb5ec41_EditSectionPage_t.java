 /**********************************************************************************
  *
  * $Header: /usr/src/sakai/melete-2.4/melete-app/src/java/org/sakaiproject/tool/melete/EditSectionPage.java,v 1.40 2007/09/07 17:49:37 mallikat Exp $
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
 
 import java.util.*;
 import java.io.File;
 import java.io.Serializable;
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.*;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.*;
 
 import org.sakaiproject.api.app.melete.ModuleService;
 import org.sakaiproject.api.app.melete.SectionObjService;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.component.app.melete.MeleteResource;
 import org.sakaiproject.component.app.melete.Section;
 import org.sakaiproject.component.app.melete.SectionResource;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 
 /**
  * @author Rashmi Mallika - upload exceed message doesn't display Rashmi - add server listing options to dropdown Rashmi - seperate method for
  *         processing replace server view link 2 me
  */
 
 public class EditSectionPage extends SectionPage implements Serializable
 {
 	private boolean sizeWarning = false;
 
 	private String previewPage;
 
 	private ModuleService moduleService;
 
 	private String secResourceDescription1;
 
 	private boolean shouldRenderContentTypeSelect = false;
 
 	// rendering flags
 	private String currLinkUrl;
 
 	private String displayCurrLink;
 
 	// picking from server
 	private String editSelection;
 
 	private SectionResourceLicenseSelector m_selected_license;
 
 	private String selectedResourceName;
 
 	private String selectedResourceDescription;
 
 	private MeleteResource selectedResource;
 
 	private String containCollectionId;
 
 	private boolean shouldRenderServerResources = false;
 
 	private boolean shouldRenderLocalUpload = false;
 
 	public EditSectionPage()
 	{
 		setFormName("EditSectionForm");
 	}
 
 	public String setEditInfo(SectionObjService section)
 	{
 		resetSectionValues();
 		checkUploadExists();
 		setModule(section.getModule());
 		setSection(section);
 		setSecResource(section.getSectionResource());
 		if (secResource != null)
 		{
 			setMeleteResource((MeleteResource) secResource.getResource());
 			setContentResourceData(meleteResource.getResourceId());
 		}
 		else
 		{
 			setMeleteResource(null);
 			setM_license(null);
 		}
 		expandAllFlag = false;
 		setSuccess(false);
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(context);
 		String usereditor = preferencePage.getUserEditor();
 		// if fck editor then push advisors and other config values
 		if (section.getContentType().equals("typeEditor") && usereditor.equals(preferencePage.FCKEDITOR))
 		{
 			setFCKCollectionAttrib();
 		}
 		if (section.getContentType().equals("typeEditor") && usereditor.equals(preferencePage.SFERYX)) preferencePage.setDisplaySferyx(true);
 
 		return "success";
 	}
 
 	/**
 	 * @return sizeWarning render sizeWarning message if this flag is true
 	 */
 	public boolean getSizeWarning()
 	{
 		return this.sizeWarning;
 	}
 
 	/**
 	 * @param sizeWarning
 	 *        to set sizeWarning to true if section save is successful.
 	 */
 	public void setSizeWarning(boolean sizeWarning)
 	{
 		this.sizeWarning = sizeWarning;
 	}
 
 	private void setContentResourceData(String resourceId)
 	{
 		try
 		{
 			if (resourceId != null)
 			{
 				ContentResource cr = getMeleteCHService().getResource(resourceId);
 				if (cr == null) return;
 
 				if (cr.getContentType().equals(getMeleteCHService().MIME_TYPE_EDITOR))
 					this.contentEditor = new String(cr.getContent());
 				else if (cr.getContentType().equals(getMeleteCHService().MIME_TYPE_LINK)) setCurrLinkUrl(new String(cr.getContent()));
 
 				this.secResourceName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 				uploadFileName = this.secResourceName;
 				this.secResourceDescription = cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
 				containCollectionId = cr.getContainingCollection().getId();
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("error in reading resource properties in edit section");
 		}
 
 	}
 
 	/**
 	 * return content editor
 	 */
 	public String getContentEditor()
 	{
 		if (logger.isDebugEnabled()) logger.debug("EDIT GETCONTENT EDITOR CALLED");
 		return this.contentEditor;
 	}
 
 	public boolean isShouldRenderContentTypeSelect()
 	{
 		// shouldRenderContentTypeSelect = false;
 		if (shouldRenderContentTypeSelect == false && this.section != null && this.section.getContentType().equals("notype"))
 		{
 			shouldRenderContentTypeSelect = true;
 			shouldRenderUpload = false;
 			shouldRenderEditor = false;
 			shouldRenderLink = false;
 			shouldRenderNotype = true;
 		}
 		return shouldRenderContentTypeSelect;
 	}
 
 	/*
 	 * action listener for currenet site resource listings. It sets the variable
 	 */
 	public void selectedResourceReplaceAction(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UICommand cmdLink = (UICommand) evt.getComponent();
 		List cList = cmdLink.getChildren();
 		// UIParameter param = (UIParameter) cList.get(0);
 		UIComponent comp = (UIComponent) cList.get(0);
 		// Mallika - Needed to add the if condition below since param tags aren't being
 		// rendered if file size is too large
 
 		if (!(comp instanceof UIOutput))
 		{
 			UIParameter param = (UIParameter) comp;
 			selResourceIdFromList = (String) param.getValue();
 		}
 		else
 		{
 			String selclientId = cmdLink.getClientId(ctx);
 			selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 			selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 			selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 			String resId = selclientId.substring(0, selclientId.indexOf(':'));
 			int selResIndex = Integer.parseInt(resId);
 			selResIndex = selResIndex + getListNav().getCurrIndex();
 			selResourceIdFromList = ((DisplaySecResources) currSiteResourcesList.get(selResIndex)).getResource_id();
 		}
 
 		logger.info("selected resource id by user in editsectionPage is " + selResourceIdFromList);
 
 		// populate properties panel with the selected resource
 		try
 		{
 			ContentResource cr = getMeleteCHService().getResource(selResourceIdFromList);
 			if (cr != null)
 			{
 				// get resource object
 				selectedResource = (MeleteResource) sectionService.getMeleteResource(selResourceIdFromList);
 				if (selectedResource != null)
 				{
 					this.selectedResourceName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 					this.selectedResourceDescription = cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
 					renderSelectedResource = true;
 					getM_selected_license().setInitialValues(formName, sectionService, selectedResource);
 				}
 				else
 				{
 					// short term soln for missing records in melete_resource
 					logger.error("selected resource doesnot exist in meleteResource" + selResourceIdFromList);
 				}
 			}
 			ctx.renderResponse();
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 			logger.error("error while accessing content resource");
 		}
 		return;
 	}
 
 	public String saveHere()
 	{
 		checkUploadExists();
 		String dataPath = new String();
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 
 		// validation 1: modality is required.
 		if (!validateModality())
 		{
 			String errMsg = bundle.getString("add_section_modality_reqd");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "add_section_modality_reqd", errMsg));
 			return "failure";
 		}
 
 		try
 		{
 			// validation 2: if content is provided then check for copyright license
 			if (!section.getContentType().equals("notype") && getM_license().getLicenseCodes().equals(SectionResourceLicenseSelector.Copyright_CODE))
 			{
 				getM_license().checkForRequiredFields();
 			}
 
 			// validation 3: if upload a new file check fileName format -- move to uploadSectionContent()
 
 			// validation 4: check link url title
 			if (section.getContentType().equals("typeLink") && (secResourceName == null || secResourceName.trim().length() == 0))
 				throw new MeleteException("URL_title_reqd");
 
 			// save section
 			if (logger.isDebugEnabled()) logger.debug("EditSectionpage:save section");
 			String uploadHomeDir = context.getExternalContext().getInitParameter("uploadDir");
 			if (section.getContentType().equals("typeExistUpload")) section.setContentType("typeUpload");
 			if (section.getContentType().equals("typeExistLink")) section.setContentType("typeLink");
 
 			if (!section.getContentType().equals("notype"))
 			{
 				shouldRenderContentTypeSelect = false;
 				// step 1: check if new resource content or existing resource is edited
 				try
 				{
 					if (logger.isDebugEnabled()) logger.debug("Ist step of edit - check meleteResource" + meleteResource.getResourceId());
 					getMeleteCHService().checkResource(meleteResource.getResourceId());
 					editMeleteCollectionResource(uploadHomeDir, meleteResource.getResourceId());
 				}
 				catch (Exception e)
 				{
 					// resource is not there when no content type is choosen
 					if (logger.isDebugEnabled()) logger.debug("resource is new i.e. coming from notype content");
 					String addCollId = getMeleteCHService().getCollectionId(section.getContentType(), module.getModuleId());
 					String newResourceId = addResourceToMeleteCollection(uploadHomeDir, addCollId);
 					meleteResource.setResourceId(newResourceId);
 					if (logger.isDebugEnabled()) logger.debug("new resource id" + newResourceId + meleteResource);
 					/* here create association and insert new resource */
 					sectionService.insertMeleteResource(section, meleteResource);
 				}
 
 				// step 3: edit license information
 				meleteResource = getM_license().processLicenseInformation(meleteResource);
 
 				// step2: edit section properties
 				sectionService.editSection(section, meleteResource);
 			}
 			else
 			{
 				sectionService.editSection(section);
 			}
 			// uploadFileName=null;
 			String successMsg = bundle.getString("add_section_success");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "add_section_success", successMsg));
 
 			return "success";
 		}
 		catch (MeleteException mex)
 		{
 			logger.error("error in updating section " + mex.toString());
 			String errMsg = bundle.getString(mex.getMessage());
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mex.getMessage(), errMsg));
 			mex.printStackTrace();
 			return "failure";
 		}
 		catch (Exception ex)
 		{
 			logger.error("error in updating section " + ex.toString());
 			String errMsg = bundle.getString("add_section_fail");
 			logger.error("error in updating section is" + errMsg);
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "add_section_fail", errMsg));
 			ex.printStackTrace();
 			return "failure";
 		}
 
 	}
 
 	/**
 	 * instantiates saving of a section. if file needs to be uploaded, upload file. Validate content. save section. If sucess set success flag and
 	 * show the success message. if any error, display error message to the user.
 	 */
 	public String save()
 	{
 		setSuccess(false);
 		if (!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 			return "editmodulesections";
 		}
 		return "editmodulesections";
 	}
 
 	/**
 	 * save the section, if not saved yet and then refresh the page to add more sections. Revision on 11/15: - add code to initiate breadcrumps in add
 	 * section page Revised on 3/22 - to set section as null Revised on 5/31/05 - to get addsectionpage instance and call resetvalues there
 	 */
 	public String saveAndAddAnotherSection()
 	{
 		setSuccess(false);
 			if(!saveHere().equals("failure"))
 			{
 				setSuccess(true);
 			} 
 			else return "editmodulesections";
 		
 
 		// create new instance of section model
 		setSection(null);
 		resetSectionValues();
 		setSizeWarning(false);
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		sessionMap.put("currModule", module);
 
 		ValueBinding binding = Util.getBinding("#{addSectionPage}");
 		AddSectionPage aPage = (AddSectionPage) binding.getValue(context);
 		aPage.setSection(null);
 		aPage.resetSectionValues();
 		aPage.setModule(module);
 		return "addmodulesections";
 	}
 
 	/**
 	 * returns failure if the section has not been saved first. returns finish to redirect to addmodulefinish page. revised by rashmi on 3/21 to
 	 * enforce automatic save revised by rashmi 06/14/05 to remove retain values problem in finish page
 	 */
 	public String Finish()
 	{
 		setSuccess(false);
 		
 			if(!saveHere().equals("failure"))
 			{
 			setSuccess(true);
 			} else return "editmodulesections";
 		
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 		String successMsg = bundle.getString("edit_section_confirm");
 		FacesMessage msg = new FacesMessage("Info message", successMsg);
 		msg.setSeverity(FacesMessage.SEVERITY_INFO);
 		context.addMessage(null, msg);
 		return "list_auth_modules";
 
 	}
 
 	/*
 	 * Preview - force save on preview
 	 */
 	public String getPreviewPage()
 	{
 		try
 		{
 			if (!section.getContentType().equals("notype"))
 			{
 				if (this.section.getContentType().equals("typeEditor"))
 				{
 					FacesContext context = FacesContext.getCurrentInstance();
 					String uploadHomeDir = context.getExternalContext().getInitParameter("uploadDir");
 					this.previewContentData = getMeleteCHService().findLocalImagesEmbeddedInEditor(uploadHomeDir, contentEditor);
 					contentEditor = previewContentData;
 					return "editpreview";
 				}
 				else
 				{
 					this.previewContentData = getMeleteCHService().getResourceUrl(meleteResource.getResourceId());
 					return "editpreview";
 				}
 			}
 			else
 			{
 				this.previewContentData = null;
 				return "editpreview";
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 		return "#";
 
 	}
 
 	public String cancelFromPreview()
 	{
 		return "editmodulesections";
 	}
 
 	/*
 	 * reset edit section variables first and then of base class
 	 */
 	public void resetSectionValues()
 	{
 		shouldRenderContentTypeSelect = false;
 		currLinkUrl = null;
 		editSelection = null;
 		m_selected_license = null;
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		selectedResource = null;
 
 		super.resetSectionValues();
 	}
 
 	public String redirectLinktoEdit()
 	{
 		return "editmodulesections";
 		// return "#";
 	}
 
 	/*
 	 * on clicking link 2 me the page navigates back to add module section
 	 */
 	public String redirectLink()
 	{
 		return "editContentUploadServerView";
 		// return "#";
 	}
 
 	/*
 	 * on clicking expandAll resource listing shows
 	 */
 	public String expandAllResources()
 	{
 		expandAllFlag = true;
 		return "editContentUploadServerView";
 	}
 
 	/*
 	 * on clicking expandAll resource listing shows
 	 */
 	public String collapseAllResources()
 	{
 		expandAllFlag = false;
 		return "editContentUploadServerView";
 	}
 
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
 
 	public String getDisplayCurrLink()
 	{
 		if (currLinkUrl != null && currLinkUrl.length() > 50)
 			displayCurrLink = currLinkUrl.substring(0, 50) + "...";
 		else
 			displayCurrLink = currLinkUrl;
 
 		return displayCurrLink;
 	}
 
 	/**
 	 * @return Returns the currLinkUrl.
 	 */
 	public String getCurrLinkUrl()
 	{
 		if (!(getLinkUrl().equals("http://") || getLinkUrl().equals("https://"))) currLinkUrl = getLinkUrl();
 		return currLinkUrl;
 	}
 
 	/**
 	 * @param currLinkUrl
 	 *        The currLinkUrl to set.
 	 */
 	public void setCurrLinkUrl(String currLinkUrl)
 	{
 		this.currLinkUrl = currLinkUrl;
 	}
 
 	/**
 	 * @return Returns the editSelection.
 	 */
 	public String getEditSelection()
 	{
 		// if(editSelection == null) editSelection = "none";
 		return editSelection;
 	}
 
 	/**
 	 * @param editSelection
 	 *        The editSelection to set.
 	 */
 	public void setEditSelection(String editSelection)
 	{
 		this.editSelection = editSelection;
 	}
 
 	/**
 	 * @return Returns the m_selected_license.
 	 */
 	public SectionResourceLicenseSelector getM_selected_license()
 	{
 		if (m_selected_license == null)
 		{
 			m_selected_license = new SectionResourceLicenseSelector();
 			m_selected_license.setInitialValues(this.formName, sectionService, getSelectedResource());
 		}
 		return m_selected_license;
 	}
 
 	/**
 	 * @param m_selected_license
 	 *        The m_selected_license to set.
 	 */
 	public void setM_selected_license(SectionResourceLicenseSelector m_selected_license)
 	{
 		this.m_selected_license = m_selected_license;
 	}
 
 	/**
 	 * @return Returns the selectedResource.
 	 */
 	public MeleteResource getSelectedResource()
 	{
 		if (selectedResource == null) selectedResource = new MeleteResource();
 		return selectedResource;
 	}
 
 	/**
 	 * @param selectedResource
 	 *        The selectedResource to set.
 	 */
 	public void setSelectedResource(MeleteResource selectedResource)
 	{
 		this.selectedResource = selectedResource;
 	}
 
 	/**
 	 * @return Returns the selectedResourceDescription.
 	 */
 	public String getSelectedResourceDescription()
 	{
 		return selectedResourceDescription;
 	}
 
 	/**
 	 * @param selectedResourceDescription
 	 *        The selectedResourceDescription to set.
 	 */
 	public void setSelectedResourceDescription(String selectedResourceDescription)
 	{
 		this.selectedResourceDescription = selectedResourceDescription;
 	}
 
 	/**
 	 * @return Returns the selectedResourceName.
 	 */
 	public String getSelectedResourceName()
 	{
 		return selectedResourceName;
 	}
 
 	/**
 	 * @param selectedResourceName
 	 *        The selectedResourceName to set.
 	 */
 	public void setSelectedResourceName(String selectedResourceName)
 	{
 		this.selectedResourceName = selectedResourceName;
 	}
 
 	public String gotoServerView()
 	{
 		expandAllFlag = true;
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		renderSelectedResource = false;
 		selResourceIdFromList = null;
 		shouldRenderServerResources = false;
 		shouldRenderLocalUpload = false;
 		currSiteResourcesList = null;
 		getCurrSiteResourcesList();
 		return "editContentUploadServerView";
 	}
 
 	public String setServerFile()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 		if (logger.isDebugEnabled()) logger.debug("selected resource properties" + selectedResourceName + " , " + selectedResourceDescription);
 		try
 		{
 			if (selResourceIdFromList == null)
 			{
 				String res_mime_type = uploadSectionContent("file1");
 				if (logger.isDebugEnabled()) logger.debug("new names for upload content is" + res_mime_type);
 				// if new resource i.e. a local file is choosen is added
 				if (res_mime_type != null)
 				{
 					secResourceDescription = "";
 					setM_license(null);
 					ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false, secResourceName, secResourceDescription);
 
 					if (containCollectionId == null) containCollectionId = getMeleteCHService().getUploadCollectionId();
 
 					String newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type, containCollectionId,
 							getSecContentData(), res);
 					selectedResource = new MeleteResource();
 					selectedResource.setResourceId(newResourceId);
 					sectionService.insertResource(selectedResource);
 					String rUrl = getMeleteCHService().getResourceUrl(newResourceId);
 					String checkDup = rUrl.substring(rUrl.lastIndexOf("/") + 1);
 					if (!checkDup.equals(secResourceName)) secResourceName = checkDup;
 				}
 				else
 				{
 					String errMsg = bundle.getString("select_or_cancel");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "select_or_cancel", errMsg));
 					return "editContentUploadServerView";
 				}
 			}
 			else
 			{
 				logger.debug("selected existing file");
 				secResourceName = selectedResourceName;
 				secResourceDescription = selectedResourceDescription;
 				logger.debug("assigned selected disp name and properties properly to current");
 				setM_license(m_selected_license);
 			}
 			meleteResource = selectedResource;
 			ctx.renderResponse();
 		}
 		catch (MeleteException me)
 		{
 			logger.error("error in set server file for edit section content");
 			me.printStackTrace();
 			String errMsg = bundle.getString(me.getMessage());
 			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", errMsg));
 			return "editContentUploadServerView";
 		}
 		catch (Exception e)
 		{
 			logger.error("error in set server file for edit section content");
 			e.printStackTrace();
 			String errMsg = bundle.getString("add_section_fail");
 			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", errMsg));
 			return "editContentUploadServerView";
 		}
 		return "editmodulesections";
 	}
 
 	// edit link
 	public String gotoServerLinkView()
 	{
 		expandAllFlag = true;
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		renderSelectedResource = false;
 		selResourceIdFromList = null;
 		shouldRenderServerResources = false;
 		setLinkUrl(null);
 		currSiteResourcesList = null;
 		logger.debug("setting currsiteResourceList to null");
 		getCurrSiteResourcesList();
 		return "editContentLinkServerView";
 	}
 
 	public String setServerUrl()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
 		String errMsg = null;
 		if (logger.isDebugEnabled()) logger.debug("selected resource properties" + selectedResourceName + " , " + selectedResourceDescription);
 		try
 		{
 			// new link provided
 			if (selResourceIdFromList == null)
 			{
 				if (getLinkUrl().equals("http://") || getLinkUrl().equals("https://"))
 				{
 					errMsg = bundle.getString("select_or_cancel");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "select_or_cancel", errMsg));
 					return "editContentLinkServerView";
 				}
 				String check = validateLink();
 				/*
 				 * if(!check.equals("OK")) { errMsg = bundle.getString("add_section_bad_url"); ctx.addMessage (null, new
 				 * FacesMessage(FacesMessage.SEVERITY_ERROR,"add_section_bad_url",errMsg)); return "editContentLinkServerView"; }
 				 */
 				secResourceName = "";
 				secResourceDescription = "";
 				setM_license(null);
 				createLinkUrl();
 				String res_mime_type = getMeleteCHService().MIME_TYPE_LINK;
 				ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false, secResourceName, secResourceDescription);
 				String newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type, containCollectionId, getSecContentData(),
 						res);
 				selectedResource = new MeleteResource();
 				selectedResource.setResourceId(newResourceId);
 				sectionService.insertResource(selectedResource);
 				currLinkUrl = secResourceName;
 			}
 			else
 			{
 				// pick from server list
 				secResourceName = selectedResourceName;
 				secResourceDescription = selectedResourceDescription;
 				setM_license(m_selected_license);
 				currLinkUrl = new String(getMeleteCHService().getResource(selResourceIdFromList).getContent());
 			}
 			meleteResource = selectedResource;
 			ctx.renderResponse();
 		}
 		catch (Exception e)
 		{
 			logger.error("error in set server url for edit section content" + errMsg);
 			e.printStackTrace();
			if (e.getMessage() != null)
			{	
			  errMsg = bundle.getString(e.getMessage());
			  ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), errMsg));
			}  
 			return "editContentLinkServerView";
 		}
 		return "editmodulesections";
 	}
 
 	public String cancelServerFile()
 	{
 		selectedResource = null;
 		selResourceIdFromList = null;
 		renderSelectedResource = false;
 		setLinkUrl(currLinkUrl);
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		return "editmodulesections";
 	}
 
 	/**
 	 * @return Returns the shouldRenderServerResources.
 	 */
 	public boolean isShouldRenderServerResources()
 	{
 		return shouldRenderServerResources;
 	}
 
 	/**
 	 * @param shouldRenderServerResources
 	 *        The shouldRenderServerResources to set.
 	 */
 	public void setShouldRenderServerResources(boolean shouldRenderServerResources)
 	{
 		this.shouldRenderServerResources = shouldRenderServerResources;
 	}
 
 	/**
 	 * @return Returns the shouldRenderLocalUpload.
 	 */
 	public boolean isShouldRenderLocalUpload()
 	{
 		return shouldRenderLocalUpload;
 	}
 
 	/**
 	 * @param shouldRenderLocalUpload
 	 *        The shouldRenderLocalUpload to set.
 	 */
 	public void setShouldRenderLocalUpload(boolean shouldRenderLocalUpload)
 	{
 		this.shouldRenderLocalUpload = shouldRenderLocalUpload;
 	}
 }
