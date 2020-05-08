 package presentation.view;
 
 import java.awt.BorderLayout;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JPanel;
 
 import presentation.model.ModelController;
 import presentation.model.TabUserModel;
 
 public class TabUserPanel extends JPanel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 	private TabUserModel model;
 	private ModelController controller;
	private ActionUserPanel action_user_panel;
 
 	public TabUserPanel(ModelController controller) {
 		setLayout(new BorderLayout());
 		
 		model = controller.usertab_model;
 		model.addObserver(this);
 
 		this.controller = controller;
 
 		initContentPane();
 		initActionPanel();
 	}
 
 	private void initActionPanel() {
		action_user_panel = new ActionUserPanel(controller);
 		add(action_user_panel, BorderLayout.EAST);
 	}
 
 	private void initContentPane() {
 
 	}
 
 	public void update(Observable o, Object arg) {
 	
 	}
 }
