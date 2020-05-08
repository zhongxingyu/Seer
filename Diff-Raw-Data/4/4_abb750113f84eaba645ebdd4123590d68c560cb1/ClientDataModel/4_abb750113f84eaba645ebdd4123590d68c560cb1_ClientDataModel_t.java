 package org.iplantc.de.client.models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.models.FolderData;
 import org.iplantc.core.uidiskresource.client.util.DiskResourceUtil;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.services.DiskResourceServiceFacade;
 
 import com.extjs.gxt.ui.client.data.BaseTreeLoader;
 import com.extjs.gxt.ui.client.data.LoadEvent;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.data.TreeLoadEvent;
 import com.extjs.gxt.ui.client.data.TreeModel;
 import com.extjs.gxt.ui.client.event.LoadListener;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class ClientDataModel {
     private TreeStore<Folder> heirarchy;
     private FolderData page;
     private Folder folderRoot;
     private AsyncCallback<List<Folder>> folderLoadCallback;
 
     /**
      * Default constructor.
      */
     public ClientDataModel() {
         initHeirarchy();
     }
 
     private void initHeirarchy() {
         // create a custom RPC Folder loader for the TreeStore heirarchy
         final DiskResourceServiceFacade service = new DiskResourceServiceFacade();
         RpcProxy<List<Folder>> proxy = new RpcProxy<List<Folder>>() {
             @Override
             protected void load(Object loadConfig, final AsyncCallback<List<Folder>> callback) {
                 final Folder parentFolder = (Folder)loadConfig;
 
                 // load the folder contents of the given folder, without the files
                 service.getFolderContents(parentFolder.getId(), false, new AsyncCallback<String>() {
                     @Override
                     public void onSuccess(String result) {
                         // build a folder list to pass to the callback given to the proxy's load method.
                         List<Folder> folderList = new ArrayList<Folder>();
 
                         JSONArray folders = JsonUtil.getArray(JsonUtil.getObject(result), "folders"); //$NON-NLS-1$
 
                         if (folders != null) {
                             for (int i = 0,listSize = folders.size(); i < listSize; i++) {
                                 folderList.add(new Folder(JsonUtil.getObjectAt(folders, i)));
                             }
                         }
 
                         // Update the parent folder's hasSubDirs flag.
                         parentFolder.setHasSubFolders(!folderList.isEmpty());
                         heirarchy.update(parentFolder);
 
                         // pass the list to the proxy's callback
                         callback.onSuccess(folderList);
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(I18N.ERROR.retrieveFolderInfoFailed(), caught);
 
                         callback.onFailure(caught);
                     }
                 });
             }
         };
 
         // create a loader with the Folder RPC proxy
         BaseTreeLoader<Folder> loader = new BaseTreeLoader<Folder>(proxy) {
             @Override
             protected void loadData(final Object config) {
                 if (folderLoadCallback != null && proxy != null) {
                     // also notify the configurable folder load callback when the proxy loads
                     AsyncCallback<List<Folder>> callback = new AsyncCallback<List<Folder>>() {
                         @Override
                         public void onFailure(Throwable caught) {
                             onLoadFailure(config, caught);
 
                             if (folderLoadCallback != null) {
                                 folderLoadCallback.onFailure(caught);
                             }
                         }
 
                         @Override
                         public void onSuccess(List<Folder> result) {
                             onLoadSuccess(config, result);
 
                             if (folderLoadCallback != null) {
                                 folderLoadCallback.onSuccess(result);
                             }
                         }
                     };
 
                     proxy.load(reader, config, callback);
                 } else {
                     // no folder load callback (or proxy) is configured, so fallback to super's loadData.
                     super.loadData(config);
                 }
             }
 
             @Override
             public boolean hasChildren(Folder parent) {
                 return parent != null && parent.hasSubFolders();
             }
         };
 
         loader.addLoadListener(new LoadListener() {
             @Override
             public void loaderLoad(LoadEvent le) {
                 if (!(le instanceof TreeLoadEvent)) {
                     return;
                 }
 
                 TreeLoadEvent tle = (TreeLoadEvent)le;
                 if ((tle.parent instanceof Folder)) {
                     // Mark the folder as having been remotely loaded.
                     ((Folder)tle.parent).setRemotelyLoaded(true);
                 }
             }
         });
         // create the heirarchy with the Folder loader
         heirarchy = new TreeStore<Folder>(loader);
     }
 
     private void addRootFolder(JSONObject json) {
         Folder folder = new Folder(json);
         heirarchy.add(folder, true);
         // /compare user name to folder name to determine user's home folder
         if (folder.getName().equals(UserInfo.getInstance().getUsername())) {
             folderRoot = folder;
         }
     }
 
     /**
      * Rebuild from a JSON string.
      * 
      * @param json string containing the folder/file hierarchy.
      */
     public void seed(final String json) {
         if (json != null) {
             JSONObject obj = JsonUtil.getObject(json);
             if (JsonUtil.getString(obj, "status").equals("success")) { //$NON-NLS-1$ //$NON-NLS-2$
                 parseRoots(obj);
             }
 
         }
     }
 
     private void parseRoots(JSONObject obj) {
         JSONObject root;
         JSONArray items = JsonUtil.getArray(obj, "roots"); //$NON-NLS-1$
         heirarchy.removeAll();
         if (items != null) {
             for (int i = 0; i < items.size(); i++) {
                 root = JsonUtil.getObjectAt(items, i);
                 if (root != null) {
                     addRootFolder(root);
                 }
             }
         }
 
     }
 
     /**
      * Retrieve the root folder.
      * 
      * @return the root folder
      */
     public Folder getRootFolder() {
         return folderRoot;
     }
 
     /**
      * Get the id of the root folder.
      * 
      * @return id of the root folder
      */
     public String getRootFolderId() {
         return (folderRoot == null) ? null : folderRoot.getId();
     }
 
     private void addFolderToPage(final String idParentFolder, Folder folder) {
         if (page != null) {
             if (idParentFolder.equals(page.getPath())) {
                 page.addDiskResource(folder);
             }
         }
     }
 
     /**
      * Create a folder in our tree store.
      * 
      * @param pathParent location to create the new folder.
      * @param path unique id of folder to create.
      * @param name name of new folder.
      * @return newly created folder.
      */
     public Folder createFolder(final String pathParent, JSONObject jsonFolder) {
         Folder ret = null; // assume failure
         Folder folderParent = getFolder(pathParent);
 
         if (folderParent != null) {
             ret = new Folder(jsonFolder);
             ret.setParent(folderParent);
 
             folderParent.add(ret);
             folderParent.setHasSubFolders(true);
 
             heirarchy.add(folderParent, ret, true);
 
             heirarchy.update(folderParent);
             addFolderToPage(pathParent, ret);
         }
 
         return ret;
     }
 
     private void updateSubtreeIds(final List<Folder> subfolders, final String pathOrig,
             final String pathNew) {
         String pathCurrent;
         int lenOrigPath = pathOrig.length();
 
         for (Folder folder : subfolders) {
             pathCurrent = folder.getId();
             folder.setId(pathNew + pathCurrent.substring(lenOrigPath));
             heirarchy.update(folder);
         }
     }
 
     private void updateFilePaths(String pathNew) {
         for (DiskResource dr : page.getResources()) {
             if (dr instanceof File) {
                 ((File)dr).setPath(pathNew);
                 dr.setId(pathNew + "/" + dr.getName());
             }
         }
     }
 
     public Folder getFolder(final String path) {
         return heirarchy.findModel("id", path); //$NON-NLS-1$
     }
 
     public Folder renameFolder(final String pathOrig, final String pathNew) {
         Folder ret = getFolder(pathOrig);
 
         if (ret != null) {
             List<Folder> subfolders = heirarchy.getChildren(ret, true);
 
             ret.setId(pathNew);
             ret.setName(DiskResourceUtil.parseNameFromPath(pathNew));
 
             heirarchy.update(ret);
 
             // Check if the currently viewed folder was renamed.
             if (isCurrentPage(pathOrig)) {
                 page.setPath(pathNew);
                updateFilePaths(pathNew);
             }

             updateSubtreeIds(subfolders, pathOrig, pathNew);
         }
 
         return ret;
     }
 
     /**
      * Create a file in our tree store.
      * 
      * @param idParentFolder location to create the file.
      * @param file file to be created.
      */
     public void createFile(final String idParentFolder, final File file) {
         if (isCurrentPage(idParentFolder)) {
             // first remove any potentially outdated resource models
             deleteFolderDataResource(file.getId());
 
             page.addDiskResource(file);
         }
     }
 
     /**
      * Move a disk resource from one location to another
      * 
      * @param src resource to be moved
      * @param newSrcId new id for the resource
      * @param destId id of the destination
      */
     public void moveResource(DiskResource src, String newSrcId, String destId) {
         if (src != null) {
             if (src instanceof Folder) {
                 moveFolder((Folder)src, newSrcId, destId);
             }
         }
     }
 
     private void moveFolder(Folder src, String newSrcId, String destId) {
         String srcId = src.getId();
         Folder dest = getFolder(destId);
         if (dest == null) {
             return;
         }
 
         TreeModel srcParent = src.getParent();
         if (srcParent != null) {
             srcParent.remove(src);
         }
 
         dest.add(renameFolder(srcId, newSrcId));
         dest.setHasSubFolders(true);
 
         heirarchy.remove(src);
         heirarchy.add(dest, src, true);
 
         updateHasSubFolders(getFolder(DiskResourceUtil.parseParent(srcId)));
     }
 
     private void addFoldersToPage(final JSONObject objJson) {
         JSONArray folders = JsonUtil.getArray(objJson, "folders"); //$NON-NLS-1$
 
         for (int i = 0,len = folders.size(); i < len; i++) {
             page.addDiskResource(new Folder(JsonUtil.getObjectAt(folders, i)));
         }
     }
 
     private void addFilesToPage(final JSONObject objJson) {
         JSONArray files = JsonUtil.getArray(objJson, "files"); //$NON-NLS-1$
 
         if (files != null) {
             for (int i = 0,len = files.size(); i < len; i++) {
                 page.addDiskResource(new File(JsonUtil.getObjectAt(files, i)));
             }
         }
     }
 
     public void updatePage(final String json) {
         JSONObject objJson = JsonUtil.getObject(json);
 
         // could we build an object from the json string passed in?
         if (objJson != null) {
             String id = JsonUtil.getString(objJson, "id"); //$NON-NLS-1$
             String name = JsonUtil.getString(objJson, "label"); //$NON-NLS-1$
 
             // allocate our page
             page = new FolderData(id, name);
 
             addFoldersToPage(objJson);
             addFilesToPage(objJson);
         }
     }
 
     public String getCurrentPath() {
         return (page == null) ? null : page.getPath();
     }
 
     public boolean isCurrentPage(final String path) {
         boolean ret = false; // assume failure
 
         if (path != null && page != null) {
             ret = path.equals(page.getPath());
         }
 
         return ret;
     }
 
     /**
      * Removes the disk resources with the given paths from the heirarchy and the page.
      * 
      * @param paths
      */
     public void deleteDiskResources(final List<String> paths) {
         if (paths != null) {
             for (String path : paths) {
                 deleteDiskResource(path);
             }
         }
     }
 
     /**
      * Removes the disk resource with the given path from the heirarchy and the page.
      * 
      * @param path
      */
     public void deleteDiskResource(String path) {
         deleteFolderDataResource(path);
 
         Folder folder = getFolder(path);
 
         if (folder != null) {
             TreeModel parent = folder.getParent();
             if (parent != null) {
                 parent.remove(folder);
             }
 
             heirarchy.remove(folder);
             updateHasSubFolders(getFolder(DiskResourceUtil.parseParent(folder.getId())));
         }
     }
 
     /**
      * Removes the disk resource with the given path from the page.
      * 
      * @param path
      */
     private void deleteFolderDataResource(String path) {
         DiskResource remove = null;
 
         if (page != null) {
             for (DiskResource model : page.getResources()) {
                 if (model.getId().equals(path)) {
                     remove = model;
                     break;
                 }
             }
             page.removeDiskResource(remove);
         }
     }
 
     private void updateHasSubFolders(Folder parent) {
         if (parent != null) {
             parent.setHasSubFolders(heirarchy.hasChildren(parent));
             heirarchy.update(parent);
         }
     }
 
     public FolderData getPage() {
         return page;
     }
 
     public TreeStore<Folder> getHeirarchy() {
         return heirarchy;
     }
 
     /**
      * Configures an additional callback for the heirarchy's Folder loader.
      * 
      * @param callback
      */
     public void setTreeLoaderCallback(AsyncCallback<List<Folder>> callback) {
         folderLoadCallback = callback;
     }
 }
