 package org.iplantc.core.uidiskresource.client.search.presenter.impl;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasName;
 import com.google.inject.Inject;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 import com.sencha.gxt.data.shared.TreeStore;
 
 import org.iplantc.core.uicommons.client.info.ErrorAnnouncementConfig;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.info.SuccessAnnouncementConfig;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.models.search.DiskResourceQueryTemplate;
 import org.iplantc.core.uicommons.client.models.search.SearchAutoBeanFactory;
 import org.iplantc.core.uicommons.client.services.SearchServiceFacade;
 import org.iplantc.core.uidiskresource.client.events.FolderSelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.FolderSelectedEvent.FolderSelectedEventHandler;
 import org.iplantc.core.uidiskresource.client.events.FolderSelectedEvent.HasFolderSelectedEventHandlers;
 import org.iplantc.core.uidiskresource.client.search.events.DeleteSavedSearchEvent;
 import org.iplantc.core.uidiskresource.client.search.events.DeleteSavedSearchEvent.HasDeleteSavedSearchEventHandlers;
 import org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent;
 import org.iplantc.core.uidiskresource.client.search.events.SubmitDiskResourceQueryEvent;
 import org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter;
 import org.iplantc.core.uidiskresource.client.search.views.DiskResourceSearchField;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class DataSearchPresenterImpl implements DataSearchPresenter {
 
     List<DiskResourceQueryTemplate> cleanCopyQueryTemplates = Lists.newArrayList();
     final List<DiskResourceQueryTemplate> queryTemplates = Lists.newArrayList();
     DiskResourceSearchField searchField;
     TreeStore<Folder> treeStore;
     private DiskResourceQueryTemplate activeQuery = null;
     private final IplantAnnouncer announcer;
     private HandlerManager handlerManager;
     private final SearchServiceFacade searchService;
     private final SearchAutoBeanFactory factory;
     
     @Inject
     public DataSearchPresenterImpl(final SearchServiceFacade searchService, final IplantAnnouncer announcer, final SearchAutoBeanFactory factory) {
         this.searchService = searchService;
         this.announcer = announcer;
         this.factory = factory;
     }
 
     @Override
     public HandlerRegistration addFolderSelectedEventHandler(FolderSelectedEventHandler handler) {
         return ensureHandlers().addHandler(FolderSelectedEvent.TYPE, handler);
     }
 
     /**
      * This handler is responsible for saving or updating the {@link DiskResourceQueryTemplate} contained
      * in the given {@link SaveDiskResourceQueryEvent}.
      * <p/>
      * After the query has been successfully saved, a search with the given querytemplate will be
      * performed.
      */
     @Override
     public void doSaveDiskResourceQueryTemplate(final SaveDiskResourceQueryEvent event) {
         // Assume that once the filter is saved, a search should be performed.
         final DiskResourceQueryTemplate queryTemplate = event.getQueryTemplate();
 
         if (Strings.isNullOrEmpty(queryTemplate.getName())) {
             // Given query template has no name, ripple error back to view
 
             // TODO Ripple error back to view
             GWT.log("TODO: User tried to save query with no name, cannot save. Ripple error back to view");
             return;
         } else {
             // Check for name uniqueness
             final Set<String> uniqueNames = getUniqueNames(getQueryTemplates());
             if (uniqueNames.size() == getQueryTemplates().size()) {
                 // Sanity check: There were no dupes in the current list
                 if (uniqueNames.contains(queryTemplate.getName())) {
                     /*
                      * The given query template is already in the list, remove it. The new one will be
                      * added to the list submitted to the service.
                      */
                     for (DiskResourceQueryTemplate hasId : ImmutableList.copyOf(getQueryTemplates())) {
                         String inListName = hasId.getName();
                         if (queryTemplate.getName().equalsIgnoreCase(inListName)) {
                             getQueryTemplates().remove(hasId);
 
                             break;
                         }
                     }
                 }
             }
         }
 
         final ImmutableList<DiskResourceQueryTemplate> toBeSaved = ImmutableList.copyOf(Iterables.concat(queryTemplates, Collections.singletonList(queryTemplate)));
         // Call service to save template
         searchService.saveQueryTemplates(toBeSaved, new AsyncCallback<Boolean>() {
 
             @Override
             public void onFailure(Throwable caught) {
                announcer.schedule(new ErrorAnnouncementConfig("Unable to save filter."));
             }
 
             @Override
             public void onSuccess(Boolean result) {
                 // Clear list of saved query templates and re-add result.
                 queryTemplates.clear();
                 for (DiskResourceQueryTemplate qt : toBeSaved) {
                     // Make sure all saved templates are set as saved.
                     final Splittable encode = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(qt));
                     StringQuoter.create(true).assign(encode, "saved");
                     final DiskResourceQueryTemplate as = AutoBeanCodex.decode(factory, DiskResourceQueryTemplate.class, encode).as();
                     queryTemplates.add(as);
                 }
 
                 /*
                  * Determine if there has been a name change, if so, remove the original from the
                  * treestore.
                  */
                 for (DiskResourceQueryTemplate qt : cleanCopyQueryTemplates) {
                     if (qt.getName().equals(event.getOriginalName())) {
                         treeStore.remove(qt);
                     }
                 }
                 // Create immutable copy of saved templates
                 setCleanCopyQueryTemplates(searchService.createFrozenList(toBeSaved));
 
                 // Call our method to perform search with saved template
                 doSubmitDiskResourceQuery(new SubmitDiskResourceQueryEvent(queryTemplate));
             }
         });
 
     }
 
     /**
      * This handler is responsible for submitting a search with the {@link DiskResourceQueryTemplate}
      * contained in the given {@link SubmitDiskResourceQueryEvent}.
      * <p/>
      * Additionally, this method also ensures that this presenter's query template collection is also maintained in the
      * {@link DiskResourceView#getTreeStore()}, and is responsible for setting the current "active query".
      */
     @Override
     public void doSubmitDiskResourceQuery(final SubmitDiskResourceQueryEvent event) {
         DiskResourceQueryTemplate toSubmit = event.getQueryTemplate();
 
         List<DiskResourceQueryTemplate> toUpdate;
         // If we don't have the query in our collection
         final boolean isNewQuery = Strings.isNullOrEmpty(toSubmit.getName());
         if (isNewQuery) {
             toUpdate = Lists.newArrayList(getQueryTemplates());
         } else {
             toUpdate = Lists.newArrayList();
             // If it is an existing query, determine if it is dirty. If so, set dirty flag
             if (templateHasChanged(toSubmit, cleanCopyQueryTemplates)) {
                 toSubmit.setDirty(true);
                 // Replace existing object in current template list
                 for (DiskResourceQueryTemplate qt : getQueryTemplates()) {
                     if (qt.getName().equalsIgnoreCase(toSubmit.getName())) {
                         toUpdate.add(toSubmit);
                     } else {
                         toUpdate.add(qt);
                     }
                 }
                 getQueryTemplates().clear();
                 getQueryTemplates().addAll(toUpdate);
             } else {
                 toUpdate = Lists.newArrayList(getQueryTemplates());
             }
         }
 
         // Performing a search has the effect of setting the given query as the current active query.
         updateDataNavigationWindow(toUpdate, treeStore);
 
         activeQuery = toSubmit;
         fireEvent(new FolderSelectedEvent(activeQuery));
      
     }
 
     @Override
     public DiskResourceQueryTemplate getActiveQuery() {
         return activeQuery;
     }
     
     @Override
     public void loadSavedQueries(List<DiskResourceQueryTemplate> savedQueries) {
         setCleanCopyQueryTemplates(searchService.createFrozenList(savedQueries));
 
         queryTemplates.clear();
         queryTemplates.addAll(savedQueries);
 
         // Update navigation window
         updateDataNavigationWindow(queryTemplates, treeStore);
     }
 
     @Override
     public void onDeleteSavedSearch(DeleteSavedSearchEvent event) {
         searchField.clearSearch();
         final DiskResourceQueryTemplate savedSearch = event.getSavedSearch();
         if (treeStore.remove(savedSearch)) {
             if (queryTemplates.remove(savedSearch)) {
                announcer.schedule(new SuccessAnnouncementConfig("Deleted saved filter: " + savedSearch.getName()));
                 searchService.saveQueryTemplates(queryTemplates, new AsyncCallback<Boolean>() {
 
                     @Override
                     public void onFailure(Throwable caught) {
                        announcer.schedule(new ErrorAnnouncementConfig("Unable to save filter."));
                     }
 
                     @Override
                     public void onSuccess(Boolean result) {
                         if (!result) {
                             GWT.log("Failed to save query templates after delete of saved search");
                         }
                     }
                 });
             } else {
                 GWT.log("Failed to remove saved search from presenter");
             }
         } else {
             GWT.log("Failed to remove saved search from treeStore.");
         }
     }
 
     @Override
     public void onFolderSelected(FolderSelectedEvent event) {
         if (event.getSelectedFolder() instanceof DiskResourceQueryTemplate) {
             final DiskResourceQueryTemplate selectedQuery = (DiskResourceQueryTemplate)event.getSelectedFolder();
             searchField.edit(selectedQuery);
         } else {
             // Clear search form
             searchField.clearSearch();
         }
     }
 
     @Override
     public void searchInit(final HasFolderSelectedEventHandlers hasFolderSelectedHandlers, final HasDeleteSavedSearchEventHandlers hasDeleteSavedSearchEventHandlers,
             final FolderSelectedEventHandler folderSelectedEventHandler, final TreeStore<Folder> treeStore, final DiskResourceSearchField searchField) {
         hasFolderSelectedHandlers.addFolderSelectedEventHandler(this);
         hasDeleteSavedSearchEventHandlers.addDeleteSavedSearchEventHandler(this);
         // Add handler which will listen to our FolderSelectedEvents
         addFolderSelectedEventHandler(folderSelectedEventHandler);
         this.treeStore = treeStore;
         this.searchField = searchField;
         searchField.addSaveDiskResourceQueryEventHandler(this);
         searchField.addSubmitDiskResourceQueryEventHandler(this);
     }
 
     boolean areTemplatesEqual(DiskResourceQueryTemplate lhs, DiskResourceQueryTemplate rhs) {
         final AutoBean<DiskResourceQueryTemplate> lhsAb = AutoBeanUtils.getAutoBean(lhs);
         final AutoBean<DiskResourceQueryTemplate> rhsAb = AutoBeanUtils.getAutoBean(rhs);
 
         final boolean deepEquals = AutoBeanUtils.deepEquals(lhsAb, rhsAb);
         return deepEquals;
     }
 
     HandlerManager createHandlerManager() {
         return new HandlerManager(this);
     }
 
     HandlerManager ensureHandlers() {
         return handlerManager == null ? handlerManager = createHandlerManager() : handlerManager;
     }
 
 
     void fireEvent(GwtEvent<?> event) {
         if (handlerManager != null) {
             handlerManager.fireEvent(event);
         }
     }
 
     HandlerManager getHandlerManager() {
         return handlerManager;
     }
 
     List<DiskResourceQueryTemplate> getQueryTemplates() {
         return queryTemplates;
     }
 
     Set<String> getUniqueNames(List<DiskResourceQueryTemplate> hasNames) {
         final HashSet<String> queryNameSet = Sets.newHashSet();
         for (HasName hasName : hasNames) {
             if (queryNameSet.contains(hasName.getName())) {
                 // We have a dupe name!!
                 GWT.log("Duplicate QueryTemplate name found: " + hasName.getName());
                 // TODO Determine what to do when dupe name is found. Currently it is ommitted silently.
             } else {
                 queryNameSet.add(hasName.getName());
             }
         }
 
         return queryNameSet;
     }
 
     void setCleanCopyQueryTemplates(List<DiskResourceQueryTemplate> cleanCopyQueryTemplates) {
         this.cleanCopyQueryTemplates = cleanCopyQueryTemplates;
     }
 
     /**
      * Ensures that the navigation window shows the given templates.
      * These show up in the navigation window as "magic folders".
      * <p/>
      * This method ensures that the only the given list of queryTemplates will be displayed in the
      * navigation pane.
      * 
      * 
      * Only objects which are instances of {@link DiskResourceQueryTemplate} will be operated on. Items
      * which can't be found in the tree store will be added, and items which are already in the store and
      * are marked as dirty will be updated.
      * 
      * @param queryTemplates
      * @param treeStore
      */
     void updateDataNavigationWindow(final List<DiskResourceQueryTemplate> queryTemplates, final TreeStore<Folder> treeStore) {
         for (DiskResourceQueryTemplate qt : queryTemplates) {
             // If the item already exists in the store and the template is dirty, update it
             if (treeStore.findModelWithKey(qt.getId()) != null) {
                 if (qt.isDirty()) {
                     treeStore.update(qt);
                 }
             } else {
                 treeStore.add(qt);
             }
         }
     }
 
     private boolean templateHasChanged(DiskResourceQueryTemplate template, List<DiskResourceQueryTemplate> controlList) {
         for (DiskResourceQueryTemplate qt : controlList) {
             if (qt.getName().equalsIgnoreCase(template.getName()) && !areTemplatesEqual(qt, template)) {
                 // Given template has been changed
                 return true;
             }
         }
         return false;
     }
 
 }
