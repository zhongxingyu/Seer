 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on May 22, 2007
  */
 package com.soartech.simjr.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 
 import bibliothek.gui.dock.common.CControl;
 import bibliothek.gui.dock.common.CLocation;
 import bibliothek.gui.dock.common.DefaultMultipleCDockable;
 import bibliothek.gui.dock.common.DefaultSingleCDockable;
 import bibliothek.gui.dock.common.MultipleCDockableFactory;
 import bibliothek.gui.dock.common.MultipleCDockableLayout;
 import bibliothek.gui.dock.common.SingleCDockable;
 import bibliothek.gui.dock.common.event.CFocusListener;
 import bibliothek.gui.dock.common.intern.CDockable;
 import bibliothek.gui.dock.common.menu.CLayoutChoiceMenuPiece;
 import bibliothek.gui.dock.common.menu.CLookAndFeelMenuPiece;
 import bibliothek.gui.dock.common.menu.CThemeMenuPiece;
 import bibliothek.gui.dock.common.menu.SingleCDockableListMenuPiece;
 import bibliothek.gui.dock.common.theme.ThemeMap;
 import bibliothek.gui.dock.facile.menu.FreeMenuPiece;
 import bibliothek.gui.dock.facile.menu.RootMenuPiece;
 import bibliothek.gui.dock.facile.menu.SubmenuPiece;
 import bibliothek.gui.dock.support.lookandfeel.LookAndFeelList;
 import bibliothek.util.xml.XElement;
 import bibliothek.util.xml.XIO;
 
 import com.soartech.simjr.ProgressMonitor;
 import com.soartech.simjr.SimJrProps;
 import com.soartech.simjr.SimulationException;
 import com.soartech.simjr.adaptables.Adaptables;
 import com.soartech.simjr.services.ServiceManager;
 import com.soartech.simjr.services.SimulationService;
 import com.soartech.simjr.ui.actions.ActionManager;
 import com.soartech.simjr.ui.actions.AddDistanceToolAction;
 import com.soartech.simjr.ui.actions.AddToPolygonAction;
 import com.soartech.simjr.ui.actions.AdjustMapOpacityAction;
 import com.soartech.simjr.ui.actions.ClearDistanceToolsAction;
 import com.soartech.simjr.ui.actions.ExitAction;
 import com.soartech.simjr.ui.actions.ExportScenarioAction;
 import com.soartech.simjr.ui.actions.LoadContainerAction;
 import com.soartech.simjr.ui.actions.LoadDockingLayoutAction;
 import com.soartech.simjr.ui.actions.LoadScenarioAction;
 import com.soartech.simjr.ui.actions.LockViewToEntityAction;
 import com.soartech.simjr.ui.actions.NewPlanViewDisplayAction;
 import com.soartech.simjr.ui.actions.PauseAction;
 import com.soartech.simjr.ui.actions.RemoveFromPolygonAction;
 import com.soartech.simjr.ui.actions.RestoreDefaultLayoutAction;
 import com.soartech.simjr.ui.actions.RunAction;
 import com.soartech.simjr.ui.actions.SaveDockingLayoutAction;
 import com.soartech.simjr.ui.actions.ShowAllAction;
 import com.soartech.simjr.ui.actions.ShowGridAction;
 import com.soartech.simjr.ui.actions.ToggleCategoryLabelsAction;
 import com.soartech.simjr.ui.actions.UnloadContainerAction;
 import com.soartech.simjr.ui.actions.ZoomInAction;
 import com.soartech.simjr.ui.actions.ZoomOutAction;
 import com.soartech.simjr.ui.cheatsheets.CheatSheetView;
 import com.soartech.simjr.ui.properties.EntityPropertiesView;
 import com.soartech.simjr.ui.pvd.PlanViewDisplay;
 import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;
 
 /**
  * @author ray
  */
 public class SimulationMainFrame extends JFrame implements SimulationService, PlanViewDisplayProvider
 {
 //    public static final String PVD_FRAME_KEY = "__pvds";
     public static final String RADIO_MESSAGES_FRAME_KEY = "__radioMessages";
     public static final String CONSOLE_FRAME_KEY = "__console";
     public static final String SOAR_STATUS_FRAME_KEY = "__soarStatus";
 
     public static final String ENTITY_PROPERTIES_FRAME_KEY = "__entityProperties";
     public static final String CHEAT_SHEET_FRAME_KEY = "__cheatSheet";
 
     public static final String ENTITIES_FRAME_KEY = "__entities";
     
     private final CLocation defaultPvdLocation = CLocation.base().normalRectangle(0, 0, 0.8, 0.7).stack(0);
     
     private final CLocation defaultEntityListLocation = CLocation.base().normalRectangle(0.8, 0, 0.2, 0.5);
     private final CLocation defaultEntityPropertiesLocation = CLocation.base().normalRectangle(0.8, 0.5, 0.2, 0.5);
     private final CLocation defaultRadioMessagesLocation = CLocation.base().normalRectangle(0, 0.7, 0.8, 0.3);
     private final CLocation defaultConsoleLocation = CLocation.base().normalRectangle(0, 0.7, 0.8, 0.3).stack(0);
     private final CLocation defaultCheatSheetLocation = CLocation.base().normalRectangle(0, 0.7, 0.8, 0.3).stack(0);
     
     private final CLocation defaultSingleDockableLocation = CLocation.base().normalRectangle(0, 0.7, 0.8, 0.3).stack(0);
     
     public static final CLocation defaultSAPLocation = CLocation.base().normalRectangle(0.8, 0, 0.2, 0.5);
     
     private static final long serialVersionUID = 2229452633565018323L;
 
     private ServiceManager services;
         
     private JMenu fileMenu = new JMenu("File");
     
     private RootMenuPiece viewMenuRoot = new RootMenuPiece( "View", false );
     
     private EntityPropertiesView propertiesView;
     private CheatSheetView cheatSheetView;
 
     private Map<String,SingleCDockable> singleDockables = new HashMap<String,SingleCDockable>();
     private Map<String,SingleCDockable> singleAuxillaryDockables = new HashMap<String,SingleCDockable>();
     
     /**
      * The factory for PVD frames for DF
      */
     private PvdFactory pvdFactory;
     
     /**
      * The common controller for Docking Frames.
      */
     private CControl control;
     
     private Dimension frameDimension;
     
     private SimulationTimePanel simTimePanel;
 
     private class PvdFrame extends DefaultMultipleCDockable
     {
         PlanViewDisplay pvd;
         String title;
         
         public PvdFrame(MultipleCDockableFactory<PvdFrame, PvdLayout> factory, String string)
         {
             super(factory, string);
         }
     }
     
     private int nextPvdId = 0;
     private List<PvdFrame> pvds = new ArrayList<PvdFrame>();
     private PvdFrame activePvdFrame;
     
     public static SimulationMainFrame findService(ServiceManager services)
     {
         return services.findService(SimulationMainFrame.class);
     }
     
     public static PlanViewDisplay getActivePlanViewDisplay(ServiceManager services)
     {
         SimulationMainFrame mf = findService(services);
         
         return mf != null ? mf.getActivePlanViewDisplay() : null;
     }
     
     /**
      * @param serviceManager
      */
     public SimulationMainFrame(ServiceManager serviceManager)
     {
         this.services = serviceManager;
         this.services.addService(this);
         
         setTitle(SimJrProps.get("simjr.window.title","Sim Jr"));
         frameDimension = new Dimension(SimJrProps.get("simjr.window.width", 1000), SimJrProps.get("simjr.window.height", 800));
         setSize(frameDimension);
 
         //Dockable Frames integration
         control = new CControl(this);
         add(control.getContentArea());
         
         //set the default theme
         ThemeMap themes = control.getThemes();
         themes.select(ThemeMap.KEY_FLAT_THEME);
         
         //set the default look and feel
         LookAndFeelList lafList = LookAndFeelList.getDefaultList();
         lafList.setLookAndFeel(lafList.getSystem());
         
         // Listen for window closing event so we can save dock layout before
         // the frame is dispose.
         this.addWindowListener(new WindowAdapter() {
 
             public void windowClosing(WindowEvent arg0)
             {
                 control.destroy();
             }});
 
         //add factory that lets DF create multiple PVD frames
         pvdFactory = new PvdFactory();
         control.addMultipleDockableFactory("pvd", pvdFactory);
         
         //create the first PVD
         createPvdFrame(null, false);
 
         addDockable(new EntityListPanel(services), defaultEntityListLocation, ENTITIES_FRAME_KEY);
         addDockable(this.propertiesView = new EntityPropertiesView(services), defaultEntityPropertiesLocation, ENTITY_PROPERTIES_FRAME_KEY);
         addDockable(new RadioMessagePanel(services), defaultRadioMessagesLocation, RADIO_MESSAGES_FRAME_KEY);
         addDockable(new ConsolePanel(services), defaultConsoleLocation, CONSOLE_FRAME_KEY);
         addDockable(services.findService(CheatSheetView.class), defaultCheatSheetLocation, CHEAT_SHEET_FRAME_KEY);
         
         initMenus();
         initToolbar();
     }
 
     private void addDockable(SingleCDockable dockable, CLocation location, String key)
     {
         dockable.setLocation(location);
         singleDockables.put(key, dockable);
         control.addDockable(dockable);
         dockable.setVisible(true);        
     }
     
     public void addAuxillaryDockable(SingleCDockable dockable, CLocation location, String key)
     {
         dockable.setLocation(location);
         singleAuxillaryDockables.put(key, dockable);
         control.addDockable(dockable);        
     }
     
     /**
      * Wrap the given component in a dockable frame and add it to the main frame.
      * 
      * @param id
      * @param title
      * @param c
      * @param nextTo
      */
     public void addFrame(String id, String title, Component c, Component nextTo)
     {
         //create a dockable to hold the component
        DefaultSingleCDockable dockable = new DefaultSingleCDockable(id, title,
                c);
        dockable.setMinimizable(false);
        dockable.setCloseable(true);
         
         //add the dockable to the main frame
         addDockable(dockable, defaultSingleDockableLocation, id);
         
         //TODO: implement setting the location next to the given component
     }
     
     /**
      * Writes all the settings of this application.
      * @param element the xml element to write into
      */
     public void writeXML(XElement element)
     {
         control.getResources().writeXML(element.addElement("resources"));
         
         //create a new element to store main frame info in the layout file
         XElement mainFrameElement = element.addElement("mainFrame");
 
         //save the main frame's size to the layout file
         mainFrameElement.addElement("size").addInt("width", this.getSize().width).addInt("height", this.getSize().height);
 
         //save the main frame's location to the layout file
         mainFrameElement.addElement("location").addInt("x", this.getLocationOnScreen().x).addInt("y", this.getLocationOnScreen().y);
     }
     
     /**
      * Reads all the settings of this application.
      * @param element the element to read from
      */
     public void readXML(XElement element)
     {
         control.getResources().readXML(element.getElement("resources"));
         
         if(element.getElement("mainFrame") != null)
         {
             //restore the saved main frame's size from the layout file
             XElement sizeElement = element.getElement("mainFrame").getElement("size");
             Dimension sizeDimension = new Dimension(sizeElement.getInt("width"), sizeElement.getInt("height")); 
             this.setSize(sizeDimension);
             
             //restore the saved main frame's size from the layout file
             XElement locationElement = element.getElement("mainFrame").getElement("location");
             Point locationPoint = new Point(locationElement.getInt("x"), locationElement.getInt("y")); 
             this.setLocation(locationPoint);
         }
     }
     
     public void saveDockingLayoutToFile(String file)
     {
         try{
             XElement element = new XElement("config");
             writeXML(element);
             OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
             XIO.writeUTF(element, out);
             
             //layoutFilepath = file;
         }
         catch( IOException ex ){
             ex.printStackTrace();
         }
     }
     
     public void loadDockingLayoutFromFile(final String file)
     {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
         //read in the persisted layout file with DF
         try{
             InputStream in = new BufferedInputStream( new FileInputStream(file));
             readXML( XIO.readUTF( in ) );
             in.close();
             
             //layoutFilepath = file;
             
             applyDefaultDockingLayout();
         }
         catch( IOException ex ){
             ex.printStackTrace();
         }
             }});
     }
     
     /**
      * Reset the frames to show the built-in layout as determined by the 
      * default frame locations in this class's CLocation member variables. 
      * 
      * This should be the same layout you see upon creating a new simulation.
      * 
      * This method does not save the layout, it only resets the frame's positions.
      * 
      */
     public void resetDockingLayout()
     {
         //close all non-default dockables
         for(SingleCDockable dockable : singleAuxillaryDockables.values())
         {
             if (dockable.isVisible())
             {
                 dockable.setVisible(false);
             }
             control.removeDockable(dockable);
         }        
         
         //close each single dockable
         for(SingleCDockable dockable : singleDockables.values())
         {
             dockable.setVisible(false);
         }
         
         //close each pvd frame
         for(PvdFrame frame : pvds)
         {
             if(frame.getControl() != null)
             {
                 frame.setVisible(false);
             }
         }
         
         //reset the location of each single dockable
         singleDockables.get(ENTITIES_FRAME_KEY).setLocation(defaultEntityListLocation);
         singleDockables.get(ENTITY_PROPERTIES_FRAME_KEY).setLocation(defaultEntityPropertiesLocation);
         singleDockables.get(RADIO_MESSAGES_FRAME_KEY).setLocation(defaultRadioMessagesLocation);
         singleDockables.get(CONSOLE_FRAME_KEY).setLocation(defaultConsoleLocation);
         singleDockables.get(CHEAT_SHEET_FRAME_KEY).setLocation(defaultCheatSheetLocation);
         
         //reset the location of each pvd frame
         for(PvdFrame frame : pvds)
         {
             frame.setLocation(defaultPvdLocation);
         }
         
         //show the first pvd frame
         for(PvdFrame frame : pvds)
         {
             if(frame.title.equals("PVD1"))
             {
                 control.addDockable(frame);
                 frame.setVisible(true);
             }
         }
 
         //show each single dockable
         for(SingleCDockable dockable : singleDockables.values())
         {
             dockable.setVisible(true);
         }
     }
     
     /**
      * Apply the default docking layout from the set that has been loaded.
      * 
      * The default is the one named "default". If a layout named "default" 
      * does not exist, then the first layout in the set will be loaded.
      */
     public void applyDefaultDockingLayout()
     {
         List<String> layouts = Arrays.asList(control.layouts());
         if(layouts.contains("default"))
         {
             control.load("default");
         }
         
         if(layouts.size() > 0)
         {
             control.load(layouts.get(0));
         }
     }
     
     /**
      * @return the cheatSheetView
      */
     public CheatSheetView getCheatSheetView()
     {
         return cheatSheetView;
     }
     
     public PlanViewDisplay createPlanViewDisplay(boolean split)
     {
         return createPvdFrame(null, split).pvd;
     }
 
     private PvdFrame createPvdFrame(String title, boolean split)
     {
         boolean first = nextPvdId == 0;
         
         if(title == null)
         {
             title = "PVD" + ++nextPvdId;
         }
         
         //create the DF dockable for the lan view display
         final PvdFrame pf = new PvdFrame(pvdFactory, title);
         pf.setLocation(defaultPvdLocation);
         pvds.add(pf);
         
         createPlanViewDisplay(pf, title, split, first);
         
         return pf;
     }
     
     private PlanViewDisplay createPlanViewDisplay(PvdFrame pf, String title, boolean split, boolean first)
     {
         pf.pvd = new PlanViewDisplay(services, getActivePlanViewDisplay());
         pf.title = title;
         
         //DF settings
 //        pf.setLayout(new BorderLayout());
         pf.setCloseable(true);
         pf.setMinimizable(false);
         pf.setExternalizable(false);
         pf.setMaximizable(true);
         pf.setTitleText(title);
         pf.setResizeLocked(true);
         pf.setTitleIcon(SimulationImages.PVD);
         
         if(first)
         {
             pf.setCloseable(false);
             
             activePvdFrame = pf;
         }
         
         //add the plan view display to the dockable's content pane
         pf.getContentPane().add(pf.pvd);
         
         //add the dockable to the DF controller
         control.addDockable("__" + pf.title, pf);
         
         if(!first && pvds.get(0).isVisible())
         {
             // make sure the frame shows up in a reasonable place.
             pf.setLocation(defaultPvdLocation);
         }
 
         //set the DF dockable visible
         pf.setVisible(true);
         
         if(!first)
         {
            pf.pvd.showAll();
         }
         
         pf.addFocusListener(new MyFocusListener());
         return pf.pvd;
     }
     
     private void addAction(JMenu menu, Class<?> klass)
     {
         ActionManager am = services.findService(ActionManager.class);
         menu.add(am.getAction(klass.getCanonicalName()));
     }
     
     private JMenuItem createMenuItemFromAction(FreeMenuPiece piece, Class<?> klass)
     {
         ActionManager am = services.findService(ActionManager.class);
         
         //add the action to the menu to create the component, 
         //then remove it from the menu and return it so it can be used
         JMenuItem item = piece.getMenu().add(am.getAction(klass.getCanonicalName()));
         piece.getMenu().remove(item);
         return item;
     }
     
     private void initMenus()
     {
         ActionManager am = services.findService(ActionManager.class);
         
         JMenuBar bar = new JMenuBar();
         
         //create the file menu
         addAction(fileMenu, LoadScenarioAction.class);
         addAction(fileMenu, ExportScenarioAction.class);
         fileMenu.addSeparator();
         addAction(fileMenu, LoadDockingLayoutAction.class);
         addAction(fileMenu, SaveDockingLayoutAction.class);
         fileMenu.addSeparator();
         addAction(fileMenu, ExitAction.class);
         bar.add(fileMenu);
         
         //create the views menu
         initViewsMenu(bar, am);
         
         //create the simulation menu
         JMenu simMenu = new JMenu("Simulation");
         addAction(simMenu, RunAction.class);
         addAction(simMenu, PauseAction.class);
         bar.add(simMenu);
 
         //create the tools menu
         JMenu toolsMenu = new JMenu("Tools");
         addAction(toolsMenu, LoadContainerAction.class);
         addAction(toolsMenu, UnloadContainerAction.class);
         toolsMenu.addSeparator();
         addAction(toolsMenu, AddToPolygonAction.class);
         addAction(toolsMenu, RemoveFromPolygonAction.class);
         toolsMenu.addSeparator();
         addAction(toolsMenu, ToggleCategoryLabelsAction.class);
         bar.add(toolsMenu);
         
         //create the help menu
         JMenu helpMenu = new JMenu("Help");
         helpMenu.add(new AbstractAction("About") {
 
             private static final long serialVersionUID = 486968928363337592L;
 
             public void actionPerformed(ActionEvent arg0)
             {
                 AboutDialog.show(SimulationMainFrame.this);
             } });
         bar.add(helpMenu);
         
         setJMenuBar(bar);
     }
 
     /**
      * @param viewMenu
      */
     private void initViewsMenu(JMenuBar bar, ActionManager am)
     {
         //add components to the piece, then add pieces to the viewMenuRoot
         FreeMenuPiece piece1 = new FreeMenuPiece();
         viewMenuRoot.add(piece1);
         
         piece1.add(createMenuItemFromAction(piece1, ZoomInAction.class));
         piece1.add(createMenuItemFromAction(piece1, ZoomOutAction.class));
         piece1.add(createMenuItemFromAction(piece1, ShowAllAction.class));
         piece1.add(createMenuItemFromAction(piece1, AdjustMapOpacityAction.class));
         
         piece1.add(new JSeparator());
         piece1.add(createMenuItemFromAction(piece1, AddDistanceToolAction.class));
         piece1.add(createMenuItemFromAction(piece1, ClearDistanceToolsAction.class));
         
         JCheckBoxMenuItem showGridMenuItem = new JCheckBoxMenuItem();
         new ShowGridAction(am, showGridMenuItem);
         piece1.add(showGridMenuItem);
         
         JCheckBoxMenuItem lockMenuItem = new JCheckBoxMenuItem();
         new LockViewToEntityAction(am, lockMenuItem);
         piece1.add(lockMenuItem);
         
         piece1.add(new JSeparator());
         
         //add the list of views to show/hide to the view menu
         SingleCDockableListMenuPiece piece2 = new SingleCDockableListMenuPiece(control);
         viewMenuRoot.add(piece2);
         
         //add some more actions to the view menu
         FreeMenuPiece piece3 = new FreeMenuPiece();
         viewMenuRoot.add(piece3);
         piece3.add(new JSeparator());
         piece3.add(createMenuItemFromAction(piece3, NewPlanViewDisplayAction.class));
         piece3.add(new PlanViewDisplayMenu());
         piece3.add(new JSeparator());
         piece3.add(createMenuItemFromAction(piece3, RestoreDefaultLayoutAction.class));
         piece3.add(new JSeparator());
         
         //add the layouts submenu to the view menu
         CLayoutChoiceMenuPiece piece4 = new CLayoutChoiceMenuPiece(control, false);
         SubmenuPiece submenuPiece = new SubmenuPiece(); 
         submenuPiece.getRoot().add(piece4);
         submenuPiece.getMenu().setText("Layouts");
         viewMenuRoot.add(submenuPiece);
         
         //add look and feel submenu to the view menu
         CLookAndFeelMenuPiece piece5 = new CLookAndFeelMenuPiece(control);
         SubmenuPiece submenuPiece2 = new SubmenuPiece(); 
         submenuPiece2.getRoot().add(piece5);
         submenuPiece2.getMenu().setText("LookAndFeel");
         viewMenuRoot.add(submenuPiece2);
         
         //add theme submenu to the view menu
         CThemeMenuPiece piece6 = new CThemeMenuPiece(control);
         SubmenuPiece submenuPiece3 = new SubmenuPiece(); 
         submenuPiece3.getRoot().add(piece6);
         submenuPiece3.getMenu().setText("Theme");
         viewMenuRoot.add(submenuPiece3);
 
         //add the view menu to the bar
         bar.add(viewMenuRoot.getMenu());
     }
     
     public void addViewMenuAction(Class<?> klass)
     {
         FreeMenuPiece piece3 = new FreeMenuPiece();
         viewMenuRoot.add(piece3);
         piece3.add(new JSeparator());
         piece3.add(createMenuItemFromAction(piece3, klass));
     }
     
     private void addAction(JToolBar bar, Class<?> klass)
     {
         ActionManager am = services.findService(ActionManager.class);
         bar.add(am.getAction(klass.getCanonicalName()));
     }
     
     private void initToolbar()
     {
         ActionManager am = services.findService(ActionManager.class);
         
         JToolBar tools = new JToolBar();
         
         tools.setFloatable(false);
         
         addAction(tools, RunAction.class);
         addAction(tools, PauseAction.class);
         
         simTimePanel = new SimulationTimePanel(services);
         tools.add(simTimePanel);
         tools.addSeparator();
         addAction(tools, ZoomInAction.class);
         addAction(tools, ZoomOutAction.class);
         addAction(tools, ShowAllAction.class);
         JToggleButton lockButton = new JToggleButton();
         new LockViewToEntityAction(am, lockButton);
         tools.add(lockButton);
         add(tools, BorderLayout.NORTH);
     }
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.ui.pvd.PlanViewDisplayProvider#getActivePlanViewDisplay()
      */
     public PlanViewDisplay getActivePlanViewDisplay()
     {
         return activePvdFrame != null ? activePvdFrame.pvd : null;
     }
     
     public EntityPropertiesView getPropertiesView()
     {
         return propertiesView;
     }
     
     private class MyFocusListener implements CFocusListener
     {
         @Override
         public void focusGained(CDockable dockable)
         {
             if(dockable instanceof PvdFrame)
             {
                 if(activePvdFrame != null)
                 {
                     activePvdFrame.setTitleText(activePvdFrame.title);
                 }
                 activePvdFrame = (PvdFrame) dockable;
                 // TODO: JCC - Not sure if this is acceptable. The frames library relies on titles though,
                 //             and modifying the title like this causes problems.
                 //activePvdFrame.setTitleText(activePvdFrame.title + " (active)");
                 services.findService(ActionManager.class).updateActions();
             }
         }
 
         @Override
         public void focusLost(CDockable dockable)
         {
             //do nothing for now
         }
         
     }
     
     private class PlanViewDisplayMenu extends JMenu
     {
         private static final long serialVersionUID = -7628240663942850385L;
 
         public PlanViewDisplayMenu()
         {
             super("PVDs");
             
             addMenuListener(new MenuListener() {
 
                 public void menuCanceled(MenuEvent e)
                 {
                 }
 
                 public void menuDeselected(MenuEvent e)
                 {
                 }
 
                 public void menuSelected(MenuEvent e)
                 {
                     populate();
                 }});
         }
         
         private void populate()
         {
             removeAll();
             
             for(final PvdFrame pf : pvds)
             {
                 add(new AbstractAction("Show " + pf.title) {
                     private static final long serialVersionUID = -4858584994300254664L;
 
                     public void actionPerformed(ActionEvent arg0)
                     {
                         for(PvdFrame frame : pvds)
                         {
                             if(frame.getTitleText().equals(pf.title))
                             {
                                 if(!frame.isVisible())
                                 {
                                     control.addDockable("__" + frame.title, frame);
                                     frame.setVisible(true);
                                 }
                                 else
                                 {
                                     frame.toFront();
                                 }
                                 break;
                             }
                         }
                     }
                 });
                 
             }
         }
     }
     
     private class PvdFactory implements MultipleCDockableFactory<PvdFrame, PvdLayout>
     {
         @Override
         public PvdLayout create()
         {
             return new PvdLayout();
         }
 
         @Override
         public boolean match(PvdFrame dockable, PvdLayout layout)
         {
             String name = dockable.getTitleText();
             return name.equals(layout.getName());
         }
 
         @Override
         public PvdFrame read(PvdLayout layout)
         {
             String name = layout.getName();
             
             for(PvdFrame f : pvds)
             {
                 if(f.getTitleText().equals(name))
                 {
                     return f;
                 }
             }
             
             return createPvdFrame(name, false);
         }
 
         @Override
         public PvdLayout write(PvdFrame dockable)
         {
             PvdLayout layout = new PvdLayout();
             layout.setName(dockable.getTitleText());
             return layout;
         }
         
     }
     
     /**
      * Describes the layout of one {@link PvdDockable}
      */
     private static class PvdLayout implements MultipleCDockableLayout
     {
         /** the name of the pvd */
         private String name;
         
         /**
          * Sets the name of the pvd that is shown.
          * @param name the name of the pvd
          */
         public void setName( String name ) {
             this.name = name;
         }
         
         /**
          * Gets the name of the pvd that is shown.
          * @return the name
          */
         public String getName() {
             return name;
         }
 
         public void readStream( DataInputStream in ) throws IOException 
         {
             //do nothing. this method is for binary layout files
 //            name = in.readUTF();
         }
 
         public void readXML( XElement element ) {
             name = element.getString();
         }
 
         public void writeStream( DataOutputStream out ) throws IOException
         {
             //do nothing. this method is for binary layout files
 //            out.writeUTF( name );
         }
 
         public void writeXML( XElement element ) {
             element.setString( name );
         }
     }
     
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.SimulationService#shutdown()
      */
     public void shutdown() throws SimulationException
     {
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.SimulationService#start()
      */
     public void start(ProgressMonitor progress) throws SimulationException
     {
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
      */
     public Object getAdapter(Class<?> klass)
     {
         if (klass == SimulationTimePanel.class && simTimePanel != null)
         {
             return simTimePanel;
         }
         return Adaptables.adaptUnchecked(this, klass, false);
     }
 
     public CControl getControl()
     {
         return control;
     }
     
     public void putDockable(String key, SingleCDockable value)
     {
         singleDockables.put(key, value);
     }
     
 }
