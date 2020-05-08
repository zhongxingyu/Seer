 package gui;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.JLabel;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.FlowLayout;
 
 public class LessonsTab {
 
 	private JPanel LessonsTab;
 	private String Selection;
 
 	/**
 	 * Create the application.
 	 */
 	public LessonsTab() 
 	{
 		LessonsTab = new JPanel();
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 * @return 
 	 */
 	public JPanel initialize() {
 		LessonsTab.setBounds(100, 100, 450, 300);
 		LessonsTab.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(44dlu;pref)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(39dlu;pref)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("8dlu:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(7dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(10dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(9dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(9dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JButton StartLessonButton = new JButton("Start Lesson");
 		StartLessonButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(Selection.startsWith("Chapter") || Selection.compareTo("Lessons") == 0 || Selection == null)
 					return;
 				else
 				{
 					LessonWindow NewWindow = new LessonWindow(Selection);
 					NewWindow.OpenWindow();
 				}
 			}
 		});
 		LessonsTab.add(StartLessonButton, "2, 2, 3, 1");
 		
 		JScrollPane PreviewPane = new JScrollPane();
 		LessonsTab.add(PreviewPane, "6, 2, 17, 15, fill, fill");
 		
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.WHITE);
 		PreviewPane.setViewportView(panel);
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		String imgStr = "Lessons/LessonsPreview.png";
 		final ImageIcon LessonsTitle = new ImageIcon(imgStr);
 		final JLabel LessonPreviewLabel = new JLabel("", LessonsTitle, JLabel.CENTER);
 		panel.add(LessonPreviewLabel);
 		
 		JScrollPane TreePane = new JScrollPane();
 		LessonsTab.add(TreePane, "2, 4, 3, 13, fill, fill");
 		
 		final JTree LessonsTree = new JTree();
 		LessonsTree.setModel(new DefaultTreeModel(
 			new DefaultMutableTreeNode("Lessons") {
 				{
 					DefaultMutableTreeNode node_1;
 					node_1 = new DefaultMutableTreeNode("Chapter 1");
 						node_1.add(new DefaultMutableTreeNode("Lesson 1.0"));
						node_1.add(new DefaultMutableTreeNode("Lesson 1.1"));
 						node_1.add(new DefaultMutableTreeNode("History of Lisp"));
 						node_1.add(new DefaultMutableTreeNode("Modern Lisp"));
 						node_1.add(new DefaultMutableTreeNode("Future of Lisp"));
 					add(node_1);
 					node_1 = new DefaultMutableTreeNode("Chapter 2");
 						node_1.add(new DefaultMutableTreeNode("Lesson 2.1"));
 						node_1.add(new DefaultMutableTreeNode("Lesson 2.2"));
 						node_1.add(new DefaultMutableTreeNode("Lesson 2.3"));
 						node_1.add(new DefaultMutableTreeNode("Lesson 2.4"));
 					add(node_1);
 					node_1 = new DefaultMutableTreeNode("Chapter 3");
 						node_1.add(new DefaultMutableTreeNode("Lesson 3.1"));
 						node_1.add(new DefaultMutableTreeNode("Lesson 3.2"));
 						node_1.add(new DefaultMutableTreeNode("Lesson 3.3"));
 						node_1.add(new DefaultMutableTreeNode("Lesson 3.4"));
 					add(node_1);
 				}
 			}
 		));
 		TreePane.setViewportView(LessonsTree);
 		
 		LessonsTree.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent e) {
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode) LessonsTree.getLastSelectedPathComponent();	
 				Object nodeInfo = node.getUserObject();
 				Selection = nodeInfo.toString();
				String imgStr = "Lessons/" + Selection + "Preview" + ".png";
 				final ImageIcon LessonPreview = new ImageIcon(imgStr);
 				LessonPreviewLabel.setIcon(LessonPreview);
 			}
 		});
 		return LessonsTab;
 	}
 }
