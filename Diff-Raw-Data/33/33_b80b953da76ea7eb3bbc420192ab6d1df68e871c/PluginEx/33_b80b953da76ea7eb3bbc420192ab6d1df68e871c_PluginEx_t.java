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
 
 import Jama.Matrix;
 
 /**
  * This is a sample Cytoscape plugin using core graph and data structures.
  * For each currently selected node in the graph view, the action method
  * of this plugin additionally selects the neighbors of that node.
  *
  * Note that selection is a property of the view of the graph, while neighbors
  * are a property of the graph itself. Thus this plugin must access both the
  * graph and its view.
  */
 public class PluginEx extends CytoscapePlugin {
 
     /**
      * This constructor creates an action and adds it to the Plugins menu.
      */
     public PluginEx() {
         //create a new action to respond to menu activation
         PluginEx.MolbiPluginAction action = new PluginEx.MolbiPluginAction();
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
         public MolbiPluginAction() {
             super("PluginKatz (alpha = 0.1)");
         }
 
         /**
          * This method is called when the user selects the menu item.
          */
         public void actionPerformed(ActionEvent ae) {
             run();
 
 
         }
 
         private void run() {
             //get the network object; this contains the graph
             CyNetwork network = Cytoscape.getCurrentNetwork();
             //get the network view object
             CyNetworkView view = Cytoscape.getCurrentNetworkView();
             //can't continue if either of these is null
             if (network == null || view == null) {
                 JOptionPane.showMessageDialog(view.getComponent(), "No network/view loaded.");
                 return;
             }
 
             int N = network.getNodeCount();
            if (N == 0) {
                 JOptionPane.showMessageDialog(view.getComponent(), "No network/view loaded.");
                 return;
            }
          
             double[][] A = new double[N][N];
             for (int i = 0; i < N; i++) {
                 for (int j = 0; j < N; j++) {
                     A[i][j] = 0.0;
                 }
             }
 
                      for (CyEdge edge : (List<CyEdge>) network.edgesList()) {
 
               int i = edge.getSource().getRootGraphIndex();
                 int j = edge.getTarget().getRootGraphIndex();
                 
               
                        
                 A[i][j] = 1;
 
             }
 
             Matrix M = new Matrix(A);
             Matrix I = Matrix.identity(N, N);
             Matrix IVec = Matrix.identity(1, N);
 
             double alpha = 0.1; // emulates degree centrality
 
             Matrix res = ((I.minus(M.transpose().times(alpha))).inverse()).minus(I).times(I);
 
             double[][] res2 = res.getArrayCopy();
             
              StringBuilder sb = new StringBuilder("");
             for (int i = 0; i < N; i++) {
                 sb.append("C(");
                 sb.append(i);
                 sb.append("): ");
                 sb.append((res2[i][0]));
                 sb.append("\n");
             }
             
 
 
             JOptionPane.showMessageDialog(view.getComponent(), "All done.\n" + sb.toString());
             //tell the view to redraw since we've changed the selection
             view.redrawGraph(false, true);
         }
     }
 }
