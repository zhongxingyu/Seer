 /*
  * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Olivier Grisel
  */
 package org.nuxeo.ecm.platform.semanticentities.jsf.actions;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.contexts.Contexts;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.international.StatusMessage;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.trash.TrashService;
 import org.nuxeo.ecm.platform.query.api.PageProvider;
 import org.nuxeo.ecm.platform.semanticentities.DereferencingException;
 import org.nuxeo.ecm.platform.semanticentities.EntitySuggestion;
 import org.nuxeo.ecm.platform.semanticentities.LocalEntityService;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntity;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntityService;
 import org.nuxeo.ecm.platform.semanticentities.SemanticAnalysisService;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceRelation;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.ecm.webapp.helpers.EventManager;
 import org.nuxeo.ecm.webapp.helpers.EventNames;
 import org.nuxeo.runtime.api.Framework;
 
 @Name("semanticEntitiesActions")
 @Scope(ScopeType.CONVERSATION)
 public class SemanticEntitiesActions {
 
     public static final Log log = LogFactory.getLog(SemanticEntitiesActions.class);
 
     @In(create = true)
     protected NavigationContext navigationContext;
 
     @In(required = false)
     protected CoreSession documentManager;
 
     @In(create = true)
     protected FacesMessages facesMessages;
 
     @In(create = true)
     protected Map<String,String> messages;
 
     protected String documentSuggestionKeywords;
 
     protected String selectedDocumentId;
 
     protected EntitySuggestion selectedEntitySuggestion;
 
     protected List<DocumentModel> documentSuggestions;
 
     protected LocalEntityService leService;
 
     protected SemanticAnalysisService saService;
 
     protected boolean isRemoteEntitySearchDisplayed = false;
 
     protected URI selectedEntitySuggestionUri;
 
     protected String selectedEntitySuggestionLabel;
 
     protected LocalEntityService getLocalEntityService() {
         if (leService == null) {
             try {
                 leService = Framework.getService(LocalEntityService.class);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return leService;
     }
 
     protected SemanticAnalysisService getSemanticAnalysisService() {
         if (saService == null) {
             try {
                 saService = Framework.getService(SemanticAnalysisService.class);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return saService;
     }
 
     @Factory(scope = ScopeType.EVENT, value = "semanticWorkInProgressMessage")
     public String getSemanticWorkInProgressMessage() {
         DocumentRef docRef = navigationContext.getCurrentDocument().getRef();
         return getSemanticWorkInProgressMessageFor(docRef);
     }
 
     public String getSemanticWorkInProgressMessageFor(DocumentRef docRef) {
         String message = getSemanticAnalysisService().getProgressStatus(docRef);
         if (message != null) {
             // there is some work in progress: invalidate the cached results to
             // display the real state
             invalidateCurrentDocumentProviders();
         }
         return message;
     }
 
     @Factory(scope = ScopeType.SESSION, value = "canBrowseEntityContainer")
     public boolean getCanBrowseEntityContainer() throws Exception {
         // the ScopeType.SESSION scope might hide change in permissions on the
         // entity container unless affected users do logout but this is
         // necessary to avoid DB requests at each page view since the
         // canBrowseEntityContainer variable is used to filter an action that
         // shows up in the top level banner
         return getLocalEntityService().getEntityContainer(documentManager) != null;
     }
 
     public void launchAsyncAnalysis() throws ClientException {
         getSemanticAnalysisService().launchAnalysis(navigationContext.getCurrentDocument());
         invalidateCurrentDocumentProviders();
     }
 
     public String goToEntityContainer() throws Exception {
         DocumentModel entityContainer = getLocalEntityService().getEntityContainer(documentManager);
         if (entityContainer == null) {
             // the user does not have the permission to browse the entities
             return null;
         }
         return navigationContext.navigateToDocument(entityContainer);
     }
 
     @Factory(scope = ScopeType.CONVERSATION, value = "entityOccurrenceProvider")
     public PageProvider<DocumentModel> getCurrentEntityOccurrenceProvider() throws ClientException, Exception {
         return getEntityOccurrenceProvider(navigationContext.getCurrentDocument());
     }
 
     /**
      * Return the documents that hold an occurrence to the given entity.
      */
     public PageProvider<DocumentModel> getEntityOccurrenceProvider(DocumentModel entity) throws ClientException,
                                                                                         Exception {
         return getLocalEntityService().getRelatedDocuments(documentManager, entity.getRef(), null);
     }
 
     @Factory(scope = ScopeType.EVENT, value = "relatedPeopleProvider")
     public PageProvider<DocumentModel> getRelatedPeopleProvider() throws ClientException, Exception {
         return getRelatedEntitiesProvider(navigationContext.getCurrentDocument(), "Person");
     }
 
     @Factory(scope = ScopeType.EVENT, value = "relatedPlacesProvider")
     public PageProvider<DocumentModel> getRelatedPlacesProvider() throws ClientException, Exception {
         return getRelatedEntitiesProvider(navigationContext.getCurrentDocument(), "Place");
     }
 
     @Factory(scope = ScopeType.EVENT, value = "relatedOrganizationsProvider")
     public PageProvider<DocumentModel> getRelatedOrganizationsProvider() throws ClientException, Exception {
         return getRelatedEntitiesProvider(navigationContext.getCurrentDocument(), "Organization");
     }
 
     /**
      * Return the local entities that hold an occurrence to the given document.
      */
     public PageProvider<DocumentModel> getRelatedEntitiesProvider(DocumentModel doc, String entityType) throws ClientException,
                                                                                                        Exception {
         return getLocalEntityService().getRelatedEntities(documentManager, doc.getRef(), entityType);
     }
 
     /*
      * Ajax callbacks for new occurrence relationship creation.
      */
 
     public List<DocumentModel> suggestDocuments(Object keywords) {
         try {
             return getLocalEntityService().suggestDocument(documentManager, keywords.toString(), null, 10);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.fetchingDocuments"));
             return Collections.emptyList();
         }
     }
 
     public void setSelectedDocumentId(String selectedDocumentId) {
         this.selectedDocumentId = selectedDocumentId;
     }
 
     public List<EntitySuggestion> suggestEntities(Object keywords) {
         try {
             return getLocalEntityService().suggestEntity(documentManager, keywords.toString(), null, 10);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.fetchingEntities"));
             return Collections.emptyList();
         }
     }
 
     public void setSelectedSuggestion(EntitySuggestion suggestion) {
         this.selectedEntitySuggestion = suggestion;
     }
 
     public void addNewOccurrenceRelation() {
         try {
             if (selectedDocumentId != null) {
                 getLocalEntityService().addOccurrences(documentManager, new IdRef(selectedDocumentId),
                     navigationContext.getCurrentDocument().getRef(), null);
             } else if (selectedEntitySuggestion != null) {
                 DocumentModel localEntity = leService
                         .asLocalEntity(documentManager, selectedEntitySuggestion);
                leService.addOccurrences(documentManager, navigationContext.getCurrentDocument().getRef(),
                     localEntity.getRef(), null);
             }
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.addingRelation"));
         }
         invalidateCurrentDocumentProviders();
     }
 
     public void removeOccurrenceRelation(String docId, String entityId) {
         OccurrenceRelation rel;
         try {
             rel = getLocalEntityService().getOccurrenceRelation(documentManager, new IdRef(docId),
                 new IdRef(entityId));
             if (rel != null) {
                 // TODO: define an invalidate transition to be used by default
                 // for explicitly handling human correction of false positives
                 DocumentModel relDoc = rel.getOccurrenceDocument();
                 List<DocumentModel> docToDelete = Arrays.asList(relDoc);
                 TrashService trashService = Framework.getService(TrashService.class);
                 if (trashService.canDelete(docToDelete, documentManager.getPrincipal(), false)) {
                     trashService.trashDocuments(docToDelete);
                 } else {
                     facesMessages.add(StatusMessage.Severity.WARN, messages.get("error.removingRelation"));
                 }
             }
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.removingRelation"));
         }
         invalidateCurrentDocumentProviders();
     }
 
     /*
      * Ajax callbacks for remote entity linking and syncing
      */
 
     @Factory(scope = ScopeType.EVENT, value = "currentEntitySameAs")
     public List<RemoteEntity> getCurrentEntitySameAs() {
         try {
             return RemoteEntity.fromDocument(navigationContext.getCurrentDocument());
         } catch (ClientException e) {
             log.error(e, e);
             facesMessages
                     .add(StatusMessage.Severity.ERROR, messages.get("error.fetchingLocalLinkedEntities"));
             return Collections.emptyList();
         }
     }
 
     public void showSuggestRemoteEntitySearch() {
         isRemoteEntitySearchDisplayed = true;
     }
 
     public boolean getShowSuggestRemoteEntitySearch() {
         return isRemoteEntitySearchDisplayed;
     }
 
     public List<RemoteEntity> suggestRemoteEntity(Object input) {
         String type = navigationContext.getCurrentDocument().getType();
         String keywords = (String) input;
         try {
             RemoteEntityService remoteEntityService = Framework.getService(RemoteEntityService.class);
             List<RemoteEntity> filteredSuggestions = new ArrayList<RemoteEntity>();
             List<RemoteEntity> suggestions = remoteEntityService.suggestRemoteEntity(keywords, type, 5);
             // TODO: filter out entities that already have a local entity synced
             // to them
             filteredSuggestions.addAll(suggestions);
             return filteredSuggestions;
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.fetchingRemoteEntities"));
             return Collections.emptyList();
         }
     }
 
     public void setSelectedEntitySuggestionUri(URI uri) {
         selectedEntitySuggestionUri = uri;
     }
 
     public void setSelectedEntitySuggestionLabel(String label) {
         selectedEntitySuggestionLabel = label;
     }
 
     public void addRemoteEntityLinkAndSync() {
         if (selectedEntitySuggestionLabel == null || selectedEntitySuggestionUri == null) {
             // TODO: display some user friendly warning
             return;
         }
         RemoteEntity re = new RemoteEntity(selectedEntitySuggestionLabel, selectedEntitySuggestionUri);
         DocumentModel doc = navigationContext.getChangeableDocument();
         try {
             // TODO: once the UI allows to set multiple links to several remote
             // sources we should set override back to false
             syncAndSaveDocument(doc, re.uri, true);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.linkingToRemoteEntity"));
         }
         Contexts.removeFromAllContexts("currentEntitySameAs");
     }
 
     public void syncWithSameAsLink(String uri) {
         try {
             syncAndSaveDocument(navigationContext.getChangeableDocument(), URI.create(uri), true);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.syncingWithRemoteEntity"));
         }
     }
 
     protected void syncAndSaveDocument(DocumentModel doc, URI uri, boolean fullSync) throws Exception,
                                                                                     DereferencingException,
                                                                                     ClientException {
         RemoteEntityService remoteEntityService = Framework.getService(RemoteEntityService.class);
         if (remoteEntityService.canDereference(uri)) {
             remoteEntityService.dereferenceInto(doc, uri, fullSync);
         }
         doc = documentManager.saveDocument(doc);
         documentManager.save();
         notifyDocumentUpdated(doc);
     }
 
     public void removeSameAsLink(String uri) {
         try {
             DocumentModel doc = navigationContext.getChangeableDocument();
             RemoteEntityService remoteEntityService = Framework.getService(RemoteEntityService.class);
             remoteEntityService.removeSameAsLink(doc, URI.create(uri));
             doc = documentManager.saveDocument(doc);
             documentManager.save();
             notifyDocumentUpdated(doc);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR, messages.get("error.unlinkingRemoteEntity"));
         }
         Contexts.removeFromAllContexts("currentEntitySameAs");
     }
 
     @Observer(value = EventNames.USER_ALL_DOCUMENT_TYPES_SELECTION_CHANGED, create = false)
     public void onDocumentNavigation() {
         selectedDocumentId = null;
         selectedEntitySuggestion = null;
         isRemoteEntitySearchDisplayed = false;
         invalidateCurrentDocumentProviders();
     }
 
     public void invalidateCurrentDocumentProviders() {
         Contexts.removeFromAllContexts("entityOccurrenceProvider");
         Contexts.removeFromAllContexts("relatedPlacesProvider");
         Contexts.removeFromAllContexts("relatedPeopleProvider");
         Contexts.removeFromAllContexts("relatedOrganizationsProvider");
     }
 
     protected void notifyDocumentUpdated(DocumentModel doc) throws ClientException {
         navigationContext.invalidateCurrentDocument();
         facesMessages.add(StatusMessage.Severity.INFO, messages.get("document_modified"),
             messages.get(doc.getType()));
         EventManager.raiseEventsOnDocumentChange(doc);
     }
 
     // TODO: move this to a JSF function
     public String ellipsis(String content, int maxSize) {
         if (content == null) {
             return "";
         } else if (content.length() > maxSize) {
             return content.substring(0, maxSize) + "[...]";
         }
         return content;
     }
 }
