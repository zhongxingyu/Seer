 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
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
 package cytoscape.visual.ui;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.util.CyColorChooser;
 import cytoscape.visual.GlobalAppearanceCalculator;
 import cytoscape.visual.NodeAppearanceCalculator;
 import cytoscape.visual.VisualPropertyType;
 import static cytoscape.visual.VisualPropertyType.*;
 
 import cytoscape.visual.ui.icon.VisualPropertyIcon;
 
 import org.jdesktop.swingx.JXList;
 import org.jdesktop.swingx.border.DropShadowBorder;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.JColorChooser;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 
 
 /**
  * Dialog for editing default visual property values.<br>
  * This is a modal dialog.
  *
  * <p>
  *     Basic idea is the following:
  *  <ul>
  *      <li>Build dummy network with 2 nodes and 1 edge.</li>
  *      <li>Edit the default appearence of the dummy network</li>
  *      <li>Create a image from the dummy.</li>
  *  </ul>
  * </p>
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author kono
  */
 public class DefaultAppearenceBuilder extends JDialog {
 	private static final Set<VisualPropertyType> EDGE_PROPS;
 	private static final Set<VisualPropertyType> NODE_PROPS;
 	private static DefaultAppearenceBuilder dab = null;
 	private final NodeAppearanceCalculator nac = Cytoscape.getVisualMappingManager().getVisualStyle()
 	                                                      .getNodeAppearanceCalculator();
 
 	static {
 		EDGE_PROPS = new TreeSet<VisualPropertyType>(VisualPropertyType.getEdgeVisualPropertyList());
 		NODE_PROPS = new TreeSet<VisualPropertyType>(VisualPropertyType.getNodeVisualPropertyList());
 	}
 
 	/**
 	 * Creates a new DefaultAppearenceBuilder object.
 	 *
 	 * @param parent DOCUMENT ME!
 	 * @param modal DOCUMENT ME!
 	 */
 	public DefaultAppearenceBuilder(Frame parent, boolean modal) {
 		super(parent, modal);
 		initComponents();
 		buildList();
 
 		this.addComponentListener(new ComponentAdapter() {
 				public void componentResized(ComponentEvent e) {
 					mainView.updateView();
 				}
 			});
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param parent DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public static JPanel showDialog(Frame parent) {
 		buildDefaultViewDialog(parent);
 		dab.setLocationRelativeTo(parent);
 		dab.setSize(900, 400);
 		dab.lockSize();
 		dab.lockNodeSizeCheckBox.setSelected(dab.nac.getNodeSizeLocked());
 		dab.mainView.updateView();
 		dab.setLocationRelativeTo(Cytoscape.getDesktop());
 		dab.setVisible(true);
 
 		return dab.getPanel();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public static JPanel getDefaultView(String vsName) {
 		buildDefaultViewDialog(null);
 		Cytoscape.getVisualMappingManager().setVisualStyle(vsName);
 		dab.mainView.updateBackgroungColor(Cytoscape.getVisualMappingManager().getVisualStyle()
 		                                            .getGlobalAppearanceCalculator()
 		                                            .getDefaultBackgroundColor());
 		dab.mainView.updateView();
 
 		return dab.getPanel();
 	}
 
 	private static void buildDefaultViewDialog(Frame component) {
 		dab = new DefaultAppearenceBuilder(component, true);
 		dab.mainView.createDummyNetworkView();
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
 	private void initComponents() {
 		jXPanel1 = new org.jdesktop.swingx.JXPanel();
 		mainView = new DefaultViewPanel();
 		jXTitledPanel1 = new org.jdesktop.swingx.JXTitledPanel();
 		defaultObjectTabbedPane = new javax.swing.JTabbedPane();
 		nodeScrollPane = new javax.swing.JScrollPane();
 		nodeList = new JXList();
 		edgeList = new JXList();
 		edgeScrollPane = new javax.swing.JScrollPane();
 		globalScrollPane = new javax.swing.JScrollPane();
 		lockNodeSizeCheckBox = new javax.swing.JCheckBox();
 		applyButton = new javax.swing.JButton();
 
 		globalList = new JXList();
 
 		cancelButton = new javax.swing.JButton();
 		cancelButton.setVisible(false);
 
 		lockNodeSizeCheckBox.setOpaque(false);
 
 		nodeList.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					listActionPerformed(e);
 				}
 			});
 
 		edgeList.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					listActionPerformed(e);
 				}
 			});
 
 		globalList.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					globalListActionPerformed(e);
 				}
 			});
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Default Appearence for "
 		         + Cytoscape.getVisualMappingManager().getVisualStyle().getName());
 		mainView.setBorder(new javax.swing.border.LineBorder(java.awt.Color.darkGray, 1, true));
 
 		org.jdesktop.layout.GroupLayout jXPanel2Layout = new org.jdesktop.layout.GroupLayout(mainView);
 		mainView.setLayout(jXPanel2Layout);
 		jXPanel2Layout.setHorizontalGroup(jXPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                .add(0, 300, Short.MAX_VALUE));
 		jXPanel2Layout.setVerticalGroup(jXPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                              .add(0, 237, Short.MAX_VALUE));
 
 		jXTitledPanel1.setTitle("Default Visual Properties");
 		jXTitledPanel1.setTitlePainter(new BasicGradientPainter(new Point2D.Double(.2d, 0),
 		                                                        new Color(Color.gray.getRed(),
 		                                                                  Color.gray.getGreen(),
 		                                                                  Color.gray.getBlue(), 100),
 		                                                        new Point2D.Double(.8d, 0),
 		                                                        Color.WHITE));
 		jXTitledPanel1.setTitleFont(new java.awt.Font("SansSerif", 1, 12));
 		jXTitledPanel1.setMinimumSize(new java.awt.Dimension(300, 27));
 		jXTitledPanel1.setPreferredSize(new java.awt.Dimension(300, 27));
 		defaultObjectTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
 
 		nodeScrollPane.setViewportView(nodeList);
 		edgeScrollPane.setViewportView(edgeList);
 		globalScrollPane.setViewportView(globalList);
 
 		defaultObjectTabbedPane.addTab("Node", nodeScrollPane);
 		defaultObjectTabbedPane.addTab("Edge", edgeScrollPane);
 		defaultObjectTabbedPane.addTab("Global", globalScrollPane);
 
 		org.jdesktop.layout.GroupLayout jXTitledPanel1Layout = new org.jdesktop.layout.GroupLayout(jXTitledPanel1
 		                                                                                           .getContentContainer());
 		jXTitledPanel1.getContentContainer().setLayout(jXTitledPanel1Layout);
 		jXTitledPanel1Layout.setHorizontalGroup(jXTitledPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                            .add(defaultObjectTabbedPane,
 		                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                 250, Short.MAX_VALUE));
 		jXTitledPanel1Layout.setVerticalGroup(jXTitledPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                          .add(defaultObjectTabbedPane,
 		                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                               243, Short.MAX_VALUE));
 
 		lockNodeSizeCheckBox.setFont(new java.awt.Font("SansSerif", 1, 12));
 		lockNodeSizeCheckBox.setText("Lock Node Width/Height");
 		lockNodeSizeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		lockNodeSizeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		lockNodeSizeCheckBox.setSelected(nac.getNodeSizeLocked());
 		lockNodeSizeCheckBox.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					lockSize();
 				}
 			});
 
 		applyButton.setText("Apply");
 		applyButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					
 					Cytoscape.getVisualMappingManager().setNetworkView(Cytoscape.getCurrentNetworkView());
 					Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 					dispose();
 				}
 			});
 
 		cancelButton.setText("Cancel");
 		cancelButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					// TODO Auto-generated method stub
 					dispose();
 				}
 			});
 
 		org.jdesktop.layout.GroupLayout jXPanel1Layout = new org.jdesktop.layout.GroupLayout(jXPanel1);
 		jXPanel1.setLayout(jXPanel1Layout);
 		jXPanel1Layout.setHorizontalGroup(jXPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                .add(jXPanel1Layout.createSequentialGroup()
 		                                                                   .addContainerGap()
 		                                                                   .add(jXPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                                      .add(jXPanel1Layout.createSequentialGroup()
 		                                                                                                         .add(lockNodeSizeCheckBox,
 		                                                                                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                              138,
 		                                                                                                              Short.MAX_VALUE)
 		                                                                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                         .add(cancelButton)
 		                                                                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                         .add(applyButton))
 		                                                                                      .add(mainView,
 		                                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                           Short.MAX_VALUE))
 		                                                                   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                   .add(jXTitledPanel1,
 		                                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                        198, Short.MAX_VALUE)
 		                                                                   .add(12, 12, 12)));
 		jXPanel1Layout.setVerticalGroup(jXPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                              .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                   jXPanel1Layout.createSequentialGroup()
 		                                                                 .addContainerGap()
 		                                                                 .add(jXPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 		                                                                                    .add(org.jdesktop.layout.GroupLayout.LEADING,
 		                                                                                         jXTitledPanel1,
 		                                                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                         270,
 		                                                                                         Short.MAX_VALUE)
 		                                                                                    .add(jXPanel1Layout.createSequentialGroup()
 		                                                                                                       .add(mainView,
 		                                                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                            Short.MAX_VALUE)
 		                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                       .add(jXPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                                                          .add(lockNodeSizeCheckBox)
 		                                                                                                                          .add(cancelButton)
 		                                                                                                                          .add(applyButton))))
 		                                                                 .addContainerGap()));
 
 		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                .add(jXPanel1,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE));
 		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                              .add(jXPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                   org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                   Short.MAX_VALUE));
 		pack();
 	} // </editor-fold>
 
 	private void listActionPerformed(MouseEvent e) {
 		if (e.getClickCount() == 1) {
 			Object newValue = null;
 
 			final JList list;
 
 			try {
 				if (e.getSource() == nodeList) {
 					list = nodeList;
 				} else {
 					list = edgeList;
 				}
 
 				newValue = VizMapperMainPanel.showValueSelectDialog((VisualPropertyType) list
 				                                                                                                                                                                                                                                                                                                                                                              .getSelectedValue(),
 				                                                    this);
 				VizMapperMainPanel.apply(newValue, (VisualPropertyType) list.getSelectedValue());
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 
 			buildList();
 			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 			mainView.updateView();
 			mainView.repaint();
 		}
 	}
 
 	private void globalListActionPerformed(MouseEvent e) {
 		if (e.getClickCount() == 1) {
 			final String selected = (String) globalList.getSelectedValue();
 			Color newColor = CyColorChooser.showDialog(this, "Choose new color.", Color.white);
 
 			try {
 				Cytoscape.getVisualMappingManager().getVisualStyle().getGlobalAppearanceCalculator()
 				         .setDefaultColor(selected, newColor);
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 
 			buildList();
 			Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 			if (selected.equals("Background Color")) {
 				Cytoscape.getVisualMappingManager().applyGlobalAppearances();
 				mainView.updateBackgroungColor(newColor);
 			}
 
 			mainView.updateView();
 			mainView.repaint();
 		}
 	}
 
 	// Variables declaration - do not modify
 	private javax.swing.JButton applyButton;
 	private javax.swing.JButton cancelButton;
 	private javax.swing.JCheckBox lockNodeSizeCheckBox;
 	private javax.swing.JScrollPane nodeScrollPane;
 	private javax.swing.JScrollPane edgeScrollPane;
 	private javax.swing.JScrollPane globalScrollPane;
 	private javax.swing.JTabbedPane defaultObjectTabbedPane;
 	private JXList nodeList;
 	private JXList edgeList;
 	private JXList globalList;
 	private org.jdesktop.swingx.JXPanel jXPanel1;
 
 	//	private org.jdesktop.swingx.JXPanel jXPanel2;
 	private org.jdesktop.swingx.JXTitledPanel jXTitledPanel1;
 
 	// End of variables declaration
 	protected DefaultViewPanel mainView;
 
 	//	 End of variables declaration
 	private JPanel getPanel() {
 		return mainView;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 */
 	private void buildList() {
 		List<Icon> nodeIcons = new ArrayList<Icon>();
 		List<Icon> edgeIcons = new ArrayList<Icon>();
 		List<Icon> globalIcons = new ArrayList<Icon>();
 
 		DefaultListModel model = new DefaultListModel();
 		nodeList.setModel(model);
 
 		for (VisualPropertyType type : NODE_PROPS) {
 			final VisualPropertyIcon nodeIcon = (VisualPropertyIcon) (type.getVisualProperty()
 			                                                              .getDefaultIcon());
 			nodeIcon.setLeftPadding(15);
 			model.addElement(type);
 			nodeIcons.add(nodeIcon);
 		}
 
 		DefaultListModel eModel = new DefaultListModel();
 		edgeList.setModel(eModel);
 
 		for (VisualPropertyType type : EDGE_PROPS) {
 			final VisualPropertyIcon edgeIcon = (VisualPropertyIcon) (type.getVisualProperty()
 			                                                              .getDefaultIcon());
 
 			if (edgeIcon != null) {
 				edgeIcon.setLeftPadding(15);
 				eModel.addElement(type);
 				edgeIcons.add(edgeIcon);
 			}
 		}
 
 		GlobalAppearanceCalculator gac = Cytoscape.getVisualMappingManager().getVisualStyle()
 		                                          .getGlobalAppearanceCalculator();
 		DefaultListModel gModel = new DefaultListModel();
 		globalList.setModel(gModel);
 
 		for (String name : gac.getGlobalAppearanceNames()) {
 			try {
 				globalIcons.add(new GlobalIcon(name, gac.getDefaultColor(name)));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			gModel.addElement(name);
 		}
 
 		nodeList.setCellRenderer(new VisualPropCellRenderer(nodeIcons));
 		edgeList.setCellRenderer(new VisualPropCellRenderer(edgeIcons));
 		globalList.setCellRenderer(new VisualPropCellRenderer(globalIcons));
 
 		mainView.updateView();
 		mainView.repaint();
 	}
 
 	private void lockSize() {
 		if (lockNodeSizeCheckBox.isSelected()) {
 			NODE_PROPS.remove(NODE_WIDTH);
 			NODE_PROPS.remove(NODE_HEIGHT);
 			NODE_PROPS.add(NODE_SIZE);
 			nac.setNodeSizeLocked(true);
 		} else {
 			NODE_PROPS.add(NODE_WIDTH);
 			NODE_PROPS.add(NODE_HEIGHT);
 			NODE_PROPS.remove(NODE_SIZE);
 			nac.setNodeSizeLocked(false);
 		}
 
 		buildList();
 		mainView.updateView();
 		repaint();
 	}
 
 	class VisualPropCellRenderer extends JLabel implements ListCellRenderer {
 		private final Font SELECTED_FONT = new Font("SansSerif", Font.ITALIC, 14);
 		private final Font NORMAL_FONT = new Font("SansSerif", Font.BOLD, 12);
 		private final Color SELECTED_COLOR = new Color(10, 50, 180, 20);
 		private final Color SELECTED_FONT_COLOR = new Color(0, 150, 255, 150);
 		private final List<Icon> icons;
 
 		public VisualPropCellRenderer(List<Icon> icons) {
 			this.icons = icons;
 			setOpaque(true);
 		}
 
 		public Component getListCellRendererComponent(JList list, Object value, int index,
 		                                              boolean isSelected, boolean cellHasFocus) {
 			final VisualPropertyIcon icon;
 
 			if (icons.size() > index) {
 				icon = (VisualPropertyIcon) icons.get(index);
 			} else
 				icon = null;
 
 			setText(value.toString());
 			setIcon(icon);
 			setFont(isSelected ? SELECTED_FONT : NORMAL_FONT);
 
 			this.setVerticalTextPosition(SwingConstants.CENTER);
 			this.setVerticalAlignment(SwingConstants.CENTER);
 			this.setIconTextGap(55);
 
 			if (value instanceof VisualPropertyType
 			    && (((VisualPropertyType) value).getDataType() == String.class)) {
 				final Object defVal = ((VisualPropertyType) value).getDefault(Cytoscape.getVisualMappingManager()
 				                                                                       .getVisualStyle());
 
 				if (defVal != null) {
 					this.setToolTipText((String) defVal);
 				}
 			}
 
 			setBackground(isSelected ? SELECTED_COLOR : list.getBackground());
 			setForeground(isSelected ? SELECTED_FONT_COLOR : list.getForeground());
 
 			if (icon != null) {
 				setPreferredSize(new Dimension(250, icon.getIconHeight() + 12));
 			}
 
 			this.setBorder(new DropShadowBorder());
 
 			return this;
 		}
 	}
 
 	/*
 	 * Draw global color icon
 	 */
 	class GlobalIcon extends VisualPropertyIcon {
 		public GlobalIcon(String name, Color color) {
 			super(name, color);
 		}
 
 		public void paintIcon(Component c, Graphics g, int x, int y) {
 			Graphics2D g2d = (Graphics2D) g;
 
 			g2d.setColor(color);
 			g2d.fillRect(5, 3, 50, 32);
 
 			g2d.setStroke(new BasicStroke(1f));
 			g2d.setColor(Color.DARK_GRAY);
 			g2d.drawRect(5, 3, 50, 32);
 		}
 	}
 }
