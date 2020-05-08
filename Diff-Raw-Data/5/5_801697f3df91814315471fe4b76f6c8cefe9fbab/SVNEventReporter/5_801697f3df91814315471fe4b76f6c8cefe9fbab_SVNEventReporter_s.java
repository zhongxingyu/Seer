 package edu.uci.lighthouse.core.listeners;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.osgi.framework.BundleContext;
 import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
 import org.tigris.subversion.subclipse.core.client.IConsoleListener;
 import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
 import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
 import org.tigris.subversion.svnclientadapter.ISVNInfo;
 import org.tigris.subversion.svnclientadapter.SVNNodeKind;
 import org.tigris.subversion.svnclientadapter.SVNRevision;
 import org.tigris.subversion.svnclientadapter.SVNUrl;
 
 public class SVNEventReporter implements IConsoleListener, IPluginListener {
 
 	private Collection<ISVNEventListener> listeners = new LinkedList<ISVNEventListener>();
 	private IConsoleListener pluginConsoleListener;
 	private int trunksCounter = -1; // It's a counter used in the case trunk has
 									// external references to other SVN trunks
 	private List<Trunk> trunks = new Vector<Trunk>();
 	private int command;
 	
 	private List<File> conflictingFiles = new LinkedList<File>();
 
 	private static Logger logger = Logger.getLogger(SVNEventReporter.class);
 
 	public SVNEventReporter() {
 		/*
 		 * Starts the SVNUIPlugin. This is required because we want the
 		 * ConsoleListener instance for decorating it. If this is not done, we
 		 * cannot guarantee that Lighthouse Core plug-in will start after
 		 * subclipse and hence, we can get a null pointer.
 		 */
 		SVNUIPlugin.getPlugin();
 	}
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		pluginConsoleListener = SVNProviderPlugin.getPlugin()
 				.getConsoleListener();
 		logger.debug("ConsoleListener: " + pluginConsoleListener);
 		SVNProviderPlugin.getPlugin().setConsoleListener(this);
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		SVNProviderPlugin.getPlugin().setConsoleListener(pluginConsoleListener);
 	}
 
 	@Override
 	public void logCommandLine(String arg0) {
 		pluginConsoleListener.logCommandLine(arg0);
 		logger.debug("logCommandLine: " + arg0);
 	}
 
 	@Override
 	public void logCompleted(String arg0) {
 		pluginConsoleListener.logCompleted(arg0);
 		logger.debug("logCompleted: " + arg0);
 
 		try {
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			ISVNClientAdapter svnAdapter = SVNProviderPlugin.getPlugin()
 					.getSVNClient();
 			SVNRevision svnRevision = SVNRevision
 					.getRevision(parseRevision(arg0));
 
 			if (trunksCounter == 0) {
 
 				trunks.get(trunksCounter).svnRevision = svnRevision;
 
 				Map<IFile, ISVNInfo> svnFiles = new HashMap<IFile, ISVNInfo>();
 				Map<IFile, ISVNInfo> svnConflictingFiles = new HashMap<IFile, ISVNInfo>();
 
 				for (Trunk trunk : trunks) {
 					for (File file : trunk) {
 						IFile iFile = workspace.getRoot().getFileForLocation(
 								Path.fromOSString(file.getPath()));
 						ISVNInfo svnInfo = svnAdapter.getInfo(
								gerSVNUrlFromFile(file), trunk.svnRevision,
 								trunk.svnRevision);
 						svnFiles.put(iFile, svnInfo);
 						if (conflictingFiles.contains(file)) {
 							svnConflictingFiles.put(iFile, svnInfo);
 						}
 					}
 				}
 
 				if (svnFiles.size() > 0) {
 					switch (command) {
 					case Command.CHECKOUT:
 						fireCheckout(svnFiles);
 						break;
 
 					case Command.UPDATE/* |Command.REVERT */:
 						fireUpdate(svnFiles);
 						break;
 
 					case Command.COMMIT:
 						fireCommit(svnFiles);
 						break;
 					}
 				}
 				
 				if (svnConflictingFiles.size() > 0) {
 					fireConflict(svnConflictingFiles);	
 				}
 
 			} else {
 				trunks.get(trunksCounter).svnRevision = svnRevision;
 				trunksCounter--;
 			}
 		} catch (Exception e) {
 			logger.error(e,e);
 		}
 	}
 
 	@Override
 	public void logError(String arg0) {
 		pluginConsoleListener.logError(arg0);
 		logger.debug("logError: " + arg0);
 		String[] tokens = arg0.split("\\s+");
 		if (tokens.length > 1 && "C".equals(tokens[0])) {
 			conflictingFiles.add(new File(tokens[1]));
 		}
 	}
 
 	@Override
 	public void logMessage(String arg0) {
 		pluginConsoleListener.logMessage(arg0);
 		logger.debug("logMessage: " + arg0);
 		if (arg0.contains("external")) {
 			trunks.add(new Trunk());
 			trunksCounter++;
 		}
 	}
 
 	@Override
 	public void logRevision(long arg0, String arg1) {
 		pluginConsoleListener.logRevision(arg0, arg1);
 		logger.debug("logRevision: " + arg0);
 	}
 
 	@Override
 	public void onNotify(File file, SVNNodeKind kind) {
 		pluginConsoleListener.onNotify(file, kind);
 		logger.debug("onNotify: " + file + " (" + kind + ")");
 
 		if (kind == SVNNodeKind.FILE
 				&& file.getAbsolutePath().endsWith(".java")) {
 			trunks.get(trunksCounter).add(file);
 		}
 	}
 
 	@Override
 	public void setCommand(int arg0) {
 		pluginConsoleListener.setCommand(arg0);
 		logger.debug("setCommand: " + arg0);
 
 		command = arg0;
 		trunksCounter = 0;
 		trunks.clear();
 		trunks.add(new Trunk());
 		conflictingFiles.clear();
 	}
 
 	public void addSVNEventListener(ISVNEventListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeSVNEventListener(ISVNEventListener listener) {
 		listeners.remove(listener);
 	}
 
 	protected void fireCheckout(Map<IFile, ISVNInfo> svnFiles) {
 		logger.info("checkout: " + svnFiles.size() + " files");
 		for (ISVNEventListener listener : listeners) {
 			listener.checkout(svnFiles);
 		}
 	}
 
 	protected void fireCommit(Map<IFile, ISVNInfo> svnFiles) {
 		logger.info("commit: " + svnFiles.size() + " files");
 		for (ISVNEventListener listener : listeners) {
 			listener.commit(svnFiles);
 		}
 	}
 
 	protected void fireUpdate(Map<IFile, ISVNInfo> svnFiles) {
 		logger.info("update: " + svnFiles.size() + " files");
 		for (ISVNEventListener listener : listeners) {
 			listener.update(svnFiles);
 		}
 	}
 	
 	protected void fireConflict(Map<IFile, ISVNInfo> svnFiles) {
 		logger.info("conflict: " + svnFiles.size() + " files");
 		for (ISVNEventListener listener : listeners) {
 			listener.conflict(svnFiles);
 		}
 	}
 
 	private String parseRevision(String arg0) {
 		String result = null;
 		String[] tokens = arg0.split("[\\s\\.]");
 		for (String token : tokens) {
 			if (token.matches("\\d+")) {
 				result = token;
 				break;
 			}
 		}
 		return result;
 	}
 
	private SVNUrl gerSVNUrlFromFile(File file) {
 		SVNUrl result = null;
 		try {
 			ISVNClientAdapter svnAdapter = SVNProviderPlugin.getPlugin()
 					.getSVNClient();
 			ISVNInfo info = svnAdapter.getInfo(file);
 			result = info.getUrl();
 		} catch (Exception e) {
 			logger.error(e,e);
 		}
 		return result;
 	}
 
 	@SuppressWarnings("serial")
 	private class Trunk extends LinkedList<File> {
 		SVNRevision svnRevision = null;
 	}
 }
