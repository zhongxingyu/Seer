 package org.bushido.collections;
 
 import java.awt.BorderLayout;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.HierarchyListener;
 import java.util.Enumeration;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTextPane;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.border.Border;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
import org.bushido.tree.SwingTreeModel;
import org.bushido.tree.Tree;
 
 public final class VisualTreeDemo extends JFrame {
 
 	private static final long serialVersionUID = -5751363792809578164L;
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				final VisualTreeDemo frame = new VisualTreeDemo();
 				frame.setSize(640, 480);
 				frame.setLocationRelativeTo(null);
 				frame.setVisible(true);
 			}
 		});
 	}
 
 	private VisualTreeDemo() {
 		super("Collections visual demonstration");
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		builUI();
 	}
 
 	private void builUI() {
 		setLayout(new BorderLayout());
 		final Tree<String> tree = testTree();
 		final JSplitPane treePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		treePane.addHierarchyListener(new HierarchyListener() {
 			@Override
 			public void hierarchyChanged(HierarchyEvent e) {
 				treePane.setDividerLocation(0.5d);
 			}
 		});
 		treePane.setLeftComponent(treeView(tree));
 		treePane.setRightComponent(treeTextPane(tree));
 		this.add(treePane, BorderLayout.CENTER);
 	}
 
 	private JPanel treeTextPane(final Tree<String> tree) {
 		final StyledDocument doc = new DefaultStyledDocument();
 		final JTextPane text = new JTextPane(doc);
 		text.setText(tree.toString());
 		final MutableAttributeSet attrSet = new SimpleAttributeSet();
 		StyleConstants.setLineSpacing(attrSet, -0.3f);
 		StyleConstants.setFontSize(attrSet, 18);
 		StyleConstants.setBold(attrSet, true);
 		doc.setParagraphAttributes(0, doc.getLength(), attrSet, true);
 		text.setEditable(true);
 		return demoPanel("Tree collection displayed as String", text);
 	}
 
 	private JPanel treeView(final Tree<String> tree) {
 		final JTree result = new JTree(SwingTreeModel.fromTree(tree));
 		result.setLargeModel(true);
 
 		// expand tree
 		expandAll(result, new TreePath(result.getModel().getRoot()), true) ;
 
 		return demoPanel("Tree collection displayed with JTree", result);
 	}
 
 	private static void expandAll(JTree tree, TreePath parent, boolean expand) {
 		// Traverse children
 		TreeNode node = (TreeNode) parent.getLastPathComponent();
 		if (node.getChildCount() >= 0) {
 			for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
 				TreeNode n = (TreeNode) e.nextElement();
 				TreePath path = parent.pathByAddingChild(n);
 				expandAll(tree, path, expand);
 			}
 		}
 
 		// Expansion or collapse must be done bottom-up
 		if (expand) {
 			tree.expandPath(parent);
 		} else {
 			tree.collapsePath(parent);
 		}
 	}
 
 	private JPanel demoPanel(final String title, final JComponent component) {
 		final JPanel result = new JPanel(new BorderLayout());
 		final Border border = BorderFactory.createTitledBorder(title);
 		result.setBorder(border);
 		result.add(component, BorderLayout.CENTER);
 		return result;
 	}
 
 	private Tree<String> testTree() {
 		final Tree<String> result = new Tree<String>("root");
 		// level 1
 		Tree.Node<String> next = result.appendChild(result.getRoot(),
 				"level1-0");
 		result.appendChild(result.getRoot(), "level1-1");
 		result.appendChild(result.appendChild(result.getRoot(), "level1-2"),
 				"level2-2");
 		result.appendChild(result.appendChild(result.getRoot(), "level1-3"),
 				"level2-3");
 		// level 2
 		result.appendChild(next, "level2-0");
 		next = result.appendChild(next, "level2-1");
 		Tree.Node<String> level2_1 = next;
 		// level 3
 		next = result.appendChild(next, "level3-0");
 		Tree.Node<String> level3_0 = next;
 		result.appendChild(next, "level4-0");
 		next = result.appendChild(next, "level4-1");
 		next = result.appendChild(next, "level5-0");
 		result.appendChild(level3_0, "level4-2");
 		result.appendChild(level2_1, "level3-1");
 		return result;
 	}
 
 }
