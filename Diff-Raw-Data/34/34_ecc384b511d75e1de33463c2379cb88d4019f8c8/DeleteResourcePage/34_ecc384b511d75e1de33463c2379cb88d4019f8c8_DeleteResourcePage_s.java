 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
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
 
 import org.sakaiproject.util.ResourceLoader;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.sakaiproject.util.ResourceLoader;
 
 
 public class DeleteResourcePage implements Serializable{
 
 	 /** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(DeleteResourcePage.class);
 	private SectionService sectionService;
 	private MeleteCHService meleteCHService;
 
 	private String fromPage;
 	private boolean warningFlag;
 	private String delResourceName;
 	private String delResourceId;
 	private String courseId;
 
     public DeleteResourcePage(){
     }
 
   	public void resetValues()
   	{
   		warningFlag = false;
   		fromPage = "";
   		delResourceId = null;
   		delResourceName = null;
   		courseId = null;
   	}
 
   	public void setFromPage(String fromPage)
   	{
   		this.fromPage = fromPage;
   	}
 
   	public void processDeletion(String delResourceId, String courseId)
   	{
   		this.delResourceId = delResourceId;
   		this.courseId = courseId;
   		List res_in_use = sectionService.findResourceInUse(delResourceId,courseId);
 		if(res_in_use != null)	logger.debug("res_in_use size " + res_in_use.size());
 		if (res_in_use != null && res_in_use.size() > 0)
 			warningFlag = true;
   	}
 
   	public void setResourceName(String title)
   	{
   		delResourceName = title;
   	}
 
   	public String deleteResource()
   	{
   		FacesContext context = FacesContext.getCurrentInstance();
   		try
   		{
   			if(delResourceId != null)
   			{
   				// delete from content resource
  				sectionService.deleteResourceInUse(this.delResourceId, this.courseId);
   				meleteCHService.removeResource(this.delResourceId);
   				logger.debug("delete resource is done now move back to page");
   			}
   			if (fromPage.startsWith("edit"))
   			{
   				ValueBinding binding = Util.getBinding("#{editSectionPage}");
   				EditSectionPage editPage = (EditSectionPage) binding.getValue(context);
   				if(editPage.meleteResource.getResourceId().equals(delResourceId))
   				{
   					logger.debug("remove resource from cache" );
   					editPage.secResource.setResource(null);
   					editPage.meleteResource = null;
   					editPage.resetMeleteResourceValues();
   				}
   				editPage.refreshCurrSiteResourcesList();
   			}
   			else if(fromPage.startsWith("manage"))
   			{
   				ValueBinding binding = Util.getBinding("#{manageResourcesPage}");
   				ManageResourcesPage managePage = (ManageResourcesPage) binding.getValue(context);
   				managePage.refreshCurrSiteResourcesList();
   			}
   			else
   			{
   				ValueBinding binding = Util.getBinding("#{addSectionPage}");
   				AddSectionPage addPage = (AddSectionPage) binding.getValue(context);
   				if(addPage.meleteResource == null || addPage.meleteResource.getResourceId() == null
 					|| delResourceId.equals(addPage.meleteResource.getResourceId()))
   				{
   					addPage.resetMeleteResourceValues();
   					addPage.meleteResource = null;
   				}
   				addPage.refreshCurrSiteResourcesList();
   			}
   			return fromPage;
   		}
 		catch (Exception e)
 		{
 			//e.printStackTrace();
 			logger.debug("error in delete resource" + e.toString());
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String errMsg = bundle.getString("delete_resource_fail");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), errMsg));
 			return "#";
 		}
 	}
 
   	public String cancelDeleteResource()
   	{
   		return fromPage;
   	}
 
 	/**
 	 * @return Returns the SectionService.
 	 */
 	public SectionService getSectionService() {
 		return sectionService;
 	}
 	/**
 	 * @param SectionService The SectionService to set.
 	 */
 	public void setSectionService(SectionService sectionService) {
 		this.sectionService = sectionService;
 	}
 	/**
 	 * @param meleteCHService the meleteCHService to set
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService)
 	{
 		this.meleteCHService = meleteCHService;
 	}
 
 
 
 	/**
 	 * @return the warningFlag
 	 */
 	public boolean isWarningFlag()
 	{
 		return this.warningFlag;
 	}
 
 	/**
 	 * @param warningFlag the warningFlag to set
 	 */
 	public void setWarningFlag(boolean warningFlag)
 	{
 		this.warningFlag = warningFlag;
 	}
 
 	/**
 	 * @return the delResourceName
 	 */
 	public String getDelResourceName()
 	{
 		return this.delResourceName;
 	}
 
  }
