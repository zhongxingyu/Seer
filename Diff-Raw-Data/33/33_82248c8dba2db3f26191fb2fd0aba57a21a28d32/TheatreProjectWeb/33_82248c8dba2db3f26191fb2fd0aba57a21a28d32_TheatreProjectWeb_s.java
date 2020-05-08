 package theatreProject.client;
 
 import java.util.ArrayList;
 import com.google.gwt.core.client.EntryPoint;
 import theatreProject.domain.shared.InventoryObject;
 import theatreProject.shared.FieldVerifier;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 
 
 public class TheatreProjectWeb implements EntryPoint {
 
 	/**
 	 * This is the entry point method.
 	 */
 	
 	public void onModuleLoad() {
 
 		// Use RootPanel.get() to get the entire body element
 		final RootPanel rootPanel = RootPanel.get();
 		rootPanel.setStyleName("gwt-Root");
 		
 		//mainPanel is the primary panel we put stuff in
 		final VerticalPanel mainPanel = new VerticalPanel();
 		rootPanel.add(mainPanel, 10, 10);
 		mainPanel.setSize("430px", "280px");
 		
 		//initializers of labels
 		final Label title = new Label("Theatre Web Application");
 		title.setStyleName("gwt-Header");
 		mainPanel.add(title);
 		title.setSize("338px", "25px");
 		
 		final Label lblRules = new Label("And here are our rules.");
 		mainPanel.add(lblRules);
 		
 		//panel to hold the search bar and button
 		final HorizontalPanel searchBarPanel = new HorizontalPanel();
 		mainPanel.add(searchBarPanel);
 		searchBarPanel.setSize("284px", "34px");
 		
 		//search bar
 		final TextBox searchParameters = new TextBox();
 		searchParameters.setText("Enter search terms here");
 		searchBarPanel.add(searchParameters);
 		
 		//primary search button
 		final Button btnSearch = new Button("Search");
 		searchBarPanel.add(btnSearch);
 		
 		
 		//panel to hold the view all and view checked out buttons
 		final HorizontalPanel searchAllPanel = new HorizontalPanel();
 		mainPanel.add(searchAllPanel);
 		
 		//button for viewing all items
 		final Button btnViewAll = new Button("View All");
 		searchAllPanel.add(btnViewAll);
 		
 		//button to view all checkout out items
 		final Button btnViewCheckedOut = new Button("View Checked Out");
 		searchAllPanel.add(btnViewCheckedOut);
 		
 		//an empty panel which will get the search results put into it
 		final VerticalPanel searchResultsPanel = new VerticalPanel();
 		mainPanel.add(searchResultsPanel);
 		
 		final Button btnManageUsers = new Button("Manage Users");
 		mainPanel.add(btnManageUsers);
 		
 		
 		final Label lblContactInfo = new Label("Please contact Terena is you have any questions.");
 		mainPanel.add(lblContactInfo);
 		
 
 		//button events
 		// TODO
 		btnSearch.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 			//	ArrayList<InventoryObject> found;
 			//	String parameters = searchParameters.getText();
				//found = inventory.search(parameters);
 				//pass found to some displayObjects method, which would grab the parameters
 				    //we want to show for the item and put it into the searchResultsPanel,
 					//and divide the panel up so it's easy on the eyes.
 				
 			}
 		});
 		
 		btnViewAll.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				//pass the parameter database from our inventory into displayObjects
 					//method from above
 			}
 		});
 		
 		btnViewCheckedOut.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				//ArrayList<InventoryObject> found = inventory.checkOutList();
 				//pass the parameter found to the displayObjects method
 			}
 		});
		
 		btnManageUsers.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 			}
 		});
 	}
 }
