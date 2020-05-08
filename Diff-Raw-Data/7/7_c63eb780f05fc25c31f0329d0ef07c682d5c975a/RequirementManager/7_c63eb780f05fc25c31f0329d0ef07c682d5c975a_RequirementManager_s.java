 package edu.wpi.cs.wpisuitetng.modules.RequirementManager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 
 import edu.wpi.cs.wpisuitetng.janeway.modules.IJanewayModule;
 import edu.wpi.cs.wpisuitetng.janeway.modules.JanewayTabModel;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.ToolbarView;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.MainView;
 
 
 public class RequirementManager implements IJanewayModule {
 
<<<<<<< HEAD
 	private List<JanewayTabModel> tabs;
	
=======
	List<JanewayTabModel> tabs;
 
>>>>>>> developer-comm
 	// Constructor
 	public RequirementManager() {
 		tabs = new ArrayList<JanewayTabModel>();
 
 		ToolbarView toolBar = new ToolbarView();
 		MainView mainPanel = new MainView();
 
 		// Create a tab model that contains the toolbar panel and the main content panel
 		JanewayTabModel tab1 = new JanewayTabModel(getName(), new ImageIcon(), toolBar, mainPanel);
 
 		// Add the tab to the list of tabs owned by this module
 		tabs.add(tab1);
 	}
 	
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return "RequirementManager";
 	}
 
 	@Override
 	public List<JanewayTabModel> getTabs() {
 		// TODO Auto-generated method stub
 		return tabs;
 	}
 
 }
