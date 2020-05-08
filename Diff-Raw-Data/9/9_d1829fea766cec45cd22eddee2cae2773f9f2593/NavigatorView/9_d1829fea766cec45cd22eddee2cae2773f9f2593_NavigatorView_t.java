 package imat.gui;
 
 import imat.backend.CategoryNode;
 import imat.backend.CustomCategories;
 import imat.backend.CustomProductLists;
 import imat.backend.ShopModel;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.UIManager;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import se.chalmers.ait.dat215.project.IMatDataHandler;
 import se.chalmers.ait.dat215.project.Product;
 
 public class NavigatorView extends JPanel implements ActionListener, PropertyChangeListener, KeyListener {
 
 	private JTree tree;
 	private static ProductsView view;
 	private JLabel searchLabel;
 	private JTextField searchField;
 	private static CustomCategories currentCategory;
 	private JLabel favouriteLabel;
 
 	private ShopModel model;
 	private final String AC_SEARCH = "search";
 	
 	/**
 	 * Create the panel.
 	 */
 	public NavigatorView(ShopModel model) {
 		this.model = model;
 		this.model.addPropertyChangeListeter(this);
 		
 		CustomProductLists.generateCustomLists();
 		setPreferredSize(new Dimension(250, 600));
 		view = new ProductsView(this.model);
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
 		for (CustomCategories c : CustomCategories.values()) {
 			if (c.isMain()) {
 				CategoryNode tmp = new CategoryNode(c);
 				if (c.isParent()) {
 					for (CustomCategories t : CustomCategories
 							.getSubCategories(c)) {
 						tmp.add(new CategoryNode(t));
 					}
 				}
 				root.add(tmp);
 			}
 
 		}
 
 		tree = new JTree(root);
 		tree.setOpaque(false);
 		tree.setModel(new DefaultTreeModel(root));
 		tree.setPreferredSize(new Dimension(247, 20));
 		tree.setMaximumSize(new Dimension(1000, 1000));
 		tree.addMouseMotionListener(new TreeMouseMotionListener());
 		tree.setBackground(UIManager.getColor("Panel.background"));
 		tree.addMouseListener(new TreeMouseListener());
 		tree.addTreeExpansionListener(new TreeTreeExpansionListener());
 		tree.setFont(new Font("SansSerif", Font.BOLD, 12));
 		tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 
 		tree.setRootVisible(false);
 		tree.setToggleClickCount(1);
 		tree.putClientProperty("JTree.lineStyle", "None");
 		DefaultTreeCellRenderer tcr = (DefaultTreeCellRenderer) tree
 				.getCellRenderer();
 		tcr.setLeafIcon(null);
 		tcr.setOpenIcon(null);
 		tcr.setClosedIcon(null);
 		tcr.setBackgroundNonSelectionColor(this.getBackground());
 		tcr.setFont(new Font("SansSerif", Font.BOLD, 16));
 
 		searchLabel = new JLabel("Hitta mat");
 
 		searchField = new JTextField();
 		searchField.setFont(new Font("SansSerif", Font.ITALIC, 12));
 		searchField.setColumns(10);
 		searchField.setActionCommand(AC_SEARCH);
 		searchField.addActionListener(this);
 		searchField.addKeyListener(this);
 		
 		favouriteLabel = new JLabel("Favoriter");
 		favouriteLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		favouriteLabel.setOpaque(true);
 		favouriteLabel.addMouseListener(new FavouriteLabelMouseListener());
 		favouriteLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
 		
 		GroupLayout groupLayout = new GroupLayout(this);
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addComponent(tree, GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
 						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 							.addComponent(searchField, GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
 							.addComponent(searchLabel))
 						.addComponent(favouriteLabel))
 					.addContainerGap())
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(searchLabel)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(searchField, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
 					.addGap(18)
 					.addComponent(favouriteLabel)
 					.addGap(18)
 					.addComponent(tree, GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
 					.addContainerGap())
 		);
 		setLayout(groupLayout);
 		DefaultTreeModel tmp = (DefaultTreeModel) tree.getModel();
 		tmp.reload();
 
 	}
 
	public static CustomCategories getCurrentCategory() {
		return currentCategory;
	}
 	private class TreeTreeExpansionListener implements TreeExpansionListener {
 		public void treeCollapsed(TreeExpansionEvent arg0) {
 		}
 
 		public void treeExpanded(TreeExpansionEvent arg0) {
 			TreePath path = (TreePath) arg0.getPath();
 			for (int i = 0; i < tree.getRowCount(); i++) {
 				if (tree.getPathForRow(i) != path) {
 					tree.collapseRow(i);
 				}
 
 			}
 		}
 	}
 
 	private class TreeMouseListener extends MouseAdapter {
 		@Override
 		public void mousePressed(MouseEvent e) {
 			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
 			if (path != null) {
 				CategoryNode node = (CategoryNode) tree
 						.getLastSelectedPathComponent();
 				if (node.getChildCount() == 0) {
 					if (path.getPathCount() == 2) {
 						for (int i = 0; i < tree.getRowCount(); i++) {
 							tree.collapseRow(i);
 						}
 					}
 					currentCategory = node.getCustomCategory();
 					if (currentCategory.getProducts() != null) {
 						view.setProducts(currentCategory.getProducts());
 					}
 
 				}
 			}
 
 		}
 	}
 
 	private class TreeMouseMotionListener extends MouseMotionAdapter {
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			TreePath path = (TreePath) tree.getPathForLocation(e.getX(),
 					e.getY());
 			if (path != null) {
 				tree.setSelectionPath(path);
 			}
 		}
 	}
 	private class FavouriteLabelMouseListener extends MouseAdapter {
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			tree.setSelectionPath(null);
 			favouriteLabel.setBackground(new Color(184, 207, 229));
 		}
 		@Override
 		public void mouseExited(MouseEvent e) {
 			favouriteLabel.setBackground(new Color(238, 238, 238));
 		}
 		@Override
 		public void mousePressed(MouseEvent e) {
 			view.setProducts(IMatDataHandler.getInstance().favorites());
 		}
 	}
 
 	public ProductsView getProductsView() {
 		return view;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(AC_SEARCH)) {
 			Object o = e.getSource();
 			String searchString = "";
 			if (o instanceof JTextField) {
 				searchString = ((JTextField) o).getText();
 			} else if (o instanceof SuggestionLabel) {
 				searchString = ((SuggestionLabel) o).getText();
 			}
 			List<Product> res = IMatDataHandler.getInstance().findProducts(searchString);
 			if (res.size() == 0) {
 				model.fuzzySearch(searchString);
 			} else {
 				getProductsView().setProducts(res, searchString);
 			}
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName().equals("fuzzySearch")) {
 			Object oList = evt.getNewValue();
 			if (oList instanceof List<?>) {
 				List<SuggestionLabel> suggestions= new ArrayList<SuggestionLabel>();
 				for (String s : (List<String>) oList) {
 					suggestions.add(new SuggestionLabel(s, this));
 				}
 				// TODO: Show notification panel!
 				// notify(suggestions);
 			}
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {}
 
 	@Override
 	public void keyReleased(KeyEvent evt) {
 		for(ActionListener a: searchField.getActionListeners()) {
 		    a.actionPerformed(new ActionEvent(searchField, ActionEvent.ACTION_PERFORMED, AC_SEARCH) {
 		    	
 		    });
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {}
 }
