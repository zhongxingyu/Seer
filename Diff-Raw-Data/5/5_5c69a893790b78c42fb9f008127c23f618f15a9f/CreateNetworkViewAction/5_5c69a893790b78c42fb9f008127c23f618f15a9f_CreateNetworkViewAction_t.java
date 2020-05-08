 package cytoscape.actions;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.util.CytoscapeAction;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.text.NumberFormat;
 import java.text.DecimalFormat;
 
 public class CreateNetworkViewAction extends CytoscapeAction {
 
     public CreateNetworkViewAction() {
         super("Create View");
         setPreferredMenu("Edit");
         setAcceleratorCombo(java.awt.event.KeyEvent.VK_V, ActionEvent.ALT_MASK);
     }
 
     public CreateNetworkViewAction(boolean label) {
         super();
     }
 
     public void actionPerformed(ActionEvent e) {
         CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
         createViewFromCurrentNetwork(cyNetwork);
     }
 
     public static void createViewFromCurrentNetwork(CyNetwork cyNetwork) {
         NumberFormat formatter = new DecimalFormat("#,###,###");
         if (cyNetwork.getNodeCount()
                 > CytoscapeInit.getSecondaryViewThreshold()) {
             int n = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(),
                     "Network contains "
                     + formatter.format(cyNetwork.getNodeCount())
                     + " nodes and " + formatter.format
                     (cyNetwork.getEdgeCount()) + " edges.  "
                     + "\nRendering a network this size may take several "
                     + "minutes.\n"
                     + "Do you wish to proceed?", "Rendering Large Network",
                     JOptionPane.YES_NO_OPTION);
             if (n == JOptionPane.YES_OPTION) {
                 Cytoscape.createNetworkView(Cytoscape.getCurrentNetwork());
             } else {
                 JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                         "Create View Request Cancelled by User.");
             }
        } else {
            Cytoscape.createNetworkView(Cytoscape.getCurrentNetwork());            
         }
     }
 }
