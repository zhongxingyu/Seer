 // $Revision$
 // $Date$
 // $Author$
 package cytoscape.actions;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.giny.PhoebeNetworkView;
 import cytoscape.data.readers.GMLReader2;
 import cytoscape.data.readers.GraphReader;
 import cytoscape.data.readers.InteractionsReader;
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.CyFileFilter;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.util.FileUtil;
 import cytoscape.view.CyMenus;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.NetworkViewManager;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.text.DecimalFormat;
 
 import edu.umd.cs.piccolo.PCanvas;
 import edu.umd.cs.piccolo.PLayer;
 import phoebe.PGraphView;
 
 /**
  * User has requested loading of an Expression Matrix File.
  * Could be SIF File or GML File.
  */
 public class LoadGraphFileAction extends CytoscapeAction {
     protected CyMenus windowMenu;
 
     /**
      * Constructor.
      *
      * @param windowMenu WindowMenu Object.
      */
     public LoadGraphFileAction(CyMenus windowMenu) {
         super("Network...");
         setPreferredMenu("File.Load");
         setAcceleratorCombo(java.awt.event.KeyEvent.VK_L,
                 ActionEvent.CTRL_MASK);
         this.windowMenu = windowMenu;
     }
 
     /**
      * Constructor.
      *
      * @param windowMenu WindowMenu Object.
      * @param label      boolean label.
      */
     public LoadGraphFileAction(CyMenus windowMenu, boolean label) {
         super();
         this.windowMenu = windowMenu;
     }
 
     /**
      * User Initiated Request.
      *
      * @param e Action Event.
      */
     public void actionPerformed(ActionEvent e) {
 
         // Create FileFilters
         CyFileFilter intFilter = new CyFileFilter();
         CyFileFilter gmlFilter = new CyFileFilter();
         CyFileFilter graphFilter = new CyFileFilter();
 
         // Add accepted File Extensions
         gmlFilter.addExtension("gml");
         gmlFilter.setDescription("GML files");
         intFilter.addExtension("sif");
         intFilter.setDescription("Interaction files");
         graphFilter.addExtension("sif");
         graphFilter.addExtension("gml");
         graphFilter.setDescription("All network files");
 
         // Get the file name
         File  file = FileUtil.getFile("Load Network File",
             FileUtil.LOAD, new CyFileFilter[]
             {intFilter, gmlFilter, graphFilter});
 
         // if the name is not null, then load
         if (file != null) {
             int fileType = Cytoscape.FILE_SIF;
 
             //  long enough to have a "gml" extension
             if (file.getName().length() > 4) {
                 String name = file.getName();
                 String extension = name.substring(name.length() - 3);
                 if (extension.equalsIgnoreCase("gml"))
                     fileType = Cytoscape.FILE_GML;
             }
 
             //  Create LoadNetwork Task
             LoadNetworkTask task = new LoadNetworkTask(file, fileType);
 
             //  Configure JTask Dialog Pop-Up Box
             JTaskConfig jTaskConfig = new JTaskConfig();
             jTaskConfig.setOwner(Cytoscape.getDesktop());
             jTaskConfig.displayCloseButton(true);
             jTaskConfig.displayStatus(true);
             jTaskConfig.setAutoDispose(false);
 
             //  Execute Task in New Thread;  pops open JTask Dialog Box.
             TaskManager.executeTask(task, jTaskConfig);
         }
     }
 }
 
 /**
  * Task to Load New Network Data.
  */
 class LoadNetworkTask implements Task {
     private File file;
     private int fileType;
     private CyNetwork cyNetwork;
     private TaskMonitor taskMonitor;
 
     /**
      * Constructor.
      *
      * @param file         File.
      * @param fileType     FileType, e.g. Cytoscape.FILE_SIF or
      *                     Cytoscape.FILE_GML.
      */
     public LoadNetworkTask(File file, int fileType) {
         this.file = file;
         this.fileType = fileType;
     }
 
     /**
      * Executes Task.
      */
     public void run() {
         taskMonitor.setStatus("Reading in Network Data...");
 
         try {
             cyNetwork = this.createNetwork(file.getAbsolutePath(),
                     fileType, Cytoscape.getBioDataServer(),
                     CytoscapeInit.getDefaultSpeciesName());
 
             if (cyNetwork != null) {
                 informUserOfGraphStats(cyNetwork);
             } else {
                 StringBuffer sb = new StringBuffer();
                 sb.append("Could not read network from file: " + file.getName());
                 sb.append("\nThis file may not be a valid GML or SIF file.");
                 taskMonitor.setException(new IOException(sb.toString()),
                         sb.toString());
             }
             taskMonitor.setPercentCompleted(100);
         } catch (IOException e) {
             taskMonitor.setException(e, "Unable to load network file.");
         }
     }
 
     /**
      * Inform User of Network Stats.
      */
     private void informUserOfGraphStats(CyNetwork newNetwork) {
         NumberFormat formatter = new DecimalFormat("#,###,###");
         StringBuffer sb = new StringBuffer();
 
         //  Give the user some confirmation
         sb.append("Succesfully loaded network from:  " + file.getName());
         sb.append("\n\nNetwork contains " + formatter.format
                 (newNetwork.getNodeCount()));
         sb.append(" nodes and " + formatter.format(newNetwork.getEdgeCount()));
         sb.append(" edges.\n\n");
 
         if (newNetwork.getNodeCount() < CytoscapeInit.getViewThreshold()) {
             sb.append("Network is under "
                     + CytoscapeInit.getViewThreshold()
                     + " nodes.  A view will be automatically created.");
         } else {
             sb.append("Network is over "
                     + CytoscapeInit.getViewThreshold()
                     + " nodes.  A view will not been created."
                     + "  If you wish to view this network, use "
                     + "\"Create View\" from the \"Edit\" menu.");
         }
         taskMonitor.setStatus(sb.toString());
     }
 
     /**
      * Halts the Task:  Not Currently Implemented.
      */
     public void halt() {
         //   Task can not currently be halted.
     }
 
     /**
      * Sets the Task Monitor.
      *
      * @param taskMonitor TaskMonitor Object.
      */
     public void setTaskMonitor(TaskMonitor taskMonitor)
             throws IllegalThreadStateException {
         this.taskMonitor = taskMonitor;
     }
 
     /**
      * Gets the Task Title.
      *
      * @return Task Title.
      */
     public String getTitle() {
         return new String("Loading Network");
     }
 
     /**
      * Creates a cytoscape.data.CyNetwork from a file.
      * The passed variable determines the
      * type of file, i.e. GML, SIF, etc.<p>
      * This operation may take a long time to complete.
      *
      * @param location      the location of the file
      * @param file_type     the type of file GML, SIF, SBML, etc.
      * @param biodataserver provides the name conversion service
      * @param species       the species used by the BioDataServer
      */
     private CyNetwork createNetwork(String location, int file_type,
             BioDataServer biodataserver, String species) throws IOException {
 
         GraphReader reader;
         taskMonitor.setPercentCompleted(5);
 
         //  Set the reader according to what file type was passed.
         if (file_type == Cytoscape.FILE_SIF) {
             reader = new InteractionsReader(biodataserver, species, location,
                     taskMonitor);
         } else if (file_type == Cytoscape.FILE_GML) {
             reader = new GMLReader2(location, taskMonitor);
         } else {
             throw new IOException("File Type not Supported.");
         }
 
         // Have the GraphReader read the given file
         reader.read();
 
         //  Get the RootGraph indices of the nodes and
         //  Edges that were just created
         final int[] nodes = reader.getNodeIndicesArray();
         final int[] edges = reader.getEdgeIndicesArray();
 
         File file = new File (location);
         final String title = file.getName();
 
         // Create a new cytoscape.data.CyNetwork from these nodes and edges
         taskMonitor.setStatus("Creating Cytoscape Network...");
 
         //  Create the CyNetwork
         //  First, set the view threshold to 0.  By doing so, we can disable
         //  the auto-creating of the CyNetworkView.
         int realThreshold = CytoscapeInit.getViewThreshold();
         CytoscapeInit.setViewThreshold(0);
         CyNetwork network = Cytoscape.createNetwork(nodes, edges,
             CyNetworkNaming.getSuggestedNetworkTitle (title));
 
         //  Reset back to the real View Threshold
         CytoscapeInit.setViewThreshold(realThreshold);
 
         //  Store GML Data as a Network Attribute
         if (file_type == Cytoscape.FILE_GML) {
             network.putClientData("GML", reader);
         }
 
         //  Conditionally, Create the CyNetworkView
         if (network.getNodeCount() < CytoscapeInit.getViewThreshold()  ) {
             createCyNetworkView(network);
 
             //  Layout Network
             if (Cytoscape.getNetworkView(network.getIdentifier()) != null) {
                 reader.layout(Cytoscape.getNetworkView
                    (network.getIdentifier()));
             }
 
             //  Lastly, make the GraphView Canvas Visible.
             SwingUtilities.invokeLater(new Runnable () {
                 public void run() {
                     PGraphView view =(PGraphView)
                             Cytoscape.getCurrentNetworkView();
                     PCanvas pCanvas = view.getCanvas();
                     pCanvas.setVisible(true);
                 }
             });
         }
         return network;
     }
 
     /**
      * Creates the CyNetworkView.
      * Most of this code is copied directly from Cytoscape.createCyNetworkView.
      * However, it requires a bit of a hack to actually hide the network
      * view from the user, and I didn't want to use this hack in the core
      * Cytoscape.java class.
      */
     private void createCyNetworkView (CyNetwork cyNetwork) {
         final PhoebeNetworkView view = new PhoebeNetworkView(cyNetwork,
                 cyNetwork.getTitle());
 
       //  Start of Hack:  Hide the View
       PCanvas pCanvas = view.getCanvas();
       pCanvas.setVisible(false);
       //  End of Hack
 
       view.setIdentifier(cyNetwork.getIdentifier());
       Cytoscape.getNetworkViewMap().put(cyNetwork.getIdentifier(), view);
       view.setTitle(cyNetwork.getTitle());
 
      // if Squiggle function enabled, enable squiggling on the created view
      if (Cytoscape.isSquiggleEnabled()) {
        view.getSquiggleHandler().beginSquiggling();
      }

       Cytoscape.firePropertyChange
               (cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED,null, view);
 
       //  Instead of calling fitContent(), access PGraphView directly.
       view.getCanvas().getCamera().animateViewToCenterBounds
                     (view.getCanvas().getLayer().getFullBounds(), true, 0);
     }
 }
