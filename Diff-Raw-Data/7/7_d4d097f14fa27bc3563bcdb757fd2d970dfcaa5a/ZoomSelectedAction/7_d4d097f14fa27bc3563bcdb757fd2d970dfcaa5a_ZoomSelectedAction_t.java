 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import java.awt.geom.Rectangle2D;
 
 
 import edu.umd.cs.piccolo.util.PBounds;
 import edu.umd.cs.piccolo.activities.PTransformActivity;
 import java.util.List;
 import java.util.Iterator;
 
 import giny.view.*;
 import cytoscape.giny.*;
 import cytoscape.Cytoscape;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.view.CyNetworkView;
 
 //-------------------------------------------------------------------------
 public class ZoomSelectedAction extends CytoscapeAction {
        
     public ZoomSelectedAction ()  {
         super();
     }
     
     public void actionPerformed(ActionEvent e) {
         
       zoomSelected();
     }
 
   public static void zoomSelected () {
       CyNetworkView view = Cytoscape.getCurrentNetworkView();
         List selected_nodes = view.getSelectedNodes();
 
         if ( selected_nodes.size() == 0 ) {return;}
 
         Iterator selected_nodes_iterator = selected_nodes.iterator();
         double bigX;
         double bigY;
         double smallX;
         double smallY;
         double W;
         double H;
         
         NodeView first = ( NodeView )selected_nodes_iterator.next();
         bigX = first.getXPosition();
         smallX = bigX;
         bigY = first.getYPosition();
         smallY = bigY;
     
         while ( selected_nodes_iterator.hasNext() ) {
           NodeView nv = ( NodeView )selected_nodes_iterator.next();
           double x = nv.getXPosition();
           double y = nv.getYPosition();
 
           if ( x > bigX ) {
             bigX = x;
           } else if ( x < smallX ) {
             smallX = x;
           }
 
           if ( y > bigY ) {
             bigY = y;
           } else if ( y < smallY ) {
             smallY = y;
           }
         }
         
        PBounds zoomToBounds;
        if (selected_nodes.size() == 1) {
          zoomToBounds = new PBounds( smallX - 100 , smallY - 100 , ( bigX - smallX + 200 ), ( bigY - smallY + 200 ) );
        } else {
          zoomToBounds = new PBounds( smallX  , smallY  , ( bigX - smallX + 100 ), ( bigY - smallY + 100 ) );
        }
         PTransformActivity activity =  ( ( PhoebeNetworkView )view).getCanvas().getCamera().animateViewToCenterBounds( zoomToBounds, true, 500 );
     }
 }
