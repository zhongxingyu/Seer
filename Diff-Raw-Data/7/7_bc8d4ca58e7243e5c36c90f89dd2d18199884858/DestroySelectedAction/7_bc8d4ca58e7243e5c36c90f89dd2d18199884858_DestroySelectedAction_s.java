 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 
 import giny.model.*;
 import giny.view.*;
 import java.util.*;
 
 import cytoscape.Cytoscape;
 import cytoscape.CyNetwork;
 import cytoscape.util.CytoscapeAction;
 //-------------------------------------------------------------------------
 public class DestroySelectedAction extends CytoscapeAction  {
 
     public DestroySelectedAction () {
         super( "Destroy Selected Nodes/Edges");
         setPreferredMenu( "Edit" );
     }
     
     public DestroySelectedAction ( boolean label) {
         super(  );
     }
     
     public void actionPerformed (ActionEvent e) {
 	CyNetwork gp = Cytoscape.getCurrentNetwork();
 	Set flaggedNodes = gp.getFlaggedNodes();
 	Set flaggedEdges = gp.getFlaggedEdges();
 	int [] hiddenNodeIndices = new int [flaggedNodes.size()];
 	int [] hiddenEdgeIndices = new int [flaggedEdges.size()];
 	
 	int j=0;
 	for (Iterator i = flaggedNodes.iterator(); i.hasNext(); ) {
 	    hiddenNodeIndices[j++] = gp.getIndex((Node) i.next());
 	}
 	j=0;
 	for (Iterator i = flaggedEdges.iterator(); i.hasNext(); ) {
 	    hiddenEdgeIndices[j++] = gp.getIndex((Edge) i.next());
 	}
	gp.hideNodes(hiddenNodeIndices);
 	gp.hideEdges(hiddenEdgeIndices);
 	    
     }//action performed
 
 }
 
