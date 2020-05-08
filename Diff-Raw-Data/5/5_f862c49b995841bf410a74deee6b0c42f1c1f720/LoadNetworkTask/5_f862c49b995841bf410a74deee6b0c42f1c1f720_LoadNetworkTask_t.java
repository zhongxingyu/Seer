 /*
  File: LoadNetworkTask.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
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
 
 // $Revision: 8703 $
 // $Date: 2006-11-06 23:17:02 -0800 (Mon, 06 Nov 2006) $
 // $Author: pwang $
 package cytoscape.actions;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.logger.CyLogger;
 
 import cytoscape.data.readers.GraphReader;
 
 import cytoscape.init.CyInitParams;
 
 import cytoscape.layout.CyLayoutAlgorithm;
 
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 
 import cytoscape.task.ui.JTask;
 import cytoscape.task.ui.JTaskConfig;
 
 import cytoscape.task.util.TaskManager;
 
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.net.URI;
 import java.net.URL;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import javax.swing.JOptionPane;
 import cytoscape.data.readers.NNFReader;
 
 /**
  * Task to load a new network.
  */
 public class LoadNetworkTask implements Task {
 	private static long taskLoadStart = 0; // "0" means that it has not bene initialised!
 
 	/**
 	 *  Load a network from a url.  The reader code will attempt to determine
 	 *  the format of the network (GML, XGMML, SIF) from the HTTP content-type
 	 *  header.  If it is unable to figure it out from there, it will try writing
 	 *  the HTTP stream to a file to look at the first couple of bytes.  Note that
 	 *  the actual opening of the HTTP stream is postponed until the task is
 	 *  initiated to facility the ability of the user to abort the attempt.
 	 *
 	 * @param u the URL to load the network from
 	 * @param skipMessage if true, dispose of the task monitor dialog immediately
 	 */
 	public static void loadURL(URL u, boolean skipMessage) {
 		loadURL(u, skipMessage, null);
 	}
 
 	/**
 	 *  Load a network from a file.  The reader code will attempt to determine
 	 *  the format of the network (GML, XGMML, SIF) from the file extension.
 	 *  If it is unable to figure it out from there, it will try reading
 	 *  the the first couple of bytes from the file.
 	 *
 	 * @param file the file to load the network from
 	 * @param skipMessage if true, dispose of the task monitor dialog immediately
 	 */
 	public static void loadFile(File file, boolean skipMessage) {
 		// Create LoadNetwork Task
 		loadFile(file, skipMessage, null);
 	}
 
 	/**
 	 *  Load a network from a url.  The reader code will attempt to determine
 	 *  the format of the network (GML, XGMML, SIF) from the HTTP content-type
 	 *  header.  If it is unable to figure it out from there, it will try writing
 	 *  the HTTP stream to a file to look at the first couple of bytes.  Note that
 	 *  the actual opening of the HTTP stream is postponed until the task is
 	 *  initiated to facility the ability of the user to abort the attempt.
 	 *
 	 * @param u the URL to load the network from
 	 * @param skipMessage if true, dispose of the task monitor dialog immediately
 	 * @param layoutAlgorithm if this is non-null, use this algorithm to lay out the network
 	 *                        after it has been read in (provided that a view was created).
 	 */
 	public static void loadURL(URL u, boolean skipMessage, CyLayoutAlgorithm layoutAlgorithm) {
 		taskLoadStart = System.nanoTime();
 		LoadNetworkTask task = new LoadNetworkTask(u, layoutAlgorithm);
 		setupTask(task, skipMessage, true);
 	}
 
 	/**
 	 *  Load a network from a file.  The reader code will attempt to determine
 	 *  the format of the network (GML, XGMML, SIF) from the file extension.
 	 *  If it is unable to figure it out from there, it will try reading
 	 *  the the first couple of bytes from the file.
 	 *
 	 * @param file the file to load the network from
 	 * @param skipMessage if true, dispose of the task monitor dialog immediately
 	 * @param layoutAlgorithm if this is non-null, use this algorithm to lay out the network
 	 *                        after it has been read in (provided that a view was created).
 	 */
 	public static void loadFile(File file, boolean skipMessage, CyLayoutAlgorithm layoutAlgorithm) {
 		taskLoadStart = System.nanoTime();
 		LoadNetworkTask task = new LoadNetworkTask(file, layoutAlgorithm);
 		setupTask(task, skipMessage, true);
 	}
 
 	private static void setupTask(LoadNetworkTask task, boolean skipMessage, boolean cancelable) {
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 
 		if (cancelable)
 			jTaskConfig.displayCancelButton(true);
 
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(skipMessage);
 
 		// Execute Task in New Thread; pops open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 	}
 
 	private URI uri;
 	private TaskMonitor taskMonitor;
 	private GraphReader reader;
 	private String name;
 	private URL url;
 	private Thread myThread = null;
 	private boolean interrupted = false;
 	private CyLayoutAlgorithm layoutAlgorithm = null;
 
 	private LoadNetworkTask(URL u, CyLayoutAlgorithm layout) {
 		url = u;
 		name = u.toString();
 		reader = null;
 		layoutAlgorithm = layout;
 
 		// Postpone getting the reader since we want to do that in a thread
 	}
 
 	private LoadNetworkTask(File file, CyLayoutAlgorithm layout) {
 		reader = Cytoscape.getImportHandler().getReader(file.getAbsolutePath());
 		uri = file.toURI();
 		name = file.getName();
 		layoutAlgorithm = layout;
 
 		if (reader == null) {
 			uri = null;
 			url = null;
 			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Unable to open file " + name,
 			                              "File Open Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Executes Task.
 	 */
 	public void run() {
 		if ((reader == null) && (url == null))
 			return;
 
 		myThread = Thread.currentThread();
 
 		if ((reader == null) && (url != null)) {
 			try {
 				taskMonitor.setStatus("Opening URL " + url);
 				reader = Cytoscape.getImportHandler().getReader(url);
 
 				if (interrupted)
 					return;
 
 				uri = url.toURI();
 			} catch (Exception e) {
 				uri = null;
 				taskMonitor.setException(e,
 				                         "Unable to connect to URL " + name + ": " + e.getMessage());
 
 				return;
 			}
 
 			if (reader == null) {
 				uri = null;
 				taskMonitor.setException(null, "Unable to connect to URL " + name);
 
 				return;
 			}
 
 			// URL is open, things will get very messy if the user cancels the actual
 			// network load, so prevent them from doing so
 			((JTask) taskMonitor).setCancel(false);
 		}
 
		taskMonitor.setStatus("Loading Network Data...");
 
 		// Remove unnecessary listeners:
 		if ((CytoscapeInit.getCyInitParams().getMode() == CyInitParams.GUI)
 		    || (CytoscapeInit.getCyInitParams().getMode() == CyInitParams.EMBEDDED_WINDOW)) {
 			Cytoscape.getDesktop().getSwingPropertyChangeSupport()
 			         .removePropertyChangeListener(Cytoscape.getDesktop().getBirdsEyeViewHandler());
 		}
 
 		try {
 			taskMonitor.setPercentCompleted(-1);
 
 			taskMonitor.setStatus("Creating Cytoscape Network...");
 
 			final CyNetwork cyNetwork = Cytoscape.createNetwork(reader, true, null);
 
 			Object[] ret_val = new Object[2];
 			ret_val[0] = cyNetwork;
 			ret_val[1] = uri;
 
 			if ((CytoscapeInit.getCyInitParams().getMode() == CyInitParams.GUI)
 			    || (CytoscapeInit.getCyInitParams().getMode() == CyInitParams.EMBEDDED_WINDOW)) {
 				Cytoscape.getDesktop().getSwingPropertyChangeSupport()
 				         .addPropertyChangeListener(Cytoscape.getDesktop().getBirdsEyeViewHandler());
 				Cytoscape.getDesktop().getNetworkViewManager()
 				         .firePropertyChange(CytoscapeDesktop.NETWORK_VIEW_FOCUSED, null,
 				                             Cytoscape.getCurrentNetworkView().getNetwork()
 				                                      .getIdentifier());
 			}
 
 			Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, ret_val);
 
 			if (cyNetwork != null) {
 				informUserOfGraphStats(cyNetwork);
 			} else {
 				StringBuffer sb = new StringBuffer();
 				sb.append("Could not read network from: ");
 				sb.append(name);
 				sb.append("\nThis file may not be a valid file format.");
 				taskMonitor.setException(new IOException(sb.toString()), sb.toString());
 			}
 
 			taskMonitor.setPercentCompleted(100);
 		} catch (Exception e) {
 			taskMonitor.setException(e, "Unable to load network.");
 
 			return;
 		} finally {
 			reader = null;
 		}
 	}
 
 	/**
 	 * Inform User of Network Stats.
 	 */
 	private void informUserOfGraphStats(CyNetwork newNetwork) {
 		NumberFormat formatter = new DecimalFormat("#,###,###");
 		StringBuffer sb = new StringBuffer();
 		
 		String msg = "";
 		if (reader instanceof NNFReader){
 			final NNFReader theReader = (NNFReader) reader;
 			msg += "Successfully loaded "+ theReader.getNetworks().size() + " nested networks from " + name;
 
 			if (taskLoadStart != 0) // Display how long it took to load the NNs.
 				msg += " in " + (System.nanoTime() - taskLoadStart + 500000L) / 1000000L + "ms.";
 			else
 				msg += ".";
 
 			taskMonitor.setStatus(msg);
 			return;
 		}
 		
 		// Give the user some confirmation
 		sb.append("Successfully loaded network from:  ");
 		sb.append(name);
 		sb.append("\n\nNetwork contains " + formatter.format(newNetwork.getNodeCount()));
 		sb.append(" nodes and " + formatter.format(newNetwork.getEdgeCount()));
 		sb.append(" edges.\n\n");
 
 		if (newNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties()
 		                                                              .getProperty("viewThreshold"))) {
 			sb.append("Network is under "
 			          + CytoscapeInit.getProperties().getProperty("viewThreshold")
 			          + " nodes.  A view has been created automatically.");
 		} else {
 			sb.append("Network is over "
 			          + CytoscapeInit.getProperties().getProperty("viewThreshold")
 			          + " nodes.  A view has not been created."
 			          + "  If you wish to view this network, use "
 			          + "\"Create View\" from the \"Edit\" menu.");
 		}
 
 		taskMonitor.setStatus(sb.toString());
 	}
 
 	/**
 	 * Halts the Task: Not Currently Implemented.
 	 */
 	public void halt() {
 		// Task can not currently be halted.
 		CyLogger.getLogger().info("Halt called");
 
 		if (myThread != null) {
 			myThread.interrupt();
 			this.interrupted = true;
 			((JTask) taskMonitor).setDone();
 		}
 	}
 
 	/**
 	 * Sets the Task Monitor.
 	 *
 	 * @param taskMonitor
 	 *            TaskMonitor Object.
 	 */
 	public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
 		this.taskMonitor = taskMonitor;
 	}
 
 	/**
 	 * Gets the Task Title.
 	 *
 	 * @return Task Title.
 	 */
 	public String getTitle() {
 		if (reader instanceof NNFReader){
 			return new String("Loading Network(s)");
 		}
 		
		return new String("Loading Network");
 	}
 }
