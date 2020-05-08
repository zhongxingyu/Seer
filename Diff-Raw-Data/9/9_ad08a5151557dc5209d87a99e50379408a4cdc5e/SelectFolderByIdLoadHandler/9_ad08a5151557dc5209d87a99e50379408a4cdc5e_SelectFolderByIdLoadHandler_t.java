 package org.iplantc.core.uidiskresource.client.presenters.proxy;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.info.ErrorAnnouncementConfig;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 import org.iplantc.core.uidiskresource.client.views.HasHandlerRegistrationMgmt;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.sencha.gxt.data.shared.loader.LoadEvent;
 import com.sencha.gxt.data.shared.loader.LoadHandler;
 
 /**
  * A <code>LoadHandler</code> which is used to lazily load, expand, and select a desired folder.
  * 
  * 
  * @author jstroot
  * 
  */
 public final class SelectFolderByIdLoadHandler implements LoadHandler<Folder, List<Folder>> {
 
     private final Stack<String> pathsToLoad = new Stack<String>();
     private final LinkedList<String> path;
     private boolean rootsLoaded;
 
     private final HasId folderToSelect;
 
     private final DiskResourceView view;
     private final DiskResourceView.Presenter presenter;
     private final HasHandlerRegistrationMgmt regMgr;
 
     public SelectFolderByIdLoadHandler(final HasId folderToSelect,
             final DiskResourceView.Presenter presenter) {
         presenter.maskView();
         this.folderToSelect = folderToSelect;
         this.presenter = presenter;
         this.regMgr = presenter;
         this.view = presenter.getView();
 
         // Split the string on "/"
         path = Lists.newLinkedList(Splitter.on("/").trimResults().omitEmptyStrings().split(folderToSelect.getId())); //$NON-NLS-1$
 
         rootsLoaded = view.getTreeStore().getAllItemsCount() > 0;
         if (rootsLoaded) {
             initPathsToLoad();
         }
     }
 
     @Override
     public void onLoad(LoadEvent<Folder, List<Folder>> event) {
         if (!rootsLoaded) {
            // Folders must have been loaded to have this method called. Set this flag before calling
            // initPathsToLoad, since it may attempt to load sub-folders, which may not be an async call,
            // which will in turn call this method again before initPathsToLoad returns.
             rootsLoaded = true;
            initPathsToLoad();
             return;
         }
 
         // Exit condition
         if (pathsToLoad.isEmpty()) {
             view.setSelectedFolder(event.getLoadConfig());
             unmaskView();
         } else {
             path.add(pathsToLoad.pop());
             Folder folder = view.getFolderById("/".concat(Joiner.on("/").join(path)));
             if (folder != null) {
                 // Trigger remote load by expanding folder
                 view.expandFolder(folder);
             } else {
                 // This handler has loaded as much as it can, but has encountered a folder along the path
                 // that does not exist. Select the last folder loaded, then report the error.
                 String folderName = SafeHtmlUtils.htmlEscape(path.getLast());
                 SafeHtml errMsg = SafeHtmlUtils.fromTrustedString(I18N.ERROR
                         .diskResourceDoesNotExist(folderName));
                 IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(errMsg));
 
                 view.setSelectedFolder(event.getLoadConfig());
                 unmaskView();
             }
         }
     }
 
     /**
      * Verify if the desired selected folder exists
      * This only needs to occur once.
      */
     private void initPathsToLoad() {
         Folder folder = view.getFolderById(folderToSelect.getId());
         // Find the paths which are not yet loaded, and push them onto the 'pathsToLoad' stack
         while ((folder == null) && !path.isEmpty()) {
             pathsToLoad.push(path.removeLast());
             folder = view.getFolderById("/".concat(Joiner.on("/").join(path))); //$NON-NLS-1$ //$NON-NLS-2$
         }
 
         if (folder != null) {
             if (view.isLoaded(folder)) {
                if (!folder.equals(presenter.getSelectedFolder())) {
                     view.setSelectedFolder(folder);
                 }
                 unmaskView();
             } else {
                 // Once a valid folder is found in the view, remotely load the
                 // folder, which will add the next folder in the path to the view's treeStore.
                 view.expandFolder(folder);
             }
 
         }
         // If no folders could be found in view
         if (path.isEmpty()) {
             unmaskView();
         }
     }
 
     private void unmaskView() {
         regMgr.unregisterHandler(this);
         presenter.unMaskView();
     }
 }
