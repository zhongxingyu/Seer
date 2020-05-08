 package cytoscape.actions;
 
 import cytoscape.Cytoscape;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.view.CyNetworkView;
import giny.view.Bend;
 import giny.view.EdgeView;
 import giny.view.NodeView;
 
 import java.awt.EventQueue;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.geom.Point2D;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 
 public class BarAction extends CytoscapeAction
 {
 
   public BarAction() { super("Bar Bug"); setPreferredMenu("Layout"); }
 
   public void actionPerformed(ActionEvent e) {
     CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
     final NodeView[] nodes = new NodeView[graphView.getNodeViewCount()];
     final EdgeView[] edges = new EdgeView[graphView.getEdgeViewCount()];
     Iterator nodeIter = graphView.getNodeViewsIterator();
     int nodeInx = 0;
     while (nodeIter.hasNext()) {
       nodes[nodeInx++] = (NodeView) nodeIter.next(); }
     Iterator edgeIter = graphView.getEdgeViewsIterator();
     int edgeInx = 0;
     while (edgeIter.hasNext()) {
       edges[edgeInx++] = (EdgeView) edgeIter.next(); }
     Frame cyFrame = Cytoscape.getDesktop();
     JDialog dialog = new JDialog(cyFrame, "Bar", true);
     dialog.setResizable(false);
     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     JLabel label = new JLabel
       ("<html><body><h3>Behold the power of foo!</h3></body></html>");
     label.setBorder(new EmptyBorder(15, 15, 15, 15));
     dialog.getContentPane().add(label);
     dialog.pack();
     dialog.move((cyFrame.size().width - dialog.size().width) / 2 +
                 cyFrame.location().x,
                 (cyFrame.size().height - dialog.size().height) / 20 +
                 cyFrame.location().y);
     final boolean[] stop = new boolean[] { false };
     final boolean[] forward = new boolean[] { true };
     (new Thread(new Runnable() {
         public void run()
         {
           while (true)
           {
             if (stop[0]) return;
             // Invoking all Giny and Piccolo operations from AWT event
             // dispatch thread for 100% correctness.
             EventQueue.invokeLater(new Runnable() {
                 public void run()
                 {
                   double offset = (forward[0] ? 40.0d : -40.0d);
                   forward[0] = !forward[0];
                   for (int i = 0; i < nodes.length; i++) {
                     //nodes[i].setXPosition(nodes[i].getXPosition() + offset);
                     //nodes[i].setYPosition(nodes[i].getYPosition() + offset);
                     nodes[i].setOffset(nodes[i].getXPosition() + offset,
                                        nodes[i].getYPosition() + offset); }
                   for (int i = 0; i < edges.length; i++) {
                    Bend bend = edges[i].getBend();
                    List handles = bend.getHandles();
                     for (int j = 0; j < handles.size(); j++) {
                       Point2D point = (Point2D) handles.get(j);
                       bend.moveHandle
                         (j, new Point2D.Double(point.getX() + offset,
                                                point.getY() + offset)); } }
                 } });
             try { Thread.sleep(2000); }
             catch (InterruptedException e) {}
           }
         } })).start();
     dialog.show(); // This blocks until dialog is disposed of.
     stop[0] = true;
   }
 
 }
