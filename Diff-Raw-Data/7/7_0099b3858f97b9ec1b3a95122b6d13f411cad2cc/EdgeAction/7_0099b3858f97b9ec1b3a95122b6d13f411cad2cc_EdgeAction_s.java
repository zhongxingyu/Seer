 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.graphutil;
 
 import cytoscape.view.*;
 import cytoscape.browsers.*;
 import java.util.*;
 import phoebe.*;
 import giny.view.*;
 import edu.umd.cs.piccolo.*;
 import edu.umd.cs.piccolo.nodes.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class EdgeAction {
     final static String LINETYPEMENUCAPTION[] = { "Bezier", "Polyline" };
     final static int LINETYPEMENUVALUE[] = { PEdgeView.CURVED_LINES, PEdgeView.STRAIGHT_LINES };
     private static class LineTypeMenuAction extends AbstractAction {
         private int i;
         private PEdgeView ev;
         public LineTypeMenuAction(int index, PEdgeView edgeview) {
             super(LINETYPEMENUCAPTION[index]);
             i = index;
             ev = edgeview;
         }
         public void actionPerformed(ActionEvent e) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                     ev.setLineType(LINETYPEMENUVALUE[i]);
                 }
             });
         }
     }
     final static String ENDMENUCAPTION[] = {
         "None","White Delta","Black Delta","Edge Color Delta",
         "White Arrow","Black Arrow","Edge Color Arrow",
         "White Diamond","Black Diamond","Edge Color Diamond",
         "White Circle","Black Circle","Edge Color Circle",
         "White T","Black T","Edge Color T"};
     final static int ENDMENUVALUE[] = {
         PEdgeView.NO_END,PEdgeView.WHITE_DELTA,PEdgeView.BLACK_DELTA,PEdgeView.EDGE_COLOR_DELTA,
         PEdgeView.WHITE_ARROW,PEdgeView.BLACK_ARROW,PEdgeView.EDGE_COLOR_ARROW,
         PEdgeView.WHITE_DIAMOND,PEdgeView.BLACK_DIAMOND,PEdgeView.EDGE_COLOR_DIAMOND,
         PEdgeView.WHITE_CIRCLE,PEdgeView.BLACK_CIRCLE,PEdgeView.EDGE_COLOR_CIRCLE,
         PEdgeView.WHITE_T,PEdgeView.BLACK_T,PEdgeView.EDGE_COLOR_T};
     private static class SourceEndMenuAction extends AbstractAction {
         private int i;
         private PEdgeView ev;
         public SourceEndMenuAction(int index, PEdgeView edgeview) {
             super(ENDMENUCAPTION[index]);
             i = index;
             ev = edgeview;
         }
         public void actionPerformed(ActionEvent e) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                     ev.setSourceEdgeEnd(ENDMENUVALUE[i]);
                 }
             });
         }
     }
     private static class TargetEndMenuAction extends AbstractAction {
         private int i;
         private PEdgeView ev;
         public TargetEndMenuAction(int index, PEdgeView edgeview) {
             super(ENDMENUCAPTION[index]);
             i = index;
             ev = edgeview;
         }
         public void actionPerformed(ActionEvent e) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                     ev.setTargetEdgeEnd(ENDMENUVALUE[i]);
                 }
             });
         }
     }
     final static String WIDTHMENUCAPTION[] = {
         "1/2","1","2","3","4","8","16","32","64"};
     final static float WIDTHMENUVALUE[] = {
         0.5f,1f,2f,3f,4f,8f,16f,32f,64f};
     private static class WidthMenuAction extends AbstractAction {
         private int i;
         private PEdgeView ev;
         public WidthMenuAction(int index, PEdgeView edgeview) {
             super(WIDTHMENUCAPTION[index]);
             i = index;
             ev = edgeview;
         }
         public void actionPerformed(ActionEvent e) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                     ev.setStrokeWidth(WIDTHMENUVALUE[i]);
                 }
             });
         }
     }
 
 
   public EdgeAction () {
   }
 
   public static String getTitle ( Object[] args, PNode node ) {
     //System.out.println( "Getting Title" );
     final NetworkView nv = ( NetworkView )args[0];
     //return nv.getNetwork().getNodeAttributes().getCanonicalName( node );
 
     if ( node instanceof PEdgeView ) {
       return  nv.getNetwork().
         getEdgeAttributes().
         getCanonicalName(  nv.getNetwork().getGraphPerspective().
                            getEdge( ( (PEdgeView)node).getGraphPerspectiveIndex() ) );
     }
     //      return nv.getNetwork().getGraphPerspective().
     //    getEdge( ( (PEdgeView)node).getGraphPerspectiveIndex() ).
     //    getIdentifier();
 
     return "";
   }
 
   public static JMenuItem viewEdgeAttributeBrowser ( Object[] args, PNode node ) {
     final NetworkView network = ( NetworkView )args[0];
     final PEdgeView view = ( PEdgeView )node;
     return new JMenuItem( new AbstractAction( "Attribute Browser" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
 
                   List edges = network.getView().getSelectedEdges();
                   Object[] objects;
                   if ( !edges.isEmpty() ) {
                     if ( !view.isSelected() ) {
                       edges.add( view );
                     }
                     objects = new Object[ edges.size() ];
                     for ( int i = 0; i < edges.size(); ++i ) {
                       objects[i] = ( ( EdgeView )edges.get(i) ).getEdge();
                     }
 
                   } else {
                     objects =  new Object[] { view.getEdge() };
                   }
                   TabbedBrowser nodeBrowser = new TabbedBrowser (objects,
                                                     network.getNetwork().getEdgeAttributes(),
                                                     new Vector(),
                                                     network.getCytoscapeObj().
                                                     getConfiguration().getProperties().
                                                     getProperty("webBrowserScript",
                                                                  "noScriptDefined") ,
                                                     TabbedBrowser.BROWSING_NODES );
      } } ); } } );
   }
 
   public static JMenuItem editEdge ( Object[] args, PNode node ) {
     final NetworkView network = ( NetworkView )args[0];
     final GraphView v = network.getView();
     final PEdgeView ev = ( PEdgeView )node;
 
     JMenu editMenu = new JMenu("Edit Edge");
     JCheckBoxMenuItem jmi;
     int i;
     JMenu lineTypeMenu = new JMenu("Curve Type");
     for (i = 0; i < LINETYPEMENUCAPTION.length; i++) {
         jmi = new JCheckBoxMenuItem( new LineTypeMenuAction(i, ev));
         jmi.setSelected(ev.getLineType() == LINETYPEMENUVALUE[i]);
         lineTypeMenu.add(jmi);
     }
     editMenu.add(lineTypeMenu);
     JMenu sourceEndMenu = new JMenu("Source Edge End");
     for (i = 0; i < ENDMENUCAPTION.length; i++) {
         jmi = new JCheckBoxMenuItem( new SourceEndMenuAction(i, ev));
/*TODO: uncomment when giny is updated to include getSourceEdgeEnd
         jmi.setSelected(ev.getSourceEdgeEnd() == ENDMENUVALUE[i]);
*/
         sourceEndMenu.add(jmi);
     }
     editMenu.add(sourceEndMenu);
     JMenu targetEndMenu = new JMenu("Target Edge End");
     for (i = 0; i < ENDMENUCAPTION.length; i++) {
         jmi = new JCheckBoxMenuItem( new TargetEndMenuAction(i, ev));
/*TODO: uncomment when giny is updated to include getTargetEdgeEnd
         jmi.setSelected(ev.getTargetEdgeEnd() == ENDMENUVALUE[i]);
*/
         targetEndMenu.add(jmi);
     }
     editMenu.add(targetEndMenu);
     JMenu widthMenu = new JMenu("Width (pts)");
       for (i = 0; i < WIDTHMENUCAPTION.length; i++) {
           jmi = new JCheckBoxMenuItem( new WidthMenuAction(i, ev));
         jmi.setSelected(ev.getStrokeWidth() == WIDTHMENUVALUE[i]);
 
           widthMenu.add(jmi);
       }
       editMenu.add(widthMenu);
 
      editMenu.add( new JMenuItem( new AbstractAction( "<html>Color <small><i>(short-term)</i></small></html>" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                  JColorChooser color = new JColorChooser();
                  ev.setUnselectedPaint( color.showDialog( v.getComponent() , "Choose a Node Color", (java.awt.Color)ev.getUnselectedPaint() ) );
                } } ); } } ) );
 
      return editMenu;
 
   }
 
   /**
    * This will open an web page that will give you more info.
    */
   public static JMenuItem openWebInfo ( Object[] args, PNode node ) {
 
     final PNode nv = node;
 
     JMenu web_menu = new JMenu( "Web Info" );
 
     web_menu.add(  new JMenuItem( new AbstractAction( "<html>SGD <small><i>yeast only</i></small></html>" ) {
         public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL( "http://db.yeastgenome.org/cgi-bin/SGD/locus.pl?locus="+gene );
 
                 } } ); } } ) );
 
     web_menu.add(  new JMenuItem( new AbstractAction( "Google" ) {
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL( "http://www.google.com/search?q="+gene);
 
                 } } ); } } ) );
 
 
     JMenu gn_menu = new JMenu( "GenomeNet" );
     web_menu.add( gn_menu );
     gn_menu.add(  new JMenuItem( new AbstractAction( "Pathway" ) {
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=pathway&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "KO" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=ko&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Genes" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=genes&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Genome" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=genome&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Ligand" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=ligand&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Compound" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=compound&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Glycan" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                       //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=glycan&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Reaction" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=reaction&keywords="+gene );
 
                 } } ); } } ));
 
     gn_menu.add(  new JMenuItem( new AbstractAction( "Enzyme" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=enzyme&keywords="+gene );
 
                 } } ); } }) );
 
            gn_menu.addSeparator();
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "Swiss-Prot" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=swissprot&keywords="+gene );
 
                 } } ); } }) );
 
 
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "Ref-Seq" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=refseq&keywords="+gene );
 
                 } } ); } } ));
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "GenBank" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=genbank&keywords="+gene );
 
                 } } ); } } ));
 
            gn_menu.add(  new JMenuItem( new AbstractAction( "Embl" ){
             public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   String gene = null;
                   if ( nv instanceof PEdgeView ) {
                     gene = ( ( PEdgeView ) nv).getLabel().getText();
                   }
                   //System.out.println( "Node: "+nv.getLabel() );
                   //System.out.println( "GEne: "+gene );
                   if ( gene == null ) {
                     gene = ( String )nv.getClientProperty("tooltip");
                     //System.out.println( "Gene: "+gene );
                   }
                   OpenBrowser.openURL("http://www.genome.ad.jp/dbget-bin/www_bfind_sub?mode=bfind&max_hit=1000&dbkey=embl&keywords="+gene );
 
                 } } ); } } ));
 
            return web_menu;
 
    }
 
   //----------------------------------------//
   // Edge Ends
   //----------------------------------------//
 
    public static JMenuItem edgeEndBorderColor (Object[] args, PNode node ) {
     final PGraphView v = ( PGraphView )args[0];
     final PPath icon = ( PPath )node;
 
     return new JMenuItem( new AbstractAction( "Choose Border Color" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                  JColorChooser color = new JColorChooser();
                  icon.setStrokePaint( color.showDialog( v.getComponent() , "Choose a Border Color", (java.awt.Color)icon.getPaint() ) );
                } } ); } } );
 
   }
 
   public static JMenuItem edgeEndColor (Object[] args, PNode node ) {
     final PPath icon = ( PPath )node;
     final PGraphView v = ( PGraphView )args[0];
 
     return new JMenuItem( new AbstractAction( "Color" ) {
          public void actionPerformed ( ActionEvent e ) {
            // Do this in the GUI Event Dispatch thread...
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                  JColorChooser color = new JColorChooser();
                  icon.setPaint( color.showDialog( v.getComponent() , "Border Color", (java.awt.Color)icon.getPaint() ) );
                } } ); } } );
   }
 
 }
