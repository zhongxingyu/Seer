 /*
   File: CytoPanelImp.java
 
   Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package org.cytoscape.internal.view;
 
 
 import java.awt.BorderLayout;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.cytoscape.application.swing.CytoPanel;
 import org.cytoscape.application.swing.CytoPanelComponent;
 import org.cytoscape.application.swing.CytoPanelName;
 import org.cytoscape.application.swing.CytoPanelState;
 import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
 import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
 import org.cytoscape.event.CyEventHelper;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The CytoPanel class extends JPanel to provide the following functionality:
  * <UL>
  * <LI> Floating/Docking of Panel.
  * <UL>
  *
  * CytoPanel also implements CytoPanel interface.
  *
  * @author Ethan Cerami, Benjamin Gross
  */
 public class CytoPanelImp extends JPanel implements CytoPanel, ChangeListener {
 	
 	private final static long serialVersionUID = 1202339868245830L;
 	
 	private final static Logger logger = LoggerFactory.getLogger(CytoPanelImp.class);
 
 	/**
 	 * These are the minimum sizes for our CytoPanels.  A CytoPanel can't exceed these
 	 * values.
 	 */
 	private static final int WEST_MIN_WIDTH = 100;
 	private static final int WEST_MAX_WIDTH = 400;
 	private static final int WEST_MIN_HEIGHT = 500;
 	private static final int SOUTH_MIN_WIDTH = 500;
 	private static final int SOUTH_MIN_HEIGHT = 50;
 	
 	private static final int EAST_MIN_WIDTH = 100;
 	private static final int EAST_MAX_WIDTH = 1500;
 	private static final int EAST_MIN_HEIGHT = 100;
 	private static final int EAST_MAX_HEIGHT = 600;
 	
 	/**
 	 * The JTabbedPane we hide.
 	 */
 	private JTabbedPane tabbedPane;
 
 	/**
 	 * Our state.
 	 */
 	private CytoPanelState cytoPanelState;
 
 	/**
 	 * Our compass direction.
 	 */
 	private CytoPanelName compassDirection;
 
 	/**
 	 * Notification state change.
 	 */
 	private final int NOTIFICATION_STATE_CHANGE = 0;
 
 	/**
 	 * Notification component selected.
 	 */
 	private final int NOTIFICATION_COMPONENT_SELECTED = 1;
 
 	/**
 	 * Notification component added.
 	 */
 	private final int NOTIFICATION_COMPONENT_ADDED = 2;
 
 	/**
 	 * Notification component removed.
 	 */
 	private final int NOTIFICATION_COMPONENT_REMOVED = 3;
 
 	/**
 	 * Reference to CytoPanelContainer we live in.
 	 */
 	private CytoPanelContainer cytoPanelContainer;
 
 	/**
 	 * External window used to hold the floating CytoPanel.
 	 */
 	private JFrame externalFrame;
 
 	/**
 	 * The float icon.
 	 */
 	private ImageIcon floatIcon;
 
 	/**
 	 * The dock icon.
 	 */
 	private ImageIcon dockIcon;
 	
 	/**
 	 * The close icon.
 	 */
 	private ImageIcon closeIcon;
 
 	/**
 	 * The label which contains the tab title - not sure if its needed.
 	 */
 	private JLabel floatLabel;
 
 	/**
 	 * The float/dock button.
 	 */
 	private JButton floatButton;
 	
 	private JButton closeButton;
 
 	/**
 	 * The float/dock button.
 	 */
 	private final int FLOAT_PANEL_SCALE_FACTOR = 2;
 
 	/**
 	 * Color of the dock/float button panel.
 	 */
 	private Color FLOAT_PANEL_COLOR = new Color(224, 224, 224);
 
 	/* the following constants should probably move into common constants class */
 
 	//The float button tool tip.
 	private static final String TOOL_TIP_FLOAT = "Float Window";
 
 	// The dock button tool tip.
 	private static final String TOOL_TIP_DOCK = "Dock Window";
 	
 	// The dock button tool tip.
 	private static final String TOOL_TIP_CLOSE = "Close Window";
 
 	/**
 	 * Location of our icons.
 	 */
 	private static final String RESOURCE_DIR = "/images/";
 
 	/**
 	 * Icons for window
 	 */
 	
 	private static final String FLOAT_GIF = "float.gif";
 	private static final String DOCK_GIF = "pin.gif";
 	private static final String CLOSE_PNG = "ximian/stock_close-16.png";
 
 	private final CyEventHelper cyEventHelper;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param compassDirection  Compass direction of this CytoPanel.
 	 * @param tabPlacement      Tab placement of this CytoPanel.
 	 * @param cytoPanelState    The starting CytoPanel state.
 	 */
 	public CytoPanelImp(final CytoPanelName compassDirection, final int tabPlacement,
 			final CytoPanelState cytoPanelState, final CyEventHelper eh) {
 		
 		this.cyEventHelper = eh;
 		// setup our tabbed pane
 		tabbedPane = new JTabbedPane(tabPlacement);
 		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		tabbedPane.addChangeListener(this);
 
 		this.compassDirection = compassDirection;
 
 		// init the icons
 		initIcons();
 
 		// construct our panel
 		constructPanel();
 
 		// to hidden by default 
 		setState(cytoPanelState);
 	}
 
 	/**
 	 * Sets CytoPanelContainer interface reference.
 	 *
 	 * @param cytoPanelContainer Reference to CytoPanelContainer
 	 */
 	public void setCytoPanelContainer(CytoPanelContainer cytoPanelContainer) {
 		// set our cytoPanelContainerReference
 		this.cytoPanelContainer = cytoPanelContainer;
 	}
 
 	/**
 	 * Returns the proper title based on our compass direction.
 	 *
 	 * @return A title string
 	 */
 	private String getTitle() {
 		return compassDirection.getTitle();
 	}
 
 	public CytoPanelName getCytoPanelName() {
 		return compassDirection;
 	}
 
 
 	public void add(CytoPanelComponent comp) {
 		// Check our sizes, and override, if necessary
 		checkSizes(comp.getComponent());
 		tabbedPane.addTab(comp.getTitle(), comp.getIcon(), comp.getComponent());
 		notifyListeners(NOTIFICATION_COMPONENT_ADDED);
 	}
 
 	/**
 	 * Adds a component to the CytoPanel with specified title, icon, and tool tip.
 	 *
 	 * @param title     Component title (can be null).
 	 * @param icon      Component icon (can be null).
 	 * @param component Component reference.
 	 * @param tip       Component Tool tip text.
 	 */
 	public void add(String title, Icon icon, Component component, String tip) {
 		// Check our sizes, and override, if necessary
 		checkSizes(component);
 		// add tab to JTabbedPane (string, icon, component, tip)
 		tabbedPane.addTab(title, icon, component, tip);
 
 		// send out a notification
 		notifyListeners(NOTIFICATION_COMPONENT_ADDED);
 	}
 
 	/**
 	 * Returns the number of components in the CytoPanel.
 	 *
 	 * @return int Number of components.
 	 */
 	public int getCytoPanelComponentCount() {
 		// return the number of tabs in the JTabbedPane.
 		return tabbedPane.getTabCount();
 	}
 
 	/**
 	 * Returns the currently selected component.
 	 *
 	 * @return component Currently selected Component reference.
 	 */
 	public Component getSelectedComponent() {
 		// get currently selected component in the JTabbedPane.
 		return tabbedPane.getSelectedComponent();
 	}
 
 	/**
 	 * Returns the component at index.
 	 *
 	 * @return component at the given index.
 	 */
 	public Component getComponentAt(int index) {
 		return tabbedPane.getComponentAt(index);
 	}
 
 	/**
 	 * Returns the currently selected index.
 	 *
 	 * @return index Currently selected index.
 	 */
 	public int getSelectedIndex() {
 		// get currently selected component in the JTabbedPane.
 		return tabbedPane.getSelectedIndex();
 	}
 
 	/**
 	 * Returns the index for the specified component.
 	 *
 	 * @param component Component reference.
 	 * @return int      Index of the Component or -1 if not found.
 	 */
 	public int indexOfComponent(Component component) {
 		// get the index from JTabbedPane
 		return tabbedPane.indexOfComponent(component);
 	}
 
 	/**
 	 * Returns the first Component index with given title.
 	 *
 	 * @param title Component title.
 	 * @return int  Component index with given title or -1 if not found.
 	 */
 	public int indexOfComponent(String title) {
 		// get the index from JTabbedPane
 		return tabbedPane.indexOfTab(title);
 	}
 
 	/**
 	 * Removes specified component from the CytoPanel.
 	 *
 	 * @param component Component reference.
 	 */
 	public void remove(Component component) {
 		// remove tab from JTabbedPane (component)
 		tabbedPane.remove(component);
 
 		// send out a notification
 		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
 	}
 
 	public void remove(CytoPanelComponent comp) {
 		tabbedPane.remove(comp.getComponent());
 	}
 
 	/**
 	 * Removes the component from the CytoPanel at the specified index.
 	 *
 	 * @param index Component index.
 	 */
 	public void remove(int index) {
 		// remove tab from JTabbedPane (index)
 		tabbedPane.remove(index);
 
 		// send out a notification
 		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
 	}
 
 	/**
 	 * Removes all the components from the CytoPanel.
 	 */
 	@Override
 	public void removeAll() {
 		// remove all tabs and components from JTabbedPane
 		tabbedPane.removeAll();
 
 		// send out a notification
 		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
 	}
 
 	/**
 	 * Sets the selected index on the CytoPanel.
 	 *
 	 * @param index The desired index.
 	 */
 	@Override
 	public void setSelectedIndex(final int index) {
 		// set selected index
 		if(tabbedPane.getTabCount()<=index)
 			return;
 		
 		tabbedPane.setSelectedIndex(index);
 		resizeSelectedComponent();
 		// do not have to sent out notification - the tabbedPane will let us know.
 	}
 
 	/**
 	 * Sets the state of the CytoPanel.
 	 *
 	 * @param cytoPanelState A CytoPanelState.
 	 */
 	public void setState(CytoPanelState cytoPanelState) {
 		boolean success = false;
 
 		// 'switch' on the state
 		if (cytoPanelState == CytoPanelState.HIDE) {
 			hideCytoPanel(cytoPanelState);
 			success = true;
 		} else if (cytoPanelState == CytoPanelState.FLOAT) {
 			FloatCytoPanel();
 			success = true;
 		} else if (cytoPanelState == CytoPanelState.DOCK) {
 			DockCytoPanel();
 			success = true;
 		}
 
 		// houston we have a problem
 		if (!success) {
 			// made it here, houston, we have a problem
 			throw new IllegalArgumentException("Illegal Argument:  " + cytoPanelState
 			                                   + ".  is unknown.  Please see CytoPanelState class.");
 		}
 
 		// set our new state
 		this.cytoPanelState = cytoPanelState;
 
 		// let our listeners know
 		notifyListeners(NOTIFICATION_STATE_CHANGE);
 	}
 
 	/**
 	 * Gets the state of the CytoPanel.
 	 *
 	 * @return A CytoPanelState.
 	 */
 	public CytoPanelState getState() {
 		return cytoPanelState;
 	}
 
 	/**
 	 * Our implementation of the ChangeListener interface,
 	 * to determine when new tab has been selected
 	 */
 	public void stateChanged(ChangeEvent e) {
 		// Handle the resize
 		resizeSelectedComponent();
 		
 		// let our listeners know
 		notifyListeners(NOTIFICATION_COMPONENT_SELECTED);
 	}
 
 	/**
 	 * Initialize all Icons.
 	 */
 	private void initIcons() {
 		// icon strings
 		String floatIconStr = new String(RESOURCE_DIR + FLOAT_GIF);
 		String dockIconStr = new String(RESOURCE_DIR + DOCK_GIF);
 		String closeIconStr = new String(RESOURCE_DIR + CLOSE_PNG);
 
 		// create our icon objects
 		floatIcon = new ImageIcon(getClass().getResource(floatIconStr));
 		dockIcon = new ImageIcon(getClass().getResource(dockIconStr));
 		closeIcon = new ImageIcon(getClass().getResource(closeIconStr));
 	}
 
 	/**
 	 * Shows the CytoPanel.
 	 */
 	private void showCytoPanel(CytoPanelState cytoPanelState) {
 		// make ourselves visible
 		setVisible(true);
 
 		//  if our parent is a BiModalSplitPane, show the split
 		Container parent = this.getParent();
 
 		if (parent instanceof BiModalJSplitPane) {
 			BiModalJSplitPane biModalSplitPane = (BiModalJSplitPane) parent;
 			biModalSplitPane.setMode(cytoPanelState, BiModalJSplitPane.MODE_SHOW_SPLIT);
 		}
 	}
 
 	/**
 	 * Hides the CytoPanel.
 	 */
 	private void hideCytoPanel(CytoPanelState cytoPanelState) {
 		// dock ourselves
 		if (isFloating()) {
 			DockCytoPanel();
 		}
 
 		// hide ourselves
 		setVisible(false);
 
 		//  if our Parent Container is a BiModalSplitPane, hide the split
 		Container parent = this.getParent();
 
 		if (parent instanceof BiModalJSplitPane) {
 			BiModalJSplitPane biModalSplitPane = (BiModalJSplitPane) parent;
 			biModalSplitPane.setMode(cytoPanelState, BiModalJSplitPane.MODE_HIDE_SPLIT);
 		}
 	}
 	
 	/**
 	 * Checks to make sure the CytoPanel is within the appropriate dimensions
 	 * by overriding the sizes, if necessary
 	 */
 	private void checkSizes(Component comp) {
 		if (compassDirection == CytoPanelName.WEST) {
 			comp.setMinimumSize(new Dimension(WEST_MIN_WIDTH, WEST_MIN_HEIGHT));
 		} else if (compassDirection == CytoPanelName.SOUTH) {
 			comp.setMinimumSize(new Dimension(SOUTH_MIN_WIDTH, SOUTH_MIN_HEIGHT));
 		} else if (compassDirection == CytoPanelName.EAST) {
 			comp.setMinimumSize(new Dimension(EAST_MIN_WIDTH, EAST_MIN_HEIGHT));
 		}
 	}
 	
 	/**
 	 * Size the divider to the currently selected panel's preferred Size
 	 */
 	private void resizeSelectedComponent() {
 		/* 
 		 * Set default resize behavior based on the currently
 		 * selected panel's preferredSize setting
 		 */
 		Component panel = tabbedPane.getSelectedComponent();
 		// Make sure we're not being notified that we've deleted
 		// the last panel
 		int width = 0;
 		if (panel != null && cytoPanelContainer instanceof JSplitPane) {
 			JSplitPane jsp = (JSplitPane)cytoPanelContainer;
 			// if the panel is 0x0, it's probably not created, yet
 			if (panel.getSize().width == 0 && panel.getSize().height == 0)
 				return;
 
 			if (panel.getPreferredSize() != null) {
 				width = panel.getPreferredSize().width;
 			}
 			
 			if (compassDirection == CytoPanelName.WEST) {
 				if (width > WEST_MAX_WIDTH)
 					width = WEST_MAX_WIDTH;
 				else if (width < WEST_MIN_WIDTH)
 					width = WEST_MIN_WIDTH;
				jsp.setDividerLocation(width+jsp.getInsets().left+jsp.getInsets().right+5);
 			} else if (compassDirection == CytoPanelName.EAST) {
 				if (width > EAST_MAX_WIDTH)
 					width = EAST_MAX_WIDTH;
 				else if (width < EAST_MIN_WIDTH)
 					width = EAST_MIN_WIDTH;
 				
 				jsp.setDividerLocation(jsp.getSize().width
 				                       -jsp.getInsets().right
 				                       -jsp.getInsets().left
 				                       -jsp.getDividerSize()
				                       -width-5);
 			}
 		// TODO: What's the right thing to do with SOUTH?
 		}
 	}
 
 
 	/**
 	 * Constructs this CytoPanel.
 	 */
 	private void constructPanel() {
 		// init our components
 		initLabel();
 		initButton();
 
 		// add label and button components to yet another panel, 
 		// so we can layout properly
 		final JPanel floatDockPanel = new JPanel(new BorderLayout());
 		final JPanel closeAndFloatPanel = new JPanel(new FlowLayout());
 		
 		closeAndFloatPanel.setBackground(FLOAT_PANEL_COLOR);
 		
 		// set float dock panel attributes
 		closeAndFloatPanel.add(floatButton);
 		closeAndFloatPanel.add(closeButton);
 		floatDockPanel.add(floatLabel, BorderLayout.WEST);
 		floatDockPanel.add(closeAndFloatPanel, BorderLayout.EAST);
 		
 		floatDockPanel.setBackground(FLOAT_PANEL_COLOR);
 
 		// set preferred size - we can use float or dock icon dimensions - they are the same
 		final FontMetrics fm = floatLabel.getFontMetrics(floatLabel.getFont());
 		floatDockPanel.setMinimumSize(new Dimension((fm.stringWidth(getTitle()) + floatIcon.getIconWidth())
 				* FLOAT_PANEL_SCALE_FACTOR, floatIcon.getIconHeight()));
 		floatDockPanel.setPreferredSize(new Dimension((fm.stringWidth(getTitle()) + floatIcon.getIconWidth())
 				* FLOAT_PANEL_SCALE_FACTOR, floatIcon.getIconHeight() + 10));
 
 		// use the border layout for this CytoPanel
 		setLayout(new BorderLayout());
 		add(floatDockPanel, BorderLayout.NORTH);
 		add(tabbedPane, BorderLayout.CENTER);
 	}
 
 	/**
 	 * Add a component to the CytoPanel just below the TabbedPane.
 	 *
 	 * @param pComponent    the component to be added.
 	 */
 	public void addComponentToSouth(Component pComponent) {
 		add(pComponent, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Remove a component from the CytoPanel just below the TabbedPane.
 	 *
 	 * @param pComponent  the component to be removed.
 	 */
 	public void removeComponentAtSouth(Component pComponent) {
 		remove(pComponent);
 	}
 
 	/**
 	 * Initializes the label.
 	 */
 	private void initLabel() {
 		floatLabel = new JLabel(getTitle());
 		floatLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
 		floatLabel.setBackground(FLOAT_PANEL_COLOR);
 		floatLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
 	}
 
 	/**
 	 * Initializes the button.
 	 */
 	private void initButton() {
 		// Create Float / Dock Button
 		floatButton = new JButton();
 		floatButton.setIcon(floatIcon);
 		floatButton.setToolTipText(TOOL_TIP_FLOAT);
 		floatButton.setRolloverEnabled(true);
 		
 		// Create close button
 		closeButton = new JButton();
 		closeButton.setIcon(closeIcon);
 		closeButton.setToolTipText(TOOL_TIP_CLOSE);
 		closeButton.setRolloverEnabled(true);
 
 		//  Set 0 Margin All-Around and setBorderPainted to false
 		//  so that button appears as small as possible
 		floatButton.setMargin(new Insets(0, 0, 0, 0));
 		floatButton.setBorder(new EmptyBorder(1, 1, 1, 1));
 		floatButton.setBorderPainted(false);
 		floatButton.setSelected(false);
 		floatButton.setBackground(FLOAT_PANEL_COLOR);
 		
 		closeButton.setMargin(new Insets(0, 0, 0, 0));
 		closeButton.setBorder(new EmptyBorder(1, 1, 1, 1));
 		closeButton.setBorderPainted(false);
 		closeButton.setSelected(false);
 		closeButton.setBackground(FLOAT_PANEL_COLOR);
 
 		// When User Hovers Over Button, highlight it with a gray box
 		floatButton.addMouseListener(new MouseAdapter() {
 			public void mouseEntered(MouseEvent e) {
 				floatButton.setBorder(new LineBorder(Color.GRAY, 1));
 				floatButton.setBorderPainted(true);
 				floatButton.setBackground(Color.LIGHT_GRAY);
 			}
 			public void mouseExited(MouseEvent e) {
 				floatButton.setBorder(new EmptyBorder(1, 1, 1, 1));
 				floatButton.setBorderPainted(false);
 				floatButton.setBackground(FLOAT_PANEL_COLOR);
 			}
 		});
 
 		floatButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (isFloating())
 					DockCytoPanel();
 				else
 					FloatCytoPanel();
 				
 				notifyListeners(NOTIFICATION_STATE_CHANGE);
 			}
 		});
 		
 		// When User Hovers Over Button, highlight it with a gray box
 		closeButton.addMouseListener(new MouseAdapter() {
 			public void mouseEntered(MouseEvent e) {
 				closeButton.setBorder(new LineBorder(Color.GRAY, 1));
 				closeButton.setBorderPainted(true);
 				closeButton.setBackground(Color.LIGHT_GRAY);
 			}
 			public void mouseExited(MouseEvent e) {
 				closeButton.setBorder(new EmptyBorder(1, 1, 1, 1));
 				closeButton.setBorderPainted(false);
 				closeButton.setBackground(FLOAT_PANEL_COLOR);
 			}
 		});
 
 		closeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setState(CytoPanelState.HIDE);
 				notifyListeners(NOTIFICATION_STATE_CHANGE);
 			}
 		});
 	}
 
 	/**
 	 * Float cytoPanel
 	 */
 	private void FloatCytoPanel() {
 		// show ourselves
 		showCytoPanel(CytoPanelState.FLOAT);
 
 		if (!isFloating()) {
 			// new frame to place this CytoPanel
 			externalFrame = new JFrame();
 			
 			// When floating, Cytopanel is always on top.
 			externalFrame.setAlwaysOnTop(true);
 			
 			// add listener to handle when window is closed
 			addWindowListener();
 
 			//  Add CytoPanel to the New External Frame
 			Container contentPane = externalFrame.getContentPane();
 			contentPane.add(this, BorderLayout.CENTER);
 			final Dimension windowSize = this.getSelectedComponent().getPreferredSize();
 			
 			int height = windowSize.height;
 			if(height>EAST_MAX_HEIGHT)
 				windowSize.height = EAST_MAX_HEIGHT;
 			externalFrame.setSize(windowSize);
 			externalFrame.validate();
 
 			// set proper title of frame
 			externalFrame.setTitle(getTitle());
 
 			// set proper button icon/text
 			floatButton.setIcon(dockIcon);
 			floatButton.setToolTipText(TOOL_TIP_DOCK);
 
 			// set float label text
 			floatLabel.setText("");
 			
 			// set location of external frame
 			setLocationOfExternalFrame(externalFrame);
 			// lets show it
 			externalFrame.setVisible(true);
 
 			// set our new state
 			this.cytoPanelState = CytoPanelState.FLOAT;
 
 			// turn off the border
 			floatButton.setBorderPainted(false);
 
 			// re-layout
 			this.validate();
 
 			// SOUTH_WEST is used for manualLayout, it is nested in WEST
 			if (compassDirection == CytoPanelName.SOUTH_WEST) {
 				try {
 					this.getParent().getParent().validate();
 				} catch (Exception e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Dock cytoPanel
 	 */
 	private void DockCytoPanel() {
 		// show ourselves
 		showCytoPanel(CytoPanelState.DOCK);
 
 		if (isFloating()) {
 			// remove cytopanel from external view
 			externalFrame.remove(this);
 
 			// add this cytopanel back to cytopanel container
 			if (cytoPanelContainer == null) {
 				logger.warn("cytoPanelContainer reference has not been set.");
 			}
 
 			cytoPanelContainer.insertCytoPanel(this, compassDirection);
 
 			// dispose of the external frame
 			externalFrame.dispose();
 
 			// set proper button icon/text
 			floatButton.setIcon(floatIcon);
 			floatButton.setToolTipText(TOOL_TIP_FLOAT);
 
 			// set float label text
 			floatLabel.setText(getTitle());
 
 			// set our new state
 			this.cytoPanelState = CytoPanelState.DOCK;
 
 			// turn off the border
 			floatButton.setBorderPainted(false);
 
 			// re-layout
 			this.validate();
 
 			// SOUTH_WEST is used for manualLayout, it is nested in WEST
 			if (compassDirection == CytoPanelName.SOUTH_WEST) {
 				try {
 					this.getParent().getParent().validate();
 				} catch (Exception e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Are we floating ?
 	 */
 	private boolean isFloating() {
 		return (cytoPanelState == CytoPanelState.FLOAT);
 	}
 
 	/**
 	 * Adds the listener to the floating window.
 	 */
 	private void addWindowListener() {
 		externalFrame.addWindowListener(new WindowAdapter() {
 				/**
 				 * Window is Closing.
 				 *
 				 * @param e Window Event.
 				 */
 				public void windowClosing(WindowEvent e) {
 					DockCytoPanel();
 					notifyListeners(NOTIFICATION_STATE_CHANGE);
 				}
 			});
 	}
 
 	/**
 	 * Sets the Location of the External Frame.
 	 *
 	 * @param externalWindow ExternalFrame Object.
 	 */
 	private void setLocationOfExternalFrame(JFrame externalWindow) {
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		Dimension screenDimension = tk.getScreenSize();
 
 		//  Get Absolute Location and Bounds, relative to Screen
 		Rectangle containerBounds = cytoPanelContainer.getBounds();
 		containerBounds.setLocation(cytoPanelContainer.getLocationOnScreen());
 
 		Point p = CytoPanelUtil.getLocationOfExternalFrame(screenDimension, containerBounds,
 		                                                   externalWindow.getSize(),
 		                                                   compassDirection, false);
 
 		externalWindow.setLocation(p);
 		externalWindow.setVisible(true);
 	}
 
 	/**
 	 * Code to notify our listeners of some particular event.
 	 *
 	 * @param notificationType What type of notification to perform.
 	 */
 	private void notifyListeners(int notificationType) {
 			// determine what event to fire
 			switch (notificationType) {
 				case NOTIFICATION_STATE_CHANGE:
 					cyEventHelper.fireEvent(new CytoPanelStateChangedEvent(this, this, cytoPanelState));
 					break;
 
 				case NOTIFICATION_COMPONENT_SELECTED:
 					int selectedIndex = tabbedPane.getSelectedIndex();
 					cyEventHelper.fireEvent(new CytoPanelComponentSelectedEvent(this,this,selectedIndex));
 					break;
 
 				case NOTIFICATION_COMPONENT_ADDED:
 					break;
 
 				case NOTIFICATION_COMPONENT_REMOVED:
 					break;
 			}
 	}
 
 	
 	@Override
 	public Component getThisComponent() {
 		return this;
 	}
 }
