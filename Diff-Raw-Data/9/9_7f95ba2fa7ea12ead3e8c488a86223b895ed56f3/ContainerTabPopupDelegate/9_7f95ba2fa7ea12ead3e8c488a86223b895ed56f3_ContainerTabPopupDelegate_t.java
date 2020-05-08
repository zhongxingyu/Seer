 /*
  * Created On:  30-Nov-06 12:25:24 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.container;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.container.ContainerVersionArtifactVersionDelta.Delta;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.container.ContainerDraft;
 import com.thinkparity.ophelia.model.container.ContainerDraft.ArtifactState;
 
 import com.thinkparity.ophelia.browser.application.browser.DefaultBrowserPopupDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanelPopupDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.ContainerPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.PopupDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DocumentView;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.contact.CreateOutgoingUserInvitation;
 import com.thinkparity.ophelia.browser.platform.action.contact.Read;
 import com.thinkparity.ophelia.browser.platform.action.container.*;
 import com.thinkparity.ophelia.browser.platform.action.document.Open;
 import com.thinkparity.ophelia.browser.platform.action.document.OpenVersion;
 import com.thinkparity.ophelia.browser.platform.action.profile.Update;
 import com.thinkparity.ophelia.browser.platform.action.profile.UpdatePassword;
 import com.thinkparity.ophelia.browser.util.swing.plaf.ThinkParityMenuItem;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 final class ContainerTabPopupDelegate extends DefaultBrowserPopupDelegate
         implements TabPanelPopupDelegate, PopupDelegate {
 
     /** A list of action ids, used for the container popup. */
     private final List<ActionId> actionIds;
     
     /** A list of data, used for the container popup. */
     private final List<Data> dataList;
     
     /** A <code>ContainerModel</code>. */
     private final ContainerTabModel model;
 
     /**
      * Create ContainerTabPopupFactory.
      * 
      * @param model
      *            The <code>ContainerModel</code>.
      */
     ContainerTabPopupDelegate(final ContainerTabModel model) {
         super();
         this.model = model;
         this.actionIds = new ArrayList<ActionId>();
         this.dataList = new ArrayList<Data>();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.PopupDelegate#showForContainer(com.thinkparity.codebase.model.container.Container, com.thinkparity.ophelia.model.container.ContainerDraft)
      */
     public void showForContainer(final Container container, final ContainerDraft draft) {
         final boolean online = isOnline();
         boolean needSeparator = false;
 
         // create draft
         if (null == draft
                 && ((online && container.isLatest()) || !isDistributed(container.getId()))) {
             final Data createDraftData = new Data(1);
             createDraftData.set(CreateDraft.DataKey.CONTAINER_ID, container.getId());
             addWithExpand(ActionId.CONTAINER_CREATE_DRAFT, createDraftData, container);  
             needSeparator = true;
         }
 
         // publish
         if (null != draft && online && isLocalDraftModified(container.getId())) {
             final Data publishData = new Data(2);
             publishData.set(Publish.DataKey.CONTAINER_ID, draft.getContainerId());
             publishData.set(Publish.DataKey.DISPLAY_AVATAR, Boolean.TRUE);
             add(ActionId.CONTAINER_PUBLISH, publishData);
             needSeparator = true;
         }
 
         // add document
        if (isLocalDraft(draft)) {
             final Data addDocumentData = new Data(2);
             addDocumentData.set(AddDocument.DataKey.CONTAINER_ID, draft.getContainerId());
             addDocumentData.set(AddDocument.DataKey.FILES, new File[0]);
             add(ActionId.CONTAINER_ADD_DOCUMENT, addDocumentData);
             needSeparator = true;
         }
 
         // separator
         if (needSeparator) {
             addSeparator();
             needSeparator = false;
         }
 
         // Rename container
         if (!isDistributed(container.getId())) {
             final Data renameData = new Data(1);
             renameData.set(Rename.DataKey.CONTAINER_ID, container.getId());
             addWithExpand(ActionId.CONTAINER_RENAME, renameData, container);
             needSeparator = true;
         }
 
         // archive
         if (online && isDistributed(container.getId()) && !isLocalDraft(draft)) {
             final Data archiveData = new Data(1);
             archiveData.set(Archive.DataKey.CONTAINER_ID, container.getId());
             addWithExpand(ActionId.CONTAINER_ARCHIVE, archiveData, container);
             needSeparator = true;
         }
 
         // delete
         // This menu is shown if online, or if it has never been published.
         if (online || !isDistributed(container.getId())) {
             final Data deleteData = new Data(1);
             deleteData.set(Delete.DataKey.CONTAINER_ID, container.getId());
             addWithExpand(ActionId.CONTAINER_DELETE, deleteData, container);
             needSeparator = true;
         }
 
         // export
         if (isDistributed(container.getId())) {
             if (needSeparator) {
                 addSeparator();
             }
             final Data exportData = new Data(1);
             exportData.set(com.thinkparity.ophelia.browser.platform.action.container.Export.DataKey.CONTAINER_ID, container.getId());
             addWithExpand(ActionId.CONTAINER_EXPORT, exportData, container);
             needSeparator = true;
         }
 
         // include the container's id and unique id in the menu
         if (model.isDevelopmentMode()) {
             final Clipboard systemClipboard =
                 Toolkit.getDefaultToolkit().getSystemClipboard();
             final ActionListener debugActionListener = new ActionListener() {
                 public void actionPerformed(final ActionEvent e) {
                     final StringSelection stringSelection =
                         new StringSelection(((JComponent) e.getSource()).getClientProperty("COPY_ME").toString());
                     systemClipboard.setContents(stringSelection, null);
                 }
             };
             final JMenuItem idJMenuItem = new ThinkParityMenuItem(MessageFormat.format("getId():{0,number,#}", container.getId()));
             idJMenuItem.putClientProperty("COPY_ME", container.getId());
             idJMenuItem.addActionListener(debugActionListener);
 
             final JMenuItem uidJMenuItem = new ThinkParityMenuItem(MessageFormat.format("getUniqueId():{0}", container.getUniqueId()));
             uidJMenuItem.putClientProperty("COPY_ME", container.getUniqueId());
             uidJMenuItem.addActionListener(debugActionListener);
 
             addSeparator();
             add(idJMenuItem);
             add(uidJMenuItem);
             add(MessageFormat.format("isBookmarked:{0}", container.isBookmarked()));
             add(MessageFormat.format("isLatest:{0}", container.isLatest()));
             add(MessageFormat.format("isSeen:{0}", container.isSeen()));
         }
         show();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.PopupDelegate#showForDocument(com.thinkparity.ophelia.model.container.ContainerDraft, com.thinkparity.codebase.model.document.Document)
      */
     public void showForDocument(final ContainerDraft draft, final Document document) {
         // open
         if (ArtifactState.REMOVED != draft.getState(document)) {
             final Data data = new Data(1);
             data.set(Open.DataKey.DOCUMENT_ID, document.getId());
             add(ActionId.DOCUMENT_OPEN, data);
         }
 
         // rename document
         if (ArtifactState.ADDED == draft.getState(document)) {
             final Data renameData = new Data(2);
             renameData.set(RenameDocument.DataKey.CONTAINER_ID, draft.getContainerId());
             renameData.set(RenameDocument.DataKey.DOCUMENT_ID, document.getId());
             add(ActionId.CONTAINER_RENAME_DOCUMENT, renameData);
         }
 
         // revert
         if (ArtifactState.MODIFIED == draft.getState(document)) {
             final Data revertData = new Data(2);
             revertData.set(RevertDocument.DataKey.CONTAINER_ID, draft.getContainerId());
             revertData.set(RevertDocument.DataKey.DOCUMENT_ID, document.getId());
             add(ActionId.CONTAINER_REVERT_DOCUMENT, revertData);
         }
 
         // undelete
         if (ArtifactState.REMOVED == draft.getState(document)) {
             final Data undeleteData = new Data(2);
             undeleteData.set(UndeleteDocument.DataKey.CONTAINER_ID, draft.getContainerId());
             undeleteData.set(UndeleteDocument.DataKey.DOCUMENT_ID, document.getId());
             add(ActionId.CONTAINER_UNDELETE_DOCUMENT, undeleteData);
         }
 
         // remove document
         if (ArtifactState.REMOVED != draft.getState(document)) {
             final Data removeData = new Data(2);
             removeData.set(RemoveDocument.DataKey.CONTAINER_ID, draft.getContainerId());
             removeData.set(RemoveDocument.DataKey.DOCUMENT_ID, document.getId());
             add(ActionId.CONTAINER_REMOVE_DOCUMENT, removeData);
         }
 
         show();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.PopupDelegate#showForDocument(com.thinkparity.codebase.model.document.DocumentVersion)
      */
     public void showForDocument(final DocumentVersion documentVersion) {
         // open
         final Data data = new Data(2);
         data.set(OpenVersion.DataKey.DOCUMENT_ID, documentVersion.getArtifactId());
         data.set(OpenVersion.DataKey.VERSION_ID, documentVersion.getVersionId());
         add(ActionId.DOCUMENT_OPEN_VERSION, data);
         
         show();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.ContainerTabPopupDelegate#showForDraft(com.thinkparity.codebase.model.container.Container,
      *      com.thinkparity.ophelia.model.container.ContainerDraft)
      * 
      */
     public void showForDraft(final Container container, final ContainerDraft draft) {
         final boolean online = isOnline();
 
         // publish
         if (online && isLocalDraftModified(container.getId())) {
             final Data publishData = new Data(2);
             publishData.set(Publish.DataKey.CONTAINER_ID, draft.getContainerId());
             publishData.set(Publish.DataKey.DISPLAY_AVATAR, Boolean.TRUE);
             add(ActionId.CONTAINER_PUBLISH, publishData);
         }
 
         // add document
         final Data addDocumentData = new Data(2);
         addDocumentData.set(AddDocument.DataKey.CONTAINER_ID, draft.getContainerId());
         addDocumentData.set(AddDocument.DataKey.FILES, new File[0]);
         add(ActionId.CONTAINER_ADD_DOCUMENT, addDocumentData);
 
         // update draft comment
         if (online) {
             final Data data = new Data(1);
             data.set(UpdateDraftComment.DataKey.CONTAINER_ID, draft.getContainerId());
             add(ActionId.CONTAINER_UPDATE_DRAFT_COMMENT, data);
         }
 
         // delete draft
         // This menu is shown if online, or if it has never been published.
         if (online || !isDistributed(container.getId())) {
             final Data deleteData = new Data(2);
             deleteData.set(DeleteDraft.DataKey.CONTAINER_ID, draft.getContainerId());
             add(ActionId.CONTAINER_DELETE_DRAFT, deleteData);
         }
 
         // print
         final List<Document> documentsNotDeleted = getDocumentsNotDeleted(draft);
         if (documentsNotDeleted.size() > 0) {
             addSeparator();
             final Data printData = new Data(1);
             printData.set(PrintDraft.DataKey.CONTAINER_ID, draft.getContainerId());
             add(ActionId.CONTAINER_PRINT_DRAFT, printData);
         }
 
         show();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanelPopupDelegate#showForPanel(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      *
      */
     public void showForPanel(final TabPanel tabPanel) {
         final ContainerPanel containerPanel = (ContainerPanel) tabPanel;
         showForContainer(containerPanel.getContainer(),
                 containerPanel.getDraft());
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.PopupDelegate#showForUser(com.thinkparity.codebase.model.user.User)
      */
     public void showForUser(final User user) {
         final boolean online = isOnline();
 
         // invite
         if (online) {
             if (!isLocalUser(user) && !doesExistContact(user) && !doesExistOutgoingUserInvitation(user)) {
                 final Data data = new Data(1);
                 data.set(CreateOutgoingUserInvitation.DataKey.USER_ID, user.getLocalId());
                 add(ActionId.CONTACT_CREATE_OUTGOING_USER_INVITATION, data);
                 addSeparator();
             }
         }
 
         // open
         if (isLocalUser(user)) {
             final Data data = new Data(1);
             data.set(Update.DataKey.DISPLAY_AVATAR, Boolean.TRUE);
             add(ActionId.PROFILE_UPDATE, data);
 
             final Data updatePasswordData = new Data(1);
             updatePasswordData.set(UpdatePassword.DataKey.DISPLAY_AVATAR, Boolean.TRUE);
             add(ActionId.PROFILE_UPDATE_PASSWORD, updatePasswordData);
         } else {
             final Data data = new Data(1);
             data.set(Read.DataKey.CONTACT_ID, user.getId());
             add(ActionId.CONTACT_READ, data);
         }
 
         show();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.PopupDelegate#showForVersion(com.thinkparity.codebase.model.container.ContainerVersion, java.util.List, java.util.List, com.thinkparity.codebase.model.user.User)
      */
     public void showForVersion(final ContainerVersion version,
             final List<DocumentView> documentViews,
             final List<ArtifactReceipt> publishedTo, final User publishedBy) {
         final boolean online = isOnline();
         boolean needSeparator = false;
 
         // publish version
         if (online) {
             final Data publishData = new Data(3);
             publishData.set(PublishVersion.DataKey.CONTAINER_ID, version.getArtifactId());
             publishData.set(PublishVersion.DataKey.VERSION_ID, version.getVersionId());
             publishData.set(PublishVersion.DataKey.DISPLAY_AVATAR, Boolean.TRUE);
             add(ActionId.CONTAINER_PUBLISH_VERSION, publishData);
             needSeparator = true;
         }
 
         // display version comment
         if (version.isSetComment()) {
             final Data commentData = new Data(2);
             commentData.set(DisplayVersionInfo.DataKey.CONTAINER_ID, version.getArtifactId());
             commentData.set(DisplayVersionInfo.DataKey.VERSION_ID, version.getVersionId());
             add(ActionId.CONTAINER_DISPLAY_VERSION_INFO, commentData);
             needSeparator = true;
         }
 
         // separator
         if (needSeparator) {
             addSeparator();
             needSeparator = false;
         }
 
         // print
         final List<DocumentView> documentViewsNotDeleted = getDocumentViewsNotDeleted(documentViews);
         if (documentViewsNotDeleted.size() > 0) {
             final Data printData = new Data(2);
             printData.set(com.thinkparity.ophelia.browser.platform.action.container.PrintVersion.DataKey.CONTAINER_ID, version.getArtifactId());
             printData.set(com.thinkparity.ophelia.browser.platform.action.container.PrintVersion.DataKey.VERSION_ID, version.getVersionId());
             add(ActionId.CONTAINER_PRINT_VERSION, printData);
         }
 
         // include the version's created on/updated on
         if (model.isDevelopmentMode()) {
             addSeparator();
             add(MessageFormat.format(
                     "getVersionId():{0,number,#}", version.getVersionId()));
             add(MessageFormat.format(
                     "createdOn():{0,date,yyyy-MM-dd HH:mm:ss.SSS Z}",
                     version.getCreatedOn().getTime()));
             add(MessageFormat.format(
                     "updatedOn():{0,date,yyyy-MM-dd HH:mm:ss.SSS Z}",
                     version.getUpdatedOn().getTime()));
         }
 
         show();
     }
 
     /**
      * Add an action to a popup menu.
      * A second action to expand the container is inserted.
      * 
      * @param actionId
      *            An <code>ActionId</code>.
      * @param data
      *            The <code>Data</code>.       
      * @param container
      *            The <code>Container</code>.            
      */
     private void addWithExpand(final ActionId actionId, final Data data,
             final Container container) {
         actionIds.clear();
         dataList.clear();
         
         final Data expandData = new Data(2);
         expandData.set(Expand.DataKey.CONTAINER_ID, container.getId());
         expandData.set(Expand.DataKey.ARCHIVE_TAB, Boolean.FALSE);
         actionIds.add(ActionId.CONTAINER_EXPAND);
         dataList.add(expandData);
         
         actionIds.add(actionId);
         dataList.add(data);
         add(actionIds, dataList, 1);
     }
 
     /**
      * Determine if the specified user is a contact.
      * 
      * @param user
      *            A <code>User</code>.
      * @return True if this is the local user; false otherwise.
      */
     private boolean doesExistContact(final User user) {
         return model.readDoesExistContact(user).booleanValue();
     }
 
     /**
      * Determine if the specified user is a contact.
      * 
      * @param user
      *            A <code>User</code>.
      * @return True if this is the local user; false otherwise.
      */
     private boolean doesExistOutgoingUserInvitation(final User user) {
         return model.readDoesExistOutgoingUserInvitation(user).booleanValue();
     }
 
     /**
      * Get the list of documents that have not been deleted.
      * 
      * @param draft
      *            A <code>ContainerDraft</code>.
      * @return A List of <code>Document</code>.
      */
     private List<Document> getDocumentsNotDeleted(final ContainerDraft draft) {
         final List<Document> documentsNotDeleted = new ArrayList<Document>();
         for (final Document document : draft.getDocuments()) {
             if (ArtifactState.REMOVED != draft.getState(document)) {
                 documentsNotDeleted.add(document);
             }
         }
         return documentsNotDeleted;
     }
 
     /**
      * Get the list of document views that have not been deleted.
      * 
      * @param documentViews
      *            A list of <code>DocumentView</code>.
      * @return A List of <code>DocumentView</code>.
      */
     private List<DocumentView> getDocumentViewsNotDeleted(final List<DocumentView> documentViews) {
         final List<DocumentView> documentsNotDeleted = new ArrayList<DocumentView>();
         for (final DocumentView documentView : documentViews) {
             if (Delta.REMOVED != documentView.getDelta()) {
                 documentsNotDeleted.add(documentView);
             }
         }
         return documentsNotDeleted;
     }
 
     /**
      * Determine if the container has been distributed.
      * 
      * @param containerId
      *            A <code>Long</code>.
      * @return True if this container has been distributed; false otherwise.
      */
     private boolean isDistributed(final Long containerId) {
         return model.readIsDistributed(containerId);
     }
 
     /**
      * Determine if the draft is a local draft.
      * 
      * @param draft
      *            A <code>ContainerDraft</code>.
      * @return True if the draft is not null and the draft is local.
      */
     private boolean isLocalDraft(final ContainerDraft draft) {
         return null != draft && draft.isLocal().booleanValue();
     }
 
     /**
      * Determine if the local draft is modified, ie. at least one document changed.
      * 
      * @param containerId
      *            A <code>Long</code>.
      * @return True if this container has a document that has been modified; false otherwise.
      */
     private boolean isLocalDraftModified(final Long containerId) {
         return model.readIsLocalDraftModified(containerId);
     }
 
     /**
      * Determine if the specified user is the local user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return True if this is the local user; false otherwise.
      */
     private boolean isLocalUser(final User user) {
         return model.readIsLocalUser(user).booleanValue();
     }
 
     /**
      * Determine if online.
      * 
      * @return True if online; false otherwise.
      */
     private boolean isOnline() {
         return model.isOnline().booleanValue();
     }
 }
