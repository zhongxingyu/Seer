 /**
  * 
  */
 package cytoscape.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.URLUtil;
 import cytoscape.util.ZipUtil;
 import cytoscape.logger.CyLogger;
 import java.util.zip.ZipFile;
 
 /**
  * @author skillcoy
  * 
  */
 public class InstallablePlugin implements Installable {
   private static CyLogger logger = CyLogger.getLogger(InstallablePlugin.class);
 
   private PluginInfo infoObj;
 
 	public InstallablePlugin(PluginInfo obj) {
 		this.infoObj = obj;
 	}
 
 	public PluginInfo getInfoObj() {
 		return this.infoObj;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see cytoscape.plugin.Installable#install()
 	 */
 	public boolean install() throws java.io.IOException,
 			cytoscape.plugin.ManagerException {
 		return installToDir(null, null);
 	}
 
 	public boolean installToDir(File dir) throws java.io.IOException,
 		cytoscape.plugin.ManagerException {
 		return installToDir(dir, null);
 	}
 
 	public boolean installToDir(File dir, cytoscape.task.TaskMonitor taskMonitor) 
 		throws java.io.IOException,cytoscape.plugin.ManagerException {
 		
 		
 		File PluginDir = dir;
 		if (PluginDir == null) 
 			PluginDir = infoObj.getPluginDirectory();
 		else
 			PluginDir = new File(PluginDir, getInfoObj().getName() + "-" + getInfoObj().getObjectVersion());
 		
 		if (!PluginDir.exists()) {
 			PluginDir.mkdirs();
 		}
 
 		File Download = new File(PluginDir, createFileName(infoObj));
 		if (taskMonitor != null)
 			taskMonitor.setStatus(infoObj.toString() + " loading...");
 		URLUtil.download(infoObj.getObjectUrl(), Download, taskMonitor);
 
 		try {
 			String ClassName = getPluginClass(Download.getAbsolutePath(),
 					infoObj.getFileType());
 
 			if (ClassName != null) {
 				infoObj.setPluginClassName(ClassName);
 			} else { // no class name so delete the plugin
 				Download.delete();
 				Download.getParentFile().delete();
 				ManagerException E = new ManagerException(
 						infoObj.getName()
 								+ " does not define the attribute 'Cytoscape-Plugin' in the jar manifest file.\n"
 								+ "This plugin cannot be auto-installed.  Please install manually or contact the plugin author.");
 				throw E;
 			}
 		} catch (IOException ioe) {
 			Download.delete();
 			Download.getParentFile().delete();
       throw ioe;
 		}
 
 		switch (infoObj.getFileType()) {
 		case JAR: // do nothing, it's installed
 			break;
 		case ZIP:
 			try {
 				List<String> UnzippedFiles = ZipUtil.unzip(Download
 						.getAbsolutePath(), Download.getParent(), taskMonitor);
 				infoObj.setFileList(UnzippedFiles);
 			} catch (IOException ioe) {
 				Download.delete();
 				throw ioe;
 			}
 			break;
 		}
 
 		infoObj.setInstallLocation(PluginDir.getAbsolutePath());
 		infoObj.addFileName(Download.getAbsolutePath());
 
 		return true;
 	}
 	
 	public boolean install(cytoscape.task.TaskMonitor taskMonitor)
 			throws java.io.IOException, cytoscape.plugin.ManagerException {
 
 		return installToDir(null, taskMonitor);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see cytoscape.plugin.Installable#uninstall()
 	 */
 	public boolean uninstall() throws cytoscape.plugin.ManagerException {
 		boolean Deleted = false;
 		if (infoObj.getInstallLocation() != null
 				&& infoObj.getInstallLocation().length() > 0) {
 			File Installed = new File(infoObj.getInstallLocation());
 			Deleted = PluginManager.recursiveDeleteFiles(Installed);
 		} else {
 			for (String f : infoObj.getFileList()) {
 				Deleted = (new File(f)).delete();
 			}
 		}
 		return Deleted;
 	}
 
 	public java.util.List<cytoscape.plugin.DownloadableInfo> findUpdates()
 			throws java.io.IOException, org.jdom.JDOMException {
 
 		final List<DownloadableInfo> UpdatablePlugins = new ArrayList<DownloadableInfo>();
 		final Set<DownloadableInfo> Seen = new HashSet<DownloadableInfo>();
 
 		Seen.add(this.infoObj);
 
 		if (this.infoObj.getDownloadableURL() == null
 				|| this.infoObj.getDownloadableURL().length() <= 0) {
 			return UpdatablePlugins;
 		}
 
 		final PluginInfo InfoToUpdate = this.infoObj;
 		final List<Exception> Exceptions = new ArrayList<Exception>();
 
 		PluginManagerInquireTask task = new PluginManagerInquireTask(
 				this.infoObj.getDownloadableURL(), new PluginInquireAction() {
 
 					public String getProgressBarMessage() {
 						return "Connecting to "
 								+ InfoToUpdate.getDownloadableURL()
 								+ " to search for updates...";
 					}
 
 					public void inquireAction(List<DownloadableInfo> Results) {
 
 						if (isExceptionThrown()) {
 							Exceptions.add(0, getIOException());
 							Exceptions.add(1, getJDOMException());
 						}
 
 						for (DownloadableInfo NewInfo : Results) {
 							if (!InfoToUpdate.getType().equals(
 									NewInfo.getType()))
 								continue;
 
 							PluginInfo New = (PluginInfo) NewInfo;
 							// ID or classname are unique
 							boolean newer = InfoToUpdate
 									.isNewerObjectVersion(New);
 							if ((New.getID().equals(InfoToUpdate.getID()) || New
 									.getPluginClassName().equals(
 											InfoToUpdate.getPluginClassName()))
 									&& newer) {
 
 								if (!Seen.contains(New) && newer) {
 									UpdatablePlugins.add(New);
 								} else {
 									Seen.add(New);
 								}
 							}
 						}
 					}
 				});
 
 		// Execute Task in New Thread; pop open JTask Dialog Box.
 		TaskManager.executeTask(task, null);
 
 		if (Exceptions.size() > 0) {
 			if (Exceptions.get(0) != null) {
 				throw (java.io.IOException) Exceptions.get(0);
 			}
 			if (Exceptions.size() > 1 && Exceptions.get(1) != null) {
 				throw (org.jdom.JDOMException) Exceptions.get(1);
 			}
 		}
 
 		return UpdatablePlugins;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see cytoscape.plugin.Installable#update()
 	 */
 	public boolean update(DownloadableInfo newObj) throws java.io.IOException,
 			cytoscape.plugin.ManagerException {
 		return update(newObj, null);
 	}
 
 	public boolean update(DownloadableInfo newInfoObj,
 			cytoscape.task.TaskMonitor taskMonitor) throws java.io.IOException,
 			cytoscape.plugin.ManagerException {
 
 		PluginInfo newObj = (PluginInfo) newInfoObj;
 
 		if (infoObj.getDownloadableURL() == null) {
 			throw new ManagerException(
 					infoObj.getName()
 							+ " does not have a project url.\nCannot auto-update this plugin.");
 		}
 		// ID or classname
 		if ((infoObj.getID().equals(newObj.getID()) || infoObj
 				.getPluginClassName().equals(newObj.getPluginClassName()))
 				&& infoObj.getDownloadableURL().equals(
 						newObj.getDownloadableURL())
 				&& infoObj.isNewerObjectVersion(newObj)) {
 
 			this.infoObj = newObj;
 			this.install(taskMonitor);
 
 		} else {
 			throw new ManagerException(
 					"Failed to update '"
 							+ infoObj.getName()
 							+ "', the new plugin did not match what is currently installed\n"
 							+ "or the version was not newer than what is currently installed.");
 		}
 
 		return true;
 	}
 
 	/*
 	 * Standard name for the plugin install file <Plugin Name>.jar|zip
 	 */
 	private String createFileName(PluginInfo Obj) {
 		return Obj.getName() + "." + Obj.getFileType().toString();
 	}
 
 	/*
 	 * Iterate through all class files, return the subclass of CytoscapePlugin.
 	 * Similar to CytoscapeInit, however only plugins with manifest files that
 	 * describe the class of the CytoscapePlugin are valid.
 	 */
 	private String getPluginClass(String FileName, PluginInfo.FileType Type)
 			throws IOException {
 		String PluginClassName = null;
 
 		switch (Type) {
 		case JAR:
 			JarFile Jar = new JarFile(FileName);
             try {
                 PluginClassName = getManifestAttribute(Jar.getManifest());
             }
             finally {
                 if (Jar != null) {
                     Jar.close();
                 }
             }
 			break;
 
 		case ZIP:
 			List<ZipEntry> Entries = ZipUtil
					.getAllFiles(FileName, "\\w+\\.jar");
 			if (Entries.size() <= 0) {
 				String[] FilePath = FileName.split("/");
 				FileName = FilePath[FilePath.length - 1];
 				throw new IOException(
 						FileName
 								+ " does not contain any jar files or is not a zip file.");
 			}
 
             ZipFile zf = null;
 
             zf = new ZipFile(FileName);
             try {
                 for (ZipEntry Entry : Entries) {
                     String EntryName = Entry.getName();
 
                     InputStream is = ZipUtil.readFile(zf, EntryName);
                     try {
                         JarInputStream jis = new JarInputStream(is);
                         try {
                             PluginClassName = getManifestAttribute(jis.getManifest());
                         }
                         finally {
                             if (jis != null) {
                                 jis.close();
                             }
                         }
                     }
                     finally {
                         if (is != null) {
                             is.close();
                         }
                     }
                 }
             }
             finally {
                 zf.close();
             }
 		}
 
         return PluginClassName;
 	}
 
 	/*
 	 * Gets the manifest file value for the Cytoscape-Plugin attribute
 	 */
 	private String getManifestAttribute(Manifest m) {
 		String Value = null;
 		if (m != null) {
 			Value = m.getMainAttributes().getValue("Cytoscape-Plugin");
 		}
 		return Value;
 	}
 
 }
