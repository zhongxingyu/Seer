 package views;
 
 import infrastructure.IterableExtensions;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTree;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 import controllers.IProjectController;
 
 public class ProjectView extends JPanel implements IProjectView {
 
 	private static final long serialVersionUID = 281050945259788431L;
 	private JTree tree;
 	private JButton btnOpen;
 	private JButton btnCreate;
 	private JButton btnValidate;
 	private JButton btnTransform;
 	private IProjectController projectController;
 	private MouseAdapter transformToDomainDiagram;
 	private MouseAdapter reloadDiagram;
 
 	/**
 	 * Create the panel.
 	 */
 	public ProjectView() {
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow")}));
 		
 		this.btnCreate = new JButton("Create");
 		this.add(this.btnCreate, "2, 2");
 		
 		this.btnOpen = new JButton("Open");
 		this.add(this.btnOpen, "4, 2");
 		
 		this.btnValidate = new JButton("Validate");
 		this.add(btnValidate, "6, 2");
 		
 		this.btnTransform = new JButton("Transform");
 		this.add(this.btnTransform, "2, 4");
 
 		this.tree = new JTree();
 		tree.setModel(null);
 		this.add(this.tree, "2, 6, 6, 1, fill, fill");
 	}
 
 	@Override
 	public void setController(IProjectController controller) {
 		this.projectController = controller;
 		
 		this.btnCreate.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e){
 				String name = JOptionPane.showInputDialog(null, "Provide the project's name", "New Project", JOptionPane.QUESTION_MESSAGE);
 				if (name != null){
 					projectController.createProject(name);
 					refreshTree(projectController.getProjectTree());
 				}
 			}	
 		});
 		
 		this.btnOpen.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e){
 				String name = JOptionPane.showInputDialog(null, "Provide the project's name", "Open Project", JOptionPane.QUESTION_MESSAGE);
 				if (name != null){
 					try {
 						projectController.openProject(name);
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 					refreshTree(projectController.getProjectTree());
 				}
 			}	
 		});
 		
 		this.btnValidate.addMouseListener(new MouseAdapter() {
 			private String[] toleranceOptions = {"Low", "Medium", "High"};
 			
 			@Override
 			public void mouseClicked(MouseEvent e){
 				
 				String tolerance = (String)JOptionPane.showInputDialog(null, "Select the tolerance level", "Project Validation",
 						JOptionPane.QUESTION_MESSAGE, null, toleranceOptions, "Low");
 				if (tolerance != null){
 					int toleranceLevel = IterableExtensions.getListOf(toleranceOptions).indexOf(tolerance) + 1;
 					projectController.validateProject(toleranceLevel);
 				}
 			}	
 		});
 
 		transformToDomainDiagram = new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e){
 				try {
 					projectController.navigateToDomainDiagram();
 					btnTransform.removeMouseListener(transformToDomainDiagram);
 					btnTransform.addMouseListener(reloadDiagram);
					btnTransform.setText("EDR");
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		};
 		reloadDiagram = new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e){
 				try {
 					projectController.showDiagram();
 					btnTransform.removeMouseListener(reloadDiagram);
 					btnTransform.addMouseListener(transformToDomainDiagram);
 					btnTransform.setText("Transform");
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		};
 		this.btnTransform.addMouseListener(transformToDomainDiagram);	
 
 		this.tree.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				TreePath path = tree.getPathForLocation(e.getX(),e.getY());
 				if (e.getClickCount() == 2) {
 					projectController.changeElement(path);					
 				}
 			}
 		});
 
 		this.tree.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				TreePath path = tree.getSelectionPath();
 				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
 					projectController.deleteElement(path);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void refreshTree(TreeModel projectTree) {
 		tree.setModel(projectTree);
 	}
 }
