 package quizsystem.gui;
 
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import quizsystem.gui.abs.AbstractPanel;
 import quizsystem.gui.abs.AbstractWindow;
 import quizsystem.gui.controller.StudentController;
 import quizsystem.gui.menus.StudentWindowMenu;
 import quizsystem.gui.model.StudentModel;
 import quizsystem.gui.panels.previewpanel.PreviewPanel;
 
 public class StudentWindow extends AbstractWindow {
 	
 	private static final long serialVersionUID = 1L;
 
 	private StudentWindowMenu windowMenu;
 	private PreviewPanel previewPanel;
 	
 	public StudentWindow() {
 		super("Student");
 	}
 
 	@Override
 	public void init() {
 		this.panels = new ArrayList<AbstractPanel>();
 		windowMenu = new StudentWindowMenu();		
 		model = new StudentModel();
 		controller = new StudentController();
 		controller.addModel(model);
 		
 		setJMenuBar(windowMenu);
 		
		//previewPanel = new PreviewPanel((StudentModel)model, (StudentController)controller);
 		JPanel panel2 = new JPanel();
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
 				previewPanel.getView(), panel2);
 		
 		this.addPanel(previewPanel);
 		this.add(splitPane);
 		
 	}
 
 	@Override
 	public void dispose() {
 		System.exit(0);
 	}
 }
 
