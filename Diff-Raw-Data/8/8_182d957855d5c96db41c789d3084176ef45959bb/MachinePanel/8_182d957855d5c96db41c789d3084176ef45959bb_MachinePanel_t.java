 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.filechooser.FileFilter;
 
 import org.jgraph.JGraph;
 import org.jgraph.graph.DefaultEdge;
 import org.jgraph.graph.DefaultGraphCell;
 import org.jgraph.graph.DefaultGraphModel;
 import org.jgraph.graph.GraphConstants;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.State;
 import de.unisiegen.gtitool.core.entities.Transition;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineException;
 import de.unisiegen.gtitool.core.exceptions.state.StateException;
 import de.unisiegen.gtitool.core.machines.Machine;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 import de.unisiegen.gtitool.ui.EditorPanel;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.jgraphcomponents.DefaultStateView;
 import de.unisiegen.gtitool.ui.jgraphcomponents.DefaultTransitionView;
 import de.unisiegen.gtitool.ui.jgraphcomponents.GPCellViewFactory;
 import de.unisiegen.gtitool.ui.model.ConsoleColumnModel;
 import de.unisiegen.gtitool.ui.model.ConsoleTableModel;
 import de.unisiegen.gtitool.ui.model.DefaultMachineModel;
 import de.unisiegen.gtitool.ui.model.MachineColumnModel;
 import de.unisiegen.gtitool.ui.netbeans.MachinesPanelForm;
 import de.unisiegen.gtitool.ui.popup.DefaultPopupMenu;
 import de.unisiegen.gtitool.ui.popup.StatePopupMenu;
 import de.unisiegen.gtitool.ui.popup.TransitionPopupMenu;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.preferences.listener.ColorChangedAdapter;
 import de.unisiegen.gtitool.ui.storage.Storage;
 
 
 /**
  * The Panel containing the diagramm and table representing a machine
  * 
  * @author Benjamin Mies
  * @version $Id$
  */
 public class MachinePanel implements EditorPanel
 {
 
   //
   // Attributes
   //
   /** the parent window */
   private JFrame parent;
 
 
   /** The {@linkMachinesPanelForm} */
   private MachinesPanelForm gui;
 
 
   /** The {@link DefaultMachineModel} */
   private DefaultMachineModel model;
 
 
   /** The {@link Machine} */
   private Machine machine;
 
 
   /** The {@link JGraph} containing the diagramm */
   private JGraph graph;
 
 
   /** The {@link DefaultGraphModel} for this graph */
   private DefaultGraphModel graphModel;
 
 
   /** The {@link MouseAdapter} for the mouse icon in the toolbar */
   private MouseAdapter mouse;
 
 
   /** The {@link MouseAdapter} for the add State icon in the toolbar */
   private MouseAdapter addState;
 
 
   /** The {@link MouseAdapter} for the transition icon in the toolbar */
   private MouseAdapter transition;
 
 
   /** The {@link MouseAdapter} for the transition icon in the toolbar */
   private MouseMotionAdapter transitionMove;
 
 
   /** The {@link MouseAdapter} for the start icon in the toolbar */
   private MouseAdapter start;
 
 
   /** The {@link MouseAdapter} for the end icon in the toolbar */
   private MouseAdapter end;
 
 
   /** The source state for a new Transition */
   private DefaultStateView firstState;
 
 
   /** The tmp state for a new Transition */
   private DefaultGraphCell tmpState;
 
 
   /** The tmp transition */
   private DefaultEdge tmpTransition;
 
 
   /** Signals if drag in progress */
   private boolean dragged;
 
 
   /** The alphabet of this Machine */
   private Alphabet alphabet;
 
 
   /** The zoom factor for this graph */
   private double zoomFactor;
 
 
   /** The {@link ConsoleTableModel} for the warning table */
   private ConsoleTableModel warningTableModel;
 
 
   /** The {@link ConsoleTableModel} for the error table */
   private ConsoleTableModel errorTableModel;
 
 
   /** The actual highlighted error states */
   private ArrayList < DefaultStateView > oldErrorStates = new ArrayList < DefaultStateView > ();
 
 
   /** The actual highlighted error transitions */
   private ArrayList < DefaultTransitionView > oldErrorTransitions = new ArrayList < DefaultTransitionView > ();
 
 
   /** The {@link JPopupMenu} */
   private JPopupMenu popup;
 
 
   /**
    * Create a temporary Object to paint the Transiton on Mouse move
    * 
    * @param x the x position of the new state view
    * @param y the y position of the new state view
    * @return {@link DefaultGraphCell} the new created tmp Object
    */
   private DefaultGraphCell createTmpObject ( double x, double y )
   {
     String viewClass = "de.unisiegen.gtitool.ui.jgraphcomponents.StateView"; //$NON-NLS-1$
 
     DefaultGraphCell cell = new DefaultGraphCell ();
 
     // set the view class (indirection for the renderer and the editor)
     GPCellViewFactory.setViewClass ( cell.getAttributes (), viewClass );
 
     // Set bounds
     GraphConstants.setBounds ( cell.getAttributes (), new Rectangle2D.Double (
         x, y, 1, 1 ) );
 
     GraphConstants.setBorder ( cell.getAttributes (), BorderFactory
         .createRaisedBevelBorder () );
 
     // Set the line width
     GraphConstants.setLineWidth ( cell.getAttributes (), 2 );
 
     // Add a Floating Port
     cell.addPort ();
 
     return cell;
 
   }
 
 
   /**
    * Create a new Machine Panel Object
    * 
    * @param pParent The parent frame
    * @param pModel the {@link DefaultMachineModel} of this panel
    */
   public MachinePanel ( JFrame pParent, DefaultMachineModel pModel )
   {
     this.parent = pParent;
     this.model = pModel;
     this.machine = pModel.getMachine ();
     this.alphabet = this.machine.getAlphabet ();
     this.gui = new MachinesPanelForm ();
     this.gui.setMachinePanel ( this );
     this.graph = this.model.getJGraph ();
     this.graphModel = this.model.getGraphModel ();
     this.zoomFactor = ( ( double ) PreferenceManager.getInstance ()
         .getZoomFactorItem ().getFactor () ) / 100;
     intitializeMouseAdapter ();
     this.graph.addMouseListener ( this.mouse );
     this.gui.diagrammContentPanel.setViewportView ( this.graph );
 
     this.errorTableModel = new ConsoleTableModel ();
     this.gui.jTableErrors.setModel ( this.errorTableModel );
     this.gui.jTableErrors.setColumnModel ( new ConsoleColumnModel () );
     this.warningTableModel = new ConsoleTableModel ();
     this.gui.jTableWarnings.setModel ( this.warningTableModel );
     this.gui.jTableWarnings.setColumnModel ( new ConsoleColumnModel () );
     this.gui.jTableMachine.setModel ( this.model.getTableModel () );
     this.gui.jTableMachine.setColumnModel ( new MachineColumnModel (
         this.machine.getAlphabet () ) );
 
     PreferenceManager.getInstance ().addColorChangedListener (
         new ColorChangedAdapter ()
         {
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedStartState ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedStartState ( Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultStateView state = ( DefaultStateView ) object;
                 if ( state.getState ().isStartState () )
                   GraphConstants.setGradientColor ( state.getAttributes (),
                       pNewColor );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedState ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedState ( Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultStateView state = ( DefaultStateView ) object;
                 if ( !state.getState ().isStartState () )
                   GraphConstants.setGradientColor ( state.getAttributes (),
                       pNewColor );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedTransition ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedTransition ( Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultTransitionView t = ( DefaultTransitionView ) object;
                 GraphConstants.setLineColor ( t.getAttributes (), pNewColor );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedSymbol ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedSymbol ( @SuppressWarnings ( "unused" )
           Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultTransitionView t = ( DefaultTransitionView ) object;
                 GraphConstants.setLabelColor ( t.getAttributes (),
                     PreferenceManager.getInstance ().getColorItemSymbol ()
                         .getColor () );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
         } );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.gtitool.ui.EditorPanel#getPanel()
    */
   public JPanel getPanel ()
   {
     return this.gui;
   }
 
 
   /**
    * Handle Toolbar Add State button value changed
    * 
    * @param state The new State of the Add State Toolbar button
    */
   public void handleToolbarAddState ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.addState );
     else
       this.graph.removeMouseListener ( this.addState );
   }
 
 
   /**
    * Handle Toolbar Alphabet button action event
    */
   public void handleToolbarAlphabet ()
   {
     JDialog changeAlphabetDialog = new JDialog ( this.parent, true );
 
     EditAlphabetPanel editAlphabetPanel = new EditAlphabetPanel ( this.machine );
     changeAlphabetDialog.add ( editAlphabetPanel.getPanel () );
 
     changeAlphabetDialog.setTitle ( Messages
         .getString ( "PreferencesDialog.AlphabetEdit" ) ); //$NON-NLS-1$
     changeAlphabetDialog.pack ();
     int x = this.parent.getBounds ().x + ( this.parent.getWidth () / 2 )
         - ( changeAlphabetDialog.getWidth () / 2 );
     int y = this.parent.getBounds ().y + ( this.parent.getHeight () / 2 )
         - ( changeAlphabetDialog.getHeight () / 2 );
     changeAlphabetDialog.setBounds ( x, y, changeAlphabetDialog.getWidth (),
         changeAlphabetDialog.getHeight () );
     changeAlphabetDialog.setVisible ( true );
   }
 
 
   /**
    * Handle Toolbar End button value changed
    * 
    * @param state The new State of the End Toolbar button
    */
   public void handleToolbarEnd ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.end );
     else
       this.graph.removeMouseListener ( this.end );
   }
 
 
   /**
    * Handle Toolbar Mouse button value changed
    * 
    * @param state The new State of the Mouse Toolbar button
    */
   public void handleToolbarMouse ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.mouse );
     else
       this.graph.removeMouseListener ( this.mouse );
   }
 
 
   /**
    * Handle Toolbar Start button value changed
    * 
    * @param state The new State of the Start Toolbar button
    */
   public void handleToolbarStart ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.start );
     else
       this.graph.removeMouseListener ( this.start );
   }
 
 
   /**
    * Handle Toolbar Transition button value changed
    * 
    * @param state The new State of the Transition Toolbar button
    */
   public void handleToolbarTransition ( boolean state )
   {
     if ( state )
     {
       this.graph.addMouseListener ( this.transition );
       this.graph.addMouseMotionListener ( this.transitionMove );
     }
     else
     {
       this.graph.removeMouseListener ( this.transition );
       this.graph.removeMouseMotionListener ( this.transitionMove );
     }
   }
 
 
   /**
    * Initialize the Mouse Adapter of the Toolbar
    */
   @SuppressWarnings ( "synthetic-access" )
   private void intitializeMouseAdapter ()
   {
     this.mouse = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse has been clicked on a component.
        */
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // Return if pressed Button is not the left mouse button
         if ( e.getButton () != MouseEvent.BUTTON3 )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open a new popup menu
         DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
             .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                 .getY () );
         if ( object == null )
           MachinePanel.this.popup = createPopupMenu ();
         else if ( object instanceof DefaultTransitionView )
           MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
         else
           MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
         if ( MachinePanel.this.popup != null )
           MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
               .getX (), e.getY () );
       }
     };
 
     this.addState = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse button has been clicked (pressed and released)
        * on a component.
        */
       @SuppressWarnings ( "synthetic-access" )
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3 )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         try
         {
           State newState = new State ( MachinePanel.this.alphabet, false, false );
 
           MachinePanel.this.model.createStateView ( e.getPoint ().x
               / MachinePanel.this.zoomFactor, e.getPoint ().y
               / MachinePanel.this.zoomFactor, newState );
         }
         catch ( StateException e1 )
         {
           e1.printStackTrace ();
           System.exit ( 1 );
         }
 
       }
     };
 
     this.transition = new MouseAdapter ()
     {
 
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3
             && MachinePanel.this.firstState == null )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         // if drag in progress return
         if ( MachinePanel.this.dragged )
           return;
 
         if ( MachinePanel.this.firstState == null )
         {
           MachinePanel.this.firstState = ( DefaultStateView ) MachinePanel.this.graph
               .getSelectionCellAt ( e.getPoint () );
           if ( MachinePanel.this.firstState == null )
             return;
         }
         else
         {
           DefaultStateView target = null ;
           try
           {
 
             target = ( DefaultStateView ) MachinePanel.this.graph
                 .getNextCellForLocation ( MachinePanel.this.tmpState, e
                     .getPoint ().getX (), e.getPoint ().getY () );
 
             MachinePanel.this.graphModel.remove ( new Object []
             { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
 
           }
 
           catch ( ClassCastException exc )
           {
             MachinePanel.this.graphModel.remove ( new Object []
                                                               { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
           }
           TransitionDialog dialog = new TransitionDialog (
               MachinePanel.this.parent, MachinePanel.this.alphabet,
               MachinePanel.this.firstState.getState (), target == null ? null
                   : target.getState () );
           dialog.show ();
           if ( dialog.DIALOG_RESULT == TransitionDialog.DIALOG_CONFIRMED )
           {
             Transition newTransition = dialog.getTransition ();
             if ( target == null )
             {
 
               try
               {
                 State newState = new State ( MachinePanel.this.alphabet, false,
                     false );
                 target = MachinePanel.this.model.createStateView ( e
                     .getPoint ().x
                     / MachinePanel.this.zoomFactor, e.getPoint ().y
                     / MachinePanel.this.zoomFactor, newState );
                 newTransition.setStateEnd ( target.getState () );
 
               }
               catch ( StateException e1 )
               {
                 e1.printStackTrace ();
                 System.exit ( 1 );
                 return;
               }
 
             }
 
             MachinePanel.this.model.createTransitionView ( newTransition,
                 MachinePanel.this.firstState, target );
             dialog.dispose ();
           }
           MachinePanel.this.firstState = null;
           MachinePanel.this.tmpTransition = null;
           MachinePanel.this.tmpState = null;
         }
       }
 
 
       @Override
       public void mouseReleased ( MouseEvent e )
       {
         if ( e.getButton () != MouseEvent.BUTTON1 )
           return;
 
         if ( !MachinePanel.this.dragged || MachinePanel.this.firstState == null )
           return;
 
         DefaultStateView target = null;
 
         try
         {
           target = ( DefaultStateView ) MachinePanel.this.graph
               .getNextCellForLocation ( MachinePanel.this.tmpState, e
                   .getPoint ().getX (), e.getPoint ().getY () );
 
           MachinePanel.this.graphModel.remove ( new Object []
           { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
 
           if ( target != null && target.equals ( MachinePanel.this.firstState ) )
             return;
         }
         catch ( ClassCastException exc )
         {
           MachinePanel.this.graphModel.remove ( new Object []
                                                             { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
         }
 
         TransitionDialog dialog = new TransitionDialog (
             MachinePanel.this.parent, MachinePanel.this.alphabet,
             MachinePanel.this.firstState.getState (), target == null ? null
                 : target.getState () );
         dialog.show ();
         if ( dialog.DIALOG_RESULT == TransitionDialog.DIALOG_CONFIRMED )
         {
           Transition newTransition = dialog.getTransition ();
           if ( target == null )
           {
             try
             {
               State newState;
               newState = new State ( MachinePanel.this.alphabet, false, false );
               target = MachinePanel.this.model.createStateView (
                   e.getPoint ().x / MachinePanel.this.zoomFactor,
                   e.getPoint ().y / MachinePanel.this.zoomFactor, newState );
               newTransition.setStateEnd ( target.getState () );
             }
             catch ( StateException e1 )
             {
               e1.printStackTrace ();
               System.exit ( 1 );
               return;
             }
           }
 
           MachinePanel.this.model.createTransitionView ( newTransition,
               MachinePanel.this.firstState, target );
           dialog.dispose ();
 
         }
         MachinePanel.this.firstState = null;
         MachinePanel.this.tmpTransition = null;
         MachinePanel.this.tmpState = null;
         MachinePanel.this.dragged = false;
 
       }
 
     };
 
     this.transitionMove = new MouseMotionAdapter ()
     {
 
       @Override
       public void mouseDragged ( MouseEvent e )
       {
         double x, y;
         if ( MachinePanel.this.firstState == null )
         {
           MachinePanel.this.dragged = true;
           MachinePanel.this.firstState = ( DefaultStateView ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
         }
 
         else
         {
           // Remove old tmp state and transition
           MachinePanel.this.graphModel.remove ( new Object []
           { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
           x = e.getX () / MachinePanel.this.zoomFactor;
           y = e.getY () / MachinePanel.this.zoomFactor;
           MachinePanel.this.tmpState = createTmpObject ( x, y );
           MachinePanel.this.graph.getGraphLayoutCache ().insert (
               MachinePanel.this.tmpState );
 
           MachinePanel.this.tmpTransition = new DefaultEdge ( "" ); //$NON-NLS-1$
           GraphConstants.setLineEnd ( MachinePanel.this.tmpTransition
               .getAttributes (), GraphConstants.ARROW_CLASSIC );
           GraphConstants.setEndFill ( MachinePanel.this.tmpTransition
               .getAttributes (), true );
 
           MachinePanel.this.graph.getGraphLayoutCache ().insertEdge (
               MachinePanel.this.tmpTransition,
               MachinePanel.this.firstState.getChildAt ( 0 ),
               MachinePanel.this.tmpState.getChildAt ( 0 ) );
         }
       }
 
 
       /**
        * Invoked when the mouse button has been moved on a component (with no
        * buttons no down).
        */
       @Override
       public void mouseMoved ( MouseEvent e )
       {
         double x, y;
 
         if ( MachinePanel.this.firstState != null )
         {
           // Remove old tmp state and transition
           MachinePanel.this.graphModel.remove ( new Object []
           { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
 
           x = e.getX () / MachinePanel.this.zoomFactor;
           y = e.getY () / MachinePanel.this.zoomFactor;
           // x = MachinePanel.this.firstStatePosition.x < x ? x - 5 : x + 5;
           // y = MachinePanel.this.firstStatePosition.y < x ? y - 3 : y + 10;
           MachinePanel.this.tmpState = createTmpObject ( x, y );
           MachinePanel.this.graph.getGraphLayoutCache ().insert (
               MachinePanel.this.tmpState );
 
           MachinePanel.this.tmpTransition = new DefaultEdge ( "" ); //$NON-NLS-1$
           GraphConstants.setLineEnd ( MachinePanel.this.tmpTransition
               .getAttributes (), GraphConstants.ARROW_CLASSIC );
           GraphConstants.setEndFill ( MachinePanel.this.tmpTransition
               .getAttributes (), true );
 
           MachinePanel.this.graph.getGraphLayoutCache ().insertEdge (
               MachinePanel.this.tmpTransition,
               MachinePanel.this.firstState.getChildAt ( 0 ),
               MachinePanel.this.tmpState.getChildAt ( 0 ) );
         }
       }
     };
 
     this.start = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse button has been clicked (pressed and released)
        * on a component.
        */
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3 )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         try
         {
           State newState = new State ( MachinePanel.this.alphabet, true, false );
           MachinePanel.this.model.createStateView ( e.getPoint ().x
               / MachinePanel.this.zoomFactor, e.getPoint ().y
               / MachinePanel.this.zoomFactor, newState );
         }
         catch ( StateException e1 )
         {
           e1.printStackTrace ();
           System.exit ( 1 );
         }
 
       }
     };
 
     this.end = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse button has been clicked (pressed and released)
        * on a component.
        */
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3 )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         try
         {
           State newState = new State ( MachinePanel.this.alphabet, false, true );
           MachinePanel.this.model.createStateView ( e.getPoint ().x
               / MachinePanel.this.zoomFactor, e.getPoint ().y
               / MachinePanel.this.zoomFactor, newState );
         }
         catch ( StateException e1 )
         {
           e1.printStackTrace ();
           System.exit ( 1 );
         }
       }
     };
   }
 
 
   /**
    * Create a standard Popup Menu
    * 
    * @return the new created Popup Menu
    */
   private DefaultPopupMenu createPopupMenu ()
   {
     int factor = ( new Double ( this.zoomFactor * 100 ) ).intValue ();
     return new DefaultPopupMenu ( this, this.machine, factor );
   }
 
 
   /**
    * Create a new Popup Menu for the given Transition
    * 
    * @param pTransition the Transition for to create a popup menu
    * @return the new created Popup Menu
    */
   private TransitionPopupMenu createTransitionPopupMenu (
       DefaultTransitionView pTransition )
   {
     return new TransitionPopupMenu ( this.graph, this.gui, this.model,
         pTransition, this.alphabet );
   }
 
 
   /**
    * Create a new Popup Menu for the given State
    * 
    * @param pState the State for to create a popup menu
    * @return the new created Popup Menu
    */
   private StatePopupMenu createStatePopupMenu ( DefaultStateView pState )
   {
     return new StatePopupMenu ( this.parent, this.graph, this.model,
         pState );
   }
 
 
   /**
    * Set the zoom factor for this panel
    * 
    * @param pFactor the new zoom factor
    */
   public void setZoomFactor ( double pFactor )
   {
     this.zoomFactor = pFactor;
     this.graph.setScale ( pFactor );
   }
 
 
   /**
    * Getter for the {@link Machine}
    * 
    * @return the {@link Machine} of this panel
    */
   public Machine getMachine ()
   {
     return this.machine;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.gtitool.ui.EditorPanel#getAlphabet()
    */
   public Alphabet getAlphabet ()
   {
     return this.machine.getAlphabet ();
   }
 
 
   /**
    * Add a new Error
    * 
    * @param e The {@link MachineException} containing the data
    */
   public void addError ( MachineException e )
   {
     this.errorTableModel.addRow ( e );
   }
 
 
   /**
    * Add a new Warning
    * 
    * @param e The {@link MachineException} containing the data
    */
   public void addWarning ( MachineException e )
   {
     this.warningTableModel.addRow ( e );
   }
 
 
   /**
    * Handle Row Selection for the tables
    * 
    * @param e {@link MouseEvent}
    */
   @SuppressWarnings ( "unchecked" )
   public void handleTableMouseEvent ( MouseEvent e )
   {
     JTable table = ( JTable ) e.getSource ();
     int index = table.rowAtPoint ( e.getPoint () );
 
     if ( index >= 0 )
     {
       highlightStates ( ( ArrayList < State > ) table.getModel ().getValueAt (
           index, ConsoleTableModel.STATES_COLUMN ) );
       highlightTransitions ( ( ArrayList < Transition > ) table.getModel ()
           .getValueAt ( index, ConsoleTableModel.TRANSITIONS_COLUMN ) );
     }
   }
 
 
   /**
    * Highlight the affected states
    * 
    * @param states list with all states that are affected
    */
   private void highlightStates ( ArrayList < State > states )
   {
     for ( DefaultStateView view : this.oldErrorStates )
     {
       if ( view.getState ().isStartState () )
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemStartState ()
                 .getColor () );
       else
       {
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemState ().getColor () );
       }
     }
     this.oldErrorStates.clear ();
     for ( State state : states )
     {
       DefaultStateView view = this.model.getStateViewForState ( state );
       this.oldErrorStates.add ( view );
       GraphConstants.setGradientColor ( view.getAttributes (),
           PreferenceManager.getInstance ().getColorItemErrorState ()
               .getColor () );
     }
     this.graphModel.cellsChanged ( DefaultGraphModel
         .getAll ( MachinePanel.this.graphModel ) );
   }
 
 
   /**
    * Highlight the affected transitions
    * 
    * @param transitions list with all transitions that are affected
    */
   private void highlightTransitions ( ArrayList < Transition > transitions )
   {
     for ( DefaultTransitionView view : this.oldErrorTransitions )
     {
       GraphConstants.setLineColor ( view.getAttributes (), PreferenceManager
           .getInstance ().getColorItemTransition ().getColor () );
     }
     this.oldErrorTransitions.clear ();
 
     for ( Transition t : transitions )
     {
       DefaultTransitionView view = this.model
           .getTransitionViewForTransition ( t );
       this.oldErrorTransitions.add ( view );
       GraphConstants.setLineColor ( view.getAttributes (), PreferenceManager
           .getInstance ().getColorItemErrorTransition ().getColor () );
     }
     this.graphModel.cellsChanged ( DefaultGraphModel
         .getAll ( MachinePanel.this.graphModel ) );
   }
 
 
   /**
    * Remove all highlighting
    */
   public void clearHighlight ()
   {
     for ( DefaultTransitionView view : this.oldErrorTransitions )
     {
       GraphConstants.setLineColor ( view.getAttributes (), PreferenceManager
           .getInstance ().getColorItemTransition ().getColor () );
     }
 
     for ( DefaultStateView view : this.oldErrorStates )
     {
       if ( view.getState ().isStartState () )
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemStartState ()
                 .getColor () );
       else
       {
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemState ().getColor () );
       }
     }
     this.graphModel.cellsChanged ( DefaultGraphModel
         .getAll ( MachinePanel.this.graphModel ) );
   }
 
 
   /**
    * Clear all Error and Warning messages
    */
   public void clearValidationMessages ()
   {
     this.errorTableModel.clearData ();
     this.warningTableModel.clearData ();
 
   }
 
 
   /**
    * Handle save operation
    */
   public void handleSave ()
   {
     try
     {
       PreferenceManager prefmanager = PreferenceManager.getInstance ();
       JFileChooser chooser = new JFileChooser ( prefmanager.getWorkingPath () );
       chooser.setMultiSelectionEnabled ( false );
       chooser.setAcceptAllFileFilterUsed ( false );
       chooser.addChoosableFileFilter ( new FileFilter ()
       {
 
         @Override
         public boolean accept ( File file )
         {
           if ( file.isDirectory () )
           {
             return true;
           }
           if ( file.getName ().matches ( ".+\\.dfa" ) ) //$NON-NLS-1$
             return true;
           return false;
 
         }
 
 
         @Override
         public String getDescription ()
         {
           return Messages.getString ( "NewDialog.DFA" ) + " (*.dfa)"; //$NON-NLS-1$ //$NON-NLS-2$ /$NON-NLS-2$
         }
 
       } );
       int n = chooser.showSaveDialog ( this.parent );
       if ( n == JFileChooser.CANCEL_OPTION
           || chooser.getSelectedFile () == null )
         return;
       String filename = chooser.getSelectedFile ().toString ().matches (
           ".+\\.dfa" ) ? //$NON-NLS-1$
       chooser.getSelectedFile ().toString ()
           : chooser.getSelectedFile ().toString () + ".dfa"; //$NON-NLS-1$
 
       Storage.getInstance ().store ( this.model, filename );
       JOptionPane
           .showMessageDialog (
               this.parent,
              Messages.getString ( "MachinePanel.DataSaved" ), Messages.getString ( "MachinePanel.Save" ), JOptionPane.INFORMATION_MESSAGE ); //$NON-NLS-1$//$NON-NLS-2$
       prefmanager.setWorkingPath ( chooser.getCurrentDirectory ()
           .getAbsolutePath () );
 
     }
     catch ( StoreException e )
     {
       JOptionPane.showMessageDialog ( this.parent, e.getMessage (), Messages
           .getString ( "MachinePanel.Save" ), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
     }
   }
 }
