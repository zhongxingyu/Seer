 // VizMapAttrTab.java
 //--------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------------
 package cytoscape.visual.ui;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import cytoscape.visual.*;
 import cytoscape.visual.calculators.*;
 import cytoscape.visual.mappings.ObjectMapping;
 import y.view.LineType;
 import y.view.Arrow;
 import java.util.*;
 import java.lang.reflect.Constructor;
 import cytoscape.dialogs.GridBagGroup;
 import cytoscape.dialogs.MiscGB;
 /**
  *  VizMapAttrTab implements a tab for each mappable attribute of the graph except
  *  the size tab, which is a special case.
  *  These tabs are displayed in the Set Visual Properties dialog.
  */
 public class VizMapAttrTab extends VizMapTab {
     /**
      *	all calculators known to the CalculatorCatalog of this type
      */
     private Collection calculators;
 
     /**
      *	reference to calculator catalog
      */
     private CalculatorCatalog catalog;
 
     /**
      *	the calculator whose UI is being displayed by this tab
      */
     private Calculator currentCalculator;
 
     /**
      *	default object to display
      */
     private Object defaultObj;
 
     /**
      *	underlying network
      */
     private Network n;
 
     /**	the panel containing calculator-specific UI and provided
      *	by the currently selected calculator
      */
     private JPanel calcPanel;
 
     /**
      *	the parent JDialog
      */
     private VizMapUI mainUIDialog;
 
     /**
      *	the type of this VizMapAttrTab
      */
     private byte type;
 
     /** VisualMappingManager for the window */
     private VisualMappingManager VMM;
 
     /** Node apperance calculator reference */
     private NodeAppearanceCalculator nodeCalc;
     /** Edge appearance calculator reference */
     private EdgeAppearanceCalculator edgeCalc;
 
     /** Default ValueDisplayer */
     private ValueDisplayer defaultValueDisplayer;
 
     /** Combo box for calculator selection */
     private JComboBox calcComboBox;
 
     /** Calculator UI */
     private GridBagGroup mapPanelGBG;
     private JPanel calcContainer;
 
     /** Icon for upper left corner of tab */
     private ImageIcon imageIcon;
 
     /**
      *	create a new tab representing the underlying type. Retrieve current
      *	calculator and default settings from the VMM.
      *
      *	@param	VMM	VisualMappingManager for the window
      *	@param	n	Underlying network
      *	@param	type	One of types defined in {@link VisualMappingManager}
      *	@param	c	
      */
     public VizMapAttrTab (VizMapUI mainUI, VisualMappingManager VMM, byte type) {
 	super(new BorderLayout(), false);
 	
 	// set the name of this component appropriately
 	setName(getTypeName(type));
 
 	this.VMM = VMM;
 	this.mainUIDialog = mainUI;
 	this.catalog = VMM.getCalculatorCatalog();
 	this.n = VMM.getNetwork();
 	this.type = type;
 
 	// get appearance calculator references
 	this.nodeCalc = VMM.getNodeAppearanceCalculator();
 	this.edgeCalc = VMM.getEdgeAppearanceCalculator();
 	
 	// register to listen for changes in the catalog
 	catalog.addChangeListener(new CatalogListener(), this.type);
 
 	// get current default (currently selected) calculator/color from VMM
 	getDefaults(type);
 
 	// draw default portion of the tab
 	drawDefault();
 
 	// draw calculator manipulation portion of the tab
 	drawCalc();
     }
 
     private void setupCalcComboBox() {
 	/* build the list of known calculators - each calculator has a toString()
 	   method that returns the name, so calculators can be passed to the
 	   JComboBox.
 	*/
 	Object comboArray[] = new Object[calculators.size() + 1];
 	comboArray[0] = new String("None");
 	Iterator calcIter = calculators.iterator();
 	for (int i = 1; calcIter.hasNext(); i++) {
 	    comboArray[i] = calcIter.next();
 	}
 	this.calcComboBox = new JComboBox(comboArray);
 	
 	// attach listener
 	this.calcComboBox.addItemListener(new calcComboSelectionListener());
 
 	// set the currently selected calculator
 	if (this.currentCalculator == null) {
 	    /* Index 0 is always the "None" string. However, setSelectedIndex(0) does not call
 	       event handlers. Thus, in RmCalcListener, switchCalculator() is called explicitly.
 	    */
 	    this.calcComboBox.setSelectedIndex(0);
 	}
 	else
 	    this.calcComboBox.setSelectedItem(this.currentCalculator);
     }
 
     /**
      * Reset the calculator controls when the calculator choices have changed.
      */
     public void resetCalculatorDisplay() {
 	// reset local calculator collection
 	refreshCalculators();
 	if (this.calcComboBox != null)
 	    mapPanelGBG.panel.remove(this.calcComboBox);
 	setupCalcComboBox();
 	MiscGB.insert(mapPanelGBG, calcComboBox, 0, 0, 4, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	//switchCalculator(c);
     }
 
     protected void drawCalc() {
 	//this.mapPanel = new JPanel(false);
 	//mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.Y_AXIS));
 
 	// grid bag layout
 	this.mapPanelGBG = new GridBagGroup("Mapping");
 	MiscGB.pad(mapPanelGBG.constraints, 2, 2);
 	MiscGB.inset(mapPanelGBG.constraints, 3);
 
 	// Initialize here
 	this.calcContainer = new JPanel(false);
 
 	resetCalculatorDisplay();
 	/*
 	// calculator select prompt
 	this.calcComboBox = setupCalcComboBox();
 	// set up selection listener for combo box
 	calcComboBox.addItemListener(new calcComboSelectionListener());
 	MiscGB.insert(mapPanelGBG, calcComboBox, 0, 0, 4, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	*/
 	// new calculator button
 	JButton newCalc = new JButton("New");
 	newCalc.addActionListener(new NewCalcListener());
 	MiscGB.insert(mapPanelGBG, newCalc, 0, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 
 	// duplicate calculator button
 	JButton dupeCalc = new JButton("Duplicate");
 	dupeCalc.addActionListener(new DupeCalcListener());
 	MiscGB.insert(mapPanelGBG, dupeCalc, 1, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 
 	// rename calculator button
 	JButton renCalc = new JButton("Rename");
 	renCalc.addActionListener(new RenCalcListener());
 	MiscGB.insert(mapPanelGBG, renCalc, 2, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 
 	// remove calculator button
 	JButton rmCalc = new JButton("Remove");
 	rmCalc.addActionListener(new RmCalcListener());
 	MiscGB.insert(mapPanelGBG, rmCalc, 3, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 
 	// set up the current calculator's variable panel if a calculator is
 	// selected
 	//switchCalculator(c);
 	/*
 	if (c != null) {
 	    this.calcPanel = c.getUI(mainUIDialog, n);
 	}
 	else {
 	    this.calcPanel = new JPanel();
 	}
 	calcContainer.add(calcPanel);
 	*/
 	// add to gridbag
 	MiscGB.insert(mapPanelGBG, calcContainer, 0, 2, 4, 1, 5, 5, GridBagConstraints.BOTH);
 	//MiscGB.insert(mapPanelGBG, Box.createVerticalStrut(3), 0, 2, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER);
 	/*
 	  mapPanel.add(Box.createVerticalStrut(3));
 	  mapPanel.add(mapPanelGBG.panel);
 	  mapPanelGBG.panel.setMaximumSize(mapPanelGBG.panel.getPreferredSize());
 	  mapPanel.add(Box.createVerticalStrut(3));
 	  mapPanel.add(calcPanel);
 	  mapPanel.add(Box.createVerticalStrut(3));
 	*/
 	add(mapPanelGBG.panel, BorderLayout.CENTER);
 	//add(mapPanel);
     }
 
     // new calculator button pressed
     private class NewCalcListener extends AbstractAction {
 	public void actionPerformed(ActionEvent e) {
 	    // get available mappings
 	    Set mapperNames = catalog.getMappingNames();
 	    // convert to array for JOptionPane
 	    Object[] mapperArray = mapperNames.toArray();
 
 	    // show JOptionPane with the available mappers
 	    String selectedMapper = (String) JOptionPane.showInputDialog(mainUIDialog,
 					"Choose a mapper",
 					"New Calculator",
 					JOptionPane.QUESTION_MESSAGE,
 					null,
 					mapperArray,
 					mapperArray[0]);
 	    if (selectedMapper == null)
 		return;
 
 	    // get a name for the new calculator
 	    String calcName = getCalculatorName(null);
 	    if (calcName == null)
 		return;
 
 	    // create the new calculator
 	    // get the selected mapper
 	    Class mapperClass = catalog.getMapping(selectedMapper);
 	    // create the selected mapper
 	    Class[] conTypes = {Object.class, byte.class};
 	    Constructor mapperCon;
 	    try {
 		mapperCon = mapperClass.getConstructor(conTypes);
 	    }
 	    catch (NoSuchMethodException exc) {
 		// show error message, mapper was not programmed correctly
 		System.err.println("Invalid mapper " + mapperClass.getName());
 		JOptionPane.showMessageDialog(mainUIDialog,
 					      "Mapper " + mapperClass.getName() + " does not have an acceptable constructor. See documentation for ObjectMapper.",
 					      "Invalid Mapper",
 					      JOptionPane.ERROR_MESSAGE);
 		return;
 	    }
 
 	    // create the mapper
 	    byte mapType; // node or edge calculator
 	    switch(type) {
 	    case VizMapUI.EDGE_COLOR:
 	    case VizMapUI.EDGE_LINETYPE:
 	    case VizMapUI.EDGE_SRCARROW:
 	    case VizMapUI.EDGE_TGTARROW:
 	    case VizMapUI.EDGE_LABEL:
 	    case VizMapUI.EDGE_FONT_FACE:
 	    case VizMapUI.EDGE_FONT_SIZE:
 	    case VizMapUI.EDGE_TOOLTIP:
 		mapType = ObjectMapping.EDGE_MAPPING;
 		break;
 	    default:
 		mapType = ObjectMapping.NODE_MAPPING;
 	    }
 	    Object[] invokeArgs = {defaultObj, new Byte(mapType)};
 	    ObjectMapping mapper = null;
 	    try {
 		mapper = (ObjectMapping) mapperCon.newInstance(invokeArgs);
 	    }
 	    catch (Exception exc) {
 		System.err.println("Error creating mapping");
 		JOptionPane.showMessageDialog(mainUIDialog,
 					      "Error creating mapping " + mapperClass.getName(),
 					      "Error",
 					      JOptionPane.ERROR_MESSAGE);
 	    }
 	    // create and add a generic calculator based on this tab's type
 	    Calculator calc = null;
 	    if (type == VizMapUI.NODE_COLOR || type == VizMapUI.NODE_BORDER_COLOR) {
 		calc = new GenericNodeColorCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.NODE_LINETYPE) {
 		calc = new GenericNodeLineTypeCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.NODE_SHAPE) {
 		calc = new GenericNodeShapeCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.NODE_HEIGHT || type == VizMapUI.NODE_WIDTH
 		     || type == VizMapUI.NODE_SIZE) {
 		calc = new GenericNodeSizeCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.NODE_LABEL) {
 		calc = new GenericNodeLabelCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.NODE_TOOLTIP) {
 		calc = new GenericNodeToolTipCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.EDGE_COLOR) {
 		calc = new GenericEdgeColorCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.EDGE_LINETYPE) {
 		calc = new GenericEdgeLineTypeCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.EDGE_SRCARROW || type == VizMapUI.EDGE_TGTARROW) {
 		calc = new GenericEdgeArrowCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.EDGE_LABEL) {
 		calc = new GenericEdgeLabelCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.EDGE_TOOLTIP) {
 		calc = new GenericEdgeToolTipCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.EDGE_FONT_FACE) {
 		calc = new GenericEdgeFontFaceCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.EDGE_FONT_SIZE) {
 		calc = new GenericEdgeFontSizeCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.NODE_FONT_FACE) {
 		calc = new GenericNodeFontFaceCalculator(calcName, mapper);
 	    }
 	    else if (type == VizMapUI.NODE_FONT_SIZE) {
 		calc = new GenericNodeFontSizeCalculator(calcName, mapper);
 	    }
 	    // set current calculator to the new calculator
 	    currentCalculator = calc;
 	    // notify the catalog - this triggers events that refresh the UI
 	    catalog.addCalculator(calc);
 	}
     }
 
     // duplicate calculator button pressed
     private class DupeCalcListener extends AbstractAction {
 	public void actionPerformed(ActionEvent e) {
 	    Calculator clone = duplicateCalculator(currentCalculator);
 	    // die if user cancelled in the middle of duplication
 	    if (clone == null)
 		return;
 	    currentCalculator = clone;
 	    catalog.addCalculator(clone);
 	}
     }
 
     // duplicate the given calculator, prompting for a name
     private Calculator duplicateCalculator(Calculator c) {
         Calculator clone = null;
 	try {
 	    clone = (Calculator) c.clone();
 	}
 	catch (CloneNotSupportedException exc) { // this will never happen
 	    System.err.println("Fatal error - Calculator didn't support Cloneable");
 	    exc.printStackTrace();
 	    return null;
 	}
 	// get new name for clone
 	String newName = getCalculatorName(clone);
 	if (newName == null)
 	    return null;
 	clone.setName(newName);
 	return clone;
     }
 
     private String getCalculatorName(Calculator c) {
 	// default to the next available name for c
 	String suggestedName = null;
	if (c != null)
	    suggestedName = this.catalog.checkCalculatorName(c.toString(), this.type);
 	
 	// keep prompting for input until user cancels or we get a valid name
 	while(true) {
 	    String ret = (String) JOptionPane.showInputDialog(mainUIDialog,
 							      "New name for calculator",
 							      "Calculator Name Input",
 							      JOptionPane.QUESTION_MESSAGE,
 							      null, null,
 							      suggestedName);
 	    if (ret == null) {
 		return null;
 	    }
 	    String newName = catalog.checkCalculatorName(ret, this.type);
 	    if (newName.equals(ret))
 		return ret;
 	    int alt = JOptionPane.showConfirmDialog(mainUIDialog,
 						    "Calculator with name " + ret + " already exists,\nrename to " + newName + " okay?",
 						    "Duplicate calculator name",
 						    JOptionPane.YES_NO_OPTION,
 						    JOptionPane.WARNING_MESSAGE,
 						    null);
 	    if (alt == JOptionPane.YES_OPTION)
 		return newName;
 	}
     }
 
     // rename calculator button pressed
     private class RenCalcListener extends AbstractAction {
 	public void actionPerformed(ActionEvent e) {
 	    // get the new name, keep prompting for input until a valid input is received
 	    String calcName = getCalculatorName(currentCalculator);
 	    if (calcName == null)
 		return;
 	    catalog.renameCalculator(currentCalculator, calcName);
 	}
     }
 
     // remove calculator button pressed
     private class RmCalcListener extends AbstractAction {
 	public void actionPerformed(ActionEvent e) {
 	    catalog.removeCalculator(currentCalculator);
 	    switchCalculator(null);
 	    /* switchCalculator must be called explicitly in this method because we use
 	       setSelectedIndex(0) to set the current calculator to null in setupCalcComboBox,
 	       which does not trigger an event.
 	    */
 	}    
     }
 
     /**
      * Internal class to listen for possible changes in the state of the catalog
      */
     private class CatalogListener implements ChangeListener {
 	public void stateChanged(ChangeEvent e) {
 	    resetCalculatorDisplay();
 	}
     }
     
     private class calcComboSelectionListener implements ItemListener {
 	public void itemStateChanged(ItemEvent e) {
 	    if (e.getStateChange() == ItemEvent.SELECTED) {
 		Object selected = calcComboBox.getSelectedItem();
 		if (selected.equals("None")) { // "None" selected, use null
 		    switchCalculator(null);
 		}
 		else {
 		    switchCalculator((Calculator) selected);
 		}
 	    }
 	}
     }
 
     /**
      * Check that the calculator is not selected by other objects. If so,
      * pop up a dialog offering to duplicate the calculator.
      *
      * @param	c	newly selected calculator
      */
     VizMapTab checkCalcSelected(Calculator c) {
 	if (this.currentCalculator == c)
 	    return this;
 	return null;
     }
 
     /**
      * Designed as a hook for VizMapSizeTab to set the combo box display when
      * node width/height are locked. Changes the display, which fires an event
      * alerting calcComboSelectionListener which changes the selected calculator.
      */
     void setComboBox(Calculator c) {
 	if (c == null) { // select "None"
 	    this.calcComboBox.setSelectedIndex(0);
 	}
 	else {
 	    this.calcComboBox.setSelectedItem(c);
 	}
     }
 
     public void refreshUI() {
 	if (this.calcPanel != null)
 	    this.calcContainer.remove(this.calcPanel);
 	if (this.currentCalculator != null) {
 	    this.calcPanel = this.currentCalculator.getUI(this.mainUIDialog, this.n);
 	    this.calcContainer.add(this.calcPanel);
 	}
 	else
 	    this.calcPanel = null;
     }
     
     /**
      * Called by action listener on the calculator selection combo box. Switch
      * the current calculator to the selected calculator and place the calculator's
      * UI into the calculator UI panel.
      *
      * @param	c		new Calculator to use for this tab's mapping
      */
     void switchCalculator(Calculator c) {
 	if (c != null) {
 	    // check that the calculator is valid - a little kludgy
 	    String newName = catalog.checkCalculatorName(c.toString(), this.type);
 	    // if the names are equal, that means that the catalog doesn't know about
 	    // the Calculator c, so change the calculator to null and reflect this in
 	    // the combo box.
 	    if (newName.equals(c.toString())) {
 		c = null;
 		setComboBox(null);
 	    }
 	    
 	    // check that nobody else is using the selected calculator
 	    VizMapTab calcSelectedTab = mainUIDialog.checkCalcSelected(c);
 	    if (calcSelectedTab != null && calcSelectedTab != this) {
 		// offer to duplicate
 		int alt = JOptionPane.showConfirmDialog(mainUIDialog,
 							"Calculator " + c.toString() + " already used, create a duplicate?",
 							"Calculator already used",
 							JOptionPane.YES_NO_OPTION,
 							JOptionPane.WARNING_MESSAGE,
 							null);
 		if (alt == JOptionPane.YES_OPTION) {
 		    // duplicate and continue
 		    Calculator clone = duplicateCalculator(c);
 		    // die if user cancelled
 		    if (clone == null)
 			return;
 		    this.currentCalculator = clone;
 		    catalog.addCalculator(clone);
 		    // addCalculator throws an event that will call
 		    // switchCalculator, so just exit this call.
 		    return;
 		}
 		else {
 		    // reset the combobox back
 		    setComboBox(this.currentCalculator);
 		    return;
 		}
 	    }
 	}
 	this.currentCalculator = c;
 	refreshUI();
 
 	/*
 	Dimension d = this.calcPanel.getMaximumSize();
 	if (d != null)
 	    d.setSize(d.getWidth(), 150);
 	else
 	    this.calcPanel.setMaximumSize(new Dimension(0, 150));
 	*/
 	//mapPanel.add(calcPanel);
 	//mapPanel.add(Box.createVerticalStrut(3));
 	//calcContainer.add(calcPanel);
 	//MiscGB.insert(mapPanelGBG, calcPanel, 0, 1, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 5, 5, GridBagConstraints.BOTH);
 
 	// tell the respective appearance calculators
 	switch(this.type) {
 	case VizMapUI.NODE_COLOR:
 	    nodeCalc.setNodeFillColorCalculator((NodeColorCalculator) c);
 	    break;
 	case VizMapUI.NODE_BORDER_COLOR:
 	    nodeCalc.setNodeBorderColorCalculator((NodeColorCalculator) c);
 	    break;
 	case VizMapUI.NODE_LINETYPE:
 	    nodeCalc.setNodeLineTypeCalculator((NodeLineTypeCalculator) c);
 	    break;
 	case VizMapUI.NODE_SHAPE:
 	    nodeCalc.setNodeShapeCalculator((NodeShapeCalculator) c);
 	    break;
 	case VizMapUI.NODE_LABEL:
 	    nodeCalc.setNodeLabelCalculator((NodeLabelCalculator) c);
 	    break;
 	case VizMapUI.NODE_HEIGHT:
 	    nodeCalc.setNodeHeightCalculator((NodeSizeCalculator) c);
 	    break;
 	case VizMapUI.NODE_WIDTH:
 	    nodeCalc.setNodeWidthCalculator((NodeSizeCalculator) c);
 	    break;
 	case VizMapUI.NODE_SIZE:
 	    nodeCalc.setNodeWidthCalculator((NodeSizeCalculator) c);
 	    nodeCalc.setNodeHeightCalculator((NodeSizeCalculator) c);
 	    break;
 	case VizMapUI.NODE_TOOLTIP:
 	    nodeCalc.setNodeToolTipCalculator((NodeToolTipCalculator) c);
 	    break;
 	case VizMapUI.EDGE_COLOR:
 	    edgeCalc.setEdgeColorCalculator((EdgeColorCalculator) c);
 	    break;
 	case VizMapUI.EDGE_LINETYPE:
 	    edgeCalc.setEdgeLineTypeCalculator((EdgeLineTypeCalculator) c);
 	    break;
 	case VizMapUI.EDGE_SRCARROW:
 	    edgeCalc.setEdgeSourceArrowCalculator((EdgeArrowCalculator) c);
 	    break;
 	case VizMapUI.EDGE_TGTARROW:
 	    edgeCalc.setEdgeTargetArrowCalculator((EdgeArrowCalculator) c);
 	    break;
 	case VizMapUI.EDGE_LABEL:
 	    edgeCalc.setEdgeLabelCalculator((EdgeLabelCalculator) c);
 	    break;
 	case VizMapUI.EDGE_TOOLTIP:
 	    edgeCalc.setEdgeToolTipCalculator((EdgeToolTipCalculator) c);
 	    break;
 	case VizMapUI.EDGE_FONT_FACE:
 	    edgeCalc.setEdgeFontFaceCalculator((EdgeFontFaceCalculator) c);
 	    break;
 	case VizMapUI.EDGE_FONT_SIZE:
 	    edgeCalc.setEdgeFontSizeCalculator((EdgeFontSizeCalculator) c);
 	    break;
 	case VizMapUI.NODE_FONT_FACE:
 	    nodeCalc.setNodeFontFaceCalculator((NodeFontFaceCalculator) c);
 	    break;
 	case VizMapUI.NODE_FONT_SIZE:
 	    nodeCalc.setNodeFontSizeCalculator((NodeFontSizeCalculator) c);
 	    break;
 	}
 	super.validate();
 	super.repaint();
 	mainUIDialog.pack();
 	mainUIDialog.repaint();
     }
 
     /**
      *	Listener for the ValueDisplayer to notify VizMapAttrTab when the default
      *	object changed.
      */
     private class DefaultItemChangedListener implements ItemListener {
 	public void itemStateChanged(ItemEvent e) {
 	    // prevent bugs, could be removed later
 	    if (e.getItemSelectable() == defaultValueDisplayer &&
 		e.getStateChange() == ItemEvent.SELECTED) {
 		_setDefault(defaultValueDisplayer.getValue());
 	    }
 	}
     }
 
     /**
      * Set the default value represented in this VizMapAttrTab.
      */
     public void setDefault(Object o) {
 	defaultValueDisplayer.setObject(o);
     }
 
     /**
      *	Set the default object.
      */
     protected void _setDefault(Object c) {
 	// tell the respective appearance calculators
 	switch(this.type) {
 	case VizMapUI.NODE_COLOR:
 	    nodeCalc.setDefaultNodeFillColor((Color) c);
 	    break;
 	case VizMapUI.NODE_BORDER_COLOR:
 	    nodeCalc.setDefaultNodeBorderColor((Color) c);
 	    break;
 	case VizMapUI.NODE_LINETYPE:
 	    nodeCalc.setDefaultNodeLineType((LineType) c);
 	    break;
 	case VizMapUI.NODE_SHAPE:
 	    nodeCalc.setDefaultNodeShape(((Byte) c).byteValue());
 	    break;
 	case VizMapUI.NODE_HEIGHT:
 	    nodeCalc.setDefaultNodeHeight(((Double) c).doubleValue());
 	    break;
 	case VizMapUI.NODE_WIDTH:
 	    nodeCalc.setDefaultNodeWidth(((Double) c).doubleValue());
 	    break;
 	case VizMapUI.NODE_SIZE:
 	    nodeCalc.setDefaultNodeHeight(((Double) c).doubleValue());
 	    nodeCalc.setDefaultNodeWidth(((Double) c).doubleValue());
 	    break;
 	case VizMapUI.NODE_LABEL:
 	    nodeCalc.setDefaultNodeLabel((String) c);
 	    break;
 	case VizMapUI.NODE_TOOLTIP:
 	    nodeCalc.setDefaultNodeToolTip((String) c);
 	    break;
 	case VizMapUI.EDGE_COLOR:
 	    edgeCalc.setDefaultEdgeColor((Color) c);
 	    break;
 	case VizMapUI.EDGE_LINETYPE:
 	    edgeCalc.setDefaultEdgeLineType((LineType) c);
 	    break;
 	case VizMapUI.EDGE_SRCARROW:
 	    edgeCalc.setDefaultEdgeSourceArrow((Arrow) c);
 	    break;
 	case VizMapUI.EDGE_TGTARROW:
 	    edgeCalc.setDefaultEdgeTargetArrow((Arrow) c);
 	    break;
 	case VizMapUI.EDGE_LABEL:
 	    edgeCalc.setDefaultEdgeLabel((String) c);
 	    break;
 	case VizMapUI.EDGE_TOOLTIP:
 	    edgeCalc.setDefaultEdgeToolTip((String) c);
 	    break;
 	case VizMapUI.EDGE_FONT_FACE:
 	    edgeCalc.setDefaultEdgeFontFace((Font) c);
 	    break;
 	case VizMapUI.EDGE_FONT_SIZE:
 	    edgeCalc.setDefaultEdgeFontSize(((Double) c).floatValue());
 	    break;
 	case VizMapUI.NODE_FONT_FACE:
 	    nodeCalc.setDefaultNodeFontFace((Font) c);
 	    break;
 	case VizMapUI.NODE_FONT_SIZE:
 	    nodeCalc.setDefaultNodeFontSize(((Double) c).floatValue());
 	    break;
 	}
     }	
 
     /**
      *	Set the image icon
      */
     protected void setImageIcon() {
 	// tell the respective appearance calculators
 	imageIcon = null;
 	switch(this.type) {
 	case VizMapUI.NODE_COLOR:
 	    imageIcon = new ImageIcon(this.getClass().getResource("images/nodeColorWheel.jpg"),"Node Color");
 	    break;
 	case VizMapUI.NODE_BORDER_COLOR:
 	    imageIcon = new ImageIcon(this.getClass().getResource("images/nodeBorderColorWheel.jpg"),"Node Color");
 	    break;
 	}
     }
 
     protected void drawDefault() {
 	JPanel outerDefPanel = new JPanel(false);
 	outerDefPanel.setLayout(new BoxLayout(outerDefPanel,BoxLayout.X_AXIS));
 
 	JPanel defPanel = new JPanel(false);
 	defPanel.setLayout(new BoxLayout(defPanel, BoxLayout.Y_AXIS));
 	Box content = new Box(BoxLayout.X_AXIS);
 
 	// create the ValueDisplayer
 	this.defaultValueDisplayer = ValueDisplayer.getDisplayFor(mainUIDialog,
 								  getName(),
 								  defaultObj);
 	defaultValueDisplayer.addItemListener(new DefaultItemChangedListener());
 
 	// create the button
 	JButton defaultButton = new JButton("Change Default");
 	// attach ActionListener from ValueDisplayer to button
 	defaultButton.addActionListener(defaultValueDisplayer.getInputListener());
 
 	// dump components into content Box
 	content.add(Box.createHorizontalGlue());
 	content.add(defaultButton);
 	content.add(Box.createHorizontalStrut(3));
 	content.add(defaultValueDisplayer);
 	content.add(Box.createHorizontalGlue());
 
 	// pad the default panel
 	defPanel.add(Box.createVerticalStrut(3));
 	defPanel.add(content);
 	defPanel.add(Box.createVerticalStrut(3));
 
 	// attach a border
 	Border defBorder = BorderFactory.createLineBorder(Color.BLACK);
 	defPanel.setBorder(BorderFactory.createTitledBorder(defBorder,
 							    "Default",
 							    TitledBorder.CENTER,
 							    TitledBorder.TOP));
 	defPanel.validate();
 
 	setImageIcon();
 	if(imageIcon==null)
 	    this.add(defPanel, BorderLayout.NORTH);
 	else {
 	    JButton tempB = new JButton();
 	    tempB.setIcon(imageIcon);
 	    outerDefPanel.add(tempB);
 	    outerDefPanel.add(defPanel);
 	    this.add(outerDefPanel, BorderLayout.NORTH);
 	}
     }	
 
     /**
      * Refreshes the UI for calculator selection. Use when the set of available
      * calculators has changed.
      */
     public void refreshCalculators() {
 	this.calculators = getCalculators(this.type);
     }
 
     /**
      * Gets the defaults for the VizMapAttrTab. Use when the default setting for
      * an attribute, the currently selected calculator for an attribute, or
      * the set of avaiable calculators has changed.
      */
     protected void getDefaults(byte type) {
 	// this.currentCalculatoralculators = getCalculators(type);
 	switch (type) {
 	case VizMapUI.NODE_COLOR:
 	    this.defaultObj = nodeCalc.getDefaultNodeFillColor();
 	    this.currentCalculator = nodeCalc.getNodeFillColorCalculator();
 	    break;
 	case VizMapUI.NODE_BORDER_COLOR:
 	    this.defaultObj = nodeCalc.getDefaultNodeBorderColor();
 	    this.currentCalculator = nodeCalc.getNodeBorderColorCalculator();
 	    break;
 	case VizMapUI.NODE_LINETYPE:
 	    this.defaultObj = nodeCalc.getDefaultNodeLineType();
 	    this.currentCalculator = nodeCalc.getNodeLineTypeCalculator();
 	    break;
 	case VizMapUI.NODE_SHAPE:
 	    this.defaultObj = new Byte(nodeCalc.getDefaultNodeShape());
 	    this.currentCalculator = nodeCalc.getNodeShapeCalculator();
 	    break;
 	case VizMapUI.NODE_HEIGHT:
 	    this.defaultObj = new Double(nodeCalc.getDefaultNodeHeight());
 	    this.currentCalculator = nodeCalc.getNodeHeightCalculator();
 	    break;
 	case VizMapUI.NODE_WIDTH:
 	    this.defaultObj = new Double(nodeCalc.getDefaultNodeWidth());
 	    this.currentCalculator = nodeCalc.getNodeWidthCalculator();
 	    break;
 	case VizMapUI.NODE_SIZE:
 	    this.defaultObj = new Double(nodeCalc.getDefaultNodeHeight());
 	    this.currentCalculator = nodeCalc.getNodeHeightCalculator();
 	    break;
 	case VizMapUI.NODE_LABEL:
 	    this.defaultObj = nodeCalc.getDefaultNodeLabel();
 	    this.currentCalculator = nodeCalc.getNodeLabelCalculator();
 	    break;
 	case VizMapUI.NODE_TOOLTIP:
 	    this.defaultObj = nodeCalc.getDefaultNodeToolTip();
 	    this.currentCalculator = nodeCalc.getNodeToolTipCalculator();
 	    break;
 	case VizMapUI.EDGE_COLOR:
 	    this.defaultObj = edgeCalc.getDefaultEdgeColor();
 	    this.currentCalculator = edgeCalc.getEdgeColorCalculator();
 	    break;
 	case VizMapUI.EDGE_LINETYPE:
 	    this.defaultObj = edgeCalc.getDefaultEdgeLineType();
 	    this.currentCalculator = edgeCalc.getEdgeLineTypeCalculator();
 	    break;
 	case VizMapUI.EDGE_SRCARROW:
 	    this.defaultObj = edgeCalc.getDefaultEdgeSourceArrow();
 	    this.currentCalculator = edgeCalc.getEdgeSourceArrowCalculator();
 	    break;
 	case VizMapUI.EDGE_TGTARROW:
 	    this.defaultObj = edgeCalc.getDefaultEdgeTargetArrow();
 	    this.currentCalculator = edgeCalc.getEdgeTargetArrowCalculator();
 	    break;
 	case VizMapUI.EDGE_LABEL:
 	    this.defaultObj = edgeCalc.getDefaultEdgeLabel();
 	    this.currentCalculator = edgeCalc.getEdgeLabelCalculator();
 	    break;
 	case VizMapUI.EDGE_TOOLTIP:
 	    this.defaultObj = edgeCalc.getDefaultEdgeToolTip();
 	    this.currentCalculator = edgeCalc.getEdgeToolTipCalculator();
 	    break;
 	case VizMapUI.NODE_FONT_FACE:
 	    this.defaultObj = n.getGraph().getDefaultNodeRealizer().getLabel().getFont();
 	    // set the font in the appearance calculator, since they might not match at this point
 	    _setDefault(defaultObj);
 	    this.currentCalculator = nodeCalc.getNodeFontFaceCalculator();
 	    break;
 	case VizMapUI.EDGE_FONT_FACE:
 	    this.defaultObj = n.getGraph().getDefaultEdgeRealizer().getLabel().getFont();
 	    // set the font in the appearance calculator, since they might not match at this point
 	    _setDefault(defaultObj);
 	    this.currentCalculator = edgeCalc.getEdgeFontFaceCalculator();
 	    break;	  
 	case VizMapUI.NODE_FONT_SIZE:
 	    this.defaultObj = new Double(n.getGraph().getDefaultNodeRealizer().getLabel().getFont().getSize2D());
 	    // set the font in the appearance calculator, since they might not match at this point
 	    _setDefault(defaultObj);
 	    this.currentCalculator = nodeCalc.getNodeFontSizeCalculator();
 	    break;
 	case VizMapUI.EDGE_FONT_SIZE:
 	    this.defaultObj = new Double(n.getGraph().getDefaultEdgeRealizer().getLabel().getFont().getSize2D());
 	    // set the font in the appearance calculator, since they might not match at this point
 	    _setDefault(defaultObj);
 	    this.currentCalculator = edgeCalc.getEdgeFontSizeCalculator();
 	    break;
 	}
     }
     
     private Collection getCalculators(byte type) {
 	switch(type) {
 	case VizMapUI.NODE_COLOR:
 	    return catalog.getNodeColorCalculators();
 	case VizMapUI.NODE_BORDER_COLOR:
 	    return catalog.getNodeColorCalculators();
 	case VizMapUI.NODE_LINETYPE:
 	    return catalog.getNodeLineTypeCalculators();
 	case VizMapUI.NODE_SHAPE:
 	    return catalog.getNodeShapeCalculators();
 	case VizMapUI.NODE_SIZE:
 	case VizMapUI.NODE_HEIGHT:
 	case VizMapUI.NODE_WIDTH:
 	    return catalog.getNodeSizeCalculators();
 	case VizMapUI.NODE_LABEL:
 	    return catalog.getNodeLabelCalculators();
 	case VizMapUI.NODE_TOOLTIP:
 	    return catalog.getNodeToolTipCalculators();
 	case VizMapUI.EDGE_COLOR:
 	    return catalog.getEdgeColorCalculators();
 	case VizMapUI.EDGE_LINETYPE:
 	    return catalog.getEdgeLineTypeCalculators();
 	case VizMapUI.EDGE_SRCARROW:
 	    return catalog.getEdgeArrowCalculators();
 	case VizMapUI.EDGE_TGTARROW:
 	    return catalog.getEdgeArrowCalculators();
 	case VizMapUI.EDGE_LABEL:
 	    return catalog.getEdgeLabelCalculators();
 	case VizMapUI.EDGE_TOOLTIP:
 	    return catalog.getEdgeToolTipCalculators();
 	case VizMapUI.EDGE_FONT_FACE:
 	    return catalog.getEdgeFontFaceCalculators();
 	case VizMapUI.EDGE_FONT_SIZE:
 	    return catalog.getEdgeFontSizeCalculators();
 	case VizMapUI.NODE_FONT_FACE:
 	    return catalog.getNodeFontFaceCalculators();
 	case VizMapUI.NODE_FONT_SIZE:
 	    return catalog.getNodeFontSizeCalculators();
 	default:
 	    System.err.println("WARNING: Couldn't find match for type " + type);
 	    return null;
 	}
     }
     
     protected static String getTypeName(byte type) {
 	switch (type) {
 	case VizMapUI.NODE_COLOR:
 	    return "Node Color";
 	case VizMapUI.NODE_BORDER_COLOR:
 	    return "Node Border Color";
 	case VizMapUI.NODE_LINETYPE:
 	    return "Node Border Type";
 	case VizMapUI.NODE_SHAPE:
 	    return "Node Shape";
 	case VizMapUI.NODE_WIDTH:
 	    return "Node Width";
 	case VizMapUI.NODE_HEIGHT:
 	    return "Node Height";
 	case VizMapUI.NODE_SIZE:
 	    return "Node Size";
 	case VizMapUI.NODE_LABEL:
 	    return "Node Label";
 	case VizMapUI.NODE_TOOLTIP:
 	    return "Node Tooltip";
 	case VizMapUI.EDGE_COLOR:
 	    return "Edge Color";
 	case VizMapUI.EDGE_LINETYPE:
 	    return "Edge Line Type";
 	case VizMapUI.EDGE_SRCARROW:
 	    return "Edge Source Arrow";
 	case VizMapUI.EDGE_TGTARROW:
 	    return "Edge Target Arrow";
 	case VizMapUI.EDGE_LABEL:
 	    return "Edge Label";
 	case VizMapUI.EDGE_TOOLTIP:
 	    return "Edge Tooltip";
 	case VizMapUI.EDGE_FONT_FACE:
 	    return "Edge Font Face";
 	case VizMapUI.EDGE_FONT_SIZE:
 	    return "Edge Font Size";
 	case VizMapUI.NODE_FONT_FACE:
 	    return "Node Font Face";
 	case VizMapUI.NODE_LABEL_FONT:
 	    return "Node Font Size";
 	default:
 	    return null;
 	}
     }
 }
