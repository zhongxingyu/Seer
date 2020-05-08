 /*
  * The ISGCI specific implementation of the tabbedpane. Will create a
  * startpage upon creation. Tabs can (and should be created) via 
  * the {link {@link #addTab(Graph)} method, because it closes the startpage
  * and draws a new graph - which this class should be used for.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.jgrapht.Graph;
 import org.jgrapht.graph.DefaultEdge;
 
 import sun.security.util.PendingException;
 import teo.isgci.db.Algo;
 import teo.isgci.db.Algo.NamePref;
 import teo.isgci.drawing.JGraphXInterface;
 import teo.isgci.drawing.DrawingLibraryInterface;
 import teo.isgci.gc.GraphClass;
 import teo.isgci.problem.Complexity;
 import teo.isgci.problem.Problem;
 
 /**
  * The ISGCI specific implementation of the tabbedpane. Will create a
  * startpage upon creation. Tabs can (and should be created) via 
  * the {link {@link #addTab(Graph)} method, because it closes the startpage
  * and draws a new graph - which this class should be used for.
  */
 public class ISGCITabbedPane extends JTabbedPane {
 
     /**
      * The version of this class. Should be changed every time
      * this class is modified.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Indicates whether the startpage is currently a tab in the tabbedpane.
      */
     private boolean startpageActive = false;
     
     /**
      * The startpage that is displayed upon start of the window. 
      */
     private JPanel startpage;
 
     /**
      * Maps the content of the tabs to their corresponding
      * DrawingLibraryInterface.
      */
     private HashMap<JComponent, DrawingLibraryInterface> panelToInterfaceMap
         = new HashMap<JComponent, DrawingLibraryInterface>();
     
     /**
      * Maps the tab to their corresponding drawUnproper state.
      */
     private HashMap<JComponent, Boolean> panelToDrawUnproper
         = new HashMap<JComponent, Boolean>();
     
     /**
      * Maps the tab to their corresponding Problem.
      */
     private HashMap<JComponent, Problem> panelToProblem
         = new HashMap<JComponent, Problem>();
     
     /**
      * Maps the content of the tabs to their corresponding
      * naming preference.
      */
     private HashMap<JComponent, Algo.NamePref> panelToNamingPref
         = new HashMap<JComponent, Algo.NamePref>();
     
     /**
      * Maps a complexity to the corresponding color.
      */
     private HashMap<JComponent, HashMap<Complexity, Color>> 
         panelToComplexityColoring
         = new HashMap<JComponent, HashMap<Complexity, Color>>();
     
     /**
      * The mode which indicates the default preferred name of a Node.
      */
     private Algo.NamePref defaultMode = NamePref.BASIC;
     
     /**
      * Creates a new Tabbed pane with a startpage as only active tab.
      */
     public ISGCITabbedPane() {
         addStartpage();
         
         ChangeListener changeListener = new ChangeListener() {
             public void stateChanged(ChangeEvent changeEvent) {
                 
                 ISGCITabbedPane sourceTabbedPane = 
                         (ISGCITabbedPane) changeEvent.getSource();
                if (!sourceTabbedPane.startpageActive && (sourceTabbedPane.getSelectedIndex() >= 0)) {
                     ((ISGCIMainFrame) sourceTabbedPane.
                         getParent().getParent().getParent().getParent())
                         .setDrawUnproper(sourceTabbedPane
                         .getDrawUnproper(sourceTabbedPane
                         .getComponentAt(sourceTabbedPane.getSelectedIndex())));
                 }
             }
         };
         addChangeListener(changeListener);
     }
 
     /**
      * Adds a Startpage to the ISGCITabbedPane. Should only be called in the
      * Constructor.
      */
     private void addStartpage() {
         if (startpageActive) {
             return;
         }
         startpageActive = true;
         startpage = new JPanel();
         addTab("Welcome to ISGCI", startpage);
         setSelectedComponent(startpage);
         ButtonTabComponent closeButton = new ButtonTabComponent(this);
         this.setTabComponentAt(this.getSelectedIndex(), closeButton);
         resetDefaultColorScheme();
     }
 
     /**
      * Removes the startpage from the ISGCITabbedPane.
      */
     public void removeStartpage() {
         remove(startpage);
         startpageActive = false;
     }
     
     /**
      * Adds a new tab with a graph that is drawn via the DrawingInterface.
      * Will close the startpage if it's still open.
      * @param <V>
      *          The class of the vertices.
      * @param <E>
      *          The class of the edges.
      * @param graph
      *          The graph that will be drawn and interacted with within 
      *          this tab.
      * @param name
      *          The name of the Tab
      */
     public <V, E> void drawInNewTab(Graph<V, E> graph, String name) {
         if (startpageActive) {
             removeStartpage();
         }
         
         JGraphXInterface<V, E> graphXInterface 
         = new JGraphXInterface<V, E>(graph);
 
         JComponent panel = graphXInterface.getPanel();
         addTab(name, panel);
         setSelectedComponent(panel);
         ButtonTabComponent closeButton = new ButtonTabComponent(this);
         this.setTabComponentAt(this.getSelectedIndex(), closeButton);
         resetDefaultColorScheme();
         
         // save context for later reference
         panelToInterfaceMap.put(panel, graphXInterface);
         panelToNamingPref.put(panel, defaultMode);
     }
     
     /**
      * Draws the graph in the currently active tab. If the startpage is still
      * active, the startpage will be closed and a new tab will be created
      * instead.
      * 
      * @param graph
      *          The graph that will be drawn.
      *          
      * @param <V>
      *          The class of the vertex.
      *          
      * @param <E>
      *          The class of the edge.
      *          
      * @param name
      *          The name of the Tab
      */
     public <V, E> void drawInActiveTab(Graph<V, E> graph, String name) {
         if (startpageActive || getSelectedComponent() == null) {
             drawInNewTab(graph, name);
         } else {
             getActiveDrawingLibraryInterface().setGraph(graph);
             setTitleAt(getSelectedIndex(), name);
         }
     }
     
     /**
      * Returns the active DrawingLibraryInterface.
      * 
      * @return
      *          The DrawingLibraryInterface that is associated with the 
      *          currently active tab.
      */
     public DrawingLibraryInterface getActiveDrawingLibraryInterface() {
         Component panel = getSelectedComponent();
         if (panel == startpage || !panelToInterfaceMap.containsKey(panel)) {
             return null; // TODO ?
         }
         
         return panelToInterfaceMap.get(panel);
     }
 
     /**
      * 
      * @return 
      *          The default naming preference of this tabbed pane.
      */
     public NamePref getNamingPref() {
         return defaultMode;
     }
 
     /**
      * Changes the default naming preference of this tabbed pane.
      * 
      * @param pref
      *          The new default naming preference of this tabbed pane.
      */
     public void setNamingPref(NamePref pref) {
         this.defaultMode = pref;
     }
     
     /**
      * @param c 
      *          The tab for which the naming preference is wanted.
      * 
      * @return 
      *          The default naming preference of the given Tab.
      */
     public NamePref getNamingPref(Component c) {
         if (panelToNamingPref.containsKey(c)) {
             return panelToNamingPref.get(c);
         } else 
             return defaultMode;
     }
 
     /**
      * Changes the naming preferences of target tab.
      * 
      * @param pref
      *          The new default naming preference of this tab.
      * @param c
      *          The Tab for which the naming preference is changed.
      */
     public void setNamingPref(NamePref pref, Component c) {
         if (panelToNamingPref.containsKey(c)) {
             panelToNamingPref.remove(c);
             panelToNamingPref.put((JComponent) c, pref);
         }
     }
 
     /**
      * Sets the drawUnproper value for the currently open tab.
      * 
      * @param c
      *          The Tab for which the drawUnproper state is changed.
      * @param state
      *          the new drawUnproper state of the open tab
      */
     public void setDrawUnproper(boolean state, Component c) {
         if (panelToDrawUnproper.containsKey(getSelectedComponent())) {
             panelToDrawUnproper.remove(getSelectedComponent());
         }
         panelToDrawUnproper.put((JComponent) getSelectedComponent(), state);
     }
     
     /**
      * @param c
      *          The tab for which the drawUnproper state is wanted.
      * @return
      *          true if unproper inclusions shall be drawn, else false.
      */
     public boolean getDrawUnproper(Component c) {
         if (!panelToDrawUnproper.containsKey(getSelectedComponent())) {
             return true;
         } else
             return (panelToDrawUnproper.get(getSelectedComponent()));
     }
 
     /**
      * Sets the problem for a given tab. The problem of a tab determines the
      * coloring of the graph on it.
      * 
      * @param problem
      *          the new problem of the tab.
      *          
      * @param c
      *          the tab for which the problem is changed.
      */
     public void setProblem(Problem problem, Component c) {
        if (startpageActive) return;
         if (panelToProblem.containsKey(c)){
             panelToProblem.remove(c);
         }
         panelToProblem.put((JComponent) c, problem);
         Graph graph = getActiveDrawingLibraryInterface().getGraph();
         for (Object o : graph.vertexSet()) {
             Set<GraphClass> node = (Set<GraphClass>) o;
             getActiveDrawingLibraryInterface().getGraphManipulationInterface().
                 colorNode(node, complexityColor(node));            
         }
         getSelectedComponent().repaint();
     }
     
     /**
      * Gives the problem for a given tab. The problem of a tab determines the
      * coloring of the graph on it.
      * 
      * @param c
      *          the tab for which the problem is wanted
      *          
      * @return
      *          the problem of the given tab, 
      *          null or "none" if no problem is chosen
      */
     public Problem getProblem(Component c) {
         if (panelToProblem.containsKey(c)) {
             return panelToProblem.get(c);
         }
         return null;
     }
     
     /**
      * Sets a new color for the given complexity on the currently selected tab.
      * 
      * @param complexity
      *          the complexity for which the color is changed.
      *          
      * @param color
      *          the new color for the given complexity
      */
     public void setColor(Complexity complexity, Color color) {        
         HashMap<Complexity, Color> complexityToColor = 
                 new HashMap<Complexity, Color>();
         
         if (panelToComplexityColoring.containsKey(getSelectedComponent())) {
             complexityToColor =
                     panelToComplexityColoring.get(getSelectedComponent());
         } else {
             panelToComplexityColoring.put((JComponent) getSelectedComponent(), 
                     complexityToColor);
         }
         
         complexityToColor.put(complexity, color);        
     }
     
     /**
      * Resets the coloring scheme of the currently selected tab.
      */
     public void resetDefaultColorScheme() {        
         HashMap<Complexity, Color> complexityToColor = 
                 new HashMap<Complexity, Color>();
         
         if (panelToComplexityColoring.containsKey(getSelectedComponent())) {
             complexityToColor =
                     panelToComplexityColoring.get(getSelectedComponent());
         } else {
             panelToComplexityColoring.put((JComponent) getSelectedComponent(), 
                     complexityToColor);
         }
         
         complexityToColor.put(Complexity.LINEAR, Color.green);
         
         complexityToColor.put(Complexity.UNKNOWN, Color.white);
         complexityToColor.put(Complexity.OPEN, Color.white);
         
         complexityToColor.put(null, Color.white);
         
         complexityToColor.put(Complexity.P, Color.green.darker());
         
         complexityToColor.put(Complexity.CONPC, Color.red);
         complexityToColor.put(Complexity.NPC, Color.red);
         complexityToColor.put(Complexity.NPH, Color.red);
         
         complexityToColor.put(Complexity.GIC, Color.red.brighter());
     }
     
     /**
      * @param node 
      *          the node for which the color is returned.
      * 
      * @return 
      *          the color for node considering its complexity for the active
      *          problem.
      */
     protected Color complexityColor(Set<GraphClass> node) {
         Problem problem = getProblem(getSelectedComponent());
         Complexity complexity = problem.getComplexity(node.iterator().next());
         
         return panelToComplexityColoring.
                 get(getSelectedComponent()).get(complexity);
     }
 }
 
 /* EOF */
