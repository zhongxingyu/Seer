 /*
  File: PluginManager.java 
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 package cytoscape.plugin;
 
 import cytoscape.*;
 
 import cytoscape.util.URLUtil;
 import cytoscape.util.ZipUtil;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 
 /**
  * @author skillcoy
  * 
  */
 public class PluginManager {
 	private static PluginManager pluginMgr = null;
 
 	private PluginTracker pluginTracker;
 
 	private static File tempDir;
 	private String cyVersion;
 
 	static {
 		new PluginManager(null);
 	}
 
 	/**
 	 * This is used in testing to isolate each test case.
 	 * DO NOT USE THIS IN CYTOSCAPE RUNTIME CODE
 	 */
 	protected void resetManager() {
 		pluginMgr = null;
 	}
 	
 	
 	/**
 	 * Get the PluginManager object.  
 	 * @return PluginManager
 	 */
 	public static PluginManager getPluginManager() {
 		if (pluginMgr == null) {
 			pluginMgr = new PluginManager(null);
 		}
 		return pluginMgr;
 	}
 	/**
 	 * This should ONLY be used by tests!!
 	 * @param Tracker
 	 * @return
 	 */
 	protected static PluginManager getPluginManager(PluginTracker Tracker) {
 		if (pluginMgr == null) {
 			pluginMgr = new PluginManager(Tracker);
 		}
 		return pluginMgr;
 	}
 	
 	// create plugin manager
 	private PluginManager(PluginTracker Tracker) {
 
 		try {
 			if (Tracker != null) {
 				System.out.println("Setting tracker");
 				pluginTracker = Tracker;
 			} else {
 				System.out.println("Creating tracker");
 				pluginTracker = new PluginTracker(CytoscapeInit.getConfigDirectory(), "track_plugins.xml");
 			}
 			cyVersion = CytoscapeVersion.version;
 			tempDir = new File(CytoscapeInit.getConfigDirectory(), "plugins");
 
 			if (!tempDir.exists()) {
 				tempDir.mkdir();
 			}
 		} catch (java.io.IOException E) {
 			E.printStackTrace(); // TODO do something useful with error
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public File getTempDownloadDirectory() {
 		return tempDir;
 	}
 
 	/**
 	 * Get a list of plugins by status. CURRENT: currently installed INSTALL:
 	 * plugins to be installed DELETE: plugins to be deleted
 	 * 
 	 * @param Status
 	 * @return
 	 */
 	public List<PluginInfo> getPlugins(PluginTracker.PluginStatus Status) {
 		return pluginTracker.getListByStatus(Status);
 	}
 
 
 	/**
 	 * Calls the given url, expects document describing plugins available for
 	 * download
 	 * 
 	 * @param Url
 	 * @return List of PluginInfo objects
 	 */
 	public List<PluginInfo> inquire(String Url) throws ManagerError {
 		List<PluginInfo> Plugins = null;
 
 		try {
 			PluginFileReader Reader = new PluginFileReader(Url);
 			Plugins = Reader.getPlugins();
 		} catch (java.io.IOException E) {
 			throw new ManagerError("Failed to read xml file at " + Url, E);
 		} catch (org.jdom.JDOMException E) {
 			throw new ManagerError(Url
 					+ " did not return correctly formatted xml", E);
 		}
 
 		return Plugins;
 	}
 
 	/**
 	 * Registers a currently installed plugin with tracking object. Only useful
 	 * if the plugin was not installed via the install process.
 	 * 
 	 * @param Plugin
 	 * @param JarFileName
 	 */
 	public void register(CytoscapePlugin Plugin, String JarFileName) {
 		PluginInfo InfoObj;
 		Map<String, List<PluginInfo>> CurrentInstalled = ManagerUtil
 				.sortByClass(getPlugins(PluginTracker.PluginStatus.CURRENT));
 		
 		if (CurrentInstalled.containsKey(Plugin.getClass().getName()) &&
 				Plugin.getPluginInfoObject() == null) {
 			return;
 		}
 		
 		if (Plugin.getPluginInfoObject() == null) {
 			InfoObj = new PluginInfo();
 			InfoObj.setName(Plugin.getClass().getName());
 			InfoObj.setPluginClassName(Plugin.getClass().getName());
 
 			if (JarFileName != null)
 				InfoObj.addFileName(JarFileName);
 		} else {
 			InfoObj = Plugin.getPluginInfoObject();
 			InfoObj.setPluginClassName(Plugin.getClass().getName());
 
 			if (JarFileName != null) {
 				InfoObj.addFileName(JarFileName);
 			}
 		}
 		// I think we can safely assume it's a jar file if it's registering
 		// since only CytoscapePlugin registers and at that point all we know is 
 		// it's a jar
 		InfoObj.setFiletype(PluginInfo.FileType.JAR);
 		pluginTracker.addPlugin(InfoObj, PluginTracker.PluginStatus.CURRENT);
 	}
 
 	/**
 	 * Takes all objects on the "to-install" list and installs them from them
 	 * temporary download directory.
 	 */
 	public void install() throws ManagerError {
 		List<PluginInfo> Plugins = pluginTracker
 				.getListByStatus(PluginTracker.PluginStatus.INSTALL);
 
 		for (PluginInfo CurrentPlugin : Plugins) {
 			String ClassName = null;
 			List<String> FileList = CurrentPlugin.getFileList();
 
 			// TESTING
 			if (FileList.size() > 1) {
 				throw new ManagerError(
 						"Unexpected files in file list for plugin "
 								+ CurrentPlugin.getName());
 			}
 
 			try {
 				switch (CurrentPlugin.getFileType()) {
 				case JAR:
 
 					File InstallFile = new File("plugins"
 							+ System.getProperty("file.separator")
 							+ createFileName(CurrentPlugin));
 					FileInputStream fis = new FileInputStream(FileList.get(0));
 					FileOutputStream fos = new FileOutputStream(InstallFile);
 
 					byte[] buffer = new byte[1];
 
 					while (((fis.read(buffer)) != -1)) {
 						fos.write(buffer);
 					}
 					fis.close();
 					fos.close();
 					
 					CurrentPlugin.addFileName(InstallFile.getAbsolutePath());
 					break;
 
 				case ZIP:
 					InputStream is = ZipUtil.readFile(FileList.get(0), "plugins"
 							+ System.getProperty("file.separator")
 							+ "\\w+\\.jar");
 					if (is != null ) {
 						
 						List<String> UnzippedFiles = ZipUtil.unzip(FileList.get(0));
 						CurrentPlugin.setFileList(UnzippedFiles);
 						is.close();
 					} else {
 						throw new ManagerError(
 								"Zip file "
 										+ CurrentPlugin.getUrl()
 										+ " did not contain a plugin directory with a jar file.\nThis plugin will need to be installed manually.");
 					}
 					break;
 				};
 
 				pluginTracker.addPlugin(CurrentPlugin, PluginTracker.PluginStatus.CURRENT);
 
 			} catch (IOException E) {
 				throw new ManagerError("Failed to install file "
 						+ FileList.get(0), E);
 			} finally { // always remove it.  If it errored in installation we don't want to try it again
 				pluginTracker.removePlugin(CurrentPlugin,
 						PluginTracker.PluginStatus.INSTALL);
 			}
 		}
 	}
 
 	/**
 	 * Marks the given object for deletion the next time Cytoscape is restarted.
 	 * 
 	 * @param Obj
 	 */
 	public void delete(PluginInfo Obj) {
 		pluginTracker.addPlugin(Obj, PluginTracker.PluginStatus.DELETE);
 	}
 
 	/**
 	 * Takes all objects on the "to-delete" list and deletes them
 	 */
 	public void delete() throws ManagerError {
 		String ErrorMsg = "Failed to delete all files for the following plugins:\n";
 		List<String> DeleteFailed = new ArrayList<String>();
 
 		List<PluginInfo> Plugins = pluginTracker
 				.getListByStatus(PluginTracker.PluginStatus.DELETE);
 
 		for (PluginInfo CurrentPlugin : Plugins) {
 			boolean deleteOk = false;
 
 			// shouldn't happen...
 			if (CurrentPlugin.getFileList().size() <= 0) {
 				throw new ManagerError(
 						CurrentPlugin.getName() + " " + CurrentPlugin.getPluginVersion() 
 								+ " does not have a list of files.  Please delete this plugin manually.");
 			}
 
 			// needs the list of all files installed
 			deleteOk = deleteFiles(CurrentPlugin.getFileList());
 
 			pluginTracker.removePlugin(CurrentPlugin,
 					PluginTracker.PluginStatus.DELETE);
 
 			pluginTracker.removePlugin(CurrentPlugin,
 					PluginTracker.PluginStatus.CURRENT);
 
 			
 			if (!deleteOk) {
 				DeleteFailed.add(CurrentPlugin.getName());
 			}
 		}
 
 		// any files that failed to delete should get noted
 		if (DeleteFailed.size() > 0) {
 			for (String Msg : DeleteFailed) {
 				ErrorMsg += ("-" + Msg + "\n");
 			}
 
 			throw new ManagerError(ErrorMsg);
 		}
 	}
 
 	// Need access to this in install too if installation fails
 	private boolean deleteFiles(List<String> Files) {
 		boolean deleteOk = false;
 		for (String FileName : Files) {
 			File ToDelete = new java.io.File(FileName);
 			deleteOk = ToDelete.delete();
 			System.err.println("Deleting " + FileName + " " + deleteOk);
 		}
 		return deleteOk;
 	}
 
 	/**
 	 * Get list of plugins that would update the given plugin.
 	 * 
 	 * @param Plugin
 	 * @return List<PluginInfo>
 	 * @throws ManagerError
 	 */
 	public List<PluginInfo> findUpdates(PluginInfo Plugin) throws ManagerError {
 		List<PluginInfo> UpdatablePlugins = new ArrayList<PluginInfo>();
 		
 		if (Plugin.getProjectUrl() == null || Plugin.getProjectUrl().length() <= 0) {
 			return UpdatablePlugins;
 		}
 		
 		for (PluginInfo New : inquire(Plugin.getProjectUrl())) {
 			if (New.getID().equals(Plugin.getID()) && isUpdatable(Plugin, New)) {
 				UpdatablePlugins.add(New);
 			}
 		}
 		return UpdatablePlugins;
 	}
 
 	/**
 	 * Finds the given version of the new object, sets the old object for
 	 * deletion and downloads new object to temporary directory
 	 * 
 	 * @param Current
 	 *            PluginInfo object currently installed
 	 * @param New
 	 *            PluginInfo object to install
 	 */
 	public void update(PluginInfo Current, PluginInfo New) throws ManagerError {
 		// find new plugin, download, add to install list
 		if (Current.getProjectUrl() == null) {
 			throw new ManagerError(
 					Current.getName()
 							+ " does not have a project url.\nCannot auto-update this plugin.");
 		}
 
 		if (Current.getID().equals(New.getID())
 				&& Current.getProjectUrl().equals(New.getProjectUrl())
 				&& isVersionNew(Current, New)) {
 			download(New);
 			pluginTracker.addPlugin(New, PluginTracker.PluginStatus.INSTALL);
 			pluginTracker.addPlugin(Current, PluginTracker.PluginStatus.DELETE);
 		} else {
 			throw new ManagerError(
 					"Failed to update '"
 							+ Current.getName()
 							+ "', the new plugin did not match what is currently installed\n"
 							+ "or the version was not newer than what is currently installed.");
 		}
 	}
 
 	/**
 	 * Downloads given object to the temporary directory.
 	 * 
 	 * @param Obj
 	 * @return File downloaded
 	 */
 	public File download(PluginInfo Obj) throws ManagerError {
 		File Download = null;
 		String ClassName = null;
 		try {
 			Download = new File(tempDir, createFileName(Obj));
 			URLUtil.download(Obj.getUrl(), Download);
 
 			ClassName = getPluginClass(Download.getAbsolutePath(), Obj.getFileType());
 		} catch (IOException E) {
 			throw new ManagerError("Failed to download file from "
 					+ Obj.getUrl() + " to " + tempDir.getAbsolutePath(), E);
 		}
 
 		if (ClassName != null) {
 			Obj.setPluginClassName(ClassName);
 		} else {
 			Download.delete();
 			ManagerError E =  new ManagerError(
 				Obj.getName()
 						+ " does not define the attribute 'Cytoscape-Plugin' in the jar manifest file.\n"
 						+ "This plugin cannot be auto-installed.  Please install manually or contact the plugin author.");
 			E.printStackTrace();
 			throw E;
 		}
 		
 		Obj.addFileName(Download.getAbsolutePath());
 		pluginTracker.addPlugin(Obj, PluginTracker.PluginStatus.INSTALL);
 
 		return Download;
 	}
 
 	private String createFileName(PluginInfo Obj) {
 		return Obj.getName() + "-" + Obj.getPluginVersion() + "."
 				+ Obj.getFileType().toString();
 	}
 
 	/**
 	 * Checks to see new plugin matches the original plugin and has a newer
 	 * version
 	 */
 	private boolean isUpdatable(PluginInfo Current, PluginInfo New) {
 		boolean hasUpdate = false;
 
 		if ((Current.getID() != null) && (New.getID() != null)) {
 			boolean newVersion = isVersionNew(Current, New);
 
 			if ((Current.getID().trim().equals(New.getID().trim()) && Current
 					.getProjectUrl().equals(New.getProjectUrl()))
 					&& newVersion) {
 				hasUpdate = true;
 			}
 		}
 
 		return hasUpdate;
 	}
 
 	/**
 	 * compares the version numbers. Be sure the plugin info objects are passed
 	 * in order
 	 * 
 	 * @param Current
 	 *            The currently installed PluginInfo object
 	 * @param New
 	 *            The new PluginInfo object to compare to
 	 */
 	private boolean isVersionNew(PluginInfo Current, PluginInfo New) {
 		boolean isNew = false;
 		String[] CurrentVersion = Current.getPluginVersion().split("\\.");
 		String[] NewVersion = New.getPluginVersion().split("\\.");
 
 		for (int i = 0; i < NewVersion.length; i++) {
 			// if we're beyond the end of the current version array then it's a
 			// new version
 			if (CurrentVersion.length <= i) {
 				isNew = true;
 				break;
 			}
 
 			// if at any point the new version number is greater
 			// then it's "new" ie. 1.2.1 > 1.1
 			// whoops...what if they add a character in here?? TODO !!!!
 			if (Integer.valueOf(NewVersion[i]) > Integer
 					.valueOf(CurrentVersion[i]))
 				isNew = true;
 		}
 		return isNew;
 	}
 
 	/*
 	 * Iterate through all class files, return the subclass of CytoscapePlugin.
 	 * Similar to CytoscapeInit, however only plugins with manifest files that
 	 * describe the class of the CytoscapePlugin are valid.
 	 */
 	private String getPluginClass(String FileName, PluginInfo.FileType Type) throws IOException {
 		String PluginClassName = null;
 
 		switch(Type) {
 			case JAR:
 				JarFile Jar = new JarFile(FileName);
 				PluginClassName = getManifestAttribute(Jar.getManifest());
 				Jar.close();
 				break;
 				
 			case ZIP:
 				List<ZipEntry> Entries = ZipUtil.getAllFiles(FileName, ".*plugins/.*\\.jar");
 			
 				for (ZipEntry Entry: Entries) {
 					String EntryName = Entry.getName();
 				
 					if (EntryName.endsWith(".jar")) {
 						InputStream is = ZipUtil.readFile(FileName, EntryName);
 						JarInputStream jis = new JarInputStream(is);
 						PluginClassName = getManifestAttribute(jis.getManifest());
 						jis.close();
 					}
 				}
 			};
 		return PluginClassName;
 	}
 
 	private String getManifestAttribute(Manifest m) {
 		String Value = null;
 		if (m!=null) {
 			Value = m.getMainAttributes().getValue("Cytoscape-Plugin"); 
 		}
 		return Value;
 	}
 		
 		
 }
