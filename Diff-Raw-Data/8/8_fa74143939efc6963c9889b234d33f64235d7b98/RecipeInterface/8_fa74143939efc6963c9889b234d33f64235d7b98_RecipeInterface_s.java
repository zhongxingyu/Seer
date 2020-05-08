 package net.davidjkelley.recipes.client;
 
 import java.util.ArrayList;
 import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class RecipeInterface extends VerticalPanel {
 	//String recipe;
 	MainInterface mainInterface;
 	TabLayoutPanel tabPanel;
 	DialogBox content;
 	Recipe recipe;
 	ArrayList<ProcessInterface> processes = new ArrayList<ProcessInterface>();
 	
 	public RecipeInterface(Recipe passedRecipe, MainInterface passedMainInterface) {
 		this.recipe = passedRecipe;
 		this.mainInterface = passedMainInterface;
 		
 		int n = 4;
 		for (int j = 0; j < n; j++) {
 		processes.add(new ProcessInterface());
 		}
 		init();
 	}
  public void init() {
 	 tabPanel = new TabLayoutPanel(2.2, Unit.EM);
 	 tabPanel.setAnimationDuration(500);
 	 for (int i=0;i<processes.size();i++) {
 		 tabPanel.add(processes.get(i), Integer.toString(i+1) + ". " + processes.get(i).getName());
 	 }
 	 
 	 tabPanel.setSize("50em", "50em");
 	 this.add(tabPanel);
  }
 
 }
