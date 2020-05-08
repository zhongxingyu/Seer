 package net.davidjkelley.recipes.client;
 
 import java.util.ArrayList;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class MainInterface extends VerticalPanel {
 	RecipeInterface recipeInterface;
 	//Button recipeSaveButton = new Button("Save Recipe");
 	Label selectRecipeLabel = new Label("Select Recipe: ");
 	String[] toBeDBisfied = {"one", "two", "three"};
 	EasyListBox<Recipe> recipeDropDown = new EasyListBox<Recipe>();
 	Button addRecipeButton = new Button("+");
 	FlexTable recipeFlexTable = new FlexTable();
 	
 	public MainInterface() {
 		//TODO - Update this to a dB interaction rather than a for loop of junk strings
 		int j = 0;
 		while (j < 5) {
 			String[] ingredients = {"One", "Two", "Count: " + Integer.toString(j)};
 			ArrayList<Process> processes = new ArrayList<Process>();
 			int i = 0;
 			while (i < 3) {
 			Process p = new Process();
 			processes.add(p);
 			i++;
 			}
 			Recipe r = new Recipe(ingredients, processes, "Title" + Integer.toString(j));
 			recipeDropDown.addItem(r.getTitle(), "lol", r);
 			j++;
 		}
 		
 		recipeDropDown.setSelectedIndex(0);		
 		//TODO passing MainInterface twice? hella resource wasteful
 		
 
 		
 		HorizontalPanel topInterface = new HorizontalPanel();
 		topInterface.setVerticalAlignment(ALIGN_MIDDLE);
 		recipeFlexTable.setWidget(0, 0, topInterface);
 		topInterface.add(selectRecipeLabel);
 		topInterface.add(recipeDropDown);
 		topInterface.add(addRecipeButton);
 		addRecipeButton.addClickHandler(new AddRecipeListener());
        // recipeSaveButton.addClickHandler(new SaveRecipeListener());   
 
 		
 		recipeInterface = new RecipeInterface(recipeDropDown.getSelectedObject(), this);
 		recipeDropDown.addChangeHandler(new RecipeDropDownHandler());
 		recipeFlexTable.setWidget(1,0,recipeInterface);
 		recipeFlexTable.getFlexCellFormatter().setColSpan(1, 0, 3);
 		this.add(recipeFlexTable);
 	}
 	
 //	public class RecipeDropDownListener implements ClickHandler {
 //		public void onClick(ClickEvent e) {
 //			//TODO - will eventually return the name of the recipe and populate the recipe interface
 //			System.out.println(e.getSource().toString());
 //		}
 //	}
 	
 	public class AddRecipeListener implements ClickHandler {
 		public void onClick (ClickEvent e) {
 			final DialogBox addNewRecipeDialogBox = new DialogBox(true);
 			//addNewRecipeDialogBox.setSize("500px", "500px");
 			//addNewRecipeDialogBox.setText("New Recipe");
 			FlexTable newRecipeFlexTable = new FlexTable();
 			
 			final TextBox newRecipeTextBox = new TextBox();
 			
 			int row = 0;
 			
 			//newRecipeFlexTable.setText(row, 0, "Name");
 			newRecipeFlexTable.setWidget(row, 0, newRecipeTextBox);
 			newRecipeFlexTable.getFlexCellFormatter().setColSpan(row, 0, 2);
 
 			Button cancel = new Button("Cancel");
 			Button okay = new Button("Save");
 			cancel.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent e) {
 					//TODO - make better memory management
 					addNewRecipeDialogBox.hide();
 				}
 			});
 			
 			okay.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent e) {
 					if (newRecipeTextBox.getText().length() >= 1){
 						
 //						//TODO - memory mgmt as well as dB interaction needs
 //						//to be implement
 						System.out.println("Closed with a save of: " + newRecipeTextBox.getText());
 						addNewRecipeDialogBox.hide();
 					}
 					else {
 						return;
 					}
 					}	
 			});
 			row++;
 			newRecipeFlexTable.setWidget(row, 0, cancel);
 			newRecipeFlexTable.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER,
 					HasVerticalAlignment.ALIGN_MIDDLE);
 			newRecipeFlexTable.setWidget(row, 1, okay);
 			newRecipeFlexTable.getFlexCellFormatter().setAlignment(row, 1, HasHorizontalAlignment.ALIGN_CENTER,
 					HasVerticalAlignment.ALIGN_MIDDLE);
 			addNewRecipeDialogBox.setText("New Recipe");
 			//DecoratorPanel dP = new DecoratorPanel();
 			addNewRecipeDialogBox.setWidget(newRecipeFlexTable);
 			addNewRecipeDialogBox.setAnimationEnabled(true);
 			addNewRecipeDialogBox.setGlassEnabled(true);
 			addNewRecipeDialogBox.center();
 			
 		}
 	}
 	
 	public class RecipeDropDownHandler implements ChangeHandler {
 		public void onChange(ChangeEvent e) {
 			//TODO implement full recipe object in dropdown
 			int location = (recipeDropDown.getSelectedIndex());
 			//String recipe = (recipeDropDown.getItemText(location));
 			recipeInterface = new RecipeInterface(recipeDropDown.getSelectedObject(), MainInterface.this);
 		}
 	}
 	
 //	public class SaveRecipeListener implements ClickHandler {
 //		@Override
 //		public void onClick(ClickEvent event) {
 //			System.out.println("Save Recipe button clicked");
 //			
 //		}
 //	}
 //	public class CheckBoxListener implements ItemListener  {
 //		
 //		   public void itemStateChanged(ItemEvent e) {
 //			   System.out.println("Changed");
 //		   }
 //		
 //	}
 }
