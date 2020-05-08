 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package com.adito.networkplaces;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.VFS;
 import org.apache.commons.vfs.impl.StandardFileSystemManager;
 import org.apache.commons.vfs.provider.bzip2.Bzip2FileProvider;
 import org.apache.commons.vfs.provider.gzip.GzipFileProvider;
 import org.apache.commons.vfs.provider.jar.JarFileProvider;
 import org.apache.commons.vfs.provider.tar.TarFileProvider;
 import org.apache.commons.vfs.provider.tar.Tbz2FileProvider;
 import org.apache.commons.vfs.provider.tar.TgzFileProvider;
 import org.apache.commons.vfs.provider.temp.TemporaryFileProvider;
 import org.apache.commons.vfs.provider.zip.ZipFileProvider;
import org.apache.commons.vfs.provider.sftp.SftpFileProvider;
 
 import com.adito.agent.DefaultAgentManager;
 import com.adito.boot.ContextHolder;
 import com.adito.core.CoreMenuTree;
 import com.adito.core.CoreServlet;
 import com.adito.core.CoreUtil;
 import com.adito.core.MenuItem;
 import com.adito.core.PageTaskMenuTree;
 import com.adito.extensions.ExtensionException;
 import com.adito.extensions.types.DefaultPlugin;
 import com.adito.navigation.MenuTree;
 import com.adito.navigation.NavigationManager;
 import com.adito.networkplaces.forms.FileSystemForm;
 import com.adito.networkplaces.itemactions.FavoriteOpenWebFolderAction;
 import com.adito.networkplaces.itemactions.OpenWebFolderAction;
 import com.adito.networkplaces.store.cifs.CIFSProvider;
 import com.adito.networkplaces.store.cifs.CIFSStore;
 import com.adito.networkplaces.store.file.FileProvider;
 import com.adito.networkplaces.store.file.FileStore;
 import com.adito.networkplaces.store.ftp.FTPProvider;
 import com.adito.networkplaces.store.ftp.FTPStore;
 import com.adito.networkplaces.store.jar.JarProvider;
 import com.adito.networkplaces.store.jar.JarStore;
 import com.adito.networkplaces.store.tar.TarStore;
 import com.adito.networkplaces.store.webdav.WebDAVStore;
 import com.adito.networkplaces.store.zip.ZipProvider;
 import com.adito.networkplaces.store.zip.ZipStore;
import com.adito.networkplaces.store.sftp.SFTPStore;
import com.adito.networkplaces.store.sftp.SFTPProvider;

 import com.adito.policyframework.Permission;
 import com.adito.policyframework.PolicyConstants;
 import com.adito.policyframework.PolicyDatabase;
 import com.adito.policyframework.PolicyDatabaseFactory;
 import com.adito.policyframework.PolicyUtil;
 import com.adito.policyframework.ResourceType;
 import com.adito.policyframework.itemactions.AddToFavoritesAction;
 import com.adito.policyframework.itemactions.CloneResourceAction;
 import com.adito.policyframework.itemactions.EditResourceAction;
 import com.adito.policyframework.itemactions.RemoveFromFavoritesAction;
 import com.adito.policyframework.itemactions.RemoveResourceAction;
 import com.adito.security.SessionInfo;
 import com.adito.table.TableItemActionMenuTree;
 import com.adito.vfs.VFSProviderManager;
 import com.adito.vfs.utils.UploadHandlerFactory;
 
 
 /**
  * Plugin implementation thats the <i>Network Places</i> feature.
  */
 public class NetworkPlacePlugin extends DefaultPlugin {
 
 	/**
 	 * Network place resource type ID
 	 */
 	public final static int NETWORK_PLACE_RESOURCE_TYPE_ID = 2;
 
 	/**
 	 * Network place resource type
 	 */
 	public final static ResourceType NETWORK_PLACE_RESOURCE_TYPE = new NetworkPlaceResourceType();
 
 	/**
 	 * Extension bundle ID
 	 */
 	public static final String BUNDLE_ID = "adito-community-network-places";
 
 	final static Log log = LogFactory.getLog(NetworkPlacePlugin.class);
 
 	/**
 	 * Message resources key (resource bundle id)
 	 */
 	public static final String MESSAGE_RESOURCES_KEY = "networkPlaces";
 
 	/**
 	 * Constructor.
 	 */
 	public NetworkPlacePlugin() {
 		super("/WEB-INF/adito-community-network-places-tiles-defs.xml", true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see com.adito.plugin.DefaultPlugin#startPlugin()
 	 */
 	public void activatePlugin() throws ExtensionException {
 		super.activatePlugin();
 		try {
 			initDatabase();
 			initPolicyFramework();
 			initTableItemActions();
 			initMainMenu();
 			initPageTasks();
 			initFileSystems();
 			initUploadHandler();
 			initTagLib();
 			initService();
             CoreUtil.updateEventsTable(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, NetworkPlacesEventConstants.class);
 		} catch (Exception e) {
 			throw new ExtensionException(ExtensionException.INTERNAL_ERROR, e, "Failed to start.");
 		}
 	}
 
 	void initDatabase() throws Exception {
 		NetworkPlaceDatabaseFactory.getInstance().open(CoreServlet.getServlet(), this.getPluginDefinition());
 	}
 
 	void initPolicyFramework() throws Exception {
 
 		PolicyDatabase pdb = PolicyDatabaseFactory.getInstance();
 
 		// Network Place
 		pdb.registerResourceType(NETWORK_PLACE_RESOURCE_TYPE);
 		NETWORK_PLACE_RESOURCE_TYPE.addPermission(PolicyConstants.PERM_CREATE_EDIT_AND_ASSIGN);
 		NETWORK_PLACE_RESOURCE_TYPE.addPermission(PolicyConstants.PERM_EDIT_AND_ASSIGN);
 		NETWORK_PLACE_RESOURCE_TYPE.addPermission(PolicyConstants.PERM_DELETE);
 		NETWORK_PLACE_RESOURCE_TYPE.addPermission(PolicyConstants.PERM_ASSIGN);
 		NETWORK_PLACE_RESOURCE_TYPE.addPermission(PolicyConstants.PERM_PERSONAL_CREATE_EDIT_AND_DELETE);
 	}
 
 	void initService() throws InstantiationException, IllegalAccessException {
 		DefaultAgentManager.getInstance().registerService(NetworkPlaceService.class);
 	}
 
 	void initTableItemActions() throws Exception {
 		MenuTree tree = NavigationManager.getMenuTree(TableItemActionMenuTree.MENU_TABLE_ITEM_ACTION_MENU_TREE);
 		tree.addMenuItem("favorites", new FavoriteOpenWebFolderAction());
 
 		// Network Places
 		tree.addMenuItem(null, new MenuItem("networkPlace",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						null,
 						100,
 						false,
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("networkPlace", new AddToFavoritesAction(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY));
 		tree.addMenuItem("networkPlace", new RemoveFromFavoritesAction(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY));
 		tree.addMenuItem("networkPlace", new RemoveResourceAction(SessionInfo.ALL_CONTEXTS,
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY));
 		tree.addMenuItem("networkPlace", new EditResourceAction(SessionInfo.ALL_CONTEXTS,
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY));
 		tree.addMenuItem("networkPlace", new CloneResourceAction(SessionInfo.MANAGEMENT_CONSOLE_CONTEXT,
 			NetworkPlacePlugin.MESSAGE_RESOURCES_KEY));
 		tree.addMenuItem("networkPlace", new OpenWebFolderAction(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY));
 	}
 
 	void initMainMenu() throws Exception {
 		MenuTree tree = NavigationManager.getMenuTree(CoreMenuTree.MENU_ITEM_MENU_TREE);
 
 		tree.addMenuItem("resources", new MenuItem("userNetworkPlaces",
 				        NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"/showUserNetworkPlaces.do",
 						500,
 						true,
 						null,
 						SessionInfo.USER_CONSOLE_CONTEXT,
                         NETWORK_PLACE_RESOURCE_TYPE,
                         new Permission[] { PolicyConstants.PERM_PERSONAL_CREATE_EDIT_AND_DELETE },
                         NETWORK_PLACE_RESOURCE_TYPE));
 		tree.addMenuItem("globalResources", new MenuItem("networkPlaces",
 				        NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"/showNetworkPlaces.do",
 						200,
 						true,
 						null,
 						SessionInfo.MANAGEMENT_CONSOLE_CONTEXT,
 						NETWORK_PLACE_RESOURCE_TYPE,
 						new Permission[] { PolicyConstants.PERM_CREATE_EDIT_AND_ASSIGN,
 							PolicyConstants.PERM_EDIT_AND_ASSIGN,
 							PolicyConstants.PERM_DELETE,
 							PolicyConstants.PERM_ASSIGN },
 						null) {
 			public boolean isAvailable(int checkNavigationContext, SessionInfo info, HttpServletRequest request) {
 				boolean available = super.isAvailable(checkNavigationContext, info, request);
 				if (available) {
 					try {
 						PolicyUtil.checkPermissions(NETWORK_PLACE_RESOURCE_TYPE,
 							new Permission[] { PolicyConstants.PERM_CREATE_EDIT_AND_ASSIGN,
 								PolicyConstants.PERM_EDIT_AND_ASSIGN,
 								PolicyConstants.PERM_DELETE,
 								PolicyConstants.PERM_ASSIGN },
 							request);
 						available = true;
 					} catch (Exception e1) {
 						available = false;
 					}
 				}
 				return available;
 			}
 		});
 	}
 
 	void initPageTasks() throws Exception {
 		MenuTree tree = NavigationManager.getMenuTree(PageTaskMenuTree.PAGE_TASK_MENU_TREE);
 
 		// Networking showNetworkPlaces
 		tree.addMenuItem(null, new MenuItem("showNetworkPlaces", null, null, 100, false, SessionInfo.MANAGEMENT_CONSOLE_CONTEXT));
 		tree.addMenuItem("showNetworkPlaces", new MenuItem("createNetworkPlace",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"/defaultNetworkPlaceDetails.do",
 						100,
 						true,
 						"_self",
 						SessionInfo.MANAGEMENT_CONSOLE_CONTEXT,
 						NETWORK_PLACE_RESOURCE_TYPE,
 						new Permission[] { PolicyConstants.PERM_CREATE_EDIT_AND_ASSIGN }));
 
         // Networking userNetworkPlaces
 		tree.addMenuItem(null, new MenuItem("showUserNetworkPlaces", null, null, 100, false, SessionInfo.USER_CONSOLE_CONTEXT));
 		tree.addMenuItem("showUserNetworkPlaces", new MenuItem("createPersonalNetworkPlace",
 		    			NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 		    			"/defaultNetworkPlaceDetails.do",
 		    			100,
 		    			true,
 		    			"_self",
 		    			SessionInfo.USER_CONSOLE_CONTEXT,
 		    			NETWORK_PLACE_RESOURCE_TYPE,
             			new Permission[] { PolicyConstants.PERM_PERSONAL_CREATE_EDIT_AND_DELETE }));
 
 		// Networking fileSystem
 		tree.addMenuItem(null, new MenuItem("fileSystem", null, null, 100, false, SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new WriteRequiredFileSystemPageTask("createFolder",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"javascript: setActionTarget('showMkDir'); document.forms[0].submit();",
 						100,
 						true,
 						"_self",
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new DeleteRequiredFileSystemPageTask("deleteSelected",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"javascript: setActionTarget('deleteSelected'); document.forms[0].submit();",
 						100,
 						true,
 						"_self",
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new MenuItem("copySelected",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"javascript: setActionTarget('copy'); document.forms[0].submit();",
 						100,
 						true,
 						"_self",
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new DeleteRequiredFileSystemPageTask("cutSelected",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"javascript: setActionTarget('cut'); document.forms[0].submit();",
 						100,
 						true,
 						"_self",
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new WriteRequiredFileSystemPageTask("paste",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"javascript: setActionTarget('confirmPaste'); document.forms[0].submit();",
 						100,
 						true,
 						"_self",
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new MenuItem("zipSelected",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"javascript: setActionTarget('zip'); document.forms[0].submit();",
 						100,
 						true,
 						"_self",
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new MenuItem("home",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						"javascript: setActionTarget('home'); document.forms[0].submit();",
 						100,
 						true,
 						"_self",
 						SessionInfo.ALL_CONTEXTS));
 		tree.addMenuItem("fileSystem", new WriteRequiredFileSystemPageTask("upload", NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, "javascript: setActionTarget('upload'); document.forms[0].submit();", 100, true, "_self", SessionInfo.ALL_CONTEXTS));
 	}
 
 	void initFileSystems() throws FileSystemException {
 		VFSProviderManager mgr = VFSProviderManager.getInstance();
 
 		// Intialise the additional commons vfs providers
 
 		/*
 		((StandardFileSystemManager)VFS.getManager()).addProvider("webdav", new WebdavFileProvider());
 		*/
 		//
 
 		//NOTE: This Code for Old Apache Commons VFS
 		/*
 		((StandardFileSystemManager)VFS.getManager()).addProvider("jar", new JarFileProvider());
 		((StandardFileSystemManager)VFS.getManager()).addProvider("zip", new ZipFileProvider());
 		((StandardFileSystemManager)VFS.getManager()).addProvider("tar", new TarFileProvider());
 		((StandardFileSystemManager)VFS.getManager()).addProvider("tgz", new TgzFileProvider());
 		((StandardFileSystemManager)VFS.getManager()).addProvider("tbz2", new Tbz2FileProvider());
 		((StandardFileSystemManager)VFS.getManager()).addProvider("gz", new GzipFileProvider());
 		((StandardFileSystemManager)VFS.getManager()).addProvider("tmp", new TemporaryFileProvider());
 		((StandardFileSystemManager)VFS.getManager()).addProvider(new String[] { "bzip2", "bz2" }, new Bzip2FileProvider());
 		*/
 
 		//NOTE: This Code for Apache Commons VFS
 		StandardFileSystemManager sfsm = new StandardFileSystemManager();
 		sfsm.addProvider("jar", new JarFileProvider());
 		sfsm.addProvider("zip", new ZipFileProvider());
 		sfsm.addProvider("tar", new TarFileProvider());
 		sfsm.addProvider("tgz", new TgzFileProvider());
 		sfsm.addProvider("tbz2", new Tbz2FileProvider());
 		sfsm.addProvider("gz", new GzipFileProvider());
 		sfsm.addProvider("tmp", new TemporaryFileProvider());
 		sfsm.addProvider(new String[] { "bzip2", "bz2" }, new Bzip2FileProvider());
 
 		mgr.registerProvider(new FileProvider());
 		mgr.registerProvider(new FTPProvider());
 		mgr.registerProvider(new SFTPProvider());//For SFTP Drive Mapping
 		mgr.registerProvider(new CIFSProvider());
 		mgr.registerProvider(new JarProvider());
 		mgr.registerProvider(new ZipProvider());
 
 		/*
 		Don't seem to work as expected.
 
         mgr.registerProvider(new TarProvider());
 		mgr.registerProvider(new WebDAVProvider());
 		mgr.registerProvider(new TgzProvider());
 		mgr.registerProvider(new Tbz2Provider());
 		*/
 	}
 
 	void initUploadHandler() {
 		UploadHandlerFactory.getInstance().addHandler(NetworkPlaceUploadHandler.TYPE_VFS, NetworkPlaceUploadHandler.class);
 	}
 
 	void initTagLib() {
 		ContextHolder.getContext().setResourceAlias("/server/taglibs/vfs", "/WEB-INF/vfs.tld");
 	}
 
     public void stopPlugin() throws ExtensionException {
         super.stopPlugin();
         try {
             stopDatabase();
             removePolicyFramework();
             removeTableItemActions();
             removeMainMenu();
             removePageTasks();
             removeFileSystems();
             removeUploadHandler();
             removeTagLib();
         } catch (Exception e) {
             throw new ExtensionException(ExtensionException.INTERNAL_ERROR, e, "Failed to start.");
         }
     }
 
     private void removeTagLib() {
         ContextHolder.getContext().removeResourceAlias("/server/taglibs/vfs");
     }
 
     private void removeUploadHandler() {
         UploadHandlerFactory.getInstance().removeHandler(NetworkPlaceUploadHandler.TYPE_VFS);
     }
 
     private void removeFileSystems() {
         VFSProviderManager mgr = VFSProviderManager.getInstance();
         mgr.deregisterProvider(FileStore.FILE_SCHEME);
         mgr.deregisterProvider(FTPStore.FTP_SCHEME);
         mgr.deregisterProvider(SFTPStore.SFTP_SCHEME);//For SFTP Drive Mapping
         mgr.deregisterProvider(CIFSStore.CIFS_SCHEME);
         mgr.deregisterProvider(WebDAVStore.WEBDAV_SCHEME);
         mgr.deregisterProvider(JarStore.JAR_SCHEME);
         mgr.deregisterProvider(ZipStore.ZIP_SCHEME);
         mgr.deregisterProvider(TarStore.TAR_SCHEME);
        // mgr.deregisterProvider(TgzStore.TGZ_SCHEME);
         //mgr.deregisterProvider(Tbz2Store.TBZ2_SCHEME);
     }
 
     private void removePageTasks() {
         MenuTree tree = NavigationManager.getMenuTree(PageTaskMenuTree.PAGE_TASK_MENU_TREE);
         tree.removeMenuItem("showNetworkPlaces", "createNetworkPlace");
         tree.removeMenuItem("fileSystem", "createFolder");
         tree.removeMenuItem("fileSystem", "deleteSelected");
         tree.removeMenuItem("fileSystem", "copySelected");
         tree.removeMenuItem("fileSystem", "cutSelected");
         tree.removeMenuItem("fileSystem", "paste");
         tree.removeMenuItem("fileSystem", "zipSelected");
         tree.removeMenuItem("fileSystem", "home");
         tree.removeMenuItem("fileSystem", "upload");
     }
 
     private void removeMainMenu() {
         MenuTree tree = NavigationManager.getMenuTree(CoreMenuTree.MENU_ITEM_MENU_TREE);
         tree.removeMenuItem("resources", "userNetworkPlaces");
         tree.removeMenuItem("globalResources", "networkPlaces");
     }
 
     private void removeTableItemActions() {
         MenuTree tree = NavigationManager.getMenuTree(TableItemActionMenuTree.MENU_TABLE_ITEM_ACTION_MENU_TREE);
         tree.removeMenuItem("favorites", FavoriteOpenWebFolderAction.TABLE_ITEM_ACTION_ID);
         tree.removeMenuItem("networkPlace", AddToFavoritesAction.TABLE_ITEM_ACTION_ID);
         tree.removeMenuItem("networkPlace", RemoveFromFavoritesAction.TABLE_ITEM_ACTION_ID);
         tree.removeMenuItem("networkPlace", RemoveResourceAction.TABLE_ITEM_ACTION_ID);
         tree.removeMenuItem("networkPlace", EditResourceAction.TABLE_ITEM_ACTION_ID);
         tree.removeMenuItem("networkPlace", OpenWebFolderAction.TABLE_ITEM_ACTION_ID);
     }
 
     private void removePolicyFramework() throws Exception {
         PolicyDatabase pdb = PolicyDatabaseFactory.getInstance();
         pdb.deregisterResourceType(NETWORK_PLACE_RESOURCE_TYPE);
     }
 
     private void stopDatabase() throws Exception {
         NetworkPlaceDatabaseFactory.getInstance().close();
     }
 
 
 	class WriteRequiredFileSystemPageTask extends MenuItem {
 		private WriteRequiredFileSystemPageTask(String id, String key, String path, int weight, boolean leaf, String target, int context) {
 			super(id, key, path, weight, leaf, target, context);
 		}
 
 		public boolean isAvailable(int checkNavigationContext, SessionInfo info, HttpServletRequest request) {
 			boolean available = super.isAvailable(checkNavigationContext, info, request);
 			if(available && request.getAttribute("fileSystemForm") != null) {
 				FileSystemForm fsf = (FileSystemForm)request.getAttribute("fileSystemForm");
 				return fsf.getVFSResource() != null && !fsf.isViewOnly() && !fsf.getVFSResource().getMount().isReadOnly();
 			}
 			return available;
 		}
 	}
 
 
 	class DeleteRequiredFileSystemPageTask extends WriteRequiredFileSystemPageTask {
 		private DeleteRequiredFileSystemPageTask(String id, String key, String path, int weight, boolean leaf, String target, int context) {
 			super(id, key, path, weight, leaf, target, context);
 		}
 
 		public boolean isAvailable(int checkNavigationContext, SessionInfo info, HttpServletRequest request) {
 			boolean available = super.isAvailable(checkNavigationContext, info, request);
 			if(available && request.getAttribute("fileSystemForm") != null) {
 				FileSystemForm fsf = (FileSystemForm)request.getAttribute("fileSystemForm");
 				return fsf.getNetworkPlace() != null && !fsf.isViewOnly() && !fsf.getNetworkPlace().isNoDelete();
 			}
 			return available;
 		}
 	}
 }
