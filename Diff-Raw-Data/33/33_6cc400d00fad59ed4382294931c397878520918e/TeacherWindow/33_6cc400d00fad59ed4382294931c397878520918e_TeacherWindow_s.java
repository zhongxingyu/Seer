 package quizsystem.gui;
 
 import java.awt.FlowLayout;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import quizsystem.gui.abs.AbstractWindow;
 import quizsystem.gui.controller.TeacherController;
 import quizsystem.gui.menus.TeacherWindowMenu;
import quizsystem.gui.model.TeacherModel;
 import quizsystem.gui.panels.previewpanel.PreviewPanel;
 import quizsystem.gui.abs.AbstractPanel;
 
 public class TeacherWindow extends AbstractWindow {
 	
 	private TeacherWindowMenu windowMenu;
 	private PreviewPanel previewPanel;
 	
 	public TeacherWindow() {
 		super("Teacher");
 		
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public void init() {
 		this.panels = new ArrayList<AbstractPanel>();
 		windowMenu = new TeacherWindowMenu();		
 		model = new TeacherModel();
 		controller = new TeacherController();
 		controller.addModel(model);
 		
 		setJMenuBar(windowMenu);
 		
 		previewPanel = new PreviewPanel((TeacherModel)model, (TeacherController)controller);
 		JPanel panel2 = new JPanel();
 		JPanel panel1 = new JPanel();
 		panel1.setLayout(new FlowLayout());
 		panel1.add(previewPanel.getView());
 		
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
 				panel2, panel1);
 		
 		this.addPanel(previewPanel);
 		this.add(splitPane);
 	}
 
 	@Override
 	public void dispose() {
 		
 		System.exit(0);
 	}
 
 }
