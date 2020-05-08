 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nodes;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import controlP5.Button;
 
 import controlP5.CallbackEvent;
 import controlP5.CallbackListener;
 import controlP5.ColorPicker;
 import controlP5.ControlEvent;
 import controlP5.ControlP5;
 import controlP5.Group;
 import controlP5.ListBox;
 import controlP5.RadioButton;
 import controlP5.Slider;
 import controlP5.Tab;
 import controlP5.Textfield;
 import controlP5.Toggle;
 
 import processing.core.PVector;
 import processing.core.PApplet;
 
 import java.util.ArrayList;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
import java.util.HashSet;
 import java.util.concurrent.atomic.AtomicBoolean;
 import nodes.Graph.GraphIterator;
 
 
 /**
  *
  * @author kdbanman
  */
 public class ControlPanel extends PApplet implements Selection.SelectionListener {
     int w, h;
     
     ControlP5 cp5;
     Graph graph;
     
     // update flag raised if the controllers have not responded to a change in
     // selection.  see selectionChanged() and draw().
     AtomicBoolean selectionUpdated;
     
     // for copy/paste by keyboard
     Clipboard clipboard;
     
     // control element size parameters
     int tabHeight;
     int padding;
     int elementHeight;
     int labelledElementHeight;
     int buttonWidth;
     int buttonHeight;
     int modifiersBoxHeight;
     
     // Control elements that need to be accessed outside of setup
     
     // copy/paste menu
     Button copyButton;
     Button pasteButton;
     Button clearButton;
     
     // tab and subtab containing autolayout so that it can be stopped if the
     // user leaves either of those tabs
     Tab transformTab;
     Group positionGroup;
     
     // subtab lists and open tab reference to be manipulated in draw() so that
     // they behave as tabs
     ArrayList<Group> importSubTabs;
     Group openImportSubTab;
     ArrayList<Group> transformSubTabs;
     Group openTransformSubTab;
     
     // text field for user-input URIs for description retrieval from the web
     Textfield importWebURI;
     
     // selection modifier menu and populator
     ListBox modifierMenu;
     ModifierPopulator modifierPopulator;
     
     // radio for radial layout sorting order (lexico or numerical)
     RadioButton sortOrder;
     
     // radio for position scaling direction (expansion or contraction)
     RadioButton scaleDirection;
     
     // toggle button for laying out the entire graph (force-directed algorithm)
     Toggle autoLayout;
     
     // controller group for colorizing selected nodes and edges
     ColorPicker colorPicker;
     // since colorpicker is a controlgroup, and its events can't be handled with
     // the listener architecture like *everything* else, this flag is necessary
     // so that changes in colorpicker as it reacts to selection changes do not
     // propagate to element color changes.
     boolean changeElementColor;
     
     Slider sizeSlider;
     
     Slider labelSizeSlider;
     
     public ControlPanel(int frameWidth, int frameHeight, Graph parentGraph) {
         w = frameWidth;
         h = frameHeight;
         
         // initialize graph
         graph = parentGraph;
         
         selectionUpdated = new AtomicBoolean();
         
         // for copy/paste
         clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         
         // element size parameters
         padding = 10;
         tabHeight = 30;
         elementHeight = 20;
         labelledElementHeight = 40;
         buttonWidth = 100;
         buttonHeight = 30;
         modifiersBoxHeight = 200;
         
         // selection modifier menu populator
         modifierPopulator = new ModifierPopulator(graph);
         
         // sub tab lists to manipulate in draw() for tab behaviour
         importSubTabs = new ArrayList<>();
         transformSubTabs = new ArrayList<>();
     }
     
     @Override
     public void setup() {
         // subscribe to changes in selection.  see overridden selectionChanged()
         graph.selection.addListener(this);
         
         size(w, h);
         
         cp5 = new ControlP5(this)
                 .setMoveable(false);
         
         // define main controller tabs
         
         Tab importTab = cp5.addTab("Load Triples")
                 .setWidth(w / 4)
                 .setHeight(tabHeight)
                 .setActive(true);
         // transformTab is defined externally so that autolayout can be stopped
         // if the tab is left by the user
         transformTab = cp5.addTab("Transform")
                 .setWidth(w / 4)
                 .setHeight(tabHeight);
         Tab optionTab = cp5.addTab("Options")
                 .setWidth(w / 4)
                 .setHeight(tabHeight);
         Tab saveTab = cp5.addTab("Save")
                 .setWidth(w / 4)
                 .setHeight(tabHeight);
         cp5.getDefaultTab().remove();
         
         // copy/paste 'menu'
         copyButton = cp5.addButton("Copy to Clipboard")
                 .setWidth(buttonWidth)
                 .setHeight(elementHeight)
                 .setVisible(false)
                 .addCallback(new CopyListener());
         pasteButton = cp5.addButton("Paste from Clipboard")
                 .setWidth(buttonWidth)
                 .setHeight(elementHeight)
                 .setVisible(false)
                 .addCallback(new PasteListener());
         clearButton = cp5.addButton("Clear Field")
                 .setWidth(buttonWidth)
                 .setHeight(elementHeight)
                 .setVisible(false)
                 .addCallback(new ClearListener());
         
         //===========
         // Import tab
         //===========
         
         // triple import subtabs
         ////////////////////////
         
         int importTabsVert = 2 * tabHeight + padding;
         
         Group webGroup = new SubTab(cp5, "Web")
                 .setBarHeight(tabHeight)
                 .setPosition(0, importTabsVert)
                 .setWidth(w / 4)
                 .hideArrow()
                 .setOpen(true)
                 .moveTo(importTab);
         Group virtuosoGroup = new SubTab(cp5, "Virtuoso")
                 .setBarHeight(tabHeight)
                 .setPosition(w / 4, importTabsVert)
                 .setWidth(w / 4)
                 .hideArrow()
                 .setOpen(false)
                 .moveTo(importTab);
         Group exploreGroup = new SubTab(cp5, "Explore")
                 .setBarHeight(tabHeight)
                 .setPosition(w / 2, importTabsVert)
                 .setWidth(w / 4)
                 .hideArrow()
                 .setOpen(false)
                 .moveTo(importTab);
         // register triple import subtabs so that they may be manipulated in
         // draw() to behave as tabs
         importSubTabs.add(webGroup);
         importSubTabs.add(virtuosoGroup);
         importSubTabs.add(exploreGroup);
         openImportSubTab = webGroup;
         
         // Web import elements
         
         importWebURI = cp5.addTextfield("URI",
                 padding,
                 padding,
                 w - 2 * padding,
                 elementHeight)
                 .setAutoClear(false)
                 .moveTo(webGroup)
                 .setText("http://www.w3.org/1999/02/22-rdf-syntax-ns")
                 .addCallback(new CopyPasteMenuListener());
         cp5.addButton("Query Web")
                 .setSize(buttonWidth, buttonHeight)
                 .setPosition(w - buttonWidth - padding, 
                     labelledElementHeight + padding)
                 .moveTo(webGroup)
                 .addCallback(new QueryWebListener());
         
         // Virtuoso import elements
         
         cp5.addTextfield("IP:Port", 
                     padding - w / 4, 
                     padding, 
                     w - 2 * padding, 
                     elementHeight)
                 .setAutoClear(false)
                 .addCallback(new CopyPasteMenuListener())
                 .moveTo(virtuosoGroup);
         cp5.addTextfield("Username", 
                     padding - w / 4, 
                     labelledElementHeight + padding, 
                     w - 2 * padding, 
                     elementHeight)
                 .setAutoClear(false)
                 .addCallback(new CopyPasteMenuListener())
                 .moveTo(virtuosoGroup);
         cp5.addTextfield("Password", 
                     padding - w / 4, 
                     2 * labelledElementHeight + padding, 
                     w - 2 * padding, 
                     elementHeight)
                 .setAutoClear(false)
                 .setPasswordMode(true)
                 .addCallback(new CopyPasteMenuListener())
                 .moveTo(virtuosoGroup);
         cp5.addTextfield("Query", 
                     padding - w / 4, 
                     3 * labelledElementHeight + padding, 
                     w - 2 * padding, 
                     elementHeight)
                 .setAutoClear(false)
                 .addCallback(new CopyPasteMenuListener())
                 .moveTo(virtuosoGroup);
         
         cp5.addButton("Query Virtuoso")
                 .setSize(buttonWidth, buttonHeight)
                 .setPosition(w - buttonWidth - padding - w / 4, 
                     4 * labelledElementHeight + padding)
                 .moveTo(virtuosoGroup);
         
         // Explore tab elements
         
         cp5.addRadioButton("Source Choice")
                 .setPosition(padding - w / 2, padding)
                 .setItemHeight(elementHeight)
                 .setItemWidth(elementHeight)
                 .addItem("Query linked data web", 0)
                 .addItem("Query virtuoso server", 1)
                 .activate(0)
                 .moveTo(exploreGroup);
         
         //==============
         // Transform tab
         //==============
         
         // selection modifier menu
         modifierMenu = cp5.addListBox("Selection Modifiers", 
                     padding, 
                     tabHeight + padding, 
                     w - 2 * padding, 
                     modifiersBoxHeight)
                 .setBarHeight(tabHeight)
                 .setItemHeight(elementHeight)
                 .setScrollbarWidth(elementHeight)
                 .moveTo(transformTab)
                 .hideBar();
         // populate menu according to selection
         modifierPopulator.populate(modifierMenu, graph.selection);
         
         // Transformation subtabs
         /////////////////////////
         
         // vertical positiion of transformation subtabs
         int transformTabsVert = modifiersBoxHeight + 3 * tabHeight + padding;
         
         // positionGroup is defined externally so that autolayout can be stopped
         // if the tab is left by the user
         positionGroup = new SubTab(cp5, "Layout")
                 .setBarHeight(tabHeight)
                 .setPosition(0, transformTabsVert)
                 .setWidth(w / 4)
                 .hideArrow()
                 .setOpen(true)
                 .moveTo(transformTab);
         Group colorSizeGroup = new SubTab(cp5, "Color and Size")
                 .setBarHeight(tabHeight)
                 .setPosition(w / 4, transformTabsVert)
                 .setWidth(w / 4)
                 .hideArrow()
                 .setOpen(false)
                 .moveTo(transformTab);
         Group labelGroup = new SubTab(cp5, "Label")
                 .setBarHeight(tabHeight)
                 .setPosition(w / 2, transformTabsVert)
                 .setWidth(w / 4)
                 .hideArrow()
                 .setOpen(false)
                 .moveTo(transformTab);
         Group hideGroup = new SubTab(cp5, "Delete")
                 .setBarHeight(tabHeight)
                 .setPosition(3 * (w / 4), transformTabsVert)
                 .setWidth(w / 4)
                 .hideArrow()
                 .setOpen(false)
                 .moveTo(transformTab);
         
         // register transformation subtabs so that they may be manipulated in
         // draw() to behave as tabs
         transformSubTabs.add(positionGroup);
         transformSubTabs.add(colorSizeGroup);
         transformSubTabs.add(labelGroup);
         transformSubTabs.add(hideGroup);
         openTransformSubTab = positionGroup;
         
         // Layout controllers
         
         cp5.addButton("Scale Positions")
                 .setPosition(padding, padding)
                 .setHeight(buttonHeight)
                 .setWidth(buttonWidth)
                 .moveTo(positionGroup)
                 .addCallback(new ScaleLayoutListener());
         scaleDirection = cp5.addRadio("Scale Direction")
                 .setPosition(2 * padding + buttonWidth, padding)
                 .setItemHeight(buttonHeight / 2)
                 .moveTo(positionGroup)
                 .addItem("Expand from center", 0)
                 .addItem("Contract from center", 1)
                 .activate(0);
         
         cp5.addButton("Radial Sort")
                 .setPosition(padding, 2 * padding + buttonHeight)
                 .setHeight(buttonHeight)
                 .setWidth(buttonWidth)
                 .moveTo(positionGroup)
                 .addCallback(new RadialLayoutListener());
         sortOrder = cp5.addRadio("Sort Order")
                 .setPosition(2 * padding + buttonWidth, 2 * padding + buttonHeight)
                 .setItemHeight(buttonHeight / 2)
                 .moveTo(positionGroup)
                 .addItem("Numerical Order", 0)
                 .addItem("Alphabetical Order", 1)
                 .activate(0);
         
         autoLayout = cp5.addToggle("Autolayout Entire Graph")
                 .setPosition(padding, 4 * padding + 3 * buttonHeight)
                 .setHeight(elementHeight)
                 .setWidth(buttonWidth)
                 .moveTo(positionGroup);
         
         cp5.addButton("Center Camera")
                 .setPosition(width - buttonWidth - padding, 4 * padding + 3 * buttonHeight)
                 .setHeight(buttonHeight)
                 .setWidth(buttonWidth)
                 .moveTo(positionGroup)
                 .addCallback(new CenterCameraListener());
         
         // color and size controllers
         
         //NOTE:  ColorPicker is a ControlGroup, not a Controller, so I can't 
         //       attach a callback to it.  It's functionality is in the 
         //       controlEvent() function of the ControlPanel
         colorPicker = cp5.addColorPicker("Color")
                 .setPosition(-(w / 4) + padding, padding)
                 .moveTo(colorSizeGroup);
         changeElementColor = true;
         
         sizeSlider = cp5.addSlider("Size")
                 .setPosition(-(w / 4) + padding, 2 * padding + 80)
                 .setHeight(elementHeight)
                 .setWidth(w - 80)
                 .setRange(5, 100)
                 .setValue(10)
                 .moveTo(colorSizeGroup)
                 .addCallback(new ElementSizeListener());
         
         // label controllers
         
         cp5.addButton("Show Labels")
                 .setPosition(padding - w / 2, padding)
                 .setHeight(buttonHeight)
                 .setWidth(buttonWidth)
                 .moveTo(labelGroup)
                 .addCallback(new ShowLabelListener());
         
         cp5.addButton("Hide Labels")
                 .setPosition(padding - w / 2, buttonHeight + 2 * padding)
                 .setHeight(buttonHeight)
                 .setWidth(buttonWidth)
                 .moveTo(labelGroup)
                 .addCallback(new HideLabelListener());
         
         labelSizeSlider = cp5.addSlider("Label Size")
                 .setPosition(padding - w / 2, padding * 3 + 2 * buttonHeight)
                 .setWidth(w - 80)
                 .setHeight(elementHeight)
                 .setRange(5, 100)
                 .setValue(10)
                 .moveTo(labelGroup)
                 .addCallback(new LabelSizeListener());
         
         // visibility controllers
         
         cp5.addButton("Delete Selection")
                 .setPosition(padding - 3 * (w / 4), padding)
                 .setSize(buttonWidth, buttonHeight)
                 .moveTo(hideGroup)
                 .addCallback(new ElementRemovalListener());
         
         // change color picker, size slider, and label size slider to reflect selection
         updateControllersToSelection();
         
         //============
         // Options tab
         //============
         
         //=========
         // Save tab
         //=========
     }
     
     // all controlP5 controllers are drawn after draw(), so herein lies any
     // arbiter-style controller logic, as well as miscellaneous actions that 
     // must occur every frame.
     @Override
     public void draw() {
         
         // make subTabs perform as tabs instead of Groups
         
         for (Group subTab : transformSubTabs) {
           if (subTab.isOpen() && subTab != openTransformSubTab) {
             openTransformSubTab.setOpen(false);
             openTransformSubTab = subTab;
           }
         }
         for (Group subTab : importSubTabs) {
             if (subTab.isOpen() && subTab != openImportSubTab) {
                 openImportSubTab.setOpen(false);
                 openImportSubTab = subTab;
             }
         }
         
         // stop autoLayout if any other tab is selected
         if (autoLayout.getState() && (!transformTab.isOpen() || !positionGroup.isOpen())) {
             autoLayout.setState(false);
         }
         
         // update controllers to selection if selection has changed since
         // last draw() call
         if (selectionUpdated.getAndSet(false)) {
             // populate the dynamic, selection-dependent selection modifier menu
             modifierPopulator.populate(modifierMenu, graph.selection);
 
             // change color picker, size slider, and label size slider to reflect selection
             updateControllersToSelection();
         }
         
          background(0);
     }
     
     // every time selection is changed, this is called
     @Override
     public void selectionChanged() {
         // queue controller selection update if one is not already queued
         selectionUpdated.compareAndSet(false, true);
     }
     
     // called every time cp5 broadcasts an event.  since ControlGroups cannot
     // have specific listeners, their actions must be dealt with here.
     public void controlEvent(ControlEvent event) {
         // adjust color of selected elements (if event is not from selection update)
         if (changeElementColor && event.isFrom(colorPicker)) {
             int newColor = colorPicker.getColorValue();
             for (GraphElement e : graph.selection) {
                 e.setColor(newColor);
             }
         } else if (event.isFrom(modifierMenu)) {
             modifierPopulator.run((int) event.getValue());
         }
     }
     
     // called whenever the mouse is released within the control panel
     @Override
     public void mouseReleased() {
         //clear the copy paste menu if the left mouse button is clicked outside
         // of any part of the menu
         if (!(copyButton.isInside()
                 || pasteButton.isInside()
                 || clearButton.isInside())
                 && mouseButton == LEFT) {
             copyButton.setVisible(false);
             pasteButton.setVisible(false);
             clearButton.setVisible(false);
         }
     }
     
     public void updateControllersToSelection() {
         // do not change element colors with the resulting ControlEvent here
         changeElementColor = false;
         colorPicker.setColorValue(graph.selection.getColor());
         changeElementColor = true;
         
         sizeSlider.setValue(graph.selection.getSize());
         labelSizeSlider.setValue(graph.selection.getLabelSize());
     }
     
     /*
      * ControlP5 does not support nesting tabs within other tabs, so this is an
      * extension of controller Groups to behave as nested tabs.  NOTE:  the
      * tab-behaviour control logic is maintained for each group of tabs
      * by a list of each member of the group and a reference to whichever tab is
      * currently open in the group.  these are defined as fields in ControlPanel
      * and are manipulated within draw().
      */
     private class SubTab extends Group {
       
         SubTab(ControlP5 theControlP5, String theName) {
             // call Group constructor
             super(theControlP5, theName);
         }
       
         @Override
         protected void postDraw(PApplet theApplet) {
             // draw the group with the color behaviour of a Tab instead of a Group
             if (isBarVisible) {
                 theApplet.fill(isOpen ? color.getActive() : 
                         (isInside ? color.getForeground() : color.getBackground()));
                 theApplet.rect(0, -1, _myWidth - 1, -_myHeight);
                 _myLabel.draw(theApplet, 0, -_myHeight-1, this);
                 if (isCollapse && isArrowVisible) {
                     theApplet.fill(_myLabel.getColor());
                     theApplet.pushMatrix();
 
                     if (isOpen) {
                             theApplet.triangle(_myWidth - 10, -_myHeight / 2 - 3, _myWidth - 4, -_myHeight / 2 - 3, _myWidth - 7, -_myHeight / 2);
                     } else {
                             theApplet.triangle(_myWidth - 10, -_myHeight / 2, _myWidth - 4, -_myHeight / 2, _myWidth - 7, -_myHeight / 2 - 3);
                     }
                     theApplet.popMatrix();
                 }
             }
         }
     }
     
     /*****************************
      * Import controller listeners
      ****************************/
     
     /*
      * attach to a Textfield for copy/paste dropdown menu on right click.
      * each textfield needs its own unique instance (one controller per listener
      * is a controlP5 thing (and maybe even a general thing).  technically this
      * risks concurrent modification of the copy and paste buttons, but the odds
      * of near-simultaneous right-clicks on textfields is low.
      */
     private class CopyPasteMenuListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_PRESSED 
                     && mouseButton == RIGHT) {
                 // textfields occur in multiple tabs, so the menu must be moved
                 // to the corret tab
                 Tab activeTab = event.getController().getTab();
                 
                 // move each button sequentially below the cursor
                 copyButton.setPosition(mouseX, mouseY)
                         .setVisible(true)
                         .moveTo(activeTab)
                         .bringToFront();
                 pasteButton.setPosition(mouseX, mouseY + elementHeight)
                         .setVisible(true)
                         .moveTo(activeTab)
                         .bringToFront();
                 clearButton.setPosition(mouseX, mouseY + 2 * elementHeight)
                         .setVisible(true)
                         .moveTo(activeTab)
                         .bringToFront();
             }
         }
     }
     
     /*
      * attach to button for copying from active textfield
      */
     private class CopyListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // get active text field
                 for (Textfield c : cp5.getAll(Textfield.class)) {
                     if (c.isActive()) {
                         // get text field contents and copy to clipboard
                         String fieldContents = c.getText();
                         StringSelection data = new StringSelection(fieldContents);
                         // data is passed as both parameters because of an
                         // unimplemented feature in AWT
                         clipboard.setContents(data, data);
                     }
                 }
             }
         }
     }
     
     /*
      * attach to button for pasting to active textfield
      */
     private class PasteListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // get active text field
                 for (Textfield c : cp5.getAll(Textfield.class)) {
                     if (c.isActive()) {
                         // separate current textfield contents about cursor position
                         int idx = c.getIndex();
                         String before = c.getText().substring(0, idx);
                         String after = "";
                         if (c.getIndex() != c.getText().length()) {
                             after = c.getText().substring(idx, c.getText().length());
                         }
 
                         // get (valid) clipboard contents and insert at cursor position
                         Transferable clipData = clipboard.getContents(this);
                         String s = "";
                         try {
                           s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
                         } catch (UnsupportedFlavorException | IOException ee) {
                             System.out.println("Cannot paste clipboard contents.");
                         }
                         c.setText(before + s + after);
                     }
                 }
             }
         }
     }
     
     /*
      * attach to button for clearing active text field
      */
     private class ClearListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // get active text field
                 for (Textfield c : cp5.getAll(Textfield.class)) {
                     if (c.isActive()) {
                         c.clear();
                     }
                 }
             }
         }
     }
     
     /*
      * attach to web query button in import tab to enable retrieval of rdf
      * descriptions as published at resources' uris.
      */
     private class QueryWebListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // get uri from text field
                 String uri = importWebURI.getText();
                 // retrieve description as a jena model
                 Model toAdd = Importer.getDescriptionFromWeb(uri);
                 // add the retriveed model to the graph (toAdd is empty if 
                 // an error was encountered)
                 graph.addTriples(toAdd);
             }
         }
     }
     
     /*************************************
      * Transformation controller listeners
      ************************************/
     
     /*
      * attach to layout scale button to enable expansion/contraction of the positions
      * of each node in the selection about their average center.
      */
     private class ScaleLayoutListener implements CallbackListener {
 
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // calculate center of current selection of nodes
                 PVector center = new PVector();
                 for (Node n : graph.selection.getNodes()) {
                     center.add(n.getPosition());
                 }
                 center.x =  center.x / graph.selection.nodeCount();
                 center.y =  center.y / graph.selection.nodeCount();
                 center.z =  center.z / graph.selection.nodeCount();
                 
                 // set scale based on user selection.
                 // index 0 == expand, 1 == contract
                 float scale;
                 if (scaleDirection.getState(0)) scale = -0.2f;
                 else scale = 0.2f;
                 
                 // scale each node position outward or inward from center
                 for (Node n : graph.selection.getNodes()) {
                     n.getPosition().lerp(center, scale);
                 }
             }
         }
     }
     
     /*
      * attach to button that centers PApplet camera on center of graph at a 
      * heuristically calculated distance to contain most of the graph
      */
     private class CenterCameraListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 
                 GraphIterator it = graph.iterator();
                 if (it.hasNext()) {
                     GraphElement first = it.next();
                     // calculate center of graph
                     PVector center = first.getPosition().get();
                     float minX = center.x;
                     float maxX = center.x;
                     float minY = center.y;
                     float maxY = center.y;
                     float minZ = center.z;
                     float maxZ = center.z;
 
                     for (Node n : graph.getNodes()) {
                         PVector nPos = n.getPosition();
 
                         center.add(n.getPosition());
                         
                         minX = Nodes.min(minX, nPos.x);
                         maxX = Nodes.max(maxX, nPos.x);
                         minY = Nodes.min(minY, nPos.y);
                         maxY = Nodes.max(maxY, nPos.y);
                         minZ = Nodes.min(minZ, nPos.z);
                         maxZ = Nodes.max(maxZ, nPos.z);
                     }
                     center.x =  center.x / graph.nodeCount();
                     center.y =  center.y / graph.nodeCount();
                     center.z =  center.z / graph.nodeCount();
 
                     float avgDist = (maxX - minX + maxY - minY + maxZ - minZ) / 3;
                     // set camera
                     graph.pApp.cam.lookAt(center.x, center.y, center.z);
                     graph.pApp.cam.setDistance(avgDist);
                 }
             }
         }
     }
     
     /*
      * attach to radial sort button.  selected nodes are laid out in a circle
      * whose axis is perpendicular to the screen and whose radius is chosen to
      * accomodate all the nodes with a bit of room to spare.  the nodes will 
      * be ordered along the circumference lexicographically or numerically, 
      * depending on the state of a separate radio button called sortOrder.
      */
     private class RadialLayoutListener implements CallbackListener {
 
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // get array of names of selected nodes (graph.selection stores
                 // things as HashSets, which are not sortable)
                 String[] names = new String[graph.selection.nodeCount()];
                 int i = 0;
                 for (Node n : graph.selection.getNodes()) {
                     names[i] = n.getName();
                     i++;
                 }
                 
                 // sort the array of names according to the user choice
                 // index 0 == numerical, 1 == lexicographical
                 if (sortOrder.getState(0)) {
                     quickSort(names, 0, names.length - 1, true);
                 } else {
                     // lexicographical sort implicit
                     quickSort(names, 0, names.length - 1, false);
                 }
                 
                 // calculate radius of circle from number and size of nodes
                 // along with the midpoint of the nodes
                 PVector center = new PVector();
                 float maxSize = 0;
                 for (Node n : graph.selection.getNodes()) {
                     center.add(n.getPosition());
                     maxSize = Nodes.max(maxSize, n.getSize());
                 }
                 // radius is circumference / 2pi, but this has been adjusted for appearance
                 float radius = (float) ((float) graph.selection.nodeCount() * 2 * maxSize) / (Nodes.PI);
                 
                 // center is average position
                 center.x =  center.x / graph.selection.nodeCount();
                 center.y =  center.y / graph.selection.nodeCount();
                 center.z =  center.z / graph.selection.nodeCount();
                 
                 // get horizontal and vertical unit vectors w.r.t. screen
                 PVector horiz = graph.proj.getScreenHoriz();
                 //upper left corner
                 graph.proj.calculatePickPoints(0, 0);
                 PVector vert = graph.proj.getScreenVert();
                 
                 // angular separation of nodes is 2pi / number of nodes
                 float theta = 2 * Nodes.PI / (float) graph.selection.nodeCount();
                 float currAngle = 0;
                 
                 // lay out the selected nodes in the new order in a circle
                 // whose axis is orthogonal to the screen
                 for (String name : names) {
                     Node n = graph.getNode(name);
                     
                     PVector hComp = horiz.get();
                     hComp.mult(Nodes.cos(currAngle) * radius);
                     
                     PVector vComp = vert.get();
                     vComp.mult(Nodes.sin(currAngle) * radius);
                     
                     hComp.add(vComp);
                     n.setPosition(hComp);
                     
                     currAngle += theta;
                 }
             }
         }
         
         // standard quicksort implementation with a boolean parameter to 
         // indicate numerical or lexicographical ordering of the strings
         private void quickSort(String[] arr, int p, int r, boolean numerical) {
             if (p < r) {
                 int pivot = partition(arr, p, r, numerical);
                 quickSort(arr, p, pivot - 1, numerical);
                 quickSort(arr, pivot + 1, r, numerical);
             }
         }
         
         // quicksort partition function with choice of sort order
         private int partition(String[] arr, int p, int r, boolean numerical) {
             String pivot = arr[r];
             int swap = p - 1;
             for (int j = p ; j < r ; j++) {
                 boolean greaterThanPivot;
                 if (numerical) greaterThanPivot = numLess(arr[j], pivot);
                 else greaterThanPivot = lexLess(arr[j], pivot);
                 
                 if (greaterThanPivot) {
                     swap++;
                     String tmp = arr[swap];
                     arr[swap] = arr[j];
                     arr[j] = tmp;
                 }
             }
             String tmp = arr[swap + 1];
             arr[swap + 1] = arr[r];
             arr[r] = tmp;
             
             return swap + 1;
         }
         
         // numerical string comparison
         private boolean numLess(String left, String right) {
             if (left.length() > right.length()) {
                 return false;
             } else if (left.length() < right.length()) {
                 return true;
             }
             
             return lexLess(left, right);
         }
         
         // lexicographical string comparison
         private boolean lexLess(String left, String right) {
             return left.compareToIgnoreCase(right) < 0;
         }
     }
     
     /*
      * attach to element size slider to enable node/edge size manipulation
      */
     private class ElementSizeListener implements CallbackListener {
 
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED
                     || event.getAction() == ControlP5.ACTION_RELEASEDOUTSIDE) {
                 // get the size from the slider control
                 int newSize = 10;
                 try {
                     newSize = (int) ((Slider) event.getController()).getValue();
                 } catch (Exception e) {
                     System.out.println("ERROR:  ElementSizeListener not hooked up to a Slider.");
                 }
                 
                 // apply the new size to each element in the selection
                 for (GraphElement e : graph.selection) {
                     e.setSize(newSize);
                 }
             }
         }
     }
     
     /*
      * attach to hide label button to enable label-showing control
      */
     private class HideLabelListener implements CallbackListener {
 
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // hide label for each element in the selection
                 for (GraphElement e : graph.selection) {
                     e.setDisplayLabel(false);
                 }
             }
         }
     }
     
     /*
      * attach to show label button to enable label-hiding control
      */
     private class ShowLabelListener implements CallbackListener {
 
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 // show each label for each element in the selection
                 for (GraphElement e : graph.selection) {
                     e.setDisplayLabel(true);
                 }
             }
         }
     }
     
     /*
      * attach to label size slider to enable label size control
      */
     private class LabelSizeListener implements CallbackListener {
 
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED
                     || event.getAction() == ControlP5.ACTION_RELEASEDOUTSIDE) {
                 int newSize = 10;
                 // get the new size from the slider control
                 try {
                     newSize = (int) ((Slider) event.getController()).getValue();
                 } catch (Exception e) {
                     System.out.println("ERROR:  LabelSizeListener not hooked up to a Slider.");
                 }
                 
                 // apply the new label size to each element in the selection
                 for (GraphElement e : graph.selection) {
                     e.setLabelSize(newSize);
                 }
             }
         }
     }
     
     /*
      * attach to element removal button to enable removal of any subset of
      * the graph's nodes or edges
      */
     private class ElementRemovalListener implements CallbackListener {
         
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                // for both removal loops below, deep copies of the selected
                // nodes and edges are created.  the removal calls still work
                // because the Graph.remove*(GraphElement) methods are wrappers
                // for Graph.remove*(String) methods (only names matter)
                HashSet<Node> nodesCopy = new HashSet<>(graph.selection.getNodes());
                HashSet<Edge> edgesCopy = new HashSet<>(graph.selection.getEdges());
                
                
                 // remove all nodes in the selection (this will remove all
                 // connected edges
                for (Node n : nodesCopy) {
                     graph.removeNode(n);
                 }
                 // remove all remaining edges in the selection
                for (Edge e : edgesCopy) {
                     graph.removeEdge(e);
                 }
             }
         }
     }
 }
