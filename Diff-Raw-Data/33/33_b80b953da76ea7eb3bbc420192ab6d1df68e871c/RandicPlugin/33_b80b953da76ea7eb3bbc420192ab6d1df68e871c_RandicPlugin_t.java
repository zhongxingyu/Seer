 /**
  * @author stephanmg
  * @date 01m13d12y
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test.plugin;
 
 import java.util.*;
 import java.awt.event.ActionEvent;
 import javax.swing.JOptionPane;
 
 import giny.model.Node;
 import giny.view.NodeView;
 
 import cytoscape.plugin.CytoscapePlugin;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.Cytoscape;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.CyEdge;
 import cytoscape.view.CyNetworkView;
 import cytoscape.data.Semantics;
 
 /**
  * This is a sample Cytoscape plugin using core graph and data structures. 
  * For each currently selected node in the graph view, the action method 
  * of this plugin additionally selects the neighbors of that node.
  *
  * Note that selection is a property of the view of the graph, while neighbors
  * are a property of the graph itself. Thus this plugin must access both the
  * graph and its view.
  */
 public class RandicPlugin extends CytoscapePlugin {
     
     /**
      * This constructor creates an action and adds it to the Plugins menu.
      */
     public RandicPlugin() {
         //create a new action to respond to menu activation
         RandicPlugin.MolbiPluginAction action = new RandicPlugin.MolbiPluginAction();
         //set the preferred menu
         action.setPreferredMenu("Plugins.Molbi");
         //and add it to the menus
         Cytoscape.getDesktop().getCyMenus().addAction(action);
     }
     
     /**
      * Gives a description of this plugin.
      */
     public String describe() {
         String sb = "no function yet";
         return sb;
     }
         
     /**
      * This class gets attached to the menu item.
      */
     public class MolbiPluginAction extends CytoscapeAction {
         /**
          * The constructor sets the text that should appear on the menu item.
          */
         public MolbiPluginAction() {super("RandicPlugin");}
         
         /**
          * This method is called when the user selects the menu item.
          */
         public void actionPerformed(ActionEvent ae) {
             run();
             
 
         }
         
         private void run(){
             //get the network object; this contains the graph
             CyNetwork network = Cytoscape.getCurrentNetwork();
             //get the network view object
             CyNetworkView view = Cytoscape.getCurrentNetworkView();
             //can't continue if either of these is null
             if (network == null || view == null) {
                 JOptionPane.showMessageDialog(view.getComponent(), "No network/view loaded.");
                 return;
             }
             
             
             Double nodeNr=0.;
             Double degreeSum=0.;
             System.out.println("RUNNING...");
             
                 int N = network.getNodeCount();
            if (N == 0) {
                 JOptionPane.showMessageDialog(view.getComponent(), "No network/view loaded.");
                 return;
            }
            
             Double IRandic = 0.0;
             // do fancy analysis here:
             System.out.println(network.getNodeCount());
             for (CyNode node : (List<CyNode>)network.nodesList()) {
                 nodeNr++;
                
                 IRandic += Math.pow(network.getDegree(node), 0.5);
                 
                 System.out.println(node.toString());
                 degreeSum += network.getDegree(node);
                 System.out.println(degreeSum);
             }
             
             System.out.println("DONE.");
             Double avrg = degreeSum / nodeNr;
             JOptionPane.showMessageDialog(view.getComponent(), "All done.\nRandic Index "+IRandic);
             //tell the view to redraw since we've changed the selection
             view.redrawGraph(false, true);
         }
         
 
     }
 }
 
