 package kea.kme.pullpit.client.UI;
 
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class UIAdmin implements EventHandler{
 
 	private Button adminPullAllButton;
 	private Button adminCreateUserButton;
 	private Button adminPullBandsButton;
 	private Button adminPullAgentsButton;
 	private Button adminPullContactsButton;
 	
 	private TextBox userTextBox;
 	private TextBox passwordTextBox;
 
		
 	public UIAdmin(){	
 		adminPullAllButton = new Button("PullAll");
 		adminCreateUserButton = new Button("CreateUser");
 		adminPullBandsButton = new Button("PullBands");
 		adminPullAgentsButton = new Button("PullAgents");
 		adminPullContactsButton = new Button("PullContacts");
 		
 		userTextBox = new TextBox();
 		passwordTextBox = new TextBox();
 		userTextBox.setText("Username goes here");
 		passwordTextBox.setText("Password goes here");
 		
 		addActionHandlers();
 				
 		UIMain.getInstance().getContentPanel().add(adminPullAllButton);
 		UIMain.getInstance().getContentPanel().add(adminCreateUserButton);
 		UIMain.getInstance().getContentPanel().add(adminPullBandsButton);
 		UIMain.getInstance().getContentPanel().add(adminPullAgentsButton);
 		UIMain.getInstance().getContentPanel().add(adminPullContactsButton);
 		UIMain.getInstance().getContentPanel().add(userTextBox);
 		UIMain.getInstance().getContentPanel().add(passwordTextBox);
 		
 	}
 	
 	public void addActionHandlers(){
 		adminPullAllButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				UIMain.getInstance().getContentPanel().clear();
 				UIMain.getInstance().getContentPanel()
 						.add(new Label("pulling ALL - IT'S OVER 9000!"));
 			}
 		});
 		
 		adminCreateUserButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				UIMain.getInstance().getContentPanel().clear();
 				UIMain.getInstance().getContentPanel()
 						.add(new Label("Creating user"));
 			}
 		});
 		adminPullBandsButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				UIMain.getInstance().getContentPanel().clear();
 				UIMain.getInstance().getContentPanel()
 						.add(new Label("Pulling bands"));
 				
 			}
 		});
 		adminPullAgentsButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				UIMain.getInstance().getContentPanel().clear();
 				UIMain.getInstance().getContentPanel()
 						.add(new Label("Pulling agents"));
 			}
 		});
 		adminPullContactsButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				UIMain.getInstance().getContentPanel().clear();
				UIMain.getInstance().getContentPanel()
						.add(new Label("pulling contacts"));
 			}
 		});
 	}
 }
 
