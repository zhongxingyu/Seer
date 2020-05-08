 package kea.kme.pullpit.client.UI;
 
 import java.util.ArrayList;
 
 import kea.kme.pullpit.client.objects.PullPitUser;
 import kea.kme.pullpit.client.services.AdminService;
 import kea.kme.pullpit.client.services.AdminServiceAsync;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CaptionPanel;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * @author Emil Thorenfeldt
  * 
  */
 public class UIAdmin implements EventHandler {
 
 	// Declaring buttons
 	private Button adminPullAllButton, adminGetTokenButton;
 
 	// Declaring panels
 	private DisclosurePanel userPanel;
 	private VerticalPanel adminPanel;
 
 	// Declaring flextable for users
 	private FlexTable userTable;
 	private FlexTable importTable;
 	
 	private TextBox userIdBox;
 
 	// Declaring an AdminService Async callback
 	private AdminServiceAsync asasync;
 //	private int[] pulls;
 
 	// Declaring a PopupPanel
 	private PopupPanel myPop;
 	
 //	private PullPitMessages messages = GWT.create(PullPitMessages.class);
 
 	// Declaring and initializing PullPit constants to be able to reference them
 	// in the code
 	private PullPitConstants constants = GWT.create(PullPitConstants.class);
 	
 	private final int REFRESH_INTERVAL = 500;
 	private Timer timer;
 	private Label importLabel;
 
 	private CaptionPanel importPanel;
 
 	/**
 	 * The constructor which makes the admin view. Gives the options to see
 	 * authentication key, repopulate the database, and table which contains the
 	 * users currently in the system, with the possibility to create or delete
 	 * user.
 	 */
 	public UIAdmin() {
 		//Initializing buttons
 		adminPullAllButton = new Button(constants.pullAll());
 		adminGetTokenButton = new Button(constants.showToken());
 		adminPullAllButton.setStyleName("brownButton");
 		adminGetTokenButton.setStyleName("brownButton");
 
 		//Initializing userTable
 		userTable = new FlexTable();
 		userTable.addStyleName("dataTable");
 		userPanel = new DisclosurePanel(constants.users());
 		userPanel.setStyleName("caption");
 		userPanel.add(userTable);
 		
 		//initializing asynchronous service
 		asasync = GWT.create(AdminService.class);
 		
 		//adding widgets to panel
 		adminPanel = new VerticalPanel();
 		adminPanel.add(adminPullAllButton);
 		adminPanel.add(adminGetTokenButton);
 		adminPanel.add(userPanel);
 
 		//Adding actionhandler to panel
 		userPanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
 			
 			@Override
 			public void onOpen(OpenEvent<DisclosurePanel> event) {
 				//Initializing asynchronous callback with type PullPitUser
 				AsyncCallback<PullPitUser[]> callback = new AsyncCallback<PullPitUser[]>() {
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						
 						Window.alert(constants.getUserError()+caught.getMessage());
 					}
 
 					@Override
 					public void onSuccess(PullPitUser[] result) {
 						//calls method to populate the cells in the userTable
 						populateUserTable(result);
 
 					}
 
 				};
 				asasync.getUsers(callback);
 			}
 		});
 		
 		addActionHandlers();
 		UIMain.getInstance().changeContentTo(adminPanel);
 
 	}
 
 	/**
 	 * population the userTable with the result array of PullPitUsers
 	 * Adding a callback which deletes a user, and removes it from the table
 	 * @param result
 	 */
 	private void populateUserTable(PullPitUser[] result) {
 		
 		//Filling out userTable with user fields
 		final ArrayList<PullPitUser> userList = new ArrayList<PullPitUser>();
 		for (PullPitUser ppu : result)
 			userList.add(ppu);
 		userTable.clear();
 		userTable.setText(0, 0, constants.userID());
 		userTable.setText(0, 1, constants.emailAdd());
 		userTable.setText(0, 2, constants.userName());
 		userTable.setText(0, 3, constants.delete());
 		int i;
 		//Setting the css id of the specific row
 		for (i = 1; i <= result.length; i++) {
 			if (i % 2 == 0) {
 				userTable.getRowFormatter().addStyleName(i,
 						"tableRowColorBrown");
 			} else {
 				userTable.getRowFormatter().addStyleName(i,
 						"tableRowColorWhite");
 			}
 			//Setting text in the each cell per row, to contain the current user object
 			final PullPitUser u = result[i - 1];
 			String userID = "-";
 			if (u.getUserID() != 0)
 				userID = "" + u.getUserID();
 			userTable.setText(i, 0, userID);
 			userTable.setText(i, 1, u.getEmailAdd());
 			userTable.setText(i, 2, u.getUserName());
 			Button removeButton = new Button("X");
 			removeButton.setStyleName("brownButton");
 			final int j = i;
 			//Adding clickhandler to the removebutton
 			removeButton.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					AsyncCallback<Void> callback = new AsyncCallback<Void>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 							Window.alert(constants.errorRemoveUser()+caught.getMessage());
 						}
 
 						@Override
 						public void onSuccess(Void nothing) {
 							//Removing the row from table array and repopulating table with revised content
 							userList.remove(j - 1);
 							populateUserTable(userList
 									.toArray(new PullPitUser[userList.size()]));
 						}
 
 					};
 					//Making a async call to remove the user from the user relation in the database
 					asasync.removeUser(u.getUserName(), callback);
 
 				}
 			});
 			//Setting the index of the removeButton
 			userTable.setWidget(i, 3, removeButton);
 		}
 		//Adding the add-button
 		userIdBox = new TextBox();
 		final TextBox emailAddBox = new TextBox();
 		final TextBox userNameBox = new TextBox();
 		Button addButton = new Button(constants.add());
 		addButton.setStyleName("brownButton");
 		
 		//Setting actionhanler on the Button element
 		addButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				Integer userID;
 				//testing to set userID, else its set to 0
 				if (userIdBox.getText().equals("")
 						|| userIdBox.getText() == null) {
 					userID = 0;
 					//validating the input
 				} else {
 					userID = FieldVerifier.verifyInt(userIdBox.getText());
 				}
 				//checking for invalid userID
 				if (userID == -1) {
 					Window.alert(constants.invalidUserID());
 				}
 				//creating new PullPitUser object
 				else {
 					String emailAdd = emailAddBox.getText();
 					String userName = userNameBox.getText();
 					PullPitUser newUser = new PullPitUser(userID, emailAdd,
 							userName);
 					//Initializing callback
 					AsyncCallback<PullPitUser[]> callback = new AsyncCallback<PullPitUser[]>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 							Window.alert(constants.errorAddingUser()+caught.getMessage());
 						}
 
 						@Override
 						public void onSuccess(PullPitUser[] result) {
 							//Recreating userTable
 							populateUserTable(result);
 						}
 					};
 					//Making a callback which add the user to the database
 					asasync.addUser(newUser, callback);
 				}
 			}
 		});
 		//Setting widgets of the table
 		userTable.setWidget(i, 0, userIdBox);
 		userTable.setWidget(i, 1, emailAddBox);
 		userTable.setWidget(i, 2, userNameBox);
 		userTable.setWidget(i, 3, addButton);
 	}
 
 	/**
 	 * Setting actionhandlers on buttons
 	 */
 	public void addActionHandlers() {
 		adminPullAllButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {				
 				importLabel = new Label(constants.importing());
 				importTable = new FlexTable();
 				importTable.getColumnFormatter().setWidth(0, "125px");
 				importTable.setText(0, 0, constants.type());
 				importTable.setText(0, 1, constants.count());
 				importTable.setText(1, 0, constants.shows());
 				importTable.setText(2, 0, constants.bands());
 				importTable.setText(3, 0, constants.venues());
 				importTable.setText(4, 0, constants.showVenues());
 				importTable.setText(5, 0, constants.agents());
 				importTable.setText(6, 0, constants.bookers());
 				importTable.setText(7, 0, constants.contacts());
 				
 				myPop = new PopupPanel();
 				myPop.setAutoHideEnabled(true);
 				myPop.setStyleName("resultPop");
 				
 				importPanel = new CaptionPanel();
 				importPanel.setStyleName("capPanel");
 				importPanel.setWidth("200px");
				VerticalPanel tempPanel = new VerticalPanel();
				tempPanel.add(importLabel);
				tempPanel.add(importTable);
				importPanel.add(tempPanel);
 				myPop.add(importPanel);
 				myPop.show();
 				AsyncCallback<Void> truncateCallback = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert(constants.unexpectedError());
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						AsyncCallback<Void> importCallback = initImportCallback();
 						asasync.pullViaBackend(importCallback);
 					}
 				};
 				asasync.truncateAll(truncateCallback);
 			}
 		});
 		
 		//Adding clickhandler to getTokenButton
 		adminGetTokenButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				AsyncCallback<String> callback = new AsyncCallback<String>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert(constants.errorGettingToken()+caught.getMessage());
 					}
 
 					@Override
 					public void onSuccess(String result) {
 						//Showing a popup with the recieved token from the callback
 						PopupPanel tokenPopup = new PopupPanel();
 						tokenPopup.setStyleName("authPop");
 						tokenPopup.setAutoHideEnabled(true);
 						tokenPopup.add(new Label(result));
 						tokenPopup.show();
 					}
 				};
 				//Getting token
 				asasync.getToken(callback);
 			}
 		});
 	}
 	
 	private AsyncCallback<Void> initImportCallback() {
 		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				startRefreshing();
 			}
 		}; 
 		return callback;
 	}
 
 	private AsyncCallback<int[]> getStatusCallback() {
 		AsyncCallback<int[]> callback = new AsyncCallback<int[]>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(int[] result) {
 				for (int i = 0; i < result.length-1; i++) {
 					importTable.setText(i+1, 1, getStatusFromInt(result[i]));
 				}
 				updateOverallStatus(result[result.length-1]);
 			}
 		};
 		return callback;
 	}
 	
 	private void updateOverallStatus(int status) {
 		if (status==0) {
 			importLabel.setText(constants.importSuccess());
 			timer.cancel();
 		} else {
 			importLabel.setText(constants.importing());
 		}
 		
 	}
 
 	private String getStatusFromInt(int status) {
 		if (status==-1) {
 			return constants.inProgress();
 		} else if (status==0) {
 			return constants.waiting();
 		} else {
 			return status+"";
 		}
 	}
 
 	private void startRefreshing() {
 		timer = new Timer() {
 			
 			@Override
 			public void run() {
 				updateStatus();
 			}
 		};
 		
 		timer.scheduleRepeating(REFRESH_INTERVAL);
 	}
 	
 	private void updateStatus() {
 		AsyncCallback<int[]> callback = getStatusCallback();
 		asasync.getImportStatus(callback);
 	}
 }
