 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 
 package com.flexive.faces.beans;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.components.content.FxWrappedContent;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.ReferencedContent;
 import com.flexive.shared.content.*;
 import com.flexive.shared.interfaces.ContentEngine;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Backing bean for the content editor component.
  * The bean probvides a HashMap containing all contents
  * that are currently stored in the view-state of the client. The key
  * for a specific content is the editor id which is provided
  * by the content editor component.
  */
 public class FxContentEditorBean implements Serializable {
     private static final long serialVersionUID = 4667553874041738594L;
     private static final Log LOG = LogFactory.getLog(FxContentEditorBean.class);
 
     private static final String BEAN_NAME="fxContentEditorBean";
     private static final String EDITOR_REFERENCE_PREFIX ="_reference_";
     private String editorId;
     private FxData element;
     // String containing comma separated xpaths indicating which elements to add
     private String childrenToAdd;
     private HashMap<String, FxWrappedContent> contentStorage;
     // the component to rerender (used for resetting component tree)
     private String reRender;
     // JSF id of the reference to be edited/created
     private String referenceId;
     // binary support hack (the contents of binaries are not sent via ajax-requests.
     // Therefore if i.e images change, the whole form is submitted manually before
     // the next a4j action is performed. In order not to loose the a4j action, its
     // parameters set via parameters and executed manually.
     private String storageKey;
     private String nextA4jAction;
     private String actionXpath;
     // used by javascript to store folded groups
     // when the whole form is submitted
     private String toggledGroups;
     private String allOpened;
     // messages id
     public static final String MESSAGES_ID = "ceMessages";
     // affected xpath for error messages
     private String affectedXPath;
 
     public String getMessagesId() {
         return MESSAGES_ID;
     }
 
     public String getReRender() {
         return reRender;
     }
 
     public void setReRender(String reRender) {
         this.reRender = reRender;
     }
 
     public String getEditorId() {
         return editorId;
     }
 
     public void setEditorId(String editorId) {
         this.editorId = editorId;
     }
 
     public String getStorageKey() {
         return storageKey;
     }
 
     public void setStorageKey(String storageKey) {
         this.storageKey = storageKey;
     }
 
     public String getNextA4jAction() {
         return nextA4jAction;
     }
 
     public void setNextA4jAction(String nextA4jAction) {
         this.nextA4jAction = nextA4jAction;
     }
 
     public String getActionXpath() {
         return actionXpath;
     }
 
     public void setActionXpath(String actionXpath) {
         this.actionXpath = actionXpath;
     }
 
     public FxData getElement() {
         return element;
     }
 
     public void setElement(FxData element) {
         this.element = element;
     }
 
     public String getReferenceId() {
         return referenceId;
     }
 
     public void setReferenceId(String referenceId) {
         this.referenceId = referenceId;
     }
 
    /**
     * Delete the content currently being edited.
     *
     * @throws FxApplicationException on erros
     */
     public void _delete() throws FxApplicationException {
         // remove referencing contents from storage
         for (FxWrappedContent c: getReferencingContents(editorId)) {
             contentStorage.remove(c.getEditorId());
         }
         String formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
         EJBLookup.getContentEngine().remove(contentStorage.get(editorId).getContent().getPk());
         new FxFacesMsgInfo("ContentEditor.nfo.deleted", contentStorage.get(editorId).getContent().getId()).addToContext(formPrefix + ":" + editorId + "_" + MESSAGES_ID);
         // if content was referenced, update reference
         FxWrappedContent parent = getParentContent(editorId);
         if (parent != null) {
             FxPropertyData propData = parent.getContent().getPropertyData(parent.getIdGenerator().decode(parent.getGuiSettings().getOpenedReferenceId()));
             propData.getValue().setEmpty();
             parent.getGuiSettings().setOpenedReferenceId(null);
         }
         contentStorage.remove(editorId);
     }
 
     /**
      * JSF-Action to delete an existing content.
      */
     public void delete() {
         String formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
         try {
             _delete();
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     /**
      * Saves the content.
      */
     public void save() {
         String formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
         try {
             _save(false);
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     /**
      * Saves the content in a new version.
      */
     public void saveInNewVersion() {
         String formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
         try {
             _save(true);
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     /**
      * Saves the content in the current or in a new version.
      *
      * @param newVersion true if the data should be saved in a new version.
      * @return returns the new pk, or null on errors
      */
     public FxPK _save(boolean newVersion) throws FxApplicationException {
         try {
             FxPK pk=null;
             String formPrefix = "";
             FxWrappedContent parent;
             FxWrappedContent oldContent = contentStorage.get(editorId);
             formPrefix = oldContent.getGuiSettings().getFormPrefix();
             String msg = oldContent.getContent().getPk().isNew() ? "ContentEditor.nfo.created" : "ContentEditor.nfo.updated";
             //Store the content
             ContentEngine co = EJBLookup.getContentEngine();
             co.prepareSave(oldContent.getContent());
             pk = newVersion ? co.createNewVersion(oldContent.getContent()) : co.save(oldContent.getContent());
             new FxFacesMsgInfo(msg, pk.getId()).addToContext(formPrefix + ":" + editorId + "_" + MESSAGES_ID);
             parent = getParentContent(editorId);
             //if content is not referenced put modified content into storage
             if (parent == null) {
                 contentStorage.put(editorId, new FxWrappedContent(co.load(pk), editorId, oldContent.getGuiSettings(), false));
             }
             else {
                 // if content was referenced, update reference and remove from storage
                 FxPropertyData propData = parent.getContent().getPropertyData(parent.getIdGenerator().decode(parent.getGuiSettings().getOpenedReferenceId()));
                 propData.setValue(new FxReference(false, new ReferencedContent(pk)));
                 // remove from storage
                 contentStorage.remove(editorId);
                 // close reference
                 parent.getGuiSettings().setOpenedReferenceId(null);
             }
             return pk;
         }
         catch (FxApplicationException e) {
             setAffectedXPath(e);
             throw e;
         }
     }
 
     /**
      * JSF-action to delete the current version of the content.
      */
     public void deleteCurrentVersion() {
         String formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
         try {
             _deleteCurrentVersion();
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     public void _deleteCurrentVersion() throws FxApplicationException {
          // remove referencing contents from storage
             for (FxWrappedContent c: getReferencingContents(editorId)) {
                 contentStorage.remove(c.getEditorId());
             }
             FxPK oldPk = contentStorage.get(editorId).getContent().getPk();
             FxWrappedContent oldContent = contentStorage.get(editorId);
             // close opened references
             oldContent.getGuiSettings().setOpenedReferenceId(null);
             String formPrefix = oldContent.getGuiSettings().getFormPrefix();
             //remove version
             EJBLookup.getContentEngine().removeVersion(oldPk);
             // create new pk with maximum available version
             FxPK newPk = new FxPK(oldPk.getId(), FxPK.MAX);
             FxContent content = EJBLookup.getContentEngine().load(newPk);
             FxWrappedContent parent = getParentContent(editorId);
             // put maximum available version into content storage
             getContentStorage().put(editorId, new FxWrappedContent(content, editorId, oldContent.getGuiSettings(), parent!=null));
             // if content was referenced, update reference
             if (parent != null) {
                 FxPropertyData propData = parent.getContent().getPropertyData(parent.getIdGenerator().decode(parent.getGuiSettings().getOpenedReferenceId()));
                 propData.setValue(new FxReference(false, new ReferencedContent(content.getPk())));
             }
             new FxFacesMsgInfo("Content.nfo.deletedVersion", oldPk).addToContext(formPrefix + ":" + editorId + "_" + MESSAGES_ID);
     }
 
      /**
      * JSF-Action to edit/create a referenced content.
      */
     public void createReference() {
         String formPrefix = "";
         try {
              formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
              String referenceId = contentStorage.get(editorId).getIdGenerator().get(element);
              // set opened reference
              contentStorage.get(editorId).getGuiSettings().setOpenedReferenceId(referenceId);
              // create wrapped content for referenced type and put into storage
              long typeId = CacheAdmin.getFilteredEnvironment().getProperty(((FxPropertyData)element).getPropertyId()).getReferencedType().getId();
              String referenceEditorId = EDITOR_REFERENCE_PREFIX+editorId;
              FxWrappedContent.GuiSettings guiSettings= FxWrappedContent.GuiSettings.createGuiSettingsForReference(
                      contentStorage.get(editorId).getGuiSettings(), true);
              FxWrappedContent wc = new FxWrappedContent(EJBLookup.getContentEngine().initialize(typeId),
                                 referenceEditorId, guiSettings, true);
              contentStorage.put(referenceEditorId, wc);
             }
         catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     public void editReference() {
         String formPrefix = "";
         try {
              formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
              String referenceId = contentStorage.get(editorId).getIdGenerator().get(element);
              // set opened reference
              contentStorage.get(editorId).getGuiSettings().setOpenedReferenceId(referenceId);
              // create wrapped content for referenced pk and put into storage
              ReferencedContent ref = ((FxReference)((FxPropertyData)element).getValue()).getBestTranslation();
              FxPK pk = new FxPK(ref.getId(), ref.getVersion());
              String referenceEditorId = EDITOR_REFERENCE_PREFIX+editorId;
              FxWrappedContent.GuiSettings guiSettings= FxWrappedContent.GuiSettings.createGuiSettingsForReference(
                      contentStorage.get(editorId).getGuiSettings(), true);
              guiSettings.setDisableDelete(true);
              FxWrappedContent wc = new FxWrappedContent(EJBLookup.getContentEngine().load(pk),
                                 referenceEditorId, guiSettings, true);
              contentStorage.put(referenceEditorId, wc);
             }
         catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     /**
      * JSF-Action to compact the given content.
      */
     public void compact() {
         contentStorage.get(editorId).getContent().getRootGroup().removeEmptyEntries();
         resetForm(contentStorage.get(editorId).getGuiSettings().getFormPrefix());
     }
 
     /**
      * JSF-action to cancel editing.
      */
     public void cancel() {
         String formPrefix = "";
         try {
              // remove referencing contents from storage
             for (FxWrappedContent c: getReferencingContents(editorId)) {
                 contentStorage.remove(c.getEditorId());
             }
             formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
             // check if the content is referenced by an existing content
             FxWrappedContent parent = getParentContent(editorId);
             if (parent != null) {
                 parent.getGuiSettings().setOpenedReferenceId(null);
             }
             else {
                 contentStorage.get(editorId).setReset(true);
             }
         }
         catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
 
     /**
      * JSF-action to enable editing.
      */
     public void enableEdit() {
         contentStorage.get(editorId).getGuiSettings().setEditMode(true);
         resetForm(contentStorage.get(editorId).getGuiSettings().getFormPrefix());
     }
 
     /**
      * Ajax call for removing an assignment or group.
      */
     public void removeElement() {
         String formPrefix = "";
         try {
             formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
             contentStorage.get(editorId).getContent().remove(element.getXPathFull());
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     /**
      * Ajax call for adding another assignment or group.
      */
     public void addElement() {
         String formPrefix = "";
         try {
             formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
             element.createNew(element.getPos() + 1);
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     /**
      * Ajax call for moving an assignment or group up.
      */
     public void moveElementUp() {
         String formPrefix = "";
         try {
             formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
             element.getParent().moveChild(element.getXPathElement(), -1);
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     /**
      * Ajax call for moving an assignment or group down.
      */
     public void moveElementDown() {
         String formPrefix = "";
         try {
             formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
             element.getParent().moveChild(element.getXPathElement(), +1);
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             resetForm(formPrefix);
         }
     }
 
     public String getChildrenToAdd() {
         return childrenToAdd;
     }
 
     public void setChildrenToAdd(String childrenToAdd) {
         this.childrenToAdd = childrenToAdd;
     }
 
     /**
      * Ajax call for inserting an assignments or groups below a given
      * FxData element.
      */
     public void addChilds() {
         String formPrefix = "";
         try {
             formPrefix = contentStorage.get(editorId).getGuiSettings().getFormPrefix();
             FxGroupData grp;
             int pos;
 
             if (element instanceof FxGroupData) {
                 grp = (FxGroupData) element;
                 pos = 1;
             } else {
                 grp = element.getParent();
                 pos = element.getPos() + 1;
             }
             String[] children = getChildrenToAdd() == null || getChildrenToAdd().trim().length() == 0 ? new String[0] : getChildrenToAdd().split(",");
 
             for (String ele : children) {
                 grp.addEmptyChild(ele, pos++);
             }
 
         } catch (Throwable t) {
             addErrorMessage(t, formPrefix);
         }
         finally {
             //reset children
             setChildrenToAdd(null);
             resetForm(formPrefix);
         }
     }
 
     /**
      * Returns the content storage.
      *
      * @return content storage.
      */
     public HashMap<String, FxWrappedContent> getContentStorage() {
         return contentStorage;
     }
 
     /**
      * Sets the content storage.
      * (Used to store the content storage in the view-state).
      *
      * @param contentStorage content storage.
      */
     public void setContentStorage(HashMap<String, FxWrappedContent> contentStorage) {
         this.contentStorage = contentStorage;
     }
 
     /**
      * JSF-Action to perform a pending A4J-action that was manually set by javascript.
      */
     public void resolveA4jAction() {
         if (StringUtils.isEmpty(storageKey) || StringUtils.isEmpty(nextA4jAction) || StringUtils.isEmpty(actionXpath))
             return;
         try {
             setEditorId(storageKey);
             final List<FxData> data = contentStorage.get(storageKey).getContent().getData(actionXpath);
             setElement(data.get(0));
             if (nextA4jAction.equalsIgnoreCase("addElement")) {
                 addElement();
             } else if (nextA4jAction.equalsIgnoreCase("removeElement")) {
                 removeElement();
             } else if (nextA4jAction.equalsIgnoreCase("moveElementUp")) {
                 moveElementUp();
             } else if (nextA4jAction.equalsIgnoreCase("moveElementDown")) {
                 moveElementDown();
             } else if (nextA4jAction.equalsIgnoreCase("addChilds")) {
                 addChilds();
             }
         } catch (Exception e) {
             LOG.warn("Failed to execute content editor action " + nextA4jAction + " for XPath "
                     + actionXpath + ": " + e.getMessage(), e);
         }
         finally {
             storageKey = null;
             nextA4jAction = null;
             actionXpath = null;
         }
     }
 
     /**
      * Reset a subtree of the componet tree in order to avoid component tree
      * update problems that occur when the structure of the component tree changes
      * in between requests for the same view.
      *
      * @param formPrefix form prefix
      */
     private void resetForm(String formPrefix) {
         FxJsfUtils.resetFaceletsComponent(formPrefix + ":" + reRender);
     }
 
     /**
      * Adds an error message to the content editor
      *
      * @param t          throwable
      * @param formPrefix form prefix
      */
     private void addErrorMessage(Throwable t, String formPrefix) {
         if ("".equals(formPrefix))
             new FxFacesMsgErr(t).addToContext();
         else
             new FxFacesMsgErr(t).addToContext(formPrefix + ":" + editorId + "_" + MESSAGES_ID);
     }
 
     private void setAffectedXPath(FxApplicationException e) {
          if (!StringUtils.isEmpty(e.getAffectedXPath()))
             this.affectedXPath = editorId+":"+FxJsfUtils.encodeJSFIdentifier(e.getAffectedXPath());
     }
 
     /**
      * Returns the managed bean name, as must be set in faces-config.xml.
      *
      * @return managed bean name.
      */
     public static String getBeanName() {
         return BEAN_NAME;   
     }
 
     public String getEditorReferencePrefix() {
         return EDITOR_REFERENCE_PREFIX;
     }
 
     /**
      * Returns the parent content, if the content with the specified id is a referenced content,
      * otherwise null.
      *
      * @param editorId editor/(==storage) id of the content.
      * @return the parent content, if the content with the specified id is a referenced content,
      * otherwise null.
      */
     public FxWrappedContent getParentContent(String editorId) {
         FxWrappedContent parent = null;
         if (editorId.startsWith(EDITOR_REFERENCE_PREFIX)) {
             String strippedId = editorId.substring(EDITOR_REFERENCE_PREFIX.length());
             for (String key : contentStorage.keySet()) {
                 if (strippedId.startsWith(key)) {
                     parent= contentStorage.get(key);
                     break;
                 }
             }
         }
         return parent;
     }
 
     public List<FxWrappedContent> getReferencingContents(String editorId) {
         List<FxWrappedContent> result = new ArrayList<FxWrappedContent>(2);
         boolean found=true;
         String refEditorId = editorId;
         while (found) {
             refEditorId = EDITOR_REFERENCE_PREFIX+refEditorId;
             FxWrappedContent wc = contentStorage.containsKey(refEditorId) ? contentStorage.get(refEditorId) :null;
             if (wc == null)
                 found = false;
             else
                 result.add(wc);
         }
         return result;
     }
 
     public String getToggledGroups() {
         return toggledGroups;
     }
 
     public void setToggledGroups(String toggledGroups) {
         this.toggledGroups = toggledGroups;
     }
 
     public String getAllOpened() {
         return allOpened;
     }
 
     public void setAllOpened(String allOpened) {
         this.allOpened = allOpened;
     }
 
     public String getAffectedXPath() {
         return affectedXPath;
     }
 
     public void setAffectedXPath(String affectedXPath) {
         this.affectedXPath = null;
     }
 }
