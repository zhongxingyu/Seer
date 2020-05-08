 package org.iplantc.core.uidiskresource.client.presenters.handlers;
 
 import java.util.Collection;
 import java.util.Set;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.events.diskresources.DiskResourceRefreshEvent;
 import org.iplantc.core.uicommons.client.events.diskresources.DiskResourceRefreshEvent.DiskResourceRefreshEventHandler;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.info.SuccessAnnouncementConfig;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceRenamedEvent.DiskResourceRenamedEventHandler;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceSelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourceSelectedEvent.DiskResourceSelectedEventHandler;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesDeletedEvent.DiskResourcesDeletedEventHandler;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesMovedEvent;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesMovedEvent.DiskResourcesMovedEventHandler;
 import org.iplantc.core.uidiskresource.client.events.FolderCreatedEvent.FolderCreatedEventHandler;
 import org.iplantc.core.uidiskresource.client.events.ShowFilePreviewEvent;
 import org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 
 public final class DiskResourcesEventHandler implements DiskResourcesDeletedEventHandler,
         DiskResourceSelectedEventHandler, DiskResourcesMovedEventHandler,
         DiskResourceRenamedEventHandler, FolderCreatedEventHandler, DiskResourceRefreshEventHandler {
     private final DiskResourceView.Presenter presenter;
     private final DiskResourceView view;
     private final DataSearchPresenter searchPresenter;
 
     public DiskResourcesEventHandler(DiskResourceView.Presenter presenter, DataSearchPresenter searchPresenter) {
         this.presenter = presenter;
         this.view = presenter.getView();
         this.searchPresenter = searchPresenter;
     }
 
     @Override
     public void onRefresh(DiskResourceRefreshEvent event) {
        //if no folder is selected in left nav and refresh is enabled...then execute search
        if(presenter.getSelectedFolder() == null) {
            searchPresenter.fireActiveQuery();
            return;
        }
         presenter.refreshFolder(event.getCurrentFolderId(), event.getResources());
     }
 
     @Override
     public void onDiskResourcesDeleted(Collection<DiskResource> resources, Folder parentFolder) {
         //re-load center grid with search results
         if(presenter.getSelectedFolder() == null) {
             searchPresenter.fireActiveQuery();
             return;
         }
         
         presenter.doRefresh(parentFolder);
     }
 
     @Override
     public void onSelect(DiskResourceSelectedEvent event) {
         if (event.getSource() != view) {
             return;
         }
 
         if (event.getSelectedItem() instanceof Folder) {
             presenter.setSelectedFolderById(event.getSelectedItem());
         } else if (event.getSelectedItem() instanceof File) {
             EventBus.getInstance().fireEvent(
                     new ShowFilePreviewEvent((File)event.getSelectedItem(), this));
         }
     }
 
     @Override
     public void onDiskResourcesMoved(DiskResourcesMovedEvent event) {
         
         //re-load center grid with search results
         if(presenter.getSelectedFolder() == null) {
             searchPresenter.fireActiveQuery();
             return;
         }
         
         Set<DiskResource> resourcesToMove = event.getResourcesToMove();
         Folder destinationFolder = event.getDestinationFolder();
         Folder selectedFolder = presenter.getSelectedFolder();
         // moved contents only not the folder itself
         if (event.isMoveContents()) {
             presenter.doRefresh(destinationFolder);
             presenter.doRefresh(selectedFolder);
         } else {
 
             if (resourcesToMove.contains(selectedFolder)) {
                 selectedFolderMovedFromNavTree(selectedFolder, destinationFolder);
             } else {
                 diskResourcesMovedFromGrid(resourcesToMove, selectedFolder, destinationFolder);
             }
         }
         IplantAnnouncer.getInstance().schedule(
                 new SuccessAnnouncementConfig("Selected item(s) moved to " + destinationFolder.getId()));
     }
 
     private void selectedFolderMovedFromNavTree(Folder selectedFolder, Folder destinationFolder) {
         // If the selected folder happens to be one of the moved items, then view the destination by
         // setting it as the selected folder.
         Folder parentFolder = view.getParentFolder(selectedFolder);
 
         if (DiskResourceUtil.isDescendantOfFolder(parentFolder, destinationFolder)) {
             // The destination is under the parent, so if we prune the parent and set the destination
             // as the selected folder, the parent will lazy-load down to the destination.
             view.removeChildren(parentFolder);
         } else if (DiskResourceUtil.isDescendantOfFolder(destinationFolder, parentFolder)) {
             // The parent is under the destination, so we only need to view the destination folder's
             // contents and refresh its children.
             presenter.doRefresh(destinationFolder);
         } else {
             // Refresh the parent folder since it has lost a child.
             presenter.doRefresh(parentFolder);
             // Refresh the destination folder since it has gained a child.
             presenter.doRefresh(destinationFolder);
         }
 
         // View the destination folder's contents.
         presenter.setSelectedFolderById(destinationFolder);
     }
 
     private void diskResourcesMovedFromGrid(Set<DiskResource> resourcesToMove, Folder selectedFolder,
             Folder destinationFolder) {
         if (DiskResourceUtil.containsFolder(resourcesToMove)) {
             // Refresh the destination folder, since it has gained a child.
             if (DiskResourceUtil.isDescendantOfFolder(destinationFolder, selectedFolder)) {
                 view.removeChildren(destinationFolder);
             } else {
                 // Refresh the selected folder since it has lost a child. This will also reload the
                 // selected folder's contents in the grid.
                 presenter.doRefresh(selectedFolder);
                 // Refresh the destination folder since it has gained a child.
                 presenter.doRefresh(destinationFolder);
                 return;
             }
         }
 
         // Refresh the selected folder's contents.
         presenter.setSelectedFolderById(selectedFolder);
     }
 
     @Override
     public void onRename(DiskResource originalDr, DiskResource newDr) {
         //re-load center grid with search results
         if(presenter.getSelectedFolder() == null) {
             searchPresenter.fireActiveQuery();
             return;
         }
         
         Folder parent = view.getFolderById(DiskResourceUtil.parseParent(newDr.getPath()));
         if (parent != null) {
             presenter.doRefresh(parent);
         }
     }
 
     @Override
     public void onFolderCreated(Folder parentFolder, Folder newFolder) {
         view.addFolder(parentFolder, newFolder);
     }
 
 }
