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
  **********************************************************************************/
 package org.sakaiproject.tool.melete;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.api.app.melete.MeleteAuthorPrefService;
 import org.sakaiproject.component.app.melete.MeleteUserPreference;
 import org.sakaiproject.component.app.melete.MeleteSitePreference;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 
 public class AuthorPreferencePage {
   final static String SFERYX = "Sferyx Editor";
   final static String FCKEDITOR = "FCK Editor";
   private String editorChoice;
   private String userEditor;
   private String userView="true";
   private String showLTI = "false";
   private ArrayList availableEditors;
   private boolean displaySferyx=false;
 
   //rendering flags
   private boolean shouldRenderSferyx=false;
   private boolean shouldRenderFCK=false;
   private boolean shouldRenderEditorPanel = false;
   private MeleteAuthorPrefService authorPref;
   private MeleteUserPreference mup;
   private MeleteSitePreference msp;
   private String materialPrintable="false";
   private String materialAutonumber="false";
 
   /** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(AuthorPreferencePage.class);
 
   public AuthorPreferencePage()
   {
   }
 
   private void getUserChoice()
   {
 		FacesContext context = FacesContext.getCurrentInstance();
   		Map sessionMap = context.getExternalContext().getSessionMap();
 
   		mup = (MeleteUserPreference) getAuthorPref().getUserChoice((String)sessionMap.get("userId"));
   		msp = (MeleteSitePreference) getAuthorPref().getSiteChoice((String)sessionMap.get("courseId"));
 
   		// if no choice is set then read default from sakai.properties
   		if ((mup == null)||(mup.getEditorChoice() == null))
   		{
   			editorChoice = getMeleteDefaultEditor();
   		}
   		else
   		{
   			editorChoice = mup.getEditorChoice();
   		}
   		if(editorChoice.equals(SFERYX))
   		{
   			shouldRenderSferyx = true;
   		  	shouldRenderFCK = false;
   		}
   		else if(editorChoice.equals(FCKEDITOR))
   		{
   			shouldRenderSferyx = false;
   		  	shouldRenderFCK = true;
   		 }
 
   		if (mup != null && (mup.isViewExpChoice() == null || !mup.isViewExpChoice().booleanValue()))
   			userView = "false";
   		else
   			userView = "true";
   		
  		if (mup != null && (mup.isShowLTIChoice() == null || !mup.isShowLTIChoice().booleanValue()))
  			showLTI = "false";
  		else
   			showLTI = "true";
   		  		
   		if(msp != null && msp.isPrintable())
   			materialPrintable = "true";
 
   		if(msp != null && msp.isAutonumber())
   			materialAutonumber = "true";
   	return;
   	}
 
 
 
   public String getMeleteDefaultEditor()
 	{
 		String defaultEditor = ServerConfigurationService.getString("melete.wysiwyg.editor", "");
 		if (logger.isDebugEnabled()) logger.debug("getMeleteDefaultEditor:"+defaultEditor+"...");
 
 		if(defaultEditor == null || defaultEditor.length() == 0)
 		{
 			defaultEditor = ServerConfigurationService.getString("wysiwyg.editor", "");
 			if (logger.isDebugEnabled()) logger.debug("default editor ie from wysiwyg.editor is" + defaultEditor);
 			if(defaultEditor.equalsIgnoreCase(FCKEDITOR) || defaultEditor.startsWith("FCK") || defaultEditor.startsWith("fck"))
 				defaultEditor = FCKEDITOR;
 		}
 
 		return defaultEditor;
 	}
 
   public ArrayList getAvailableEditors()
   {
   	if( availableEditors == null)
   	{
   		availableEditors = new ArrayList();
   		int count = ServerConfigurationService.getInt("melete.wysiwyg.editor.count", 0);
   		FacesContext context = FacesContext.getCurrentInstance();
         ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
   		for(int i=1;i <=count; i++)
   			{
   			String label = ServerConfigurationService.getString("melete.wysiwyg.editor"+i, "");
   			String displayLabel ="";
   			if(label.equalsIgnoreCase(FCKEDITOR)) displayLabel = bundle.getString("FCKEDITOR");
   			if(label.equalsIgnoreCase(SFERYX)) displayLabel = bundle.getString("SFERYX");
   			availableEditors.add(new SelectItem(label, displayLabel));
   			}
   	}
   	return availableEditors;
   }
 
   public boolean isShouldRenderEditorPanel()
   {
 	  int count = ServerConfigurationService.getInt("melete.wysiwyg.editor.count", 0);
 	  if (count > 1) shouldRenderEditorPanel = true;
 	  return shouldRenderEditorPanel;
   }
 /**
  * @return Returns the shouldRenderFCK.
  */
 public boolean isShouldRenderFCK() {
 	getUserChoice();
 	return shouldRenderFCK;
 }
 /**
  * @param shouldRenderFCK The shouldRenderFCK to set.
  */
 public void setShouldRenderFCK(boolean shouldRenderFCK) {
 		this.shouldRenderFCK = shouldRenderFCK;
 }
 /**
  * @return Returns the shouldRenderSferyx.
  */
 public boolean isShouldRenderSferyx() {
 	getUserChoice();
 	return shouldRenderSferyx;
 }
 /**
  * @param shouldRenderSferyx The shouldRenderSferyx to set.
  */
 public void setShouldRenderSferyx(boolean shouldRenderSferyx) {
 	this.shouldRenderSferyx = shouldRenderSferyx;
 }
 
 
 // editor settings
 public String goToEditorPreference()
 {
 	return "pref_editor";
 }
 
 /**
  * Default editor is Sferyx
  * @return Returns the userEditor.
  */
 public String getUserEditor() {
 	getEditorChoice();
 	if (editorChoice == null)
 		userEditor = SFERYX;
 	else userEditor = editorChoice;
 	return userEditor;
 }
 
 public String getEditorChoice()
 {
 	getUserChoice();
 	return editorChoice;
 }
 /**
  * @param userEditor The userEditor to set.
  */
 public void setUserEditor(String userEditor) {
 	this.userEditor = userEditor;
 }
 public String getUserView() {
 	getUserChoice();
 	return userView;
 }
 /**
  * @param userView The userView to set.
  */
 public void setUserView(String userView) {
 	this.userView = userView;
 }
 public String setUserChoice()
 {
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		ResourceLoader bundle = new ResourceLoader("org.sakaiproject.tool.melete.bundle.Messages");
         if (mup == null)
         {
         	mup = new MeleteUserPreference();
         }
 		try
 		{
 			if (userEditor != null)
 			{
 				mup.setEditorChoice(userEditor);
 			}
 			if (userView.equals("true"))
 			{
 				mup.setViewExpChoice(true);
 			}
 			else
 			{
 				mup.setViewExpChoice(false);
 			}
 			
 			if (showLTI.equals("true"))	mup.setShowLTIChoice(true);
 			else mup.setShowLTIChoice(false);			
 
 		mup.setUserId((String)sessionMap.get("userId"));
 		authorPref.insertUserChoice(mup);
 
 		// set Site Preferences
 		if(msp == null) {
 			msp = new MeleteSitePreference();
 			msp.setPrefSiteId((String)sessionMap.get("courseId"));
 		}
 
 		//set print preference
 		if (materialPrintable.equals("true"))msp.setPrintable(true);
 		else msp.setPrintable(false);
 
 		//set autonumber preference
 		if (materialAutonumber.equals("true"))msp.setAutonumber(true);
 		else msp.setAutonumber(false);
 
 		authorPref.insertUserSiteChoice(msp);
 		}
 		catch(Exception e)
 		{
 			String errMsg = bundle.getString("Set_prefs_fail");
 			context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Set_prefs_fail",errMsg));
 			return "author_preference";
 		//	return "pref_editor";
 		}
 
 	String successMsg = bundle.getString("Set_prefs_success");
 	context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Set_prefs_success",successMsg));
 
 	return "author_preference";
 	//return "pref_editor";
 }
 
 public boolean isMaterialPrintable(String site_id)
 {
 	MeleteSitePreference checkMsp = (MeleteSitePreference)getAuthorPref().getSiteChoice(site_id);
 	if(checkMsp != null)return checkMsp.isPrintable();
 	else return false;
 }
 public boolean isMaterialAutonumber(String site_id)
 {
 	MeleteSitePreference checkMsp = (MeleteSitePreference)getAuthorPref().getSiteChoice(site_id);
 	if(checkMsp != null)return checkMsp.isAutonumber();
 	else return false;
 }
 
 public String backToPrefsPage()
 {
 	userEditor = editorChoice;
 	return "author_preference";
 //	return "pref_editor";
 }
 
 /**
  * @return Returns the authorPref.
  */
 public MeleteAuthorPrefService getAuthorPref() {
 	return authorPref;
 }
 /**
  * @param authorPref The authorPref to set.
  */
 public void setAuthorPref(MeleteAuthorPrefService authorPref) {
 	this.authorPref = authorPref;
 }
 
 
 
 /**
  * @return Returns the displaySferyx.
  */
 public boolean isDisplaySferyx() {
 	return displaySferyx;
 }
 /**
  * @param displaySferyx The displaySferyx to set.
  */
 public void setDisplaySferyx(boolean displaySferyx) {
 	this.displaySferyx = displaySferyx;
 }
 
 public MeleteUserPreference getMup() {
 		return mup;
 }
 
 public void setMup(MeleteUserPreference mup) {
 	this.mup = mup;
 }
 
 /**
  * @return the materialPrintable
  */
 public String getMaterialPrintable()
 {
 	return this.materialPrintable;
 }
 
 /**
  * @param materialPrintable the materialPrintable to set
  */
 public void setMaterialPrintable(String materialPrintable)
 {
 	this.materialPrintable = materialPrintable;
 }
 /**
  * @return the materialAutonumber
  */
 public String getMaterialAutonumber()
  {
        return this.materialAutonumber;
  }
 
  /**
   * @param materialAutonumber the materialAutonumber to set
   */
 public void setMaterialAutonumber(String materialAutonumber)
  {
        this.materialAutonumber = materialAutonumber;
  }
 
 public String getShowLTI() {
 	return showLTI;
 }
 
 public void setShowLTI(String showLTI) {
 	this.showLTI = showLTI;
 }
 
 public boolean getUserLTIChoice(String userId){
 	MeleteUserPreference checkMup = (MeleteUserPreference)getAuthorPref().getUserChoice(userId);
	if(checkMup != null) return checkMup.isShowLTIChoice();
 	else return false;
 }
 
 }
