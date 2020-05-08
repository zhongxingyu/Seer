 package binevi.View;
 
 import binevi.IO.BioPAXHandler;
 import binevi.IO.SXGIOHandler;
 import binevi.Parser.PathCaseParsers.PathCaseXMLParser;
 import binevi.Parser.PathCaseParsers.SysBioXMLParser;
 import binevi.Parser.PathCaseParsers.MetaboliteXMLParser;
 import binevi.Parser.PathCaseParsers.MQLXMLParser;
 import binevi.Resources.PathCaseResources.*;
 import edu.cwru.nashua.pathwaysservice.ExportSBML;
 import edu.cwru.nashua.pathwaysservice.PathwaysService;
 import edu.cwru.nashua.pathwaysservice.PathwaysServiceMetabolomics;
 import y.base.*;
 import y.io.GMLIOHandler;
 import y.io.JPGIOHandler;
 import y.io.YGFIOHandler;
 //import y.io.IOHandler;
 import y.io.gml.EncoderFactory;
 import y.io.gml.ParserFactory;
 import y.option.OptionHandler;
 import y.util.D;
 import y.util.Maps;
 import y.view.*;
 import y.view.hierarchy.HierarchyManager;
 import y.view.hierarchy.AutoBoundsFeature;
 import y.view.hierarchy.GroupNodeRealizer;
 import y.layout.*;
 
 import javax.swing.*;
 import javax.xml.parsers.ParserConfigurationException;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.print.PageFormat;
 import java.awt.print.PrinterJob;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.Queue;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.io.*;
 
 //import yext.graphml.writer.AttrDataProviderOutputHandler;
 //import yext.graphml.writer.YGraphElementProvider;
 //import yext.graphml.reader.AttrDataAcceptorInputHandler;
 //import yext.graphml.reader.YGraphElementFactory;
 //import yext.graphml.graph2D.GraphMLIOHandler;
 //import org.graphdrawing.graphml.attr.AttributeConstants;
 //import org.graphdrawing.graphml.writer.DirectGraphMLWriter;
 //import org.graphdrawing.graphml.writer.XmlWriter;
 //import org.graphdrawing.graphml.writer.DomXmlWriter;
 //import org.graphdrawing.graphml.GraphMLConstants;
 //import org.graphdrawing.graphml.reader.dom.DOMGraphMLParser;
 
 public class PathCaseViewerMetabolomics extends JPanel {
 
 	private int fileRev = 0;
 	public File getFileName(String extension) {
 		fileRev++;
 		return new File(System.getProperty("user.home") + "/Desktop/file" + fileRev + extension);
 	}
 	
     //PathCase Constants
     public static final String GUIDSEPERATOR = ";";
     public static final String COLORSEPERATOR = "@";
     public static final String TEXTSEPERATOR = "%#%";
     private String valueOfcompartmentH;
     //Thread Manager
     private ExecutorService pool;
 
     //Data Cache
     public PathCaseRepository repository;
     public PathCaseRepository repository2;
 //    private LayoutInfo mappingPWLayout= new LayoutInfo();
 
     //Web Service
     PathwaysService service;
     PathwaysServiceMetabolomics serviceM;
     //configuration of the applet
     HashMap<String, Object> configuration;
 
     //Data cache
     HashMap<String, String> PathCaseIDToNameMap;
     HashMap<String, String> PathCaseIDToNameMap2;
     HashMap<String, String> PathCaseIDToNameMap_Pathway;
     HashMap<String, String> PathCaseIDToTooltips;
     //For cofactors and regulators, the list contains multiple pathcase identifiers since we merged the modulators into a single node
     //for substrate/product, generic process, and collapsed pathway nodes, the list contains only a single identifier, thus it is safe to obtain only the first id in the list
     HashMap<Node, HashSet<String>> nodeToPathCaseId;
     HashMap<Node, HashSet<String>> nodeToPathCaseId2;
     HashMap<Node, HashSet<String>> nodeToPathCaseId_Pathway;
     HashMap<String, Color> DBIDToCustomFillColor;
 
     //reference to applet for redirection purposes
     PathCaseViewerApplet applet;
     String compartmentID;
     String pathwayID;
 //    String biomodelXML;
     double modelGraphBoundaryX;
 
     //keep the current graph mode;
     public PathCaseViewMode graphMode;
     boolean bFrozenLoaded=false;       // This is flag to determine where the tool tip text is from
     boolean bHasPWMapping=false;       // This is flag to determine whether there's a mapping pathway, if so, we should not save frozen layout
 //    HashMap<Node,String> nodeToIDandName;
     HashMap<String,String> posToIDandName;
     HashMap<Integer,HashSet<String>> nodeIndexToID;
 
     HashSet<String> displayedTissues;
     
     public String getUUID(Node n) {
     	Graph2D graph = graphViewer.view.getGraph2D();
     	NodeRealizer realizer = graph.getRealizer(n);
     	String name = realizer.getLabelText();
     	return name + System.nanoTime();
     }
     
     public void addNodeToDataCache(Node n, String pathCaseId) {
     	Graph2D graph = graphViewer.view.getGraph2D();
     	HashSet<String> hSet = new HashSet<String>();
     	hSet.add(pathCaseId);
     	if(nodeToPathCaseId != null) {
     		nodeToPathCaseId.put(n, hSet);
     	}
     	if(nodeToPathCaseId2 != null) {
     		nodeToPathCaseId2.put(n, hSet);
     	}
     	if(nodeToPathCaseId_Pathway != null) {
     		nodeToPathCaseId_Pathway.put(n, hSet);
     	}
     	if(PathCaseIDToNameMap != null) {
     		PathCaseIDToNameMap.put(pathCaseId, graph.getLabelText(n));
     	}
     	if(PathCaseIDToNameMap2 != null) {
     		PathCaseIDToNameMap2.put(pathCaseId, graph.getLabelText(n));
     	}
     	if(PathCaseIDToNameMap_Pathway != null) {
     		PathCaseIDToNameMap_Pathway.put(pathCaseId, graph.getLabelText(n));
     	}
     }
     
     public String getPathCaseIdForNode(Node n) {
     	HashSet<String> results = nodeToPathCaseId.get(n);
     	return results.iterator().next();
     }
     
     
 
     //create a panel with dockable pathcase tools
     public PathCaseViewerMetabolomics(PathCaseViewerApplet applet, boolean geneviewer, boolean organismpanel, boolean layoutsaving,boolean mapping2pathway) {
         createContentPane(geneviewer, organismpanel, layoutsaving,mapping2pathway);
         pool = Executors.newFixedThreadPool(10);
         repository = new PathCaseRepository();
 
         this.applet = applet;
 
         graphMode = new PathCaseViewMode();
     }
 
     public JPanel getContentPane() {
         return this;
     }
 
     //////////////////////////////        CREATE GUI ELEMENTS       //////////////////////////
 
     //Main graph viewer panel
     protected DefaultYGraphViewerMetabolomics graphViewer;
 //    protected DefaultYGraphViewerMetabolomics graphViewerCompartmentH;
 
     //the panel that shows an overview of the current graph
     protected JSplitPane overviewPane;
     JSplitPane overviewSplitPane;
 
     //the toolbar that carries graph manipulation buttons, and buttons to switch between graph modes
     protected JToolBar itemsToolBar;
 
     //place the statusbar at the top, to give an important message, or show a message
     protected JToolBar statusBar;
     //the text to be displayed on the top statusbar
     protected JLabel statusBarText;
 
     //PathCase related buttons
     private JToggleButton organismBrowserButton;
     private JToggleButton geneViewerButton;
     private JToggleButton compartmentBrowserButton;
 //    private JToggleButton mappingM2PButton;
 
     //Organism and Gene Viewer components
     OrganismCheckPanel organismPanel;
     JSplitPane organismSplitPane;
     GeneViewerPanel geneViewerPanel;
     JSplitPane geneSplitPane;
     OrganismCheckPanel compartmentPanel;
     JSplitPane compartmentSplitPane;
 
 
     //define contents of the panel
     private void createContentPane(boolean geneviewer, boolean organismpanel, boolean layoutsaving, boolean mapping2pathway) {
         setLayout(new BorderLayout(0, 0));
         setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
         //graphViewer
         createGraphPanel();
         //minimapPanel
         createMinimap();
 
         itemsToolBar = createMainToolBar(geneviewer, organismpanel, layoutsaving,mapping2pathway);
         statusBar = createStatusBar();
 
         add(itemsToolBar, BorderLayout.NORTH);
         add(statusBar, BorderLayout.NORTH);
 
         statusBar.setEnabled(false);
         statusBar.setVisible(false);
 
 
         overviewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, null, graphViewer.view);
         overviewSplitPane.setDividerLocation(0);
         overviewSplitPane.setDividerSize(5);
         overviewSplitPane.setContinuousLayout(true);
 
         createCompartmentViewer();
         compartmentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewSplitPane, null);
         compartmentSplitPane.setDividerLocation(0);
         compartmentSplitPane.setDividerSize(5);
         compartmentSplitPane.setContinuousLayout(true);
         compartmentSplitPane.setEnabled(true);
 
         if (organismpanel) {
             //organismPanel
             createOrganismViewer();
 
             organismSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewSplitPane, null);
             organismSplitPane.setDividerLocation(0);
             organismSplitPane.setDividerSize(5);
             organismSplitPane.setContinuousLayout(true);
             organismSplitPane.setEnabled(false);
 
         }
 
         if (geneviewer) {
             createGeneViewer();
 
             if (organismpanel)
                 geneSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, organismSplitPane, null);
             else
                 geneSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewSplitPane, null);
 
 
             geneSplitPane.setDividerLocation(0);
             geneSplitPane.setDividerSize(5);
             geneSplitPane.setContinuousLayout(true);
             geneSplitPane.setEnabled(false);
 
             add(geneSplitPane, BorderLayout.CENTER);
 
         } else if (organismpanel) {
             add(organismSplitPane, BorderLayout.CENTER);
         } else   {
             add(overviewSplitPane, BorderLayout.CENTER);
 //            add(compartmentSplitPane, BorderLayout.CENTER);
         }
 
         //geneViewerPanel
 
 
     }
 
     //Creates a toolbar for this base.
     protected JToolBar createMainToolBar(boolean geneviewer, boolean organismpanel, final boolean layoutsaving,boolean mapping2pathway) {
         JToolBar toolBar = new JToolBar();
         toolBar.setFloatable(false);
         //toolBar.setBackground(Color.white);
         //toolBar.setForeground(Color.white);
         //toolBar.setMargin(new Insets(-5,-5,-5,-5));
         final JButton fileOperations = new JButton();
         toolBar.add(fileOperations);
         fileOperations.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/fileIcon.GIF")));
         fileOperations.setToolTipText("File Operations");
         fileOperations.setMaximumSize(new java.awt.Dimension(21, 21));
         fileOperations.setMinimumSize(new java.awt.Dimension(21, 21));
         fileOperations.setPreferredSize(new java.awt.Dimension(21, 21));
         fileOperations.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedfileIcon.GIF")));
         fileOperations.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 showFilePopupMenu(fileOperations, layoutsaving);
             }
         });
         toolBar.addSeparator();
 
         ButtonGroup graphModeButtonGroup = new ButtonGroup();
 
         JToggleButton editingModeButton = new JToggleButton();
         toolBar.add(editingModeButton);
         graphModeButtonGroup.add(editingModeButton);
         editingModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/editIcon.GIF")));
         editingModeButton.setToolTipText("Editing Tool");
         editingModeButton.setSelected(true);
         editingModeButton.setMaximumSize(new java.awt.Dimension(21, 21));
         editingModeButton.setMinimumSize(new java.awt.Dimension(21, 21));
         editingModeButton.setPreferredSize(new java.awt.Dimension(21, 21));
         editingModeButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressededitIcon.GIF")));
         editingModeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.EDIT);
             }
         });
 
         JToggleButton magnifyingModeButton = new JToggleButton();
         toolBar.add(magnifyingModeButton);
         graphModeButtonGroup.add(magnifyingModeButton);
         magnifyingModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/magnifierIcon.GIF")));
         magnifyingModeButton.setToolTipText("Magnifying Tool");
         magnifyingModeButton.setMaximumSize(new java.awt.Dimension(21, 21));
         magnifyingModeButton.setMinimumSize(new java.awt.Dimension(21, 21));
         magnifyingModeButton.setPreferredSize(new java.awt.Dimension(21, 21));
         magnifyingModeButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedmagnifierIcon.GIF")));
         magnifyingModeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.MAGNIFIER);
             }
         });
 
         JToggleButton panningModeButton = new JToggleButton();
         toolBar.add(panningModeButton);
         graphModeButtonGroup.add(panningModeButton);
         panningModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/panIcon.GIF")));
         panningModeButton.setToolTipText("Panning Tool");
         panningModeButton.setMaximumSize(new java.awt.Dimension(21, 21));
         panningModeButton.setMinimumSize(new java.awt.Dimension(21, 21));
         panningModeButton.setPreferredSize(new java.awt.Dimension(21, 21));
         panningModeButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedpanIcon.GIF")));
         panningModeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.PAN);
             }
         });
 
         JToggleButton interactiveZoomModeButton = new JToggleButton();
         toolBar.add(interactiveZoomModeButton);
         graphModeButtonGroup.add(interactiveZoomModeButton);
         interactiveZoomModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/interactiveZoomIcon.GIF")));
         interactiveZoomModeButton.setToolTipText("Interactive Zooming Tool");
         interactiveZoomModeButton.setMaximumSize(new java.awt.Dimension(21, 21));
         interactiveZoomModeButton.setMinimumSize(new java.awt.Dimension(21, 21));
         interactiveZoomModeButton.setPreferredSize(new java.awt.Dimension(21, 21));
         interactiveZoomModeButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedinteractiveZoomIcon.GIF")));
         interactiveZoomModeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.INTERACTIVE_ZOOM);
             }
         });
 
         JToggleButton areaZoomModeButton = new JToggleButton();
         toolBar.add(areaZoomModeButton);
         graphModeButtonGroup.add(areaZoomModeButton);
         areaZoomModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/marqueeZoomIcon.GIF")));
         areaZoomModeButton.setToolTipText("Area Zooming Tool");
         areaZoomModeButton.setMaximumSize(new java.awt.Dimension(21, 21));
         areaZoomModeButton.setMinimumSize(new java.awt.Dimension(21, 21));
         areaZoomModeButton.setPreferredSize(new java.awt.Dimension(21, 21));
         areaZoomModeButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedmarqueeZoomIcon.GIF")));
         areaZoomModeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.AREA_ZOOM);
             }
         });
         toolBar.addSeparator();
 
         if (graphViewer.ISDELETIONENABLED) {
             JButton deleteSelectionButton = new JButton();
             toolBar.add(deleteSelectionButton);
             deleteSelectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/deleteIcon.GIF")));
             deleteSelectionButton.setToolTipText("Delete Selection");
             deleteSelectionButton.setMaximumSize(new java.awt.Dimension(21, 21));
             deleteSelectionButton.setMinimumSize(new java.awt.Dimension(21, 21));
             deleteSelectionButton.setPreferredSize(new java.awt.Dimension(21, 21));
             deleteSelectionButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/presseddeleteIcon.GIF")));
             deleteSelectionButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     graphViewer.view.getGraph2D().removeSelection();
                     graphViewer.view.getGraph2D().updateViews();
                 }
             });
         }
 
 
         JButton pointZoomInButton = new JButton();
         toolBar.add(pointZoomInButton);
         pointZoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pointZoomInIcon.GIF")));
         pointZoomInButton.setToolTipText("Point Zoom In");
         pointZoomInButton.setMaximumSize(new java.awt.Dimension(21, 21));
         pointZoomInButton.setMinimumSize(new java.awt.Dimension(21, 21));
         pointZoomInButton.setPreferredSize(new java.awt.Dimension(21, 21));
         pointZoomInButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedpointZoomInIcon.GIF")));
         pointZoomInButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.view.setZoom(graphViewer.view.getZoom() * 1.2);
                 //optional code that adjusts the size of the
                 //view's world rectangle. The world rectangle
                 //defines the region of the canvas that is
                 //accessible by using the scrollbars of the view.
                 Rectangle box = graphViewer.view.getGraph2D().getBoundingBox();
                 graphViewer.view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);
 
                 graphViewer.view.updateView();
             }
         });
 
         JButton pointZoomOutButton = new JButton();
         toolBar.add(pointZoomOutButton);
         pointZoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pointZoomOutIcon.GIF")));
         pointZoomOutButton.setToolTipText("Point Zoom Out");
         pointZoomOutButton.setMaximumSize(new java.awt.Dimension(21, 21));
         pointZoomOutButton.setMinimumSize(new java.awt.Dimension(21, 21));
         pointZoomOutButton.setPreferredSize(new java.awt.Dimension(21, 21));
         pointZoomOutButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedpointZoomOutIcon.GIF")));
         pointZoomOutButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.view.setZoom(graphViewer.view.getZoom() * 0.8);
                 //optional code that adjusts the size of the
                 //view's world rectangle. The world rectangle
                 //defines the region of the canvas that is
                 //accessible by using the scrollbars of the view.
                 Rectangle box = graphViewer.view.getGraph2D().getBoundingBox();
                 graphViewer.view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);
                 graphViewer.view.updateView();
             }
         });
 
         JButton fitToViewButton = new JButton();
         toolBar.add(fitToViewButton);
         fitToViewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/zoomtofitIcon.GIF")));
         fitToViewButton.setToolTipText("Fit Content");
         fitToViewButton.setMaximumSize(new java.awt.Dimension(34, 21));
         fitToViewButton.setMinimumSize(new java.awt.Dimension(34, 21));
         fitToViewButton.setPreferredSize(new java.awt.Dimension(34, 21));
         fitToViewButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedzoomtofitIcon.GIF")));
         fitToViewButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.view.fitContent();
                 graphViewer.view.updateView();
             }
         });
 
         JButton normalSizedviewButton = new JButton();
         toolBar.add(normalSizedviewButton);
         normalSizedviewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/zoomtoNormalIcon.GIF")));
         normalSizedviewButton.setToolTipText("Zoom to Normal Size");
         normalSizedviewButton.setMaximumSize(new java.awt.Dimension(34, 21));
         normalSizedviewButton.setMinimumSize(new java.awt.Dimension(34, 21));
         normalSizedviewButton.setPreferredSize(new java.awt.Dimension(34, 21));
         normalSizedviewButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedzoomtoNormalIcon.GIF")));
         normalSizedviewButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 graphViewer.view.setZoom(1.0);
                 //view.setWorldRect((int)view.getWorldRect().getWidth()/2, (int)view.getWorldRect().getHeight()/2, (int)view.getWorldRect().getWidth(), (int)view.getWorldRect().getHeight());
                 graphViewer.view.updateView();
             }
         });
 
         final JButton showLayoutOptionsButton = new JButton();
         toolBar.add(showLayoutOptionsButton);
         showLayoutOptionsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/layoutChangeIcon.GIF")));
         showLayoutOptionsButton.setToolTipText("Choose Layout");
         showLayoutOptionsButton.setMaximumSize(new java.awt.Dimension(34, 21));
         showLayoutOptionsButton.setMinimumSize(new java.awt.Dimension(34, 21));
         showLayoutOptionsButton.setPreferredSize(new java.awt.Dimension(34, 21));
         showLayoutOptionsButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedlayoutChangeIcon.GIF")));
         showLayoutOptionsButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 showLayoutPopupMenu(showLayoutOptionsButton);
             }
         });
 
         final JToggleButton minimapButton = new JToggleButton();
         toolBar.add(minimapButton);
         minimapButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/minimapIcon.GIF")));
         minimapButton.setToolTipText("Display/Hide Minimap");
         minimapButton.setMaximumSize(new java.awt.Dimension(34, 21));
         minimapButton.setMinimumSize(new java.awt.Dimension(34, 21));
         minimapButton.setPreferredSize(new java.awt.Dimension(34, 21));
         minimapButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedminimapIcon.GIF")));
         minimapButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 if (graphViewer != null && overviewSplitPane != null && overviewPane != null) {
                     if (minimapButton.isSelected()) {
                         overviewSplitPane.setLeftComponent(overviewPane);
                         overviewSplitPane.setDividerLocation(200);
                         overviewSplitPane.setEnabled(true);
                     } else {
                         overviewSplitPane.setLeftComponent(null);
                         overviewSplitPane.setDividerLocation(0);
                         overviewSplitPane.setEnabled(false);
 
                     }
                     repaint();
                 }
             }
         });
 
         toolBar.addSeparator();
 
         geneViewerButton = new JToggleButton();
         toolBar.add(geneViewerButton);
         geneViewerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/geneViewerIcon.GIF")));
         geneViewerButton.setToolTipText("Display/Hide Gene Viewer");
         geneViewerButton.setMaximumSize(new java.awt.Dimension(34, 21));
         geneViewerButton.setMinimumSize(new java.awt.Dimension(34, 21));
         geneViewerButton.setPreferredSize(new java.awt.Dimension(34, 21));
         geneViewerButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedgeneViewerIcon.GIF")));
         geneViewerButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 //if (view != null && splitPane != null && overviewPane !=null )
                 {
                     if (geneViewerButton.isSelected()) {
                         geneViewerDisplayAction();
 
                     } else {
                         geneViewerHideAction();
                     }
                     repaint();
                 }
             }
         });
         geneViewerButton.setEnabled(geneviewer);
         geneViewerButton.setVisible(geneviewer);
 
         organismBrowserButton = new JToggleButton();
         toolBar.add(organismBrowserButton);
         organismBrowserButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/organismPanelIcon.GIF")));
         organismBrowserButton.setToolTipText("Display/Hide Organism Panel");
         organismBrowserButton.setMaximumSize(new java.awt.Dimension(34, 21));
         organismBrowserButton.setMinimumSize(new java.awt.Dimension(34, 21));
         organismBrowserButton.setPreferredSize(new java.awt.Dimension(34, 21));
         organismBrowserButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedOrganismPanelIcon.GIF")));
         organismBrowserButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 //if (view != null && splitPane != null && overviewPane !=null )
                 {
                     if (organismBrowserButton.isSelected()) {
                         organismSplitPane.setRightComponent(organismPanel);
                         organismSplitPane.setDividerLocation(organismSplitPane.getWidth() - organismPanel.getPreferredSize().width);
                         organismSplitPane.setEnabled(true);
                     } else {
                         organismSplitPane.setRightComponent(null);
                         organismSplitPane.setDividerLocation(0);
                         organismSplitPane.setEnabled(false);
                     }
                     repaint();
                 }
             }
         });
         organismBrowserButton.setEnabled(organismpanel);
         organismBrowserButton.setVisible(organismpanel);
 
 
         toolBar.addSeparator();
 
         final JButton hideCommonButton = new JButton();
         toolBar.add(hideCommonButton);
         hideCommonButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/commonmolIcon.GIF")));
         hideCommonButton.setToolTipText("Change Display Mode");
         hideCommonButton.setMaximumSize(new java.awt.Dimension(34, 21));
         hideCommonButton.setMinimumSize(new java.awt.Dimension(34, 21));
         hideCommonButton.setPreferredSize(new java.awt.Dimension(34, 21));
         hideCommonButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedcommonmolIcon.GIF")));
         hideCommonButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 JPopupMenu menu = getModesMenu();
                 menu.show(hideCommonButton, 0, 15);
             }
         });
 
         if(mapping2pathway){
             final JButton mappingM2PButton = new JButton();
             toolBar.add(mappingM2PButton);
             mappingM2PButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/commonM2P.GIF")));//"/binevi/Resources/Images/commonM2P.GIF"
             mappingM2PButton.setToolTipText("Choose mapping pathway");
             mappingM2PButton.setMaximumSize(new java.awt.Dimension(34, 21));
             mappingM2PButton.setMinimumSize(new java.awt.Dimension(34, 21));
             mappingM2PButton.setPreferredSize(new java.awt.Dimension(34, 21));
             mappingM2PButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/binevi/Resources/Images/pressedcommonM2P.GIF")));//pressedcommonM2P
             mappingM2PButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     JPopupMenu menu = getMappingPWsMenu();
                     menu.show(mappingM2PButton, 0, 15);
                 }
             });
             mappingM2PButton.setEnabled(true);
 //            mappingM2PButton.setVisible(mapping2pathway);
         }
 
 
         return toolBar;
     }
 
     private void geneViewerDisplayAction() {
         createGeneViewer();
         loadGeneViewerOrganismListFromOrgIds(organismPanel.getSelectedOrganismIdsOfLeaves());//organisms that have a gene
         geneSplitPane.setRightComponent(geneViewerPanel);
         geneSplitPane.setDividerLocation(geneSplitPane.getHeight() - 155/*geneViewerPanel.getPreferredSize().height*/);
         geneSplitPane.setEnabled(true);
         //geneViewerPanel.geneSelected(null,null);
     }
 
     private void geneViewerHideAction() {
         geneSelectProcessesandCollapsedPathways(new HashSet<GenomeTable.GeneEntry>());
         geneSplitPane.setRightComponent(null);
         geneSplitPane.setDividerLocation(0);
         geneSplitPane.setEnabled(false);
     }
 
     public void createGraphPanel() {
             graphViewer = new DefaultYGraphViewerMetabolomics(this);
             graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.EDIT);
             graphViewer.registerViewActions();
             graphViewer.registerViewListeners();
 //        }
     }
 
     public void createOrganismViewer() {
         organismPanel = new OrganismCheckPanel(true, this);
     }
 
     public void createCompartmentViewer() {
         compartmentPanel = new OrganismCheckPanel(true, this);
     }
 
     public void createGeneViewer() {
         geneViewerPanel = new GeneViewerPanel(this);
         //geneViewerPanel.setMaximumSize(new Dimension(150,1000));
     }
 
     public void createMinimap() {
 
         Overview overView = new Overview(graphViewer.view);
         overView.setPreferredSize(new Dimension(150, 150));
 
         overviewPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, overView, null);
 
         overviewPane.setDividerLocation(overView.getPreferredSize().height);
         overviewPane.setDividerSize(5);
         overviewPane.setContinuousLayout(true);
     }
 
     protected JToolBar createStatusBar() {
         JToolBar statusbar = new JToolBar();
         statusbar.setFloatable(false);
 
         JProgressBar progressBar = new JProgressBar();
         progressBar.setBorderPainted(true);
         progressBar.setIndeterminate(true);
 
         statusBarText = new JLabel();
         statusBarText.setFont(new Font("Arial", Font.BOLD, 18));
         statusBarText.setHorizontalTextPosition(JLabel.CENTER);
 
         statusbar.add(statusBarText);
         statusbar.addSeparator();
         statusbar.add(progressBar);
 
         statusbar.setBackground(Color.WHITE);
 
         return statusbar;
     }
 
     public void destroy() {
         if (graphViewer != null) graphViewer.destroyViewModes();
         graphViewer = null;
 
         pool.shutdown(); // Disable new tasks from being submitted
         try {
             // Wait a while for existing tasks to terminate
             if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                 pool.shutdownNow(); // Cancel currently executing tasks
                 // Wait a while for tasks to respond to being cancelled
                 if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                     System.err.println("Pool did not terminate");
             }
         } catch (InterruptedException ie) {
             // (Re-)Cancel if current thread also interrupted
             pool.shutdownNow();
             // Preserve interrupt status
             Thread.currentThread().interrupt();
         }
     }
 
     //////////////////////           LAYOUT OPERATIONS           //////////////////////////////
 
     private void showLayoutPopupMenu(JButton showLayoutOptionsButton) {
         JPopupMenu menu = graphViewer.getLayoutMenu();
         menu.show(showLayoutOptionsButton, 0, 15);
 
     }
 
     //////////////////////            FILE OPERATIONS             /////////////////////////////
 
     public void showFilePopupMenuMultiAction(JButton fileOperations) {
         JPopupMenu menu = new JPopupMenu();
         menu.add(new LoadAction(this));
         menu.add(new SaveAction());
         menu.add(new SaveSubsetAction());
         menu.addSeparator();
         menu.add(new PrintAction());
         menu.addSeparator();
         menu.add(new ExitAction());
         menu.show(fileOperations, 0, 15);
 
     }
 
     public void showFilePopupMenu(JButton fileOperations, boolean layoutsaving) {
 
         JPopupMenu menu = new JPopupMenu();
         JMenuItem item = new JMenuItem("Export Graph As JPEG");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 JFileChooser chooser = new JFileChooser();
                 chooser.setSelectedFile(getFileName(".jpg"));
                 if (chooser.showSaveDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
                     String name = chooser.getSelectedFile().toString();
 
                     if (!name.endsWith(".jpg")) name = name + ".jpg";
                     JPGIOHandler ioh = new JPGIOHandler();
                     //ioh.setQuality(1.0f);
                     try {
                         ioh.write(graphViewer.view.getGraph2D(), name);
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
 
                 }
             }
         });
         menu.add(item);
 
         item = new JMenuItem("Visualize Graph from (BioPAX or SXG) file");
         item.addActionListener(new LoadAction(this));
         menu.add(item);
 
 //        //Xinjian Qi Added for frozen SystemBiology layout information
 //        item = new JMenuItem("Save the System Biology layout(old)");
 //        item.addActionListener(new ActionListener() {
 //            public void actionPerformed(ActionEvent evt) {
 //                pool.submit(new Thread() {
 //                        public void run() {
 ////                                String lo = layoutInfo("System Biology");
 ////                                createLoaderDialog("Saving the System Biology layout information...");
 ////                                try {
 ////                                    boolean b ;
 ////                                    b = tmpSaveLayoutToFile("lo");
 ////
 ////                                    if (!b) {
 ////                                        throw new Exception("layout not stored");
 ////                                    }
 ////                                    killLoaderDialog();
 ////                                    JOptionPane.showMessageDialog(null, "Layout has been successfully saved", "information", JOptionPane.INFORMATION_MESSAGE);
 ////                                } catch (Exception e) {
 ////                                    D.show(e);
 ////                                }
 //
 ////                             GMLIOHandler ioh = new GMLIOHandler();
 ////
 ////                            GraphMLIOHandler ioHandler = new GraphMLIOHandler();
 //////                            ioHandler.getRealizerSerializerManager().addNodeRealizerSerializer(new PathCaseShapeNodeRealizer());
 ////                            ioHandler.addNamespace("binevi.view.PathCaseShapeNodeRealizerSerializer", "PachCaseShapeNode");
 ////                            try{
 ////                            ioHandler.write(graphViewer.view.getGraph2D(), "D:\\SBLayout.txt");
 ////                            }catch(IOException e){
 ////                                D.show(e);
 ////                            }
 //
 ///*for customized data only
 //                            NodeMap idMap = graphViewer.view.getGraph2D().createNodeMap();
 //                            NodeMap typeMap = graphViewer.view.getGraph2D().createNodeMap();
 //                            for (NodeCursor nc = graphViewer.view.getGraph2D().nodes(); nc.ok(); nc.next()) {
 //                                idMap.set(nc.node(),nodeToPathCaseId.get(nc.node()));
 //                                switch(((PathCaseShapeNodeRealizer)graphViewer.view.getGraph2D().getRealizer(nc.node())).getNodeRole()){
 //                                    case SPECIES:
 //                                        typeMap.setInt(nc.node(),1);
 //                                        break;
 //                                    case COMPARTMENT:
 //                                        typeMap.setInt(nc.node(),2);
 //                                        break;
 //                                    default:
 //                                        typeMap.setInt(nc.node(),3);
 //                                        break;
 //                                }
 //                            }
 //                            graphViewer.view.getGraph2D().addDataProvider("idMap",idMap);
 //                            graphViewer.view.getGraph2D().addDataProvider("typeMap",typeMap);
 //
 //                            GMLIOHandler ioh = new GMLIOHandler();
 ////                            ioh.
 //
 //                            FileOutputStream out = null;
 //                            try {
 ////                                ioh.write(graphViewer.view.getGraph2D(), "D:\\SBLayout.txt");
 //
 //                                out = new FileOutputStream("D:\\SBLayout.txt");
 //
 //      // Low-level support for GraphML attributes of simple type.
 //      AttrDataProviderOutputHandler nodeIdAttribute =
 //          new AttrDataProviderOutputHandler("idMap", idMap, AttributeConstants.TYPE_STRING);
 //      AttrDataProviderOutputHandler nodeTypeAttribute =
 //          new AttrDataProviderOutputHandler("typeMap", typeMap, AttributeConstants.TYPE_INT);
 //
 //      // Instead of using class Graph2D, the GraphML machinery is given a Graph object.
 //      YGraphElementProvider gep = new YGraphElementProvider(graphViewer.view.getGraph2D());
 //
 //      // Low-level support for writing GraphML file format.
 //      DirectGraphMLWriter writer = new DirectGraphMLWriter();
 //      writer.setGraphElementProvider(gep);
 //      // The node attribute is registered.
 //      writer.addNodeOutputHandler(nodeIdAttribute);
 //      writer.addNodeOutputHandler(nodeTypeAttribute);
 //
 //      XmlWriter coreWriter = new DomXmlWriter(out);
 //      coreWriter.addNamespace(GraphMLConstants.GRAPHML_BASE_NS_URI, "");
 //      writer.write(coreWriter);
 //        out.close();
 //
 ////                                writeGraph(graph, (NodeMap) graph.getDataProvider(NODE_ATTRIBUTE_NAME+"n"),
 ////                                    (EdgeMap) graph.getDataProvider(EDGE_ATTRIBUTE_NAME+"e"), fileName);
 //
 //                              } catch ( IOException ioe ) {
 //                                D.show( ioe );
 //                              }       */
 //                            Graph2D g = graphViewer.view.getGraph2D();
 //                            List list = createNodeDataList(g);
 //
 //                            EncoderFactory backupEncoderFactory = GMLIOHandler.getEncoderFactory();
 //                            ParserFactory backupParserFactory = GMLIOHandler.getParserFactory();
 //                            CustomGMLFactory factory = new CustomGMLFactory(list);
 //                            GMLIOHandler.setEncoderFactory(factory);
 //                            GMLIOHandler.setParserFactory(factory);
 ////                            System.out.println("saving starting");
 //                            for (NodeCursor nc = graphViewer.view.getGraph2D().nodes(); nc.ok(); nc.next()) {
 //                                for (Iterator it = list.iterator(); it.hasNext();)
 //                                {
 //                                  NodeData data = (NodeData) it.next();
 //                                  if (data.getClassType().equals(String.class)){
 //                                      String val="Empty";
 //                                      if(nodeToPathCaseId.get(nc.node())!=null && nodeToPathCaseId.get(nc.node()).size()>0) {
 //                                          val=nodeToPathCaseId.get(nc.node()).iterator().next();
 //                                          val+="@@@";
 //                                          val+=getNodeTipText(nc.node());
 //                                      }
 //                                      data.getNodeMap().set(nc.node(), val);
 //                                  }
 //                                  else if (data.getClassType().equals(Integer.class)){
 //                                      int iType=0;
 //                                      PathCaseShapeNodeRealizer.PathCaseNodeRole ncRole=((PathCaseShapeNodeRealizer)g.getRealizer(nc.node())).getNodeRole();
 ////                                      switch(((PathCaseShapeNodeRealizer)g.getRealizer(nc.node())).getNodeRole()){
 ////                                        case SPECIES:
 ////                                            iType=1;
 ////                                            break;
 ////                                        case COMPARTMENT:
 ////                                            iType=2;
 ////                                            break;
 ////                                        default:
 ////                                            iType=3;
 ////                                            break;
 ////                                    }
 //                                      if(ncRole==PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT) iType=1;
 //                                        if(ncRole==PathCaseShapeNodeRealizer.PathCaseNodeRole.COMPARTMENT) iType=2;
 //                                      data.getNodeMap().set(nc.node(), iType);
 //                                  }
 //                                }
 //                            }
 //
 //                              GMLIOHandler ioh = new GMLIOHandler();
 ////                             IOHandler ioh = getGraphMLIOHandler();
 //                              try {
 //                                    ioh.write(g, "D:\\SBLayout.gml");
 //                              } catch ( IOException ioe ) {
 //                                  System.out.println("saving exception:"+ioe.toString());
 //                                    D.show( ioe );
 //                              }
 //                            GMLIOHandler.setEncoderFactory(backupEncoderFactory );
 //                            GMLIOHandler.setParserFactory(backupParserFactory);
 ////                    else {
 ////                              if ( !name.endsWith( ".ygf" ) ) name = name + ".ygf";
 ////                              YGFIOHandler ioh = new YGFIOHandler();
 ////                              try {
 ////                                ioh.write( view.getGraph2D(), name );
 ////                              } catch ( IOException ioe ) {
 ////                                D.show( ioe );
 ////                              }
 ////                            }
 //                        }
 //                    });
 //            }
 //        });
 //        menu.add(item);
 
         item = new JMenuItem("Load saved ReconModels.org layout");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 pool.submit(new Thread() {
                         public void run() {
                             Graph2DView view;
                              view = graphViewer.view;//new Graph2DView();
 
                             Graph2D g = view.getGraph2D();
 
                             List list = createNodeDataList(g);
 //
                             EncoderFactory backupEncoderFactory = GMLIOHandler.getEncoderFactory();
                             ParserFactory backupParserFactory = GMLIOHandler.getParserFactory();
                             CustomGMLFactory factory = new CustomGMLFactory(list);
                             GMLIOHandler.setEncoderFactory(factory);
                             GMLIOHandler.setParserFactory(factory);
                             graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.EDIT);
 //                            view.addViewMode(new EditMode());
 
                              y.io.IOHandler ioh=new GMLIOHandler();
 //                            graphViewer.view.setGraph2D(new Graph2D());
                             try{
                                 g.clear();
                                 ioh.read(g,"D:\\SBLayout.gml");//SBLayout.gml
                                 view.updateView();
                             }catch(IOException ioe){
                                 D.show(ioe);
                             }
 
                             for (NodeCursor nodeCursor = g.nodes(); nodeCursor.ok(); nodeCursor.next()) {
                                 Node nod = nodeCursor.node();
 
                                 for (Iterator it = list.iterator(); it.hasNext();)
                                 {
                                   NodeData data = (NodeData) it.next();
 //                                  if (data.getClassType().equals(String.class)){
 //                                    data.getNodeMap().set(node, nodeToPathCaseId.get(node));
 //                                  }
 //                                  else
 //                                    if (data.getClassType().equals(Integer.class)){
 //                                        int value=data.getNodeMap().getInt(nodeCursor.node());
 //                                      switch(value){
 ////                                              ((PathCaseShapeNodeRealizer)graphViewer.view.getGraph2D().getRealizer(node)).getNodeRole()){
 //                                        case 1:
 ////                                            data.getNodeMap().set(node, 1);
 //                                            break;
 //                                        case 2:
 //                                            GroupNodeRealizer nr = (GroupNodeRealizer)graphViewer.view.getGraph2D().getRealizer(node);
 //                                            ((AutoBoundsFeature)nr).setAutoBoundsEnabled(false);
 //                                            nr.setOpenGroupIcon(null);
 //                                            g.setRealizer(node,nr);
 //                                            break;
 //                                        default:
 //                                            break;
 //                                    }
 //                                  }else
                                     if(data.getClassType().equals(String.class)){
                                         Object value = data.getNodeMap().get(nod);
                                         if(value == null)
                                             System.out.println("value is null");
                                         else
                                             System.out.println("String value:"+value.toString());
                                         NodeRealizer nr = (NodeRealizer)view.getGraph2D().getRealizer(nod);
                                         nr.setLabelText((String)value);
                                         g.setRealizer(nod,nr);
                                     }
                                     else if(data.getClassType().equals(Integer.class)){
                                         int value=data.getNodeMap().getInt(nod);
                                         switch(value){
                                             case 2:
 //                                                GroupNodeRealizer nr = new GroupNodeRealizer();
 //        GroupNodeRealizer re=new GroupNodeRealizer();
 //                nr.setShapeType(GroupNodeRealizer.RECT);// ShapeNodeRealizer.RECT
 //                                 nr.setAutoBoundsEnabled(true);
 //                Color selectedColor;
 
 //                                    PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer)  graphViewer.view.getGraph2D().getRealizer(nod);
 //                                    GroupNodeRealizer nr = (GroupNodeRealizer)graphViewer.view.getGraph2D().getRealizer(nod);
                                             NodeRealizer nr = view.getGraph2D().getRealizer(nod);
 //                                            ((AutoBoundsFeature)nr).setAutoBoundsEnabled(false);
                                                 nr.setTransparent(true);
 //                                            nr.setOpenGroupIcon(null);
 //                                                nr.setFillColor(Color.green);
                                             g.setRealizer(nod,nr);
                                                 break;
                                             case 1:
 //                                                NodeRealizer nrz = (NodeRealizer)view.getGraph2D().getRealizer(nod);
 //                                                nrz.setFillColor(Color.red);
 //                                                g.setRealizer(nod,nrz);
                                                 break;
                                         }
 //                                        System.out.println("Int value:"+value);
 //                                        NodeRealizer nr = (NodeRealizer)view.getGraph2D().getRealizer(nod);
 //                                            nr.setLabelText(nr.getLabel()+"--"+ String.valueOf(value));
                                     }
                                 }
 //                                if (HierarchyManager.getInstance(graphViewer.view.getGraph2D()).isGroupNode(node)) {
 ////                                    PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer)  graphViewer.view.getGraph2D().getRealizer(node);
 //                                    GroupNodeRealizer nr = (GroupNodeRealizer)graphViewer.view.getGraph2D().getRealizer(node);
 //                                ((AutoBoundsFeature)nr).setAutoBoundsEnabled(false);
 //                                    nr.setOpenGroupIcon(null);
 //
 ////                                    ((GroupFeature)nr).setGroupClosed(true);
 //
 ////                                    nr.setFillColor(Color.BLACK);
 //                                    nr.setTransparent(true);
 ////                                    nr.setVisible(false);
 //                                }
                             }
 
                             view.fitContent();
                             view.updateView();
                             GMLIOHandler.setEncoderFactory(backupEncoderFactory );
                             GMLIOHandler.setParserFactory(backupParserFactory);
 //                            try {
 //                              graphViewer.view.getGraph2D().clear();
 ////                              IOHandler ioh;
 ////
 ////                              ioh = new GMLIOHandler();
 ////                              ioh.read(graphViewer.view.getGraph2D(), "D:\\SBLayout.txt" );
 //                            } catch ( Exception e ) {
 //                              String message = "Unexpected error while loading resource \"" + "D:\\SBLayout.txt" + "\" due to " + e.getMessage();
 //                              D.showError( message );
 //                              throw new RuntimeException( message, e );
 //                            }
 //
 //
 //                            FileInputStream in = null;
 //                            try {
 //                              in = new FileInputStream("D:\\SBLayout.txt");
 //
 //                                Graph graph2 = new Graph();
 //                              // Low-level support for GraphML attributes of simple type.
 //                              AttrDataAcceptorInputHandler nodeIdAttribute =
 //                                  new AttrDataAcceptorInputHandler("idMap", (NodeMap)graph2.getDataProvider("idMap"), AttributeConstants.TYPE_STRING);
 //                        //      AttrDataAcceptorInputHandler nodeTypeAttribute =
 //                        //          new AttrDataAcceptorInputHandler("typeMap", (NodeMap) graphViewer.view.getGraph2D().getDataProvider("typeMap"), AttributeConstants.TYPE_INT);
 //
 //                              // Instead of using class Graph2D, the GraphML machinery is given a Graph object.
 //                              YGraphElementFactory gef = new YGraphElementFactory(graph2);
 //
 //                              // Low-level support for processing the GraphML file format's DOM structure.
 //                              DOMGraphMLParser parser = new DOMGraphMLParser();
 //                              // The node attribute is registered.
 //                              parser.addDOMInputHandler(nodeIdAttribute);
 //                        //      parser.addDOMInputHandler(nodeTypeAttribute);
 //                              parser.setGraphElementFactory(gef);
 //
 //                              parser.parse(in);
 //                              in.close();
 //                            }catch(IOException e){
 //                              D.showError( e.getMessage());
 //                            } finally {
 //                        //      if (in != null) {
 //                        //        in.close();
 //                        //      }
 //                            }
 
 //                            graphViewer.view.setGraph2D(new Graph2D);
 
 //                            graphViewer.view.fitContent();
 //                            graphViewer.view.updateView();
 //                            for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
 //                                Node node = nodeCursor.node();
 //                                if (HierarchyManager.getInstance(graphViewer.view.getGraph2D()).isGroupNode(node)) {
 ////                                    PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer)  graphViewer.view.getGraph2D().getRealizer(node);
 //                                    GroupNodeRealizer nr = (GroupNodeRealizer)graphViewer.view.getGraph2D().getRealizer(node);
 //                                ((AutoBoundsFeature)nr).setAutoBoundsEnabled(false);
 //                                    nr.setOpenGroupIcon(null);
 //
 ////                                    ((GroupFeature)nr).setGroupClosed(true);
 //
 ////                                    nr.setFillColor(Color.BLACK);
 //                                    nr.setTransparent(true);
 ////                                    nr.setVisible(false);
 //                                }
 //                            }
 //                            graphViewer.view.updateView();
                         }
                     });
             }
         });
 //        menu.add(item);
 
 
         item = new JMenuItem("Save BioModels Layout");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 pool.submit(new Thread() {
                         public void run() {
                             JPasswordField pw = new JPasswordField();
                             JLabel label1 = new JLabel("Please be Careful!\n");
                             JLabel label2 = new JLabel("Saving this layout \n");
                             JLabel label3 = new JLabel("WILL OVERRIDE and PERMANENTLY ELIMINATE \n");
                             JLabel label4 = new JLabel("any manually curated version of this model, if one exists.\n");
                             JLabel label5 = new JLabel("Password:\n");
                             int ans = JOptionPane.showConfirmDialog(null,new Object[]{label1,label2,label3,label4,label5,pw}, "Careful!" ,
                                     JOptionPane.OK_CANCEL_OPTION);
 
                         if (ans == JOptionPane.OK_OPTION) {
                         if("dblab".equals(new String(pw.getPassword()))){
                             if(((String) configuration.get("modelID")).split(";").length==1 && !bHasPWMapping){
                              GMLIOHandler ioh = new GMLIOHandler();
                             String str2="",str1="";
 
                             try{
 
                                 //delete faked nodes:
                                   for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
                                       Node node = nodeCursor.node();
                                       if(graphViewer.view.getGraph2D().getLayout(node).getWidth()==1 && graphViewer.view.getGraph2D().getLayout(node).getHeight()==1)
                                       graphViewer.view.getGraph2D().removeNode(node);
                                   }
 
 
                                 ByteArrayOutputStream strea=new ByteArrayOutputStream();
                                 ioh.write(graphViewer.view.getGraph2D(),strea);
                                 str2=strea.toString();
 
 //                                getNodeTipText
 //                                System.out.println(strO);
                             }catch(IOException e){
                                 D.show(e);
                             }
 
                            Graph2D g = graphViewer.view.getGraph2D();
                             List list = createNodeDataList(g);
 
                             EncoderFactory backupEncoderFactory = GMLIOHandler.getEncoderFactory();
                             ParserFactory backupParserFactory = GMLIOHandler.getParserFactory();
                             CustomGMLFactory factory = new CustomGMLFactory(list);
                             GMLIOHandler.setEncoderFactory(factory);
                             GMLIOHandler.setParserFactory(factory);
 //                            System.out.println("saving starting");
                             for (NodeCursor nc = graphViewer.view.getGraph2D().nodes(); nc.ok(); nc.next()) {
                                 for (Iterator it = list.iterator(); it.hasNext();)
                                 {
                                   NodeData data = (NodeData) it.next();
                                   if (data.getClassType().equals(String.class)){
                                       String val="Empty";
                                       if(!bFrozenLoaded){
                                           if(nodeToPathCaseId.get(nc.node())!=null && nodeToPathCaseId.get(nc.node()).size()>0) {
                                               val=nodeToPathCaseId.get(nc.node()).iterator().next();
                                               val+="@@@";
                                               val+=getNodeTipText(nc.node());
 //                                              val+="@@@";
 //                                              val+=nodeToPathCaseId.get(nc.node()).toArray()[0];
                                           }
                                       }else{
                                           val=posToIDandName.get(String.valueOf(nc.node().index()));
                                       }
                                       data.getNodeMap().set(nc.node(), val);
                                   } else if (data.getClassType().equals(Integer.class)){
                                       //this value is not used currently.
                                       int iType=0;
                                       if(!bFrozenLoaded){
                                           PathCaseShapeNodeRealizer.PathCaseNodeRole ncRole=((PathCaseShapeNodeRealizer)g.getRealizer(nc.node())).getNodeRole();
     //                                      switch(((PathCaseShapeNodeRealizer)g.getRealizer(nc.node())).getNodeRole()){
     //                                        case SPECIES:
     //                                            iType=1;
     //                                            break;
     //                                        case COMPARTMENT:
     //                                            iType=2;
     //                                            break;
     //                                        default:
     //                                            iType=3;
     //                                            break;
     //                                    }
                                           if(ncRole==PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT) iType=1;
                                           if(ncRole==PathCaseShapeNodeRealizer.PathCaseNodeRole.COMPARTMENT) iType=2;
                                       }
                                       data.getNodeMap().set(nc.node(), iType);
                                   }
                                 }
                             }
 
                               GMLIOHandler ioh2 = new GMLIOHandler();
 //                             IOHandler ioh = getGraphMLIOHandler();
                               try {
 //                                    ioh2.write(g, "D:\\SBLayout.gml");
 
                                 ByteArrayOutputStream strea=new ByteArrayOutputStream();
                                 ioh.write(graphViewer.view.getGraph2D(),strea);
                                 str1=strea.toString();
                                 str1+=("###"+str2);
                               } catch ( IOException ioe ) {
 //                                  System.out.println("saving exception:"+ioe.toString());
                                     D.show( ioe );
                               }
                             GMLIOHandler.setEncoderFactory(backupEncoderFactory );
                             GMLIOHandler.setParserFactory(backupParserFactory);
 
                         //call web service to save
                             if (serviceM != null) {
                                 String layout = serviceM.getPathwaysServiceSoap().retrieveLayout("SBsave", "",(String) configuration.get("modelID"), str1);
                             }
                         }else{
                                 JOptionPane.showMessageDialog(null, "You can only save layout for a single model", "information", JOptionPane.INFORMATION_MESSAGE);
                             }
                         }else{
                             JOptionPane.showMessageDialog(overviewPane,"Wrong Password","Error",JOptionPane.ERROR_MESSAGE);
                         }
                         }
                         }
                     });
             }
         });
         menu.add(item);
 
 //        item = new JMenuItem("Load System Biology layout WITH HIERARCHY(directly)...");
 //        item.addActionListener(new ActionListener() {
 //            public void actionPerformed(ActionEvent evt) {
 //                pool.submit(new Thread() {
 //                        public void run() {
 ////                            GraphMLIOHandler ioHandler = new GraphMLIOHandler();
 ////                            nodeToIDandName=new HashMap<Node,String>();
 //                            posToIDandName=new HashMap<String,String>();
 //                            Graph2DView view;
 //                             view = graphViewer.view;//new Graph2DView();
 //
 //                            Graph2D g = view.getGraph2D();
 //
 //                            List list = createNodeDataList(g);
 ////
 //                            EncoderFactory backupEncoderFactory = GMLIOHandler.getEncoderFactory();
 //                            ParserFactory backupParserFactory = GMLIOHandler.getParserFactory();
 //                            CustomGMLFactory factory = new CustomGMLFactory(list);
 //                            GMLIOHandler.setEncoderFactory(factory);
 //                            GMLIOHandler.setParserFactory(factory);
 //                            graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.EDIT);
 ////                            view.addViewMode(new EditMode());
 //
 //                             y.io.IOHandler ioh=new GMLIOHandler();
 ////                            graphViewer.view.setGraph2D(new Graph2D());
 //                            try{
 //                                g.clear();
 //                                ioh.read(g,"D:\\SBLayout.gml");//SBLayout.gml
 //                                view.updateView();
 //                            }catch(IOException ioe){
 //                                D.show(ioe);
 //                            }
 //
 //                            for (NodeCursor nodeCursor = g.nodes(); nodeCursor.ok(); nodeCursor.next()) {
 //                                Node nod = nodeCursor.node();
 //                                if(nod==null)break;
 //                                for (Iterator it = list.iterator(); it.hasNext();)
 //                                {
 //                                  NodeData data = (NodeData) it.next();
 //                                    if(data.getClassType().equals(String.class)){
 //                                        Object value = data.getNodeMap().get(nod);
 //                                        if(value == null)
 //                                            System.out.println("value is null");
 //                                        NodeRealizer nr = (NodeRealizer)view.getGraph2D().getRealizer(nod);
 //
 //                                        if(nr!=null)
 //                                        posToIDandName.put(String.valueOf(nod.index()),(String)value);
 //                                    }
 //
 //                                }
 //                            }
 //
 //                            GMLIOHandler.setEncoderFactory(backupEncoderFactory );
 //                            GMLIOHandler.setParserFactory(backupParserFactory);
 //
 //                            try{
 //                                Graph2D graph = graphViewer.view.getGraph2D();
 //                                graph.clear();
 //                                ioh.read(graph, "D:\\SBLayout_simple.gml");
 //                                graphViewer.view.fitContent();
 //                                graph.updateViews();
 //                            }catch(IOException e){
 //                                D.show(e);
 //                            }
 //                            for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
 //                                Node node = nodeCursor.node();
 //                                if (HierarchyManager.getInstance(graphViewer.view.getGraph2D()).isGroupNode(node)) {
 ////                                    PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer)  graphViewer.view.getGraph2D().getRealizer(node);
 //                                    GroupNodeRealizer nr = (GroupNodeRealizer)graphViewer.view.getGraph2D().getRealizer(node);
 //                                ((AutoBoundsFeature)nr).setAutoBoundsEnabled(false);
 //                                    nr.setOpenGroupIcon(null);
 //
 //                                    nr.setTransparent(true);
 //                                }
 //                            }
 //                            graphViewer.view.updateView();
 //
 //                        }
 //                    });
 //                bFrozenLoaded=true;
 //            }
 //        });
 //        menu.add(item);
         
         item = new JMenuItem("Save to SBML File");
         item.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try{
 				//Output SBML File
 				JFileChooser chooser = new JFileChooser();
 				chooser.setSelectedFile(getFileName(".sbml"));
 				if(chooser.showSaveDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
 					String sbml = ExportSBML.generateSBML(repository);
 					File f = new File(chooser.getSelectedFile().toString());
 					FileWriter fw = new FileWriter(f);
         			fw.write(sbml);
         			fw.close();
 				}
 				} catch (Exception e) {
 					JOptionPane.showMessageDialog(graphViewer.view, e.getMessage());
 				}
 			}
 		});
         menu.add(item);
         menu.show(fileOperations, 0, 15);
     }
     
     protected List createNodeDataList(Graph2D forGraph)
     {
       List list = new ArrayList(2);
       NodeMap nodeMap;
       nodeMap = forGraph.createNodeMap();
       list.add(new NodeData("idMap", String.class, nodeMap));
       nodeMap = forGraph.createNodeMap();
       list.add(new NodeData("typeMap", Integer.class, nodeMap));
       return list;
     }
 
     protected List createNodeDataList2(Graph2D forGraph)
       {
         List list = new ArrayList(2);
         NodeMap nodeMap;
         nodeMap = forGraph.createNodeMap();
         list.add(new NodeData("stringValue", String.class, nodeMap));
         nodeMap = forGraph.createNodeMap();
         list.add(new NodeData("intValue", Integer.class, nodeMap));
         return list;
       }
 
 
     public boolean tmpSaveLayoutToFile(String lo){
          try{
         // Create file
         FileWriter fstream = new FileWriter("D:\\SBLayout.txt");
         BufferedWriter out = new BufferedWriter(fstream);
         out.write(lo);
         //Close the output stream
         out.close();
         }catch (Exception e){//Catch exception if any
           System.err.println("Error: " + e.getMessage());
          return false;
         }
         return true;
     }
 
 
     // Action that prints the contents of the view
     protected class PrintAction extends AbstractAction {
         PageFormat pageFormat;
         OptionHandler printOptions;
 
         PrintAction() {
             super("Print");
 
             //setup option handler
             printOptions = new OptionHandler("Print Options");
             printOptions.addInt("Poster Rows", 1);
             printOptions.addInt("Poster Columns", 1);
             printOptions.addBool("Add Poster Coords", false);
             final String[] area = {"View", "Graph"};
             printOptions.addEnum("Clip Area", area, 1);
         }
 
         public void actionPerformed(ActionEvent e) {
             Graph2DPrinter gprinter = new Graph2DPrinter(graphViewer.view);
 
             //show custom print dialog and adopt values
             if (!printOptions.showEditor()) return;
             gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
             gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
             gprinter.setPrintPosterCoords(
                     printOptions.getBool("Add Poster Coords"));
             if (printOptions.get("Clip Area").equals("Graph")) {
                 gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
             } else {
                 gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
             }
 
             //show default print dialogs
             PrinterJob printJob = PrinterJob.getPrinterJob();
             if (pageFormat == null) pageFormat = printJob.defaultPage();
             PageFormat pf = printJob.pageDialog(pageFormat);
             if (pf == pageFormat) {
                 return;
             } else {
                 pageFormat = pf;
             }
 
             //setup printjob.
             //Graph2DPrinter is of type Printable
             printJob.setPrintable(gprinter, pageFormat);
 
             if (printJob.printDialog()) {
                 try {
                     printJob.print();
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         }
     }
 
     //Action that terminates the application
     protected class ExitAction extends AbstractAction {
         ExitAction() {
             super("Exit");
         }
 
         public void actionPerformed(ActionEvent e) {
             System.exit(0);
         }
     }
 
     //Action that saves the current graph to a file.
     protected class SaveAction extends AbstractAction {
         JFileChooser chooser;
 
         SaveAction() {
             super("Save...");
             chooser = null;
         }
 
         public void actionPerformed(ActionEvent e) {
             if (chooser == null) {
                 chooser = new JFileChooser();
             }
             if (chooser.showSaveDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
                 String name = chooser.getSelectedFile().toString();
                 if (name.endsWith(".gml")) {
                     GMLIOHandler ioh = new GMLIOHandler();
                     try {
                         ioh.write(graphViewer.view.getGraph2D(), name);
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
                 } else if (name.endsWith(".jpg")) {
                     JPGIOHandler ioh = new JPGIOHandler();
                     try {
                         ioh.write(graphViewer.view.getGraph2D(), name);
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
                 } else {
                     if (!name.endsWith(".ygf")) name = name + ".ygf";
                     YGFIOHandler ioh = new YGFIOHandler();
                     try {
                         ioh.write(graphViewer.view.getGraph2D(), name);
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
                 }
             }
         }
     }
 
     //Action that saves the current subset of the graph to a file.
     protected class SaveSubsetAction extends AbstractAction {
         JFileChooser chooser;
 
         public SaveSubsetAction() {
             super("Save selection...");
             chooser = null;
         }
 
         public void actionPerformed(ActionEvent e) {
             if (chooser == null) {
                 chooser = new JFileChooser();
                 
             }
             chooser.setSelectedFile(getFileName(".ygf"));
             if (chooser.showSaveDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
                 String name = chooser.getSelectedFile().toString();
                 if (!name.endsWith(".ygf")) name = name + ".ygf";
                 YGFIOHandler ioh = new YGFIOHandler();
                 try {
                     DataProvider dp = Selections.createSelectionDataProvider(graphViewer.view.getGraph2D());
                     ioh.writeSubset(graphViewer.view.getGraph2D(), dp, name);
                 } catch (IOException ioe) {
                     D.show(ioe);
                 }
             }
         }
     }
 
     //Action that loads the current graph from a file.
     protected class LoadAction extends AbstractAction {
         JFileChooser chooser;
         PathCaseViewerMetabolomics pathCaseViewer;
 
         public LoadAction(PathCaseViewerMetabolomics pathCaseViewer) {
             super("Load...");
             chooser = null;
             this.pathCaseViewer = pathCaseViewer;
         }
 
         /*public LoadAction() {
             super("Load...");
             chooser = null;
         }*/
 
         public void actionPerformed(ActionEvent evt) {
             if (chooser == null) {
                 chooser = new JFileChooser();
             }
             if (chooser.showOpenDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
                 String name = chooser.getSelectedFile().toString();
                 if (name.endsWith(".gml")) {
                     GMLIOHandler ioh = new GMLIOHandler();
                     try {
                         graphViewer.view.getGraph2D().clear();
                         ioh.read(graphViewer.view.getGraph2D(), name);
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
                 } else if (name.endsWith(".ygf")) {
                     YGFIOHandler ioh = new YGFIOHandler();
                     try {
                         graphViewer.view.getGraph2D().clear();
                         ioh.read(graphViewer.view.getGraph2D(), name);
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
                 } else if (name.endsWith(".sxg")) {
 
                     SXGIOHandler ioh = new SXGIOHandler();
                     try {
                         graphViewer.view.getGraph2D().clear();
                         ioh.read(graphViewer.view.getGraph2D(), name);
 
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
                 } else {//BIOPAX by default
                     graphViewer.resetView();
                     repository.reset();
                     BioPAXHandler ioh = new BioPAXHandler(repository);
                     try {
 
                         //graphViewer.view.updateView();
 
                         if (pathCaseViewer != null) {
                             pathCaseViewer.geneViewerButton.removeAll();
                             pathCaseViewer.organismBrowserButton.removeAll();
                             pathCaseViewer.geneViewerButton.setEnabled(false);
                             pathCaseViewer.organismBrowserButton.setEnabled(false);
 
                             ioh.read(graphViewer.view.getGraph2D(), name);
                             if (ioh.node2idtable != null) {
                                 nodeToPathCaseId = ioh.node2idtable;
                                 PathCaseIDToNameMap = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId);
                             } else {
                                 nodeToPathCaseId = new HashMap<Node, HashSet<String>>();
                                 PathCaseIDToNameMap = new HashMap<String, String>();
                             }
                             if (graphViewer.view.getGraph2D().nodeCount() < 1) {
                                 //error or empty graph
                                 JOptionPane.showMessageDialog(pathCaseViewer, "Error occured while loading from BioPax file.", "Warning", JOptionPane.WARNING_MESSAGE);
                             }
                         } else {
                             ioh.read(graphViewer.view.getGraph2D(), name);
                             if (graphViewer.view.getGraph2D().nodeCount() < 1) {
                                 //error or empty graph
                                 JOptionPane.showMessageDialog(null, "Error occured while loading from BioPax file.", "Warning", JOptionPane.WARNING_MESSAGE);
                             }
                         }
 
                     } catch (IOException ioe) {
                         D.show(ioe);
                     }
                 }
 
                 //force redisplay of view contents
                 graphViewer.bestLayout();
                 graphViewer.view.fitContent();
                 graphViewer.view.getGraph2D().updateViews();
             }
         }
     }
 
     ///////////////////           TEST AS AN APPLICATION    /////////////////////////////////
 
     public static void main(String args[]) {
 
 //      SwingUtility.setPlaf("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 //		SwingUtility.setPlaf("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
         //SwingUtility.setPlaf("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
         //SwingUtility.setPlaf("javax.swing.plaf.metal.MetalLookAndFeel");
 
         //configureDocking();
 
         // create and show the GUI
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 runGUI();
             }
         });
     }
 
     private static void runGUI() {
         // create out application frame
         JFrame frame = new JFrame();
         PathCaseViewerMetabolomics viewer = new PathCaseViewerMetabolomics(null, false, false, false,false);
         frame.getRootPane().setContentPane(viewer);
         frame.setSize(1024, 768);
         setCloseOperation(frame);
 
         // now show the frame
         frame.setVisible(true);
         viewer.loadSysBioGraph();
     }
 
 
     public static void setCloseOperation(JFrame f) {
         if (!Boolean.getBoolean("disable.system.exit"))
             f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         else
             f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
     }
 
     /**
      * Opens a JOptionPane with the error message and formatted stack trace of the throwable in a scrollable text area.
      *
      * @param c             optional argument for parent component to open modal error
      *                      dialog relative to
      * @param error_message short string description of failure, must be non-null
      * @param t             the throwable that's being reported, must be non-null
      */
     public static void showErrorDialog(Component c, String error_message, Throwable t) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         pw.println(error_message);
         pw.print("Exception is: ");
         t.printStackTrace(pw);
         pw.flush();
 
         JTextArea ta = new JTextArea(sw.toString(), 15, 60);
         JScrollPane sp = new JScrollPane(ta);
         JOptionPane.showMessageDialog(c, sp, error_message,
                 JOptionPane.ERROR_MESSAGE);
     }
 
     /////////////////////////  PROCESS APPLET PARAMETERS ///////////////////////////
 
     class PathCaseViewMode {
         public boolean showcommonmoleculesingraph;
         public boolean showmodulatorsingraph;
         public boolean showlinkingpathwaysingraph;
 
         public PathCaseViewMode() {
             defaultValues();
         }
 
         public void defaultValues() {
             showcommonmoleculesingraph =true;// false;  By default, we do NOT display common species.
 //03/18/09 revised for SysBio Model Visualization By Xinjian
             /*showmodulatorsingraph = true;    */
             showmodulatorsingraph = false;
             showlinkingpathwaysingraph = false;
         }
 
          public boolean isDefault() {
             return !showcommonmoleculesingraph && showmodulatorsingraph && !showlinkingpathwaysingraph;
         }
     }
 
     public void setConfiguration(HashMap<String, Object> configuration) {
         this.configuration = configuration;
     }
 
 
     public HashMap<String,Node> revert(HashMap<Node,HashSet<String>> nodeTOid){
         HashMap<String,Node> idTOnode=new HashMap<String,Node>();
         for(Node n:nodeTOid.keySet()){
             idTOnode.put((String)nodeTOid.get(n).toArray()[0],n) ;
         }
         return idTOnode;
     }
 
      public HashMap<String,Node> revertModel(HashMap<Node,HashSet<String>> nodeTOid){
         HashMap<String,Node> idTOnode=new HashMap<String,Node>();
         for(Node n:nodeTOid.keySet()){
             idTOnode.put(((String)nodeTOid.get(n).toArray()[0]).split("=")[0],n) ;
         }
         return idTOnode;
     }
 
     public boolean nodeTypeMatch(Node n1, Node n2){
         PathCaseShapeNodeRealizer.PathCaseNodeRole n1r,n2r;
         n1r=((PathCaseShapeNodeRealizer)graphViewer.view.getGraph2D().getRealizer(n1)).getNodeRole();
         n2r=((PathCaseShapeNodeRealizer)graphViewer.view.getGraph2D().getRealizer(n2)).getNodeRole();
         if(((n1r==PathCaseShapeNodeRealizer.PathCaseNodeRole.SPECIES)&&(n2r==PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT))||
                 (n1r==PathCaseShapeNodeRealizer.PathCaseNodeRole.REACTION)&&(n2r==PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS))
             return true;
         else
             return false;
     }
 
     public void processAppletParameters(final boolean firstTime) {
        this.valueOfcompartmentH =(String)configuration.get("compartmentH");
         Boolean loadFromBioPAX = (Boolean) configuration.get("loadFromBioPAX");
         if (!loadFromBioPAX)
         {
             pool.submit(new Thread() {
                 public void run() {
                     //NOTE: each of the methods here should kill the loader dialog regardless of an exception inside
 
                     //load graph from service
                     if(((String)configuration.get("compartmentH")).equalsIgnoreCase("MQLXML") && ((String)configuration.get("xmlLocation"))!=""){
                         System.out.println((String)configuration.get("xmlLocation"));
                         createLoaderDialog("Loading MQL Data From MQL Output...");
                      loadMQLGraph(readFile((String)configuration.get("xmlLocation")));
                         createLoaderDialog("Applying New Layout");
 //                        if (firstTime) {
 //                            if(hasMultiCompartments(graphViewer.view.getGraph2D()) ){ //if has compartments>1
 //                                itemsToolBar.getComponent(12).setVisible(false);  // hide the layout button
 //                            }else{  //only one compartment,apply layout.
 //                                graphViewer.view.setVisible(false);
                                 itemsToolBar.getComponent(12).setVisible(false); //hide the layout button
                         if(bFrozenLoaded)itemsToolBar.getComponent(18).setVisible(false);
 
 //                                graphViewer.bestLayout();
                                 graphViewer.view.setVisible(true);
 //                            }
 //                        }
                     }
                     else
                     {
                         if(((String)configuration.get("xmlLocation"))!=""){
                             loadSysBioGraph(readFile((String)configuration.get("xmlLocation")));
 
                         }
                          else
                         {   createLoaderDialog("Loading Network Data From ReconModels.org");
                     //
                     loadSysBioGraph();     // this fucntion load saved layout
 
                     //apply initial layout
 //                    createLoaderDialog("Applying New Layout");
 
                     //set custom coloring
                     String highlightEntities = (String) configuration.get("highlightEntities");
                     if(highlightEntities!=null && !highlightEntities.equalsIgnoreCase(""))
                         makeCustomNodePainting(highlightEntities);
 
 
 //                    if (firstTime) {
 //
 //                        if(hasMultiCompartments(graphViewer.view.getGraph2D()) ){ //if has compartments>1
 //
 //                            itemsToolBar.getComponent(12).setVisible(false);  // hide the layout button
 //                        }else{  //only one compartment,apply layout.
 //                            graphViewer.view.setVisible(false);
                             itemsToolBar.getComponent(12).setVisible(false); //hide the layout button
                         if(bFrozenLoaded)itemsToolBar.getComponent(18).setVisible(false);
 //                            graphViewer.bestLayout();
 //                            graphViewer.view.setVisible(true);
 //                        }
 //                    }
                     }
                     }
                     killLoaderDialog();
                     graphViewer.view.fitContent();
                     graphViewer.view.updateView();
                 }
             });
 
         }
         else {
             (new LoadAction(this)).actionPerformed(null);
         }
     }
 
 
     public boolean hasMultiCompartments(Graph2D graph){
         int n=0;
           for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
             Node node = nodeCursor.node();
             if (HierarchyManager.getInstance(graph).isGroupNode(node)) n++;
             if(n>1)return true;
           }
         return false;
     }
     private void connectToService(String WebServiceURL) {
 
         try {
             service = new PathwaysService(WebServiceURL);
         } catch (MalformedURLException e) {
             D.show(e);
             //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             //return;
         }
     }
 
    private void connectToServiceCompartmentH(String WebServiceURL) {
 
         try {
             serviceM = new PathwaysServiceMetabolomics(WebServiceURL);
         } catch (MalformedURLException e) {
             D.show(e);
             //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             //return;
         }
     }
 
     public void processAppletParameters() {
         this.valueOfcompartmentH =(String)configuration.get("compartmentH");
         this.compartmentID = (String)configuration.get("compartmentID");
         if(valueOfcompartmentH.equalsIgnoreCase("model")){
             Boolean loadFromBioPAX = (Boolean) configuration.get("loadFromBioPAX");
 
 //03/18/09 added for SysBio Model Visualization By Xinjian
             loadFromBioPAX=false;
 
             if (!loadFromBioPAX)
                 pool.submit(new Thread() {
                     public void run() {
 
                         try {
 
                             while (!applet.APPLETFINISHED) {
                                 sleepSafe(250);
                            }
                             //NOTE: each of the methods here should kill the loader dialog regardless of an exception inside
 
 //03/18/09 revised for SysBio Model Visualization By Xinjian
                             /*//load graph from service
                             createLoaderDialog("Loading Graph Data From PathCase Web Service");
                             loadPathCaseGraphCompartmentH();*/
                             createLoaderDialog("Loading Model Data From SysBio Web Service");
                             loadSysBioGraph();
                             sleepSafe(1000);
 
                             //load gene-organism-pathway, set organism hierarchy
                             Boolean organismBrowserEnabled = (Boolean) configuration.get("organismBrowserEnabled");
 //03/18/09 revised for SysBio Model Visualization By Xinjian
                             organismBrowserEnabled = false;
                             if (organismBrowserEnabled) {
                                 createLoaderDialog("Downloading Organism Taxonomy");
                                 String WebServiceURL = (String) configuration.get("WebServiceUrl");
                                 String setOrganism = (String) configuration.get("setOrganism");
                                 loadOrganismHierarchyFromRepositoryCompartmentH(WebServiceURL, setOrganism);
                             }
                             sleepSafe(1000);
 
                             Boolean geneViewerEnabled = (Boolean) configuration.get("geneViewerEnabled");
 //03/18/09 revised for SysBio Model Visualization By Xinjian
                             geneViewerEnabled = false;
                             if (geneViewerEnabled) {
                                 createLoaderDialog("Loading Gene List");
                                 String WebServiceURL = (String) configuration.get("WebServiceUrl");
                                 String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
                                 loadGenomeListCompartmentH(WebServiceURL, expandedPathwayGuids);
                             }
                             sleepSafe(1000);
 
                             //load genes
                             //Gene Viewer is self loading, see gene viewer...
 
                             //apply initial layout
                             createLoaderDialog("Applying New Layout");
 
                         }catch (Exception e) {
                             e.printStackTrace();
                             System.out.println(e.toString());
                         }finally {
                             killLoaderDialog();
                         }
                     }
                 });
             else {
                 (new LoadAction(this)).actionPerformed(null);
             }
 
         }else{
             if (configuration != null) {
                 processAppletParameters(false);
             }
         }
     }
 
     private String readFile( String file ) {
         try{
 
             BufferedReader reader = new BufferedReader( new FileReader (file));
             String line  = null;
             StringBuilder stringBuilder = new StringBuilder();
             String ls = System.getProperty("line.separator");
             while( ( line = reader.readLine() ) != null ) {
                 stringBuilder.append( line );
                 stringBuilder.append( ls );
             }
             return stringBuilder.toString();
         }
         catch(IOException e){
             e.printStackTrace();
         }
          return "";
      }
 
     //  loadSysBioGraph  loadSysBioGraph_Xu
         private void loadSysBioGraph_Xu() { //07/07/09 get data from file, afterwards, use loadSysBioGraph_FromService(), change loadSysBioGraph_FromService name back to loadSysBioGraph() for normal use.
 
        //TODO: for test
 //                FileWriter fw = new FileWriter("./tmpxml.xml");
 //                fw.write (graphXML);
 //                fw.close();
 //
                 String inputXML = readFile("d:\\XuHan\\PathwayViewOutput_Glycogen.xml");// ("d:\\a.xml");PathwayViewOutput_Urea_cycle
 //                repository.
                  MetaboliteXMLParser parser= new MetaboliteXMLParser(repository);
                 parser.loadRepositoryFromGraphXML(inputXML);
                 reloadFromRepository_Xu(false);
         }
 
     String getSavedLayout(String mid){
         if (serviceM != null) {
             String layout = serviceM.getPathwaysServiceSoap().retrieveLayout("SBget", "",mid, "");
             return layout;
         }else
         return "";
     }
 
     void loadFromLayout(String layout){
                             posToIDandName=new HashMap<String,String>();
                             nodeIndexToID=new HashMap<Integer,HashSet<String>>();
                             nodeToPathCaseId = new HashMap<Node, HashSet<String>>();
                             Graph2DView view= graphViewer.view;
 
                             Graph2D g = view.getGraph2D();
 
                             view.setVisible(false);
                             List list = createNodeDataList(g);
 
                             EncoderFactory backupEncoderFactory = GMLIOHandler.getEncoderFactory();
                             ParserFactory backupParserFactory = GMLIOHandler.getParserFactory();
                             CustomGMLFactory factory = new CustomGMLFactory(list);
                             GMLIOHandler.setEncoderFactory(factory);
                             GMLIOHandler.setParserFactory(factory);
                             graphViewer.setViewMode(DefaultYGraphViewerMetabolomics.GRAPH_MODE.EDIT);
 
                             y.io.IOHandler ioh=new GMLIOHandler();
                             try{
                                 g.clear();
                                 ByteArrayInputStream strea=new ByteArrayInputStream(layout.split("###")[0].getBytes());
                                 ioh.read(g,strea);//SBLayout.gml     "D:\\SBLayout.gml"
                                 view.updateView();
                             }catch(IOException ioe){
                                 D.show(ioe);
                             }
 
                             for (NodeCursor nodeCursor = g.nodes(); nodeCursor.ok(); nodeCursor.next()) {
                                 Node nod = nodeCursor.node();
                                 if(nod==null)break;
                                 Object value=null;
                                 for (Iterator it = list.iterator(); it.hasNext();)
                                 {
                                   NodeData data = (NodeData) it.next();
                                     if(data.getClassType().equals(String.class)){
                                         value = data.getNodeMap().get(nod);
                                         if(value == null)
                                             System.out.println("value is null");
                                         NodeRealizer nr = (NodeRealizer)view.getGraph2D().getRealizer(nod);
                                         if(nr!=null)
                                         posToIDandName.put(String.valueOf(nod.index()),(String)value);
                                         break;
                                     }
                                 }
                                HashSet<String> spelist = new HashSet<String>();
                                 spelist.add(((String)value).split("@@@")[0]);
                                 nodeToPathCaseId.put(nod,spelist);
                                 nodeIndexToID.put(new Integer(nod.index()),spelist);
                             }
                             GMLIOHandler.setEncoderFactory(backupEncoderFactory );
                             GMLIOHandler.setParserFactory(backupParserFactory);
                             Graph2D graph = graphViewer.view.getGraph2D();
                             try{
                                 HierarchyManager hm = new HierarchyManager(graph);
                                 hm.createGroupNode(graph.createNode());
                                 graph.clear();
                                 ByteArrayInputStream strea=new ByteArrayInputStream(layout.split("###")[1].getBytes());
                                 ioh.read(graph,strea);// "D:\\SBLayout_simple.gml"
                                 graphViewer.view.fitContent();
                             }catch(IOException e){
                                 D.show(e);
                             }
 
                             for (NodeCursor nodeCursor = g.nodes(); nodeCursor.ok(); nodeCursor.next()) {
                                 Node node = nodeCursor.node();
                                 
                                 nodeToPathCaseId.put(node,nodeIndexToID.get(new Integer(node.index())));
 
                                 if (HierarchyManager.getInstance(graphViewer.view.getGraph2D()).isGroupNode(node)) {
                                     GroupNodeRealizer nr = (GroupNodeRealizer)graphViewer.view.getGraph2D().getRealizer(node);
                                     ((AutoBoundsFeature)nr).setAutoBoundsEnabled(false);
                                     nr.setOpenGroupIcon(null);
                                     nr.setTransparent(true);
                                 }
                             }
 
                 bFrozenLoaded=true;
 //                            String highlightEntities = (String) configuration.get("highlightEntities");
 //                    if(highlightEntities!=null && !highlightEntities.equalsIgnoreCase(""))
 //                        makeCustomNodePainting(highlightEntities);
 //         for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
 ////        for (Node node : nodeToPathCaseId.keySet()) {
 //            NodeRealizer nr = (NodeRealizer)graphViewer.view.getGraph2D().getRealizer(nodeCursor.node());
 ////            HashSet<String> dbids = nodeToPathCaseId.get(node);
 ////            if (dbids == null /*|| dbids.size() != 1*/)
 ////                    continue;
 ////
 ////                String dbid = dbids.iterator().next();
 ////
 ////                Color customFillColor = null;
 ////                if (dbid != null && DBIDToCustomFillColor != null && DBIDToCustomFillColor.size() > 0)
 ////                    customFillColor = DBIDToCustomFillColor.get(dbid);
 //            nr.setFillColor(Color.GREEN);
 //        }
 
 
                     view.setVisible(true);
                     graphViewer.view.updateView();
 
     }
 
 
     private void loadSysBioGraph(String xmlFile)
     {
             String modelID=(String) configuration.get("modelID");
             String[] modelID_split=modelID.split(";");
             if(modelID.contains(";")) modelID=modelID.split(";")[0];
 
             String reactionGuids=(String) configuration.get("reactionGuids");
 
             if(reactionGuids==null) reactionGuids="";
 //            System.out.println(modelID);
              String graphXML=xmlFile;
             SysBioXMLParser parser=null;
 
 //        parser = new SysBioXMLParser(repository);
 //
 //        parser.loadRepositoryFromGraphXML(graphXML);
 //
 //        reloadFromRepository(false,reactionGuids);
 
         
 
 //                String strlayout=getSavedLayout(modelID);
 //                if(modelID_split.length==1 && strlayout.length()>20){
 
 //                    graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID, reactionGuids, "");//.getSBModelByID(modelID,"testModelId");//
 
 //                    parser = new SysBioXMLParser(repository);
 //
 //                    parser.loadRepositoryFromGraphXML(graphXML);
 //
 //                    reloadFromRepository(false,reactionGuids);
 //
 //                    PathCaseIDToTooltips=new HashMap<String, String>();
 //
 //                      for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
 //                          Node node = nodeCursor.node();
 //                          if(nodeToPathCaseId.containsKey(node)){
 //                              String strID=nodeToPathCaseId.get(node).toArray()[0].toString();
 //                              String strText=getNodeTipText(node);
 //                              PathCaseIDToTooltips.put(strID,strText);
 //                          }
 //                      }
 //
 //                        graphViewer.view.getGraph2D().clear();
 //                        graphViewer.view.getGraph2D().createNode();
 //                        loadFromLayout(strlayout);
 //                }else{//if there are multimodel
 //                    graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID, reactionGuids, "");//.getSBModelByID(modelID,"testModelId");//
 
                     parser = new SysBioXMLParser(repository);
 
                     parser.loadRepositoryFromGraphXML(graphXML);
 
                     reloadFromRepository(false,reactionGuids);
 //                }
 
                 //if there are multi models
 //                if(modelID_split.length>1){
 //                    repository2 = new PathCaseRepository();
 //                    repository.copyTo(repository2);  //save current repository to repository2
 //                    for(int mID=1;mID<modelID_split.length;mID++){
 //                        repository.reset();
 //    //                    SysBioXMLParser parser2 = new SysBioXMLParser(repository);
 //                        parser = new SysBioXMLParser(repository);
 //                        graphXML=  serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID_split[mID], reactionGuids, "");
 //                        parser.loadRepositoryFromGraphXML(graphXML);
 //
 //                         if(repository2!=null){
 //                            modelGraphBoundaryX=graphViewer.view.getGraph2D().getBoundingBox().getMaxX()+10;
 //
 //    //                        createGraphFromRepository(graphMode,reactionGuids);
 //                            if(modelID_split[mID].equalsIgnoreCase(modelID_split[mID-1]))
 //                                createAnotherGraphFromRepository(graphMode,"",modelGraphBoundaryX,0,1);
 //                            else
 //                                createAnotherGraphFromRepository(graphMode,"",modelGraphBoundaryX,0,0);
 //    //                        System.out.println("--- The Two models have comparments:"+getGoupNodesNum(graphViewer.view.getGraph2D()));
 //
 //                            HierarchyManager hm = HierarchyManager.getInstance(graphViewer.view.getGraph2D());
 //                            Graph2D gr=graphViewer.view.getGraph2D();
 //
 //                            for(Node n:nodeToPathCaseId2.keySet()){
 //                                if(hm.isGroupNode(n)){
 //                                    NodeRealizer groupNodeRealizer = gr.getRealizer(n);
 //                                    groupNodeRealizer.setLocation(groupNodeRealizer.getX()+modelGraphBoundaryX,groupNodeRealizer.getY());
 //                                }
 //                                else{
 //                                    NodeRealizer nr=gr.getRealizer(n);
 //                                    gr.setLocation(n,nr.getCenterX()+modelGraphBoundaryX-25,nr.getCenterY()-10);
 //                                }
 //                            }
 //                        }
 //                    }
 //                }
 
     }
 
     //07/07/09 get data with webservice, change name back to loadSysBioGraph() for normal use.
     private void loadSysBioGraph() { // loadSysBioGraph_FromService   loadSysBioGraph
         boolean done = false;
 
         if (configuration == null) done = true;
         else if (serviceM == null) {
                 String WebServiceURL = (String) configuration.get("WebServiceUrl");
                 if (WebServiceURL != null && !WebServiceURL.equals(""))
 //                connectToService(WebServiceURL);
                 connectToServiceCompartmentH(WebServiceURL);
                 else
                     done = true;
         }
 
         if (serviceM == null)
             done = true;
 
         if (!done) {
             String modelID=(String) configuration.get("modelID");
             String[] modelID_split=modelID.split(";");
             if(modelID.contains(";")) modelID=modelID.split(";")[0];
 
             String reactionGuids=(String) configuration.get("reactionGuids");
 
             if(reactionGuids==null) reactionGuids="";
 //            System.out.println(modelID);
              String graphXML="";
             SysBioXMLParser parser=null;
             if(((String) configuration.get("compartmentH")).equalsIgnoreCase("usermodel")){ //user upload type
                 graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "usermodel", "","", "",modelID, reactionGuids, "");//.getSBModelByID(modelID,"testModelId");//
 
                 parser = new SysBioXMLParser(repository);
 
                 parser.loadRepositoryFromGraphXML(graphXML);
 
                 reloadFromRepository(false,reactionGuids);
             } else{
                 String strlayout=getSavedLayout(modelID);
                 if(modelID_split.length==1 && strlayout.length()>20){
 
                     graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID, reactionGuids, "");//.getSBModelByID(modelID,"testModelId");//
 
                     parser = new SysBioXMLParser(repository);
 
                     parser.loadRepositoryFromGraphXML(graphXML);
 
                     reloadFromRepository(false,reactionGuids);
 
                     PathCaseIDToTooltips=new HashMap<String, String>();
 
                       for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
                           Node node = nodeCursor.node();
                           if(nodeToPathCaseId.containsKey(node)){
                               String strID=nodeToPathCaseId.get(node).toArray()[0].toString();
                               String strText=getNodeTipText(node);
                               PathCaseIDToTooltips.put(strID,strText);
                           }
                       }
 
                         graphViewer.view.getGraph2D().clear();
                         graphViewer.view.getGraph2D().createNode();
                         loadFromLayout(strlayout);
                 }else{//if there are multimodel
                     graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID, reactionGuids, "");//.getSBModelByID(modelID,"testModelId");//
 
                     parser = new SysBioXMLParser(repository);
 
                     parser.loadRepositoryFromGraphXML(graphXML);
 
                     reloadFromRepository(false,reactionGuids);
                 }
 
                 //if there are multi models
                 if(modelID_split.length>1){
                     repository2 = new PathCaseRepository();
                     repository.copyTo(repository2);  //save current repository to repository2
                     for(int mID=1;mID<modelID_split.length;mID++){
                         repository.reset();
     //                    SysBioXMLParser parser2 = new SysBioXMLParser(repository);
                         parser = new SysBioXMLParser(repository);
                         graphXML=  serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID_split[mID], reactionGuids, "");
                         parser.loadRepositoryFromGraphXML(graphXML);
 
                          if(repository2!=null){
                             modelGraphBoundaryX=graphViewer.view.getGraph2D().getBoundingBox().getMaxX()+10;
 
     //                        createGraphFromRepository(graphMode,reactionGuids);
                             if(modelID_split[mID].equalsIgnoreCase(modelID_split[mID-1]))
                                 createAnotherGraphFromRepository(graphMode,"",modelGraphBoundaryX,0,1);
                             else
                                 createAnotherGraphFromRepository(graphMode,"",modelGraphBoundaryX,0,0);
     //                        System.out.println("--- The Two models have comparments:"+getGoupNodesNum(graphViewer.view.getGraph2D()));
 
                             HierarchyManager hm = HierarchyManager.getInstance(graphViewer.view.getGraph2D());
                             Graph2D gr=graphViewer.view.getGraph2D();
 
                             for(Node n:nodeToPathCaseId2.keySet()){
                                 if(hm.isGroupNode(n)){
                                     NodeRealizer groupNodeRealizer = gr.getRealizer(n);
                                     groupNodeRealizer.setLocation(groupNodeRealizer.getX()+modelGraphBoundaryX,groupNodeRealizer.getY());
                                 }
                                 else{
                                     NodeRealizer nr=gr.getRealizer(n);
                                     gr.setLocation(n,nr.getCenterX()+modelGraphBoundaryX-25,nr.getCenterY()-10);
                                 }
                             }
                         }
                     }
                 }
             }
 
         }
 //        graphViewer.view.fitContent();
     }
 
       private void loadMQLGraph(String inputXML) { // loadSysBioGraph_FromService   loadSysBioGraph
         boolean done = false;
 
         if (configuration == null) done = true;
         else {
             MQLXMLParser parser= new MQLXMLParser(repository);
             parser.loadRepositoryFromGraphXML(inputXML);
             reloadFromMQLRepository(false,"");
         }
 
 //        if (!done) {
 //            String modelID=(String) configuration.get("modelID");
 //            String[] modelID_split=modelID.split(";");
 //            if(modelID.contains(";")) modelID=modelID.split(";")[0];
 //
 //            String reactionGuids=(String) configuration.get("reactionGuids");
 //
 //            if(reactionGuids==null) reactionGuids="";
 ////            System.out.println(modelID);
 //             String graphXML="";
 //            SysBioXMLParser parser=null;
 //            if(((String) configuration.get("compartmentH")).equalsIgnoreCase("usermodel")){ //user upload type
 //                graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "usermodel", "","", "",modelID, reactionGuids, "");//.getSBModelByID(modelID,"testModelId");//
 //
 //                parser = new SysBioXMLParser(repository);
 //
 //                parser.loadRepositoryFromGraphXML(graphXML);
 //
 //                reloadFromRepository(false,reactionGuids);
 //            } else{
 //                String strlayout=getSavedLayout(modelID);
 //                if(modelID_split.length==1 && strlayout.length()>20){
 //                        graphViewer.view.getGraph2D().createNode();
 //                        loadFromLayout(strlayout);
 //                }else{//if there are multimodel
 //                    graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID, reactionGuids, "");//.getSBModelByID(modelID,"testModelId");//
 //
 //                    parser = new SysBioXMLParser(repository);
 //
 //                    parser.loadRepositoryFromGraphXML(graphXML);
 //
 //                    reloadFromRepository(false,reactionGuids);
 //                }
 //
 //                //if there are multi models
 //                if(modelID_split.length>1){
 //                    repository2 = new PathCaseRepository();
 //                    repository.copyTo(repository2);  //save current repository to repository2
 //                    for(int mID=1;mID<modelID_split.length;mID++){
 //                        repository.reset();
 //    //                    SysBioXMLParser parser2 = new SysBioXMLParser(repository);
 //                        parser = new SysBioXMLParser(repository);
 //                        graphXML=  serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",modelID_split[mID], reactionGuids, "");
 //                        parser.loadRepositoryFromGraphXML(graphXML);
 //
 //                         if(repository2!=null){
 //                            modelGraphBoundaryX=graphViewer.view.getGraph2D().getBoundingBox().getMaxX()+10;
 //
 //    //                        createGraphFromRepository(graphMode,reactionGuids);
 //                            if(modelID_split[mID].equalsIgnoreCase(modelID_split[mID-1]))
 //                                createAnotherGraphFromRepository(graphMode,"",modelGraphBoundaryX,0,1);
 //                            else
 //                                createAnotherGraphFromRepository(graphMode,"",modelGraphBoundaryX,0,0);
 //    //                        System.out.println("--- The Two models have comparments:"+getGoupNodesNum(graphViewer.view.getGraph2D()));
 //
 //                            HierarchyManager hm = HierarchyManager.getInstance(graphViewer.view.getGraph2D());
 //                            Graph2D gr=graphViewer.view.getGraph2D();
 //
 //                            for(Node n:nodeToPathCaseId2.keySet()){
 //                                if(hm.isGroupNode(n)){
 //                                    NodeRealizer groupNodeRealizer = gr.getRealizer(n);
 //                                    groupNodeRealizer.setLocation(groupNodeRealizer.getX()+modelGraphBoundaryX,groupNodeRealizer.getY());
 //                                }
 //                                else{
 //                                    NodeRealizer nr=gr.getRealizer(n);
 //                                    gr.setLocation(n,nr.getCenterX()+modelGraphBoundaryX-25,nr.getCenterY()-10);
 //                                }
 //                            }
 //                        }
 //                    }
 //                }
 //            }
 //
 //        }
 //        graphViewer.view.fitContent();
     }
 
     private void loadPathCaseGraph(String pwid) {
         boolean done = false;
 
         if (configuration == null) done = true;
 
         else if (serviceM == null) {
             String WebServiceURL = (String) configuration.get("WebServiceUrl");
             if (WebServiceURL != null && !WebServiceURL.equals(""))
 //                connectToService(WebServiceURL);
             connectToServiceCompartmentH(WebServiceURL);
             else
                 done = true;
         }
 
         if (serviceM == null)
             done = true;
 
         if (!done) {
 
             String modelID=(String) configuration.get("modelID");
             pathwayID=pwid;
                                                       
             String graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", pathwayID, "","", "",modelID, "", "");
 
             repository.resetPWrelated();
             PathCaseXMLParser parser = new PathCaseXMLParser(repository);
 
             parser.loadRepositoryFromGraphXML(graphXML);
             System.out.println("Creating Pathway graph");
             reloadFromRepository_Pathway(false);
         }
     }
 
     String getPathWayIDFromModelID(String mId){
         if(mId!=null)
             mId="a13e6ceb-e807-4171-ac05-9ce6db75816e";  // model:41a0a620-d272-4915-9107-201b376c4981,Conant2007_glycolysis_2C              ;pathway id:a13e6ceb-e807-4171-ac05-9ce6db75816e,   Glycolysis / Gluconeogenesis
         return mId;
     }
 
     public void doFirstTimeLayoutCompartmentH() {
        /* initialLayout();*/
 
         Boolean FrozenLayout = (Boolean) configuration.get("FrozenLayout");
         if (FrozenLayout != null && FrozenLayout) {
             doInitialFrozenLayoutCompartmentH();
         } else {
 //            initialLayout();
 
 
             Graph2D view = graphViewer.view.getGraph2D();
 //
         int m=0,j=0;
         NodeMap nodeMap = Maps.createHashedNodeMap();
         for (NodeCursor nodeCursor = view.nodes(); nodeCursor.ok(); nodeCursor.next()) {
             Node node = nodeCursor.node();
             if (HierarchyManager.getInstance(view).isGroupNode(node)) {
                 PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
 //                nr.setLocation(graphViewer.view.getViewPoint2D().getX()+ view.getRealizer(node).getBoundingBox().getMinX(),graphViewer.view.getViewPoint2D().getY()+ view.getRealizer(node).getBoundingBox().getMinY());
 //                if(m==0){
                     String st=this.nodeToPathCaseId.get(node).toArray()[0].toString();
                     String st1=st.split(":")[1];
                     System.out.println(st1);
                     String st2=st.split(":")[2];
                     System.out.println(st2);
                 if(st1!="" && st2!="")
                     nr.setLocation(Double.parseDouble(st1),Double.parseDouble(st2));
             }
         }
         }
 
     }
 
     public void doInitialFrozenLayoutCompartmentH() {
           //TODO: only dealing with expanded Pathways now
 //          int np = numberOfPathways();
 //          if(np <1){
 //              initialLayout();
 
         Graph2D view = graphViewer.view.getGraph2D();
 
          graphViewer.view.updateView();
       }
 
     public void doFirstTimeLayout() {
         //layout: check parameter(whether to use frozen layout or not
         Boolean FrozenLayout = (Boolean) configuration.get("FrozenLayout");
         if (FrozenLayout != null && FrozenLayout){
             doInitialFrozenLayout();
         }else{
             initialLayout();
         }
     }
 
     public void dojslayout(){
         graphViewer.view.fitContent();
     }
 
     private void initialLayout() {
         String layout = (String) configuration.get("layout");
         String sourceNode = (String) configuration.get("sourceNode");
         if (layout != null && sourceNode != null)
             graphViewer.initialLayout(layout, sourceNode);
         else
             graphViewer.bestLayout();
     }
 
     public void makeCustomNodePainting(String highlightEntities) {
 
         //Highlighting
         DBIDToCustomFillColor = new HashMap<String, Color>();
         HashSet<String> highlightNodes=new HashSet<String>();
 
 
         if (!highlightEntities.equals("")) {
             if (highlightEntities.endsWith(GUIDSEPERATOR))
                 highlightEntities = highlightEntities.substring(0, highlightEntities.length() - GUIDSEPERATOR.length());
 
             StringTokenizer stguid = new StringTokenizer(highlightEntities, GUIDSEPERATOR);
             while (stguid.hasMoreTokens()) {
                 String guidpluscolor = stguid.nextToken().toLowerCase();
                 StringTokenizer stcolor = new StringTokenizer(guidpluscolor, COLORSEPERATOR);
                 if (stcolor.countTokens() < 2) {
                     System.out.println("Error in higlightentities parameter: color name seperation failed : format should be guid@colorname");
                     //System.exit(1);
                 }
 
                 String guid = stcolor.nextToken().toLowerCase().trim();
                 String colorname = stcolor.nextToken().toLowerCase().trim();
                 highlightNodes.add(guid);
                 DBIDToCustomFillColor.put(guid, PathCaseViewGenerator.colorNameToColorObject(colorname));
             }
         }
 
         if(valueOfcompartmentH.equalsIgnoreCase("model")){
             if(!bFrozenLoaded)
                 graphColorResetCompartmentH(highlightNodes);
             else 
                 graphColorResetCompartmentHFrozen(highlightNodes);
         }else graphColorReset();
 
         graphViewer.view.updateView();
     }
 
     private void graphColorResetCompartmentHFrozen(HashSet<String> highlights){
 //        for (Node node : nodeToPathCaseId.keySet()) {
         HashSet<Node> nonChangedNodes=new HashSet<Node>();
         HashSet<Edge> nonChangedEdges=new HashSet<Edge>();
 
         for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
             NodeRealizer nr = (NodeRealizer)graphViewer.view.getGraph2D().getRealizer(nodeCursor.node());
             HashSet<String> dbids = nodeToPathCaseId.get(nodeCursor.node());
             if (dbids == null /*|| dbids.size() != 1*/)
                     continue;
 //
                 String dbid = dbids.iterator().next();
             if(highlights.contains(dbid)){
                 nonChangedNodes.add(nodeCursor.node());
                 for(NodeCursor nodeCur = nodeCursor.node().neighbors(); nodeCur.ok(); nodeCur.next()){
                                nonChangedNodes.add(nodeCur.node());
                 }
                 for(EdgeCursor ec=nodeCursor.node().edges(); ec.ok();ec.next())
                 nonChangedEdges.add(ec.edge());
             }
 //
                 Color customFillColor = null;
                 if (dbid != null && DBIDToCustomFillColor != null && DBIDToCustomFillColor.size() > 0)
                     customFillColor = DBIDToCustomFillColor.get(dbid);
 //            nr.setFillColor(Color.GREEN);
             if (customFillColor != null)
                 nr.setFillColor(customFillColor);
         }
         HierarchyManager hm = HierarchyManager.getInstance(graphViewer.view.getGraph2D());
         for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
             if(nonChangedNodes.contains(nodeCursor.node()) || hm.isGroupNode(nodeCursor.node()))continue;
             else {
                  NodeRealizer nr = (NodeRealizer)graphViewer.view.getGraph2D().getRealizer(nodeCursor.node());
                  nr.setFillColor(Color.white);
                  nr.getLabel().setTextColor(Color.RED);
 //                    nr.setLineColor(Color.ORANGE);
                 nr.setLineColor(Color.gray);
             }
         }
         for(EdgeCursor eCursor=graphViewer.view.getGraph2D().edges();eCursor.ok();eCursor.next()){
              if(nonChangedEdges.contains(eCursor.edge()))continue;
             else {
                  EdgeRealizer nr = (EdgeRealizer)graphViewer.view.getGraph2D().getRealizer(eCursor.edge());
 //                 nr.setFillColor(Color.white);
                  nr.setLineColor(Color.gray);
 //                 nr.getLabel().setTextColor(Color.RED);
 //                    nr.setLineColor(Color.ORANGE);
 //                nr.setLineColor(Color.gray);
             }
         }
     }
     private void graphColorResetCompartmentH(HashSet<String> highlights){
         HashSet<Node> nonChangedNodes=new HashSet<Node>();
         HashSet<Edge> nonChangedEdges=new HashSet<Edge>();
 
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
                 PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
                 HashSet<String> dbids = nodeToPathCaseId.get(node);
 
                 //TODO: ignores multiple id nodes, ie, modulators
                 if (dbids == null /*|| dbids.size() != 1*/)
                     continue;
 
                 String dbid = dbids.iterator().next();
 
                 Color customFillColor = null;
 
                 if (dbid != null && DBIDToCustomFillColor != null && DBIDToCustomFillColor.size() > 0)
                     customFillColor = DBIDToCustomFillColor.get(dbid);
 
                 nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.NORMAL);
 
 
                 if (customFillColor != null || DBIDToCustomFillColor == null || DBIDToCustomFillColor.size() == 0) {
 //                    if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS)
 //                        PathCaseViewGenerator.makeGenericProcessShapeNodeRealizerNormal(nr);
 //                    else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)
 //                        PathCaseViewGenerator.makeCollapsedPathwayShapeNodeRealizerNormal(nr);
 //                    else
 //                    if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
 //                        PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerNormal(nr);
 //                    else
 //                    if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
 //                        PathCaseViewGenerator.makeCofactorShapeNodeRealizerNormal(nr);
 //                    else
 //                    if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR)
 //                        PathCaseViewGenerator.makeRegulatorShapeNodeRealizerNormal(nr);
 
                     if (customFillColor != null)
                         PathCaseViewGenerator.fillNodeRealizerCustom(nr, customFillColor);
                 }
                 //grayout the rest
                 else if (customFillColor == null && DBIDToCustomFillColor != null && DBIDToCustomFillColor.size() > 0) {
                     /*if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS)
                         PathCaseViewGenerator.makeGenericProcessShapeNodeRealizerGrayedOut(nr);
                     else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)
                         PathCaseViewGenerator.makeCollapsedPathwayShapeNodeRealizerGrayedOut(nr);
                     else
                     if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
                         PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerGrayedOut(nr);
                     else
                     if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                         PathCaseViewGenerator.makeCofactorShapeNodeRealizerGrayedOut(nr);
                     else
                     if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR)
                         PathCaseViewGenerator.makeRegulatorShapeNodeRealizerGrayedOut(nr);
                     */
 
 //                    if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
 //                        PathCaseViewGenerator.makeGenericProcessShapeNodeRealizerGrayedOut(nr);
 //                        nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT);
 //                    } else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
 //                        PathCaseViewGenerator.makeCollapsedPathwayShapeNodeRealizerGrayedOut(nr);
 //                        nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT);
 //                    }
 
                 }
 
             }
 
 
         for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
             NodeRealizer nr = (NodeRealizer)graphViewer.view.getGraph2D().getRealizer(nodeCursor.node());
             HashSet<String> dbids = nodeToPathCaseId.get(nodeCursor.node());
             if (dbids == null /*|| dbids.size() != 1*/)
                     continue;
 //
                 String dbid = dbids.iterator().next();
             if(highlights.contains(dbid)){
                 nonChangedNodes.add(nodeCursor.node());
                 for(NodeCursor nodeCur = nodeCursor.node().neighbors(); nodeCur.ok(); nodeCur.next()){
                                nonChangedNodes.add(nodeCur.node());
                 }
                 for(EdgeCursor ec=nodeCursor.node().edges(); ec.ok();ec.next())
                 nonChangedEdges.add(ec.edge());
             }
 //
                 Color customFillColor = null;
                 if (dbid != null && DBIDToCustomFillColor != null && DBIDToCustomFillColor.size() > 0)
                     customFillColor = DBIDToCustomFillColor.get(dbid);
 //            nr.setFillColor(Color.GREEN);
             if (customFillColor != null)
                 nr.setFillColor(customFillColor);
         }
         HierarchyManager hm = HierarchyManager.getInstance(graphViewer.view.getGraph2D());
         for (NodeCursor nodeCursor = graphViewer.view.getGraph2D().nodes(); nodeCursor.ok(); nodeCursor.next()) {
             if(nonChangedNodes.contains(nodeCursor.node()) || hm.isGroupNode(nodeCursor.node()))continue;
             else {
                  NodeRealizer nr = (NodeRealizer)graphViewer.view.getGraph2D().getRealizer(nodeCursor.node());
                  nr.setFillColor(Color.white);
                  nr.getLabel().setTextColor(Color.RED);
 //                    nr.setLineColor(Color.ORANGE);
                 nr.setLineColor(Color.gray);
             }
         }
         for(EdgeCursor eCursor=graphViewer.view.getGraph2D().edges();eCursor.ok();eCursor.next()){
              if(nonChangedEdges.contains(eCursor.edge()))continue;
             else {
                  EdgeRealizer nr = (EdgeRealizer)graphViewer.view.getGraph2D().getRealizer(eCursor.edge());
 //                 nr.setFillColor(Color.white);
                  nr.setLineColor(Color.gray);
 //                 nr.getLabel().setTextColor(Color.RED);
 //                    nr.setLineColor(Color.ORANGE);
 //                nr.setLineColor(Color.gray);
             }
         }
 
 
 
 
 
 
             //wy:highlight linking entities if not grayed out
             if (DBIDToCustomFillColor == null || DBIDToCustomFillColor.size() <= 0) {
                 String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
                 HashSet<String> linkingMolecules = TableQueries.getLinkingMolecules(GUIDParser(expandedPathwayGuids), repository);
                 for (String id : linkingMolecules) {
                     Node node = this.getNodeByPathCaseID(id);
                     if (node==null) continue;
                     PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
                     if (nr.getNodeMode() != PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT && nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON) {
                         PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerLinkingMolecule(nr);
                     }
                 }
 
 /*            linkingMolecules = TableQueries.getLinkingMoleculesWithOtherPathways(GUIDParser(expandedPathwayGuids),repository);
             for (String id: linkingMolecules)
             {
                 Node node = this.getNodeByPathCaseID(id);
                 PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
                 if (nr.getNodeMode() !=PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT && nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
                 {
                     PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerLinkingMoleculeWithOtherPathways(nr);
                 }
             }*/
             }
 
 //            for (Edge edge : graphViewer.view.getGraph2D().getEdgeArray()) {
 //                EdgeRealizer ne = graphViewer.view.getGraph2D().getRealizer(edge);
 //                Node from = edge.source();
 //                Node to = edge.target();
 //
 //                PathCaseShapeNodeRealizer nrfrom = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(from);
 //                PathCaseShapeNodeRealizer nrto = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(to);
 //
 //                if (((nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON) && (nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)) || ((nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) && (nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)))
 //                    PathCaseViewGenerator.makeMetaboliteEdgeNormal(ne);
 //                    //regulator and cofactor edges are always directed to processes
 //                else
 //                if (nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
 //                    PathCaseViewGenerator.makeRegulatorCofactorEdgeNormal(ne, nrfrom);
 //                else
 //                if (nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
 //                    PathCaseViewGenerator.makeRegulatorCofactorEdgeNormal(ne, nrto);
 //                else if (nrfrom.getNodeRole() == nrto.getNodeRole())
 //                    PathCaseViewGenerator.makeArtificialEdgeNormal(ne);
 //            }
 
             makeGrayingOutAfterNodeRoleSetup();
     }
 
     private void graphColorReset() {
 
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             HashSet<String> dbids = nodeToPathCaseId.get(node);
 
             //TODO: ignores multiple id nodes, ie, modulators
             if (dbids == null /*|| dbids.size() != 1*/)
                 continue;
 
             String dbid = dbids.iterator().next();
 
             Color customFillColor = null;
 
 
             if (dbid != null && DBIDToCustomFillColor != null)
                 customFillColor = DBIDToCustomFillColor.get(dbid);
 
 
             nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.NORMAL);
 
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS)
                 PathCaseViewGenerator.makeGenericProcessShapeNodeRealizerNormal(nr);
             else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)
                 PathCaseViewGenerator.makeCollapsedPathwayShapeNodeRealizerNormal(nr);
             else
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
                 PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerNormal(nr);
             else
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                 PathCaseViewGenerator.makeCofactorShapeNodeRealizerNormal(nr);
             else
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR)
                 PathCaseViewGenerator.makeRegulatorShapeNodeRealizerNormal(nr);
 
             if (customFillColor != null)
                 PathCaseViewGenerator.fillNodeRealizerCustom(nr, customFillColor);
         }
 
 
         for (Edge edge : graphViewer.view.getGraph2D().getEdgeArray()) {
             EdgeRealizer ne = graphViewer.view.getGraph2D().getRealizer(edge);
             Node from = edge.source();
             Node to = edge.target();
 
             PathCaseShapeNodeRealizer nrfrom = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(from);
             PathCaseShapeNodeRealizer nrto = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(to);
 
             if (((nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON) && (nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)) || ((nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) && (nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)))
                 PathCaseViewGenerator.makeMetaboliteEdgeNormal(ne);
                 //regulator and cofactor edges are always directed to processes
             else
             if (nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nrfrom.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                 PathCaseViewGenerator.makeRegulatorCofactorEdgeNormal(ne, nrfrom);
             else
             if (nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nrto.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                 PathCaseViewGenerator.makeRegulatorCofactorEdgeNormal(ne, nrto);
             else if (nrfrom.getNodeRole() == nrto.getNodeRole())
                 PathCaseViewGenerator.makeArtificialEdgeNormal(ne);
         }
     }
 
     public void loadOrganismHierarchyFromRepositoryCompartmentH(String WebServiceURL, String setOrganism) {
          if (serviceM == null)
             connectToServiceCompartmentH(WebServiceURL);
 
         if (serviceM == null) {
             JOptionPane.showMessageDialog(this, "Cannot access to organism hierarchy web method.", "Warning", JOptionPane.WARNING_MESSAGE);
             return;
         }
 
         if (repository == null) {
             JOptionPane.showMessageDialog(this, "Cannot access to local cache.", "Warning", JOptionPane.WARNING_MESSAGE);
             return;
         }
 
         if (!this.organismBrowserButton.isEnabled()) return;
 
         PathCaseXMLParser parser = new PathCaseXMLParser(repository);
         String organismXML = serviceM.getPathwaysServiceSoap().getOrganismHierarchy();
 
 
         parser.loadRepositoryFromOrganismXML(organismXML);
 
         resetOrganismHierarchyInBrowser(setOrganism);
     }
 
     public void loadOrganismHierarchyFromRepository(String WebServiceURL, String setOrganism) {
         if (service == null)
             connectToService(WebServiceURL);
 
         if (service == null)
             return;
 
         if (repository == null)
             return;
 
         if (!this.organismBrowserButton.isEnabled()) return;
 
         PathCaseXMLParser parser = new PathCaseXMLParser(repository);
         String organismXML = service.getPathwaysServiceSoap().getOrganismHierarchy();
         parser.loadRepositoryFromOrganismXML(organismXML);
 
         resetOrganismHierarchyInBrowser(setOrganism);
         //organismSelected(organismPanel.selectedOrganismIdList);
         /*for (String s:organismPanel.selectedOrganismIdList)
         {
             System.out.println(s);
         }*/
     }
 
     HashSet<String> organismIDsWGeneData;
 
 
     public void loadGenomeListCompartmentH(String WebServiceURL, String expandedPathwayGuids) {
     if (serviceM == null)
         connectToServiceCompartmentH(WebServiceURL);
 
     if (serviceM == null) {
         JOptionPane.showMessageDialog(this, "Cannot access to gene mapping web method.", "Warning", JOptionPane.WARNING_MESSAGE);
         return;
     }
 
     if (repository == null) {
         JOptionPane.showMessageDialog(this, "Cannot access to local cache.", "Warning", JOptionPane.WARNING_MESSAGE);
         return;
     }
 
     if (!this.geneViewerButton.isEnabled()) return;
 
         ArrayList<String> pathwaylist = new ArrayList<String>(GUIDParser(expandedPathwayGuids));
          if (pathwaylist.size() != 1) return;
 //    HashSet<String> pathwaylist = GUIDParser(expandedPathwayGuids);
 //    String pathwaylistlinked = GUIDSetToString(pathwaylist);
 
     PathCaseXMLParser parser = new PathCaseXMLParser(repository);
     String genomepathwaylistXML = serviceM.getPathwaysServiceSoap().getGenomesForPathways(pathwaylist.get(0));
     //System.out.println(genomepathwaylistXML);
     organismIDsWGeneData = parser.loadOrganismListFromGeneMappingXML(genomepathwaylistXML);
 }
 
     public void loadGenomeList(String WebServiceURL, String expandedPathwayGuids) {
         if (service == null)
             connectToService(WebServiceURL);
 
         if (service == null)
             return;
 
         if (repository == null)
             return;
 
         if (!this.geneViewerButton.isEnabled()) return;
 
         ArrayList<String> pathwaylist = new ArrayList<String>(GUIDParser(expandedPathwayGuids));
         if (pathwaylist.size() != 1) return;
 
         PathCaseXMLParser parser = new PathCaseXMLParser(repository);
         String genomepathwaylistXML = service.getPathwaysServiceSoap().getGenomesForPathway(pathwaylist.get(0));
 
         //System.out.println(genomepathwaylistXML);
         organismIDsWGeneData = parser.loadOrganismListFromGeneMappingXML(genomepathwaylistXML);
     }
 
     /////////////////////////////////////////    UTILITIES   ////////////////////////////////////////
 
     public Node getNodeByPathCaseID(String id) {
         //TODO use index instead of exhaustive search
         Iterator<Node> iter = nodeToPathCaseId_Pathway.keySet().iterator();
         Node n = null;
         while(iter.hasNext())
         {
             n = iter.next();
             HashSet<String> pid = nodeToPathCaseId_Pathway.get(n);
             if (pid != null && pid.contains(id))
                 return n;
         }
 //        Node[] pwNodes=(Node[])nodeToPathCaseId_Pathway.keySet().toArray();
 //        for (Node n : pwNodes ){//graphViewer.view.getGraph2D().getNodeArray()) {
 //            HashSet<String> pid = nodeToPathCaseId_Pathway.get(n);
 //            if (pid != null && pid.contains(id))
 //                return n;
 //        }
 
         return null;
     }
 
     private HashSet<String> GUIDParser(String guidlist) {
         HashSet<String> guidlistparsed = new HashSet<String>();
         if (guidlist == null) return guidlistparsed;
 
         StringTokenizer st = new StringTokenizer(guidlist, GUIDSEPERATOR);
         while (st.hasMoreTokens()) {
             String token = st.nextToken().trim().toLowerCase();
             if (!token.equals(""))
                 guidlistparsed.add(token);
         }
         return guidlistparsed;
     }
 
     private String GUIDSetToString(HashSet<String> Guids) {
         boolean firstTime = true;
         String output = "";
         for (String id : Guids) {
             if (!firstTime) {
                 output += GUIDSEPERATOR;
             } else {
                 firstTime = false;
             }
             output += id;
         }
 
         return output;
     }
 
     private void reloadFromRepository_Xu(boolean doLayout) {
 //        graphViewer.resetView();
 //        createGraphFromRepository_Xu(graphMode);
 
 //        String highlightEntities = (String) configuration.get("highlightEntities");
 //        makeCustomNodePainting(highlightEntities);
 
 //        graphViewer.view.updateView();
 
 //        if(this.valueOfcompartmentH.equalsIgnoreCase("true")||this.valueOfcompartmentH.equalsIgnoreCase("neutral")){
 //            if (doLayout && graphMode.isDefault())
 //                doFirstTimeLayout();
 //            else if (doLayout)
                 graphViewer.bestLayout();
 //        }else{
 //            if (doLayout)
 //                graphViewer.bestLayout();
 //        }
     }
 
     private void reloadFromRepository_Pathway(boolean doLayout) {
 
         createGraphFromRepository_Pathway(graphMode);
         System.out.println("Created pathway graph");
 
 //           String highlightEntities = (String) configuration.get("highlightEntities");
 
            graphViewer.view.updateView();
        }
 
     
     private void reloadFromRepository(boolean doLayout) {
         reloadFromRepository(doLayout,"");
     }
 
     int getGoupNodesNum(Graph2D grp){
         HierarchyManager hm = HierarchyManager.getInstance(grp);
         int iR=0;
         for(Node n:grp.getNodeArray()){
             if(hm.isGroupNode(n))iR++;
         }
         return iR;
     }
     
     public LinkedList<Node> getGroupNodes(Graph2D graph) {
     	LinkedList<Node> resultNodes = new LinkedList<Node>();
     	HierarchyManager hm = HierarchyManager.getInstance(graph);
     	for(Node n : graph.getNodeArray()) {
     		if(hm.isGroupNode(n)) {
     			resultNodes.add(n);
     		}
     	}
     	return resultNodes;
     }
 
     private void reloadFromMQLRepository(boolean doLayout, String reactionGuids) {
         graphViewer.resetView();
         createGraphFromMQLRepository(graphMode,reactionGuids);
 
         graphViewer.view.updateView();
     }
 
     private void reloadFromRepository(boolean doLayout, String reactionGuids) {
         graphViewer.resetView();
 //        graphViewer.view.getGraph2D().clear();
         createGraphFromRepository(graphMode,reactionGuids);
 //        loadPathCaseGraph("959c48c3-fa7e-4ada-91c2-1800f2f179cb");   //lo ad pathway
 
         System.out.println("--- The value of show common is:"+graphMode.showcommonmoleculesingraph);
 
         if(repository2!=null){
             modelGraphBoundaryX=graphViewer.view.getGraph2D().getBoundingBox().getMaxX()+50;
                                                                                                                       
             createAnotherGraphFromRepository(graphMode,"",modelGraphBoundaryX,0,0);
 
             System.out.println("--- The Two models have comparments:"+getGoupNodesNum(graphViewer.view.getGraph2D()));
 
             HierarchyManager hm = HierarchyManager.getInstance(graphViewer.view.getGraph2D());
             Graph2D gr=graphViewer.view.getGraph2D();
 
             for(Node n:nodeToPathCaseId2.keySet()){
                 if(hm.isGroupNode(n)){
                     NodeRealizer groupNodeRealizer = gr.getRealizer(n);
 
                     groupNodeRealizer.setLocation(groupNodeRealizer.getX()+modelGraphBoundaryX,groupNodeRealizer.getY());
 
                 }
                 else{
                     NodeRealizer nr=gr.getRealizer(n);
                     gr.setLocation(n,nr.getCenterX()+modelGraphBoundaryX,nr.getCenterY());
                 }
             }
         }
 
         graphViewer.view.updateView();
     }
 
     private void drawFinalGraph(String pwid, String strLayout){
 //
         
 //        if(!bFrozenLoaded){
         bFrozenLoaded=false;
              createGraphFromRepository(graphMode,""); //create biomodel graph      If do not redraw model graph there'll be problems when more corresponding pathway exits.
 
             if(hasMultiCompartments(graphViewer.view.getGraph2D())){ //if has compartments>1
                 }else{  //only one compartment,apply layout.
                         (new PathCaseLayouterMetabolomics("organic")).start(graphViewer.view.getGraph2D());
                     }
             graphViewer.view.fitContent();
                 graphViewer.view.updateView();
 //        }else{
 //            String modelID=(String) configuration.get("modelID");
 //            String strlayout=getSavedLayout(modelID);
 //            loadFromLayout(strlayout);
 //        }
 
 
         modelGraphBoundaryX=graphViewer.view.getGraph2D().getBoundingBox().getMaxX()+50;
         double modelGraphBoundaryY=graphViewer.view.getGraph2D().getBoundingBox().getMinY();
 
         //adding split line between biomodel graph with pathway
         Node lTop=graphViewer.view.getGraph2D().createNode();
         NodeRealizer lTopr= new ShapeNodeRealizer();
         lTopr.setSize(2,2);
         lTopr.setLocation(modelGraphBoundaryX,0);
         lTopr.setFillColor(Color.red);
         graphViewer.view.getGraph2D().setRealizer(lTop,lTopr);
         Node lBot=graphViewer.view.getGraph2D().createNode();
 
         loadPathCaseGraph(pwid);   //lo ad pathway         959c48c3-fa7e-4ada-91c2-1800f2f179cb
         System.out.println("load pathcasegraph, doing frozenlayout");
         doInitialFrozenLayout(strLayout);
 
         Node pwFrameNode=null;
         for (Node n:nodeToPathCaseId_Pathway.keySet()){
             if(nodeToPathCaseId_Pathway.get(n).contains("pathway")){
                 pwFrameNode=n;
                 break;
             }
         }
 
         NodeRealizer lBotr= new ShapeNodeRealizer();
         lBotr.setSize(2,2);
         lBotr.setLocation(modelGraphBoundaryX,graphViewer.view.getGraph2D().getBoundingBox().getMaxY());
         lBotr.setFillColor(Color.red);
         graphViewer.view.getGraph2D().setRealizer(lBot,lBotr);
         Edge ledge=graphViewer.view.getGraph2D().createEdge(lTop,lBot);
         GenericEdgeRealizer er = new GenericEdgeRealizer();
         graphViewer.view.getGraph2D().setRealizer(ledge, er);
         er.setLineType(LineType.DASHED_2 );
 //                    er.setRatio(-1);
 
         graphViewer.view.updateView();
 
         //resize the pathway's rectangle:
         if(pwFrameNode!=null){
             PathCaseShapeNodeRealizer nrpw = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(pwFrameNode);
             modelGraphBoundaryX+=45;
             nrpw.setWidth(graphViewer.view.getGraph2D().getBoundingBox().getMaxX()-modelGraphBoundaryX);
             nrpw.setHeight(graphViewer.view.getGraph2D().getBoundingBox().getHeight()-modelGraphBoundaryY);
 //            nrpw.setX(modelGraphBoundaryX);
             nrpw.setLocation(modelGraphBoundaryX,modelGraphBoundaryY-10);
              graphViewer.view.getGraph2D().setRealizer(pwFrameNode,nrpw);
         }
 
 
         //add dashed lines for corresponding elements. When click make it lighter.
         ///get species list and mapping with molecules list.
         HashMap<String, Node> SpecTONode=revertModel(nodeToPathCaseId);
 
         HashMap<String, Node> MoleTONode= revert(nodeToPathCaseId_Pathway);
 
         for(ModelPathwayElementsMappingTable.SpecMoleTableEntry sp:repository.modelPathwayElementsMappingTable.getSpeMoleContentTable()){
                 Node nS=SpecTONode.get(sp.SpeciesID);
                 Node nT=MoleTONode.get(sp.MolecularID);
             if(nS!=null && nT!=null){
                 Edge eST=graphViewer.view.getGraph2D().createEdge(nS,nT);
                 GenericEdgeRealizer erST = new GenericEdgeRealizer();
                 graphViewer.view.getGraph2D().setRealizer(eST, erST);
                 erST.setLineType(LineType.DOTTED_1);
                 erST.setLineColor(Color.lightGray);
                 EdgeLabel el=getEdgeLabel(sp.QualifierName);
                 erST.addLabel(el);
             }
         }
 
         for(ModelPathwayElementsMappingTable.ReacProcTableEntry sp:repository.modelPathwayElementsMappingTable.getReacProcContentTable()){
                 Node nS=SpecTONode.get(sp.ReactionID);
                 Node nT=MoleTONode.get(sp.ProcessID);
                 if(nS!=null && nT!=null){
                     Edge eST=graphViewer.view.getGraph2D().createEdge(nS,nT);
                     GenericEdgeRealizer erST = new GenericEdgeRealizer();
                     graphViewer.view.getGraph2D().setRealizer(eST, erST);
                     erST.setLineType(LineType.DOTTED_1);
                     erST.setLineColor(Color.lightGray);
                     EdgeLabel el=getEdgeLabel(sp.QualifierName);
                     erST.addLabel(el);
                 }
         }
 
         graphViewer.view.fitContent();
 
         lTopr.setY(graphViewer.view.getGraph2D().getBoundingBox().getMinY());
         lBotr.setY(graphViewer.view.getGraph2D().getBoundingBox().getMaxY());//graphViewer.view.getFrame().getMaximizedBounds().getMaxY());
     }
 
     EdgeLabel getEdgeLabel(String lab){
         EdgeLabel el=new EdgeLabel(lab);
         el.setFontSize(25);
         el.setFontStyle(Font.BOLD);
         el.setTextColor(Color.RED);
 //        el.setPosition(EdgeLabel.HEAD);
         el.setPreferredPlacement(LabelLayoutConstants.PLACE_AT_TARGET);
         el.setVisible(false);
         return el;
     }
 
     private void reloadModelWithPathway(String pid) {
         graphViewer.resetView();
 
         drawFinalGraph(pid,"hierarchical"); //organic
         bHasPWMapping=true;
 
 //        graphViewer.view.updateView();
 
     }
     private void reLoadGraph() {
 
         graphViewer.resetView();
         repository = new PathCaseRepository();
         graphViewer.view.updateView();
 
         configuration.put("setOrganism", OrganismTable.ROOTID);
 
         processAppletParameters();
     }
 
      private void createGraphFromRepository_Test(PathCaseViewMode mode) {
 //        nodeToPathCaseId = PathCaseViewGenerator.createGraphFromWholeRepository(repository, graphViewer.view.getGraph2D(), mode);
 //        if(valueOfcompartmentH.equalsIgnoreCase("true"))
 
 //03/18/09 revised for SysBio Model Visualization By Xinjian
 //            nodeToPathCaseId = PathCaseViewGenerator.createGraphFromWholeRepositoryCompartmentH(repository, graphViewer.view.getGraph2D(), mode);
 //         nodeToPathCaseId = PathCaseViewGenerator.createGraphFromSysBioModel(repository, graphViewer.view.getGraph2D(), mode,configuration.get("appletWidth").toString(),configuration.get("appletHeight").toString());
 //         HashMap<Node, HashSet<String>> nodeToPathCaseId;
          Graph2D gra=graphViewer.view.getGraph2D();
 //         Graph2D view = graphViewer.view.getGraph2D();
         HierarchyManager hm = new HierarchyManager(gra);
 
          Node n1=gra.createNode();
          PathCaseShapeNodeRealizer  n1r=new PathCaseShapeNodeRealizer ();
 
          n1r.setLocation(100,100);
          n1r.setSize(100,100);
          n1r.setFillColor(Color.green);
          n1r.setVisible(true);
          gra.setRealizer(n1,n1r);
          HashSet<String> sid=new HashSet<String>();
          sid.add("n1");
 
          Node n2=gra.createNode();
          PathCaseShapeNodeRealizer  n2r=new PathCaseShapeNodeRealizer ();
          n2r.setLocation(300,300);
          n2r.setSize(200,200);
          n2r.setFillColor(Color.green);
          n2r.setVisible(true);
          gra.setRealizer(n2,n2r);
 
          Node n3=gra.createNode();
          PathCaseShapeNodeRealizer  n3r=new PathCaseShapeNodeRealizer ();
          n3r.setLocation(50,50);
          n3r.setSize(20,20);
          n3r.setFillColor(Color.green);
          n3r.setVisible(true);
          gra.setRealizer(n3,n3r);
 
          Rectangle rec=gra.getBoundingBox();
          Node m1=gra.createNode();
          PathCaseShapeNodeRealizer  m1r=new PathCaseShapeNodeRealizer ();
          m1r.setSize(1,1);
          m1r.setFillColor(Color.red);
          m1r.setLocation(5,5);//(rec.getMinX(),rec.getMinY());
          gra.setRealizer(m1,m1r);
          Node m2=gra.createNode();
          PathCaseShapeNodeRealizer  m2r=new PathCaseShapeNodeRealizer ();
          m2r.setSize(1,1);
          m2r.setFillColor(Color.red);
          m2r.setLocation(1745,1535);//(799,599);//(rec.getMaxX(),rec.getMaxY());
          gra.setRealizer(m2,m2r);
 
          Node groupNode = hm.createGroupNode(gra);
          NodeList nodelist1 = new NodeList();
          nodelist1.add(n1);
          nodelist1.add(m1);
          hm.groupSubgraph(nodelist1, groupNode);
 
          Node groupNode2 = hm.createGroupNode(gra);
          NodeList nodelist2 = new NodeList();
          nodelist2.add(n2);
          nodelist2.add(m2);
 //         nodelist2.add(n3);
          hm.groupSubgraph(nodelist2, groupNode2);
 
 
 
 
 //         nodeToPathCaseId.put(n1,sid);
 
 //         else
 //            nodeToPathCaseId = PathCaseViewGenerator.createGraphFromWholeRepository(repository, graphViewer.view.getGraph2D(), mode);
 //        PathCaseIDToNameMap = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId);
     }
 
     private void createGraphFromRepository_Pathway(PathCaseViewMode mode) {
         nodeToPathCaseId_Pathway =PathCaseViewGenerator.createGraphForASinglePathway(repository, graphViewer.view.getGraph2D(), this.pathwayID, false, true, false);
 
         PathCaseIDToNameMap_Pathway = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId_Pathway);
 
     }
 
     private void createGraphFromMQLRepository(PathCaseViewMode mode,String reactionGuids) {
 
         nodeToPathCaseId = PathCaseViewGenerator.createGraphFromMQL(repository, graphViewer.view.getGraph2D(), mode,configuration.get("appletWidth").toString(),configuration.get("appletHeight").toString(),reactionGuids);
 
 //        PathCaseIDToNameMap = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId);
     }
 
     private void createGraphFromRepository(PathCaseViewMode mode,String reactionGuids) {
 
         nodeToPathCaseId = PathCaseViewGenerator.createGraphFromSysBioModelN(repository, graphViewer.view.getGraph2D(), mode,configuration.get("appletWidth").toString(),configuration.get("appletHeight").toString(),reactionGuids);
 
         PathCaseIDToNameMap = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId);
     }
 
     private void createGraphFromRepository(PathCaseViewMode mode,String reactionGuids,boolean showCommon) {
         nodeToPathCaseId = PathCaseViewGenerator.createGraphFromSysBioModelN(repository, graphViewer.view.getGraph2D(), mode,configuration.get("appletWidth").toString(),configuration.get("appletHeight").toString(),reactionGuids);
         PathCaseIDToNameMap = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId);
     }
 
     private void createAnotherGraphFromRepository(PathCaseViewMode mode,String reactionGuids,double dx,double dy,int iModelColor) {
         nodeToPathCaseId2 = PathCaseViewGenerator.createGraphFromSysBioModelN(repository, graphViewer.view.getGraph2D(),mode,configuration.get("appletWidth").toString(),configuration.get("appletHeight").toString(),reactionGuids,dx,dy,iModelColor);
 //        System.out.println("another Test visualization2");
         PathCaseIDToNameMap2 = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId2);
 //        System.out.println("another Test visualization3");
         mergeNodeToID(nodeToPathCaseId,nodeToPathCaseId2);
         mergeIDToName(PathCaseIDToNameMap,PathCaseIDToNameMap2);
     }
 
     private void mergeNodeToID(HashMap<Node, HashSet<String>> nT1,HashMap<Node, HashSet<String>> nT2){
         //merge into nT1
         for(Node n:nT2.keySet()){
             nT1.put(n,nT2.get(n));
         }
     }
 
     private void mergeIDToName(HashMap<String, String> nT1,HashMap<String, String> nT2){
         //merge into nT1
         for(String n:nT2.keySet()){
             nT1.put(n,nT2.get(n));
         }
     }
 
 
 //    private void createGraphFromRepository_Xu(PathCaseViewMode mode) {
 //             nodeToPathCaseId = PathCaseViewGenerator.createGraphFromMetabolite(repository, graphViewer.view.getGraph2D(), mode,"1024","768");
 ////            PathCaseIDToNameMap = TableQueries.getPathCaseIDToNameMap(repository, graphViewer.view, nodeToPathCaseId);
 //        }
 
 
     private void sleepSafe(long time) {
         try {
             Thread.sleep(time);
         }
         catch (InterruptedException e) {
             //e.printStackTrace();
         }
     }
 
     protected void createLoaderDialog(String message) {
         statusBarText.setText(message);
         add(statusBar, BorderLayout.NORTH);
         statusBar.setEnabled(true);
         statusBar.setVisible(true);
         itemsToolBar.setEnabled(false);
         itemsToolBar.setVisible(false);
         //repaint();
     }
 
     public void killLoaderDialog() {
         add(itemsToolBar, BorderLayout.NORTH);
         statusBar.setEnabled(false);
         statusBar.setVisible(false);
         itemsToolBar.setEnabled(true);
         itemsToolBar.setVisible(true);
         //repaint();
     }
 
     public String getRedirectionString(String itemID, String itemType, String query, boolean DisplayGO, boolean organismdependentquery) {
         String defaultvalue = "";
 
         if (applet == null || configuration == null)
             return defaultvalue;
 
         String setOrganism;
         //selected organism fix
         /////////////////////////////////////////////////////TODO: give warning if multiple organism
         HashSet<String> setorganismset = organismPanel.collectselectedleaves();
         //System.out.println(setorganismset.size());
         if (organismdependentquery && !setorganismset.contains(OrganismTable.ROOTID) && (setorganismset.size() > 1 || setorganismset.size() == 0)) {
             JOptionPane.showMessageDialog(this, "You should select exactly one organism to make a query on the graph.", "Warning", JOptionPane.WARNING_MESSAGE);
             return defaultvalue;
         } else if (!setorganismset.contains(OrganismTable.ROOTID)) {
             setOrganism = (String) setorganismset.toArray()[0];
         } else {
             setOrganism = OrganismTable.ROOTID;
         }
 
         //////////////////////////
 
 
         String RedirectionWebPageUrl = (String) configuration.get("RedirectionWebPageUrl");
         //String setOrganism = configuration.get("setOrganism");
 
         String viewID = (String) configuration.get("viewID");
         String terms = (String) configuration.get("terms");
         String type = (String) configuration.get("type");
         String page = (String) configuration.get("page");
 
         String urlextension;
 
         if (!setOrganism.equals(""))
             urlextension = "?viewID=" + viewID + "&query=" + query + "&itemID=" + itemID + "&itemType=" + itemType + "&DisplayGO=" + DisplayGO + "&organismName=" + setOrganism;
         else
             urlextension = "?viewID=" + viewID + "&query=" + query + "&itemID=" + itemID + "&itemType=" + itemType + "&DisplayGO=" + DisplayGO + "&organismName=" + OrganismTable.ROOTID;
 
         if (terms != null && !terms.equals("")) urlextension += "&terms=" + terms;
         if (type != null && !type.equals("")) urlextension += "&type=" + type;
         if (page != null && !page.equals("")) urlextension += "&page=" + page;
 
         String wholeredirectionString = defaultvalue;
         if ((RedirectionWebPageUrl != null)) {
             RedirectionWebPageUrl = RedirectionWebPageUrl.replace('\\', '/');
             wholeredirectionString = RedirectionWebPageUrl + urlextension;
         } else {
             System.out.println("REDIRECTION URL IS NULL");
             return defaultvalue;
         }
 
         return wholeredirectionString;
 
     }
 
     private void PageRedirect(String itemID, String itemType, String query, boolean DisplayGO, boolean organismdependentquery) {
 
         if (applet == null || configuration == null)
             return;
 
 
         String wholeredirectionString = getRedirectionString(itemID, itemType, query, DisplayGO, organismdependentquery);
         Boolean rightClickQueryingEnabled = (Boolean) configuration.get("rightClickQueryingEnabled");
 
         try {
             final URL redirectURL = new URL(wholeredirectionString);
 
             // if (checkAppletContext()) {
             if (rightClickQueryingEnabled) {
 
                 pool.submit(new Thread() {
                     public void run() {
 
                         while (true) {
                             System.out.println("Trying to redirect to: " + redirectURL);
 
                             if (applet != null && applet.getAppletContext() != null) {
                                 applet.getAppletContext().showDocument(redirectURL);
                                 System.out.println("Redirection is done");
                                 break;
                             }
 
                             try {
                                 Thread.sleep(2000);
                             } catch (InterruptedException e) {
                                 //e.printStackTrace();
                             }
                         }
 
                     }
                 });
             } else {
                 applet.getAppletContext().showDocument(redirectURL, "_blank");
             }
             //}
 
         }
         catch (MalformedURLException murle) {
             System.out.println("Could not recognise redirect URL");
         }
     }
 
     //////////////////////////////////  Access from the graph viewer /////////////////////////////
 
 
         public JPopupMenu getMappingPWsMenu() {
             JPopupMenu menu = new JPopupMenu();
     
             if(repository.mappingPathwayTable.getAllPathways().size()==0){
                 if(!bFrozenLoaded){
                     JMenuItem item1 = new JMenuItem("No corresponding pathway exists.");
                     item1.setEnabled(false);
 
                     item1.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
             //                graphMode.showcommonmoleculesingraph = item1.getState();
                             graphViewer.view.updateView();
                         }
                     });
                     menu.add(item1);
                 }else{
 //                    JMenuItem item1 = new JMenuItem("Load from saved layout...");
 //                    item1.setEnabled(false);
 //
 //                    item1.addActionListener(new ActionListener() {
 //                        public void actionPerformed(ActionEvent evt) {
 //            //                graphMode.showcommonmoleculesingraph = item1.getState();
 //                            graphViewer.view.updateView();
 //                        }
 //                    });
 //                    menu.add(item1);
 
                     String graphXML = serviceM.getPathwaysServiceSoap().getGraphData("", "", "","", "",(String) configuration.get("modelID"), "", "");//.getSBModelByID(modelID,"testModelId");//
 
                     SysBioXMLParser parser = new SysBioXMLParser(repository);
 
                     parser.loadRepositoryFromGraphXML(graphXML);
 
                    if(!repository.mappingPathwayTable.getAllPathways().isEmpty()){
                        for(String pid:repository.mappingPathwayTable.getAllPathways()){
                             JMenuItem item = new JMenuItem(repository.mappingPathwayTable.getNameById(pid));
                             final String pwid=pid;
                             item.addActionListener(new ActionListener() {
                                 public void actionPerformed(ActionEvent evt) {
                                     reloadModelWithPathway(pwid);
                                 }
                             });
                             menu.add(item);
                        }
                    }else{
                         JMenuItem item1 = new JMenuItem("No corresponding pathway exists.");
                     item1.setEnabled(false);
 
                     item1.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
             //                graphMode.showcommonmoleculesingraph = item1.getState();
                             graphViewer.view.updateView();
                         }
                     });
                     menu.add(item1);
                    }
                 }
             }else{
                 for(String pid:repository.mappingPathwayTable.getAllPathways()){
                     JMenuItem item = new JMenuItem(repository.mappingPathwayTable.getNameById(pid));
                     final String pwid=pid;
                     item.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             reloadModelWithPathway(pwid);
                         }
                     });
                     menu.add(item);
                 }
             }
 
             return menu;
         }
 
 
     public JPopupMenu getModesMenu() {
         JPopupMenu menu = new JPopupMenu();
 
         final JCheckBoxMenuItem item1 = new JCheckBoxMenuItem("Display Common Species", graphMode.showcommonmoleculesingraph);
         item1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 graphMode.showcommonmoleculesingraph = item1.getState();
 //                repository2=null;
                 if (!bFrozenLoaded) reloadFromRepository(true);
                 else{
                     //the graph was loaded from frozen layout, we need to create repository firstly...
                     
                 }
             }
         });
         menu.add(item1);
 
         return menu;
     }
 
     public JMenu getPathCasePaperPopupQueries() {
         JMenu menu = new JMenu("PathCase Queries");
 
         Boolean loadFromBioPAX = (Boolean) configuration.get("loadFromBioPAX");
         Boolean rightClickQueryingEnabled = (Boolean) configuration.get("rightClickQueryingEnabled");
         if (loadFromBioPAX || !rightClickQueryingEnabled)
             return null;
 
         if (configuration != null) {
             String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
             HashSet<String> expandedPathwayGuidsForDisplay = GUIDParser(expandedPathwayGuids);
 
             if (expandedPathwayGuidsForDisplay != null && expandedPathwayGuidsForDisplay.size() > 0)
             {
                 for (final String expandedId : expandedPathwayGuidsForDisplay) {
                     String pathwayName = TableQueries.getPathwayNameById(repository, expandedId);
                     JMenuItem item = new JMenuItem("Collapse pathway " + pathwayName);
                     item.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             collapsePathwayByID(expandedId);
                         }
                     });
                     menu.add(item);
                 }
             }else
             return null;
         }
 
         return menu;
     }
 
     public JMenu getPathCaseNodePopupQueries(Node v) {
         JMenuItem item;
         JMenu menu = new JMenu("PathCase Queries");
 
 
         Boolean loadFromBioPAX = (Boolean) configuration.get("loadFromBioPAX");
         Boolean rightClickQueryingEnabled = (Boolean) configuration.get("rightClickQueryingEnabled");
         if (loadFromBioPAX || !rightClickQueryingEnabled)
             return null;
 
         PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(v);
         if (nr.getNodeMode().equals(PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT)) {
             return null;
         }
 
         boolean singlePathwayInGraph = false;
 
         if (configuration != null) {
             String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
             HashSet<String> expandedPathwayGuidsForDisplay = GUIDParser(expandedPathwayGuids);
             if (expandedPathwayGuidsForDisplay != null && expandedPathwayGuidsForDisplay.size() == 1)
                 singlePathwayInGraph = true;
         }
 
         //TODO: handle only single name/id
         final HashSet<String> dbids = nodeToPathCaseId.get(v);
 
         if (dbids == null) {
             System.out.println("nodeToPathCaseId is null");
         } else for (Iterator it = dbids.iterator(); it.hasNext();) {
             final String dbid = (String) it.next();
 
             String nodename = PathCaseIDToNameMap.get(dbid);
             if (nodename == null) nodename = nr.getLabelText();
 
 
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON || nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR) || nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN) || nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT) || nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR) || nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR) || nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR)) {
 
                 item = new JMenuItem("Details of " + nodename);
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         PageRedirect(dbid, "MolecularEntity", "000", false, false);
                     }
                 });
 
                 menu.add(item);
 
                 item = new JMenuItem("Pathways/Processes involving " + nodename + "  with a specific use");
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         PageRedirect(dbid, "MolecularEntity", "009", false, true);
                     }
                 });
                 menu.add(item);
 
 
                 if (singlePathwayInGraph) {
                     menu.add(item);
                     item = new JMenuItem("Processes within a given number of steps from " + nodename + " in this pathway");
                     item.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             PageRedirect(dbid, "MolecularEntity", "004", false, true);
                         }
                     });
                     menu.add(item);
 
                     item = new JMenuItem("Find paths between " + nodename + " and another molecule in this pathway");
                     item.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             PageRedirect(dbid, "MolecularEntity", "011", false, true);
                         }
                     });
                     menu.add(item);
                 }
 
             } else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                 item = new JMenuItem("Details of " + nodename);
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         PageRedirect(dbid, "Process", "000", false, false);
                     }
                 });
                 menu.add(item);
 
                 if (singlePathwayInGraph) {
                     item = new JMenuItem("Processes within a given number of steps from " + nodename + " in this pathway");
                     item.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             PageRedirect(dbid, "Process", "003", false, true);
                         }
                     });
                     menu.add(item);
                 }
 
                 item = new JMenuItem("Processes within a given number of steps from " + nodename + " in the metabolic network");
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         PageRedirect(dbid, "Process", "012", false, true);
                     }
                 });
                 menu.add(item);
 
                 if (singlePathwayInGraph) {
                     item = new JMenuItem("Processes sharing activators and inhibitors with " + nodename + " in this pathway ");
                     item.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             PageRedirect(dbid, "Process", "005", false, true);
                         }
                     });
                     menu.add(item);
                 }
 
                 item = new JMenuItem("Show genes for process " + nodename);
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         if (!geneViewerButton.isSelected()) {
                             geneViewerButton.setSelected(true);
                             geneViewerDisplayAction();
                         }
                         String dbid_conv = TableQueries.getGenericProcessEntityIdbyGenericProcessID(repository, dbid);
                         geneViewerPanel.highlightGenesForGenericProcess(dbid_conv);
                     }
                 });
                 menu.add(item);
 
             } else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
 
                 item = new JMenuItem("Details of " + nodename);
 
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         PageRedirect(dbid, "Pathway", "000", false, false);
                     }
                 });
                 menu.add(item);
                 item = new JMenuItem("Pathways within a given number of steps from " + nodename);
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         PageRedirect(dbid, "Pathway", "002", false, true);
                     }
                 });
                 menu.add(item);
 
 
                 item = new JMenuItem("Expand " + nodename + " in this graph");
                 item.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         expandPathwayByID(dbid);
                     }
                 });
                 menu.add(item);
             }
 
             if (it.hasNext())
                 menu.addSeparator();
         }
         return menu;
     }
     public PathCaseShapeNodeRealizer.PathCaseNodeRole getNodeRole(Node v) {
         PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(v);
         return nr.getNodeRole();
     }
     public String getNodeTipText(Node v) {
         if (bFrozenLoaded){
             if (posToIDandName == null) return "Parameter Specified";//"posToIDandName is null";
             else
                 return PathCaseIDToTooltips.get(posToIDandName.get(String.valueOf(v.index())).split("@@@")[0]); //"same node tip text";//
         }
         if (PathCaseIDToNameMap == null) return "Parameter Specified";// "PathCaseIDToNameMap is null";
 //Qi collapse begin
 //        PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(v);
 //Qi collapse end
         PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(v);
         String fullname = "";
 
         HashSet<String> gpipset = nodeToPathCaseId.get(v);
         HashSet<String> gpipset_pw=null;
         if(gpipset==null) gpipset_pw=nodeToPathCaseId_Pathway.get(v);
         int i = 0;
         if (gpipset != null)
         {
             //this branch is for system biology  
             for (String gpid : gpipset) {
                 String name = PathCaseIDToNameMap.get(gpid);
                 
                 if (name == null || name.equals("")|| name.equalsIgnoreCase("Unknown")) {name = "Parameter Specified"; return name;}
 
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
 //                    name = "<b>METABOLITE: </b>" + name;
                         name = "<b>Species:</b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
 //                    name = "<b>ENZYME: </b><i>" + name + "</i>";
                     String strReaction= "<b>Reaction: </b>";
                     if(nr.getFillColor()==Color.ORANGE)
                         strReaction= "<b>Reversible Reaction: </b>";
 
                     if(name.contains(":math:"))
 //                        name = "<b>Reaction: </b><i>" + name.split(":math:")[0] + "</i><br><b>Kinetic Law: </b>"+ name.split(":math:")[1];
                     name = strReaction + name.split(":math:")[0];// + "<br><b>Kinetic Law: </b>"+ name.split(":math:")[1];
                     else
                         name =strReaction + name;
 //                        name = "<b>Reaction: </b><i>" + name + "</i>";
 
                     if (gpid != null) {
                         HashSet<String> cofactorNames = TableQueries.getCofactorTypedNamesByGenericProcessID(gpid, repository);
                         HashSet<String> regulatorNames = TableQueries.getRegulatorTypedNamesByGenericProcessID(gpid, repository);
 
                         if (cofactorNames != null && cofactorNames.size() > 0) {
                             name += "<br><b>COFACTORS:</b><font size=-1 color=\"red\">";
                             for (String mname : cofactorNames) {
                                 name += " " + mname;
                             }
                             name += "</font>";
                         }
 
                         if (regulatorNames != null && regulatorNames.size() > 0) {
                             name += "<br><b>REGULATORS:</b><font size=-1 color=\"blue\">";
                             for (String mname : regulatorNames) {
                                 name += " " + mname;
                             }
                             name += "</font>";
                         }
 
                     }
                 } else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)
                     name = "<b>PATHWAY: </b>" + name;
                 else
                 {
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR) name = "COFACTOR: " + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN)
                     name = "<b>COFACTOR IN: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                     name = "<b>COFACTOR OUT: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR)
                     name = "<b>REGULATOR: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR)
                     name = "<b>INHIBITOR: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR)
                     name = "<b>ACTIVATOR: </b>" + name;
                 }
 
                 if (i < gpipset.size() - 1) {
                     fullname += name + "<br>";
                 }
                 else{
                    fullname += name;
                 }
                 i++;
             }
 
         }else if (gpipset_pw != null){            
             for (String gpid : gpipset_pw) {
                 String name = PathCaseIDToNameMap_Pathway.get(gpid);
                 if (name == null || name.equals("") || name.equals("NULL")){ name = "Parameter Specified";fullname=name;break;}
 
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
                     name = "<b>METABOLITE: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                     name = "<b>ENZYME: </b><i>" + name + "</i>";
 //                    name = "<b>ENZYME: </b>" + name + "";
 
                     if (gpid != null) {
                         HashSet<String> cofactorNames = TableQueries.getCofactorTypedNamesByGenericProcessID(gpid, repository);
                         HashSet<String> regulatorNames = TableQueries.getRegulatorTypedNamesByGenericProcessID(gpid, repository);
 
                         if (cofactorNames != null && cofactorNames.size() > 0) {
                             name += "<br><b>COFACTORS:</b><font size=-1 color=\"red\">";
                             for (String mname : cofactorNames) {
                                 name += " " + mname;
                             }
                             name += "</font>";
                         }
                         if (regulatorNames != null && regulatorNames.size() > 0) {
                             name += "<br><b>REGULATORS:</b><font size=-1 color=\"blue\">";
                             for (String mname : regulatorNames) {
                                 name += " " + mname;
                             }
                             name += "</font>";
                         }
                     }
                 } else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)
                     name = "<b>PATHWAY: </b>" + name;
                 else
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR) name = "COFACTOR: " + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN)
                     name = "<b>COFACTOR IN: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                     name = "<b>COFACTOR OUT: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR)
                     name = "<b>REGULATOR: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR)
                     name = "<b>INHIBITOR: </b>" + name;
                 else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR)
                     name = "<b>ACTIVATOR: </b>" + name;
 
                 if (i < gpipset_pw.size() - 1) fullname += name + "<br>";
                 else fullname += name;
                 i++;
             }           
         }else return "not identified.";
 //        System.out.println(   "<html>" + fullname + "</html>");
 //        return   "<html>" + fullname + "</html>";
         return  "<html><body>" + fullname + "</body></html>";
     }
 
     ///////////////////////////////    EXPAND COLLAPSE PATHWAY /////////////////////////////////////
 
     private void collapsePathwayByID(String dbid) {
 
         if (configuration == null)
             return;
 
         String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
         HashSet<String> expandedPathwayGuidsForDisplay = GUIDParser(expandedPathwayGuids);
 
         String collapsedPathwayGuids = (String) configuration.get("collapsedPathwayGuids");
         HashSet<String> collapsedPathwayGuidsForDisplay = GUIDParser(collapsedPathwayGuids);
 
         //String genericProcessGuids = (String) configuration.get("genericProcessGuids");
         //String moleculeGuids = (String) configuration.get("moleculeGuids");
 
         //HashSet<String> moleculeGuidsForDisplay = GUIDParser(moleculeGuids);
         //HashSet<String> genericProcessGuidsForDisplay = GUIDParser(genericProcessGuids);
 
         //comment to be removed if safe expand/collapse is required
         /*if (*/
         expandedPathwayGuidsForDisplay.remove(dbid);/*)*/
         {
             collapsedPathwayGuidsForDisplay.add(dbid);
 
             configuration.put("expandedPathwayGuids", GUIDSetToString(expandedPathwayGuidsForDisplay));
             configuration.put("collapsedPathwayGuids", GUIDSetToString(collapsedPathwayGuidsForDisplay));
 
             //System.out.println(dbid);
 
             reLoadGraph();
             //loadOrganismHierarchyFromRepository(OrganismTable.ROOTID);
             callCollapseJS(dbid);
         }
     }
 
     private void expandPathwayByID(String dbid) {
 
         String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
         HashSet<String> expandedPathwayGuidsForDisplay = GUIDParser(expandedPathwayGuids);
 
         String collapsedPathwayGuids = (String) configuration.get("collapsedPathwayGuids");
         HashSet<String> collapsedPathwayGuidsForDisplay = GUIDParser(collapsedPathwayGuids);
 
         //comment to be removed if safe expand/collapse is required
         /*if (*/
         collapsedPathwayGuidsForDisplay.remove(dbid);/*)*/
         {
             expandedPathwayGuidsForDisplay.add(dbid);
 
             configuration.put("expandedPathwayGuids", GUIDSetToString(expandedPathwayGuidsForDisplay));
             configuration.put("collapsedPathwayGuids", GUIDSetToString(collapsedPathwayGuidsForDisplay));
 
             reLoadGraph();
             callExpandJS(dbid);
         }
     }
 
     private void callExpandJS(String collapsedPathwayGuid) {
 
         //TODO:  callExpandJS
         /*
         try {
             JSObject.getWindow(this.redirectorApplet).eval("AjaxFunctions.UpdateSessionExpandPathway('" + collapsedPathwayGuid + "')");
         }
         catch (Exception me) {
             System.out.println("Expand pathway javascript function failed");
         }
         */
     }
 
     private void callCollapseJS(String expandedPathwayGuid) {
         //TODO: callCollapseJS
         /*
         try {
             JSObject.getWindow(this.redirectorApplet).eval("AjaxFunctions.UpdateSessionCollapsePathway('" + expandedPathwayGuid + "')");
         }
         catch (Exception me) {
             System.out.println("Collapse pathway javascript function failed");
         }
         */
     }
 
     ///////////////////////////////    Organism and Gene Viewer Related  /////////////////////////////////////
 
     public void resetOrganismHierarchyInBrowser(String setOrganism) {
         OrganismCheckNode rootOrganism = new OrganismCheckNode("Unspecified", OrganismTable.ROOTID);
 
         HashSet<String> collapsedpathwayitemidsingraph = new HashSet<String>();
         HashSet<String> genericprocessitemidsingraph = new HashSet<String>();
 
 
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             if (nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)) {
                 collapsedpathwayitemidsingraph.add(nodeToPathCaseId.get(node).iterator().next());
             } else if (nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS)) {
                 genericprocessitemidsingraph.add(nodeToPathCaseId.get(node).iterator().next());
             }
         }
 
         HashSet<String> orgidsingraph = new HashSet<String>();
         orgidsingraph.addAll(TableQueries.getOrganismIdsInAListOfGenericProcessEntities(repository, genericprocessitemidsingraph));
         orgidsingraph.addAll(TableQueries.getOrganismIdsInAListOfCollapsedPathwayEntities(repository, collapsedpathwayitemidsingraph));
 
         HashSet<String> usefulorgidsinhierarchy = TableQueries.getUsefulIdsInOrganismHierarchy(repository, orgidsingraph);
         PathCaseViewGenerator.createOrganismHierarchyFromRepository(repository, rootOrganism, usefulorgidsinhierarchy);
 
         organismPanel.initwithRoot(rootOrganism);
         organismPanel.root.setSelected(true, true);
         organismPanel.selectGivenNode(setOrganism);
     }
 
     public void loadGenomeDataForOrganism(String selectedOrganismId) {
         if (configuration != null) {
             String WebServiceURL = (String) configuration.get("WebServiceUrl");
             loadGenomeDataForOrganism(WebServiceURL, selectedOrganismId);
         }
     }
 
     public void loadGenomeDataForOrganism(String WebServiceURL, String selectedOrganismId) {
         if(((String)configuration.get("graphContent")).equalsIgnoreCase("true")){
                 if (serviceM == null)
                 connectToServiceCompartmentH(WebServiceURL);
 
             if (serviceM == null)
                 return;
 
             if (repository == null)
                 return;
 
             createLoaderDialog("Loading Genome Data From PathCase Web Service");
 
             PathCaseXMLParser parser = new PathCaseXMLParser(repository);
             //parse expanded pathway id from here...
 
             String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
             HashSet<String> expandedPathwayGuidsForDisplay = GUIDParser(expandedPathwayGuids);
             String pathwaylistlinked = GUIDSetToString(expandedPathwayGuidsForDisplay);
 
 
             if (expandedPathwayGuidsForDisplay != null) {
                 String genomeXML = service.getPathwaysServiceSoap().getGeneMappingForOrganismPathways(pathwaylistlinked, selectedOrganismId);
 
                 parser.loadRepositoryFromGeneXML(genomeXML);
             }
         }else{
         if (service == null)
             connectToService(WebServiceURL);
 
         if (service == null)
             return;
 
         if (repository == null)
             return;
 
         createLoaderDialog("Loading Genome Data From PathCase Web Service");
 
         PathCaseXMLParser parser = new PathCaseXMLParser(repository);
         //parse expanded pathway id from here...
 
         String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
         HashSet<String> expandedPathwayGuidsForDisplay = GUIDParser(expandedPathwayGuids);
 
         if (expandedPathwayGuidsForDisplay != null && expandedPathwayGuidsForDisplay.size() == 1) {
             String genomeXML = service.getPathwaysServiceSoap().getGeneMappingForOrganismPathway(expandedPathwayGuidsForDisplay.iterator().next(), selectedOrganismId);
             parser.loadRepositoryFromGeneXML(genomeXML);
         }
 
         }
       /*  String pathwaylistlinked = GUIDSetToString(expandedPathwayGuidsForDisplay);
          if (expandedPathwayGuidsForDisplay != null) {
             String genomeXML = service.getPathwaysServiceSoap().getGeneMappingForOrganismPathways(pathwaylistlinked, selectedOrganismId);
 
             parser.loadRepositoryFromGeneXML(genomeXML);
         }*///test by Xinjian
 
         killLoaderDialog();
         //
     }
 
     public void organismSelected(HashSet<String> selectedOrganismIdList) {
 
         graphColorReset();
 
         if (selectedOrganismIdList == null) return;
 
         //grayout generic processes
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
 
             HashSet<String> idset = nodeToPathCaseId.get(node);
             if (idset==null) continue;
             String dbid = idset.iterator().next();
             HashSet<String> orgidsinlist = new HashSet<String>();
 
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             if (nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)) {
                 HashSet<String> orgidsinrepositorybycollapsedpathways = TableQueries.getOrganismGroupIDListByCollapsedPathwayId(repository, dbid);
                 if (orgidsinrepositorybycollapsedpathways != null)
                     orgidsinlist.addAll(orgidsinrepositorybycollapsedpathways);
                 else
                     orgidsinlist.add(OrganismTable.UNKNOWNID);
             } else if (nr.getNodeRole().equals(PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS)) {
                 HashSet<String> orgidsinrepositorybygenericprocesses = TableQueries.getOrganismGroupIDListByGenericProcessId(repository, dbid);
                 if (orgidsinrepositorybygenericprocesses != null)
                     orgidsinlist.addAll(orgidsinrepositorybygenericprocesses);
                 else
                     orgidsinlist.add(OrganismTable.UNKNOWNID);
             }
 
             boolean containsany = false;
 
             if (selectedOrganismIdList.contains(OrganismTable.ROOTID))
                 containsany = true;
             else
                 for (String orgid : orgidsinlist) {
                     if (selectedOrganismIdList.contains(orgid)) {
                         containsany = true;
                         break;
                     }
                     //System.out.print(orgid+" ");
                 }
 
             //System.out.println();
 
             if (!containsany) {
 
 
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                     PathCaseViewGenerator.makeGenericProcessShapeNodeRealizerGrayedOut(nr);
                     nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT);
                 } else if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                     PathCaseViewGenerator.makeCollapsedPathwayShapeNodeRealizerGrayedOut(nr);
                     nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT);
                 }
                 /*else if (nr.getNodeRole()==PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT)
                     PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerGrayedOut(nr);*/
 
             }
         }
 
         //grayout substrates and products
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
 
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR) {
                 EdgeCursor edgeiter = node.edges();
                 int countgrayedout = 0;
                 //int alledgecount = edgeiter.size();
                 int alledgecount = 0;
 
                 for (int i = 0; i < edgeiter.size(); i++) {
                     Edge edge = edgeiter.edge();
                     Node otherend = edge.opposite(node);
                     PathCaseShapeNodeRealizer nrother = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(otherend);
 
                     if ((nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) && nrother.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT) {
                         countgrayedout++;
                     }
 
                     if ((nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)) {
                         alledgecount++;
                     }
                     edgeiter.next();
                 }
                 if (countgrayedout == alledgecount && alledgecount > 0) {
                     nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT);
                     if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
                         PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerGrayedOut(nr);
                     else
                     if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                         PathCaseViewGenerator.makeCofactorShapeNodeRealizerGrayedOut(nr);
                     else
                     if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR)
                         PathCaseViewGenerator.makeRegulatorShapeNodeRealizerGrayedOut(nr);
                 }
                 //System.out.println(countgrayedout+" "+alledgecount);
             }
         }
 
         //grayout edges
         for (Edge edge : graphViewer.view.getGraph2D().getEdgeArray()) {
             Node from = edge.source();
             Node to = edge.target();
 
             PathCaseShapeNodeRealizer nrfrom = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(from);
             PathCaseShapeNodeRealizer nrto = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(to);
             EdgeRealizer ne = graphViewer.view.getGraph2D().getRealizer(edge);
 
             if ((nrfrom.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT || nrto.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT) || (nrfrom.getNodeRole() == nrto.getNodeRole() && (nrfrom.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT || nrto.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT))) {
                 PathCaseViewGenerator.makeEdgeGrayedOut(ne);
             }
 
         }
 
         // repository.genericProcessToSpecificProcessListTable.
         /*for (String organismId : selectedOrganismIdList) {
             System.out.print(organismId + " ");
         }
         System.out.println();*/
 
         loadGeneViewerOrganismListFromOrgIds(selectedOrganismIdList);
 
         graphViewer.view.updateView();
         overviewPane.updateUI();
     }
 
     private void makeGrayingOutAfterNodeRoleSetup() {
 //grayout substrates and products
          for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
 
              PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
              if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR) {
                  EdgeCursor edgeiter = node.edges();
                  int countgrayedout = 0;
                  //int alledgecount = edgeiter.size();
                  int alledgecount = 0;
 
                  for (int i = 0; i < edgeiter.size(); i++) {
                      Edge edge = edgeiter.edge();
                      Node otherend = edge.opposite(node);
                      PathCaseShapeNodeRealizer nrother = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(otherend);
 
                      if ((nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) && nrother.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT) {
                          countgrayedout++;
                      }
 
                      if ((nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS || nrother.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY)) {
                          alledgecount++;
                      }
                      edgeiter.next();
                  }
                  if (countgrayedout == alledgecount && alledgecount > 0) {
                      nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT);
                      if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON)
                          PathCaseViewGenerator.makeSubstrateProductShapeNodeRealizerGrayedOut(nr);
                      else
                      if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT)
                          PathCaseViewGenerator.makeCofactorShapeNodeRealizerGrayedOut(nr);
                      else
                      if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR || nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR)
                          PathCaseViewGenerator.makeRegulatorShapeNodeRealizerGrayedOut(nr);
                  }
                  //System.out.println(countgrayedout+" "+alledgecount);
              }
          }
 
          //grayout edges
          for (Edge edge : graphViewer.view.getGraph2D().getEdgeArray()) {
              Node from = edge.source();
              Node to = edge.target();
 
              PathCaseShapeNodeRealizer nrfrom = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(from);
              PathCaseShapeNodeRealizer nrto = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(to);
              EdgeRealizer ne = graphViewer.view.getGraph2D().getRealizer(edge);
 
              if ((nrfrom.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT || nrto.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT) || (nrfrom.getNodeRole() == nrto.getNodeRole() && (nrfrom.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT || nrto.getNodeMode() == PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT))) {
                  PathCaseViewGenerator.makeEdgeGrayedOut(ne);
              }
          }
 
      }
 
 
     public void geneSelectProcessesandCollapsedPathways(HashSet<GenomeTable.GeneEntry> selectedGenes) {
 
 
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
             String dbid = nodeToPathCaseId.get(node).iterator().next();
             Color customFillColor = null;
 
             if (dbid != null && DBIDToCustomFillColor != null)
                 customFillColor = DBIDToCustomFillColor.get(dbid);
 
 
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             if (nr.getNodeMode() != PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT) {
                 //nr.setNodeMode(PathCaseShapeNodeRealizer.PathCaseNodeMode.NORMAL);
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS)
                     PathCaseViewGenerator.makeGenericProcessShapeNodeRealizerNormal(nr);
             }
 
             if (customFillColor != null)
                 PathCaseViewGenerator.fillNodeRealizerCustom(nr, customFillColor);
         }
 
         if (selectedGenes == null) {
             graphViewer.view.updateView();
             return;
         }
 
         //System.out.println(selectedGenes.size());
 
         HashSet<String> gpidsofgenes = new HashSet<String>();
         for (GenomeTable.GeneEntry gene : selectedGenes) {
             gpidsofgenes.add(gene.genericprocessId);
         }
 
         boolean centered = false;
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             if (nr.getNodeMode() != PathCaseShapeNodeRealizer.PathCaseNodeMode.GRAYED_OUT) {
 
                 String nodeid = nodeToPathCaseId.get(node).iterator().next();
                 //System.out.println(nodeid);
                 String dbid = TableQueries.getGenericProcessEntityIdbyGenericProcessID(repository, nodeid);
 
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS && gpidsofgenes.contains(dbid)) {
                     //System.out.println("highlighted");
                     PathCaseViewGenerator.makeGenericProcessShapeNodeRealizerGeneSelected(nr);
                     if (!centered) {
                         graphViewer.view.setCenter(graphViewer.view.getGraph2D().getCenterX(node), graphViewer.view.getGraph2D().getCenterY(node));
                         centered = true;
                     }
                 }
             }
         }
 
         graphViewer.view.updateView();
     }
 
     public void loadGeneViewerOrganismListFromOrgIds(HashSet<String> orgidlist) {
         if (orgidlist == null) return;
 
         HashSet<String> neworglist = new HashSet<String>(orgidlist);
 
         if (organismIDsWGeneData != null)
             neworglist.retainAll(organismIDsWGeneData);
 
         HashMap<String, String> OrganismSimplifiedNamesToId = TableQueries.getOrganismSimplifiedNamesToId(repository, neworglist);
         if (OrganismSimplifiedNamesToId == null) return;
         geneViewerPanel.updateOrganismNameToIdMap(OrganismSimplifiedNamesToId);
     }
 
 
     public void  doInitialFrozenLayout_Pathway() {
 //        Graph2D pwLayout=getAutoGeneratedLayoutInfo_Pathway(pathwayID, "organic");
         Graph2D tempgraph = new Graph2D();
         String layoutType="organic";
         HashMap<Node, HashSet<String>> tempNodeToPathCaseId = PathCaseViewGenerator.createGraphForASinglePathway(repository, tempgraph, pathwayID, false, true, false);
         (new PathCaseLayouter(layoutType)).start(tempgraph);
 
         
     }
 
     private Graph2D getAutoGeneratedLayoutInfo_Pathway(String pathwayId, String layoutType) {
         Graph2D tempgraph = new Graph2D();
         HashMap<Node, HashSet<String>> tempNodeToPathCaseId = PathCaseViewGenerator.createGraphForASinglePathway(repository, tempgraph, pathwayId, false, true, false);
         (new PathCaseLayouter(layoutType)).start(tempgraph);
 
          NodeList nodelist = new NodeList();
          for(Node n:tempgraph.getNodeArray()){
             nodelist.add(n);
          }
 
          HierarchyManager thm = new HierarchyManager(tempgraph);
          Node groupNode = thm.createGroupNode(tempgraph);
 //                 tempgraph.setLocation(groupNode,new YPoint(0,0));
 //                 tempgraph.setSize(groupNode,1024,768);
 //         thm.groupSubgraph(nodelist, groupNode);
 //         Node[] pathwayNArray= tempgraph.getNodeArray();
 //         Edge[] pathwayEArray=tempgraph.getEdgeArray();
 //         tempgraph.edges().edge().
         return tempgraph;
     }
 
     ///////////////////////////////////////// YUAN's PART LAYOUT:    ///////////////////////////////////////////////
 
     /**layout: To choose freezed layout scheme
      * The graph being visualized doesn't have the information about pathways being invovled,
      * so currently, we can only use the LayoutInfo objects to get that.
      */
 
     public void doInitialFrozenLayout() {
         doInitialFrozenLayout("hierarchical");
     }
     public void doInitialFrozenLayout(String strLayout) {
         //TODO: only dealing with expanded Pathways now
 //        int np = numberOfPathways();
         int np=1;
 //        if(np <1){
 //            initialLayout();
 //            PathCaseLayouter.SALabeling().doLayout(graphViewer.view.getGraph2D());
 //            graphViewer.view.fitContent();
 //            return;
 //        }
 
         System.out.println("doing frozen layout");
         LayoutInfo[] layouts = new LayoutInfo[np];
 //        String expandedPathwayGuids =pathwayID;// (String) configuration.get("expandedPathwayGuids");
         for (int i = 0; i < np; i++) {
             String layout = serviceM.getPathwaysServiceSoap().retrieveLayout("", pathwayID, "", "");
 //            layouts[i] = new LayoutParser().getParsedXMLInfo(layout);            
 
             if (layout.length() < 10) {//since layout is in xml format, the length should be larger than 10
                 layouts[i]= getAutoGeneratedLayoutInfo(pathwayID,strLayout);//"hierarchical");// "organic");
             }else
             {
                 try {
                 layouts[i] = new LayoutParser().getParsedXMLInfo(layout);
             } catch (Exception e) {
                 System.out.println(e);               
             }
             }
         }
         applyFrozenLayout(layouts);
     }
 
      private LayoutInfo getAutoGeneratedLayoutInfo(String pathwayId, String layoutType) {
         Graph2D tempgraph = new Graph2D();
         HashMap<Node, HashSet<String>> tempNodeToPathCaseId = PathCaseViewGenerator.createGraphForASinglePathway(repository, tempgraph, pathwayId, false, true, false);
         (new PathCaseLayouter(layoutType)).start(tempgraph);
 
         return getLayoutInfo(tempgraph, tempNodeToPathCaseId);//,pathwayNArray,pathwayEArray);
     }
     //get how many expanded pathways in the graph, return -1 if there're nodes not belong to the pathways
     private int numberOfPathways() {
         String collapsedPathwayGuids = (String) configuration.get("collapsedPathwayGuids");
         String expandedPathwayGuids = (String) configuration.get("expandedPathwayGuids");
         String genericProcessGuids = (String) configuration.get("genericProcessGuids");
         String moleculeGuids = (String) configuration.get("moleculeGuids");
         if (GUIDParser(collapsedPathwayGuids).size() == 0 && GUIDParser(genericProcessGuids).size() == 0 && GUIDParser(moleculeGuids).size() == 0) {
             return GUIDParser(expandedPathwayGuids).size();
         } else {
             return -1;
         }
     }
 
     private LayoutInfo getLayoutInfo(Graph2D view, HashMap<Node, HashSet<String>> tmpnodeToPathCaseId){//, Node[] pathwayNArray, Edge[] pathwayEArray) {
             HashMap<NodeRealizer, String> nodeTORelatedProcessGUID = new HashMap<NodeRealizer, String>(); //filled during the first part, traversing nodes
             String layout = "";
             //Graph2D view = graphViewer.view.getGraph2D();
             /**
              * Own Designed Compact Layout String Structure:
              * First part is for nodes, then an empty line followed by the part for edges:
              * {[ProcessGUID | null][comma][GUID][comma][cofactor| ][comma][centerX][comma][centerY]\n}*
              * \n
              * {[SourceNodeProcessGUID | null][comma][SourceNodeGUID][comma][TargetNodeProcessGUID | null][comma][TargetNodeGUID][comma]{[bendX][comma][bendY][comma]}* \n }*
              * cofactor,inhibitor,activator or regulator as c,i,a,r repecitvely in [cofactor] field and a space if none of them
              */
 
 //               Node[] pathwayNodes=(Node[])tmpnodeToPathCaseId.keySet().toArray();
             //TODO: generate the LayoutInfo directly or first to XML format
             for (Node node : view.getNodeArray()) {//pathwayNArray){ // view.getNodeArray()) {
 
 //                PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
                 PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) view.getRealizer(node);
                 //skip pathway nodes
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                     continue;
                 }
                 //skip box nodes
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.PATHWAY_BOX) {
                     continue;
                 }
                 //skip tissue group node
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.TISSUEGROUP){
                     continue;
                 }
 
                 //first find which process node does this node belongs to, attach the process GUID or null
                 if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                     layout += "null,";
                     nodeTORelatedProcessGUID.put(nr, "null");
                 } else {
                     NodeCursor nc = node.neighbors();
                     boolean flag = false;
                     for (int i = 0; i < nc.size(); i++) {
                         nc.cyclicNext();
                         Node tmpn = nc.node();
 //                        PathCaseShapeNodeRealizer tmpnr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(tmpn);
                         PathCaseShapeNodeRealizer tmpnr = (PathCaseShapeNodeRealizer) view.getRealizer(tmpn);
                         //currently we use one connected process GUID as part of indentification
                         if (tmpnr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                             layout += tmpnodeToPathCaseId.get(tmpn).iterator().next() + ",";
                             nodeTORelatedProcessGUID.put(nr, tmpnodeToPathCaseId.get(tmpn).iterator().next());
                             flag = true; //there is at least one connected processnode
                             break;
                         }
                     }
                     if (flag == false) {
                         layout += "WRONG,";//ie, pathways sharing metabolites with the current pathway
                     }
                 }
                 HashSet<String> ids = tmpnodeToPathCaseId.get(node);
                 layout += (ids.iterator().next() + ",");
 
                 //identifier cofactor nodes since they could also be inhibitors or regulators in the same process
                 PathCaseShapeNodeRealizer.PathCaseNodeRole role = nr.getNodeRole();
                 if (role == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR) {
                     layout += "a,";
                 }else if(role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR){
                     layout += "c,";
                 }else if(role == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR){
                     layout += "r,";
                 }else if(role == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR){
                     layout += "i,";
                 }else{
                     layout += " ,";
                 }
 
                 //upper left coords for Graph2D.setLocation
                 layout += (nr.getBoundingBox().x + ",");
                 layout += (nr.getBoundingBox().y + "\n");
                 //System.out.println(nr.getBoundingBox().width+" "+nr.getBoundingBox().height);
             }
 
             layout += "\n";
 
             for (Edge edge :view.getEdgeArray()) { //pathwayEArray){ // view.getEdgeArray()) {
 //                EdgeRealizer er = graphViewer.view.getGraph2D().getRealizer(edge);
                 EdgeRealizer er = view.getRealizer(edge);
                 NodeRealizer nrs = er.getSourceRealizer();
                 NodeRealizer nrt = er.getTargetRealizer();
 
 //                PathCaseShapeNodeRealizer nrs1 = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(nrs.getNode());
                 PathCaseShapeNodeRealizer nrs1 = (PathCaseShapeNodeRealizer) view.getRealizer(nrs.getNode());
                 if (nrs1.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                     continue;
                 }
 //                PathCaseShapeNodeRealizer nrt1 = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(nrt.getNode());
                 PathCaseShapeNodeRealizer nrt1 = (PathCaseShapeNodeRealizer) view.getRealizer(nrt.getNode());
                 if (nrt1.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                     continue;
                 }
 
                 String sc = "";
                 PathCaseShapeNodeRealizer.PathCaseNodeRole roles = nrs1.getNodeRole();
                 if (roles == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR) {
                     sc += "a";
                 }else if(roles == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR){
                     sc += "c";
                 }else if(roles == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR){
                     sc += "r";
                 }else if(roles == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR){
                     sc += "i";
                 }else{
                     sc += " ";
                 }
 
                 String tc = "";
                 PathCaseShapeNodeRealizer.PathCaseNodeRole rolet = nrt1.getNodeRole();
                 if (rolet == PathCaseShapeNodeRealizer.PathCaseNodeRole.ACTIVATOR) {
                     tc += "a";
                 }else if(rolet == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR){
                     tc += "c";
                 }else if(rolet == PathCaseShapeNodeRealizer.PathCaseNodeRole.REGULATOR){
                     tc += "r";
                 }else if(rolet == PathCaseShapeNodeRealizer.PathCaseNodeRole.INHIBITOR){
                     tc += "i";
                 }else{
                     tc += " ";
                 }
 
                 layout += (nodeTORelatedProcessGUID.get(nrs) + "," + tmpnodeToPathCaseId.get(nrs.getNode()).iterator().next() + "," + sc + "," +
                         nodeTORelatedProcessGUID.get(nrt) + "," + tmpnodeToPathCaseId.get(nrt.getNode()).iterator().next() + "," + tc + ",");//source and target X,Y
                 for (int i = 0; i < er.bendCount(); i++) {
                     Bend b = er.getBend(i);
                     layout += (b.getX() + "," + b.getY() + ",");
                 }
                 layout += "\n";
             }
             //System.out.println(layout);
             //convert absolute coordinates to relative coordinates with the top-left one as (0,0)
             LayoutInfo info = new LayoutParser().getParsedInfo(layout);
             info.convertToRelativePositions();
             return info;
         }
 
 
     private LayoutBox applyLayoutInfo(LayoutInfo info) {
         return applyLayoutInfo(info, modelGraphBoundaryX+50, 0);
     }
 
     /**
      * xshift and yshift are added to the positions,
      * also are the exact coordinates for the position of the top-left point of the exact box.
      *
      * <p>
      * After a pathway is placed, or a shared node is moved,
      * the position of nodes will be updated in the corresponding LayoutInfo obj.
      * @param info
      * @param xshift
      * @param yshift
      */
     private LayoutBox applyLayoutInfo(LayoutInfo info, double xshift, double yshift) {
         Graph2D view = graphViewer.view.getGraph2D();
         /*Print out all node IDs to the console
         for (Node node : graphViewer.view.getGraph2D().getNodeArray()) {
             for (Iterator it = nodeToPathCaseId.get(node).iterator(); it.hasNext();)
             { //check each PathCaseID this node represents
                 System.out.println(it.next());
             }
         }
         //*/
 
         //assign coordinates to nodes
         Iterator<LayoutInfo.NodeLayout> e = info.nodes.iterator();
         Node n;
         LayoutInfo.NodeLayout nl;
         while (e.hasNext()) {
             nl = e.next();
             n = getNodeByPathCaseID(nl.processID, nl.nodeID, nl.cofactor);
             if (n != null) {
                 view.setLocation(n, nl.x + xshift, nl.y + yshift);
 //                System.out.println("node   (" + nl.processID+","+nl.nodeID+","+nl.cofactor);
                 //System.out.println("Found node " + nl.nodeID + "when assign coordinates");
             } else {
                 System.err.println("Didn't find node " + nl.nodeID + " when assign coordinates");
                 System.out.println("node----(" + nl.processID+","+nl.nodeID+","+nl.cofactor);
             }
         }
 
         //*assign bend points to edges
         Iterator<LayoutInfo.EdgeLayout> ee = info.edges.iterator();
         LayoutInfo.EdgeLayout el;
         while (ee.hasNext()) {
             el = ee.next();
             Edge edge = getEdgeByPathCaseID(el.sourcepid, el.sourceNode, el.targetNode);
             if (edge == null) {
 //                System.err.println("No edge psid: " + el.sourcepid + " sid: " + el.sourceNode + " tid: " + el.targetNode + "when assign coordinates");
                 continue;
             }
             EdgeRealizer er = graphViewer.view.getGraph2D().getRealizer(edge);
             Iterator<LayoutPoint> eb = el.bends.iterator();
             er.clearBends();
             while (eb.hasNext()) {
                 LayoutPoint pt = eb.next();
                 er.insertBend(pt.x + xshift, pt.y + yshift);
             }
         }//*/ //
 
         LayoutBox b = info.getExactLayoutBox();
         info.convertToShiftedPositions(b.topleft.x+xshift,b.topleft.y+yshift);
         b = info.getExactLayoutBox();
         return b;
     }
 
     /**
      * Place pathway2 which has shared nodes with pathway1, don't consider other pathways
      * the position of nodes will be updated in the corresponding LayoutInfo obj.
      * @param box1 box of the pathway placed and depended on, if the pathway is shifted,this box should be the shifted box
      * @param info2 to be placed, converted to the relative positions already
      * @param shared shared nodes
      * @param position to return the relative position pathway2 is placed {0,1,2,3) => {"left", "right", "top", "below"}
      * @return
      */
     private LayoutBox placeAnotherPathway(LayoutBox box1,LayoutInfo info2,LinkedHashMap<LayoutInfo.NodeLayout, LayoutInfo.NodeLayout> shared,RelativePos position){
         Graph2D view = graphViewer.view.getGraph2D();
         LayoutBox box2 = info2.getExactLayoutBox();
         String[] fourpos = {"left", "right", "top", "below"};
         String[] filps = {"no","leftright","topdown"};
 
         if (shared.size() == 1) {//locate shared node / locate box 2/ move shared node
             Iterator<LayoutInfo.NodeLayout> e = shared.keySet().iterator();
             LayoutInfo.NodeLayout nl1 = e.next();
             LayoutInfo.NodeLayout nl2 = shared.get(nl1);
             String pos = "";//pathway2 on the (left,right,top,below) of pathway1
             String flip = "";//flipping?
 
             //the total distance for the two positions for the shared node in the 2 pathways to be moved
             double distance = box1.width + box2.width + box1.height + box2.height + 240;//initialized to be big enough
 
             for(String f: filps){
                 double x2 = nl2.x;
                 double y2 = nl2.y;
 
                 if(f.equals("leftright")){
                     if(nl2.processID.equals("null")){
                         x2 = box2.topleft.x + (box2.width - (x2 - box2.topleft.x)) - 60;//minus 60 which is the width of process node's box, since using upperleft coor
                     }else if(nl2.cofactor.equalsIgnoreCase("true")){
                         x2 = box2.topleft.x + (box2.width - (x2 - box2.topleft.x)) - 60;
                     }else{
                         x2 = box2.topleft.x + (box2.width - (x2 - box2.topleft.x)) - 20;
                     }
                 }
 
                 if(f.equals("topdown")){
                     if(nl2.cofactor.equalsIgnoreCase("true")){
                         y2 = box2.topleft.y + (box2.height - (y2 - box2.topleft.y)) - 12;
                     }else{
                         y2 = box2.topleft.y + (box2.height - (y2 - box2.topleft.y)) - 20;
                     }
                 }
 
                 for (String s : fourpos) {
                     double d = getDistanceMoved(box1, box2, new LayoutPoint(nl1.x,nl1.y), new LayoutPoint(x2,y2), s);
                     //System.out.println("        position: "+s+" flipping: "+f+" "+d);
                     if (d < distance) {
                         distance = d;
                         pos = s;
                         flip = f;
                     }
                 }
             }
             System.out.println("        position: "+pos+" flipping: "+flip+" with moving distance of the shared node "+distance);
 
             if(flip.equals("leftright")){
                 info2.filpLayoutLeftRight();
             }else if(flip.equals("topdown")){
                 info2.filpLayoutUpDown();
             }
 
             if(position != null){
                 position.setPos(pos);
             }
 
             LayoutInfo info = new LayoutInfo();
             LayoutInfo.NodeLayout snl = info.new NodeLayout("", "", true, 0, 0); //just need the x,y; initialized before passed to getExactBox2
             box2 = getExactBox2(box1, box2,new LayoutPoint(nl1.x,nl1.y), new LayoutPoint(nl2.x,nl2.y), pos, snl);
 
             applyLayoutInfo(info2, box2.topleft.x, box2.topleft.y);
             info2.convertToShiftedPositions(box2.topleft.x, box2.topleft.y);
             Node n = getNodeByPathCaseID(nl1.processID, nl1.nodeID, nl1.cofactor);
             if (n != null) {
                 view.setLocation(n, snl.x, snl.y);
                 System.out.println("            shared node placed at "+snl.x+","+snl.y);
                 double d2=Math.sqrt((nl2.x-snl.x)*(nl2.x-snl.x) + (nl2.y-snl.y)*(nl2.y-snl.y));
                 System.out.println("            moving distance for shared node to the original position in the pathway just placed "+d2);
                 nl1.x=snl.x;
                 nl2.x=snl.x;
                 nl1.y=snl.y;
                 nl2.y=snl.y;
             } else {
                 System.err.println("Didn't find node " + nl1.nodeID + " when assign coordinates to the shared node");
             }
             return box2;
         }
 
         if (shared.size() > 1){
             //get the two center points for those shared nodes in two pathways
             LayoutPoint p1=new LayoutPoint(0,0),p2=new LayoutPoint(0,0);
             getCenterPoints(shared,p1,p2);
 
             String pos = "";//position is pathway2 on the (left,right,top,below) of pathway1
             String flip = "";//flipping?
             //the total distance for the two positions for the shared node in the 2 pathways to be moved
             //initialized to be big enough
             double distance = box1.width + box2.width + box1.height + box2.height + 240;
 
             for (String s : fourpos) {
                 double d = getDistanceMoved(box1, box2, p1,p2, s);
                 //System.out.println(d);
                 if(d < distance){
                     distance = d;
                     pos = s;
                 }
             }
 
             for(String f: filps){
                 double x2 = p2.x;
                 double y2 = p2.y;
 
                 if(f.equals("leftright")){
                     x2 = box2.topleft.x + (box2.width - (x2 - box2.topleft.x));
                 }
 
                 if(f.equals("topdown")){
                     y2 = box2.topleft.y + (box2.height - (y2 - box2.topleft.y));
                 }
 
                 for (String s : fourpos) {
                     double d = getDistanceMoved(box1, box2, p1, new LayoutPoint(x2,y2), s);
                     //System.out.println("        position: "+s+" flipping: "+f+" "+d);
                     if (d < distance) {
                         distance = d;
                         pos = s;
                         flip = f;
                     }
                 }
             }
 
             if(flip.equals("leftright")){
                 info2.filpLayoutLeftRight();
             }else if(flip.equals("topdown")){
                 info2.filpLayoutUpDown();
             }
             System.out.println("        position: "+pos+" flipping: "+flip+" with moving distance of the center of shared nodes"+distance);
 
             LayoutInfo info = new LayoutInfo();
             LayoutInfo.NodeLayout snl = info.new NodeLayout("", "", true, 0, 0); //actually don't need this,just to work with the existing method
             box2 = getExactBox2(box1, box2, p1,p2, pos, snl);
             applyLayoutInfo(info2, box2.topleft.x, box2.topleft.y);
             info2.convertToShiftedPositions(box2.topleft.x, box2.topleft.y);
 
             moveMultiSharedNodes(box1,box2,pos,shared);
             return box2;
         }
 
         else{
             System.err.println("error:No Shared Node but called placeAnotherPathway");
             initialLayout();
             return new LayoutBox(0,0,0,0);
         }
     }
 
     /**
      * @deprecated just for experiments
      * place a pathway sharing one node with an existing pathway just keep the shared node's position and shift the second pathway
      */
     private void placeSecondPathway(LayoutInfo info,LinkedHashMap<LayoutInfo.NodeLayout, LayoutInfo.NodeLayout> shared){
         Graph2D view = graphViewer.view.getGraph2D();
         if (shared.size() == 1) {//locate shared node / locate box 2/ move shared node
             Iterator<LayoutInfo.NodeLayout> e = shared.keySet().iterator();
             LayoutInfo.NodeLayout nl1 = e.next();
             LayoutInfo.NodeLayout nl2 = shared.get(nl1);
             System.out.println(""+nl1+nl2);
             double xshift = nl1.x - nl2.x;
             double yshift = nl1.y - nl2.y;
             applyLayoutInfo(info, xshift, yshift);
             Node n = n = getNodeByPathCaseID(nl2.processID, nl2.nodeID, nl2.cofactor);
             view.setLocation(n,nl1.x,nl2.y);
          }
     }
 
     /**
      * @param box1 the layout box for nodes placed already
      * @param box2 the relative layout box for the pathway to be placed
      * @param nl1 the position of the shared node in pathway 1, could be outside the layoutbox 1 if moved since other sharing
      * @param nl2 the position of the shared node in pathway 2, could be outside the layoutbox 1 if moved since other sharing
      * @param position  pathway2 on the (left,right,top,below) of pathway1
      * @return the total distance for the two positions for the shared node in the 2 pathways to be moved
      */
     private double getDistanceMoved(LayoutBox box1, LayoutBox box2, LayoutPoint nl1, LayoutPoint nl2, String position) {
         //exact position for the top-left point of the exact box of pathway2
         double x = 0;
         double y = 0;
 
         //position for the shared node
         double sx, sy;
         double dist = 0;//to be returned
 
         if (position.equals("right")) {
             sx = box1.topleft.x + box1.width + 120;
             dist = (sx - nl1.x) + (nl2.x + 120);
         } else if (position.equals("left")) {
             sx = box1.topleft.x - 120;
             dist = (nl1.x - sx) + (box2.width - nl2.x + 120);
         } else if (position.equals("top")) {
             dist = (nl1.y - box1.topleft.y + 40) + (box2.height - nl2.y + 40);
         } else if (position.equals("below")) {
             sy = box1.topleft.y + box1.height + 40;
             dist = (sy - nl1.y) + (nl2.y + 40);
         } else {
             System.err.println("Wrong relative position string between the 2 pathways when calling getDistanceMoved");
         }
         return dist;
     }
 
     /**
      * to get the absolute position for pathway2 depend on box1
      * box1 and nl1; box2 and nl2 must be consistent
      * @param box1 the layout box for nodes placed already
      * @param box2 the relative layout box for the pathway to be placed
      * @param nl1 the position of the shared node in layoutBox 1
      * @param nl2 the position of the shared node in layoutBox 2
      * @param position  pathway2 on the (left,right,top,below) of pathway1
      * @param sn to return the absolute position for the shared node, intialized before passed here
      * @return
      */
     private LayoutBox getExactBox2(LayoutBox box1, LayoutBox box2, LayoutPoint nl1, LayoutPoint nl2, String position, LayoutInfo.NodeLayout sn) {
         //exact position for the top-left point of the exact box of pathway2
         double x = 0;
         double y = 0;
 
         if (position.equals("right")) {
             x = box1.topleft.x + box1.width + 240;
             y = nl1.y - nl2.y;
             sn.x = x - 120;
             sn.y = nl1.y;
         } else if (position.equals("left")) {
             x = box1.topleft.x - box2.width - 240;
             y = nl1.y - nl2.y;
             sn.x = box1.topleft.x - 120;
             sn.y = nl1.y;
         } else if (position.equals("top")) {
             x = nl1.x - nl2.x;
             y = box1.topleft.y - box2.height - 80;
             sn.x = nl1.x;
             sn.y = box1.topleft.y - 40;
         } else if (position.equals("below")) {
             x = nl1.x - nl2.x;
             y = box1.topleft.y + box1.height + 80;
             sn.x = nl1.x;
             sn.y = box1.topleft.y + box1.height + 40;
         } else {
             System.err.println("Wrong relative position string between the 2 pathways when calling getDistanceMoved");
         }
         return new LayoutBox(x, y, box2.width, box2.height);
     }
 
     /**
      * place pathway p outside the LayoutBox largeBox to make the surrounding all box more compact, so less white space created
      * There suppose to be no shared nodes between this pathway with pathways placed already
      * @param largeBox
      * @param p only using the width and height of the layout box, won't matter whether relative or absolute positions
      * @return the shifted LayoutBox for pathway p
      */
     private LayoutBox placePathwayOutsideBox(LayoutBox largeBox,LayoutInfo p){
         double xs,ys;
         LayoutBox smallBox = p.getExactLayoutBox();
         if(((largeBox.height+smallBox.height)* Math.max(largeBox.width,smallBox.width))
             <=
            ((largeBox.width+smallBox.width)-Math.max(largeBox.height,smallBox.height))){//smaller one on top
             xs=largeBox.topleft.x+(largeBox.width/2)-(smallBox.width/2);
             ys=largeBox.topleft.y-smallBox.height-smallBox.topleft.y-40;
         }else{//smaller one on right
             xs=largeBox.topleft.x+largeBox.width-smallBox.topleft.x+240;
             ys=largeBox.topleft.y+(largeBox.height/2)-(smallBox.height/2);
         }
 
         applyLayoutInfo(p,xs,ys);
         return new LayoutBox(smallBox.topleft.x+xs,smallBox.topleft.y+ys,smallBox.width, smallBox.height);
     }
 
     /**
      * get the two center points for the two sets of nodes
      * p1,p2 must be initialized before passed here
      * @param shared
      * @param p1
      * @param p2
      */
     private void getCenterPoints(LinkedHashMap<LayoutInfo.NodeLayout, LayoutInfo.NodeLayout> shared,LayoutPoint p1,LayoutPoint p2){
         int n=shared.size();
         LayoutPoint[] pts1 = new LayoutPoint[n];
         LayoutPoint[] pts2 = new LayoutPoint[n];
         Iterator<LayoutInfo.NodeLayout> e = shared.keySet().iterator();
         int i = 0;
         while(e.hasNext()){
             LayoutInfo.NodeLayout nl1 = e.next();
             LayoutInfo.NodeLayout nl2 = shared.get(nl1);
             pts1[i]=new LayoutPoint(nl1.x, nl1.y);
             pts2[i]=new LayoutPoint(nl2.x, nl2.y);
             i++;
         }
         LayoutPoint tp1,tp2;
         tp1 = LayoutPoint.getCenterPoint(pts1);
         tp2 = LayoutPoint.getCenterPoint(pts2);
         p1.x=tp1.x;
         p1.y=tp1.y;
         p2.x=tp2.x;
         p2.y=tp2.y;
     }
 
     /**
      * move shared nodes
      * nodes in box1 and box2 are place already
      * @param box1
      * @param box2
      * @param pos box2's relative position(left/right/top/below) to box1
      * @param shared nodes shared between graphs in box1 and box2
      */
     private void moveMultiSharedNodes(LayoutBox box1,LayoutBox box2,String pos,LinkedHashMap<LayoutInfo.NodeLayout, LayoutInfo.NodeLayout> shared){
         Graph2D view = graphViewer.view.getGraph2D();
 
         //initilaize the cells, for solving overlapping later
         int cellsNum;//the number of cells for shared nodes in that extra space
         double min;//the min x or y coordinate of that extra space
         boolean samey;//true if shared nodes placed horizontally as the two pathway are top-below.
         if(pos.equals("top") || pos.equals("below")){
             cellsNum = (int)Math.max(box1.width,box2.width)/40;
             samey=true;
             min= Math.min(box1.topleft.x,box2.topleft.x);
         }else{
             cellsNum = (int)Math.max(box1.height,box2.height)/40;
             samey=false;
             min = Math.min(box1.topleft.y,box2.topleft.y);
         }
         int cells[] = new int[cellsNum];//number of nodes in that cell
         for(int b:cells){
             b=0;
         }
 
         for (LayoutInfo.NodeLayout nl1 : shared.keySet()) {
             LayoutInfo.NodeLayout nl2 = shared.get(nl1);
             Node n = getNodeByPathCaseID(nl1.processID, nl1.nodeID, nl1.cofactor);
 
             if (n != null) {
                 //y=kx+d: the line which the two copies in the two pathways of the shared node is on
                 double k = (nl1.y - nl2.y) / (nl1.x - nl2.x);
                 double d = nl1.y - k * nl1.x;
                 double nx = 0, ny = 0;
 
                 if (pos.equals("top")) {
                     ny = box1.topleft.y - 40;
                     nx = (ny - d) / k;
                 } else if (pos.equals("below")) {
                     ny = box2.topleft.y - 40;
                     nx = (ny - d) / k;
                 } else if (pos.equals("left")) {
                     nx = box1.topleft.x - 120;
                     ny = k * nx + d;
                 } else if (pos.equals("right")) {
                     nx = box2.topleft.x - 120;
                     ny = k * nx + d;
                 } else {
                     System.err.println("Wrong relative position between two pathways sharing mutiple nodes: " + pos
                             + "so cannot place the shared node to right position");
                 }
                 view.setLocation(n, nx, ny);
 
                 nl1.x = nx;
                 nl2.x = nx;
                 nl1.y = ny;
                 nl2.y = ny;
 
                 //*debug code
                 System.out.println("     a shared node is moved  "+nl1+", "+nl2);
                 PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) (graphViewer.view.getGraph2D().getRealizer(n));
                 PathCaseShapeNodeRealizer.PathCaseNodeRole role = nr.getNodeRole();
                 if (role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR
                         || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN
                         || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT) {
                         System.out.println("            a cofactor");
                 }else{
                       System.out.println("           NOT a cofactor");
                 }
                 //*/
 
                 if(samey){
                     int i = (int)((ny-min+120)/40);
                     cells[i]++;
                 }else{
                     int i = (int)((nx-min+120)/40);
                     cells[i]++;
                 }
 
             } else {
                 System.err.println("Didn't find node " + nl1.nodeID + " when assign coordinates to the shared node");
             }
         }
 
         /* partially solving overlapping
         for(LayoutInfo.NodeLayout nl1 : shared.keySet()) {
             LayoutInfo.NodeLayout nl2 = shared.get(nl1);
             Node n = getNodeByPathCaseID(nl1.processID, nl1.nodeID, nl1.cofactor);
 
             if(samey){
                int c = (int)((nl1.y-min+120)/40);
                if(cells[c]>1){
                    if(cells[c-1] == 0){
                        view.setLocation(n,nl1.x, nl1.y-40);
                        nl1.y = nl1.y-40;
                        nl2.y = nl2.y-40;
                        cells[c]--;
                        cells[c-1]++;
                        System.out.println("     a shared node is moved to avoid overlapping "+nl1+", "+nl2);
                        break;
                    }
 
                    if(cells[c+1] == 0){
                        view.setLocation(n,nl1.x, nl1.y+40);
                        nl1.y = nl1.y+40;
                        nl2.y = nl2.y+40;
                        cells[c]--;
                        cells[c+1]++;
                        System.out.println("     a shared node is moved to avoid overlapping "+nl1+", "+nl2);
                        break;
                    }
                }
             }else{
                int c = (int)((nl1.x-min+120)/40);
                if(cells[c]>1){
                    if(cells[c-1] == 0){
                        view.setLocation(n,nl1.x-40, nl1.y);
                        nl1.x = nl1.x-40;
                        nl2.x = nl2.x-40;
                        cells[c]--;
                        cells[c-1]++;
                        System.out.println("     a shared node is moved to avoid overlapping "+nl1+", "+nl2);
                        break;
                    }
 
                    if(cells[c+1] == 0){
                        view.setLocation(n,nl1.x+40, nl1.y);
                        nl1.x = nl1.x+40;
                        nl2.x = nl2.x+40;
                        cells[c]--;
                        cells[c+1]++;
                        System.out.println("     a shared node is moved to avoid overlapping "+nl1+", "+nl2);
                        break;
                    }
                }
             }
         }
         //*/
     }
 
     /**
      * layout: remove bend points on the edges associated with the node
      *
      * @param n the node
      */
     public void removeBendsOfEdges(Node n) {
         Graph2D view = graphViewer.view.getGraph2D();
 
         //n.edges() didn't work correctly here
         for (Edge edge: view.getEdgeArray()) {
             EdgeRealizer er = graphViewer.view.getGraph2D().getRealizer(edge);
             if(edge.source() == n || edge.target() == n){
                 //System.out.println(er.bendCount());
                 er.clearBends();
             }
         }
     }
 
 
     /**
      * layout: apply frozen layout for pathways to be visualized.
      * apply default auto layout if not all pathway layouts are frozen.
      * @param layouts all the LayoutInfo objs for the pathways being visualzed.
      */
     private void applyFrozenLayout(LayoutInfo[] layouts) {
         for(LayoutInfo i:layouts){
             if(i.isEmpty()){
                initialLayout();
                 return;
             }else{
                 i.convertToRelativePositions();
             }
         }
 
         LayoutBox tmpBox = null;//the box surrounding all pathways placed already
         int[] dependOn = new int[layouts.length];
         ArrayList<Queue> queues = pathwayQueues(layouts,dependOn);
         LayoutBox[] boxes = new LayoutBox[layouts.length];//if a pathway has not been actuall placed, the cell will be null
 
         for(Queue<Integer> connectedPathways:queues){
             /*experiment code
             connectedPathways.clear();
             connectedPathways.add(2);
             dependOn[1]=0;
             connectedPathways.add(0);
             dependOn[2]=1;
             connectedPathways.add(1);
             //*/
             System.out.println("Placing a new connected group: "+connectedPathways);
             while(connectedPathways.size() != 0){
                 int p=connectedPathways.remove();
                 if(tmpBox == null){//first pathway to be placed
                     System.out.println("place the largest pathway "+p+" with size: "+layouts[p].nodes.size());
                     boxes[p]=applyLayoutInfo(layouts[p]);
                     tmpBox=boxes[p];
                     System.out.println("            Placed in Box "+tmpBox);
                 }else if(dependOn[p]==-1){
                     System.out.println("place a pathway "+p+" with size: "+layouts[p].nodes.size()+" outside "+tmpBox);
                     LayoutBox b = placePathwayOutsideBox(tmpBox,layouts[p]);
                     boxes[p]=b;
                     tmpBox=tmpBox.SurroundingBox(b);
                 }else{
                     System.out.println("place pathway "+p+" with size: "+layouts[p].nodes.size()+" depends on pathway:"+dependOn[p]);
                     RelativePos position = new RelativePos(); //{0,1,2,3) => {"right", "left", "top", "below"}
                     //layouts[p] is the one to be placed, so layouts[dependOn[p]]'s sharedNodes is called
                     LayoutBox b = placeAnotherPathway(boxes[dependOn[p]],layouts[p],layouts[dependOn[p]].sharedNodes(layouts[p]),position);
                     if(overlapWithPlacedPathways(boxes,b)){
                         System.out.println("    place pathway "+p+" overlapped");
                         if(!trySamplePositions(boxes,b,layouts,dependOn,p,layouts[dependOn[p]].sharedNodes(layouts[p]),position)){
                             System.out.println("       Failed in all sample positions, place outside box"+tmpBox);
                             layouts[p].convertToRelativePositions();
                             b = placeAnotherPathway(tmpBox,layouts[p],layouts[dependOn[p]].sharedNodes(layouts[p]),position);
                         }
                     }
 
                     for(LayoutInfo.NodeLayout nl: layouts[dependOn[p]].sharedNodes(layouts[p]).keySet()){
                         removeBendsOfEdges(getNodeByPathCaseID(nl.processID,nl.nodeID,nl.cofactor));
                     }
 
                     //test:LayoutBox b = placeAnotherPathway(tmpBox,layouts[p],layouts[dependOn[p]].sharedNodes(layouts[p]),position);
                     System.out.println("            Placed in Box "+b);
                     boxes[p]=b;
                     tmpBox=tmpBox.SurroundingBox(b);
                     //placeSecondPathway(layouts[p],layouts[dependOn[p]].sharedNodes(layouts[p]));
                 }
             }
         }
 
         /*test
         for(LayoutInfo i:layouts){
             LayoutBox b = i.getExactLayoutBox();
             System.out.println(b);
             applyLayoutInfo(i,b.topleft.x,b.topleft.y);
         }
         //*/
     }
 
     /**
      * return a list of queues for connected groups of pathways
      * place pathways according to the order of the queues and the list
      * @param layouts
      * @param dependOn the index number of pathway this one depends on, -1 if no dependency
      * @return
      */
     private ArrayList<Queue> pathwayQueues(LayoutInfo[] layouts,int[] dependOn){
         ArrayList<Queue> queues = new ArrayList<Queue>();//each queue is for a disconnected group of pathways
         int n=layouts.length;
         boolean[] added = new boolean[n];//whether the pathway is added into a queue
         //boolean[] processed = new boolean[n];//whether all neighbours of this pathway are added into some queue
         Queue<Integer> toProcess = new LinkedList<Integer>();
 
         //current queue to add pathways into
         //if not null, then exists a queue to add pathways into
         //otherwise need to create a new queue(for disconnected group of pathways)
         Queue currentQueue=null;
 
         Arrays.sort(layouts,new LayoutBoxSizeComparator());
 
         /*
         for(LayoutInfo i:layouts){
             System.out.println(i.getExactLayoutBox().size());
         }
         //*/
 
         for(int i=n-1;i>=0;i--){
             if(added[i]){
                 continue;
             }
 
             currentQueue=new LinkedList<Integer>();//current queue to add pathways into
             queues.add(currentQueue);
             toProcess.add(i);
             currentQueue.add(i);
             added[i]=true;
             dependOn[i]=-1;
 
             while(toProcess.size() != 0){
                 int p = toProcess.remove();
                 for(int j=n-2;j>=0;j--){//the first one is already added, so j starting from 1
                     //find all non-visited neighbour pathways of layout[p]
                     if(added[j]){
                         continue;
                     }
                     if(layouts[p].sharedNodes(layouts[j]).size() > 0 && !added[j]){
                         currentQueue.add(j);
                         toProcess.add(j);
                         added[j]=true;
                         dependOn[j]=p;
                     }
                 }
                 //processed[p]=true;
             }
             currentQueue=null;//finished a connected group of pathways
         }
 
         return queues;
     }
 
     /**
      * @param boxes
      * @param b
      * @return
      */
     private boolean overlapWithPlacedPathways(LayoutBox[] boxes,LayoutBox b){
         for(int i=0;i<boxes.length;i++){
             LayoutBox box = boxes[i];
             if(box != null){//pathways not actually placed(including p), the box would be null
                 if(box.overlapLayoutBox(b)){//overlapping with some pathway already placed before
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * layout: change the number sample to try more or less
      * @param boxes
      * @param b
      * @param layouts
      * @param dependOn
      * @param p
      * @param shared
      * @param position
      * @return
      */
     private boolean trySamplePositions(LayoutBox[] boxes,LayoutBox b,LayoutInfo[] layouts,int[] dependOn,int p,LinkedHashMap<LayoutInfo.NodeLayout, LayoutInfo.NodeLayout> shared,RelativePos position){
         //record all the current placed positions of shared nodes, to recover later
         ArrayList<LayoutInfo.NodeLayout> sharedCopy = new ArrayList<LayoutInfo.NodeLayout>(shared.size());
         for (LayoutInfo.NodeLayout nl : shared.keySet()) {
             sharedCopy.add(layouts[p].new NodeLayout(nl.processID, nl.nodeID, nl.cofactor, nl.x, nl.y));
         }
 
         boolean noOverlap=false;
         int sample=20;//half of the number of sample positions to try
 
         if(position.p == 0 || position.p == 1){//left or right cases, move vertically
             System.out.println("try sample positions vertically");
             double d = layouts[dependOn[p]].getExactLayoutBox().height /(2*sample);
             for(int i=0;i<sample;i++){
                 double y = b.topleft.y + d*(i+1);
                 LayoutBox tmpB = new LayoutBox(b.topleft.x,y,b.width,b.height);
                 if(!overlapWithPlacedPathways(boxes,tmpB)){
                     System.out.println("succeeded a sample position");
                     applyLayoutInfo(layouts[p],0,y-b.topleft.y);
                     b = tmpB;
                     restorePositions(sharedCopy);
                     return true;
                 }
 
                 y = b.topleft.y - d*(i+1);
                 tmpB = new LayoutBox(b.topleft.x,y,b.width,b.height);
                 if(!overlapWithPlacedPathways(boxes,tmpB)){
                     System.out.println("succeeded a sample position");
                     applyLayoutInfo(layouts[p],0,y-b.topleft.y);
                     b = tmpB;
                     restorePositions(sharedCopy);
                     return true;
                 }
             }
         }else{//top or below cases, move horizontally
             System.out.println("try sample positions horizontally");
             double d = layouts[dependOn[p]].getExactLayoutBox().width /(2*sample);
             for(int i=0;i<sample;i++){
                 double y = b.topleft.x + d*(i+1);
                 LayoutBox tmpB = new LayoutBox(b.topleft.x,y,b.width,b.height);
                 if(!overlapWithPlacedPathways(boxes,tmpB)){
                     System.out.println("succeeded a sample position");
                     applyLayoutInfo(layouts[p],0,y-b.topleft.y);
                     b = tmpB;
                     restorePositions(sharedCopy);
                     return true;
                 }
 
                 y = b.topleft.y - d*(i+1);
                 tmpB = new LayoutBox(b.topleft.x,y,b.width,b.height);
                 if(!overlapWithPlacedPathways(boxes,tmpB)){
                     System.out.println("succeeded a sample position");
                     applyLayoutInfo(layouts[p],0,y-b.topleft.y);
                     b = tmpB;
                     restorePositions(sharedCopy);
                     return true;
                 }
             }
         }
         return noOverlap;
     }
 
     /**
      * recover positions of these nodes
      * @param nodes
      */
     private void restorePositions(ArrayList<LayoutInfo.NodeLayout> nodes){
         Graph2D view = graphViewer.view.getGraph2D();
         for(LayoutInfo.NodeLayout nl:nodes){
             Node n = getNodeByPathCaseID(nl.processID, nl.nodeID, nl.cofactor);
             view.setLocation(n,nl.x,nl.y);
         }
     }
 
 
     /**
      * layout: get layoutInfo from current pathway graph
      * skipping collapsed pathway nodes currently
      *
      */
 
     private String layoutInfo(String sysbio) {
         //System.out.println(getLayoutInfo().toString());
         return getLayoutInfo(sysbio).toXMLString();
     }
 
      private LayoutInfo getLayoutInfo(String sysbio) {
         HashMap<NodeRealizer, String> nodeTORelatedProcessGUID = new HashMap<NodeRealizer, String>(); //filled during the first part, traversing nodes
         String layout = "";
         Graph2D view = graphViewer.view.getGraph2D();
         /**
          * Own Designed Compact Layout String Structure:
          * First part is for nodes, then an empty line followed by the part for edges:
          * {[ProcessGUID | null][comma][GUID][comma][cofactor| ][comma][centerX][comma][centerY]\n}*
          * \n
          * {[SourceNodeProcessGUID | null][comma][SourceNodeGUID][comma][TargetNodeProcessGUID | null][comma][TargetNodeGUID][comma]{[bendX][comma][bendY][comma]}* \n }*
          */
         //TODO: generate the LayoutInfo directly or first to XML format
         for (Node node : view.getNodeArray()) {
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             //skip pathway nodes
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                 continue;
             }
 
             //first find which process node does this node belongs to, attach the process GUID or null
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                 layout += "null,";
                 nodeTORelatedProcessGUID.put(nr, "null");
             } else {
                 NodeCursor nc = node.neighbors();
                 boolean flag = false;
                 for (int i = 0; i < nc.size(); i++) {
                     nc.cyclicNext();
                     Node tmpn = nc.node();
                     PathCaseShapeNodeRealizer tmpnr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(tmpn);
                     //currently we use one connected process GUID as part of indentification
                     if (tmpnr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                         layout += nodeToPathCaseId.get(tmpn).iterator().next() + ",";
                         nodeTORelatedProcessGUID.put(nr, nodeToPathCaseId.get(tmpn).iterator().next());
                         flag = true; //there is at least one connected processnode
                         break;
                     }
                 }
                 if (flag == false) {
                     layout += "WRONG,";//ie, pathways sharing metabolites with the current pathway
                 }
             }
 //            if(nodeToPathCaseId.get(node).iterator().next().equals("57041a9b-4291-4b53-bf0b-dc3f5e9e5081")){
 //                int i=0;
 //            }
             layout += (nodeToPathCaseId.get(node).iterator().next() + ",");
 
             //identifier cofactor nodes since they could also be inhibitors or regulators in the same process
             PathCaseShapeNodeRealizer.PathCaseNodeRole role = nr.getNodeRole();
             if (role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR
                     || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN
                     || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT) {
                 layout += "cofactor,";
             } else {
                 layout += " ,";
             }
 
             //upper left coords for Graph2D.setLocation
             layout += (nr.getBoundingBox().x + ",");
             layout += (nr.getBoundingBox().y + "\n");
             //System.out.println(nr.getBoundingBox().width+" "+nr.getBoundingBox().height);
         }
 
         layout += "\n";
 //
 //        for (Edge edge : view.getEdgeArray()) {
 //            EdgeRealizer er = graphViewer.view.getGraph2D().getRealizer(edge);
 //            NodeRealizer nrs = er.getSourceRealizer();
 //            NodeRealizer nrt = er.getTargetRealizer();
 //
 //            PathCaseShapeNodeRealizer nrs1 = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(nrs.getNode());
 //            if (nrs1.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
 //                continue;
 //            }
 //            PathCaseShapeNodeRealizer nrt1 = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(nrt.getNode());
 //            if (nrt1.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
 //                continue;
 //            }
 //
 //            layout += (nodeTORelatedProcessGUID.get(nrs) + "," + nodeToPathCaseId.get(nrs.getNode()).iterator().next() + "," +
 //                    nodeTORelatedProcessGUID.get(nrt) + "," + nodeToPathCaseId.get(nrt.getNode()).iterator().next() + ",");//source and target X,Y
 //            for (int i = 0; i < er.bendCount(); i++) {
 //                Bend b = er.getBend(i);
 //                layout += (b.getX() + "," + b.getY() + ",");
 //            }
 //            layout += "\n";
 //        }
 
         //convert absolute coordinates to relative coordinates with the top-left one as (0,0)
         LayoutInfo info = new LayoutParser().getParsedInfo(layout);
         info.convertToRelativePositions();
         return info;
     }
 
 
     private String layoutInfo() {
         //System.out.println(getLayoutInfo().toString());
         return getLayoutInfo().toXMLString();
     }
 
     private LayoutInfo getLayoutInfo() {
         HashMap<NodeRealizer, String> nodeTORelatedProcessGUID = new HashMap<NodeRealizer, String>(); //filled during the first part, traversing nodes
         String layout = "";
         Graph2D view = graphViewer.view.getGraph2D();
         /**
          * Own Designed Compact Layout String Structure:
          * First part is for nodes, then an empty line followed by the part for edges:
          * {[ProcessGUID | null][comma][GUID][comma][cofactor| ][comma][centerX][comma][centerY]\n}*
          * \n
          * {[SourceNodeProcessGUID | null][comma][SourceNodeGUID][comma][TargetNodeProcessGUID | null][comma][TargetNodeGUID][comma]{[bendX][comma][bendY][comma]}* \n }*
          */
         //TODO: generate the LayoutInfo directly or first to XML format
         for (Node node : view.getNodeArray()) {
             PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(node);
             //skip pathway nodes
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                 continue;
             }
 
             //first find which process node does this node belongs to, attach the process GUID or null
             if (nr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                 layout += "null,";
                 nodeTORelatedProcessGUID.put(nr, "null");
             } else {
                 NodeCursor nc = node.neighbors();
                 boolean flag = false;
                 for (int i = 0; i < nc.size(); i++) {
                     nc.cyclicNext();
                     Node tmpn = nc.node();
                     PathCaseShapeNodeRealizer tmpnr = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(tmpn);
                     //currently we use one connected process GUID as part of indentification
                     if (tmpnr.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.GENERICPROCESS) {
                         layout += nodeToPathCaseId.get(tmpn).iterator().next() + ",";
                         nodeTORelatedProcessGUID.put(nr, nodeToPathCaseId.get(tmpn).iterator().next());
                         flag = true; //there is at least one connected processnode
                         break;
                     }
                 }
                 if (flag == false) {
                     layout += "WRONG,";//ie, pathways sharing metabolites with the current pathway
                 }
             }
             if(nodeToPathCaseId.get(node).iterator().next().equals("57041a9b-4291-4b53-bf0b-dc3f5e9e5081")){
                 int i=0;
             }
             layout += (nodeToPathCaseId.get(node).iterator().next() + ",");
 
             //identifier cofactor nodes since they could also be inhibitors or regulators in the same process
             PathCaseShapeNodeRealizer.PathCaseNodeRole role = nr.getNodeRole();
             if (role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR
                     || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN
                     || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT) {
                 layout += "cofactor,";
             } else {
                 layout += " ,";
             }
 
             //upper left coords for Graph2D.setLocation
             layout += (nr.getBoundingBox().x + ",");
             layout += (nr.getBoundingBox().y + "\n");
             //System.out.println(nr.getBoundingBox().width+" "+nr.getBoundingBox().height);
         }
 
         layout += "\n";
 
         for (Edge edge : view.getEdgeArray()) {
             EdgeRealizer er = graphViewer.view.getGraph2D().getRealizer(edge);
             NodeRealizer nrs = er.getSourceRealizer();
             NodeRealizer nrt = er.getTargetRealizer();
 
             PathCaseShapeNodeRealizer nrs1 = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(nrs.getNode());
             if (nrs1.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                 continue;
             }
             PathCaseShapeNodeRealizer nrt1 = (PathCaseShapeNodeRealizer) graphViewer.view.getGraph2D().getRealizer(nrt.getNode());
             if (nrt1.getNodeRole() == PathCaseShapeNodeRealizer.PathCaseNodeRole.COLLAPSEDPATHWAY) {
                 continue;
             }
 
             layout += (nodeTORelatedProcessGUID.get(nrs) + "," + nodeToPathCaseId.get(nrs.getNode()).iterator().next() + "," +
                     nodeTORelatedProcessGUID.get(nrt) + "," + nodeToPathCaseId.get(nrt.getNode()).iterator().next() + ",");//source and target X,Y
             for (int i = 0; i < er.bendCount(); i++) {
                 Bend b = er.getBend(i);
                 layout += (b.getX() + "," + b.getY() + ",");
             }
             layout += "\n";
         }
 
         //convert absolute coordinates to relative coordinates with the top-left one as (0,0)
         LayoutInfo info = new LayoutParser().getParsedInfo(layout);
         info.convertToRelativePositions();
         return info;
     }
 
      public Node getNodeByPathCaseID(String pid, String id, String cofactor) {
          boolean b=false;
          if(cofactor.equalsIgnoreCase("true"))b=true;
          return getNodeByPathCaseID(pid,id,b);
      }
 
     //pid is the GUID of one process connected with the node, if the node is a process node, then pid should be "null" or null
     public Node getNodeByPathCaseID(String pid, String id, boolean cofactor) {
         //TODO          use index instead of exhaustive search
         /* getNodeByID Debug Code
         if(pid !=null && id !=null && pid.equals("131eaa55-1b6a-4760-82c9-718f862995c8") && id.equals("ac47abf2-d306-11d5-bd13-00b0d0794900"))
         {
             System.out.print("131eaa55-1b6a-4760-82c9-718f862995c8,ac47abf2-d306-11d5-bd13-00b0d0794900");
             System.out.println(" is Cofactor? "+ cofactor);
         }
         //*/
 
         if (pid == null || pid.equals("null")) {
             return getNodeByPathCaseID(id); //searching for a process node, no duplicate possibility
         } else {
             Iterator<Node> iter = nodeToPathCaseId_Pathway.keySet().iterator();
             Node node = null;
 //            boolean iFound=false;
             while(iter.hasNext())
             {
                 node = iter.next();
 //                HashSet<String> pid = nodeToPathCaseId_Pathway.get(n);
 //                if (pid != null && pid.contains(id))
 //                    return n;
                 for (Iterator it = nodeToPathCaseId_Pathway.get(node).iterator(); it.hasNext();)
                 { //check each PathCaseID this node represents
                     String tmpid = (String) it.next();
                     if (tmpid != null && tmpid.equals(id)) {
                         NodeCursor nc = node.neighbors();
                         for (int i = 0; i < nc.size(); i++) {
                             nc.cyclicNext();
                             Node tmpn = nc.node();
                             String tmpPid = nodeToPathCaseId_Pathway.get(tmpn).iterator().next(); //process node only associate with one PathCaseID
                             if (tmpPid != null && tmpPid.equals(pid)) {
                                 PathCaseShapeNodeRealizer nr = (PathCaseShapeNodeRealizer) (graphViewer.view.getGraph2D().getRealizer(node));
                                 boolean c;
 
                                 PathCaseShapeNodeRealizer.PathCaseNodeRole role = nr.getNodeRole();
                                 if (role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOR
                                         || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTORIN
                                         || role == PathCaseShapeNodeRealizer.PathCaseNodeRole.COFACTOROUT) {
                                     c = true;
                                 } else {
                                     c = false;
                                 }
                                 //currently commented out, may be needed for other pathways.
 //                                if (c == cofactor) {
                                     return node;
 //                                }
                             }
                         }
 //                       iFound=true;
 //                        break;
                     }
                 }
 //                if(iFound) break;
             }
             
 //            for (Node node :(Node[])nodeToPathCaseId_Pathway.keySet().toArray()){//  graphViewer.view.getGraph2D().getNodeArray()) {
 //
 //            }
         }
         return null;
     }
 
     //pid is the GUID of one process connected with the node, if the node is a process node, then pid should be "null" or null
     public Edge getEdgeByPathCaseID(String psid, String sid, String tid) {
         boolean flag = false;
         if (psid == null || psid.equals("null")) {
             flag = true; //source node is a process node
         }
         Graph2D view = graphViewer.view.getGraph2D();
         //here we should extract pathway's edges only, not including model's edges
         for (Edge edge : view.getEdgeArray()) {
             EdgeRealizer er = graphViewer.view.getGraph2D().getRealizer(edge);
             NodeRealizer nrs = er.getSourceRealizer();
             NodeRealizer nrt = er.getTargetRealizer();
             HashSet<String> s = nodeToPathCaseId_Pathway.get(nrs.getNode());
             HashSet<String> t = nodeToPathCaseId_Pathway.get(nrt.getNode());
             if(s!=null && t!=null){
                 if (s.contains(sid) && t.contains(tid)) {
                     if (flag) {
                         return edge;
                     } else {
                         Node node = nrs.getNode();
                         NodeCursor nc = node.neighbors();
                         for (int i = 0; i < nc.size(); i++) {//see if the source node connected with the same process node
                             nc.cyclicNext();
                             Node tmpn = nc.node();
                             String tmpPid = nodeToPathCaseId_Pathway.get(tmpn).iterator().next();//process node only has one PathCaseID associated
                             if (tmpPid.equals(psid)) {
                                 return edge;
                             }
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      * apply stored a single pathway layout
      * @deprecated use applyFrozenLayout(LayoutInfo[])
      */
     private void storedSinglePathwayLayout(LayoutInfo info) {
         if (info.isEmpty()) {
             initialLayout();
             return;
         }
 
         applyLayoutInfo(info);
     }
 
     /**
      * apply stored layout for 2 neighbouring pathways
      * @deprecated use applyFrozenLayout(LayoutInfo[])
      * @param info1
      * @param info2
      */
     private void storedTwoPathwayLayout(LayoutInfo info1, LayoutInfo info2) {
         Graph2D view = graphViewer.view.getGraph2D();
 
         if (info1.isEmpty() && info2.isEmpty()) {//not using freezed layout at all
             initialLayout();
             return;
         } else if (info2.isEmpty()) {
             initialLayout();
             //*
             String layout = layoutInfo();
             LayoutInfo info = new LayoutParser().getParsedInfo(layout);
             LayoutBox box = info.getExactLayoutBox();
             applyLayoutInfo(info1,box.topleft.x+box.width,0);
             //*/
             return;
         } else if (info1.isEmpty()) {
             initialLayout();
             //*
             String layout = layoutInfo();
             LayoutInfo info = new LayoutParser().getParsedInfo(layout);
             LayoutBox box = info.getExactLayoutBox();
             applyLayoutInfo(info2,box.topleft.x+box.width,0);
             //*/
             return;
         }
 
         info1.convertToRelativePositions();
         info2.convertToRelativePositions();
         LayoutBox box1 = info1.getExactLayoutBox();
         LayoutBox box2 = info2.getExactLayoutBox();
         LinkedHashMap<LayoutInfo.NodeLayout, LayoutInfo.NodeLayout> shared = info1.sharedNodes(info2);
 
         /*Debug code
         System.out.println("shared nodes");
         Iterator<LayoutInfo.NodeLayout> e = shared.keySet().iterator();
         while(e.hasNext()){
             LayoutInfo.NodeLayout nl = e.next();
             System.out.println(nl.nodeID+"  "+shared.get(nl).nodeID);
         }
         //System.out.println("Box1     ("+box1.topleft.x+","+box1.topleft.y+");("+(box1.topleft.x+box1.width)+","+(box1.topleft.y+box1.height));
         //System.out.println("Box2    ("+(box1.topleft.x+box1.width+120)+");("+( box1.topleft.y+40));
         //*/
         //if(shared.size() == 0)return;
 
         if(shared.size() == 0){
             /**
              * place one first(larger one here)
              * then try place the second one to make the new large box bounding all nodes more square-like
              */
             LayoutBox largeBox;
             LayoutInfo smallInfo;
             if(box1.isLargerBox(box2)){
                 applyLayoutInfo(info2);
                 largeBox=box2;
                 smallInfo=info1;
             }else{
                 applyLayoutInfo(info1);
                 largeBox=box1;
                 smallInfo=info2;
             }
             placePathwayOutsideBox(largeBox,smallInfo);
             return;
         }else{
             applyLayoutInfo(info1);
             placeAnotherPathway(box1,info2,shared,null);
         }
     }
 
     
     public static class NodeData
   {
 
     /** Holds value of property propertyName. */
     private String propertyName;
 
     /** Holds value of property classType. */
     private Class classType;
 
     /** Holds value of property nodeMap. */
     private NodeMap nodeMap;
 
     /** creates a generic node meta data object
      * @param propertyName the name of the property, which will be used for
      * display in the optionhandler, and in the gml file
      * @param classtype the type of the attributes, either
      * <CODE>String.class</CODE>, <CODE>Float.class</CODE>,
      * <CODE>Double.class</CODE>, <CODE>Integer.class</CODE>
      * or <CODE>Boolean.class</CODE>
      * @param nodeMap the nodemap used for the mapping between the nodes
      * and the values
      */
     public NodeData(String propertyName, Class classtype, NodeMap nodeMap)
     {
       this.propertyName = propertyName;
       this.classType = classtype;
       this.nodeMap = nodeMap;
     }
 
     /** Getter for property propertyName.
      * @return Value of property propertyName.
      */
     public String getPropertyName()
     {
       return this.propertyName;
     }
 
     /** Getter for property classType.
      * @return Value of property classType.
      */
     public Class getClassType()
     {
       return this.classType;
     }
 
     /** Getter for property nodeMap.
      * @return Value of property nodeMap.
      */
     public NodeMap getNodeMap()
     {
       return this.nodeMap;
     }
   }
 
 }
 
